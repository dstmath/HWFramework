package ohos.com.sun.org.apache.xerces.internal.dom;

public class DeferredTextImpl extends TextImpl implements DeferredNode {
    static final long serialVersionUID = 2310613872100393425L;
    protected transient int fNodeIndex;

    DeferredTextImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
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
        boolean z = false;
        needsSyncData(false);
        DeferredDocumentImpl deferredDocumentImpl = (DeferredDocumentImpl) ownerDocument();
        this.data = deferredDocumentImpl.getNodeValueString(this.fNodeIndex);
        if (deferredDocumentImpl.getNodeExtra(this.fNodeIndex) == 1) {
            z = true;
        }
        isIgnorableWhitespace(z);
    }
}
