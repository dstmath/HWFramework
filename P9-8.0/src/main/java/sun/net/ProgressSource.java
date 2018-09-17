package sun.net;

import java.net.URL;

public class ProgressSource {
    private boolean connected;
    private String contentType;
    private long expected;
    private long lastProgress;
    private String method;
    private long progress;
    private ProgressMonitor progressMonitor;
    private State state;
    private int threshold;
    private URL url;

    public enum State {
        NEW,
        CONNECTED,
        UPDATE,
        DELETE
    }

    public ProgressSource(URL url, String method) {
        this(url, method, -1);
    }

    public ProgressSource(URL url, String method, long expected) {
        this.progress = 0;
        this.lastProgress = 0;
        this.expected = -1;
        this.connected = false;
        this.threshold = 8192;
        this.url = url;
        this.method = method;
        this.contentType = "content/unknown";
        this.progress = 0;
        this.lastProgress = 0;
        this.expected = expected;
        this.state = State.NEW;
        this.progressMonitor = ProgressMonitor.getDefault();
        this.threshold = this.progressMonitor.getProgressUpdateThreshold();
    }

    public boolean connected() {
        if (this.connected) {
            return true;
        }
        this.connected = true;
        this.state = State.CONNECTED;
        return false;
    }

    public void close() {
        this.state = State.DELETE;
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

    public void setContentType(String ct) {
        this.contentType = ct;
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

    public void beginTracking() {
        this.progressMonitor.registerSource(this);
    }

    public void finishTracking() {
        this.progressMonitor.unregisterSource(this);
    }

    public void updateProgress(long latestProgress, long expectedProgress) {
        this.lastProgress = this.progress;
        this.progress = latestProgress;
        this.expected = expectedProgress;
        if (connected()) {
            this.state = State.UPDATE;
        } else {
            this.state = State.CONNECTED;
        }
        if (this.lastProgress / ((long) this.threshold) != this.progress / ((long) this.threshold)) {
            this.progressMonitor.updateProgress(this);
        }
        if (this.expected != -1 && this.progress >= this.expected && this.progress != 0) {
            close();
        }
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String toString() {
        return getClass().getName() + "[url=" + this.url + ", method=" + this.method + ", state=" + this.state + ", content-type=" + this.contentType + ", progress=" + this.progress + ", expected=" + this.expected + "]";
    }
}
