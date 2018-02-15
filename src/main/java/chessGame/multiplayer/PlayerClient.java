package chessGame.multiplayer;

import chessGame.mechanics.Color;
import chessGame.mechanics.move.PlayerMove;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 */
public class PlayerClient implements Runnable {

    private final ClientSocketWrapper wrapper;
    private final BooleanProperty startFailed = new SimpleBooleanProperty();
    private final Chat allChat = new Chat();
    private ObjectProperty<MultiPlayerGame> game = new SimpleObjectProperty<>();
    private ObservableList<MultiPlayer> onlinePlayer = FXCollections.observableArrayList();
    private ObservableList<String> inGamePlayers = FXCollections.observableArrayList();
    private ObservableList<String> hostingPlayers = FXCollections.observableArrayList();
    private String hostName;

    private ObjectProperty<MultiPlayer> player = new SimpleObjectProperty<>();
    private StringProperty playerName = new SimpleStringProperty();
    private boolean host;
    private Chat gameChat = null;
    private Thread socketListener;
    private PlayerMove lastReceived = null;

    public PlayerClient() throws IOException {
        int port = 4445;

        //the ip of your server
        String serverHost = "";
        this.wrapper = new ClientSocketWrapper(new Socket(serverHost, port));

        socketListener = new Thread(this);
        socketListener.setDaemon(true);
        socketListener.start();

        //set the inGame flags for players
        ObservableList<String> inGamePlayer = getInGamePlayers();

        inGamePlayer.addListener((InvalidationListener) observable -> {

            for (MultiPlayer player : getOnlinePlayerList()) {
                player.setInGame(inGamePlayer.contains(player.getName()));
            }
        });

        //set the hosting flags for players
        ObservableList<String> hostingPlayer = getHostingPlayers();

        hostingPlayer.addListener((InvalidationListener) observable -> {

            for (MultiPlayer player : getOnlinePlayerList()) {
                player.setHosting(hostingPlayer.contains(player.getName()));
            }
        });
    }

    private ObservableList<String> getInGamePlayers() {
        return inGamePlayers;
    }

    public ObservableList<MultiPlayer> getOnlinePlayerList() {
        return onlinePlayer;
    }

    private ObservableList<String> getHostingPlayers() {
        return hostingPlayers;
    }

    public StringProperty playerNameProperty() {
        return playerName;
    }

    public void writeMessage(Chat.Message message) {
        wrapper.writeMessage(message);
    }

    public void closeClient() {
        wrapper.logOutClient();
        socketListener.interrupt();
    }

    public void startHost() {
        host = true;
        wrapper.writeStartHosting();
    }

    public boolean isHost() {
        return host;
    }

    public void acceptHost(String hostName) {
        host = false;
        this.hostName = hostName;
        wrapper.writeAcceptHost(hostName);
    }

    public void stopHosting() {
        host = false;
        wrapper.terminateHosting();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Client-" + getPlayerName());
        playerName.addListener((observable, oldValue, newValue) -> socketListener.setName("Client-" + newValue));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(wrapper.getInputStream()))) {

            String line;

            while (!Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
                System.out.println(Thread.currentThread().getName() + " read: " + line);

                if (wrapper.isMove(line)) {
                    PlayerMove move = wrapper.getMove(line);

                    if (!move.equals(lastReceived)) {
                        Platform.runLater(() -> game.get().makeMove(move));
                    }

                } else if (wrapper.isChat(line)) {
                    Chat.Message message = wrapper.getMessage(line);
                    getCurrentChat().addMessage(message);

                } else if (wrapper.isStartGameFailed(line)) {
                    startFailed.set(true);

                } else if (wrapper.isGameStarting(line)) {
                    String opponent = wrapper.getGamePartner(line);
                    boolean isWhite = wrapper.getGameColor(line);
                    startGame(opponent, isWhite ? Color.WHITE : Color.BLACK);

                } else if (wrapper.isPlayerList(line)) {
                    Collection<String> playerList = wrapper.getPlayerList(line);
                    Platform.runLater(() -> onlinePlayer.setAll(playerList.stream().filter(s -> !s.equals(getPlayerName())).map(MultiPlayer::new).collect(Collectors.toList())));

                } else if (wrapper.isHostsList(line)) {
                    Collection<String> playerList = wrapper.getHosts(line);
                    setHosts(playerList);

                } else if (wrapper.isInGameList(line)) {
                    Collection<String> playerList = wrapper.getInGameList(line);
                    setInGameList(playerList);

                } else if (wrapper.isGuest(line)) {
                    String guestName = wrapper.getGuestName(line);
                    setPlayerName(guestName);

                }
            }
            System.out.println("Loop finished");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPlayerName() {
        return playerName.get();
    }

    private void setPlayerName(String name) {
        Objects.requireNonNull(name);

        if (this.getPlayerName() == null) {
            wrapper.writePlayerLogin(name);
        } else {
            wrapper.writeRename(name);
        }

        Platform.runLater(() -> {
            this.playerName.set(name);
            this.player.set(new MultiPlayer(name));
        });
    }

    private Chat getCurrentChat() {
        return getPlayer() != null && getPlayer().isInGame() ? gameChat : allChat;
    }

    private void startGame(String gamePartner, Color color) {
        gameChat = new Chat();

        if (isStartFailed()) {
            System.err.println("Illegal State, Starting a Game even though the start failed");
            return;
        }

        if (hostName != null && !hostName.equals(gamePartner)) {
            System.err.println("Inequal partner und host: Host:" + hostName + ", Partner: " + gamePartner);
        }

        MultiPlayer ownPlayer = getPlayer();
        MultiPlayer opponentPlayer = null;

        for (MultiPlayer multiPlayer : onlinePlayer) {
            if (gamePartner.equals(multiPlayer.getName())) {
                opponentPlayer = multiPlayer;
                break;
            }
        }

        if (opponentPlayer == null) {
            System.err.println("Missing Player: " + gamePartner);
            //todo notify opponent of failed start
            return;
        }

        MultiPlayer black;
        MultiPlayer white;

        if (color == Color.WHITE) {
            black = opponentPlayer;
            white = ownPlayer;
        } else {
            black = ownPlayer;
            white = opponentPlayer;
        }

        black.setType(Color.BLACK);
        white.setType(Color.WHITE);

        MultiPlayerGame game = new MultiPlayerGame(black, white, this);

        game.finishedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                gameChat = null;
                ownPlayer.setInGame(false);
                setGame(null);
                wrapper.writeEndGame();
            }
        });

        ownPlayer.setInGame(true);
        setGame(game);
    }

    private void setHosts(Collection<String> playerList) {
        Platform.runLater(() -> {
            hostingPlayers.setAll(playerList);

            MultiPlayer player = getPlayer();

            if (player != null) {
                player.setHosting(playerList.contains(player.getName()));
            }
        });
    }

    private void setInGameList(Collection<String> playerList) {
        Platform.runLater(() -> {
            inGamePlayers.setAll(playerList);
            MultiPlayer player = getPlayer();

            if (player != null) {
                player.setInGame(playerList.contains(player.getName()));
            }
        });
    }

    public MultiPlayer getPlayer() {
        return player.get();
    }

    private boolean isStartFailed() {
        return startFailed.get();
    }

    public void setStartFailed(boolean startFailed) {
        Platform.runLater(() -> this.startFailed.set(startFailed));
    }

    public ObjectProperty<MultiPlayer> playerProperty() {
        return player;
    }

    public MultiPlayerGame getGame() {
        return game.get();
    }

    private void setGame(MultiPlayerGame game) {
        Platform.runLater(() -> this.game.set(game));
    }

    public ObjectProperty<MultiPlayerGame> gameProperty() {
        return game;
    }

    public BooleanProperty startFailedProperty() {
        return startFailed;
    }

    public Chat getAllChat() {
        return allChat;
    }

    public Chat getGameChat() {
        return gameChat;
    }

    ClientSocketWrapper getWrapper() {
        return wrapper;
    }


}
