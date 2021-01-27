package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.org.w3c.dom.Node;

public final class DOMInputSource extends XMLInputSource {
    private Node fNode;

    public DOMInputSource() {
        this(null);
    }

    public DOMInputSource(Node node) {
        super(null, getSystemIdFromNode(node), null);
        this.fNode = node;
    }

    public DOMInputSource(Node node, String str) {
        super(null, str, null);
        this.fNode = node;
    }

    public Node getNode() {
        return this.fNode;
    }

    public void setNode(Node node) {
        this.fNode = node;
    }

    private static String getSystemIdFromNode(Node node) {
        if (node != null) {
            try {
                return node.getBaseURI();
            } catch (Exception | NoSuchMethodError unused) {
            }
        }
        return null;
    }
}
