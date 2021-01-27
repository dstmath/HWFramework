package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

public class ElementDefinitionImpl extends ParentNode {
    static final long serialVersionUID = -8373890672670022714L;
    protected NamedNodeMapImpl attributes;
    protected String name;

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public short getNodeType() {
        return 21;
    }

    public ElementDefinitionImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl);
        this.name = str;
        this.attributes = new NamedNodeMapImpl(coreDocumentImpl);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.name;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.ChildNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node cloneNode(boolean z) {
        ElementDefinitionImpl cloneNode = super.cloneNode(z);
        cloneNode.attributes = this.attributes.cloneMap(cloneNode);
        return cloneNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public NamedNodeMap getAttributes() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this.attributes;
    }
}
