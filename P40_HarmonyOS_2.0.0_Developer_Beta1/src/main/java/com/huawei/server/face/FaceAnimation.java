package com.huawei.server.face;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.utils.HwPartResourceUtils;

public class FaceAnimation {
    private static final String TAG = "FaceAnimation";
    private AnimationDrawable faceDdetectedAnimation;
    private AnimationDrawable faceFailAnimation;
    private AnimationDrawable faceSuccessAnimation;
    private boolean isFaceDetecting = false;
    private Context mContext;
    private String mCurrentFaceMessage;
    private Animation.AnimationListener mDetectedFailedAnimListener = new Animation.AnimationListener() {
        /* class com.huawei.server.face.FaceAnimation.AnonymousClass3 */

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
            FaceAnimation.this.handleFailDetectintAnimation();
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }
    };
    private Animation.AnimationListener mDetectedSucceededAnimListener = new Animation.AnimationListener() {
        /* class com.huawei.server.face.FaceAnimation.AnonymousClass1 */

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
            FaceAnimation.this.setDetectedAnimation();
            FaceAnimation.this.handleSucceedAnimation();
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }
    };
    private int mDetectedTime = 0;
    private Animation.AnimationListener mDetectingAnimListener = new Animation.AnimationListener() {
        /* class com.huawei.server.face.FaceAnimation.AnonymousClass2 */

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
            if (FaceAnimation.this.mFaceDetectedView != null && FaceAnimation.this.isFaceDetecting) {
                FaceAnimation.this.mFaceDetectedView.startAnimation(FaceAnimation.this.mScaleDetectingAnimation);
                Log.w(FaceAnimation.TAG, "bioface faceDetecting continue");
            }
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }
    };
    private Drawable mFaceDetectedDrawable;
    private ImageView mFaceDetectedView;
    private TextView mFaceMessage;
    private Handler mHandler;
    private Animation mScaleDetectingAnimation;

    public FaceAnimation(Context context, ImageView faceDetectedView, Handler handler, TextView faceMessage) {
        this.mContext = context;
        this.mHandler = handler;
        this.mFaceDetectedView = faceDetectedView;
        this.mFaceMessage = faceMessage;
        initAnimationParms();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFailDetectintAnimation() {
        setDetectedAnimation();
        handleFailedAnimation();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDetectedAnimation() {
        Drawable faceDetectDrawable = this.mContext.getDrawable(HwPartResourceUtils.getResourceId("detect_anmiation_complete"));
        ImageView imageView = this.mFaceDetectedView;
        if (imageView != null && faceDetectDrawable != null) {
            imageView.setBackground(faceDetectDrawable);
            if (faceDetectDrawable instanceof AnimationDrawable) {
                this.faceDdetectedAnimation = (AnimationDrawable) faceDetectDrawable;
                this.faceDdetectedAnimation.start();
                this.mDetectedTime = this.faceDdetectedAnimation.getNumberOfFrames() * this.mContext.getResources().getInteger(HwPartResourceUtils.getResourceId("face_animation_duration"));
                Log.w(TAG, "bioface setDetectedAnimation delay:" + this.mDetectedTime);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSucceedAnimation() {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.huawei.server.face.FaceAnimation.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                FaceAnimation.this.startSuccessedAnimation();
            }
        }, (long) this.mDetectedTime);
    }

    private void handleFailedAnimation() {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.huawei.server.face.FaceAnimation.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                FaceAnimation.this.startFailedAnimation();
            }
        }, (long) this.mDetectedTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startFailedAnimation() {
        Drawable faceDetectDrawable = this.mContext.getDrawable(HwPartResourceUtils.getResourceId("detect_anmiation_fail"));
        ImageView imageView = this.mFaceDetectedView;
        if (imageView != null && faceDetectDrawable != null) {
            imageView.setBackground(faceDetectDrawable);
            if (faceDetectDrawable instanceof AnimationDrawable) {
                this.faceFailAnimation = (AnimationDrawable) faceDetectDrawable;
                this.faceFailAnimation.start();
                this.mFaceMessage.setText(this.mCurrentFaceMessage);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSuccessedAnimation() {
        Drawable faceDetectDrawable = this.mContext.getDrawable(HwPartResourceUtils.getResourceId("detect_anmiation_success"));
        ImageView imageView = this.mFaceDetectedView;
        if (imageView != null && faceDetectDrawable != null) {
            imageView.setBackground(faceDetectDrawable);
            if (faceDetectDrawable instanceof AnimationDrawable) {
                this.faceSuccessAnimation = (AnimationDrawable) faceDetectDrawable;
                this.faceSuccessAnimation.start();
                this.mFaceMessage.setText(this.mCurrentFaceMessage);
            }
        }
    }

    private void initAnimationParms() {
        Drawable drawable;
        this.mFaceDetectedDrawable = this.mContext.getDrawable(HwPartResourceUtils.getResourceId("ic_face_all_info"));
        ImageView imageView = this.mFaceDetectedView;
        if (imageView != null && (drawable = this.mFaceDetectedDrawable) != null) {
            imageView.setBackground(drawable);
            this.mScaleDetectingAnimation = AnimationUtils.loadAnimation(this.mContext, HwPartResourceUtils.getResourceId("ic_face_detecting_anim"));
        }
    }

    public void faceDetecting(String faceMessage) {
        TextView textView;
        if (this.mFaceDetectedView != null && (textView = this.mFaceMessage) != null && this.mScaleDetectingAnimation != null && !this.isFaceDetecting) {
            this.isFaceDetecting = true;
            this.mCurrentFaceMessage = faceMessage;
            textView.setText(this.mCurrentFaceMessage);
            this.mFaceDetectedView.setBackground(this.mFaceDetectedDrawable);
            this.mScaleDetectingAnimation.setAnimationListener(this.mDetectingAnimListener);
            this.mFaceDetectedView.startAnimation(this.mScaleDetectingAnimation);
            Log.w(TAG, "bioface faceDetecting");
        }
    }

    public void faceDetectedSuccess(String faceMessage) {
        ImageView imageView = this.mFaceDetectedView;
        if (imageView != null && this.mScaleDetectingAnimation != null && this.isFaceDetecting) {
            this.mCurrentFaceMessage = faceMessage;
            imageView.setBackground(this.mFaceDetectedDrawable);
            this.mScaleDetectingAnimation.setAnimationListener(this.mDetectedSucceededAnimListener);
            this.mFaceDetectedView.startAnimation(this.mScaleDetectingAnimation);
            this.isFaceDetecting = false;
            Log.w(TAG, "bioface faceDetectedSuccess");
        }
    }

    public void faceDetectedFailed(String faceMessage) {
        ImageView imageView = this.mFaceDetectedView;
        if (imageView != null && this.mScaleDetectingAnimation != null && this.isFaceDetecting) {
            this.mCurrentFaceMessage = faceMessage;
            imageView.setBackground(this.mFaceDetectedDrawable);
            this.mScaleDetectingAnimation.setAnimationListener(this.mDetectedFailedAnimListener);
            this.mFaceDetectedView.startAnimation(this.mScaleDetectingAnimation);
            this.isFaceDetecting = false;
            Log.w(TAG, "bioface faceDetectedFailed");
        }
    }
}
