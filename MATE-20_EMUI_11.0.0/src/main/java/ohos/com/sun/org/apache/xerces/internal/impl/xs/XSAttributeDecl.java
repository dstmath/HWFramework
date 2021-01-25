package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;

public class XSAttributeDecl implements XSAttributeDeclaration {
    public static final short SCOPE_ABSENT = 0;
    public static final short SCOPE_GLOBAL = 1;
    public static final short SCOPE_LOCAL = 2;
    XSObjectList fAnnotations = null;
    short fConstraintType = 0;
    ValidatedInfo fDefault = null;
    XSComplexTypeDecl fEnclosingCT = null;
    String fName = null;
    private XSNamespaceItem fNamespaceItem = null;
    short fScope = 0;
    String fTargetNamespace = null;
    XSSimpleType fType = null;
    public QName fUnresolvedTypeName = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 1;
    }

    public void setValues(String str, String str2, XSSimpleType xSSimpleType, short s, short s2, ValidatedInfo validatedInfo, XSComplexTypeDecl xSComplexTypeDecl, XSObjectList xSObjectList) {
        this.fName = str;
        this.fTargetNamespace = str2;
        this.fType = xSSimpleType;
        this.fConstraintType = s;
        this.fScope = s2;
        this.fDefault = validatedInfo;
        this.fEnclosingCT = xSComplexTypeDecl;
        this.fAnnotations = xSObjectList;
    }

    public void reset() {
        this.fName = null;
        this.fTargetNamespace = null;
        this.fType = null;
        this.fUnresolvedTypeName = null;
        this.fConstraintType = 0;
        this.fScope = 0;
        this.fDefault = null;
        this.fAnnotations = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return this.fName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return this.fTargetNamespace;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public XSSimpleTypeDefinition getTypeDefinition() {
        return this.fType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public short getScope() {
        return this.fScope;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public XSComplexTypeDefinition getEnclosingCTDefinition() {
        return this.fEnclosingCT;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public short getConstraintType() {
        return this.fConstraintType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public String getConstraintValue() {
        if (getConstraintType() == 0) {
            return null;
        }
        return this.fDefault.stringValue();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public XSAnnotation getAnnotation() {
        XSObjectList xSObjectList = this.fAnnotations;
        if (xSObjectList != null) {
            return (XSAnnotation) xSObjectList.item(0);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public XSObjectList getAnnotations() {
        XSObjectList xSObjectList = this.fAnnotations;
        return xSObjectList != null ? xSObjectList : XSObjectListImpl.EMPTY_LIST;
    }

    public ValidatedInfo getValInfo() {
        return this.fDefault;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return this.fNamespaceItem;
    }

    /* access modifiers changed from: package-private */
    public void setNamespaceItem(XSNamespaceItem xSNamespaceItem) {
        this.fNamespaceItem = xSNamespaceItem;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public Object getActualVC() {
        if (getConstraintType() == 0) {
            return null;
        }
        return this.fDefault.actualValue;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public short getActualVCType() {
        if (getConstraintType() == 0) {
            return 45;
        }
        return this.fDefault.actualValueType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
    public ShortList getItemValueTypes() {
        if (getConstraintType() == 0) {
            return null;
        }
        return this.fDefault.itemValueTypes;
    }
}
