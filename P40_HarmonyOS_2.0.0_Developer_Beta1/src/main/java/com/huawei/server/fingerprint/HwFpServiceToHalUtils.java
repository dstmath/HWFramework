package com.huawei.server.fingerprint;

import android.content.Context;
import android.util.Log;
import com.huawei.android.biometric.BiometricsFingerprintEx;
import com.huawei.android.biometric.FingerprintSupportEx;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class HwFpServiceToHalUtils {
    private static final int INVALID_VALUE = -1;
    public static final int MSG_UPDATE_SCREEN_FILM_STATE = 405;
    public static final int NON_TOUGHENED_FILM = 1;
    public static final String SET_TOUGHENED_FILM_STATE = "film_state";
    private static final String TAG = "HwFpServiceToHalUtils";
    public static final int TOUGHENED_FILM = 2;
    private static BiometricsFingerprintEx mDaemonEx = null;

    public HwFpServiceToHalUtils(Context context) {
    }

    private static ArrayList<Byte> convertStringToList(String param) {
        if (param == null) {
            Log.e(TAG, "param is null");
            return new ArrayList<>(0);
        }
        byte[] paramNameBytes = param.getBytes(StandardCharsets.UTF_8);
        if (paramNameBytes == null || paramNameBytes.length == 0) {
            return new ArrayList<>(0);
        }
        ArrayList<Byte> paramArrayList = new ArrayList<>(paramNameBytes.length);
        for (byte i : paramNameBytes) {
            paramArrayList.add(Byte.valueOf(i));
        }
        return paramArrayList;
    }

    public static int sendDataToHal(int cmdId, String param) {
        int result = -1;
        Log.i(TAG, "sendDataToHal cmdId: " + cmdId + ", param: " + param);
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Log.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        ArrayList<Byte> paramArrayList = convertStringToList(param);
        if (paramArrayList != null && !paramArrayList.isEmpty()) {
            result = daemon.sendDataToHal(cmdId, paramArrayList);
        }
        Log.i(TAG, "sendDataToHal result: " + result + ", paramSize: " + paramArrayList.size());
        return result;
    }

    public static BiometricsFingerprintEx getFingerprintDaemonEx() {
        BiometricsFingerprintEx biometricsFingerprintEx = mDaemonEx;
        if (biometricsFingerprintEx != null) {
            return biometricsFingerprintEx;
        }
        try {
            mDaemonEx = FingerprintSupportEx.getFingerprintDaemon();
        } catch (NoSuchElementException e) {
            Log.e(TAG, "Service doesn't exist or cannot be opened");
        }
        Log.w(TAG, "getFingerprintDaemonEx inst = " + mDaemonEx);
        return mDaemonEx;
    }

    public static void destroyDaemonEx() {
        mDaemonEx = null;
    }

    public static void sendCommandToHal(int command) {
        sendCommandToHal(command, -1);
    }

    public static int sendCommandToHal(int command, int defaultValue) {
        BiometricsFingerprintEx daemon = getFingerprintDaemonEx();
        if (daemon != null) {
            int result = daemon.sendCmdToHal(command);
            Log.i(TAG, "sendCommandToHal: result" + result + ", command: " + command);
            return result;
        }
        Log.e(TAG, "Fingerprintd is not available!");
        return defaultValue;
    }
}
