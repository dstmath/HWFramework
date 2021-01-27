package ohos.javax.xml.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import ohos.javax.xml.validation.Schema;
import ohos.org.xml.sax.HandlerBase;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Parser;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.helpers.DefaultHandler;

public abstract class SAXParser {
    public abstract Parser getParser() throws SAXException;

    public abstract Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException;

    public abstract XMLReader getXMLReader() throws SAXException;

    public abstract boolean isNamespaceAware();

    public abstract boolean isValidating();

    public abstract void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException;

    protected SAXParser() {
    }

    public void reset() {
        throw new UnsupportedOperationException("This SAXParser, \"" + getClass().getName() + "\", does not support the reset functionality.  Specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }

    public void parse(InputStream inputStream, HandlerBase handlerBase) throws SAXException, IOException {
        if (inputStream != null) {
            parse(new InputSource(inputStream), handlerBase);
            return;
        }
        throw new IllegalArgumentException("InputStream cannot be null");
    }

    public void parse(InputStream inputStream, HandlerBase handlerBase, String str) throws SAXException, IOException {
        if (inputStream != null) {
            InputSource inputSource = new InputSource(inputStream);
            inputSource.setSystemId(str);
            parse(inputSource, handlerBase);
            return;
        }
        throw new IllegalArgumentException("InputStream cannot be null");
    }

    public void parse(InputStream inputStream, DefaultHandler defaultHandler) throws SAXException, IOException {
        if (inputStream != null) {
            parse(new InputSource(inputStream), defaultHandler);
            return;
        }
        throw new IllegalArgumentException("InputStream cannot be null");
    }

    public void parse(InputStream inputStream, DefaultHandler defaultHandler, String str) throws SAXException, IOException {
        if (inputStream != null) {
            InputSource inputSource = new InputSource(inputStream);
            inputSource.setSystemId(str);
            parse(inputSource, defaultHandler);
            return;
        }
        throw new IllegalArgumentException("InputStream cannot be null");
    }

    public void parse(String str, HandlerBase handlerBase) throws SAXException, IOException {
        if (str != null) {
            parse(new InputSource(str), handlerBase);
            return;
        }
        throw new IllegalArgumentException("uri cannot be null");
    }

    public void parse(String str, DefaultHandler defaultHandler) throws SAXException, IOException {
        if (str != null) {
            parse(new InputSource(str), defaultHandler);
            return;
        }
        throw new IllegalArgumentException("uri cannot be null");
    }

    public void parse(File file, HandlerBase handlerBase) throws SAXException, IOException {
        if (file != null) {
            parse(new InputSource(file.toURI().toASCIIString()), handlerBase);
            return;
        }
        throw new IllegalArgumentException("File cannot be null");
    }

    public void parse(File file, DefaultHandler defaultHandler) throws SAXException, IOException {
        if (file != null) {
            parse(new InputSource(file.toURI().toASCIIString()), defaultHandler);
            return;
        }
        throw new IllegalArgumentException("File cannot be null");
    }

    public void parse(InputSource inputSource, HandlerBase handlerBase) throws SAXException, IOException {
        if (inputSource != null) {
            Parser parser = getParser();
            if (handlerBase != null) {
                parser.setDocumentHandler(handlerBase);
                parser.setEntityResolver(handlerBase);
                parser.setErrorHandler(handlerBase);
                parser.setDTDHandler(handlerBase);
            }
            parser.parse(inputSource);
            return;
        }
        throw new IllegalArgumentException("InputSource cannot be null");
    }

    public void parse(InputSource inputSource, DefaultHandler defaultHandler) throws SAXException, IOException {
        if (inputSource != null) {
            XMLReader xMLReader = getXMLReader();
            if (defaultHandler != null) {
                xMLReader.setContentHandler(defaultHandler);
                xMLReader.setEntityResolver(defaultHandler);
                xMLReader.setErrorHandler(defaultHandler);
                xMLReader.setDTDHandler(defaultHandler);
            }
            xMLReader.parse(inputSource);
            return;
        }
        throw new IllegalArgumentException("InputSource cannot be null");
    }

    public Schema getSchema() {
        throw new UnsupportedOperationException("This parser does not support specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }

    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException("This parser does not support specification \"" + getClass().getPackage().getSpecificationTitle() + "\" version \"" + getClass().getPackage().getSpecificationVersion() + "\"");
    }
}
