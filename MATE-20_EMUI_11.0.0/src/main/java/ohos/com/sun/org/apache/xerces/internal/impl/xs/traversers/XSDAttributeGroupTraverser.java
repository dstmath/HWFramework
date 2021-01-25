package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeGroupDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public class XSDAttributeGroupTraverser extends XSDAbstractTraverser {
    XSDAttributeGroupTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    public XSAttributeGroupDecl traverseLocal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        QName qName = (QName) checkAttributes[XSAttributeChecker.ATTIDX_REF];
        if (qName == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{"attributeGroup (local)", "ref"}, element);
            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
            return null;
        }
        XSAttributeGroupDecl xSAttributeGroupDecl = (XSAttributeGroupDecl) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 2, qName, element);
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement != null) {
            if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo);
                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
            } else {
                String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(firstChildElement);
                if (syntheticAnnotation != null) {
                    traverseSyntheticAnnotation(firstChildElement, syntheticAnnotation, checkAttributes, false, xSDocumentInfo);
                }
            }
            if (firstChildElement != null) {
                reportSchemaError("s4s-elt-must-match.1", new Object[]{qName.rawname, "(annotation?)", DOMUtil.getLocalName(firstChildElement)}, firstChildElement);
            }
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return xSAttributeGroupDecl;
    }

    /* access modifiers changed from: package-private */
    public XSAttributeGroupDecl traverseGlobal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        String str;
        String str2;
        Element element2;
        XSAnnotationImpl xSAnnotationImpl;
        XSObjectListImpl xSObjectListImpl;
        Object[] validRestrictionOf;
        Element element3;
        XSAttributeGroupDecl xSAttributeGroupDecl = new XSAttributeGroupDecl();
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, true, xSDocumentInfo);
        String str3 = (String) checkAttributes[XSAttributeChecker.ATTIDX_NAME];
        if (str3 == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{"attributeGroup (global)", "name"}, element);
            str = "(no name)";
        } else {
            str = str3;
        }
        xSAttributeGroupDecl.fName = str;
        xSAttributeGroupDecl.fTargetNamespace = xSDocumentInfo.fTargetNamespace;
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
            String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation != null) {
                element3 = firstChildElement;
                str2 = str;
                xSAnnotationImpl = traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo);
            } else {
                element3 = firstChildElement;
                str2 = str;
                xSAnnotationImpl = null;
            }
            element2 = element3;
        } else {
            XSAnnotationImpl traverseAnnotationDecl = traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo);
            Element nextSiblingElement = DOMUtil.getNextSiblingElement(firstChildElement);
            xSAnnotationImpl = traverseAnnotationDecl;
            str2 = str;
            element2 = nextSiblingElement;
        }
        Element traverseAttrsAndAttrGrps = traverseAttrsAndAttrGrps(element2, xSAttributeGroupDecl, xSDocumentInfo, schemaGrammar, null);
        if (traverseAttrsAndAttrGrps != null) {
            reportSchemaError("s4s-elt-must-match.1", new Object[]{str2, "(annotation?, ((attribute | attributeGroup)*, anyAttribute?))", DOMUtil.getLocalName(traverseAttrsAndAttrGrps)}, traverseAttrsAndAttrGrps);
        }
        if (str2.equals("(no name)")) {
            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
            return null;
        }
        xSAttributeGroupDecl.removeProhibitedAttrs();
        XSAttributeGroupDecl xSAttributeGroupDecl2 = (XSAttributeGroupDecl) this.fSchemaHandler.getGrpOrAttrGrpRedefinedByRestriction(2, new QName(XMLSymbols.EMPTY_STRING, str2, str2, xSDocumentInfo.fTargetNamespace), xSDocumentInfo, element);
        if (!(xSAttributeGroupDecl2 == null || (validRestrictionOf = xSAttributeGroupDecl.validRestrictionOf(str2, xSAttributeGroupDecl2)) == null)) {
            reportSchemaError((String) validRestrictionOf[validRestrictionOf.length - 1], validRestrictionOf, element2);
            reportSchemaError("src-redefine.7.2.2", new Object[]{str2, validRestrictionOf[validRestrictionOf.length - 1]}, element2);
        }
        if (xSAnnotationImpl != null) {
            xSObjectListImpl = new XSObjectListImpl();
            xSObjectListImpl.addXSObject(xSAnnotationImpl);
        } else {
            xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
        }
        xSAttributeGroupDecl.fAnnotations = xSObjectListImpl;
        if (schemaGrammar.getGlobalAttributeGroupDecl(xSAttributeGroupDecl.fName) == null) {
            schemaGrammar.addGlobalAttributeGroupDecl(xSAttributeGroupDecl);
        }
        String schemaDocument2SystemId = this.fSchemaHandler.schemaDocument2SystemId(xSDocumentInfo);
        XSAttributeGroupDecl globalAttributeGroupDecl = schemaGrammar.getGlobalAttributeGroupDecl(xSAttributeGroupDecl.fName, schemaDocument2SystemId);
        if (globalAttributeGroupDecl == null) {
            schemaGrammar.addGlobalAttributeGroupDecl(xSAttributeGroupDecl, schemaDocument2SystemId);
        }
        if (this.fSchemaHandler.fTolerateDuplicates) {
            if (globalAttributeGroupDecl != null) {
                xSAttributeGroupDecl = globalAttributeGroupDecl;
            }
            this.fSchemaHandler.addGlobalAttributeGroupDecl(xSAttributeGroupDecl);
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return xSAttributeGroupDecl;
    }
}
