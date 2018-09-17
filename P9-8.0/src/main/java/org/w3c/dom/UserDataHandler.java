package org.w3c.dom;

public interface UserDataHandler {
    public static final short NODE_ADOPTED = (short) 5;
    public static final short NODE_CLONED = (short) 1;
    public static final short NODE_DELETED = (short) 3;
    public static final short NODE_IMPORTED = (short) 2;
    public static final short NODE_RENAMED = (short) 4;

    void handle(short s, String str, Object obj, Node node, Node node2);
}
