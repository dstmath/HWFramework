package ohos.msdp.movement;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import ohos.msdp.movement.IMSDPMovementService;
import ohos.msdp.movement.IMSDPMovementStatusChangeCallBack;

public class HwMSDPMovementManager {
    private static final String AIDL_MESSAGE_SERVICE_CLASS = "com.huawei.msdp.movement.HwMSDPMovementService";
    private static final String AIDL_MESSAGE_SERVICE_PACKAGE = "com.huawei.msdp";
    private static final long BIND_SERVICE_DELAY_TIME = 2000;
    private static final int CONNECT_TIMES = 10;
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    private static final int HIGH_LEVEL_MOVE = 32;
    private static final long HIGH_LEVEL_NUM = 4294967295L;
    private static final int MAX_COUNT_TIMES = 10;
    private static final int MSG_BIND_SERVICE = 1;
    private static final int MSG_PROCESS_MODULE = 2;
    private static final long PROCESS_MODULE_DELAY_TIME = 1000;
    private static final String SDK_VERSION = "10.0.3";
    private static final String TAG = "HwMSDPMovementManager";
    private static int sSupportedModule;
    private boolean isClientConnected = false;
    private boolean isConnectedMsdp = false;
    private int mConnectCount = 0;
    private ServiceConnection mConnection = new ServiceConnection() {
        /* class ohos.msdp.movement.HwMSDPMovementManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(HwMSDPMovementManager.TAG, "onServiceConnected");
            if (HwMSDPMovementManager.this.mMovementHandler != null) {
                HwMSDPMovementManager.this.mMovementHandler.removeMessages(1);
            }
            HwMSDPMovementManager.this.mService = IMSDPMovementService.Stub.asInterface(iBinder);
            HwMSDPMovementManager.this.isConnectedMsdp = true;
            HwMSDPMovementManager.this.notifyServiceDied();
            HwMSDPMovementManager.this.registerSink();
            HwMSDPMovementManager.this.processModule();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(HwMSDPMovementManager.TAG, "onServiceDisconnected");
            HwMSDPMovementManager.this.isConnectedMsdp = false;
            if (HwMSDPMovementManager.this.mServiceConnection != null) {
                HwMSDPMovementManager.this.isClientConnected = false;
                HwMSDPMovementManager.this.mServiceConnection.onServiceDisconnected(true);
            }
        }
    };
    private Context mContext;
    private int mModuleCount = 0;
    private MovementHandler mMovementHandler = null;
    private String mPackageName;
    private IMSDPMovementService mService = null;
    private HwMSDPMovementServiceConnection mServiceConnection;
    private ServiceDeathRecipient mServiceDeathRecipient = null;
    private IMSDPMovementStatusChangeCallBack mSink;

    public HwMSDPMovementManager(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mPackageName = context.getPackageName();
            this.mMovementHandler = new MovementHandler(Looper.getMainLooper());
            this.mServiceDeathRecipient = new ServiceDeathRecipient();
        }
    }

    private IMSDPMovementStatusChangeCallBack createMSDPMovementSink(final HwMSDPMovementStatusChangeCallback hwMSDPMovementStatusChangeCallback) {
        if (hwMSDPMovementStatusChangeCallback == null) {
            return null;
        }
        return new IMSDPMovementStatusChangeCallBack.Stub() {
            /* class ohos.msdp.movement.HwMSDPMovementManager.AnonymousClass2 */

            @Override // ohos.msdp.movement.IMSDPMovementStatusChangeCallBack
            public void onActivityChanged(int i, HwMSDPMovementChangeEvent hwMSDPMovementChangeEvent) throws RemoteException {
                hwMSDPMovementStatusChangeCallback.onMovementStatusChanged(i, hwMSDPMovementChangeEvent);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processModule() {
        Log.i(TAG, "processModule");
        if (this.mModuleCount > 10) {
            HwMSDPMovementServiceConnection hwMSDPMovementServiceConnection = this.mServiceConnection;
            if (hwMSDPMovementServiceConnection != null) {
                this.isClientConnected = false;
                hwMSDPMovementServiceConnection.onServiceDisconnected(false);
                return;
            }
            return;
        }
        sSupportedModule = getSupportedModule();
        if (sSupportedModule == 0) {
            this.mModuleCount++;
            MovementHandler movementHandler = this.mMovementHandler;
            if (movementHandler != null) {
                movementHandler.sendEmptyMessageDelayed(2, PROCESS_MODULE_DELAY_TIME);
                return;
            }
            return;
        }
        HwMSDPMovementServiceConnection hwMSDPMovementServiceConnection2 = this.mServiceConnection;
        if (hwMSDPMovementServiceConnection2 != null && this.mMovementHandler != null) {
            this.isClientConnected = true;
            hwMSDPMovementServiceConnection2.onServiceConnected();
            this.mMovementHandler.removeCallbacksAndMessages(null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindService() {
        if (this.mConnectCount > 10) {
            Log.e(TAG, "try connect 10 times, connection fail");
        } else if (this.mContext != null) {
            Intent intent = new Intent();
            intent.setClassName(AIDL_MESSAGE_SERVICE_PACKAGE, AIDL_MESSAGE_SERVICE_CLASS);
            this.mContext.bindService(intent, this.mConnection, 1);
            Log.i(TAG, "bindService");
            this.mConnectCount++;
            MovementHandler movementHandler = this.mMovementHandler;
            if (movementHandler != null) {
                movementHandler.sendEmptyMessageDelayed(1, BIND_SERVICE_DELAY_TIME);
            }
        }
    }

    public boolean connectService(HwMSDPMovementStatusChangeCallback hwMSDPMovementStatusChangeCallback, HwMSDPMovementServiceConnection hwMSDPMovementServiceConnection) {
        Log.i(TAG, "HwMSDPMovementSDK Version = 10.0.3 isSystemUser : " + isSystemUser() + " Client:" + this.mPackageName);
        if (!isSystemUser()) {
            Log.e(TAG, "not system user.");
            return false;
        }
        Log.i(TAG, "isConnectedMsdp : " + this.isConnectedMsdp + " isClientConnected:" + this.isClientConnected);
        if (this.isConnectedMsdp && !this.isClientConnected) {
            disconnectService();
        }
        if (hwMSDPMovementServiceConnection == null || hwMSDPMovementStatusChangeCallback == null) {
            return false;
        }
        this.mServiceConnection = hwMSDPMovementServiceConnection;
        this.mSink = createMSDPMovementSink(hwMSDPMovementStatusChangeCallback);
        this.mConnectCount = 0;
        this.mModuleCount = 0;
        if (this.isConnectedMsdp) {
            return true;
        }
        bindService();
        return true;
    }

    public String getServiceVersion() {
        Log.i(TAG, "getServiceVersion");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "getServiceVersion mService is null.");
            return "";
        }
        try {
            return iMSDPMovementService.getServcieVersion();
        } catch (RemoteException unused) {
            Log.e(TAG, "getServiceVersion error");
            return "";
        }
    }

    public String[] getSupportedMovements(int i) {
        Log.i(TAG, "getSupportedMovements");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "getSupportedMovements mService is null.");
            return new String[0];
        }
        try {
            return iMSDPMovementService.getSupportedMovements(i);
        } catch (RemoteException unused) {
            Log.e(TAG, "getSupportedMovements error");
            return new String[0];
        }
    }

    public boolean enableMovementEvent(int i, String str, int i2, long j, HwMSDPOtherParameters hwMSDPOtherParameters) {
        boolean enableMovementEvent;
        Log.i(TAG, "enableMovementEvent type = " + i);
        if (TextUtils.isEmpty(str) || j < 0) {
            Log.e(TAG, "activity is null or reportLatencyNs < 0");
            return false;
        }
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "enableMovementEvent mService is null.");
            return false;
        }
        if (i == 0) {
            try {
                enableMovementEvent = iMSDPMovementService.enableMovementEvent(i, this.mPackageName, str, i2, j, hwMSDPOtherParameters);
            } catch (RemoteException unused) {
                Log.e(TAG, "enableMovementEvent error");
                return false;
            }
        } else if (i == 1) {
            enableMovementEvent = iMSDPMovementService.enableMovementEvent(i, this.mPackageName, str, i2, j, hwMSDPOtherParameters);
        } else if (i == 2) {
            enableMovementEvent = iMSDPMovementService.enableMovementEvent(i, this.mPackageName, str, i2, j, getHwMSDPOtherParam());
        } else {
            Log.e(TAG, "unknown movement type  [ " + i + " ]");
            return false;
        }
        return enableMovementEvent;
    }

    private HwMSDPOtherParameters getHwMSDPOtherParam() {
        long currentTimeMillis = System.currentTimeMillis();
        return new HwMSDPOtherParameters((double) (HIGH_LEVEL_NUM & currentTimeMillis), (double) (currentTimeMillis >> 32), 0.0d, 0.0d, "");
    }

    public boolean disableMovementEvent(int i, String str, int i2) {
        Log.i(TAG, "disableMovementEvent type = " + i);
        if (TextUtils.isEmpty(str)) {
            Log.e(TAG, "movement is null.");
            return false;
        } else if (this.mService == null) {
            Log.e(TAG, "disableMovementEvent mService is null.");
            return false;
        } else if (i == 0 || i == 1 || i == 2) {
            return this.mService.disableMovementEvent(i, this.mPackageName, str, i2);
        } else {
            try {
                Log.e(TAG, "unknown movement type [" + i + " ]");
                return false;
            } catch (RemoteException unused) {
                Log.e(TAG, "disableMovementEvent error");
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerSink() {
        IMSDPMovementStatusChangeCallBack iMSDPMovementStatusChangeCallBack;
        Log.i(TAG, "registerSink");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null || (iMSDPMovementStatusChangeCallBack = this.mSink) == null) {
            Log.e(TAG, "mService or mSink is null.");
            return;
        }
        try {
            boolean registerSink = iMSDPMovementService.registerSink(this.mPackageName, iMSDPMovementStatusChangeCallBack);
            Log.i(TAG, "registerSink isRegisterSuccess = " + registerSink);
        } catch (RemoteException unused) {
            Log.e(TAG, "registerSink error");
        }
    }

    private void unregisterSink() {
        IMSDPMovementStatusChangeCallBack iMSDPMovementStatusChangeCallBack;
        Log.i(TAG, "unregisterSink");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null || (iMSDPMovementStatusChangeCallBack = this.mSink) == null) {
            Log.e(TAG, "mService or mSink is null.");
            return;
        }
        try {
            boolean unregisterSink = iMSDPMovementService.unregisterSink(this.mPackageName, iMSDPMovementStatusChangeCallBack);
            Log.i(TAG, "unregisterSink isUnregisterSuccess = " + unregisterSink);
        } catch (RemoteException unused) {
            Log.e(TAG, "unregisterSink error");
        }
    }

    public HwMSDPMovementChangeEvent getCurrentMovement(int i) {
        Log.i(TAG, "getCurrentMovement type " + i);
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "getCurrentMovement mService is null.");
            return null;
        }
        try {
            return iMSDPMovementService.getCurrentMovement(i, this.mPackageName);
        } catch (RemoteException unused) {
            Log.e(TAG, "getCurrentMovement error");
            return null;
        }
    }

    public boolean flush() {
        Log.i(TAG, "flush");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "flush mService is null.");
            return false;
        }
        try {
            return iMSDPMovementService.flush();
        } catch (RemoteException unused) {
            Log.e(TAG, "flush error");
            return false;
        }
    }

    public int getSupportedModule() {
        Log.i(TAG, "getSupportedModule");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "getSupportedModule mService is null.");
            return 0;
        }
        try {
            return iMSDPMovementService.getSupportedModule();
        } catch (RemoteException unused) {
            Log.e(TAG, "getSupportedModule error");
            return 0;
        }
    }

    public boolean initEnvironment(String str) {
        Log.i(TAG, "initEnvironment");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "initEnvironment mService is null.");
            return false;
        }
        try {
            boolean initEnvironment = iMSDPMovementService.initEnvironment(this.mPackageName, str, getHwMSDPOtherParam());
            Log.i(TAG, "initEnvironment isInitSuccess: " + initEnvironment);
            return initEnvironment;
        } catch (RemoteException unused) {
            Log.e(TAG, "initEnvironment error");
            return false;
        }
    }

    public boolean exitEnvironment(String str) {
        Log.i(TAG, "exitEnvironment");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "exitEnvironment mService is null.");
            return false;
        }
        try {
            boolean exitEnvironment = iMSDPMovementService.exitEnvironment(this.mPackageName, str, getHwMSDPOtherParam());
            Log.i(TAG, "exitEnvironment isExitSuccess:" + exitEnvironment);
            return exitEnvironment;
        } catch (RemoteException unused) {
            Log.e(TAG, "exitEnvironment error");
            return false;
        }
    }

    public boolean disconnectService() {
        Log.i(TAG, "disconnectService");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "disconnectService mService is null.");
            return false;
        }
        if (iMSDPMovementService.asBinder() != null) {
            this.mService.asBinder().unlinkToDeath(this.mServiceDeathRecipient, 0);
        }
        unregisterSink();
        Context context = this.mContext;
        if (context != null) {
            context.unbindService(this.mConnection);
        }
        HwMSDPMovementServiceConnection hwMSDPMovementServiceConnection = this.mServiceConnection;
        if (hwMSDPMovementServiceConnection != null) {
            this.isClientConnected = false;
            hwMSDPMovementServiceConnection.onServiceDisconnected(true);
        }
        this.mService = null;
        this.isConnectedMsdp = false;
        this.mConnectCount = 0;
        MovementHandler movementHandler = this.mMovementHandler;
        if (movementHandler != null) {
            movementHandler.removeMessages(1);
        }
        Log.i(TAG, "disconnectService true");
        return true;
    }

    private boolean isSystemUser() {
        try {
            Object invoke = Class.forName("android.os.UserHandle").getMethod("myUserId", new Class[0]).invoke(null, new Object[0]);
            if (invoke == null || !(invoke instanceof Integer)) {
                return false;
            }
            int intValue = ((Integer) invoke).intValue();
            Log.d(TAG, "user id:" + intValue);
            if (intValue == 0) {
                return true;
            }
            return false;
        } catch (ClassNotFoundException unused) {
            Log.e(TAG, "ClassNotFoundException");
            return false;
        } catch (NoSuchMethodException unused2) {
            Log.e(TAG, "NoSuchMethodException");
            return false;
        } catch (IllegalAccessException unused3) {
            Log.e(TAG, "IllegalAccessException");
            return false;
        } catch (IllegalArgumentException unused4) {
            Log.e(TAG, "IllegalArgumentException");
            return false;
        } catch (InvocationTargetException unused5) {
            Log.e(TAG, "InvocationTargetException");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyServiceDied() {
        Log.i(TAG, "notifyServiceDied");
        try {
            if (this.mService != null && this.mService.asBinder() != null) {
                this.mService.asBinder().linkToDeath(this.mServiceDeathRecipient, 0);
            }
        } catch (RemoteException unused) {
            Log.e(TAG, "IBinder register linkToDeath fail.");
        }
    }

    /* access modifiers changed from: private */
    public class MovementHandler extends Handler {
        MovementHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            int i = message.what;
            if (i == 1) {
                HwMSDPMovementManager.this.bindService();
            } else if (i == 2) {
                HwMSDPMovementManager.this.processModule();
            }
        }
    }

    /* access modifiers changed from: private */
    public class ServiceDeathRecipient implements IBinder.DeathRecipient {
        ServiceDeathRecipient() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.i(HwMSDPMovementManager.TAG, "the movement Service has died !");
            if (HwMSDPMovementManager.this.mServiceConnection != null) {
                HwMSDPMovementManager.this.isClientConnected = false;
                HwMSDPMovementManager.this.mServiceConnection.onServiceDisconnected(false);
            }
            if (HwMSDPMovementManager.this.mService != null && HwMSDPMovementManager.this.mService.asBinder() != null) {
                HwMSDPMovementManager.this.mService.asBinder().unlinkToDeath(HwMSDPMovementManager.this.mServiceDeathRecipient, 0);
                HwMSDPMovementManager.this.mService = null;
            }
        }
    }
}
