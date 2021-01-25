package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.models.CMBuilder;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class XSConstraints {
    private static final Comparator ELEMENT_PARTICLE_COMPARATOR = new Comparator() {
        /* class ohos.com.sun.org.apache.xerces.internal.impl.xs.XSConstraints.AnonymousClass1 */

        @Override // java.util.Comparator
        public int compare(Object obj, Object obj2) {
            XSElementDecl xSElementDecl = (XSElementDecl) ((XSParticleDecl) obj).fValue;
            XSElementDecl xSElementDecl2 = (XSElementDecl) ((XSParticleDecl) obj2).fValue;
            String namespace = xSElementDecl.getNamespace();
            String namespace2 = xSElementDecl2.getNamespace();
            String name = xSElementDecl.getName();
            String name2 = xSElementDecl2.getName();
            int i = 1;
            if (namespace == namespace2) {
                i = 0;
            } else if (namespace == null) {
                i = -1;
            } else if (namespace2 != null) {
                i = namespace.compareTo(namespace2);
            }
            return i != 0 ? i : name.compareTo(name2);
        }
    };
    static final int OCCURRENCE_UNKNOWN = -2;
    static final XSSimpleType STRING_TYPE = ((XSSimpleType) SchemaGrammar.SG_SchemaNS.getGlobalTypeDecl("string"));
    private static XSParticleDecl fEmptyParticle = null;

    private static void checkIDConstraintRestriction(XSElementDecl xSElementDecl, XSElementDecl xSElementDecl2) throws XMLSchemaException {
    }

    private static boolean checkOccurrenceRange(int i, int i2, int i3, int i4) {
        if (i < i3) {
            return false;
        }
        if (i4 != -1) {
            return i2 != -1 && i2 <= i4;
        }
        return true;
    }

    public static XSParticleDecl getEmptySequence() {
        if (fEmptyParticle == null) {
            XSModelGroupImpl xSModelGroupImpl = new XSModelGroupImpl();
            xSModelGroupImpl.fCompositor = 102;
            xSModelGroupImpl.fParticleCount = 0;
            xSModelGroupImpl.fParticles = null;
            xSModelGroupImpl.fAnnotations = XSObjectListImpl.EMPTY_LIST;
            XSParticleDecl xSParticleDecl = new XSParticleDecl();
            xSParticleDecl.fType = 3;
            xSParticleDecl.fValue = xSModelGroupImpl;
            xSParticleDecl.fAnnotations = XSObjectListImpl.EMPTY_LIST;
            fEmptyParticle = xSParticleDecl;
        }
        return fEmptyParticle;
    }

    public static boolean checkTypeDerivationOk(XSTypeDefinition xSTypeDefinition, XSTypeDefinition xSTypeDefinition2, short s) {
        if (xSTypeDefinition == SchemaGrammar.fAnyType) {
            return xSTypeDefinition == xSTypeDefinition2;
        }
        if (xSTypeDefinition == SchemaGrammar.fAnySimpleType) {
            return xSTypeDefinition2 == SchemaGrammar.fAnyType || xSTypeDefinition2 == SchemaGrammar.fAnySimpleType;
        }
        if (xSTypeDefinition.getTypeCategory() != 16) {
            return checkComplexDerivation((XSComplexTypeDecl) xSTypeDefinition, xSTypeDefinition2, s);
        }
        if (xSTypeDefinition2.getTypeCategory() == 15) {
            if (xSTypeDefinition2 != SchemaGrammar.fAnyType) {
                return false;
            }
            xSTypeDefinition2 = SchemaGrammar.fAnySimpleType;
        }
        return checkSimpleDerivation((XSSimpleType) xSTypeDefinition, (XSSimpleType) xSTypeDefinition2, s);
    }

    public static boolean checkSimpleDerivationOk(XSSimpleType xSSimpleType, XSTypeDefinition xSTypeDefinition, short s) {
        if (xSSimpleType != SchemaGrammar.fAnySimpleType) {
            if (xSTypeDefinition.getTypeCategory() == 15) {
                if (xSTypeDefinition != SchemaGrammar.fAnyType) {
                    return false;
                }
                xSTypeDefinition = SchemaGrammar.fAnySimpleType;
            }
            return checkSimpleDerivation(xSSimpleType, (XSSimpleType) xSTypeDefinition, s);
        } else if (xSTypeDefinition == SchemaGrammar.fAnyType || xSTypeDefinition == SchemaGrammar.fAnySimpleType) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkComplexDerivationOk(XSComplexTypeDecl xSComplexTypeDecl, XSTypeDefinition xSTypeDefinition, short s) {
        if (xSComplexTypeDecl == SchemaGrammar.fAnyType) {
            return xSComplexTypeDecl == xSTypeDefinition;
        }
        return checkComplexDerivation(xSComplexTypeDecl, xSTypeDefinition, s);
    }

    private static boolean checkSimpleDerivation(XSSimpleType xSSimpleType, XSSimpleType xSSimpleType2, short s) {
        if (xSSimpleType == xSSimpleType2) {
            return true;
        }
        if ((s & 2) == 0 && (xSSimpleType.getBaseType().getFinal() & 2) == 0) {
            XSSimpleType xSSimpleType3 = (XSSimpleType) xSSimpleType.getBaseType();
            if (xSSimpleType3 == xSSimpleType2) {
                return true;
            }
            if (xSSimpleType3 != SchemaGrammar.fAnySimpleType && checkSimpleDerivation(xSSimpleType3, xSSimpleType2, s)) {
                return true;
            }
            if ((xSSimpleType.getVariety() == 2 || xSSimpleType.getVariety() == 3) && xSSimpleType2 == SchemaGrammar.fAnySimpleType) {
                return true;
            }
            if (xSSimpleType2.getVariety() == 3) {
                XSObjectList memberTypes = xSSimpleType2.getMemberTypes();
                int length = memberTypes.getLength();
                for (int i = 0; i < length; i++) {
                    if (checkSimpleDerivation(xSSimpleType, (XSSimpleType) memberTypes.item(i), s)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkComplexDerivation(XSComplexTypeDecl xSComplexTypeDecl, XSTypeDefinition xSTypeDefinition, short s) {
        if (xSComplexTypeDecl == xSTypeDefinition) {
            return true;
        }
        if ((xSComplexTypeDecl.fDerivedBy & s) != 0) {
            return false;
        }
        XSTypeDefinition xSTypeDefinition2 = xSComplexTypeDecl.fBaseType;
        if (xSTypeDefinition2 == xSTypeDefinition) {
            return true;
        }
        if (!(xSTypeDefinition2 == SchemaGrammar.fAnyType || xSTypeDefinition2 == SchemaGrammar.fAnySimpleType)) {
            if (xSTypeDefinition2.getTypeCategory() == 15) {
                return checkComplexDerivation((XSComplexTypeDecl) xSTypeDefinition2, xSTypeDefinition, s);
            }
            if (xSTypeDefinition2.getTypeCategory() == 16) {
                if (xSTypeDefinition.getTypeCategory() == 15) {
                    if (xSTypeDefinition != SchemaGrammar.fAnyType) {
                        return false;
                    }
                    xSTypeDefinition = SchemaGrammar.fAnySimpleType;
                }
                return checkSimpleDerivation((XSSimpleType) xSTypeDefinition2, (XSSimpleType) xSTypeDefinition, s);
            }
        }
        return false;
    }

    public static Object ElementDefaultValidImmediate(XSTypeDefinition xSTypeDefinition, String str, ValidationContext validationContext, ValidatedInfo validatedInfo) {
        XSSimpleType xSSimpleType;
        if (xSTypeDefinition.getTypeCategory() == 16) {
            xSSimpleType = (XSSimpleType) xSTypeDefinition;
        } else {
            XSComplexTypeDecl xSComplexTypeDecl = (XSComplexTypeDecl) xSTypeDefinition;
            if (xSComplexTypeDecl.fContentType == 1) {
                xSSimpleType = xSComplexTypeDecl.fXSSimpleType;
            } else if (xSComplexTypeDecl.fContentType != 3 || !((XSParticleDecl) xSComplexTypeDecl.getParticle()).emptiable()) {
                return null;
            } else {
                xSSimpleType = null;
            }
        }
        if (xSSimpleType == null) {
            xSSimpleType = STRING_TYPE;
        }
        try {
            return validatedInfo != null ? xSSimpleType.validate(validatedInfo.stringValue(), validationContext, validatedInfo) : xSSimpleType.validate(str, validationContext, validatedInfo);
        } catch (InvalidDatatypeValueException unused) {
            return null;
        }
    }

    static void reportSchemaError(XMLErrorReporter xMLErrorReporter, SimpleLocator simpleLocator, String str, Object[] objArr) {
        if (simpleLocator != null) {
            xMLErrorReporter.reportError((XMLLocator) simpleLocator, XSMessageFormatter.SCHEMA_DOMAIN, str, objArr, (short) 1);
        } else {
            xMLErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, str, objArr, 1);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:61:0x0195 A[SYNTHETIC, Splitter:B:61:0x0195] */
    public static void fullSchemaChecking(XSGrammarBucket xSGrammarBucket, SubstitutionGroupHandler substitutionGroupHandler, CMBuilder cMBuilder, XMLErrorReporter xMLErrorReporter) {
        boolean z;
        XSCMValidator contentModel;
        boolean z2;
        int i;
        SchemaGrammar[] grammars = xSGrammarBucket.getGrammars();
        int i2 = 1;
        for (int length = grammars.length - 1; length >= 0; length--) {
            substitutionGroupHandler.addSubstitutionGroup(grammars[length].getSubstitutionGroups());
        }
        XSParticleDecl xSParticleDecl = new XSParticleDecl();
        XSParticleDecl xSParticleDecl2 = new XSParticleDecl();
        xSParticleDecl.fType = 3;
        xSParticleDecl2.fType = 3;
        for (int length2 = grammars.length - 1; length2 >= 0; length2--) {
            XSGroupDecl[] redefinedGroupDecls = grammars[length2].getRedefinedGroupDecls();
            SimpleLocator[] rGLocators = grammars[length2].getRGLocators();
            int i3 = 0;
            while (i3 < redefinedGroupDecls.length) {
                int i4 = i3 + 1;
                XSGroupDecl xSGroupDecl = redefinedGroupDecls[i3];
                XSModelGroupImpl xSModelGroupImpl = xSGroupDecl.fModelGroup;
                int i5 = i4 + 1;
                XSModelGroupImpl xSModelGroupImpl2 = redefinedGroupDecls[i4].fModelGroup;
                xSParticleDecl.fValue = xSModelGroupImpl;
                xSParticleDecl2.fValue = xSModelGroupImpl2;
                if (xSModelGroupImpl2 == null) {
                    if (xSModelGroupImpl != null) {
                        SimpleLocator simpleLocator = rGLocators[(i5 / 2) - i2];
                        Object[] objArr = new Object[2];
                        objArr[0] = xSGroupDecl.fName;
                        objArr[i2] = "rcase-Recurse.2";
                        reportSchemaError(xMLErrorReporter, simpleLocator, "src-redefine.6.2.2", objArr);
                    }
                } else if (xSModelGroupImpl != null) {
                    try {
                        particleValidRestriction(xSParticleDecl, substitutionGroupHandler, xSParticleDecl2, substitutionGroupHandler);
                    } catch (XMLSchemaException e) {
                        String key = e.getKey();
                        int i6 = (i5 / 2) - i2;
                        reportSchemaError(xMLErrorReporter, rGLocators[i6], key, e.getArgs());
                        i = 1;
                        reportSchemaError(xMLErrorReporter, rGLocators[i6], "src-redefine.6.2.2", new Object[]{xSGroupDecl.fName, key});
                    }
                } else if (!xSParticleDecl2.emptiable()) {
                    SimpleLocator simpleLocator2 = rGLocators[(i5 / 2) - i2];
                    Object[] objArr2 = new Object[2];
                    objArr2[0] = xSGroupDecl.fName;
                    objArr2[i2] = "rcase-Recurse.2";
                    reportSchemaError(xMLErrorReporter, simpleLocator2, "src-redefine.6.2.2", objArr2);
                }
                i = i2;
                i2 = i;
                i3 = i5;
            }
        }
        SymbolHash symbolHash = new SymbolHash();
        for (int length3 = grammars.length - i2; length3 >= 0; length3--) {
            boolean z3 = grammars[length3].fFullChecked;
            XSComplexTypeDecl[] uncheckedComplexTypeDecls = grammars[length3].getUncheckedComplexTypeDecls();
            SimpleLocator[] uncheckedCTLocators = grammars[length3].getUncheckedCTLocators();
            int i7 = 0;
            for (int i8 = 0; i8 < uncheckedComplexTypeDecls.length; i8++) {
                if (!z3 && uncheckedComplexTypeDecls[i8].fParticle != null) {
                    symbolHash.clear();
                    try {
                        checkElementDeclsConsistent(uncheckedComplexTypeDecls[i8], uncheckedComplexTypeDecls[i8].fParticle, symbolHash, substitutionGroupHandler);
                    } catch (XMLSchemaException e2) {
                        reportSchemaError(xMLErrorReporter, uncheckedCTLocators[i8], e2.getKey(), e2.getArgs());
                    }
                }
                if (uncheckedComplexTypeDecls[i8].fBaseType != null && uncheckedComplexTypeDecls[i8].fBaseType != SchemaGrammar.fAnyType && uncheckedComplexTypeDecls[i8].fDerivedBy == 2 && (uncheckedComplexTypeDecls[i8].fBaseType instanceof XSComplexTypeDecl)) {
                    XSParticleDecl xSParticleDecl3 = uncheckedComplexTypeDecls[i8].fParticle;
                    XSParticleDecl xSParticleDecl4 = ((XSComplexTypeDecl) uncheckedComplexTypeDecls[i8].fBaseType).fParticle;
                    if (xSParticleDecl3 != null) {
                        if (xSParticleDecl4 != null) {
                            try {
                                particleValidRestriction(uncheckedComplexTypeDecls[i8].fParticle, substitutionGroupHandler, ((XSComplexTypeDecl) uncheckedComplexTypeDecls[i8].fBaseType).fParticle, substitutionGroupHandler);
                            } catch (XMLSchemaException e3) {
                                reportSchemaError(xMLErrorReporter, uncheckedCTLocators[i8], e3.getKey(), e3.getArgs());
                                z = false;
                                reportSchemaError(xMLErrorReporter, uncheckedCTLocators[i8], "derivation-ok-restriction.5.4.2", new Object[]{uncheckedComplexTypeDecls[i8].fName});
                            }
                        } else {
                            z = false;
                            reportSchemaError(xMLErrorReporter, uncheckedCTLocators[i8], "derivation-ok-restriction.5.4.2", new Object[]{uncheckedComplexTypeDecls[i8].fName});
                        }
                        contentModel = uncheckedComplexTypeDecls[i8].getContentModel(cMBuilder);
                        if (contentModel != null) {
                            try {
                                z2 = contentModel.checkUniqueParticleAttribution(substitutionGroupHandler);
                            } catch (XMLSchemaException e4) {
                                reportSchemaError(xMLErrorReporter, uncheckedCTLocators[i8], e4.getKey(), e4.getArgs());
                            }
                            if (!z3 && z2) {
                                uncheckedComplexTypeDecls[i7] = uncheckedComplexTypeDecls[i8];
                                i7++;
                            }
                        }
                        z2 = z;
                        uncheckedComplexTypeDecls[i7] = uncheckedComplexTypeDecls[i8];
                        i7++;
                    } else if (xSParticleDecl4 != null && !xSParticleDecl4.emptiable()) {
                        reportSchemaError(xMLErrorReporter, uncheckedCTLocators[i8], "derivation-ok-restriction.5.3.2", new Object[]{uncheckedComplexTypeDecls[i8].fName, uncheckedComplexTypeDecls[i8].fBaseType.getName()});
                    }
                }
                z = false;
                contentModel = uncheckedComplexTypeDecls[i8].getContentModel(cMBuilder);
                if (contentModel != null) {
                }
                z2 = z;
                uncheckedComplexTypeDecls[i7] = uncheckedComplexTypeDecls[i8];
                i7++;
            }
            if (!z3) {
                grammars[length3].setUncheckedTypeNum(i7);
                grammars[length3].fFullChecked = true;
            }
        }
    }

    public static void checkElementDeclsConsistent(XSComplexTypeDecl xSComplexTypeDecl, XSParticleDecl xSParticleDecl, SymbolHash symbolHash, SubstitutionGroupHandler substitutionGroupHandler) throws XMLSchemaException {
        short s = xSParticleDecl.fType;
        if (s != 2) {
            int i = 0;
            if (s == 1) {
                XSElementDecl xSElementDecl = (XSElementDecl) xSParticleDecl.fValue;
                findElemInTable(xSComplexTypeDecl, xSElementDecl, symbolHash);
                if (xSElementDecl.fScope == 1) {
                    XSElementDecl[] substitutionGroup = substitutionGroupHandler.getSubstitutionGroup(xSElementDecl);
                    while (i < substitutionGroup.length) {
                        findElemInTable(xSComplexTypeDecl, substitutionGroup[i], symbolHash);
                        i++;
                    }
                    return;
                }
                return;
            }
            XSModelGroupImpl xSModelGroupImpl = (XSModelGroupImpl) xSParticleDecl.fValue;
            while (i < xSModelGroupImpl.fParticleCount) {
                checkElementDeclsConsistent(xSComplexTypeDecl, xSModelGroupImpl.fParticles[i], symbolHash, substitutionGroupHandler);
                i++;
            }
        }
    }

    public static void findElemInTable(XSComplexTypeDecl xSComplexTypeDecl, XSElementDecl xSElementDecl, SymbolHash symbolHash) throws XMLSchemaException {
        String str = xSElementDecl.fName + "," + xSElementDecl.fTargetNamespace;
        XSElementDecl xSElementDecl2 = (XSElementDecl) symbolHash.get(str);
        if (xSElementDecl2 == null) {
            symbolHash.put(str, xSElementDecl);
        } else if (xSElementDecl != xSElementDecl2 && xSElementDecl.fType != xSElementDecl2.fType) {
            throw new XMLSchemaException("cos-element-consistent", new Object[]{xSComplexTypeDecl.fName, xSElementDecl.fName});
        }
    }

    private static boolean particleValidRestriction(XSParticleDecl xSParticleDecl, SubstitutionGroupHandler substitutionGroupHandler, XSParticleDecl xSParticleDecl2, SubstitutionGroupHandler substitutionGroupHandler2) throws XMLSchemaException {
        return particleValidRestriction(xSParticleDecl, substitutionGroupHandler, xSParticleDecl2, substitutionGroupHandler2, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:138:0x024e  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00c0  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0108  */
    private static boolean particleValidRestriction(XSParticleDecl xSParticleDecl, SubstitutionGroupHandler substitutionGroupHandler, XSParticleDecl xSParticleDecl2, SubstitutionGroupHandler substitutionGroupHandler2, boolean z) throws XMLSchemaException {
        Vector vector;
        SubstitutionGroupHandler substitutionGroupHandler3;
        Vector vector2;
        short s;
        int i;
        int i2;
        short s2;
        XSParticleDecl xSParticleDecl3;
        Vector vector3;
        SubstitutionGroupHandler substitutionGroupHandler4;
        Vector vector4;
        boolean z2;
        XSElementDecl xSElementDecl;
        XSParticleDecl xSParticleDecl4 = xSParticleDecl;
        if (xSParticleDecl.isEmpty() && !xSParticleDecl2.emptiable()) {
            throw new XMLSchemaException("cos-particle-restrict.a", null);
        } else if (xSParticleDecl.isEmpty() || !xSParticleDecl2.isEmpty()) {
            short s3 = xSParticleDecl4.fType;
            if (s3 == 3) {
                s3 = ((XSModelGroupImpl) xSParticleDecl4.fValue).fCompositor;
                XSParticleDecl nonUnaryGroup = getNonUnaryGroup(xSParticleDecl);
                if (nonUnaryGroup != xSParticleDecl4) {
                    short s4 = nonUnaryGroup.fType;
                    if (s4 == 3) {
                        s4 = ((XSModelGroupImpl) nonUnaryGroup.fValue).fCompositor;
                    }
                    s3 = s4;
                } else {
                    nonUnaryGroup = xSParticleDecl4;
                }
                vector = removePointlessChildren(nonUnaryGroup);
                xSParticleDecl4 = nonUnaryGroup;
            } else {
                vector = null;
            }
            int i3 = xSParticleDecl4.fMinOccurs;
            int i4 = xSParticleDecl4.fMaxOccurs;
            if (substitutionGroupHandler != null && s3 == 1) {
                XSElementDecl xSElementDecl2 = (XSElementDecl) xSParticleDecl4.fValue;
                if (xSElementDecl2.fScope == 1) {
                    XSElementDecl[] substitutionGroup = substitutionGroupHandler.getSubstitutionGroup(xSElementDecl2);
                    if (substitutionGroup.length > 0) {
                        Vector vector5 = new Vector(substitutionGroup.length + 1);
                        for (XSElementDecl xSElementDecl3 : substitutionGroup) {
                            addElementToParticleVector(vector5, xSElementDecl3);
                        }
                        addElementToParticleVector(vector5, xSElementDecl2);
                        Collections.sort(vector5, ELEMENT_PARTICLE_COMPARATOR);
                        vector2 = vector5;
                        i2 = i3;
                        i = i4;
                        s = 101;
                        substitutionGroupHandler3 = null;
                        s2 = xSParticleDecl2.fType;
                        if (s2 != 3) {
                            s2 = ((XSModelGroupImpl) xSParticleDecl2.fValue).fCompositor;
                            xSParticleDecl3 = getNonUnaryGroup(xSParticleDecl2);
                            if (xSParticleDecl3 != xSParticleDecl2) {
                                s2 = xSParticleDecl3.fType;
                                if (s2 == 3) {
                                    s2 = ((XSModelGroupImpl) xSParticleDecl3.fValue).fCompositor;
                                }
                            } else {
                                xSParticleDecl3 = xSParticleDecl2;
                            }
                            vector3 = removePointlessChildren(xSParticleDecl3);
                        } else {
                            xSParticleDecl3 = xSParticleDecl2;
                            vector3 = null;
                        }
                        int i5 = xSParticleDecl3.fMinOccurs;
                        int i6 = xSParticleDecl3.fMaxOccurs;
                        if (substitutionGroupHandler2 != null && s2 == 1) {
                            xSElementDecl = (XSElementDecl) xSParticleDecl3.fValue;
                            if (xSElementDecl.fScope == 1) {
                                XSElementDecl[] substitutionGroup2 = substitutionGroupHandler2.getSubstitutionGroup(xSElementDecl);
                                if (substitutionGroup2.length > 0) {
                                    Vector vector6 = new Vector(substitutionGroup2.length + 1);
                                    for (XSElementDecl xSElementDecl4 : substitutionGroup2) {
                                        addElementToParticleVector(vector6, xSElementDecl4);
                                    }
                                    addElementToParticleVector(vector6, xSElementDecl);
                                    Collections.sort(vector6, ELEMENT_PARTICLE_COMPARATOR);
                                    vector4 = vector6;
                                    z2 = true;
                                    s2 = 101;
                                    substitutionGroupHandler4 = null;
                                    if (s != 1) {
                                        if (s != 2) {
                                            switch (s) {
                                                case 101:
                                                    if (s2 != 1) {
                                                        if (s2 != 2) {
                                                            switch (s2) {
                                                                case 101:
                                                                    checkRecurseLax(vector2, i3, i4, substitutionGroupHandler3, vector4, i5, i6, substitutionGroupHandler4);
                                                                    return z2;
                                                                case 102:
                                                                case 103:
                                                                    break;
                                                                default:
                                                                    throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
                                                            }
                                                        } else {
                                                            if (i2 == -2) {
                                                                i2 = xSParticleDecl4.minEffectiveTotalRange();
                                                            }
                                                            checkNSRecurseCheckCardinality(vector2, i2, i == -2 ? xSParticleDecl4.maxEffectiveTotalRange() : i, substitutionGroupHandler3, xSParticleDecl3, i5, i6, z);
                                                            return z2;
                                                        }
                                                    }
                                                    throw new XMLSchemaException("cos-particle-restrict.2", new Object[]{"choice:all,sequence,elt"});
                                                case 102:
                                                    if (s2 == 1) {
                                                        throw new XMLSchemaException("cos-particle-restrict.2", new Object[]{"seq:elt"});
                                                    } else if (s2 != 2) {
                                                        switch (s2) {
                                                            case 101:
                                                                int size = i3 * vector2.size();
                                                                if (i4 != -1) {
                                                                    i4 *= vector2.size();
                                                                }
                                                                checkMapAndSum(vector2, size, i4, substitutionGroupHandler3, vector4, i5, i6, substitutionGroupHandler4);
                                                                return z2;
                                                            case 102:
                                                                checkRecurse(vector2, i3, i4, substitutionGroupHandler3, vector4, i5, i6, substitutionGroupHandler4);
                                                                return z2;
                                                            case 103:
                                                                checkRecurseUnordered(vector2, i3, i4, substitutionGroupHandler3, vector4, i5, i6, substitutionGroupHandler4);
                                                                return z2;
                                                            default:
                                                                throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
                                                        }
                                                    } else {
                                                        if (i2 == -2) {
                                                            i2 = xSParticleDecl4.minEffectiveTotalRange();
                                                        }
                                                        checkNSRecurseCheckCardinality(vector2, i2, i == -2 ? xSParticleDecl4.maxEffectiveTotalRange() : i, substitutionGroupHandler3, xSParticleDecl3, i5, i6, z);
                                                        return z2;
                                                    }
                                                case 103:
                                                    if (s2 != 1) {
                                                        if (s2 != 2) {
                                                            switch (s2) {
                                                                case 101:
                                                                case 102:
                                                                    break;
                                                                case 103:
                                                                    checkRecurse(vector2, i3, i4, substitutionGroupHandler3, vector4, i5, i6, substitutionGroupHandler4);
                                                                    return z2;
                                                                default:
                                                                    throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
                                                            }
                                                        } else {
                                                            if (i2 == -2) {
                                                                i2 = xSParticleDecl4.minEffectiveTotalRange();
                                                            }
                                                            checkNSRecurseCheckCardinality(vector2, i2, i == -2 ? xSParticleDecl4.maxEffectiveTotalRange() : i, substitutionGroupHandler3, xSParticleDecl3, i5, i6, z);
                                                            return z2;
                                                        }
                                                    }
                                                    throw new XMLSchemaException("cos-particle-restrict.2", new Object[]{"all:choice,sequence,elt"});
                                                default:
                                                    return z2;
                                            }
                                        } else {
                                            if (s2 != 1) {
                                                if (s2 != 2) {
                                                    switch (s2) {
                                                        case 101:
                                                        case 102:
                                                        case 103:
                                                            break;
                                                        default:
                                                            throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
                                                    }
                                                } else {
                                                    checkNSSubset((XSWildcardDecl) xSParticleDecl4.fValue, i3, i4, (XSWildcardDecl) xSParticleDecl3.fValue, i5, i6);
                                                    return z2;
                                                }
                                            }
                                            throw new XMLSchemaException("cos-particle-restrict.2", new Object[]{"any:choice,sequence,all,elt"});
                                        }
                                    } else if (s2 == 1) {
                                        checkNameAndTypeOK((XSElementDecl) xSParticleDecl4.fValue, i3, i4, (XSElementDecl) xSParticleDecl3.fValue, i5, i6);
                                        return z2;
                                    } else if (s2 != 2) {
                                        switch (s2) {
                                            case 101:
                                                Vector vector7 = new Vector();
                                                vector7.addElement(xSParticleDecl4);
                                                checkRecurseLax(vector7, 1, 1, substitutionGroupHandler3, vector4, i5, i6, substitutionGroupHandler4);
                                                return z2;
                                            case 102:
                                            case 103:
                                                Vector vector8 = new Vector();
                                                vector8.addElement(xSParticleDecl4);
                                                checkRecurse(vector8, 1, 1, substitutionGroupHandler3, vector4, i5, i6, substitutionGroupHandler4);
                                                return z2;
                                            default:
                                                throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
                                        }
                                    } else {
                                        checkNSCompat((XSElementDecl) xSParticleDecl4.fValue, i3, i4, (XSWildcardDecl) xSParticleDecl3.fValue, i5, i6, z);
                                        return z2;
                                    }
                                }
                            }
                        }
                        vector4 = vector3;
                        substitutionGroupHandler4 = substitutionGroupHandler2;
                        z2 = false;
                        if (s != 1) {
                        }
                    }
                }
            }
            substitutionGroupHandler3 = substitutionGroupHandler;
            s = s3;
            vector2 = vector;
            i2 = -2;
            i = -2;
            s2 = xSParticleDecl2.fType;
            if (s2 != 3) {
            }
            int i52 = xSParticleDecl3.fMinOccurs;
            int i62 = xSParticleDecl3.fMaxOccurs;
            xSElementDecl = (XSElementDecl) xSParticleDecl3.fValue;
            if (xSElementDecl.fScope == 1) {
            }
            vector4 = vector3;
            substitutionGroupHandler4 = substitutionGroupHandler2;
            z2 = false;
            if (s != 1) {
            }
        } else {
            throw new XMLSchemaException("cos-particle-restrict.b", null);
        }
    }

    private static void addElementToParticleVector(Vector vector, XSElementDecl xSElementDecl) {
        XSParticleDecl xSParticleDecl = new XSParticleDecl();
        xSParticleDecl.fValue = xSElementDecl;
        xSParticleDecl.fType = 1;
        vector.addElement(xSParticleDecl);
    }

    private static XSParticleDecl getNonUnaryGroup(XSParticleDecl xSParticleDecl) {
        return (xSParticleDecl.fType == 1 || xSParticleDecl.fType == 2 || xSParticleDecl.fMinOccurs != 1 || xSParticleDecl.fMaxOccurs != 1 || xSParticleDecl.fValue == null || ((XSModelGroupImpl) xSParticleDecl.fValue).fParticleCount != 1) ? xSParticleDecl : getNonUnaryGroup(((XSModelGroupImpl) xSParticleDecl.fValue).fParticles[0]);
    }

    private static Vector removePointlessChildren(XSParticleDecl xSParticleDecl) {
        if (xSParticleDecl.fType == 1 || xSParticleDecl.fType == 2) {
            return null;
        }
        Vector vector = new Vector();
        XSModelGroupImpl xSModelGroupImpl = (XSModelGroupImpl) xSParticleDecl.fValue;
        for (int i = 0; i < xSModelGroupImpl.fParticleCount; i++) {
            gatherChildren(xSModelGroupImpl.fCompositor, xSModelGroupImpl.fParticles[i], vector);
        }
        return vector;
    }

    private static void gatherChildren(int i, XSParticleDecl xSParticleDecl, Vector vector) {
        int i2 = xSParticleDecl.fMinOccurs;
        int i3 = xSParticleDecl.fMaxOccurs;
        short s = xSParticleDecl.fType;
        if (s == 3) {
            s = ((XSModelGroupImpl) xSParticleDecl.fValue).fCompositor;
        }
        if (s == 1 || s == 2) {
            vector.addElement(xSParticleDecl);
        } else if (i2 != 1 || i3 != 1) {
            vector.addElement(xSParticleDecl);
        } else if (i == s) {
            XSModelGroupImpl xSModelGroupImpl = (XSModelGroupImpl) xSParticleDecl.fValue;
            for (int i4 = 0; i4 < xSModelGroupImpl.fParticleCount; i4++) {
                gatherChildren(s, xSModelGroupImpl.fParticles[i4], vector);
            }
        } else if (!xSParticleDecl.isEmpty()) {
            vector.addElement(xSParticleDecl);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x004e: APUT  (r0v5 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r9v14 java.lang.String) */
    private static void checkNameAndTypeOK(XSElementDecl xSElementDecl, int i, int i2, XSElementDecl xSElementDecl2, int i3, int i4) throws XMLSchemaException {
        String str;
        if (xSElementDecl.fName != xSElementDecl2.fName || xSElementDecl.fTargetNamespace != xSElementDecl2.fTargetNamespace) {
            throw new XMLSchemaException("rcase-NameAndTypeOK.1", new Object[]{xSElementDecl.fName, xSElementDecl.fTargetNamespace, xSElementDecl2.fName, xSElementDecl2.fTargetNamespace});
        } else if (!xSElementDecl2.getNillable() && xSElementDecl.getNillable()) {
            throw new XMLSchemaException("rcase-NameAndTypeOK.2", new Object[]{xSElementDecl.fName});
        } else if (!checkOccurrenceRange(i, i2, i3, i4)) {
            Object[] objArr = new Object[5];
            objArr[0] = xSElementDecl.fName;
            objArr[1] = Integer.toString(i);
            String str2 = SchemaSymbols.ATTVAL_UNBOUNDED;
            if (i2 == -1) {
                str = str2;
            } else {
                str = Integer.toString(i2);
            }
            objArr[2] = str;
            objArr[3] = Integer.toString(i3);
            if (i4 != -1) {
                str2 = Integer.toString(i4);
            }
            objArr[4] = str2;
            throw new XMLSchemaException("rcase-NameAndTypeOK.3", objArr);
        } else {
            if (xSElementDecl2.getConstraintType() == 2) {
                if (xSElementDecl.getConstraintType() == 2) {
                    boolean z = xSElementDecl.fType.getTypeCategory() == 16 || ((XSComplexTypeDecl) xSElementDecl.fType).fContentType == 1;
                    if ((!z && !xSElementDecl2.fDefault.normalizedValue.equals(xSElementDecl.fDefault.normalizedValue)) || (z && !xSElementDecl2.fDefault.actualValue.equals(xSElementDecl.fDefault.actualValue))) {
                        throw new XMLSchemaException("rcase-NameAndTypeOK.4.b", new Object[]{xSElementDecl.fName, xSElementDecl.fDefault.stringValue(), xSElementDecl2.fDefault.stringValue()});
                    }
                } else {
                    throw new XMLSchemaException("rcase-NameAndTypeOK.4.a", new Object[]{xSElementDecl.fName, xSElementDecl2.fDefault.stringValue()});
                }
            }
            checkIDConstraintRestriction(xSElementDecl, xSElementDecl2);
            short s = xSElementDecl.fBlock;
            short s2 = xSElementDecl2.fBlock;
            if ((s & s2) != s2 || (s == 0 && s2 != 0)) {
                throw new XMLSchemaException("rcase-NameAndTypeOK.6", new Object[]{xSElementDecl.fName});
            } else if (!checkTypeDerivationOk(xSElementDecl.fType, xSElementDecl2.fType, 25)) {
                throw new XMLSchemaException("rcase-NameAndTypeOK.7", new Object[]{xSElementDecl.fName, xSElementDecl.fType.getName(), xSElementDecl2.fType.getName()});
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0026: APUT  (r9v3 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r5v2 java.lang.String) */
    private static void checkNSCompat(XSElementDecl xSElementDecl, int i, int i2, XSWildcardDecl xSWildcardDecl, int i3, int i4, boolean z) throws XMLSchemaException {
        String str;
        if (z && !checkOccurrenceRange(i, i2, i3, i4)) {
            Object[] objArr = new Object[5];
            objArr[0] = xSElementDecl.fName;
            objArr[1] = Integer.toString(i);
            String str2 = SchemaSymbols.ATTVAL_UNBOUNDED;
            if (i2 == -1) {
                str = str2;
            } else {
                str = Integer.toString(i2);
            }
            objArr[2] = str;
            objArr[3] = Integer.toString(i3);
            if (i4 != -1) {
                str2 = Integer.toString(i4);
            }
            objArr[4] = str2;
            throw new XMLSchemaException("rcase-NSCompat.2", objArr);
        } else if (!xSWildcardDecl.allowNamespace(xSElementDecl.fTargetNamespace)) {
            throw new XMLSchemaException("rcase-NSCompat.1", new Object[]{xSElementDecl.fName, xSElementDecl.fTargetNamespace});
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0020: APUT  (r7v2 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r6v3 java.lang.String) */
    private static void checkNSSubset(XSWildcardDecl xSWildcardDecl, int i, int i2, XSWildcardDecl xSWildcardDecl2, int i3, int i4) throws XMLSchemaException {
        String str;
        if (!checkOccurrenceRange(i, i2, i3, i4)) {
            Object[] objArr = new Object[4];
            objArr[0] = Integer.toString(i);
            String str2 = SchemaSymbols.ATTVAL_UNBOUNDED;
            if (i2 == -1) {
                str = str2;
            } else {
                str = Integer.toString(i2);
            }
            objArr[1] = str;
            objArr[2] = Integer.toString(i3);
            if (i4 != -1) {
                str2 = Integer.toString(i4);
            }
            objArr[3] = str2;
            throw new XMLSchemaException("rcase-NSSubset.2", objArr);
        } else if (!xSWildcardDecl.isSubsetOf(xSWildcardDecl2)) {
            throw new XMLSchemaException("rcase-NSSubset.1", null);
        } else if (xSWildcardDecl.weakerProcessContents(xSWildcardDecl2)) {
            throw new XMLSchemaException("rcase-NSSubset.3", new Object[]{xSWildcardDecl.getProcessContentsAsString(), xSWildcardDecl2.getProcessContentsAsString()});
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0021: APUT  (r4v2 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r3v4 java.lang.String) */
    private static void checkNSRecurseCheckCardinality(Vector vector, int i, int i2, SubstitutionGroupHandler substitutionGroupHandler, XSParticleDecl xSParticleDecl, int i3, int i4, boolean z) throws XMLSchemaException {
        String str;
        if (!z || checkOccurrenceRange(i, i2, i3, i4)) {
            int size = vector.size();
            for (int i5 = 0; i5 < size; i5++) {
                try {
                    particleValidRestriction((XSParticleDecl) vector.elementAt(i5), substitutionGroupHandler, xSParticleDecl, null, false);
                } catch (XMLSchemaException unused) {
                    throw new XMLSchemaException("rcase-NSRecurseCheckCardinality.1", null);
                }
            }
            return;
        }
        Object[] objArr = new Object[4];
        objArr[0] = Integer.toString(i);
        String str2 = SchemaSymbols.ATTVAL_UNBOUNDED;
        if (i2 == -1) {
            str = str2;
        } else {
            str = Integer.toString(i2);
        }
        objArr[1] = str;
        objArr[2] = Integer.toString(i3);
        if (i4 != -1) {
            str2 = Integer.toString(i4);
        }
        objArr[3] = str2;
        throw new XMLSchemaException("rcase-NSRecurseCheckCardinality.2", objArr);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001f: APUT  (r9v2 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r8v2 java.lang.String) */
    private static void checkRecurse(Vector vector, int i, int i2, SubstitutionGroupHandler substitutionGroupHandler, Vector vector2, int i3, int i4, SubstitutionGroupHandler substitutionGroupHandler2) throws XMLSchemaException {
        String str;
        int i5 = 0;
        if (!checkOccurrenceRange(i, i2, i3, i4)) {
            Object[] objArr = new Object[4];
            objArr[0] = Integer.toString(i);
            String str2 = SchemaSymbols.ATTVAL_UNBOUNDED;
            if (i2 == -1) {
                str = str2;
            } else {
                str = Integer.toString(i2);
            }
            objArr[1] = str;
            objArr[2] = Integer.toString(i3);
            if (i4 != -1) {
                str2 = Integer.toString(i4);
            }
            objArr[3] = str2;
            throw new XMLSchemaException("rcase-Recurse.1", objArr);
        }
        int size = vector.size();
        int size2 = vector2.size();
        int i6 = 0;
        while (i5 < size) {
            XSParticleDecl xSParticleDecl = (XSParticleDecl) vector.elementAt(i5);
            int i7 = i6;
            while (i6 < size2) {
                XSParticleDecl xSParticleDecl2 = (XSParticleDecl) vector2.elementAt(i6);
                i7++;
                try {
                    particleValidRestriction(xSParticleDecl, substitutionGroupHandler, xSParticleDecl2, substitutionGroupHandler2);
                    i5++;
                    i6 = i7;
                } catch (XMLSchemaException unused) {
                    if (xSParticleDecl2.emptiable()) {
                        i6++;
                    } else {
                        throw new XMLSchemaException("rcase-Recurse.2", null);
                    }
                }
            }
            throw new XMLSchemaException("rcase-Recurse.2", null);
        }
        while (i6 < size2) {
            if (((XSParticleDecl) vector2.elementAt(i6)).emptiable()) {
                i6++;
            } else {
                throw new XMLSchemaException("rcase-Recurse.2", null);
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001f: APUT  (r10v2 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r9v2 java.lang.String) */
    private static void checkRecurseUnordered(Vector vector, int i, int i2, SubstitutionGroupHandler substitutionGroupHandler, Vector vector2, int i3, int i4, SubstitutionGroupHandler substitutionGroupHandler2) throws XMLSchemaException {
        String str;
        if (!checkOccurrenceRange(i, i2, i3, i4)) {
            Object[] objArr = new Object[4];
            objArr[0] = Integer.toString(i);
            String str2 = SchemaSymbols.ATTVAL_UNBOUNDED;
            if (i2 == -1) {
                str = str2;
            } else {
                str = Integer.toString(i2);
            }
            objArr[1] = str;
            objArr[2] = Integer.toString(i3);
            if (i4 != -1) {
                str2 = Integer.toString(i4);
            }
            objArr[3] = str2;
            throw new XMLSchemaException("rcase-RecurseUnordered.1", objArr);
        }
        int size = vector.size();
        int size2 = vector2.size();
        boolean[] zArr = new boolean[size2];
        for (int i5 = 0; i5 < size; i5++) {
            XSParticleDecl xSParticleDecl = (XSParticleDecl) vector.elementAt(i5);
            for (int i6 = 0; i6 < size2; i6++) {
                try {
                    particleValidRestriction(xSParticleDecl, substitutionGroupHandler, (XSParticleDecl) vector2.elementAt(i6), substitutionGroupHandler2);
                    if (!zArr[i6]) {
                        zArr[i6] = true;
                    } else {
                        throw new XMLSchemaException("rcase-RecurseUnordered.2", null);
                    }
                } catch (XMLSchemaException unused) {
                }
            }
            throw new XMLSchemaException("rcase-RecurseUnordered.2", null);
        }
        for (int i7 = 0; i7 < size2; i7++) {
            XSParticleDecl xSParticleDecl2 = (XSParticleDecl) vector2.elementAt(i7);
            if (!zArr[i7] && !xSParticleDecl2.emptiable()) {
                throw new XMLSchemaException("rcase-RecurseUnordered.2", null);
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001f: APUT  (r7v2 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r6v3 java.lang.String) */
    private static void checkRecurseLax(Vector vector, int i, int i2, SubstitutionGroupHandler substitutionGroupHandler, Vector vector2, int i3, int i4, SubstitutionGroupHandler substitutionGroupHandler2) throws XMLSchemaException {
        String str;
        if (!checkOccurrenceRange(i, i2, i3, i4)) {
            Object[] objArr = new Object[4];
            objArr[0] = Integer.toString(i);
            String str2 = SchemaSymbols.ATTVAL_UNBOUNDED;
            if (i2 == -1) {
                str = str2;
            } else {
                str = Integer.toString(i2);
            }
            objArr[1] = str;
            objArr[2] = Integer.toString(i3);
            if (i4 != -1) {
                str2 = Integer.toString(i4);
            }
            objArr[3] = str2;
            throw new XMLSchemaException("rcase-RecurseLax.1", objArr);
        }
        int size = vector.size();
        int size2 = vector2.size();
        int i5 = 0;
        for (int i6 = 0; i6 < size; i6++) {
            XSParticleDecl xSParticleDecl = (XSParticleDecl) vector.elementAt(i6);
            int i7 = i5;
            while (i5 < size2) {
                i7++;
                try {
                    if (particleValidRestriction(xSParticleDecl, substitutionGroupHandler, (XSParticleDecl) vector2.elementAt(i5), substitutionGroupHandler2)) {
                        i7--;
                    }
                    i5 = i7;
                } catch (XMLSchemaException unused) {
                    i5++;
                }
            }
            throw new XMLSchemaException("rcase-RecurseLax.2", null);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001f: APUT  (r6v2 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r5v3 java.lang.String) */
    private static void checkMapAndSum(Vector vector, int i, int i2, SubstitutionGroupHandler substitutionGroupHandler, Vector vector2, int i3, int i4, SubstitutionGroupHandler substitutionGroupHandler2) throws XMLSchemaException {
        String str;
        if (!checkOccurrenceRange(i, i2, i3, i4)) {
            Object[] objArr = new Object[4];
            objArr[0] = Integer.toString(i);
            String str2 = SchemaSymbols.ATTVAL_UNBOUNDED;
            if (i2 == -1) {
                str = str2;
            } else {
                str = Integer.toString(i2);
            }
            objArr[1] = str;
            objArr[2] = Integer.toString(i3);
            if (i4 != -1) {
                str2 = Integer.toString(i4);
            }
            objArr[3] = str2;
            throw new XMLSchemaException("rcase-MapAndSum.2", objArr);
        }
        int size = vector.size();
        int size2 = vector2.size();
        for (int i5 = 0; i5 < size; i5++) {
            XSParticleDecl xSParticleDecl = (XSParticleDecl) vector.elementAt(i5);
            for (int i6 = 0; i6 < size2; i6++) {
                try {
                    particleValidRestriction(xSParticleDecl, substitutionGroupHandler, (XSParticleDecl) vector2.elementAt(i6), substitutionGroupHandler2);
                } catch (XMLSchemaException unused) {
                }
            }
            throw new XMLSchemaException("rcase-MapAndSum.1", null);
        }
    }

    public static boolean overlapUPA(XSElementDecl xSElementDecl, XSElementDecl xSElementDecl2, SubstitutionGroupHandler substitutionGroupHandler) {
        if (xSElementDecl.fName == xSElementDecl2.fName && xSElementDecl.fTargetNamespace == xSElementDecl2.fTargetNamespace) {
            return true;
        }
        XSElementDecl[] substitutionGroup = substitutionGroupHandler.getSubstitutionGroup(xSElementDecl);
        for (int length = substitutionGroup.length - 1; length >= 0; length--) {
            if (substitutionGroup[length].fName == xSElementDecl2.fName && substitutionGroup[length].fTargetNamespace == xSElementDecl2.fTargetNamespace) {
                return true;
            }
        }
        XSElementDecl[] substitutionGroup2 = substitutionGroupHandler.getSubstitutionGroup(xSElementDecl2);
        for (int length2 = substitutionGroup2.length - 1; length2 >= 0; length2--) {
            if (substitutionGroup2[length2].fName == xSElementDecl.fName && substitutionGroup2[length2].fTargetNamespace == xSElementDecl.fTargetNamespace) {
                return true;
            }
        }
        return false;
    }

    public static boolean overlapUPA(XSElementDecl xSElementDecl, XSWildcardDecl xSWildcardDecl, SubstitutionGroupHandler substitutionGroupHandler) {
        if (xSWildcardDecl.allowNamespace(xSElementDecl.fTargetNamespace)) {
            return true;
        }
        XSElementDecl[] substitutionGroup = substitutionGroupHandler.getSubstitutionGroup(xSElementDecl);
        for (int length = substitutionGroup.length - 1; length >= 0; length--) {
            if (xSWildcardDecl.allowNamespace(substitutionGroup[length].fTargetNamespace)) {
                return true;
            }
        }
        return false;
    }

    public static boolean overlapUPA(XSWildcardDecl xSWildcardDecl, XSWildcardDecl xSWildcardDecl2) {
        XSWildcardDecl performIntersectionWith = xSWildcardDecl.performIntersectionWith(xSWildcardDecl2, xSWildcardDecl.fProcessContents);
        return (performIntersectionWith != null && performIntersectionWith.fType == 3 && performIntersectionWith.fNamespaceList.length == 0) ? false : true;
    }

    public static boolean overlapUPA(Object obj, Object obj2, SubstitutionGroupHandler substitutionGroupHandler) {
        if (obj instanceof XSElementDecl) {
            if (obj2 instanceof XSElementDecl) {
                return overlapUPA((XSElementDecl) obj, (XSElementDecl) obj2, substitutionGroupHandler);
            }
            return overlapUPA((XSElementDecl) obj, (XSWildcardDecl) obj2, substitutionGroupHandler);
        } else if (obj2 instanceof XSElementDecl) {
            return overlapUPA((XSElementDecl) obj2, (XSWildcardDecl) obj, substitutionGroupHandler);
        } else {
            return overlapUPA((XSWildcardDecl) obj, (XSWildcardDecl) obj2);
        }
    }
}
