package android.os;

import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public abstract class FileObserver {
    public static final int ACCESS = 1;
    public static final int ALL_EVENTS = 4095;
    public static final int ATTRIB = 4;
    public static final int CLOSE_NOWRITE = 16;
    public static final int CLOSE_WRITE = 8;
    public static final int CREATE = 256;
    public static final int DELETE = 512;
    public static final int DELETE_SELF = 1024;
    private static final String LOG_TAG = "FileObserver";
    public static final int MODIFY = 2;
    public static final int MOVED_FROM = 64;
    public static final int MOVED_TO = 128;
    public static final int MOVE_SELF = 2048;
    public static final int OPEN = 32;
    private static ObserverThread s_observerThread;
    private Integer m_descriptor;
    private int m_mask;
    private String m_path;

    private static class ObserverThread extends Thread {
        private int m_fd;
        private HashMap<Integer, WeakReference> m_observers;

        private native int init();

        private native void observe(int i);

        private native int startWatching(int i, String str, int i2);

        private native void stopWatching(int i, int i2);

        public ObserverThread() {
            super(FileObserver.LOG_TAG);
            this.m_observers = new HashMap();
            this.m_fd = init();
        }

        public void run() {
            observe(this.m_fd);
        }

        public int startWatching(String path, int mask, FileObserver observer) {
            int wfd = startWatching(this.m_fd, path, mask);
            Integer i = new Integer(wfd);
            if (wfd >= 0) {
                synchronized (this.m_observers) {
                    this.m_observers.put(i, new WeakReference(observer));
                }
            }
            return i.intValue();
        }

        public void stopWatching(int descriptor) {
            stopWatching(this.m_fd, descriptor);
        }

        public void onEvent(int wfd, int mask, String path) {
            FileObserver fileObserver = null;
            synchronized (this.m_observers) {
                WeakReference weak = (WeakReference) this.m_observers.get(Integer.valueOf(wfd));
                if (weak != null) {
                    fileObserver = (FileObserver) weak.get();
                    if (fileObserver == null) {
                        this.m_observers.remove(Integer.valueOf(wfd));
                    }
                }
            }
            if (fileObserver != null) {
                try {
                    fileObserver.onEvent(mask, path);
                } catch (Throwable throwable) {
                    Log.wtf(FileObserver.LOG_TAG, "Unhandled exception in FileObserver " + fileObserver, throwable);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.FileObserver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.FileObserver.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.FileObserver.<clinit>():void");
    }

    public abstract void onEvent(int i, String str);

    public FileObserver(String path) {
        this(path, ALL_EVENTS);
    }

    public FileObserver(String path, int mask) {
        this.m_path = path;
        this.m_mask = mask;
        this.m_descriptor = Integer.valueOf(-1);
    }

    protected void finalize() {
        stopWatching();
    }

    public void startWatching() {
        if (this.m_descriptor.intValue() < 0) {
            this.m_descriptor = Integer.valueOf(s_observerThread.startWatching(this.m_path, this.m_mask, this));
        }
    }

    public void stopWatching() {
        if (this.m_descriptor.intValue() >= 0) {
            s_observerThread.stopWatching(this.m_descriptor.intValue());
            this.m_descriptor = Integer.valueOf(-1);
        }
    }
}
