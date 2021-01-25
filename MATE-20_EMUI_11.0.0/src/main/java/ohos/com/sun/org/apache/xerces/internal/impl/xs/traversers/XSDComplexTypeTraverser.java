package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeFacetException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSFacets;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeGroupDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeUseImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSConstraints;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSModelGroupImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSWildcardDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDAbstractTraverser;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;

/* access modifiers changed from: package-private */
public class XSDComplexTypeTraverser extends XSDAbstractParticleTraverser {
    private static final boolean DEBUG = false;
    private static final int GLOBAL_NUM = 11;
    private static XSParticleDecl fErrorContent;
    private static XSWildcardDecl fErrorWildcard;
    private XSAnnotationImpl[] fAnnotations = null;
    private XSAttributeGroupDecl fAttrGrp = null;
    private XSTypeDefinition fBaseType = null;
    private short fBlock = 0;
    private XSComplexTypeDecl fComplexTypeDecl = null;
    private short fContentType = 0;
    private short fDerivedBy = 2;
    private short fFinal = 0;
    private Object[] fGlobalStore = null;
    private int fGlobalStorePos = 0;
    private boolean fIsAbstract = false;
    private String fName = null;
    private XSParticleDecl fParticle = null;
    private String fTargetNamespace = null;
    private XSSimpleType fXSSimpleType = null;

    private void traverseComplexContentDecl(Element element, boolean z) {
    }

    private void traverseSimpleContentDecl(Element element) {
    }

    private static XSParticleDecl getErrorContent() {
        if (fErrorContent == null) {
            XSParticleDecl xSParticleDecl = new XSParticleDecl();
            xSParticleDecl.fType = 2;
            xSParticleDecl.fValue = getErrorWildcard();
            xSParticleDecl.fMinOccurs = 0;
            xSParticleDecl.fMaxOccurs = -1;
            XSModelGroupImpl xSModelGroupImpl = new XSModelGroupImpl();
            xSModelGroupImpl.fCompositor = 102;
            xSModelGroupImpl.fParticleCount = 1;
            xSModelGroupImpl.fParticles = new XSParticleDecl[1];
            xSModelGroupImpl.fParticles[0] = xSParticleDecl;
            XSParticleDecl xSParticleDecl2 = new XSParticleDecl();
            xSParticleDecl2.fType = 3;
            xSParticleDecl2.fValue = xSModelGroupImpl;
            fErrorContent = xSParticleDecl2;
        }
        return fErrorContent;
    }

    private static XSWildcardDecl getErrorWildcard() {
        if (fErrorWildcard == null) {
            XSWildcardDecl xSWildcardDecl = new XSWildcardDecl();
            xSWildcardDecl.fProcessContents = 2;
            fErrorWildcard = xSWildcardDecl;
        }
        return fErrorWildcard;
    }

    XSDComplexTypeTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: private */
    public static final class ComplexTypeRecoverableError extends Exception {
        private static final long serialVersionUID = 6802729912091130335L;
        Element errorElem = null;
        Object[] errorSubstText = null;

        ComplexTypeRecoverableError() {
        }

        ComplexTypeRecoverableError(String str, Object[] objArr, Element element) {
            super(str);
            this.errorSubstText = objArr;
            this.errorElem = element;
        }
    }

    /* access modifiers changed from: package-private */
    public XSComplexTypeDecl traverseLocal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        String genAnonTypeName = genAnonTypeName(element);
        contentBackup();
        XSComplexTypeDecl traverseComplexTypeDecl = traverseComplexTypeDecl(element, genAnonTypeName, checkAttributes, xSDocumentInfo, schemaGrammar);
        contentRestore();
        schemaGrammar.addComplexTypeDecl(traverseComplexTypeDecl, this.fSchemaHandler.element2Locator(element));
        traverseComplexTypeDecl.setIsAnonymous();
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return traverseComplexTypeDecl;
    }

    /* access modifiers changed from: package-private */
    public XSComplexTypeDecl traverseGlobal(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, true, xSDocumentInfo);
        String str = (String) checkAttributes[XSAttributeChecker.ATTIDX_NAME];
        contentBackup();
        XSComplexTypeDecl traverseComplexTypeDecl = traverseComplexTypeDecl(element, str, checkAttributes, xSDocumentInfo, schemaGrammar);
        contentRestore();
        schemaGrammar.addComplexTypeDecl(traverseComplexTypeDecl, this.fSchemaHandler.element2Locator(element));
        if (str == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_COMPLEXTYPE, SchemaSymbols.ATT_NAME}, element);
            traverseComplexTypeDecl = null;
        } else {
            if (schemaGrammar.getGlobalTypeDecl(traverseComplexTypeDecl.getName()) == null) {
                schemaGrammar.addGlobalComplexTypeDecl(traverseComplexTypeDecl);
            }
            String schemaDocument2SystemId = this.fSchemaHandler.schemaDocument2SystemId(xSDocumentInfo);
            XSTypeDefinition globalTypeDecl = schemaGrammar.getGlobalTypeDecl(traverseComplexTypeDecl.getName(), schemaDocument2SystemId);
            if (globalTypeDecl == null) {
                schemaGrammar.addGlobalComplexTypeDecl(traverseComplexTypeDecl, schemaDocument2SystemId);
            }
            if (this.fSchemaHandler.fTolerateDuplicates) {
                if (globalTypeDecl != null && (globalTypeDecl instanceof XSComplexTypeDecl)) {
                    traverseComplexTypeDecl = (XSComplexTypeDecl) globalTypeDecl;
                }
                this.fSchemaHandler.addGlobalTypeDecl(traverseComplexTypeDecl);
            }
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return traverseComplexTypeDecl;
    }

    private XSComplexTypeDecl traverseComplexTypeDecl(Element element, String str, Object[] objArr, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        this.fComplexTypeDecl = new XSComplexTypeDecl();
        this.fAttrGrp = new XSAttributeGroupDecl();
        Boolean bool = (Boolean) objArr[XSAttributeChecker.ATTIDX_ABSTRACT];
        XInt xInt = (XInt) objArr[XSAttributeChecker.ATTIDX_BLOCK];
        Boolean bool2 = (Boolean) objArr[XSAttributeChecker.ATTIDX_MIXED];
        XInt xInt2 = (XInt) objArr[XSAttributeChecker.ATTIDX_FINAL];
        this.fName = str;
        this.fComplexTypeDecl.setName(this.fName);
        this.fTargetNamespace = xSDocumentInfo.fTargetNamespace;
        this.fBlock = xInt == null ? xSDocumentInfo.fBlockDefault : xInt.shortValue();
        this.fFinal = xInt2 == null ? xSDocumentInfo.fFinalDefault : xInt2.shortValue();
        this.fBlock = (short) (this.fBlock & 3);
        this.fFinal = (short) (this.fFinal & 3);
        int i = 0;
        this.fIsAbstract = bool != null && bool.booleanValue();
        this.fAnnotations = null;
        try {
            Element firstChildElement = DOMUtil.getFirstChildElement(element);
            if (firstChildElement != null) {
                if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    addAnnotation(traverseAnnotationDecl(firstChildElement, objArr, false, xSDocumentInfo));
                    firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                } else {
                    String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
                    if (syntheticAnnotation != null) {
                        addAnnotation(traverseSyntheticAnnotation(element, syntheticAnnotation, objArr, false, xSDocumentInfo));
                    }
                }
                if (firstChildElement != null && DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, SchemaSymbols.ELT_ANNOTATION}, firstChildElement);
                }
            } else {
                String syntheticAnnotation2 = DOMUtil.getSyntheticAnnotation(element);
                if (syntheticAnnotation2 != null) {
                    addAnnotation(traverseSyntheticAnnotation(element, syntheticAnnotation2, objArr, false, xSDocumentInfo));
                }
            }
            if (firstChildElement == null) {
                this.fBaseType = SchemaGrammar.fAnyType;
                this.fDerivedBy = 2;
                processComplexContent(firstChildElement, bool2.booleanValue(), false, xSDocumentInfo, schemaGrammar);
            } else if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_SIMPLECONTENT)) {
                traverseSimpleContent(firstChildElement, xSDocumentInfo, schemaGrammar);
                Element nextSiblingElement = DOMUtil.getNextSiblingElement(firstChildElement);
                if (nextSiblingElement != null) {
                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(nextSiblingElement)}, nextSiblingElement);
                }
            } else if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_COMPLEXCONTENT)) {
                traverseComplexContent(firstChildElement, bool2.booleanValue(), xSDocumentInfo, schemaGrammar);
                Element nextSiblingElement2 = DOMUtil.getNextSiblingElement(firstChildElement);
                if (nextSiblingElement2 != null) {
                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(nextSiblingElement2)}, nextSiblingElement2);
                }
            } else {
                this.fBaseType = SchemaGrammar.fAnyType;
                this.fDerivedBy = 2;
                processComplexContent(firstChildElement, bool2.booleanValue(), false, xSDocumentInfo, schemaGrammar);
            }
        } catch (ComplexTypeRecoverableError e) {
            handleComplexTypeError(e.getMessage(), e.errorSubstText, e.errorElem);
        }
        XSComplexTypeDecl xSComplexTypeDecl = this.fComplexTypeDecl;
        String str2 = this.fName;
        String str3 = this.fTargetNamespace;
        XSTypeDefinition xSTypeDefinition = this.fBaseType;
        short s = this.fDerivedBy;
        short s2 = this.fFinal;
        short s3 = this.fBlock;
        short s4 = this.fContentType;
        boolean z = this.fIsAbstract;
        XSAttributeGroupDecl xSAttributeGroupDecl = this.fAttrGrp;
        XSSimpleType xSSimpleType = this.fXSSimpleType;
        XSParticleDecl xSParticleDecl = this.fParticle;
        XSAnnotationImpl[] xSAnnotationImplArr = this.fAnnotations;
        if (xSAnnotationImplArr != null) {
            i = xSAnnotationImplArr.length;
        }
        xSComplexTypeDecl.setValues(str2, str3, xSTypeDefinition, s, s2, s3, s4, z, xSAttributeGroupDecl, xSSimpleType, xSParticleDecl, new XSObjectListImpl(xSAnnotationImplArr, i));
        return this.fComplexTypeDecl;
    }

    private void traverseSimpleContent(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) throws ComplexTypeRecoverableError {
        Node node;
        XSComplexTypeDecl xSComplexTypeDecl;
        XSSimpleType xSSimpleType;
        short s;
        Element element2;
        XSSimpleType xSSimpleType2;
        XSSimpleType xSSimpleType3;
        short s2;
        XSFacets xSFacets;
        Element element3;
        short s3;
        Element element4;
        Element element5;
        Element element6;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        this.fContentType = 1;
        this.fParticle = null;
        Node firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
            String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation != null) {
                addAnnotation(traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo));
            }
            node = firstChildElement;
        } else {
            addAnnotation(traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo));
            node = DOMUtil.getNextSiblingElement(firstChildElement);
        }
        if (node != null) {
            String localName = DOMUtil.getLocalName(node);
            if (localName.equals(SchemaSymbols.ELT_RESTRICTION)) {
                this.fDerivedBy = 2;
            } else if (localName.equals(SchemaSymbols.ELT_EXTENSION)) {
                this.fDerivedBy = 1;
            } else {
                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, localName}, node);
            }
            Element nextSiblingElement = DOMUtil.getNextSiblingElement(node);
            if (nextSiblingElement == null) {
                Object[] checkAttributes2 = this.fAttrChecker.checkAttributes(node, false, xSDocumentInfo);
                QName qName = (QName) checkAttributes2[XSAttributeChecker.ATTIDX_BASE];
                if (qName != null) {
                    XSTypeDefinition xSTypeDefinition = (XSTypeDefinition) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 7, qName, node);
                    if (xSTypeDefinition != null) {
                        this.fBaseType = xSTypeDefinition;
                        if (xSTypeDefinition.getTypeCategory() == 15) {
                            XSComplexTypeDecl xSComplexTypeDecl2 = (XSComplexTypeDecl) xSTypeDefinition;
                            s = xSComplexTypeDecl2.getFinal();
                            if (xSComplexTypeDecl2.getContentType() == 1) {
                                xSComplexTypeDecl = xSComplexTypeDecl2;
                                xSSimpleType = (XSSimpleType) xSComplexTypeDecl2.getSimpleType();
                            } else if (this.fDerivedBy == 2 && xSComplexTypeDecl2.getContentType() == 3 && ((XSParticleDecl) xSComplexTypeDecl2.getParticle()).emptiable()) {
                                xSSimpleType = null;
                                xSComplexTypeDecl = xSComplexTypeDecl2;
                            } else {
                                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                throw new ComplexTypeRecoverableError("src-ct.2.1", new Object[]{this.fName, xSComplexTypeDecl2.getName()}, node);
                            }
                        } else {
                            XSSimpleType xSSimpleType4 = (XSSimpleType) xSTypeDefinition;
                            if (this.fDerivedBy != 2) {
                                s = xSSimpleType4.getFinal();
                                xSComplexTypeDecl = null;
                                xSSimpleType = xSSimpleType4;
                            } else {
                                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                throw new ComplexTypeRecoverableError("src-ct.2.1", new Object[]{this.fName, xSSimpleType4.getName()}, node);
                            }
                        }
                        if ((this.fDerivedBy & s) != 0) {
                            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                            this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                            throw new ComplexTypeRecoverableError(this.fDerivedBy == 1 ? "cos-ct-extends.1.1" : "derivation-ok-restriction.1", new Object[]{this.fName, this.fBaseType.getName()}, node);
                        }
                        Element firstChildElement2 = DOMUtil.getFirstChildElement(node);
                        if (firstChildElement2 != null) {
                            if (DOMUtil.getLocalName(firstChildElement2).equals(SchemaSymbols.ELT_ANNOTATION)) {
                                addAnnotation(traverseAnnotationDecl(firstChildElement2, checkAttributes2, false, xSDocumentInfo));
                                element5 = DOMUtil.getNextSiblingElement(firstChildElement2);
                                xSSimpleType2 = xSSimpleType;
                            } else {
                                String syntheticAnnotation2 = DOMUtil.getSyntheticAnnotation(node);
                                if (syntheticAnnotation2 != null) {
                                    element6 = firstChildElement2;
                                    xSSimpleType2 = xSSimpleType;
                                    addAnnotation(traverseSyntheticAnnotation(node, syntheticAnnotation2, checkAttributes2, false, xSDocumentInfo));
                                } else {
                                    element6 = firstChildElement2;
                                    xSSimpleType2 = xSSimpleType;
                                }
                                element5 = element6;
                            }
                            if (element5 == null || !DOMUtil.getLocalName(element5).equals(SchemaSymbols.ELT_ANNOTATION)) {
                                element2 = element5;
                            } else {
                                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, SchemaSymbols.ELT_ANNOTATION}, element5);
                            }
                        } else {
                            xSSimpleType2 = xSSimpleType;
                            String syntheticAnnotation3 = DOMUtil.getSyntheticAnnotation(node);
                            if (syntheticAnnotation3 != null) {
                                addAnnotation(traverseSyntheticAnnotation(node, syntheticAnnotation3, checkAttributes2, false, xSDocumentInfo));
                            }
                            element2 = firstChildElement2;
                        }
                        if (this.fDerivedBy == 2) {
                            if (element2 == null || !DOMUtil.getLocalName(element2).equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                                xSSimpleType3 = xSSimpleType2;
                            } else {
                                xSSimpleType3 = this.fSchemaHandler.fSimpleTypeTraverser.traverseLocal(element2, xSDocumentInfo, schemaGrammar);
                                if (xSSimpleType3 == null) {
                                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                    this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                    throw new ComplexTypeRecoverableError();
                                } else if (xSSimpleType2 == null || XSConstraints.checkSimpleDerivationOk(xSSimpleType3, xSSimpleType2, xSSimpleType2.getFinal())) {
                                    element2 = DOMUtil.getNextSiblingElement(element2);
                                } else {
                                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                    this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                    throw new ComplexTypeRecoverableError("derivation-ok-restriction.5.2.2.1", new Object[]{this.fName, xSSimpleType3.getName(), xSSimpleType2.getName()}, element2);
                                }
                            }
                            if (xSSimpleType3 != null) {
                                if (element2 != null) {
                                    XSDAbstractTraverser.FacetInfo traverseFacets = traverseFacets(element2, xSSimpleType3, xSDocumentInfo);
                                    element3 = traverseFacets.nodeAfterFacets;
                                    xSFacets = traverseFacets.facetdata;
                                    s2 = traverseFacets.fPresentFacets;
                                    s3 = traverseFacets.fFixedFacets;
                                } else {
                                    s3 = 0;
                                    s2 = 0;
                                    element3 = null;
                                    xSFacets = null;
                                }
                                String genAnonTypeName = genAnonTypeName(element);
                                this.fXSSimpleType = this.fSchemaHandler.fDVFactory.createTypeRestriction(genAnonTypeName, xSDocumentInfo.fTargetNamespace, 0, xSSimpleType3, null);
                                try {
                                    this.fValidationState.setNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                                    this.fXSSimpleType.applyFacets(xSFacets, s2, s3, this.fValidationState);
                                } catch (InvalidDatatypeFacetException e) {
                                    reportSchemaError(e.getKey(), e.getArgs(), element2);
                                    this.fXSSimpleType = this.fSchemaHandler.fDVFactory.createTypeRestriction(genAnonTypeName, xSDocumentInfo.fTargetNamespace, 0, xSSimpleType3, null);
                                }
                                XSSimpleType xSSimpleType5 = this.fXSSimpleType;
                                if (xSSimpleType5 instanceof XSSimpleTypeDecl) {
                                    ((XSSimpleTypeDecl) xSSimpleType5).setAnonymous(true);
                                }
                                if (element3 == null) {
                                    element4 = element3;
                                } else if (isAttrOrAttrGroup(element3)) {
                                    element4 = element3;
                                    Element traverseAttrsAndAttrGrps = traverseAttrsAndAttrGrps(element3, this.fAttrGrp, xSDocumentInfo, schemaGrammar, this.fComplexTypeDecl);
                                    if (traverseAttrsAndAttrGrps != null) {
                                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                        this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                        throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(traverseAttrsAndAttrGrps)}, traverseAttrsAndAttrGrps);
                                    }
                                } else {
                                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                    this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(element3)}, element3);
                                }
                                try {
                                    mergeAttributes(xSComplexTypeDecl.getAttrGrp(), this.fAttrGrp, this.fName, false, element);
                                    this.fAttrGrp.removeProhibitedAttrs();
                                    Object[] validRestrictionOf = this.fAttrGrp.validRestrictionOf(this.fName, xSComplexTypeDecl.getAttrGrp());
                                    if (validRestrictionOf != null) {
                                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                        this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                        throw new ComplexTypeRecoverableError((String) validRestrictionOf[validRestrictionOf.length - 1], validRestrictionOf, element4);
                                    }
                                } catch (ComplexTypeRecoverableError e2) {
                                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                    this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                    throw e2;
                                }
                            } else {
                                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                throw new ComplexTypeRecoverableError("src-ct.2.2", new Object[]{this.fName}, element2);
                            }
                        } else {
                            this.fXSSimpleType = xSSimpleType2;
                            if (element2 != null) {
                                if (isAttrOrAttrGroup(element2)) {
                                    Element traverseAttrsAndAttrGrps2 = traverseAttrsAndAttrGrps(element2, this.fAttrGrp, xSDocumentInfo, schemaGrammar, this.fComplexTypeDecl);
                                    if (traverseAttrsAndAttrGrps2 == null) {
                                        this.fAttrGrp.removeProhibitedAttrs();
                                    } else {
                                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                        this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                        throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(traverseAttrsAndAttrGrps2)}, traverseAttrsAndAttrGrps2);
                                    }
                                } else {
                                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                    this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(element2)}, element2);
                                }
                            }
                            if (xSComplexTypeDecl != null) {
                                try {
                                    mergeAttributes(xSComplexTypeDecl.getAttrGrp(), this.fAttrGrp, this.fName, true, element);
                                } catch (ComplexTypeRecoverableError e3) {
                                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                    this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                                    throw e3;
                                }
                            }
                        }
                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                        this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                        return;
                    }
                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                    this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                    throw new ComplexTypeRecoverableError();
                }
                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                throw new ComplexTypeRecoverableError("s4s-att-must-appear", new Object[]{localName, "base"}, node);
            }
            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
            throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(nextSiblingElement)}, nextSiblingElement);
        }
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.2", new Object[]{this.fName, SchemaSymbols.ELT_SIMPLECONTENT}, element);
    }

    private void traverseComplexContent(Element element, boolean z, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) throws ComplexTypeRecoverableError {
        Object[] objArr;
        Object[] validRestrictionOf;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        Boolean bool = (Boolean) checkAttributes[XSAttributeChecker.ATTIDX_MIXED];
        boolean booleanValue = bool != null ? bool.booleanValue() : z;
        this.fXSSimpleType = null;
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
            String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation != null) {
                addAnnotation(traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo));
            }
        } else {
            addAnnotation(traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo));
            firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
        }
        if (firstChildElement != null) {
            String localName = DOMUtil.getLocalName(firstChildElement);
            if (localName.equals(SchemaSymbols.ELT_RESTRICTION)) {
                this.fDerivedBy = 2;
            } else if (localName.equals(SchemaSymbols.ELT_EXTENSION)) {
                this.fDerivedBy = 1;
            } else {
                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, localName}, firstChildElement);
            }
            Element nextSiblingElement = DOMUtil.getNextSiblingElement(firstChildElement);
            if (nextSiblingElement == null) {
                Object[] checkAttributes2 = this.fAttrChecker.checkAttributes(firstChildElement, false, xSDocumentInfo);
                QName qName = (QName) checkAttributes2[XSAttributeChecker.ATTIDX_BASE];
                if (qName != null) {
                    XSTypeDefinition xSTypeDefinition = (XSTypeDefinition) this.fSchemaHandler.getGlobalDecl(xSDocumentInfo, 7, qName, firstChildElement);
                    if (xSTypeDefinition == null) {
                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                        this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                        throw new ComplexTypeRecoverableError();
                    } else if (xSTypeDefinition instanceof XSComplexTypeDecl) {
                        XSComplexTypeDecl xSComplexTypeDecl = (XSComplexTypeDecl) xSTypeDefinition;
                        this.fBaseType = xSComplexTypeDecl;
                        if ((xSComplexTypeDecl.getFinal() & this.fDerivedBy) != 0) {
                            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                            this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                            throw new ComplexTypeRecoverableError(this.fDerivedBy == 1 ? "cos-ct-extends.1.1" : "derivation-ok-restriction.1", new Object[]{this.fName, this.fBaseType.getName()}, firstChildElement);
                        }
                        Element firstChildElement2 = DOMUtil.getFirstChildElement(firstChildElement);
                        if (firstChildElement2 != null) {
                            if (DOMUtil.getLocalName(firstChildElement2).equals(SchemaSymbols.ELT_ANNOTATION)) {
                                addAnnotation(traverseAnnotationDecl(firstChildElement2, checkAttributes2, false, xSDocumentInfo));
                                firstChildElement2 = DOMUtil.getNextSiblingElement(firstChildElement2);
                            } else {
                                String syntheticAnnotation2 = DOMUtil.getSyntheticAnnotation(firstChildElement2);
                                if (syntheticAnnotation2 != null) {
                                    objArr = checkAttributes2;
                                    addAnnotation(traverseSyntheticAnnotation(firstChildElement2, syntheticAnnotation2, checkAttributes2, false, xSDocumentInfo));
                                    if (firstChildElement2 != null && DOMUtil.getLocalName(firstChildElement2).equals(SchemaSymbols.ELT_ANNOTATION)) {
                                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                        this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                                        throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, SchemaSymbols.ELT_ANNOTATION}, firstChildElement2);
                                    }
                                }
                            }
                            objArr = checkAttributes2;
                            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                            this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                            throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, SchemaSymbols.ELT_ANNOTATION}, firstChildElement2);
                        }
                        objArr = checkAttributes2;
                        String syntheticAnnotation3 = DOMUtil.getSyntheticAnnotation(firstChildElement2);
                        if (syntheticAnnotation3 != null) {
                            addAnnotation(traverseSyntheticAnnotation(firstChildElement2, syntheticAnnotation3, objArr, false, xSDocumentInfo));
                        }
                        try {
                            processComplexContent(firstChildElement2, booleanValue, true, xSDocumentInfo, schemaGrammar);
                            XSParticleDecl xSParticleDecl = (XSParticleDecl) xSComplexTypeDecl.getParticle();
                            if (this.fDerivedBy != 2) {
                                if (this.fParticle == null) {
                                    this.fContentType = xSComplexTypeDecl.getContentType();
                                    this.fXSSimpleType = (XSSimpleType) xSComplexTypeDecl.getSimpleType();
                                    this.fParticle = xSParticleDecl;
                                } else if (xSComplexTypeDecl.getContentType() != 0) {
                                    if (this.fContentType == 2 && xSComplexTypeDecl.getContentType() != 2) {
                                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                        this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                                        throw new ComplexTypeRecoverableError("cos-ct-extends.1.4.3.2.2.1.a", new Object[]{this.fName}, firstChildElement2);
                                    } else if (this.fContentType == 3 && xSComplexTypeDecl.getContentType() != 3) {
                                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                        this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                                        throw new ComplexTypeRecoverableError("cos-ct-extends.1.4.3.2.2.1.b", new Object[]{this.fName}, firstChildElement2);
                                    } else if ((this.fParticle.fType == 3 && ((XSModelGroupImpl) this.fParticle.fValue).fCompositor == 103) || (((XSParticleDecl) xSComplexTypeDecl.getParticle()).fType == 3 && ((XSModelGroupImpl) ((XSParticleDecl) xSComplexTypeDecl.getParticle()).fValue).fCompositor == 103)) {
                                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                        this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                                        throw new ComplexTypeRecoverableError("cos-all-limited.1.2", new Object[0], firstChildElement2);
                                    } else {
                                        XSModelGroupImpl xSModelGroupImpl = new XSModelGroupImpl();
                                        xSModelGroupImpl.fCompositor = 102;
                                        xSModelGroupImpl.fParticleCount = 2;
                                        xSModelGroupImpl.fParticles = new XSParticleDecl[2];
                                        xSModelGroupImpl.fParticles[0] = (XSParticleDecl) xSComplexTypeDecl.getParticle();
                                        xSModelGroupImpl.fParticles[1] = this.fParticle;
                                        xSModelGroupImpl.fAnnotations = XSObjectListImpl.EMPTY_LIST;
                                        XSParticleDecl xSParticleDecl2 = new XSParticleDecl();
                                        xSParticleDecl2.fType = 3;
                                        xSParticleDecl2.fValue = xSModelGroupImpl;
                                        xSParticleDecl2.fAnnotations = XSObjectListImpl.EMPTY_LIST;
                                        this.fParticle = xSParticleDecl2;
                                    }
                                }
                                this.fAttrGrp.removeProhibitedAttrs();
                                try {
                                    mergeAttributes(xSComplexTypeDecl.getAttrGrp(), this.fAttrGrp, this.fName, true, firstChildElement2);
                                } catch (ComplexTypeRecoverableError e) {
                                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                    this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                                    throw e;
                                }
                            } else if (this.fContentType != 3 || xSComplexTypeDecl.getContentType() == 3) {
                                try {
                                    mergeAttributes(xSComplexTypeDecl.getAttrGrp(), this.fAttrGrp, this.fName, false, firstChildElement2);
                                    this.fAttrGrp.removeProhibitedAttrs();
                                    if (!(xSComplexTypeDecl == SchemaGrammar.fAnyType || (validRestrictionOf = this.fAttrGrp.validRestrictionOf(this.fName, xSComplexTypeDecl.getAttrGrp())) == null)) {
                                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                        this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                                        throw new ComplexTypeRecoverableError((String) validRestrictionOf[validRestrictionOf.length - 1], validRestrictionOf, firstChildElement2);
                                    }
                                } catch (ComplexTypeRecoverableError e2) {
                                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                    this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                                    throw e2;
                                }
                            } else {
                                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                                this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                                throw new ComplexTypeRecoverableError("derivation-ok-restriction.5.4.1.2", new Object[]{this.fName, xSComplexTypeDecl.getName()}, firstChildElement2);
                            }
                            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                            this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                        } catch (ComplexTypeRecoverableError e3) {
                            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                            this.fAttrChecker.returnAttrArray(objArr, xSDocumentInfo);
                            throw e3;
                        }
                    } else {
                        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                        this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                        throw new ComplexTypeRecoverableError("src-ct.1", new Object[]{this.fName, xSTypeDefinition.getName()}, firstChildElement);
                    }
                } else {
                    this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                    this.fAttrChecker.returnAttrArray(checkAttributes2, xSDocumentInfo);
                    throw new ComplexTypeRecoverableError("s4s-att-must-appear", new Object[]{localName, "base"}, firstChildElement);
                }
            } else {
                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(nextSiblingElement)}, nextSiblingElement);
            }
        } else {
            this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
            throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.2", new Object[]{this.fName, SchemaSymbols.ELT_COMPLEXCONTENT}, element);
        }
    }

    private void mergeAttributes(XSAttributeGroupDecl xSAttributeGroupDecl, XSAttributeGroupDecl xSAttributeGroupDecl2, String str, boolean z, Element element) throws ComplexTypeRecoverableError {
        XSObjectList attributeUses = xSAttributeGroupDecl.getAttributeUses();
        int length = attributeUses.getLength();
        for (int i = 0; i < length; i++) {
            XSAttributeUseImpl xSAttributeUseImpl = (XSAttributeUseImpl) attributeUses.item(i);
            XSAttributeUse attributeUse = xSAttributeGroupDecl2.getAttributeUse(xSAttributeUseImpl.fAttrDecl.getNamespace(), xSAttributeUseImpl.fAttrDecl.getName());
            if (attributeUse == null) {
                String addAttributeUse = xSAttributeGroupDecl2.addAttributeUse(xSAttributeUseImpl);
                if (addAttributeUse != null) {
                    throw new ComplexTypeRecoverableError("ct-props-correct.5", new Object[]{str, addAttributeUse, xSAttributeUseImpl.fAttrDecl.getName()}, element);
                }
            } else if (attributeUse != xSAttributeUseImpl && z) {
                reportSchemaError("ct-props-correct.4", new Object[]{str, xSAttributeUseImpl.fAttrDecl.getName()}, element);
                xSAttributeGroupDecl2.replaceAttributeUse(attributeUse, xSAttributeUseImpl);
            }
        }
        if (!z) {
            return;
        }
        if (xSAttributeGroupDecl2.fAttributeWC == null) {
            xSAttributeGroupDecl2.fAttributeWC = xSAttributeGroupDecl.fAttributeWC;
        } else if (xSAttributeGroupDecl.fAttributeWC != null) {
            xSAttributeGroupDecl2.fAttributeWC = xSAttributeGroupDecl2.fAttributeWC.performUnionWith(xSAttributeGroupDecl.fAttributeWC, xSAttributeGroupDecl2.fAttributeWC.fProcessContents);
            if (xSAttributeGroupDecl2.fAttributeWC == null) {
                throw new ComplexTypeRecoverableError("src-ct.5", new Object[]{str}, element);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00b1  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00d9  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00e6  */
    /* JADX WARNING: Removed duplicated region for block: B:65:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private void processComplexContent(Element element, boolean z, boolean z2, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) throws ComplexTypeRecoverableError {
        Element element2;
        boolean z3;
        XSParticleDecl xSParticleDecl;
        if (element != null) {
            String localName = DOMUtil.getLocalName(element);
            if (localName.equals(SchemaSymbols.ELT_GROUP)) {
                xSParticleDecl = this.fSchemaHandler.fGroupTraverser.traverseLocal(element, xSDocumentInfo, schemaGrammar);
                element2 = DOMUtil.getNextSiblingElement(element);
            } else {
                if (localName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                    xSParticleDecl = traverseSequence(element, xSDocumentInfo, schemaGrammar, 0, this.fComplexTypeDecl);
                    z3 = xSParticleDecl != null && ((XSModelGroupImpl) xSParticleDecl.fValue).fParticleCount == 0;
                    element2 = DOMUtil.getNextSiblingElement(element);
                } else if (localName.equals(SchemaSymbols.ELT_CHOICE)) {
                    xSParticleDecl = traverseChoice(element, xSDocumentInfo, schemaGrammar, 0, this.fComplexTypeDecl);
                    z3 = xSParticleDecl != null && xSParticleDecl.fMinOccurs == 0 && ((XSModelGroupImpl) xSParticleDecl.fValue).fParticleCount == 0;
                    element2 = DOMUtil.getNextSiblingElement(element);
                } else if (localName.equals(SchemaSymbols.ELT_ALL)) {
                    xSParticleDecl = traverseAll(element, xSDocumentInfo, schemaGrammar, 8, this.fComplexTypeDecl);
                    z3 = xSParticleDecl != null && ((XSModelGroupImpl) xSParticleDecl.fValue).fParticleCount == 0;
                    element2 = DOMUtil.getNextSiblingElement(element);
                } else {
                    element2 = element;
                    xSParticleDecl = null;
                }
                if (z3) {
                    Element firstChildElement = DOMUtil.getFirstChildElement(element);
                    if (firstChildElement != null && DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                    }
                    if (firstChildElement == null) {
                        xSParticleDecl = null;
                    }
                }
                if (xSParticleDecl == null && z) {
                    xSParticleDecl = XSConstraints.getEmptySequence();
                }
                this.fParticle = xSParticleDecl;
                if (this.fParticle != null) {
                    this.fContentType = 0;
                } else if (z) {
                    this.fContentType = 3;
                } else {
                    this.fContentType = 2;
                }
                if (element2 != null) {
                    return;
                }
                if (isAttrOrAttrGroup(element2)) {
                    Element traverseAttrsAndAttrGrps = traverseAttrsAndAttrGrps(element2, this.fAttrGrp, xSDocumentInfo, schemaGrammar, this.fComplexTypeDecl);
                    if (traverseAttrsAndAttrGrps != null) {
                        throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(traverseAttrsAndAttrGrps)}, traverseAttrsAndAttrGrps);
                    } else if (!z2) {
                        this.fAttrGrp.removeProhibitedAttrs();
                        return;
                    } else {
                        return;
                    }
                } else {
                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1", new Object[]{this.fName, DOMUtil.getLocalName(element2)}, element2);
                }
            }
        } else {
            xSParticleDecl = null;
            element2 = null;
        }
        z3 = false;
        if (z3) {
        }
        xSParticleDecl = XSConstraints.getEmptySequence();
        this.fParticle = xSParticleDecl;
        if (this.fParticle != null) {
        }
        if (element2 != null) {
        }
    }

    private boolean isAttrOrAttrGroup(Element element) {
        String localName = DOMUtil.getLocalName(element);
        return localName.equals(SchemaSymbols.ELT_ATTRIBUTE) || localName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP) || localName.equals(SchemaSymbols.ELT_ANYATTRIBUTE);
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

    private void handleComplexTypeError(String str, Object[] objArr, Element element) {
        if (str != null) {
            reportSchemaError(str, objArr, element);
        }
        this.fBaseType = SchemaGrammar.fAnyType;
        this.fContentType = 3;
        this.fXSSimpleType = null;
        this.fParticle = getErrorContent();
        this.fAttrGrp.fAttributeWC = getErrorWildcard();
    }

    private void contentBackup() {
        if (this.fGlobalStore == null) {
            this.fGlobalStore = new Object[11];
            this.fGlobalStorePos = 0;
        }
        int i = this.fGlobalStorePos;
        Object[] objArr = this.fGlobalStore;
        if (i == objArr.length) {
            Object[] objArr2 = new Object[(i + 11)];
            System.arraycopy(objArr, 0, objArr2, 0, i);
            this.fGlobalStore = objArr2;
        }
        Object[] objArr3 = this.fGlobalStore;
        int i2 = this.fGlobalStorePos;
        this.fGlobalStorePos = i2 + 1;
        objArr3[i2] = this.fComplexTypeDecl;
        int i3 = this.fGlobalStorePos;
        this.fGlobalStorePos = i3 + 1;
        objArr3[i3] = this.fIsAbstract ? Boolean.TRUE : Boolean.FALSE;
        Object[] objArr4 = this.fGlobalStore;
        int i4 = this.fGlobalStorePos;
        this.fGlobalStorePos = i4 + 1;
        objArr4[i4] = this.fName;
        int i5 = this.fGlobalStorePos;
        this.fGlobalStorePos = i5 + 1;
        objArr4[i5] = this.fTargetNamespace;
        int i6 = this.fGlobalStorePos;
        this.fGlobalStorePos = i6 + 1;
        objArr4[i6] = new Integer((this.fDerivedBy << 16) + this.fFinal);
        Object[] objArr5 = this.fGlobalStore;
        int i7 = this.fGlobalStorePos;
        this.fGlobalStorePos = i7 + 1;
        objArr5[i7] = new Integer((this.fBlock << 16) + this.fContentType);
        Object[] objArr6 = this.fGlobalStore;
        int i8 = this.fGlobalStorePos;
        this.fGlobalStorePos = i8 + 1;
        objArr6[i8] = this.fBaseType;
        int i9 = this.fGlobalStorePos;
        this.fGlobalStorePos = i9 + 1;
        objArr6[i9] = this.fAttrGrp;
        int i10 = this.fGlobalStorePos;
        this.fGlobalStorePos = i10 + 1;
        objArr6[i10] = this.fParticle;
        int i11 = this.fGlobalStorePos;
        this.fGlobalStorePos = i11 + 1;
        objArr6[i11] = this.fXSSimpleType;
        int i12 = this.fGlobalStorePos;
        this.fGlobalStorePos = i12 + 1;
        objArr6[i12] = this.fAnnotations;
    }

    private void contentRestore() {
        Object[] objArr = this.fGlobalStore;
        int i = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i;
        this.fAnnotations = (XSAnnotationImpl[]) objArr[i];
        int i2 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i2;
        this.fXSSimpleType = (XSSimpleType) objArr[i2];
        int i3 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i3;
        this.fParticle = (XSParticleDecl) objArr[i3];
        int i4 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i4;
        this.fAttrGrp = (XSAttributeGroupDecl) objArr[i4];
        int i5 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i5;
        this.fBaseType = (XSTypeDefinition) objArr[i5];
        int i6 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i6;
        int intValue = ((Integer) objArr[i6]).intValue();
        this.fBlock = (short) (intValue >> 16);
        this.fContentType = (short) intValue;
        Object[] objArr2 = this.fGlobalStore;
        int i7 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i7;
        int intValue2 = ((Integer) objArr2[i7]).intValue();
        this.fDerivedBy = (short) (intValue2 >> 16);
        this.fFinal = (short) intValue2;
        Object[] objArr3 = this.fGlobalStore;
        int i8 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i8;
        this.fTargetNamespace = (String) objArr3[i8];
        int i9 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i9;
        this.fName = (String) objArr3[i9];
        int i10 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i10;
        this.fIsAbstract = ((Boolean) objArr3[i10]).booleanValue();
        Object[] objArr4 = this.fGlobalStore;
        int i11 = this.fGlobalStorePos - 1;
        this.fGlobalStorePos = i11;
        this.fComplexTypeDecl = (XSComplexTypeDecl) objArr4[i11];
    }

    private void addAnnotation(XSAnnotationImpl xSAnnotationImpl) {
        if (xSAnnotationImpl != null) {
            XSAnnotationImpl[] xSAnnotationImplArr = this.fAnnotations;
            if (xSAnnotationImplArr == null) {
                this.fAnnotations = new XSAnnotationImpl[1];
            } else {
                XSAnnotationImpl[] xSAnnotationImplArr2 = new XSAnnotationImpl[(xSAnnotationImplArr.length + 1)];
                System.arraycopy(xSAnnotationImplArr, 0, xSAnnotationImplArr2, 0, xSAnnotationImplArr.length);
                this.fAnnotations = xSAnnotationImplArr2;
            }
            XSAnnotationImpl[] xSAnnotationImplArr3 = this.fAnnotations;
            xSAnnotationImplArr3[xSAnnotationImplArr3.length - 1] = xSAnnotationImpl;
        }
    }
}
