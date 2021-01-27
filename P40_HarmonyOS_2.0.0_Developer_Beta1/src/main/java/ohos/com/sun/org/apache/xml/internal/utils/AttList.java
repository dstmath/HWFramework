package ohos.com.sun.org.apache.xml.internal.utils;

import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.Attributes;

public class AttList implements Attributes {
    NamedNodeMap m_attrs;
    int m_lastIndex = (this.m_attrs.getLength() - 1);

    public String getType(int i) {
        return "CDATA";
    }

    public String getType(String str) {
        return "CDATA";
    }

    public String getType(String str, String str2) {
        return "CDATA";
    }

    public AttList(NamedNodeMap namedNodeMap) {
        this.m_attrs = namedNodeMap;
    }

    public int getLength() {
        return this.m_attrs.getLength();
    }

    public String getURI(int i) {
        String namespaceOfNode = DOM2Helper.getNamespaceOfNode(this.m_attrs.item(i));
        return namespaceOfNode == null ? "" : namespaceOfNode;
    }

    public String getLocalName(int i) {
        return DOM2Helper.getLocalNameOfNode(this.m_attrs.item(i));
    }

    public String getQName(int i) {
        return this.m_attrs.item(i).getName();
    }

    public String getValue(int i) {
        return this.m_attrs.item(i).getValue();
    }

    public String getValue(String str) {
        Attr namedItem = this.m_attrs.getNamedItem(str);
        if (namedItem != null) {
            return namedItem.getValue();
        }
        return null;
    }

    public String getValue(String str, String str2) {
        Node namedItemNS = this.m_attrs.getNamedItemNS(str, str2);
        if (namedItemNS == null) {
            return null;
        }
        return namedItemNS.getNodeValue();
    }

    public int getIndex(String str, String str2) {
        for (int length = this.m_attrs.getLength() - 1; length >= 0; length--) {
            Node item = this.m_attrs.item(length);
            String namespaceURI = item.getNamespaceURI();
            if (namespaceURI == null) {
                if (str != null) {
                    continue;
                }
            } else if (!namespaceURI.equals(str)) {
                continue;
            }
            if (item.getLocalName().equals(str2)) {
                return length;
            }
        }
        return -1;
    }

    public int getIndex(String str) {
        for (int length = this.m_attrs.getLength() - 1; length >= 0; length--) {
            if (this.m_attrs.item(length).getNodeName().equals(str)) {
                return length;
            }
        }
        return -1;
    }
}
