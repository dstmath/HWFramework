package ohos.msdp.devicestatus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import ohos.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack;
import ohos.msdp.devicestatus.IMSDPDeviceStatusService;

public class HwMSDPDeviceStatus {
    private static final String AIDL_MESSAGE_SERVICE_CLASS = "com.huawei.msdp.devicestatus.HwMSDPDeviceStatusService";
    private static final String AIDL_MESSAGE_SERVICE_PACKAGE = "com.huawei.msdp";
    private static final int MAX_RETRY_TIMES = 10;
    private static final int MOTION_ENABLED = 1;
    private static final int MSG_DEFAULT = 0;
    private static final int MSG_NEED_BIND_SERVICE = 1;
    private static final String TAG = HwMSDPDeviceStatus.class.getSimpleName();
    private static final int sdkVersion = Build.VERSION.SDK_INT;
    private ServiceDeathHandler deathHandler;
    private int mConnectCount = 0;
    private ServiceConnection mConnection = new ServiceConnection() {
        /* class ohos.msdp.devicestatus.HwMSDPDeviceStatus.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(HwMSDPDeviceStatus.TAG, "Connection service OK.");
            HwMSDPDeviceStatus.this.mHandler.removeMessages(1);
            HwMSDPDeviceStatus.this.mService = IMSDPDeviceStatusService.Stub.asInterface(iBinder);
            HwMSDPDeviceStatus.this.registerSink();
            HwMSDPDeviceStatus.this.notifyServiceDied();
            HwMSDPDeviceStatus.this.mServiceConnection.onServiceConnected();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            HwMSDPDeviceStatus.this.mService = null;
            HwMSDPDeviceStatus.this.mServiceConnection.onServiceDisconnected();
        }
    };
    private Context mContext = null;
    private Handler mHandler;
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
            if (Looper.myLooper() != Looper.getMainLooper()) {
                HandlerThread handlerThread = new HandlerThread("deviceStatus");
                handlerThread.start();
                initHandler(handlerThread.getLooper());
                return;
            }
            initHandler(Looper.getMainLooper());
        }
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            /* class ohos.msdp.devicestatus.HwMSDPDeviceStatus.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    HwMSDPDeviceStatus.this.bindService();
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyServiceDied() {
        try {
            if (this.mService != null) {
                this.mService.asBinder().linkToDeath(this.deathHandler, 0);
            }
        } catch (RemoteException unused) {
            Log.e(TAG, "IBinder register linkToDeath function fail.");
        }
    }

    public boolean registerSink() {
        IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack;
        Log.d(TAG, "registerSink.");
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null || (iMSDPDeviceStatusChangedCallBack = this.mSink) == null) {
            Log.e(TAG, "mService or mSink is null.");
            return false;
        }
        try {
            return iMSDPDeviceStatusService.registerDeviceStatusCallBack(this.packageName, iMSDPDeviceStatusChangedCallBack);
        } catch (RemoteException unused) {
            Log.e(TAG, "registerSink error.");
            return false;
        }
    }

    public boolean unregisterSink() {
        IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack;
        Log.d(TAG, "unregisterSink.");
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null || (iMSDPDeviceStatusChangedCallBack = this.mSink) == null) {
            Log.e(TAG, "mService or mSink is null.");
            return false;
        }
        try {
            return iMSDPDeviceStatusService.freeDeviceStatusService(this.packageName, iMSDPDeviceStatusChangedCallBack);
        } catch (RemoteException unused) {
            Log.e(TAG, "unregisterSink error.");
            return false;
        }
    }

    public boolean connectService(HwMSDPDeviceStatusChangedCallBack hwMSDPDeviceStatusChangedCallBack, HwMSDPDeviceStatusServiceConnection hwMSDPDeviceStatusServiceConnection) {
        Log.d(TAG, "connectService.");
        if (hwMSDPDeviceStatusServiceConnection == null || hwMSDPDeviceStatusChangedCallBack == null) {
            Log.e(TAG, "connection or sink is null.");
            return false;
        }
        this.mServiceConnection = hwMSDPDeviceStatusServiceConnection;
        if (this.mService != null) {
            return true;
        }
        this.mSink = createDeviceStatusRecognitionHardwareSink(hwMSDPDeviceStatusChangedCallBack);
        bindService();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindService() {
        if (this.mConnectCount > 10) {
            Log.d(TAG, "time out, connetion fail!");
        } else if (this.mService == null && this.mContext != null) {
            Log.d(TAG, this.mContext.getPackageName() + "bind device status service.");
            Intent intent = new Intent();
            intent.setClassName(AIDL_MESSAGE_SERVICE_PACKAGE, AIDL_MESSAGE_SERVICE_CLASS);
            this.mContext.bindService(intent, this.mConnection, 1);
            this.mConnectCount++;
            this.mHandler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    public boolean disconnectService() {
        ServiceConnection serviceConnection;
        Log.d(TAG, "disconnectService.");
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        try {
            iMSDPDeviceStatusService.asBinder().unlinkToDeath(this.deathHandler, 0);
        } catch (RuntimeException unused) {
            Log.e(TAG, "unlinkToDeath Exception.");
        }
        unregisterSink();
        Context context = this.mContext;
        if (!(context == null || (serviceConnection = this.mConnection) == null)) {
            context.unbindService(serviceConnection);
        }
        this.mServiceConnection.onServiceDisconnected();
        this.mService = null;
        this.mConnectCount = 0;
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(0);
        return true;
    }

    public String[] getSupportedDeviceStatus() {
        Log.d(TAG, "getSupportedDeviceStatus.");
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null.");
            return new String[0];
        }
        try {
            return iMSDPDeviceStatusService.getSupportDeviceStatus();
        } catch (RemoteException unused) {
            Log.e(TAG, "getSupportDeviceStatus error.");
            return new String[0];
        }
    }

    public boolean enableDeviceStatusEvent(String str, int i, long j) {
        Log.d(TAG, "enableDeviceStatusEvent.");
        if (TextUtils.isEmpty(str) || j < 0) {
            Log.e(TAG, "deviceStatus is null or reportLatencyNs < 0.");
            return false;
        }
        String str2 = TAG;
        Log.d(str2, str + "," + i + "," + j);
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        try {
            return iMSDPDeviceStatusService.enableDeviceStatusService(this.packageName, str, i, j);
        } catch (RemoteException unused) {
            Log.e(TAG, "enabledeviceStatusEvent error.");
            return false;
        }
    }

    public boolean disableDeviceStatusEvent(String str, int i) {
        Log.d(TAG, "disableDeviceStatusEvent.");
        if (TextUtils.isEmpty(str)) {
            Log.e(TAG, "deviceStatus is null.");
            return false;
        }
        String str2 = TAG;
        Log.d(str2, str + "," + i);
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        try {
            return iMSDPDeviceStatusService.disableDeviceStatusService(this.packageName, str, i);
        } catch (RemoteException unused) {
            Log.e(TAG, "disableDeviceStatusEvent error.");
            return false;
        }
    }

    public HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus() {
        Log.d(TAG, "getCurrentDeviceStatus.");
        IMSDPDeviceStatusService iMSDPDeviceStatusService = this.mService;
        if (iMSDPDeviceStatusService == null) {
            Log.e(TAG, "mService is null.");
            return null;
        }
        try {
            return iMSDPDeviceStatusService.getCurrentDeviceStatus(this.packageName);
        } catch (RemoteException unused) {
            Log.e(TAG, "getCurrentDeviceStatus error.");
            return null;
        }
    }

    private IMSDPDeviceStatusChangedCallBack createDeviceStatusRecognitionHardwareSink(final HwMSDPDeviceStatusChangedCallBack hwMSDPDeviceStatusChangedCallBack) {
        if (hwMSDPDeviceStatusChangedCallBack == null) {
            return null;
        }
        return new IMSDPDeviceStatusChangedCallBack.Stub() {
            /* class ohos.msdp.devicestatus.HwMSDPDeviceStatus.AnonymousClass3 */

            @Override // ohos.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack
            public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent hwMSDPDeviceStatusChangeEvent) throws RemoteException {
                hwMSDPDeviceStatusChangedCallBack.onDeviceStatusChanged(hwMSDPDeviceStatusChangeEvent);
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
                try {
                    HwMSDPDeviceStatus.this.mService.asBinder().unlinkToDeath(HwMSDPDeviceStatus.this.deathHandler, 0);
                } catch (RuntimeException unused) {
                    Log.e(HwMSDPDeviceStatus.TAG, "unlinkToDeath Exception.");
                }
                HwMSDPDeviceStatus.this.mService = null;
            }
        }
    }
}
