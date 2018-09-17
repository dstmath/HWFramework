package com.android.internal.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewOutlineProvider;
import android.view.Window.WindowControllerCallback;
import com.android.internal.R;
import com.android.internal.policy.PhoneWindow;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;

public class DecorCaptionView extends ViewGroup implements OnTouchListener, OnGestureListener {
    private static final String TAG = "DecorCaptionView";
    private View mCaption;
    private boolean mCheckForDragging;
    private View mClickTarget;
    private View mClose;
    private final Rect mCloseRect;
    private View mContent;
    private int mDragSlop;
    private boolean mDragging;
    private GestureDetector mGestureDetector;
    private boolean mLeftMouseButtonReleased;
    private View mMaximize;
    private final Rect mMaximizeRect;
    private boolean mOverlayWithAppContent;
    private PhoneWindow mOwner;
    private boolean mShow;
    private ArrayList<View> mTouchDispatchList;
    private int mTouchDownX;
    private int mTouchDownY;

    public DecorCaptionView(Context context) {
        super(context);
        this.mOwner = null;
        this.mShow = false;
        this.mDragging = false;
        this.mOverlayWithAppContent = false;
        this.mTouchDispatchList = new ArrayList(2);
        this.mCloseRect = new Rect();
        this.mMaximizeRect = new Rect();
        init(context);
    }

    public DecorCaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOwner = null;
        this.mShow = false;
        this.mDragging = false;
        this.mOverlayWithAppContent = false;
        this.mTouchDispatchList = new ArrayList(2);
        this.mCloseRect = new Rect();
        this.mMaximizeRect = new Rect();
        init(context);
    }

    public DecorCaptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mOwner = null;
        this.mShow = false;
        this.mDragging = false;
        this.mOverlayWithAppContent = false;
        this.mTouchDispatchList = new ArrayList(2);
        this.mCloseRect = new Rect();
        this.mMaximizeRect = new Rect();
        init(context);
    }

    private void init(Context context) {
        this.mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mGestureDetector = new GestureDetector(context, (OnGestureListener) this);
    }

    protected void onFinishInflate() {
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
        this.mOwner.getDecorView().setOutlineProvider(ViewOutlineProvider.BOUNDS);
        this.mMaximize = findViewById(R.id.maximize_window);
        this.mClose = findViewById(R.id.close_window);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            if (this.mMaximizeRect.contains(x, y)) {
                this.mClickTarget = this.mMaximize;
            }
            if (this.mCloseRect.contains(x, y)) {
                this.mClickTarget = this.mClose;
            }
        }
        if (this.mClickTarget != null) {
            return true;
        }
        return false;
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

    public boolean onTouch(View v, MotionEvent e) {
        boolean z = true;
        boolean z2 = false;
        int x = (int) e.getX();
        int y = (int) e.getY();
        switch (e.getActionMasked()) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                if (this.mShow) {
                    if (!(e.getToolType(e.getActionIndex()) == 3 && (e.getButtonState() & 1) == 0)) {
                        this.mCheckForDragging = true;
                        this.mTouchDownX = x;
                        this.mTouchDownY = y;
                        break;
                    }
                }
                return false;
            case HwCfgFilePolicy.EMUI /*1*/:
            case HwCfgFilePolicy.BASE /*3*/:
                if (this.mDragging) {
                    this.mDragging = false;
                    if (!this.mCheckForDragging) {
                        z2 = true;
                    }
                    return z2;
                }
                break;
            case HwCfgFilePolicy.PC /*2*/:
                if (this.mDragging || !this.mCheckForDragging || !passedSlop(x, y)) {
                    if (this.mDragging && !this.mLeftMouseButtonReleased && e.getToolType(e.getActionIndex()) == 3 && (e.getButtonState() & 1) == 0) {
                        this.mLeftMouseButtonReleased = true;
                        break;
                    }
                }
                this.mCheckForDragging = false;
                this.mDragging = true;
                this.mLeftMouseButtonReleased = false;
                startMovingTask(e.getRawX(), e.getRawY());
                break;
                break;
        }
        if (!this.mDragging) {
            z = this.mCheckForDragging;
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

    private boolean passedSlop(int x, int y) {
        return Math.abs(x - this.mTouchDownX) > this.mDragSlop || Math.abs(y - this.mTouchDownY) > this.mDragSlop;
    }

    public void onConfigurationChanged(boolean show) {
        this.mShow = show;
        updateCaptionVisibility();
    }

    public void addView(View child, int index, LayoutParams params) {
        if (!(params instanceof MarginLayoutParams)) {
            throw new IllegalArgumentException("params " + params + " must subclass MarginLayoutParams");
        } else if (index >= 2 || getChildCount() >= 2) {
            throw new IllegalStateException("DecorCaptionView can only handle 1 client view");
        } else {
            super.addView(child, 0, params);
            this.mContent = child;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8) {
            measureChildWithMargins(this.mCaption, widthMeasureSpec, 0, heightMeasureSpec, 0);
            captionHeight = this.mCaption.getMeasuredHeight();
        } else {
            captionHeight = 0;
        }
        if (this.mContent != null) {
            if (this.mOverlayWithAppContent) {
                measureChildWithMargins(this.mContent, widthMeasureSpec, 0, heightMeasureSpec, 0);
            } else {
                measureChildWithMargins(this.mContent, widthMeasureSpec, 0, heightMeasureSpec, captionHeight);
            }
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
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
        boolean invisible = isFillingScreen() || !this.mShow;
        this.mCaption.setVisibility(invisible ? 8 : 0);
        this.mCaption.setOnTouchListener(this);
    }

    private void maximizeWindow() {
        WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                callback.exitFreeformMode();
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot change task workspace.");
            }
        }
    }

    public boolean isCaptionShowing() {
        return this.mShow;
    }

    public int getCaptionHeight() {
        return this.mCaption != null ? this.mCaption.getHeight() : 0;
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

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(-1, -1);
    }

    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
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
            this.mOwner.dispatchOnWindowDismissed(true);
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
