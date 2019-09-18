package com.huawei.msdp.devicestatus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack;
import com.huawei.msdp.devicestatus.IMSDPDeviceStatusService;

public class HwMSDPDeviceStatus {
    private static final String AIDL_MESSAGE_SERVICE_CLASS = "com.huawei.msdp.devicestatus.HwMSDPDeviceStatusService";
    private static final String AIDL_MESSAGE_SERVICE_PACKAGE = "com.huawei.msdp";
    /* access modifiers changed from: private */
    public static final String TAG = HwMSDPDeviceStatus.class.getSimpleName();
    private static final int sdkVersion = Build.VERSION.SDK_INT;
    /* access modifiers changed from: private */
    public ServiceDeathHandler deathHandler;
    private int mConnectCount = 0;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(HwMSDPDeviceStatus.TAG, "Connection service OK");
            HwMSDPDeviceStatus.this.mHandler.removeMessages(1);
            IMSDPDeviceStatusService unused = HwMSDPDeviceStatus.this.mService = IMSDPDeviceStatusService.Stub.asInterface(service);
            HwMSDPDeviceStatus.this.registerSink();
            HwMSDPDeviceStatus.this.notifyServiceDied();
            HwMSDPDeviceStatus.this.mServiceConnection.onServiceConnected();
        }

        public void onServiceDisconnected(ComponentName name) {
            IMSDPDeviceStatusService unused = HwMSDPDeviceStatus.this.mService = null;
            HwMSDPDeviceStatus.this.mServiceConnection.onServiceDisconnected();
        }
    };
    private Context mContext = null;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 1) {
                HwMSDPDeviceStatus.this.bindService();
            }
        }
    };
    /* access modifiers changed from: private */
    public IMSDPDeviceStatusService mService = null;
    /* access modifiers changed from: private */
    public HwMSDPDeviceStatusServiceConnection mServiceConnection = null;
    private IMSDPDeviceStatusChangedCallBack mSink;
    private String packageName;

    private class ServiceDeathHandler implements IBinder.DeathRecipient {
        private ServiceDeathHandler() {
        }

        public void binderDied() {
            Log.d(HwMSDPDeviceStatus.TAG, "device status service has died!");
            if (HwMSDPDeviceStatus.this.mServiceConnection != null) {
                HwMSDPDeviceStatus.this.mServiceConnection.onServiceDisconnected();
            }
            if (HwMSDPDeviceStatus.this.mService != null) {
                HwMSDPDeviceStatus.this.mService.asBinder().unlinkToDeath(HwMSDPDeviceStatus.this.deathHandler, 0);
                IMSDPDeviceStatusService unused = HwMSDPDeviceStatus.this.mService = null;
            }
        }
    }

    public HwMSDPDeviceStatus(Context context) {
        String str = TAG;
        Log.d(str, "HwdeviceStatusRecognition, android version :" + sdkVersion);
        if (context != null) {
            this.mContext = context;
            this.packageName = context.getPackageName();
            this.deathHandler = new ServiceDeathHandler();
        }
    }

    /* access modifiers changed from: private */
    public void notifyServiceDied() {
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
            String str = TAG;
            Log.e(str, "registerSink error:" + var3.getMessage());
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
            String str = TAG;
            Log.e(str, "unregisterSink error:" + var3.getMessage());
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

    /* access modifiers changed from: private */
    public void bindService() {
        if (this.mConnectCount > 10) {
            Log.d(TAG, "time out, connetion fail!");
        } else if (this.mService == null) {
            Log.d(TAG, this.mContext.getPackageName() + "bind device status  service");
            Intent bindIntent = new Intent();
            bindIntent.setClassName(AIDL_MESSAGE_SERVICE_PACKAGE, AIDL_MESSAGE_SERVICE_CLASS);
            this.mContext.bindService(bindIntent, this.mConnection, 1);
            this.mConnectCount++;
            this.mHandler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    public boolean disconnectService() {
        Log.d(TAG, "disconnectService");
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            return false;
        }
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

    public String[] getSupportedDeviceStatus() {
        Log.d(TAG, "getSupportedDeviceStatus");
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            return new String[0];
        }
        try {
            return this.mService.getSupportDeviceStatus();
        } catch (RemoteException var2) {
            String str = TAG;
            Log.e(str, "getSupportDeviceStatus error :" + var2.getMessage());
            return new String[0];
        }
    }

    public boolean enableDeviceStatusEvent(String deviceStatus, int eventType, long reportLatencyNs) {
        Log.d(TAG, "enableDeviceStatusEvent");
        boolean result = false;
        String str = TAG;
        Log.d(str, "deviceStatus:" + deviceStatus);
        String str2 = TAG;
        Log.d(str2, "eventType:" + eventType);
        String str3 = TAG;
        Log.d(str3, "reportLatencyNs:" + reportLatencyNs);
        if (TextUtils.isEmpty(deviceStatus) || reportLatencyNs < 0) {
            Log.e(TAG, "deviceStatus is null or reportLatencyNs < 0");
            return false;
        }
        String str4 = TAG;
        Log.d(str4, deviceStatus + "," + eventType + "," + reportLatencyNs);
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            return false;
        }
        try {
            result = this.mService.enableDeviceStatusService(this.packageName, deviceStatus, eventType, reportLatencyNs);
        } catch (RemoteException var7) {
            String str5 = TAG;
            Log.e(str5, "enabledeviceStatusEvent error:" + var7.getMessage());
        }
        return result;
    }

    public boolean disableDeviceStatusEvent(String deviceStatus, int eventType) {
        boolean result = false;
        Log.d(TAG, "disableDeviceStatusEvent");
        if (TextUtils.isEmpty(deviceStatus)) {
            Log.e(TAG, "deviceStatus is null");
            return false;
        }
        String str = TAG;
        Log.d(str, deviceStatus + "," + eventType);
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            return false;
        }
        try {
            result = this.mService.disableDeviceStatusService(this.packageName, deviceStatus, eventType);
        } catch (RemoteException var5) {
            String str2 = TAG;
            Log.e(str2, "disableDeviceStatusEvent error:" + var5.getMessage());
        }
        return result;
    }

    public HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus() {
        Log.d(TAG, "getCurrentDeviceStatus");
        HwMSDPDeviceStatusChangeEvent deviceStatus = null;
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            return null;
        }
        try {
            deviceStatus = this.mService.getCurrentDeviceStatus(this.packageName);
        } catch (RemoteException var3) {
            String str = TAG;
            Log.e(str, "getCurrentDeviceStatus error :" + var3.getMessage());
        }
        return deviceStatus;
    }

    private IMSDPDeviceStatusChangedCallBack createDeviceStatusRecognitionHardwareSink(final HwMSDPDeviceStatusChangedCallBack sink) {
        if (sink == null) {
            return null;
        }
        return new IMSDPDeviceStatusChangedCallBack.Stub() {
            public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent event) throws RemoteException {
                sink.onDeviceStatusChanged(event);
            }
        };
    }
}
