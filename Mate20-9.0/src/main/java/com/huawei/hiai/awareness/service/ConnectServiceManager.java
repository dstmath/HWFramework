package com.huawei.hiai.awareness.service;

import android.content.Context;
import com.huawei.hiai.awareness.common.log.LogUtil;
import com.huawei.hiai.awareness.movement.MovementController;
import com.huawei.msdp.movement.HwMSDPMovementChangeEvent;
import com.huawei.msdp.movement.HwMSDPMovementManager;
import com.huawei.msdp.movement.HwMSDPMovementServiceConnection;
import com.huawei.msdp.movement.HwMSDPMovementStatusChangeCallback;
import com.huawei.msdp.movement.HwMSDPOtherParameters;
import com.huawei.msdp.movement.MovementConstant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectServiceManager {
    /* access modifiers changed from: private */
    public static final String TAG = ("sdk_" + ConnectServiceManager.class.getSimpleName());
    private static ConnectServiceManager sInstance;
    private Context mConnectServiceManagerContext = null;
    private String[] mEmptyStringArray = new String[0];
    /* access modifiers changed from: private */
    public HwMSDPMovementManager mHwMSDPMovement = null;
    private HwMSDPMovementServiceConnection mHwMSDPMovementServiceConnection = null;
    private HwMSDPMovementStatusChangeCallback mHwMSDPMovementStatusChangeCallBack = null;
    boolean mIsConDevStatusServer = false;
    boolean mIsConMovementServer = false;
    /* access modifiers changed from: private */
    public String[] mMSDPMovementSupportedActivities = this.mEmptyStringArray;
    /* access modifiers changed from: private */
    public int mMSDPSupportModule = -1;
    /* access modifiers changed from: private */
    public String[] mMSDPSupportedEnvironments = this.mEmptyStringArray;
    ConcurrentHashMap<String, Integer> mMovementMap;

    public static synchronized ConnectServiceManager getInstance() {
        ConnectServiceManager connectServiceManager;
        synchronized (ConnectServiceManager.class) {
            if (sInstance == null) {
                sInstance = new ConnectServiceManager();
            }
            connectServiceManager = sInstance;
        }
        return connectServiceManager;
    }

    public int getMovementCapability() {
        int capabilityValue = 0;
        LogUtil.e(TAG, "getMovementCapability() mIsConMovementServer:" + this.mIsConMovementServer);
        if (!this.mIsConMovementServer) {
            LogUtil.e(TAG, "getMovementCapability() movement service not connected");
            return -1;
        }
        String[] activities = getmMSDPMovementSupportedActivities();
        if (activities == null) {
            LogUtil.e(TAG, "getMovementCapability() no supported activity");
            return 0;
        }
        for (String tmp : activities) {
            MovementController.getInstance();
            if (MovementController.getDefaultMovementActionConfigMap().containsKey(tmp)) {
                MovementController.getInstance();
                capabilityValue |= MovementController.getDefaultMovementActionConfigMap().get(tmp).intValue();
            }
        }
        return capabilityValue;
    }

    public String[] getmMSDPMovementSupportedActivities() {
        LogUtil.e(TAG, "getmMSDPMovementSupportedActivities() mIsConMovementServer:" + this.mIsConMovementServer);
        if (this.mIsConMovementServer) {
            this.mMSDPSupportModule = this.mHwMSDPMovement.getSupportedModule();
            LogUtil.e(TAG, "getmMSDPMovementSupportedActivities() mMSDPSupportModule:" + this.mMSDPSupportModule);
            if (1 == (this.mMSDPSupportModule & 1)) {
                return this.mHwMSDPMovement.getSupportedMovements(1);
            }
        }
        return this.mEmptyStringArray;
    }

    public int getmMSDPSupportModule() {
        LogUtil.e(TAG, "getmMSDPSupportModule() mIsConMovementServer:" + this.mIsConMovementServer);
        if (!this.mIsConMovementServer) {
            return -1;
        }
        this.mMSDPSupportModule = this.mHwMSDPMovement.getSupportedModule();
        return this.mMSDPSupportModule;
    }

    public String[] getmMSDPSupportedEnvironments() {
        LogUtil.e(TAG, "getmMSDPSupportedEnvironments() mIsConMovementServer:" + this.mIsConMovementServer);
        if (this.mIsConMovementServer) {
            this.mMSDPSupportModule = this.mHwMSDPMovement.getSupportedModule();
            LogUtil.d(TAG, "getmMSDPSupportedEnvironments() mMSDPSupportModule:" + this.mMSDPSupportModule);
            if (2 == (this.mMSDPSupportModule & 2)) {
                return this.mHwMSDPMovement.getSupportedMovements(2);
            }
        }
        return this.mEmptyStringArray;
    }

    public void initialize(Context context) {
        if (this.mHwMSDPMovement == null) {
            this.mHwMSDPMovement = new HwMSDPMovementManager(context);
        }
        if (this.mHwMSDPMovementServiceConnection == null) {
            this.mHwMSDPMovementServiceConnection = new HwMSDPMovementServiceConnection() {
                public void onServiceConnected() {
                    ConnectServiceManager.this.mIsConMovementServer = true;
                    LogUtil.d(ConnectServiceManager.TAG, "onServiceConnected() Movement service connected");
                    int unused = ConnectServiceManager.this.mMSDPSupportModule = ConnectServiceManager.this.mHwMSDPMovement.getSupportedModule();
                    LogUtil.d(ConnectServiceManager.TAG, "onServiceConnected() mMSDPSupportModule:" + ConnectServiceManager.this.mMSDPSupportModule);
                    if (1 == (ConnectServiceManager.this.mMSDPSupportModule & 1)) {
                        String[] unused2 = ConnectServiceManager.this.mMSDPMovementSupportedActivities = ConnectServiceManager.this.mHwMSDPMovement.getSupportedMovements(1);
                        if (ConnectServiceManager.this.mMSDPMovementSupportedActivities != null) {
                            int length = ConnectServiceManager.this.mMSDPMovementSupportedActivities.length;
                            for (int i = 0; i < length; i++) {
                                LogUtil.d(ConnectServiceManager.TAG, "onServiceConnected() i = " + i + " mMSDPMovementSupportedActivities[i] = " + ConnectServiceManager.this.mMSDPMovementSupportedActivities[i]);
                            }
                        }
                    }
                    if (2 == (ConnectServiceManager.this.mMSDPSupportModule & 2)) {
                        String[] unused3 = ConnectServiceManager.this.mMSDPSupportedEnvironments = ConnectServiceManager.this.mHwMSDPMovement.getSupportedMovements(1);
                        if (ConnectServiceManager.this.mMSDPSupportedEnvironments != null) {
                            for (String initEnvironment : ConnectServiceManager.this.mMSDPSupportedEnvironments) {
                                ConnectServiceManager.this.mHwMSDPMovement.initEnvironment(initEnvironment);
                                LogUtil.d(ConnectServiceManager.TAG, "onServiceConnected() i = " + i + " mMSDPSupportedEnvironments[i] = " + ConnectServiceManager.this.mMSDPSupportedEnvironments[i]);
                            }
                        }
                    }
                }

                public void onServiceDisconnected(Boolean var1) {
                    ConnectServiceManager.this.mIsConMovementServer = false;
                    LogUtil.d(ConnectServiceManager.TAG, "onServiceDisconnected() Movement service Disconnected");
                }
            };
        }
        if (this.mHwMSDPMovementStatusChangeCallBack == null) {
            this.mHwMSDPMovementStatusChangeCallBack = new HwMSDPMovementStatusChangeCallback() {
                public void onMovementStatusChanged(int type, HwMSDPMovementChangeEvent var1) {
                    LogUtil.d(ConnectServiceManager.TAG, "onMovementStatusChanged() type is:" + type);
                    if (type == 0 || 2 == type) {
                        LogUtil.d(ConnectServiceManager.TAG, "onMovementStatusChanged()");
                        if (ConnectServiceManager.this.mMSDPMovementSupportedActivities == null || ConnectServiceManager.this.mMSDPMovementSupportedActivities.length == 0) {
                            LogUtil.d(ConnectServiceManager.TAG, "onMovementStatusChanged() mMSDPMovementSupportedActivities == null ");
                            int unused = ConnectServiceManager.this.mMSDPSupportModule = ConnectServiceManager.this.mHwMSDPMovement.getSupportedModule();
                            LogUtil.d(ConnectServiceManager.TAG, "onMovementStatusChanged() mMSDPSupportModule:" + ConnectServiceManager.this.mMSDPSupportModule);
                            if (1 == (ConnectServiceManager.this.mMSDPSupportModule & 1)) {
                                String[] unused2 = ConnectServiceManager.this.mMSDPMovementSupportedActivities = ConnectServiceManager.this.mHwMSDPMovement.getSupportedMovements(1);
                            }
                        }
                        if (ConnectServiceManager.this.mMSDPMovementSupportedActivities == null || ConnectServiceManager.this.mMSDPMovementSupportedActivities.length == 0) {
                            LogUtil.e(ConnectServiceManager.TAG, "onMovementStatusChanged() mMSDPMovementSupportedActivities == null 2! ");
                        } else {
                            MovementController.getInstance().onMovementStatusChanged(var1);
                        }
                    } else {
                        LogUtil.d(ConnectServiceManager.TAG, "onMovementStatusChanged() type is not support!");
                    }
                }
            };
        }
    }

    public boolean onStart() {
        return startConnectService();
    }

    private boolean startConnectService() {
        if (!this.mIsConMovementServer && this.mHwMSDPMovement != null) {
            LogUtil.d(TAG, " startConnectService() mHwMSDPMovement");
            if (!this.mHwMSDPMovement.connectService(this.mHwMSDPMovementStatusChangeCallBack, this.mHwMSDPMovementServiceConnection)) {
                LogUtil.e(TAG, " startConnectService() mHwMSDPMovement fail ");
            } else {
                LogUtil.d(TAG, " startConnectService() mHwMSDPMovement success ");
            }
        }
        LogUtil.d(TAG, " startConnectService():MovementServer=" + this.mIsConMovementServer);
        return this.mIsConMovementServer;
    }

    public boolean stopConnectService() {
        boolean isSuccess = true;
        if (this.mIsConMovementServer && this.mHwMSDPMovement != null) {
            LogUtil.d(TAG, " stopConnectService() mHwMSDPMovement");
            isSuccess = this.mHwMSDPMovement.disConnectService();
            if (!isSuccess) {
                LogUtil.e(TAG, " stopConnectService() mHwMSDPMovement fail ");
            } else {
                LogUtil.d(TAG, " stopConnectService() mHwMSDPMovement success ");
            }
        }
        return isSuccess;
    }

    public void enableMovementEvent(ConcurrentHashMap<String, Integer> map) {
        LogUtil.d(TAG, " enableMovementEvent() ");
        if (map == null) {
            LogUtil.e(TAG, " enableMovementEvent() map == null ");
        } else if (this.mMSDPMovementSupportedActivities == null) {
            this.mMovementMap = map;
        } else if (this.mHwMSDPMovement == null) {
            LogUtil.e(TAG, " enableMovementEvent() mHwMSDPMovement == null ");
        } else {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String curActivity = entry.getKey();
                if (curActivity != null) {
                    this.mHwMSDPMovement.enableMovementEvent(1, curActivity, 1, 200000000000L, new HwMSDPOtherParameters(1.0d, 0.0d, 0.0d, 0.0d, ""));
                    this.mHwMSDPMovement.enableMovementEvent(1, curActivity, 2, 200000000000L, new HwMSDPOtherParameters(1.0d, 0.0d, 0.0d, 0.0d, ""));
                }
            }
        }
    }

    public void enableMovementEvent(String activity, int movementType, Long screenOnReportPeriod, HwMSDPOtherParameters hwMSDPOtherParameters) {
        LogUtil.d(TAG, " enableMovementEvent() activity:" + activity + "\t movementtype:" + movementType);
        if (activity == null) {
            LogUtil.e(TAG, " enableMovementEvent() activity == null ");
        } else if (this.mHwMSDPMovement == null) {
            LogUtil.e(TAG, " enableMovementEvent() mHwMSDPMovement == null ");
        } else {
            this.mHwMSDPMovement.enableMovementEvent(movementType, activity, 1, screenOnReportPeriod.longValue(), hwMSDPOtherParameters);
            this.mHwMSDPMovement.enableMovementEvent(movementType, activity, 2, screenOnReportPeriod.longValue(), hwMSDPOtherParameters);
        }
    }

    public void diableMovementEvent(String activity, int movementType) {
        LogUtil.d(TAG, " disenableMovementEvent() activity:" + activity + "\t movementtype:" + movementType);
        if (activity == null) {
            LogUtil.e(TAG, " disenableMovementEvent() activity == null ");
        } else if (this.mHwMSDPMovement == null) {
            LogUtil.e(TAG, " enableMovementEvent() mHwMSDPMovement == null ");
        } else {
            this.mHwMSDPMovement.disableMovementEvent(movementType, activity, 1);
            this.mHwMSDPMovement.disableMovementEvent(movementType, activity, 2);
        }
    }

    public void onDestroy() {
        stopConnectService();
    }

    public boolean isIntegradeSensorHub() {
        if (this.mHwMSDPMovement != null) {
            String MSDPserviceVersion = this.mHwMSDPMovement.getServiceVersion();
            LogUtil.d(TAG, "mHwMSDPMovement.getServiceVersion():" + MSDPserviceVersion);
            if (MSDPserviceVersion != null && MovementConstant.FLAG_MSDP9_1.compareToIgnoreCase(MSDPserviceVersion) <= 0) {
                return true;
            }
        }
        return false;
    }

    public synchronized void setConnectServiceManagerContext(Context connectServiceManagerContext) {
        this.mConnectServiceManagerContext = connectServiceManagerContext;
    }

    public synchronized Context getConnectServiceManagerContext() {
        return this.mConnectServiceManagerContext;
    }
}
