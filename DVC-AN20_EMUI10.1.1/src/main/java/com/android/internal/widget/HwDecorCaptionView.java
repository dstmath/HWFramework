package com.android.internal.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.HwPCMultiWindowCompatibility;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.pc.AbsHwDecorCaptionView;
import android.pc.IHwPCManager;
import android.util.AttributeSet;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.IntentExEx;
import com.huawei.android.hardware.input.InputManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwpartpoweroffice.BuildConfig;
import com.huawei.internal.policy.PhoneWindowEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.ArrayList;

public class HwDecorCaptionView extends AbsHwDecorCaptionView implements View.OnClickListener {
    private static final String BOARD_PAD_MRX = "MRX";
    private static final String BOARD_PAD_MXX = "MXX";
    private static final float CAPTION_HEIGHT_ADAPT_PUNCH = 32.0f;
    private static final float MARGIN_MOVE_DISTANCE = 32.0f;
    private static final int MARGIN_RESTORE_DISTANCE = 0;
    private static final String MNOTCHPROP = SystemPropertiesEx.get("ro.config.hw_notch_size", BuildConfig.FLAVOR);
    private static final String TAG = "DecorCaptionView";
    private static final long mDoubleClickInterval = 500;
    private View mBack;
    private final Rect mBackRect = new Rect();
    private View mCaption;
    private RelativeLayout mCaptionLayout;
    private boolean mCheckForDragging;
    private View mClickTarget;
    private View mClose;
    private final Rect mCloseRect = new Rect();
    private View mContent;
    private Context mContext;
    private int mCurUIMode;
    private int mDragSlop;
    private boolean mDragging = false;
    private View mFullScreen;
    private final Rect mFullScreenRect = new Rect();
    private boolean mIsHiCarCastMode = false;
    private long mLastClickTime = 0;
    private View mMaximize;
    private final Rect mMaximizeRect = new Rect();
    private View mMinimize;
    private final Rect mMinimizeRect = new Rect();
    private Handler mMyHandler = new Handler();
    private boolean mOverlayWithAppContent = false;
    private PhoneWindowEx mOwner = null;
    private boolean mShow = false;
    private TextView mTitleView;
    private ArrayList<View> mTouchDispatchList = new ArrayList<>(2);
    private int mTouchDownX;
    private int mTouchDownY;
    private boolean mUseRtlRes;
    private int mWindowState;

    public HwDecorCaptionView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mContext = context;
        this.mCurUIMode = this.mContext.getResources().getConfiguration().uiMode & 48;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        HwDecorCaptionView.super.onFinishInflate();
        this.mCaption = getCaptionView().getChildAt(MARGIN_RESTORE_DISTANCE);
        this.mIsHiCarCastMode = HwPCUtils.isHiCarCastModeForClient();
        if (this.mIsHiCarCastMode) {
            this.mCaption.setVisibility(8);
        }
        boolean isRtlSupport = ContextEx.hasRtlSupport(this.mContext);
        int layoutDirection = this.mContext.getResources().getConfiguration().getLayoutDirection();
        if (!isRtlSupport && layoutDirection == 1) {
            this.mUseRtlRes = true;
        }
        this.mCaptionLayout = (RelativeLayout) getCaptionView().findViewById(HwPartResourceUtils.getResourceId(this.mUseRtlRes ? "hw_caption_rtl" : "hw_caption"));
        this.mMaximize = getCaptionView().findViewById(this.mUseRtlRes ? 34603210 : 34603185);
        this.mClose = getCaptionView().findViewById(this.mUseRtlRes ? 34603212 : 34603187);
        this.mMinimize = getCaptionView().findViewById(this.mUseRtlRes ? 34603211 : 34603186);
        this.mBack = getCaptionView().findViewById(this.mUseRtlRes ? 34603213 : 34603188);
        this.mTitleView = (TextView) getCaptionView().findViewById(this.mUseRtlRes ? 34603209 : 34603184);
        this.mFullScreen = getCaptionView().findViewById(this.mUseRtlRes ? 34603214 : 34603208);
        this.mMaximize.setOnClickListener(this);
        this.mClose.setOnClickListener(this);
        this.mMinimize.setOnClickListener(this);
        this.mBack.setOnClickListener(this);
        this.mFullScreen.setOnClickListener(this);
        if (isMaximized()) {
            this.mMaximize.setContentDescription(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("restore_current_window")));
        } else {
            this.mMaximize.setContentDescription(this.mContext.getResources().getString(33686039));
        }
        if (!isFullscreen()) {
            this.mFullScreen.setContentDescription(this.mContext.getResources().getString(33686056));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanAllViews() {
        this.mMinimize.setPressed(false);
        this.mMinimize.setHovered(false);
        this.mMaximize.setPressed(false);
        this.mMaximize.setHovered(false);
        this.mClose.setPressed(false);
        this.mClose.setHovered(false);
        this.mFullScreen.setPressed(false);
        this.mFullScreen.setHovered(false);
    }

    public void setPhoneWindow(PhoneWindowEx owner, boolean show) {
        this.mOwner = owner;
        boolean isBottomRight = true;
        this.mShow = (this.mIsHiCarCastMode || !show) ? MARGIN_RESTORE_DISTANCE : true;
        this.mOverlayWithAppContent = owner.isOverlayWithDecorCaptionEnabled();
        if (this.mOverlayWithAppContent) {
            this.mCaption.setBackgroundColor(MARGIN_RESTORE_DISTANCE);
        }
        updateCaptionVisibility();
        this.mOwner.getDecorView().setOutlineProvider(ViewOutlineProvider.BOUNDS);
        try {
            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
            if (pcManager != null) {
                onWindowStateChanged(pcManager.getWindowState(this.mOwner.getAppToken()));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot get window state.");
        }
        WindowManager.LayoutParams attributes = this.mOwner.getAttributes();
        Context context = this.mOwner.getContext();
        if (context != null && HwPCUtils.enabledInPad() && HwPCUtils.isValidExtDisplayId(context)) {
            String packageName = context.getPackageName();
            if (attributes == null) {
                return;
            }
            if ("com.huawei.contacts".equals(packageName) || "com.android.contacts".equals(packageName)) {
                if (attributes.gravity != 85) {
                    isBottomRight = MARGIN_RESTORE_DISTANCE;
                }
                if (isBottomRight) {
                    this.mShow = false;
                    updateCaptionVisibility();
                }
            }
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            this.mClickTarget = calTouchedView(ev);
        }
        return this.mClickTarget != null;
    }

    private View calTouchedView(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        if (this.mMaximizeRect.contains(x, y) && this.mMaximize.getVisibility() == 0) {
            return this.mMaximize;
        }
        if (this.mCloseRect.contains(x, y) && this.mClose.getVisibility() == 0) {
            return this.mClose;
        }
        if (this.mMinimizeRect.contains(x, y) && this.mMinimize.getVisibility() == 0) {
            return this.mMinimize;
        }
        if (this.mBackRect.contains(x, y) && this.mBack.getVisibility() == 0) {
            return this.mBack;
        }
        if (!this.mFullScreenRect.contains(x, y) || this.mFullScreen.getVisibility() != 0) {
            return null;
        }
        return this.mFullScreen;
    }

    public void onClick(View view) {
        clickView(view);
    }

    private void clickView(View view) {
        if (view == this.mMaximize) {
            maximizeWindow();
        } else if (view == this.mFullScreen) {
            fullscreenWindow();
        } else if (view == this.mClose) {
            if (!HwPCUtils.enabledInPad() || !"com.android.incallui".equals(this.mContext.getPackageName()) || !HwPCUtils.isPcCastMode()) {
                Context context = this.mContext;
                HwPCUtils.bdReport(context, 10016, "Exit app:" + this.mContext.getPackageName());
                this.mOwner.dispatchOnWindowDismissed(true, false);
                return;
            }
            minimizeWindow();
            this.mMyHandler.postDelayed(new Runnable() {
                /* class com.android.internal.widget.HwDecorCaptionView.AnonymousClass1 */

                public void run() {
                    HwDecorCaptionView.this.cleanAllViews();
                }
            }, 300);
        } else if (view == this.mMinimize) {
            minimizeWindow();
            this.mMyHandler.postDelayed(new Runnable() {
                /* class com.android.internal.widget.HwDecorCaptionView.AnonymousClass2 */

                public void run() {
                    HwDecorCaptionView.this.cleanAllViews();
                }
            }, 300);
        } else if (view == this.mBack) {
            backWindow();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        View view;
        boolean z = false;
        if (this.mClickTarget == null) {
            return false;
        }
        int action = event.getAction();
        View view2 = this.mClickTarget;
        if (action == 0 || action == 2) {
            z = true;
        }
        view2.setPressed(z);
        if (action == 1 && this.mClickTarget == (view = calTouchedView(event))) {
            clickView(view);
        }
        if (action == 1 || action == 3) {
            this.mClickTarget = null;
        }
        return true;
    }

    public boolean onTouch(View v, MotionEvent e) {
        int i;
        int x = (int) e.getX();
        int y = (int) e.getY();
        boolean fromMouse = e.getToolType(e.getActionIndex()) == 3 ? true : MARGIN_RESTORE_DISTANCE;
        boolean primaryButton = (e.getButtonState() & 1) != 0 ? true : MARGIN_RESTORE_DISTANCE;
        int actionMasked = e.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked != 2) {
                    if (actionMasked != 3) {
                        HwPCUtils.log(TAG, "touch event not found!");
                        i = 1;
                    }
                } else if (this.mDragging || !this.mCheckForDragging) {
                    i = 1;
                } else if (fromMouse || passedSlop(x, y)) {
                    this.mCheckForDragging = false;
                    this.mDragging = true;
                    Point offset = this.mOwner.getViewDecorViewOffset();
                    int offsetX = offset == null ? MARGIN_RESTORE_DISTANCE : offset.x;
                    int offsetY = offset == null ? MARGIN_RESTORE_DISTANCE : offset.y;
                    if (isMaximized() || isLayoutSplit()) {
                        try {
                            int taskId = ActivityManagerEx.getTaskForActivity(this.mOwner.getAppToken(), false);
                            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
                            if (pcManager != null) {
                                pcManager.hwRestoreTask(taskId, e.getRawX() + ((float) offsetX), e.getRawY() + ((float) offsetY));
                            }
                        } catch (RemoteException e2) {
                            Log.e(TAG, "Cannot change task workspace.");
                        }
                    }
                    startMovingTask(e.getRawX() + ((float) offsetX), e.getRawY() + ((float) offsetY));
                    i = 1;
                } else {
                    i = 1;
                }
            }
            if (!this.mDragging) {
                i = 1;
            } else {
                if (actionMasked == 1) {
                    finishMovingTask();
                }
                this.mDragging = false;
                return !this.mCheckForDragging;
            }
        } else if (!this.mShow) {
            return false;
        } else {
            if (!fromMouse || primaryButton) {
                i = 1;
                this.mCheckForDragging = true;
                this.mTouchDownX = x;
                this.mTouchDownY = y;
            } else {
                i = 1;
            }
        }
        if (!this.mDragging && e.getAction() == i) {
            long time = e.getDownTime();
            long j = this.mLastClickTime;
            if (j == 0 || time - j >= mDoubleClickInterval) {
                this.mLastClickTime = time;
            } else {
                this.mLastClickTime = 0;
                maximizeWindow();
            }
        }
        return this.mDragging || this.mCheckForDragging;
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        this.mTouchDispatchList.ensureCapacity(3);
        View view = this.mCaption;
        if (view != null) {
            this.mTouchDispatchList.add(view);
        }
        View view2 = this.mContent;
        if (view2 != null) {
            this.mTouchDispatchList.add(view2);
        }
        return this.mTouchDispatchList;
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    private boolean passedSlop(int x, int y) {
        return Math.abs(x - this.mTouchDownX) > this.mDragSlop || Math.abs(y - this.mTouchDownY) > this.mDragSlop;
    }

    public void onConfigurationChanged(boolean show) {
        Log.i(TAG, "enter the onConfigurationChanged, show = " + show);
        this.mShow = !this.mIsHiCarCastMode && show;
        updateCaptionVisibility();
        if (isDarkMode() && this.mCurUIMode != 32) {
            updateDarkShade();
            this.mCurUIMode = 32;
        } else if (!isDarkMode() && this.mCurUIMode != 16) {
            updateShade(true);
            this.mCurUIMode = 16;
        }
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(params instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException("params " + params + " must subclass MarginLayoutParams");
        } else if (index >= 2 || getCaptionView().getChildCount() >= 2) {
            throw new IllegalStateException("DecorCaptionView can only handle 1 client view");
        } else {
            HwDecorCaptionView.super.addView(child, (int) MARGIN_RESTORE_DISTANCE, params);
            this.mContent = child;
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8) {
            measureChildWithMargins(this.mCaption, widthMeasureSpec, MARGIN_RESTORE_DISTANCE, heightMeasureSpec, MARGIN_RESTORE_DISTANCE);
            captionHeight = this.mCaption.getMeasuredHeight();
        } else {
            captionHeight = MARGIN_RESTORE_DISTANCE;
        }
        View view = this.mContent;
        if (view != null) {
            if (this.mOverlayWithAppContent) {
                measureChildWithMargins(view, widthMeasureSpec, MARGIN_RESTORE_DISTANCE, heightMeasureSpec, MARGIN_RESTORE_DISTANCE);
            } else {
                measureChildWithMargins(view, widthMeasureSpec, MARGIN_RESTORE_DISTANCE, heightMeasureSpec, captionHeight);
            }
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8) {
            View view = this.mCaption;
            view.layout(MARGIN_RESTORE_DISTANCE, MARGIN_RESTORE_DISTANCE, view.getMeasuredWidth(), this.mCaption.getMeasuredHeight());
            captionHeight = this.mCaption.getBottom() - this.mCaption.getTop();
            this.mMaximize.getHitRect(this.mMaximizeRect);
            this.mClose.getHitRect(this.mCloseRect);
            this.mMinimize.getHitRect(this.mMinimizeRect);
            this.mBack.getHitRect(this.mBackRect);
            this.mFullScreen.getHitRect(this.mFullScreenRect);
        } else {
            captionHeight = MARGIN_RESTORE_DISTANCE;
            this.mMaximizeRect.setEmpty();
            this.mCloseRect.setEmpty();
            this.mMinimizeRect.setEmpty();
            this.mBackRect.setEmpty();
            this.mFullScreenRect.setEmpty();
        }
        View view2 = this.mContent;
        if (view2 != null) {
            if (this.mOverlayWithAppContent) {
                view2.layout(MARGIN_RESTORE_DISTANCE, MARGIN_RESTORE_DISTANCE, view2.getMeasuredWidth(), this.mContent.getMeasuredHeight());
            } else {
                view2.layout(MARGIN_RESTORE_DISTANCE, captionHeight, view2.getMeasuredWidth(), this.mContent.getMeasuredHeight() + captionHeight);
            }
        }
        this.mOwner.notifyRestrictedCaptionAreaCallback(this.mMaximize.getLeft(), this.mMaximize.getTop(), this.mClose.getRight(), this.mClose.getBottom());
    }

    private void updateCaptionVisibility() {
        boolean z = this.mShow;
        int i = MARGIN_RESTORE_DISTANCE;
        boolean invisible = (!z || isFullscreen()) ? true : MARGIN_RESTORE_DISTANCE;
        Log.i(TAG, "updateCaptionVisibility, invisible = " + invisible + " mShow = " + this.mShow + " isFullscreen = " + isFullscreen());
        View view = this.mCaption;
        if (invisible) {
            i = 8;
        }
        view.setVisibility(i);
        setOnTouchListener(this.mCaption);
    }

    private boolean isVisible() {
        return this.mShow && !isFullscreen();
    }

    private void minimizeWindow() {
        try {
            ActivityManagerEx.moveActivityTaskToBack(this.mOwner.getAppToken(), true);
        } catch (RemoteException e) {
            Log.e(TAG, "minimizeWindow, Cannot change task workspace.");
        }
    }

    private void maximizeWindow() {
        try {
            int taskId = ActivityManagerEx.getTaskForActivity(this.mOwner.getAppToken(), false);
            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
            if (pcManager == null) {
                return;
            }
            if (isMaximized()) {
                pcManager.hwRestoreTask(taskId, -1.0f, -1.0f);
                this.mMaximize.setContentDescription(this.mContext.getResources().getString(33686039));
                return;
            }
            pcManager.hwResizeTask(taskId, new Rect(MARGIN_RESTORE_DISTANCE, MARGIN_RESTORE_DISTANCE, MARGIN_RESTORE_DISTANCE, MARGIN_RESTORE_DISTANCE));
            this.mMaximize.setContentDescription(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("restore_current_window")));
        } catch (RemoteException e) {
            Log.e(TAG, "maximizeWindow, Cannot change task workspace.");
        }
    }

    private void backWindow() {
        sendEvent(4, MARGIN_RESTORE_DISTANCE, MARGIN_RESTORE_DISTANCE);
        sendEvent(4, MARGIN_RESTORE_DISTANCE, 1);
    }

    private void fullscreenWindow() {
        try {
            int taskId = ActivityManagerEx.getTaskForActivity(this.mOwner.getAppToken(), false);
            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
            if (pcManager != null) {
                pcManager.hwResizeTask(taskId, new Rect(-1, -1, -1, -1));
                this.mFullScreen.setContentDescription(this.mContext.getResources().getString(33686056));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "fullscreenWindow, Cannot change task workspace.");
        }
    }

    public void sendEvent(int code, int metaState, int action) {
        long downTime = SystemClock.uptimeMillis();
        InputManagerEx.injectInputEvent((InputManager) this.mContext.getSystemService("input"), new KeyEvent(downTime, downTime, action, code, MARGIN_RESTORE_DISTANCE, metaState, -1, MARGIN_RESTORE_DISTANCE, 72, 257), InputManagerEx.getInjectInputEventModeAsync());
    }

    public boolean isCaptionShowing() {
        return this.mShow;
    }

    public int getCaptionHeight() {
        View view = this.mCaption;
        return view != null ? view.getHeight() : MARGIN_RESTORE_DISTANCE;
    }

    public void removeContentView() {
        if (this.mContent != null) {
            getCaptionView().removeView(this.mContent);
            this.mContent = null;
        }
    }

    public View getCaption() {
        return this.mCaption;
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.MarginLayoutParams(getCaptionView().getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.MarginLayoutParams(-1, -1);
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new ViewGroup.MarginLayoutParams(p);
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof ViewGroup.MarginLayoutParams;
    }

    public boolean isMaximized() {
        return HwPCMultiWindowCompatibility.isLayoutMaximized(this.mWindowState);
    }

    public boolean isLayoutSplit() {
        return HwPCMultiWindowCompatibility.isLayoutSplit(this.mWindowState);
    }

    private boolean isFullscreen() {
        return HwPCMultiWindowCompatibility.isLayoutFullscreen(this.mWindowState);
    }

    public void onWindowStateChanged(int state) {
        CharSequence charSequence;
        StringBuilder sb = new StringBuilder();
        sb.append("onWindowStateChanged(");
        TextView textView = this.mTitleView;
        if (textView == null) {
            charSequence = "NULL";
        } else {
            charSequence = textView.getText();
        }
        sb.append((Object) charSequence);
        sb.append(")");
        sb.append(Integer.toHexString(this.mWindowState));
        sb.append(" to ");
        sb.append(Integer.toHexString(state));
        sb.append(" isFullscreen ");
        sb.append(isFullscreen());
        sb.append(" isMaximized ");
        sb.append(isMaximized());
        HwPCUtils.log("HwPCMultiWindowManager", sb.toString());
        cleanAllViews();
        if (this.mWindowState != state && state != -1) {
            this.mWindowState = state;
            if (isVisible()) {
                this.mCaption.setVisibility(MARGIN_RESTORE_DISTANCE);
                if (!HwPCMultiWindowCompatibility.isMaximizeable(this.mWindowState)) {
                    this.mMaximize.setVisibility(8);
                } else {
                    this.mMaximize.setVisibility(MARGIN_RESTORE_DISTANCE);
                    if (isMaximized()) {
                        this.mMaximize.setBackgroundResource(33751729);
                        this.mMaximize.setContentDescription(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("restore_current_window")));
                    } else {
                        this.mMaximize.setBackgroundResource(33751731);
                        this.mMaximize.setContentDescription(this.mContext.getResources().getString(33686039));
                    }
                }
                if (!HwPCMultiWindowCompatibility.isFullscreenable(this.mWindowState)) {
                    this.mFullScreen.setVisibility(8);
                } else if (isFullscreen()) {
                    this.mFullScreen.setVisibility(8);
                } else {
                    showFullScreen();
                }
                if (HwPCUtils.enabledInPad() && isNotchInScreenForPc()) {
                    moveCloseViewAndBackView();
                    adaptCationHeight();
                }
                if (isDarkMode()) {
                    updateDarkShade();
                    return;
                }
                return;
            }
            Log.i(TAG, "will setVisibility GONE of mCaption view.");
            this.mCaption.setVisibility(8);
        }
    }

    private void showFullScreen() {
        if (this.mUseRtlRes) {
            this.mFullScreen.setBackgroundResource(33751810);
        } else {
            this.mFullScreen.setBackgroundResource(33751804);
        }
        this.mFullScreen.setContentDescription(this.mContext.getResources().getString(33686056));
        this.mFullScreen.setVisibility(MARGIN_RESTORE_DISTANCE);
    }

    public void updateShade(boolean isLight) {
        this.mCaptionLayout.setBackgroundResource(HwPartResourceUtils.getResourceId("hw_decor_caption_title"));
        if (this.mUseRtlRes) {
            this.mBack.setBackgroundResource(33751809);
        } else {
            this.mBack.setBackgroundResource(33751733);
        }
        this.mMinimize.setBackgroundResource(33751735);
        if (isMaximized()) {
            this.mMaximize.setBackgroundResource(33751729);
            this.mMaximize.setContentDescription(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("restore_current_window")));
        } else {
            this.mMaximize.setBackgroundResource(33751731);
            this.mMaximize.setContentDescription(this.mContext.getResources().getString(33686039));
        }
        if (this.mUseRtlRes) {
            this.mFullScreen.setBackgroundResource(33751810);
        } else {
            this.mFullScreen.setBackgroundResource(33751804);
        }
        this.mFullScreen.setContentDescription(this.mContext.getResources().getString(33686056));
        this.mClose.setBackgroundResource(33751737);
        this.mTitleView.setTextColor(-16777216);
    }

    public void setTitle(CharSequence title) {
        this.mTitleView.setText(title);
    }

    public boolean processKeyEvent(KeyEvent event) {
        if (event.getAction() == 0 && event.getKeyCode() == 111 && isFullscreen()) {
            Context context = this.mContext;
            if (context != null && context.getPackageName() != null && (this.mContext.getPackageName().equals("com.huawei.himovie") || this.mContext.getPackageName().equals("com.huawei.himovie.overseas") || this.mContext.getPackageName().equals("com.huawei.cloud"))) {
                return false;
            }
            try {
                int taskId = ActivityManagerEx.getTaskForActivity(this.mOwner.getAppToken(), false);
                IHwPCManager pcManager = HwPCUtils.getHwPCManager();
                if (pcManager != null) {
                    pcManager.hwRestoreTask(taskId, -1.0f, -1.0f);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "maximizeWindow, Cannot change task workspace.");
            }
            return true;
        } else if (event.getAction() != 1 || event.getKeyCode() != 51 || !event.isCtrlPressed() || !isFullscreen()) {
            return false;
        } else {
            clickView(this.mClose);
            return true;
        }
    }

    private boolean isSplitMode() {
        Intent intent;
        Context context = this.mOwner.getContext();
        if (!(context instanceof Activity) || (intent = ((Activity) context).getIntent()) == null) {
            return false;
        }
        return (IntentExEx.getHwFlags(intent) & 4) != 0;
    }

    private int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private boolean isNotchInScreenForPc() {
        if (Build.BOARD == null) {
            return false;
        }
        if (Build.BOARD.startsWith(BOARD_PAD_MXX) || Build.BOARD.startsWith(BOARD_PAD_MRX)) {
            return true;
        }
        return false;
    }

    private void setViewMargin(View view, boolean isStart, int moveDistance) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null && (params instanceof RelativeLayout.LayoutParams)) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) params;
            if (isStart) {
                layoutParams.setMarginStart(moveDistance);
            } else {
                layoutParams.setMarginEnd(moveDistance);
            }
            view.setLayoutParams(layoutParams);
        }
    }

    private void moveCloseViewAndBackView() {
        int layoutDirection = this.mContext.getResources().getConfiguration().getLayoutDirection();
        boolean isStart = true;
        if (!isSplitMode() || layoutDirection == 1) {
            View view = layoutDirection == 1 ? this.mClose : this.mBack;
            int marginMoveDistance = MARGIN_RESTORE_DISTANCE;
            if (layoutDirection == 1) {
                isStart = MARGIN_RESTORE_DISTANCE;
            }
            if (isMaximized()) {
                marginMoveDistance = dip2px(this.mContext, 32.0f);
            }
            setViewMargin(view, isStart, marginMoveDistance);
        }
    }

    private void adaptCationHeight() {
        ViewGroup.LayoutParams params = this.mCaptionLayout.getLayoutParams();
        params.height = isMaximized() ? dip2px(this.mContext, 32.0f) : -2;
        this.mCaptionLayout.setLayoutParams(params);
    }

    private int getNightMode() {
        return this.mContext.getResources().getConfiguration().uiMode & 48;
    }

    private boolean isDarkMode() {
        return getNightMode() == 32;
    }

    private void updateDarkShade() {
        this.mCaptionLayout.setBackgroundResource(HwPartResourceUtils.getResourceId("hw_decor_caption_title_dark"));
        if (this.mUseRtlRes) {
            this.mBack.setBackgroundResource(33751820);
        } else {
            this.mBack.setBackgroundResource(33751732);
        }
        if (isMaximized()) {
            this.mMaximize.setBackgroundResource(33751728);
            this.mMaximize.setContentDescription(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("restore_current_window")));
        } else {
            this.mMaximize.setBackgroundResource(33751730);
            this.mMaximize.setContentDescription(this.mContext.getResources().getString(33686039));
        }
        if (this.mUseRtlRes) {
            this.mFullScreen.setBackgroundResource(33751819);
        } else {
            this.mFullScreen.setBackgroundResource(33751803);
        }
        this.mFullScreen.setContentDescription(this.mContext.getResources().getString(33686056));
        this.mMinimize.setBackgroundResource(33751734);
        this.mClose.setBackgroundResource(33751736);
        this.mTitleView.setTextColor(this.mContext.getResources().getColor(HwPartResourceUtils.getResourceId("hw_decor_title_text_white")));
    }
}
