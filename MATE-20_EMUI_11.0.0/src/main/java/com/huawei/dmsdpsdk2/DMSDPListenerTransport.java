package com.huawei.dmsdpsdk2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.IDMSDPListener;
import java.util.Map;

public class DMSDPListenerTransport extends IDMSDPListener.Stub {
    private static final int EVENT_DEVICE_CHANGE = 1;
    private static final int EVENT_DEVICE_SERVICE_CHANGE = 2;
    private static final String TAG = "DMSDPListenerTransport";
    private final Handler mHandler;
    private DMSDPListener mListener;

    public DMSDPListenerTransport(DMSDPListener listener, Looper looper) {
        this.mListener = listener;
        this.mHandler = new Handler(looper) {
            /* class com.huawei.dmsdpsdk2.DMSDPListenerTransport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                DMSDPListenerTransport.this.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message msg) {
        HwLog.d(TAG, "handleMessage: " + msg.what);
        if (msg.obj instanceof DeviceServiceWrapper) {
            DeviceServiceWrapper wrapper = (DeviceServiceWrapper) msg.obj;
            int i = msg.what;
            if (i == 1) {
                this.mListener.onDeviceChange(wrapper.getDevice(), msg.arg1, wrapper.getInfo());
            } else if (i != 2) {
                HwLog.e(TAG, "Unknown message id:" + msg.what + ", can not be here!");
            } else {
                this.mListener.onDeviceServiceChange(wrapper.getService(), msg.arg1, wrapper.getInfo());
            }
        } else {
            HwLog.e(TAG, "not DeviceServiceWrapper instance");
        }
    }

    @Override // com.huawei.dmsdpsdk2.IDMSDPListener
    public void onDeviceChange(DMSDPDevice device, int state, Map info) throws RemoteException {
        HwLog.d(TAG, "onDeviceChange. state:" + state);
        sendMessage(1, state, new DeviceServiceWrapper(device, null, info));
    }

    @Override // com.huawei.dmsdpsdk2.IDMSDPListener
    public void onDeviceServiceChange(DMSDPDeviceService deviceService, int state, Map info) throws RemoteException {
        HwLog.d(TAG, "onDeviceServiceChange. state:" + state);
        sendMessage(2, state, new DeviceServiceWrapper(null, deviceService, info));
    }

    private void sendMessage(int msgWhat, int arg, Object obj) {
        if (!this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(msgWhat, arg, -1, obj), 0)) {
            HwLog.e(TAG, "sendMessage error here");
        }
    }

    /* access modifiers changed from: private */
    public static class DeviceServiceWrapper {
        private DMSDPDevice device;
        private Map<String, Object> info;
        private DMSDPDeviceService service;

        public DeviceServiceWrapper(DMSDPDevice device2, DMSDPDeviceService service2, Map<String, Object> info2) {
            this.device = device2;
            this.service = service2;
            this.info = info2;
        }

        public DMSDPDevice getDevice() {
            return this.device;
        }

        public DMSDPDeviceService getService() {
            return this.service;
        }

        public Map<String, Object> getInfo() {
            return this.info;
        }

        public void setDevice(DMSDPDevice device2) {
            this.device = device2;
        }

        public void setService(DMSDPDeviceService service2) {
            this.service = service2;
        }

        public void setInfo(Map<String, Object> info2) {
            this.info = info2;
        }
    }
}
