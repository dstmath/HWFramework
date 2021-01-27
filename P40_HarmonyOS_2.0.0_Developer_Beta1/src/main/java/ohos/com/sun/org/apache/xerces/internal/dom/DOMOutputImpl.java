package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.OutputStream;
import java.io.Writer;
import ohos.org.w3c.dom.ls.LSOutput;

public class DOMOutputImpl implements LSOutput {
    protected OutputStream fByteStream = null;
    protected Writer fCharStream = null;
    protected String fEncoding = null;
    protected String fSystemId = null;

    public Writer getCharacterStream() {
        return this.fCharStream;
    }

    public void setCharacterStream(Writer writer) {
        this.fCharStream = writer;
    }

    public OutputStream getByteStream() {
        return this.fByteStream;
    }

    public void setByteStream(OutputStream outputStream) {
        this.fByteStream = outputStream;
    }

    public String getSystemId() {
        return this.fSystemId;
    }

    public void setSystemId(String str) {
        this.fSystemId = str;
    }

    public String getEncoding() {
        return this.fEncoding;
    }

    public void setEncoding(String str) {
        this.fEncoding = str;
    }
}
