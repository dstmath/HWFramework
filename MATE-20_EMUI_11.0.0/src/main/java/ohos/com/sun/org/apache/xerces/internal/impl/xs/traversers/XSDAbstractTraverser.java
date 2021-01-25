package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.util.Locale;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSFacets;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationState;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeGroupDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeUseImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSWildcardDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public abstract class XSDAbstractTraverser {
    protected static final int CHILD_OF_GROUP = 4;
    protected static final int GROUP_REF_WITH_ALL = 2;
    protected static final int NOT_ALL_CONTEXT = 0;
    protected static final String NO_NAME = "(no name)";
    protected static final int PROCESSING_ALL_EL = 1;
    protected static final int PROCESSING_ALL_GP = 8;
    private static final XSSimpleType fQNameDV = ((XSSimpleType) SchemaGrammar.SG_SchemaNS.getGlobalTypeDecl(SchemaSymbols.ATTVAL_QNAME));
    protected XSAttributeChecker fAttrChecker = null;
    private StringBuffer fPattern = new StringBuffer();
    protected XSDHandler fSchemaHandler = null;
    protected SymbolTable fSymbolTable = null;
    protected boolean fValidateAnnotations = false;
    ValidationState fValidationState = new ValidationState();
    private final XSFacets xsFacets = new XSFacets();

    XSDAbstractTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        this.fSchemaHandler = xSDHandler;
        this.fAttrChecker = xSAttributeChecker;
    }

    /* access modifiers changed from: package-private */
    public void reset(SymbolTable symbolTable, boolean z, Locale locale) {
        this.fSymbolTable = symbolTable;
        this.fValidateAnnotations = z;
        this.fValidationState.setExtraChecking(false);
        this.fValidationState.setSymbolTable(symbolTable);
        this.fValidationState.setLocale(locale);
    }

    /* access modifiers changed from: package-private */
    public XSAnnotationImpl traverseAnnotationDecl(Element element, Object[] objArr, boolean z, XSDocumentInfo xSDocumentInfo) {
        String str;
        String str2;
        this.fAttrChecker.returnAttrArray(this.fAttrChecker.checkAttributes(element, z, xSDocumentInfo), xSDocumentInfo);
        String annotation = DOMUtil.getAnnotation(element);
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement != null) {
            do {
                String localName = DOMUtil.getLocalName(firstChildElement);
                if (localName.equals(SchemaSymbols.ELT_APPINFO) || localName.equals(SchemaSymbols.ELT_DOCUMENTATION)) {
                    this.fAttrChecker.returnAttrArray(this.fAttrChecker.checkAttributes(firstChildElement, true, xSDocumentInfo), xSDocumentInfo);
                } else {
                    reportSchemaError("src-annotation", new Object[]{localName}, firstChildElement);
                }
                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
            } while (firstChildElement != null);
        }
        if (annotation == null) {
            return null;
        }
        SchemaGrammar grammar = this.fSchemaHandler.getGrammar(xSDocumentInfo.fTargetNamespace);
        Vector vector = (Vector) objArr[XSAttributeChecker.ATTIDX_NONSCHEMA];
        if (vector == null || vector.isEmpty()) {
            if (this.fValidateAnnotations) {
                xSDocumentInfo.addAnnotation(new XSAnnotationInfo(annotation, element));
            }
            return new XSAnnotationImpl(annotation, grammar);
        }
        StringBuffer stringBuffer = new StringBuffer(64);
        stringBuffer.append(" ");
        int i = 0;
        while (i < vector.size()) {
            int i2 = i + 1;
            String str3 = (String) vector.elementAt(i);
            int indexOf = str3.indexOf(58);
            if (indexOf == -1) {
                str2 = "";
                str = str3;
            } else {
                str2 = str3.substring(0, indexOf);
                str = str3.substring(indexOf + 1);
            }
            if (element.getAttributeNS(xSDocumentInfo.fNamespaceSupport.getURI(this.fSymbolTable.addSymbol(str2)), str).length() != 0) {
                i = i2 + 1;
            } else {
                stringBuffer.append(str3);
                stringBuffer.append("=\"");
                i = i2 + 1;
                stringBuffer.append(processAttValue((String) vector.elementAt(i2)));
                stringBuffer.append("\" ");
            }
        }
        StringBuffer stringBuffer2 = new StringBuffer(annotation.length() + stringBuffer.length());
        int indexOf2 = annotation.indexOf(SchemaSymbols.ELT_ANNOTATION);
        if (indexOf2 == -1) {
            return null;
        }
        int length = indexOf2 + SchemaSymbols.ELT_ANNOTATION.length();
        stringBuffer2.append(annotation.substring(0, length));
        stringBuffer2.append(stringBuffer.toString());
        stringBuffer2.append(annotation.substring(length, annotation.length()));
        String stringBuffer3 = stringBuffer2.toString();
        if (this.fValidateAnnotations) {
            xSDocumentInfo.addAnnotation(new XSAnnotationInfo(stringBuffer3, element));
        }
        return new XSAnnotationImpl(stringBuffer3, grammar);
    }

    /* access modifiers changed from: package-private */
    public XSAnnotationImpl traverseSyntheticAnnotation(Element element, String str, Object[] objArr, boolean z, XSDocumentInfo xSDocumentInfo) {
        String str2;
        SchemaGrammar grammar = this.fSchemaHandler.getGrammar(xSDocumentInfo.fTargetNamespace);
        Vector vector = (Vector) objArr[XSAttributeChecker.ATTIDX_NONSCHEMA];
        if (vector == null || vector.isEmpty()) {
            if (this.fValidateAnnotations) {
                xSDocumentInfo.addAnnotation(new XSAnnotationInfo(str, element));
            }
            return new XSAnnotationImpl(str, grammar);
        }
        StringBuffer stringBuffer = new StringBuffer(64);
        stringBuffer.append(" ");
        int i = 0;
        while (i < vector.size()) {
            int i2 = i + 1;
            String str3 = (String) vector.elementAt(i);
            int indexOf = str3.indexOf(58);
            if (indexOf == -1) {
                str2 = "";
            } else {
                str2 = str3.substring(0, indexOf);
                str3.substring(indexOf + 1);
            }
            xSDocumentInfo.fNamespaceSupport.getURI(this.fSymbolTable.addSymbol(str2));
            stringBuffer.append(str3);
            stringBuffer.append("=\"");
            i = i2 + 1;
            stringBuffer.append(processAttValue((String) vector.elementAt(i2)));
            stringBuffer.append("\" ");
        }
        StringBuffer stringBuffer2 = new StringBuffer(str.length() + stringBuffer.length());
        int indexOf2 = str.indexOf(SchemaSymbols.ELT_ANNOTATION);
        if (indexOf2 == -1) {
            return null;
        }
        int length = indexOf2 + SchemaSymbols.ELT_ANNOTATION.length();
        stringBuffer2.append(str.substring(0, length));
        stringBuffer2.append(stringBuffer.toString());
        stringBuffer2.append(str.substring(length, str.length()));
        String stringBuffer3 = stringBuffer2.toString();
        if (this.fValidateAnnotations) {
            xSDocumentInfo.addAnnotation(new XSAnnotationInfo(stringBuffer3, element));
        }
        return new XSAnnotationImpl(stringBuffer3, grammar);
    }

    static final class FacetInfo {
        final short fFixedFacets;
        final short fPresentFacets;
        final XSFacets facetdata;
        final Element nodeAfterFacets;

        FacetInfo(XSFacets xSFacets, Element element, short s, short s2) {
            this.facetdata = xSFacets;
            this.nodeAfterFacets = element;
            this.fPresentFacets = s;
            this.fFixedFacets = s2;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00a7  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00b6  */
    public FacetInfo traverseFacets(Element element, XSSimpleType xSSimpleType, XSDocumentInfo xSDocumentInfo) {
        short s;
        XSObjectListImpl xSObjectListImpl;
        Vector vector;
        XSObjectListImpl xSObjectListImpl2;
        boolean z;
        Object[] objArr;
        short s2;
        short s3;
        short s4;
        short s5;
        XSAnnotationImpl xSAnnotationImpl;
        Vector vector2;
        int i;
        Element element2;
        Element element3;
        XSObjectListImpl xSObjectListImpl3;
        XSObjectListImpl xSObjectListImpl4;
        Vector vector3;
        Vector vector4;
        short s6;
        XSObjectListImpl xSObjectListImpl5;
        Element element4;
        XSObjectListImpl xSObjectListImpl6;
        Element element5;
        Object obj;
        InvalidDatatypeValueException e;
        boolean containsQName = containsQName(xSSimpleType);
        Vector vector5 = containsQName ? new Vector() : null;
        this.xsFacets.reset();
        Element element6 = element;
        short s7 = 0;
        XSObjectListImpl xSObjectListImpl7 = null;
        Vector vector6 = null;
        XSObjectListImpl xSObjectListImpl8 = null;
        short s8 = 0;
        while (true) {
            if (element6 == null) {
                s = s7;
                xSObjectListImpl = xSObjectListImpl7;
                vector = vector6;
                xSObjectListImpl2 = xSObjectListImpl8;
                break;
            }
            String localName = DOMUtil.getLocalName(element6);
            if (localName.equals(SchemaSymbols.ELT_ENUMERATION)) {
                objArr = this.fAttrChecker.checkAttributes(element6, false, xSDocumentInfo, containsQName);
                String str = (String) objArr[XSAttributeChecker.ATTIDX_VALUE];
                if (str == null) {
                    reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_ENUMERATION, SchemaSymbols.ATT_VALUE}, element6);
                    this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                    element6 = DOMUtil.getNextSiblingElement(element6);
                    s7 = s7;
                } else {
                    NamespaceSupport namespaceSupport = (NamespaceSupport) objArr[XSAttributeChecker.ATTIDX_ENUMNSDECLS];
                    if (xSSimpleType.getVariety() == 1 && xSSimpleType.getPrimitiveKind() == 20) {
                        xSDocumentInfo.fValidationContext.setNamespaceSupport(namespaceSupport);
                        try {
                            xSObjectListImpl3 = xSObjectListImpl7;
                            try {
                                obj = this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 6, (QName) fQNameDV.validate(str, (ValidationContext) xSDocumentInfo.fValidationContext, (ValidatedInfo) null), element6);
                            } catch (InvalidDatatypeValueException e2) {
                                e = e2;
                            }
                        } catch (InvalidDatatypeValueException e3) {
                            e = e3;
                            xSObjectListImpl3 = xSObjectListImpl7;
                            reportSchemaError(e.getKey(), e.getArgs(), element6);
                            obj = null;
                            if (obj != null) {
                            }
                        }
                        if (obj != null) {
                            this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                            element6 = DOMUtil.getNextSiblingElement(element6);
                            s7 = s7;
                            xSObjectListImpl7 = xSObjectListImpl3;
                        } else {
                            xSDocumentInfo.fValidationContext.setNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                        }
                    } else {
                        xSObjectListImpl3 = xSObjectListImpl7;
                    }
                    if (vector6 == null) {
                        vector3 = new Vector();
                        xSObjectListImpl4 = new XSObjectListImpl();
                    } else {
                        vector3 = vector6;
                        xSObjectListImpl4 = xSObjectListImpl3;
                    }
                    vector3.addElement(str);
                    xSObjectListImpl4.addXSObject(null);
                    if (containsQName) {
                        vector5.addElement(namespaceSupport);
                    }
                    Element firstChildElement = DOMUtil.getFirstChildElement(element6);
                    if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element6);
                        if (syntheticAnnotation != null) {
                            s6 = s7;
                            element5 = firstChildElement;
                            vector4 = vector3;
                            xSObjectListImpl6 = xSObjectListImpl4;
                            xSObjectListImpl5 = xSObjectListImpl8;
                            xSObjectListImpl6.addXSObject(xSObjectListImpl4.getLength() - 1, traverseSyntheticAnnotation(element6, syntheticAnnotation, objArr, false, xSDocumentInfo));
                        } else {
                            vector4 = vector3;
                            xSObjectListImpl6 = xSObjectListImpl4;
                            xSObjectListImpl5 = xSObjectListImpl8;
                            s6 = s7;
                            element5 = firstChildElement;
                        }
                        element4 = element5;
                    } else {
                        xSObjectListImpl4.addXSObject(xSObjectListImpl4.getLength() - 1, traverseAnnotationDecl(firstChildElement, objArr, false, xSDocumentInfo));
                        element4 = DOMUtil.getNextSiblingElement(firstChildElement);
                        vector4 = vector3;
                        xSObjectListImpl6 = xSObjectListImpl4;
                        xSObjectListImpl5 = xSObjectListImpl8;
                        s6 = s7;
                    }
                    if (element4 != null) {
                        reportSchemaError("s4s-elt-must-match.1", new Object[]{"enumeration", "(annotation?)", DOMUtil.getLocalName(element4)}, element4);
                    }
                    vector6 = vector4;
                    xSObjectListImpl7 = xSObjectListImpl6;
                    xSObjectListImpl8 = xSObjectListImpl5;
                    z = containsQName;
                    s7 = s6;
                }
            } else {
                xSObjectListImpl = xSObjectListImpl7;
                xSObjectListImpl2 = xSObjectListImpl8;
                if (localName.equals(SchemaSymbols.ELT_PATTERN)) {
                    s8 = (short) (s8 | 8);
                    Object[] checkAttributes = this.fAttrChecker.checkAttributes(element6, false, xSDocumentInfo);
                    String str2 = (String) checkAttributes[XSAttributeChecker.ATTIDX_VALUE];
                    if (str2 == null) {
                        reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_PATTERN, SchemaSymbols.ATT_VALUE}, element6);
                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                        element6 = DOMUtil.getNextSiblingElement(element6);
                        xSObjectListImpl8 = xSObjectListImpl2;
                        xSObjectListImpl7 = xSObjectListImpl;
                    } else {
                        if (this.fPattern.length() == 0) {
                            this.fPattern.append(str2);
                        } else {
                            this.fPattern.append("|");
                            this.fPattern.append(str2);
                        }
                        Element firstChildElement2 = DOMUtil.getFirstChildElement(element6);
                        if (firstChildElement2 == null || !DOMUtil.getLocalName(firstChildElement2).equals(SchemaSymbols.ELT_ANNOTATION)) {
                            String syntheticAnnotation2 = DOMUtil.getSyntheticAnnotation(element6);
                            if (syntheticAnnotation2 != null) {
                                if (xSObjectListImpl2 == null) {
                                    xSObjectListImpl2 = new XSObjectListImpl();
                                }
                                z = containsQName;
                                element3 = firstChildElement2;
                                vector2 = vector6;
                                i = 3;
                                xSObjectListImpl2.addXSObject(traverseSyntheticAnnotation(element6, syntheticAnnotation2, checkAttributes, false, xSDocumentInfo));
                            } else {
                                z = containsQName;
                                element3 = firstChildElement2;
                                vector2 = vector6;
                                i = 3;
                            }
                            element2 = element3;
                        } else {
                            XSObjectListImpl xSObjectListImpl9 = xSObjectListImpl2 == null ? new XSObjectListImpl() : xSObjectListImpl2;
                            xSObjectListImpl9.addXSObject(traverseAnnotationDecl(firstChildElement2, checkAttributes, false, xSDocumentInfo));
                            xSObjectListImpl2 = xSObjectListImpl9;
                            z = containsQName;
                            element2 = DOMUtil.getNextSiblingElement(firstChildElement2);
                            vector2 = vector6;
                            i = 3;
                        }
                        if (element2 != null) {
                            Object[] objArr2 = new Object[i];
                            objArr2[0] = "pattern";
                            objArr2[1] = "(annotation?)";
                            objArr2[2] = DOMUtil.getLocalName(element2);
                            reportSchemaError("s4s-elt-must-match.1", objArr2, element2);
                        }
                        xSObjectListImpl8 = xSObjectListImpl2;
                        vector6 = vector2;
                        xSObjectListImpl7 = xSObjectListImpl;
                        s7 = s7;
                        objArr = checkAttributes;
                    }
                } else {
                    vector = vector6;
                    z = containsQName;
                    if (!localName.equals(SchemaSymbols.ELT_MINLENGTH)) {
                        if (!localName.equals(SchemaSymbols.ELT_MAXLENGTH)) {
                            if (!localName.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                                if (!localName.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
                                    if (!localName.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                                        if (!localName.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                                            if (!localName.equals(SchemaSymbols.ELT_TOTALDIGITS)) {
                                                if (!localName.equals(SchemaSymbols.ELT_FRACTIONDIGITS)) {
                                                    if (!localName.equals(SchemaSymbols.ELT_WHITESPACE)) {
                                                        if (!localName.equals(SchemaSymbols.ELT_LENGTH)) {
                                                            s = s7;
                                                            break;
                                                        }
                                                        s2 = 1;
                                                    } else {
                                                        s2 = 16;
                                                    }
                                                } else {
                                                    s2 = 1024;
                                                }
                                            } else {
                                                s2 = 512;
                                            }
                                        } else {
                                            s2 = 256;
                                        }
                                    } else {
                                        s2 = 128;
                                    }
                                } else {
                                    s2 = 32;
                                }
                            } else {
                                s2 = 64;
                            }
                        } else {
                            s2 = 4;
                        }
                    } else {
                        s2 = 2;
                    }
                    Object[] checkAttributes2 = this.fAttrChecker.checkAttributes(element6, false, xSDocumentInfo);
                    if ((s8 & s2) != 0) {
                        reportSchemaError("src-single-facet-value", new Object[]{localName}, element6);
                        this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                        element6 = DOMUtil.getNextSiblingElement(element6);
                    } else if (checkAttributes2[XSAttributeChecker.ATTIDX_VALUE] == null) {
                        if (element6.getAttributeNodeNS((String) null, "value") == null) {
                            reportSchemaError("s4s-att-must-appear", new Object[]{element6.getLocalName(), SchemaSymbols.ATT_VALUE}, element6);
                        }
                        this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                        element6 = DOMUtil.getNextSiblingElement(element6);
                    } else {
                        short s9 = (short) (s8 | s2);
                        short s10 = ((Boolean) checkAttributes2[XSAttributeChecker.ATTIDX_FIXED]).booleanValue() ? (short) (s7 | s2) : s7;
                        if (s2 == 1) {
                            this.xsFacets.length = ((XInt) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                        } else if (s2 == 2) {
                            this.xsFacets.minLength = ((XInt) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                        } else if (s2 == 4) {
                            this.xsFacets.maxLength = ((XInt) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                        } else if (s2 == 16) {
                            this.xsFacets.whiteSpace = ((XInt) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE]).shortValue();
                        } else if (s2 == 32) {
                            this.xsFacets.maxInclusive = (String) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE];
                        } else if (s2 == 64) {
                            this.xsFacets.maxExclusive = (String) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE];
                        } else if (s2 == 128) {
                            this.xsFacets.minExclusive = (String) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE];
                        } else if (s2 == 256) {
                            this.xsFacets.minInclusive = (String) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE];
                        } else if (s2 == 512) {
                            this.xsFacets.totalDigits = ((XInt) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                        } else if (s2 == 1024) {
                            this.xsFacets.fractionDigits = ((XInt) checkAttributes2[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                        }
                        Element firstChildElement3 = DOMUtil.getFirstChildElement(element6);
                        if (firstChildElement3 == null || !DOMUtil.getLocalName(firstChildElement3).equals(SchemaSymbols.ELT_ANNOTATION)) {
                            String syntheticAnnotation3 = DOMUtil.getSyntheticAnnotation(element6);
                            if (syntheticAnnotation3 != null) {
                                s4 = s2;
                                s3 = s9;
                                s5 = 4;
                                xSAnnotationImpl = traverseSyntheticAnnotation(element6, syntheticAnnotation3, checkAttributes2, false, xSDocumentInfo);
                                firstChildElement3 = firstChildElement3;
                            } else {
                                s4 = s2;
                                s3 = s9;
                                s5 = 4;
                                xSAnnotationImpl = null;
                            }
                        } else {
                            XSAnnotationImpl traverseAnnotationDecl = traverseAnnotationDecl(firstChildElement3, checkAttributes2, false, xSDocumentInfo);
                            firstChildElement3 = DOMUtil.getNextSiblingElement(firstChildElement3);
                            s4 = s2;
                            xSAnnotationImpl = traverseAnnotationDecl;
                            s3 = s9;
                            s5 = 4;
                        }
                        if (s4 == 1) {
                            this.xsFacets.lengthAnnotation = xSAnnotationImpl;
                        } else if (s4 == 2) {
                            this.xsFacets.minLengthAnnotation = xSAnnotationImpl;
                        } else if (s4 == s5) {
                            this.xsFacets.maxLengthAnnotation = xSAnnotationImpl;
                        } else if (s4 == 16) {
                            this.xsFacets.whiteSpaceAnnotation = xSAnnotationImpl;
                        } else if (s4 == 32) {
                            this.xsFacets.maxInclusiveAnnotation = xSAnnotationImpl;
                        } else if (s4 == 64) {
                            this.xsFacets.maxExclusiveAnnotation = xSAnnotationImpl;
                        } else if (s4 == 128) {
                            this.xsFacets.minExclusiveAnnotation = xSAnnotationImpl;
                        } else if (s4 == 256) {
                            this.xsFacets.minInclusiveAnnotation = xSAnnotationImpl;
                        } else if (s4 == 512) {
                            this.xsFacets.totalDigitsAnnotation = xSAnnotationImpl;
                        } else if (s4 == 1024) {
                            this.xsFacets.fractionDigitsAnnotation = xSAnnotationImpl;
                        }
                        if (firstChildElement3 != null) {
                            reportSchemaError("s4s-elt-must-match.1", new Object[]{localName, "(annotation?)", DOMUtil.getLocalName(firstChildElement3)}, firstChildElement3);
                        }
                        s8 = s3;
                        xSObjectListImpl8 = xSObjectListImpl2;
                        vector6 = vector;
                        s7 = s10;
                        xSObjectListImpl7 = xSObjectListImpl;
                        objArr = checkAttributes2;
                    }
                    xSObjectListImpl8 = xSObjectListImpl2;
                    vector6 = vector;
                    xSObjectListImpl7 = xSObjectListImpl;
                    containsQName = z;
                }
                s7 = s7;
            }
            this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
            element6 = DOMUtil.getNextSiblingElement(element6);
            containsQName = z;
        }
        if (vector != null) {
            s8 = (short) (s8 | 2048);
            XSFacets xSFacets = this.xsFacets;
            xSFacets.enumeration = vector;
            xSFacets.enumNSDecls = vector5;
            xSFacets.enumAnnotations = xSObjectListImpl;
        }
        if ((s8 & 8) != 0) {
            this.xsFacets.pattern = this.fPattern.toString();
            this.xsFacets.patternAnnotations = xSObjectListImpl2;
        }
        this.fPattern.setLength(0);
        return new FacetInfo(this.xsFacets, element6, s8, s);
    }

    private boolean containsQName(XSSimpleType xSSimpleType) {
        if (xSSimpleType.getVariety() == 1) {
            short primitiveKind = xSSimpleType.getPrimitiveKind();
            return primitiveKind == 18 || primitiveKind == 20;
        } else if (xSSimpleType.getVariety() == 2) {
            return containsQName((XSSimpleType) xSSimpleType.getItemType());
        } else {
            if (xSSimpleType.getVariety() == 3) {
                XSObjectList memberTypes = xSSimpleType.getMemberTypes();
                for (int i = 0; i < memberTypes.getLength(); i++) {
                    if (containsQName((XSSimpleType) memberTypes.item(i))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public Element traverseAttrsAndAttrGrps(Element element, XSAttributeGroupDecl xSAttributeGroupDecl, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, XSComplexTypeDecl xSComplexTypeDecl) {
        String str;
        String str2;
        String str3;
        int i;
        XSObjectList xSObjectList;
        short s;
        String str4;
        String str5;
        Element element2 = element;
        while (true) {
            str = "src-attribute_group.2";
            str2 = "src-ct.4";
            if (element2 == null) {
                break;
            }
            String localName = DOMUtil.getLocalName(element2);
            String str6 = "ag-props-correct.3";
            String str7 = "ag-props-correct.2";
            short s2 = 2;
            if (!localName.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                if (!localName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                    break;
                }
                XSAttributeGroupDecl traverseLocal = this.fSchemaHandler.fAttributeGroupTraverser.traverseLocal(element2, xSDocumentInfo, schemaGrammar);
                if (traverseLocal != null) {
                    XSObjectList attributeUses = traverseLocal.getAttributeUses();
                    int length = attributeUses.getLength();
                    int i2 = 0;
                    while (i2 < length) {
                        XSAttributeUseImpl xSAttributeUseImpl = (XSAttributeUseImpl) attributeUses.item(i2);
                        if (xSAttributeUseImpl.fUse == s2) {
                            xSAttributeGroupDecl.addAttributeUse(xSAttributeUseImpl);
                            i = length;
                            xSObjectList = attributeUses;
                            str3 = str6;
                            s = s2;
                        } else {
                            XSAttributeUse attributeUseNoProhibited = xSAttributeGroupDecl.getAttributeUseNoProhibited(xSAttributeUseImpl.fAttrDecl.getNamespace(), xSAttributeUseImpl.fAttrDecl.getName());
                            if (attributeUseNoProhibited == null) {
                                String addAttributeUse = xSAttributeGroupDecl.addAttributeUse(xSAttributeUseImpl);
                                if (addAttributeUse != null) {
                                    if (xSComplexTypeDecl == null) {
                                        str5 = str6;
                                    } else {
                                        str5 = "ct-props-correct.5";
                                    }
                                    i = length;
                                    xSObjectList = attributeUses;
                                    str3 = str6;
                                    reportSchemaError(str5, new Object[]{xSComplexTypeDecl == null ? xSAttributeGroupDecl.fName : xSComplexTypeDecl.getName(), xSAttributeUseImpl.fAttrDecl.getName(), addAttributeUse}, element2);
                                } else {
                                    i = length;
                                    xSObjectList = attributeUses;
                                    str3 = str6;
                                }
                            } else {
                                i = length;
                                xSObjectList = attributeUses;
                                str3 = str6;
                                if (xSAttributeUseImpl != attributeUseNoProhibited) {
                                    if (xSComplexTypeDecl == null) {
                                        str4 = str7;
                                    } else {
                                        str4 = "ct-props-correct.4";
                                    }
                                    s = 2;
                                    reportSchemaError(str4, new Object[]{xSComplexTypeDecl == null ? xSAttributeGroupDecl.fName : xSComplexTypeDecl.getName(), xSAttributeUseImpl.fAttrDecl.getName()}, element2);
                                }
                            }
                            s = 2;
                        }
                        i2++;
                        s2 = s;
                        attributeUses = xSObjectList;
                        str2 = str2;
                        str = str;
                        length = i;
                        str6 = str3;
                    }
                    if (traverseLocal.fAttributeWC != null) {
                        if (xSAttributeGroupDecl.fAttributeWC == null) {
                            xSAttributeGroupDecl.fAttributeWC = traverseLocal.fAttributeWC;
                        } else {
                            xSAttributeGroupDecl.fAttributeWC = xSAttributeGroupDecl.fAttributeWC.performIntersectionWith(traverseLocal.fAttributeWC, xSAttributeGroupDecl.fAttributeWC.fProcessContents);
                            if (xSAttributeGroupDecl.fAttributeWC == null) {
                                reportSchemaError(xSComplexTypeDecl == null ? str : str2, new Object[]{xSComplexTypeDecl == null ? xSAttributeGroupDecl.fName : xSComplexTypeDecl.getName()}, element2);
                            }
                        }
                    }
                }
            } else {
                XSAttributeUseImpl traverseLocal2 = this.fSchemaHandler.fAttributeTraverser.traverseLocal(element2, xSDocumentInfo, schemaGrammar, xSComplexTypeDecl);
                if (traverseLocal2 != null) {
                    if (traverseLocal2.fUse == 2) {
                        xSAttributeGroupDecl.addAttributeUse(traverseLocal2);
                    } else {
                        XSAttributeUse attributeUseNoProhibited2 = xSAttributeGroupDecl.getAttributeUseNoProhibited(traverseLocal2.fAttrDecl.getNamespace(), traverseLocal2.fAttrDecl.getName());
                        if (attributeUseNoProhibited2 == null) {
                            String addAttributeUse2 = xSAttributeGroupDecl.addAttributeUse(traverseLocal2);
                            if (addAttributeUse2 != null) {
                                if (xSComplexTypeDecl != null) {
                                    str6 = "ct-props-correct.5";
                                }
                                reportSchemaError(str6, new Object[]{xSComplexTypeDecl == null ? xSAttributeGroupDecl.fName : xSComplexTypeDecl.getName(), traverseLocal2.fAttrDecl.getName(), addAttributeUse2}, element2);
                            }
                        } else if (attributeUseNoProhibited2 != traverseLocal2) {
                            if (xSComplexTypeDecl != null) {
                                str7 = "ct-props-correct.4";
                            }
                            reportSchemaError(str7, new Object[]{xSComplexTypeDecl == null ? xSAttributeGroupDecl.fName : xSComplexTypeDecl.getName(), traverseLocal2.fAttrDecl.getName()}, element2);
                        }
                    }
                }
            }
            element2 = DOMUtil.getNextSiblingElement(element2);
        }
        if (element2 == null || !DOMUtil.getLocalName(element2).equals(SchemaSymbols.ELT_ANYATTRIBUTE)) {
            return element2;
        }
        XSWildcardDecl traverseAnyAttribute = this.fSchemaHandler.fWildCardTraverser.traverseAnyAttribute(element2, xSDocumentInfo, schemaGrammar);
        if (xSAttributeGroupDecl.fAttributeWC == null) {
            xSAttributeGroupDecl.fAttributeWC = traverseAnyAttribute;
        } else {
            xSAttributeGroupDecl.fAttributeWC = traverseAnyAttribute.performIntersectionWith(xSAttributeGroupDecl.fAttributeWC, traverseAnyAttribute.fProcessContents);
            if (xSAttributeGroupDecl.fAttributeWC == null) {
                reportSchemaError(xSComplexTypeDecl == null ? str : str2, new Object[]{xSComplexTypeDecl == null ? xSAttributeGroupDecl.fName : xSComplexTypeDecl.getName()}, element2);
            }
        }
        return DOMUtil.getNextSiblingElement(element2);
    }

    /* access modifiers changed from: package-private */
    public void reportSchemaError(String str, Object[] objArr, Element element) {
        this.fSchemaHandler.reportSchemaError(str, objArr, element);
    }

    /* access modifiers changed from: package-private */
    public void checkNotationType(String str, XSTypeDefinition xSTypeDefinition, Element element) {
        if (xSTypeDefinition.getTypeCategory() == 16) {
            XSSimpleType xSSimpleType = (XSSimpleType) xSTypeDefinition;
            if (xSSimpleType.getVariety() == 1 && xSSimpleType.getPrimitiveKind() == 20 && (xSSimpleType.getDefinedFacets() & 2048) == 0) {
                reportSchemaError("enumeration-required-notation", new Object[]{xSTypeDefinition.getName(), str, DOMUtil.getLocalName(element)}, element);
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x007f: APUT  (r5v5 java.lang.Object[]), (0 ??[int, short, byte, char]), (r4v3 java.lang.String) */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0090, code lost:
        if (r3 > 1) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x009f, code lost:
        if (r3 > 1) goto L_0x0092;
     */
    public XSParticleDecl checkOccurrences(XSParticleDecl xSParticleDecl, String str, Element element, int i, long j) {
        int i2 = xSParticleDecl.fMinOccurs;
        int i3 = xSParticleDecl.fMaxOccurs;
        int i4 = 1;
        boolean z = (j & ((long) (1 << XSAttributeChecker.ATTIDX_MINOCCURS))) != 0;
        boolean z2 = (j & ((long) (1 << XSAttributeChecker.ATTIDX_MAXOCCURS))) != 0;
        boolean z3 = (i & 1) != 0;
        boolean z4 = (i & 8) != 0;
        boolean z5 = (i & 2) != 0;
        if ((i & 4) != 0) {
            if (!z) {
                reportSchemaError("s4s-att-not-allowed", new Object[]{str, "minOccurs"}, element);
                i2 = 1;
            }
            if (!z2) {
                reportSchemaError("s4s-att-not-allowed", new Object[]{str, "maxOccurs"}, element);
                i3 = 1;
            }
        }
        if (i2 == 0 && i3 == 0) {
            xSParticleDecl.fType = 0;
            return null;
        }
        if (z3) {
            if (i3 != 1) {
                Object[] objArr = new Object[2];
                objArr[0] = i3 == -1 ? SchemaSymbols.ATTVAL_UNBOUNDED : Integer.toString(i3);
                objArr[1] = ((XSElementDecl) xSParticleDecl.fValue).getName();
                reportSchemaError("cos-all-limited.2", objArr, element);
            }
            i4 = i3;
            xSParticleDecl.fMinOccurs = i2;
            xSParticleDecl.fMaxOccurs = i4;
            return xSParticleDecl;
        }
        if ((z4 || z5) && i3 != 1) {
            reportSchemaError("cos-all-limited.1.2", null, element);
        }
        i4 = i3;
        xSParticleDecl.fMinOccurs = i2;
        xSParticleDecl.fMaxOccurs = i4;
        return xSParticleDecl;
        i2 = 1;
        xSParticleDecl.fMinOccurs = i2;
        xSParticleDecl.fMaxOccurs = i4;
        return xSParticleDecl;
    }

    private static String processAttValue(String str) {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (charAt == '\"' || charAt == '<' || charAt == '&' || charAt == '\t' || charAt == '\n' || charAt == '\r') {
                return escapeAttValue(str, i);
            }
        }
        return str;
    }

    private static String escapeAttValue(String str, int i) {
        int length = str.length();
        StringBuffer stringBuffer = new StringBuffer(length);
        stringBuffer.append(str.substring(0, i));
        while (i < length) {
            char charAt = str.charAt(i);
            if (charAt == '\"') {
                stringBuffer.append(SerializerConstants.ENTITY_QUOT);
            } else if (charAt == '<') {
                stringBuffer.append(SerializerConstants.ENTITY_LT);
            } else if (charAt == '&') {
                stringBuffer.append(SerializerConstants.ENTITY_AMP);
            } else if (charAt == '\t') {
                stringBuffer.append("&#x9;");
            } else if (charAt == '\n') {
                stringBuffer.append(SerializerConstants.ENTITY_CRLF);
            } else if (charAt == '\r') {
                stringBuffer.append("&#xD;");
            } else {
                stringBuffer.append(charAt);
            }
            i++;
        }
        return stringBuffer.toString();
    }
}
