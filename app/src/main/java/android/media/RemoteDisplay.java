package android.media;

import android.os.Handler;
import android.view.Surface;
import dalvik.system.CloseGuard;

public final class RemoteDisplay {
    public static final int DISPLAY_ERROR_CONNECTION_DROPPED = 2;
    public static final int DISPLAY_ERROR_UNKOWN = 1;
    public static final int DISPLAY_FLAG_SECURE = 1;
    private final CloseGuard mGuard;
    private final Handler mHandler;
    private final Listener mListener;
    private final String mOpPackageName;
    private long mPtr;

    /* renamed from: android.media.RemoteDisplay.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ int val$flags;
        final /* synthetic */ int val$height;
        final /* synthetic */ int val$session;
        final /* synthetic */ Surface val$surface;
        final /* synthetic */ int val$width;

        AnonymousClass1(Surface val$surface, int val$width, int val$height, int val$flags, int val$session) {
            this.val$surface = val$surface;
            this.val$width = val$width;
            this.val$height = val$height;
            this.val$flags = val$flags;
            this.val$session = val$session;
        }

        public void run() {
            RemoteDisplay.this.mListener.onDisplayConnected(this.val$surface, this.val$width, this.val$height, this.val$flags, this.val$session);
        }
    }

    /* renamed from: android.media.RemoteDisplay.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ int val$error;

        AnonymousClass3(int val$error) {
            this.val$error = val$error;
        }

        public void run() {
            RemoteDisplay.this.mListener.onDisplayError(this.val$error);
        }
    }

    public interface Listener {
        void onDisplayConnected(Surface surface, int i, int i2, int i3, int i4);

        void onDisplayDisconnected();

        void onDisplayError(int i);
    }

    private native void nativeDispose(long j);

    private native long nativeListen(String str, String str2);

    private native void nativePause(long j);

    private native void nativeResume(long j);

    private RemoteDisplay(Listener listener, Handler handler, String opPackageName) {
        this.mGuard = CloseGuard.get();
        this.mListener = listener;
        this.mHandler = handler;
        this.mOpPackageName = opPackageName;
    }

    protected void finalize() throws Throwable {
        try {
            dispose(true);
        } finally {
            super.finalize();
        }
    }

    public static RemoteDisplay listen(String iface, Listener listener, Handler handler, String opPackageName) {
        if (iface == null) {
            throw new IllegalArgumentException("iface must not be null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        } else if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        } else {
            RemoteDisplay display = new RemoteDisplay(listener, handler, opPackageName);
            display.startListening(iface);
            return display;
        }
    }

    public void dispose() {
        dispose(false);
    }

    public void pause() {
        nativePause(this.mPtr);
    }

    public void resume() {
        nativeResume(this.mPtr);
    }

    private void dispose(boolean finalized) {
        if (this.mPtr != 0) {
            if (this.mGuard != null) {
                if (finalized) {
                    this.mGuard.warnIfOpen();
                } else {
                    this.mGuard.close();
                }
            }
            nativeDispose(this.mPtr);
            this.mPtr = 0;
        }
    }

    private void startListening(String iface) {
        this.mPtr = nativeListen(iface, this.mOpPackageName);
        if (this.mPtr == 0) {
            throw new IllegalStateException("Could not start listening for remote display connection on \"" + iface + "\"");
        }
        this.mGuard.open("dispose");
    }

    private void notifyDisplayConnected(Surface surface, int width, int height, int flags, int session) {
        this.mHandler.post(new AnonymousClass1(surface, width, height, flags, session));
    }

    private void notifyDisplayDisconnected() {
        this.mHandler.post(new Runnable() {
            public void run() {
                RemoteDisplay.this.mListener.onDisplayDisconnected();
            }
        });
    }

    private void notifyDisplayError(int error) {
        this.mHandler.post(new AnonymousClass3(error));
    }
}
