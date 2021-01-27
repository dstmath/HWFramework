package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSConstraints;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSGroupDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSModelGroupImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public class XSDGroupTraverser extends XSDAbstractParticleTraverser {
    XSDGroupTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00dd  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00fd  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0101  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0114  */
    public XSParticleDecl traverseLocal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        XSGroupDecl xSGroupDecl;
        XSGroupDecl xSGroupDecl2;
        Element element2;
        char c;
        XSAnnotationImpl xSAnnotationImpl;
        int intValue;
        int intValue2;
        XSParticleDecl xSParticleDecl;
        XSAnnotationImpl xSAnnotationImpl2;
        XSParticleDecl xSParticleDecl2;
        XSObjectListImpl xSObjectListImpl;
        XSAnnotationImpl xSAnnotationImpl3;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        QName qName = (QName) checkAttributes[XSAttributeChecker.ATTIDX_REF];
        XInt xInt = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MINOCCURS];
        XInt xInt2 = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MAXOCCURS];
        XSParticleDecl xSParticleDecl3 = null;
        if (qName == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{"group (local)", "ref"}, element);
            xSGroupDecl = null;
        } else {
            xSGroupDecl = (XSGroupDecl) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 4, qName, element);
        }
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
            String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation != null) {
                element2 = firstChildElement;
                xSGroupDecl2 = xSGroupDecl;
                c = 2;
                xSAnnotationImpl3 = traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo);
            } else {
                element2 = firstChildElement;
                xSGroupDecl2 = xSGroupDecl;
                c = 2;
                xSAnnotationImpl = null;
                if (element2 != null) {
                    Object[] objArr = new Object[3];
                    objArr[0] = "group (local)";
                    objArr[1] = "(annotation?)";
                    objArr[c] = DOMUtil.getLocalName(element);
                    reportSchemaError("s4s-elt-must-match.1", objArr, element);
                }
                intValue = xInt.intValue();
                intValue2 = xInt2.intValue();
                if (!(xSGroupDecl2 == null || xSGroupDecl2.fModelGroup == null || (intValue == 0 && intValue2 == 0))) {
                    if (this.fSchemaHandler.fDeclPool == null) {
                        xSParticleDecl = this.fSchemaHandler.fDeclPool.getParticleDecl();
                    } else {
                        xSParticleDecl = new XSParticleDecl();
                    }
                    xSParticleDecl.fType = 3;
                    xSParticleDecl.fValue = xSGroupDecl2.fModelGroup;
                    xSParticleDecl.fMinOccurs = intValue;
                    xSParticleDecl.fMaxOccurs = intValue2;
                    if (xSGroupDecl2.fModelGroup.fCompositor != 103) {
                        xSAnnotationImpl2 = xSAnnotationImpl;
                        xSParticleDecl2 = checkOccurrences(xSParticleDecl, SchemaSymbols.ELT_GROUP, (Element) element.getParentNode(), 2, ((Long) checkAttributes[XSAttributeChecker.ATTIDX_FROMDEFAULT]).longValue());
                    } else {
                        xSAnnotationImpl2 = xSAnnotationImpl;
                        xSParticleDecl2 = xSParticleDecl;
                    }
                    if (qName == null) {
                        if (xSAnnotationImpl2 != null) {
                            xSObjectListImpl = new XSObjectListImpl();
                            xSObjectListImpl.addXSObject(xSAnnotationImpl2);
                        } else {
                            xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
                        }
                        xSParticleDecl2.fAnnotations = xSObjectListImpl;
                    } else {
                        xSParticleDecl2.fAnnotations = xSGroupDecl2.fAnnotations;
                    }
                    xSParticleDecl3 = xSParticleDecl2;
                }
                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                return xSParticleDecl3;
            }
        } else {
            xSAnnotationImpl3 = traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo);
            element2 = DOMUtil.getNextSiblingElement(firstChildElement);
            xSGroupDecl2 = xSGroupDecl;
            c = 2;
        }
        xSAnnotationImpl = xSAnnotationImpl3;
        if (element2 != null) {
        }
        intValue = xInt.intValue();
        intValue2 = xInt2.intValue();
        if (this.fSchemaHandler.fDeclPool == null) {
        }
        xSParticleDecl.fType = 3;
        xSParticleDecl.fValue = xSGroupDecl2.fModelGroup;
        xSParticleDecl.fMinOccurs = intValue;
        xSParticleDecl.fMaxOccurs = intValue2;
        if (xSGroupDecl2.fModelGroup.fCompositor != 103) {
        }
        if (qName == null) {
        }
        xSParticleDecl3 = xSParticleDecl2;
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return xSParticleDecl3;
    }

    /* access modifiers changed from: package-private */
    public XSGroupDecl traverseGlobal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        XSGroupDecl xSGroupDecl;
        XSAnnotationImpl xSAnnotationImpl;
        XSParticleDecl xSParticleDecl;
        Object grpOrAttrGrpRedefinedByRestriction;
        XSObjectListImpl xSObjectListImpl;
        String str;
        XSAnnotationImpl xSAnnotationImpl2;
        Element element2;
        int i;
        String str2;
        Element element3;
        String str3;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, true, xSDocumentInfo);
        String str4 = (String) checkAttributes[XSAttributeChecker.ATTIDX_NAME];
        if (str4 == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{"group (global)", "name"}, element);
        }
        XSGroupDecl xSGroupDecl2 = new XSGroupDecl();
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        XSGroupDecl xSGroupDecl3 = null;
        if (firstChildElement == null) {
            reportSchemaError("s4s-elt-must-match.2", new Object[]{"group (global)", "(annotation?, (all | choice | sequence))"}, element);
            xSGroupDecl = xSGroupDecl2;
            xSParticleDecl = null;
            xSAnnotationImpl = null;
        } else {
            String localName = firstChildElement.getLocalName();
            if (localName.equals(SchemaSymbols.ELT_ANNOTATION)) {
                XSAnnotationImpl traverseAnnotationDecl = traverseAnnotationDecl(firstChildElement, checkAttributes, true, xSDocumentInfo);
                Element nextSiblingElement = DOMUtil.getNextSiblingElement(firstChildElement);
                if (nextSiblingElement != null) {
                    localName = nextSiblingElement.getLocalName();
                }
                xSAnnotationImpl2 = traverseAnnotationDecl;
                str = "s4s-elt-must-match.2";
                xSGroupDecl = xSGroupDecl2;
                element2 = nextSiblingElement;
            } else {
                String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
                if (syntheticAnnotation != null) {
                    str = "s4s-elt-must-match.2";
                    xSGroupDecl = xSGroupDecl2;
                    element2 = firstChildElement;
                    localName = localName;
                    xSAnnotationImpl2 = traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo);
                } else {
                    str = "s4s-elt-must-match.2";
                    xSGroupDecl = xSGroupDecl2;
                    element2 = firstChildElement;
                    xSAnnotationImpl2 = null;
                }
            }
            if (element2 == null) {
                reportSchemaError(str, new Object[]{"group (global)", "(annotation?, (all | choice | sequence))"}, element);
                str2 = "s4s-elt-must-match.1";
                i = 3;
                element3 = element2;
            } else {
                if (localName.equals(SchemaSymbols.ELT_ALL)) {
                    str3 = "s4s-elt-must-match.1";
                    i = 3;
                    xSParticleDecl = traverseAll(element2, xSDocumentInfo, schemaGrammar, 4, xSGroupDecl);
                    element3 = element2;
                } else {
                    str3 = "s4s-elt-must-match.1";
                    i = 3;
                    if (localName.equals(SchemaSymbols.ELT_CHOICE)) {
                        element3 = element2;
                        xSParticleDecl = traverseChoice(element3, xSDocumentInfo, schemaGrammar, 4, xSGroupDecl);
                    } else if (localName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                        element3 = element2;
                        xSParticleDecl = traverseSequence(element3, xSDocumentInfo, schemaGrammar, 4, xSGroupDecl);
                    } else {
                        element3 = element2;
                        str2 = str3;
                        reportSchemaError(str2, new Object[]{"group (global)", "(annotation?, (all | choice | sequence))", DOMUtil.getLocalName(element2)}, element3);
                    }
                }
                str2 = str3;
                if (!(element3 == null || DOMUtil.getNextSiblingElement(element3) == null)) {
                    Object[] objArr = new Object[i];
                    objArr[0] = "group (global)";
                    objArr[1] = "(annotation?, (all | choice | sequence))";
                    objArr[2] = DOMUtil.getLocalName(DOMUtil.getNextSiblingElement(element3));
                    reportSchemaError(str2, objArr, DOMUtil.getNextSiblingElement(element3));
                }
                xSAnnotationImpl = xSAnnotationImpl2;
            }
            xSParticleDecl = null;
            Object[] objArr2 = new Object[i];
            objArr2[0] = "group (global)";
            objArr2[1] = "(annotation?, (all | choice | sequence))";
            objArr2[2] = DOMUtil.getLocalName(DOMUtil.getNextSiblingElement(element3));
            reportSchemaError(str2, objArr2, DOMUtil.getNextSiblingElement(element3));
            xSAnnotationImpl = xSAnnotationImpl2;
        }
        if (str4 != null) {
            xSGroupDecl.fName = str4;
            xSGroupDecl.fTargetNamespace = xSDocumentInfo.fTargetNamespace;
            if (xSParticleDecl == null) {
                xSParticleDecl = XSConstraints.getEmptySequence();
            }
            xSGroupDecl.fModelGroup = (XSModelGroupImpl) xSParticleDecl.fValue;
            if (xSAnnotationImpl != null) {
                xSObjectListImpl = new XSObjectListImpl();
                xSObjectListImpl.addXSObject(xSAnnotationImpl);
            } else {
                xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
            }
            xSGroupDecl.fAnnotations = xSObjectListImpl;
            if (schemaGrammar.getGlobalGroupDecl(xSGroupDecl.fName) == null) {
                schemaGrammar.addGlobalGroupDecl(xSGroupDecl);
            }
            String schemaDocument2SystemId = this.fSchemaHandler.schemaDocument2SystemId(xSDocumentInfo);
            XSGroupDecl globalGroupDecl = schemaGrammar.getGlobalGroupDecl(xSGroupDecl.fName, schemaDocument2SystemId);
            if (globalGroupDecl == null) {
                schemaGrammar.addGlobalGroupDecl(xSGroupDecl, schemaDocument2SystemId);
            }
            if (this.fSchemaHandler.fTolerateDuplicates) {
                XSGroupDecl xSGroupDecl4 = globalGroupDecl != null ? globalGroupDecl : xSGroupDecl;
                this.fSchemaHandler.addGlobalGroupDecl(xSGroupDecl4);
                xSGroupDecl3 = xSGroupDecl4;
            } else {
                xSGroupDecl3 = xSGroupDecl;
            }
        }
        if (!(xSGroupDecl3 == null || (grpOrAttrGrpRedefinedByRestriction = this.fSchemaHandler.getGrpOrAttrGrpRedefinedByRestriction(4, new QName(XMLSymbols.EMPTY_STRING, str4, str4, xSDocumentInfo.fTargetNamespace), xSDocumentInfo, element)) == null)) {
            schemaGrammar.addRedefinedGroupDecl(xSGroupDecl3, (XSGroupDecl) grpOrAttrGrpRedefinedByRestriction, this.fSchemaHandler.element2Locator(element));
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return xSGroupDecl3;
    }
}
