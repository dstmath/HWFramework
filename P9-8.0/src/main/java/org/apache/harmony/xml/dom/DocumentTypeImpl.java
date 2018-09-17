package org.apache.harmony.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;

public final class DocumentTypeImpl extends LeafNodeImpl implements DocumentType {
    private String publicId;
    private String qualifiedName;
    private String systemId;

    public DocumentTypeImpl(DocumentImpl document, String qualifiedName, String publicId, String systemId) {
        super(document);
        if (qualifiedName == null || "".equals(qualifiedName)) {
            throw new DOMException((short) 14, qualifiedName);
        }
        int prefixSeparator = qualifiedName.lastIndexOf(":");
        if (prefixSeparator != -1) {
            String prefix = qualifiedName.substring(0, prefixSeparator);
            String localName = qualifiedName.substring(prefixSeparator + 1);
            if (!DocumentImpl.isXMLIdentifier(prefix)) {
                throw new DOMException((short) 14, qualifiedName);
            } else if (!DocumentImpl.isXMLIdentifier(localName)) {
                throw new DOMException((short) 5, qualifiedName);
            }
        } else if (!DocumentImpl.isXMLIdentifier(qualifiedName)) {
            throw new DOMException((short) 5, qualifiedName);
        }
        this.qualifiedName = qualifiedName;
        this.publicId = publicId;
        this.systemId = systemId;
    }

    public String getNodeName() {
        return this.qualifiedName;
    }

    public short getNodeType() {
        return (short) 10;
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
