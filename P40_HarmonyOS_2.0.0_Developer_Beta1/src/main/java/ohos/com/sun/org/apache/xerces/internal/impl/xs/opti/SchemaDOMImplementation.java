package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;

final class SchemaDOMImplementation implements DOMImplementation {
    private static final SchemaDOMImplementation singleton = new SchemaDOMImplementation();

    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }

    private SchemaDOMImplementation() {
    }

    public Document createDocument(String str, String str2, DocumentType documentType) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public DocumentType createDocumentType(String str, String str2, String str3) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Object getFeature(String str, String str2) {
        if (singleton.hasFeature(str, str2)) {
            return singleton;
        }
        return null;
    }

    public boolean hasFeature(String str, String str2) {
        boolean z = str2 == null || str2.length() == 0;
        if (str.equalsIgnoreCase("Core") || str.equalsIgnoreCase("XML")) {
            return z || str2.equals("1.0") || str2.equals("2.0") || str2.equals("3.0");
        }
        return false;
    }
}
