package chessGame.multiplayer;

import chessGame.mechanics.Color;
import chessGame.mechanics.move.MoveCoder;
import chessGame.mechanics.move.PlayerMove;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Class which dictates the Communication between {@link Server} and {@link PlayerClient}.
 */
public class SocketWrapper {
    private static final String playerLogIn = "PLAYERLOGIN";
    private static final String playerList = "PLAYERLIST";
    private static final String separator = "$";
    private static final String gameEnd = "GAMEOVER";
    private static final String startGame = "GAMESTART";
    private static final String startGameFailed = "GAMESTARTFAILED";
    private static final String chat = "CHAT";
    private static final String move = "MOVE";
    private static final String hosts = "HOSTS";
    private static final String hosting = "HOSTING";
    private static final String hostingTerminate = "STOPPEDHOSTING";
    private static final String accepts = "ACCEPT";
    private static final String playerLogOut = "LOGOUT";
    private static final String playerReName = "RENAME";
    private static final String guest = "GUEST";
    private static final String inGameList = "INGAMELIST";

    private static final int timeStampIndex = 1;
    private static final int nameIndex = 2;
    private static final int messageIndex = 3;
    private final OutputStream outputStream;
    private final InputStream inputStream;

    public SocketWrapper(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    public void logOutClient() {
        write(playerLogOut);
    }

    boolean isGuest(String line) {
        return line.startsWith(guest);
    }

    String getGuestName(String line) {
        return line.substring(line.indexOf(separator) + 1);
    }

    void writeGuestName(String guestName) {
        write(guest + separator + guestName);
    }

    private void write(String output) {
        //dont close it
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(output);
    }

    void writeAcceptHost(String hostName) {
        write(accepts + separator + hostName);
    }

    void writeRename(String newName) {
        write(playerReName + separator + newName);
    }

    InputStream getInputStream() {
        return inputStream;
    }

    void writeStartFailed() {
        write(startGameFailed);
    }

    boolean isStartGameFailed(String line) {
        return line.startsWith(startGameFailed);
    }

    boolean isGameStarting(String line) {
        return line.startsWith(startGame);
    }

    String getGamePartner(String line) {
        return line.substring(line.indexOf(separator) + 1, line.lastIndexOf(separator));
    }

    String getGameColor(String line) {
        return line.substring(line.lastIndexOf(separator) + 1);
    }

    void writeStartGame(String opponent, Color color) {
        write(startGame + separator + opponent + separator + color);
    }

    void writeEndGame() {
        write(gameEnd);
    }

    boolean isGameEnd(String line) {
        return line.startsWith(gameEnd);
    }

    boolean isAccepting(String line) {
        return line.startsWith(accepts);
    }

    void writeMessage(Chat.Message message) {
        String messageString = chat + separator +
                message.getTimeStamp() + separator +
                message.getPlayerName() + separator +
                message.getContent();
        write(messageString);
    }

    Chat.Message getMessage(String input) {
        if (!isChat(input)) {
            return null;
        }
        String[] split = input.split("\\$");

        String timeStamp = split[timeStampIndex];
        long parsedTimeStamp = Long.parseLong(timeStamp);
        String name = split[nameIndex];

        String message;

        if (split.length > 4) {
            StringBuilder builder = new StringBuilder();

            //iterate in case the message contains the separator
            for (int i = messageIndex; i < split.length; i++) {
                String messagePart = split[i];
                builder.append(messagePart);
            }
            message = builder.toString();

        } else {
            message = split[messageIndex];
        }
        return new Chat.Message(parsedTimeStamp, message, name);
    }

    boolean isChat(String string) {
        return string.startsWith(chat);
    }

    String getAcceptedHost(String line) {
        return line.substring(line.indexOf(separator) + 1);
    }

    boolean isHostingTerminated(String line) {
        return line.startsWith(hostingTerminate);
    }

    boolean isHosting(String line) {
        return line.startsWith(hosting);
    }

    void writeStartHosting() {
        write(hosting);
    }

    void terminateHosting() {
        write(hostingTerminate);
    }

    PlayerMove getMove(String input) {
        if (!isMove(input)) {
            return null;
        }
        int i = input.indexOf(separator);
        String substring = input.substring(i + 1, input.length());
        return MoveCoder.decode(substring);
    }

    boolean isMove(String string) {
        return string.startsWith(move);
    }

    void writeMove(PlayerMove playerMove) {
        String encodedMove = MoveCoder.encode(playerMove);
        encodedMove = move + separator + encodedMove;
        write(encodedMove);
    }

    void writeMove(String line) {
        write(line);
    }

    MultiPlayer getPlayer(String input) {
        int i = input.indexOf(SocketWrapper.separator);
        String substring = input.substring(i + 1);
        return new MultiPlayer(substring);
    }

    void writeHosts(Collection<String> players) {
        StringBuilder hostingBuilder = new StringBuilder();
        hostingBuilder.append(hosts);

        for (String s : players) {
            hostingBuilder.append(separator).append(s);
        }

        write(hostingBuilder.toString());
    }

    boolean isHostsList(String line) {
        return line.startsWith(hosts);
    }

    Collection<String> getHosts(String line) {
        String[] split = line.split("\\$");
        return new ArrayList<>(Arrays.asList(split).subList(1, split.length));
    }

    boolean isInGameList(String line) {
        return line.startsWith(inGameList);
    }

    void writeInGameList(Collection<String> inGamePlayers) {
        StringBuilder builder = new StringBuilder();
        builder.append(inGameList);

        for (String player : inGamePlayers) {
            builder.append(separator).append(player);
        }
        write(builder.toString());
    }

    Collection<String> getInGameList(String line) {
        String[] split = line.split("\\$");
        return new ArrayList<>(Arrays.asList(split).subList(1, split.length));
    }

    boolean isPlayerList(String line) {
        return line.startsWith(playerList);
    }

    Collection<String> getPlayerList(String line) {
        String[] split = line.split("\\$");
        return new ArrayList<>(Arrays.asList(split).subList(1, split.length));
    }

    void writeOnlinePlayers(Collection<String> players) {
        StringBuilder builder = new StringBuilder();
        builder.append(playerList);

        for (String player : players) {
            builder.append(separator).append(player);
        }

        write(builder.toString());
    }

    boolean isPlayerLogin(String string) {
        return string.startsWith(playerLogIn);
    }

    void writePlayerLogin(String player) {
        String playerString = playerLogIn + separator + player;
        write(playerString);
    }

    boolean isLogOut(String line) {
        return line.startsWith(playerLogOut);
    }

}
