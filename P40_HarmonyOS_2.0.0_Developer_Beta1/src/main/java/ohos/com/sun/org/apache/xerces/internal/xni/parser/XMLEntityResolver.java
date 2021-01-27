package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public interface XMLEntityResolver {
    XMLInputSource resolveEntity(XMLResourceIdentifier xMLResourceIdentifier) throws XNIException, IOException;
}
