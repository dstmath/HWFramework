package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public class XSAttributeUseImpl implements XSAttributeUse {
    public XSObjectList fAnnotations = null;
    public XSAttributeDecl fAttrDecl = null;
    public short fConstraintType = 0;
    public ValidatedInfo fDefault = null;
    public short fUse = 0;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 4;
    }

    public void reset() {
        this.fDefault = null;
        this.fAttrDecl = null;
        this.fUse = 0;
        this.fConstraintType = 0;
        this.fAnnotations = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse
    public boolean getRequired() {
        return this.fUse == 1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse
    public XSAttributeDeclaration getAttrDeclaration() {
        return this.fAttrDecl;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse
    public short getConstraintType() {
        return this.fConstraintType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse
    public String getConstraintValue() {
        if (getConstraintType() == 0) {
            return null;
        }
        return this.fDefault.stringValue();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse
    public Object getActualVC() {
        if (getConstraintType() == 0) {
            return null;
        }
        return this.fDefault.actualValue;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse
    public short getActualVCType() {
        if (getConstraintType() == 0) {
            return 45;
        }
        return this.fDefault.actualValueType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse
    public ShortList getItemValueTypes() {
        if (getConstraintType() == 0) {
            return null;
        }
        return this.fDefault.itemValueTypes;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse
    public XSObjectList getAnnotations() {
        XSObjectList xSObjectList = this.fAnnotations;
        return xSObjectList != null ? xSObjectList : XSObjectListImpl.EMPTY_LIST;
    }
}
