package javax.xml.transform.dom;

import javax.xml.transform.Result;
import org.w3c.dom.Node;

public class DOMResult implements Result {
    public static final String FEATURE = "http://javax.xml.transform.dom.DOMResult/feature";
    private Node nextSibling;
    private Node node;
    private String systemId;

    public DOMResult() {
        this.node = null;
        this.nextSibling = null;
        this.systemId = null;
        setNode(null);
        setNextSibling(null);
        setSystemId(null);
    }

    public DOMResult(Node node) {
        this.node = null;
        this.nextSibling = null;
        this.systemId = null;
        setNode(node);
        setNextSibling(null);
        setSystemId(null);
    }

    public DOMResult(Node node, String systemId) {
        this.node = null;
        this.nextSibling = null;
        this.systemId = null;
        setNode(node);
        setNextSibling(null);
        setSystemId(systemId);
    }

    public DOMResult(Node node, Node nextSibling) {
        this.node = null;
        this.nextSibling = null;
        this.systemId = null;
        if (nextSibling != null) {
            if (node == null) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            } else if ((node.compareDocumentPosition(nextSibling) & 16) == 0) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        setNode(node);
        setNextSibling(nextSibling);
        setSystemId(null);
    }

    public DOMResult(Node node, Node nextSibling, String systemId) {
        this.node = null;
        this.nextSibling = null;
        this.systemId = null;
        if (nextSibling != null) {
            if (node == null) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            } else if ((node.compareDocumentPosition(nextSibling) & 16) == 0) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        setNode(node);
        setNextSibling(nextSibling);
        setSystemId(systemId);
    }

    public void setNode(Node node) {
        if (this.nextSibling != null) {
            if (node == null) {
                throw new IllegalStateException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            } else if ((node.compareDocumentPosition(this.nextSibling) & 16) == 0) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        this.node = node;
    }

    public Node getNode() {
        return this.node;
    }

    public void setNextSibling(Node nextSibling) {
        if (nextSibling != null) {
            if (this.node == null) {
                throw new IllegalStateException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            } else if ((this.node.compareDocumentPosition(nextSibling) & 16) == 0) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        this.nextSibling = nextSibling;
    }

    public Node getNextSibling() {
        return this.nextSibling;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSystemId() {
        return this.systemId;
    }
}
