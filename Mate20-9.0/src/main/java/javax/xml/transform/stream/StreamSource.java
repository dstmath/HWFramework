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

    public StreamSource() {
    }

    public StreamSource(InputStream inputStream2) {
        setInputStream(inputStream2);
    }

    public StreamSource(InputStream inputStream2, String systemId2) {
        setInputStream(inputStream2);
        setSystemId(systemId2);
    }

    public StreamSource(Reader reader2) {
        setReader(reader2);
    }

    public StreamSource(Reader reader2, String systemId2) {
        setReader(reader2);
        setSystemId(systemId2);
    }

    public StreamSource(String systemId2) {
        this.systemId = systemId2;
    }

    public StreamSource(File f) {
        setSystemId(f);
    }

    public void setInputStream(InputStream inputStream2) {
        this.inputStream = inputStream2;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public void setReader(Reader reader2) {
        this.reader = reader2;
    }

    public Reader getReader() {
        return this.reader;
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

    public void setSystemId(File f) {
        this.systemId = FilePathToURI.filepath2URI(f.getAbsolutePath());
    }
}
