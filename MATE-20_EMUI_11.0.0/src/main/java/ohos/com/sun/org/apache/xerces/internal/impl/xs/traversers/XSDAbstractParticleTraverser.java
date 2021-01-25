package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSModelGroupImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObject;
import ohos.org.w3c.dom.Element;

/* access modifiers changed from: package-private */
public abstract class XSDAbstractParticleTraverser extends XSDAbstractTraverser {
    ParticleArray fPArray = new ParticleArray();

    XSDAbstractParticleTraverser(XSDHandler xSDHandler, XSAttributeChecker xSAttributeChecker) {
        super(xSDHandler, xSAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b1  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00bd  */
    public XSParticleDecl traverseAll(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, int i, XSObject xSObject) {
        XSAnnotationImpl xSAnnotationImpl;
        XSObjectListImpl xSObjectListImpl;
        XSParticleDecl xSParticleDecl;
        XSAnnotationImpl xSAnnotationImpl2;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
            String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation != null) {
                xSAnnotationImpl2 = traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo);
            } else {
                xSAnnotationImpl = null;
                this.fPArray.pushContext();
                while (firstChildElement != null) {
                    if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ELEMENT)) {
                        xSParticleDecl = this.fSchemaHandler.fElementTraverser.traverseLocal(firstChildElement, xSDocumentInfo, schemaGrammar, 1, xSObject);
                    } else {
                        reportSchemaError("s4s-elt-must-match.1", new Object[]{"all", "(annotation?, element*)", DOMUtil.getLocalName(firstChildElement)}, firstChildElement);
                        xSParticleDecl = null;
                    }
                    if (xSParticleDecl != null) {
                        this.fPArray.addParticle(xSParticleDecl);
                    }
                    firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                }
                XInt xInt = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MINOCCURS];
                XInt xInt2 = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MAXOCCURS];
                Long l = (Long) checkAttributes[XSAttributeChecker.ATTIDX_FROMDEFAULT];
                XSModelGroupImpl xSModelGroupImpl = new XSModelGroupImpl();
                xSModelGroupImpl.fCompositor = 103;
                xSModelGroupImpl.fParticleCount = this.fPArray.getParticleCount();
                xSModelGroupImpl.fParticles = this.fPArray.popContext();
                if (xSAnnotationImpl == null) {
                    xSObjectListImpl = new XSObjectListImpl();
                    xSObjectListImpl.addXSObject(xSAnnotationImpl);
                } else {
                    xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
                }
                xSModelGroupImpl.fAnnotations = xSObjectListImpl;
                XSParticleDecl xSParticleDecl2 = new XSParticleDecl();
                xSParticleDecl2.fType = 3;
                xSParticleDecl2.fMinOccurs = xInt.intValue();
                xSParticleDecl2.fMaxOccurs = xInt2.intValue();
                xSParticleDecl2.fValue = xSModelGroupImpl;
                xSParticleDecl2.fAnnotations = xSObjectListImpl;
                XSParticleDecl checkOccurrences = checkOccurrences(xSParticleDecl2, SchemaSymbols.ELT_ALL, (Element) element.getParentNode(), i, l.longValue());
                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                return checkOccurrences;
            }
        } else {
            xSAnnotationImpl2 = traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo);
            firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
        }
        xSAnnotationImpl = xSAnnotationImpl2;
        this.fPArray.pushContext();
        while (firstChildElement != null) {
        }
        XInt xInt3 = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MINOCCURS];
        XInt xInt22 = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MAXOCCURS];
        Long l2 = (Long) checkAttributes[XSAttributeChecker.ATTIDX_FROMDEFAULT];
        XSModelGroupImpl xSModelGroupImpl2 = new XSModelGroupImpl();
        xSModelGroupImpl2.fCompositor = 103;
        xSModelGroupImpl2.fParticleCount = this.fPArray.getParticleCount();
        xSModelGroupImpl2.fParticles = this.fPArray.popContext();
        if (xSAnnotationImpl == null) {
        }
        xSModelGroupImpl2.fAnnotations = xSObjectListImpl;
        XSParticleDecl xSParticleDecl22 = new XSParticleDecl();
        xSParticleDecl22.fType = 3;
        xSParticleDecl22.fMinOccurs = xInt3.intValue();
        xSParticleDecl22.fMaxOccurs = xInt22.intValue();
        xSParticleDecl22.fValue = xSModelGroupImpl2;
        xSParticleDecl22.fAnnotations = xSObjectListImpl;
        XSParticleDecl checkOccurrences2 = checkOccurrences(xSParticleDecl22, SchemaSymbols.ELT_ALL, (Element) element.getParentNode(), i, l2.longValue());
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return checkOccurrences2;
    }

    /* access modifiers changed from: package-private */
    public XSParticleDecl traverseSequence(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, int i, XSObject xSObject) {
        return traverseSeqChoice(element, xSDocumentInfo, schemaGrammar, i, false, xSObject);
    }

    /* access modifiers changed from: package-private */
    public XSParticleDecl traverseChoice(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, int i, XSObject xSObject) {
        return traverseSeqChoice(element, xSDocumentInfo, schemaGrammar, i, true, xSObject);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0110  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0113  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0129  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0135  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0152  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0155  */
    private XSParticleDecl traverseSeqChoice(Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar, int i, boolean z, XSObject xSObject) {
        XSAnnotationImpl xSAnnotationImpl;
        XSObjectListImpl xSObjectListImpl;
        XSParticleDecl xSParticleDecl;
        XSAnnotationImpl xSAnnotationImpl2;
        Object[] checkAttributes = this.fAttrChecker.checkAttributes(element, false, xSDocumentInfo);
        Element firstChildElement = DOMUtil.getFirstChildElement(element);
        if (firstChildElement == null || !DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
            String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element);
            if (syntheticAnnotation != null) {
                xSAnnotationImpl2 = traverseSyntheticAnnotation(element, syntheticAnnotation, checkAttributes, false, xSDocumentInfo);
            } else {
                xSAnnotationImpl = null;
                this.fPArray.pushContext();
                while (firstChildElement != null) {
                    String localName = DOMUtil.getLocalName(firstChildElement);
                    if (localName.equals(SchemaSymbols.ELT_ELEMENT)) {
                        xSParticleDecl = this.fSchemaHandler.fElementTraverser.traverseLocal(firstChildElement, xSDocumentInfo, schemaGrammar, 0, xSObject);
                    } else {
                        if (localName.equals(SchemaSymbols.ELT_GROUP)) {
                            xSParticleDecl = this.fSchemaHandler.fGroupTraverser.traverseLocal(firstChildElement, xSDocumentInfo, schemaGrammar);
                            if (hasAllContent(xSParticleDecl)) {
                                reportSchemaError("cos-all-limited.1.2", null, firstChildElement);
                            }
                        } else if (localName.equals(SchemaSymbols.ELT_CHOICE)) {
                            xSParticleDecl = traverseChoice(firstChildElement, xSDocumentInfo, schemaGrammar, 0, xSObject);
                        } else if (localName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                            xSParticleDecl = traverseSequence(firstChildElement, xSDocumentInfo, schemaGrammar, 0, xSObject);
                        } else if (localName.equals(SchemaSymbols.ELT_ANY)) {
                            xSParticleDecl = this.fSchemaHandler.fWildCardTraverser.traverseAny(firstChildElement, xSDocumentInfo, schemaGrammar);
                        } else {
                            reportSchemaError("s4s-elt-must-match.1", z ? new Object[]{"choice", "(annotation?, (element | group | choice | sequence | any)*)", DOMUtil.getLocalName(firstChildElement)} : new Object[]{"sequence", "(annotation?, (element | group | choice | sequence | any)*)", DOMUtil.getLocalName(firstChildElement)}, firstChildElement);
                        }
                        xSParticleDecl = null;
                    }
                    if (xSParticleDecl != null) {
                        this.fPArray.addParticle(xSParticleDecl);
                    }
                    firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                }
                XInt xInt = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MINOCCURS];
                XInt xInt2 = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MAXOCCURS];
                Long l = (Long) checkAttributes[XSAttributeChecker.ATTIDX_FROMDEFAULT];
                XSModelGroupImpl xSModelGroupImpl = new XSModelGroupImpl();
                xSModelGroupImpl.fCompositor = !z ? (short) 101 : 102;
                xSModelGroupImpl.fParticleCount = this.fPArray.getParticleCount();
                xSModelGroupImpl.fParticles = this.fPArray.popContext();
                if (xSAnnotationImpl == null) {
                    xSObjectListImpl = new XSObjectListImpl();
                    xSObjectListImpl.addXSObject(xSAnnotationImpl);
                } else {
                    xSObjectListImpl = XSObjectListImpl.EMPTY_LIST;
                }
                xSModelGroupImpl.fAnnotations = xSObjectListImpl;
                XSParticleDecl xSParticleDecl2 = new XSParticleDecl();
                xSParticleDecl2.fType = 3;
                xSParticleDecl2.fMinOccurs = xInt.intValue();
                xSParticleDecl2.fMaxOccurs = xInt2.intValue();
                xSParticleDecl2.fValue = xSModelGroupImpl;
                xSParticleDecl2.fAnnotations = xSObjectListImpl;
                XSParticleDecl checkOccurrences = checkOccurrences(xSParticleDecl2, !z ? SchemaSymbols.ELT_CHOICE : SchemaSymbols.ELT_SEQUENCE, (Element) element.getParentNode(), i, l.longValue());
                this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
                return checkOccurrences;
            }
        } else {
            xSAnnotationImpl2 = traverseAnnotationDecl(firstChildElement, checkAttributes, false, xSDocumentInfo);
            firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
        }
        xSAnnotationImpl = xSAnnotationImpl2;
        this.fPArray.pushContext();
        while (firstChildElement != null) {
        }
        XInt xInt3 = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MINOCCURS];
        XInt xInt22 = (XInt) checkAttributes[XSAttributeChecker.ATTIDX_MAXOCCURS];
        Long l2 = (Long) checkAttributes[XSAttributeChecker.ATTIDX_FROMDEFAULT];
        XSModelGroupImpl xSModelGroupImpl2 = new XSModelGroupImpl();
        xSModelGroupImpl2.fCompositor = !z ? (short) 101 : 102;
        xSModelGroupImpl2.fParticleCount = this.fPArray.getParticleCount();
        xSModelGroupImpl2.fParticles = this.fPArray.popContext();
        if (xSAnnotationImpl == null) {
        }
        xSModelGroupImpl2.fAnnotations = xSObjectListImpl;
        XSParticleDecl xSParticleDecl22 = new XSParticleDecl();
        xSParticleDecl22.fType = 3;
        xSParticleDecl22.fMinOccurs = xInt3.intValue();
        xSParticleDecl22.fMaxOccurs = xInt22.intValue();
        xSParticleDecl22.fValue = xSModelGroupImpl2;
        xSParticleDecl22.fAnnotations = xSObjectListImpl;
        XSParticleDecl checkOccurrences2 = checkOccurrences(xSParticleDecl22, !z ? SchemaSymbols.ELT_CHOICE : SchemaSymbols.ELT_SEQUENCE, (Element) element.getParentNode(), i, l2.longValue());
        this.fAttrChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        return checkOccurrences2;
    }

    /* access modifiers changed from: protected */
    public boolean hasAllContent(XSParticleDecl xSParticleDecl) {
        if (xSParticleDecl != null && xSParticleDecl.fType == 3 && ((XSModelGroupImpl) xSParticleDecl.fValue).fCompositor == 103) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public static class ParticleArray {
        int fContextCount = 0;
        XSParticleDecl[] fParticles = new XSParticleDecl[10];
        int[] fPos = new int[5];

        protected ParticleArray() {
        }

        /* access modifiers changed from: package-private */
        public void pushContext() {
            this.fContextCount++;
            int i = this.fContextCount;
            int[] iArr = this.fPos;
            if (i == iArr.length) {
                int[] iArr2 = new int[(i * 2)];
                System.arraycopy(iArr, 0, iArr2, 0, i);
                this.fPos = iArr2;
            }
            int[] iArr3 = this.fPos;
            int i2 = this.fContextCount;
            iArr3[i2] = iArr3[i2 - 1];
        }

        /* access modifiers changed from: package-private */
        public int getParticleCount() {
            int[] iArr = this.fPos;
            int i = this.fContextCount;
            return iArr[i] - iArr[i - 1];
        }

        /* access modifiers changed from: package-private */
        public void addParticle(XSParticleDecl xSParticleDecl) {
            int[] iArr = this.fPos;
            int i = this.fContextCount;
            int i2 = iArr[i];
            XSParticleDecl[] xSParticleDeclArr = this.fParticles;
            if (i2 == xSParticleDeclArr.length) {
                XSParticleDecl[] xSParticleDeclArr2 = new XSParticleDecl[(iArr[i] * 2)];
                System.arraycopy(xSParticleDeclArr, 0, xSParticleDeclArr2, 0, iArr[i]);
                this.fParticles = xSParticleDeclArr2;
            }
            XSParticleDecl[] xSParticleDeclArr3 = this.fParticles;
            int[] iArr2 = this.fPos;
            int i3 = this.fContextCount;
            int i4 = iArr2[i3];
            iArr2[i3] = i4 + 1;
            xSParticleDeclArr3[i4] = xSParticleDecl;
        }

        /* access modifiers changed from: package-private */
        public XSParticleDecl[] popContext() {
            int[] iArr = this.fPos;
            int i = this.fContextCount;
            int i2 = iArr[i] - iArr[i - 1];
            XSParticleDecl[] xSParticleDeclArr = null;
            if (i2 != 0) {
                XSParticleDecl[] xSParticleDeclArr2 = new XSParticleDecl[i2];
                System.arraycopy(this.fParticles, iArr[i - 1], xSParticleDeclArr2, 0, i2);
                for (int i3 = this.fPos[this.fContextCount - 1]; i3 < this.fPos[this.fContextCount]; i3++) {
                    this.fParticles[i3] = null;
                }
                xSParticleDeclArr = xSParticleDeclArr2;
            }
            this.fContextCount--;
            return xSParticleDeclArr;
        }
    }
}
