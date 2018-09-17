package org.w3c.dom;

public class DOMException extends RuntimeException {
    public static final short DOMSTRING_SIZE_ERR = (short) 2;
    public static final short HIERARCHY_REQUEST_ERR = (short) 3;
    public static final short INDEX_SIZE_ERR = (short) 1;
    public static final short INUSE_ATTRIBUTE_ERR = (short) 10;
    public static final short INVALID_ACCESS_ERR = (short) 15;
    public static final short INVALID_CHARACTER_ERR = (short) 5;
    public static final short INVALID_MODIFICATION_ERR = (short) 13;
    public static final short INVALID_STATE_ERR = (short) 11;
    public static final short NAMESPACE_ERR = (short) 14;
    public static final short NOT_FOUND_ERR = (short) 8;
    public static final short NOT_SUPPORTED_ERR = (short) 9;
    public static final short NO_DATA_ALLOWED_ERR = (short) 6;
    public static final short NO_MODIFICATION_ALLOWED_ERR = (short) 7;
    public static final short SYNTAX_ERR = (short) 12;
    public static final short TYPE_MISMATCH_ERR = (short) 17;
    public static final short VALIDATION_ERR = (short) 16;
    public static final short WRONG_DOCUMENT_ERR = (short) 4;
    public short code;

    public DOMException(short code, String message) {
        super(message);
        this.code = code;
    }
}
