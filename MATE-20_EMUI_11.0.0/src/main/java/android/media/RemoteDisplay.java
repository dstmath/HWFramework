package android.media;

import android.annotation.UnsupportedAppUsage;
import android.os.Handler;
import android.view.Surface;
import dalvik.system.CloseGuard;

public final class RemoteDisplay {
    public static final int DISPLAY_ERROR_CONNECTION_DROPPED = 2;
    public static final int DISPLAY_ERROR_UIBC_ERROR = 30;
    public static final int DISPLAY_ERROR_UNKOWN = 1;
    public static final int DISPLAY_FLAG_PEER_HDCP_SUPPORT = 256;
    public static final int DISPLAY_FLAG_SECURE = 1;
    public static final int DISPLAY_WARING_PLAY_ACTION = 50;
    private final CloseGuard mGuard = CloseGuard.get();
    private final Handler mHandler;
    private final Listener mListener;
    private final String mOpPackageName;
    private long mPtr;

    public interface Listener {
        int notifyUibcCreate(int i);

        void onDisplayConnected(Surface surface, int i, int i2, int i3, int i4);

        void onDisplayDataNotify(String str);

        void onDisplayDisconnected();

        void onDisplayError(int i);
    }

    private native void nativeCheckVerificationResult(long j, boolean z);

    private native void nativeDispose(long j);

    private native long nativeListen(String str, String str2);

    private native void nativePause(long j);

    private native void nativeResume(long j);

    private native void nativeSendWifiDisplayAction(long j, String str);

    private native void nativeSetDisplayParameters(long j, String str);

    private native void nativeSetVideoBitrate(long j, int i);

    private RemoteDisplay(Listener listener, Handler handler, String opPackageName) {
        this.mListener = listener;
        this.mHandler = handler;
        this.mOpPackageName = opPackageName;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
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
        } else if (handler != null) {
            RemoteDisplay display = new RemoteDisplay(listener, handler, opPackageName);
            display.startListening(iface);
            return display;
        } else {
            throw new IllegalArgumentException("handler must not be null");
        }
    }

    @UnsupportedAppUsage
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
            CloseGuard closeGuard = this.mGuard;
            if (closeGuard != null) {
                if (finalized) {
                    closeGuard.warnIfOpen();
                } else {
                    closeGuard.close();
                }
            }
            nativeDispose(this.mPtr);
            this.mPtr = 0;
        }
    }

    private void startListening(String iface) {
        this.mPtr = nativeListen(iface, this.mOpPackageName);
        if (this.mPtr != 0) {
            this.mGuard.open("dispose");
            return;
        }
        throw new IllegalStateException("Could not start listening for remote display connection on \"" + iface + "\"");
    }

    @UnsupportedAppUsage
    private void notifyDisplayConnected(final Surface surface, final int width, final int height, final int flags, final int session) {
        this.mHandler.post(new Runnable() {
            /* class android.media.RemoteDisplay.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                RemoteDisplay.this.mListener.onDisplayConnected(surface, width, height, flags, session);
            }
        });
    }

    @UnsupportedAppUsage
    private void notifyDisplayDisconnected() {
        this.mHandler.post(new Runnable() {
            /* class android.media.RemoteDisplay.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                RemoteDisplay.this.mListener.onDisplayDisconnected();
            }
        });
    }

    @UnsupportedAppUsage
    private void notifyDisplayError(final int error) {
        this.mHandler.post(new Runnable() {
            /* class android.media.RemoteDisplay.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                RemoteDisplay.this.mListener.onDisplayError(error);
            }
        });
    }

    @UnsupportedAppUsage
    private void notifyDisplayData(final String inputDataType) {
        this.mHandler.post(new Runnable() {
            /* class android.media.RemoteDisplay.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                RemoteDisplay.this.mListener.onDisplayDataNotify(inputDataType);
            }
        });
    }

    public void setVideoBitrate(int bitrate) {
        nativeSetVideoBitrate(this.mPtr, bitrate);
    }

    public void setDisplayParameters(String params) {
        nativeSetDisplayParameters(this.mPtr, params);
    }

    public void checkVerificationResult(boolean isRight) {
        nativeCheckVerificationResult(this.mPtr, isRight);
    }

    public void sendWifiDisplayAction(String action) {
        nativeSendWifiDisplayAction(this.mPtr, action);
    }

    private int notifyUibcCreate(int capSupport) {
        return this.mListener.notifyUibcCreate(capSupport);
    }
}
