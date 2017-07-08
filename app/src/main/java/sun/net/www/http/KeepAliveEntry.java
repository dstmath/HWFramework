package sun.net.www.http;

/* compiled from: KeepAliveCache */
class KeepAliveEntry {
    HttpClient hc;
    long idleStartTime;

    KeepAliveEntry(HttpClient hc, long idleStartTime) {
        this.hc = hc;
        this.idleStartTime = idleStartTime;
    }
}
