package huawei.com.android.server.fingerprint;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.server.fingerprint.fingerprintAnimation.WaterEffectView;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_CANCEL;

public class FingerprintAnimationView extends FrameLayout {
    private static String TAG = "FingerprintAnimationView";
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
        String str = TAG;
        Log.d(str, "mCenterX = " + this.mCenterX + ",mCenterY=" + this.mCenterY);
    }

    public void setScale(float scale) {
        this.mScale = scale;
        String str = TAG;
        Log.d(str, "mScale = " + this.mScale);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mWaterEffectView != null) {
            this.mWaterEffectView.onResume();
            this.mWaterEffectView.playAnim(((float) this.mCenterX) * this.mScale, ((float) this.mCenterY) * this.mScale);
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mWaterEffectView != null) {
            this.mWaterEffectView.onPause();
        }
    }

    public WindowManager.LayoutParams getViewParams() {
        if (this.mViewParams == null) {
            this.mViewParams = new WindowManager.LayoutParams(-1, -1);
            this.mViewParams.layoutInDisplayCutoutMode = 1;
            this.mViewParams.type = FingerViewController.TYPE_FINGER_VIEW;
            this.mViewParams.flags = 1304;
            this.mViewParams.privateFlags |= RET_REG_CANCEL.ID;
            this.mViewParams.format = -3;
        }
        return this.mViewParams;
    }

    public boolean isAdded() {
        return this.mIsAdded;
    }

    public void setAddState(boolean isAdded) {
        String str = TAG;
        Log.i(str, "is animation view added =" + isAdded);
        this.mIsAdded = isAdded;
    }
}
