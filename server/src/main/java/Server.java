import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 *
 */
public class Server extends Thread {
    private static int port = 4445;

    private static Set<String> idlePlayers = new HashSet<>();
    private static List<String> gameHosts = new ArrayList<>();
    private static Map<String, InGame> inGameMap = new HashMap<>();
    private static Map<String, ServerSocketWrapper> nameWrapperMap = new HashMap<>();
    private static Map<ServerSocketWrapper, String> wrapperNameMap = new HashMap<>();
    private static Logger logger;
    private ServerSocket socket;
    private static Server server;

    private Server() throws IOException {
        socket = new ServerSocket(port);
    }

    public static void main(String[] args) throws IOException {
        logger = Logger.getGlobal();
        logger.addHandler(new FileHandler("serverLog.log", true));

        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();

            try {
                Socket socket = new Socket(hostAddress, port);
                socket.close();
                logger.log(INFO, "Server already up");
            } catch (IOException e) {
                startServer();
            }
        } catch (IOException e) {
            logger.log(SEVERE, "error occurred while starting server", e);
        }
    }

    private static synchronized void shutDownServer() {
        if (!server.isInterrupted() && server.isAlive()) {

            for (ClientHandler handler : server.handlers) {
                handler.logOut();
            }

            server.interrupt();
        }
    }

    private static void startServer() throws IOException {
        server = new Server();
        server.start();
    }

    private Collection<ClientHandler> handlers = new ArrayList<>();

    @Override
    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, e) -> logger.log(SEVERE, "Server crashed" + e));
        Thread.currentThread().setName("ChessGame-Server");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket client = socket.accept();
                ClientHandler handler = new ClientHandler(client);
                handlers.add(handler);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ClientHandler extends Thread {
        private static int counter = 0;
        ServerSocketWrapper wrapper;

        private ClientHandler(Socket socket) throws IOException {
            wrapper = new ServerSocketWrapper(socket);
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

                    } else if (wrapper.isShutDown(line)) {
                        shutDownServer();

                    } else {
                        logger.log(INFO, "Unknown Message received on Server: " + line + "  at: " + LocalTime.now());
                    }
                }
            } catch (IOException e) {
                //log out wrapper on exception
                logOut();
                throw new RuntimeException(e);
            }
        }

        private void logIn(String line) {
            String name = wrapper.getPlayer(line);

            if (nameWrapperMap.containsKey(name)) {
                logger.log(SEVERE, "Name " + name + " is already allocated");
            } else {
                nameWrapperMap.put(name, wrapper);
                wrapperNameMap.put(wrapper, name);
                idlePlayers.add(name);

                for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                    socketWrapper.writeOnlinePlayers(nameWrapperMap.keySet());
                }
            }
        }

        private void propagateMessage(String message) {
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
                logger.warning("Missing Name for Wrapper");
            }

            for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                socketWrapper.writeHosts(gameHosts);
            }
        }

        private synchronized void acceptGame(String line) {
            String acceptedHost = wrapper.getAcceptedHost(line);

            if (gameHosts.contains(acceptedHost)) {

                if (inGameMap.containsKey(acceptedHost)) {
                    wrapper.writeStartFailed();

                    gameHosts.remove(acceptedHost);

                    for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                        socketWrapper.writeHosts(gameHosts);
                    }
                    return;
                }

                ServerSocketWrapper hostWrapper = nameWrapperMap.get(acceptedHost);
                String acceptingPlayer = wrapperNameMap.get(wrapper);

                if (hostWrapper == null || acceptingPlayer == null) {
                    logger.warning("Missing Data on Server. Game failed to start, Wrapper: " + hostWrapper + " acceptingPlayer: " + acceptingPlayer + " acceptedHost: " + acceptedHost);
                    wrapper.writeStartFailed();
                } else {
                    InGame inGame = new InGame(acceptingPlayer, acceptedHost);
                    inGameMap.put(acceptedHost, inGame);
                    inGameMap.put(acceptingPlayer, inGame);

                    wrapper.writeStartGame(acceptedHost, true);
                    hostWrapper.writeStartGame(acceptingPlayer, false);

                    idlePlayers.remove(acceptedHost);
                    idlePlayers.remove(acceptingPlayer);

                    gameHosts.remove(acceptedHost);

                    for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
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

            if (inGame != null) {
                String player1 = inGame.player1;
                String player2 = inGame.player2;

                inGameMap.remove(player1);
                inGameMap.remove(player2);

                idlePlayers.add(player1);
                idlePlayers.add(player2);

                for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                    socketWrapper.writeInGameList(inGameMap.keySet());
                }
            }
        }

        private synchronized void terminateHosting() {
            String terminator = wrapperNameMap.get(wrapper);

            gameHosts.remove(terminator);

            for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                socketWrapper.writeHosts(gameHosts);
            }
        }

        private void logOut() {
            String player = wrapperNameMap.remove(wrapper);
            nameWrapperMap.remove(player);
            idlePlayers.remove(player);

            for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                socketWrapper.writeOnlinePlayers(nameWrapperMap.keySet());
            }

            Thread.currentThread().interrupt();
        }

        private void propagateMove(String line) {
            String ownPlayer = wrapperNameMap.get(wrapper);
            InGame inGame = inGameMap.get(ownPlayer);

            if (inGame == null) {
                logger.warning("Illegal State: Made Move on non existent game.");
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
