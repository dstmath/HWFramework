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
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SomeArgs args = msg.obj;
                    try {
                        CallScreeningService.this.mCallScreeningAdapter = (ICallScreeningAdapter) args.arg1;
                        CallScreeningService.this.onScreenCall(Details.createFromParcelableCall((ParcelableCall) args.arg2));
                        return;
                    } finally {
                        args.recycle();
                    }
                default:
                    return;
            }
        }
    };

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
                return new CallResponse(this.mShouldDisallowCall, this.mShouldRejectCall, this.mShouldSkipCallLog, this.mShouldSkipNotification, null);
            }
        }

        /* synthetic */ CallResponse(boolean shouldDisallowCall, boolean shouldRejectCall, boolean shouldSkipCallLog, boolean shouldSkipNotification, CallResponse -this4) {
            this(shouldDisallowCall, shouldRejectCall, shouldSkipCallLog, shouldSkipNotification);
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
        /* synthetic */ CallScreeningBinder(CallScreeningService this$0, CallScreeningBinder -this1) {
            this();
        }

        private CallScreeningBinder() {
        }

        public void screenCall(ICallScreeningAdapter adapter, ParcelableCall call) {
            Log.v((Object) this, "screenCall", new Object[0]);
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = adapter;
            args.arg2 = call;
            CallScreeningService.this.mHandler.obtainMessage(1, args).sendToTarget();
        }
    }

    public abstract void onScreenCall(Details details);

    public IBinder onBind(Intent intent) {
        Log.v((Object) this, "onBind", new Object[0]);
        return new CallScreeningBinder(this, null);
    }

    public boolean onUnbind(Intent intent) {
        Log.v((Object) this, "onUnbind", new Object[0]);
        return false;
    }

    public final void respondToCall(Details callDetails, CallResponse response) {
        try {
            if (response.getDisallowCall()) {
                this.mCallScreeningAdapter.disallowCall(callDetails.getTelecomCallId(), response.getRejectCall(), response.getSkipCallLog() ^ 1, response.getSkipNotification() ^ 1);
            } else {
                this.mCallScreeningAdapter.allowCall(callDetails.getTelecomCallId());
            }
        } catch (RemoteException e) {
        }
    }
}
