package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;

public interface XMLDTDSource {
    XMLDTDHandler getDTDHandler();

    void setDTDHandler(XMLDTDHandler xMLDTDHandler);
}
