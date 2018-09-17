package sun.net.www.http;

/* compiled from: KeepAliveStream */
class KeepAliveCleanerEntry {
    HttpClient hc;
    KeepAliveStream kas;

    public KeepAliveCleanerEntry(KeepAliveStream kas, HttpClient hc) {
        this.kas = kas;
        this.hc = hc;
    }

    protected KeepAliveStream getKeepAliveStream() {
        return this.kas;
    }

    protected HttpClient getHttpClient() {
        return this.hc;
    }

    protected void setQueuedForCleanup() {
        this.kas.queuedForCleanup = true;
    }

    protected boolean getQueuedForCleanup() {
        return this.kas.queuedForCleanup;
    }
}
