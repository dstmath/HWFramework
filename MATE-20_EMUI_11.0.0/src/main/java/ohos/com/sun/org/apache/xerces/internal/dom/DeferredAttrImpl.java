package ohos.com.sun.org.apache.xerces.internal.dom;

public final class DeferredAttrImpl extends AttrImpl implements DeferredNode {
    static final long serialVersionUID = 6903232312469148636L;
    protected transient int fNodeIndex;

    DeferredAttrImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
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
        boolean z = false;
        needsSyncData(false);
        DeferredDocumentImpl deferredDocumentImpl = (DeferredDocumentImpl) ownerDocument();
        this.name = deferredDocumentImpl.getNodeName(this.fNodeIndex);
        int nodeExtra = deferredDocumentImpl.getNodeExtra(this.fNodeIndex);
        isSpecified((nodeExtra & 32) != 0);
        if ((nodeExtra & 512) != 0) {
            z = true;
        }
        isIdAttribute(z);
        this.type = deferredDocumentImpl.getTypeInfo(deferredDocumentImpl.getLastChild(this.fNodeIndex));
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.AttrImpl
    public void synchronizeChildren() {
        ((DeferredDocumentImpl) ownerDocument()).synchronizeChildren(this, this.fNodeIndex);
    }
}
