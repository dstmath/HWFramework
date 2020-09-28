package com.huawei.coauth.auth;

import android.content.Context;
import android.util.Log;
import com.huawei.coauth.auth.CoAuth;

class CoAuthCheckUtil {
    private static final String TAG = CoAuth.class.getName();

    private CoAuthCheckUtil() {
    }

    public static boolean connectServiceCheck(Context context, CoAuth.IConnectServiceCallback callback) {
        if (context == null || context.getApplicationContext() == null || context.getApplicationContext().getPackageName() == null) {
            Log.e(TAG, "connectService request failed, empty context");
            return false;
        } else if (callback != null) {
            return true;
        } else {
            Log.e(TAG, "connectService request failed, empty callback");
            return false;
        }
    }

    public static boolean disconnectServiceCheck(Context context) {
        if (context != null) {
            return true;
        }
        Log.e(TAG, "disconnectService request failed, empty context");
        return false;
    }

    public static boolean createCoAuthPairGroupCheck(String moduleName, CoAuthDevice peerDevice, CoAuth.ICreateCallback callback) {
        if (moduleName == null || moduleName.isEmpty()) {
            Log.e(TAG, "createCoAuthPairGroup request failed, empty moduleName");
            return false;
        } else if (!coAuthDeviceCheck(peerDevice)) {
            Log.e(TAG, "createCoAuthPairGroup request failed, invalid peerDevice");
            return false;
        } else if (callback != null) {
            return true;
        } else {
            Log.e(TAG, "createCoAuthPairGroup request failed, empty callback");
            return false;
        }
    }

    public static boolean destroyCoAuthPairGroupCheck(CoAuthGroup coAuthGroup, CoAuth.IDestroyCallback callback) {
        if (coAuthGroup == null || coAuthGroup.getGroupId() == null || coAuthGroup.getGroupId().isEmpty()) {
            Log.e(TAG, "destroyCoAuthPairGroup request failed, empty coAuthGroup");
            return false;
        } else if (callback != null) {
            return true;
        } else {
            Log.e(TAG, "destroyCoAuthPairGroup request failed, empty callback");
            return false;
        }
    }

    public static boolean coAuthCheck(CoAuthContext coAuthContext, CoAuth.ICoAuthCallback callback) {
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
        } else if (coAuthContext.getVerifyDeviceId() == null || coAuthContext.getVerifyDeviceId().isEmpty()) {
            Log.e(TAG, "coAuthCheck request failed, empty coAuthContext:verifyDeviceId");
            return false;
        } else if (callback != null) {
            return true;
        } else {
            Log.e(TAG, "coAuthCheck request failed, empty callback");
            return false;
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
