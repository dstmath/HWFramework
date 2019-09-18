package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.widget.AutoScrollHelper;

public class DropDownListView extends ListView {
    private Drawable mCacheSelector;
    private boolean mDrawsInPressedState;
    private boolean mHijackFocus;
    private boolean mIsForSpinner;
    private boolean mIsHwEmphasizeTheme;
    private int mListBottom;
    private boolean mListSelectionHidden;
    private int mListTop;
    private boolean mNeedAdjustSelector;
    Rect mRectSeletor;
    /* access modifiers changed from: private */
    public ResolveHoverRunnable mResolveHoverRunnable;
    private AutoScrollHelper.AbsListViewAutoScroller mScrollHelper;

    private class ResolveHoverRunnable implements Runnable {
        private ResolveHoverRunnable() {
        }

        public void run() {
            ResolveHoverRunnable unused = DropDownListView.this.mResolveHoverRunnable = null;
            DropDownListView.this.drawableStateChanged();
        }

        public void cancel() {
            ResolveHoverRunnable unused = DropDownListView.this.mResolveHoverRunnable = null;
            DropDownListView.this.removeCallbacks(this);
        }

        public void post() {
            DropDownListView.this.post(this);
        }
    }

    public DropDownListView(Context context, boolean hijackFocus) {
        this(context, hijackFocus, 16842861);
    }

    public DropDownListView(Context context, boolean hijackFocus, int defStyleAttr) {
        super(context, null, defStyleAttr);
        this.mRectSeletor = new Rect();
        this.mNeedAdjustSelector = false;
        this.mIsForSpinner = false;
        this.mHijackFocus = hijackFocus;
        setCacheColorHint(0);
        this.mIsHwEmphasizeTheme = HwWidgetFactory.isHwEmphasizeTheme(context);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowSelector() {
        return isHovered() || super.shouldShowSelector();
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        drawSelectorDrwable(canvas);
        super.dispatchDraw(canvas);
    }

    private void drawSelectorDrwable(Canvas canvas) {
        int childCount = getChildCount();
        int selectedPosition = getCheckedItemPosition();
        Rect rect = this.mRectSeletor;
        this.mListTop = 0;
        this.mListBottom = getHeight();
        for (int i = 0; i < childCount; i++) {
            if (selectedPosition == getFirstVisiblePosition() + i) {
                View childView = getChildAt(i);
                Drawable selectorDrawable = getSelectorDrawable(selectedPosition, childView);
                if (selectorDrawable != null) {
                    int top = childView.getTop() >= this.mListTop ? childView.getTop() : this.mListTop;
                    int bottom = childView.getBottom() <= this.mListBottom ? childView.getBottom() : this.mListBottom;
                    if (getScrollY() > 0 && childView.getTop() - getScrollY() < this.mListTop) {
                        top = getScrollY() + this.mListTop;
                    }
                    if (getScrollY() < 0 && childView.getBottom() - getScrollY() > this.mListBottom) {
                        bottom = getScrollY() + this.mListBottom;
                    }
                    rect.top = top;
                    rect.bottom = bottom;
                    rect.left = 0;
                    rect.right = getWidth();
                    selectorDrawable.setBounds(rect);
                    selectorDrawable.draw(canvas);
                    rect.setEmpty();
                }
            }
        }
    }

    private Drawable getSelectorDrawable(int selectedPosition, View childView) {
        if (getChildCount() == 1) {
            return getResources().getDrawable(33751793, null);
        }
        if (selectedPosition == getFirstVisiblePosition()) {
            return getResources().getDrawable(33751790, null);
        }
        if (selectedPosition == getLastVisiblePosition()) {
            return getResources().getDrawable(33751791, null);
        }
        if (getScrollY() > 0 && childView.getTop() - getScrollY() < this.mListTop) {
            return getResources().getDrawable(33751790, null);
        }
        if (getScrollY() >= 0 || childView.getBottom() - getScrollY() <= this.mListBottom) {
            return getResources().getDrawable(33751802, null);
        }
        return getResources().getDrawable(33751791, null);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mResolveHoverRunnable != null) {
            this.mResolveHoverRunnable.cancel();
        }
        return super.onTouchEvent(ev);
    }

    public boolean onHoverEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == 10 && this.mResolveHoverRunnable == null) {
            this.mResolveHoverRunnable = new ResolveHoverRunnable();
            this.mResolveHoverRunnable.post();
        }
        boolean handled = super.onHoverEvent(ev);
        if (action == 9 || action == 7) {
            int position = pointToPosition((int) ev.getX(), (int) ev.getY());
            if (!(position == -1 || position == this.mSelectedPosition)) {
                View hoveredItem = getChildAt(position - getFirstVisiblePosition());
                if (hoveredItem.isEnabled()) {
                    requestFocus();
                    positionSelector(position, hoveredItem);
                    setSelectedPositionInt(position);
                    setNextSelectedPositionInt(position);
                }
                updateSelectorState();
            }
        } else if (!super.shouldShowSelector()) {
            setSelectedPositionInt(-1);
            setNextSelectedPositionInt(-1);
        }
        return handled;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        if (this.mResolveHoverRunnable == null) {
            super.drawableStateChanged();
        }
    }

    public boolean onForwardedEvent(MotionEvent event, int activePointerId) {
        boolean handledEvent = true;
        boolean clearPressedItem = false;
        int actionMasked = event.getActionMasked();
        switch (actionMasked) {
            case 1:
                handledEvent = false;
                break;
            case 2:
                break;
            case 3:
                handledEvent = false;
                break;
        }
        int activeIndex = event.findPointerIndex(activePointerId);
        if (activeIndex < 0) {
            handledEvent = false;
        } else {
            int x = (int) event.getX(activeIndex);
            int y = (int) event.getY(activeIndex);
            int position = pointToPosition(x, y);
            if (position == -1) {
                clearPressedItem = true;
            } else {
                View child = getChildAt(position - getFirstVisiblePosition());
                setPressedItem(child, position, (float) x, (float) y);
                handledEvent = true;
                if (actionMasked == 1) {
                    performItemClick(child, position, getItemIdAtPosition(position));
                }
            }
        }
        if (!handledEvent || clearPressedItem) {
            clearPressedItem();
        }
        if (handledEvent) {
            if (this.mScrollHelper == null) {
                this.mScrollHelper = new AutoScrollHelper.AbsListViewAutoScroller(this);
            }
            this.mScrollHelper.setEnabled(true);
            this.mScrollHelper.onTouch(this, event);
        } else if (this.mScrollHelper != null) {
            this.mScrollHelper.setEnabled(false);
        }
        return handledEvent;
    }

    public void setListSelectionHidden(boolean hideListSelection) {
        this.mListSelectionHidden = hideListSelection;
    }

    private void clearPressedItem() {
        this.mDrawsInPressedState = false;
        setPressed(false);
        updateSelectorState();
        View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
        if (motionView != null) {
            motionView.setPressed(false);
        }
    }

    private void setPressedItem(View child, int position, float x, float y) {
        this.mDrawsInPressedState = true;
        drawableHotspotChanged(x, y);
        if (!isPressed()) {
            setPressed(true);
        }
        if (this.mDataChanged) {
            layoutChildren();
        }
        View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
        if (!(motionView == null || motionView == child || !motionView.isPressed())) {
            motionView.setPressed(false);
        }
        this.mMotionPosition = position;
        child.drawableHotspotChanged(x - ((float) child.getLeft()), y - ((float) child.getTop()));
        if (!child.isPressed()) {
            child.setPressed(true);
        }
        setSelectedPositionInt(position);
        positionSelectorLikeTouch(position, child, x, y);
        refreshDrawableState();
    }

    /* access modifiers changed from: package-private */
    public boolean touchModeDrawsInPressedState() {
        return this.mDrawsInPressedState || super.touchModeDrawsInPressedState();
    }

    /* access modifiers changed from: package-private */
    public View obtainView(int position, boolean[] isScrap) {
        View view = super.obtainView(position, isScrap);
        if ((view instanceof TextView) && !this.mIsForSpinner) {
            ((TextView) view).setHorizontallyScrolling(true);
        }
        return view;
    }

    public boolean isInTouchMode() {
        return (this.mHijackFocus && this.mListSelectionHidden) || super.isInTouchMode();
    }

    public boolean hasWindowFocus() {
        return this.mHijackFocus || super.hasWindowFocus();
    }

    public boolean isFocused() {
        return this.mHijackFocus || super.isFocused();
    }

    public boolean hasFocus() {
        return this.mHijackFocus || super.hasFocus();
    }

    /* access modifiers changed from: protected */
    public Drawable initializeVariousScrollIndicators(Context context) {
        return super.initializeVariousScrollIndicators(context);
    }

    /* access modifiers changed from: protected */
    public boolean reSizeScrollIndicators(int[] xy) {
        return super.reSizeScrollIndicators(xy);
    }

    /* access modifiers changed from: protected */
    public void adjustSelector(int pos, Rect rect) {
        int i;
        int i2;
        int i3;
        if (this.mNeedAdjustSelector) {
            if (this.mCacheSelector == null) {
                this.mCacheSelector = getSelector();
            }
            if (rect.top < 0) {
                rect.top = 0;
            }
            if (rect.bottom > getHeight()) {
                rect.bottom = getHeight();
            }
            if (rect.left < 0) {
                rect.left = 0;
            }
            rect.right = getWidth();
            int fvp = getFirstVisiblePosition();
            int lvp = getLastVisiblePosition();
            if (fvp == lvp) {
                if (this.mIsHwEmphasizeTheme) {
                    i3 = 33751274;
                } else {
                    i3 = 33751273;
                }
                setSelector(i3);
            } else if (fvp == pos) {
                if (this.mIsHwEmphasizeTheme) {
                    i2 = 33751276;
                } else {
                    i2 = 33751275;
                }
                setSelector(i2);
            } else if (lvp == pos) {
                if (this.mIsHwEmphasizeTheme) {
                    i = 33751270;
                } else {
                    i = 33751269;
                }
                setSelector(i);
            } else if (this.mCacheSelector != null) {
                setSelector(this.mCacheSelector);
            }
        }
    }

    public void setNeedAdjustSelector(boolean flag) {
        this.mNeedAdjustSelector = flag;
    }

    public void setSpinner(boolean isForSpinner) {
        this.mIsForSpinner = isForSpinner;
    }
}
