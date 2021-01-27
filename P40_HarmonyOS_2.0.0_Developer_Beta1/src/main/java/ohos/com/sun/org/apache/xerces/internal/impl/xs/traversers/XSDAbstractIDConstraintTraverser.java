package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.xpath.XPathException;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.Field;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.Selector;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.org.w3c.dom.Element;

class XSDAbstractIDConstraintTraverser extends XSDAbstractTraverser {
    public XSDAbstractIDConstraintTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r20v0, resolved type: ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDAbstractIDConstraintTraverser */
    /* JADX DEBUG: Multi-variable search result rejected for r1v4, resolved type: ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSAttributeChecker */
    /* JADX DEBUG: Multi-variable search result rejected for r1v14, resolved type: ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSAttributeChecker */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r14v1, types: [boolean] */
    /* JADX WARN: Type inference failed for: r14v3 */
    /* JADX WARN: Type inference failed for: r14v4 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00fd  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x010c  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x01d8  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x01c4 A[SYNTHETIC] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public boolean traverseIdentityConstraint(IdentityConstraint identityConstraint, Element element, XSDocumentInfo xSDocumentInfo, Object[] objArr) {
        ?? r14;
        Object[] objArr2;
        String str;
        String str2;
        Object[] objArr3;
        String str3;
        String str4;
        char c;
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement == null) {
            reportSchemaError("s4s-elt-must-match.2", new Object[]{"identity constraint", "(annotation?, selector, field+)"}, element);
            return false;
        }
        if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
            identityConstraint.addAnnotation(traverseAnnotationDecl(firstChildElement, objArr, false, xSDocumentInfo));
            firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
            if (firstChildElement == null) {
                reportSchemaError("s4s-elt-must-match.2", new Object[]{"identity constraint", "(annotation?, selector, field+)"}, element);
                return false;
            }
        } else {
            String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation != null) {
                r14 = 0;
                identityConstraint.addAnnotation(traverseSyntheticAnnotation(element, syntheticAnnotation, objArr, false, xSDocumentInfo));
                if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_SELECTOR)) {
                    Object[] objArr4 = new Object[3];
                    objArr4[r14] = "identity constraint";
                    objArr4[1] = "(annotation?, selector, field+)";
                    objArr4[2] = SchemaSymbols.ELT_SELECTOR;
                    reportSchemaError("s4s-elt-must-match.1", objArr4, firstChildElement);
                    return r14;
                }
                Object[] checkAttributes = this.fAttrChecker.checkAttributes(firstChildElement, r14, xSDocumentInfo);
                Element firstChildElement2 = DOMUtil.getFirstChildElement(firstChildElement);
                if (firstChildElement2 != null) {
                    if (DOMUtil.getLocalName(firstChildElement2).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        identityConstraint.addAnnotation(traverseAnnotationDecl(firstChildElement2, checkAttributes, r14, xSDocumentInfo));
                        firstChildElement2 = DOMUtil.getNextSiblingElement(firstChildElement2);
                        c = 2;
                    } else {
                        Object[] objArr5 = new Object[3];
                        objArr5[r14] = SchemaSymbols.ELT_SELECTOR;
                        objArr5[1] = "(annotation?)";
                        c = 2;
                        objArr5[2] = DOMUtil.getLocalName(firstChildElement2);
                        reportSchemaError("s4s-elt-must-match.1", objArr5, firstChildElement2);
                    }
                    if (firstChildElement2 != null) {
                        Object[] objArr6 = new Object[3];
                        objArr6[r14] = SchemaSymbols.ELT_SELECTOR;
                        objArr6[1] = "(annotation?)";
                        objArr6[c] = DOMUtil.getLocalName(firstChildElement2);
                        reportSchemaError("s4s-elt-must-match.1", objArr6, firstChildElement2);
                    }
                } else {
                    String syntheticAnnotation2 = DOMUtil.getSyntheticAnnotation(firstChildElement);
                    if (syntheticAnnotation2 != null) {
                        objArr2 = checkAttributes;
                        str = "s4s-elt-must-match.1";
                        identityConstraint.addAnnotation(traverseSyntheticAnnotation(element, syntheticAnnotation2, checkAttributes, false, xSDocumentInfo));
                        str2 = (String) objArr2[XSAttributeChecker.ATTIDX_XPATH];
                        String str5 = "s4s-att-must-appear";
                        if (str2 != null) {
                            Object[] objArr7 = new Object[2];
                            objArr7[r14] = SchemaSymbols.ELT_SELECTOR;
                            objArr7[1] = SchemaSymbols.ATT_XPATH;
                            reportSchemaError(str5, objArr7, firstChildElement);
                            return r14;
                        }
                        String trim = XMLChar.trim(str2);
                        try {
                            identityConstraint.setSelector(new Selector(new Selector.XPath(trim, this.fSymbolTable, xSDocumentInfo.fNamespaceSupport), identityConstraint));
                            this.fAttrChecker.returnAttrArray(objArr2, xSDocumentInfo);
                            Element nextSiblingElement = DOMUtil.getNextSiblingElement(firstChildElement);
                            if (nextSiblingElement == null) {
                                Object[] objArr8 = new Object[2];
                                objArr8[r14] = "identity constraint";
                                objArr8[1] = "(annotation?, selector, field+)";
                                reportSchemaError("s4s-elt-must-match.2", objArr8, firstChildElement);
                                return r14;
                            }
                            Element element2 = nextSiblingElement;
                            while (element2 != null) {
                                if (!DOMUtil.getLocalName(element2).equals(SchemaSymbols.ELT_FIELD)) {
                                    Object[] objArr9 = new Object[3];
                                    objArr9[r14] = "identity constraint";
                                    objArr9[1] = "(annotation?, selector, field+)";
                                    objArr9[2] = SchemaSymbols.ELT_FIELD;
                                    reportSchemaError(str, objArr9, element2);
                                    element2 = DOMUtil.getNextSiblingElement(element2);
                                } else {
                                    Object[] checkAttributes2 = this.fAttrChecker.checkAttributes(element2, r14, xSDocumentInfo);
                                    Element firstChildElement3 = DOMUtil.getFirstChildElement(element2);
                                    if (firstChildElement3 != null && DOMUtil.getLocalName(firstChildElement3).equals(SchemaSymbols.ELT_ANNOTATION)) {
                                        identityConstraint.addAnnotation(traverseAnnotationDecl(firstChildElement3, checkAttributes2, r14, xSDocumentInfo));
                                        firstChildElement3 = DOMUtil.getNextSiblingElement(firstChildElement3);
                                    }
                                    if (firstChildElement3 != null) {
                                        Object[] objArr10 = new Object[3];
                                        objArr10[r14] = SchemaSymbols.ELT_FIELD;
                                        objArr10[1] = "(annotation?)";
                                        objArr10[2] = DOMUtil.getLocalName(firstChildElement3);
                                        reportSchemaError(str, objArr10, firstChildElement3);
                                    } else {
                                        String syntheticAnnotation3 = DOMUtil.getSyntheticAnnotation(element2);
                                        if (syntheticAnnotation3 != null) {
                                            objArr3 = checkAttributes2;
                                            str = str;
                                            str3 = str5;
                                            identityConstraint.addAnnotation(traverseSyntheticAnnotation(element, syntheticAnnotation3, checkAttributes2, false, xSDocumentInfo));
                                            str4 = (String) objArr3[XSAttributeChecker.ATTIDX_XPATH];
                                            if (str4 != null) {
                                                Object[] objArr11 = new Object[2];
                                                objArr11[r14] = SchemaSymbols.ELT_FIELD;
                                                objArr11[1] = SchemaSymbols.ATT_XPATH;
                                                reportSchemaError(str3, objArr11, element2);
                                                this.fAttrChecker.returnAttrArray(objArr3, xSDocumentInfo);
                                                return r14;
                                            }
                                            String trim2 = XMLChar.trim(str4);
                                            try {
                                                identityConstraint.addField(new Field(new Field.XPath(trim2, this.fSymbolTable, xSDocumentInfo.fNamespaceSupport), identityConstraint));
                                                element2 = DOMUtil.getNextSiblingElement(element2);
                                                this.fAttrChecker.returnAttrArray(objArr3, xSDocumentInfo);
                                                str5 = str3;
                                            } catch (XPathException e) {
                                                String key = e.getKey();
                                                Object[] objArr12 = new Object[1];
                                                objArr12[r14] = trim2;
                                                reportSchemaError(key, objArr12, element2);
                                                this.fAttrChecker.returnAttrArray(objArr3, xSDocumentInfo);
                                                return r14;
                                            }
                                        }
                                    }
                                    objArr3 = checkAttributes2;
                                    str = str;
                                    str3 = str5;
                                    str4 = (String) objArr3[XSAttributeChecker.ATTIDX_XPATH];
                                    if (str4 != null) {
                                    }
                                }
                            }
                            if (identityConstraint.getFieldCount() > 0) {
                                return true;
                            }
                            return r14;
                        } catch (XPathException e2) {
                            String key2 = e2.getKey();
                            Object[] objArr13 = new Object[1];
                            char c2 = r14 == true ? 1 : 0;
                            char c3 = r14 == true ? 1 : 0;
                            char c4 = r14 == true ? 1 : 0;
                            objArr13[c2] = trim;
                            reportSchemaError(key2, objArr13, firstChildElement);
                            this.fAttrChecker.returnAttrArray(objArr2, xSDocumentInfo);
                            return r14;
                        }
                    }
                }
                objArr2 = checkAttributes;
                str = "s4s-elt-must-match.1";
                str2 = (String) objArr2[XSAttributeChecker.ATTIDX_XPATH];
                String str52 = "s4s-att-must-appear";
                if (str2 != null) {
                }
            }
        }
        r14 = 0;
        if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_SELECTOR)) {
        }
    }
}
