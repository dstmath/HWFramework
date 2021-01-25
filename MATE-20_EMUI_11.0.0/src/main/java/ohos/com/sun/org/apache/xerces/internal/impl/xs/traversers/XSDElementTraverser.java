package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSConstraints;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObject;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public class XSDElementTraverser extends XSDAbstractTraverser {
    boolean fDeferTraversingLocalElements;
    protected final XSElementDecl fTempElementDecl = new XSElementDecl();

    XSDElementTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    public XSParticleDecl traverseLocal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, int i, XSObject xSObject) {
        XSParticleDecl xSParticleDecl;
        if (this.fSchemaHandler.fDeclPool != null) {
            xSParticleDecl = this.fSchemaHandler.fDeclPool.getParticleDecl();
        } else {
            xSParticleDecl = new XSParticleDecl();
        }
        if (this.fDeferTraversingLocalElements) {
            xSParticleDecl.fType = 1;
            Attr attributeNode = element.getAttributeNode(SchemaSymbols.ATT_MINOCCURS);
            if (attributeNode != null) {
                try {
                    int parseInt = Integer.parseInt(XMLChar.trim(attributeNode.getValue()));
                    if (parseInt >= 0) {
                        xSParticleDecl.fMinOccurs = parseInt;
                    }
                } catch (NumberFormatException unused) {
                }
            }
            this.fSchemaHandler.fillInLocalElemInfo(element, xSDocumentInfo, i, xSObject, xSParticleDecl);
            return xSParticleDecl;
        }
        traverseLocal(xSParticleDecl, element, xSDocumentInfo, schemaGrammar, i, xSObject, null);
        if (xSParticleDecl.fType == 0) {
            return null;
        }
        return xSParticleDecl;
    }

    /* access modifiers changed from: protected */
    public void traverseLocal(XSParticleDecl xSParticleDecl, Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, int i, XSObject xSObject, String[] strArr) {
        short s;
        XSAnnotationImpl xSAnnotationImpl;
        XSElementDecl xSElementDecl;
        XSObjectList xSObjectList;
        XSObjectListImpl xSObjectListImpl;
        if (strArr != null) {
            xSDocumentInfo.fNamespaceSupport.setEffectiveContext(strArr);
        }
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        QName qName = (QName) checkAttributes[XSAttributeChecker.ATTIDX_REF];
        XInt xInt = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MINOCCURS];
        XInt xInt2 = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MAXOCCURS];
        XSAnnotationImpl xSAnnotationImpl2 = null;
        if (element.getAttributeNode(SchemaSymbols.ATT_REF) == null) {
            s = 1;
            xSElementDecl = traverseNamedElement(element, checkAttributes, xSDocumentInfo, schemaGrammar, false, xSObject);
            xSAnnotationImpl = null;
        } else if (qName != null) {
            XSElementDecl xSElementDecl2 = (XSElementDecl) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 3, qName, element);
            Element firstChildElement = DOMUtil.getFirstChildElement(element);
            if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
                if (syntheticAnnotation != null) {
                    xSAnnotationImpl2 = traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo);
                    firstChildElement = firstChildElement;
                }
            } else {
                XSAnnotationImpl traverseAnnotationDecl = traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo);
                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                xSAnnotationImpl2 = traverseAnnotationDecl;
            }
            if (firstChildElement != null) {
                reportSchemaError("src-element.2.2", new Object[]{qName.rawname, DOMUtil.getLocalName(firstChildElement)}, firstChildElement);
            }
            s = 1;
            xSAnnotationImpl = xSAnnotationImpl2;
            xSElementDecl = xSElementDecl2;
        } else {
            s = 1;
            xSElementDecl = null;
            xSAnnotationImpl = null;
        }
        xSParticleDecl.fMinOccurs = xInt.intValue();
        xSParticleDecl.fMaxOccurs = xInt2.intValue();
        if (xSElementDecl != null) {
            xSParticleDecl.fType = s;
            xSParticleDecl.fValue = xSElementDecl;
        } else {
            xSParticleDecl.fType = 0;
        }
        if (qName != null) {
            if (xSAnnotationImpl != null) {
                xSObjectListImpl = new XSObjectListImpl();
                xSObjectListImpl.addXSObject(xSAnnotationImpl);
            } else {
                xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
            }
            xSParticleDecl.fAnnotations = xSObjectListImpl;
        } else {
            if (xSElementDecl != null) {
                xSObjectList = xSElementDecl.fAnnotations;
            } else {
                xSObjectList = XSObjectListImpl.EMPTY_LIST;
            }
            xSParticleDecl.fAnnotations = xSObjectList;
        }
        checkOccurrences(xSParticleDecl, SchemaSymbols.ELT_ELEMENT, (Element) element.getParentNode(), i, ((Long) checkAttributes[XSAttributeChecker.ATTIDX_FROMDEFAULT]).longValue());
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
    }

    /* access modifiers changed from: package-private */
    public XSElementDecl traverseGlobal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, true, xSDocumentInfo);
        XSElementDecl traverseNamedElement = traverseNamedElement(element, checkAttributes, xSDocumentInfo, schemaGrammar, true, null);
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return traverseNamedElement;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x02c5  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x02d7  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x02f8  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0319  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x031e  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0356  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x03c2  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x03fa A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x03fb  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00b3  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00dd  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00e0  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x012d  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0139  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x015e  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0179  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0181  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0193  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01c7  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x01cd A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01ec  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01f2  */
    public XSElementDecl traverseNamedElement(Element element, Object[] objArr, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, boolean z, XSObject xSObject) {
        XSElementDecl xSElementDecl;
        int i;
        Element firstChildElement;
        int i2;
        XSAnnotationImpl xSAnnotationImpl;
        XSObjectListImpl xSObjectListImpl;
        int i3;
        SchemaGrammar schemaGrammar2;
        XSTypeDefinition xSTypeDefinition;
        QName qName;
        String str;
        short s;
        int i4;
        XSElementDecl xSElementDecl2;
        String str2;
        String str3;
        String str4;
        QName qName2;
        QName qName3;
        XSElementDecl xSElementDecl3;
        String str5;
        int i5;
        String str6;
        String syntheticAnnotation;
        Boolean bool = (Boolean) objArr[XSAttributeChecker.ATTIDX_ABSTRACT];
        XInt xInt = (XInt) objArr[XSAttributeChecker.ATTIDX_BLOCK];
        String str7 = (String) objArr[XSAttributeChecker.ATTIDX_DEFAULT];
        XInt xInt2 = (XInt) objArr[XSAttributeChecker.ATTIDX_FINAL];
        String str8 = (String) objArr[XSAttributeChecker.ATTIDX_FIXED];
        XInt xInt3 = (XInt) objArr[XSAttributeChecker.ATTIDX_FORM];
        String str9 = (String) objArr[XSAttributeChecker.ATTIDX_NAME];
        Boolean bool2 = (Boolean) objArr[XSAttributeChecker.ATTIDX_NILLABLE];
        QName qName4 = (QName) objArr[XSAttributeChecker.ATTIDX_SUBSGROUP];
        QName qName5 = (QName) objArr[XSAttributeChecker.ATTIDX_TYPE];
        if (this.fSchemaHandler.fDeclPool != null) {
            xSElementDecl = this.fSchemaHandler.fDeclPool.getElementDecl();
        } else {
            xSElementDecl = new XSElementDecl();
        }
        if (str9 != null) {
            xSElementDecl.fName = this.fSymbolTable.addSymbol(str9);
        }
        if (z) {
            xSElementDecl.fTargetNamespace = xSDocumentInfo.fTargetNamespace;
            xSElementDecl.setIsGlobal();
        } else {
            if (xSObject instanceof XSComplexTypeDecl) {
                xSElementDecl.setIsLocal((XSComplexTypeDecl) xSObject);
            }
            if (xInt3 != null) {
                if (xInt3.intValue() == 1) {
                    xSElementDecl.fTargetNamespace = xSDocumentInfo.fTargetNamespace;
                } else {
                    xSElementDecl.fTargetNamespace = null;
                }
            } else if (xSDocumentInfo.fAreLocalElementsQualified) {
                xSElementDecl.fTargetNamespace = xSDocumentInfo.fTargetNamespace;
            } else {
                xSElementDecl.fTargetNamespace = null;
            }
            if (xInt != null) {
                xSElementDecl.fBlock = xSDocumentInfo.fBlockDefault;
                if (xSElementDecl.fBlock != 31) {
                    xSElementDecl.fBlock = (short) (xSElementDecl.fBlock & 7);
                }
            } else {
                xSElementDecl.fBlock = xInt.shortValue();
                if (!(xSElementDecl.fBlock == 31 || (xSElementDecl.fBlock | 7) == 7)) {
                    reportSchemaError("s4s-att-invalid-value", new Object[]{xSElementDecl.fName, "block", "must be (#all | List of (extension | restriction | substitution))"}, element);
                }
            }
            xSElementDecl.fFinal = xInt2 != null ? xSDocumentInfo.fFinalDefault : xInt2.shortValue();
            xSElementDecl.fFinal = (short) (xSElementDecl.fFinal & 3);
            if (bool2.booleanValue()) {
                xSElementDecl.setIsNillable();
            }
            if (bool != null && bool.booleanValue()) {
                xSElementDecl.setIsAbstract();
            }
            if (str8 == null) {
                xSElementDecl.fDefault = new ValidatedInfo();
                xSElementDecl.fDefault.normalizedValue = str8;
                xSElementDecl.setConstraintType(2);
                i = 1;
            } else if (str7 != null) {
                xSElementDecl.fDefault = new ValidatedInfo();
                xSElementDecl.fDefault.normalizedValue = str7;
                i = 1;
                xSElementDecl.setConstraintType(1);
            } else {
                i = 1;
                xSElementDecl.setConstraintType(0);
            }
            if (qName4 == null) {
                xSElementDecl.fSubGroup = (XSElementDecl) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 3, qName4, element);
            }
            firstChildElement = DOMUtil.getFirstChildElement(element);
            if (firstChildElement != null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
                if (syntheticAnnotation == null) {
                    i2 = i;
                    xSAnnotationImpl = traverseSyntheticAnnotation(element, syntheticAnnotation, objArr, false, xSDocumentInfo);
                    firstChildElement = firstChildElement;
                } else {
                    i2 = i;
                    xSAnnotationImpl = null;
                }
            } else {
                xSAnnotationImpl = traverseAnnotationDecl(firstChildElement, objArr, false, xSDocumentInfo);
                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                i2 = i;
            }
            if (xSAnnotationImpl == null) {
                xSObjectListImpl = new XSObjectListImpl();
                xSObjectListImpl.addXSObject(xSAnnotationImpl);
            } else {
                xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
            }
            xSElementDecl.fAnnotations = xSObjectListImpl;
            if (firstChildElement == null) {
                String localName = DOMUtil.getLocalName(firstChildElement);
                if (localName.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                    schemaGrammar2 = schemaGrammar;
                    xSTypeDefinition = this.fSchemaHandler.fComplexTypeTraverser.traverseLocal(firstChildElement, xSDocumentInfo, schemaGrammar2);
                    firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                } else {
                    schemaGrammar2 = schemaGrammar;
                    if (localName.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                        xSTypeDefinition = this.fSchemaHandler.fSimpleTypeTraverser.traverseLocal(firstChildElement, xSDocumentInfo, schemaGrammar2);
                        firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                    }
                }
                i3 = i2;
                if (xSTypeDefinition == null || qName5 == null) {
                    qName = qName5;
                } else {
                    qName = qName5;
                    xSTypeDefinition = (XSTypeDefinition) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 7, qName, element);
                    if (xSTypeDefinition == null) {
                        xSElementDecl.fUnresolvedTypeName = qName;
                    }
                }
                if (xSTypeDefinition == null && xSElementDecl.fSubGroup != null) {
                    xSTypeDefinition = xSElementDecl.fSubGroup.fType;
                }
                if (xSTypeDefinition == null) {
                    xSTypeDefinition = SchemaGrammar.fAnyType;
                }
                xSElementDecl.fType = xSTypeDefinition;
                if (firstChildElement != null) {
                    String localName2 = DOMUtil.getLocalName(firstChildElement);
                    while (firstChildElement != null && (localName2.equals(SchemaSymbols.ELT_KEY) || localName2.equals(SchemaSymbols.ELT_KEYREF) || localName2.equals(SchemaSymbols.ELT_UNIQUE))) {
                        if (localName2.equals(SchemaSymbols.ELT_KEY) || localName2.equals(SchemaSymbols.ELT_UNIQUE)) {
                            DOMUtil.setHidden(firstChildElement, this.fSchemaHandler.fHiddenNodes);
                            this.fSchemaHandler.fUniqueOrKeyTraverser.traverse(firstChildElement, xSElementDecl, xSDocumentInfo, schemaGrammar2);
                            if (DOMUtil.getAttrValue(firstChildElement, SchemaSymbols.ATT_NAME).length() != 0) {
                                XSDHandler xSDHandler = this.fSchemaHandler;
                                if (xSDocumentInfo.fTargetNamespace == null) {
                                    str6 = "," + DOMUtil.getAttrValue(firstChildElement, SchemaSymbols.ATT_NAME);
                                    qName3 = qName;
                                } else {
                                    StringBuilder sb = new StringBuilder();
                                    qName3 = qName;
                                    sb.append(xSDocumentInfo.fTargetNamespace);
                                    sb.append(",");
                                    sb.append(DOMUtil.getAttrValue(firstChildElement, SchemaSymbols.ATT_NAME));
                                    str6 = sb.toString();
                                }
                                XSDHandler xSDHandler2 = this.fSchemaHandler;
                                str5 = str9;
                                str4 = str8;
                                str3 = str7;
                                qName2 = qName4;
                                str2 = localName2;
                                xSElementDecl3 = xSElementDecl;
                                i5 = i2;
                                xSDHandler.checkForDuplicateNames(str6, 1, this.fSchemaHandler.getIDRegistry(), this.fSchemaHandler.getIDRegistry_sub(), firstChildElement, xSDocumentInfo);
                                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                                if (firstChildElement == null) {
                                    i2 = i5;
                                    str9 = str5;
                                    xSElementDecl = xSElementDecl3;
                                    localName2 = DOMUtil.getLocalName(firstChildElement);
                                    qName = qName3;
                                    qName4 = qName2;
                                    str8 = str4;
                                    str7 = str3;
                                } else {
                                    i2 = i5;
                                    str9 = str5;
                                    xSElementDecl = xSElementDecl3;
                                    qName = qName3;
                                    qName4 = qName2;
                                    str8 = str4;
                                    str7 = str3;
                                    localName2 = str2;
                                }
                                schemaGrammar2 = schemaGrammar;
                            }
                        } else if (localName2.equals(SchemaSymbols.ELT_KEYREF)) {
                            this.fSchemaHandler.storeKeyRef(firstChildElement, xSDocumentInfo, xSElementDecl);
                        }
                        qName3 = qName;
                        str2 = localName2;
                        xSElementDecl3 = xSElementDecl;
                        qName2 = qName4;
                        str5 = str9;
                        str4 = str8;
                        str3 = str7;
                        i5 = i2;
                        firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                        if (firstChildElement == null) {
                        }
                        schemaGrammar2 = schemaGrammar;
                    }
                }
                if (str9 == null) {
                    if (z) {
                        i4 = 2;
                        Object[] objArr2 = new Object[2];
                        s = 0;
                        objArr2[0] = SchemaSymbols.ELT_ELEMENT;
                        objArr2[i2] = SchemaSymbols.ATT_NAME;
                        reportSchemaError("s4s-att-must-appear", objArr2, element);
                    } else {
                        i4 = 2;
                        s = 0;
                        reportSchemaError("src-element.2.1", null, element);
                    }
                    str = "(no name)";
                } else {
                    i4 = 2;
                    s = 0;
                    str = str9;
                }
                if (firstChildElement != null) {
                    Object[] objArr3 = new Object[3];
                    objArr3[s] = str;
                    objArr3[i2] = "(annotation?, (simpleType | complexType)?, (unique | key | keyref)*))";
                    objArr3[i4] = DOMUtil.getLocalName(firstChildElement);
                    reportSchemaError("s4s-elt-must-match.1", objArr3, firstChildElement);
                }
                if (!(str7 == null || str8 == null)) {
                    Object[] objArr4 = new Object[i2];
                    objArr4[s] = str;
                    reportSchemaError("src-element.1", objArr4, element);
                }
                if (!(i3 == 0 || qName == null)) {
                    Object[] objArr5 = new Object[i2];
                    objArr5[s] = str;
                    reportSchemaError("src-element.3", objArr5, element);
                }
                checkNotationType(str, xSTypeDefinition, element);
                if (xSElementDecl.fDefault != null) {
                    this.fValidationState.setNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                    if (XSConstraints.ElementDefaultValidImmediate(xSElementDecl.fType, xSElementDecl.fDefault.normalizedValue, this.fValidationState, xSElementDecl.fDefault) == null) {
                        Object[] objArr6 = new Object[i4];
                        objArr6[s] = str;
                        objArr6[i2] = xSElementDecl.fDefault.normalizedValue;
                        reportSchemaError("e-props-correct.2", objArr6, element);
                        xSElementDecl.fDefault = null;
                        xSElementDecl.setConstraintType(s);
                    }
                }
                if (xSElementDecl.fSubGroup != null && !XSConstraints.checkTypeDerivationOk(xSElementDecl.fType, xSElementDecl.fSubGroup.fType, xSElementDecl.fSubGroup.fFinal)) {
                    Object[] objArr7 = new Object[i4];
                    objArr7[s] = str;
                    objArr7[i2] = qName4.prefix + ":" + qName4.localpart;
                    reportSchemaError("e-props-correct.4", objArr7, element);
                    xSElementDecl.fSubGroup = null;
                }
                if (xSElementDecl.fDefault != null || ((xSTypeDefinition.getTypeCategory() != 16 || !((XSSimpleType) xSTypeDefinition).isIDType()) && (xSTypeDefinition.getTypeCategory() != 15 || !((XSComplexTypeDecl) xSTypeDefinition).containsTypeID()))) {
                    xSElementDecl2 = null;
                } else {
                    Object[] objArr8 = new Object[i2];
                    objArr8[s] = xSElementDecl.fName;
                    reportSchemaError("e-props-correct.5", objArr8, element);
                    xSElementDecl2 = null;
                    xSElementDecl.fDefault = null;
                    xSElementDecl.setConstraintType(s);
                }
                if (xSElementDecl.fName == null) {
                    return xSElementDecl2;
                }
                if (z) {
                    schemaGrammar.addGlobalElementDeclAll(xSElementDecl);
                    if (schemaGrammar.getGlobalElementDecl(xSElementDecl.fName) == null) {
                        schemaGrammar.addGlobalElementDecl(xSElementDecl);
                    }
                    String schemaDocument2SystemId = this.fSchemaHandler.schemaDocument2SystemId(xSDocumentInfo);
                    XSElementDecl globalElementDecl = schemaGrammar.getGlobalElementDecl(xSElementDecl.fName, schemaDocument2SystemId);
                    if (globalElementDecl == null) {
                        schemaGrammar.addGlobalElementDecl(xSElementDecl, schemaDocument2SystemId);
                    }
                    if (this.fSchemaHandler.fTolerateDuplicates) {
                        XSElementDecl xSElementDecl4 = globalElementDecl != null ? globalElementDecl : xSElementDecl;
                        this.fSchemaHandler.addGlobalElementDecl(xSElementDecl4);
                        return xSElementDecl4;
                    }
                }
                return xSElementDecl;
            }
            schemaGrammar2 = schemaGrammar;
            xSTypeDefinition = null;
            i3 = 0;
            if (xSTypeDefinition == null) {
            }
            qName = qName5;
            xSTypeDefinition = xSElementDecl.fSubGroup.fType;
            if (xSTypeDefinition == null) {
            }
            xSElementDecl.fType = xSTypeDefinition;
            if (firstChildElement != null) {
            }
            if (str9 == null) {
            }
            if (firstChildElement != null) {
            }
            Object[] objArr42 = new Object[i2];
            objArr42[s] = str;
            reportSchemaError("src-element.1", objArr42, element);
            Object[] objArr52 = new Object[i2];
            objArr52[s] = str;
            reportSchemaError("src-element.3", objArr52, element);
            checkNotationType(str, xSTypeDefinition, element);
            if (xSElementDecl.fDefault != null) {
            }
            Object[] objArr72 = new Object[i4];
            objArr72[s] = str;
            objArr72[i2] = qName4.prefix + ":" + qName4.localpart;
            reportSchemaError("e-props-correct.4", objArr72, element);
            xSElementDecl.fSubGroup = null;
            if (xSElementDecl.fDefault != null) {
            }
            xSElementDecl2 = null;
            if (xSElementDecl.fName == null) {
            }
        }
        if (xInt != null) {
        }
        xSElementDecl.fFinal = xInt2 != null ? xSDocumentInfo.fFinalDefault : xInt2.shortValue();
        xSElementDecl.fFinal = (short) (xSElementDecl.fFinal & 3);
        if (bool2.booleanValue()) {
        }
        xSElementDecl.setIsAbstract();
        if (str8 == null) {
        }
        if (qName4 == null) {
        }
        firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement != null) {
        }
        syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
        if (syntheticAnnotation == null) {
        }
        if (xSAnnotationImpl == null) {
        }
        xSElementDecl.fAnnotations = xSObjectListImpl;
        if (firstChildElement == null) {
        }
        xSTypeDefinition = null;
        i3 = 0;
        if (xSTypeDefinition == null) {
        }
        qName = qName5;
        xSTypeDefinition = xSElementDecl.fSubGroup.fType;
        if (xSTypeDefinition == null) {
        }
        xSElementDecl.fType = xSTypeDefinition;
        if (firstChildElement != null) {
        }
        if (str9 == null) {
        }
        if (firstChildElement != null) {
        }
        Object[] objArr422 = new Object[i2];
        objArr422[s] = str;
        reportSchemaError("src-element.1", objArr422, element);
        Object[] objArr522 = new Object[i2];
        objArr522[s] = str;
        reportSchemaError("src-element.3", objArr522, element);
        checkNotationType(str, xSTypeDefinition, element);
        if (xSElementDecl.fDefault != null) {
        }
        Object[] objArr722 = new Object[i4];
        objArr722[s] = str;
        objArr722[i2] = qName4.prefix + ":" + qName4.localpart;
        reportSchemaError("e-props-correct.4", objArr722, element);
        xSElementDecl.fSubGroup = null;
        if (xSElementDecl.fDefault != null) {
        }
        xSElementDecl2 = null;
        if (xSElementDecl.fName == null) {
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDAbstractTraverser
    public void reset(SymbolTable symbolTable, boolean z, Locale locale) {
        super.reset(symbolTable, z, locale);
        this.fDeferTraversingLocalElements = true;
    }
}
