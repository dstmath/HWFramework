package android.view.inspector;

import android.graphics.Color;

public interface PropertyReader {
    void readBoolean(int i, boolean z);

    void readByte(int i, byte b);

    void readChar(int i, char c);

    void readColor(int i, int i2);

    void readColor(int i, long j);

    void readColor(int i, Color color);

    void readDouble(int i, double d);

    void readFloat(int i, float f);

    void readGravity(int i, int i2);

    void readInt(int i, int i2);

    void readIntEnum(int i, int i2);

    void readIntFlag(int i, int i2);

    void readLong(int i, long j);

    void readObject(int i, Object obj);

    void readResourceId(int i, int i2);

    void readShort(int i, short s);

    public static class PropertyTypeMismatchException extends RuntimeException {
        public PropertyTypeMismatchException(int id, String expectedPropertyType, String actualPropertyType, String propertyName) {
            super(formatMessage(id, expectedPropertyType, actualPropertyType, propertyName));
        }

        public PropertyTypeMismatchException(int id, String expectedPropertyType, String actualPropertyType) {
            super(formatMessage(id, expectedPropertyType, actualPropertyType, null));
        }

        private static String formatMessage(int id, String expectedPropertyType, String actualPropertyType, String propertyName) {
            if (propertyName == null) {
                return String.format("Attempted to read property with ID 0x%08X as type %s, but the ID is of type %s.", Integer.valueOf(id), expectedPropertyType, actualPropertyType);
            }
            return String.format("Attempted to read property \"%s\" with ID 0x%08X as type %s, but the ID is of type %s.", propertyName, Integer.valueOf(id), expectedPropertyType, actualPropertyType);
        }
    }
}
