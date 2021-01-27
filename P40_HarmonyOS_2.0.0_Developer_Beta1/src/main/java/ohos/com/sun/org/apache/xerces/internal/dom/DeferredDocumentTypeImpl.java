package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.PrintStream;
import ohos.org.w3c.dom.Node;

public class DeferredDocumentTypeImpl extends DocumentTypeImpl implements DeferredNode {
    static final long serialVersionUID = -2172579663227313509L;
    protected transient int fNodeIndex;

    DeferredDocumentTypeImpl(DeferredDocumentImpl deferredDocumentImpl, int i) {
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
        this.publicID = deferredDocumentImpl.getNodeValue(this.fNodeIndex);
        this.systemID = deferredDocumentImpl.getNodeURI(this.fNodeIndex);
        this.internalSubset = deferredDocumentImpl.getNodeValue(deferredDocumentImpl.getNodeExtra(this.fNodeIndex));
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode
    public void synchronizeChildren() {
        boolean mutationEvents = ownerDocument().getMutationEvents();
        ownerDocument().setMutationEvents(false);
        needsSyncChildren(false);
        DeferredDocumentImpl deferredDocumentImpl = (DeferredDocumentImpl) this.ownerDocument;
        this.entities = new NamedNodeMapImpl(this);
        this.notations = new NamedNodeMapImpl(this);
        this.elements = new NamedNodeMapImpl(this);
        Node node = null;
        for (int lastChild = deferredDocumentImpl.getLastChild(this.fNodeIndex); lastChild != -1; lastChild = deferredDocumentImpl.getPrevSibling(lastChild)) {
            Node nodeObject = deferredDocumentImpl.getNodeObject(lastChild);
            short nodeType = nodeObject.getNodeType();
            if (nodeType != 1) {
                if (nodeType == 6) {
                    this.entities.setNamedItem(nodeObject);
                } else if (nodeType == 12) {
                    this.notations.setNamedItem(nodeObject);
                } else if (nodeType == 21) {
                    this.elements.setNamedItem(nodeObject);
                }
            } else if (getOwnerDocument().allowGrammarAccess) {
                insertBefore(nodeObject, node);
                node = nodeObject;
            }
            PrintStream printStream = System.out;
            printStream.println("DeferredDocumentTypeImpl#synchronizeInfo: node.getNodeType() = " + ((int) nodeObject.getNodeType()) + ", class = " + nodeObject.getClass().getName());
        }
        ownerDocument().setMutationEvents(mutationEvents);
        setReadOnly(true, false);
    }
}
