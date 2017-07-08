package com.android.server.mtm.condition;

import android.os.Bundle;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.security.trustcircle.IOTController;

public class ConditionMatchorImp implements ConditionMatchor {
    private static final boolean DEBUG = false;
    private static final String TAG = "ConditionMatchorImp";

    public static ConditionMatchor getConditionMatchor(int conditionType) {
        switch (conditionType) {
            case IOTController.TYPE_MASTER /*1000*/:
                return CombinedConditionMatchor.getInstance();
            case IOTController.TYPE_SLAVE /*1001*/:
            case EventTracker.TRACK_TYPE_KILL /*1002*/:
            case EventTracker.TRACK_TYPE_TRIG /*1003*/:
            case EventTracker.TRACK_TYPE_END /*1004*/:
                return AppTypeConditionMatchor.getInstance();
            case EventTracker.TRACK_TYPE_STOP /*1005*/:
            case HwPackageManagerService.transaction_sendLimitedPackageBroadcast /*1006*/:
            case HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST /*1007*/:
            case HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED /*1008*/:
                return GroupConditionMatchor.getInstance();
            case HwPackageManagerService.TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP /*1009*/:
            case ConditionMatchor.PACKAGENAMECONTAINS /*1012*/:
                return PackageConditionMatchor.getInstance();
            case ConditionMatchor.PROCESSNAME /*1010*/:
            case ConditionMatchor.PROCESSNAMECONTAINS /*1013*/:
                return ProcessConditionMatchor.getInstance();
            default:
                return null;
        }
    }

    public int conditionMatch(int conditiontype, Bundle args) {
        return 0;
    }
}
