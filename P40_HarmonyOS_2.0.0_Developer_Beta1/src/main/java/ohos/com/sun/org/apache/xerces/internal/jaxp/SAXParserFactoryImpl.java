package ohos.com.sun.org.apache.xerces.internal.jaxp;

import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.parsers.SAXParser;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.javax.xml.validation.Schema;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;

public class SAXParserFactoryImpl extends SAXParserFactory {
    private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
    private static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
    private static final String XINCLUDE_FEATURE = "http://apache.org/xml/features/xinclude";
    private boolean fSecureProcess = true;
    private Map<String, Boolean> features;
    private Schema grammar;
    private boolean isXIncludeAware;

    public SAXParser newSAXParser() throws ParserConfigurationException {
        try {
            return new SAXParserImpl(this, this.features, this.fSecureProcess);
        } catch (SAXException e) {
            throw new ParserConfigurationException(e.getMessage());
        }
    }

    private SAXParserImpl newSAXParserImpl() throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        try {
            return new SAXParserImpl(this, this.features);
        } catch (SAXNotSupportedException e) {
            throw e;
        } catch (SAXNotRecognizedException e2) {
            throw e2;
        } catch (SAXException e3) {
            throw new ParserConfigurationException(e3.getMessage());
        }
    }

    public void setFeature(String str, boolean z) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException();
        } else if (!str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
            putInFeatures(str, z);
            try {
                newSAXParserImpl();
            } catch (SAXNotSupportedException e) {
                this.features.remove(str);
                throw e;
            } catch (SAXNotRecognizedException e2) {
                this.features.remove(str);
                throw e2;
            }
        } else if (System.getSecurityManager() == null || z) {
            this.fSecureProcess = z;
            putInFeatures(str, z);
        } else {
            throw new ParserConfigurationException(SAXMessageFormatter.formatMessage(null, "jaxp-secureprocessing-feature", null));
        }
    }

    public boolean getFeature(String str) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException();
        } else if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
            return this.fSecureProcess;
        } else {
            return newSAXParserImpl().getXMLReader().getFeature(str);
        }
    }

    public Schema getSchema() {
        return this.grammar;
    }

    public void setSchema(Schema schema) {
        this.grammar = schema;
    }

    public boolean isXIncludeAware() {
        return getFromFeatures(XINCLUDE_FEATURE);
    }

    public void setXIncludeAware(boolean z) {
        putInFeatures(XINCLUDE_FEATURE, z);
    }

    public void setValidating(boolean z) {
        putInFeatures(VALIDATION_FEATURE, z);
    }

    public boolean isValidating() {
        return getFromFeatures(VALIDATION_FEATURE);
    }

    private void putInFeatures(String str, boolean z) {
        if (this.features == null) {
            this.features = new HashMap();
        }
        this.features.put(str, z ? Boolean.TRUE : Boolean.FALSE);
    }

    private boolean getFromFeatures(String str) {
        Boolean bool;
        Map<String, Boolean> map = this.features;
        if (map == null || (bool = map.get(str)) == null) {
            return false;
        }
        return bool.booleanValue();
    }

    public boolean isNamespaceAware() {
        return getFromFeatures("http://xml.org/sax/features/namespaces");
    }

    public void setNamespaceAware(boolean z) {
        putInFeatures("http://xml.org/sax/features/namespaces", z);
    }
}
