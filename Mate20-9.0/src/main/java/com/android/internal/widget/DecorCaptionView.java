package com.android.internal.widget;

import android.app.Activity;
import android.content.Context;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
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
import com.android.internal.policy.DecorView;
import com.android.internal.policy.PhoneWindow;
import com.huawei.android.app.HwActivityManager;
import java.util.ArrayList;

public class DecorCaptionView extends ViewGroup implements View.OnTouchListener, GestureDetector.OnGestureListener {
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
        this.mMaximize = findViewById(16909070);
        this.mClose = findViewById(16908823);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            if (this.mMaximizeRect.contains(x, y)) {
                if (HwFreeFormUtils.isFreeFormEnable() && this.mMaximize != null) {
                    this.mMaximize.setBackgroundResource(33751976);
                }
                this.mClickTarget = this.mMaximize;
            }
            if (this.mCloseRect.contains(x, y)) {
                if (HwFreeFormUtils.isFreeFormEnable() && this.mClose != null) {
                    this.mClose.setBackgroundResource(33751973);
                }
                this.mClickTarget = this.mClose;
            }
        }
        return this.mClickTarget != null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if ((action == 1 || action == 3) && HwFreeFormUtils.isFreeFormEnable()) {
            if (this.mMaximize != null) {
                this.mMaximize.setBackgroundResource(33751975);
            }
            if (this.mClose != null) {
                this.mClose.setBackgroundResource(33751972);
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

    public boolean onTouch(View v, MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        boolean z = false;
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
                    startMovingTask(e.getRawX(), e.getRawY());
                    break;
                }
        }
        if (this.mDragging || this.mCheckForDragging) {
            z = true;
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
            super.addView(child, 0, params);
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
        } else {
            captionHeight = 0;
            this.mMaximizeRect.setEmpty();
            this.mCloseRect.setEmpty();
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

    private boolean isFillingScreen() {
        return ((getWindowSystemUiVisibility() | getSystemUiVisibility()) & 2565) != 0;
    }

    private void updateCaptionVisibility() {
        int i = 0;
        boolean invisible = isFillingScreen() || !this.mShow;
        Window.WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null && HwFreeFormUtils.isFreeFormEnable() && (callback instanceof Activity) && HwActivityManager.getCaptionState(((Activity) callback).getActivityToken()) == 8 && this.mContext.getResources().getConfiguration().orientation == 1) {
            invisible = true;
            ((DecorView) this.mOwner.getDecorView()).setHideFreeFormForeground();
        }
        View view = this.mCaption;
        if (invisible) {
            i = 8;
        }
        view.setVisibility(i);
        this.mCaption.setOnTouchListener(this);
    }

    private void maximizeWindow() {
        Window.WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                if (HwPCUtils.isValidExtDisplayId(this.mContext)) {
                    HwPCUtils.log(TAG, "use google decorCaptionView so error");
                }
                if (HwFreeFormUtils.isFreeFormEnable() && getContext().getPackageName() != null && "com.tencent.mm".equals(getContext().getPackageName())) {
                    HwFreeFormUtils.setHideAnimator(false);
                }
                callback.exitFreeformMode();
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

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        if (this.mClickTarget == this.mMaximize) {
            maximizeWindow();
        } else if (this.mClickTarget == this.mClose) {
            this.mOwner.dispatchOnWindowDismissed(true, false);
        }
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
