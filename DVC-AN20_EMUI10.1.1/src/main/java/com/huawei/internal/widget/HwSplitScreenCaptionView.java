package com.huawei.internal.widget;

import android.app.WindowConfiguration;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
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
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.policy.DecorView;
import com.android.internal.policy.PhoneWindow;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;

public class HwSplitScreenCaptionView extends AbsHwMultiWindowCaptionView {
    private static final int ATTRIBUTE_BAR_PADDING_TOP = 12;
    private static final int ATTRIBUTE_MARGIN_TOP = 12;
    private static final int BAR_DRAWABLE_HEIGHT = 4;
    private static final int CHILD_NUMBER = 3;
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", BuildConfig.FLAVOR).equals(BuildConfig.FLAVOR));
    private static final String TAG = "HwSplitScreenCaptionView";
    private static final int TIPS_TOP_IN_PORTRAIT_CAPTION = 24;
    private View mCaption;
    private View mClickTarget;
    private View mContent;
    private View mDragBar;
    private int mDragBarHeight;
    private final Rect mDragBarRect = new Rect();
    private int mDragSlop;
    private GestureDetector mGestureDetector;
    private boolean mIsUpDownSplit = false;
    private int mStatusBarHeight;
    private View mTips;
    private TextView mTipsButton;
    private final Rect mTipsButtonRect = new Rect();
    private final Rect mTipsRect = new Rect();
    private int mTipsShownValue = 0;
    private ArrayList<View> mTouchDispatchList = new ArrayList<>(3);
    private long mTouchDownTime;
    private int mTouchDownX;
    private int mTouchDownY;

    public HwSplitScreenCaptionView(Context context) {
        super(context);
        init(context);
    }

    public HwSplitScreenCaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HwSplitScreenCaptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.huawei.internal.widget.HwSplitScreenCaptionView */
    /* JADX WARN: Multi-variable type inference failed */
    private void init(Context context) {
        this.mContext = context;
        this.mGestureDetector = new GestureDetector(context, (GestureDetector.OnGestureListener) this);
        this.mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mDragBarHeight = context.getResources().getDimensionPixelSize(34472526);
        this.mStatusBarHeight = context.getResources().getDimensionPixelSize(17105443);
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
        this.mDragBar = findViewById(34603269);
        this.mIsUpDownSplit = isUpDownSplit();
        resetDragBar();
        applyTheme(this.mDragBar);
        updateCaptionVisibility();
        this.mDecorView = this.mOwner.getDecorView();
        this.mDecorView.setOutlineProvider(ViewOutlineProvider.HW_MULTIWINDOW_SPLITSCREEN_OUTLINE_PROVIDER);
        this.mDecorView.setClipToOutline(true);
        if (this.mDecorView.getElevation() != 0.0f) {
            this.mOwner.setElevation(0.0f);
        } else {
            this.mDecorView.setElevation(0.0f);
        }
        setOutlineProvider(ViewOutlineProvider.HW_MULTIWINDOW_SPLITSCREEN_OUTLINE_PROVIDER);
        setClipToOutline(true);
        setElevation(0.0f);
        this.mWindowMode = this.mDecorView.getWindowMode();
        adjustFrameBorder();
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
        Log.d(TAG, "onConfigurationChanged, isShow = " + isShow);
        this.mIsShow = isShow;
        this.mIsUpDownSplit = isUpDownSplit();
        updateBar();
        updateCaptionVisibility();
        adjustFrameBorder();
    }

    private void updateBar() {
        View view = this.mDragBar;
        if (view instanceof ImageView) {
            ImageView dragBarImg = (ImageView) view;
            dragBarImg.setImageResource(33751921);
            updateDragBarViewSrc(dragBarImg);
        }
    }

    private void updateDragBarViewSrc(ImageView dragBarImg) {
        applyTheme(dragBarImg);
        if (this.mOwner != null) {
            View view = this.mOwner.getDecorView();
            if ((view instanceof DecorView) && view.hasWindowFocus()) {
                ((DecorView) view).showOrHideHighlightView(true);
            }
        }
    }

    private void updateCaptionVisibility() {
        View view;
        this.mCaption.setVisibility(this.mIsShow ? 0 : 8);
        if (!this.mIsShow && (view = this.mTips) != null) {
            view.setVisibility(8);
            this.mTipsShownValue = 0;
        }
        int windowMode = 0;
        DecorView dv = this.mOwner.getDecorView();
        if (dv != null) {
            ViewOutlineProvider viewOutlineProvider = null;
            dv.setOutlineProvider(this.mIsShow ? ViewOutlineProvider.HW_MULTIWINDOW_SPLITSCREEN_OUTLINE_PROVIDER : null);
            dv.setClipToOutline(true);
            if (this.mIsShow) {
                viewOutlineProvider = ViewOutlineProvider.HW_MULTIWINDOW_SPLITSCREEN_OUTLINE_PROVIDER;
            }
            setOutlineProvider(viewOutlineProvider);
            setClipToOutline(true);
            windowMode = dv.getWindowMode();
        }
        if (!this.mIsShow) {
            this.mWindowMode = windowMode;
            return;
        }
        if (this.mIsUpDownSplit) {
            this.mDragBar.setPadding(0, dip2px(this.mContext, 12.0f), 0, this.mDragBarHeight - dip2px(this.mContext, 16.0f));
        } else {
            int paddingTopBottom = (this.mDragBarHeight - dip2px(this.mContext, 4.0f)) / 2;
            this.mDragBar.setPadding(0, paddingTopBottom, 0, paddingTopBottom);
        }
        if (WindowConfiguration.isHwSplitScreenPrimaryWindowingMode(windowMode)) {
            try {
                showTips(windowMode);
            } catch (InflateException | UnsupportedOperationException e) {
                Log.e(TAG, "get tips fail");
            } catch (Exception e2) {
                Log.e(TAG, "get tips fail");
            }
        } else {
            saveTipStateAfterModeChange();
        }
        this.mWindowMode = windowMode;
    }

    private boolean isUpDownSplit() {
        Bundle bundle = HwActivityTaskManager.getSplitStacksPos(this.mContext.getDisplayId(), 0);
        if (bundle == null) {
            Log.i(TAG, "HwSplitStacksPos bundle is null");
            return true;
        }
        int primaryScreenType = bundle.getInt("primaryPosition");
        Log.i(TAG, "primaryScreenType: " + primaryScreenType);
        if (primaryScreenType == 0) {
            return true;
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mDragBarRect.contains((int) ev.getX(), (int) ev.getY()) && this.mDragBar.getVisibility() == 0) {
            return true;
        }
        if (!this.mTipsRect.contains((int) ev.getX(), (int) ev.getY()) || this.mTips.getVisibility() != 0) {
            return false;
        }
        if (this.mTipsButtonRect.contains((int) ev.getX(), (int) ev.getY())) {
            this.mClickTarget = this.mTipsButton;
        }
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        if (this.mClickTarget != this.mTipsButton) {
            return true;
        }
        Log.d(TAG, "click tipsButton");
        this.mTips.setVisibility(8);
        saveTipState(this.mTipsShownValue, this.mContext);
        this.mTipsShownValue = 0;
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0042, code lost:
        if (r0 != 3) goto L_0x00ba;
     */
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        int posX = (int) event.getX();
        int posY = (int) event.getY();
        if (this.mClickTarget != null) {
            this.mGestureDetector.onTouchEvent(event);
            if (actionMasked == 1 || actionMasked == 3) {
                this.mClickTarget = null;
            }
            return true;
        }
        if (actionMasked == 1 || actionMasked == 3) {
            this.mDragBar.setBackgroundColor(0);
        }
        if (!this.mDragBarRect.contains(posX, posY)) {
            Log.d(TAG, "Touch point not in mDragBarRect");
            return false;
        }
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    Log.d(TAG, "onTouchEvent: MotionEvent.ACTION_MOVE, mHasLongClicked = " + this.mHasLongClicked);
                    if (!this.mHasLongClicked && !passedSlop(posX, posY) && System.currentTimeMillis() - this.mTouchDownTime > ((long) LONG_PRESS_TIME_IN_MS)) {
                        this.mDragBar.setBackgroundColor(0);
                        Log.d(TAG, "long press = " + this.mDragSlop);
                        onLongClick(this.mDragBar, event);
                        this.mHasLongClicked = true;
                        return false;
                    }
                }
            }
            Log.d(TAG, "onTouchEvent: MotionEvent.ACTION_UP");
        } else {
            this.mHasLongClicked = false;
            if (!this.mIsShow) {
                return false;
            }
            Log.d(TAG, "onTouchEvent: MotionEvent.ACTION_DOWN");
            this.mTouchDownX = posX;
            this.mTouchDownY = posY;
            this.mTouchDownTime = System.currentTimeMillis();
            this.mDragBar.setBackground(this.mDragBarBackground);
        }
        return true;
    }

    private boolean passedSlop(int x, int y) {
        return Math.abs(x - this.mTouchDownX) > this.mDragSlop || Math.abs(y - this.mTouchDownY) > this.mDragSlop;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mCaption.getVisibility() != 8) {
            ViewGroup.LayoutParams params = this.mCaption.getLayoutParams();
            if (!this.mIsUpDownSplit) {
                params.height = this.mDragBarHeight;
            } else if (WindowConfiguration.isHwSplitScreenPrimaryWindowingMode(this.mWindowMode)) {
                params.height = this.mDragBarHeight + (IS_NOTCH_PROP ? 0 : this.mStatusBarHeight);
            } else {
                params.height = this.mDragBarHeight + dip2px(this.mContext, 12.0f);
            }
            this.mCaption.setLayoutParams(params);
            measureChildWithMargins(this.mCaption, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        View view = this.mTips;
        if (!(view == null || view.getVisibility() == 8)) {
            measureChildWithMargins(this.mTips, View.MeasureSpec.makeMeasureSpec((int) (((float) View.MeasureSpec.getSize(widthMeasureSpec)) * 0.6666667f), 1073741824), 0, heightMeasureSpec, 0);
        }
        View view2 = this.mContent;
        if (view2 != null) {
            measureChildWithMargins(view2, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        if (this.mCaption.getVisibility() != 8) {
            View view = this.mCaption;
            view.layout(0, 0, view.getMeasuredWidth(), this.mCaption.getMeasuredHeight());
            this.mDragBar.getHitRect(this.mDragBarRect);
        }
        int windowMode = getWindowMode();
        View view2 = this.mTips;
        if (view2 == null || view2.getVisibility() == 8 || !WindowConfiguration.isHwSplitScreenPrimaryWindowingMode(windowMode)) {
            this.mTipsButtonRect.setEmpty();
            this.mTipsRect.setEmpty();
        } else {
            int tipsWidth = this.mTips.getMeasuredWidth();
            int tipsTop = dip2px(this.mContext, 28.0f);
            if (this.mCaption.getMeasuredHeight() > this.mContext.getResources().getDimensionPixelSize(34472526)) {
                tipsTop = dip2px(this.mContext, 24.0f) + this.mStatusBarHeight;
            }
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
            view3.layout(0, 0, view3.getMeasuredWidth(), this.mContent.getMeasuredHeight());
        }
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
        if (this.mTipsShownValue != 0 && WindowConfiguration.isHwSplitScreenPrimaryWindowingMode(this.mWindowMode)) {
            int windowMode = getWindowMode();
            if (!WindowConfiguration.isHwSplitScreenPrimaryWindowingMode(windowMode) && WindowConfiguration.isHwMultiStackWindowingMode(windowMode)) {
                saveTipState(this.mTipsShownValue, this.mContext);
                this.mTipsShownValue = 0;
            }
        }
    }

    private void showTips(int windowMode) {
        this.mTipsShownValue = 0;
        if (this.mTips != null && this.mCaption.getVisibility() == 0 && !WindowConfiguration.isHwSplitScreenSecondaryWindowingMode(this.mWindowMode)) {
            if (!isNeedShowTips(this.mContext, this.mIsUpDownSplit)) {
                this.mTips.setVisibility(8);
                return;
            }
            ViewStub stub = (ViewStub) findViewById(34603264);
            if (!(stub == null || stub.getParent() == null)) {
                stub.inflate();
                Log.d(TAG, "inflate bubble view");
            }
            TextView content1 = (TextView) this.mTips.findViewById(34603398);
            TextView content2 = (TextView) this.mTips.findViewById(34603399);
            content2.setVisibility(0);
            this.mTipsButton = (TextView) this.mTips.findViewById(34603397);
            if (this.mIsUpDownSplit) {
                content1.setText(this.mContext.getString(33685659));
                content2.setText(this.mContext.getString(33685660));
                this.mTipsShownValue = 16;
            } else {
                content1.setText(this.mContext.getString(33685657));
                content2.setText(this.mContext.getString(33685658));
                this.mTipsShownValue = 1;
            }
            handleTipsLayoutDirection(this.mContext);
            Log.i(TAG, "showTips isUpDownSplit " + this.mIsUpDownSplit + " windowMode = " + windowMode);
            this.mTips.setVisibility(0);
        }
    }
}
