package sun.net;

import java.net.URL;
import java.util.EventObject;
import sun.net.ProgressSource.State;

public class ProgressEvent extends EventObject {
    private String contentType;
    private long expected;
    private String method;
    private long progress;
    private State state;
    private URL url;

    public ProgressEvent(ProgressSource source, URL url, String method, String contentType, State state, long progress, long expected) {
        super(source);
        this.url = url;
        this.method = method;
        this.contentType = contentType;
        this.progress = progress;
        this.expected = expected;
        this.state = state;
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

    public State getState() {
        return this.state;
    }

    public String toString() {
        return getClass().getName() + "[url=" + this.url + ", method=" + this.method + ", state=" + this.state + ", content-type=" + this.contentType + ", progress=" + this.progress + ", expected=" + this.expected + "]";
    }
}
