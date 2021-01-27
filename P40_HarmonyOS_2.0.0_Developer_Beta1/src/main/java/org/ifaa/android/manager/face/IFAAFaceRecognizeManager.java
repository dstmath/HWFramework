package org.ifaa.android.manager.face;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;
import com.huawei.facerecognition.FaceRecognizeManager;
import org.ifaa.android.manager.face.IFAAFaceManager;

public class IFAAFaceRecognizeManager {
    public static final int CODE_CALLBACK_ACQUIRE = 3;
    public static final int CODE_CALLBACK_RESULT = 1;
    public static final int IFAA_FACE_AUTHENTICATOR_FAIL = 103;
    public static final int IFAA_FACE_AUTHENTICATOR_SUCCESS = 100;
    public static final int IFAA_FACE_AUTH_ERROR_CANCEL = 102;
    public static final int IFAA_FACE_AUTH_ERROR_LOCKED = 129;
    public static final int IFAA_FACE_AUTH_ERROR_TIMEOUT = 113;
    public static final int IFAA_FACE_AUTH_STATUS_BRIGHT = 406;
    public static final int IFAA_FACE_AUTH_STATUS_DARK = 405;
    public static final int IFAA_FACE_AUTH_STATUS_EYE_CLOSED = 403;
    public static final int IFAA_FACE_AUTH_STATUS_FACE_OFFET_BOTTOM = 412;
    public static final int IFAA_FACE_AUTH_STATUS_FACE_OFFET_LEFT = 409;
    public static final int IFAA_FACE_AUTH_STATUS_FACE_OFFET_RIGHT = 410;
    public static final int IFAA_FACE_AUTH_STATUS_FACE_OFFET_TOP = 411;
    public static final int IFAA_FACE_AUTH_STATUS_FAR_FACE = 404;
    public static final int IFAA_FACE_AUTH_STATUS_INSUFFICIENT = 402;
    public static final int IFAA_FACE_AUTH_STATUS_MOUTH_OCCLUSION = 408;
    public static final int IFAA_FACE_AUTH_STATUS_PARTIAL = 401;
    public static final int IFAA_FACE_AUTH_STATUS_QUALITY = 407;
    private static final int IFAA_FACE_ERRORCODE_MAXSIZE = 16;
    public static final String LOG_TAG = "IFAAFaceRecognize";
    public static final int TYPE_CALLBACK_AUTH = 2;
    private static FaceRecognizeManager hwFaceManager;
    private static IFAAFaceRecognizeManager ifaaFrManager;
    private IFAAFaceManager.AuthenticatorCallback mAuthenticatorCallback;

    public static int converHwAcquireInfoToIFAA(int hwAcquireInfo) {
        Log.e(LOG_TAG, "converHwhwAcquireInfoToIFAA hwAcquireInfo is" + hwAcquireInfo);
        SparseIntArray codeMap = new SparseIntArray(16);
        codeMap.put(29, IFAA_FACE_AUTH_STATUS_PARTIAL);
        codeMap.put(31, IFAA_FACE_AUTH_STATUS_BRIGHT);
        codeMap.put(30, IFAA_FACE_AUTH_STATUS_DARK);
        codeMap.put(22, IFAA_FACE_AUTH_STATUS_EYE_CLOSED);
        codeMap.put(11, IFAA_FACE_AUTH_STATUS_FACE_OFFET_BOTTOM);
        codeMap.put(10, IFAA_FACE_AUTH_STATUS_FACE_OFFET_RIGHT);
        codeMap.put(9, IFAA_FACE_AUTH_STATUS_FACE_OFFET_TOP);
        codeMap.put(8, IFAA_FACE_AUTH_STATUS_FACE_OFFET_LEFT);
        codeMap.put(7, IFAA_FACE_AUTH_STATUS_FAR_FACE);
        codeMap.put(6, IFAA_FACE_AUTH_STATUS_INSUFFICIENT);
        codeMap.put(5, IFAA_FACE_AUTH_STATUS_INSUFFICIENT);
        codeMap.put(4, IFAA_FACE_AUTH_STATUS_QUALITY);
        codeMap.put(0, 100);
        return codeMap.get(hwAcquireInfo, IFAA_FACE_AUTHENTICATOR_FAIL);
    }

    public static int converHwErrorCodeToIFAA(int hwErrorCode) {
        Log.e(LOG_TAG, "converHwErrorCodeToIFAA hwErrorCode is" + hwErrorCode);
        SparseIntArray errorCodeMap = new SparseIntArray(16);
        errorCodeMap.put(0, 100);
        errorCodeMap.put(4, IFAA_FACE_AUTH_ERROR_TIMEOUT);
        errorCodeMap.put(2, IFAA_FACE_AUTH_ERROR_CANCEL);
        errorCodeMap.put(8, IFAA_FACE_AUTH_ERROR_LOCKED);
        return errorCodeMap.get(hwErrorCode, IFAA_FACE_AUTHENTICATOR_FAIL);
    }

    public static synchronized void createInstance(Context context) {
        synchronized (IFAAFaceRecognizeManager.class) {
            if (ifaaFrManager == null) {
                ifaaFrManager = new IFAAFaceRecognizeManager(context);
            }
        }
    }

    public static synchronized IFAAFaceRecognizeManager getInstance() {
        IFAAFaceRecognizeManager iFAAFaceRecognizeManager;
        synchronized (IFAAFaceRecognizeManager.class) {
            iFAAFaceRecognizeManager = ifaaFrManager;
        }
        return iFAAFaceRecognizeManager;
    }

    public void onFaceCallbackEvent(int reqId, int type, int code, int errorCode) {
        Log.i(LOG_TAG, "onCallbackEvent gotten reqId" + reqId + " type " + type + " code " + code + "errCode" + errorCode);
        IFAAFaceManager.AuthenticatorCallback authenticatorCallback = this.mAuthenticatorCallback;
        if (authenticatorCallback == null) {
            Log.e(LOG_TAG, "mAuthenticatorCallback empty ");
            release();
        } else if (type != 2) {
            Log.e(LOG_TAG, "gotten not ifaa's auth callback reqid " + reqId + " type " + type + " code " + code + "errCode" + errorCode);
        } else if (code == 1) {
            int errCodeIfaa = converHwErrorCodeToIFAA(errorCode);
            Log.i(LOG_TAG, "errCodeIfaa" + errCodeIfaa);
            if (errCodeIfaa == 100) {
                Log.i(LOG_TAG, "ifaa face auth success");
                this.mAuthenticatorCallback.onAuthenticationSucceeded();
            } else if (errCodeIfaa == 102 || errCodeIfaa == 113 || errCodeIfaa == 129) {
                this.mAuthenticatorCallback.onAuthenticationError(errCodeIfaa);
            } else {
                this.mAuthenticatorCallback.onAuthenticationFailed(errCodeIfaa);
                Log.e(LOG_TAG, "fail reason" + errCodeIfaa);
            }
            release();
        } else if (code == 3) {
            authenticatorCallback.onAuthenticationStatus(converHwAcquireInfoToIFAA(errorCode));
        } else {
            Log.e(LOG_TAG, "bad err code,ignore");
        }
    }

    public IFAAFaceRecognizeManager(Context context) {
        if (hwFaceManager == null) {
            hwFaceManager = new FaceRecognizeManager(context, new FaceRecognizeManager.FaceRecognizeCallback() {
                /* class org.ifaa.android.manager.face.IFAAFaceRecognizeManager.AnonymousClass1 */

                public void onCallbackEvent(int reqId, int type, int code, int err) {
                    IFAAFaceRecognizeManager.this.onFaceCallbackEvent(reqId, type, code, err);
                }
            });
        }
    }

    public static FaceRecognizeManager getFRManager() {
        return hwFaceManager;
    }

    public int init() {
        FaceRecognizeManager faceRecognizeManager = hwFaceManager;
        if (faceRecognizeManager != null) {
            return faceRecognizeManager.init();
        }
        return -1;
    }

    public void release() {
        FaceRecognizeManager faceRecognizeManager = hwFaceManager;
        if (faceRecognizeManager != null) {
            faceRecognizeManager.release();
        }
    }

    public void setAuthCallback(IFAAFaceManager.AuthenticatorCallback authCallback) {
        this.mAuthenticatorCallback = authCallback;
    }
}
