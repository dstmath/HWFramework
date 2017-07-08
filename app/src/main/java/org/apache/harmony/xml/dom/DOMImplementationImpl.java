package org.apache.harmony.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

public final class DOMImplementationImpl implements DOMImplementation {
    private static DOMImplementationImpl instance;

    DOMImplementationImpl() {
    }

    public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype) throws DOMException {
        return new DocumentImpl(this, namespaceURI, qualifiedName, doctype, null);
    }

    public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) throws DOMException {
        return new DocumentTypeImpl(null, qualifiedName, publicId, systemId);
    }

    public boolean hasFeature(String feature, String version) {
        boolean z = true;
        boolean anyVersion = version == null || version.length() == 0;
        if (feature.startsWith("+")) {
            feature = feature.substring(1);
        }
        if (feature.equalsIgnoreCase("Core")) {
            if (!(anyVersion || version.equals("1.0") || version.equals("2.0"))) {
                z = version.equals("3.0");
            }
            return z;
        } else if (feature.equalsIgnoreCase("XML")) {
            if (!(anyVersion || version.equals("1.0") || version.equals("2.0"))) {
                z = version.equals("3.0");
            }
            return z;
        } else if (!feature.equalsIgnoreCase("XMLVersion")) {
            return false;
        } else {
            if (!(anyVersion || version.equals("1.0"))) {
                z = version.equals("1.1");
            }
            return z;
        }
    }

    public static DOMImplementationImpl getInstance() {
        if (instance == null) {
            instance = new DOMImplementationImpl();
        }
        return instance;
    }

    public Object getFeature(String feature, String version) {
        return hasFeature(feature, version) ? this : null;
    }
}
