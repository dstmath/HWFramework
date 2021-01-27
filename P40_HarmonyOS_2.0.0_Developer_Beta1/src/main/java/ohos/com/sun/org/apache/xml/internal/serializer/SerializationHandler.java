package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import ohos.javax.xml.transform.Transformer;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.DeclHandler;

public interface SerializationHandler extends ExtendedContentHandler, ExtendedLexicalHandler, XSLOutputAttributes, DeclHandler, DTDHandler, ErrorHandler, DOMSerializer, Serializer {
    void close();

    void flushPending() throws SAXException;

    Transformer getTransformer();

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.DOMSerializer
    void serialize(Node node) throws IOException;

    void setContentHandler(ContentHandler contentHandler);

    void setDTDEntityExpansion(boolean z);

    boolean setEscaping(boolean z) throws SAXException;

    void setIndentAmount(int i);

    void setIsStandalone(boolean z);

    void setNamespaceMappings(NamespaceMappings namespaceMappings);

    void setTransformer(Transformer transformer);
}
