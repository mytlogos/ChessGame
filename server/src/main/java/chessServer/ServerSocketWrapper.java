package chessServer;

import chessGame.mechanics.move.MoveCoder;
import chessGame.mechanics.move.PlayerMove;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;

/**
 *
 */
class ServerSocketWrapper {
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

    private final OutputStream outputStream;
    private final InputStream inputStream;

    ServerSocketWrapper(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    boolean isReset(String line) {
        return line.equals(reset);
    }

    boolean isShutDown(String line) {
        return line.equals(shutDown);
    }

    boolean isGameInterrupted(String line) {
        return line.startsWith(gameInterrupted);
    }
    private static final String moveRejected = "MOVEREJECTED";

    void writeMoveRejected(PlayerMove move) {
        String encode = MoveCoder.encode(move);
        write(moveRejected + separator + encode);
    }

    PlayerMove getMove(String input) {
        if (!isMove(input)) {
            return null;
        }
        int i = input.indexOf(separator);
        String substring = input.substring(i + 1, input.lastIndexOf(separator));
        return MoveCoder.decode(substring);
    }

    void interruptGame() {
        write(gameInterrupted);
    }

    boolean isRename(String line) {
        return line.startsWith(playerReName);
    }

    String getNewName(String line) {
        return line.substring(line.indexOf(separator) + 1);
    }

    InputStream getInputStream() {
        return inputStream;
    }

    void writeStartFailed() {
        write(startGameFailed);
    }

    private void write(String output) {
        //dont close it
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(output);
    }

    boolean isStartGameFailed(String line) {
        return line.startsWith(startGameFailed);
    }

    boolean isMove(String string) {
        return string.startsWith(move);
    }

    String getAcceptedHost(String line) {
        return line.substring(line.indexOf(separator) + 1);
    }

    void writeGuestName(String guestName) {
        write(guest + separator + guestName);
    }

    void writeStartGame(String opponent, boolean color) {
        write(startGame + separator + opponent + separator + (color ? whiteColor : blackColor));
    }

    boolean isGameEnd(String line) {
        return !line.substring(line.lastIndexOf(separator) + 1).isEmpty();
    }

    boolean isAccepting(String line) {
        return line.startsWith(accepts);
    }

    void writeMessage(String message) {
        if (!isChat(message)) {
            return;
        }
        write(message);
    }

    boolean isChat(String string) {
        return string.startsWith(chat);
    }

    boolean isHostingTerminated(String line) {
        return line.startsWith(hostingTerminate);
    }

    boolean isHosting(String line) {
        return line.startsWith(hosting);
    }

    void writeMove(String line) {
        write(line);
    }

    String getPlayer(String input) {
        int i = input.indexOf(separator);
        return input.substring(i + 1);
    }

    void writeHosts(Collection<String> players) {
        StringBuilder hostingBuilder = new StringBuilder();
        hostingBuilder.append(hosts);

        for (String s : players) {
            hostingBuilder.append(separator).append(s);
        }

        write(hostingBuilder.toString());
    }

    void writeInGameList(Collection<String> inGamePlayers) {
        StringBuilder builder = new StringBuilder();
        builder.append(inGameList);

        for (String player : inGamePlayers) {
            builder.append(separator).append(player);
        }
        write(builder.toString());
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

    boolean isLogOut(String line) {
        return line.startsWith(playerLogOut);
    }

}
