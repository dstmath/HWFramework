package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLDTDDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

public interface ExternalSubsetResolver extends XMLEntityResolver {
    XMLInputSource getExternalSubset(XMLDTDDescription xMLDTDDescription) throws XNIException, IOException;
}
