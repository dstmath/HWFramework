package com.huawei.server.fingerprint;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.huawei.android.view.WindowManagerEx;
import huawei.com.android.server.fingerprint.FingerViewController;

public class HighLightMaskView extends FrameLayout {
    private static final float FILTER_RADIUS = 20.0f;
    private static final String TAG = "HighLightMaskView";
    private View mBlackMaskView;
    private int mCenterX;
    private int mCenterY;
    private View mCircleMaskView;
    private int mHighLightShowType = -1;
    private WindowManager.LayoutParams mHighLightViewParams;
    private boolean mIsCircleViewVisible;
    private final Paint mPaint = new Paint(1);
    private String mPkgName;
    private int mRadius;
    private float mScale;

    public HighLightMaskView(Context context, int brightness, int radius, int color) {
        super(context);
        init(context, brightness, radius, color);
    }

    public void setAlpha(int alpha) {
        View view = this.mBlackMaskView;
        if (view != null) {
            view.getBackground().setAlpha(alpha);
        }
    }

    @Override // android.view.View
    public float getAlpha() {
        View view = this.mBlackMaskView;
        if (view != null) {
            return (float) view.getBackground().getAlpha();
        }
        return 0.0f;
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void init(Context context, int brightness, int radius, int color) {
        this.mPaint.setDither(true);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(color);
        this.mPaint.setMaskFilter(new BlurMaskFilter(FILTER_RADIUS, BlurMaskFilter.Blur.SOLID));
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
            /* class com.huawei.server.fingerprint.HighLightMaskView.AnonymousClass1 */

            @Override // android.view.View
            public void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                Log.i(HighLightMaskView.TAG, " mCircleMaskView onDraw");
                FingerprintController.getInstance().setViewByDisplaySettings(HighLightMaskView.this.mBlackMaskView);
                FingerprintController.getInstance().setViewByDisplaySettings(HighLightMaskView.this.mCircleMaskView);
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
                    return;
                }
                Log.i(TAG, " mCircleMaskView notifyDismissBlueSpot ");
                this.mIsCircleViewVisible = false;
                return;
            }
            Log.i(TAG, " visibility is already INVISIBLE skip");
        }
    }

    public int getCircleVisibility() {
        View view = this.mCircleMaskView;
        if (view != null) {
            return view.getVisibility();
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
            WindowManager.LayoutParams layoutParams = this.mHighLightViewParams;
            layoutParams.layoutInDisplayCutoutMode = 1;
            layoutParams.type = FingerViewController.TYPE_FINGER_VIEW;
            layoutParams.flags = 280;
            layoutParams.privateFlags |= -2147483632;
            WindowManager.LayoutParams layoutParams2 = this.mHighLightViewParams;
            layoutParams2.format = -3;
            new WindowManagerEx.LayoutParamsEx(layoutParams2).setDisplaySideMode(1);
        }
        return this.mHighLightViewParams;
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPaint.setXfermode(null);
    }
}
