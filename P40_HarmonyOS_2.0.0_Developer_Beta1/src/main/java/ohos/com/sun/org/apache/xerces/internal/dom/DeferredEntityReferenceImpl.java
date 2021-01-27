package ohos.com.sun.org.apache.xerces.internal.dom;

public class DeferredEntityReferenceImpl extends EntityReferenceImpl implements DeferredNode {
    static final long serialVersionUID = 390319091370032223L;
    protected transient int fNodeIndex;

    DeferredEntityReferenceImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
        super(deferredDocumentImpl, null);
        this.fNodeIndex = i;
        needsSyncData(true);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.DeferredNode
    public int getNodeIndex() {
        return this.fNodeIndex;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void synchronizeData() {
        needsSyncData(false);
        DeferredDocumentImpl deferredDocumentImpl = (DeferredDocumentImpl) this.ownerDocument;
        this.name = deferredDocumentImpl.getNodeName(this.fNodeIndex);
        this.baseURI = deferredDocumentImpl.getNodeValue(this.fNodeIndex);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.EntityReferenceImpl, ohos.com.sun.org.apache.xerces.internal.dom.ParentNode
    public void synchronizeChildren() {
        needsSyncChildren(false);
        isReadOnly(false);
        ((DeferredDocumentImpl) ownerDocument()).synchronizeChildren(this, this.fNodeIndex);
        setReadOnly(true, true);
    }
}
