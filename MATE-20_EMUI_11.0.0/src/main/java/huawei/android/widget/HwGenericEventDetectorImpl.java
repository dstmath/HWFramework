package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.HwGenericEventDetector;

public class HwGenericEventDetectorImpl implements HwGenericEventDetector {
    private static final float INVALID_POSITION_VALUE = -1.0f;
    private static final float LIST_DRAG_SLOP = 0.0f;
    private static final int PROGRESS_INCREMENT = 1;
    private static final int SCALED_SCROLL_FACTOR = 64;
    private static final float SCROLL_FACTOR_HIGH = 1.5f;
    private static final float SCROLL_FACTOR_LOW = 0.5f;
    private static final float SCROLL_FACTOR_MIDDLE = 1.0f;
    private static final String TAG = "HwGenericEventDetectorImpl";
    private float mHorizontalScrollFactor = 0.0f;
    private float mMotionEventX = INVALID_POSITION_VALUE;
    private float mMotionEventY = INVALID_POSITION_VALUE;
    private HwOnChangeProgressListener mOnChangeProgressListener = null;
    private HwOnChangePageListener mOnNextPageListener = null;
    private HwOnScrollListener mOnScrollListener = null;
    private float mSensitivity = 1.0f;
    private float mVerticalScrollFactor = 0.0f;

    public HwGenericEventDetectorImpl(Context context) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        if (Build.VERSION.SDK_INT >= 26) {
            this.mHorizontalScrollFactor = viewConfiguration.getScaledHorizontalScrollFactor();
            this.mVerticalScrollFactor = viewConfiguration.getScaledVerticalScrollFactor();
            return;
        }
        Resources resources = context.getResources();
        if (resources == null) {
            Log.e(TAG, "HwGenericEventDetectorImpl fail to call getResources.");
            return;
        }
        DisplayMetrics metrics = resources.getDisplayMetrics();
        if (metrics == null) {
            Log.e(TAG, "HwGenericEventDetectorImpl fail to call getDisplayMetrics.");
            return;
        }
        this.mHorizontalScrollFactor = TypedValue.applyDimension(1, 64.0f, metrics);
        this.mVerticalScrollFactor = this.mHorizontalScrollFactor;
    }

    public void setOnChangePageListener(HwOnChangePageListener listener) {
        this.mOnNextPageListener = listener;
    }

    public HwOnChangePageListener getOnChangePageListener() {
        return this.mOnNextPageListener;
    }

    public void setOnChangeProgressListener(HwOnChangeProgressListener listener) {
        this.mOnChangeProgressListener = listener;
    }

    public HwOnChangeProgressListener getOnChangeProgressListener() {
        return this.mOnChangeProgressListener;
    }

    public void setOnScrollListener(HwOnScrollListener listener) {
        this.mOnScrollListener = listener;
    }

    public HwOnScrollListener getOnScrollListener() {
        return this.mOnScrollListener;
    }

    public void setSensitivityMode(int mode) {
        if (mode == 0) {
            this.mSensitivity = SCROLL_FACTOR_HIGH;
        } else if (mode == 2) {
            this.mSensitivity = 0.5f;
        } else {
            this.mSensitivity = 1.0f;
        }
    }

    public void setSensitivity(float sensitivity) {
        this.mSensitivity = sensitivity;
    }

    public float getSensitivity() {
        return this.mSensitivity;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!event.isFromSource(2) || event.getAction() != 8) {
            return false;
        }
        float scrollH = event.getAxisValue(10);
        float scrollV = event.getAxisValue(9);
        if (Float.compare(scrollH, 0.0f) == 0 && Float.compare(scrollV, 0.0f) == 0) {
            return false;
        }
        float scroll = Float.compare(scrollH, 0.0f) == 0 ? -scrollV : scrollH;
        HwOnChangePageListener hwOnChangePageListener = this.mOnNextPageListener;
        if (hwOnChangePageListener != null && hwOnChangePageListener.onChangePage(scroll, event)) {
            return true;
        }
        HwOnChangeProgressListener hwOnChangeProgressListener = this.mOnChangeProgressListener;
        if (hwOnChangeProgressListener != null && hwOnChangeProgressListener.onChangeProgress(((int) (-scrollH)) * 1, ((int) scrollV) * 1, event)) {
            return true;
        }
        if (this.mOnScrollListener != null) {
            if (this.mOnScrollListener.onScrollBy((float) Math.round(this.mHorizontalScrollFactor * scrollH * this.mSensitivity), (float) Math.round((-scrollV) * this.mVerticalScrollFactor * this.mSensitivity), event)) {
                if (this.mMotionEventX < 0.0f || this.mMotionEventY < 0.0f) {
                    this.mMotionEventX = event.getX();
                    this.mMotionEventY = event.getY();
                }
                return true;
            }
        }
        return false;
    }

    public boolean interceptGenericMotionEvent(MotionEvent event) {
        if (this.mOnScrollListener == null) {
            return false;
        }
        if (Float.compare(this.mMotionEventX, INVALID_POSITION_VALUE) == 0 && Float.compare(this.mMotionEventY, INVALID_POSITION_VALUE) == 0) {
            return false;
        }
        if (!isBeingDragged(event)) {
            return onGenericMotionEvent(event);
        }
        this.mMotionEventX = INVALID_POSITION_VALUE;
        this.mMotionEventY = INVALID_POSITION_VALUE;
        return false;
    }

    private boolean isBeingDragged(MotionEvent event) {
        float positionX = event.getX();
        float positionY = event.getY();
        float f = this.mMotionEventX;
        if (f - 0.0f > positionX || positionX > f + 0.0f) {
            return true;
        }
        float f2 = this.mMotionEventY;
        if (f2 - 0.0f > positionY || positionY > f2 + 0.0f) {
            return true;
        }
        return false;
    }
}
