package android.app.timezone;

final class Utils {
    private Utils() {
    }

    static int validateVersion(String type, int version) {
        if (version >= 0 && version <= 999) {
            return version;
        }
        throw new IllegalArgumentException("Invalid " + type + " version=" + version);
    }

    static String validateRulesVersion(String type, String rulesVersion) {
        validateNotNull(type, rulesVersion);
        if (!rulesVersion.isEmpty()) {
            return rulesVersion;
        }
        throw new IllegalArgumentException(type + " must not be empty");
    }

    static <T> T validateNotNull(String type, T object) {
        if (object != null) {
            return object;
        }
        throw new NullPointerException(type + " == null");
    }

    static <T> T validateConditionalNull(boolean requireNotNull, String type, T object) {
        if (requireNotNull) {
            return validateNotNull(type, object);
        }
        return validateNull(type, object);
    }

    static <T> T validateNull(String type, T object) {
        if (object == null) {
            return null;
        }
        throw new IllegalArgumentException(type + " != null");
    }
}
