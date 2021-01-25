package ohos.org.xml.sax.ext;

import java.io.IOException;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;

public interface EntityResolver2 extends EntityResolver {
    InputSource getExternalSubset(String str, String str2) throws SAXException, IOException;

    InputSource resolveEntity(String str, String str2, String str3, String str4) throws SAXException, IOException;
}
