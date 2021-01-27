package android.vrsystem;

import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import com.huawei.android.view.InputChannelEx;
import com.huawei.android.view.InputConsumerEx;
import com.huawei.screenrecorder.activities.SurfaceControlEx;

public class VrTouchInterface {
    private static final String INPUT_CONSUMER_VRAR = "INPUT_CONSUMER_VRAR";
    private static final String TAG = "VrTouchInterface";
    private boolean mIsPointerListenerRegistered = false;
    private final Object mLock = new Object();

    public InputChannelEx registerInputEventCallback(IBinder binder) {
        synchronized (this.mLock) {
            if (Process.myUid() != 1000) {
                throw new SecurityException("System Uid Only");
            } else if (this.mIsPointerListenerRegistered) {
                return null;
            } else {
                InputChannelEx inputChannel = new InputChannelEx();
                Log.i(TAG, "registerInputEventListener: createInputConsumer");
                InputConsumerEx.destroyInputConsumer(INPUT_CONSUMER_VRAR, 0);
                InputConsumerEx.createInputConsumer(binder, INPUT_CONSUMER_VRAR, 0, inputChannel);
                this.mIsPointerListenerRegistered = true;
                return inputChannel;
            }
        }
    }

    public void unregisterInputEventCallback() {
        synchronized (this.mLock) {
            if (Process.myUid() != 1000) {
                throw new SecurityException("System Uid Only");
            } else if (this.mIsPointerListenerRegistered) {
                Log.i(TAG, "unregisterInputEventCallback: destroyInputConsumer");
                InputConsumerEx.destroyInputConsumer(INPUT_CONSUMER_VRAR, 0);
                this.mIsPointerListenerRegistered = false;
            }
        }
    }

    public void setDefaultDisplayPowerMode(int powerMode) {
        Log.i(TAG, "setDefaultDisplayPowerMode:" + powerMode);
        IBinder displayToken = SurfaceControlEx.getPhysicalDisplayToken(0);
        if (displayToken != null) {
            SurfaceControlEx.setDisplayPowerMode(displayToken, powerMode);
        }
    }
}
