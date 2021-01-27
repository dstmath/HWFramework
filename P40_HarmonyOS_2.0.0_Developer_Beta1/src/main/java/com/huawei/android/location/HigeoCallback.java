package com.huawei.android.location;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.location.IHwHigeoCallback;
import java.lang.ref.WeakReference;

public class HigeoCallback {
    private static final int EVENT_CALLBACK_FOR_CELL_BATCHING = 2;
    private static final int EVENT_CALLBACK_FOR_CELL_FENCE = 4;
    private static final int EVENT_CALLBACK_FOR_GEO_FENCE = 5;
    private static final int EVENT_CALLBACK_FOR_HIGEO = 1;
    private static final int EVENT_CALLBACK_FOR_MM = 0;
    private static final int EVENT_CALLBACK_FOR_WIFI_FENCE = 3;
    private static final String TAG = "HigeoCallback";
    IHwHigeoCallbackStub mCallbackStub = new IHwHigeoCallbackStub(this);
    private final Handler mHandler = new MyHandler(Looper.myLooper());

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (msg.obj instanceof Bundle) {
                        HigeoCallback.this.onMmDataRequest(msg.arg1, (Bundle) msg.obj);
                        return;
                    }
                    return;
                case 1:
                    if (msg.obj instanceof Bundle) {
                        HigeoCallback.this.onHigeoEventCallback(msg.arg1, (Bundle) msg.obj);
                        return;
                    }
                    return;
                case 2:
                    if (msg.obj instanceof Bundle) {
                        HigeoCallback.this.onCellBatchingCallback(msg.arg1, (Bundle) msg.obj);
                        return;
                    }
                    return;
                case 3:
                    if (msg.obj instanceof Bundle) {
                        HigeoCallback.this.onWifiFenceCallback(msg.arg1, (Bundle) msg.obj);
                        return;
                    }
                    return;
                case 4:
                    if (msg.obj instanceof Bundle) {
                        HigeoCallback.this.onCellFenceCallback(msg.arg1, (Bundle) msg.obj);
                        return;
                    }
                    return;
                case 5:
                    if (msg.obj instanceof Bundle) {
                        HigeoCallback.this.onGeoFenceCallback(msg.arg1, (Bundle) msg.obj);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void onMmDataRequest(int type, Bundle bundle) {
    }

    public void onHigeoEventCallback(int type, Bundle bundle) {
    }

    public void onCellBatchingCallback(int type, Bundle bundle) {
    }

    public void onWifiFenceCallback(int type, Bundle bundle) {
    }

    /* access modifiers changed from: package-private */
    public void onCellFenceCallback(int type, Bundle bundle) {
    }

    /* access modifiers changed from: package-private */
    public void onGeoFenceCallback(int type, Bundle bundle) {
    }

    private class IHwHigeoCallbackStub extends IHwHigeoCallback.Stub {
        private WeakReference<HigeoCallback> mHigeoCallbackWeakRef;

        public IHwHigeoCallbackStub(HigeoCallback higeoCallback) {
            this.mHigeoCallbackWeakRef = new WeakReference<>(higeoCallback);
        }

        public void onMmDataRequest(int type, Bundle bundle) {
            sendMessage(0, type, bundle);
        }

        public void onHigeoEventCallback(int type, Bundle bundle) {
            sendMessage(1, type, bundle);
        }

        public void onCellBatchingCallback(int type, Bundle bundle) {
            sendMessage(2, type, bundle);
        }

        public void onWifiFenceCallback(int type, Bundle bundle) {
            sendMessage(3, type, bundle);
        }

        public void onCellFenceCallback(int type, Bundle bundle) {
            sendMessage(4, type, bundle);
        }

        public void onGeoFenceCallback(int type, Bundle bundle) {
            sendMessage(5, type, bundle);
        }

        private void sendMessage(int type, int subType, Bundle bundle) {
            HigeoCallback callback = this.mHigeoCallbackWeakRef.get();
            if (callback != null) {
                Message msg = Message.obtain();
                msg.what = type;
                msg.arg1 = subType;
                msg.obj = bundle;
                callback.mHandler.sendMessage(msg);
            }
        }
    }
}
