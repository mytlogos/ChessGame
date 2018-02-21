package chessServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 *
 */
public class Server extends Thread {
    private static int port = 5000;
    private static Logger logger;
    private static Server server;
    private static int counter = 0;

    private final Set<String> idlePlayers = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> gameHosts = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, ClientHandler.InGame> inGameMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, ServerSocketWrapper> nameWrapperMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<ServerSocketWrapper, String> wrapperNameMap = Collections.synchronizedMap(new HashMap<>());

    private final ServerSocket socket;
    private boolean isShutDown = false;
    private boolean isReset = false;
    private final ExecutorService service = Executors.newFixedThreadPool(10, new HandlerThreadFactory());

    private Server() throws IOException {
        socket = new ServerSocket(port);
        setDaemon(false);
    }

    static Server getServer() {
        return server;
    }

    public static void main(String[] args) throws IOException {
        logger = Logger.getGlobal();
        logger.addHandler(new FileHandler("serverLog.log", true));
        startServer();
    }

    private static void startServer() {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();

            try {
                Socket socket = new Socket(hostAddress, port);
                socket.close();
                logger.log(INFO, "chessServer.Server already up");
            } catch (IOException e) {
                server = new Server();
                server.start();
            }
        } catch (IOException e) {
            logger.log(SEVERE, "error occurred while starting server", e);
        }
    }

    static synchronized void reset() {
        if (!server.isReset) {
            Thread thread = new Thread(() -> {
                shutDownServer();
                startServer();
            });
            thread.setDaemon(false);
            thread.setName("Resetter");
            thread.start();
        }
    }

    static synchronized void shutDownServer() {
        if (!server.isShutDown) {

            server.isShutDown = true;
            server.interrupt();

            try {
                server.socket.close();
            } catch (IOException e) {
                logger.log(SEVERE, "error in closing socket", e);
            }

            server.service.shutdownNow();
        }
    }

    @Override
    public void run() {
        setUncaughtExceptionHandler((thread, e) -> logger.log(SEVERE, "chessServer.Server crashed" + e));
        setName("ChessGame-chessServer.Server-" + counter++);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket clientSocket = socket.accept();
                System.out.println("accepted " + socket);
                service.execute(new ClientHandler(clientSocket));
            } catch (IOException e) {
                if (!this.isShutDown) {
                    logger.log(SEVERE, "error occurred on server", e);
                    shutDownServer();
                }
            }
        }
    }

    Set<String> getIdlePlayers() {
        return idlePlayers;
    }

    Set<String> getGameHosts() {
        return gameHosts;
    }

    Map<String, ClientHandler.InGame> getInGameMap() {
        return inGameMap;
    }

    Map<String, ServerSocketWrapper> getNameWrapperMap() {
        return nameWrapperMap;
    }

    Map<ServerSocketWrapper, String> getWrapperNameMap() {
        return wrapperNameMap;
    }

    static Logger getLogger() {
        return logger;
    }

    private static class HandlerThreadFactory implements ThreadFactory {
        private AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {

            Thread thread;

            if (r instanceof ClientHandler) {
                thread = new HandlerThread((ClientHandler) r);
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    logger.log(SEVERE, "ClientHandler Thread crashed", e);
                    t.interrupt();
                });
                thread.setName("ClientHandlerThread-" + integer.getAndIncrement());

            } else {
                thread = new Thread(r);
            }
            return thread;
        }
    }

    private static class HandlerThread extends Thread {

        private final ClientHandler handler;
        private boolean loggedOut = false;

        private HandlerThread(ClientHandler handler) {
            this.handler = handler;
        }

        @Override
        public void interrupt() {

            if (!loggedOut) {
                loggedOut = true;
                handler.logOut();
            }

            super.interrupt();
        }
    }

}
