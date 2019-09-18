package android.net;

import android.os.HandlerThread;
import android.os.Looper;

public final class ConnectivityThread extends HandlerThread {

    private static class Singleton {
        /* access modifiers changed from: private */
        public static final ConnectivityThread INSTANCE = ConnectivityThread.createInstance();

        private Singleton() {
        }
    }

    private ConnectivityThread() {
        super("ConnectivityThread");
    }

    /* access modifiers changed from: private */
    public static ConnectivityThread createInstance() {
        ConnectivityThread t = new ConnectivityThread();
        t.start();
        return t;
    }

    public static ConnectivityThread get() {
        return Singleton.INSTANCE;
    }

    public static Looper getInstanceLooper() {
        return Singleton.INSTANCE.getLooper();
    }
}
