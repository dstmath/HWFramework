package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeUseImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObject;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public class XSDAttributeTraverser extends XSDAbstractTraverser {
    public XSDAttributeTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: protected */
    public XSAttributeUseImpl traverseLocal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, XSComplexTypeDecl xSComplexTypeDecl) {
        XSAttributeDecl xSAttributeDecl;
        int i;
        XSObject xSObject;
        Object obj;
        String str;
        int i2;
        XSAttributeUseImpl xSAttributeUseImpl;
        short s;
        ValidatedInfo validatedInfo;
        short s2;
        XSObjectListImpl xSObjectListImpl;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        String str2 = (String) checkAttributes[XSAttributeChecker.ATTIDX_DEFAULT];
        String str3 = (String) checkAttributes[XSAttributeChecker.ATTIDX_FIXED];
        Object obj2 = (String) checkAttributes[XSAttributeChecker.ATTIDX_NAME];
        QName qName = (QName) checkAttributes[XSAttributeChecker.ATTIDX_REF];
        XInt xInt = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_USE];
        if (element.getAttributeNode(SchemaSymbols.ATT_REF) == null) {
            i = 1;
            obj = obj2;
            xSAttributeDecl = traverseNamedAttr(element, checkAttributes, xSDocumentInfo, schemaGrammar, false, xSComplexTypeDecl);
            xSObject = null;
        } else if (qName != null) {
            xSAttributeDecl = (XSAttributeDecl) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 1, qName, element);
            Element firstChildElement = DOMUtil.getFirstChildElement(element);
            if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
                if (syntheticAnnotation != null) {
                    i = 1;
                    xSObject = traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo);
                    firstChildElement = firstChildElement;
                } else {
                    i = 1;
                    xSObject = null;
                }
            } else {
                XSObject traverseAnnotationDecl = traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo);
                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                xSObject = traverseAnnotationDecl;
                i = 1;
            }
            if (firstChildElement != null) {
                Object[] objArr = new Object[i];
                objArr[0] = qName.rawname;
                reportSchemaError("src-attribute.3.2", objArr, firstChildElement);
            }
            obj = qName.localpart;
        } else {
            i = 1;
            obj = obj2;
            xSObject = null;
            xSAttributeDecl = null;
        }
        if (str2 != null) {
            str = str3;
            str3 = str2;
            i2 = i;
        } else if (str3 != null) {
            i2 = 2;
            str = null;
        } else {
            str = str3;
            str3 = str2;
            i2 = 0;
        }
        if (xSAttributeDecl != null) {
            if (this.fSchemaHandler.fDeclPool != null) {
                xSAttributeUseImpl = this.fSchemaHandler.fDeclPool.getAttributeUse();
            } else {
                xSAttributeUseImpl = new XSAttributeUseImpl();
            }
            xSAttributeUseImpl.fAttrDecl = xSAttributeDecl;
            xSAttributeUseImpl.fUse = xInt.shortValue();
            short s3 = i2 == 1 ? (short) 1 : 0;
            short s4 = i2 == 1 ? (short) 1 : 0;
            short s5 = i2 == 1 ? (short) 1 : 0;
            short s6 = i2 == 1 ? (short) 1 : 0;
            xSAttributeUseImpl.fConstraintType = s3;
            if (str3 != null) {
                xSAttributeUseImpl.fDefault = new ValidatedInfo();
                xSAttributeUseImpl.fDefault.normalizedValue = str3;
            }
            if (element.getAttributeNode(SchemaSymbols.ATT_REF) == null) {
                xSAttributeUseImpl.fAnnotations = xSAttributeDecl.getAnnotations();
            } else {
                if (xSObject != null) {
                    xSObjectListImpl = new XSObjectListImpl();
                    xSObjectListImpl.addXSObject(xSObject);
                } else {
                    xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
                }
                xSAttributeUseImpl.fAnnotations = xSObjectListImpl;
            }
        } else {
            xSAttributeUseImpl = null;
        }
        if (str3 == null || str == null) {
            s = 0;
        } else {
            Object[] objArr2 = new Object[i];
            s = 0;
            objArr2[0] = obj;
            reportSchemaError("src-attribute.1", objArr2, element);
        }
        if (!(i2 != i || xInt == null || xInt.intValue() == 0)) {
            Object[] objArr3 = new Object[i];
            objArr3[s] = obj;
            reportSchemaError("src-attribute.2", objArr3, element);
            xSAttributeUseImpl.fUse = s;
        }
        if (!(str3 == null || xSAttributeUseImpl == null)) {
            this.fValidationState.setNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
            try {
                checkDefaultValid(xSAttributeUseImpl);
                s2 = 0;
                validatedInfo = null;
            } catch (InvalidDatatypeValueException e) {
                reportSchemaError(e.getKey(), e.getArgs(), element);
                Object[] objArr4 = new Object[2];
                s2 = 0;
                objArr4[0] = obj;
                objArr4[i] = str3;
                reportSchemaError("a-props-correct.2", objArr4, element);
                validatedInfo = null;
                xSAttributeUseImpl.fDefault = null;
                xSAttributeUseImpl.fConstraintType = 0;
            }
            if (((XSSimpleType) xSAttributeDecl.getTypeDefinition()).isIDType()) {
                Object[] objArr5 = new Object[i];
                objArr5[s2] = obj;
                reportSchemaError("a-props-correct.3", objArr5, element);
                xSAttributeUseImpl.fDefault = validatedInfo;
                xSAttributeUseImpl.fConstraintType = s2;
            }
            if (xSAttributeUseImpl.fAttrDecl.getConstraintType() == 2 && xSAttributeUseImpl.fConstraintType != 0 && (xSAttributeUseImpl.fConstraintType != 2 || !xSAttributeUseImpl.fAttrDecl.getValInfo().actualValue.equals(xSAttributeUseImpl.fDefault.actualValue))) {
                Object[] objArr6 = new Object[2];
                objArr6[0] = obj;
                objArr6[i] = xSAttributeUseImpl.fAttrDecl.getValInfo().stringValue();
                reportSchemaError("au-props-correct.2", objArr6, element);
                xSAttributeUseImpl.fDefault = xSAttributeUseImpl.fAttrDecl.getValInfo();
                xSAttributeUseImpl.fConstraintType = 2;
            }
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return xSAttributeUseImpl;
    }

    /* access modifiers changed from: protected */
    public XSAttributeDecl traverseGlobal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, true, xSDocumentInfo);
        XSAttributeDecl traverseNamedAttr = traverseNamedAttr(element, checkAttributes, xSDocumentInfo, schemaGrammar, true, null);
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return traverseNamedAttr;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r28v0, resolved type: ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDAttributeTraverser */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v6, types: [ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl, java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r2v8 */
    /* JADX WARN: Type inference failed for: r2v13 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0133  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0137  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x013c  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0148  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0163  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0180  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x01bf  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x022b  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0232  */
    /* JADX WARNING: Unknown variable types count: 1 */
    public XSAttributeDecl traverseNamedAttr(Element element, Object[] objArr, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, boolean z, XSComplexTypeDecl xSComplexTypeDecl) {
        XSAttributeDecl xSAttributeDecl;
        XSComplexTypeDecl xSComplexTypeDecl2;
        short s;
        String str;
        short s2;
        ValidatedInfo validatedInfo;
        Element firstChildElement;
        ValidatedInfo validatedInfo2;
        char c;
        String str2;
        XSAnnotationImpl xSAnnotationImpl;
        char c2;
        XSSimpleType xSSimpleType;
        int i;
        XSObjectListImpl xSObjectListImpl;
        String str3;
        Object obj;
        ?? r2;
        ValidatedInfo validatedInfo3;
        XSTypeDefinition xSTypeDefinition;
        String syntheticAnnotation;
        XSComplexTypeDecl xSComplexTypeDecl3;
        short s3;
        String str4;
        String str5 = (String) objArr[XSAttributeChecker.ATTIDX_DEFAULT];
        String str6 = (String) objArr[XSAttributeChecker.ATTIDX_FIXED];
        XInt xInt = (XInt) objArr[XSAttributeChecker.ATTIDX_FORM];
        String str7 = (String) objArr[XSAttributeChecker.ATTIDX_NAME];
        QName qName = (QName) objArr[XSAttributeChecker.ATTIDX_TYPE];
        if (this.fSchemaHandler.fDeclPool != null) {
            xSAttributeDecl = this.fSchemaHandler.fDeclPool.getAttributeDecl();
        } else {
            xSAttributeDecl = new XSAttributeDecl();
        }
        if (str7 != null) {
            str7 = this.fSymbolTable.addSymbol(str7);
        }
        if (z) {
            str = xSDocumentInfo.fTargetNamespace;
            s = 1;
            xSComplexTypeDecl2 = null;
        } else {
            if (xSComplexTypeDecl != null) {
                xSComplexTypeDecl3 = xSComplexTypeDecl;
                s3 = 2;
            } else {
                s3 = 0;
                xSComplexTypeDecl3 = null;
            }
            if (xInt != null) {
                if (xInt.intValue() == 1) {
                    str4 = xSDocumentInfo.fTargetNamespace;
                }
                s = s3;
                xSComplexTypeDecl2 = xSComplexTypeDecl3;
                str = null;
            } else {
                if (xSDocumentInfo.fAreLocalAttributesQualified) {
                    str4 = xSDocumentInfo.fTargetNamespace;
                }
                s = s3;
                xSComplexTypeDecl2 = xSComplexTypeDecl3;
                str = null;
            }
            s = s3;
            xSComplexTypeDecl2 = xSComplexTypeDecl3;
            str = str4;
        }
        if (z) {
            if (str6 != null) {
                ValidatedInfo validatedInfo4 = new ValidatedInfo();
                validatedInfo4.normalizedValue = str6;
                validatedInfo = validatedInfo4;
                s2 = 2;
            } else if (str5 != null) {
                ValidatedInfo validatedInfo5 = new ValidatedInfo();
                validatedInfo5.normalizedValue = str5;
                validatedInfo = validatedInfo5;
                s2 = 1;
            }
            firstChildElement = DOMUtil.getFirstChildElement(element);
            if (firstChildElement != null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
                if (syntheticAnnotation == null) {
                    validatedInfo2 = validatedInfo;
                    str2 = str;
                    c = 0;
                    xSAnnotationImpl = traverseSyntheticAnnotation(element, syntheticAnnotation, objArr, false, xSDocumentInfo);
                    firstChildElement = firstChildElement;
                } else {
                    validatedInfo2 = validatedInfo;
                    str2 = str;
                    c = 0;
                    xSAnnotationImpl = null;
                }
            } else {
                xSAnnotationImpl = traverseAnnotationDecl(firstChildElement, objArr, false, xSDocumentInfo);
                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                validatedInfo2 = validatedInfo;
                str2 = str;
                c = 0;
            }
            if (firstChildElement != null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                c2 = c;
                xSSimpleType = null;
            } else {
                xSSimpleType = this.fSchemaHandler.fSimpleTypeTraverser.traverseLocal(firstChildElement, xSDocumentInfo, schemaGrammar);
                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                c2 = 1;
            }
            if (xSSimpleType == null && qName != null) {
                xSTypeDefinition = (XSTypeDefinition) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 7, qName, element);
                if (xSTypeDefinition == null && xSTypeDefinition.getTypeCategory() == 16) {
                    xSSimpleType = (XSSimpleType) xSTypeDefinition;
                } else {
                    Object[] objArr2 = new Object[2];
                    objArr2[c] = qName.rawname;
                    i = 1;
                    objArr2[1] = "simpleType definition";
                    reportSchemaError("src-resolve", objArr2, element);
                    if (xSTypeDefinition == null) {
                        xSAttributeDecl.fUnresolvedTypeName = qName;
                    }
                    if (xSSimpleType == null) {
                        xSSimpleType = SchemaGrammar.fAnySimpleType;
                    }
                    if (xSAnnotationImpl != null) {
                        xSObjectListImpl = new XSObjectListImpl();
                        xSObjectListImpl.addXSObject(xSAnnotationImpl);
                    } else {
                        xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
                    }
                    xSAttributeDecl.setValues(str7, str2, xSSimpleType, s2, s, validatedInfo2, xSComplexTypeDecl2, xSObjectListImpl);
                    if (str7 == null) {
                        if (z) {
                            Object[] objArr3 = new Object[2];
                            objArr3[c] = SchemaSymbols.ELT_ATTRIBUTE;
                            objArr3[i] = SchemaSymbols.ATT_NAME;
                            reportSchemaError("s4s-att-must-appear", objArr3, element);
                        } else {
                            reportSchemaError("src-attribute.3.1", null, element);
                        }
                        str3 = "(no name)";
                    } else {
                        str3 = str7;
                    }
                    if (firstChildElement != null) {
                        Object[] objArr4 = new Object[3];
                        objArr4[c] = str3;
                        objArr4[i] = "(annotation?, (simpleType?))";
                        objArr4[2] = DOMUtil.getLocalName(firstChildElement);
                        reportSchemaError("s4s-elt-must-match.1", objArr4, firstChildElement);
                    }
                    if (!(str5 == null || str6 == null)) {
                        Object[] objArr5 = new Object[i];
                        objArr5[c] = str3;
                        reportSchemaError("src-attribute.1", objArr5, element);
                    }
                    if (!(c2 == 0 || qName == null)) {
                        Object[] objArr6 = new Object[i];
                        objArr6[c] = str3;
                        reportSchemaError("src-attribute.4", objArr6, element);
                    }
                    checkNotationType(str3, xSSimpleType, element);
                    if (validatedInfo2 != null) {
                        this.fValidationState.setNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                        try {
                            checkDefaultValid(xSAttributeDecl);
                        } catch (InvalidDatatypeValueException e) {
                            reportSchemaError(e.getKey(), e.getArgs(), element);
                            Object[] objArr7 = new Object[2];
                            objArr7[c] = str3;
                            objArr7[i] = validatedInfo2.normalizedValue;
                            reportSchemaError("a-props-correct.2", objArr7, element);
                            validatedInfo3 = null;
                            r2 = 0;
                            obj = "(no name)";
                            xSAttributeDecl.setValues(str3, str2, xSSimpleType, 0, s, null, xSComplexTypeDecl2, xSObjectListImpl);
                        }
                    }
                    obj = "(no name)";
                    r2 = 0;
                    validatedInfo3 = validatedInfo2;
                    if (validatedInfo3 != null && xSSimpleType.isIDType()) {
                        Object[] objArr8 = new Object[i];
                        objArr8[c] = str3;
                        reportSchemaError("a-props-correct.3", objArr8, element);
                        xSAttributeDecl.setValues(str3, str2, xSSimpleType, 0, s, null, xSComplexTypeDecl2, xSObjectListImpl);
                    }
                    if (str3.equals(XMLSymbols.PREFIX_XMLNS)) {
                        reportSchemaError("no-xmlns", r2, element);
                        return r2;
                    } else if (str2 != null && str2.equals(SchemaSymbols.URI_XSI)) {
                        Object[] objArr9 = new Object[i];
                        objArr9[c] = SchemaSymbols.URI_XSI;
                        reportSchemaError("no-xsi", objArr9, element);
                        return r2;
                    } else if (str3.equals(obj)) {
                        return r2;
                    } else {
                        if (z) {
                            if (schemaGrammar.getGlobalAttributeDecl(str3) == null) {
                                schemaGrammar.addGlobalAttributeDecl(xSAttributeDecl);
                            }
                            String schemaDocument2SystemId = this.fSchemaHandler.schemaDocument2SystemId(xSDocumentInfo);
                            XSAttributeDecl globalAttributeDecl = schemaGrammar.getGlobalAttributeDecl(str3, schemaDocument2SystemId);
                            if (globalAttributeDecl == null) {
                                schemaGrammar.addGlobalAttributeDecl(xSAttributeDecl, schemaDocument2SystemId);
                            }
                            if (this.fSchemaHandler.fTolerateDuplicates) {
                                XSAttributeDecl xSAttributeDecl2 = globalAttributeDecl != null ? globalAttributeDecl : xSAttributeDecl;
                                this.fSchemaHandler.addGlobalAttributeDecl(xSAttributeDecl2);
                                return xSAttributeDecl2;
                            }
                        }
                        return xSAttributeDecl;
                    }
                }
            }
            i = 1;
            if (xSSimpleType == null) {
            }
            if (xSAnnotationImpl != null) {
            }
            xSAttributeDecl.setValues(str7, str2, xSSimpleType, s2, s, validatedInfo2, xSComplexTypeDecl2, xSObjectListImpl);
            if (str7 == null) {
            }
            if (firstChildElement != null) {
            }
            Object[] objArr52 = new Object[i];
            objArr52[c] = str3;
            reportSchemaError("src-attribute.1", objArr52, element);
            Object[] objArr62 = new Object[i];
            objArr62[c] = str3;
            reportSchemaError("src-attribute.4", objArr62, element);
            checkNotationType(str3, xSSimpleType, element);
            if (validatedInfo2 != null) {
            }
            obj = "(no name)";
            r2 = 0;
            validatedInfo3 = validatedInfo2;
            Object[] objArr82 = new Object[i];
            objArr82[c] = str3;
            reportSchemaError("a-props-correct.3", objArr82, element);
            xSAttributeDecl.setValues(str3, str2, xSSimpleType, 0, s, null, xSComplexTypeDecl2, xSObjectListImpl);
            if (str3.equals(XMLSymbols.PREFIX_XMLNS)) {
            }
        }
        s2 = 0;
        validatedInfo = null;
        firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement != null) {
        }
        syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
        if (syntheticAnnotation == null) {
        }
        if (firstChildElement != null) {
        }
        c2 = c;
        xSSimpleType = null;
        xSTypeDefinition = (XSTypeDefinition) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 7, qName, element);
        if (xSTypeDefinition == null) {
        }
        Object[] objArr22 = new Object[2];
        objArr22[c] = qName.rawname;
        i = 1;
        objArr22[1] = "simpleType definition";
        reportSchemaError("src-resolve", objArr22, element);
        if (xSTypeDefinition == null) {
        }
        if (xSSimpleType == null) {
        }
        if (xSAnnotationImpl != null) {
        }
        xSAttributeDecl.setValues(str7, str2, xSSimpleType, s2, s, validatedInfo2, xSComplexTypeDecl2, xSObjectListImpl);
        if (str7 == null) {
        }
        if (firstChildElement != null) {
        }
        Object[] objArr522 = new Object[i];
        objArr522[c] = str3;
        reportSchemaError("src-attribute.1", objArr522, element);
        Object[] objArr622 = new Object[i];
        objArr622[c] = str3;
        reportSchemaError("src-attribute.4", objArr622, element);
        checkNotationType(str3, xSSimpleType, element);
        if (validatedInfo2 != null) {
        }
        obj = "(no name)";
        r2 = 0;
        validatedInfo3 = validatedInfo2;
        Object[] objArr822 = new Object[i];
        objArr822[c] = str3;
        reportSchemaError("a-props-correct.3", objArr822, element);
        xSAttributeDecl.setValues(str3, str2, xSSimpleType, 0, s, null, xSComplexTypeDecl2, xSObjectListImpl);
        if (str3.equals(XMLSymbols.PREFIX_XMLNS)) {
        }
    }

    /* access modifiers changed from: package-private */
    public void checkDefaultValid(XSAttributeDecl xSAttributeDecl) throws InvalidDatatypeValueException {
        ((XSSimpleType) xSAttributeDecl.getTypeDefinition()).validate(xSAttributeDecl.getValInfo().normalizedValue, (ValidationContext) this.fValidationState, xSAttributeDecl.getValInfo());
        ((XSSimpleType) xSAttributeDecl.getTypeDefinition()).validate(xSAttributeDecl.getValInfo().stringValue(), (ValidationContext) this.fValidationState, xSAttributeDecl.getValInfo());
    }

    /* access modifiers changed from: package-private */
    public void checkDefaultValid(XSAttributeUseImpl xSAttributeUseImpl) throws InvalidDatatypeValueException {
        ((XSSimpleType) xSAttributeUseImpl.fAttrDecl.getTypeDefinition()).validate(xSAttributeUseImpl.fDefault.normalizedValue, (ValidationContext) this.fValidationState, xSAttributeUseImpl.fDefault);
        ((XSSimpleType) xSAttributeUseImpl.fAttrDecl.getTypeDefinition()).validate(xSAttributeUseImpl.fDefault.stringValue(), (ValidationContext) this.fValidationState, xSAttributeUseImpl.fDefault);
    }
}
