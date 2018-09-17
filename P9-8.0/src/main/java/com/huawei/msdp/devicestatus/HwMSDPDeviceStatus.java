package com.huawei.msdp.devicestatus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.msdp.devicestatus.IMSDPDeviceStatusService.Stub;

public class HwMSDPDeviceStatus {
    private static final String AIDL_MESSAGE_SERVICE_CLASS = "com.huawei.msdp.devicestatus.HwMSDPDeviceStatusService";
    private static final String AIDL_MESSAGE_SERVICE_PACKAGE = "com.huawei.msdp";
    private static final String TAG = HwMSDPDeviceStatus.class.getSimpleName();
    private static final int sdkVersion = VERSION.SDK_INT;
    private ServiceDeathHandler deathHandler;
    private int mConnectCount = 0;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(HwMSDPDeviceStatus.TAG, "Connection service OK");
            HwMSDPDeviceStatus.this.mHandler.removeMessages(1);
            HwMSDPDeviceStatus.this.mService = Stub.asInterface(service);
            HwMSDPDeviceStatus.this.registerSink();
            HwMSDPDeviceStatus.this.notifyServiceDied();
            HwMSDPDeviceStatus.this.mServiceConnection.onServiceConnected();
        }

        public void onServiceDisconnected(ComponentName name) {
            HwMSDPDeviceStatus.this.mService = null;
            HwMSDPDeviceStatus.this.mServiceConnection.onServiceDisconnected();
        }
    };
    private Context mContext = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    HwMSDPDeviceStatus.this.bindService();
                    return;
                default:
                    return;
            }
        }
    };
    private IMSDPDeviceStatusService mService = null;
    private HwMSDPDeviceStatusServiceConnection mServiceConnection = null;
    private IMSDPDeviceStatusChangedCallBack mSink;
    private String packageName;

    private class ServiceDeathHandler implements DeathRecipient {
        /* synthetic */ ServiceDeathHandler(HwMSDPDeviceStatus x0, AnonymousClass1 x1) {
            this();
        }

        private ServiceDeathHandler() {
        }

        public void binderDied() {
            Log.d(HwMSDPDeviceStatus.TAG, "device status service has died!");
            if (HwMSDPDeviceStatus.this.mServiceConnection != null) {
                HwMSDPDeviceStatus.this.mServiceConnection.onServiceDisconnected();
            }
            if (HwMSDPDeviceStatus.this.mService != null) {
                HwMSDPDeviceStatus.this.mService.asBinder().unlinkToDeath(HwMSDPDeviceStatus.this.deathHandler, 0);
                HwMSDPDeviceStatus.this.mService = null;
            }
        }
    }

    public HwMSDPDeviceStatus(Context context) {
        Log.d(TAG, "HwdeviceStatusRecognition, android version :" + sdkVersion);
        if (context != null) {
            this.mContext = context;
            this.packageName = context.getPackageName();
            this.deathHandler = new ServiceDeathHandler(this, null);
        }
    }

    private void notifyServiceDied() {
        try {
            if (this.mService != null) {
                this.mService.asBinder().linkToDeath(this.deathHandler, 0);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "IBinder register linkToDeath function fail.");
        }
    }

    public boolean registerSink() {
        boolean result = false;
        Log.d(TAG, "registerSink");
        if (this.mService == null || this.mSink == null) {
            Log.e(TAG, "mService or mSink is null.");
            return false;
        }
        try {
            result = this.mService.registerDeviceStatusCallBack(this.packageName, this.mSink);
        } catch (RemoteException var3) {
            Log.e(TAG, "registerSink error:" + var3.getMessage());
        }
        return result;
    }

    public boolean unregisterSink() {
        boolean result = false;
        Log.d(TAG, "unregisterSink");
        if (this.mService == null || this.mSink == null) {
            Log.e(TAG, "mService or mSink is null.");
            return false;
        }
        try {
            result = this.mService.freeDeviceStatusService(this.packageName, this.mSink);
        } catch (RemoteException var3) {
            Log.e(TAG, "unregisterSink error:" + var3.getMessage());
        }
        return result;
    }

    public boolean connectService(HwMSDPDeviceStatusChangedCallBack sink, HwMSDPDeviceStatusServiceConnection connection) {
        Log.d(TAG, "connectService");
        if (connection == null || sink == null) {
            Log.e(TAG, "connection or sink is null");
            return false;
        }
        this.mServiceConnection = connection;
        if (this.mService == null) {
            this.mSink = createDeviceStatusRecognitionHardwareSink(sink);
            bindService();
        }
        return true;
    }

    private void bindService() {
        if (this.mConnectCount > 10) {
            Log.d(TAG, "time out, connetion fail!");
        } else if (this.mService == null) {
            Log.d(TAG, this.mContext.getPackageName() + "bind device status  service");
            Intent bindIntent = new Intent();
            bindIntent.setClassName("com.huawei.msdp", AIDL_MESSAGE_SERVICE_CLASS);
            this.mContext.bindService(bindIntent, this.mConnection, 1);
            this.mConnectCount++;
            this.mHandler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    public boolean disconnectService() {
        Log.d(TAG, "disconnectService");
        if (this.mService != null) {
            this.mService.asBinder().unlinkToDeath(this.deathHandler, 0);
            unregisterSink();
            this.mContext.unbindService(this.mConnection);
            this.mServiceConnection.onServiceDisconnected();
            this.mService = null;
            this.mConnectCount = 0;
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(0);
            return true;
        }
        Log.e(TAG, "mService is null");
        return false;
    }

    public String[] getSupportedDeviceStatus() {
        Log.d(TAG, "getSupportedDeviceStatus");
        if (this.mService != null) {
            try {
                return this.mService.getSupportDeviceStatus();
            } catch (RemoteException var2) {
                Log.e(TAG, "getSupportDeviceStatus error :" + var2.getMessage());
                return new String[0];
            }
        }
        Log.e(TAG, "mService is null");
        return new String[0];
    }

    public boolean enableDeviceStatusEvent(String deviceStatus, int eventType, long reportLatencyNs) {
        Log.d(TAG, "enableDeviceStatusEvent");
        boolean result = false;
        Log.d(TAG, "deviceStatus:" + deviceStatus);
        Log.d(TAG, "eventType:" + eventType);
        Log.d(TAG, "reportLatencyNs:" + reportLatencyNs);
        if (!TextUtils.isEmpty(deviceStatus)) {
            if (!(reportLatencyNs < 0)) {
                Log.d(TAG, deviceStatus + "," + eventType + "," + reportLatencyNs);
                if (this.mService != null) {
                    try {
                        result = this.mService.enableDeviceStatusService(this.packageName, deviceStatus, eventType, reportLatencyNs);
                    } catch (RemoteException var7) {
                        Log.e(TAG, "enabledeviceStatusEvent error:" + var7.getMessage());
                    }
                    return result;
                }
                Log.e(TAG, "mService is null");
                return false;
            }
        }
        Log.e(TAG, "deviceStatus is null or reportLatencyNs < 0");
        return false;
    }

    public boolean disableDeviceStatusEvent(String deviceStatus, int eventType) {
        boolean result = false;
        Log.d(TAG, "disableDeviceStatusEvent");
        if (TextUtils.isEmpty(deviceStatus)) {
            Log.e(TAG, "deviceStatus is null");
            return false;
        }
        Log.d(TAG, deviceStatus + "," + eventType);
        if (this.mService != null) {
            try {
                result = this.mService.disableDeviceStatusService(this.packageName, deviceStatus, eventType);
            } catch (RemoteException var5) {
                Log.e(TAG, "disableDeviceStatusEvent error:" + var5.getMessage());
            }
            return result;
        }
        Log.e(TAG, "mService is null");
        return false;
    }

    public HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus() {
        Log.d(TAG, "getCurrentDeviceStatus");
        HwMSDPDeviceStatusChangeEvent deviceStatus = null;
        if (this.mService != null) {
            try {
                deviceStatus = this.mService.getCurrentDeviceStatus(this.packageName);
            } catch (RemoteException var3) {
                Log.e(TAG, "getCurrentDeviceStatus error :" + var3.getMessage());
            }
            return deviceStatus;
        }
        Log.e(TAG, "mService is null");
        return deviceStatus;
    }

    private IMSDPDeviceStatusChangedCallBack createDeviceStatusRecognitionHardwareSink(final HwMSDPDeviceStatusChangedCallBack sink) {
        return sink != null ? new IMSDPDeviceStatusChangedCallBack.Stub() {
            public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent event) throws RemoteException {
                sink.onDeviceStatusChanged(event);
            }
        } : null;
    }
}
