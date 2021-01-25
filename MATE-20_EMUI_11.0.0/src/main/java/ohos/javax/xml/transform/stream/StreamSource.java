package ohos.javax.xml.transform.stream;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import ohos.javax.xml.transform.Source;

public class StreamSource implements Source {
    public static final String FEATURE = "http://ohos.javax.xml.transform.stream.StreamSource/feature";
    private InputStream inputStream;
    private String publicId;
    private Reader reader;
    private String systemId;

    public StreamSource() {
    }

    public StreamSource(InputStream inputStream2) {
        setInputStream(inputStream2);
    }

    public StreamSource(InputStream inputStream2, String str) {
        setInputStream(inputStream2);
        setSystemId(str);
    }

    public StreamSource(Reader reader2) {
        setReader(reader2);
    }

    public StreamSource(Reader reader2, String str) {
        setReader(reader2);
        setSystemId(str);
    }

    public StreamSource(String str) {
        this.systemId = str;
    }

    public StreamSource(File file) {
        setSystemId(file.toURI().toASCIIString());
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

    public void setPublicId(String str) {
        this.publicId = str;
    }

    public String getPublicId() {
        return this.publicId;
    }

    @Override // ohos.javax.xml.transform.Source
    public void setSystemId(String str) {
        this.systemId = str;
    }

    @Override // ohos.javax.xml.transform.Source
    public String getSystemId() {
        return this.systemId;
    }

    public void setSystemId(File file) {
        this.systemId = file.toURI().toASCIIString();
    }
}
