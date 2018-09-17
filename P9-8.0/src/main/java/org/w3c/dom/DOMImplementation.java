package org.w3c.dom;

public interface DOMImplementation {
    Document createDocument(String str, String str2, DocumentType documentType) throws DOMException;

    DocumentType createDocumentType(String str, String str2, String str3) throws DOMException;

    Object getFeature(String str, String str2);

    boolean hasFeature(String str, String str2);
}
