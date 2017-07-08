package sun.net;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class ProgressMonitor {
    private static ProgressMeteringPolicy meteringPolicy;
    private static ProgressMonitor pm;
    private ArrayList<ProgressListener> progressListenerList;
    private ArrayList<ProgressSource> progressSourceList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.ProgressMonitor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.ProgressMonitor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.ProgressMonitor.<clinit>():void");
    }

    public ProgressMonitor() {
        this.progressSourceList = new ArrayList();
        this.progressListenerList = new ArrayList();
    }

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

    public void registerSource(ProgressSource pi) {
        synchronized (this.progressSourceList) {
            if (this.progressSourceList.contains(pi)) {
                return;
            }
            this.progressSourceList.add(pi);
            if (this.progressListenerList.size() > 0) {
                Iterator<ProgressListener> iter;
                ArrayList<ProgressListener> listeners = new ArrayList();
                synchronized (this.progressListenerList) {
                    iter = this.progressListenerList.iterator();
                    while (iter.hasNext()) {
                        listeners.add((ProgressListener) iter.next());
                    }
                }
                iter = listeners.iterator();
                while (iter.hasNext()) {
                    ((ProgressListener) iter.next()).progressStart(new ProgressEvent(pi, pi.getURL(), pi.getMethod(), pi.getContentType(), pi.getState(), pi.getProgress(), pi.getExpected()));
                }
            }
        }
    }

    public void unregisterSource(ProgressSource pi) {
        synchronized (this.progressSourceList) {
            if (this.progressSourceList.contains(pi)) {
                pi.close();
                this.progressSourceList.remove((Object) pi);
                if (this.progressListenerList.size() > 0) {
                    Iterator<ProgressListener> iter;
                    ArrayList<ProgressListener> listeners = new ArrayList();
                    synchronized (this.progressListenerList) {
                        iter = this.progressListenerList.iterator();
                        while (iter.hasNext()) {
                            listeners.add((ProgressListener) iter.next());
                        }
                    }
                    iter = listeners.iterator();
                    while (iter.hasNext()) {
                        ((ProgressListener) iter.next()).progressFinish(new ProgressEvent(pi, pi.getURL(), pi.getMethod(), pi.getContentType(), pi.getState(), pi.getProgress(), pi.getExpected()));
                    }
                }
                return;
            }
        }
    }

    public void updateProgress(ProgressSource pi) {
        synchronized (this.progressSourceList) {
            if (this.progressSourceList.contains(pi)) {
                if (this.progressListenerList.size() > 0) {
                    Iterator<ProgressListener> iter;
                    ArrayList<ProgressListener> listeners = new ArrayList();
                    synchronized (this.progressListenerList) {
                        iter = this.progressListenerList.iterator();
                        while (iter.hasNext()) {
                            listeners.add((ProgressListener) iter.next());
                        }
                    }
                    iter = listeners.iterator();
                    while (iter.hasNext()) {
                        ((ProgressListener) iter.next()).progressUpdate(new ProgressEvent(pi, pi.getURL(), pi.getMethod(), pi.getContentType(), pi.getState(), pi.getProgress(), pi.getExpected()));
                    }
                }
                return;
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
