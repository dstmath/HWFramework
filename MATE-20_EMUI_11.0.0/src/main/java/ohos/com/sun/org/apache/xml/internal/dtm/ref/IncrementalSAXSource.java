package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;

public interface IncrementalSAXSource {
    Object deliverMoreNodes(boolean z);

    void setContentHandler(ContentHandler contentHandler);

    void setDTDHandler(DTDHandler dTDHandler);

    void setLexicalHandler(LexicalHandler lexicalHandler);

    void startParse(InputSource inputSource) throws SAXException;
}
