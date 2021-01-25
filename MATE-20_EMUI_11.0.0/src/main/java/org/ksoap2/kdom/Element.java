package org.ksoap2.kdom;

import java.io.IOException;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class Element extends Node {
    protected Vector attributes;
    protected String name;
    protected String namespace;
    protected Node parent;
    protected Vector prefixes;

    public void init() {
    }

    public void clear() {
        this.attributes = null;
        this.children = null;
    }

    @Override // org.ksoap2.kdom.Node
    public Element createElement(String namespace2, String name2) {
        Node node = this.parent;
        if (node == null) {
            return super.createElement(namespace2, name2);
        }
        return node.createElement(namespace2, name2);
    }

    public int getAttributeCount() {
        Vector vector = this.attributes;
        if (vector == null) {
            return 0;
        }
        return vector.size();
    }

    public String getAttributeNamespace(int index) {
        return ((String[]) this.attributes.elementAt(index))[0];
    }

    public String getAttributeName(int index) {
        return ((String[]) this.attributes.elementAt(index))[1];
    }

    public String getAttributeValue(int index) {
        return ((String[]) this.attributes.elementAt(index))[2];
    }

    public String getAttributeValue(String namespace2, String name2) {
        for (int i = 0; i < getAttributeCount(); i++) {
            if (name2.equals(getAttributeName(i)) && (namespace2 == null || namespace2.equals(getAttributeNamespace(i)))) {
                return getAttributeValue(i);
            }
        }
        return null;
    }

    public Node getRoot() {
        Element current = this;
        while (true) {
            Node node = current.parent;
            if (node == null) {
                return current;
            }
            if (!(node instanceof Element)) {
                return node;
            }
            current = (Element) node;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getNamespaceUri(String prefix) {
        int cnt = getNamespaceCount();
        for (int i = 0; i < cnt; i++) {
            if (prefix == getNamespacePrefix(i) || (prefix != null && prefix.equals(getNamespacePrefix(i)))) {
                return getNamespaceUri(i);
            }
        }
        Node node = this.parent;
        if (node instanceof Element) {
            return ((Element) node).getNamespaceUri(prefix);
        }
        return null;
    }

    public int getNamespaceCount() {
        Vector vector = this.prefixes;
        if (vector == null) {
            return 0;
        }
        return vector.size();
    }

    public String getNamespacePrefix(int i) {
        return ((String[]) this.prefixes.elementAt(i))[0];
    }

    public String getNamespaceUri(int i) {
        return ((String[]) this.prefixes.elementAt(i))[1];
    }

    public Node getParent() {
        return this.parent;
    }

    @Override // org.ksoap2.kdom.Node
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        for (int i = parser.getNamespaceCount(parser.getDepth() - 1); i < parser.getNamespaceCount(parser.getDepth()); i++) {
            setPrefix(parser.getNamespacePrefix(i), parser.getNamespaceUri(i));
        }
        for (int i2 = 0; i2 < parser.getAttributeCount(); i2++) {
            setAttribute(parser.getAttributeNamespace(i2), parser.getAttributeName(i2), parser.getAttributeValue(i2));
        }
        init();
        if (parser.isEmptyElementTag()) {
            parser.nextToken();
        } else {
            parser.nextToken();
            super.parse(parser);
            if (getChildCount() == 0) {
                addChild(7, "");
            }
        }
        parser.require(3, getNamespace(), getName());
        parser.nextToken();
    }

    public void setAttribute(String namespace2, String name2, String value) {
        if (this.attributes == null) {
            this.attributes = new Vector();
        }
        if (namespace2 == null) {
            namespace2 = "";
        }
        for (int i = this.attributes.size() - 1; i >= 0; i--) {
            String[] attribut = (String[]) this.attributes.elementAt(i);
            if (attribut[0].equals(namespace2) && attribut[1].equals(name2)) {
                if (value == null) {
                    this.attributes.removeElementAt(i);
                    return;
                } else {
                    attribut[2] = value;
                    return;
                }
            }
        }
        this.attributes.addElement(new String[]{namespace2, name2, value});
    }

    public void setPrefix(String prefix, String namespace2) {
        if (this.prefixes == null) {
            this.prefixes = new Vector();
        }
        this.prefixes.addElement(new String[]{prefix, namespace2});
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public void setNamespace(String namespace2) {
        if (namespace2 != null) {
            this.namespace = namespace2;
            return;
        }
        throw new NullPointerException("Use \"\" for empty namespace");
    }

    /* access modifiers changed from: protected */
    public void setParent(Node parent2) {
        this.parent = parent2;
    }

    @Override // org.ksoap2.kdom.Node
    public void write(XmlSerializer writer) throws IOException {
        if (this.prefixes != null) {
            for (int i = 0; i < this.prefixes.size(); i++) {
                writer.setPrefix(getNamespacePrefix(i), getNamespaceUri(i));
            }
        }
        writer.startTag(getNamespace(), getName());
        int len = getAttributeCount();
        for (int i2 = 0; i2 < len; i2++) {
            writer.attribute(getAttributeNamespace(i2), getAttributeName(i2), getAttributeValue(i2));
        }
        writeChildren(writer);
        writer.endTag(getNamespace(), getName());
    }
}
