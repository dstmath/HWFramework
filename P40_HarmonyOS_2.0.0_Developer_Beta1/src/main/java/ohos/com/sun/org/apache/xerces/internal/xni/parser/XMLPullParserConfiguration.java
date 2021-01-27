package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public interface XMLPullParserConfiguration extends XMLParserConfiguration {
    void cleanup();

    boolean parse(boolean z) throws XNIException, IOException;

    void setInputSource(XMLInputSource xMLInputSource) throws XMLConfigurationException, IOException;
}
