package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.Text;

public class DocumentFragmentImpl extends ParentNode implements DocumentFragment {
    static final long serialVersionUID = -7596449967279236746L;

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNodeName() {
        return "#document-fragment";
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public short getNodeType() {
        return 11;
    }

    public DocumentFragmentImpl(CoreDocumentImpl coreDocumentImpl) {
        super(coreDocumentImpl);
    }

    public DocumentFragmentImpl() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void normalize() {
        if (!isNormalized()) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            ChildNode childNode = this.firstChild;
            while (childNode != null) {
                ChildNode childNode2 = childNode.nextSibling;
                if (childNode.getNodeType() == 3) {
                    if (childNode2 != null && childNode2.getNodeType() == 3) {
                        ((Text) childNode).appendData(childNode2.getNodeValue());
                        removeChild(childNode2);
                        childNode2 = childNode;
                    } else if (childNode.getNodeValue() == null || childNode.getNodeValue().length() == 0) {
                        removeChild(childNode);
                    }
                }
                childNode.normalize();
                childNode = childNode2;
            }
            isNormalized(true);
        }
    }
}
