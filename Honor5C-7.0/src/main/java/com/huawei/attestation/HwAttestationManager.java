package com.huawei.attestation;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.attestation.IHwAttestationService.Stub;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class HwAttestationManager {
    public static final int AUTH_OK = 0;
    public static final int DEVICE_ID_TYPE_EMMC = 1;
    public static final int KEY_INDEX_GENERAL = 2;
    public static final int KEY_INDEX_HWCLOUD = 1;
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
    private IHwAttestationService mService;

    public int getLastError() {
        this.mService = getHwAttestationService();
        if (this.mService == null) {
            Log.e(TAG, "getState DeviceAttestation service is null");
            return STATE_ERR_NO_ATTESTATION_SERVICE;
        }
        try {
            int ret = this.mService.getLastError();
            Log.d(TAG, "mService.getState() ret = " + ret);
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
            return STATE_ERR_UNKNOW;
        }
    }

    public byte[] getAttestationSignature(int keyIndex, int deviceIdType, String signatureType, byte[] challenge) {
        this.mService = getHwAttestationService();
        if (this.mService == null) {
            Log.e(TAG, "getSignature DeviceAttestation service is null");
            return new byte[STATE_OK];
        }
        try {
            return this.mService.getAttestationSignature(keyIndex, deviceIdType, signatureType, challenge);
        } catch (RemoteException e) {
            e.printStackTrace();
            return new byte[STATE_OK];
        }
    }

    public byte[] getDeviceID(int deviceIdType) {
        this.mService = getHwAttestationService();
        if (this.mService == null) {
            Log.e(TAG, "getDeviceID DeviceAttestation service is null");
            return null;
        }
        try {
            return this.mService.getDeviceID(deviceIdType);
        } catch (RemoteException e) {
            e.printStackTrace();
            return new byte[STATE_OK];
        }
    }

    public static String getPublickKey(int keyIndex) {
        IHwAttestationService attService = getHwAttestationService();
        if (attService == null) {
            Log.e(TAG, "getDeviceID DeviceAttestation service is null");
            return null;
        }
        byte[] pubKey = new byte[AppOpsManagerEx.TYPE_CAMERA];
        try {
            int actLen = attService.getPublickKey(keyIndex, pubKey);
            if (actLen > 0) {
                return new String(pubKey, STATE_OK, actLen, StandardCharsets.UTF_8);
            }
            Log.e(TAG, "attService.getPublickKey error: " + actLen);
            return null;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDeviceCert(int keyIndex, int certType) {
        String ret = null;
        if (keyIndex >= 3) {
            return null;
        }
        if (certType != KEY_INDEX_HWCLOUD && certType != KEY_INDEX_GENERAL) {
            return null;
        }
        if (isSupportCert(keyIndex, certType)) {
            this.mService = getHwAttestationService();
            if (this.mService == null) {
                Log.e(TAG, "getDeviceCert service is null");
                return null;
            }
            boolean retry = true;
            int certLength = HwAttestationStatus.CERT_MAX_LENGTH;
            do {
                byte[] certBuf = new byte[certLength];
                try {
                    certLength = this.mService.getDeviceCert(keyIndex, certType, certBuf);
                    Log.i(TAG, "getDeviceCert cert length: " + certLength);
                } catch (RemoteException e) {
                    Log.e(TAG, "getDeviceCert get attestation service error");
                }
                if (certLength <= 0 || certLength > HwAttestationStatus.CERT_INVAILD_LENGTH) {
                    Log.e(TAG, "getDeviceCert error, length: " + certLength);
                    return null;
                } else if (certLength <= HwAttestationStatus.CERT_MAX_LENGTH || !retry) {
                    try {
                        String ret2 = new String(certBuf, STATE_OK, certLength, "UTF-8");
                        try {
                            Log.i(TAG, "getDeviceCert cert content: " + ret2);
                            ret = ret2;
                            break;
                        } catch (UnsupportedEncodingException e2) {
                            Log.e(TAG, "UnsupportedEncodingException: convert certBuf from byte array to String with UTF-8 format");
                            ret = null;
                            return ret;
                        }
                    } catch (UnsupportedEncodingException e3) {
                        Log.e(TAG, "UnsupportedEncodingException: convert certBuf from byte array to String with UTF-8 format");
                        ret = null;
                        return ret;
                    }
                } else {
                    retry = false;
                }
            } while (STATE_OK != null);
        }
        return ret;
    }

    public boolean isSupportCert(int keyIndex, int certType) {
        boolean ret = false;
        if (certType == KEY_INDEX_HWCLOUD || certType == KEY_INDEX_GENERAL) {
            this.mService = getHwAttestationService();
            if (this.mService == null) {
                Log.e(TAG, "isSupportCert service is null");
                return false;
            }
            try {
                int outCertType = this.mService.getDeviceCertType(keyIndex);
                Log.i(TAG, "isSupportCert: " + outCertType);
                if (outCertType > 0 && (certType & outCertType) != 0) {
                    ret = true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "isSupportCert get attestation service error");
            }
            return ret;
        }
        Log.e(TAG, "isSupportCert invalid certType:" + certType);
        return false;
    }

    private static IHwAttestationService getHwAttestationService() {
        return Stub.asInterface(ServiceManager.getService("attestation_service"));
    }
}
