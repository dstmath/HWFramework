package ohos.com.sun.org.apache.xerces.internal.dom;

public class DeferredProcessingInstructionImpl extends ProcessingInstructionImpl implements DeferredNode {
    static final long serialVersionUID = -4643577954293565388L;
    protected transient int fNodeIndex;

    DeferredProcessingInstructionImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
        super(deferredDocumentImpl, null, null);
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
        this.target = deferredDocumentImpl.getNodeName(this.fNodeIndex);
        this.data = deferredDocumentImpl.getNodeValueString(this.fNodeIndex);
    }
}
