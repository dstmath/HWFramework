package com.huawei.facerecognition;

import android.content.Context;
import android.view.Surface;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;

public class FaceRecognizeManager {
    public static final int CODE_CALLBACK_ACQUIRE = 3;
    public static final int CODE_CALLBACK_BUSY = 4;
    public static final int CODE_CALLBACK_CANCEL = 2;
    public static final int CODE_CALLBACK_OUT_OF_MEM = 5;
    public static final int CODE_CALLBACK_RESULT = 1;
    public static final int INVALID_FACE_ID = -1;
    public static final int REQUEST_OK = 0;
    public static final int TYPE_CALLBACK_AUTH = 2;
    public static final int TYPE_CALLBACK_ENROLL = 1;
    public static final int TYPE_CALLBACK_REMOVE = 3;
    /* access modifiers changed from: private */
    public FaceRecognizeCallback mFaceCallback;
    private FaceRecognizeManagerImpl mFaceManagerImpl;

    public interface AcquireInfo {
        public static final int FACEID_ENROLL_INFO_BEGIN = 2000;
        public static final int FACEID_FACE_ANGLE_BASE = 1000;
        public static final int FACEID_FACE_DETECTED = 38;
        public static final int FACEID_FACE_MOVED = 33;
        public static final int FACEID_HAS_REGISTERED = 37;
        public static final int FACEID_NOT_GAZE = 36;
        public static final int FACEID_NOT_THE_SAME_FACE = 35;
        public static final int FACEID_OUT_OF_BOUNDS = 34;
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
        public static final int FACE_UNLOCK_FACE_SCALE_TOO_LARGE = 7;
        public static final int FACE_UNLOCK_FACE_SCALE_TOO_SMALL = 6;
        public static final int FACE_UNLOCK_FAILURE = 3;
        public static final int FACE_UNLOCK_IMAGE_BLUR = 20;
        public static final int FACE_UNLOCK_INVALID_ARGUMENT = 1;
        public static final int FACE_UNLOCK_INVALID_HANDLE = 2;
        public static final int FACE_UNLOCK_LIVENESS_FAILURE = 14;
        public static final int FACE_UNLOCK_LIVENESS_WARNING = 13;
        public static final int FACE_UNLOCK_OK = 0;
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
        public static final int TIMEOUT = 4;
        public static final int UNKNOWN = 100;
    }

    public static final class FaceInfo {
        public int faceId;
        public boolean hasAlternateAppearance;
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

    public FaceRecognizeManager(Context context, FaceRecognizeCallback callback) {
        this.mFaceCallback = callback;
        this.mFaceManagerImpl = new FaceRecognizeManagerImpl(context, new FaceRecognizeManagerImpl.FaceRecognizeCallback() {
            public void onCallbackEvent(int reqId, int type, int code, int errorCode) {
                if (FaceRecognizeManager.this.mFaceCallback != null) {
                    FaceRecognizeManager.this.mFaceCallback.onCallbackEvent(reqId, type, code, errorCode);
                }
            }
        });
    }

    public int authenticate(int reqId, int flags, Surface preview) {
        return this.mFaceManagerImpl.authenticate((long) reqId, flags, preview);
    }

    public int cancelAuthenticate(int reqId) {
        return this.mFaceManagerImpl.cancelAuthenticate((long) reqId);
    }

    public int preparePayInfo(byte[] aaid, byte[] nonce, byte[] extra) {
        return this.mFaceManagerImpl.preparePayInfo(aaid, nonce, extra);
    }

    public int getPayResult(int[] faceId, byte[] token, int[] tokenLen, byte[] reserve) {
        return this.mFaceManagerImpl.getPayResult(faceId, token, tokenLen, reserve);
    }

    public int enroll(int reqId, int flags, byte[] token, Surface preview) {
        return this.mFaceManagerImpl.enroll(reqId, flags, token, preview);
    }

    public int cancelEnroll(int reqId) {
        return this.mFaceManagerImpl.cancelEnroll(reqId);
    }

    public long preEnroll() {
        return this.mFaceManagerImpl.preEnroll();
    }

    public int postEnroll() {
        return this.mFaceManagerImpl.postEnroll();
    }

    public int remove(int reqId, int faceId) {
        return this.mFaceManagerImpl.remove(reqId, faceId);
    }

    public int init() {
        return this.mFaceManagerImpl.init();
    }

    public int release() {
        return this.mFaceManagerImpl.release();
    }

    public int[] getEnrolledFaceIDs() {
        return this.mFaceManagerImpl.getEnrolledFaceIDs();
    }

    public FaceInfo getFaceInfo(int faceId) {
        FaceRecognizeManagerImpl.FaceInfo info = this.mFaceManagerImpl.getFaceInfo(faceId);
        if (info == null) {
            return null;
        }
        FaceInfo faceInfo = new FaceInfo();
        faceInfo.faceId = info.faceId;
        faceInfo.hasAlternateAppearance = info.hasAlternateAppearance;
        return faceInfo;
    }

    public int getHardwareSupportType() {
        return this.mFaceManagerImpl.getHardwareSupportType();
    }

    public void resetTimeout() {
        this.mFaceManagerImpl.resetTimeout();
    }

    public int getRemainingNum() {
        return this.mFaceManagerImpl.getRemainingNum();
    }

    public long getRemainingTime() {
        return this.mFaceManagerImpl.getRemainingTime();
    }

    public int getTotalAuthFailedTimes() {
        return this.mFaceManagerImpl.getTotalAuthFailedTimes();
    }

    public int getAngleDim() {
        return this.mFaceManagerImpl.getAngleDim();
    }

    public FaceRecognitionAbility getFaceRecognitionAbility() {
        FaceRecognizeManagerImpl.FaceRecognitionAbility ability = this.mFaceManagerImpl.getFaceRecognitionAbility();
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
}
