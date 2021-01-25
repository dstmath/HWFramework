package ohos.com.sun.org.apache.xerces.internal.util;

import java.io.InputStream;
import java.io.Reader;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.XMLReader;

public final class SAXInputSource extends XMLInputSource {
    private InputSource fInputSource;
    private XMLReader fXMLReader;

    public SAXInputSource() {
        this(null);
    }

    public SAXInputSource(InputSource inputSource) {
        this(null, inputSource);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SAXInputSource(XMLReader xMLReader, InputSource inputSource) {
        super(inputSource != null ? inputSource.getPublicId() : null, inputSource != null ? inputSource.getSystemId() : null, null);
        if (inputSource != null) {
            setByteStream(inputSource.getByteStream());
            setCharacterStream(inputSource.getCharacterStream());
            setEncoding(inputSource.getEncoding());
        }
        this.fInputSource = inputSource;
        this.fXMLReader = xMLReader;
    }

    public void setXMLReader(XMLReader xMLReader) {
        this.fXMLReader = xMLReader;
    }

    public XMLReader getXMLReader() {
        return this.fXMLReader;
    }

    public void setInputSource(InputSource inputSource) {
        if (inputSource != null) {
            setPublicId(inputSource.getPublicId());
            setSystemId(inputSource.getSystemId());
            setByteStream(inputSource.getByteStream());
            setCharacterStream(inputSource.getCharacterStream());
            setEncoding(inputSource.getEncoding());
        } else {
            setPublicId(null);
            setSystemId(null);
            setByteStream(null);
            setCharacterStream(null);
            setEncoding(null);
        }
        this.fInputSource = inputSource;
    }

    public InputSource getInputSource() {
        return this.fInputSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource
    public void setPublicId(String str) {
        super.setPublicId(str);
        if (this.fInputSource == null) {
            this.fInputSource = new InputSource();
        }
        this.fInputSource.setPublicId(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource
    public void setSystemId(String str) {
        super.setSystemId(str);
        if (this.fInputSource == null) {
            this.fInputSource = new InputSource();
        }
        this.fInputSource.setSystemId(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource
    public void setByteStream(InputStream inputStream) {
        super.setByteStream(inputStream);
        if (this.fInputSource == null) {
            this.fInputSource = new InputSource();
        }
        this.fInputSource.setByteStream(inputStream);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource
    public void setCharacterStream(Reader reader) {
        super.setCharacterStream(reader);
        if (this.fInputSource == null) {
            this.fInputSource = new InputSource();
        }
        this.fInputSource.setCharacterStream(reader);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource
    public void setEncoding(String str) {
        super.setEncoding(str);
        if (this.fInputSource == null) {
            this.fInputSource = new InputSource();
        }
        this.fInputSource.setEncoding(str);
    }
}
