package com.android.internal.widget;

import android.app.Activity;
import android.content.Context;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.R;
import com.android.internal.policy.DecorView;
import com.android.internal.policy.PhoneWindow;
import com.huawei.android.app.HwActivityTaskManager;
import java.util.ArrayList;

public class DecorCaptionView extends ViewGroup implements View.OnTouchListener, GestureDetector.OnGestureListener {
    private static final int MAX_HW_CAPTION_VIEW_CHILD_COUNT = 4;
    private static final String TAG = "DecorCaptionView";
    private View mCaption;
    private boolean mCheckForDragging;
    private View mClickTarget;
    private View mClose;
    private final Rect mCloseRect = new Rect();
    private View mContent;
    private Context mContext;
    private int mDragSlop;
    private boolean mDragging = false;
    private GestureDetector mGestureDetector;
    private View mMaximize;
    private final Rect mMaximizeRect = new Rect();
    private boolean mOverlayWithAppContent = false;
    private PhoneWindow mOwner = null;
    private int mRootScrollY;
    private boolean mShow = false;
    private ArrayList<View> mTouchDispatchList = new ArrayList<>(2);
    private int mTouchDownX;
    private int mTouchDownY;

    public DecorCaptionView(Context context) {
        super(context);
        init(context);
    }

    public DecorCaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DecorCaptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mGestureDetector = new GestureDetector(context, this);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCaption = getChildAt(0);
    }

    public void setPhoneWindow(PhoneWindow owner, boolean show) {
        this.mOwner = owner;
        this.mShow = show;
        this.mOverlayWithAppContent = owner.isOverlayWithDecorCaptionEnabled();
        if (this.mOverlayWithAppContent) {
            this.mCaption.setBackgroundColor(0);
        }
        updateCaptionVisibility();
        if (HwFreeFormUtils.isFreeFormEnable()) {
            DecorView dv = (DecorView) this.mOwner.getDecorView();
            dv.setOutlineProvider(ViewOutlineProvider.HW_FREEFORM_OUTLINE_PROVIDER);
            dv.setClipToOutline(true);
            dv.setElevation(0.0f);
            setOutlineProvider(ViewOutlineProvider.HW_FREEFORM_OUTLINE_PROVIDER);
            setClipToOutline(true);
            setElevation(0.0f);
            this.mMaximize = findViewById(34603185);
            this.mClose = findViewById(34603187);
            return;
        }
        this.mOwner.getDecorView().setOutlineProvider(ViewOutlineProvider.BOUNDS);
        this.mMaximize = findViewById(R.id.maximize_window);
        this.mClose = findViewById(R.id.close_window);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        View view;
        View view2;
        if (ev.getAction() == 0) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            if (this.mMaximizeRect.contains(x, y - this.mRootScrollY)) {
                if (HwFreeFormUtils.isFreeFormEnable() && (view2 = this.mMaximize) != null) {
                    view2.setBackgroundResource(com.android.hwext.internal.R.drawable.hw_freeform_decor_maximize_button_dark_press);
                }
                this.mClickTarget = this.mMaximize;
            }
            if (this.mCloseRect.contains(x, y - this.mRootScrollY)) {
                if (HwFreeFormUtils.isFreeFormEnable() && (view = this.mClose) != null) {
                    view.setBackgroundResource(com.android.hwext.internal.R.drawable.hw_freeform_decor_close_button_dark_press);
                }
                this.mClickTarget = this.mClose;
            }
        }
        return this.mClickTarget != null;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if ((action == 1 || action == 3) && HwFreeFormUtils.isFreeFormEnable()) {
            View view = this.mMaximize;
            if (view != null) {
                view.setBackgroundResource(com.android.hwext.internal.R.drawable.hw_freeform_decor_maximize_button_dark);
            }
            View view2 = this.mClose;
            if (view2 != null) {
                view2.setBackgroundResource(com.android.hwext.internal.R.drawable.hw_freeform_decor_close_button_dark);
            }
        }
        if (this.mClickTarget == null) {
            return false;
        }
        this.mGestureDetector.onTouchEvent(event);
        int action2 = event.getAction();
        if (action2 == 1 || action2 == 3) {
            this.mClickTarget = null;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002f, code lost:
        if (r7 != 3) goto L_0x0071;
     */
    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View v, MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        boolean fromMouse = e.getToolType(e.getActionIndex()) == 3;
        boolean primaryButton = (e.getButtonState() & 1) != 0;
        int actionMasked = e.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (!this.mDragging && this.mCheckForDragging && (fromMouse || passedSlop(x, y))) {
                        this.mCheckForDragging = false;
                        this.mDragging = true;
                        startMovingTask(e.getRawX(), e.getRawY());
                    }
                }
            }
            if (this.mDragging) {
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
                this.mCheckForDragging = true;
                this.mTouchDownX = x;
                this.mTouchDownY = y;
            }
        }
        return this.mDragging || this.mCheckForDragging;
    }

    @Override // android.view.ViewGroup
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

    @Override // android.view.ViewGroup
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

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(params instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException("params " + params + " must subclass MarginLayoutParams");
        } else if (index >= 2 || getChildCount() >= 2) {
            throw new IllegalStateException("DecorCaptionView can only handle 1 client view");
        } else {
            super.addView(child, 0, params);
            this.mContent = child;
        }
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params, int childNum) {
        if (!(params instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException("params " + params + " must subclass MarginLayoutParams");
        } else if (index >= childNum || getChildCount() >= childNum) {
            throw new IllegalStateException("DecorCaptionView can only handle 1 client view, childNum: " + childNum);
        } else {
            super.addView(child, 0, params);
            this.mContent = child;
        }
    }

    @Override // android.view.ViewGroup
    public void attachHwViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        if (!(params instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException("params " + params + " must subclass MarginLayoutParams");
        } else if (index >= 4 || getChildCount() >= 4) {
            throw new IllegalStateException("DecorCaptionView can only handle 1 client view");
        } else {
            super.attachViewToParent(child, 0, params);
            this.mContent = child;
        }
    }

    @Override // android.view.ViewGroup
    public void detachHwViewFromParent(View child) {
        super.detachHwViewFromParent(child);
        if (this.mContent == child) {
            this.mContent = null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
            if (this.mOverlayWithAppContent) {
                measureChildWithMargins(view, widthMeasureSpec, 0, heightMeasureSpec, 0);
            } else {
                measureChildWithMargins(view, widthMeasureSpec, 0, heightMeasureSpec, captionHeight);
            }
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
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
        View view2 = this.mContent;
        if (view2 != null) {
            if (this.mOverlayWithAppContent) {
                view2.layout(0, 0, view2.getMeasuredWidth(), this.mContent.getMeasuredHeight());
            } else {
                view2.layout(0, captionHeight, view2.getMeasuredWidth(), this.mContent.getMeasuredHeight() + captionHeight);
            }
        }
        this.mOwner.notifyRestrictedCaptionAreaCallback(this.mMaximize.getLeft(), this.mMaximize.getTop(), this.mClose.getRight(), this.mClose.getBottom());
    }

    private void updateCaptionVisibility() {
        DecorView dv;
        Window.WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        int i = 0;
        if ((callback != null && HwFreeFormUtils.isFreeFormEnable() && (callback instanceof Activity)) && HwActivityTaskManager.getCaptionState(((Activity) callback).getActivityToken()) == 8 && this.mContext.getResources().getConfiguration().orientation == 1) {
            this.mShow = false;
            ((DecorView) this.mOwner.getDecorView()).setHideFreeFormForeground();
        }
        View view = this.mCaption;
        if (!this.mShow) {
            i = 8;
        }
        view.setVisibility(i);
        if (HwFreeFormUtils.isFreeFormEnable() && (this.mOwner.getDecorView() instanceof DecorView) && (dv = (DecorView) this.mOwner.getDecorView()) != null) {
            ViewOutlineProvider viewOutlineProvider = null;
            if (this.mShow) {
                dv.setWindowFrame(null);
            } else {
                dv.setWindowFrameForced(null);
            }
            dv.setOutlineProvider(this.mShow ? ViewOutlineProvider.HW_FREEFORM_OUTLINE_PROVIDER : null);
            dv.setClipToOutline(true);
            if (this.mShow) {
                viewOutlineProvider = ViewOutlineProvider.HW_FREEFORM_OUTLINE_PROVIDER;
            }
            setOutlineProvider(viewOutlineProvider);
            setClipToOutline(true);
        }
        this.mCaption.setOnTouchListener(this);
    }

    private void toggleFreeformWindowingMode() {
        Window.WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                if (HwPCUtils.isValidExtDisplayId(this.mContext)) {
                    HwPCUtils.log(TAG, "use google decorCaptionView so error");
                }
                callback.toggleFreeformWindowingMode();
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot change task workspace.");
            }
        }
        if (HwFreeFormUtils.isFreeFormEnable()) {
            ((DecorView) this.mOwner.getDecorView()).setWindowFrameForced(null);
        }
    }

    public boolean isCaptionShowing() {
        return this.mShow;
    }

    public int getCaptionHeight() {
        View view = this.mCaption;
        if (view != null) {
            return view.getHeight();
        }
        return 0;
    }

    public void removeContentView() {
        View view = this.mContent;
        if (view != null) {
            removeView(view);
            this.mContent = null;
        }
    }

    public View getCaption() {
        return this.mCaption;
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.MarginLayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.MarginLayoutParams(-1, -1);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new ViewGroup.MarginLayoutParams(p);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof ViewGroup.MarginLayoutParams;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(MotionEvent e) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(MotionEvent e) {
        View view = this.mClickTarget;
        if (view == this.mMaximize) {
            toggleFreeformWindowingMode();
        } else if (view == this.mClose) {
            InputMethodManager.peekInstance().hideSoftInputFromWindow(this.mCaption.getWindowToken(), 2);
            this.mOwner.dispatchOnWindowDismissed(true, false);
        }
        return true;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent e) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public void onRootViewScrollYChanged(int scrollY) {
        View view = this.mCaption;
        if (view != null) {
            this.mRootScrollY = scrollY;
            view.setTranslationY((float) scrollY);
        }
    }

    public void onNotifyHwMultiWindowCaption(Bundle info) {
    }

    public void startDragBarAnim() {
    }

    public void updatePopup(boolean isImmediateDismiss, boolean hasFocus) {
    }
}
