package javax.xml.parsers;

import javax.xml.validation.Schema;
import org.apache.harmony.xml.parsers.SAXParserFactoryImpl;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public abstract class SAXParserFactory {
    private boolean namespaceAware = false;
    private boolean validating = false;

    public abstract boolean getFeature(String str) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;

    public abstract SAXParser newSAXParser() throws ParserConfigurationException, SAXException;

    public abstract void setFeature(String str, boolean z) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;

    protected SAXParserFactory() {
    }

    public static SAXParserFactory newInstance() {
        return new SAXParserFactoryImpl();
    }

    public static SAXParserFactory newInstance(String factoryClassName, ClassLoader classLoader) {
        Class<?> type;
        if (factoryClassName != null) {
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            if (classLoader != null) {
                try {
                    type = classLoader.loadClass(factoryClassName);
                } catch (ClassNotFoundException e) {
                    throw new FactoryConfigurationError((Exception) e);
                } catch (InstantiationException e2) {
                    throw new FactoryConfigurationError((Exception) e2);
                } catch (IllegalAccessException e3) {
                    throw new FactoryConfigurationError((Exception) e3);
                }
            } else {
                type = Class.forName(factoryClassName);
            }
            return (SAXParserFactory) type.newInstance();
        }
        throw new FactoryConfigurationError("factoryClassName == null");
    }

    public void setNamespaceAware(boolean awareness) {
        this.namespaceAware = awareness;
    }

    public void setValidating(boolean validating2) {
        this.validating = validating2;
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

    public void setXIncludeAware(boolean state) {
        throw new UnsupportedOperationException("This parser does not support specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }

    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException("This parser does not support specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }
}
