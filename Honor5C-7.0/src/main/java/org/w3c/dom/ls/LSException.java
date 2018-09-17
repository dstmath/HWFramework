package org.w3c.dom.ls;

public class LSException extends RuntimeException {
    public static final short PARSE_ERR = (short) 81;
    public static final short SERIALIZE_ERR = (short) 82;
    public short code;

    public LSException(short code, String message) {
        super(message);
        this.code = code;
    }
}
