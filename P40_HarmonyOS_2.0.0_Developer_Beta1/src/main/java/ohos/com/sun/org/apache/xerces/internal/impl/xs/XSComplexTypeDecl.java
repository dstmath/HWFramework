package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.models.CMBuilder;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSParticle;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSWildcard;
import ohos.org.w3c.dom.TypeInfo;

public class XSComplexTypeDecl implements XSComplexTypeDefinition, TypeInfo {
    private static final short CT_HAS_TYPE_ID = 2;
    private static final short CT_IS_ABSTRACT = 1;
    private static final short CT_IS_ANONYMOUS = 4;
    static final int DERIVATION_ANY = 0;
    static final int DERIVATION_EXTENSION = 2;
    static final int DERIVATION_LIST = 8;
    static final int DERIVATION_RESTRICTION = 1;
    static final int DERIVATION_UNION = 4;
    XSObjectListImpl fAnnotations = null;
    XSAttributeGroupDecl fAttrGrp = null;
    XSTypeDefinition fBaseType = null;
    short fBlock = 0;
    volatile XSCMValidator fCMValidator = null;
    short fContentType = 0;
    short fDerivedBy = 2;
    short fFinal = 0;
    short fMiscFlags = 0;
    String fName = null;
    private XSNamespaceItem fNamespaceItem = null;
    XSParticleDecl fParticle = null;
    String fTargetNamespace = null;
    XSCMValidator fUPACMValidator = null;
    XSSimpleType fXSSimpleType = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 3;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public short getTypeCategory() {
        return 15;
    }

    public void setValues(String str, String str2, XSTypeDefinition xSTypeDefinition, short s, short s2, short s3, short s4, boolean z, XSAttributeGroupDecl xSAttributeGroupDecl, XSSimpleType xSSimpleType, XSParticleDecl xSParticleDecl, XSObjectListImpl xSObjectListImpl) {
        this.fTargetNamespace = str2;
        this.fBaseType = xSTypeDefinition;
        this.fDerivedBy = s;
        this.fFinal = s2;
        this.fBlock = s3;
        this.fContentType = s4;
        if (z) {
            this.fMiscFlags = (short) (this.fMiscFlags | 1);
        }
        this.fAttrGrp = xSAttributeGroupDecl;
        this.fXSSimpleType = xSSimpleType;
        this.fParticle = xSParticleDecl;
        this.fAnnotations = xSObjectListImpl;
    }

    public void setName(String str) {
        this.fName = str;
    }

    public String getTypeName() {
        return this.fName;
    }

    public short getFinalSet() {
        return this.fFinal;
    }

    public String getTargetNamespace() {
        return this.fTargetNamespace;
    }

    public boolean containsTypeID() {
        return (this.fMiscFlags & 2) != 0;
    }

    public void setIsAbstractType() {
        this.fMiscFlags = (short) (this.fMiscFlags | 1);
    }

    public void setContainsTypeID() {
        this.fMiscFlags = (short) (this.fMiscFlags | 2);
    }

    public void setIsAnonymous() {
        this.fMiscFlags = (short) (this.fMiscFlags | 4);
    }

    public XSCMValidator getContentModel(CMBuilder cMBuilder) {
        short s = this.fContentType;
        if (s == 1 || s == 0) {
            return null;
        }
        if (this.fCMValidator == null) {
            synchronized (this) {
                if (this.fCMValidator == null) {
                    this.fCMValidator = cMBuilder.getContentModel(this);
                }
            }
        }
        return this.fCMValidator;
    }

    public XSAttributeGroupDecl getAttrGrp() {
        return this.fAttrGrp;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(192);
        appendTypeInfo(sb);
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void appendTypeInfo(StringBuilder sb) {
        String[] strArr = {"EMPTY", "SIMPLE", "ELEMENT", "MIXED"};
        String[] strArr2 = {"EMPTY", "EXTENSION", "RESTRICTION"};
        sb.append("Complex type name='");
        sb.append(this.fTargetNamespace);
        sb.append(',');
        sb.append(getTypeName());
        sb.append("', ");
        if (this.fBaseType != null) {
            sb.append(" base type name='");
            sb.append(this.fBaseType.getName());
            sb.append("', ");
        }
        sb.append(" content type='");
        sb.append(strArr[this.fContentType]);
        sb.append("', ");
        sb.append(" isAbstract='");
        sb.append(getAbstract());
        sb.append("', ");
        sb.append(" hasTypeId='");
        sb.append(containsTypeID());
        sb.append("', ");
        sb.append(" final='");
        sb.append((int) this.fFinal);
        sb.append("', ");
        sb.append(" block='");
        sb.append((int) this.fBlock);
        sb.append("', ");
        if (this.fParticle != null) {
            sb.append(" particle='");
            sb.append(this.fParticle.toString());
            sb.append("', ");
        }
        sb.append(" derivedBy='");
        sb.append(strArr2[this.fDerivedBy]);
        sb.append("'. ");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v1, types: [ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition] */
    /* JADX WARN: Type inference failed for: r2v3 */
    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean derivedFromType(XSTypeDefinition xSTypeDefinition, short s) {
        if (xSTypeDefinition == null) {
            return false;
        }
        ?? r2 = this;
        if (xSTypeDefinition == SchemaGrammar.fAnyType) {
            return true;
        }
        while (r2 != xSTypeDefinition && r2 != SchemaGrammar.fAnySimpleType && r2 != SchemaGrammar.fAnyType) {
            r2 = r2.getBaseType();
        }
        return r2 == xSTypeDefinition;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:32:0x0018 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v0, types: [ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl] */
    /* JADX WARN: Type inference failed for: r2v1, types: [ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition] */
    /* JADX WARN: Type inference failed for: r2v2, types: [ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition] */
    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean derivedFrom(String str, String str2, short s) {
        if (str2 == null) {
            return false;
        }
        if (str != null && str.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) && str2.equals(SchemaSymbols.ATTVAL_ANYTYPE)) {
            return true;
        }
        while (true) {
            if (!((str2.equals(this.getName()) && ((str == null && this.getNamespace() == null) || (str != null && str.equals(this.getNamespace())))) || this == SchemaGrammar.fAnySimpleType || this == SchemaGrammar.fAnyType)) {
                this = this.getBaseType();
            }
        }
        return (this == SchemaGrammar.fAnySimpleType || this == SchemaGrammar.fAnyType) ? false : true;
    }

    public boolean isDOMDerivedFrom(String str, String str2, int i) {
        if (str2 == null) {
            return false;
        }
        if (str != null && str.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) && str2.equals(SchemaSymbols.ATTVAL_ANYTYPE) && i == 1 && i == 2) {
            return true;
        }
        int i2 = i & 1;
        if (i2 != 0 && isDerivedByRestriction(str, str2, i, this)) {
            return true;
        }
        int i3 = i & 2;
        if (i3 != 0 && isDerivedByExtension(str, str2, i, this)) {
            return true;
        }
        int i4 = i & 8;
        if (!(i4 == 0 && (i & 4) == 0) && i2 == 0 && i3 == 0) {
            if (str.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) && str2.equals(SchemaSymbols.ATTVAL_ANYTYPE)) {
                str2 = SchemaSymbols.ATTVAL_ANYSIMPLETYPE;
            }
            if (!this.fName.equals(SchemaSymbols.ATTVAL_ANYTYPE) || !this.fTargetNamespace.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)) {
                XSTypeDefinition xSTypeDefinition = this.fBaseType;
                if (xSTypeDefinition != null && (xSTypeDefinition instanceof XSSimpleTypeDecl)) {
                    return ((XSSimpleTypeDecl) xSTypeDefinition).isDOMDerivedFrom(str, str2, i);
                }
                XSTypeDefinition xSTypeDefinition2 = this.fBaseType;
                if (xSTypeDefinition2 != null && (xSTypeDefinition2 instanceof XSComplexTypeDecl)) {
                    return ((XSComplexTypeDecl) xSTypeDefinition2).isDOMDerivedFrom(str, str2, i);
                }
            }
        }
        if (i3 == 0 && i2 == 0 && i4 == 0 && (i & 4) == 0) {
            return isDerivedByAny(str, str2, i, this);
        }
        return false;
    }

    private boolean isDerivedByAny(String str, String str2, int i, XSTypeDefinition xSTypeDefinition) {
        XSTypeDefinition xSTypeDefinition2 = null;
        while (true) {
            xSTypeDefinition2 = xSTypeDefinition;
            if (xSTypeDefinition2 == null || xSTypeDefinition2 == xSTypeDefinition2) {
                break;
            }
            if (str2.equals(xSTypeDefinition2.getName())) {
                if (str == null && xSTypeDefinition2.getNamespace() == null) {
                    return true;
                }
                if (str != null && str.equals(xSTypeDefinition2.getNamespace())) {
                    return true;
                }
            }
            if (isDerivedByRestriction(str, str2, i, xSTypeDefinition2) || !isDerivedByExtension(str, str2, i, xSTypeDefinition2)) {
                return true;
            }
            xSTypeDefinition = xSTypeDefinition2.getBaseType();
        }
        return false;
    }

    private boolean isDerivedByRestriction(String str, String str2, int i, XSTypeDefinition xSTypeDefinition) {
        XSTypeDefinition xSTypeDefinition2 = null;
        while (true) {
            xSTypeDefinition2 = xSTypeDefinition;
            if (xSTypeDefinition2 == null || xSTypeDefinition2 == xSTypeDefinition2) {
                break;
            } else if (str != null && str.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) && str2.equals(SchemaSymbols.ATTVAL_ANYSIMPLETYPE)) {
                return false;
            } else {
                if (str2.equals(xSTypeDefinition2.getName()) && str != null && str.equals(xSTypeDefinition2.getNamespace())) {
                    return true;
                }
                if (xSTypeDefinition2.getNamespace() == null && str == null) {
                    return true;
                }
                if (xSTypeDefinition2 instanceof XSSimpleTypeDecl) {
                    if (str.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) && str2.equals(SchemaSymbols.ATTVAL_ANYTYPE)) {
                        str2 = SchemaSymbols.ATTVAL_ANYSIMPLETYPE;
                    }
                    return ((XSSimpleTypeDecl) xSTypeDefinition2).isDOMDerivedFrom(str, str2, i);
                } else if (((XSComplexTypeDecl) xSTypeDefinition2).getDerivationMethod() != 2) {
                    return false;
                } else {
                    xSTypeDefinition = xSTypeDefinition2.getBaseType();
                }
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0053, code lost:
        return r1;
     */
    private boolean isDerivedByExtension(String str, String str2, int i, XSTypeDefinition xSTypeDefinition) {
        boolean isDOMDerivedFrom;
        XSTypeDefinition xSTypeDefinition2 = null;
        boolean z = false;
        while (true) {
            xSTypeDefinition2 = xSTypeDefinition;
            if (xSTypeDefinition2 == null || xSTypeDefinition2 == xSTypeDefinition2 || (str != null && str.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) && str2.equals(SchemaSymbols.ATTVAL_ANYSIMPLETYPE) && SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(xSTypeDefinition2.getNamespace()) && SchemaSymbols.ATTVAL_ANYTYPE.equals(xSTypeDefinition2.getName()))) {
                break;
            } else if (!str2.equals(xSTypeDefinition2.getName()) || (!(str == null && xSTypeDefinition2.getNamespace() == null) && (str == null || !str.equals(xSTypeDefinition2.getNamespace())))) {
                if (xSTypeDefinition2 instanceof XSSimpleTypeDecl) {
                    if (str.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) && str2.equals(SchemaSymbols.ATTVAL_ANYTYPE)) {
                        str2 = SchemaSymbols.ATTVAL_ANYSIMPLETYPE;
                    }
                    if ((i & 2) != 0) {
                        isDOMDerivedFrom = ((XSSimpleTypeDecl) xSTypeDefinition2).isDOMDerivedFrom(str, str2, i & 1);
                    } else {
                        isDOMDerivedFrom = ((XSSimpleTypeDecl) xSTypeDefinition2).isDOMDerivedFrom(str, str2, i);
                    }
                    return isDOMDerivedFrom & z;
                }
                if (((XSComplexTypeDecl) xSTypeDefinition2).getDerivationMethod() == 1) {
                    z |= true;
                }
                xSTypeDefinition = xSTypeDefinition2.getBaseType();
            }
        }
        return false;
    }

    public void reset() {
        this.fName = null;
        this.fTargetNamespace = null;
        this.fBaseType = null;
        this.fDerivedBy = 2;
        this.fFinal = 0;
        this.fBlock = 0;
        this.fMiscFlags = 0;
        this.fAttrGrp.reset();
        this.fContentType = 0;
        this.fXSSimpleType = null;
        this.fParticle = null;
        this.fCMValidator = null;
        this.fUPACMValidator = null;
        XSObjectListImpl xSObjectListImpl = this.fAnnotations;
        if (xSObjectListImpl != null) {
            xSObjectListImpl.clearXSObjectList();
        }
        this.fAnnotations = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        if (getAnonymous()) {
            return null;
        }
        return this.fName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean getAnonymous() {
        return (this.fMiscFlags & 4) != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return this.fTargetNamespace;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public XSTypeDefinition getBaseType() {
        return this.fBaseType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public short getDerivationMethod() {
        return this.fDerivedBy;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean isFinal(short s) {
        return (this.fFinal & s) != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public short getFinal() {
        return this.fFinal;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public boolean getAbstract() {
        return (this.fMiscFlags & 1) != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public XSObjectList getAttributeUses() {
        return this.fAttrGrp.getAttributeUses();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public XSWildcard getAttributeWildcard() {
        return this.fAttrGrp.getAttributeWildcard();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public short getContentType() {
        return this.fContentType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public XSSimpleTypeDefinition getSimpleType() {
        return this.fXSSimpleType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public XSParticle getParticle() {
        return this.fParticle;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public boolean isProhibitedSubstitution(short s) {
        return (this.fBlock & s) != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public short getProhibitedSubstitutions() {
        return this.fBlock;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
    public XSObjectList getAnnotations() {
        XSObjectListImpl xSObjectListImpl = this.fAnnotations;
        return xSObjectListImpl != null ? xSObjectListImpl : XSObjectListImpl.EMPTY_LIST;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return this.fNamespaceItem;
    }

    /* access modifiers changed from: package-private */
    public void setNamespaceItem(XSNamespaceItem xSNamespaceItem) {
        this.fNamespaceItem = xSNamespaceItem;
    }

    public XSAttributeUse getAttributeUse(String str, String str2) {
        return this.fAttrGrp.getAttributeUse(str, str2);
    }

    public String getTypeNamespace() {
        return getNamespace();
    }

    public boolean isDerivedFrom(String str, String str2, int i) {
        return isDOMDerivedFrom(str, str2, i);
    }
}
