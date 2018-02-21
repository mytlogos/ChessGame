package chessGame.settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class Settings {
    private static Settings settings = new Settings();
    private Map<String, String> keyValueMap = new HashMap<>();
    private Map<String, String> defaultMap = new HashMap<>();
    private String file = "chess.ini";
    private boolean loaded;
    private List<SetAble> setAbles = new ArrayList<>();

    private Settings() {
        if (settings != null) {
            throw new IllegalStateException("is already initiated");
        }

    }

    public static Settings getSettings() {
        return settings;
    }

    public void load() {
        if (!loaded) {
            loaded = true;
            String directory = System.getProperty("user.dir") + "\\";
            load(directory + file, keyValueMap);
            load(directory + "chessDefault.ini", defaultMap);
        }
    }

    private void load(String file, Map<String, String> map) {
        File source = new File(file);
        try {
            //noinspection ResultOfMethodCallIgnored
            source.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Scanner scanner = new Scanner(source).useDelimiter(System.lineSeparator())) {

            while (scanner.hasNext()) {
                String next = scanner.next();
                String[] strings = next.split("=");

                if (strings.length != 2) {
                    System.err.println("Mismatching Line Values " + next);
                } else {
                    String key = strings[0];
                    String value = strings[1];

                    if (key.equals("null") || key.isEmpty()) {
                        System.err.println("Illegal Key");
                    } else {
                        map.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(SetAble setAble) {
        setAbles.add(setAble);
        setAble.getManager().load();
    }

    public String getValueOrDefault(String key) {
        if (keyValueMap.containsKey(key)) {
            return keyValueMap.get(key);
        } else {
            return defaultMap.get(key);
        }
    }

    public void resetToDefault() {
        for (SetAble able : setAbles) {
            able.getManager().loadDefault();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {

            Map<String, String> keyValues = new HashMap<>();

            for (SetAble able : setAbles) {
                Map<String, String> map = able.getManager().getMap();
                keyValues.putAll(map);
            }

            for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                String line = entry.getKey() + "=" + entry.getValue() + System.lineSeparator();
                writer.write(line);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getDefault(String key) {
        return defaultMap.get(key);
    }


}
