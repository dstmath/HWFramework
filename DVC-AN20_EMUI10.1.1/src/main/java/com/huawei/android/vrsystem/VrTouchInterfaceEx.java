package com.huawei.android.vrsystem;

import android.os.IBinder;
import android.os.Looper;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.vrsystem.VrTouchInterface;

public class VrTouchInterfaceEx {
    private static final Object LOCK = new Object();
    public static final int POWER_MODE_DOZE = 1;
    public static final int POWER_MODE_NORMAL = 2;
    public static final int POWER_MODE_OFF = 0;
    private static VrTouchInterfaceEx sInstance;
    private VrInputEventReceiver mEventReceiver;
    private VrInputEventCallback mInputEventCallback;
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

    public synchronized void registerInputEventCallback(IBinder binder, Looper looper, VrInputEventCallback callback) {
        this.mInputEventCallback = callback;
        InputChannel channel = this.mVrTouchInterface.registerInputEventCallback(binder);
        if (!(this.mEventReceiver != null || channel == null || looper == null)) {
            this.mEventReceiver = new VrInputEventReceiver(channel, looper);
        }
    }

    public synchronized void unregisterInputEventCallback() {
        this.mVrTouchInterface.unregisterInputEventCallback();
        if (this.mEventReceiver != null) {
            this.mEventReceiver.dispose();
            this.mEventReceiver = null;
        }
    }

    public void setDefaultDisplayPowerMode(int powerMode) {
        this.mVrTouchInterface.setDefaultDisplayPowerMode(powerMode);
    }

    private final class VrInputEventReceiver extends InputEventReceiver {
        public VrInputEventReceiver(InputChannel channel, Looper looper) {
            super(channel, looper);
        }

        public void onInputEvent(InputEvent event) {
            if (VrTouchInterfaceEx.this.mInputEventCallback != null && event != null) {
                finishInputEvent(event, VrTouchInterfaceEx.this.mInputEventCallback.onInputEvent(event));
            }
        }
    }
}
