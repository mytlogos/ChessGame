package chessGame.multiplayer;

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
 * Class which dictates the Communication between the Server and Client.
 */
public class ClientSocketWrapper {
    private static final int timeStampIndex = 1;
    private static final int nameIndex = 2;
    private static final int messageIndex = 3;
    private static final String playerLogIn = "PLAYERLOGIN";
    private static final String playerList = "PLAYERLIST";
    private static final String separator = "$";
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
    private static final String whiteColor = "WHITE";
    private static final String blackColor = "BLACK";
    private static final String gameInterrupted = "GAMEINTERRUPT";
    private static final String shutDown = "ShutdownServer";
    private static final String reset = "Reset";
    private static final String moveRejected = "MOVEREJECTED";


    private final OutputStream outputStream;
    private final InputStream inputStream;

    public ClientSocketWrapper(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    public boolean isMoveRejected(String line) {
        return line.startsWith(moveRejected);
    }

    public PlayerMove getRejectedMove(String line) {
        String encoded = line.substring(line.indexOf(separator) + 1);
        return MoveCoder.decode(encoded);
    }

    public void resetServer() {
        write(reset);
    }

    public void shutDownServer() {
        write(shutDown);
    }

    private void write(String output) {
        //dont close it
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(output);
    }

    public void logOutClient() {
        write(playerLogOut);
    }

    boolean isGameInterrupted(String line) {
        return line.startsWith(gameInterrupted);
    }

    String getGameInterrupted(String line) {
        return line.substring(line.indexOf(separator) + 1);
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

    void writeStartHosting() {
        write(hosting);
    }

    void terminateHosting() {
        write(hostingTerminate);
    }

    Collection<String> getHosts(String line) {
        String[] split = line.split("\\$");
        return new ArrayList<>(Arrays.asList(split).subList(1, split.length));
    }

    boolean isHostsList(String line) {
        return line.startsWith(hosts);
    }

    boolean isInGameList(String line) {
        return line.startsWith(inGameList);
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

    void writePlayerLogin(String player) {
        String playerString = playerLogIn + separator + player;
        write(playerString);
    }

    boolean isGuest(String line) {
        return line.startsWith(guest);
    }

    String getGuestName(String line) {
        return line.substring(line.indexOf(separator) + 1);
    }

    void writeAcceptHost(String hostName) {
        write(accepts + separator + hostName);
    }

    void writeRename(String newName) {
        write(playerReName + separator + newName);
    }

    String getGamePartner(String line) {
        return line.substring(line.indexOf(separator) + 1, line.lastIndexOf(separator));
    }

    boolean getGameColor(String line) {
        String color = line.substring(line.lastIndexOf(separator) + 1);
        return color.equals(whiteColor);
    }

    void writeMessage(Chat.Message message) {
        String messageString = chat + separator +
                message.getTimeStamp() + separator +
                message.getPlayerName() + separator +
                message.getContent();
        write(messageString);
    }

    PlayerMove getMove(String input) {
        if (!isMove(input)) {
            return null;
        }
        int i = input.indexOf(separator);
        String substring = input.substring(i + 1, input.lastIndexOf(separator));
        return MoveCoder.decode(substring);
    }

    boolean isMove(String string) {
        return string.startsWith(move);
    }

    void writeMove(PlayerMove playerMove, String endState) {
        String encodedMove = MoveCoder.encode(playerMove);
        encodedMove = move + separator + encodedMove + separator + endState;
        write(encodedMove);
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
}
