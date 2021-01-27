package ohos.utils.zson;

public enum ZSONType {
    ARRAY,
    OBJECT,
    NAME,
    BOOLEAN,
    INTEGER,
    FLOAT,
    NULL,
    STRING,
    UNKNOWN;

    static ZSONType of(int i) {
        if (i == 2) {
            return INTEGER;
        }
        if (i == 3) {
            return FLOAT;
        }
        if (i == 4) {
            return STRING;
        }
        if (i == 6 || i == 7) {
            return BOOLEAN;
        }
        if (i == 8) {
            return NULL;
        }
        if (i == 12) {
            return OBJECT;
        }
        if (i != 14) {
            return UNKNOWN;
        }
        return ARRAY;
    }
}
