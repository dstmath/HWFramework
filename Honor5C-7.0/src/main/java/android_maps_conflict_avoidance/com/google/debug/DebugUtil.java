package android_maps_conflict_avoidance.com.google.debug;

public class DebugUtil {
    private DebugUtil() {
    }

    public static boolean isAntPropertyExpanded(String property) {
        return !property.startsWith("${");
    }

    public static String getAntProperty(String property, String def) {
        return !isAntPropertyExpanded(property) ? def : property;
    }

    public static String getAntPropertyOrNull(String property) {
        return getAntProperty(property, null);
    }

    public static Object newInstance(Class cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Cannot instantiate instance of class " + cls.getName());
        } catch (IllegalAccessException e2) {
            throw new RuntimeException("No public default constructor for class " + cls.getName());
        }
    }
}
