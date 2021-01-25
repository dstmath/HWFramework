package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;

public abstract class XMLParser {
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/entity-resolver", ERROR_HANDLER};
    protected XMLParserConfiguration fConfiguration;
    XMLSecurityManager securityManager;
    XMLSecurityPropertyManager securityPropertyManager;

    /* access modifiers changed from: protected */
    public void reset() throws XNIException {
    }

    public boolean getFeature(String str) throws SAXNotSupportedException, SAXNotRecognizedException {
        return this.fConfiguration.getFeature(str);
    }

    protected XMLParser(XMLParserConfiguration xMLParserConfiguration) {
        this.fConfiguration = xMLParserConfiguration;
        this.fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
    }

    public void parse(XMLInputSource xMLInputSource) throws XNIException, IOException {
        if (this.securityManager == null) {
            this.securityManager = new XMLSecurityManager(true);
            this.fConfiguration.setProperty("http://apache.org/xml/properties/security-manager", this.securityManager);
        }
        if (this.securityPropertyManager == null) {
            this.securityPropertyManager = new XMLSecurityPropertyManager();
            this.fConfiguration.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.securityPropertyManager);
        }
        reset();
        this.fConfiguration.parse(xMLInputSource);
    }
}
