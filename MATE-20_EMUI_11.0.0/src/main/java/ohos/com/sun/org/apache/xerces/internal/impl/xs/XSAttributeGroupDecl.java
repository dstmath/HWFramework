package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSWildcard;

public class XSAttributeGroupDecl implements XSAttributeGroupDefinition {
    private static final int INITIAL_SIZE = 5;
    public XSObjectList fAnnotations;
    int fAttrUseNum = 0;
    protected XSObjectListImpl fAttrUses = null;
    XSAttributeUseImpl[] fAttributeUses = new XSAttributeUseImpl[5];
    public XSWildcardDecl fAttributeWC = null;
    public String fIDAttrName = null;
    public String fName = null;
    private XSNamespaceItem fNamespaceItem = null;
    public String fTargetNamespace = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 5;
    }

    public String addAttributeUse(XSAttributeUseImpl xSAttributeUseImpl) {
        if (xSAttributeUseImpl.fUse != 2 && xSAttributeUseImpl.fAttrDecl.fType.isIDType()) {
            String str = this.fIDAttrName;
            if (str != null) {
                return str;
            }
            this.fIDAttrName = xSAttributeUseImpl.fAttrDecl.fName;
        }
        int i = this.fAttrUseNum;
        XSAttributeUseImpl[] xSAttributeUseImplArr = this.fAttributeUses;
        if (i == xSAttributeUseImplArr.length) {
            this.fAttributeUses = resize(xSAttributeUseImplArr, i * 2);
        }
        XSAttributeUseImpl[] xSAttributeUseImplArr2 = this.fAttributeUses;
        int i2 = this.fAttrUseNum;
        this.fAttrUseNum = i2 + 1;
        xSAttributeUseImplArr2[i2] = xSAttributeUseImpl;
        return null;
    }

    public void replaceAttributeUse(XSAttributeUse xSAttributeUse, XSAttributeUseImpl xSAttributeUseImpl) {
        for (int i = 0; i < this.fAttrUseNum; i++) {
            XSAttributeUseImpl[] xSAttributeUseImplArr = this.fAttributeUses;
            if (xSAttributeUseImplArr[i] == xSAttributeUse) {
                xSAttributeUseImplArr[i] = xSAttributeUseImpl;
            }
        }
    }

    public XSAttributeUse getAttributeUse(String str, String str2) {
        for (int i = 0; i < this.fAttrUseNum; i++) {
            if (this.fAttributeUses[i].fAttrDecl.fTargetNamespace == str && this.fAttributeUses[i].fAttrDecl.fName == str2) {
                return this.fAttributeUses[i];
            }
        }
        return null;
    }

    public XSAttributeUse getAttributeUseNoProhibited(String str, String str2) {
        for (int i = 0; i < this.fAttrUseNum; i++) {
            if (this.fAttributeUses[i].fAttrDecl.fTargetNamespace == str && this.fAttributeUses[i].fAttrDecl.fName == str2 && this.fAttributeUses[i].fUse != 2) {
                return this.fAttributeUses[i];
            }
        }
        return null;
    }

    public void removeProhibitedAttrs() {
        int i = this.fAttrUseNum;
        if (i != 0) {
            XSAttributeUseImpl[] xSAttributeUseImplArr = new XSAttributeUseImpl[i];
            int i2 = 0;
            for (int i3 = 0; i3 < this.fAttrUseNum; i3++) {
                if (this.fAttributeUses[i3].fUse != 2) {
                    xSAttributeUseImplArr[i2] = this.fAttributeUses[i3];
                    i2++;
                }
            }
            this.fAttributeUses = xSAttributeUseImplArr;
            this.fAttrUseNum = i2;
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x003b: APUT  (r13v16 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r14v28 java.lang.String) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00fe: APUT  (r13v12 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r14v10 java.lang.String) */
    public Object[] validRestrictionOf(String str, XSAttributeGroupDecl xSAttributeGroupDecl) {
        for (int i = 0; i < this.fAttrUseNum; i++) {
            XSAttributeUseImpl xSAttributeUseImpl = this.fAttributeUses[i];
            XSAttributeDecl xSAttributeDecl = xSAttributeUseImpl.fAttrDecl;
            XSAttributeUseImpl xSAttributeUseImpl2 = (XSAttributeUseImpl) xSAttributeGroupDecl.getAttributeUse(xSAttributeDecl.fTargetNamespace, xSAttributeDecl.fName);
            if (xSAttributeUseImpl2 == null) {
                XSWildcardDecl xSWildcardDecl = xSAttributeGroupDecl.fAttributeWC;
                if (xSWildcardDecl == null) {
                    return new Object[]{str, xSAttributeDecl.fName, "derivation-ok-restriction.2.2.a"};
                }
                if (!xSWildcardDecl.allowNamespace(xSAttributeDecl.fTargetNamespace)) {
                    Object[] objArr = new Object[4];
                    objArr[0] = str;
                    objArr[1] = xSAttributeDecl.fName;
                    objArr[2] = xSAttributeDecl.fTargetNamespace == null ? "" : xSAttributeDecl.fTargetNamespace;
                    objArr[3] = "derivation-ok-restriction.2.2.b";
                    return objArr;
                }
            } else if (xSAttributeUseImpl2.getRequired() && !xSAttributeUseImpl.getRequired()) {
                Object[] objArr2 = new Object[4];
                objArr2[0] = str;
                objArr2[1] = xSAttributeDecl.fName;
                objArr2[2] = xSAttributeUseImpl.fUse == 0 ? SchemaSymbols.ATTVAL_OPTIONAL : SchemaSymbols.ATTVAL_PROHIBITED;
                objArr2[3] = "derivation-ok-restriction.2.1.1";
                return objArr2;
            } else if (xSAttributeUseImpl.fUse == 2) {
                continue;
            } else {
                XSAttributeDecl xSAttributeDecl2 = xSAttributeUseImpl2.fAttrDecl;
                if (!XSConstraints.checkSimpleDerivationOk(xSAttributeDecl.fType, xSAttributeDecl2.fType, xSAttributeDecl2.fType.getFinal())) {
                    return new Object[]{str, xSAttributeDecl.fName, xSAttributeDecl.fType.getName(), xSAttributeDecl2.fType.getName(), "derivation-ok-restriction.2.1.2"};
                }
                short constraintType = xSAttributeUseImpl2.fConstraintType != 0 ? xSAttributeUseImpl2.fConstraintType : xSAttributeDecl2.getConstraintType();
                short constraintType2 = xSAttributeUseImpl.fConstraintType != 0 ? xSAttributeUseImpl.fConstraintType : xSAttributeDecl.getConstraintType();
                if (constraintType != 2) {
                    continue;
                } else if (constraintType2 != 2) {
                    return new Object[]{str, xSAttributeDecl.fName, "derivation-ok-restriction.2.1.3.a"};
                } else {
                    ValidatedInfo validatedInfo = xSAttributeUseImpl2.fDefault != null ? xSAttributeUseImpl2.fDefault : xSAttributeDecl2.fDefault;
                    ValidatedInfo validatedInfo2 = xSAttributeUseImpl.fDefault != null ? xSAttributeUseImpl.fDefault : xSAttributeDecl.fDefault;
                    if (!validatedInfo.actualValue.equals(validatedInfo2.actualValue)) {
                        return new Object[]{str, xSAttributeDecl.fName, validatedInfo2.stringValue(), validatedInfo.stringValue(), "derivation-ok-restriction.2.1.3.b"};
                    }
                }
            }
        }
        for (int i2 = 0; i2 < xSAttributeGroupDecl.fAttrUseNum; i2++) {
            XSAttributeUseImpl xSAttributeUseImpl3 = xSAttributeGroupDecl.fAttributeUses[i2];
            if (xSAttributeUseImpl3.fUse == 1) {
                XSAttributeDecl xSAttributeDecl3 = xSAttributeUseImpl3.fAttrDecl;
                if (getAttributeUse(xSAttributeDecl3.fTargetNamespace, xSAttributeDecl3.fName) == null) {
                    return new Object[]{str, xSAttributeUseImpl3.fAttrDecl.fName, "derivation-ok-restriction.3"};
                }
            }
        }
        XSWildcardDecl xSWildcardDecl2 = this.fAttributeWC;
        if (xSWildcardDecl2 == null) {
            return null;
        }
        XSWildcardDecl xSWildcardDecl3 = xSAttributeGroupDecl.fAttributeWC;
        if (xSWildcardDecl3 == null) {
            return new Object[]{str, "derivation-ok-restriction.4.1"};
        }
        if (!xSWildcardDecl2.isSubsetOf(xSWildcardDecl3)) {
            return new Object[]{str, "derivation-ok-restriction.4.2"};
        }
        if (this.fAttributeWC.weakerProcessContents(xSAttributeGroupDecl.fAttributeWC)) {
            return new Object[]{str, this.fAttributeWC.getProcessContentsAsString(), xSAttributeGroupDecl.fAttributeWC.getProcessContentsAsString(), "derivation-ok-restriction.4.3"};
        }
        return null;
    }

    static final XSAttributeUseImpl[] resize(XSAttributeUseImpl[] xSAttributeUseImplArr, int i) {
        XSAttributeUseImpl[] xSAttributeUseImplArr2 = new XSAttributeUseImpl[i];
        System.arraycopy(xSAttributeUseImplArr, 0, xSAttributeUseImplArr2, 0, Math.min(xSAttributeUseImplArr.length, i));
        return xSAttributeUseImplArr2;
    }

    public void reset() {
        this.fName = null;
        this.fTargetNamespace = null;
        for (int i = 0; i < this.fAttrUseNum; i++) {
            this.fAttributeUses[i] = null;
        }
        this.fAttrUseNum = 0;
        this.fAttributeWC = null;
        this.fAnnotations = null;
        this.fIDAttrName = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return this.fName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return this.fTargetNamespace;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition
    public XSObjectList getAttributeUses() {
        if (this.fAttrUses == null) {
            this.fAttrUses = new XSObjectListImpl(this.fAttributeUses, this.fAttrUseNum);
        }
        return this.fAttrUses;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition
    public XSWildcard getAttributeWildcard() {
        return this.fAttributeWC;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition
    public XSAnnotation getAnnotation() {
        XSObjectList xSObjectList = this.fAnnotations;
        if (xSObjectList != null) {
            return (XSAnnotation) xSObjectList.item(0);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition
    public XSObjectList getAnnotations() {
        XSObjectList xSObjectList = this.fAnnotations;
        return xSObjectList != null ? xSObjectList : XSObjectListImpl.EMPTY_LIST;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return this.fNamespaceItem;
    }

    /* access modifiers changed from: package-private */
    public void setNamespaceItem(XSNamespaceItem xSNamespaceItem) {
        this.fNamespaceItem = xSNamespaceItem;
    }
}
