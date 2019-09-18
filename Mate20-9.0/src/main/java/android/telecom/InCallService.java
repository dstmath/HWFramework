package android.telecom;

import android.annotation.SystemApi;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telecom.Phone;
import android.telecom.VideoProfile;
import android.view.Surface;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IInCallAdapter;
import com.android.internal.telecom.IInCallService;
import java.util.Collections;
import java.util.List;

public abstract class InCallService extends Service {
    private static final int MSG_ADD_CALL = 2;
    private static final int MSG_BRING_TO_FOREGROUND = 6;
    private static final int MSG_ON_CALL_AUDIO_STATE_CHANGED = 5;
    private static final int MSG_ON_CAN_ADD_CALL_CHANGED = 7;
    private static final int MSG_ON_CONNECTION_EVENT = 9;
    private static final int MSG_ON_HANDOVER_COMPLETE = 13;
    private static final int MSG_ON_HANDOVER_FAILED = 12;
    private static final int MSG_ON_RTT_INITIATION_FAILURE = 11;
    private static final int MSG_ON_RTT_UPGRADE_REQUEST = 10;
    private static final int MSG_SET_IN_CALL_ADAPTER = 1;
    private static final int MSG_SET_POST_DIAL_WAIT = 4;
    private static final int MSG_SILENCE_RINGER = 8;
    private static final int MSG_UPDATE_CALL = 3;
    public static final String SERVICE_INTERFACE = "android.telecom.InCallService";
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            boolean z = true;
            if (InCallService.this.mPhone != null || msg.what == 1) {
                switch (msg.what) {
                    case 1:
                        Phone unused = InCallService.this.mPhone = new Phone(new InCallAdapter((IInCallAdapter) msg.obj), InCallService.this.getApplicationContext().getOpPackageName(), InCallService.this.getApplicationContext().getApplicationInfo().targetSdkVersion);
                        InCallService.this.mPhone.addListener(InCallService.this.mPhoneListener);
                        InCallService.this.onPhoneCreated(InCallService.this.mPhone);
                        break;
                    case 2:
                        InCallService.this.mPhone.internalAddCall((ParcelableCall) msg.obj);
                        break;
                    case 3:
                        InCallService.this.mPhone.internalUpdateCall((ParcelableCall) msg.obj);
                        break;
                    case 4:
                        SomeArgs args = (SomeArgs) msg.obj;
                        try {
                            InCallService.this.mPhone.internalSetPostDialWait((String) args.arg1, (String) args.arg2);
                            break;
                        } finally {
                            args.recycle();
                        }
                    case 5:
                        InCallService.this.mPhone.internalCallAudioStateChanged((CallAudioState) msg.obj);
                        break;
                    case 6:
                        Phone access$000 = InCallService.this.mPhone;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        access$000.internalBringToForeground(z);
                        break;
                    case 7:
                        Phone access$0002 = InCallService.this.mPhone;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        access$0002.internalSetCanAddCall(z);
                        break;
                    case 8:
                        InCallService.this.mPhone.internalSilenceRinger();
                        break;
                    case 9:
                        SomeArgs args2 = (SomeArgs) msg.obj;
                        try {
                            InCallService.this.mPhone.internalOnConnectionEvent((String) args2.arg1, (String) args2.arg2, (Bundle) args2.arg3);
                            break;
                        } finally {
                            args2.recycle();
                        }
                    case 10:
                        InCallService.this.mPhone.internalOnRttUpgradeRequest((String) msg.obj, msg.arg1);
                        break;
                    case 11:
                        InCallService.this.mPhone.internalOnRttInitiationFailure((String) msg.obj, msg.arg1);
                        break;
                    case 12:
                        InCallService.this.mPhone.internalOnHandoverFailed((String) msg.obj, msg.arg1);
                        break;
                    case 13:
                        InCallService.this.mPhone.internalOnHandoverComplete((String) msg.obj);
                        break;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Phone mPhone;
    /* access modifiers changed from: private */
    public Phone.Listener mPhoneListener = new Phone.Listener() {
        public void onAudioStateChanged(Phone phone, AudioState audioState) {
            InCallService.this.onAudioStateChanged(audioState);
        }

        public void onCallAudioStateChanged(Phone phone, CallAudioState callAudioState) {
            InCallService.this.onCallAudioStateChanged(callAudioState);
        }

        public void onBringToForeground(Phone phone, boolean showDialpad) {
            InCallService.this.onBringToForeground(showDialpad);
        }

        public void onCallAdded(Phone phone, Call call) {
            InCallService.this.onCallAdded(call);
        }

        public void onCallRemoved(Phone phone, Call call) {
            InCallService.this.onCallRemoved(call);
        }

        public void onCanAddCallChanged(Phone phone, boolean canAddCall) {
            InCallService.this.onCanAddCallChanged(canAddCall);
        }

        public void onSilenceRinger(Phone phone) {
            InCallService.this.onSilenceRinger();
        }
    };

    private final class InCallServiceBinder extends IInCallService.Stub {
        private InCallServiceBinder() {
        }

        public void setInCallAdapter(IInCallAdapter inCallAdapter) {
            InCallService.this.mHandler.obtainMessage(1, inCallAdapter).sendToTarget();
        }

        public void addCall(ParcelableCall call) {
            InCallService.this.mHandler.obtainMessage(2, call).sendToTarget();
        }

        public void updateCall(ParcelableCall call) {
            InCallService.this.mHandler.obtainMessage(3, call).sendToTarget();
        }

        public void setPostDial(String callId, String remaining) {
        }

        public void setPostDialWait(String callId, String remaining) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = remaining;
            InCallService.this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        public void onCallAudioStateChanged(CallAudioState callAudioState) {
            InCallService.this.mHandler.obtainMessage(5, callAudioState).sendToTarget();
        }

        public void bringToForeground(boolean showDialpad) {
            InCallService.this.mHandler.obtainMessage(6, showDialpad, 0).sendToTarget();
        }

        public void onCanAddCallChanged(boolean canAddCall) {
            InCallService.this.mHandler.obtainMessage(7, canAddCall, 0).sendToTarget();
        }

        public void silenceRinger() {
            InCallService.this.mHandler.obtainMessage(8).sendToTarget();
        }

        public void onConnectionEvent(String callId, String event, Bundle extras) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = event;
            args.arg3 = extras;
            InCallService.this.mHandler.obtainMessage(9, args).sendToTarget();
        }

        public void onRttUpgradeRequest(String callId, int id) {
            InCallService.this.mHandler.obtainMessage(10, id, 0, callId).sendToTarget();
        }

        public void onRttInitiationFailure(String callId, int reason) {
            InCallService.this.mHandler.obtainMessage(11, reason, 0, callId).sendToTarget();
        }

        public void onHandoverFailed(String callId, int error) {
            InCallService.this.mHandler.obtainMessage(12, error, 0, callId).sendToTarget();
        }

        public void onHandoverComplete(String callId) {
            InCallService.this.mHandler.obtainMessage(13, callId).sendToTarget();
        }
    }

    public static abstract class VideoCall {

        public static abstract class Callback {
            public abstract void onCallDataUsageChanged(long j);

            public abstract void onCallSessionEvent(int i);

            public abstract void onCameraCapabilitiesChanged(VideoProfile.CameraCapabilities cameraCapabilities);

            public abstract void onPeerDimensionsChanged(int i, int i2);

            public abstract void onSessionModifyRequestReceived(VideoProfile videoProfile);

            public abstract void onSessionModifyResponseReceived(int i, VideoProfile videoProfile, VideoProfile videoProfile2);

            public abstract void onVideoQualityChanged(int i);
        }

        public abstract void destroy();

        public abstract void registerCallback(Callback callback);

        public abstract void registerCallback(Callback callback, Handler handler);

        public abstract void requestCallDataUsage();

        public abstract void requestCameraCapabilities();

        public abstract void sendSessionModifyRequest(VideoProfile videoProfile);

        public abstract void sendSessionModifyResponse(VideoProfile videoProfile);

        public abstract void setCamera(String str);

        public abstract void setDeviceOrientation(int i);

        public abstract void setDisplaySurface(Surface surface);

        public abstract void setPauseImage(Uri uri);

        public abstract void setPreviewSurface(Surface surface);

        public abstract void setZoom(float f);

        public abstract void unregisterCallback(Callback callback);
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [android.telecom.InCallService$InCallServiceBinder, android.os.IBinder] */
    public IBinder onBind(Intent intent) {
        return new InCallServiceBinder();
    }

    public boolean onUnbind(Intent intent) {
        if (this.mPhone != null) {
            Phone oldPhone = this.mPhone;
            this.mPhone = null;
            oldPhone.destroy();
            oldPhone.removeListener(this.mPhoneListener);
            onPhoneDestroyed(oldPhone);
        }
        return false;
    }

    @SystemApi
    @Deprecated
    public Phone getPhone() {
        return this.mPhone;
    }

    public final List<Call> getCalls() {
        return this.mPhone == null ? Collections.emptyList() : this.mPhone.getCalls();
    }

    public final boolean canAddCall() {
        if (this.mPhone == null) {
            return false;
        }
        return this.mPhone.canAddCall();
    }

    @Deprecated
    public final AudioState getAudioState() {
        if (this.mPhone == null) {
            return null;
        }
        return this.mPhone.getAudioState();
    }

    public final CallAudioState getCallAudioState() {
        if (this.mPhone == null) {
            return null;
        }
        return this.mPhone.getCallAudioState();
    }

    public final void setMuted(boolean state) {
        if (this.mPhone != null) {
            this.mPhone.setMuted(state);
        }
    }

    public final void setAudioRoute(int route) {
        if (this.mPhone != null) {
            this.mPhone.setAudioRoute(route);
        }
    }

    public final void setBluetoothAudioRoute(String address) {
        if (this.mPhone != null) {
            this.mPhone.setBluetoothAudioRoute(address);
        }
    }

    public final void requestBluetoothAudio(BluetoothDevice bluetoothDevice) {
        if (this.mPhone != null) {
            this.mPhone.requestBluetoothAudio(bluetoothDevice.getAddress());
        }
    }

    @SystemApi
    @Deprecated
    public void onPhoneCreated(Phone phone) {
    }

    @SystemApi
    @Deprecated
    public void onPhoneDestroyed(Phone phone) {
    }

    @Deprecated
    public void onAudioStateChanged(AudioState audioState) {
    }

    public void onCallAudioStateChanged(CallAudioState audioState) {
    }

    public void onBringToForeground(boolean showDialpad) {
    }

    public void onCallAdded(Call call) {
    }

    public void onCallRemoved(Call call) {
    }

    public void onCanAddCallChanged(boolean canAddCall) {
    }

    public void onSilenceRinger() {
    }

    public void onConnectionEvent(Call call, String event, Bundle extras) {
    }
}
