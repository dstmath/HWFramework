package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.TypeInfo;

public class DefaultElement extends NodeImpl implements Element {
    public String getAttribute(String str) {
        return null;
    }

    public String getAttributeNS(String str, String str2) {
        return null;
    }

    public Attr getAttributeNode(String str) {
        return null;
    }

    public Attr getAttributeNodeNS(String str, String str2) {
        return null;
    }

    public NodeList getElementsByTagName(String str) {
        return null;
    }

    public NodeList getElementsByTagNameNS(String str, String str2) {
        return null;
    }

    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    public String getTagName() {
        return null;
    }

    public boolean hasAttribute(String str) {
        return false;
    }

    public boolean hasAttributeNS(String str, String str2) {
        return false;
    }

    public DefaultElement() {
    }

    public DefaultElement(String str, String str2, String str3, String str4, short s) {
        super(str, str2, str3, str4, s);
    }

    public void setAttribute(String str, String str2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void removeAttribute(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Attr removeAttributeNode(Attr attr) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Attr setAttributeNode(Attr attr) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void setAttributeNS(String str, String str2, String str3) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void removeAttributeNS(String str, String str2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Attr setAttributeNodeNS(Attr attr) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void setIdAttributeNode(Attr attr, boolean z) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void setIdAttribute(String str, boolean z) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void setIdAttributeNS(String str, String str2, boolean z) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }
}
