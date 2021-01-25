package com.huawei.internal.widget;

import android.app.ActivityThread;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.policy.DecorView;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.widget.DecorCaptionView;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.os.HwVibrator;

public abstract class AbsHwMultiWindowCaptionView extends DecorCaptionView {
    protected static final int DIVIDE_BY_TWO = 2;
    protected static final float HALF_ADJUST = 0.5f;
    private static final boolean IS_SUPPORT_HW_VIBRATOR = HwVibrator.isSupportHwVibrator("haptic.control.widget_operation");
    protected static final int LONG_PRESS_TIME_IN_MS = ViewConfiguration.getLongPressTimeout();
    private static final int NOTIFY_FLOATWIN_TIP_THRESHOLD = SystemProperties.getInt("ro.config.float_win_threshold", 1);
    protected static final String PACKAGE_NAME_DOCK = "com.huawei.hwdockbar";
    protected static final int SHOWN_FLOATING_WIN_TIP = 256;
    protected static final int SHOWN_SPLIT_WIN_TIP_LEFT_RIGHT = 1;
    protected static final int SHOWN_SPLIT_WIN_TIP_UP_DOWN = 16;
    private static final String TAG = "AbsHwMultiWindowCaptionView";
    protected static final int TIPS_TOP_IN_CAPTION_DP = 28;
    protected static final int TOUCH_DISPATCH_LIST_CAPACITY = 3;
    protected static final float WIDTH_RATIO_OF_TIPS = 0.6666667f;
    protected Context mContext;
    protected View mDarkFrameView = null;
    protected DecorView mDecorView = null;
    protected Drawable mDragBarBackground;
    protected boolean mHasLongClicked = false;
    protected boolean mIsShow = false;
    protected PhoneWindow mOwner = null;
    protected int mWindowMode = 0;

    /* access modifiers changed from: protected */
    public abstract void getTouchOffsets(int[] iArr, float f, float f2);

    /* access modifiers changed from: protected */
    public abstract void resetDragBar();

    public AbsHwMultiWindowCaptionView(Context context) {
        super(context);
    }

    public AbsHwMultiWindowCaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsHwMultiWindowCaptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean onLongClick(View view, MotionEvent motionEvent) {
        PhoneWindow phoneWindow = this.mOwner;
        if (phoneWindow == null) {
            Log.e(TAG, "PhoneWindow is null");
            return false;
        } else if (view == null) {
            Log.w(TAG, "onLongClick failed, cause drag bar is null!");
            return false;
        } else {
            IBinder activityToken = phoneWindow.getContext().getActivityToken();
            if (HwCaptionViewUtils.isInSubFoldDisplayMode(getContext()) || HwCaptionViewUtils.isInLazyMode(getContext()) || !HwActivityTaskManager.isSupportDragForMultiWin(activityToken)) {
                Log.d(TAG, "do not drag in lazy mode or sub display mode");
                return true;
            }
            HwActivityTaskManager.handleMultiWindowSwitch(activityToken, prepareToHandleMultiWindowSwitch(view, motionEvent));
            doVibrate(view);
            return true;
        }
    }

    private Bundle prepareToHandleMultiWindowSwitch(View dragBar, MotionEvent motionEvent) {
        Bundle info = new Bundle();
        float scale = HwActivityTaskManager.getStackScale(-100);
        Bundle bundle = HwActivityTaskManager.getHwMultiWindowState();
        float globalScale = 1.0f;
        if (bundle != null) {
            globalScale = bundle.getFloat("globleScale", 1.0f);
        }
        int[] location = new int[2];
        getLocationOnScreen(location);
        Point touchRawPoint = new Point(location[0] + ((int) ((motionEvent.getRawX() - ((float) location[0])) * scale * globalScale)), location[1] + ((int) ((motionEvent.getRawY() - ((float) location[1])) * scale * globalScale)));
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "prepareToHandleMultiWindowSwitch touch point on screen: " + touchRawPoint);
        }
        info.putParcelable(ConstantValues.DRAG_TOUCH_POINT_KEY, touchRawPoint);
        int[] touchOffsets = new int[2];
        int touchX = (int) motionEvent.getX();
        int touchY = (int) motionEvent.getY();
        getTouchOffsets(touchOffsets, (float) touchX, (float) touchY);
        int touchOffsetX = (int) (((float) touchOffsets[0]) * globalScale);
        int touchOffsetY = (int) (((float) touchOffsets[1]) * globalScale);
        Log.d(TAG, "prepareToHandleMultiWindowSwitch touchX = " + touchX + ", touchY = " + touchY + ", touchOffsetX = " + touchOffsetX + ", touchOffsetY = " + touchOffsetY + ", width = " + getWidth() + ", height = " + getHeight());
        info.putParcelable(ConstantValues.DRAG_TOUCH_OFFSETS_KEY, new Point(touchOffsetX, touchOffsetY));
        info.putParcelable(ConstantValues.DRAG_BAR_DRAW_OFFSETS_KEY, new Point(dragBar.getLeft(), dragBar.getTop()));
        Bitmap dragBarBmp = Bitmap.createBitmap(dragBar.getWidth(), dragBar.getHeight(), Bitmap.Config.ARGB_8888);
        dragBar.draw(new Canvas(dragBarBmp));
        info.putParcelable(ConstantValues.DRAG_BAR_BMP_KEY, dragBarBmp);
        info.putFloat("globleScale", globalScale);
        return info;
    }

    private void doVibrate(View dragBar) {
        if (Settings.System.getInt(this.mContext.getContentResolver(), "haptic_feedback_enabled", 1) != 0) {
            if (IS_SUPPORT_HW_VIBRATOR) {
                try {
                    HwVibrator.setHwVibrator(Process.myUid(), ActivityThread.currentPackageName(), "haptic.control.widget_operation");
                } catch (SecurityException excep) {
                    Log.e(TAG, "Get exception when vibrate: " + excep.getMessage());
                }
            } else {
                dragBar.performHapticFeedback(0, 2);
            }
        }
    }

    protected static int dip2px(Context context, float dipValue) {
        return (int) ((dipValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    /* access modifiers changed from: protected */
    public int getWindowMode() {
        DecorView decorView;
        if ((this.mOwner.getDecorView() instanceof DecorView) && (decorView = this.mOwner.getDecorView()) != null) {
            return decorView.getWindowMode();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public void saveTipState(int state, Context context) {
        HwActivityTaskManager.saveMultiWindowTipState("dock_tip_notify_type", Settings.Secure.getInt(context.getContentResolver(), "dock_tip_notify_type", 0) | state);
    }

    /* access modifiers changed from: protected */
    public void handleTipsLayoutDirection(Context context) {
        boolean isRtlSupport = context.getApplicationInfo().hasRtlSupport();
        int layoutDirection = getResources().getConfiguration().getLayoutDirection();
        LinearLayout tipBottom = (LinearLayout) findViewById(34603421);
        if (tipBottom != null && !isRtlSupport && layoutDirection == 1) {
            tipBottom.setGravity(8388611);
            Log.i(TAG, "do not support RTL, set tipButton to RTL");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isNeedShowTips(Context context, boolean isUpDownSplit) {
        ContentResolver resolver = context.getContentResolver();
        int showTipsState = Settings.Secure.getInt(resolver, "dock_tip_notify_type", 0);
        if (this instanceof HwFreeFormCaptionView) {
            if (Settings.Secure.getInt(resolver, "start_floatwin_boots_count", 0) < NOTIFY_FLOATWIN_TIP_THRESHOLD || (showTipsState & 256) != 0) {
                return false;
            }
            return true;
        } else if (isUpDownSplit) {
            if ((showTipsState & 16) == 0) {
                return true;
            }
            return false;
        } else if ((showTipsState & 1) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void removeContentView() {
        AbsHwMultiWindowCaptionView.super.removeContentView();
        removeFrameBorder();
    }

    /* access modifiers changed from: protected */
    public void adjustFrameBorder() {
        if (!((this.mContext.getResources().getConfiguration().uiMode & 32) != 0) || !this.mIsShow) {
            hideFrameBorder();
        } else {
            showFrameBorder();
        }
    }

    /* access modifiers changed from: protected */
    public void applyTheme(View dragBar) {
        if (dragBar instanceof ImageView) {
            Drawable dragBarDrawable = ((ImageView) dragBar).getDrawable();
            if (dragBarDrawable == null) {
                Log.w(TAG, "drag bar drawable null.");
            } else {
                dragBarDrawable.applyTheme(new ContextThemeWrapper(getContext(), 33947785).getTheme());
            }
        }
    }

    private void showFrameBorder() {
        float frameRadius;
        View view = this.mDarkFrameView;
        if (view != null) {
            view.setVisibility(0);
            return;
        }
        int width = dip2px(this.mContext, 1.0f);
        int color = getContext().getColor(33882975);
        if (this instanceof HwSplitScreenCaptionView) {
            frameRadius = HwActivityManager.IS_PHONE ? 0.0f : this.mDecorView.getResources().getDimension(ConstantValues.SPLITSCREEN_RADIUS) - 1.0f;
        } else {
            frameRadius = this.mDecorView.getResources().getDimension(ConstantValues.FREEFORM_RADIUS) - 1.0f;
        }
        this.mDarkFrameView = new View(this.mDecorView.getContext());
        this.mDarkFrameView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.mDarkFrameView.setBackground(createDrawable(width, color, frameRadius));
        this.mDecorView.addView(this.mDarkFrameView);
    }

    private void hideFrameBorder() {
        View view = this.mDarkFrameView;
        if (view != null) {
            view.setVisibility(8);
        }
    }

    /* access modifiers changed from: protected */
    public void removeFrameBorder() {
        View view = this.mDarkFrameView;
        if (view != null) {
            this.mDecorView.removeView(view);
            this.mDarkFrameView = null;
        }
    }

    private GradientDrawable createDrawable(int width, int color, float radius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setStroke(width, color);
        gradientDrawable.setCornerRadius(radius);
        return gradientDrawable;
    }
}
