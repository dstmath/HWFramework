package ohos.javax.xml.parsers;

import ohos.javax.xml.validation.Schema;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;

public abstract class SAXParserFactory {
    private boolean namespaceAware = false;
    private boolean validating = false;

    public abstract boolean getFeature(String str) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;

    public abstract SAXParser newSAXParser() throws ParserConfigurationException, SAXException;

    public abstract void setFeature(String str, boolean z) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;

    protected SAXParserFactory() {
    }

    public static SAXParserFactory newInstance() {
        return (SAXParserFactory) FactoryFinder.find(SAXParserFactory.class, "ohos.com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
    }

    public static SAXParserFactory newInstance(String str, ClassLoader classLoader) {
        return (SAXParserFactory) FactoryFinder.newInstance(SAXParserFactory.class, str, classLoader, false);
    }

    public void setNamespaceAware(boolean z) {
        this.namespaceAware = z;
    }

    public void setValidating(boolean z) {
        this.validating = z;
    }

    public boolean isNamespaceAware() {
        return this.namespaceAware;
    }

    public boolean isValidating() {
        return this.validating;
    }

    public Schema getSchema() {
        throw new UnsupportedOperationException("This parser does not support specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }

    public void setSchema(Schema schema) {
        throw new UnsupportedOperationException("This parser does not support specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }

    public void setXIncludeAware(boolean z) {
        if (z) {
            throw new UnsupportedOperationException(" setXIncludeAware is not supported on this JAXP implementation or earlier: " + getClass());
        }
    }

    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException("This parser does not support specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }
}
