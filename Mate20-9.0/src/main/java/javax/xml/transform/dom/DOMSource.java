package javax.xml.transform.dom;

import javax.xml.transform.Source;
import org.w3c.dom.Node;

public class DOMSource implements Source {
    public static final String FEATURE = "http://javax.xml.transform.dom.DOMSource/feature";
    private Node node;
    private String systemID;

    public DOMSource() {
    }

    public DOMSource(Node n) {
        setNode(n);
    }

    public DOMSource(Node node2, String systemID2) {
        setNode(node2);
        setSystemId(systemID2);
    }

    public void setNode(Node node2) {
        this.node = node2;
    }

    public Node getNode() {
        return this.node;
    }

    public void setSystemId(String systemID2) {
        this.systemID = systemID2;
    }

    public String getSystemId() {
        return this.systemID;
    }
}
