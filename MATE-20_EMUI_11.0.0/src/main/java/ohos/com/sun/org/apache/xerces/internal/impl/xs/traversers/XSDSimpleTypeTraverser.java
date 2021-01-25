package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.util.ArrayList;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeFacetException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDAbstractTraverser;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public class XSDSimpleTypeTraverser extends XSDAbstractTraverser {
    private boolean fIsBuiltIn = false;

    XSDSimpleTypeTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    public XSSimpleType traverseGlobal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, true, xSDocumentInfo);
        String str = (String) checkAttributes[XSAttributeChecker.ATTIDX_NAME];
        if (str == null) {
            checkAttributes[XSAttributeChecker.ATTIDX_NAME] = "(no name)";
        }
        XSSimpleType traverseSimpleTypeDecl = traverseSimpleTypeDecl(element, checkAttributes, xSDocumentInfo, schemaGrammar);
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        if (str == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_SIMPLETYPE, SchemaSymbols.ATT_NAME}, element);
            traverseSimpleTypeDecl = null;
        }
        if (traverseSimpleTypeDecl != null) {
            if (schemaGrammar.getGlobalTypeDecl(traverseSimpleTypeDecl.getName()) == null) {
                schemaGrammar.addGlobalSimpleTypeDecl(traverseSimpleTypeDecl);
            }
            String schemaDocument2SystemId = this.fSchemaHandler.schemaDocument2SystemId(xSDocumentInfo);
            XSTypeDefinition globalTypeDecl = schemaGrammar.getGlobalTypeDecl(traverseSimpleTypeDecl.getName(), schemaDocument2SystemId);
            if (globalTypeDecl == null) {
                schemaGrammar.addGlobalSimpleTypeDecl(traverseSimpleTypeDecl, schemaDocument2SystemId);
            }
            if (this.fSchemaHandler.fTolerateDuplicates) {
                if (globalTypeDecl != null && (globalTypeDecl instanceof XSSimpleType)) {
                    traverseSimpleTypeDecl = (XSSimpleType) globalTypeDecl;
                }
                this.fSchemaHandler.addGlobalTypeDecl(traverseSimpleTypeDecl);
            }
        }
        return traverseSimpleTypeDecl;
    }

    /* access modifiers changed from: package-private */
    public XSSimpleType traverseLocal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        XSSimpleType simpleType = getSimpleType(genAnonTypeName(element), element, checkAttributes, xSDocumentInfo, schemaGrammar);
        if (simpleType instanceof XSSimpleTypeDecl) {
            ((XSSimpleTypeDecl) simpleType).setAnonymous(true);
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return simpleType;
    }

    private XSSimpleType traverseSimpleTypeDecl(Element element, Object[] objArr, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        return getSimpleType((String) objArr[XSAttributeChecker.ATTIDX_NAME], element, objArr, xSDocumentInfo, schemaGrammar);
    }

    private String genAnonTypeName(Element element) {
        StringBuffer stringBuffer = new StringBuffer("#AnonType_");
        Element parent = DOMUtil.getParent(element);
        while (parent != null && parent != DOMUtil.getRoot(DOMUtil.getDocument(parent))) {
            stringBuffer.append(parent.getAttribute(SchemaSymbols.ATT_NAME));
            parent = DOMUtil.getParent(parent);
        }
        return stringBuffer.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:127:0x0266  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x026a  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x028a A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0078  */
    private XSSimpleType getSimpleType(String str, Element element, Object[] objArr, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        XSAnnotationImpl[] xSAnnotationImplArr;
        Object[] objArr2;
        Object[] objArr3;
        Object[] objArr4;
        short s;
        int i;
        Object[] objArr5;
        Vector vector;
        Element element2;
        XSAnnotationImpl[] xSAnnotationImplArr2;
        XSAnnotationImpl[] xSAnnotationImplArr3;
        Element element3;
        XSSimpleType xSSimpleType;
        Vector vector2;
        ArrayList arrayList;
        short s2;
        XSSimpleType xSSimpleType2;
        XSObjectListImpl xSObjectListImpl;
        int i2;
        Element element4;
        Object[] objArr6;
        XSAnnotationImpl[] xSAnnotationImplArr4;
        XSSimpleType xSSimpleType3;
        Element element5;
        XSObjectList xSObjectList;
        XSObjectList xSObjectList2;
        XSObjectList xSObjectList3;
        XSObjectListImpl xSObjectListImpl2;
        Element element6;
        XSAnnotationImpl[] xSAnnotationImplArr5;
        XInt xInt = (XInt) objArr[XSAttributeChecker.ATTIDX_FINAL];
        int intValue = xInt == null ? xSDocumentInfo.fFinalDefault : xInt.intValue();
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
            String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation != null) {
                xSAnnotationImplArr5 = new XSAnnotationImpl[]{traverseSyntheticAnnotation(element, syntheticAnnotation, objArr, false, xSDocumentInfo)};
            } else {
                xSAnnotationImplArr = null;
                if (firstChildElement != null) {
                    reportSchemaError("s4s-elt-must-match.2", new Object[]{SchemaSymbols.ELT_SIMPLETYPE, "(annotation?, (restriction | list | union))"}, element);
                    return errorType(str, xSDocumentInfo.fTargetNamespace, 2);
                }
                String localName = DOMUtil.getLocalName(firstChildElement);
                if (localName.equals(SchemaSymbols.ELT_RESTRICTION)) {
                    s = 2;
                    objArr4 = 1;
                    objArr3 = null;
                    objArr2 = null;
                } else if (localName.equals(SchemaSymbols.ELT_LIST)) {
                    objArr3 = 1;
                    objArr4 = null;
                    objArr2 = null;
                    s = 16;
                } else if (localName.equals(SchemaSymbols.ELT_UNION)) {
                    objArr2 = 1;
                    objArr4 = null;
                    objArr3 = null;
                    s = 8;
                } else {
                    reportSchemaError("s4s-elt-must-match.1", new Object[]{SchemaSymbols.ELT_SIMPLETYPE, "(annotation?, (restriction | list | union))", localName}, element);
                    return errorType(str, xSDocumentInfo.fTargetNamespace, 2);
                }
                Element nextSiblingElement = DOMUtil.getNextSiblingElement(firstChildElement);
                if (nextSiblingElement != null) {
                    reportSchemaError("s4s-elt-must-match.1", new Object[]{SchemaSymbols.ELT_SIMPLETYPE, "(annotation?, (restriction | list | union))", DOMUtil.getLocalName(nextSiblingElement)}, nextSiblingElement);
                }
                Object[] checkAttributes = this.fAttrChecker.checkAttributes(firstChildElement, false, xSDocumentInfo);
                if (objArr4 != null) {
                    i = XSAttributeChecker.ATTIDX_BASE;
                } else {
                    i = XSAttributeChecker.ATTIDX_ITEMTYPE;
                }
                QName qName = (QName) checkAttributes[i];
                Vector vector3 = (Vector) checkAttributes[XSAttributeChecker.ATTIDX_MEMBERTYPES];
                Element firstChildElement2 = DOMUtil.getFirstChildElement(firstChildElement);
                if (firstChildElement2 == null || !DOMUtil.getLocalName(firstChildElement2).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    String syntheticAnnotation2 = DOMUtil.getSyntheticAnnotation(firstChildElement);
                    if (syntheticAnnotation2 != null) {
                        element6 = firstChildElement2;
                        vector = vector3;
                        objArr5 = checkAttributes;
                        XSAnnotationImpl traverseSyntheticAnnotation = traverseSyntheticAnnotation(firstChildElement, syntheticAnnotation2, objArr5, false, xSDocumentInfo);
                        if (xSAnnotationImplArr == null) {
                            xSAnnotationImplArr2 = new XSAnnotationImpl[]{traverseSyntheticAnnotation};
                            element2 = element6;
                        } else {
                            xSAnnotationImplArr2 = new XSAnnotationImpl[]{xSAnnotationImplArr[0], traverseSyntheticAnnotation};
                        }
                    } else {
                        element6 = firstChildElement2;
                        vector = vector3;
                        objArr5 = checkAttributes;
                        xSAnnotationImplArr2 = xSAnnotationImplArr;
                    }
                    element2 = element6;
                } else {
                    XSAnnotationImpl traverseAnnotationDecl = traverseAnnotationDecl(firstChildElement2, checkAttributes, false, xSDocumentInfo);
                    if (traverseAnnotationDecl != null) {
                        xSAnnotationImplArr = xSAnnotationImplArr == null ? new XSAnnotationImpl[]{traverseAnnotationDecl} : new XSAnnotationImpl[]{xSAnnotationImplArr[0], traverseAnnotationDecl};
                    }
                    element2 = DOMUtil.getNextSiblingElement(firstChildElement2);
                    vector = vector3;
                    objArr5 = checkAttributes;
                    xSAnnotationImplArr2 = xSAnnotationImplArr;
                }
                if ((objArr4 == null && objArr3 == null) || qName == null) {
                    xSAnnotationImplArr3 = xSAnnotationImplArr2;
                    element3 = element2;
                    xSSimpleType = null;
                } else {
                    xSAnnotationImplArr3 = xSAnnotationImplArr2;
                    element3 = element2;
                    xSSimpleType = findDTValidator(firstChildElement, str, qName, s, xSDocumentInfo);
                    if (xSSimpleType == null && this.fIsBuiltIn) {
                        this.fIsBuiltIn = false;
                        return null;
                    }
                }
                if (objArr2 != null) {
                    Vector vector4 = vector;
                    if (vector4 == null || vector4.size() <= 0) {
                        vector2 = vector4;
                    } else {
                        int size = vector4.size();
                        ArrayList arrayList2 = new ArrayList(size);
                        int i3 = 0;
                        while (i3 < size) {
                            XSSimpleType findDTValidator = findDTValidator(firstChildElement, str, (QName) vector4.elementAt(i3), 8, xSDocumentInfo);
                            if (findDTValidator != null) {
                                if (findDTValidator.getVariety() == 3) {
                                    XSObjectList memberTypes = findDTValidator.getMemberTypes();
                                    for (int i4 = 0; i4 < memberTypes.getLength(); i4++) {
                                        arrayList2.add(memberTypes.item(i4));
                                    }
                                } else {
                                    arrayList2.add(findDTValidator);
                                }
                            }
                            i3++;
                            arrayList2 = arrayList2;
                            size = size;
                            vector4 = vector4;
                        }
                        arrayList = arrayList2;
                        vector2 = vector4;
                        s2 = 3;
                        if (element3 != null || !DOMUtil.getLocalName(element3).equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                            i2 = 2;
                            i2 = 2;
                            i2 = 2;
                            i2 = 2;
                            if ((objArr4 == null || objArr3 != null) && qName == null) {
                                xSObjectListImpl = null;
                                reportSchemaError(objArr3 == null ? "src-simple-type.3.b" : "src-simple-type.2.b", null, firstChildElement);
                            } else {
                                xSObjectListImpl = null;
                                if (objArr2 != null && (vector2 == null || vector2.size() == 0)) {
                                    reportSchemaError("src-union-memberTypes-or-simpleTypes", null, firstChildElement);
                                }
                            }
                        } else if (objArr4 == null && objArr3 == null) {
                            if (objArr2 != null) {
                                if (arrayList == null) {
                                    i2 = 2;
                                    arrayList = new ArrayList(2);
                                } else {
                                    i2 = 2;
                                }
                                do {
                                    XSSimpleType traverseLocal = traverseLocal(element3, xSDocumentInfo, schemaGrammar);
                                    if (traverseLocal != null) {
                                        if (traverseLocal.getVariety() == s2) {
                                            XSObjectList memberTypes2 = traverseLocal.getMemberTypes();
                                            for (int i5 = 0; i5 < memberTypes2.getLength(); i5++) {
                                                arrayList.add(memberTypes2.item(i5));
                                            }
                                        } else {
                                            arrayList.add(traverseLocal);
                                        }
                                    }
                                    element3 = DOMUtil.getNextSiblingElement(element3);
                                    if (element3 == null) {
                                        break;
                                    }
                                } while (DOMUtil.getLocalName(element3).equals(SchemaSymbols.ELT_SIMPLETYPE));
                            } else {
                                i2 = 2;
                            }
                            xSObjectListImpl = null;
                        } else {
                            i2 = 2;
                            if (qName != null) {
                                reportSchemaError(objArr3 != null ? "src-simple-type.3.a" : "src-simple-type.2.a", null, element3);
                            }
                            if (xSSimpleType == null) {
                                xSSimpleType = traverseLocal(element3, xSDocumentInfo, schemaGrammar);
                            }
                            Element nextSiblingElement2 = DOMUtil.getNextSiblingElement(element3);
                            xSSimpleType2 = xSSimpleType;
                            element4 = nextSiblingElement2;
                            xSObjectListImpl = null;
                            if ((objArr4 == null || objArr3 != null) && xSSimpleType2 == null) {
                                this.fAttrChecker.returnAttrArray(objArr5, xSDocumentInfo);
                                String str2 = xSDocumentInfo.fTargetNamespace;
                                if (objArr4 == null) {
                                    i2 = 16;
                                }
                                return errorType(str, str2, i2 == 1 ? (short) 1 : 0);
                            } else if (objArr2 != null && (arrayList == null || arrayList.size() == 0)) {
                                this.fAttrChecker.returnAttrArray(objArr5, xSDocumentInfo);
                                return errorType(str, xSDocumentInfo.fTargetNamespace, 8);
                            } else if (objArr3 == null || !isListDatatype(xSSimpleType2)) {
                                if (objArr4 != null) {
                                    SchemaDVFactory schemaDVFactory = this.fSchemaHandler.fDVFactory;
                                    String str3 = xSDocumentInfo.fTargetNamespace;
                                    short s3 = (short) intValue;
                                    if (xSAnnotationImplArr3 == null) {
                                        xSObjectListImpl2 = xSObjectListImpl;
                                    } else {
                                        xSObjectListImpl2 = new XSObjectListImpl(xSAnnotationImplArr3, xSAnnotationImplArr3.length);
                                    }
                                    objArr6 = objArr5;
                                    xSSimpleType3 = schemaDVFactory.createTypeRestriction(str, str3, s3, xSSimpleType2, xSObjectListImpl2);
                                    xSAnnotationImplArr4 = xSAnnotationImplArr3;
                                } else {
                                    objArr6 = objArr5;
                                    if (objArr3 != null) {
                                        SchemaDVFactory schemaDVFactory2 = this.fSchemaHandler.fDVFactory;
                                        String str4 = xSDocumentInfo.fTargetNamespace;
                                        short s4 = (short) intValue;
                                        if (xSAnnotationImplArr3 == null) {
                                            xSObjectList3 = null;
                                        } else {
                                            xSObjectList3 = new XSObjectListImpl(xSAnnotationImplArr3, xSAnnotationImplArr3.length);
                                        }
                                        xSAnnotationImplArr4 = xSAnnotationImplArr3;
                                        xSSimpleType3 = schemaDVFactory2.createTypeList(str, str4, s4, xSSimpleType2, xSObjectList3);
                                    } else {
                                        xSAnnotationImplArr4 = xSAnnotationImplArr3;
                                        if (objArr2 != null) {
                                            XSSimpleType[] xSSimpleTypeArr = (XSSimpleType[]) arrayList.toArray(new XSSimpleType[arrayList.size()]);
                                            SchemaDVFactory schemaDVFactory3 = this.fSchemaHandler.fDVFactory;
                                            String str5 = xSDocumentInfo.fTargetNamespace;
                                            short s5 = (short) intValue;
                                            if (xSAnnotationImplArr4 == null) {
                                                xSObjectList2 = null;
                                            } else {
                                                xSObjectList2 = new XSObjectListImpl(xSAnnotationImplArr4, xSAnnotationImplArr4.length);
                                            }
                                            xSSimpleType3 = schemaDVFactory3.createTypeUnion(str, str5, s5, xSSimpleTypeArr, xSObjectList2);
                                        } else {
                                            xSSimpleType3 = null;
                                        }
                                    }
                                }
                                if (objArr4 == null || element4 == null) {
                                    element5 = element4;
                                } else {
                                    XSDAbstractTraverser.FacetInfo traverseFacets = traverseFacets(element4, xSSimpleType2, xSDocumentInfo);
                                    element5 = traverseFacets.nodeAfterFacets;
                                    try {
                                        this.fValidationState.setNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                                        xSSimpleType3.applyFacets(traverseFacets.facetdata, traverseFacets.fPresentFacets, traverseFacets.fFixedFacets, this.fValidationState);
                                    } catch (InvalidDatatypeFacetException e) {
                                        reportSchemaError(e.getKey(), e.getArgs(), firstChildElement);
                                        SchemaDVFactory schemaDVFactory4 = this.fSchemaHandler.fDVFactory;
                                        String str6 = xSDocumentInfo.fTargetNamespace;
                                        short s6 = (short) intValue;
                                        if (xSAnnotationImplArr4 == null) {
                                            xSObjectList = null;
                                        } else {
                                            xSObjectList = new XSObjectListImpl(xSAnnotationImplArr4, xSAnnotationImplArr4.length);
                                        }
                                        xSSimpleType3 = schemaDVFactory4.createTypeRestriction(str, str6, s6, xSSimpleType2, xSObjectList);
                                    }
                                }
                                if (element5 != null) {
                                    if (objArr4 != null) {
                                        reportSchemaError("s4s-elt-must-match.1", new Object[]{SchemaSymbols.ELT_RESTRICTION, "(annotation?, (simpleType?, (minExclusive | minInclusive | maxExclusive | maxInclusive | totalDigits | fractionDigits | length | minLength | maxLength | enumeration | whiteSpace | pattern)*))", DOMUtil.getLocalName(element5)}, element5);
                                    } else if (objArr3 != null) {
                                        reportSchemaError("s4s-elt-must-match.1", new Object[]{SchemaSymbols.ELT_LIST, "(annotation?, (simpleType?))", DOMUtil.getLocalName(element5)}, element5);
                                    } else if (objArr2 != null) {
                                        reportSchemaError("s4s-elt-must-match.1", new Object[]{SchemaSymbols.ELT_UNION, "(annotation?, (simpleType*))", DOMUtil.getLocalName(element5)}, element5);
                                    }
                                }
                                this.fAttrChecker.returnAttrArray(objArr6, xSDocumentInfo);
                                return xSSimpleType3;
                            } else {
                                Object[] objArr7 = new Object[i2];
                                objArr7[0] = str;
                                objArr7[1] = xSSimpleType2.getName();
                                reportSchemaError("cos-st-restricts.2.1", objArr7, firstChildElement);
                                this.fAttrChecker.returnAttrArray(objArr5, xSDocumentInfo);
                                return errorType(str, xSDocumentInfo.fTargetNamespace, 16);
                            }
                        }
                        xSSimpleType2 = xSSimpleType;
                        element4 = element3;
                        if (objArr4 == null) {
                        }
                        this.fAttrChecker.returnAttrArray(objArr5, xSDocumentInfo);
                        String str22 = xSDocumentInfo.fTargetNamespace;
                        if (objArr4 == null) {
                        }
                        return errorType(str, str22, i2 == 1 ? (short) 1 : 0);
                    }
                } else {
                    vector2 = vector;
                }
                s2 = 3;
                arrayList = null;
                if (element3 != null) {
                }
                i2 = 2;
                i2 = 2;
                i2 = 2;
                i2 = 2;
                if (objArr4 == null) {
                }
                xSObjectListImpl = null;
                reportSchemaError(objArr3 == null ? "src-simple-type.3.b" : "src-simple-type.2.b", null, firstChildElement);
                xSSimpleType2 = xSSimpleType;
                element4 = element3;
                if (objArr4 == null) {
                }
                this.fAttrChecker.returnAttrArray(objArr5, xSDocumentInfo);
                String str222 = xSDocumentInfo.fTargetNamespace;
                if (objArr4 == null) {
                }
                return errorType(str, str222, i2 == 1 ? (short) 1 : 0);
            }
        } else {
            XSAnnotationImpl traverseAnnotationDecl2 = traverseAnnotationDecl(firstChildElement, objArr, false, xSDocumentInfo);
            xSAnnotationImplArr5 = traverseAnnotationDecl2 != null ? new XSAnnotationImpl[]{traverseAnnotationDecl2} : null;
            firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
        }
        xSAnnotationImplArr = xSAnnotationImplArr5;
        if (firstChildElement != null) {
        }
    }

    private XSSimpleType findDTValidator(Element element, String str, QName qName, short s, XSDocumentInfo xSDocumentInfo) {
        XSTypeDefinition xSTypeDefinition;
        if (qName == null || (xSTypeDefinition = (XSTypeDefinition) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 7, qName, element)) == null) {
            return null;
        }
        if (xSTypeDefinition.getTypeCategory() != 16) {
            reportSchemaError("cos-st-restricts.1.1", new Object[]{qName.rawname, str}, element);
            return null;
        } else if (xSTypeDefinition == SchemaGrammar.fAnySimpleType && s == 2) {
            if (checkBuiltIn(str, xSDocumentInfo.fTargetNamespace)) {
                return null;
            }
            reportSchemaError("cos-st-restricts.1.1", new Object[]{qName.rawname, str}, element);
            return null;
        } else if ((xSTypeDefinition.getFinal() & s) == 0) {
            return (XSSimpleType) xSTypeDefinition;
        } else {
            if (s == 2) {
                reportSchemaError("st-props-correct.3", new Object[]{str, qName.rawname}, element);
            } else if (s == 16) {
                reportSchemaError("cos-st-restricts.2.3.1.1", new Object[]{qName.rawname, str}, element);
            } else if (s == 8) {
                reportSchemaError("cos-st-restricts.3.3.1.1", new Object[]{qName.rawname, str}, element);
            }
            return null;
        }
    }

    private final boolean checkBuiltIn(String str, String str2) {
        if (str2 != SchemaSymbols.URI_SCHEMAFORSCHEMA) {
            return false;
        }
        if (SchemaGrammar.SG_SchemaNS.getGlobalTypeDecl(str) != null) {
            this.fIsBuiltIn = true;
        }
        return this.fIsBuiltIn;
    }

    private boolean isListDatatype(XSSimpleType xSSimpleType) {
        if (xSSimpleType.getVariety() == 2) {
            return true;
        }
        if (xSSimpleType.getVariety() == 3) {
            XSObjectList memberTypes = xSSimpleType.getMemberTypes();
            for (int i = 0; i < memberTypes.getLength(); i++) {
                if (((XSSimpleType) memberTypes.item(i)).getVariety() == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private XSSimpleType errorType(String str, String str2, short s) {
        XSSimpleType xSSimpleType = (XSSimpleType) SchemaGrammar.SG_SchemaNS.getTypeDefinition("string");
        if (s == 2) {
            return this.fSchemaHandler.fDVFactory.createTypeRestriction(str, str2, 0, xSSimpleType, null);
        }
        if (s == 8) {
            return this.fSchemaHandler.fDVFactory.createTypeUnion(str, str2, 0, new XSSimpleType[]{xSSimpleType}, null);
        }
        if (s != 16) {
            return null;
        }
        return this.fSchemaHandler.fDVFactory.createTypeList(str, str2, 0, xSSimpleType, null);
    }
}
