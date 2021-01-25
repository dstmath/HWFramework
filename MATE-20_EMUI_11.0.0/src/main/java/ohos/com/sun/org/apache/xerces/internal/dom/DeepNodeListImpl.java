package ohos.com.sun.org.apache.xerces.internal.dom;

import java.util.Vector;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;

public class DeepNodeListImpl implements NodeList {
    protected int changes;
    protected boolean enableNS;
    protected Vector nodes;
    protected String nsName;
    protected NodeImpl rootNode;
    protected String tagName;

    public DeepNodeListImpl(NodeImpl nodeImpl, String str) {
        this.changes = 0;
        this.enableNS = false;
        this.rootNode = nodeImpl;
        this.tagName = str;
        this.nodes = new Vector();
    }

    public DeepNodeListImpl(NodeImpl nodeImpl, String str, String str2) {
        this(nodeImpl, str2);
        this.nsName = (str == null || str.equals("")) ? null : str;
        this.enableNS = true;
    }

    public int getLength() {
        item(Integer.MAX_VALUE);
        return this.nodes.size();
    }

    public Node item(int i) {
        NodeImpl nodeImpl;
        if (this.rootNode.changes() != this.changes) {
            this.nodes = new Vector();
            this.changes = this.rootNode.changes();
        }
        if (i < this.nodes.size()) {
            return (Node) this.nodes.elementAt(i);
        }
        if (this.nodes.size() == 0) {
            nodeImpl = this.rootNode;
        } else {
            nodeImpl = (NodeImpl) this.nodes.lastElement();
        }
        while (nodeImpl != null && i >= this.nodes.size()) {
            nodeImpl = nextMatchingElementAfter(nodeImpl);
            if (nodeImpl != null) {
                this.nodes.addElement(nodeImpl);
            }
        }
        return nodeImpl;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0055, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00bd, code lost:
        return r5;
     */
    public Node nextMatchingElementAfter(Node node) {
        String str;
        String str2;
        Node nextSibling;
        while (true) {
            Node node2 = null;
            if (node == null) {
                return null;
            }
            if (node.hasChildNodes()) {
                node = node.getFirstChild();
            } else if (node == this.rootNode || (nextSibling = node.getNextSibling()) == null) {
                while (node != this.rootNode && (node2 = node.getNextSibling()) == null) {
                    node = node.getParentNode();
                }
                node = node2;
            } else {
                node = nextSibling;
            }
            if (!(node == this.rootNode || node == null || node.getNodeType() != 1)) {
                if (!this.enableNS) {
                    if (this.tagName.equals("*") || node.getTagName().equals(this.tagName)) {
                        break;
                    }
                } else if (this.tagName.equals("*")) {
                    String str3 = this.nsName;
                    if (str3 == null || !str3.equals("*")) {
                        ElementImpl elementImpl = node;
                        if ((this.nsName == null && elementImpl.getNamespaceURI() == null) || ((str2 = this.nsName) != null && str2.equals(elementImpl.getNamespaceURI()))) {
                            break;
                        }
                    } else {
                        return node;
                    }
                } else {
                    ElementImpl elementImpl2 = node;
                    if (elementImpl2.getLocalName() != null && elementImpl2.getLocalName().equals(this.tagName)) {
                        String str4 = this.nsName;
                        if (str4 == null || !str4.equals("*")) {
                            if ((this.nsName == null && elementImpl2.getNamespaceURI() == null) || ((str = this.nsName) != null && str.equals(elementImpl2.getNamespaceURI()))) {
                                break;
                            }
                        } else {
                            return node;
                        }
                    }
                }
            }
        }
        return node;
    }
}
