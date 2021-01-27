package ohos.javax.xml.validation;

import java.io.File;
import java.net.URL;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;

public abstract class SchemaFactory {
    private static SecuritySupport ss = new SecuritySupport();

    public abstract ErrorHandler getErrorHandler();

    public abstract LSResourceResolver getResourceResolver();

    public abstract boolean isSchemaLanguageSupported(String str);

    public abstract Schema newSchema() throws SAXException;

    public abstract Schema newSchema(Source[] sourceArr) throws SAXException;

    public abstract void setErrorHandler(ErrorHandler errorHandler);

    public abstract void setResourceResolver(LSResourceResolver lSResourceResolver);

    protected SchemaFactory() {
    }

    public static SchemaFactory newInstance(String str) {
        ClassLoader contextClassLoader = ss.getContextClassLoader();
        if (contextClassLoader == null) {
            contextClassLoader = SchemaFactory.class.getClassLoader();
        }
        SchemaFactory newFactory = new SchemaFactoryFinder(contextClassLoader).newFactory(str);
        if (newFactory != null) {
            return newFactory;
        }
        throw new IllegalArgumentException("No SchemaFactory that implements the schema language specified by: " + str + " could be loaded");
    }

    public static SchemaFactory newInstance(String str, String str2, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = ss.getContextClassLoader();
        }
        SchemaFactory createInstance = new SchemaFactoryFinder(classLoader).createInstance(str2);
        if (createInstance == null) {
            throw new IllegalArgumentException("Factory " + str2 + " could not be loaded to implement the schema language specified by: " + str);
        } else if (createInstance.isSchemaLanguageSupported(str)) {
            return createInstance;
        } else {
            throw new IllegalArgumentException("Factory " + createInstance.getClass().getName() + " does not implement the schema language specified by: " + str);
        }
    }

    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException("the name parameter is null");
        }
        throw new SAXNotRecognizedException(str);
    }

    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException("the name parameter is null");
        }
        throw new SAXNotRecognizedException(str);
    }

    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException("the name parameter is null");
        }
        throw new SAXNotRecognizedException(str);
    }

    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException("the name parameter is null");
        }
        throw new SAXNotRecognizedException(str);
    }

    public Schema newSchema(Source source) throws SAXException {
        return newSchema(new Source[]{source});
    }

    public Schema newSchema(File file) throws SAXException {
        return newSchema(new StreamSource(file));
    }

    public Schema newSchema(URL url) throws SAXException {
        return newSchema(new StreamSource(url.toExternalForm()));
    }
}
