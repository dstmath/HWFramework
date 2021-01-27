package ohos.dmsdp.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import java.util.Map;
import ohos.dmsdp.sdk.IDMSDPListener;

public class DMSDPListenerTransport extends IDMSDPListener.Stub {
    private static final int EVENT_DEVICE_CHANGE = 1;
    private static final int EVENT_DEVICE_SERVICE_CHANGE = 2;
    private static final String TAG = "DMSDPListenerTransport";
    private final Handler mHandler;
    private DMSDPListener mListener;

    public DMSDPListenerTransport(DMSDPListener dMSDPListener, Looper looper) {
        this.mListener = dMSDPListener;
        this.mHandler = new Handler(looper) {
            /* class ohos.dmsdp.sdk.DMSDPListenerTransport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                DMSDPListenerTransport.this.handleMessage(message);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message message) {
        HwLog.d(TAG, "handleMessage: " + message.what);
        if (message.obj instanceof DeviceServiceWrapper) {
            DeviceServiceWrapper deviceServiceWrapper = (DeviceServiceWrapper) message.obj;
            int i = message.what;
            if (i == 1) {
                this.mListener.onDeviceChange(deviceServiceWrapper.getDevice(), message.arg1, deviceServiceWrapper.getInfo());
            } else if (i != 2) {
                HwLog.e(TAG, "Unknown message id:" + message.what + ", can not be here!");
            } else {
                this.mListener.onDeviceServiceChange(deviceServiceWrapper.getService(), message.arg1, deviceServiceWrapper.getInfo());
            }
        }
    }

    @Override // ohos.dmsdp.sdk.IDMSDPListener
    public void onDeviceChange(DMSDPDevice dMSDPDevice, int i, Map map) throws RemoteException {
        HwLog.d(TAG, "onDeviceChange. state:" + i);
        sendMessage(1, i, new DeviceServiceWrapper(dMSDPDevice, null, map));
    }

    @Override // ohos.dmsdp.sdk.IDMSDPListener
    public void onDeviceServiceChange(DMSDPDeviceService dMSDPDeviceService, int i, Map map) throws RemoteException {
        HwLog.d(TAG, "onDeviceServiceChange. state:" + i);
        sendMessage(2, i, new DeviceServiceWrapper(null, dMSDPDeviceService, map));
    }

    private void sendMessage(int i, int i2, Object obj) {
        if (!this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(i, i2, -1, obj), 0)) {
            HwLog.e(TAG, "sendMessage error here");
        }
    }

    /* access modifiers changed from: private */
    public static class DeviceServiceWrapper {
        private Map<String, Object> details;
        private DMSDPDevice device;
        private DMSDPDeviceService service;

        DeviceServiceWrapper(DMSDPDevice dMSDPDevice, DMSDPDeviceService dMSDPDeviceService, Map<String, Object> map) {
            this.device = dMSDPDevice;
            this.service = dMSDPDeviceService;
            this.details = map;
        }

        public DMSDPDevice getDevice() {
            return this.device;
        }

        public void setDevice(DMSDPDevice dMSDPDevice) {
            this.device = dMSDPDevice;
        }

        public DMSDPDeviceService getService() {
            return this.service;
        }

        public void setService(DMSDPDeviceService dMSDPDeviceService) {
            this.service = dMSDPDeviceService;
        }

        public Map<String, Object> getInfo() {
            return this.details;
        }

        public void setInfo(Map<String, Object> map) {
            this.details = map;
        }
    }
}
