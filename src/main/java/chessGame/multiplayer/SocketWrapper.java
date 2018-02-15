package chessGame.multiplayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class which dictates the Communication between the Server and Client.
 */
public class SocketWrapper {
    static final int timeStampIndex = 1;
    static final int nameIndex = 2;
    static final int messageIndex = 3;
    static final String playerLogIn = "PLAYERLOGIN";
    static final String playerList = "PLAYERLIST";
    static final String separator = "$";
    static final String gameEnd = "GAMEOVER";
    static final String startGame = "GAMESTART";
    static final String startGameFailed = "GAMESTARTFAILED";
    static final String chat = "CHAT";
    static final String move = "MOVE";
    static final String hosts = "HOSTS";
    static final String hosting = "HOSTING";
    static final String hostingTerminate = "STOPPEDHOSTING";
    static final String accepts = "ACCEPT";
    static final String playerLogOut = "LOGOUT";
    static final String playerReName = "RENAME";
    static final String guest = "GUEST";
    static final String inGameList = "INGAMELIST";
    static final String whiteColor = "WHITE";
    static final String blackColor = "BLACK";
    private static final String shutDown = "ShutdownServer";


    private final OutputStream outputStream;
    private final InputStream inputStream;

    SocketWrapper(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    void shutDownServer() {
        write(shutDown);
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

    boolean isChat(String string) {
        return string.startsWith(chat);
    }

    boolean isMove(String string) {
        return string.startsWith(move);
    }

}
