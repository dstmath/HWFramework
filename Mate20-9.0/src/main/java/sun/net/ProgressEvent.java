package sun.net;

import java.net.URL;
import java.util.EventObject;
import sun.net.ProgressSource;

public class ProgressEvent extends EventObject {
    private String contentType;
    private long expected;
    private String method;
    private long progress;
    private ProgressSource.State state;
    private URL url;

    public ProgressEvent(ProgressSource source, URL url2, String method2, String contentType2, ProgressSource.State state2, long progress2, long expected2) {
        super(source);
        this.url = url2;
        this.method = method2;
        this.contentType = contentType2;
        this.progress = progress2;
        this.expected = expected2;
        this.state = state2;
    }

    public URL getURL() {
        return this.url;
    }

    public String getMethod() {
        return this.method;
    }

    public String getContentType() {
        return this.contentType;
    }

    public long getProgress() {
        return this.progress;
    }

    public long getExpected() {
        return this.expected;
    }

    public ProgressSource.State getState() {
        return this.state;
    }

    public String toString() {
        return getClass().getName() + "[url=" + this.url + ", method=" + this.method + ", state=" + this.state + ", content-type=" + this.contentType + ", progress=" + this.progress + ", expected=" + this.expected + "]";
    }
}
