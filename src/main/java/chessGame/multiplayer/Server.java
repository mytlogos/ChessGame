package chessGame.multiplayer;

import chessGame.mechanics.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;

/**
 *
 */
public class Server extends Thread {
    private static Set<String> idlePlayers = new HashSet<>();
    private static List<String> gameHosts = new ArrayList<>();
    private static Map<String, InGame> inGameMap = new HashMap<>();
    private static Map<String, SocketWrapper> nameWrapperMap = new HashMap<>();
    private static Map<SocketWrapper, String> wrapperNameMap = new HashMap<>();
    private ServerSocket socket;

    private Server() throws IOException {
        socket = new ServerSocket(PlayerClient.port);
        setDaemon(true);
    }

    public static Server startServer() throws IOException {
        Server server = new Server();
        server.start();
        return server;
    }

    @Override
    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, e) -> System.err.println("Server crashed: " + e.getMessage()));
        Thread.currentThread().setName("ChessGame-Server");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                System.out.println("isAwaiting" + " Time: " + LocalTime.now());
                Socket client = socket.accept();
                new ClientHandler(client);
                System.out.println("Started Listening to Socket " + client);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void finalize() {
        System.out.println("finalized?");
    }

    private static class ClientHandler extends Thread {
        private static int counter = 0;
        SocketWrapper wrapper;

        private ClientHandler(Socket socket) throws IOException {
            wrapper = new SocketWrapper(socket);
            wrapper.writeGuestName(allocateGuestName());
            setDaemon(true);
            start();
        }

        private String allocateGuestName() {
            String guest = "Guest";
            int counter = 1;
            String name = guest + counter;

            while (nameWrapperMap.keySet().contains(name)) {
                counter++;
                name = guest + counter;
            }
            return name;
        }

        @Override
        public void run() {
            setName("ClientHandler-" + counter++);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(wrapper.getInputStream()))) {
                String line;

                //listen as long as it does not read null or thread is interrupted
                while (!Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
                    System.out.println(Thread.currentThread().getName() + " read: " + line);

                    if (wrapper.isPlayerLogin(line)) {
                        logIn(line);

                    } else if (wrapper.isChat(line)) {
                        propagateMessage(line);

                    } else if (wrapper.isHosting(line)) {
                        propagateHosting();

                    } else if (wrapper.isAccepting(line)) {
                        acceptGame(line);

                    } else if (wrapper.isGameEnd(line)) {
                        endGame();

                    } else if (wrapper.isHostingTerminated(line)) {
                        terminateHosting();

                    } else if (wrapper.isLogOut(line)) {
                        logOut();

                    } else if (wrapper.isMove(line)) {
                        propagateMove(line);

                    } else {
                        System.err.println("Unknown Message received on Server: " + line + " Time: " + LocalTime.now());
                    }
                }

                System.out.println("listening on server side stopped" + " Time: " + LocalTime.now());
                System.out.println("interrupted: " + Thread.currentThread().isInterrupted());
            } catch (IOException e) {
                //log out wrapper on exception
                logOut();
                throw new RuntimeException(e);
            }
        }

        private void logIn(String line) {
            String name = wrapper.getPlayer(line).getName();

            if (nameWrapperMap.containsKey(name)) {
                System.out.println("Server Side: name is already allocated");
            } else {
                nameWrapperMap.put(name, wrapper);
                wrapperNameMap.put(wrapper, name);
                idlePlayers.add(name);

                for (SocketWrapper socketWrapper : nameWrapperMap.values()) {
                    socketWrapper.writeOnlinePlayers(nameWrapperMap.keySet());
                }
            }
        }

        private void propagateMessage(String line) {
            Chat.Message message = wrapper.getMessage(line);
            String player = wrapperNameMap.get(wrapper);

            InGame inGame = inGameMap.get(player);

            //if player is not in a game write to all Chat, else to game chat
            if (inGame == null) {
                for (String nonPlayingPlayer : idlePlayers) {
                    nameWrapperMap.get(nonPlayingPlayer).writeMessage(message);
                }
            } else {
                String opponent = getOpponent(player, inGame);
                nameWrapperMap.get(opponent).writeMessage(message);
                wrapper.writeMessage(message);
            }
        }

        private void propagateHosting() {
            String hostingPlayer = wrapperNameMap.get(wrapper);

            if (hostingPlayer != null) {
                gameHosts.add(hostingPlayer);
            } else {
                System.err.println("Missing Name!");
            }

            System.out.println("Player " + hostingPlayer + " started hosting");

            for (SocketWrapper socketWrapper : nameWrapperMap.values()) {
                socketWrapper.writeHosts(gameHosts);
            }
        }

        private synchronized void acceptGame(String line) {
            String acceptedHost = wrapper.getAcceptedHost(line);

            if (gameHosts.contains(acceptedHost)) {

                if (inGameMap.containsKey(acceptedHost)) {
                    System.err.println("Host is already playing");
                    wrapper.writeStartFailed();

                    gameHosts.remove(acceptedHost);

                    for (SocketWrapper socketWrapper : nameWrapperMap.values()) {
                        socketWrapper.writeHosts(gameHosts);
                    }
                    return;
                }

                SocketWrapper hostWrapper = nameWrapperMap.get(acceptedHost);
                String acceptingPlayer = wrapperNameMap.get(wrapper);

                if (hostWrapper == null || acceptingPlayer == null) {
                    System.err.println("Missing Data on Server. Game failed to start");
                    wrapper.writeStartFailed();
                } else {
                    InGame inGame = new InGame(acceptingPlayer, acceptedHost);
                    inGameMap.put(acceptedHost, inGame);
                    inGameMap.put(acceptingPlayer, inGame);

                    wrapper.writeStartGame(acceptedHost, Color.WHITE);
                    hostWrapper.writeStartGame(acceptingPlayer, Color.BLACK);

                    idlePlayers.remove(acceptedHost);
                    idlePlayers.remove(acceptingPlayer);

                    gameHosts.remove(acceptedHost);

                    for (SocketWrapper socketWrapper : nameWrapperMap.values()) {
                        socketWrapper.writeInGameList(inGameMap.keySet());
                        socketWrapper.writeHosts(gameHosts);
                    }
                }
            } else {
                wrapper.writeStartFailed();
            }

        }

        private void endGame() {
            String playerName = wrapperNameMap.get(wrapper);

            InGame inGame = inGameMap.remove(playerName);

            if (inGame == null) {
                System.out.println("Game already removed");
            } else {
                String player1 = inGame.player1;
                String player2 = inGame.player2;

                inGameMap.remove(player1);
                inGameMap.remove(player2);

                idlePlayers.add(player1);
                idlePlayers.add(player2);

                for (SocketWrapper socketWrapper : nameWrapperMap.values()) {
                    socketWrapper.writeInGameList(inGameMap.keySet());
                }
            }
        }

        private synchronized void terminateHosting() {
            String terminator = wrapperNameMap.get(wrapper);

            gameHosts.remove(terminator);

            for (SocketWrapper socketWrapper : nameWrapperMap.values()) {
                socketWrapper.writeHosts(gameHosts);
            }
        }

        private void logOut() {
            String player = wrapperNameMap.remove(wrapper);
            nameWrapperMap.remove(player);
            idlePlayers.remove(player);

            for (SocketWrapper socketWrapper : nameWrapperMap.values()) {
                socketWrapper.writeOnlinePlayers(nameWrapperMap.keySet());
            }

            Thread.currentThread().interrupt();
        }

        private void propagateMove(String line) {
            String ownPlayer = wrapperNameMap.get(wrapper);
            InGame inGame = inGameMap.get(ownPlayer);

            if (inGame == null) {
                System.err.println("Illegal State: Made Move on non existent game.");
            } else {
                String opponent = getOpponent(ownPlayer, inGame);
                nameWrapperMap.get(opponent).writeMove(line);
            }
        }

        private String getOpponent(String ownPlayer, InGame inGame) {
            return ownPlayer.equals(inGame.player1) ? inGame.player2 : inGame.player1;
        }

    }

    private static class InGame {
        private String player1;
        private String player2;

        private InGame(String player1, String player2) {
            this.player1 = player1;
            this.player2 = player2;
        }
    }
}
