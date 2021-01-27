package ohos.com.sun.org.apache.xerces.internal.dom;

public class DeferredNotationImpl extends NotationImpl implements DeferredNode {
    static final long serialVersionUID = 5705337172887990848L;
    protected transient int fNodeIndex;

    DeferredNotationImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
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
        DeferredDocumentImpl deferredDocumentImpl = (DeferredDocumentImpl) ownerDocument();
        this.name = deferredDocumentImpl.getNodeName(this.fNodeIndex);
        deferredDocumentImpl.getNodeType(this.fNodeIndex);
        this.publicId = deferredDocumentImpl.getNodeValue(this.fNodeIndex);
        this.systemId = deferredDocumentImpl.getNodeURI(this.fNodeIndex);
        int nodeExtra = deferredDocumentImpl.getNodeExtra(this.fNodeIndex);
        deferredDocumentImpl.getNodeType(nodeExtra);
        this.baseURI = deferredDocumentImpl.getNodeName(nodeExtra);
    }
}
