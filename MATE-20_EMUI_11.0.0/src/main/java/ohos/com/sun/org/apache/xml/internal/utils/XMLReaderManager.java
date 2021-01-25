package ohos.com.sun.org.apache.xml.internal.utils;

import java.util.HashMap;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLReader;

public class XMLReaderManager {
    private static final XMLReaderManager m_singletonManager = new XMLReaderManager();
    private static final String property = "org.xml.sax.driver";
    private String _accessExternalDTD = "all";
    private boolean _secureProcessing;
    private XMLSecurityManager _xmlSecurityManager;
    private HashMap m_inUse;
    private boolean m_overrideDefaultParser;
    private ThreadLocal<ReaderWrapper> m_readers;

    private XMLReaderManager() {
    }

    public static XMLReaderManager getInstance(boolean z) {
        m_singletonManager.setOverrideDefaultParser(z);
        return m_singletonManager;
    }

    public synchronized XMLReader getXMLReader() throws SAXException {
        XMLReader xMLReader;
        if (this.m_readers == null) {
            this.m_readers = new ThreadLocal<>();
        }
        if (this.m_inUse == null) {
            this.m_inUse = new HashMap();
        }
        ReaderWrapper readerWrapper = this.m_readers.get();
        boolean z = readerWrapper != null;
        xMLReader = z ? readerWrapper.reader : null;
        String systemProperty = SecuritySupport.getSystemProperty(property);
        if (!z || this.m_inUse.get(xMLReader) == Boolean.TRUE || readerWrapper.overrideDefaultParser != this.m_overrideDefaultParser || (systemProperty != null && !xMLReader.getClass().getName().equals(systemProperty))) {
            xMLReader = JdkXmlUtils.getXMLReader(this.m_overrideDefaultParser, this._secureProcessing);
            if (!z) {
                this.m_readers.set(new ReaderWrapper(xMLReader, this.m_overrideDefaultParser));
                this.m_inUse.put(xMLReader, Boolean.TRUE);
            }
        } else {
            this.m_inUse.put(xMLReader, Boolean.TRUE);
        }
        try {
            xMLReader.setProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", this._accessExternalDTD);
        } catch (SAXException e) {
            XMLSecurityManager.printWarning(xMLReader.getClass().getName(), "http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", e);
        }
        try {
            if (this._xmlSecurityManager != null) {
                XMLSecurityManager.Limit[] values = XMLSecurityManager.Limit.values();
                for (XMLSecurityManager.Limit limit : values) {
                    xMLReader.setProperty(limit.apiProperty(), this._xmlSecurityManager.getLimitValueAsString(limit));
                }
                if (this._xmlSecurityManager.printEntityCountInfo()) {
                    xMLReader.setProperty("http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo", "yes");
                }
            }
        } catch (SAXException e2) {
            XMLSecurityManager.printWarning(xMLReader.getClass().getName(), "", e2);
        }
        return xMLReader;
    }

    public synchronized void releaseXMLReader(XMLReader xMLReader) {
        if (this.m_readers.get().reader == xMLReader && xMLReader != null) {
            this.m_inUse.remove(xMLReader);
        }
    }

    public boolean overrideDefaultParser() {
        return this.m_overrideDefaultParser;
    }

    public void setOverrideDefaultParser(boolean z) {
        this.m_overrideDefaultParser = z;
    }

    public void setFeature(String str, boolean z) {
        if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
            this._secureProcessing = z;
        }
    }

    public Object getProperty(String str) {
        if (str.equals("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD")) {
            return this._accessExternalDTD;
        }
        if (str.equals("http://apache.org/xml/properties/security-manager")) {
            return this._xmlSecurityManager;
        }
        return null;
    }

    public void setProperty(String str, Object obj) {
        if (str.equals("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD")) {
            this._accessExternalDTD = (String) obj;
        } else if (str.equals("http://apache.org/xml/properties/security-manager")) {
            this._xmlSecurityManager = (XMLSecurityManager) obj;
        }
    }

    /* access modifiers changed from: package-private */
    public class ReaderWrapper {
        boolean overrideDefaultParser;
        XMLReader reader;

        public ReaderWrapper(XMLReader xMLReader, boolean z) {
            this.reader = xMLReader;
            this.overrideDefaultParser = z;
        }
    }
}
