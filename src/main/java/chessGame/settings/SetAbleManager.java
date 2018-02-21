package chessGame.settings;

import javafx.beans.property.*;

import java.util.*;

/**
 *
 */
public class SetAbleManager {
    private Collection<SetAbleEntry> setAbleEntries = new ArrayList<>();

    public SetAbleManager(SetAbleEntry... entries) {
        setAbleEntries.addAll(Arrays.asList(entries));
    }

    public void loadDefault() {
        Settings settings = Settings.getSettings();

        for (SetAbleEntry entry : setAbleEntries) {
            Property<?> property = entry.property();

            if (DoubleProperty.class.isAssignableFrom(property.getClass())) {
                setDouble(entry, settings, true);
            } else if (BooleanProperty.class.isAssignableFrom(property.getClass())) {
                setBoolean(entry, settings, true);

            } else if (StringProperty.class.isAssignableFrom(property.getClass())) {
                setString(entry, settings, true);

            } else if (IntegerProperty.class.isAssignableFrom(property.getClass())) {
                setInteger(entry, settings, true);

            } else if (entry instanceof EnumEntry) {
                setEnum((EnumEntry) entry, settings, true);
            }
        }
    }

    void load() {
        Settings settings = Settings.getSettings();

        for (SetAbleEntry entry : setAbleEntries) {
            Property<?> property = entry.property();

            if (DoubleProperty.class.isAssignableFrom(property.getClass())) {
                setDouble(entry, settings, false);
            } else if (BooleanProperty.class.isAssignableFrom(property.getClass())) {
                setBoolean(entry, settings, false);

            } else if (StringProperty.class.isAssignableFrom(property.getClass())) {
                setString(entry, settings, false);

            } else if (IntegerProperty.class.isAssignableFrom(property.getClass())) {
                setInteger(entry, settings, false);

            } else if (entry instanceof EnumEntry) {
                setEnum((EnumEntry) entry, settings, false);
            }
        }
    }

    private void setEnum(EnumEntry entry, Settings settings, boolean defaultOnly) {
        String value;

        if (defaultOnly) {
            value = settings.getDefault(entry.getKey());
        } else {
            value = settings.getValueOrDefault(entry.getKey());
        }

        ObjectProperty<Enum> enumProperty = entry.property();
        //noinspection unchecked
        Enum valueOf = Enum.valueOf(entry.getEnumClass(), value);
        enumProperty.setValue(valueOf);
    }

    private void setDouble(SetAbleEntry entry, Settings settings, boolean defaultOnly) {

        String value;

        if (defaultOnly) {
            value = settings.getDefault(entry.getKey());
        } else {
            value = settings.getValueOrDefault(entry.getKey());
        }

        double doubleValue = Double.parseDouble(value);
        DoubleProperty property = (DoubleProperty) entry.property();
        property.set(doubleValue);
    }

    private void setBoolean(SetAbleEntry entry, Settings settings, boolean defaultOnly) {
        String value;

        if (defaultOnly) {
            value = settings.getDefault(entry.getKey());
        } else {
            value = settings.getValueOrDefault(entry.getKey());
        }

        boolean b = Boolean.parseBoolean(value);
        BooleanProperty property = (BooleanProperty) entry.property();
        property.set(b);
    }

    private void setString(SetAbleEntry entry, Settings settings, boolean defaultOnly) {
        String value;

        if (defaultOnly) {
            value = settings.getDefault(entry.getKey());
        } else {
            value = settings.getValueOrDefault(entry.getKey());
        }
        StringProperty property = (StringProperty) entry.property();
        property.set(value);
    }

    private void setInteger(SetAbleEntry entry, Settings settings, boolean defaultOnly) {
        String value;

        if (defaultOnly) {
            value = settings.getDefault(entry.getKey());
        } else {
            value = settings.getValueOrDefault(entry.getKey());
        }

        int anInt = Integer.parseInt(value);
        IntegerProperty property = (IntegerProperty) entry.property();
        property.set(anInt);
    }

    Map<String, String> getMap() {
        Map<String, String> map = new HashMap<>();

        for (SetAbleEntry entry : setAbleEntries) {
            map.put(entry.getKey(), String.valueOf(entry.getProperty()));
        }
        return map;
    }
}
