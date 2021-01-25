package com.huawei.coauth.auth;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import java.util.List;

/* access modifiers changed from: package-private */
public class CoAuthCheckUtil {
    private static final String TAG = CoAuth.class.getName();

    private CoAuthCheckUtil() {
    }

    public static boolean connectServiceCheck(Context context) {
        if (context != null && context.getApplicationContext() != null && context.getApplicationContext().getPackageName() != null) {
            return true;
        }
        Log.e(TAG, "connectService request failed, empty context");
        return false;
    }

    public static boolean disconnectServiceCheck(Context context) {
        if (context != null) {
            return true;
        }
        Log.e(TAG, "disconnectService request failed, empty context");
        return false;
    }

    public static boolean createCoAuthPairGroupCheck(String moduleName, CoAuthDevice peerDevice) {
        if (moduleName == null || moduleName.isEmpty()) {
            Log.e(TAG, "createCoAuthPairGroup request failed, empty moduleName");
            return false;
        } else if (coAuthDeviceCheck(peerDevice)) {
            return true;
        } else {
            Log.e(TAG, "createCoAuthPairGroup request failed, invalid peerDevice");
            return false;
        }
    }

    public static boolean destroyCoAuthPairGroupCheck(CoAuthGroup coAuthGroup) {
        if (coAuthGroup != null && coAuthGroup.getGroupId() != null && !coAuthGroup.getGroupId().isEmpty()) {
            return true;
        }
        Log.e(TAG, "destroyCoAuthPairGroup request failed, empty coAuthGroup");
        return false;
    }

    public static boolean coAuthCheck(CoAuthContext coAuthContext) {
        if (coAuthContext == null) {
            Log.e(TAG, "coAuth request failed, empty coAuthContext");
            return false;
        } else if (!coAuthGroupCheck(coAuthContext)) {
            Log.e(TAG, "coAuth request failed, invalid coAuthGroup");
            return false;
        } else if (coAuthContext.getAuthType() == null) {
            Log.e(TAG, "coAuth request failed, empty coAuthContext:authType");
            return false;
        } else if (coAuthContext.isCoAuthBegin()) {
            Log.e(TAG, "coAuth request failed, invalid coAuthContext:coAuthCount");
            return false;
        } else if (coAuthContext.getSensorDeviceId() == null || coAuthContext.getSensorDeviceId().isEmpty()) {
            Log.e(TAG, "coAuth request failed, empty coAuthContext:sensorDeviceId");
            return false;
        } else if (coAuthContext.getVerifyDeviceId() != null && !coAuthContext.getVerifyDeviceId().isEmpty()) {
            return true;
        } else {
            Log.e(TAG, "coAuthCheck request failed, empty coAuthContext:verifyDeviceId");
            return false;
        }
    }

    public static boolean queryCoAuthMethodCheck(CoAuthContext coAuthContext) {
        if (coAuthContext != null && coAuthGroupCheck(coAuthContext)) {
            return true;
        }
        Log.e(TAG, "coAuthCheck request failed, empty getCoAuthGroup");
        return false;
    }

    public static boolean setPropertyCheck(CoAuthContext coAuthContext, byte[] key, byte[] value) {
        if (key != null && value != null) {
            return propertyCheck(coAuthContext);
        }
        Log.e(TAG, "setPropertyCheck request failed, empty key");
        return false;
    }

    public static boolean getPropertyCheck(CoAuthContext coAuthContext, byte[] key) {
        if (key != null) {
            return propertyCheck(coAuthContext);
        }
        Log.e(TAG, "getPropertyCheck request failed, empty key");
        return false;
    }

    private static boolean propertyCheck(CoAuthContext coAuthContext) {
        if (coAuthContext == null || !coAuthGroupCheck(coAuthContext)) {
            Log.e(TAG, "propertyCheck request failed, empty getCoAuthGroup");
            return false;
        } else if (coAuthContext.getAuthType() == CoAuthType.TYPE_IGNORE) {
            Log.e(TAG, "propertyCheck request failed, wrong auth type");
            return false;
        } else if (coAuthContext.getSensorDeviceId() != null && !coAuthContext.getSensorDeviceId().isEmpty()) {
            return true;
        } else {
            if (coAuthContext.getVerifyDeviceId() != null && !coAuthContext.getVerifyDeviceId().isEmpty()) {
                return true;
            }
            Log.e(TAG, "propertyCheck request failed, empty coAuthContext:sensorDeviceId or verifyDeviceId");
            return false;
        }
    }

    public static boolean initCoAuthIdmGroupCheck(String idmGid, List<CoAuthDevice> devList) {
        if (TextUtils.isEmpty(idmGid)) {
            Log.e(TAG, "initCoAuthIdmGroupCheck request failed, empty idmGid");
            return false;
        } else if (devList == null || devList.size() == 0) {
            Log.e(TAG, "initCoAuthIdmGroupCheck request failed, empty devList");
            return false;
        } else {
            for (CoAuthDevice authDevice : devList) {
                if (!coAuthDeviceCheck(authDevice)) {
                    Log.e(TAG, "initCoAuthIdmGroupCheck request failed, invalid devList");
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean cancelCoAuthCheck(CoAuthContext coAuthContext) {
        if (coAuthContext == null) {
            Log.e(TAG, "cancelCoAuth request failed, empty coAuthContext");
            return false;
        } else if (!coAuthGroupCheck(coAuthContext)) {
            Log.e(TAG, "cancelCoAuth request failed, invalid coAuthGroup");
            return false;
        } else if (coAuthContext.getAuthType() == null) {
            Log.e(TAG, "cancelCoAuth request failed, empty coAuthContext:authType");
            return false;
        } else if (coAuthContext.getSensorDeviceId() == null || coAuthContext.getSensorDeviceId().isEmpty()) {
            Log.e(TAG, "cancelCoAuth request failed, empty coAuthContext:sensorDeviceId");
            return false;
        } else if (coAuthContext.getVerifyDeviceId() != null && !coAuthContext.getVerifyDeviceId().isEmpty()) {
            return true;
        } else {
            Log.e(TAG, "cancelCoAuth request failed, empty coAuthContext:verifyDeviceId");
            return false;
        }
    }

    private static boolean coAuthGroupCheck(CoAuthContext coAuthContext) {
        if (coAuthContext.getCoAuthGroup() != null && !coAuthContext.getCoAuthGroup().getGroupId().isEmpty()) {
            return true;
        }
        Log.e(TAG, "request failed, invalid coAuthGroup");
        return false;
    }

    private static boolean coAuthDeviceCheck(CoAuthDevice peerDevice) {
        if (peerDevice == null) {
            Log.e(TAG, "request failed, empty peerDevice");
            return false;
        } else if (peerDevice.getDeviceId() == null || peerDevice.getDeviceId().isEmpty()) {
            Log.e(TAG, "request failed, empty peerDeviceId");
            return false;
        } else if (peerDevice.getIp() != null && !peerDevice.getIp().isEmpty()) {
            return true;
        } else {
            Log.e(TAG, "request failed, empty peerDeviceIp");
            return false;
        }
    }
}
