package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public interface XMLDTDScanner extends XMLDTDSource, XMLDTDContentModelSource {
    boolean scanDTDExternalSubset(boolean z) throws IOException, XNIException;

    boolean scanDTDInternalSubset(boolean z, boolean z2, boolean z3) throws IOException, XNIException;

    void setInputSource(XMLInputSource xMLInputSource) throws IOException;

    void setLimitAnalyzer(XMLLimitAnalyzer xMLLimitAnalyzer);

    boolean skipDTD(boolean z) throws IOException;
}
