package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public interface XMLDocumentScanner extends XMLDocumentSource {
    int next() throws XNIException, IOException;

    boolean scanDocument(boolean z) throws IOException, XNIException;

    void setInputSource(XMLInputSource xMLInputSource) throws IOException;
}
