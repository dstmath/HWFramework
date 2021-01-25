package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.ContentModelValidator;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class XMLElementDecl {
    public static final short TYPE_ANY = 0;
    public static final short TYPE_CHILDREN = 3;
    public static final short TYPE_EMPTY = 1;
    public static final short TYPE_MIXED = 2;
    public static final short TYPE_SIMPLE = 4;
    public ContentModelValidator contentModelValidator;
    public final QName name = new QName();
    public int scope = -1;
    public final XMLSimpleType simpleType = new XMLSimpleType();
    public short type = -1;

    public void setValues(QName qName, int i, short s, ContentModelValidator contentModelValidator2, XMLSimpleType xMLSimpleType) {
        this.name.setValues(qName);
        this.scope = i;
        this.type = s;
        this.contentModelValidator = contentModelValidator2;
        this.simpleType.setValues(xMLSimpleType);
    }

    public void clear() {
        this.name.clear();
        this.type = -1;
        this.scope = -1;
        this.contentModelValidator = null;
        this.simpleType.clear();
    }
}
