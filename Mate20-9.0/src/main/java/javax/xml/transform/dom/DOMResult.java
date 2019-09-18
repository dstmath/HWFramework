package javax.xml.transform.dom;

import javax.xml.transform.Result;
import org.w3c.dom.Node;

public class DOMResult implements Result {
    public static final String FEATURE = "http://javax.xml.transform.dom.DOMResult/feature";
    private Node nextSibling = null;
    private Node node = null;
    private String systemId = null;

    public DOMResult() {
        setNode(null);
        setNextSibling(null);
        setSystemId(null);
    }

    public DOMResult(Node node2) {
        setNode(node2);
        setNextSibling(null);
        setSystemId(null);
    }

    public DOMResult(Node node2, String systemId2) {
        setNode(node2);
        setNextSibling(null);
        setSystemId(systemId2);
    }

    public DOMResult(Node node2, Node nextSibling2) {
        if (nextSibling2 != null) {
            if (node2 == null) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            } else if ((node2.compareDocumentPosition(nextSibling2) & 16) == 0) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        setNode(node2);
        setNextSibling(nextSibling2);
        setSystemId(null);
    }

    public DOMResult(Node node2, Node nextSibling2, String systemId2) {
        if (nextSibling2 != null) {
            if (node2 == null) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            } else if ((node2.compareDocumentPosition(nextSibling2) & 16) == 0) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        setNode(node2);
        setNextSibling(nextSibling2);
        setSystemId(systemId2);
    }

    public void setNode(Node node2) {
        if (this.nextSibling != null) {
            if (node2 == null) {
                throw new IllegalStateException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            } else if ((node2.compareDocumentPosition(this.nextSibling) & 16) == 0) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        this.node = node2;
    }

    public Node getNode() {
        return this.node;
    }

    public void setNextSibling(Node nextSibling2) {
        if (nextSibling2 != null) {
            if (this.node == null) {
                throw new IllegalStateException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            } else if ((this.node.compareDocumentPosition(nextSibling2) & 16) == 0) {
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        this.nextSibling = nextSibling2;
    }

    public Node getNextSibling() {
        return this.nextSibling;
    }

    public void setSystemId(String systemId2) {
        this.systemId = systemId2;
    }

    public String getSystemId() {
        return this.systemId;
    }
}
