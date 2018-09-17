package javax.xml.validation;

import java.io.File;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public abstract class SchemaFactory {
    public abstract ErrorHandler getErrorHandler();

    public abstract LSResourceResolver getResourceResolver();

    public abstract boolean isSchemaLanguageSupported(String str);

    public abstract Schema newSchema() throws SAXException;

    public abstract Schema newSchema(Source[] sourceArr) throws SAXException;

    public abstract void setErrorHandler(ErrorHandler errorHandler);

    public abstract void setResourceResolver(LSResourceResolver lSResourceResolver);

    protected SchemaFactory() {
    }

    public static SchemaFactory newInstance(String schemaLanguage) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = SchemaFactory.class.getClassLoader();
        }
        SchemaFactory f = new SchemaFactoryFinder(cl).newFactory(schemaLanguage);
        if (f != null) {
            return f;
        }
        throw new IllegalArgumentException(schemaLanguage);
    }

    public static SchemaFactory newInstance(String schemaLanguage, String factoryClassName, ClassLoader classLoader) {
        if (schemaLanguage == null) {
            throw new NullPointerException("schemaLanguage == null");
        } else if (factoryClassName == null) {
            throw new NullPointerException("factoryClassName == null");
        } else {
            Class<?> type;
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            if (classLoader != null) {
                try {
                    type = classLoader.loadClass(factoryClassName);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(e);
                } catch (InstantiationException e2) {
                    throw new IllegalArgumentException(e2);
                } catch (IllegalAccessException e3) {
                    throw new IllegalArgumentException(e3);
                }
            }
            type = Class.forName(factoryClassName);
            SchemaFactory result = (SchemaFactory) type.newInstance();
            if (result != null && (result.isSchemaLanguageSupported(schemaLanguage) ^ 1) == 0) {
                return result;
            }
            throw new IllegalArgumentException(schemaLanguage);
        }
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        throw new SAXNotRecognizedException(name);
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        throw new SAXNotRecognizedException(name);
    }

    public void setProperty(String name, Object object) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        throw new SAXNotRecognizedException(name);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        throw new SAXNotRecognizedException(name);
    }

    public Schema newSchema(Source schema) throws SAXException {
        return newSchema(new Source[]{schema});
    }

    public Schema newSchema(File schema) throws SAXException {
        return newSchema(new StreamSource(schema));
    }

    public Schema newSchema(URL schema) throws SAXException {
        return newSchema(new StreamSource(schema.toExternalForm()));
    }
}
