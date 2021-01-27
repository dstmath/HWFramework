package ohos.com.sun.org.apache.xerces.internal.impl.xs.identity;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSIDCDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public abstract class IdentityConstraint implements XSIDCDefinition {
    protected XSAnnotationImpl[] fAnnotations = null;
    protected String fElementName;
    protected int fFieldCount;
    protected Field[] fFields;
    protected String fIdentityConstraintName;
    protected String fNamespace;
    protected int fNumAnnotations;
    protected Selector fSelector;
    protected short type;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSIDCDefinition
    public XSIDCDefinition getRefKey() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 10;
    }

    protected IdentityConstraint(String str, String str2, String str3) {
        this.fNamespace = str;
        this.fIdentityConstraintName = str2;
        this.fElementName = str3;
    }

    public String getIdentityConstraintName() {
        return this.fIdentityConstraintName;
    }

    public void setSelector(Selector selector) {
        this.fSelector = selector;
    }

    public Selector getSelector() {
        return this.fSelector;
    }

    public void addField(Field field) {
        Field[] fieldArr = this.fFields;
        if (fieldArr == null) {
            this.fFields = new Field[4];
        } else {
            int i = this.fFieldCount;
            if (i == fieldArr.length) {
                this.fFields = resize(fieldArr, i * 2);
            }
        }
        Field[] fieldArr2 = this.fFields;
        int i2 = this.fFieldCount;
        this.fFieldCount = i2 + 1;
        fieldArr2[i2] = field;
    }

    public int getFieldCount() {
        return this.fFieldCount;
    }

    public Field getFieldAt(int i) {
        return this.fFields[i];
    }

    public String getElementName() {
        return this.fElementName;
    }

    public String toString() {
        String obj = super.toString();
        int lastIndexOf = obj.lastIndexOf(36);
        if (lastIndexOf != -1) {
            return obj.substring(lastIndexOf + 1);
        }
        int lastIndexOf2 = obj.lastIndexOf(46);
        return lastIndexOf2 != -1 ? obj.substring(lastIndexOf2 + 1) : obj;
    }

    public boolean equals(IdentityConstraint identityConstraint) {
        if (!(this.fIdentityConstraintName.equals(identityConstraint.fIdentityConstraintName) && this.fSelector.toString().equals(identityConstraint.fSelector.toString()))) {
            return false;
        }
        if (!(this.fFieldCount == identityConstraint.fFieldCount)) {
            return false;
        }
        for (int i = 0; i < this.fFieldCount; i++) {
            if (!this.fFields[i].toString().equals(identityConstraint.fFields[i].toString())) {
                return false;
            }
        }
        return true;
    }

    static final Field[] resize(Field[] fieldArr, int i) {
        Field[] fieldArr2 = new Field[i];
        System.arraycopy(fieldArr, 0, fieldArr2, 0, fieldArr.length);
        return fieldArr2;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return this.fIdentityConstraintName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return this.fNamespace;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSIDCDefinition
    public short getCategory() {
        return this.type;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSIDCDefinition
    public String getSelectorStr() {
        Selector selector = this.fSelector;
        if (selector != null) {
            return selector.toString();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSIDCDefinition
    public StringList getFieldStrs() {
        String[] strArr = new String[this.fFieldCount];
        int i = 0;
        while (true) {
            int i2 = this.fFieldCount;
            if (i >= i2) {
                return new StringListImpl(strArr, i2);
            }
            strArr[i] = this.fFields[i].toString();
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSIDCDefinition
    public XSObjectList getAnnotations() {
        return new XSObjectListImpl(this.fAnnotations, this.fNumAnnotations);
    }

    public void addAnnotation(XSAnnotationImpl xSAnnotationImpl) {
        if (xSAnnotationImpl != null) {
            XSAnnotationImpl[] xSAnnotationImplArr = this.fAnnotations;
            if (xSAnnotationImplArr == null) {
                this.fAnnotations = new XSAnnotationImpl[2];
            } else {
                int i = this.fNumAnnotations;
                if (i == xSAnnotationImplArr.length) {
                    XSAnnotationImpl[] xSAnnotationImplArr2 = new XSAnnotationImpl[(i << 1)];
                    System.arraycopy(xSAnnotationImplArr, 0, xSAnnotationImplArr2, 0, i);
                    this.fAnnotations = xSAnnotationImplArr2;
                }
            }
            XSAnnotationImpl[] xSAnnotationImplArr3 = this.fAnnotations;
            int i2 = this.fNumAnnotations;
            this.fNumAnnotations = i2 + 1;
            xSAnnotationImplArr3[i2] = xSAnnotationImpl;
        }
    }
}
