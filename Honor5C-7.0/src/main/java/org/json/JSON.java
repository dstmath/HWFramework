package org.json;

class JSON {
    JSON() {
    }

    static double checkDouble(double d) throws JSONException {
        if (!Double.isInfinite(d) && !Double.isNaN(d)) {
            return d;
        }
        throw new JSONException("Forbidden numeric value: " + d);
    }

    static Boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String stringValue = (String) value;
            if ("true".equalsIgnoreCase(stringValue)) {
                return Boolean.valueOf(true);
            }
            if ("false".equalsIgnoreCase(stringValue)) {
                return Boolean.valueOf(false);
            }
        }
        return null;
    }

    static Double toDouble(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return Double.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    static Integer toInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((int) Double.parseDouble((String) value));
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    static Long toLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return Long.valueOf(((Number) value).longValue());
        }
        if (value instanceof String) {
            try {
                return Long.valueOf((long) Double.parseDouble((String) value));
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    static String toString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

    public static JSONException typeMismatch(Object indexOrName, Object actual, String requiredType) throws JSONException {
        if (actual == null) {
            throw new JSONException("Value at " + indexOrName + " is null.");
        }
        throw new JSONException("Value " + actual + " at " + indexOrName + " of type " + actual.getClass().getName() + " cannot be converted to " + requiredType);
    }

    public static JSONException typeMismatch(Object actual, String requiredType) throws JSONException {
        if (actual == null) {
            throw new JSONException("Value is null.");
        }
        throw new JSONException("Value " + actual + " of type " + actual.getClass().getName() + " cannot be converted to " + requiredType);
    }
}
