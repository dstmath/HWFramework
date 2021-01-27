package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMConfiguration;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;

public class PSVIDocumentImpl extends DocumentImpl {
    static final long serialVersionUID = -8822220250676434522L;

    public PSVIDocumentImpl() {
    }

    public PSVIDocumentImpl(DocumentType documentType) {
        super(documentType);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.DocumentImpl, ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl, ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.ChildNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node cloneNode(boolean z) {
        Node pSVIDocumentImpl = new PSVIDocumentImpl();
        callUserDataHandlers(this, pSVIDocumentImpl, 1);
        cloneNode(pSVIDocumentImpl, z);
        ((PSVIDocumentImpl) pSVIDocumentImpl).mutationEvents = this.mutationEvents;
        return pSVIDocumentImpl;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.DocumentImpl, ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public DOMImplementation getImplementation() {
        return PSVIDOMImplementationImpl.getDOMImplementation();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public Element createElementNS(String str, String str2) throws DOMException {
        return new PSVIElementNSImpl(this, str, str2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public Element createElementNS(String str, String str2, String str3) throws DOMException {
        return new PSVIElementNSImpl(this, str, str2, str3);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public Attr createAttributeNS(String str, String str2) throws DOMException {
        return new PSVIAttrNSImpl(this, str, str2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public Attr createAttributeNS(String str, String str2, String str3) throws DOMException {
        return new PSVIAttrNSImpl(this, str, str2, str3);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public DOMConfiguration getDomConfig() {
        super.getDomConfig();
        return this.fConfiguration;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        throw new NotSerializableException(getClass().getName());
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        throw new NotSerializableException(getClass().getName());
    }
}
