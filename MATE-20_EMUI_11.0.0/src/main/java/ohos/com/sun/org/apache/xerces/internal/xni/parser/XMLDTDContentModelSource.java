package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;

public interface XMLDTDContentModelSource {
    XMLDTDContentModelHandler getDTDContentModelHandler();

    void setDTDContentModelHandler(XMLDTDContentModelHandler xMLDTDContentModelHandler);
}
