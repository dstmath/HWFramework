package ohos.com.sun.org.apache.xerces.internal.dom;

public class DeferredCDATASectionImpl extends CDATASectionImpl implements DeferredNode {
    static final long serialVersionUID = 1983580632355645726L;
    protected transient int fNodeIndex;

    DeferredCDATASectionImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
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
        this.data = ((DeferredDocumentImpl) ownerDocument()).getNodeValueString(this.fNodeIndex);
    }
}
