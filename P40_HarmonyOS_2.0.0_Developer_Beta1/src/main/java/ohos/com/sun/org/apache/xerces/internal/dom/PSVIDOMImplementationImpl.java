package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;

public class PSVIDOMImplementationImpl extends CoreDOMImplementationImpl {
    static PSVIDOMImplementationImpl singleton = new PSVIDOMImplementationImpl();

    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl
    public boolean hasFeature(String str, String str2) {
        return super.hasFeature(str, str2) || str.equalsIgnoreCase(Constants.DOM_PSVI);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl
    public Document createDocument(String str, String str2, DocumentType documentType) throws DOMException {
        if (documentType == null || documentType.getOwnerDocument() == null) {
            PSVIDocumentImpl pSVIDocumentImpl = new PSVIDocumentImpl(documentType);
            pSVIDocumentImpl.appendChild(pSVIDocumentImpl.createElementNS(str, str2));
            return pSVIDocumentImpl;
        }
        throw new DOMException(4, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "WRONG_DOCUMENT_ERR", null));
    }
}
