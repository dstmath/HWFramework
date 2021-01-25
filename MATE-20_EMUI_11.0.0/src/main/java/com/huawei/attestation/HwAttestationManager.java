package com.huawei.attestation;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.attestation.IHwAttestationService;
import com.huawei.hsm.permission.StubController;
import huawei.android.security.IHwSecurityService;
import java.nio.charset.StandardCharsets;

public class HwAttestationManager {
    public static final int AUTH_OK = 0;
    public static final int DEVICE_ID_TYPE_EMMC = 1;
    private static final int HW_ATTESTATION_PLUGIN_ID = 23;
    public static final int KEY_INDEX_GENERAL = 2;
    public static final int KEY_INDEX_HWCLOUD = 1;
    private static final String SECURITY_SERVICE = "securityserver";
    public static final int STATE_ERR_DEVICE_KEY = -2;
    public static final int STATE_ERR_GET_CERT = -8;
    public static final int STATE_ERR_GET_CERT_TYPE = -7;
    public static final int STATE_ERR_GET_PUBKEY = -6;
    public static final int STATE_ERR_INPUT_PARAMETER = -4;
    public static final int STATE_ERR_NO_ATTESTATION_SERVICE = -1;
    public static final int STATE_ERR_PERMISSION_DENIED = -5;
    public static final int STATE_ERR_READ_EMMCID = -3;
    public static final int STATE_ERR_UNKNOW = -10;
    public static final int STATE_OK = 0;
    private static final String TAG = "HwAttestationManager";
    private static IHwAttestationService sHwAttestationManager;
    private static final Object sInstanceSync = new Object();

    public int getLastError() {
        if (getHwAttestationService() == null) {
            return -1;
        }
        try {
            int ret = sHwAttestationManager.getLastError();
            Log.d(TAG, "mService.getState() ret = " + ret);
            return ret;
        } catch (RemoteException e) {
            Log.e(TAG, "MService.getLastError() error.");
            return -1;
        }
    }

    public byte[] getAttestationSignatureWithPkgName(int keyIndex, int deviceIdType, String signatureType, byte[] challenge, String packageName) {
        if (signatureType.length() > 128 || challenge.length > 64) {
            Log.e(TAG, "oversize : signatureType length" + signatureType.length() + "challenge length " + challenge.length);
            return new byte[0];
        }
        if (getHwAttestationService() != null) {
            try {
                return sHwAttestationManager.getAttestationSignatureWithPkgName(keyIndex, deviceIdType, signatureType, challenge, packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "getAttestationSignatureWithPkgName failed", e);
            }
        }
        Log.e(TAG, "getHwAttestationService failed");
        return new byte[0];
    }

    public byte[] getAttestationSignature(int keyIndex, int deviceIdType, String signatureType, byte[] challenge) {
        if (signatureType.length() > 128 || challenge.length > 64) {
            Log.e(TAG, "oversize : signatureType length" + signatureType.length() + "challenge length " + challenge.length);
            return new byte[0];
        }
        if (getHwAttestationService() != null) {
            try {
                return sHwAttestationManager.getAttestationSignature(keyIndex, deviceIdType, signatureType, challenge);
            } catch (RemoteException e) {
                Log.e(TAG, "MService.getAttestationSignature error.");
            }
        }
        return new byte[0];
    }

    public byte[] getDeviceID(int deviceIdType) {
        if (getHwAttestationService() != null) {
            try {
                return sHwAttestationManager.getDeviceID(deviceIdType);
            } catch (RemoteException e) {
                Log.e(TAG, "MService.getDeviceID error.");
            }
        }
        return new byte[0];
    }

    public static String getPublickKey(int keyIndex) {
        sHwAttestationManager = getHwAttestationService();
        IHwAttestationService iHwAttestationService = sHwAttestationManager;
        if (iHwAttestationService == null) {
            Log.e(TAG, "getDeviceID DeviceAttestation service is null");
            return null;
        }
        byte[] pubKey = new byte[StubController.PERMISSION_CAMERA];
        try {
            int actLen = iHwAttestationService.getPublickKey(keyIndex, pubKey);
            if (actLen > 0) {
                return new String(pubKey, 0, actLen, StandardCharsets.UTF_8);
            }
            Log.e(TAG, "attService.getPublickKey error: " + actLen);
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "AttService.getPublickKey error.");
            return null;
        }
    }

    private static IHwAttestationService getHwAttestationService() {
        synchronized (sInstanceSync) {
            if (sHwAttestationManager != null) {
                return sHwAttestationManager;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sHwAttestationManager = IHwAttestationService.Stub.asInterface(secService.querySecurityInterface(23));
                } catch (RemoteException e) {
                    Log.e(TAG, "Get HwAttestationService() failed!");
                }
            }
            return sHwAttestationManager;
        }
    }
}
