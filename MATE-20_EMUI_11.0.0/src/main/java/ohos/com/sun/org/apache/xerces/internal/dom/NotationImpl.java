package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Notation;

public class NotationImpl extends NodeImpl implements Notation {
    static final long serialVersionUID = -764632195890658402L;
    protected String baseURI;
    protected String name;
    protected String publicId;
    protected String systemId;

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public short getNodeType() {
        return 12;
    }

    public NotationImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl);
        this.name = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.name;
    }

    public String getPublicId() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.publicId;
    }

    public String getSystemId() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.systemId;
    }

    public void setPublicId(String str) {
        if (!isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            this.publicId = str;
            return;
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public void setSystemId(String str) {
        if (!isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            this.systemId = str;
            return;
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getBaseURI() {
        if (needsSyncData()) {
            synchronizeData();
        }
        String str = this.baseURI;
        if (str == null || str.length() == 0) {
            return this.baseURI;
        }
        try {
            return new URI(this.baseURI).toString();
        } catch (URI.MalformedURIException unused) {
            return null;
        }
    }

    public void setBaseURI(String str) {
        if (needsSyncData()) {
            synchronizeData();
        }
        this.baseURI = str;
    }
}
