package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.KeyRef;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.UniqueOrKey;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public class XSDKeyrefTraverser extends XSDAbstractIDConstraintTraverser {
    public XSDKeyrefTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    public void traverse(Element element, XSElementDecl xSElementDecl, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        String str = (String) checkAttributes[XSAttributeChecker.ATTIDX_NAME];
        if (str == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_KEYREF, SchemaSymbols.ATT_NAME}, element);
            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
            return;
        }
        QName qName = (QName) checkAttributes[XSAttributeChecker.ATTIDX_REFER];
        if (qName == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_KEYREF, SchemaSymbols.ATT_REFER}, element);
            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
            return;
        }
        UniqueOrKey uniqueOrKey = null;
        IdentityConstraint identityConstraint = (IdentityConstraint) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 5, qName, element);
        if (identityConstraint != null) {
            if (identityConstraint.getCategory() == 1 || identityConstraint.getCategory() == 3) {
                uniqueOrKey = (UniqueOrKey) identityConstraint;
            } else {
                reportSchemaError("src-resolve", new Object[]{qName.rawname, "identity constraint key/unique"}, element);
            }
        }
        if (uniqueOrKey == null) {
            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
            return;
        }
        KeyRef keyRef = new KeyRef(xSDocumentInfo.fTargetNamespace, str, xSElementDecl.fName, uniqueOrKey);
        if (traverseIdentityConstraint(keyRef, element, xSDocumentInfo, checkAttributes)) {
            if (uniqueOrKey.getFieldCount() != keyRef.getFieldCount()) {
                reportSchemaError("c-props-correct.2", new Object[]{str, uniqueOrKey.getIdentityConstraintName()}, element);
            } else {
                if (schemaGrammar.getIDConstraintDecl(keyRef.getIdentityConstraintName()) == null) {
                    schemaGrammar.addIDConstraintDecl(xSElementDecl, keyRef);
                }
                String schemaDocument2SystemId = this.fSchemaHandler.schemaDocument2SystemId(xSDocumentInfo);
                IdentityConstraint iDConstraintDecl = schemaGrammar.getIDConstraintDecl(keyRef.getIdentityConstraintName(), schemaDocument2SystemId);
                if (iDConstraintDecl == null) {
                    schemaGrammar.addIDConstraintDecl(xSElementDecl, keyRef, schemaDocument2SystemId);
                }
                if (this.fSchemaHandler.fTolerateDuplicates) {
                    if (iDConstraintDecl != null && (iDConstraintDecl instanceof KeyRef)) {
                        keyRef = (KeyRef) iDConstraintDecl;
                    }
                    this.fSchemaHandler.addIDConstraintDecl(keyRef);
                }
            }
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
    }
}
