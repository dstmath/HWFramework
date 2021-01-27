package com.huawei.msdp.movement;

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
import com.huawei.msdp.movement.IMSDPMovementService;
import com.huawei.msdp.movement.IMSDPMovementStatusChangeCallBack;
import java.lang.reflect.InvocationTargetException;

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
    private boolean isClientConnected = false;
    private boolean isConnectedMsdp = false;
    private int mConnectCount = 0;
    private ServiceConnection mConnection = new ServiceConnection() {
        /* class com.huawei.msdp.movement.HwMSDPMovementManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(HwMSDPMovementManager.TAG, "onServiceConnected");
            if (HwMSDPMovementManager.this.mMovementHandler != null) {
                HwMSDPMovementManager.this.mMovementHandler.removeMessages(1);
            }
            HwMSDPMovementManager.this.mService = IMSDPMovementService.Stub.asInterface(service);
            HwMSDPMovementManager.this.isConnectedMsdp = true;
            HwMSDPMovementManager.this.notifyServiceDied();
            HwMSDPMovementManager.this.registerSink();
            HwMSDPMovementManager.this.processModule();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
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
    private int mSupportModule = 0;

    public HwMSDPMovementManager(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mPackageName = context.getPackageName();
            this.mMovementHandler = new MovementHandler(Looper.getMainLooper());
            this.mServiceDeathRecipient = new ServiceDeathRecipient();
        }
    }

    public HwMSDPMovementManager(Context context, Object object) {
        if (context != null) {
            this.mContext = context;
            if (object != null) {
                this.mPackageName = object.getClass().getName();
                this.mMovementHandler = new MovementHandler(Looper.getMainLooper());
                this.mServiceDeathRecipient = new ServiceDeathRecipient();
                return;
            }
            throw new NullPointerException("object must not be null.");
        }
    }

    private IMSDPMovementStatusChangeCallBack createMovementSink(final HwMSDPMovementStatusChangeCallback sink) {
        if (sink == null) {
            return null;
        }
        return new IMSDPMovementStatusChangeCallBack.Stub() {
            /* class com.huawei.msdp.movement.HwMSDPMovementManager.AnonymousClass2 */

            @Override // com.huawei.msdp.movement.IMSDPMovementStatusChangeCallBack
            public void onActivityChanged(int type, HwMSDPMovementChangeEvent event) throws RemoteException {
                sink.onMovementStatusChanged(type, event);
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
            }
        } else if (getSupportedModule() == 0) {
            this.mModuleCount++;
            MovementHandler movementHandler = this.mMovementHandler;
            if (movementHandler != null) {
                movementHandler.sendEmptyMessageDelayed(2, PROCESS_MODULE_DELAY_TIME);
            }
        } else {
            HwMSDPMovementServiceConnection hwMSDPMovementServiceConnection2 = this.mServiceConnection;
            if (hwMSDPMovementServiceConnection2 != null && this.mMovementHandler != null) {
                this.isClientConnected = true;
                hwMSDPMovementServiceConnection2.onServiceConnected();
                this.mMovementHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindService() {
        if (this.mConnectCount > 10) {
            Log.e(TAG, "try connect 10 times, connection fail");
            return;
        }
        Intent bindIntent = new Intent();
        bindIntent.setClassName(AIDL_MESSAGE_SERVICE_PACKAGE, AIDL_MESSAGE_SERVICE_CLASS);
        this.mContext.bindService(bindIntent, this.mConnection, 1);
        Log.i(TAG, "bindService");
        this.mConnectCount++;
        MovementHandler movementHandler = this.mMovementHandler;
        if (movementHandler != null) {
            movementHandler.sendEmptyMessageDelayed(1, BIND_SERVICE_DELAY_TIME);
        }
    }

    public boolean connectService(HwMSDPMovementStatusChangeCallback sink, HwMSDPMovementServiceConnection connection) {
        Log.i(TAG, "HwMSDPMovementSDK Version = 10.0.3 isSystemUser : " + isSystemUser() + " Client:" + this.mPackageName);
        if (!isSystemUser()) {
            Log.e(TAG, "not system user.");
            return false;
        }
        Log.i(TAG, "isConnectedMsdp: " + this.isConnectedMsdp + " isClientConnected:" + this.isClientConnected);
        if (this.isConnectedMsdp && !this.isClientConnected) {
            disConnectService();
        }
        if (connection == null || sink == null) {
            return false;
        }
        this.mServiceConnection = connection;
        this.mSink = createMovementSink(sink);
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
            Log.e(TAG, "mService is null.");
            return "";
        }
        try {
            return iMSDPMovementService.getServcieVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "getServiceVersion error");
            return "";
        }
    }

    public String[] getSupportedMovements(int type) {
        Log.i(TAG, "getSupportedMovements");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "mService is null.");
            return new String[0];
        }
        try {
            return iMSDPMovementService.getSupportedMovements(type);
        } catch (RemoteException e) {
            Log.e(TAG, "getSupportedMovements error");
            return new String[0];
        }
    }

    public boolean enableMovementEvent(int type, String movement, int eventType, long reportLatencyNs, HwMSDPOtherParameters parameters) {
        Log.i(TAG, "enableMovementEvent type =" + type);
        if (TextUtils.isEmpty(movement) || reportLatencyNs < 0) {
            Log.e(TAG, "activity is null or reportLatencyNs < 0");
            return false;
        }
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        } else if (type == 0) {
            try {
                return iMSDPMovementService.enableMovementEvent(type, this.mPackageName, movement, eventType, reportLatencyNs, parameters);
            } catch (RemoteException e) {
                Log.e(TAG, "enableMovementEvent error");
                return false;
            }
        } else if (type == 1) {
            return iMSDPMovementService.enableMovementEvent(type, this.mPackageName, movement, eventType, reportLatencyNs, parameters);
        } else {
            if (type == 2) {
                return iMSDPMovementService.enableMovementEvent(type, this.mPackageName, movement, eventType, reportLatencyNs, getHwOtherParam());
            }
            Log.e(TAG, "unknown movement type [ " + type + " ]");
            return false;
        }
    }

    private HwMSDPOtherParameters getHwOtherParam() {
        long currentTime = System.currentTimeMillis();
        return new HwMSDPOtherParameters(Double.valueOf((double) (HIGH_LEVEL_NUM & currentTime)).doubleValue(), Double.valueOf((double) (currentTime >> 32)).doubleValue(), 0.0d, 0.0d, "");
    }

    public boolean disableMovementEvent(int type, String movement, int eventType) {
        Log.i(TAG, "disableMovementEvent type = " + type);
        if (TextUtils.isEmpty(movement)) {
            Log.e(TAG, "movement is null.");
            return false;
        } else if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        } else if (type == 0 || type == 1 || type == 2) {
            return this.mService.disableMovementEvent(type, this.mPackageName, movement, eventType);
        } else {
            try {
                Log.e(TAG, "unknown movement type [" + type + " ]");
                return false;
            } catch (RemoteException e) {
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
            boolean isSuccess = iMSDPMovementService.registerSink(this.mPackageName, iMSDPMovementStatusChangeCallBack);
            Log.i(TAG, "registerSink result = " + isSuccess);
        } catch (RemoteException e) {
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
            boolean isSuccess = iMSDPMovementService.unregisterSink(this.mPackageName, iMSDPMovementStatusChangeCallBack);
            Log.i(TAG, "unregisterSink result = " + isSuccess);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterSink error");
        }
    }

    public HwMSDPMovementChangeEvent getCurrentMovement(int type) {
        Log.i(TAG, "getCurrentMovement type " + type);
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "mService is null.");
            return null;
        }
        try {
            return iMSDPMovementService.getCurrentMovement(type, this.mPackageName);
        } catch (RemoteException e) {
            Log.e(TAG, "getCurrentMovement error");
            return null;
        }
    }

    public boolean flush() {
        Log.i(TAG, "flush");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        try {
            return iMSDPMovementService.flush();
        } catch (RemoteException e) {
            Log.e(TAG, "flush error");
            return false;
        }
    }

    public int getSupportedModule() {
        Log.i(TAG, "getSupportedModule");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "mService is null.");
            return 0;
        }
        try {
            return iMSDPMovementService.getSupportedModule();
        } catch (RemoteException e) {
            Log.e(TAG, "getSupportedModule error");
            return 0;
        }
    }

    public boolean initEnvironment(String environment) {
        Log.i(TAG, "initEnvironment");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        try {
            boolean isInitSuccess = iMSDPMovementService.initEnvironment(this.mPackageName, environment, getHwOtherParam());
            Log.i(TAG, "initEnvironment result:" + isInitSuccess);
            return isInitSuccess;
        } catch (RemoteException e) {
            Log.e(TAG, "initEnvironment error");
            return false;
        }
    }

    public boolean exitEnvironment(String environment) {
        Log.i(TAG, "exitEnvironment");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        try {
            boolean isExitSuccess = iMSDPMovementService.exitEnvironment(this.mPackageName, environment, getHwOtherParam());
            Log.i(TAG, "exitEnvironment result:" + isExitSuccess);
            return isExitSuccess;
        } catch (RemoteException e) {
            Log.e(TAG, "exitEnvironment error");
            return false;
        }
    }

    public boolean disConnectService() {
        Log.i(TAG, "disConnectService");
        IMSDPMovementService iMSDPMovementService = this.mService;
        if (iMSDPMovementService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        try {
            if (iMSDPMovementService.asBinder() != null) {
                this.mService.asBinder().unlinkToDeath(this.mServiceDeathRecipient, 0);
            }
            unregisterSink();
            this.mContext.unbindService(this.mConnection);
            if (this.mServiceConnection != null) {
                this.isClientConnected = false;
                this.mServiceConnection.onServiceDisconnected(true);
            }
            this.mService = null;
            this.isConnectedMsdp = false;
            this.mConnectCount = 0;
            if (this.mMovementHandler != null) {
                this.mMovementHandler.removeMessages(1);
            }
            Log.i(TAG, "disConnectService true");
        } catch (Exception e) {
            Log.w(TAG, "disConnectService exception.");
        }
        return true;
    }

    private boolean isSystemUser() {
        try {
            Object object = Class.forName("android.os.UserHandle").getMethod("myUserId", new Class[0]).invoke(null, new Object[0]);
            if (!(object instanceof Integer)) {
                return false;
            }
            int userId = ((Integer) object).intValue();
            Log.d(TAG, "user id:" + userId);
            if (userId == 0) {
                return true;
            }
            return false;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException");
            return false;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "NoSuchMethodException");
            return false;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "IllegalAccessException");
            return false;
        } catch (IllegalArgumentException e4) {
            Log.e(TAG, "IllegalArgumentException");
            return false;
        } catch (InvocationTargetException e5) {
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
        } catch (RemoteException e) {
            Log.e(TAG, "IBinder register linkToDeath fail.");
        }
    }

    /* access modifiers changed from: private */
    public class MovementHandler extends Handler {
        MovementHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg == null) {
                Log.w(HwMSDPMovementManager.TAG, "msg is null");
                return;
            }
            int i = msg.what;
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
            Log.i(HwMSDPMovementManager.TAG, "the movement Service has died!");
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
