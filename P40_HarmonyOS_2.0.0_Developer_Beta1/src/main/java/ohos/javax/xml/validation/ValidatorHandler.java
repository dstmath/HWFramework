package ohos.javax.xml.validation;

import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;

public abstract class ValidatorHandler implements ContentHandler {
    public abstract ContentHandler getContentHandler();

    public abstract ErrorHandler getErrorHandler();

    public abstract LSResourceResolver getResourceResolver();

    public abstract TypeInfoProvider getTypeInfoProvider();

    public abstract void setContentHandler(ContentHandler contentHandler);

    public abstract void setErrorHandler(ErrorHandler errorHandler);

    public abstract void setResourceResolver(LSResourceResolver lSResourceResolver);

    protected ValidatorHandler() {
    }

    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException();
        }
        throw new SAXNotRecognizedException(str);
    }

    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException();
        }
        throw new SAXNotRecognizedException(str);
    }

    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException();
        }
        throw new SAXNotRecognizedException(str);
    }

    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException();
        }
        throw new SAXNotRecognizedException(str);
    }
}
