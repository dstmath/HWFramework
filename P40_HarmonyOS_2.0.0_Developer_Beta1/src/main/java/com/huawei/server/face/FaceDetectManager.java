package com.huawei.server.face;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.huawei.android.biometric.FingerprintSupportEx;
import com.huawei.server.fingerprint.FingerViewController;

public class FaceDetectManager {
    private static final String TAG = "FaceDetectManager";
    private static FaceDetectManager sInstance;
    private final Runnable mCancelFaceAuthRunnable = new Runnable() {
        /* class com.huawei.server.face.FaceDetectManager.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            if (FaceDetectManager.this.mContext != null && FaceDetectManager.this.mHandler != null) {
                FaceViewController.getInstance().cancelFaceAuth();
            }
        }
    };
    private Context mContext;
    private int mCookie;
    private FingerViewController.ICallBack mFingerViewChangeCallback;
    private Handler mHandler = null;

    private FaceDetectManager() {
    }

    public static synchronized FaceDetectManager getInstance() {
        FaceDetectManager faceDetectManager;
        synchronized (FaceDetectManager.class) {
            if (sInstance == null) {
                sInstance = new FaceDetectManager();
            }
            faceDetectManager = sInstance;
        }
        return faceDetectManager;
    }

    public void initFaceDetect(Context context, Looper looper) {
        if (FaceViewController.isFaceThreeDimensional()) {
            this.mContext = context;
            this.mHandler = new Handler(looper) {
                /* class com.huawei.server.face.FaceDetectManager.AnonymousClass2 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    FaceDetectManager.this.procMessge(msg);
                }
            };
        }
    }

    public void registCallback(FingerViewController.ICallBack fingerViewChangeCallback) {
        if (FaceViewController.isFaceThreeDimensional()) {
            this.mFingerViewChangeCallback = fingerViewChangeCallback;
        }
    }

    public void setCookie(int cookie) {
        if (FaceViewController.isFaceThreeDimensional() && FaceViewController.getInstance().isFaceSupportBiometric()) {
            this.mCookie = cookie;
            Log.w(TAG, "startCurrentClient cookie " + cookie);
        }
    }

    public int getCookie() {
        return this.mCookie;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public void cancelFaceAuth() {
        if (FaceViewController.isFaceThreeDimensional() && this.mHandler != null && FaceViewController.getInstance().isFaceSupportBiometric() && FaceViewController.getInstance().isBiomericDetecting()) {
            if (FingerprintSupportEx.hasCallbacks(this.mHandler, this.mCancelFaceAuthRunnable)) {
                this.mHandler.removeCallbacks(this.mCancelFaceAuthRunnable);
            }
            this.mHandler.post(this.mCancelFaceAuthRunnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void procMessge(Message msg) {
        if (msg == null) {
            Log.w(TAG, "bioface msg is null");
        }
        Log.i(TAG, "bioface FingerViewController proc messge " + msg.what);
        if (msg.what == 1) {
            if (this.mHandler != null) {
                FingerViewController.getInstance(this.mContext).removeMaskOrButton();
            }
            if (this.mFingerViewChangeCallback != null) {
                Log.i(TAG, "bioface FingerViewController proc messge MSG_REMOVE_VIEW");
                this.mFingerViewChangeCallback.onFingerViewStateChange(3);
            }
        } else if (msg.what == 2) {
            if (this.mFingerViewChangeCallback != null) {
                Log.i(TAG, "bioface FingerViewController proc messge MSG_FACE_DETECT_SUCCESS");
                this.mFingerViewChangeCallback.onFingerViewStateChange(2);
            }
        } else if (msg.what != 3) {
            Log.i(TAG, "bioface FingerViewController others");
        } else if (this.mFingerViewChangeCallback != null) {
            Log.i(TAG, "bioface FingerViewController proc messge MSG_FACE_VIEW_INIT");
            this.mFingerViewChangeCallback.onFingerViewStateChange(1);
        }
    }
}
