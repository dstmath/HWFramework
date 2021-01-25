package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class XSElementDecl implements XSElementDeclaration {
    private static final short ABSTRACT = 8;
    private static final short CONSTRAINT_MASK = 3;
    static final int INITIAL_SIZE = 2;
    private static final short NILLABLE = 4;
    public static final short SCOPE_ABSENT = 0;
    public static final short SCOPE_GLOBAL = 1;
    public static final short SCOPE_LOCAL = 2;
    public XSObjectList fAnnotations = null;
    public short fBlock = 0;
    public ValidatedInfo fDefault = null;
    private String fDescription = null;
    XSComplexTypeDecl fEnclosingCT = null;
    public short fFinal = 0;
    int fIDCPos = 0;
    IdentityConstraint[] fIDConstraints = new IdentityConstraint[2];
    short fMiscFlags = 0;
    public String fName = null;
    private XSNamespaceItem fNamespaceItem = null;
    public short fScope = 0;
    public XSElementDecl fSubGroup = null;
    public String fTargetNamespace = null;
    public XSTypeDefinition fType = null;
    public QName fUnresolvedTypeName = null;

    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 2;
    }

    public void setConstraintType(short s) {
        short s2 = this.fMiscFlags;
        this.fMiscFlags = (short) (s2 ^ (s2 & 3));
        this.fMiscFlags = (short) ((s & 3) | this.fMiscFlags);
    }

    public void setIsNillable() {
        this.fMiscFlags = (short) (this.fMiscFlags | 4);
    }

    public void setIsAbstract() {
        this.fMiscFlags = (short) (this.fMiscFlags | 8);
    }

    public void setIsGlobal() {
        this.fScope = 1;
    }

    public void setIsLocal(XSComplexTypeDecl xSComplexTypeDecl) {
        this.fScope = 2;
        this.fEnclosingCT = xSComplexTypeDecl;
    }

    public void addIDConstraint(IdentityConstraint identityConstraint) {
        int i = this.fIDCPos;
        IdentityConstraint[] identityConstraintArr = this.fIDConstraints;
        if (i == identityConstraintArr.length) {
            this.fIDConstraints = resize(identityConstraintArr, i * 2);
        }
        IdentityConstraint[] identityConstraintArr2 = this.fIDConstraints;
        int i2 = this.fIDCPos;
        this.fIDCPos = i2 + 1;
        identityConstraintArr2[i2] = identityConstraint;
    }

    public IdentityConstraint[] getIDConstraints() {
        int i = this.fIDCPos;
        if (i == 0) {
            return null;
        }
        IdentityConstraint[] identityConstraintArr = this.fIDConstraints;
        if (i < identityConstraintArr.length) {
            this.fIDConstraints = resize(identityConstraintArr, i);
        }
        return this.fIDConstraints;
    }

    static final IdentityConstraint[] resize(IdentityConstraint[] identityConstraintArr, int i) {
        IdentityConstraint[] identityConstraintArr2 = new IdentityConstraint[i];
        System.arraycopy(identityConstraintArr, 0, identityConstraintArr2, 0, Math.min(identityConstraintArr.length, i));
        return identityConstraintArr2;
    }

    public String toString() {
        if (this.fDescription == null) {
            String str = this.fTargetNamespace;
            if (str != null) {
                int length = str.length();
                String str2 = this.fName;
                StringBuffer stringBuffer = new StringBuffer(length + (str2 != null ? str2.length() : 4) + 3);
                stringBuffer.append('\"');
                stringBuffer.append(this.fTargetNamespace);
                stringBuffer.append('\"');
                stringBuffer.append(':');
                stringBuffer.append(this.fName);
                this.fDescription = stringBuffer.toString();
            } else {
                this.fDescription = this.fName;
            }
        }
        return this.fDescription;
    }

    public int hashCode() {
        int hashCode = this.fName.hashCode();
        String str = this.fTargetNamespace;
        return str != null ? (hashCode << 16) + str.hashCode() : hashCode;
    }

    public void reset() {
        this.fScope = 0;
        this.fName = null;
        this.fTargetNamespace = null;
        this.fType = null;
        this.fUnresolvedTypeName = null;
        this.fMiscFlags = 0;
        this.fBlock = 0;
        this.fFinal = 0;
        this.fDefault = null;
        this.fAnnotations = null;
        this.fSubGroup = null;
        for (int i = 0; i < this.fIDCPos; i++) {
            this.fIDConstraints[i] = null;
        }
        this.fIDCPos = 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return this.fName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return this.fTargetNamespace;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public XSTypeDefinition getTypeDefinition() {
        return this.fType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public short getScope() {
        return this.fScope;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public XSComplexTypeDefinition getEnclosingCTDefinition() {
        return this.fEnclosingCT;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public short getConstraintType() {
        return (short) (this.fMiscFlags & 3);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public String getConstraintValue() {
        if (getConstraintType() == 0) {
            return null;
        }
        return this.fDefault.stringValue();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public boolean getNillable() {
        return (this.fMiscFlags & 4) != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public XSNamedMap getIdentityConstraints() {
        return new XSNamedMapImpl(this.fIDConstraints, this.fIDCPos);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public XSElementDeclaration getSubstitutionGroupAffiliation() {
        return this.fSubGroup;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public boolean isSubstitutionGroupExclusion(short s) {
        return (this.fFinal & s) != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public short getSubstitutionGroupExclusions() {
        return this.fFinal;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public boolean isDisallowedSubstitution(short s) {
        return (this.fBlock & s) != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public short getDisallowedSubstitutions() {
        return this.fBlock;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public boolean getAbstract() {
        return (this.fMiscFlags & 8) != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public XSAnnotation getAnnotation() {
        XSObjectList xSObjectList = this.fAnnotations;
        if (xSObjectList != null) {
            return (XSAnnotation) xSObjectList.item(0);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
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

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public Object getActualVC() {
        if (getConstraintType() == 0) {
            return null;
        }
        return this.fDefault.actualValue;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public short getActualVCType() {
        if (getConstraintType() == 0) {
            return 45;
        }
        return this.fDefault.actualValueType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration
    public ShortList getItemValueTypes() {
        if (getConstraintType() == 0) {
            return null;
        }
        return this.fDefault.itemValueTypes;
    }
}
