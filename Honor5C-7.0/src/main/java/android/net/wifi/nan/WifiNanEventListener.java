package android.net.wifi.nan;

import android.net.wifi.nan.IWifiNanEventListener.Stub;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class WifiNanEventListener {
    private static final boolean DBG = false;
    public static final int LISTEN_CONFIG_COMPLETED = 1;
    public static final int LISTEN_CONFIG_FAILED = 2;
    public static final int LISTEN_IDENTITY_CHANGED = 8;
    public static final int LISTEN_NAN_DOWN = 4;
    private static final String TAG = "WifiNanEventListener";
    private static final boolean VDBG = false;
    public IWifiNanEventListener callback;
    private final Handler mHandler;

    /* renamed from: android.net.wifi.nan.WifiNanEventListener.2 */
    class AnonymousClass2 extends Handler {
        AnonymousClass2(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiNanEventListener.LISTEN_CONFIG_COMPLETED /*1*/:
                    WifiNanEventListener.this.onConfigCompleted((ConfigRequest) msg.obj);
                case WifiNanEventListener.LISTEN_CONFIG_FAILED /*2*/:
                    WifiNanEventListener.this.onConfigFailed((ConfigRequest) msg.obj, msg.arg1);
                case WifiNanEventListener.LISTEN_NAN_DOWN /*4*/:
                    WifiNanEventListener.this.onNanDown(msg.arg1);
                case WifiNanEventListener.LISTEN_IDENTITY_CHANGED /*8*/:
                    WifiNanEventListener.this.onIdentityChanged();
                default:
            }
        }
    }

    public WifiNanEventListener() {
        this(Looper.myLooper());
    }

    public WifiNanEventListener(Looper looper) {
        this.callback = new Stub() {
            public void onConfigCompleted(ConfigRequest completedConfig) {
                Message msg = WifiNanEventListener.this.mHandler.obtainMessage(WifiNanEventListener.LISTEN_CONFIG_COMPLETED);
                msg.obj = completedConfig;
                WifiNanEventListener.this.mHandler.sendMessage(msg);
            }

            public void onConfigFailed(ConfigRequest failedConfig, int reason) {
                Message msg = WifiNanEventListener.this.mHandler.obtainMessage(WifiNanEventListener.LISTEN_CONFIG_FAILED);
                msg.arg1 = reason;
                msg.obj = failedConfig;
                WifiNanEventListener.this.mHandler.sendMessage(msg);
            }

            public void onNanDown(int reason) {
                Message msg = WifiNanEventListener.this.mHandler.obtainMessage(WifiNanEventListener.LISTEN_NAN_DOWN);
                msg.arg1 = reason;
                WifiNanEventListener.this.mHandler.sendMessage(msg);
            }

            public void onIdentityChanged() {
                WifiNanEventListener.this.mHandler.sendMessage(WifiNanEventListener.this.mHandler.obtainMessage(WifiNanEventListener.LISTEN_IDENTITY_CHANGED));
            }
        };
        this.mHandler = new AnonymousClass2(looper);
    }

    public void onConfigCompleted(ConfigRequest completedConfig) {
        Log.w(TAG, "onConfigCompleted: called in stub - override if interested or disable");
    }

    public void onConfigFailed(ConfigRequest failedConfig, int reason) {
        Log.w(TAG, "onConfigFailed: called in stub - override if interested or disable");
    }

    public void onNanDown(int reason) {
        Log.w(TAG, "onNanDown: called in stub - override if interested or disable");
    }

    public void onIdentityChanged() {
    }
}
