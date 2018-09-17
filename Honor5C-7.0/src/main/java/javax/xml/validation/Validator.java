package javax.xml.validation;

import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public abstract class Validator {
    public abstract ErrorHandler getErrorHandler();

    public abstract LSResourceResolver getResourceResolver();

    public abstract void reset();

    public abstract void setErrorHandler(ErrorHandler errorHandler);

    public abstract void setResourceResolver(LSResourceResolver lSResourceResolver);

    public abstract void validate(Source source, Result result) throws SAXException, IOException;

    protected Validator() {
    }

    public void validate(Source source) throws SAXException, IOException {
        validate(source, null);
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
}
