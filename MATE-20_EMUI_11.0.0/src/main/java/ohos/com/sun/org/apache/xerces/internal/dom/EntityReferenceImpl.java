package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.EntityReference;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

public class EntityReferenceImpl extends ParentNode implements EntityReference {
    static final long serialVersionUID = -7381452955687102062L;
    protected String baseURI;
    protected String name;

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public short getNodeType() {
        return 5;
    }

    public EntityReferenceImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl);
        this.name = str;
        isReadOnly(true);
        needsSyncChildren(true);
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
        EntityReferenceImpl cloneNode = super.cloneNode(z);
        cloneNode.setReadOnly(true, z);
        return cloneNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getBaseURI() {
        NamedNodeMap entities;
        EntityImpl namedItem;
        if (needsSyncData()) {
            synchronizeData();
        }
        String str = this.baseURI;
        if (str == null) {
            DocumentType doctype = getOwnerDocument().getDoctype();
            if (!(doctype == null || (entities = doctype.getEntities()) == null || (namedItem = entities.getNamedItem(getNodeName())) == null)) {
                return namedItem.getBaseURI();
            }
        } else if (!(str == null || str.length() == 0)) {
            try {
                return new URI(this.baseURI).toString();
            } catch (URI.MalformedURIException unused) {
                return null;
            }
        }
        return this.baseURI;
    }

    public void setBaseURI(String str) {
        if (needsSyncData()) {
            synchronizeData();
        }
        this.baseURI = str;
    }

    /* access modifiers changed from: protected */
    public String getEntityRefValue() {
        String str;
        String str2;
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        if (this.firstChild == null) {
            return "";
        }
        if (this.firstChild.getNodeType() == 5) {
            str = ((EntityReferenceImpl) this.firstChild).getEntityRefValue();
        } else if (this.firstChild.getNodeType() != 3) {
            return null;
        } else {
            str = this.firstChild.getNodeValue();
        }
        if (this.firstChild.nextSibling == null) {
            return str;
        }
        StringBuffer stringBuffer = new StringBuffer(str);
        for (ChildNode childNode = this.firstChild.nextSibling; childNode != null; childNode = childNode.nextSibling) {
            if (childNode.getNodeType() == 5) {
                str2 = ((EntityReferenceImpl) childNode).getEntityRefValue();
            } else if (childNode.getNodeType() != 3) {
                return null;
            } else {
                str2 = childNode.getNodeValue();
            }
            stringBuffer.append(str2);
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode
    public void synchronizeChildren() {
        NamedNodeMap entities;
        EntityImpl namedItem;
        needsSyncChildren(false);
        DocumentType doctype = getOwnerDocument().getDoctype();
        if (!(doctype == null || (entities = doctype.getEntities()) == null || (namedItem = entities.getNamedItem(getNodeName())) == null)) {
            isReadOnly(false);
            for (Node firstChild = namedItem.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                insertBefore(firstChild.cloneNode(true), null);
            }
            setReadOnly(true, true);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setReadOnly(boolean z, boolean z2) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (z2) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            for (ChildNode childNode = this.firstChild; childNode != null; childNode = childNode.nextSibling) {
                childNode.setReadOnly(z, true);
            }
        }
        isReadOnly(z);
    }
}
