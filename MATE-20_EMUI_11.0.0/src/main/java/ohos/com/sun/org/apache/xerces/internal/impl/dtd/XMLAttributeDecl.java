package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class XMLAttributeDecl {
    public final QName name = new QName();
    public boolean optional;
    public final XMLSimpleType simpleType = new XMLSimpleType();

    public void setValues(QName qName, XMLSimpleType xMLSimpleType, boolean z) {
        this.name.setValues(qName);
        this.simpleType.setValues(xMLSimpleType);
        this.optional = z;
    }

    public void clear() {
        this.name.clear();
        this.simpleType.clear();
        this.optional = false;
    }
}
