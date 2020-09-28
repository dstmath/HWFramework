package android.view.inspector;

import java.util.Set;
import java.util.function.IntFunction;

public interface PropertyMapper {
    int mapBoolean(String str, int i);

    int mapByte(String str, int i);

    int mapChar(String str, int i);

    int mapColor(String str, int i);

    int mapDouble(String str, int i);

    int mapFloat(String str, int i);

    int mapGravity(String str, int i);

    int mapInt(String str, int i);

    int mapIntEnum(String str, int i, IntFunction<String> intFunction);

    int mapIntFlag(String str, int i, IntFunction<Set<String>> intFunction);

    int mapLong(String str, int i);

    int mapObject(String str, int i);

    int mapResourceId(String str, int i);

    int mapShort(String str, int i);

    public static class PropertyConflictException extends RuntimeException {
        public PropertyConflictException(String name, String newPropertyType, String existingPropertyType) {
            super(String.format("Attempted to map property \"%s\" as type %s, but it is already mapped as %s.", name, newPropertyType, existingPropertyType));
        }
    }
}
