package org.apache.xml.serializer;

import java.io.IOException;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializerFilter;

public interface DOM3Serializer {
    DOMErrorHandler getErrorHandler();

    LSSerializerFilter getNodeFilter();

    void serializeDOM3(Node node) throws IOException;

    void setErrorHandler(DOMErrorHandler dOMErrorHandler);

    void setNewLine(char[] cArr);

    void setNodeFilter(LSSerializerFilter lSSerializerFilter);
}
