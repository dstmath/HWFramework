package com.android.internal.widget;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.HwPCMultiWindowCompatibility;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
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
import android.widget.TextView;
import com.android.internal.policy.PhoneWindow;
import java.util.ArrayList;

public class HwDecorCaptionView extends AbsHwDecorCaptionView implements View.OnClickListener {
    private static final String TAG = "DecorCaptionView";
    private static final long mDoubleClickInterval = 500;
    private View mBack;
    private final Rect mBackRect = new Rect();
    private View mCaption;
    private boolean mCheckForDragging;
    private View mClickTarget;
    private View mClose;
    private final Rect mCloseRect = new Rect();
    private View mContent;
    private Context mContext;
    private int mDragSlop;
    private boolean mDragging = false;
    private View mFullScreen;
    private final Rect mFullScreenRect = new Rect();
    private boolean mIsLight;
    private long mLastClickTime = 0;
    private View mMaximize;
    private final Rect mMaximizeRect = new Rect();
    private View mMinimize;
    private final Rect mMinimizeRect = new Rect();
    private Handler mMyHandler = new Handler();
    private boolean mOverlayWithAppContent = false;
    private PhoneWindow mOwner = null;
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

    public HwDecorCaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HwDecorCaptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        HwDecorCaptionView.super.onFinishInflate();
        this.mCaption = getChildAt(0);
        boolean isRtlSupport = this.mContext.getApplicationInfo().hasRtlSupport();
        int layoutDirection = this.mContext.getResources().getConfiguration().getLayoutDirection();
        if (!isRtlSupport && layoutDirection == 1) {
            this.mUseRtlRes = true;
        }
        this.mMaximize = findViewById(this.mUseRtlRes ? 34603210 : 34603185);
        this.mClose = findViewById(this.mUseRtlRes ? 34603212 : 34603187);
        this.mMinimize = findViewById(this.mUseRtlRes ? 34603211 : 34603186);
        this.mBack = findViewById(this.mUseRtlRes ? 34603213 : 34603188);
        this.mTitleView = (TextView) findViewById(this.mUseRtlRes ? 34603209 : 34603184);
        this.mFullScreen = findViewById(this.mUseRtlRes ? 34603214 : 34603208);
        this.mMaximize.setOnClickListener(this);
        this.mClose.setOnClickListener(this);
        this.mMinimize.setOnClickListener(this);
        this.mBack.setOnClickListener(this);
        this.mFullScreen.setOnClickListener(this);
        if (isMaximized()) {
            this.mMaximize.setContentDescription(getResources().getString(33686216));
        } else {
            this.mMaximize.setContentDescription(getResources().getString(33686039));
        }
        if (!isFullscreen()) {
            this.mFullScreen.setContentDescription(getResources().getString(33686056));
        }
    }

    /* access modifiers changed from: private */
    public void cleanAllViews() {
        this.mMinimize.setPressed(false);
        this.mMinimize.setHovered(false);
        this.mMaximize.setPressed(false);
        this.mMaximize.setHovered(false);
        this.mClose.setPressed(false);
        this.mClose.setHovered(false);
        this.mFullScreen.setPressed(false);
        this.mFullScreen.setHovered(false);
    }

    public void setPhoneWindow(PhoneWindow owner, boolean show) {
        this.mOwner = owner;
        this.mShow = show;
        this.mOverlayWithAppContent = owner.isOverlayWithDecorCaptionEnabled();
        if (this.mOverlayWithAppContent) {
            this.mCaption.setBackgroundColor(0);
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
            if (attributes != null && "com.android.contacts".equals(packageName)) {
                if (attributes.gravity == 85) {
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
                public void run() {
                    HwDecorCaptionView.this.cleanAllViews();
                }
            }, 300);
        } else if (view == this.mMinimize) {
            minimizeWindow();
            this.mMyHandler.postDelayed(new Runnable() {
                public void run() {
                    HwDecorCaptionView.this.cleanAllViews();
                }
            }, 300);
        } else if (view == this.mBack) {
            backWindow();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean z = false;
        if (this.mClickTarget == null) {
            return false;
        }
        int action = event.getAction();
        View view = this.mClickTarget;
        if (action == 0 || action == 2) {
            z = true;
        }
        view.setPressed(z);
        if (action == 1) {
            View view2 = calTouchedView(event);
            if (this.mClickTarget == view2) {
                clickView(view2);
            }
        }
        if (action == 1 || action == 3) {
            this.mClickTarget = null;
        }
        return true;
    }

    public boolean onTouch(View v, MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        boolean z = true;
        boolean fromMouse = e.getToolType(e.getActionIndex()) == 3;
        boolean primaryButton = (e.getButtonState() & 1) != 0;
        switch (e.getActionMasked()) {
            case 0:
                if (this.mShow) {
                    if (!fromMouse || primaryButton) {
                        this.mCheckForDragging = true;
                        this.mTouchDownX = x;
                        this.mTouchDownY = y;
                        break;
                    }
                } else {
                    return false;
                }
            case 1:
            case 3:
                if (this.mDragging) {
                    this.mDragging = false;
                    return !this.mCheckForDragging;
                }
                break;
            case 2:
                if (!this.mDragging && this.mCheckForDragging && (fromMouse || passedSlop(x, y))) {
                    this.mCheckForDragging = false;
                    this.mDragging = true;
                    Point offset = this.mOwner.getDecorView().getViewRootImpl().mOffset;
                    int offsetX = offset == null ? 0 : offset.x;
                    int offsetY = offset == null ? 0 : offset.y;
                    if (isMaximized()) {
                        try {
                            int taskId = ActivityManager.getService().getTaskForActivity(this.mOwner.getAppToken(), false);
                            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
                            if (pcManager != null) {
                                pcManager.hwRestoreTask(taskId, e.getRawX() + ((float) offsetX), e.getRawY() + ((float) offsetY));
                            }
                        } catch (RemoteException e2) {
                            Log.e(TAG, "Cannot change task workspace.");
                        }
                    }
                    startMovingTask(e.getRawX() + ((float) offsetX), e.getRawY() + ((float) offsetY));
                    break;
                }
        }
        if (!this.mDragging && e.getAction() == 1) {
            long time = e.getDownTime();
            if (this.mLastClickTime == 0 || time - this.mLastClickTime >= mDoubleClickInterval) {
                this.mLastClickTime = time;
            } else {
                this.mLastClickTime = 0;
                maximizeWindow();
            }
        }
        if (!this.mDragging && !this.mCheckForDragging) {
            z = false;
        }
        return z;
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        this.mTouchDispatchList.ensureCapacity(3);
        if (this.mCaption != null) {
            this.mTouchDispatchList.add(this.mCaption);
        }
        if (this.mContent != null) {
            this.mTouchDispatchList.add(this.mContent);
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
        this.mShow = show;
        updateCaptionVisibility();
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(params instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException("params " + params + " must subclass MarginLayoutParams");
        } else if (index >= 2 || getChildCount() >= 2) {
            throw new IllegalStateException("DecorCaptionView can only handle 1 client view");
        } else {
            HwDecorCaptionView.super.addView(child, 0, params);
            this.mContent = child;
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        if (this.mCaption.getVisibility() != 8) {
            measureChildWithMargins(this.mCaption, widthMeasureSpec, 0, heightMeasureSpec, 0);
            i = this.mCaption.getMeasuredHeight();
        } else {
            i = 0;
        }
        int captionHeight = i;
        if (this.mContent != null) {
            if (this.mOverlayWithAppContent) {
                measureChildWithMargins(this.mContent, widthMeasureSpec, 0, heightMeasureSpec, 0);
            } else {
                measureChildWithMargins(this.mContent, widthMeasureSpec, 0, heightMeasureSpec, captionHeight);
            }
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8) {
            this.mCaption.layout(0, 0, this.mCaption.getMeasuredWidth(), this.mCaption.getMeasuredHeight());
            captionHeight = this.mCaption.getBottom() - this.mCaption.getTop();
            this.mMaximize.getHitRect(this.mMaximizeRect);
            this.mClose.getHitRect(this.mCloseRect);
            this.mMinimize.getHitRect(this.mMinimizeRect);
            this.mBack.getHitRect(this.mBackRect);
            this.mFullScreen.getHitRect(this.mFullScreenRect);
        } else {
            captionHeight = 0;
            this.mMaximizeRect.setEmpty();
            this.mCloseRect.setEmpty();
            this.mMinimizeRect.setEmpty();
            this.mBackRect.setEmpty();
            this.mFullScreenRect.setEmpty();
        }
        if (this.mContent != null) {
            if (this.mOverlayWithAppContent) {
                this.mContent.layout(0, 0, this.mContent.getMeasuredWidth(), this.mContent.getMeasuredHeight());
            } else {
                this.mContent.layout(0, captionHeight, this.mContent.getMeasuredWidth(), this.mContent.getMeasuredHeight() + captionHeight);
            }
        }
        this.mOwner.notifyRestrictedCaptionAreaCallback(this.mMaximize.getLeft(), this.mMaximize.getTop(), this.mClose.getRight(), this.mClose.getBottom());
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [com.android.internal.widget.HwDecorCaptionView, android.view.View$OnTouchListener] */
    private void updateCaptionVisibility() {
        int i = 0;
        boolean invisible = !this.mShow || isFullscreen();
        View view = this.mCaption;
        if (invisible) {
            i = 8;
        }
        view.setVisibility(i);
        this.mCaption.setOnTouchListener(this);
    }

    private boolean isVisible() {
        return this.mShow && !isFullscreen();
    }

    private void minimizeWindow() {
        try {
            ActivityManager.getService().moveActivityTaskToBack(this.mOwner.getAppToken(), true);
        } catch (RemoteException e) {
            Log.e(TAG, "minimizeWindow, Cannot change task workspace.");
        }
    }

    private void maximizeWindow() {
        try {
            int taskId = ActivityManager.getService().getTaskForActivity(this.mOwner.getAppToken(), false);
            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
            if (pcManager == null) {
                return;
            }
            if (isMaximized()) {
                pcManager.hwRestoreTask(taskId, -1.0f, -1.0f);
                this.mMaximize.setContentDescription(getResources().getString(33686039));
                return;
            }
            pcManager.hwResizeTask(taskId, new Rect(0, 0, 0, 0));
            this.mMaximize.setContentDescription(getResources().getString(33686216));
        } catch (RemoteException e) {
            Log.e(TAG, "maximizeWindow, Cannot change task workspace.");
        }
    }

    private void backWindow() {
        sendEvent(4, 0, 0);
        sendEvent(4, 0, 1);
    }

    private void fullscreenWindow() {
        try {
            int taskId = ActivityManager.getService().getTaskForActivity(this.mOwner.getAppToken(), false);
            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
            if (pcManager != null) {
                pcManager.hwResizeTask(taskId, new Rect(-1, -1, -1, -1));
                this.mFullScreen.setContentDescription(getResources().getString(33686056));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "fullscreenWindow, Cannot change task workspace.");
        }
    }

    public void sendEvent(int code, int metaState, int action) {
        long downTime = SystemClock.uptimeMillis();
        KeyEvent ev = new KeyEvent(downTime, downTime, action, code, 0, metaState, -1, 0, 72, 257);
        InputManager.getInstance().injectInputEvent(ev, 0);
    }

    public boolean isCaptionShowing() {
        return this.mShow;
    }

    public int getCaptionHeight() {
        if (this.mCaption != null) {
            return this.mCaption.getHeight();
        }
        return 0;
    }

    public void removeContentView() {
        if (this.mContent != null) {
            removeView(this.mContent);
            this.mContent = null;
        }
    }

    public View getCaption() {
        return this.mCaption;
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.MarginLayoutParams(getContext(), attrs);
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

    private boolean isFullscreen() {
        return HwPCMultiWindowCompatibility.isLayoutFullscreen(this.mWindowState);
    }

    public void onWindowStateChanged(int state) {
        StringBuilder sb = new StringBuilder();
        sb.append("onWindowStateChanged(");
        sb.append(this.mTitleView == null ? "NULL" : this.mTitleView.getText());
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
                this.mCaption.setVisibility(0);
                if (!HwPCMultiWindowCompatibility.isMaximizeable(this.mWindowState)) {
                    this.mMaximize.setVisibility(8);
                } else {
                    this.mMaximize.setVisibility(0);
                    if (isMaximized()) {
                        this.mMaximize.setBackgroundResource(33751729);
                        this.mMaximize.setContentDescription(getResources().getString(33686216));
                    } else {
                        this.mMaximize.setBackgroundResource(33751731);
                        this.mMaximize.setContentDescription(getResources().getString(33686039));
                    }
                }
                if (!HwPCMultiWindowCompatibility.isFullscreenable(this.mWindowState)) {
                    this.mFullScreen.setVisibility(8);
                } else if (isFullscreen()) {
                    this.mFullScreen.setVisibility(8);
                } else {
                    if (this.mUseRtlRes) {
                        this.mFullScreen.setBackgroundResource(33751810);
                    } else {
                        this.mFullScreen.setBackgroundResource(33751804);
                    }
                    this.mFullScreen.setContentDescription(getResources().getString(33686056));
                }
            } else {
                this.mCaption.setVisibility(8);
            }
        }
    }

    public void updateShade(boolean isLight) {
        this.mIsLight = isLight;
        if (this.mUseRtlRes) {
            this.mBack.setBackgroundResource(33751809);
        } else {
            this.mBack.setBackgroundResource(33751733);
        }
        this.mMinimize.setBackgroundResource(33751735);
        if (isMaximized()) {
            this.mMaximize.setBackgroundResource(33751729);
            this.mMaximize.setContentDescription(getResources().getString(33686216));
        } else {
            this.mMaximize.setBackgroundResource(33751731);
            this.mMaximize.setContentDescription(getResources().getString(33686039));
        }
        if (isFullscreen()) {
            this.mFullScreen.setVisibility(8);
        } else {
            if (this.mUseRtlRes) {
                this.mFullScreen.setBackgroundResource(33751810);
            } else {
                this.mFullScreen.setBackgroundResource(33751804);
            }
            this.mFullScreen.setContentDescription(getResources().getString(33686056));
        }
        this.mClose.setBackgroundResource(33751737);
        this.mTitleView.setTextColor(-16777216);
    }

    public void setTitle(CharSequence title) {
        this.mTitleView.setText(title);
    }

    public boolean processKeyEvent(KeyEvent event) {
        if (event.getAction() != 0 || event.getKeyCode() != 111 || !isFullscreen()) {
            return false;
        }
        if (this.mContext != null && this.mContext.getPackageName() != null && (this.mContext.getPackageName().equals("com.huawei.himovie") || this.mContext.getPackageName().equals("com.huawei.himovie.overseas") || this.mContext.getPackageName().equals("com.huawei.cloud"))) {
            return false;
        }
        try {
            int taskId = ActivityManager.getService().getTaskForActivity(this.mOwner.getAppToken(), false);
            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
            if (pcManager != null) {
                pcManager.hwRestoreTask(taskId, -1.0f, -1.0f);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "maximizeWindow, Cannot change task workspace.");
        }
        return true;
    }
}
