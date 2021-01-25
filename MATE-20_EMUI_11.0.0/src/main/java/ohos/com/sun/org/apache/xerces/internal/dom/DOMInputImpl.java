package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.InputStream;
import java.io.Reader;
import ohos.org.w3c.dom.ls.LSInput;

public class DOMInputImpl implements LSInput {
    protected String fBaseSystemId = null;
    protected InputStream fByteStream = null;
    protected boolean fCertifiedText = false;
    protected Reader fCharStream = null;
    protected String fData = null;
    protected String fEncoding = null;
    protected String fPublicId = null;
    protected String fSystemId = null;

    public DOMInputImpl() {
    }

    public DOMInputImpl(String str, String str2, String str3) {
        this.fPublicId = str;
        this.fSystemId = str2;
        this.fBaseSystemId = str3;
    }

    public DOMInputImpl(String str, String str2, String str3, InputStream inputStream, String str4) {
        this.fPublicId = str;
        this.fSystemId = str2;
        this.fBaseSystemId = str3;
        this.fByteStream = inputStream;
        this.fEncoding = str4;
    }

    public DOMInputImpl(String str, String str2, String str3, Reader reader, String str4) {
        this.fPublicId = str;
        this.fSystemId = str2;
        this.fBaseSystemId = str3;
        this.fCharStream = reader;
        this.fEncoding = str4;
    }

    public DOMInputImpl(String str, String str2, String str3, String str4, String str5) {
        this.fPublicId = str;
        this.fSystemId = str2;
        this.fBaseSystemId = str3;
        this.fData = str4;
        this.fEncoding = str5;
    }

    public InputStream getByteStream() {
        return this.fByteStream;
    }

    public void setByteStream(InputStream inputStream) {
        this.fByteStream = inputStream;
    }

    public Reader getCharacterStream() {
        return this.fCharStream;
    }

    public void setCharacterStream(Reader reader) {
        this.fCharStream = reader;
    }

    public String getStringData() {
        return this.fData;
    }

    public void setStringData(String str) {
        this.fData = str;
    }

    public String getEncoding() {
        return this.fEncoding;
    }

    public void setEncoding(String str) {
        this.fEncoding = str;
    }

    public String getPublicId() {
        return this.fPublicId;
    }

    public void setPublicId(String str) {
        this.fPublicId = str;
    }

    public String getSystemId() {
        return this.fSystemId;
    }

    public void setSystemId(String str) {
        this.fSystemId = str;
    }

    public String getBaseURI() {
        return this.fBaseSystemId;
    }

    public void setBaseURI(String str) {
        this.fBaseSystemId = str;
    }

    public boolean getCertifiedText() {
        return this.fCertifiedText;
    }

    public void setCertifiedText(boolean z) {
        this.fCertifiedText = z;
    }
}
