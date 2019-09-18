package org.apache.xml.serializer.utils;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

public final class AttList implements Attributes {
    NamedNodeMap m_attrs;
    DOM2Helper m_dh;
    int m_lastIndex = (this.m_attrs.getLength() - 1);

    public AttList(NamedNodeMap attrs, DOM2Helper dh) {
        this.m_attrs = attrs;
        this.m_dh = dh;
    }

    public int getLength() {
        return this.m_attrs.getLength();
    }

    public String getURI(int index) {
        String ns = this.m_dh.getNamespaceOfNode((Attr) this.m_attrs.item(index));
        if (ns == null) {
            return "";
        }
        return ns;
    }

    public String getLocalName(int index) {
        return this.m_dh.getLocalNameOfNode((Attr) this.m_attrs.item(index));
    }

    public String getQName(int i) {
        return ((Attr) this.m_attrs.item(i)).getName();
    }

    public String getType(int i) {
        return "CDATA";
    }

    public String getValue(int i) {
        return ((Attr) this.m_attrs.item(i)).getValue();
    }

    public String getType(String name) {
        return "CDATA";
    }

    public String getType(String uri, String localName) {
        return "CDATA";
    }

    public String getValue(String name) {
        Attr attr = (Attr) this.m_attrs.getNamedItem(name);
        if (attr != null) {
            return attr.getValue();
        }
        return null;
    }

    public String getValue(String uri, String localName) {
        Node a = this.m_attrs.getNamedItemNS(uri, localName);
        if (a == null) {
            return null;
        }
        return a.getNodeValue();
    }

    public int getIndex(String uri, String localPart) {
        for (int i = this.m_attrs.getLength() - 1; i >= 0; i--) {
            Node a = this.m_attrs.item(i);
            String u = a.getNamespaceURI();
            if (u == null) {
                if (uri != null) {
                    continue;
                }
            } else if (!u.equals(uri)) {
                continue;
            }
            if (a.getLocalName().equals(localPart)) {
                return i;
            }
        }
        return -1;
    }

    public int getIndex(String qName) {
        for (int i = this.m_attrs.getLength() - 1; i >= 0; i--) {
            if (this.m_attrs.item(i).getNodeName().equals(qName)) {
                return i;
            }
        }
        return -1;
    }
}
