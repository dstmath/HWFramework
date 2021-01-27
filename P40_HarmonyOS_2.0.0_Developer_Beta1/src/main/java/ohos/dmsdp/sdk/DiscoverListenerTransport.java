package ohos.dmsdp.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import java.util.Map;
import ohos.dmsdp.sdk.IDiscoverListener;

public class DiscoverListenerTransport extends IDiscoverListener.Stub {
    private static final int EVENT_DEVICE_FOUND = 1;
    private static final int EVENT_DEVICE_LOST = 2;
    private static final int EVENT_DEVICE_UPDATE = 3;
    private static final int EVENT_STATE_CHANGE = 4;
    private static final String TAG = "DiscoverListenerTransport";
    private final Handler mHandler;
    private DiscoverListener mListener;

    public DiscoverListenerTransport(DiscoverListener discoverListener, Looper looper) {
        this.mListener = discoverListener;
        this.mHandler = new Handler(looper) {
            /* class ohos.dmsdp.sdk.DiscoverListenerTransport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                DiscoverListenerTransport.this.handleMessage(message);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message message) {
        HwLog.d(TAG, "handleMessage: " + message.what);
        int i = message.what;
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        HwLog.e(TAG, "Unknown message id:" + message.what + ", can not be here!");
                    } else if (message.obj instanceof Map) {
                        this.mListener.onStateChanged(message.arg1, (Map) message.obj);
                    }
                } else if (message.obj instanceof DMSDPDevice) {
                    this.mListener.onDeviceUpdate((DMSDPDevice) message.obj, message.arg1);
                }
            } else if (message.obj instanceof DMSDPDevice) {
                this.mListener.onDeviceLost((DMSDPDevice) message.obj);
            }
        } else if (message.obj instanceof DMSDPDevice) {
            this.mListener.onDeviceFound((DMSDPDevice) message.obj);
        }
    }

    @Override // ohos.dmsdp.sdk.IDiscoverListener
    public void onDeviceFound(DMSDPDevice dMSDPDevice) throws RemoteException {
        HwLog.d(TAG, "onDeviceFound");
        sendMessage(1, dMSDPDevice, 0);
    }

    @Override // ohos.dmsdp.sdk.IDiscoverListener
    public void onDeviceLost(DMSDPDevice dMSDPDevice) throws RemoteException {
        HwLog.d(TAG, "onDeviceLost");
        sendMessage(2, dMSDPDevice, 0);
    }

    @Override // ohos.dmsdp.sdk.IDiscoverListener
    public void onDeviceUpdate(DMSDPDevice dMSDPDevice, int i) throws RemoteException {
        HwLog.d(TAG, "onDeviceUpdate:" + i);
        sendMessage(3, dMSDPDevice, 0);
    }

    @Override // ohos.dmsdp.sdk.IDiscoverListener
    public void onStateChanged(int i, Map map) throws RemoteException {
        HwLog.d(TAG, "onStateChanged:" + i);
        sendMessage(4, i, -1, map, 0);
    }

    private void sendMessage(int i, Object obj, long j) {
        sendMessage(i, -1, -1, obj, j);
    }

    private void sendMessage(int i, int i2, int i3, Object obj, long j) {
        if (!this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(i, i2, i3, obj), j)) {
            HwLog.e(TAG, "sendMessage error here");
        }
    }
}
