package ohos.javax.xml.transform.dom;

import ohos.javax.xml.transform.Source;
import ohos.org.w3c.dom.Node;

public class DOMSource implements Source {
    public static final String FEATURE = "http://ohos.javax.xml.transform.dom.DOMSource/feature";
    private Node node;
    private String systemID;

    public DOMSource() {
    }

    public DOMSource(Node node2) {
        setNode(node2);
    }

    public DOMSource(Node node2, String str) {
        setNode(node2);
        setSystemId(str);
    }

    public void setNode(Node node2) {
        this.node = node2;
    }

    public Node getNode() {
        return this.node;
    }

    @Override // ohos.javax.xml.transform.Source
    public void setSystemId(String str) {
        this.systemID = str;
    }

    @Override // ohos.javax.xml.transform.Source
    public String getSystemId() {
        return this.systemID;
    }
}
