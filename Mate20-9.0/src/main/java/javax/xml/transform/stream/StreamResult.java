package javax.xml.transform.stream;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.transform.Result;

public class StreamResult implements Result {
    public static final String FEATURE = "http://javax.xml.transform.stream.StreamResult/feature";
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

    public StreamResult(String systemId2) {
        this.systemId = systemId2;
    }

    public StreamResult(File f) {
        setSystemId(f);
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

    public void setSystemId(String systemId2) {
        this.systemId = systemId2;
    }

    public void setSystemId(File f) {
        this.systemId = FilePathToURI.filepath2URI(f.getAbsolutePath());
    }

    public String getSystemId() {
        return this.systemId;
    }
}
