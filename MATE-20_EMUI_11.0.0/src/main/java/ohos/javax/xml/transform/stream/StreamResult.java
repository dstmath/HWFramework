package ohos.javax.xml.transform.stream;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import ohos.javax.xml.transform.Result;

public class StreamResult implements Result {
    public static final String FEATURE = "http://ohos.javax.xml.transform.stream.StreamResult/feature";
    private OutputStream outputStream;
    private String systemId;
    private Writer writer;

    public StreamResult() {
    }

    public StreamResult(OutputStream outputStream2) {
        setOutputStream(outputStream2);
    }

    public StreamResult(Writer writer2) {
        setWriter(writer2);
    }

    public StreamResult(String str) {
        this.systemId = str;
    }

    public StreamResult(File file) {
        setSystemId(file.toURI().toASCIIString());
    }

    public void setOutputStream(OutputStream outputStream2) {
        this.outputStream = outputStream2;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public void setWriter(Writer writer2) {
        this.writer = writer2;
    }

    public Writer getWriter() {
        return this.writer;
    }

    @Override // ohos.javax.xml.transform.Result
    public void setSystemId(String str) {
        this.systemId = str;
    }

    public void setSystemId(File file) {
        this.systemId = file.toURI().toASCIIString();
    }

    @Override // ohos.javax.xml.transform.Result
    public String getSystemId() {
        return this.systemId;
    }
}
