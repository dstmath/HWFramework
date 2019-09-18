package sun.net;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class ProgressMonitor {
    private static ProgressMeteringPolicy meteringPolicy = new DefaultProgressMeteringPolicy();
    private static ProgressMonitor pm = new ProgressMonitor();
    private ArrayList<ProgressListener> progressListenerList = new ArrayList<>();
    private ArrayList<ProgressSource> progressSourceList = new ArrayList<>();

    public static synchronized ProgressMonitor getDefault() {
        ProgressMonitor progressMonitor;
        synchronized (ProgressMonitor.class) {
            progressMonitor = pm;
        }
        return progressMonitor;
    }

    public static synchronized void setDefault(ProgressMonitor m) {
        synchronized (ProgressMonitor.class) {
            if (m != null) {
                pm = m;
            }
        }
    }

    public static synchronized void setMeteringPolicy(ProgressMeteringPolicy policy) {
        synchronized (ProgressMonitor.class) {
            if (policy != null) {
                meteringPolicy = policy;
            }
        }
    }

    public ArrayList<ProgressSource> getProgressSources() {
        ArrayList<ProgressSource> snapshot = new ArrayList<>();
        try {
            synchronized (this.progressSourceList) {
                Iterator<ProgressSource> iter = this.progressSourceList.iterator();
                while (iter.hasNext()) {
                    snapshot.add((ProgressSource) iter.next().clone());
                }
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return snapshot;
    }

    public synchronized int getProgressUpdateThreshold() {
        return meteringPolicy.getProgressUpdateThreshold();
    }

    public boolean shouldMeterInput(URL url, String method) {
        return meteringPolicy.shouldMeterInput(url, method);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        if (r14.progressListenerList.size() <= 0) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001b, code lost:
        r0 = new java.util.ArrayList<>();
        r1 = r14.progressListenerList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r2 = r14.progressListenerList.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002d, code lost:
        if (r2.hasNext() == false) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
        r0.add(r2.next());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0039, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
        r1 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0042, code lost:
        if (r1.hasNext() == false) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0044, code lost:
        r3 = new sun.net.ProgressEvent(r15, r15.getURL(), r15.getMethod(), r15.getContentType(), r15.getState(), r15.getProgress(), r15.getExpected());
        r1.next().progressStart(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0070, code lost:
        return;
     */
    public void registerSource(ProgressSource pi) {
        synchronized (this.progressSourceList) {
            if (!this.progressSourceList.contains(pi)) {
                this.progressSourceList.add(pi);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        if (r14.progressListenerList.size() <= 0) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001e, code lost:
        r0 = new java.util.ArrayList<>();
        r1 = r14.progressListenerList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0025, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r2 = r14.progressListenerList.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        if (r2.hasNext() == false) goto L_0x003c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0032, code lost:
        r0.add(r2.next());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003c, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003d, code lost:
        r1 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0045, code lost:
        if (r1.hasNext() == false) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0047, code lost:
        r3 = new sun.net.ProgressEvent(r15, r15.getURL(), r15.getMethod(), r15.getContentType(), r15.getState(), r15.getProgress(), r15.getExpected());
        r1.next().progressFinish(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0073, code lost:
        return;
     */
    public void unregisterSource(ProgressSource pi) {
        synchronized (this.progressSourceList) {
            if (this.progressSourceList.contains(pi)) {
                pi.close();
                this.progressSourceList.remove((Object) pi);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        r0 = new java.util.ArrayList<>();
        r1 = r14.progressListenerList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r2 = r14.progressListenerList.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        if (r2.hasNext() == false) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        r0.add(r2.next());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0034, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0035, code lost:
        r1 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003d, code lost:
        if (r1.hasNext() == false) goto L_0x006b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003f, code lost:
        r3 = new sun.net.ProgressEvent(r15, r15.getURL(), r15.getMethod(), r15.getContentType(), r15.getState(), r15.getProgress(), r15.getExpected());
        r1.next().progressUpdate(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
        if (r14.progressListenerList.size() <= 0) goto L_0x006b;
     */
    public void updateProgress(ProgressSource pi) {
        synchronized (this.progressSourceList) {
            if (!this.progressSourceList.contains(pi)) {
            }
        }
    }

    public void addProgressListener(ProgressListener l) {
        synchronized (this.progressListenerList) {
            this.progressListenerList.add(l);
        }
    }

    public void removeProgressListener(ProgressListener l) {
        synchronized (this.progressListenerList) {
            this.progressListenerList.remove((Object) l);
        }
    }
}
