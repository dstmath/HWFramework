package ohos.com.sun.org.apache.xerces.internal.dom;

public class DeferredEntityImpl extends EntityImpl implements DeferredNode {
    static final long serialVersionUID = 4760180431078941638L;
    protected transient int fNodeIndex;

    DeferredEntityImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
        super(deferredDocumentImpl, null);
        this.fNodeIndex = i;
        needsSyncData(true);
        needsSyncChildren(true);
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
        this.publicId = deferredDocumentImpl.getNodeValue(this.fNodeIndex);
        this.systemId = deferredDocumentImpl.getNodeURI(this.fNodeIndex);
        int nodeExtra = deferredDocumentImpl.getNodeExtra(this.fNodeIndex);
        deferredDocumentImpl.getNodeType(nodeExtra);
        this.notationName = deferredDocumentImpl.getNodeName(nodeExtra);
        this.version = deferredDocumentImpl.getNodeValue(nodeExtra);
        this.encoding = deferredDocumentImpl.getNodeURI(nodeExtra);
        int nodeExtra2 = deferredDocumentImpl.getNodeExtra(nodeExtra);
        this.baseURI = deferredDocumentImpl.getNodeName(nodeExtra2);
        this.inputEncoding = deferredDocumentImpl.getNodeValue(nodeExtra2);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode
    public void synchronizeChildren() {
        needsSyncChildren(false);
        isReadOnly(false);
        ((DeferredDocumentImpl) ownerDocument()).synchronizeChildren(this, this.fNodeIndex);
        setReadOnly(true, true);
    }
}
