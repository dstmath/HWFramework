package com.huawei.server.fingerprint;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.huawei.android.biometric.FingerprintSupportEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.List;
import java.util.Locale;

public class FingerprintAnimByThemeView extends FrameLayout {
    private static final String TAG = "FingerprintAnimViewByTheme";
    private AnimationDrawable mAnimationDrawable = new AnimationDrawable();
    private int mCenterX;
    private int mCenterY;
    private String mCurrentLang;
    private ImageView mFingerprintAnimImageView;
    private boolean mIsAdded;
    private float mScale;
    private WindowManager.LayoutParams mViewParams;

    public FingerprintAnimByThemeView(Context context, List<String> animFileNames, int fpAnimFps) {
        super(context);
        if (animFileNames == null || fpAnimFps == 0) {
            Log.i(TAG, " mFingerprintAnimImageView animFileNames or fpAnimFps is null");
            return;
        }
        this.mFingerprintAnimImageView = (ImageView) LayoutInflater.from(context).inflate(HwPartResourceUtils.getResourceId("fingerprint_anim_view_themes"), (ViewGroup) null).findViewById(HwPartResourceUtils.getResourceId("iv_fp_anim"));
        Log.i(TAG, " mFingerprintAnimImageView " + this.mFingerprintAnimImageView);
        ViewParent viewParent = this.mFingerprintAnimImageView.getParent();
        if (viewParent != null && (viewParent instanceof ViewGroup)) {
            ((ViewGroup) viewParent).removeView(this.mFingerprintAnimImageView);
        }
        for (String animFileName : animFileNames) {
            this.mAnimationDrawable.addFrame(new BitmapDrawable(BitmapFactory.decodeFile(animFileName)), FingerViewController.TIME_UNIT / fpAnimFps);
        }
        setFingerAnimViewState();
        this.mFingerprintAnimImageView.setVisibility(4);
        addView(this.mFingerprintAnimImageView);
        this.mCurrentLang = Locale.getDefault().getLanguage();
    }

    public void setCenterPoints(int centerX, int centerY) {
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        Log.i(TAG, "mCenterX = " + this.mCenterX + ",mCenterY=" + this.mCenterY);
    }

    public float getScale() {
        Log.i(TAG, "getScale mScale = " + this.mScale);
        return this.mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
        Log.i(TAG, "mScale = " + this.mScale);
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public boolean isLanguageChange() {
        String currentLang = Locale.getDefault().getLanguage();
        if (currentLang == null || currentLang.equals(this.mCurrentLang)) {
            return false;
        }
        this.mCurrentLang = currentLang;
        return true;
    }

    public void setAnimationPosition() {
        int viewHeight = this.mFingerprintAnimImageView.getHeight();
        int viewWidth = this.mFingerprintAnimImageView.getWidth();
        Log.i(TAG, " viewHeight is " + viewHeight + " viewWidth is " + viewWidth);
        int centerY = viewHeight / 2;
        int centerX = viewWidth / 2;
        if (FingerprintSupportEx.isRtlLocale(this)) {
            this.mFingerprintAnimImageView.setTranslationX(((float) centerX) - (((float) this.mCenterX) * this.mScale));
            Log.i(TAG, " isRtlLocale centerX is " + (((float) this.mCenterX) * this.mScale));
        } else {
            this.mFingerprintAnimImageView.setTranslationX((((float) this.mCenterX) * this.mScale) - ((float) centerX));
        }
        this.mFingerprintAnimImageView.setTranslationY((((float) this.mCenterY) * this.mScale) - ((float) centerY));
        this.mFingerprintAnimImageView.setVisibility(0);
        this.mAnimationDrawable.start();
        Log.i(TAG, " come in mAnimationDrawable strart ");
    }

    private void setFingerAnimViewState() {
        this.mAnimationDrawable.setOneShot(true);
        this.mFingerprintAnimImageView.setImageDrawable(this.mAnimationDrawable);
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ImageView imageView = this.mFingerprintAnimImageView;
        if (imageView != null) {
            imageView.setVisibility(4);
        }
        this.mAnimationDrawable.stop();
        Log.i(TAG, " come in mAnimationDrawable stop ");
    }

    public WindowManager.LayoutParams getFingerViewParams() {
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
        Log.i(TAG, " animation view added =" + isAdded);
        this.mIsAdded = isAdded;
    }

    public void destroy() {
        Log.i(TAG, " come in destroy ");
        this.mFingerprintAnimImageView = null;
    }
}
