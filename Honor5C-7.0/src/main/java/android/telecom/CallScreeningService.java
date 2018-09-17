package android.telecom;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.Call.Details;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.ICallScreeningAdapter;
import com.android.internal.telecom.ICallScreeningService.Stub;

public abstract class CallScreeningService extends Service {
    private static final int MSG_SCREEN_CALL = 1;
    public static final String SERVICE_INTERFACE = "android.telecom.CallScreeningService";
    private ICallScreeningAdapter mCallScreeningAdapter;
    private final Handler mHandler;

    /* renamed from: android.telecom.CallScreeningService.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CallScreeningService.MSG_SCREEN_CALL /*1*/:
                    SomeArgs args = msg.obj;
                    try {
                        CallScreeningService.this.mCallScreeningAdapter = (ICallScreeningAdapter) args.arg1;
                        CallScreeningService.this.onScreenCall(Details.createFromParcelableCall((ParcelableCall) args.arg2));
                    } finally {
                        args.recycle();
                    }
                default:
            }
        }
    }

    public static class CallResponse {
        private final boolean mShouldDisallowCall;
        private final boolean mShouldRejectCall;
        private final boolean mShouldSkipCallLog;
        private final boolean mShouldSkipNotification;

        public static class Builder {
            private boolean mShouldDisallowCall;
            private boolean mShouldRejectCall;
            private boolean mShouldSkipCallLog;
            private boolean mShouldSkipNotification;

            public Builder setDisallowCall(boolean shouldDisallowCall) {
                this.mShouldDisallowCall = shouldDisallowCall;
                return this;
            }

            public Builder setRejectCall(boolean shouldRejectCall) {
                this.mShouldRejectCall = shouldRejectCall;
                return this;
            }

            public Builder setSkipCallLog(boolean shouldSkipCallLog) {
                this.mShouldSkipCallLog = shouldSkipCallLog;
                return this;
            }

            public Builder setSkipNotification(boolean shouldSkipNotification) {
                this.mShouldSkipNotification = shouldSkipNotification;
                return this;
            }

            public CallResponse build() {
                return new CallResponse(this.mShouldRejectCall, this.mShouldSkipCallLog, this.mShouldSkipNotification, null);
            }
        }

        private CallResponse(boolean shouldDisallowCall, boolean shouldRejectCall, boolean shouldSkipCallLog, boolean shouldSkipNotification) {
            if (shouldDisallowCall || !(shouldRejectCall || shouldSkipCallLog || shouldSkipNotification)) {
                this.mShouldDisallowCall = shouldDisallowCall;
                this.mShouldRejectCall = shouldRejectCall;
                this.mShouldSkipCallLog = shouldSkipCallLog;
                this.mShouldSkipNotification = shouldSkipNotification;
                return;
            }
            throw new IllegalStateException("Invalid response state for allowed call.");
        }

        public boolean getDisallowCall() {
            return this.mShouldDisallowCall;
        }

        public boolean getRejectCall() {
            return this.mShouldRejectCall;
        }

        public boolean getSkipCallLog() {
            return this.mShouldSkipCallLog;
        }

        public boolean getSkipNotification() {
            return this.mShouldSkipNotification;
        }
    }

    private final class CallScreeningBinder extends Stub {
        private CallScreeningBinder() {
        }

        public void screenCall(ICallScreeningAdapter adapter, ParcelableCall call) {
            Log.v((Object) this, "screenCall", new Object[0]);
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = adapter;
            args.arg2 = call;
            CallScreeningService.this.mHandler.obtainMessage(CallScreeningService.MSG_SCREEN_CALL, args).sendToTarget();
        }
    }

    public abstract void onScreenCall(Details details);

    public CallScreeningService() {
        this.mHandler = new AnonymousClass1(Looper.getMainLooper());
    }

    public IBinder onBind(Intent intent) {
        Log.v((Object) this, "onBind", new Object[0]);
        return new CallScreeningBinder();
    }

    public boolean onUnbind(Intent intent) {
        Log.v((Object) this, "onUnbind", new Object[0]);
        return false;
    }

    public final void respondToCall(Details callDetails, CallResponse response) {
        boolean z = false;
        try {
            if (response.getDisallowCall()) {
                boolean z2;
                ICallScreeningAdapter iCallScreeningAdapter = this.mCallScreeningAdapter;
                String telecomCallId = callDetails.getTelecomCallId();
                boolean rejectCall = response.getRejectCall();
                if (response.getSkipCallLog()) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                if (!response.getSkipNotification()) {
                    z = true;
                }
                iCallScreeningAdapter.disallowCall(telecomCallId, rejectCall, z2, z);
                return;
            }
            this.mCallScreeningAdapter.allowCall(callDetails.getTelecomCallId());
        } catch (RemoteException e) {
        }
    }
}
