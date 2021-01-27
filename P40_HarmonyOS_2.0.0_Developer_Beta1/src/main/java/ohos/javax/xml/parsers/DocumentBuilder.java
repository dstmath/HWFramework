package ohos.javax.xml.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import ohos.javax.xml.validation.Schema;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Document;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;

public abstract class DocumentBuilder {
    public abstract DOMImplementation getDOMImplementation();

    public abstract boolean isNamespaceAware();

    public abstract boolean isValidating();

    public abstract Document newDocument();

    public abstract Document parse(InputSource inputSource) throws SAXException, IOException;

    public abstract void setEntityResolver(EntityResolver entityResolver);

    public abstract void setErrorHandler(ErrorHandler errorHandler);

    protected DocumentBuilder() {
    }

    public void reset() {
        throw new UnsupportedOperationException("This DocumentBuilder, \"" + getClass().getName() + "\", does not support the reset functionality.  Specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }

    public Document parse(InputStream inputStream) throws SAXException, IOException {
        if (inputStream != null) {
            return parse(new InputSource(inputStream));
        }
        throw new IllegalArgumentException("InputStream cannot be null");
    }

    public Document parse(InputStream inputStream, String str) throws SAXException, IOException {
        if (inputStream != null) {
            InputSource inputSource = new InputSource(inputStream);
            inputSource.setSystemId(str);
            return parse(inputSource);
        }
        throw new IllegalArgumentException("InputStream cannot be null");
    }

    public Document parse(String str) throws SAXException, IOException {
        if (str != null) {
            return parse(new InputSource(str));
        }
        throw new IllegalArgumentException("URI cannot be null");
    }

    public Document parse(File file) throws SAXException, IOException {
        if (file != null) {
            return parse(new InputSource(file.toURI().toASCIIString()));
        }
        throw new IllegalArgumentException("File cannot be null");
    }

    public Schema getSchema() {
        throw new UnsupportedOperationException("This parser does not support specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }

    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException("This parser does not support specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }
}
