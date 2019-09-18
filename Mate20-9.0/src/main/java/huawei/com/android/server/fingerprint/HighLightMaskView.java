package huawei.com.android.server.fingerprint;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.server.gesture.GestureNavConst;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_CANCEL;

public class HighLightMaskView extends FrameLayout {
    private static final String TAG = "HighLightMaskView";
    private View mBlackMaskView;
    /* access modifiers changed from: private */
    public int mCenterX;
    /* access modifiers changed from: private */
    public int mCenterY;
    private View mCircleMaskView;
    private int mHighLightShowType = -1;
    private WindowManager.LayoutParams mHighLightViewParams;
    /* access modifiers changed from: private */
    public boolean mIsCircleViewVisible;
    /* access modifiers changed from: private */
    public final Paint mPaint = new Paint(1);
    private String mPkgName;
    /* access modifiers changed from: private */
    public int mRadius;
    private int[] mSampleAlpha = null;
    private int[] mSampleBrightness = null;
    /* access modifiers changed from: private */
    public float mScale;

    public HighLightMaskView(Context context, int brightness, int radius, int color) {
        super(context);
        init(context, brightness, radius, color);
    }

    public void setAlpha(int alpha) {
        if (this.mBlackMaskView != null) {
            this.mBlackMaskView.getBackground().setAlpha(alpha);
        }
    }

    public float getAlpha() {
        if (this.mBlackMaskView != null) {
            return (float) this.mBlackMaskView.getBackground().getAlpha();
        }
        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void init(Context context, int brightness, int radius, int color) {
        this.mPaint.setDither(true);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(color);
        this.mPaint.setMaskFilter(new BlurMaskFilter(20.0f, BlurMaskFilter.Blur.SOLID));
        this.mRadius = radius;
        this.mBlackMaskView = new View(this.mContext);
        this.mBlackMaskView.setBackgroundColor(-16777216);
        int alphaNew = 0;
        if (brightness > 0) {
            alphaNew = FingerViewController.getInstance(this.mContext).getMaskAlpha(brightness);
        }
        Log.i(TAG, "alphaNew:" + alphaNew + "radius = " + radius);
        this.mBlackMaskView.getBackground().setAlpha(alphaNew);
        addView(this.mBlackMaskView, -1, -1);
        this.mCircleMaskView = new View(this.mContext) {
            public void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                Log.i(HighLightMaskView.TAG, " mCircleMaskView onDraw");
                canvas.drawCircle(((float) HighLightMaskView.this.mCenterX) * HighLightMaskView.this.mScale, ((float) HighLightMaskView.this.mCenterY) * HighLightMaskView.this.mScale, ((float) HighLightMaskView.this.mRadius) * HighLightMaskView.this.mScale, HighLightMaskView.this.mPaint);
                if (HighLightMaskView.this.mIsCircleViewVisible) {
                    Log.i(HighLightMaskView.TAG, " mCircleMaskView notifyCaptureImage 1");
                    FingerViewController.getInstance(this.mContext).notifyCaptureImage();
                }
            }
        };
        addView(this.mCircleMaskView, -1, -1);
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

    public void setCircleVisibility(int visibility) {
        if (this.mCircleMaskView != null) {
            Log.i(TAG, " mCircleMaskView setVisibility:" + visibility);
            if (visibility == 0 || this.mCircleMaskView.getVisibility() != visibility) {
                this.mCircleMaskView.setVisibility(visibility);
                if (visibility == 0) {
                    this.mIsCircleViewVisible = true;
                } else {
                    Log.i(TAG, " mCircleMaskView notifyDismissBlueSpot ");
                    FingerViewController.getInstance(this.mContext).notifyDismissBlueSpot();
                    this.mIsCircleViewVisible = false;
                }
            } else {
                Log.i(TAG, " visibility is already INVISIBLE skip");
            }
        }
    }

    public int getCircleVisibility() {
        if (this.mCircleMaskView != null) {
            return this.mCircleMaskView.getVisibility();
        }
        return 4;
    }

    public void setType(int type) {
        this.mHighLightShowType = type;
        Log.i(TAG, "mHighLightShowType = " + this.mScale);
    }

    public void setPackageName(String pkgName) {
        this.mPkgName = pkgName;
        Log.i(TAG, "mPkgName = " + this.mPkgName);
    }

    public WindowManager.LayoutParams getHighlightViewParams() {
        if (this.mHighLightViewParams == null) {
            this.mHighLightViewParams = new WindowManager.LayoutParams(-1, -1);
            this.mHighLightViewParams.layoutInDisplayCutoutMode = 1;
            this.mHighLightViewParams.type = FingerViewController.TYPE_FINGER_VIEW;
            this.mHighLightViewParams.flags = 280;
            this.mHighLightViewParams.privateFlags |= RET_REG_CANCEL.ID;
            this.mHighLightViewParams.format = -3;
        }
        return this.mHighLightViewParams;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPaint.setXfermode(null);
    }
}
