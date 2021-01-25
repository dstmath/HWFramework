package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import java.io.InputStream;
import java.io.Reader;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;

public class XMLInputSource {
    protected String fBaseSystemId;
    protected InputStream fByteStream;
    protected Reader fCharStream;
    protected String fEncoding;
    protected String fPublicId;
    protected String fSystemId;

    public XMLInputSource(String str, String str2, String str3) {
        this.fPublicId = str;
        this.fSystemId = str2;
        this.fBaseSystemId = str3;
    }

    public XMLInputSource(XMLResourceIdentifier xMLResourceIdentifier) {
        this.fPublicId = xMLResourceIdentifier.getPublicId();
        this.fSystemId = xMLResourceIdentifier.getLiteralSystemId();
        this.fBaseSystemId = xMLResourceIdentifier.getBaseSystemId();
    }

    public XMLInputSource(String str, String str2, String str3, InputStream inputStream, String str4) {
        this.fPublicId = str;
        this.fSystemId = str2;
        this.fBaseSystemId = str3;
        this.fByteStream = inputStream;
        this.fEncoding = str4;
    }

    public XMLInputSource(String str, String str2, String str3, Reader reader, String str4) {
        this.fPublicId = str;
        this.fSystemId = str2;
        this.fBaseSystemId = str3;
        this.fCharStream = reader;
        this.fEncoding = str4;
    }

    public void setPublicId(String str) {
        this.fPublicId = str;
    }

    public String getPublicId() {
        return this.fPublicId;
    }

    public void setSystemId(String str) {
        this.fSystemId = str;
    }

    public String getSystemId() {
        return this.fSystemId;
    }

    public void setBaseSystemId(String str) {
        this.fBaseSystemId = str;
    }

    public String getBaseSystemId() {
        return this.fBaseSystemId;
    }

    public void setByteStream(InputStream inputStream) {
        this.fByteStream = inputStream;
    }

    public InputStream getByteStream() {
        return this.fByteStream;
    }

    public void setCharacterStream(Reader reader) {
        this.fCharStream = reader;
    }

    public Reader getCharacterStream() {
        return this.fCharStream;
    }

    public void setEncoding(String str) {
        this.fEncoding = str;
    }

    public String getEncoding() {
        return this.fEncoding;
    }
}
