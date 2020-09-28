package android.os;

import android.annotation.UnsupportedAppUsage;
import android.util.Log;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    @UnsupportedAppUsage
    private static ObserverThread s_observerThread = new ObserverThread();
    private int[] mDescriptors;
    private final List<File> mFiles;
    private final int mMask;

    @Retention(RetentionPolicy.SOURCE)
    public @interface NotifyEventType {
    }

    public abstract void onEvent(int i, String str);

    /* access modifiers changed from: private */
    public static class ObserverThread extends Thread {
        private int m_fd = init();
        private HashMap<Integer, WeakReference> m_observers = new HashMap<>();

        private native int init();

        private native void observe(int i);

        private native void startWatching(int i, String[] strArr, int i2, int[] iArr);

        private native void stopWatching(int i, int[] iArr);

        public ObserverThread() {
            super(FileObserver.LOG_TAG);
        }

        public void run() {
            observe(this.m_fd);
        }

        /* JADX INFO: Multiple debug info for r2v2 int[]: [D('i' int), D('wfds' int[])] */
        public int[] startWatching(List<File> files, int mask, FileObserver observer) {
            int count = files.size();
            String[] paths = new String[count];
            for (int i = 0; i < count; i++) {
                paths[i] = files.get(i).getAbsolutePath();
            }
            int[] wfds = new int[count];
            Arrays.fill(wfds, -1);
            startWatching(this.m_fd, paths, mask, wfds);
            WeakReference<FileObserver> fileObserverWeakReference = new WeakReference<>(observer);
            synchronized (this.m_observers) {
                for (int wfd : wfds) {
                    if (wfd >= 0) {
                        this.m_observers.put(Integer.valueOf(wfd), fileObserverWeakReference);
                    }
                }
            }
            return wfds;
        }

        public void stopWatching(int[] descriptors) {
            stopWatching(this.m_fd, descriptors);
        }

        @UnsupportedAppUsage
        public void onEvent(int wfd, int mask, String path) {
            FileObserver observer = null;
            synchronized (this.m_observers) {
                WeakReference weak = this.m_observers.get(Integer.valueOf(wfd));
                if (weak != null && (observer = (FileObserver) weak.get()) == null) {
                    this.m_observers.remove(Integer.valueOf(wfd));
                }
            }
            if (observer != null) {
                try {
                    observer.onEvent(mask, path);
                } catch (Throwable throwable) {
                    Log.wtf(FileObserver.LOG_TAG, "Unhandled exception in FileObserver " + observer, throwable);
                }
            }
        }
    }

    static {
        s_observerThread.start();
    }

    @Deprecated
    public FileObserver(String path) {
        this(new File(path));
    }

    public FileObserver(File file) {
        this(Arrays.asList(file));
    }

    public FileObserver(List<File> files) {
        this(files, 4095);
    }

    @Deprecated
    public FileObserver(String path, int mask) {
        this(new File(path), mask);
    }

    public FileObserver(File file, int mask) {
        this(Arrays.asList(file), mask);
    }

    public FileObserver(List<File> files, int mask) {
        this.mFiles = files;
        this.mMask = mask;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        stopWatching();
    }

    public void startWatching() {
        if (this.mDescriptors == null) {
            this.mDescriptors = s_observerThread.startWatching(this.mFiles, this.mMask, this);
        }
    }

    public void stopWatching() {
        int[] iArr = this.mDescriptors;
        if (iArr != null) {
            s_observerThread.stopWatching(iArr);
            this.mDescriptors = null;
        }
    }
}
