package org.apache.xml.serializer;

import java.io.IOException;
import javax.xml.transform.Transformer;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;

public interface SerializationHandler extends ExtendedContentHandler, ExtendedLexicalHandler, XSLOutputAttributes, DeclHandler, DTDHandler, ErrorHandler, DOMSerializer, Serializer {
    void close();

    void flushPending() throws SAXException;

    Transformer getTransformer();

    void serialize(Node node) throws IOException;

    void setContentHandler(ContentHandler contentHandler);

    void setDTDEntityExpansion(boolean z);

    boolean setEscaping(boolean z) throws SAXException;

    void setIndentAmount(int i);

    void setNamespaceMappings(NamespaceMappings namespaceMappings);

    void setTransformer(Transformer transformer);
}
