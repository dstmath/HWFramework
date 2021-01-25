package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSNotationDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public class XSDNotationTraverser extends XSDAbstractTraverser {
    XSDNotationTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    public XSNotationDecl traverse(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        XSObjectListImpl xSObjectListImpl;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, true, xSDocumentInfo);
        String str = (String) checkAttributes[XSAttributeChecker.ATTIDX_NAME];
        String str2 = (String) checkAttributes[XSAttributeChecker.ATTIDX_PUBLIC];
        String str3 = (String) checkAttributes[XSAttributeChecker.ATTIDX_SYSTEM];
        XSAnnotationImpl xSAnnotationImpl = null;
        if (str == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_NOTATION, SchemaSymbols.ATT_NAME}, element);
            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
            return null;
        }
        if (str3 == null && str2 == null) {
            reportSchemaError("PublicSystemOnNotation", null, element);
            str2 = "missing";
        }
        XSNotationDecl xSNotationDecl = new XSNotationDecl();
        xSNotationDecl.fName = str;
        xSNotationDecl.fTargetNamespace = xSDocumentInfo.fTargetNamespace;
        xSNotationDecl.fPublicId = str2;
        xSNotationDecl.fSystemId = str3;
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
            String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation != null) {
                xSAnnotationImpl = traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo);
            }
        } else {
            xSAnnotationImpl = traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo);
            firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
        }
        if (xSAnnotationImpl != null) {
            xSObjectListImpl = new XSObjectListImpl();
            xSObjectListImpl.addXSObject(xSAnnotationImpl);
        } else {
            xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
        }
        xSNotationDecl.fAnnotations = xSObjectListImpl;
        if (firstChildElement != null) {
            reportSchemaError("s4s-elt-must-match.1", new Object[]{SchemaSymbols.ELT_NOTATION, "(annotation?)", DOMUtil.getLocalName(firstChildElement)}, firstChildElement);
        }
        if (schemaGrammar.getGlobalNotationDecl(xSNotationDecl.fName) == null) {
            schemaGrammar.addGlobalNotationDecl(xSNotationDecl);
        }
        String schemaDocument2SystemId = this.fSchemaHandler.schemaDocument2SystemId(xSDocumentInfo);
        XSNotationDecl globalNotationDecl = schemaGrammar.getGlobalNotationDecl(xSNotationDecl.fName, schemaDocument2SystemId);
        if (globalNotationDecl == null) {
            schemaGrammar.addGlobalNotationDecl(xSNotationDecl, schemaDocument2SystemId);
        }
        if (this.fSchemaHandler.fTolerateDuplicates) {
            if (globalNotationDecl != null) {
                xSNotationDecl = globalNotationDecl;
            }
            this.fSchemaHandler.addGlobalNotationDecl(xSNotationDecl);
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return xSNotationDecl;
    }
}
