package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.UniqueOrKey;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public class XSDUniqueOrKeyTraverser extends XSDAbstractIDConstraintTraverser {
    public XSDUniqueOrKeyTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    public void traverse(Element element, XSElementDecl xSElementDecl, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        UniqueOrKey uniqueOrKey;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        String str = (String) checkAttributes[XSAttributeChecker.ATTIDX_NAME];
        if (str == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{DOMUtil.getLocalName(element), SchemaSymbols.ATT_NAME}, element);
            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
            return;
        }
        if (DOMUtil.getLocalName(element).equals(SchemaSymbols.ELT_UNIQUE)) {
            uniqueOrKey = new UniqueOrKey(xSDocumentInfo.fTargetNamespace, str, xSElementDecl.fName, 3);
        } else {
            uniqueOrKey = new UniqueOrKey(xSDocumentInfo.fTargetNamespace, str, xSElementDecl.fName, 1);
        }
        if (traverseIdentityConstraint(uniqueOrKey, element, xSDocumentInfo, checkAttributes)) {
            if (schemaGrammar.getIDConstraintDecl(uniqueOrKey.getIdentityConstraintName()) == null) {
                schemaGrammar.addIDConstraintDecl(xSElementDecl, uniqueOrKey);
            }
            String schemaDocument2SystemId = this.fSchemaHandler.schemaDocument2SystemId(xSDocumentInfo);
            IdentityConstraint iDConstraintDecl = schemaGrammar.getIDConstraintDecl(uniqueOrKey.getIdentityConstraintName(), schemaDocument2SystemId);
            if (iDConstraintDecl == null) {
                schemaGrammar.addIDConstraintDecl(xSElementDecl, uniqueOrKey, schemaDocument2SystemId);
            }
            if (this.fSchemaHandler.fTolerateDuplicates) {
                if (iDConstraintDecl != null) {
                    boolean z = iDConstraintDecl instanceof UniqueOrKey;
                }
                this.fSchemaHandler.addIDConstraintDecl(uniqueOrKey);
            }
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
    }
}
