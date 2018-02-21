package chessGame.settings;

import javafx.beans.property.ObjectProperty;

/**
 *
 */
public class EnumEntry extends SetAbleEntry {

    private final Class<? extends Enum> enumClass;

    public EnumEntry(String key, ObjectProperty<? extends Enum> property, Class<? extends Enum> enumClass) {
        super(key, property);
        this.enumClass = enumClass;
    }

    @Override
    public ObjectProperty<Enum> property() {
        //noinspection unchecked
        return (ObjectProperty<Enum>) super.property();
    }

    @Override
    public Enum getProperty() {
        return (Enum) super.getProperty();
    }

    public Class<? extends Enum> getEnumClass() {
        return enumClass;
    }
}
