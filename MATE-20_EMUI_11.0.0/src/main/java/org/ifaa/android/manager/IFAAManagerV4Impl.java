package org.ifaa.android.manager;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.facerecognition.FaceRecognizeManager;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import java.util.List;
import org.ifaa.android.manager.face.IFAAFaceRecognizeManager;

public class IFAAManagerV4Impl extends IFAAManagerV4 {
    private static final int ENABLE_TO_PAY = 1000;
    private static final int ID_NOT_ENROLLED = 1002;
    private static final int IFAA_VERSION_V4 = 4;
    private static final String LOG_TAG = "IFAAManagerV4Impl";
    private static final int MIN_SUPPORT_3D_FACE_MODE = 3;
    private static final String SETTINGS_FACE_CLASS = "com.android.settings.facechecker.unlock.FaceUnLockSettingsActivity";
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final int SUPPORT_FACE_PAY = 1;
    private static final int SYSTEM_LOCKED = 1001;
    private final FaceRecognizeManager mFaceManager = IFAAFaceRecognizeManager.getFRManager();
    private final FingerprintManagerEx mFingerManagerEx;
    private final IFAAManagerV3Impl mV3Impl;

    public IFAAManagerV4Impl(Context context) {
        this.mV3Impl = new IFAAManagerV3Impl(context);
        IFAAFaceRecognizeManager.createInstance(context);
        this.mFingerManagerEx = new FingerprintManagerEx(context);
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public int getSupportBIOTypes(Context context) {
        int type = this.mV3Impl.getSupportBIOTypes(context);
        FaceRecognizeManager faceRecognizeManager = this.mFaceManager;
        if (faceRecognizeManager == null) {
            Log.e(LOG_TAG, "Get face recognition manager is null. v4 type" + type);
            return type;
        }
        FaceRecognizeManager.FaceRecognitionAbility mFaceAbility = null;
        try {
            mFaceAbility = faceRecognizeManager.getFaceRecognitionAbility();
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "No USE_FACERECOGNITION permission");
        }
        if (mFaceAbility == null) {
            Log.e(LOG_TAG, "Get face recognition ability is null. v4 type" + type);
            return type;
        }
        int faceMode = mFaceAbility.faceMode;
        int secureLevel = mFaceAbility.secureLevel;
        boolean mIsChinaArea = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        Log.i(LOG_TAG, "faceMode is " + faceMode + " secureLevel is " + secureLevel);
        if (((faceMode == 3 && mIsChinaArea) || faceMode > 3) && secureLevel == 1) {
            Log.e(LOG_TAG, "adding type");
            type |= 4;
        }
        Log.i(LOG_TAG, "v4 type " + type);
        return type;
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public int startBIOManager(Context context, int authType) {
        if (authType != 4) {
            return this.mV3Impl.startBIOManager(context, authType);
        }
        Intent intent = new Intent();
        intent.setClassName(SETTINGS_PACKAGE, SETTINGS_FACE_CLASS);
        intent.setFlags(268435456);
        context.startActivity(intent);
        return 0;
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public String getDeviceModel() {
        return this.mV3Impl.getDeviceModel();
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public int getVersion() {
        return 4;
    }

    @Override // org.ifaa.android.manager.IFAAManagerV2
    public byte[] processCmdV2(Context context, byte[] param) {
        return this.mV3Impl.processCmdV2(context, param);
    }

    @Override // org.ifaa.android.manager.IFAAManagerV3
    public String getExtInfo(int authType, String keyExtInfo) {
        return this.mV3Impl.getExtInfo(authType, keyExtInfo);
    }

    @Override // org.ifaa.android.manager.IFAAManagerV3
    public void setExtInfo(int authType, String keyExtInfo, String valExtInfo) {
        this.mV3Impl.setExtInfo(authType, keyExtInfo, valExtInfo);
    }

    private static boolean isBioTypeOK(int type) {
        return type == 1 || type == 4;
    }

    @Override // org.ifaa.android.manager.IFAAManagerV4
    public int getEnabled(int type) {
        Log.i(LOG_TAG, "getEnabled enter, type is: " + type);
        if (!isBioTypeOK(type)) {
            Log.e(LOG_TAG, "unsupport type");
            return -1;
        } else if (!isBiometricsExsit(type)) {
            Log.i(LOG_TAG, "getEnabled no id exist");
            return ID_NOT_ENROLLED;
        } else if (getRemainingNum(type) < 1) {
            Log.i(LOG_TAG, "getEnabled sys locked");
            return SYSTEM_LOCKED;
        } else {
            Log.i(LOG_TAG, "getEnabled ok");
            return ENABLE_TO_PAY;
        }
    }

    @Override // org.ifaa.android.manager.IFAAManagerV4
    public int[] getIDList(int type) {
        Log.i(LOG_TAG, "getIDList enter, type is: " + type);
        if (!isBioTypeOK(type)) {
            Log.e(LOG_TAG, "unsupport type");
            return new int[0];
        }
        int[] result = getBiometricsID(type);
        StringBuilder sb = new StringBuilder();
        sb.append("id list 0 is ");
        sb.append((result == null || result.length <= 0) ? "not exist" : Integer.valueOf(result[0]));
        Log.i(LOG_TAG, sb.toString());
        return result;
    }

    private int[] getBiometricsID(int type) {
        Log.i(LOG_TAG, "getBiometricsID enter, type is: " + type);
        if (type != 1) {
            return this.mFaceManager.getEnrolledFaceIDs();
        }
        List<Integer> fingerprintIds = this.mFingerManagerEx.getFingerIds();
        if (fingerprintIds == null) {
            return new int[0];
        }
        int fingerprintCount = fingerprintIds.size();
        int[] result = new int[fingerprintCount];
        for (int i = 0; i < fingerprintCount; i++) {
            result[i] = fingerprintIds.get(i).intValue();
        }
        return result;
    }

    private boolean isBiometricsExsit(int type) {
        int[] ret = getBiometricsID(type);
        StringBuilder sb = new StringBuilder();
        sb.append("get Biometrics ID length is");
        sb.append(ret != null ? ret.length : -1);
        Log.i(LOG_TAG, sb.toString());
        return ret != null && ret.length >= 1;
    }

    private int getRemainingNum(int type) {
        return type == 1 ? this.mFingerManagerEx.getRemainingNum() : this.mFaceManager.getRemainingNum();
    }
}
