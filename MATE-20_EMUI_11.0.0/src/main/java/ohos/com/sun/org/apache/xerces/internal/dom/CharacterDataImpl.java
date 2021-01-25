package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;

public abstract class CharacterDataImpl extends ChildNode {
    static final long serialVersionUID = 7931170150428474230L;
    private static transient NodeList singletonNodeList = new NodeList() {
        /* class ohos.com.sun.org.apache.xerces.internal.dom.CharacterDataImpl.AnonymousClass1 */

        public int getLength() {
            return 0;
        }

        public Node item(int i) {
            return null;
        }
    };
    protected String data;

    public CharacterDataImpl() {
    }

    protected CharacterDataImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl);
        this.data = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public NodeList getChildNodes() {
        return singletonNodeList;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNodeValue() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.data;
    }

    /* access modifiers changed from: protected */
    public void setNodeValueInternal(String str) {
        setNodeValueInternal(str, false);
    }

    /* access modifiers changed from: protected */
    public void setNodeValueInternal(String str, boolean z) {
        CoreDocumentImpl ownerDocument = ownerDocument();
        if (!ownerDocument.errorChecking || !isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            String str2 = this.data;
            ownerDocument.modifyingCharacterData(this, z);
            this.data = str;
            ownerDocument.modifiedCharacterData(this, str2, str, z);
            return;
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setNodeValue(String str) {
        setNodeValueInternal(str);
        ownerDocument().replacedText(this);
    }

    public String getData() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.data;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public int getLength() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.data.length();
    }

    public void appendData(String str) {
        if (isReadOnly()) {
            throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
        } else if (str != null) {
            if (needsSyncData()) {
                synchronizeData();
            }
            setNodeValue(this.data + str);
        }
    }

    public void deleteData(int i, int i2) throws DOMException {
        internalDeleteData(i, i2, false);
    }

    /* access modifiers changed from: package-private */
    public void internalDeleteData(int i, int i2, boolean z) throws DOMException {
        String str;
        CoreDocumentImpl ownerDocument = ownerDocument();
        if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (i2 < 0) {
                throw new DOMException(1, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR", null));
            }
        }
        if (needsSyncData()) {
            synchronizeData();
        }
        int max = Math.max((this.data.length() - i2) - i, 0);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(this.data.substring(0, i));
            if (max > 0) {
                int i3 = i + i2;
                str = this.data.substring(i3, max + i3);
            } else {
                str = "";
            }
            sb.append(str);
            setNodeValueInternal(sb.toString(), z);
            ownerDocument.deletedText(this, i, i2);
        } catch (StringIndexOutOfBoundsException unused) {
            throw new DOMException(1, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR", null));
        }
    }

    public void insertData(int i, String str) throws DOMException {
        internalInsertData(i, str, false);
    }

    /* access modifiers changed from: package-private */
    public void internalInsertData(int i, String str, boolean z) throws DOMException {
        CoreDocumentImpl ownerDocument = ownerDocument();
        if (!ownerDocument.errorChecking || !isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            try {
                setNodeValueInternal(new StringBuffer(this.data).insert(i, str).toString(), z);
                ownerDocument.insertedText(this, i, str.length());
            } catch (StringIndexOutOfBoundsException unused) {
                throw new DOMException(1, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR", null));
            }
        } else {
            throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
        }
    }

    public void replaceData(int i, int i2, String str) throws DOMException {
        CoreDocumentImpl ownerDocument = ownerDocument();
        if (!ownerDocument.errorChecking || !isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            ownerDocument.replacingData(this);
            String str2 = this.data;
            internalDeleteData(i, i2, true);
            internalInsertData(i, str, true);
            ownerDocument.replacedCharacterData(this, str2, this.data);
            return;
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public void setData(String str) throws DOMException {
        setNodeValue(str);
    }

    public String substringData(int i, int i2) throws DOMException {
        if (needsSyncData()) {
            synchronizeData();
        }
        int length = this.data.length();
        if (i2 < 0 || i < 0 || i > length - 1) {
            throw new DOMException(1, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR", null));
        }
        return this.data.substring(i, Math.min(i2 + i, length));
    }
}
