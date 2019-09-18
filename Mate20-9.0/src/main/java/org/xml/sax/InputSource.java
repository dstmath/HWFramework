package org.xml.sax;

import java.io.InputStream;
import java.io.Reader;

public class InputSource {
    private InputStream byteStream;
    private Reader characterStream;
    private String encoding;
    private String publicId;
    private String systemId;

    public InputSource() {
    }

    public InputSource(String systemId2) {
        setSystemId(systemId2);
    }

    public InputSource(InputStream byteStream2) {
        setByteStream(byteStream2);
    }

    public InputSource(Reader characterStream2) {
        setCharacterStream(characterStream2);
    }

    public void setPublicId(String publicId2) {
        this.publicId = publicId2;
    }

    public String getPublicId() {
        return this.publicId;
    }

    public void setSystemId(String systemId2) {
        this.systemId = systemId2;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public void setByteStream(InputStream byteStream2) {
        this.byteStream = byteStream2;
    }

    public InputStream getByteStream() {
        return this.byteStream;
    }

    public void setEncoding(String encoding2) {
        this.encoding = encoding2;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setCharacterStream(Reader characterStream2) {
        this.characterStream = characterStream2;
    }

    public Reader getCharacterStream() {
        return this.characterStream;
    }
}
