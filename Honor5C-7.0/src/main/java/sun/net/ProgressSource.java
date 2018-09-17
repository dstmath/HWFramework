package sun.net;

import java.net.URL;
import java.util.prefs.Preferences;

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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.ProgressSource.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.ProgressSource.State.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.net.ProgressSource.State.<clinit>():void");
        }
    }

    public ProgressSource(URL url, String method) {
        this(url, method, -1);
    }

    public ProgressSource(URL url, String method, long expected) {
        this.progress = 0;
        this.lastProgress = 0;
        this.expected = -1;
        this.connected = false;
        this.threshold = Preferences.MAX_VALUE_LENGTH;
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
