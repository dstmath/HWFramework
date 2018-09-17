package org.xml.sax;

import java.io.IOException;

public interface XMLReader {
    ContentHandler getContentHandler();

    DTDHandler getDTDHandler();

    EntityResolver getEntityResolver();

    ErrorHandler getErrorHandler();

    boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException;

    Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException;

    void parse(String str) throws IOException, SAXException;

    void parse(InputSource inputSource) throws IOException, SAXException;

    void setContentHandler(ContentHandler contentHandler);

    void setDTDHandler(DTDHandler dTDHandler);

    void setEntityResolver(EntityResolver entityResolver);

    void setErrorHandler(ErrorHandler errorHandler);

    void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException;

    void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException;
}
