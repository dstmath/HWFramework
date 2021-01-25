package ohos.javax.xml.validation;

import java.io.IOException;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Source;
import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;

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
}
