package com.huawei.voicerecognize;

import android.os.IHwBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceId;
import vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceIdClientCallback;

@HwSystemApi
public class VoiceHidlAdapter {
    private static final byte DEFAULT_INVALID_STATE = 1;
    private static final long DEFAULT_INVALID_TOKEN = 0;
    private static final int FAIL_RESULT = -1;
    private static final String FAIL_RESULT_STR = "fail";
    private static final int SUCCESS_RESULT = 0;
    private static final String TAG = "VoiceIdHidlAdapter";
    private static final String VOICEID_SERVICE_NAME = "hwvoiceid";
    private static final Object sHidlLock = new Object();
    private long mHalVoiceId = 0;
    private VoiceIdHidlCallback mVoiceIdHidlCallback;
    private VoiceIdHidlDeathRecipient mVoiceIdHidlDeathRecipient;
    private IBiometricsVoiceId mVoiceIdProxyV1 = getVoiceIdDaemonV1();

    public interface VoiceIdHidlServiceDiedCallbackWarpper {
        void onServiceDied();
    }

    public interface VoiceRecognizeCallbackWarpper {
        void onAuthenticationAcquired(int i);

        void onAuthenticationResult(int i, int i2);

        void onCancel(int i);

        void onEnrollResult(int i, int i2, int i3);

        void onRemovedResult(int i, int i2, int i3);
    }

    private IBiometricsVoiceId getVoiceIdDaemonV1() {
        synchronized (sHidlLock) {
            if (this.mVoiceIdProxyV1 != null) {
                return this.mVoiceIdProxyV1;
            }
            Log.v(TAG, "mDaemon was null, reconnect to voicerecognition");
            try {
                this.mVoiceIdProxyV1 = IBiometricsVoiceId.getService(VOICEID_SERVICE_NAME);
            } catch (NoSuchElementException e) {
                Log.e(TAG, "get voiceidDaemon NoSuchElementException ");
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to get biometric interface");
            }
            if (this.mVoiceIdProxyV1 == null) {
                Log.e(TAG, "voiceid HIDL not available");
                return null;
            }
            return this.mVoiceIdProxyV1;
        }
    }

    public boolean isServiceConnected() {
        boolean z;
        synchronized (sHidlLock) {
            z = this.mVoiceIdProxyV1 != null;
        }
        return z;
    }

    public void deinitDaemon() {
        synchronized (sHidlLock) {
            this.mVoiceIdProxyV1 = null;
        }
    }

    public boolean isLinkToDeath(VoiceIdHidlServiceDiedCallbackWarpper serviceDiedCallback) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return false;
        }
        this.mVoiceIdHidlDeathRecipient = new VoiceIdHidlDeathRecipient(serviceDiedCallback);
        boolean isLink = daemonV1.asBinder().linkToDeath(this.mVoiceIdHidlDeathRecipient, 0);
        if (!isLink) {
            Log.e(TAG, "voiceid 2.0 HAL link failed");
        }
        return isLink;
    }

    public long setNotify(VoiceRecognizeCallbackWarpper voiceRecognizeCallback) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return 0;
        }
        this.mVoiceIdHidlCallback = new VoiceIdHidlCallback(voiceRecognizeCallback);
        try {
            this.mHalVoiceId = daemonV1.setNotify(this.mVoiceIdHidlCallback);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to open voiceid 2.0 HAL");
            this.mHalVoiceId = 0;
        }
        return this.mHalVoiceId;
    }

    public ArrayList<Integer> getVoiceTypeList() {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return new ArrayList<>(0);
        }
        try {
            return daemonV1.getEnrolledVoiceTypes();
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to getEnrolledVoiceTypes");
            return new ArrayList<>(0);
        }
    }

    public ArrayList<Integer> updateActiveUser(int userId) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return new ArrayList<>(0);
        }
        try {
            return daemonV1.setActiveGroup(userId);
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to setActiveGroup");
            return new ArrayList<>(0);
        }
    }

    public int initAlgo(String packageName) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return -1;
        }
        try {
            return daemonV1.init();
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to init");
            return -1;
        }
    }

    public int releaseAlgo(String packageName) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return -1;
        }
        try {
            return daemonV1.release();
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to release");
            return -1;
        }
    }

    public int notifyLostState() {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return -1;
        }
        try {
            return daemonV1.notifyLostState();
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to notifyLostState");
            return -1;
        }
    }

    public byte setHandsetPubKey(byte[] secKeyVer, byte[] pubKey, byte[] signature) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return DEFAULT_INVALID_STATE;
        }
        try {
            return daemonV1.setPubKey(secKeyVer, pubKey, signature);
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to setPubKey");
            return DEFAULT_INVALID_STATE;
        }
    }

    public byte[] getHandsetAbility(byte[] headsetProVer, byte[] headsetSn, byte[] headsetAbility, byte[] headsetWordList) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return new byte[0];
        }
        try {
            return daemonV1.getAbility(headsetProVer, headsetSn, headsetAbility, headsetWordList);
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to getAbility");
            return new byte[0];
        }
    }

    public byte[] getUserStatus() {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return new byte[0];
        }
        try {
            return daemonV1.getUserStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to getUserStatus");
            return new byte[0];
        }
    }

    public long preEnroll() {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return 0;
        }
        try {
            return daemonV1.preEnroll();
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to preEnroll");
            return 0;
        }
    }

    public byte[] getEncKey(byte[] macData) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return new byte[0];
        }
        try {
            return daemonV1.genEncKey(macData);
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to getEncKey");
            return new byte[0];
        }
    }

    public byte[] getEnrollState(byte[] mac) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return new byte[0];
        }
        try {
            return daemonV1.getRegStatus(mac);
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to  getRegStatus");
            return new byte[0];
        }
    }

    public long remove(int userId, int voiceId) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return -1;
        }
        try {
            return (long) daemonV1.remove(userId, voiceId);
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to remove");
            return -1;
        }
    }

    public long removeAll(int userId) {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return -1;
        }
        try {
            return (long) daemonV1.removeAll(userId);
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to removeAll");
            return -1;
        }
    }

    public List<String> getVoiceEnrollStringList() {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return new ArrayList(0);
        }
        try {
            return daemonV1.getEnrollWordList();
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to getEnrollWordList");
            return new ArrayList(0);
        }
    }

    public int cancelEnroll() {
        IBiometricsVoiceId daemonV1 = getVoiceIdDaemonV1();
        if (daemonV1 == null) {
            return -1;
        }
        try {
            return daemonV1.cancelEnroll();
        } catch (RemoteException e) {
            Log.e(TAG, "voiceid 2.0 : Failed to cancelEnroll");
            return -1;
        }
    }

    public int setParameter(String key, String value) {
        if (key == null || value == null) {
            return -1;
        }
        synchronized (sHidlLock) {
            if (this.mVoiceIdProxyV1 == null) {
                return -1;
            }
            try {
                return this.mVoiceIdProxyV1.setParameter(key, value);
            } catch (RemoteException e) {
                Log.e(TAG, "voiceid 2.0 : Failed to setParameter");
                return -1;
            }
        }
    }

    public String getParameter(String key) {
        if (key == null) {
            return FAIL_RESULT_STR;
        }
        synchronized (sHidlLock) {
            if (this.mVoiceIdProxyV1 == null) {
                return FAIL_RESULT_STR;
            }
            try {
                String ret = this.mVoiceIdProxyV1.getParameter(key);
                if (ret == null) {
                    ret = FAIL_RESULT_STR;
                }
                return ret;
            } catch (RemoteException e) {
                Log.e(TAG, "voiceid 2.0 : Failed to getParameter");
                return FAIL_RESULT_STR;
            }
        }
    }

    private class VoiceIdHidlCallback extends IBiometricsVoiceIdClientCallback.Stub {
        private VoiceRecognizeCallbackWarpper mVoiceRecognizeCallbackWarpper;

        private VoiceIdHidlCallback(VoiceRecognizeCallbackWarpper callback) {
            this.mVoiceRecognizeCallbackWarpper = callback;
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceIdClientCallback
        public void onEnrollResult(int userId, int errorCode, int subInfo) {
            this.mVoiceRecognizeCallbackWarpper.onEnrollResult(userId, errorCode, subInfo);
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceIdClientCallback
        public void onAuthenticationResult(int userId, int errorCode) {
            this.mVoiceRecognizeCallbackWarpper.onAuthenticationResult(userId, errorCode);
        }

        public void onAuthenticationAcquired(int acquiredInfo) {
            this.mVoiceRecognizeCallbackWarpper.onAuthenticationAcquired(acquiredInfo);
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceIdClientCallback
        public void onRemovedResult(int userId, int errorCode, int voiceId) {
            this.mVoiceRecognizeCallbackWarpper.onRemovedResult(userId, errorCode, voiceId);
        }

        @Override // vendor.huawei.hardware.biometrics.hwvoiceid.V2_0.IBiometricsVoiceIdClientCallback
        public void onCancel(int type) {
            this.mVoiceRecognizeCallbackWarpper.onCancel(type);
        }
    }

    private class VoiceIdHidlDeathRecipient implements IHwBinder.DeathRecipient {
        private VoiceIdHidlServiceDiedCallbackWarpper mVoiceIdServiceDiedCallback;

        private VoiceIdHidlDeathRecipient(VoiceIdHidlServiceDiedCallbackWarpper callback) {
            this.mVoiceIdServiceDiedCallback = callback;
        }

        public void serviceDied(long cookie) {
            Log.e(VoiceHidlAdapter.TAG, "voice id HAL died");
            synchronized (VoiceHidlAdapter.sHidlLock) {
                VoiceHidlAdapter.this.mVoiceIdProxyV1 = null;
                VoiceHidlAdapter.this.mHalVoiceId = 0;
                this.mVoiceIdServiceDiedCallback.onServiceDied();
            }
        }
    }
}
