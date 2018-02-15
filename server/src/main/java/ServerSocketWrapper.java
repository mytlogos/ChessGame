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
    private static final String whiteColor = "WHITE";
    private static final String blackColor = "BLACK";
    private static final String shutDown = "ShutdownServer";

    private final OutputStream outputStream;
    private final InputStream inputStream;

    ServerSocketWrapper(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    boolean isShutDown(String line) {
        return line.equals(shutDown);
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

    void write(String output) {
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
        return line.startsWith(gameEnd);
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