package ohos.com.sun.org.apache.xerces.internal.dom;

public final class DeferredAttrNSImpl extends AttrNSImpl implements DeferredNode {
    static final long serialVersionUID = 6074924934945957154L;
    protected transient int fNodeIndex;

    DeferredAttrNSImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
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
        int indexOf = this.name.indexOf(58);
        if (indexOf < 0) {
            this.localName = this.name;
        } else {
            this.localName = this.name.substring(indexOf + 1);
        }
        int nodeExtra = deferredDocumentImpl.getNodeExtra(this.fNodeIndex);
        isSpecified((nodeExtra & 32) != 0);
        if ((nodeExtra & 512) != 0) {
            z = true;
        }
        isIdAttribute(z);
        this.namespaceURI = deferredDocumentImpl.getNodeURI(this.fNodeIndex);
        this.type = deferredDocumentImpl.getTypeInfo(deferredDocumentImpl.getLastChild(this.fNodeIndex));
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.AttrImpl
    public void synchronizeChildren() {
        ((DeferredDocumentImpl) ownerDocument()).synchronizeChildren(this, this.fNodeIndex);
    }
}
