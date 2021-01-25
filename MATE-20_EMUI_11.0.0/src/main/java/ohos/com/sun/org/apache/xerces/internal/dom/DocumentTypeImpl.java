package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.dom.ParentNode;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.UserDataHandler;

public class DocumentTypeImpl extends ParentNode implements DocumentType {
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("name", String.class), new ObjectStreamField(Constants.DOM_ENTITIES, NamedNodeMapImpl.class), new ObjectStreamField("notations", NamedNodeMapImpl.class), new ObjectStreamField("elements", NamedNodeMapImpl.class), new ObjectStreamField("publicID", String.class), new ObjectStreamField("systemID", String.class), new ObjectStreamField("internalSubset", String.class), new ObjectStreamField("doctypeNumber", Integer.TYPE), new ObjectStreamField("userData", Hashtable.class)};
    static final long serialVersionUID = 7751299192316526485L;
    private int doctypeNumber;
    protected NamedNodeMapImpl elements;
    protected NamedNodeMapImpl entities;
    protected String internalSubset;
    protected String name;
    protected NamedNodeMapImpl notations;
    protected String publicID;
    protected String systemID;
    private Map<String, ParentNode.UserDataRecord> userData;

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public short getNodeType() {
        return 10;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getTextContent() throws DOMException {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setTextContent(String str) throws DOMException {
    }

    public DocumentTypeImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl);
        this.doctypeNumber = 0;
        this.userData = null;
        this.name = str;
        this.entities = new NamedNodeMapImpl(this);
        this.notations = new NamedNodeMapImpl(this);
        this.elements = new NamedNodeMapImpl(this);
    }

    public DocumentTypeImpl(CoreDocumentImpl coreDocumentImpl, String str, String str2, String str3) {
        this(coreDocumentImpl, str);
        this.publicID = str2;
        this.systemID = str3;
    }

    public String getPublicId() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.publicID;
    }

    public String getSystemId() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.systemID;
    }

    public void setInternalSubset(String str) {
        if (needsSyncData()) {
            synchronizeData();
        }
        this.internalSubset = str;
    }

    public String getInternalSubset() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.internalSubset;
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
        DocumentTypeImpl cloneNode = super.cloneNode(z);
        cloneNode.entities = this.entities.cloneMap(cloneNode);
        cloneNode.notations = this.notations.cloneMap(cloneNode);
        cloneNode.elements = this.elements.cloneMap(cloneNode);
        return cloneNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public boolean isEqualNode(Node node) {
        if (!super.isEqualNode(node)) {
            return false;
        }
        if (needsSyncData()) {
            synchronizeData();
        }
        DocumentTypeImpl documentTypeImpl = (DocumentTypeImpl) node;
        if ((getPublicId() == null && documentTypeImpl.getPublicId() != null) || ((getPublicId() != null && documentTypeImpl.getPublicId() == null) || ((getSystemId() == null && documentTypeImpl.getSystemId() != null) || ((getSystemId() != null && documentTypeImpl.getSystemId() == null) || ((getInternalSubset() == null && documentTypeImpl.getInternalSubset() != null) || (getInternalSubset() != null && documentTypeImpl.getInternalSubset() == null)))))) {
            return false;
        }
        if (!(getPublicId() == null || getPublicId().equals(documentTypeImpl.getPublicId()))) {
            return false;
        }
        if (!(getSystemId() == null || getSystemId().equals(documentTypeImpl.getSystemId()))) {
            return false;
        }
        if (!(getInternalSubset() == null || getInternalSubset().equals(documentTypeImpl.getInternalSubset()))) {
            return false;
        }
        NamedNodeMapImpl namedNodeMapImpl = documentTypeImpl.entities;
        if ((this.entities == null && namedNodeMapImpl != null) || (this.entities != null && namedNodeMapImpl == null)) {
            return false;
        }
        NamedNodeMapImpl namedNodeMapImpl2 = this.entities;
        if (!(namedNodeMapImpl2 == null || namedNodeMapImpl == null)) {
            if (namedNodeMapImpl2.getLength() != namedNodeMapImpl.getLength()) {
                return false;
            }
            for (int i = 0; this.entities.item(i) != null; i++) {
                NodeImpl item = this.entities.item(i);
                if (!item.isEqualNode(namedNodeMapImpl.getNamedItem(item.getNodeName()))) {
                    return false;
                }
            }
        }
        NamedNodeMapImpl namedNodeMapImpl3 = documentTypeImpl.notations;
        if ((this.notations == null && namedNodeMapImpl3 != null) || (this.notations != null && namedNodeMapImpl3 == null)) {
            return false;
        }
        NamedNodeMapImpl namedNodeMapImpl4 = this.notations;
        if (namedNodeMapImpl4 == null || namedNodeMapImpl3 == null) {
            return true;
        }
        if (namedNodeMapImpl4.getLength() != namedNodeMapImpl3.getLength()) {
            return false;
        }
        for (int i2 = 0; this.notations.item(i2) != null; i2++) {
            NodeImpl item2 = this.notations.item(i2);
            if (!item2.isEqualNode(namedNodeMapImpl3.getNamedItem(item2.getNodeName()))) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setOwnerDocument(CoreDocumentImpl coreDocumentImpl) {
        super.setOwnerDocument(coreDocumentImpl);
        this.entities.setOwnerDocument(coreDocumentImpl);
        this.notations.setOwnerDocument(coreDocumentImpl);
        this.elements.setOwnerDocument(coreDocumentImpl);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public int getNodeNumber() {
        if (getOwnerDocument() != null) {
            return super.getNodeNumber();
        }
        if (this.doctypeNumber == 0) {
            this.doctypeNumber = CoreDOMImplementationImpl.getDOMImplementation().assignDocTypeNumber();
        }
        return this.doctypeNumber;
    }

    public String getName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.name;
    }

    public NamedNodeMap getEntities() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this.entities;
    }

    public NamedNodeMap getNotations() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this.notations;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setReadOnly(boolean z, boolean z2) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        super.setReadOnly(z, z2);
        this.elements.setReadOnly(z, true);
        this.entities.setReadOnly(z, true);
        this.notations.setReadOnly(z, true);
    }

    public NamedNodeMap getElements() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this.elements;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Object setUserData(String str, Object obj, UserDataHandler userDataHandler) {
        ParentNode.UserDataRecord remove;
        if (this.userData == null) {
            this.userData = new HashMap();
        }
        if (obj == null) {
            Map<String, ParentNode.UserDataRecord> map = this.userData;
            if (map == null || (remove = map.remove(str)) == null) {
                return null;
            }
            return remove.fData;
        }
        ParentNode.UserDataRecord put = this.userData.put(str, new ParentNode.UserDataRecord(obj, userDataHandler));
        if (put != null) {
            return put.fData;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Object getUserData(String str) {
        ParentNode.UserDataRecord userDataRecord;
        Map<String, ParentNode.UserDataRecord> map = this.userData;
        if (map == null || (userDataRecord = map.get(str)) == null) {
            return null;
        }
        return userDataRecord.fData;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Map<String, ParentNode.UserDataRecord> getUserDataRecord() {
        return this.userData;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        Map<String, ParentNode.UserDataRecord> map = this.userData;
        Hashtable hashtable = map == null ? null : new Hashtable(map);
        ObjectOutputStream.PutField putFields = objectOutputStream.putFields();
        putFields.put("name", this.name);
        putFields.put(Constants.DOM_ENTITIES, this.entities);
        putFields.put("notations", this.notations);
        putFields.put("elements", this.elements);
        putFields.put("publicID", this.publicID);
        putFields.put("systemID", this.systemID);
        putFields.put("internalSubset", this.internalSubset);
        putFields.put("doctypeNumber", this.doctypeNumber);
        putFields.put("userData", hashtable);
        objectOutputStream.writeFields();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField readFields = objectInputStream.readFields();
        this.name = (String) readFields.get("name", (Object) null);
        this.entities = (NamedNodeMapImpl) readFields.get(Constants.DOM_ENTITIES, (Object) null);
        this.notations = (NamedNodeMapImpl) readFields.get("notations", (Object) null);
        this.elements = (NamedNodeMapImpl) readFields.get("elements", (Object) null);
        this.publicID = (String) readFields.get("publicID", (Object) null);
        this.systemID = (String) readFields.get("systemID", (Object) null);
        this.internalSubset = (String) readFields.get("internalSubset", (Object) null);
        this.doctypeNumber = readFields.get("doctypeNumber", 0);
        Hashtable hashtable = (Hashtable) readFields.get("userData", (Object) null);
        if (hashtable != null) {
            this.userData = new HashMap(hashtable);
        }
    }
}
