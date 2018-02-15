package chessGame.multiplayer;

import chessGame.mechanics.move.MoveCoder;
import chessGame.mechanics.move.PlayerMove;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
public class ClientSocketWrapper extends SocketWrapper {

    ClientSocketWrapper(Socket socket) throws IOException {
        super(socket);
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

    void writeEndGame() {
        write(gameEnd);
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
        String substring = input.substring(i + 1, input.length());
        return MoveCoder.decode(substring);
    }

    public void logOutClient() {
        write(playerLogOut);
    }

    void writeMove(PlayerMove playerMove) {
        String encodedMove = MoveCoder.encode(playerMove);
        encodedMove = move + separator + encodedMove;
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
}
