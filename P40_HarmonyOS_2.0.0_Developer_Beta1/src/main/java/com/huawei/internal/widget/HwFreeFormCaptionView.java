package com.huawei.internal.widget;

import android.app.Activity;
import android.app.WindowConfiguration;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Flog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.policy.DecorView;
import com.android.internal.policy.PhoneWindow;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.os.ProcessExt;
import com.huawei.android.view.HwWindowManager;
import com.huawei.internal.widget.HwCaptionViewUtils;
import com.huawei.sidetouch.TpCommandConstant;
import java.util.ArrayList;
import java.util.Locale;

public class HwFreeFormCaptionView extends AbsHwMultiWindowCaptionView {
    private static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER";
    private static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL_OPAQUE = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER_LOCKSCREEN";
    private static final int BLUR_RADIUS = 10;
    private static final int CHILD_NUMBER = 4;
    private static final int ROTATION_INVALID = -1;
    private static final float SCALE_HALF = 0.5f;
    private static final float SCALE_INVALID = -1.0f;
    private static final float SCALE_THRESHOLD = 0.5f;
    private static final String TAG = "HwFreeFormCaptionView";
    private static final float UNSCALED_RATIO = 1.0f;
    private static boolean isTaskPositionDebugOpen = false;
    private static float newScale = SCALE_INVALID;
    private static float oldScale = SCALE_INVALID;
    private Runnable delayTask;
    private HwFreeFormCaptionViewAnim mAnim;
    protected View mCaption;
    private View mClickTarget;
    protected ImageView mClose;
    protected final Rect mCloseRect = new Rect();
    private View mContent;
    private View mContentCover;
    private HwFreeFormCoverView mCoverView;
    protected View mDragBar;
    protected HwDragBarPopupWindow mDragBarPopupWindow;
    protected final Rect mDragBarRect = new Rect();
    private int mDragSlop;
    private GestureDetector mGestureDetector;
    private boolean mIsCheckForDragging;
    private boolean mIsDragging = false;
    protected boolean mIsRtl;
    protected ImageView mMaximize;
    protected final Rect mMaximizeRect = new Rect();
    protected ImageView mMinimize;
    protected final Rect mMinimizeRect = new Rect();
    private int mRotation = -1;
    private int mTaskId;
    private View mTips;
    private TextView mTipsButton;
    protected final Rect mTipsButtonRect = new Rect();
    private final Rect mTipsRect = new Rect();
    private int mTipsShownValue = 0;
    private ArrayList<View> mTouchDispatchList = new ArrayList<>(4);
    private long mTouchDownTime;
    private int mTouchDownX;
    private int mTouchDownY;

    public HwFreeFormCaptionView(Context context) {
        super(context);
        init(context);
    }

    public HwFreeFormCaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HwFreeFormCaptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.huawei.internal.widget.HwFreeFormCaptionView */
    /* JADX WARN: Multi-variable type inference failed */
    private void init(Context context) {
        this.mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mGestureDetector = new GestureDetector(context, (GestureDetector.OnGestureListener) this);
        this.mContext = context;
        this.mDragBarBackground = context.getDrawable(33752028);
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver != null) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                /* class com.huawei.internal.widget.$$Lambda$HwFreeFormCaptionView$QPeSZ8K4Dl1WNmkGSrp5fMYrZTI */

                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public final void onGlobalLayout() {
                    HwFreeFormCaptionView.this.lambda$init$0$HwFreeFormCaptionView();
                }
            });
        }
    }

    public /* synthetic */ void lambda$init$0$HwFreeFormCaptionView() {
        float scaleNow = HwActivityTaskManager.getStackScale(this.mTaskId);
        HwDragBarPopupWindow hwDragBarPopupWindow = this.mDragBarPopupWindow;
        if (hwDragBarPopupWindow != null && hwDragBarPopupWindow.isShowing() && scaleNow > 0.5f) {
            this.mDragBarPopupWindow.dismiss();
        }
        if ((scaleNow > 0.5f && oldScale <= 0.5f) || (scaleNow <= 0.5f && oldScale > 0.5f)) {
            Log.i(TAG, "layout end, scale abnormal");
            startDragBarAnim();
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCaption = getChildAt(0);
        this.mTips = getChildAt(1);
        this.mContentCover = getChildAt(2);
    }

    public void setPhoneWindow(PhoneWindow owner, boolean isShow) {
        View view;
        Log.i(TAG, "begin setPhoneWindow, isShow = " + isShow);
        this.mOwner = owner;
        this.mIsShow = isShow;
        this.mTaskId = getTaskId();
        this.mDragBar = findViewById(34603298);
        this.mMaximize = (ImageView) findViewById(34603301);
        this.mMinimize = (ImageView) findViewById(34603302);
        this.mClose = (ImageView) findViewById(34603299);
        this.mCoverView = (HwFreeFormCoverView) findViewById(34603288);
        HwFreeFormCoverView hwFreeFormCoverView = this.mCoverView;
        if (!(hwFreeFormCoverView == null || (view = this.mContentCover) == null)) {
            hwFreeFormCoverView.setParent(view);
        }
        checkIfNeedHide();
        resetDragBar();
        applyTheme(this.mDragBar);
        updateCaptionVisibility();
        this.mDecorView = this.mOwner.getDecorView();
        this.mDecorView.setOutlineProvider(ViewOutlineProvider.HW_MULTIWINDOW_FREEFORM_OUTLINE_PROVIDER);
        this.mDecorView.setClipToOutline(true);
        setOutlineProvider(ViewOutlineProvider.HW_MULTIWINDOW_FREEFORM_OUTLINE_PROVIDER);
        setClipToOutline(true);
        checkLayoutDirection();
        this.mWindowMode = this.mDecorView.getWindowMode();
        adjustFrameBorder();
        this.mDragBarPopupWindow = new HwDragBarPopupWindow(this);
    }

    private void checkLayoutDirection() {
        this.mIsRtl = false;
        boolean isRtlSupport = this.mContext.getApplicationInfo().hasRtlSupport();
        int layoutDirection = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault());
        if (isRtlSupport && layoutDirection == 1) {
            this.mIsRtl = true;
        }
        if (!isRtlSupport && layoutDirection == 1 && (this.mMaximize.getLayoutParams() instanceof RelativeLayout.LayoutParams) && (this.mMinimize.getLayoutParams() instanceof RelativeLayout.LayoutParams) && (this.mClose.getLayoutParams() instanceof RelativeLayout.LayoutParams)) {
            this.mIsRtl = true;
            RelativeLayout.LayoutParams maxParams = (RelativeLayout.LayoutParams) this.mMaximize.getLayoutParams();
            ImageView imageView = this.mMaximize;
            imageView.setPadding(imageView.getPaddingEnd(), this.mMaximize.getPaddingTop(), this.mMaximize.getPaddingStart(), this.mMaximize.getPaddingBottom());
            maxParams.addRule(21);
            maxParams.removeRule(20);
            this.mMaximize.setLayoutParams(maxParams);
            RelativeLayout.LayoutParams minParams = (RelativeLayout.LayoutParams) this.mMinimize.getLayoutParams();
            ImageView imageView2 = this.mMinimize;
            imageView2.setPadding(imageView2.getPaddingEnd(), this.mMinimize.getPaddingTop(), this.mMinimize.getPaddingStart(), this.mMinimize.getPaddingBottom());
            minParams.addRule(16, 34603301);
            minParams.removeRule(17);
            this.mMinimize.setLayoutParams(minParams);
            RelativeLayout.LayoutParams closeParams = (RelativeLayout.LayoutParams) this.mClose.getLayoutParams();
            ImageView imageView3 = this.mClose;
            imageView3.setPadding(imageView3.getPaddingEnd(), this.mClose.getPaddingTop(), this.mClose.getPaddingStart(), this.mClose.getPaddingBottom());
            closeParams.addRule(20);
            closeParams.removeRule(21);
            this.mClose.setLayoutParams(closeParams);
            Log.i(TAG, "do not support RTL, set to RTL");
        }
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, 0, params, 4);
        this.mContent = child;
    }

    public void attachHwViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        super.attachHwViewToParent(child, 0, params);
        this.mContent = child;
    }

    public void detachHwViewFromParent(View child) {
        super.detachHwViewFromParent(child);
        if (this.mContent == child) {
            removeFrameBorder();
            this.mContent = null;
            saveTipStateAfterModeChange();
        }
    }

    public void onConfigurationChanged(boolean isShow) {
        Log.i(TAG, "onConfigurationChanged, isShow = " + isShow);
        this.mIsShow = isShow;
        updateCaptionVisibility();
        adjustFrameBorder();
        HwDragBarPopupWindow hwDragBarPopupWindow = this.mDragBarPopupWindow;
        if (hwDragBarPopupWindow != null) {
            hwDragBarPopupWindow.dismiss();
        }
        if (getDisplay() != null) {
            int newRotation = getDisplay().getDisplayRotation();
            int i = this.mRotation;
            if (!(i == -1 || i == newRotation)) {
                checkButtonsStatus();
            }
            this.mRotation = newRotation;
        }
    }

    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            HwFreeFormCaptionViewAnim hwFreeFormCaptionViewAnim = this.mAnim;
            if (hwFreeFormCaptionViewAnim == null || !hwFreeFormCaptionViewAnim.inAnimating) {
                newScale = HwActivityTaskManager.getStackScale(this.mTaskId);
                float f = newScale;
                if (f > 0.5f) {
                    show();
                    oldScale = newScale;
                } else if (f <= 0.5f) {
                    this.delayTask = new Runnable() {
                        /* class com.huawei.internal.widget.$$Lambda$HwFreeFormCaptionView$ssoAg7VT4KdBExF4m9qfwUiORRw */

                        @Override // java.lang.Runnable
                        public final void run() {
                            HwFreeFormCaptionView.this.lambda$onWindowVisibilityChanged$1$HwFreeFormCaptionView();
                        }
                    };
                    oldScale = f;
                }
            }
        }
    }

    public /* synthetic */ void lambda$onWindowVisibilityChanged$1$HwFreeFormCaptionView() {
        hide(newScale);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: com.huawei.internal.widget.HwFreeFormCaptionView */
    /* JADX WARN: Multi-variable type inference failed */
    private void updateCaptionVisibility() {
        HwFreeFormCoverView hwFreeFormCoverView;
        View view;
        boolean isShowCaption = isShowCaption();
        this.mCaption.setVisibility((!this.mIsShow || !isShowCaption) ? 8 : 0);
        if ((!this.mIsShow || !isShowCaption) && (view = this.mTips) != null) {
            view.setVisibility(8);
        }
        if (this.mContentCover != null && ((hwFreeFormCoverView = this.mCoverView) == null || !hwFreeFormCoverView.isAnimating())) {
            this.mContentCover.setVisibility(8);
        }
        this.mDragBar.setOnTouchListener(this);
        int windowMode = 0;
        DecorView dv = this.mOwner.getDecorView();
        if (dv != null) {
            ViewOutlineProvider viewOutlineProvider = null;
            dv.setOutlineProvider(this.mIsShow ? ViewOutlineProvider.HW_MULTIWINDOW_FREEFORM_OUTLINE_PROVIDER : null);
            dv.setClipToOutline(true);
            if (this.mIsShow) {
                viewOutlineProvider = ViewOutlineProvider.HW_MULTIWINDOW_FREEFORM_OUTLINE_PROVIDER;
            }
            setOutlineProvider(viewOutlineProvider);
            setClipToOutline(true);
            windowMode = dv.getWindowMode();
        }
        this.mWindowMode = windowMode;
    }

    private boolean isNeedCalInset() {
        if (this.mContent == null || this.mContext.getApplicationInfo() == null) {
            return false;
        }
        return HwActivityTaskManager.isNeedAdapterCaptionView(this.mContext.getApplicationInfo().processName);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isTaskPositionDebugOpen) {
            isTaskPositionDebugOpen = HwWindowManager.isTaskPositionDebugOpen();
        }
        if (!this.mDragBarRect.contains((int) ev.getX(), (int) ev.getY()) || this.mDragBar.getVisibility() != 0) {
            if (ev.getAction() == 0) {
                this.mClickTarget = calTouchedView(ev);
            }
            if (this.mClickTarget != null) {
                return true;
            }
            return false;
        }
        if (isTaskPositionDebugOpen) {
            Log.i(TAG, "hwfreeform onInterceptTouchEvent dragRect:" + this.mDragBarRect + ", touchPos: (" + ((int) ev.getX()) + "," + ((int) ev.getY()) + ")");
        }
        return true;
    }

    private View calTouchedView(MotionEvent ev) {
        int posX = (int) ev.getX();
        int posY = (int) ev.getY();
        if (isTaskPositionDebugOpen) {
            Log.i(TAG, "hwfreeform touchPos:(" + posX + "," + posY + "), maxRect:" + this.mMaximizeRect + ", minRect:" + this.mMinimizeRect + ", closeRect:" + this.mCloseRect);
        }
        if (this.mMaximizeRect.contains(posX, posY) && this.mMaximize.getVisibility() == 0) {
            return this.mMaximize;
        }
        if (this.mMinimizeRect.contains(posX, posY) && this.mMinimize.getVisibility() == 0) {
            return this.mMinimize;
        }
        if (this.mCloseRect.contains(posX, posY) && this.mClose.getVisibility() == 0) {
            return this.mClose;
        }
        if (this.mTips.getVisibility() != 0 || !this.mTipsRect.contains(posX, posY)) {
            return null;
        }
        if (this.mTipsButtonRect.contains(posX, posY)) {
            return this.mTipsButton;
        }
        return this.mTips;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean z;
        if (this.mDragBarRect.contains((int) event.getX(), (int) event.getY()) || (z = this.mIsDragging) || (!z && this.mIsCheckForDragging)) {
            onTouch(this.mDragBar, event);
            return true;
        }
        if (event.getAction() == 1 || event.getAction() == 3) {
            this.mDragBar.setBackgroundColor(0);
        }
        if (this.mClickTarget == null) {
            return false;
        }
        this.mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (action == 1 || action == 3) {
            this.mClickTarget = null;
        }
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        View view = this.mClickTarget;
        if (view == this.mMaximize) {
            if (isTaskPositionDebugOpen) {
                Log.i(TAG, "click maxButton");
            }
            IBinder appToken = this.mOwner.getAppToken();
            if (appToken == null) {
                onConfigurationChanged(false);
            }
            HwActivityTaskManager.toggleFreeformWindowingMode(appToken, this.mOwner.getContext().getPackageName());
        } else {
            IBinder iBinder = null;
            if (view == this.mMinimize) {
                if (isTaskPositionDebugOpen) {
                    Log.i(TAG, "click minButton");
                }
                if (this.mOwner.getContext() instanceof Activity) {
                    iBinder = ((Activity) this.mOwner.getContext()).getActivityToken();
                }
                HwActivityTaskManager.minimizeHwFreeForm(iBinder, this.mOwner.getContext().getPackageName(), true);
            } else if (view == this.mClose) {
                if (isTaskPositionDebugOpen) {
                    Log.i(TAG, "click closeButton");
                }
                InputMethodManager imm = (InputMethodManager) this.mContext.getSystemService(InputMethodManager.class);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(this.mCaption.getWindowToken(), 2);
                }
                if (this.mOwner.getContext() instanceof Activity) {
                    iBinder = ((Activity) this.mOwner.getContext()).getActivityToken();
                }
                HwActivityTaskManager.removeTask(-1, iBinder, this.mOwner.getContext().getPackageName(), true, "close-freeform");
            } else if (view != this.mTipsButton) {
                return true;
            } else {
                Log.d(TAG, "click tipsButton");
                this.mTips.setVisibility(8);
                saveTipState(this.mTipsShownValue, this.mContext);
                this.mTipsShownValue = 0;
            }
        }
        return true;
    }

    private float getUnscaledCordinate(float cordinate) {
        float windowScale;
        if (getDisplay() == null || getDisplay().getDisplayAdjustments() == null) {
            windowScale = 1.0f;
        } else {
            windowScale = getDisplay().getDisplayAdjustments().getCompatibilityInfo().getSdrLowResolutionRatio();
        }
        if (windowScale != 1.0f) {
            return cordinate / windowScale;
        }
        return cordinate;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002f, code lost:
        if (r7 != 3) goto L_0x00f3;
     */
    public boolean onTouch(View view, MotionEvent event) {
        HwDragBarPopupWindow hwDragBarPopupWindow;
        int posX = (int) event.getX();
        int posY = (int) event.getY();
        boolean isFromMouse = event.getToolType(event.getActionIndex()) == 3;
        boolean isPrimaryButton = (event.getButtonState() & 1) != 0;
        int actionMasked = event.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (!this.mIsDragging && this.mIsCheckForDragging && passedSlop(posX, posY)) {
                        this.mIsCheckForDragging = false;
                        this.mIsDragging = true;
                        startMovingTask(event.getRawX(), event.getRawY());
                    }
                    if (!this.mIsDragging && this.mIsCheckForDragging && !passedSlop(posX, posY) && !this.mHasLongClicked && System.currentTimeMillis() - this.mTouchDownTime > ((long) LONG_PRESS_TIME_IN_MS)) {
                        this.mDragBar.setBackgroundColor(0);
                        onLongClick(view, event);
                        this.mHasLongClicked = true;
                        return false;
                    }
                }
            } else if (HwActivityTaskManager.getStackScale(-100) <= 0.5f && !this.mHasLongClicked && (hwDragBarPopupWindow = this.mDragBarPopupWindow) != null && !hwDragBarPopupWindow.isShowing() && !this.mDragBarPopupWindow.isTouchDragBarDismiss()) {
                Flog.bdReport(991310114);
                this.mDragBarPopupWindow.show();
            }
            HwDragBarPopupWindow hwDragBarPopupWindow2 = this.mDragBarPopupWindow;
            if (hwDragBarPopupWindow2 != null) {
                hwDragBarPopupWindow2.setTouchDragBarDismissState(false);
            }
            if (this.mHasLongClicked) {
                dismissNotSupportSplitView(actionMasked == 3 ? 1 : 0);
            }
            this.mDragBar.setBackgroundColor(0);
            if (!this.mIsDragging) {
                this.mIsCheckForDragging = false;
            } else {
                if (actionMasked == 1) {
                    finishMovingTask();
                }
                this.mIsDragging = false;
                boolean isCheck = !this.mIsCheckForDragging;
                this.mIsCheckForDragging = false;
                return isCheck;
            }
        } else {
            this.mHasLongClicked = false;
            if (!this.mIsShow) {
                return false;
            }
            if (!isFromMouse || isPrimaryButton) {
                this.mIsCheckForDragging = true;
                this.mTouchDownX = posX;
                this.mTouchDownY = posY;
                this.mTouchDownTime = System.currentTimeMillis();
            }
            this.mDragBar.setBackground(this.mDragBarBackground);
        }
        return this.mIsDragging || this.mIsCheckForDragging;
    }

    @Override // com.huawei.internal.widget.AbsHwMultiWindowCaptionView
    public boolean onLongClick(View view, MotionEvent motionEvent) {
        if (isOwnerSupportSplitScreen()) {
            return super.onLongClick(view, motionEvent);
        }
        showNotSupportSplitView();
        return true;
    }

    private boolean isOwnerSupportSplitScreen() {
        if (this.mOwner == null || !(this.mOwner.getContext() instanceof Activity) || !HwActivityTaskManager.isSupportDragToSplitScreen(((Activity) this.mOwner.getContext()).getActivityToken(), true)) {
            return false;
        }
        return true;
    }

    private boolean passedSlop(int x, int y) {
        return Math.abs(x - this.mTouchDownX) > this.mDragSlop || Math.abs(y - this.mTouchDownY) > this.mDragSlop;
    }

    private void hide(float scale) {
        int[] locations = new int[2];
        this.mMaximize.getLocationOnScreen(locations);
        float maxStartPos = (float) locations[0];
        this.mMinimize.getLocationOnScreen(locations);
        float minStartPos = (float) locations[0];
        this.mClose.getLocationOnScreen(locations);
        float closeStartPos = ((float) locations[0]) - ((((float) this.mClose.getWidth()) * scale) / 2.0f);
        float barLen = ((float) this.mDragBar.getWidth()) * scale;
        this.mDragBar.getLocationOnScreen(locations);
        float endPos = ((float) locations[0]) + (barLen / 2.0f);
        float closeTransDistance = (endPos - closeStartPos) + this.mClose.getTranslationX();
        this.mDragBarRect.set(0, 0, this.mCaption.getRight(), this.mCaption.getBottom());
        float maxTransDistance = (endPos - maxStartPos) + this.mMaximize.getTranslationX();
        float minTransDistance = (endPos - minStartPos) + this.mMinimize.getTranslationX();
        setButtonStatus(this.mMaximize, maxTransDistance, 0.0f, 0.5f, 8);
        setButtonStatus(this.mMinimize, minTransDistance, 0.0f, 0.5f, 8);
        setButtonStatus(this.mClose, closeTransDistance, 0.0f, 0.5f, 8);
    }

    private void show() {
        setButtonStatus(this.mMaximize, 0.0f, 1.0f, 1.0f, 0);
        setButtonStatus(this.mMinimize, 0.0f, 1.0f, 1.0f, 0);
        setButtonStatus(this.mClose, 0.0f, 1.0f, 1.0f, 0);
        this.mMaximize.getHitRect(this.mMaximizeRect);
        this.mMinimize.getHitRect(this.mMinimizeRect);
        this.mClose.getHitRect(this.mCloseRect);
        this.mDragBar.getHitRect(this.mDragBarRect);
        checkIfNeedHide();
    }

    private void setButtonStatus(View view, float transX, float alpha, float scale, int visible) {
        view.setTranslationX(transX);
        view.setAlpha(alpha);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setVisibility(visible);
    }

    private void checkButtonsStatus() {
        HwFreeFormCaptionViewAnim hwFreeFormCaptionViewAnim = this.mAnim;
        if (hwFreeFormCaptionViewAnim == null || !hwFreeFormCaptionViewAnim.inAnimating) {
            newScale = HwActivityTaskManager.getStackScale(this.mTaskId);
            if (oldScale <= 0.5f && newScale > 0.5f) {
                show();
            } else if (oldScale > 0.5f) {
                float f = newScale;
                if (f <= 0.5f) {
                    hide(f);
                }
            }
            float f2 = newScale;
            if (f2 != 1.0f) {
                oldScale = f2;
            }
        }
    }

    private int getTaskId() {
        if (this.mOwner == null || !(this.mOwner.getContext() instanceof Activity)) {
            return -100;
        }
        return ((Activity) this.mOwner.getContext()).getTaskId();
    }

    private int calculateCaptionHeight() {
        int statusBarHeight = this.mContext.getResources().getDimensionPixelSize(17105445);
        if (statusBarHeight < this.mCaption.getHeight()) {
            return this.mCaption.getHeight() - statusBarHeight;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        int captionHeight;
        int contentHeight;
        int contentWidth;
        if (this.mCaption.getVisibility() != 8) {
            View view = this.mCaption;
            view.layout(0, 0, view.getMeasuredWidth(), this.mCaption.getMeasuredHeight());
            HwDragBarPopupWindow hwDragBarPopupWindow = this.mDragBarPopupWindow;
            if (hwDragBarPopupWindow != null) {
                hwDragBarPopupWindow.setDragBarTouchRect(this.mDragBarRect);
            }
            if (!isNeedCalInset()) {
                captionHeight = this.mCaption.getBottom() - this.mCaption.getTop();
            } else {
                captionHeight = calculateCaptionHeight();
            }
            this.mMaximize.getHitRect(this.mMaximizeRect);
            this.mMinimize.getHitRect(this.mMinimizeRect);
            this.mClose.getHitRect(this.mCloseRect);
            if (HwActivityTaskManager.getStackScale(this.mTaskId) <= 0.5f) {
                this.mDragBarRect.set(0, 0, this.mCaption.getRight(), this.mCaption.getBottom());
                Runnable runnable = this.delayTask;
                if (runnable != null) {
                    runnable.run();
                    this.delayTask = null;
                }
            } else {
                this.mDragBar.getHitRect(this.mDragBarRect);
            }
        } else {
            captionHeight = 0;
            this.mMaximizeRect.setEmpty();
            this.mMinimizeRect.setEmpty();
            this.mCloseRect.setEmpty();
        }
        View view2 = this.mTips;
        if (view2 == null || view2.getVisibility() == 8) {
            this.mTipsButtonRect.setEmpty();
            this.mTipsRect.setEmpty();
        } else {
            int tipsWidth = this.mTips.getMeasuredWidth();
            int tipsTop = dip2px(this.mContext, 28.0f);
            this.mTips.layout((this.mContent.getMeasuredWidth() - tipsWidth) / 2, tipsTop, (this.mContent.getMeasuredWidth() + tipsWidth) / 2, this.mTips.getMeasuredHeight() + tipsTop);
            this.mTips.getHitRect(this.mTipsRect);
            TextView textView = this.mTipsButton;
            if (textView != null) {
                textView.getHitRect(this.mTipsButtonRect);
                Rect tempRect = new Rect();
                if ((this.mTipsButton.getParent() instanceof View) && (this.mTipsButton.getParent().getParent() instanceof View)) {
                    ((View) this.mTipsButton.getParent()).getHitRect(tempRect);
                    this.mTipsButtonRect.offset(tempRect.left, tempRect.top);
                    ((View) this.mTipsButton.getParent().getParent()).getHitRect(tempRect);
                    this.mTipsButtonRect.offset(tempRect.left, tempRect.top);
                }
                this.mTipsButtonRect.offset(this.mTipsRect.left, this.mTipsRect.top);
            }
        }
        View view3 = this.mContent;
        if (view3 != null) {
            contentWidth = view3.getMeasuredWidth();
            contentHeight = this.mContent.getMeasuredHeight();
            this.mContent.layout(0, captionHeight, contentWidth, captionHeight + contentHeight);
        } else {
            contentWidth = 0;
            contentHeight = 0;
        }
        View view4 = this.mContentCover;
        if (view4 != null) {
            view4.layout(0, captionHeight, contentWidth, captionHeight + contentHeight);
        }
        this.mOwner.notifyRestrictedCaptionAreaCallback(this.mMaximize.getLeft(), this.mMaximize.getTop(), this.mClose.getRight(), this.mClose.getBottom());
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8) {
            measureChildWithMargins(this.mCaption, widthMeasureSpec, 0, heightMeasureSpec, 0);
            if (!isNeedCalInset()) {
                captionHeight = this.mCaption.getMeasuredHeight();
            } else {
                captionHeight = calculateCaptionHeight();
            }
        } else {
            captionHeight = 0;
        }
        View view = this.mContent;
        if (view != null) {
            measureChildWithMargins(view, widthMeasureSpec, 0, heightMeasureSpec, captionHeight);
        }
        View view2 = this.mTips;
        if (!(view2 == null || view2.getVisibility() == 8)) {
            measureChildWithMargins(this.mTips, View.MeasureSpec.makeMeasureSpec((int) (((float) View.MeasureSpec.getSize(widthMeasureSpec)) * 0.6666667f), ProcessExt.SCHED_RESET_ON_FORK), 0, heightMeasureSpec, 0);
        }
        View view3 = this.mContentCover;
        if (view3 != null) {
            measureChildWithMargins(view3, widthMeasureSpec, 0, heightMeasureSpec, captionHeight);
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        this.mTouchDispatchList.ensureCapacity(4);
        View view = this.mCaption;
        if (view != null) {
            this.mTouchDispatchList.add(view);
        }
        View view2 = this.mTips;
        if (view2 != null) {
            this.mTouchDispatchList.add(view2);
        }
        View view3 = this.mContent;
        if (view3 != null) {
            this.mTouchDispatchList.add(view3);
        }
        View view4 = this.mContentCover;
        if (view4 != null) {
            this.mTouchDispatchList.add(view4);
        }
        return this.mTouchDispatchList;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.widget.AbsHwMultiWindowCaptionView
    public void getTouchOffsets(int[] outTouchOffsets, float touchX, float touchY) {
        if (this.mDragBarRect.left == 0 && this.mClose.getVisibility() == 0) {
            outTouchOffsets[0] = (int) (((float) this.mDragBar.getLeft()) + touchX);
            outTouchOffsets[1] = (int) (((float) this.mDragBar.getTop()) + touchY);
            return;
        }
        outTouchOffsets[0] = (int) touchX;
        outTouchOffsets[1] = (int) touchY;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.widget.AbsHwMultiWindowCaptionView
    public void resetDragBar() {
        View view = this.mDragBar;
        if (view instanceof ImageView) {
            Drawable dragBarDrawable = ((ImageView) view).getDrawable();
            if (dragBarDrawable == null) {
                Log.w(TAG, "reset drag bar failed, cause its drawable is null!");
                return;
            }
            dragBarDrawable.setAlpha(ConstantValues.MAX_CHANNEL_VALUE);
            Log.d(TAG, "reset drag bar now!");
        }
    }

    @Override // com.huawei.internal.widget.AbsHwMultiWindowCaptionView
    public void removeContentView() {
        super.removeContentView();
        View view = this.mContent;
        if (view != null) {
            removeView(view);
            this.mContent = null;
        }
        saveTipStateAfterModeChange();
    }

    private void saveTipStateAfterModeChange() {
        View view = this.mTips;
        if (view != null) {
            view.setVisibility(8);
        }
        if (this.mTipsShownValue != 0 && WindowConfiguration.isHwFreeFormWindowingMode(this.mWindowMode)) {
            int windowMode = getWindowMode();
            if (!WindowConfiguration.isHwFreeFormWindowingMode(windowMode) && WindowConfiguration.isHwMultiStackWindowingMode(windowMode)) {
                saveTipState(this.mTipsShownValue, this.mContext);
                this.mTipsShownValue = 0;
            }
        }
    }

    private void showTips(int windowMode) {
        if (this.mTips != null && this.mCaption.getVisibility() == 0 && this.mDragBar.getVisibility() == 0 && WindowConfiguration.isHwFreeFormWindowingMode(windowMode)) {
            ViewStub stub = (ViewStub) findViewById(34603297);
            if (!(stub == null || stub.getParent() == null)) {
                stub.inflate();
                Log.i(TAG, "bubble view stub inflated");
            }
            this.mTipsButton = (TextView) this.mTips.findViewById(34603436);
            ((TextView) this.mTips.findViewById(34603437)).setText(this.mContext.getString(33685722));
            handleTipsLayoutDirection(this.mContext);
            this.mTips.setVisibility(0);
            this.mTipsShownValue = 256;
        }
    }

    private boolean isShowCaption() {
        boolean isShow = true;
        if (this.mOwner != null && !(this.mOwner.getContext() instanceof Activity)) {
            isShow = false;
        }
        if (this.mOwner == null || !(this.mOwner.getContext() instanceof Activity)) {
            return isShow;
        }
        Intent intent = ((Activity) this.mOwner.getContext()).getIntent();
        return isShow & (intent == null || (intent.getHwFlags() & TpCommandConstant.TSA_EVENT_LEFT_VOLUME_INDICATOR_OFF) == 0);
    }

    /* access modifiers changed from: protected */
    public void checkIfNeedHide() {
        Intent intent;
        if (this.mOwner != null && (this.mOwner.getContext() instanceof Activity) && (intent = ((Activity) this.mOwner.getContext()).getIntent()) != null && isAppLockAction(intent.getAction())) {
            this.mMaximize.setVisibility(4);
            this.mMinimize.setVisibility(4);
        }
        if ("com.huawei.hwdockbar".equals(this.mContext.getPackageName())) {
            this.mMaximize.setVisibility(8);
            this.mMinimize.setVisibility(8);
        }
    }

    private boolean isAppLockAction(String action) {
        if (action == null) {
            return false;
        }
        if (ACTION_CONFIRM_APPLOCK_CREDENTIAL.equals(action) || ACTION_CONFIRM_APPLOCK_CREDENTIAL_OPAQUE.equals(action)) {
            return true;
        }
        return false;
    }

    private boolean scaleStateAbnormal(float newScaleTmp) {
        ImageView imageView;
        ImageView imageView2;
        HwFreeFormCaptionViewAnim hwFreeFormCaptionViewAnim = this.mAnim;
        boolean isInAnim = hwFreeFormCaptionViewAnim != null && hwFreeFormCaptionViewAnim.inAnimating;
        if (newScaleTmp <= 0.5f || ((!isInAnim || !this.mAnim.inFade) && (isInAnim || (imageView2 = this.mClose) == null || imageView2.getVisibility() == 0))) {
            return newScaleTmp <= 0.5f && ((isInAnim && this.mAnim.inShow) || (!isInAnim && (imageView = this.mClose) != null && imageView.getVisibility() == 0));
        }
        return true;
    }

    public void startDragBarAnim() {
        boolean isScaleChangedOverThreshold;
        if (elementViewsDisable()) {
            Log.i(TAG, "startDragBarAnim return, elementViewNotReady");
            return;
        }
        float newScaleTmp = HwActivityTaskManager.getStackScale(this.mTaskId);
        if (newScaleTmp > 0.5f && oldScale <= 0.5f) {
            isScaleChangedOverThreshold = true;
            Flog.bdReport(991310115);
        } else if (newScaleTmp > 0.5f || oldScale <= 0.5f) {
            isScaleChangedOverThreshold = false;
        } else {
            isScaleChangedOverThreshold = true;
            Flog.bdReport(991310116);
        }
        if (isScaleChangedOverThreshold || scaleStateAbnormal(newScaleTmp)) {
            cancelAndStartNewAnimation(newScaleTmp);
        }
    }

    private void cancelAndStartNewAnimation(float newScaleTmp) {
        ImageView imageView;
        ImageView imageView2;
        HwFreeFormCaptionViewAnim hwFreeFormCaptionViewAnim = this.mAnim;
        if (hwFreeFormCaptionViewAnim != null) {
            hwFreeFormCaptionViewAnim.tryCancel();
        }
        this.mAnim = new HwFreeFormCaptionViewAnim(this);
        newScale = newScaleTmp;
        if (newScaleTmp > 0.5f && (imageView2 = this.mClose) != null && imageView2.getVisibility() != 0) {
            this.mAnim.startShowingAnim();
            oldScale = newScale;
        } else if (newScaleTmp > 0.5f || (imageView = this.mClose) == null || imageView.getVisibility() != 0) {
            Log.d(TAG, "cancelAndStartNewAnimation, no animation played");
        } else {
            this.mAnim.startFadingAnim();
            oldScale = newScale;
        }
    }

    public void updatePopup(boolean isImmediateDismiss, boolean isFocus) {
        ImageView imageView;
        ImageView imageView2;
        HwDragBarPopupWindow hwDragBarPopupWindow = this.mDragBarPopupWindow;
        if (hwDragBarPopupWindow != null && !isFocus) {
            if (!isImmediateDismiss) {
                hwDragBarPopupWindow.dismiss();
            } else {
                hwDragBarPopupWindow.immediateInvisibleDismiss();
            }
        }
        if (!elementViewsDisable()) {
            float stackScale = HwActivityTaskManager.getStackScale(this.mTaskId);
            if (stackScale > 0.5f && (imageView2 = this.mClose) != null && imageView2.getVisibility() != 0) {
                show();
            } else if (stackScale <= 0.5f && (imageView = this.mClose) != null && imageView.getVisibility() == 0) {
                hide(stackScale);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean elementViewsDisable() {
        return this.mMaximize == null || this.mMinimize == null || this.mClose == null || this.mDragBar == null;
    }

    private void showNotSupportSplitView() {
        Log.i(TAG, "showNotSupportSplitView");
        Activity activity = null;
        if (this.mOwner.getContext() instanceof Activity) {
            activity = (Activity) this.mOwner.getContext();
        }
        if (activity == null || this.mContentCover == null || this.mCoverView == null) {
            Log.e(TAG, "showNotSupportSplitView error, activity or mContentCover is null.");
            return;
        }
        Bitmap snapshot = HwCaptionViewUtils.getTaskSnapshot(activity);
        if (snapshot == null) {
            Log.e(TAG, "showNotSupportSplitView error, snapshot is null.");
            return;
        }
        int captionHeight = this.mCaption.getHeight();
        Bitmap snapshot2 = Bitmap.createBitmap(snapshot, 0, captionHeight, snapshot.getWidth(), snapshot.getHeight() - captionHeight);
        Drawable icon = HwCaptionViewUtils.bitmap2Drawable(HwActivityTaskManager.getApplicationIcon(activity.getActivityToken(), true));
        if (icon == null) {
            Log.e(TAG, "showNotSupportSplitView error, icon is null.");
            return;
        }
        this.mCoverView.setIcon(icon);
        this.mCoverView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.mCoverView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            /* class com.huawei.internal.widget.HwFreeFormCaptionView.AnonymousClass1 */

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                HwFreeFormCaptionView.this.mCoverView.playIconShowAnimation();
                HwFreeFormCaptionView.this.mCoverView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        this.mContentCover.setVisibility(0);
        HwCaptionViewUtils.startToBlur(snapshot2, this.mCoverView, new HwCaptionViewUtils.BlurListener() {
            /* class com.huawei.internal.widget.$$Lambda$HwFreeFormCaptionView$pOLQRkv5y2tlotUxU8tSMMwDnBY */

            @Override // com.huawei.internal.widget.HwCaptionViewUtils.BlurListener
            public final void onBlurDone() {
                HwFreeFormCaptionView.this.lambda$showNotSupportSplitView$2$HwFreeFormCaptionView();
            }
        }, 10);
    }

    public /* synthetic */ void lambda$showNotSupportSplitView$2$HwFreeFormCaptionView() {
        this.mCoverView.setVisibility(0);
    }

    private void dismissNotSupportSplitView(int durationType) {
        View view = this.mContentCover;
        if (view == null || view.getVisibility() != 0) {
            Log.i(TAG, "dismissNotSupportSplitView error, mContentCover is invisible.");
            return;
        }
        HwFreeFormCoverView hwFreeFormCoverView = this.mCoverView;
        if (hwFreeFormCoverView == null) {
            Log.e(TAG, "dismissNotSupportSplitView error, mCoverView is null.");
        } else {
            hwFreeFormCoverView.playDismissAnimation(durationType);
        }
    }
}
