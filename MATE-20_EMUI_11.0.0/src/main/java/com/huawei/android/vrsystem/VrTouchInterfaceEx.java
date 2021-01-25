package com.huawei.android.vrsystem;

import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.InputEvent;
import android.vrsystem.VrTouchInterface;
import com.huawei.android.view.InputChannelEx;
import com.huawei.android.view.InputEventReceiverEx;

public class VrTouchInterfaceEx {
    private static final Object LOCK = new Object();
    public static final int POWER_MODE_DOZE = 1;
    public static final int POWER_MODE_NORMAL = 2;
    public static final int POWER_MODE_OFF = 0;
    private static final String TAG = "VrTouchInterfaceEx";
    private static VrTouchInterfaceEx sInstance;
    private VrInputEventReceiver mEventReceiver;
    private VrInputEventCallback mInputEventCallback;
    private final Object mLockObject = new Object();
    private VrTouchInterface mVrTouchInterface = new VrTouchInterface();

    public interface VrInputEventCallback {
        boolean onInputEvent(InputEvent inputEvent);
    }

    private VrTouchInterfaceEx() {
    }

    public static VrTouchInterfaceEx getInstance() {
        VrTouchInterfaceEx vrTouchInterfaceEx;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new VrTouchInterfaceEx();
            }
            vrTouchInterfaceEx = sInstance;
        }
        return vrTouchInterfaceEx;
    }

    public void registerInputEventCallback(IBinder binder, Looper looper, VrInputEventCallback callback) {
        synchronized (this.mLockObject) {
            if (binder == null || looper == null || callback == null) {
                Log.d(TAG, "params is invalid in registerInputEventCallback.");
                return;
            }
            this.mInputEventCallback = callback;
            InputChannelEx channel = this.mVrTouchInterface.registerInputEventCallback(binder);
            if (this.mEventReceiver == null && channel != null) {
                this.mEventReceiver = new VrInputEventReceiver(channel, looper);
            }
        }
    }

    public void unregisterInputEventCallback() {
        synchronized (this.mLockObject) {
            if (this.mVrTouchInterface != null) {
                this.mVrTouchInterface.unregisterInputEventCallback();
            }
            if (this.mEventReceiver != null) {
                this.mEventReceiver.dispose();
                this.mEventReceiver = null;
            }
        }
    }

    public void setDefaultDisplayPowerMode(int powerMode) {
        VrTouchInterface vrTouchInterface = this.mVrTouchInterface;
        if (vrTouchInterface != null) {
            vrTouchInterface.setDefaultDisplayPowerMode(powerMode);
        }
    }

    private final class VrInputEventReceiver extends InputEventReceiverEx {
        public VrInputEventReceiver(InputChannelEx channel, Looper looper) {
            super(channel, looper);
        }

        public void onInputEvent(InputEvent event) {
            if (VrTouchInterfaceEx.this.mInputEventCallback != null && event != null) {
                finishInputEvent(event, VrTouchInterfaceEx.this.mInputEventCallback.onInputEvent(event));
            }
        }
    }
}
