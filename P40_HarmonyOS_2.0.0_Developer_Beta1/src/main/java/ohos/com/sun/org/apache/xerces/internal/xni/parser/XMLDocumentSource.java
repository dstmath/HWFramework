package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;

public interface XMLDocumentSource {
    XMLDocumentHandler getDocumentHandler();

    void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler);
}
