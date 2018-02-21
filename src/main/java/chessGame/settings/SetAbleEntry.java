package chessGame.settings;

import javafx.beans.property.Property;

/**
 *
 */
public class SetAbleEntry {
    private String key;
    private Property<?> property;


    public SetAbleEntry(String key, Property<?> property) {
        this.key = key;
        this.property = property;
    }

    public String getKey() {
        return key;
    }

    public Object getProperty() {
        return property.getValue();
    }

    public Property<?> property() {
        return property;
    }
}
