package com.huawei.android.biometric;

import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import vendor.huawei.hardware.biometrics.fingerprint.V2_2.IExtBiometricsFingerprint;

public class BiometricsFingerprintEx {
    private static final int INVALID_VALUE = -1;
    private static final String TAG_BIOMETRIC = "BiometricsFingerprintEx";
    private IBiometricsFingerprint mBiometricsFingerprint;
    private IExtBiometricsFingerprint mExtBiometricsFingerprint;

    public IBiometricsFingerprint getBiometricsFingerprint() {
        return this.mBiometricsFingerprint;
    }

    public void setBiometricsFingerprint(IBiometricsFingerprint biometricsFingerprint) {
        this.mBiometricsFingerprint = biometricsFingerprint;
    }

    public IExtBiometricsFingerprint getExtBiometricsFingerprint() {
        return this.mExtBiometricsFingerprint;
    }

    public void setExtBiometricsFingerprint(IExtBiometricsFingerprint extBiometricsFingerprint) {
        this.mExtBiometricsFingerprint = extBiometricsFingerprint;
    }

    public void setKidsFingerprintEx(int kidFpId) {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                this.mExtBiometricsFingerprint.setKidsFingerprint(kidFpId);
            }
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "setKidsFingerprintEx");
        }
    }

    public void verifyUserEx(BiometricServiceReceiverListenerEx fidoAuthenticationCallback, int groupId, String aaid, ArrayList<Byte> arrayNonces) {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                this.mExtBiometricsFingerprint.verifyUser(fidoAuthenticationCallback.getFidoAuthenticationCallback(), groupId, aaid, arrayNonces);
            }
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "verifyUserEx");
        }
    }

    public void removeEx(BiometricAuthenticatorEx biometricAuthenticatorEx, int currentUser) {
        try {
            if (this.mBiometricsFingerprint != null) {
                this.mBiometricsFingerprint.remove(biometricAuthenticatorEx.getIdentifier().getBiometricId(), currentUser);
            }
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "removeEx");
        }
    }

    public void setActiveGroupEx(int userId, String filePath) {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                this.mExtBiometricsFingerprint.setActiveGroup(userId, filePath);
            }
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "setActiveGroupEx");
        }
    }

    public void removeUserData(int groupId, String storePath) {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                this.mExtBiometricsFingerprint.removeUserData(groupId, storePath);
            }
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "removeUserData");
        }
    }

    public int checkNeedReEnrollFinger() {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                return this.mExtBiometricsFingerprint.checkNeedReEnrollFinger();
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "checkNeedReEnrollFinger");
            return -1;
        }
    }

    public int checkNeedCalibrateFingerPrint() {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                return this.mExtBiometricsFingerprint.checkNeedCalibrateFingerPrint();
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "checkNeedCalibrateFingerPrint");
            return -1;
        }
    }

    public void setCalibrateMode(int mode) {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                this.mExtBiometricsFingerprint.setCalibrateMode(mode);
            }
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "setCalibrateMode");
        }
    }

    public int getTokenLen() {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                return this.mExtBiometricsFingerprint.getTokenLen();
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "getTokenLen");
            return -1;
        }
    }

    public int sendDataToHal(int paraData, ArrayList<Byte> list) {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                return this.mExtBiometricsFingerprint.sendDataToHal(paraData, list);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "sendDataToHal");
            return -1;
        }
    }

    public int sendCmdToHal(int cmd) {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                return this.mExtBiometricsFingerprint.sendCmdToHal(cmd);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "sendCmdToHal");
            return -1;
        }
    }

    public void setLivenessSwitch(int needLivenessAuthentication) {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                this.mExtBiometricsFingerprint.setLivenessSwitch(needLivenessAuthentication);
            }
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "setLivenessSwitch");
        }
    }

    public List<Integer> getFpOldData() {
        try {
            if (this.mExtBiometricsFingerprint != null) {
                return this.mExtBiometricsFingerprint.getFpOldData();
            }
        } catch (RemoteException e) {
            Log.e(TAG_BIOMETRIC, "getFpOldData");
        }
        return Collections.emptyList();
    }
}
