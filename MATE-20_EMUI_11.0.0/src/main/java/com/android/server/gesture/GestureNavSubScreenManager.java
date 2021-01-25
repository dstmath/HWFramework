package com.android.server.gesture;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.server.gesture.GestureNavView;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.hiai.awareness.AwarenessConstants;

public class GestureNavSubScreenManager implements GestureNavView.IGestureEventProxy {
    private static final String TAG = "GestureNavSubScreenManager";
    private static final String TAG_SUB_SCREEN_GESTURE_NAV_BOTTOM = "GestureNavSubScreenBottom";
    private static final String TAG_SUB_SCREEN_GESTURE_NAV_LEFT = "GestureNavSubScreenLeft";
    private static final String TAG_SUB_SCREEN_GESTURE_NAV_RIGHT = "GestureNavSubScreenRight";
    private Context mContext;
    private final float mDelta = 0.5f;
    private GestureNavView.IGestureEventProxy mGestureEventProxy;
    private boolean mIsSubScreenGestureNavReady;
    private boolean mIsSubScreenNavBottomEnabled = false;
    private boolean mIsSubScreenNavLeftBackEnabled = false;
    private boolean mIsSubScreenNavRightBackEnabled = false;
    private final Rect mMainFoldDisplayModeRect = HwFoldScreenState.getScreenPhysicalRect(2);
    private final int mMainScreenWidth = HwFoldScreenState.getScreenPhysicalRect(2).width();
    private final int mPhoneHeight = HwFoldScreenState.getScreenPhysicalRect(1).height();
    private final int mPhoneWidth = HwFoldScreenState.getScreenPhysicalRect(3).width();
    private final float mScaleRatio = ((((float) (this.mSubFoldDisplayModeRect.right - this.mSubFoldDisplayModeRect.left)) * 1.0f) / ((float) (this.mMainFoldDisplayModeRect.right - this.mMainFoldDisplayModeRect.left)));
    private final Rect mSubFoldDisplayModeRect = HwFoldScreenState.getScreenPhysicalRect(3);
    private GestureNavView mSubScreenGestureNavBottom;
    private GestureNavView mSubScreenGestureNavLeft;
    private GestureNavView mSubScreenGestureNavRight;
    private int mSubScreenNavBackWindowWidth;
    private int mSubScreenNavBottomWindowHeight;
    private Rect mSubScreenViewEntryRect = new Rect();
    private final float mTempValue = ((((float) this.mViewWidth) * 1.0f) / (((float) this.mMainScreenWidth) * 1.0f));
    private final int mViewHeight = ((int) ((1.0f - this.mTempValue) * ((float) this.mPhoneHeight)));
    private final int mViewWidth = this.mPhoneWidth;
    private WindowManager mWindowManager;
    private int mWindowWidth = 0;

    public GestureNavSubScreenManager(Context context, int rotation, boolean isLeftEnable, boolean isRightEnable, boolean isBottomEnable) {
        Log.d(TAG, TAG);
        this.mContext = context;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mSubScreenNavBackWindowWidth = Math.round(((float) GestureNavConst.getBackWindowWidth(this.mContext)) * this.mScaleRatio);
        this.mSubScreenNavBottomWindowHeight = Math.round(((float) GestureNavConst.getBottomWindowHeight(this.mContext)) * this.mScaleRatio);
        createSubScreenGestureNavStateLocked(rotation, isLeftEnable, isRightEnable, isBottomEnable);
    }

    private void initSubScreenViewEntryConfig(int rotation) {
        Log.d(TAG, "initSubScreenViewEntryConfig:rotation=" + rotation);
        int startX = 0;
        int startY = 0;
        int width = 0;
        int height = 0;
        if (rotation == 0) {
            startX = 0;
            startY = this.mPhoneHeight - this.mViewHeight;
            width = this.mViewWidth;
            height = this.mViewHeight;
        } else if (rotation == 1) {
            startX = this.mPhoneHeight - this.mViewHeight;
            startY = this.mMainScreenWidth - this.mViewWidth;
            width = this.mViewHeight;
            height = this.mViewWidth;
        } else if (rotation == 2) {
            startX = this.mMainScreenWidth - this.mViewWidth;
            startY = 0;
            width = this.mViewWidth;
            height = this.mViewHeight;
        } else if (rotation == 3) {
            startX = 0;
            startY = 0;
            width = this.mViewHeight;
            height = this.mViewWidth;
        }
        this.mSubScreenViewEntryRect.set(startX, startY, startX + width, startY + height);
        if (GestureNavConst.DEBUG) {
            Log.d(TAG, "mSubScreenViewEntryRect:X=" + startX + ", Y=" + startY + ", W=" + width + ", H=" + height);
        }
    }

    private void createSubScreenGestureNavStateLocked(int rotation, boolean isLeftEnable, boolean isRightEnable, boolean isBottomEnable) {
        Log.d(TAG, "createSubScreenGestureNavStateLocked.");
        initSubScreenViewEntryConfig(rotation);
        updateSubScreenConfigLocked(rotation);
        updateSubScreenNavWindowLocked(rotation);
        updateSubScreenNavVisibleLocked(isLeftEnable, isRightEnable, isBottomEnable, rotation, false);
    }

    private void updateSubScreenNavWindowLocked(int rotation) {
        Log.d(TAG, "updateSubScreenNavWindowLocked.");
        if (!this.mIsSubScreenGestureNavReady) {
            createSubScreenNavWindows(rotation);
        } else {
            updateSubScreenNavWindows();
        }
    }

    public void createSubScreenNavWindows(int rotation) {
        Log.d(TAG, "createSubScreenNavWindows.");
        this.mSubScreenGestureNavLeft = new GestureNavView(this.mContext, 11);
        this.mSubScreenGestureNavRight = new GestureNavView(this.mContext, 12);
        this.mSubScreenGestureNavBottom = new GestureNavView(this.mContext, 13);
        this.mIsSubScreenGestureNavReady = true;
        Log.i(TAG, "gesture nav ready.");
        updateSubScreenConfigLocked(rotation);
        configAndAddNavWindow(TAG_SUB_SCREEN_GESTURE_NAV_LEFT, this.mSubScreenGestureNavLeft);
        configAndAddNavWindow(TAG_SUB_SCREEN_GESTURE_NAV_RIGHT, this.mSubScreenGestureNavRight);
        configAndAddNavWindow(TAG_SUB_SCREEN_GESTURE_NAV_BOTTOM, this.mSubScreenGestureNavBottom);
        this.mIsSubScreenNavLeftBackEnabled = true;
        this.mIsSubScreenNavRightBackEnabled = true;
        this.mIsSubScreenNavBottomEnabled = true;
    }

    public void updateSubScreenNavWindows() {
        Log.d(TAG, "updateSubScreenNavWindows.");
        reLayoutNavWindow(TAG_SUB_SCREEN_GESTURE_NAV_LEFT, this.mSubScreenGestureNavLeft);
        reLayoutNavWindow(TAG_SUB_SCREEN_GESTURE_NAV_RIGHT, this.mSubScreenGestureNavRight);
        reLayoutNavWindow(TAG_SUB_SCREEN_GESTURE_NAV_BOTTOM, this.mSubScreenGestureNavBottom);
    }

    public void destroySubScreenNavWindows() {
        Log.d(TAG, "destroySubScreenNavWindows.");
        this.mIsSubScreenGestureNavReady = false;
        this.mIsSubScreenNavLeftBackEnabled = false;
        this.mIsSubScreenNavRightBackEnabled = false;
        this.mIsSubScreenNavBottomEnabled = false;
        GestureUtils.removeWindowView(this.mWindowManager, this.mSubScreenGestureNavLeft, true);
        GestureUtils.removeWindowView(this.mWindowManager, this.mSubScreenGestureNavRight, true);
        GestureUtils.removeWindowView(this.mWindowManager, this.mSubScreenGestureNavBottom, true);
        this.mSubScreenGestureNavLeft = null;
        this.mSubScreenGestureNavRight = null;
        this.mSubScreenGestureNavBottom = null;
    }

    private void updateSubScreenConfigLocked(int rotation) {
        int backWindowHeight;
        int deltaOffset;
        int displayHeight;
        int displayWidth;
        if (this.mIsSubScreenGestureNavReady) {
            int startX = this.mSubScreenViewEntryRect.left;
            int startY = this.mSubScreenViewEntryRect.top;
            int viewHeight = this.mSubScreenViewEntryRect.bottom - this.mSubScreenViewEntryRect.top;
            int viewWidth = this.mSubScreenViewEntryRect.right - this.mSubScreenViewEntryRect.left;
            int backWindowWidth = this.mSubScreenNavBackWindowWidth;
            int bottomWindowHeight = this.mSubScreenNavBottomWindowHeight;
            if (rotation == 1 || rotation == 3) {
                backWindowHeight = viewHeight;
                deltaOffset = viewHeight - viewHeight;
                displayHeight = this.mSubFoldDisplayModeRect.right - this.mSubFoldDisplayModeRect.left;
                displayWidth = this.mSubFoldDisplayModeRect.bottom - this.mSubFoldDisplayModeRect.top;
            } else {
                backWindowHeight = viewHeight;
                deltaOffset = 0;
                displayWidth = this.mSubFoldDisplayModeRect.right - this.mSubFoldDisplayModeRect.left;
                displayHeight = this.mSubFoldDisplayModeRect.bottom - this.mSubFoldDisplayModeRect.top;
            }
            this.mSubScreenGestureNavLeft.updateViewConfig(displayWidth, displayHeight, startX, startY + deltaOffset, backWindowWidth, backWindowHeight, startX, startY + deltaOffset, 0, 0);
            this.mSubScreenGestureNavRight.updateViewConfig(displayWidth, displayHeight, startX + (viewWidth - backWindowWidth), startY + deltaOffset, backWindowWidth, backWindowHeight, startX + (viewWidth - backWindowWidth), startY + deltaOffset, 0, 0);
            this.mSubScreenGestureNavBottom.updateViewConfig(displayWidth, displayHeight, startX, startY + (viewHeight - bottomWindowHeight), viewWidth, bottomWindowHeight, startX, startY + (viewHeight - bottomWindowHeight), 0, 0);
            if (GestureNavConst.DEBUG) {
                Log.d(TAG, "updateSubScreenConfigLocked: Left." + this.mSubScreenGestureNavLeft.getViewConfig().toString());
                Log.d(TAG, "updateSubScreenConfigLocked: Right." + this.mSubScreenGestureNavRight.getViewConfig().toString());
                Log.d(TAG, "updateSubScreenConfigLocked: bottom." + this.mSubScreenGestureNavBottom.getViewConfig().toString());
            }
        }
    }

    private void configAndAddNavWindow(String title, GestureNavView view) {
        WindowManager.LayoutParams params = createSubScreenLayoutParams(title, view.getViewConfig());
        view.setGestureEventProxy(this);
        GestureUtils.addWindowView(this.mWindowManager, view, params);
    }

    private void reLayoutNavWindow(String title, GestureNavView view) {
        GestureUtils.updateViewLayout(this.mWindowManager, view, createSubScreenLayoutParams(title, view.getViewConfig()));
    }

    private WindowManager.LayoutParams createSubScreenLayoutParams(String title, GestureNavView.WindowConfig config) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManagerEx.LayoutParamsEx.getTypeNavigationBarPanel(), 296);
        if (ActivityManagerEx.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.flags |= 512;
        lp.format = -2;
        lp.alpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        lp.gravity = 51;
        lp.x = config.startX;
        lp.y = config.startY;
        lp.width = config.width;
        lp.height = config.height;
        lp.windowAnimations = 0;
        lp.softInputMode = 49;
        lp.setTitle(title);
        WindowManagerEx.LayoutParamsEx paramsEx = new WindowManagerEx.LayoutParamsEx(lp);
        if (config.usingNotch) {
            paramsEx.addHwFlags((int) AwarenessConstants.MSDP_ENVIRONMENT_TYPE_HOME);
        } else {
            paramsEx.clearHwFlags((int) AwarenessConstants.MSDP_ENVIRONMENT_TYPE_HOME);
        }
        paramsEx.addHwFlags(16777216);
        paramsEx.addHwFlags(2097152);
        return lp;
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureEventProxy
    public boolean onTouchEvent(GestureNavView view, MotionEvent event) {
        GestureNavView.IGestureEventProxy iGestureEventProxy = this.mGestureEventProxy;
        if (iGestureEventProxy == null) {
            return true;
        }
        iGestureEventProxy.onTouchEvent(view, event);
        return true;
    }

    public void setGestureEventProxy(GestureNavView.IGestureEventProxy proxy) {
        this.mGestureEventProxy = proxy;
    }

    public void handleRotationChangedSubScreen(int rotation) {
        Log.d(TAG, "handleRotationChangedSubScreen. rotation =" + rotation);
        initSubScreenViewEntryConfig(rotation);
        updateSubScreenConfigLocked(rotation);
        updateSubScreenNavWindowLocked(rotation);
    }

    public void handleConfigChangedSubScreen(int rotation) {
        Log.d(TAG, "handleConfigChangedSubScreen. rotation =" + rotation);
        if (this.mWindowWidth != this.mWindowManager.getDefaultDisplay().getWidth()) {
            this.mWindowWidth = this.mWindowManager.getDefaultDisplay().getWidth();
            initSubScreenViewEntryConfig(rotation);
            updateSubScreenConfigLocked(rotation);
            updateSubScreenNavWindowLocked(rotation);
        }
    }

    public void updateSubScreenNavVisibleLocked(boolean isLeftEnable, boolean isRightEnable, boolean isBottomEnable, int rotation, boolean isDelay) {
        Log.d(TAG, "updateSubScreenNavVisibleLocked, setVisibility=" + isLeftEnable + ", " + isRightEnable + ", " + isBottomEnable + ", rotation = " + rotation);
        if (rotation == 0) {
            updateSubScreenNavVisibleLocked(isLeftEnable, isRightEnable, isBottomEnable, isDelay);
        } else if (rotation == 1) {
            updateSubScreenNavVisibleLocked(false, isRightEnable, isBottomEnable, isDelay);
        } else if (rotation != 3) {
            updateSubScreenNavVisibleLocked(false, false, false, isDelay);
        } else {
            updateSubScreenNavVisibleLocked(isLeftEnable, false, isBottomEnable, isDelay);
        }
    }

    private void updateSubScreenNavVisibleLocked(boolean isLeftEnable, boolean isRightEnable, boolean isBottomEnable, boolean isDelay) {
        Log.d(TAG, "updateSubScreenNavVisibleLocked, setVisibility=" + isLeftEnable + ", " + isRightEnable + ", " + isBottomEnable);
        if (this.mIsSubScreenNavLeftBackEnabled != isLeftEnable) {
            this.mSubScreenGestureNavLeft.show(isLeftEnable, isDelay);
            this.mIsSubScreenNavLeftBackEnabled = isLeftEnable;
        }
        if (this.mIsSubScreenNavRightBackEnabled != isRightEnable) {
            this.mSubScreenGestureNavRight.show(isRightEnable, isDelay);
            this.mIsSubScreenNavRightBackEnabled = isRightEnable;
        }
        if (this.mIsSubScreenNavBottomEnabled != isBottomEnable) {
            this.mSubScreenGestureNavBottom.show(isBottomEnable, isDelay);
            this.mIsSubScreenNavBottomEnabled = isBottomEnable;
        }
    }

    public void bringSubScreenNavViewToTop() {
        Log.d(TAG, "bringNavExtViewToTop ");
        GestureNavView gestureNavView = this.mSubScreenGestureNavLeft;
        if (gestureNavView != null) {
            this.mWindowManager.removeView(gestureNavView);
            this.mSubScreenGestureNavLeft.setZOrderOnTop(true);
            WindowManager windowManager = this.mWindowManager;
            GestureNavView gestureNavView2 = this.mSubScreenGestureNavLeft;
            windowManager.addView(gestureNavView2, gestureNavView2.getLayoutParams());
        }
        GestureNavView gestureNavView3 = this.mSubScreenGestureNavRight;
        if (gestureNavView3 != null) {
            this.mWindowManager.removeView(gestureNavView3);
            this.mSubScreenGestureNavRight.setZOrderOnTop(true);
            WindowManager windowManager2 = this.mWindowManager;
            GestureNavView gestureNavView4 = this.mSubScreenGestureNavRight;
            windowManager2.addView(gestureNavView4, gestureNavView4.getLayoutParams());
        }
        GestureNavView gestureNavView5 = this.mSubScreenGestureNavBottom;
        if (gestureNavView5 != null) {
            this.mWindowManager.removeView(gestureNavView5);
            this.mSubScreenGestureNavBottom.setZOrderOnTop(true);
            WindowManager windowManager3 = this.mWindowManager;
            GestureNavView gestureNavView6 = this.mSubScreenGestureNavBottom;
            windowManager3.addView(gestureNavView6, gestureNavView6.getLayoutParams());
        }
    }
}
