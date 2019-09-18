package org.apache.harmony.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;

public final class DocumentTypeImpl extends LeafNodeImpl implements DocumentType {
    private String publicId;
    private String qualifiedName;
    private String systemId;

    public DocumentTypeImpl(DocumentImpl document, String qualifiedName2, String publicId2, String systemId2) {
        super(document);
        if (qualifiedName2 == null || "".equals(qualifiedName2)) {
            throw new DOMException(14, qualifiedName2);
        }
        int prefixSeparator = qualifiedName2.lastIndexOf(":");
        if (prefixSeparator != -1) {
            String prefix = qualifiedName2.substring(0, prefixSeparator);
            String localName = qualifiedName2.substring(prefixSeparator + 1);
            if (!DocumentImpl.isXMLIdentifier(prefix)) {
                throw new DOMException(14, qualifiedName2);
            } else if (!DocumentImpl.isXMLIdentifier(localName)) {
                throw new DOMException(5, qualifiedName2);
            }
        } else if (!DocumentImpl.isXMLIdentifier(qualifiedName2)) {
            throw new DOMException(5, qualifiedName2);
        }
        this.qualifiedName = qualifiedName2;
        this.publicId = publicId2;
        this.systemId = systemId2;
    }

    public String getNodeName() {
        return this.qualifiedName;
    }

    public short getNodeType() {
        return 10;
    }

    public NamedNodeMap getEntities() {
        return null;
    }

    public String getInternalSubset() {
        return null;
    }

    public String getName() {
        return this.qualifiedName;
    }

    public NamedNodeMap getNotations() {
        return null;
    }

    public String getPublicId() {
        return this.publicId;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public String getTextContent() throws DOMException {
        return null;
    }
}
