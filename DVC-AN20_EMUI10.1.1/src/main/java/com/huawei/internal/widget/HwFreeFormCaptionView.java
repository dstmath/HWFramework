package com.huawei.internal.widget;

import android.app.Activity;
import android.app.WindowConfiguration;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.InflateException;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewStub;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.policy.DecorView;
import com.android.internal.policy.PhoneWindow;
import com.huawei.android.app.HwActivityTaskManager;
import java.util.ArrayList;

public class HwFreeFormCaptionView extends AbsHwMultiWindowCaptionView {
    private static final int CHILD_NUMBER = 3;
    private static final String TAG = "HwFreeFormCaptionView";
    private static final float UNSCALED_RATIO = 1.0f;
    private View mCaption;
    private View mClickTarget;
    private View mClose;
    private final Rect mCloseRect = new Rect();
    private View mContent;
    private View mDragBar;
    private int mDragSlop;
    private GestureDetector mGestureDetector;
    private boolean mIsCheckForDragging;
    private boolean mIsDragging = false;
    private View mMaximize;
    private final Rect mMaximizeRect = new Rect();
    private View mTips;
    private TextView mTipsButton;
    private final Rect mTipsButtonRect = new Rect();
    private final Rect mTipsRect = new Rect();
    private int mTipsShownValue = 0;
    private ArrayList<View> mTouchDispatchList = new ArrayList<>(3);
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

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.huawei.internal.widget.HwFreeFormCaptionView */
    /* JADX WARN: Multi-variable type inference failed */
    private void init(Context context) {
        this.mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mGestureDetector = new GestureDetector(context, (GestureDetector.OnGestureListener) this);
        this.mContext = context;
        this.mDragBarBackground = context.getDrawable(33751919);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCaption = getChildAt(0);
        this.mTips = getChildAt(1);
    }

    public void setPhoneWindow(PhoneWindow owner, boolean isShow) {
        Log.i(TAG, "begin setPhoneWindow, isShow = " + isShow);
        this.mOwner = owner;
        this.mIsShow = isShow;
        this.mDragBar = findViewById(34603265);
        resetDragBar();
        applyTheme(this.mDragBar);
        updateCaptionVisibility();
        this.mDecorView = this.mOwner.getDecorView();
        this.mDecorView.setOutlineProvider(ViewOutlineProvider.HW_MULTIWINDOW_FREEFORM_OUTLINE_PROVIDER);
        this.mDecorView.setClipToOutline(true);
        setOutlineProvider(ViewOutlineProvider.HW_MULTIWINDOW_FREEFORM_OUTLINE_PROVIDER);
        setClipToOutline(true);
        this.mMaximize = findViewById(34603268);
        this.mClose = findViewById(34603266);
        checkLayoutDirection();
        this.mWindowMode = this.mDecorView.getWindowMode();
        adjustFrameBorder();
    }

    private void checkLayoutDirection() {
        boolean isRtlSupport = this.mContext.getApplicationInfo().hasRtlSupport();
        int layoutDirection = getResources().getConfiguration().getLayoutDirection();
        if (!isRtlSupport && layoutDirection == 1 && (this.mMaximize.getLayoutParams() instanceof RelativeLayout.LayoutParams) && (this.mClose.getLayoutParams() instanceof RelativeLayout.LayoutParams)) {
            RelativeLayout.LayoutParams maxParams = (RelativeLayout.LayoutParams) this.mMaximize.getLayoutParams();
            maxParams.addRule(21);
            maxParams.removeRule(20);
            this.mMaximize.setLayoutParams(maxParams);
            RelativeLayout.LayoutParams closeParams = (RelativeLayout.LayoutParams) this.mClose.getLayoutParams();
            closeParams.addRule(20);
            closeParams.removeRule(21);
            this.mClose.setLayoutParams(closeParams);
            Log.i(TAG, "do not support RTL, set to RTL");
        }
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, 0, params, 3);
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
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.huawei.internal.widget.HwFreeFormCaptionView */
    /* JADX WARN: Multi-variable type inference failed */
    private void updateCaptionVisibility() {
        View view;
        this.mCaption.setVisibility(this.mIsShow ? 0 : 8);
        if (!this.mIsShow && (view = this.mTips) != null) {
            view.setVisibility(8);
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
        if (!this.mIsShow || !isNeedShowTips(this.mContext, false)) {
            View view2 = this.mTips;
            if (view2 != null) {
                view2.setVisibility(8);
            } else {
                Log.w(TAG, "mTips is null");
            }
        } else {
            try {
                showTips(windowMode);
            } catch (InflateException | UnsupportedOperationException e) {
                Log.e(TAG, "get tips fail");
            } catch (Exception e2) {
                Log.e(TAG, "get tips fail");
            }
        }
        if (!WindowConfiguration.isHwFreeFormWindowingMode(windowMode)) {
            saveTipStateAfterModeChange();
        }
        this.mWindowMode = windowMode;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            this.mClickTarget = calTouchedView(ev);
        }
        return this.mClickTarget != null;
    }

    private View calTouchedView(MotionEvent ev) {
        int posX = (int) ev.getX();
        int posY = (int) ev.getY();
        if (this.mMaximizeRect.contains(posX, posY) && this.mMaximize.getVisibility() == 0) {
            return this.mMaximize;
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
            toggleFreeformWindowingMode();
        } else if (view == this.mClose) {
            InputMethodManager imm = (InputMethodManager) this.mContext.getSystemService(InputMethodManager.class);
            if (imm != null) {
                imm.hideSoftInputFromWindow(this.mCaption.getWindowToken(), 2);
            }
            if (this.mOwner.getContext() instanceof Activity) {
                ((Activity) this.mOwner.getContext()).moveTaskToBack(true);
            } else {
                Log.d(TAG, "The owner context is not activity: " + this.mOwner.getContext());
                this.mOwner.dispatchOnWindowDismissed(true, false);
            }
        } else if (view != this.mTipsButton) {
            return true;
        } else {
            Log.d(TAG, "click tipsButton");
            this.mTips.setVisibility(8);
            saveTipState(this.mTipsShownValue, this.mContext);
            this.mTipsShownValue = 0;
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
        if (r7 != 3) goto L_0x00b9;
     */
    public boolean onTouch(View view, MotionEvent event) {
        int posX = (int) event.getX();
        int posY = (int) event.getY();
        boolean isFromMouse = event.getToolType(event.getActionIndex()) == 3;
        boolean isPrimaryButton = (event.getButtonState() & 1) != 0;
        int actionMasked = event.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (!this.mIsDragging && this.mIsCheckForDragging && (isFromMouse || passedSlop(posX, posY))) {
                        this.mIsCheckForDragging = false;
                        this.mIsDragging = true;
                        startMovingTask(getUnscaledCordinate(event.getRawX()), getUnscaledCordinate(event.getRawY()));
                    }
                    if (!this.mIsDragging && this.mIsCheckForDragging && !passedSlop(posX, posY) && !this.mHasLongClicked && System.currentTimeMillis() - this.mTouchDownTime > ((long) LONG_PRESS_TIME_IN_MS)) {
                        this.mDragBar.setBackgroundColor(0);
                        onLongClick(view, event);
                        this.mHasLongClicked = true;
                        return false;
                    }
                }
            }
            this.mDragBar.setBackgroundColor(0);
            if (this.mIsDragging) {
                if (actionMasked == 1) {
                    finishMovingTask();
                }
                this.mIsDragging = false;
                return !this.mIsCheckForDragging;
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

    private boolean passedSlop(int x, int y) {
        return Math.abs(x - this.mTouchDownX) > this.mDragSlop || Math.abs(y - this.mTouchDownY) > this.mDragSlop;
    }

    private void toggleFreeformWindowingMode() {
        int windowMode = HwActivityTaskManager.getActivityWindowMode(this.mOwner.getAppToken());
        if (windowMode != 102) {
            Log.e(TAG, "toggleFreeformWindowingMode error windowing mode: " + windowMode);
            return;
        }
        Window.WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                callback.toggleFreeformWindowingMode();
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot change task workspace.");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8) {
            View view = this.mCaption;
            view.layout(0, 0, view.getMeasuredWidth(), this.mCaption.getMeasuredHeight());
            captionHeight = this.mCaption.getBottom() - this.mCaption.getTop();
            this.mMaximize.getHitRect(this.mMaximizeRect);
            this.mClose.getHitRect(this.mCloseRect);
        } else {
            captionHeight = 0;
            this.mMaximizeRect.setEmpty();
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
            view3.layout(0, captionHeight, view3.getMeasuredWidth(), this.mContent.getMeasuredHeight() + captionHeight);
        }
        this.mOwner.notifyRestrictedCaptionAreaCallback(this.mMaximize.getLeft(), this.mMaximize.getTop(), this.mClose.getRight(), this.mClose.getBottom());
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8) {
            measureChildWithMargins(this.mCaption, widthMeasureSpec, 0, heightMeasureSpec, 0);
            captionHeight = this.mCaption.getMeasuredHeight();
        } else {
            captionHeight = 0;
        }
        View view = this.mContent;
        if (view != null) {
            measureChildWithMargins(view, widthMeasureSpec, 0, heightMeasureSpec, captionHeight);
        }
        View view2 = this.mTips;
        if (!(view2 == null || view2.getVisibility() == 8)) {
            measureChildWithMargins(this.mTips, View.MeasureSpec.makeMeasureSpec((int) (((float) View.MeasureSpec.getSize(widthMeasureSpec)) * 0.6666667f), 1073741824), 0, heightMeasureSpec, 0);
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        this.mTouchDispatchList.ensureCapacity(3);
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
        return this.mTouchDispatchList;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.widget.AbsHwMultiWindowCaptionView
    public void getTouchOffsets(int[] outTouchOffsets, float touchX, float touchY) {
        outTouchOffsets[0] = (int) (((float) this.mDragBar.getLeft()) + touchX);
        outTouchOffsets[1] = (int) (((float) this.mDragBar.getTop()) + touchY);
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
            dragBarDrawable.setAlpha(255);
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
        if (this.mTips != null && this.mCaption.getVisibility() == 0 && WindowConfiguration.isHwFreeFormWindowingMode(windowMode)) {
            ViewStub stub = (ViewStub) findViewById(34603264);
            if (!(stub == null || stub.getParent() == null)) {
                stub.inflate();
                Log.i(TAG, "bubble view stub inflated");
            }
            this.mTipsButton = (TextView) this.mTips.findViewById(34603397);
            ((TextView) this.mTips.findViewById(34603398)).setText(this.mContext.getString(33685655));
            handleTipsLayoutDirection(this.mContext);
            this.mTips.setVisibility(0);
            this.mTipsShownValue = 256;
        }
    }
}
