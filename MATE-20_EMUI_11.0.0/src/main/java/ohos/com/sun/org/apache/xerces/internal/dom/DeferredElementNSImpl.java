package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.org.w3c.dom.NamedNodeMap;

public class DeferredElementNSImpl extends ElementNSImpl implements DeferredNode {
    static final long serialVersionUID = -5001885145370927385L;
    protected transient int fNodeIndex;

    DeferredElementNSImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
        super(deferredDocumentImpl, null);
        this.fNodeIndex = i;
        needsSyncChildren(true);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.DeferredNode
    public final int getNodeIndex() {
        return this.fNodeIndex;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ElementImpl, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public final void synchronizeData() {
        boolean z = false;
        needsSyncData(false);
        DeferredDocumentImpl deferredDocumentImpl = (DeferredDocumentImpl) this.ownerDocument;
        boolean z2 = deferredDocumentImpl.mutationEvents;
        deferredDocumentImpl.mutationEvents = false;
        this.name = deferredDocumentImpl.getNodeName(this.fNodeIndex);
        int indexOf = this.name.indexOf(58);
        if (indexOf < 0) {
            this.localName = this.name;
        } else {
            this.localName = this.name.substring(indexOf + 1);
        }
        this.namespaceURI = deferredDocumentImpl.getNodeURI(this.fNodeIndex);
        this.type = (XSTypeDefinition) deferredDocumentImpl.getTypeInfo(this.fNodeIndex);
        setupDefaultAttributes();
        int nodeExtra = deferredDocumentImpl.getNodeExtra(this.fNodeIndex);
        if (nodeExtra != -1) {
            NamedNodeMap attributes = getAttributes();
            do {
                AttrImpl attrImpl = (AttrImpl) deferredDocumentImpl.getNodeObject(nodeExtra);
                if (attrImpl.getSpecified() || (!z && (attrImpl.getNamespaceURI() == null || attrImpl.getNamespaceURI() == NamespaceContext.XMLNS_URI || attrImpl.getName().indexOf(58) >= 0))) {
                    attributes.setNamedItem(attrImpl);
                } else {
                    attributes.setNamedItemNS(attrImpl);
                    z = true;
                }
                nodeExtra = deferredDocumentImpl.getPrevSibling(nodeExtra);
            } while (nodeExtra != -1);
        }
        deferredDocumentImpl.mutationEvents = z2;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode
    public final void synchronizeChildren() {
        ((DeferredDocumentImpl) ownerDocument()).synchronizeChildren(this, this.fNodeIndex);
    }
}
