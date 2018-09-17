package android.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.R;
import com.android.internal.widget.AutoScrollHelper.AbsListViewAutoScroller;
import huawei.cust.HwCfgFilePolicy;

public class DropDownListView extends ListView {
    private Drawable mCacheSelector;
    private boolean mDrawsInPressedState;
    private boolean mHijackFocus;
    private boolean mListSelectionHidden;
    private boolean mNeedAdjustSelector;
    private ResolveHoverRunnable mResolveHoverRunnable;
    private AbsListViewAutoScroller mScrollHelper;

    private class ResolveHoverRunnable implements Runnable {
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

    public DropDownListView(Context context, boolean hijackFocus) {
        this(context, hijackFocus, R.attr.dropDownListViewStyle);
    }

    public DropDownListView(Context context, boolean hijackFocus, int defStyleAttr) {
        super(context, null, defStyleAttr);
        this.mNeedAdjustSelector = false;
        this.mHijackFocus = hijackFocus;
        setCacheColorHint(0);
    }

    boolean shouldShowSelector() {
        return !isHovered() ? super.shouldShowSelector() : true;
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

    protected void drawableStateChanged() {
        if (this.mResolveHoverRunnable == null) {
            super.drawableStateChanged();
        }
    }

    public boolean onForwardedEvent(MotionEvent event, int activePointerId) {
        boolean handledEvent = true;
        boolean clearPressedItem = false;
        int actionMasked = event.getActionMasked();
        switch (actionMasked) {
            case HwCfgFilePolicy.EMUI /*1*/:
                handledEvent = false;
                break;
            case HwCfgFilePolicy.PC /*2*/:
                break;
            case HwCfgFilePolicy.BASE /*3*/:
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
                this.mScrollHelper = new AbsListViewAutoScroller(this);
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

    boolean touchModeDrawsInPressedState() {
        return !this.mDrawsInPressedState ? super.touchModeDrawsInPressedState() : true;
    }

    View obtainView(int position, boolean[] isScrap) {
        View view = super.obtainView(position, isScrap);
        if (view instanceof TextView) {
            ((TextView) view).setHorizontallyScrolling(true);
        }
        return view;
    }

    public boolean isInTouchMode() {
        return (this.mHijackFocus && this.mListSelectionHidden) ? true : super.isInTouchMode();
    }

    public boolean hasWindowFocus() {
        return !this.mHijackFocus ? super.hasWindowFocus() : true;
    }

    public boolean isFocused() {
        return !this.mHijackFocus ? super.isFocused() : true;
    }

    public boolean hasFocus() {
        return !this.mHijackFocus ? super.hasFocus() : true;
    }

    protected Drawable initializeVariousScrollIndicators(Context context) {
        return super.initializeVariousScrollIndicators(context);
    }

    protected boolean reSizeScrollIndicators(int[] xy) {
        return super.reSizeScrollIndicators(xy);
    }

    protected void adjustSelector(int pos, Rect rect) {
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
            int fvp = getFirstVisiblePosition();
            int lvp = getLastVisiblePosition();
            if (fvp == lvp) {
                setSelector((int) androidhwext.R.drawable.list_selector_single_emui);
            } else if (fvp == pos) {
                setSelector((int) androidhwext.R.drawable.list_selector_top_emui);
            } else if (lvp == pos) {
                setSelector((int) androidhwext.R.drawable.list_selector_bottom_emui);
            } else if (this.mCacheSelector != null) {
                setSelector(this.mCacheSelector);
            }
        }
    }

    public void setNeedAdjustSelector(boolean flag) {
        this.mNeedAdjustSelector = flag;
    }
}
