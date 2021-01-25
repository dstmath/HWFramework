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
    private static final String TAG = HwMSDPDeviceStatus.class.getSimpleName();
    private static final int sdkVersion = Build.VERSION.SDK_INT;
    private ServiceDeathHandler deathHandler;
    private int mConnectCount = 0;
    private ServiceConnection mConnection = new ServiceConnection() {
        /* class com.huawei.msdp.devicestatus.HwMSDPDeviceStatus.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(HwMSDPDeviceStatus.TAG, "Connection service OK");
            HwMSDPDeviceStatus.this.mHandler.removeMessages(1);
            HwMSDPDeviceStatus.this.mService = IMSDPDeviceStatusService.Stub.asInterface(service);
            HwMSDPDeviceStatus.this.registerSink();
            HwMSDPDeviceStatus.this.notifyServiceDied();
            HwMSDPDeviceStatus.this.mServiceConnection.onServiceConnected();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            HwMSDPDeviceStatus.this.mService = null;
            HwMSDPDeviceStatus.this.mServiceConnection.onServiceDisconnected();
        }
    };
    private Context mContext = null;
    private Handler mHandler = new Handler() {
        /* class com.huawei.msdp.devicestatus.HwMSDPDeviceStatus.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                HwMSDPDeviceStatus.this.bindService();
            }
        }
    };
    private IMSDPDeviceStatusService mService = null;
    private HwMSDPDeviceStatusServiceConnection mServiceConnection = null;
    private IMSDPDeviceStatusChangedCallBack mSink;
    private String packageName;

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
    /* access modifiers changed from: public */
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
        IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack;
        Log.d(TAG, "registerSink");
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null || (iMSDPDeviceStatusChangedCallBack = this.mSink) == null) {
            Log.e(TAG, "mService or mSink is null.");
            return false;
        }
        try {
            return iMSDPDeviceStatusService.registerDeviceStatusCallBack(this.packageName, iMSDPDeviceStatusChangedCallBack);
        } catch (RemoteException var3) {
            String str = TAG;
            Log.e(str, "registerSink error:" + var3.getMessage());
            return false;
        }
    }

    public boolean unregisterSink() {
        IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack;
        Log.d(TAG, "unregisterSink");
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null || (iMSDPDeviceStatusChangedCallBack = this.mSink) == null) {
            Log.e(TAG, "mService or mSink is null.");
            return false;
        }
        try {
            return iMSDPDeviceStatusService.freeDeviceStatusService(this.packageName, iMSDPDeviceStatusChangedCallBack);
        } catch (RemoteException var3) {
            String str = TAG;
            Log.e(str, "unregisterSink error:" + var3.getMessage());
            return false;
        }
    }

    public boolean connectService(HwMSDPDeviceStatusChangedCallBack sink, HwMSDPDeviceStatusServiceConnection connection) {
        Log.d(TAG, "connectService");
        if (connection == null || sink == null) {
            Log.e(TAG, "connection or sink is null");
            return false;
        }
        this.mServiceConnection = connection;
        if (this.mService != null) {
            return true;
        }
        this.mSink = createDeviceStatusRecognitionHardwareSink(sink);
        bindService();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindService() {
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
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null");
            return false;
        }
        iMSDPDeviceStatusService.asBinder().unlinkToDeath(this.deathHandler, 0);
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
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null");
            return new String[0];
        }
        try {
            return iMSDPDeviceStatusService.getSupportDeviceStatus();
        } catch (RemoteException var2) {
            String str = TAG;
            Log.e(str, "getSupportDeviceStatus error :" + var2.getMessage());
            return new String[0];
        }
    }

    public boolean enableDeviceStatusEvent(String deviceStatus, int eventType, long reportLatencyNs) {
        Log.d(TAG, "enableDeviceStatusEvent");
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
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null");
            return false;
        }
        try {
            return iMSDPDeviceStatusService.enableDeviceStatusService(this.packageName, deviceStatus, eventType, reportLatencyNs);
        } catch (RemoteException var7) {
            String str5 = TAG;
            Log.e(str5, "enabledeviceStatusEvent error:" + var7.getMessage());
            return false;
        }
    }

    public boolean disableDeviceStatusEvent(String deviceStatus, int eventType) {
        Log.d(TAG, "disableDeviceStatusEvent");
        if (TextUtils.isEmpty(deviceStatus)) {
            Log.e(TAG, "deviceStatus is null");
            return false;
        }
        String str = TAG;
        Log.d(str, deviceStatus + "," + eventType);
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null");
            return false;
        }
        try {
            return iMSDPDeviceStatusService.disableDeviceStatusService(this.packageName, deviceStatus, eventType);
        } catch (RemoteException var5) {
            String str2 = TAG;
            Log.e(str2, "disableDeviceStatusEvent error:" + var5.getMessage());
            return false;
        }
    }

    public HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus() {
        Log.d(TAG, "getCurrentDeviceStatus");
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null");
            return null;
        }
        try {
            return iMSDPDeviceStatusService.getCurrentDeviceStatus(this.packageName);
        } catch (RemoteException var3) {
            String str = TAG;
            Log.e(str, "getCurrentDeviceStatus error :" + var3.getMessage());
            return null;
        }
    }

    private IMSDPDeviceStatusChangedCallBack createDeviceStatusRecognitionHardwareSink(final HwMSDPDeviceStatusChangedCallBack sink) {
        if (sink == null) {
            return null;
        }
        return new IMSDPDeviceStatusChangedCallBack.Stub() {
            /* class com.huawei.msdp.devicestatus.HwMSDPDeviceStatus.AnonymousClass3 */

            @Override // com.huawei.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack
            public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent event) throws RemoteException {
                sink.onDeviceStatusChanged(event);
            }
        };
    }

    /* access modifiers changed from: private */
    public class ServiceDeathHandler implements IBinder.DeathRecipient {
        private ServiceDeathHandler() {
        }

        @Override // android.os.IBinder.DeathRecipient
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
}
