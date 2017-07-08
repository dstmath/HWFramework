package org.apache.harmony.xml.dom;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListImpl implements NodeList {
    private List<NodeImpl> children;

    NodeListImpl() {
        this.children = new ArrayList();
    }

    NodeListImpl(List<NodeImpl> list) {
        this.children = list;
    }

    void add(NodeImpl node) {
        this.children.add(node);
    }

    public int getLength() {
        return this.children.size();
    }

    public Node item(int index) {
        if (index >= this.children.size()) {
            return null;
        }
        return (Node) this.children.get(index);
    }
}
