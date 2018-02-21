package chessServer;

import chessGame.mechanics.Color;
import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.game.ChessGameImpl;
import chessGame.mechanics.move.PlayerMove;
import chessGame.multiplayer.MultiPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static chessServer.Server.getLogger;
import static chessServer.Server.getServer;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 *
 */
class ClientHandler implements Runnable {
    private static AtomicInteger counter = new AtomicInteger();
    private ServerSocketWrapper wrapper;
    private String name;
    private Server server;

    ClientHandler(Socket socket) throws IOException {
        wrapper = new ServerSocketWrapper(socket);
        wrapper.writeGuestName(allocateGuestName());
        name = "chessServer.ClientHandler-" + counter.getAndIncrement();
        server = getServer();
    }

    @Override
    public String toString() {
        return name;
    }

    private String allocateGuestName() {
        String guest = "Guest";
        int counter = 1;
        String name = guest + counter;

        synchronized (server.getNameWrapperMap()) {
            while (server.getNameWrapperMap().keySet().contains(name)) {
                counter++;
                name = guest + counter;
            }
        }
        return name;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(wrapper.getInputStream()))) {
            String line;

            //listen as long as it does not read null or thread is interrupted
            while ((line = reader.readLine()) != null) {
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
                    Server.shutDownServer();

                } else if (wrapper.isReset(line)) {
                    Server.reset();

                } else if (wrapper.isRename(line)) {
                    propagateNewName(line);

                } else {
                    getLogger().log(INFO, "Unknown Message received on chessServer.Server: " + line);
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

        Map<ServerSocketWrapper, String> wrapperNameMap = server.getWrapperNameMap();
        String oldName = wrapperNameMap.remove(wrapper);

        server.getNameWrapperMap().remove(oldName);
        server.getNameWrapperMap().put(newName, wrapper);

        InGame inGame = server.getInGameMap().remove(oldName);

        if (inGame != null) {
            if (inGame.player1.equals(oldName)) {
                inGame.player1 = newName;
            } else if (inGame.player2.equals(oldName)) {
                inGame.player2 = newName;
            }

            server.getInGameMap().put(newName, inGame);
        }

        if (server.getGameHosts().remove(oldName)) {
            server.getGameHosts().add(newName);
        }

        if (server.getIdlePlayers().remove(oldName)) {
            server.getIdlePlayers().add(newName);
        }

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (wrapperNameMap) {
            for (ServerSocketWrapper socketWrapper : wrapperNameMap.keySet()) {
                socketWrapper.writeOnlinePlayers(server.getNameWrapperMap().keySet());
            }
        }
    }

    private void logIn(String line) {
        String name = wrapper.getPlayer(line);


        if (server.getNameWrapperMap().containsKey(name)) {
            getLogger().log(SEVERE, "Name " + name + " is already allocated");
        } else {
            server.getNameWrapperMap().put(name, wrapper);

            Map<ServerSocketWrapper, String> wrapperNameMap = server.getWrapperNameMap();
            wrapperNameMap.put(wrapper, name);
            server.getIdlePlayers().add(name);

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (wrapperNameMap) {
                for (ServerSocketWrapper socketWrapper : wrapperNameMap.keySet()) {
                    socketWrapper.writeOnlinePlayers(server.getNameWrapperMap().keySet());
                    socketWrapper.writeHosts(server.getGameHosts());
                    socketWrapper.writeInGameList(server.getInGameMap().keySet());
                }
            }
        }
    }

    private void propagateMessage(String message) {

        String player = server.getWrapperNameMap().get(wrapper);
        InGame inGame = server.getInGameMap().get(player);

        //if player is not in a game write to all Chat, else to game chat
        if (inGame == null) {
            Set<String> idlePlayers = server.getIdlePlayers();

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (idlePlayers) {
                for (String nonPlayingPlayer : idlePlayers) {
                    server.getNameWrapperMap().get(nonPlayingPlayer).writeMessage(message);
                }
            }
        } else {
            String opponent = getOpponent(player, inGame);
            server.getNameWrapperMap().get(opponent).writeMessage(message);
            wrapper.writeMessage(message);
        }
    }

    private void propagateHosting() {

        String hostingPlayer = server.getWrapperNameMap().get(wrapper);

        if (hostingPlayer != null) {
            server.getGameHosts().add(hostingPlayer);
        } else {
            getLogger().warning("Missing Name for Wrapper");
        }

        synchronized (server.getNameWrapperMap()) {
            for (ServerSocketWrapper socketWrapper : server.getNameWrapperMap().values()) {
                socketWrapper.writeHosts(server.getGameHosts());
            }
        }

    }

    private synchronized void acceptGame(String line) {
        String acceptedHost = wrapper.getAcceptedHost(line);

        if (server.getGameHosts().contains(acceptedHost)) {


            Map<String, ServerSocketWrapper> nameWrapperMap = server.getNameWrapperMap();

            if (server.getInGameMap().containsKey(acceptedHost)) {
                wrapper.writeStartFailed();

                server.getGameHosts().remove(acceptedHost);

                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (nameWrapperMap) {
                    for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                        socketWrapper.writeHosts(server.getGameHosts());
                    }
                }
                return;
            }

            String acceptingPlayer = server.getWrapperNameMap().get(wrapper);
            ServerSocketWrapper hostWrapper = nameWrapperMap.get(acceptedHost);

            if (hostWrapper == null || acceptingPlayer == null) {
                getLogger().warning("Missing Data on chessServer.Server. Game failed to start, Wrapper: " + hostWrapper + " acceptingPlayer: " + acceptingPlayer + " acceptedHost: " + acceptedHost);
                wrapper.writeStartFailed();
            } else {
                MultiPlayer host = new MultiPlayer(acceptedHost, Color.WHITE);
                MultiPlayer acceptor = new MultiPlayer(acceptingPlayer, Color.BLACK);

                InGame inGame = new InGame(acceptingPlayer, acceptedHost, new ChessGameImpl(acceptor, host));

                changeStatus(acceptedHost, inGame,true);
                changeStatus(acceptingPlayer, inGame,false);

                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (nameWrapperMap) {
                    for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                        socketWrapper.writeInGameList(server.getInGameMap().keySet());
                        socketWrapper.writeHosts(server.getGameHosts());
                    }
                }
            }
        } else {
            wrapper.writeStartFailed();
        }
    }

    private void changeStatus(String player, InGame inGame, boolean white) {
        this.server.getInGameMap().put(player, inGame);
        this.server.getIdlePlayers().remove(player);
        this.server.getGameHosts().remove(player);
        wrapper.writeStartGame(player, white);
    }

    private void endGame() {
        //todo for the case if the other player never ends game for whatever reason, end it forcefully

        String playerName = server.getWrapperNameMap().get(wrapper);

        InGame inGame = server.getInGameMap().remove(playerName);

        if (inGame != null) {
            String player2 = inGame.player2;
            String player1 = inGame.player1;

            server.getIdlePlayers().add(player2);
            server.getIdlePlayers().add(player1);

            server.getInGameMap().remove(player2);
            server.getInGameMap().remove(player1);

            Map<String, ServerSocketWrapper> map = server.getNameWrapperMap();

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (map) {
                for (ServerSocketWrapper socketWrapper : map.values()) {
                    socketWrapper.writeInGameList(server.getInGameMap().keySet());
                }
            }
        }
    }

    private synchronized void terminateHosting() {

        String terminator = server.getWrapperNameMap().get(wrapper);

        server.getGameHosts().remove(terminator);

        Map<String, ServerSocketWrapper> map = server.getNameWrapperMap();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (map) {
            for (ServerSocketWrapper socketWrapper : map.values()) {
                socketWrapper.writeHosts(server.getGameHosts());
            }
        }
    }

    void logOut() {
        String player = server.getWrapperNameMap().remove(wrapper);
        Map<String, ServerSocketWrapper> nameWrapperMap = server.getNameWrapperMap();
        nameWrapperMap.remove(player);
        server.getIdlePlayers().remove(player);
        server.getGameHosts().remove(player);

        InGame game = server.getInGameMap().remove(player);

        if (game != null) {
            String opponent = game.player1.equals(player) ? game.player2 : game.player1;
            ServerSocketWrapper wrapper = nameWrapperMap.get(opponent);

            if (wrapper != null) {
                wrapper.interruptGame();
            }
        }

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (nameWrapperMap) {
            for (ServerSocketWrapper socketWrapper : nameWrapperMap.values()) {
                socketWrapper.writeOnlinePlayers(nameWrapperMap.keySet());
            }
        }

        Thread.currentThread().interrupt();
    }

    private void propagateMove(String line) {
        String ownPlayer = server.getWrapperNameMap().get(wrapper);
        InGame inGame = server.getInGameMap().get(ownPlayer);

        if (inGame == null) {
            getLogger().warning("Illegal State: Made Move on non existent game.");
        } else {
            ChessGame game = inGame.game;

            PlayerMove move = wrapper.getMove(line);

            try {
                game.makeMove(move);
                game.nextRound();

                String opponent = getOpponent(ownPlayer, inGame);
                server.getNameWrapperMap().get(opponent).writeMove(line);

                endGame();
            } catch (Exception e) {
                wrapper.writeMoveRejected(move);
            }

        }

    }

    private String getOpponent(String ownPlayer, InGame inGame) {
        return ownPlayer.equals(inGame.player1) ? inGame.player2 : inGame.player1;
    }

    static class InGame {
        private String player1;
        private String player2;
        private ChessGame game;

        private InGame(String player1, String player2, ChessGame game) {
            this.player1 = player1;
            this.player2 = player2;
            this.game = game;
        }
    }
}
