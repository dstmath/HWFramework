package com.huawei.server.fingerprint;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import com.huawei.utils.HwPartResourceUtils;

public class SuspensionButton extends RelativeLayout {
    private static final float COMPARE_DIFFERENCE = 100.0f;
    private static final float COMPARE_DIFFERENCE_NEGATIVE = -100.0f;
    private static final int INVALID_VALUE = -1;
    private static final String PKGNAME_OF_KEYGUARD = "com.android.systemui";
    private static final String TAG = "SuspensionButton";
    private boolean isClickable = true;
    private boolean isMovable = false;
    private float mEndX;
    private float mEndY;
    private InterfaceCallBack mHandleViewCallback;
    private int mHeight = -1;
    private float mStartX;
    private float mStartY;

    public interface InterfaceCallBack {
        String getCurrentApp();

        void onButtonClick();

        void onButtonViewMoved(float f, float f2);

        void onConfigurationChanged(Configuration configuration);

        void userActivity();
    }

    public SuspensionButton(Context context) {
        super(context);
    }

    public SuspensionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStatusBarHeight();
        Log.e(TAG, "SuspensionButton,created");
    }

    private void initStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getContext().getResources().getDimensionPixelSize(resourceId);
        }
        this.mHeight = statusBarHeight + getContext().getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("suspension_button_height"));
    }

    public void setCallback(InterfaceCallBack callback) {
        this.mHandleViewCallback = callback;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0075  */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        boolean isActionMove;
        int action = event.getActionMasked();
        InterfaceCallBack interfaceCallBack = this.mHandleViewCallback;
        if (interfaceCallBack == null) {
            Log.i(TAG, "no callback regesited or disabled");
            return false;
        }
        interfaceCallBack.userActivity();
        if (action == 0) {
            this.mStartX = event.getRawX();
            this.mStartY = event.getRawY();
            this.isClickable = true;
            this.isMovable = false;
        } else if (action == 1) {
            this.mEndX = event.getRawX();
            this.mEndY = event.getRawY();
            if (this.isClickable) {
                this.mHandleViewCallback.onButtonClick();
            }
            if (this.isMovable) {
                this.mHandleViewCallback.onButtonViewMoved(this.mEndX, this.mEndY);
            }
        } else if (action == 2) {
            this.mEndX = event.getRawX();
            this.mEndY = event.getRawY();
            this.isMovable = false;
            float f = this.mEndX;
            float f2 = this.mStartX;
            if (f - f2 <= COMPARE_DIFFERENCE && f - f2 >= COMPARE_DIFFERENCE_NEGATIVE) {
                float f3 = this.mEndY;
                float f4 = this.mStartY;
                if (f3 - f4 <= COMPARE_DIFFERENCE && f3 - f4 >= COMPARE_DIFFERENCE_NEGATIVE) {
                    isActionMove = false;
                    if (((float) this.mHeight) < this.mEndY && isActionMove) {
                        this.isClickable = false;
                        this.isMovable = true;
                    }
                    if ("com.android.systemui".equals(this.mHandleViewCallback.getCurrentApp())) {
                        this.isMovable = false;
                    }
                    if (this.isMovable) {
                        this.mHandleViewCallback.onButtonViewMoved(this.mEndX, this.mEndY);
                    }
                }
            }
            isActionMove = true;
            this.isClickable = false;
            this.isMovable = true;
            if ("com.android.systemui".equals(this.mHandleViewCallback.getCurrentApp())) {
            }
            if (this.isMovable) {
            }
        }
        return true;
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        InterfaceCallBack interfaceCallBack = this.mHandleViewCallback;
        if (interfaceCallBack == null) {
            Log.i(TAG, "no callback regesited");
        } else {
            interfaceCallBack.onConfigurationChanged(newConfig);
        }
    }
}
