package com.huawei.server.fingerprint;

import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.biometrics.fingerprint.V2_2.IExtBiometricsFingerprint;

public class HwFpServiceToHalUtils {
    private static final int INVALID_VALUE = -1;
    public static final int MSG_UPDATE_SCREEN_FILM_STATE = 405;
    public static final int NON_TOUGHENED_FILM = 1;
    public static final String SET_TOUGHENED_FILM_STATE = "film_state";
    private static final String TAG = "HwFpServiceToHalUtils";
    public static final int TOUGHENED_FILM = 2;
    private static IExtBiometricsFingerprint mDaemonEx = null;

    public HwFpServiceToHalUtils(Context context) {
    }

    private static ArrayList<Byte> convertStringToList(String param) {
        if (param == null) {
            Slog.e(TAG, "param is null");
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
        Slog.v(TAG, "sendDataToHal cmdId: " + cmdId + ", param: " + param);
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        try {
            ArrayList<Byte> paramArrayList = convertStringToList(param);
            if (paramArrayList != null && !paramArrayList.isEmpty()) {
                result = daemon.sendDataToHal(cmdId, paramArrayList);
            }
            Slog.i(TAG, "sendDataToHal result: " + result + ", paramSize: " + paramArrayList.size());
        } catch (RemoteException e) {
            Slog.e(TAG, "sendDataToHal RemoteException");
        }
        return result;
    }

    public static IExtBiometricsFingerprint getFingerprintDaemonEx() {
        IExtBiometricsFingerprint iExtBiometricsFingerprint = mDaemonEx;
        if (iExtBiometricsFingerprint != null) {
            return iExtBiometricsFingerprint;
        }
        try {
            mDaemonEx = IExtBiometricsFingerprint.getService();
        } catch (NoSuchElementException e) {
            Slog.e(TAG, "Service doesn't exist or cannot be opened");
        } catch (RemoteException e2) {
            Slog.e(TAG, "Failed to get biometric interface");
        }
        Slog.w(TAG, "getFingerprintDaemonEx inst = " + mDaemonEx);
        return mDaemonEx;
    }

    public static void destroyDaemonEx() {
        mDaemonEx = null;
    }

    public static void sendCommandToHal(int command) {
        sendCommandToHal(command, -1);
    }

    public static int sendCommandToHal(int command, int defaultValue) {
        int result = defaultValue;
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon != null) {
            try {
                result = daemon.sendCmdToHal(command);
                Slog.i(TAG, "sendCommandToHal: result" + result + ", command: " + command);
                return result;
            } catch (RemoteException e) {
                Slog.e(TAG, "dualfingerprint sendCmdToHal RemoteException");
                return result;
            }
        } else {
            Slog.e(TAG, "Fingerprintd is not available!");
            return result;
        }
    }
}
