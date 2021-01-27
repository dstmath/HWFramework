package com.huawei.facerecognition;

import android.content.Context;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.view.Surface;
import com.huawei.coauthservice.pool.SecureRegCallBack;
import huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.FaceRecognizeManagerFactory;

public class FaceRecognizeManager {
    public static final int ALL_FACE_ID = 0;
    public static final int CODE_CALLBACK_ACQUIRE = 3;
    public static final int CODE_CALLBACK_BUSY = 4;
    public static final int CODE_CALLBACK_CANCEL = 2;
    public static final int CODE_CALLBACK_FACEID = 6;
    public static final int CODE_CALLBACK_OUT_OF_MEM = 5;
    public static final int CODE_CALLBACK_RESULT = 1;
    private static final String HW_MANAGE_FACERECOGNITION = "com.huawei.permission.MANAGE_FACERECOGNITION";
    private static final String HW_RESET_FACERECOGNITION_LOCKOUT = "com.huawei.permission.RESET_FACERECOGNITION_LOCKOUT";
    private static final String HW_USE_FACERECOGNITION = "com.huawei.permission.USE_FACERECOGNITION";
    public static final int INVALID_FACE_ID = -1;
    public static final int REQUEST_OK = 0;
    public static final int TYPE_CALLBACK_AUTH = 2;
    public static final int TYPE_CALLBACK_CHECK_NEED_UPGRADE = 5;
    public static final int TYPE_CALLBACK_ENROLL = 1;
    public static final int TYPE_CALLBACK_REMOVE = 3;
    public static final int TYPE_CALLBACK_UPGRADE = 4;
    private static final String USE_FACERECOGNITION = "android.permission.USE_FACERECOGNITION";
    private FaceRecognizeCallback mFaceCallback;
    private DefaultFaceRecognizeManagerImpl mFaceManagerImpl;

    public interface AcquireInfo {
        public static final int FACEID_ENROLL_INFO_BEGIN = 2000;
        public static final int FACEID_FACE_ANGLE_BASE = 1000;
        public static final int FACEID_FACE_DETECTED = 38;
        public static final int FACEID_FACE_MOVED = 33;
        public static final int FACEID_HAS_REGISTERED = 37;
        public static final int FACEID_NOT_GAZE = 36;
        public static final int FACEID_NOT_THE_SAME_FACE = 35;
        public static final int FACEID_OUT_OF_BOUNDS = 34;
        public static final int FACE_UNLOCK_COVERED_WITH_MASK = 45;
        public static final int FACE_UNLOCK_FACE_BAD_QUALITY = 4;
        public static final int FACE_UNLOCK_FACE_BLUR = 28;
        public static final int FACE_UNLOCK_FACE_DARKLIGHT = 30;
        public static final int FACE_UNLOCK_FACE_DARKPIC = 39;
        public static final int FACE_UNLOCK_FACE_DOWN = 18;
        public static final int FACE_UNLOCK_FACE_EYE_CLOSE = 22;
        public static final int FACE_UNLOCK_FACE_EYE_OCCLUSION = 21;
        public static final int FACE_UNLOCK_FACE_HALF_SHADOW = 32;
        public static final int FACE_UNLOCK_FACE_HIGHTLIGHT = 31;
        public static final int FACE_UNLOCK_FACE_KEEP = 19;
        public static final int FACE_UNLOCK_FACE_MOUTH_OCCLUSION = 23;
        public static final int FACE_UNLOCK_FACE_MULTI = 27;
        public static final int FACE_UNLOCK_FACE_NOT_COMPLETE = 29;
        public static final int FACE_UNLOCK_FACE_NOT_FOUND = 5;
        public static final int FACE_UNLOCK_FACE_OFFSET_BOTTOM = 11;
        public static final int FACE_UNLOCK_FACE_OFFSET_LEFT = 8;
        public static final int FACE_UNLOCK_FACE_OFFSET_RIGHT = 10;
        public static final int FACE_UNLOCK_FACE_OFFSET_TOP = 9;
        public static final int FACE_UNLOCK_FACE_RISE = 16;
        public static final int FACE_UNLOCK_FACE_ROTATED_LEFT = 15;
        public static final int FACE_UNLOCK_FACE_ROTATED_RIGHT = 17;
        public static final int FACE_UNLOCK_FACE_ROTATE_BOTTOM_LEFT = 43;
        public static final int FACE_UNLOCK_FACE_ROTATE_BOTTOM_RIGHT = 42;
        public static final int FACE_UNLOCK_FACE_ROTATE_TOP_LEFT = 41;
        public static final int FACE_UNLOCK_FACE_ROTATE_TOP_RIGHT = 40;
        public static final int FACE_UNLOCK_FACE_SCALE_TOO_LARGE = 7;
        public static final int FACE_UNLOCK_FACE_SCALE_TOO_SMALL = 6;
        @Deprecated
        public static final int FACE_UNLOCK_FAILURE = 3;
        public static final int FACE_UNLOCK_IMAGE_BLUR = 20;
        @Deprecated
        public static final int FACE_UNLOCK_INVALID_ARGUMENT = 1;
        @Deprecated
        public static final int FACE_UNLOCK_INVALID_HANDLE = 2;
        public static final int FACE_UNLOCK_LIVENESS_FAILURE = 14;
        public static final int FACE_UNLOCK_LIVENESS_WARNING = 13;
        @Deprecated
        public static final int FACE_UNLOCK_OK = 0;
        public static final int FACE_UNLOCK_OPEN_CAMERA_FAILED = 46;
        public static final int FACE_UNLOCK_WITHOUT_MASK = 44;
        public static final int MG_UNLOCK_COMPARE_FAILURE = 12;
    }

    public interface FaceErrorCode {
        public static final int ALGORITHM_NOT_INIT = 5;
        public static final int BUSY = 13;
        public static final int CAMERA_FAIL = 12;
        public static final int CANCELED = 2;
        public static final int COMPARE_FAIL = 3;
        public static final int ERRCODE_NOT_GAZE = 11;
        public static final int FAILED = 1;
        public static final int HAL_INVALIDE = 6;
        public static final int INVALID_PARAMETERS = 9;
        public static final int IN_LOCKOUT_MODE = 8;
        public static final int NO_FACE_DATA = 10;
        public static final int OVER_MAX_FACES = 7;
        public static final int SUCCESS = 0;
        public static final int SUCCESS_NEED_UPGRADE = 14;
        public static final int SUCCESS_NO_NEED_UPGRADE = 15;
        public static final int TIMEOUT = 4;
        public static final int UNKNOWN = 100;
    }

    public static final class FaceInfo {
        public int faceId;
        public boolean hasAlternateAppearance;
        public String name;
    }

    public static final class FaceRecognitionAbility {
        public int faceMode;
        public boolean isFaceRecognitionSupport;
        public int reserve;
        public int secureLevel;
    }

    public interface FaceRecognizeCallback {
        void onCallbackEvent(int i, int i2, int i3, int i4);
    }

    public FaceRecognizeManager(@NonNull Context context, @Nullable FaceRecognizeCallback callback) {
        this.mFaceCallback = callback;
        this.mFaceManagerImpl = FaceRecognizeManagerFactory.getInstance().getFaceRecognizeManagerImpl(context, new DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback() {
            /* class com.huawei.facerecognition.FaceRecognizeManager.AnonymousClass1 */

            @Override // huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback
            public void onCallbackEvent(int reqId, int type, int code, int errorCode) {
                if (FaceRecognizeManager.this.mFaceCallback != null) {
                    FaceRecognizeManager.this.mFaceCallback.onCallbackEvent(reqId, type, code, errorCode);
                }
            }
        });
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public int authenticate(int reqId, int flags, @Nullable Surface preview) {
        return this.mFaceManagerImpl.authenticate((long) reqId, flags, preview);
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public int cancelAuthenticate(int reqId) {
        return this.mFaceManagerImpl.cancelAuthenticate((long) reqId);
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int preparePayInfo(byte[] aaid, byte[] nonce, byte[] extra) {
        return this.mFaceManagerImpl.preparePayInfo(aaid, nonce, extra);
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int getPayResult(int[] faceId, byte[] token, int[] tokenLen, byte[] reserve) {
        return this.mFaceManagerImpl.getPayResult(faceId, token, tokenLen, reserve);
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int enroll(int reqId, int flags, byte[] token, @Nullable Surface preview) {
        return this.mFaceManagerImpl.enroll(reqId, flags, token, preview);
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int cancelEnroll(int reqId) {
        return this.mFaceManagerImpl.cancelEnroll(reqId);
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public long preEnroll() {
        return this.mFaceManagerImpl.preEnroll();
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int setEnrollInfo(int[] enrollInfo) {
        return this.mFaceManagerImpl.setEnrollInfo(enrollInfo);
    }

    public long getAuthenticatorId() {
        return this.mFaceManagerImpl.getAuthenticatorId();
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int postEnroll() {
        this.mFaceManagerImpl.postEnroll();
        return 0;
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int remove(int reqId, int faceId) {
        return this.mFaceManagerImpl.remove(reqId, faceId);
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public int init() {
        return this.mFaceManagerImpl.init();
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public int release() {
        return this.mFaceManagerImpl.release();
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public int[] getEnrolledFaceIDs() {
        return this.mFaceManagerImpl.getEnrolledFaceIDs();
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public FaceInfo getFaceInfo(int faceId) {
        DefaultFaceRecognizeManagerImpl.FaceInfo info = this.mFaceManagerImpl.getFaceInfo(faceId);
        if (info == null) {
            return null;
        }
        FaceInfo faceInfo = new FaceInfo();
        faceInfo.faceId = info.faceId;
        faceInfo.hasAlternateAppearance = info.hasAlternateAppearance;
        faceInfo.name = info.name;
        return faceInfo;
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int setFaceName(int faceId, String name) {
        return this.mFaceManagerImpl.rename(faceId, name);
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public int getHardwareSupportType() {
        return this.mFaceManagerImpl.getHardwareSupportType();
    }

    @RequiresPermission(HW_RESET_FACERECOGNITION_LOCKOUT)
    public void resetTimeout() {
        this.mFaceManagerImpl.resetTimeout();
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public int getRemainingNum() {
        return this.mFaceManagerImpl.getRemainingNum();
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public long getRemainingTime() {
        return this.mFaceManagerImpl.getRemainingTime();
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public int getTotalAuthFailedTimes() {
        return this.mFaceManagerImpl.getTotalAuthFailedTimes();
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int getAngleDim() {
        return this.mFaceManagerImpl.getAngleDim();
    }

    @RequiresPermission(anyOf = {"com.huawei.permission.USE_FACERECOGNITION", USE_FACERECOGNITION})
    public FaceRecognitionAbility getFaceRecognitionAbility() {
        DefaultFaceRecognizeManagerImpl.FaceRecognitionAbility ability = this.mFaceManagerImpl.getFaceRecognitionAbility();
        if (ability == null) {
            return null;
        }
        FaceRecognitionAbility faceAbility = new FaceRecognitionAbility();
        faceAbility.isFaceRecognitionSupport = ability.isFaceRecognitionSupport;
        faceAbility.faceMode = ability.faceMode;
        faceAbility.secureLevel = ability.secureLevel;
        faceAbility.reserve = ability.reserve;
        return faceAbility;
    }

    public int registerSecureRegCallBack(SecureRegCallBack callback) {
        return 1;
    }

    public static int registerSecureRegistryCallback(IBinder callback) {
        return FaceRecognizeManagerFactory.getInstance().getFaceRecognizeManagerImpl(null, null).registerSecureRegistryCallback(callback);
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int checkNeedUpgradeFeature(long reqId) {
        return this.mFaceManagerImpl.checkNeedUpgradeFeature(reqId);
    }

    @RequiresPermission(HW_MANAGE_FACERECOGNITION)
    public int upgradeFeature(long reqId, @NonNull byte[] authToken) {
        return this.mFaceManagerImpl.upgradeFeature(reqId, authToken);
    }
}
