package sun.net;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class ProgressMonitor {
    private static ProgressMeteringPolicy meteringPolicy = new DefaultProgressMeteringPolicy();
    private static ProgressMonitor pm = new ProgressMonitor();
    private ArrayList<ProgressListener> progressListenerList = new ArrayList();
    private ArrayList<ProgressSource> progressSourceList = new ArrayList();

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
        ArrayList<ProgressSource> snapshot = new ArrayList();
        try {
            synchronized (this.progressSourceList) {
                Iterator<ProgressSource> iter = this.progressSourceList.iterator();
                while (iter.hasNext()) {
                    snapshot.add((ProgressSource) ((ProgressSource) iter.next()).clone());
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

    /* JADX WARNING: Missing block: B:11:0x0019, code:
            if (r13.progressListenerList.size() <= 0) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:12:0x001b, code:
            r11 = new java.util.ArrayList();
            r2 = r13.progressListenerList;
     */
    /* JADX WARNING: Missing block: B:13:0x0022, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r10 = r13.progressListenerList.iterator();
     */
    /* JADX WARNING: Missing block: B:17:0x002d, code:
            if (r10.hasNext() == false) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:18:0x002f, code:
            r11.add((sun.net.ProgressListener) r10.next());
     */
    /* JADX WARNING: Missing block: B:26:0x003f, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:27:0x0040, code:
            r10 = r11.iterator();
     */
    /* JADX WARNING: Missing block: B:29:0x0048, code:
            if (r10.hasNext() == false) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:30:0x004a, code:
            ((sun.net.ProgressListener) r10.next()).progressStart(new sun.net.ProgressEvent(r14, r14.getURL(), r14.getMethod(), r14.getContentType(), r14.getState(), r14.getProgress(), r14.getExpected()));
     */
    /* JADX WARNING: Missing block: B:31:0x0072, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void registerSource(ProgressSource pi) {
        synchronized (this.progressSourceList) {
            if (this.progressSourceList.contains(pi)) {
                return;
            }
            this.progressSourceList.add(pi);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001c, code:
            if (r13.progressListenerList.size() <= 0) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:12:0x001e, code:
            r11 = new java.util.ArrayList();
            r2 = r13.progressListenerList;
     */
    /* JADX WARNING: Missing block: B:13:0x0025, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r10 = r13.progressListenerList.iterator();
     */
    /* JADX WARNING: Missing block: B:17:0x0030, code:
            if (r10.hasNext() == false) goto L_0x0042;
     */
    /* JADX WARNING: Missing block: B:18:0x0032, code:
            r11.add((sun.net.ProgressListener) r10.next());
     */
    /* JADX WARNING: Missing block: B:26:0x0042, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:27:0x0043, code:
            r10 = r11.iterator();
     */
    /* JADX WARNING: Missing block: B:29:0x004b, code:
            if (r10.hasNext() == false) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:30:0x004d, code:
            ((sun.net.ProgressListener) r10.next()).progressFinish(new sun.net.ProgressEvent(r14, r14.getURL(), r14.getMethod(), r14.getContentType(), r14.getState(), r14.getProgress(), r14.getExpected()));
     */
    /* JADX WARNING: Missing block: B:31:0x0075, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unregisterSource(ProgressSource pi) {
        synchronized (this.progressSourceList) {
            if (this.progressSourceList.contains(pi)) {
                pi.close();
                this.progressSourceList.remove((Object) pi);
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0014, code:
            if (r13.progressListenerList.size() <= 0) goto L_0x006d;
     */
    /* JADX WARNING: Missing block: B:10:0x0016, code:
            r11 = new java.util.ArrayList();
            r2 = r13.progressListenerList;
     */
    /* JADX WARNING: Missing block: B:11:0x001d, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            r10 = r13.progressListenerList.iterator();
     */
    /* JADX WARNING: Missing block: B:15:0x0028, code:
            if (r10.hasNext() == false) goto L_0x003a;
     */
    /* JADX WARNING: Missing block: B:16:0x002a, code:
            r11.add((sun.net.ProgressListener) r10.next());
     */
    /* JADX WARNING: Missing block: B:24:0x003a, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:25:0x003b, code:
            r10 = r11.iterator();
     */
    /* JADX WARNING: Missing block: B:27:0x0043, code:
            if (r10.hasNext() == false) goto L_0x006d;
     */
    /* JADX WARNING: Missing block: B:28:0x0045, code:
            ((sun.net.ProgressListener) r10.next()).progressUpdate(new sun.net.ProgressEvent(r14, r14.getURL(), r14.getMethod(), r14.getContentType(), r14.getState(), r14.getProgress(), r14.getExpected()));
     */
    /* JADX WARNING: Missing block: B:29:0x006d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
