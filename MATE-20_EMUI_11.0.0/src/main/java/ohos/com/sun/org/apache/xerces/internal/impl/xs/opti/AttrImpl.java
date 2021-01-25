package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.TypeInfo;

public class AttrImpl extends NodeImpl implements Attr {
    Element element;
    String value;

    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    public boolean getSpecified() {
        return true;
    }

    public boolean isId() {
        return false;
    }

    public AttrImpl() {
        this.nodeType = 2;
    }

    public AttrImpl(Element element2, String str, String str2, String str3, String str4, String str5) {
        super(str, str2, str3, str4, 2);
        this.element = element2;
        this.value = str5;
    }

    public String getName() {
        return this.rawname;
    }

    public String getValue() {
        return this.value;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public String getNodeValue() {
        return getValue();
    }

    public Element getOwnerElement() {
        return this.element;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Document getOwnerDocument() {
        return this.element.getOwnerDocument();
    }

    public void setValue(String str) throws DOMException {
        this.value = str;
    }

    public String toString() {
        return getName() + "=\"" + getValue() + "\"";
    }
}
