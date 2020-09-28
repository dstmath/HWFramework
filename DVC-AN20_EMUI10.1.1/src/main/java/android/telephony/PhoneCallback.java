package android.telephony;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.IPhoneCallback;
import com.huawei.android.telephony.RlogEx;
import java.lang.ref.WeakReference;

public class PhoneCallback {
    private static final int EVENT_RESPONSE_CALLBACK_1 = 1;
    private static final int EVENT_RESPONSE_CALLBACK_2 = 2;
    private static final int EVENT_RESPONSE_CALLBACK_3 = 3;
    private static final String LOG_TAG = "PhoneCallback";
    IPhoneCallbackStub mCallbackStub;
    private final Handler mHandler;

    public void onPhoneCallback1(int parm) {
    }

    public void onPhoneCallback2(int parm1, int param2) {
    }

    public void onPhoneCallback3(int parm1, int param2, Bundle param3) {
    }

    public PhoneCallback() {
        this.mCallbackStub = new IPhoneCallbackStub(this);
        this.mHandler = new MyHandler(Looper.myLooper());
    }

    public PhoneCallback(Looper looper) {
        this.mCallbackStub = new IPhoneCallbackStub(this);
        this.mHandler = new MyHandler(looper);
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            PhoneCallback phoneCallback = PhoneCallback.this;
            phoneCallback.log("handleMessage what= " + msg.what + " msg=" + msg);
            int i = msg.what;
            if (i == 1) {
                PhoneCallback.this.onPhoneCallback1(msg.arg1);
            } else if (i == 2) {
                PhoneCallback.this.onPhoneCallback2(msg.arg1, msg.arg2);
            } else if (i != 3) {
                PhoneCallback.this.log("handleMessage not handled message");
            } else {
                PhoneCallback.this.onPhoneCallback3(msg.arg1, msg.arg2, (Bundle) msg.obj);
            }
        }
    }

    /* access modifiers changed from: private */
    public class IPhoneCallbackStub extends IPhoneCallback.Stub {
        private WeakReference<PhoneCallback> mPhoneCallbackWeakRef;

        public IPhoneCallbackStub(PhoneCallback phoneCallback) {
            this.mPhoneCallbackWeakRef = new WeakReference<>(phoneCallback);
        }

        private void send(int what, int arg1, int arg2, Object obj) {
            PhoneCallback callback = this.mPhoneCallbackWeakRef.get();
            if (callback != null) {
                Message.obtain(callback.mHandler, what, arg1, arg2, obj).sendToTarget();
            }
        }

        public void onCallback1(int param) {
            send(1, param, 0, null);
        }

        public void onCallback2(int param1, int param2) {
            send(1, param1, param2, null);
        }

        public void onCallback3(int param1, int param2, Bundle param3) {
            send(1, param1, param2, param3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        RlogEx.i(LOG_TAG, s);
    }
}
