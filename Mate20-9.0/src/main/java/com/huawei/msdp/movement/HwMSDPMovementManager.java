package com.huawei.msdp.movement;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.msdp.movement.IMSDPMovementService;
import com.huawei.msdp.movement.IMSDPMovementStatusChangeCallBack;

public class HwMSDPMovementManager {
    private static final String AIDL_MESSAGE_SERVICE_CLASS = "com.huawei.msdp.movement.HwMSDPMovementService";
    private static final String AIDL_MESSAGE_SERVICE_PACKAGE = "com.huawei.msdp";
    private static final int BINDSERVICE = 1;
    private static final int CONNECTTIMES = 10;
    private static final int COUNTTIMES = 5;
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    private static final int PROCESSMODULE = 2;
    private static final String SDKVERSION = "1.0.8";
    private static final String TAG = "HwMSDPMovementManager";
    private static int module = 0;
    private boolean DEBUG;
    /* access modifiers changed from: private */
    public ServiceDeathHandler deathHandler;
    private int mConnectCount;
    private ServiceConnection mConnection;
    private Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public IMSDPMovementService mService;
    /* access modifiers changed from: private */
    public HwMSDPMovementServiceConnection mServiceConnection;
    private IMSDPMovementStatusChangeCallBack mSink;
    private int moduleCount;
    private String packageName;

    private class ServiceDeathHandler implements IBinder.DeathRecipient {
        public ServiceDeathHandler() {
        }

        public void binderDied() {
            Log.i(HwMSDPMovementManager.TAG, "the movement Service has died !");
            if (HwMSDPMovementManager.this.mServiceConnection != null) {
                HwMSDPMovementManager.this.mServiceConnection.onServiceDisconnected(false);
            }
            if (HwMSDPMovementManager.this.mService != null) {
                HwMSDPMovementManager.this.mService.asBinder().unlinkToDeath(HwMSDPMovementManager.this.deathHandler, 0);
                IMSDPMovementService unused = HwMSDPMovementManager.this.mService = null;
            }
        }
    }

    public HwMSDPMovementManager(Context context) {
        this.DEBUG = Log.isLoggable(TAG, 3);
        this.mService = null;
        this.mConnectCount = 0;
        this.moduleCount = 0;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        HwMSDPMovementManager.this.bindService();
                        return;
                    case 2:
                        HwMSDPMovementManager.this.processModule();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(HwMSDPMovementManager.TAG, "onServiceConnected");
                HwMSDPMovementManager.this.mHandler.removeMessages(1);
                IMSDPMovementService unused = HwMSDPMovementManager.this.mService = IMSDPMovementService.Stub.asInterface(service);
                boolean unused2 = HwMSDPMovementManager.this.registerSink();
                HwMSDPMovementManager.this.notifyServiceDied();
                HwMSDPMovementManager.this.processModule();
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.e(HwMSDPMovementManager.TAG, "onServiceDisconnected");
                HwMSDPMovementManager.this.mServiceConnection.onServiceDisconnected(true);
            }
        };
        this.DEBUG = Log.isLoggable(TAG, 3);
        if (context != null) {
            this.mContext = context;
            this.packageName = context.getPackageName();
            this.deathHandler = new ServiceDeathHandler();
        }
    }

    /* access modifiers changed from: private */
    public void processModule() {
        Log.i(TAG, "processModule");
        if (this.moduleCount <= 5) {
            module = getSupportedModule();
            if (module == 0) {
                this.moduleCount++;
                this.mHandler.sendEmptyMessageDelayed(2, 200);
            } else if (this.mServiceConnection != null) {
                this.mServiceConnection.onServiceConnected();
                this.mHandler.removeMessages(2);
            }
        } else if (this.mServiceConnection != null) {
            this.mServiceConnection.onServiceDisconnected(false);
        }
    }

    /* access modifiers changed from: private */
    public void bindService() {
        if (this.mConnectCount > 10) {
            Log.e(TAG, "try connect 10 times, connection fail");
            return;
        }
        Intent bindIntent = new Intent();
        bindIntent.setClassName("com.huawei.msdp", AIDL_MESSAGE_SERVICE_CLASS);
        this.mContext.bindService(bindIntent, this.mConnection, 1);
        this.mConnectCount++;
        this.mHandler.sendEmptyMessageDelayed(1, 2000);
    }

    public boolean connectService(HwMSDPMovementStatusChangeCallback sink, HwMSDPMovementServiceConnection connection) {
        Log.i(TAG, "HwMSDPMovementSDK Version = 1.0.8");
        if (connection == null || sink == null) {
            return false;
        }
        this.mServiceConnection = connection;
        this.mSink = createMSDPMovementSink(sink);
        Log.i(TAG, "bindService ");
        this.mConnectCount = 0;
        this.moduleCount = 0;
        bindService();
        return true;
    }

    public String getServiceVersion() {
        Log.i(TAG, "getServiceVersion");
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return "";
        }
        try {
            return this.mService.getServcieVersion();
        } catch (RemoteException re) {
            Log.e(TAG, "getServiceVersion error:" + re.getMessage());
            return "";
        } catch (RuntimeException se) {
            Log.e(TAG, "getServiceVersion error:" + se.getMessage());
            return "";
        }
    }

    public String[] getSupportedMovements(int type) {
        Log.i(TAG, "getSupportedMovements");
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return new String[0];
        }
        try {
            return this.mService.getSupportedMovements(type);
        } catch (RemoteException re) {
            Log.e(TAG, "getSupportedMovements error:" + re.getMessage());
            return new String[0];
        } catch (RuntimeException se) {
            Log.e(TAG, "getSupportedMovements error:" + se.getMessage());
            return new String[0];
        }
    }

    public boolean enableMovementEvent(int type, String movement, int eventType, long reportLatencyNs, HwMSDPOtherParameters parameters) {
        Log.i(TAG, "enableMovementEvent  type =" + type);
        if (this.DEBUG) {
            Log.d(TAG, "enableMovementEvent");
        }
        boolean result = false;
        if (TextUtils.isEmpty(movement) || reportLatencyNs < 0) {
            Log.e(TAG, "activity is null or reportLatencyNs < 0");
            return false;
        }
        if (this.DEBUG) {
            Log.d(TAG, movement + "," + eventType + "," + reportLatencyNs);
        }
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        if (type == 0) {
            try {
                result = this.mService.enableMovementEvent(type, this.packageName, movement, eventType, reportLatencyNs, parameters);
            } catch (RemoteException re) {
                Log.e(TAG, "enableMovementEvent error:" + re.getMessage());
            } catch (RuntimeException se) {
                Log.e(TAG, "enableMovementEvent error:" + se.getMessage());
            }
        } else if (1 == type) {
            result = this.mService.enableMovementEvent(type, this.packageName, movement, eventType, reportLatencyNs, parameters);
        } else if (2 == type) {
            result = this.mService.enableMovementEvent(type, this.packageName, movement, eventType, reportLatencyNs, getHwMSDPOtherParam());
        } else {
            Log.e(TAG, "unknown movement type  [ " + type + " ]");
        }
        return result;
    }

    private HwMSDPOtherParameters getHwMSDPOtherParam() {
        long currentTime = System.currentTimeMillis();
        return new HwMSDPOtherParameters((double) (4294967295L & currentTime), (double) (currentTime >> 32), 0.0d, 0.0d, "");
    }

    public boolean disableMovementEvent(int type, String movement, int eventType) {
        Log.i(TAG, "disableMovementEvent type =" + type);
        boolean result = false;
        if (TextUtils.isEmpty(movement)) {
            Log.e(TAG, "activity is null.");
            return false;
        }
        Log.d(TAG, movement + "," + eventType);
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        if (type == 0 || 1 == type || 2 == type) {
            try {
                result = this.mService.disableMovementEvent(type, this.packageName, movement, eventType);
            } catch (RemoteException re) {
                Log.e(TAG, "disableMovementEvent error:" + re.getMessage());
            } catch (RuntimeException se) {
                Log.e(TAG, "disableMovementEvent error:" + se.getMessage());
            }
        } else {
            Log.e(TAG, "unknown movement type [" + type + " ]");
        }
        return result;
    }

    /* access modifiers changed from: private */
    public boolean registerSink() {
        Log.i(TAG, "registerSink");
        boolean result = false;
        if (this.mService == null || this.mSink == null) {
            Log.e(TAG, "mService or mSink is null.");
            return result;
        }
        try {
            result = this.mService.registerSink(this.packageName, this.mSink);
        } catch (RemoteException re) {
            Log.e(TAG, "registerSink error:" + re.getMessage());
        } catch (RuntimeException se) {
            Log.e(TAG, "registerSink error:" + se.getMessage());
        }
        return result;
    }

    private boolean unregisterSink() {
        Log.i(TAG, "unregisterSink");
        boolean result = false;
        if (this.mService == null || this.mSink == null) {
            Log.e(TAG, "mService or mSink is null.");
            return result;
        }
        try {
            result = this.mService.unregisterSink(this.packageName, this.mSink);
        } catch (RemoteException re) {
            Log.e(TAG, "unregisterSink error:" + re.getMessage());
        } catch (RuntimeException se) {
            Log.e(TAG, "unregisterSink error:" + se.getMessage());
        }
        return result;
    }

    public HwMSDPMovementChangeEvent getCurrentMovement(int type) {
        Log.i(TAG, "getCurrentMovement type " + type);
        if (this.DEBUG) {
            Log.d(TAG, "getCurrentMovement");
        }
        HwMSDPMovementChangeEvent activity = null;
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return activity;
        }
        try {
            activity = this.mService.getCurrentMovement(type, this.packageName);
        } catch (RemoteException re) {
            Log.e(TAG, "getCurrentMovement error:" + re.getMessage());
        } catch (RuntimeException se) {
            Log.e(TAG, "getCurrentMovement error:" + se.getMessage());
        }
        return activity;
    }

    public boolean flush() {
        Log.i(TAG, "flush");
        boolean result = false;
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return result;
        }
        try {
            result = this.mService.flush();
        } catch (RemoteException re) {
            Log.e(TAG, "flush error:" + re.getMessage());
        } catch (RuntimeException se) {
            Log.e(TAG, "flush error:" + se.getMessage());
        }
        Log.d(TAG, "flush =" + result);
        return result;
    }

    public int getSupportedModule() {
        Log.i(TAG, "getSupportedModule");
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return 0;
        }
        try {
            int result = this.mService.getSupportedModule();
            Log.d(TAG, "getSupportedModule =" + result);
            return result;
        } catch (RemoteException re) {
            Log.e(TAG, "getSupportedModule error:" + re.getMessage());
            return 0;
        } catch (RuntimeException se) {
            Log.e(TAG, "getSupportedModule error:" + se.getMessage());
            return 0;
        }
    }

    public boolean initEnvironment(String environment) {
        Log.i(TAG, "initEnvironment");
        boolean result = false;
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return result;
        }
        try {
            boolean result2 = this.mService.initEnvironment(this.packageName, environment, getHwMSDPOtherParam());
            Log.e(TAG, "initEnvironment result:" + result2);
            return result2;
        } catch (RemoteException re) {
            Log.e(TAG, "initEnvironment error:" + re.getMessage());
            return result;
        } catch (RuntimeException se) {
            Log.e(TAG, "initEnvironment error:" + se.getMessage());
            return result;
        }
    }

    public boolean exitEnvironment(String environment) {
        boolean z = false;
        Log.i(TAG, "exitEnvironment");
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return z;
        }
        try {
            return this.mService.exitEnvironment(this.packageName, environment, getHwMSDPOtherParam());
        } catch (RemoteException re) {
            Log.e(TAG, "exitEnvironment error:" + re.getMessage());
            return z;
        } catch (RuntimeException se) {
            Log.e(TAG, "exitEnvironment error:" + se.getMessage());
            return z;
        }
    }

    public boolean disConnectService() {
        Log.i(TAG, "disConnectService");
        if (this.mService == null) {
            Log.e(TAG, "mService is null.");
            return false;
        }
        this.mService.asBinder().unlinkToDeath(this.deathHandler, 0);
        this.mContext.unbindService(this.mConnection);
        this.mServiceConnection.onServiceDisconnected(true);
        unregisterSink();
        this.mService = null;
        this.mConnectCount = 0;
        this.mHandler.removeMessages(1);
        Log.i(TAG, "disConnectService true");
        return true;
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

    private static IMSDPMovementStatusChangeCallBack createMSDPMovementSink(final HwMSDPMovementStatusChangeCallback sink) {
        if (sink == null) {
            return null;
        }
        return new IMSDPMovementStatusChangeCallBack.Stub() {
            public void onActivityChanged(int type, HwMSDPMovementChangeEvent event) throws RemoteException {
                sink.onMovementStatusChanged(type, event);
            }
        };
    }
}
