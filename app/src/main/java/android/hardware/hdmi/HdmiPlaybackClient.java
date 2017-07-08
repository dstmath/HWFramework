package android.hardware.hdmi;

import android.hardware.hdmi.IHdmiControlCallback.Stub;
import android.os.RemoteException;
import android.util.Log;

public final class HdmiPlaybackClient extends HdmiClient {
    private static final int ADDR_TV = 0;
    private static final String TAG = "HdmiPlaybackClient";

    /* renamed from: android.hardware.hdmi.HdmiPlaybackClient.1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ OneTouchPlayCallback val$callback;

        AnonymousClass1(OneTouchPlayCallback val$callback) {
            this.val$callback = val$callback;
        }

        public void onComplete(int result) {
            this.val$callback.onComplete(result);
        }
    }

    /* renamed from: android.hardware.hdmi.HdmiPlaybackClient.2 */
    class AnonymousClass2 extends Stub {
        final /* synthetic */ DisplayStatusCallback val$callback;

        AnonymousClass2(DisplayStatusCallback val$callback) {
            this.val$callback = val$callback;
        }

        public void onComplete(int status) {
            this.val$callback.onComplete(status);
        }
    }

    public interface DisplayStatusCallback {
        void onComplete(int i);
    }

    public interface OneTouchPlayCallback {
        void onComplete(int i);
    }

    HdmiPlaybackClient(IHdmiControlService service) {
        super(service);
    }

    public void oneTouchPlay(OneTouchPlayCallback callback) {
        try {
            this.mService.oneTouchPlay(getCallbackWrapper(callback));
        } catch (RemoteException e) {
            Log.e(TAG, "oneTouchPlay threw exception ", e);
        }
    }

    public int getDeviceType() {
        return 4;
    }

    public void queryDisplayStatus(DisplayStatusCallback callback) {
        try {
            this.mService.queryDisplayStatus(getCallbackWrapper(callback));
        } catch (RemoteException e) {
            Log.e(TAG, "queryDisplayStatus threw exception ", e);
        }
    }

    public void sendStandby() {
        try {
            this.mService.sendStandby(getDeviceType(), HdmiDeviceInfo.idForCecDevice(ADDR_TV));
        } catch (RemoteException e) {
            Log.e(TAG, "sendStandby threw exception ", e);
        }
    }

    private IHdmiControlCallback getCallbackWrapper(OneTouchPlayCallback callback) {
        return new AnonymousClass1(callback);
    }

    private IHdmiControlCallback getCallbackWrapper(DisplayStatusCallback callback) {
        return new AnonymousClass2(callback);
    }
}
