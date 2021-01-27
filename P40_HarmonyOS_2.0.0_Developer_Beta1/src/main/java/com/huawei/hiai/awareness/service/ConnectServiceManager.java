package com.huawei.hiai.awareness.service;

import android.content.Context;
import com.huawei.hiai.awareness.common.Utils;
import com.huawei.hiai.awareness.log.Logger;
import com.huawei.hiai.awareness.movement.MovementController;
import com.huawei.msdp.movement.HwMSDPMovementChangeEvent;
import com.huawei.msdp.movement.HwMSDPMovementManager;
import com.huawei.msdp.movement.HwMSDPMovementServiceConnection;
import com.huawei.msdp.movement.HwMSDPMovementStatusChangeCallback;
import com.huawei.msdp.movement.HwMSDPOtherParameters;
import com.huawei.msdp.movement.MovementConstant;

public class ConnectServiceManager {
    private static final String TAG = ("sdk_" + ConnectServiceManager.class.getSimpleName());
    private static ConnectServiceManager sInstance;
    private AwarenessServiceConnection mAwarenessServiceConnection = null;
    private Context mConnectServiceManagerContext = null;
    private String[] mEmptyStringArray = new String[0];
    private HwMSDPMovementManager mHwMsdpMovement;
    private HwMSDPMovementServiceConnection mHwMsdpMovementServiceConnection;
    private HwMSDPMovementStatusChangeCallback mHwMsdpMovementStatusChangeCallBack;
    private boolean mIsConMovementServer = false;
    private String[] mMsdpMovementSupportedActivities;
    private int mMsdpSupportModule;
    private String[] mMsdpSupportedEnvironments;

    public ConnectServiceManager() {
        String[] strArr = this.mEmptyStringArray;
        this.mMsdpMovementSupportedActivities = strArr;
        this.mMsdpSupportedEnvironments = strArr;
        this.mMsdpSupportModule = -1;
        this.mHwMsdpMovement = null;
        this.mHwMsdpMovementStatusChangeCallBack = null;
        this.mHwMsdpMovementServiceConnection = null;
    }

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

    public void setAwarenessServiceConnection(AwarenessServiceConnection awarenessServiceConnection) {
        this.mAwarenessServiceConnection = awarenessServiceConnection;
    }

    public int getMovementCapability() {
        int capabilityValue = 0;
        Logger.e(TAG, "getMovementCapability() mIsConMovementServer:" + this.mIsConMovementServer);
        if (!this.mIsConMovementServer) {
            Logger.e(TAG, "getMovementCapability() movement service not connected");
            return -1;
        }
        String[] activities = getMsdpMovementSupportedActivities();
        if (activities == null) {
            Logger.e(TAG, "getMovementCapability() no supported activity");
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

    public String[] getMsdpMovementSupportedActivities() {
        String str = TAG;
        Logger.e(str, "getMsdpMovementSupportedActivities() mIsConMovementServer:" + this.mIsConMovementServer);
        if (this.mIsConMovementServer) {
            this.mMsdpSupportModule = this.mHwMsdpMovement.getSupportedModule();
            String str2 = TAG;
            Logger.e(str2, "getMsdpMovementSupportedActivities() mMSDPSupportModule:" + this.mMsdpSupportModule);
            if ((this.mMsdpSupportModule & 1) == 1) {
                return this.mHwMsdpMovement.getSupportedMovements(1);
            }
        }
        return this.mEmptyStringArray;
    }

    public int getMsdpSupportModule() {
        String str = TAG;
        Logger.e(str, "getMsdpSupportModule() mIsConMovementServer:" + this.mIsConMovementServer);
        if (!this.mIsConMovementServer) {
            return -1;
        }
        this.mMsdpSupportModule = this.mHwMsdpMovement.getSupportedModule();
        return this.mMsdpSupportModule;
    }

    public String[] getMsdpSupportedEnvironments() {
        String str = TAG;
        Logger.e(str, "getMsdpSupportedEnvironments() mIsConMovementServer:" + this.mIsConMovementServer);
        if (this.mIsConMovementServer) {
            this.mMsdpSupportModule = this.mHwMsdpMovement.getSupportedModule();
            String str2 = TAG;
            Logger.d(str2, "getMsdpSupportedEnvironments() mMSDPSupportModule:" + this.mMsdpSupportModule);
            if ((this.mMsdpSupportModule & 2) == 2) {
                return this.mHwMsdpMovement.getSupportedMovements(2);
            }
        }
        return this.mEmptyStringArray;
    }

    public void initialize(Context context) {
        Logger.d(TAG, "initialize()");
        if (this.mHwMsdpMovement == null) {
            this.mHwMsdpMovement = new HwMSDPMovementManager(context);
        }
        initMsdpMovementServiceConnection();
        initMsdpMovementStatusChangeCallBack();
    }

    private void initMsdpMovementServiceConnection() {
        if (this.mHwMsdpMovementServiceConnection == null) {
            this.mHwMsdpMovementServiceConnection = new HwMSDPMovementServiceConnection() {
                /* class com.huawei.hiai.awareness.service.ConnectServiceManager.AnonymousClass1 */

                @Override // com.huawei.msdp.movement.HwMSDPMovementServiceConnection
                public void onServiceConnected() {
                    ConnectServiceManager.this.mIsConMovementServer = true;
                    Logger.d(ConnectServiceManager.TAG, "movement onServiceConnected()");
                    ConnectServiceManager connectServiceManager = ConnectServiceManager.this;
                    connectServiceManager.mMsdpSupportModule = connectServiceManager.mHwMsdpMovement.getSupportedModule();
                    Logger.d(ConnectServiceManager.TAG, "movement onServiceConnected() mMSDPSupportModule:" + ConnectServiceManager.this.mMsdpSupportModule);
                    if ((ConnectServiceManager.this.mMsdpSupportModule & 1) == 1) {
                        ConnectServiceManager connectServiceManager2 = ConnectServiceManager.this;
                        connectServiceManager2.mMsdpMovementSupportedActivities = connectServiceManager2.mHwMsdpMovement.getSupportedMovements(1);
                        String[] strArr = ConnectServiceManager.this.mMsdpMovementSupportedActivities;
                        int i = 0;
                        for (String action : strArr) {
                            i++;
                            Logger.d(ConnectServiceManager.TAG, "movement onServiceConnected() i = " + i + " action = " + action);
                        }
                    }
                    if ((ConnectServiceManager.this.mMsdpSupportModule & 2) == 2) {
                        ConnectServiceManager connectServiceManager3 = ConnectServiceManager.this;
                        connectServiceManager3.mMsdpSupportedEnvironments = connectServiceManager3.mHwMsdpMovement.getSupportedMovements(1);
                        int i2 = 0;
                        for (String actionEnvironment : ConnectServiceManager.this.mMsdpSupportedEnvironments) {
                            i2++;
                            Logger.d(ConnectServiceManager.TAG, "movement onServiceConnected() i = " + i2 + " actionEnvironment = " + actionEnvironment);
                        }
                    }
                    if (ConnectServiceManager.this.mAwarenessServiceConnection != null) {
                        ConnectServiceManager.this.mAwarenessServiceConnection.onServiceConnected();
                    }
                }

                @Override // com.huawei.msdp.movement.HwMSDPMovementServiceConnection
                public void onServiceDisconnected(Boolean var1) {
                    String str = ConnectServiceManager.TAG;
                    Logger.d(str, "movement onServiceDisconnected() var1 = " + var1);
                    ConnectServiceManager.this.mIsConMovementServer = false;
                    if (ConnectServiceManager.this.mAwarenessServiceConnection != null && !var1.booleanValue()) {
                        ConnectServiceManager.this.mAwarenessServiceConnection.onServiceDisconnected();
                    }
                }
            };
        }
    }

    private void initMsdpMovementStatusChangeCallBack() {
        if (this.mHwMsdpMovementStatusChangeCallBack == null) {
            this.mHwMsdpMovementStatusChangeCallBack = new HwMSDPMovementStatusChangeCallback() {
                /* class com.huawei.hiai.awareness.service.ConnectServiceManager.AnonymousClass2 */

                @Override // com.huawei.msdp.movement.HwMSDPMovementStatusChangeCallback
                public void onMovementStatusChanged(int type, HwMSDPMovementChangeEvent var1) {
                    if (var1 == null) {
                        Logger.e(ConnectServiceManager.TAG, "onMovementStatusChanged() var1 == null");
                        return;
                    }
                    String str = ConnectServiceManager.TAG;
                    Logger.d(str, "onMovementStatusChanged() type is:" + type);
                    if (type == 0 || type == 2) {
                        Logger.d(ConnectServiceManager.TAG, "onMovementStatusChanged()");
                        if (ConnectServiceManager.this.mMsdpMovementSupportedActivities == null || ConnectServiceManager.this.mMsdpMovementSupportedActivities.length == 0) {
                            Logger.d(ConnectServiceManager.TAG, "onMovementStatusChanged() mMSDPMovementSupportedActivities == null ");
                            ConnectServiceManager connectServiceManager = ConnectServiceManager.this;
                            connectServiceManager.mMsdpSupportModule = connectServiceManager.mHwMsdpMovement.getSupportedModule();
                            String str2 = ConnectServiceManager.TAG;
                            Logger.d(str2, "onMovementStatusChanged() mMSDPSupportModule:" + ConnectServiceManager.this.mMsdpSupportModule);
                            if ((ConnectServiceManager.this.mMsdpSupportModule & 1) == 1) {
                                ConnectServiceManager connectServiceManager2 = ConnectServiceManager.this;
                                connectServiceManager2.mMsdpMovementSupportedActivities = connectServiceManager2.mHwMsdpMovement.getSupportedMovements(1);
                            }
                        }
                        if (ConnectServiceManager.this.mMsdpMovementSupportedActivities == null || ConnectServiceManager.this.mMsdpMovementSupportedActivities.length == 0) {
                            Logger.e(ConnectServiceManager.TAG, "onMovementStatusChanged() mMSDPMovementSupportedActivities == null again! ");
                        } else {
                            MovementController.getInstance().onMovementStatusChanged(var1);
                        }
                    } else {
                        Logger.d(ConnectServiceManager.TAG, "onMovementStatusChanged() type is not support!");
                    }
                }
            };
        }
    }

    public boolean onStart() {
        return startConnectService();
    }

    private boolean startConnectService() {
        if (!this.mIsConMovementServer && this.mHwMsdpMovement != null) {
            Logger.d(TAG, "startConnectService()");
            if (!this.mHwMsdpMovement.connectService(this.mHwMsdpMovementStatusChangeCallBack, this.mHwMsdpMovementServiceConnection)) {
                Logger.e(TAG, "startConnectService() return fail");
            } else {
                Logger.d(TAG, "startConnectService() return success");
            }
        }
        String str = TAG;
        Logger.d(str, "startConnectService() mIsConMovementServer = " + this.mIsConMovementServer);
        return this.mIsConMovementServer;
    }

    public boolean stopConnectService() {
        boolean isSuccess = true;
        if (this.mIsConMovementServer && this.mHwMsdpMovement != null) {
            Logger.d(TAG, "stopConnectService() ");
            isSuccess = this.mHwMsdpMovement.disConnectService();
            if (!isSuccess) {
                Logger.e(TAG, "stopConnectService() fail ");
            } else {
                Logger.d(TAG, "stopConnectService() success ");
                this.mIsConMovementServer = false;
            }
        }
        return isSuccess;
    }

    public void enableMovementEvent(String activity, int movementType, long screenOnReportPeriod, HwMSDPOtherParameters hwMsdpOtherParameters) {
        String str = TAG;
        Logger.d(str, "enableMovementEvent() activity:" + activity + " movementType:" + movementType);
        if (activity == null) {
            Logger.e(TAG, "enableMovementEvent() activity == null ");
            return;
        }
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMsdpMovement;
        if (hwMSDPMovementManager == null) {
            Logger.e(TAG, "enableMovementEvent() mHwMsdpMovement == null ");
            return;
        }
        hwMSDPMovementManager.enableMovementEvent(movementType, activity, 1, screenOnReportPeriod, hwMsdpOtherParameters);
        this.mHwMsdpMovement.enableMovementEvent(movementType, activity, 2, screenOnReportPeriod, hwMsdpOtherParameters);
    }

    public void disableMovementEvent(String activity, int movementType) {
        String str = TAG;
        Logger.d(str, " disableMovementEvent() activity:" + activity + " movementType:" + movementType);
        if (activity == null) {
            Logger.e(TAG, "disableMovementEvent() activity == null ");
            return;
        }
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMsdpMovement;
        if (hwMSDPMovementManager == null) {
            Logger.e(TAG, "disableMovementEvent() mHwMsdpMovement == null ");
            return;
        }
        hwMSDPMovementManager.disableMovementEvent(movementType, activity, 1);
        this.mHwMsdpMovement.disableMovementEvent(movementType, activity, 2);
    }

    public boolean isIntegrateSensorHub() {
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMsdpMovement;
        if (hwMSDPMovementManager != null) {
            String serviceVersion = hwMSDPMovementManager.getServiceVersion();
            String str = TAG;
            Logger.d(str, "isIntegradeSensorHub() serviceVersion : " + serviceVersion);
            if (Utils.isApkVersionSatisfied(MovementConstant.FLAG_MSDP9_1, serviceVersion)) {
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

    public boolean isConnectMsdpMovementServer() {
        String str = TAG;
        Logger.d(str, "isConnectMsdpMovementServer() mIsConMovementServer:" + this.mIsConMovementServer);
        return this.mIsConMovementServer;
    }
}
