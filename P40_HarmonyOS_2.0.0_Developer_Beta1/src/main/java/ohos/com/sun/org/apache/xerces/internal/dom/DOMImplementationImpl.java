package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;

public class DOMImplementationImpl extends CoreDOMImplementationImpl implements DOMImplementation {
    static DOMImplementationImpl singleton = new DOMImplementationImpl();

    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl
    public boolean hasFeature(String str, String str2) {
        boolean hasFeature = super.hasFeature(str, str2);
        if (hasFeature) {
            return hasFeature;
        }
        boolean z = str2 == null || str2.length() == 0;
        if (str.startsWith("+")) {
            str = str.substring(1);
        }
        if ((!str.equalsIgnoreCase("Events") || (!z && !str2.equals("2.0"))) && ((!str.equalsIgnoreCase("MutationEvents") || (!z && !str2.equals("2.0"))) && ((!str.equalsIgnoreCase("Traversal") || (!z && !str2.equals("2.0"))) && (!str.equalsIgnoreCase("Range") || (!z && !str2.equals("2.0")))))) {
            if (!str.equalsIgnoreCase("MutationEvents")) {
                return false;
            }
            if (!z && !str2.equals("2.0")) {
                return false;
            }
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl
    public Document createDocument(String str, String str2, DocumentType documentType) throws DOMException {
        if (str == null && str2 == null && documentType == null) {
            return new DocumentImpl();
        }
        if (documentType == null || documentType.getOwnerDocument() == null) {
            DocumentImpl documentImpl = new DocumentImpl(documentType);
            documentImpl.appendChild(documentImpl.createElementNS(str, str2));
            return documentImpl;
        }
        throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
    }
}
