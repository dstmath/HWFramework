package android.net;

import android.os.HandlerThread;
import android.os.Looper;

public final class ConnectivityThread extends HandlerThread {
    private static ConnectivityThread sInstance;

    private ConnectivityThread() {
        super("ConnectivityThread");
    }

    private static synchronized ConnectivityThread getInstance() {
        ConnectivityThread connectivityThread;
        synchronized (ConnectivityThread.class) {
            if (sInstance == null) {
                sInstance = new ConnectivityThread();
                sInstance.start();
            }
            connectivityThread = sInstance;
        }
        return connectivityThread;
    }

    public static ConnectivityThread get() {
        return getInstance();
    }

    public static Looper getInstanceLooper() {
        return getInstance().getLooper();
    }
}
