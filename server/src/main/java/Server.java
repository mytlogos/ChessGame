import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 *
 */
public class Server extends Thread {
    private final static Set<String> idlePlayers = Collections.synchronizedSet(new HashSet<>());
    private final static Set<String> gameHosts = Collections.synchronizedSet(new HashSet<>());
    private final static Map<String, InGame> inGameMap = Collections.synchronizedMap(new HashMap<>());
    private final static Map<String, ServerSocketWrapper> nameWrapperMap = Collections.synchronizedMap(new HashMap<>());
    private final static Map<ServerSocketWrapper, String> wrapperNameMap = Collections.synchronizedMap(new HashMap<>());
    private static int port = 4445;
    private static Logger logger;
    private static Server server;

    private final ServerSocket socket;
    private final Thread queueManager;
    private final int serverLimit = 500;
    private final Collection<ClientHandler> handlers = Collections.synchronizedList(new ArrayList<>(serverLimit));
    private final Queue<Socket> queuedSockets = new ArrayDeque<>();
    private boolean isShutDown = false;

    private Server() throws IOException {
        socket = new ServerSocket(port);
        queueManager = new Thread(new QueueManager());
        queueManager.setDaemon(true);
        queueManager.start();
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

    private static void startServer() throws IOException {
        server = new Server();
        server.start();
    }

    @Override
    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, e) -> logger.log(SEVERE, "Server crashed" + e));
        Thread.currentThread().setName("ChessGame-Server");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket clientSocket = socket.accept();

                if (handlers.size() < serverLimit) {
                    ClientHandler handler = new ClientHandler(clientSocket);
                    handlers.add(handler);
                } else {
                    queuedSockets.add(clientSocket);
                }

            } catch (IOException e) {
                logger.log(SEVERE, "error occurred on server", e);
                shutDownServer();
            }
        }
    }

    private static synchronized void shutDownServer() {
        if (!server.isShutDown) {

            synchronized (server.handlers) {
                for (ClientHandler handler : server.handlers) {
                    handler.logOut();
                }
            }

            server.queueManager.interrupt();
            server.interrupt();
            try {
                server.socket.close();
            } catch (IOException e) {
                logger.log(SEVERE, "error in closing socket", e);
            }

            server.isShutDown = true;
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

            synchronized (nameWrapperMap) {
                while (nameWrapperMap.keySet().contains(name)) {
                    counter++;
                    name = guest + counter;
                }
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
                    if (wrapper.isPlayerLogin(line)) {
                        logIn(line);

                    } else if (wrapper.isChat(line)) {
                        propagateMessage(line);

                    } else if (wrapper.isHosting(line)) {
                        propagateHosting();

                    } else if (wrapper.isAccepting(line)) {
                        acceptGame(line);

                    } else if (wrapper.isHostingTerminated(line)) {
                        terminateHosting();

                    } else if (wrapper.isLogOut(line)) {
                        logOut();

                    } else if (wrapper.isMove(line)) {
                        propagateMove(line);

                    } else if (wrapper.isShutDown(line)) {
                        shutDownServer();

                    } else if (wrapper.isRename(line)) {
                        propagateNewName(line);

                    } else {
                        logger.log(INFO, "Unknown Message received on Server: " + line);
                    }
                }
            } catch (IOException e) {
                //log out wrapper on exception
                logOut();
                throw new RuntimeException(e);
            }
        }

        private void propagateNewName(String line) {
            String newName = wrapper.getNewName(line);
            String oldName = wrapperNameMap.remove(wrapper);

            nameWrapperMap.remove(oldName);
            nameWrapperMap.put(newName, wrapper);

            InGame inGame = inGameMap.remove(oldName);

            if (inGame != null) {
                if (inGame.player1.equals(oldName)) {
                    inGame.player1 = newName;
                } else if (inGame.player2.equals(oldName)) {
                    inGame.player2 = newName;
                }

                inGameMap.put(newName, inGame);
            }

            if (gameHosts.remove(oldName)) {
                gameHosts.add(newName);
            }

            if (idlePlayers.remove(oldName)) {
                idlePlayers.add(newName);
            }

            synchronized (wrapperNameMap) {
                for (ServerSocketWrapper socketWrapper : wrapperNameMap.keySet()) {
                    socketWrapper.writeOnlinePlayers(nameWrapperMap.keySet());
                    socketWrapper.writeHosts(gameHosts);
                    socketWrapper.writeInGameList(inGameMap.keySet());
                }
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

                synchronized (wrapperNameMap) {
                    for (ServerSocketWrapper socketWrapper : wrapperNameMap.keySet()) {
                        socketWrapper.writeOnlinePlayers(nameWrapperMap.keySet());
                        socketWrapper.writeHosts(gameHosts);
                        socketWrapper.writeInGameList(inGameMap.keySet());
                    }
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

            synchronized (nameWrapperMap) {
                for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                    socketWrapper.writeHosts(gameHosts);
                }
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

                String acceptingPlayer = wrapperNameMap.get(wrapper);
                ServerSocketWrapper hostWrapper = nameWrapperMap.get(acceptedHost);

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
                    gameHosts.remove(acceptingPlayer);

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
            //todo for the case if the other player never ends game for whatever reason, end it forcefully
            String playerName = wrapperNameMap.get(wrapper);

            InGame inGame = inGameMap.remove(playerName);

            if (inGame != null) {
                String player2 = inGame.player2;
                String player1 = inGame.player1;
                idlePlayers.add(player2);
                idlePlayers.add(player1);
                System.out.println("server: ending game between " + player1 + " and " + player2);

                inGameMap.remove(player2);
                inGameMap.remove(player1);

                synchronized (nameWrapperMap) {
                    for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                        socketWrapper.writeInGameList(inGameMap.keySet());
                    }
                }
            }
        }

        private synchronized void terminateHosting() {
            String terminator = wrapperNameMap.get(wrapper);

            gameHosts.remove(terminator);

            synchronized (nameWrapperMap) {
                for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                    socketWrapper.writeHosts(gameHosts);
                }
            }
        }

        private void logOut() {
            String player = wrapperNameMap.remove(wrapper);
            nameWrapperMap.remove(player);
            idlePlayers.remove(player);
            gameHosts.remove(player);

            InGame game = inGameMap.remove(player);

            if (game != null) {
                String opponent = game.player1.equals(player) ? game.player2 : game.player1;
                ServerSocketWrapper wrapper = nameWrapperMap.get(opponent);

                if (wrapper != null) {
                    wrapper.interruptGame();
                }
            }

            synchronized (nameWrapperMap) {
                for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                    socketWrapper.writeOnlinePlayers(nameWrapperMap.keySet());
                }
            }

            server.handlers.remove(this);

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

            if (wrapper.isGameEnd(line)) {
                endGame();
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

    private class QueueManager implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (!queuedSockets.isEmpty()) {
                    Socket socket = queuedSockets.poll();
                    try {
                        ClientHandler handler = new ClientHandler(socket);
                        handlers.add(handler);
                    } catch (IOException e) {
                        logger.log(SEVERE, "error occurred while handling socket", e);
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
