package com.huawei.zxing.client.result;

public abstract class ParsedResult {
    private final ParsedResultType type;

    public abstract String getDisplayResult();

    protected ParsedResult(ParsedResultType type2) {
        this.type = type2;
    }

    public final ParsedResultType getType() {
        return this.type;
    }

    public final String toString() {
        return getDisplayResult();
    }

    public static void maybeAppend(String value, StringBuilder result) {
        if (value != null && !value.isEmpty()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(value);
        }
    }

    public static void maybeAppend(String[] values, StringBuilder result) {
        if (values != null) {
            for (String value : values) {
                maybeAppend(value, result);
            }
        }
    }
}
