package chessGame.data;

import chessGame.mechanics.game.ChessGame;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

/**
 *
 */
public class GameFileManager {
    private String dir = System.getProperty("user.dir");
    private String separator = System.getProperty("file.separator");
    public String savedGamesDirectory = dir + separator + "games" + separator;
    private String scenarioDirectory = dir + separator + "scenarios" + separator;

    public GameFileManager() {
        initiate(scenarioDirectory);
        initiate(savedGamesDirectory);
    }

    private void initiate(String directory) {
        File dir = new File(directory);

        if (!dir.exists()) {
            try {
                Files.createDirectory(dir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String load(String file) {
        return loadFile(new File(file));
    }

    public String loadFile(File source) {
        try (Scanner reader = new Scanner(source)) {
            StringBuilder builder = new StringBuilder();

            while (reader.hasNext()) {
                builder.append(reader.next()).append(" ");
            }
            return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public Collection<File> loadScenarios() {
        return loadDirectory(scenarioDirectory);
    }

    private Collection<File> loadDirectory(String directory) {
        File dir = new File(directory);

        Collection<File> contents = new ArrayList<>();
        File[] files = dir.listFiles();

        if (files == null) {
            System.err.println("error while getting file list of " + directory);
        } else {
            for (File file : files) {
                if (file.getName().endsWith(".pgn")) {
                    contents.add(file);
                }
            }
        }
        return contents;
    }

    public File save(ChessGame game) {
        String gameString = GameConverter.convert(game);

        Collection<File> files = loadAvailable();
        String basicName = "chessGame " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyMMdd-HHmmss"));

        File file = new File(savedGamesDirectory + basicName + ".pgn");

        int counter = 0;

        while (files.contains(file)) {
            file = new File(savedGamesDirectory + basicName + "-" + (counter++) + ".pgn");
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gameString);
            writer.flush();
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    public Collection<File> loadAvailable() {
        return loadDirectory(savedGamesDirectory);
    }
}
