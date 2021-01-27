package com.huawei.server.fingerprint;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.huawei.android.biometric.FingerprintSupportEx;
import com.huawei.server.fingerprint.fingerprintanimation.WaterEffectView;

public class FingerprintAnimationView extends FrameLayout {
    private static final String TAG = "FingerprintAnimationView";
    private int mCenterX;
    private int mCenterY;
    private boolean mIsAdded = false;
    private float mScale;
    private WindowManager.LayoutParams mViewParams;
    private WaterEffectView mWaterEffectView;

    public FingerprintAnimationView(Context context) {
        super(context);
        this.mWaterEffectView = new WaterEffectView(context);
        addView(this.mWaterEffectView, -1, -1);
    }

    public void setCenterPoints(int centerX, int centerY) {
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        Log.d(TAG, "mCenterX = " + this.mCenterX + ",mCenterY=" + this.mCenterY);
    }

    public void setScale(float scale) {
        this.mScale = scale;
        Log.d(TAG, "mScale = " + this.mScale);
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        WaterEffectView waterEffectView = this.mWaterEffectView;
        if (waterEffectView != null) {
            waterEffectView.onResume();
            WaterEffectView waterEffectView2 = this.mWaterEffectView;
            float f = this.mScale;
            waterEffectView2.playAnim(((float) this.mCenterX) * f, ((float) this.mCenterY) * f);
        }
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        WaterEffectView waterEffectView = this.mWaterEffectView;
        if (waterEffectView != null) {
            waterEffectView.onPause();
        }
    }

    public WindowManager.LayoutParams getViewParams() {
        if (this.mViewParams == null) {
            this.mViewParams = new WindowManager.LayoutParams(-1, -1);
            FingerprintSupportEx.setLayoutInDisplayCutoutMode(this.mViewParams, FingerprintSupportEx.getLayoutInDisplayCutoutModeAlways());
            FingerprintSupportEx.setlayoutInDisplaySideMode(this.mViewParams, 1);
            WindowManager.LayoutParams layoutParams = this.mViewParams;
            layoutParams.type = FingerViewController.TYPE_FINGER_VIEW;
            layoutParams.flags = 1304;
            FingerprintSupportEx.setLayoutParamsPrivateFlags(layoutParams, FingerprintSupportEx.getPrivateFlagShowForAllUsers() | FingerprintSupportEx.getPrivateFlagHideNaviBar());
            this.mViewParams.format = -3;
        }
        return this.mViewParams;
    }

    public boolean isAdded() {
        return this.mIsAdded;
    }

    public void setAddState(boolean isAdded) {
        Log.i(TAG, "is animation view added =" + isAdded);
        this.mIsAdded = isAdded;
    }
}
