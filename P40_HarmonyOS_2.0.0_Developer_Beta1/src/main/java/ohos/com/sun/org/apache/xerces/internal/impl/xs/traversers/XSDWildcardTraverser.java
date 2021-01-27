package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSWildcardDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public class XSDWildcardTraverser extends XSDAbstractTraverser {
    XSDWildcardTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    public XSParticleDecl traverseAny(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        XSParticleDecl xSParticleDecl;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        XSWildcardDecl traverseWildcardDecl = traverseWildcardDecl(element, checkAttributes, xSDocumentInfo, schemaGrammar);
        if (traverseWildcardDecl != null) {
            int intValue = ((XInt) checkAttributes[XSAttributeChecker.ATTIDX_MINOCCURS]).intValue();
            int intValue2 = ((XInt) checkAttributes[XSAttributeChecker.ATTIDX_MAXOCCURS]).intValue();
            if (intValue2 != 0) {
                if (this.fSchemaHandler.fDeclPool != null) {
                    xSParticleDecl = this.fSchemaHandler.fDeclPool.getParticleDecl();
                } else {
                    xSParticleDecl = new XSParticleDecl();
                }
                xSParticleDecl.fType = 2;
                xSParticleDecl.fValue = traverseWildcardDecl;
                xSParticleDecl.fMinOccurs = intValue;
                xSParticleDecl.fMaxOccurs = intValue2;
                xSParticleDecl.fAnnotations = traverseWildcardDecl.fAnnotations;
                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                return xSParticleDecl;
            }
        }
        xSParticleDecl = null;
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return xSParticleDecl;
    }

    /* access modifiers changed from: package-private */
    public XSWildcardDecl traverseAnyAttribute(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        XSWildcardDecl traverseWildcardDecl = traverseWildcardDecl(element, checkAttributes, xSDocumentInfo, schemaGrammar);
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return traverseWildcardDecl;
    }

    /* access modifiers changed from: package-private */
    public XSWildcardDecl traverseWildcardDecl(Element element, Object[] objArr, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        XSObjectListImpl xSObjectListImpl;
        XSWildcardDecl xSWildcardDecl = new XSWildcardDecl();
        xSWildcardDecl.fType = ((XInt) objArr[XSAttributeChecker.ATTIDX_NAMESPACE]).shortValue();
        xSWildcardDecl.fNamespaceList = (String[]) objArr[XSAttributeChecker.ATTIDX_NAMESPACE_LIST];
        xSWildcardDecl.fProcessContents = ((XInt) objArr[XSAttributeChecker.ATTIDX_PROCESSCONTENTS]).shortValue();
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        XSAnnotationImpl xSAnnotationImpl = null;
        if (firstChildElement != null) {
            if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                xSAnnotationImpl = traverseAnnotationDecl(firstChildElement, objArr, false, xSDocumentInfo);
                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
            } else {
                String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
                if (syntheticAnnotation != null) {
                    xSAnnotationImpl = traverseSyntheticAnnotation(element, syntheticAnnotation, objArr, false, xSDocumentInfo);
                }
            }
            if (firstChildElement != null) {
                reportSchemaError("s4s-elt-must-match.1", new Object[]{"wildcard", "(annotation?)", DOMUtil.getLocalName(firstChildElement)}, element);
            }
        } else {
            String syntheticAnnotation2 = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation2 != null) {
                xSAnnotationImpl = traverseSyntheticAnnotation(element, syntheticAnnotation2, objArr, false, xSDocumentInfo);
            }
        }
        if (xSAnnotationImpl != null) {
            xSObjectListImpl = new XSObjectListImpl();
            xSObjectListImpl.addXSObject(xSAnnotationImpl);
        } else {
            xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
        }
        xSWildcardDecl.fAnnotations = xSObjectListImpl;
        return xSWildcardDecl;
    }
}
