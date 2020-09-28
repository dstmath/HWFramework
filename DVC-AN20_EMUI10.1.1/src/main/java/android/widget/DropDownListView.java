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
    private Context mContext;
    private boolean mDrawsInPressedState;
    private boolean mHijackFocus;
    private boolean mIsForSpinner;
    private boolean mIsHwEmphasizeTheme;
    private int mListBottom;
    private boolean mListSelectionHidden;
    private int mListTop;
    private boolean mNeedAdjustSelector;
    Rect mRectSeletor;
    private ResolveHoverRunnable mResolveHoverRunnable;
    private AutoScrollHelper.AbsListViewAutoScroller mScrollHelper;

    public DropDownListView(Context context, boolean hijackFocus) {
        this(context, hijackFocus, 16842861);
    }

    public DropDownListView(Context context, boolean hijackFocus, int defStyleAttr) {
        super(context, null, defStyleAttr);
        this.mRectSeletor = new Rect();
        this.mNeedAdjustSelector = false;
        this.mIsForSpinner = false;
        this.mContext = context;
        this.mHijackFocus = hijackFocus;
        setCacheColorHint(0);
        this.mIsHwEmphasizeTheme = HwWidgetFactory.isHwEmphasizeTheme(context);
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsListView
    public boolean shouldShowSelector() {
        return isHovered() || super.shouldShowSelector();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View, android.widget.ListView
    public void dispatchDraw(Canvas canvas) {
        drawSelectorDrwable(canvas);
        super.dispatchDraw(canvas);
    }

    private void drawSelectorDrwable(Canvas canvas) {
        View childView;
        Drawable selectorDrawable;
        int childCount = getChildCount();
        int selectedPosition = getCheckedItemPosition();
        Rect rect = this.mRectSeletor;
        this.mListTop = 0;
        this.mListBottom = getHeight();
        for (int i = 0; i < childCount; i++) {
            if (selectedPosition == getFirstVisiblePosition() + i && (selectorDrawable = getSelectorDrawable(selectedPosition, (childView = getChildAt(i)))) != null) {
                int top = childView.getTop();
                int i2 = this.mListTop;
                if (top >= i2) {
                    i2 = childView.getTop();
                }
                int top2 = i2;
                int bottom = childView.getBottom();
                int i3 = this.mListBottom;
                if (bottom <= i3) {
                    i3 = childView.getBottom();
                }
                int bottom2 = i3;
                if (getScrollY() > 0 && childView.getTop() - getScrollY() < this.mListTop) {
                    top2 = getScrollY() + this.mListTop;
                }
                if (getScrollY() < 0 && childView.getBottom() - getScrollY() > this.mListBottom) {
                    bottom2 = getScrollY() + this.mListBottom;
                }
                rect.top = top2;
                rect.bottom = bottom2;
                rect.left = 0;
                rect.right = getWidth();
                selectorDrawable.setBounds(rect);
                selectorDrawable.draw(canvas);
                rect.setEmpty();
            }
        }
    }

    private Drawable getSelectorDrawable(int selectedPosition, View childView) {
        if (getChildCount() == 1) {
            return getResources().getDrawable(33751793, this.mContext.getTheme());
        }
        if (selectedPosition == getFirstVisiblePosition()) {
            return getResources().getDrawable(33751790, this.mContext.getTheme());
        }
        if (selectedPosition == getLastVisiblePosition()) {
            return getResources().getDrawable(33751791, this.mContext.getTheme());
        }
        if (getScrollY() > 0 && childView.getTop() - getScrollY() < this.mListTop) {
            return getResources().getDrawable(33751790, this.mContext.getTheme());
        }
        if (getScrollY() >= 0 || childView.getBottom() - getScrollY() <= this.mListBottom) {
            return getResources().getDrawable(33751802, this.mContext.getTheme());
        }
        return getResources().getDrawable(33751791, this.mContext.getTheme());
    }

    @Override // android.widget.HwAbsListView, android.widget.AbsListView, android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        ResolveHoverRunnable resolveHoverRunnable = this.mResolveHoverRunnable;
        if (resolveHoverRunnable != null) {
            resolveHoverRunnable.cancel();
        }
        return super.onTouchEvent(ev);
    }

    @Override // android.view.View
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
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View
    public void drawableStateChanged() {
        if (this.mResolveHoverRunnable == null) {
            super.drawableStateChanged();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0066  */
    public boolean onForwardedEvent(MotionEvent event, int activePointerId) {
        boolean handledEvent = true;
        boolean clearPressedItem = false;
        int actionMasked = event.getActionMasked();
        if (actionMasked == 1) {
            handledEvent = false;
        } else if (actionMasked != 2) {
            if (actionMasked == 3) {
                handledEvent = false;
            }
            if (!handledEvent || clearPressedItem) {
                clearPressedItem();
            }
            if (!handledEvent) {
                if (this.mScrollHelper == null) {
                    this.mScrollHelper = new AutoScrollHelper.AbsListViewAutoScroller(this);
                }
                this.mScrollHelper.setEnabled(true);
                this.mScrollHelper.onTouch(this, event);
            } else {
                AutoScrollHelper.AbsListViewAutoScroller absListViewAutoScroller = this.mScrollHelper;
                if (absListViewAutoScroller != null) {
                    absListViewAutoScroller.setEnabled(false);
                }
            }
            return handledEvent;
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
        clearPressedItem();
        if (!handledEvent) {
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
    @Override // android.widget.AbsListView
    public boolean touchModeDrawsInPressedState() {
        return this.mDrawsInPressedState || super.touchModeDrawsInPressedState();
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.HwAbsListView, android.widget.AbsListView
    public View obtainView(int position, boolean[] isScrap) {
        View view = super.obtainView(position, isScrap);
        if ((view instanceof TextView) && !this.mIsForSpinner) {
            ((TextView) view).setHorizontallyScrolling(true);
        }
        return view;
    }

    @Override // android.view.View
    public boolean isInTouchMode() {
        return (this.mHijackFocus && this.mListSelectionHidden) || super.isInTouchMode();
    }

    @Override // android.view.View
    public boolean hasWindowFocus() {
        return this.mHijackFocus || super.hasWindowFocus();
    }

    @Override // android.view.View
    public boolean isFocused() {
        return this.mHijackFocus || super.isFocused();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean hasFocus() {
        return this.mHijackFocus || super.hasFocus();
    }

    /* access modifiers changed from: private */
    public class ResolveHoverRunnable implements Runnable {
        private ResolveHoverRunnable() {
        }

        public void run() {
            DropDownListView.this.mResolveHoverRunnable = null;
            DropDownListView.this.drawableStateChanged();
        }

        public void cancel() {
            DropDownListView.this.mResolveHoverRunnable = null;
            DropDownListView.this.removeCallbacks(this);
        }

        public void post() {
            DropDownListView.this.post(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
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
            } else {
                Drawable drawable = this.mCacheSelector;
                if (drawable != null) {
                    setSelector(drawable);
                }
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
