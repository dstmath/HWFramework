package javax.xml.transform.stream;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.transform.Source;

public class StreamSource implements Source {
    public static final String FEATURE = "http://javax.xml.transform.stream.StreamSource/feature";
    private InputStream inputStream;
    private String publicId;
    private Reader reader;
    private String systemId;

    public StreamSource(InputStream inputStream) {
        setInputStream(inputStream);
    }

    public StreamSource(InputStream inputStream, String systemId) {
        setInputStream(inputStream);
        setSystemId(systemId);
    }

    public StreamSource(Reader reader) {
        setReader(reader);
    }

    public StreamSource(Reader reader, String systemId) {
        setReader(reader);
        setSystemId(systemId);
    }

    public StreamSource(String systemId) {
        this.systemId = systemId;
    }

    public StreamSource(File f) {
        setSystemId(f);
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Reader getReader() {
        return this.reader;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getPublicId() {
        return this.publicId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public void setSystemId(File f) {
        this.systemId = FilePathToURI.filepath2URI(f.getAbsolutePath());
    }
}
