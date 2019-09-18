package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import com.android.internal.R;

public class TabWidget extends LinearLayout implements View.OnFocusChangeListener {
    private final Rect mBounds;
    private boolean mDrawBottomStrips;
    private int[] mImposedTabWidths;
    private int mImposedTabsHeight;
    private Drawable mLeftStrip;
    private Drawable mRightStrip;
    private int mSelectedTab;
    /* access modifiers changed from: private */
    public OnTabSelectionChanged mSelectionChangedListener;
    private boolean mStripMoved;

    interface OnTabSelectionChanged {
        void onTabSelectionChanged(int i, boolean z);
    }

    private class TabClickListener implements View.OnClickListener {
        private final int mTabIndex;

        private TabClickListener(int tabIndex) {
            this.mTabIndex = tabIndex;
        }

        public void onClick(View v) {
            TabWidget.this.mSelectionChangedListener.onTabSelectionChanged(this.mTabIndex, true);
        }
    }

    public TabWidget(Context context) {
        this(context, null);
    }

    public TabWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 16842883);
    }

    public TabWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TabWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mBounds = new Rect();
        this.mSelectedTab = -1;
        this.mDrawBottomStrips = true;
        this.mImposedTabsHeight = -1;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabWidget, defStyleAttr, defStyleRes);
        this.mDrawBottomStrips = a.getBoolean(3, this.mDrawBottomStrips);
        boolean isTargetSdkDonutOrLower = context.getApplicationInfo().targetSdkVersion <= 4;
        if (a.hasValueOrEmpty(1)) {
            this.mLeftStrip = a.getDrawable(1);
        } else if (isTargetSdkDonutOrLower) {
            this.mLeftStrip = context.getDrawable(17303622);
        } else {
            this.mLeftStrip = context.getDrawable(17303621);
        }
        if (a.hasValueOrEmpty(2)) {
            this.mRightStrip = a.getDrawable(2);
        } else if (isTargetSdkDonutOrLower) {
            this.mRightStrip = context.getDrawable(17303624);
        } else {
            this.mRightStrip = context.getDrawable(17303623);
        }
        a.recycle();
        setChildrenDrawingOrderEnabled(true);
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mStripMoved = true;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /* access modifiers changed from: protected */
    public int getChildDrawingOrder(int childCount, int i) {
        if (this.mSelectedTab == -1) {
            return i;
        }
        if (i == childCount - 1) {
            return this.mSelectedTab;
        }
        if (i >= this.mSelectedTab) {
            return i + 1;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void measureChildBeforeLayout(View child, int childIndex, int widthMeasureSpec, int totalWidth, int heightMeasureSpec, int totalHeight) {
        if (!isMeasureWithLargestChildEnabled() && this.mImposedTabsHeight >= 0) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mImposedTabWidths[childIndex] + totalWidth, 1073741824);
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mImposedTabsHeight, 1073741824);
        }
        super.measureChildBeforeLayout(child, childIndex, widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    /* access modifiers changed from: package-private */
    public void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        if (View.MeasureSpec.getMode(widthMeasureSpec) == 0) {
            super.measureHorizontal(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int unspecifiedWidth = View.MeasureSpec.makeSafeMeasureSpec(width, 0);
        this.mImposedTabsHeight = -1;
        super.measureHorizontal(unspecifiedWidth, heightMeasureSpec);
        int extraWidth = getMeasuredWidth() - width;
        if (extraWidth > 0) {
            int count = getChildCount();
            int childCount = 0;
            for (int i = 0; i < count; i++) {
                if (getChildAt(i).getVisibility() != 8) {
                    childCount++;
                }
            }
            if (childCount > 0) {
                if (this.mImposedTabWidths == null || this.mImposedTabWidths.length != count) {
                    this.mImposedTabWidths = new int[count];
                }
                int extraWidth2 = extraWidth;
                for (int i2 = 0; i2 < count; i2++) {
                    View child = getChildAt(i2);
                    if (child.getVisibility() != 8) {
                        int childWidth = child.getMeasuredWidth();
                        int newWidth = Math.max(0, childWidth - (extraWidth2 / childCount));
                        this.mImposedTabWidths[i2] = newWidth;
                        extraWidth2 -= childWidth - newWidth;
                        childCount--;
                        this.mImposedTabsHeight = Math.max(this.mImposedTabsHeight, child.getMeasuredHeight());
                    }
                }
                int i3 = extraWidth2;
            }
        }
        super.measureHorizontal(widthMeasureSpec, heightMeasureSpec);
    }

    public View getChildTabViewAt(int index) {
        return getChildAt(index);
    }

    public int getTabCount() {
        return getChildCount();
    }

    public void setDividerDrawable(Drawable drawable) {
        super.setDividerDrawable(drawable);
    }

    public void setDividerDrawable(int resId) {
        setDividerDrawable(this.mContext.getDrawable(resId));
    }

    public void setLeftStripDrawable(Drawable drawable) {
        this.mLeftStrip = drawable;
        requestLayout();
        invalidate();
    }

    public void setLeftStripDrawable(int resId) {
        setLeftStripDrawable(this.mContext.getDrawable(resId));
    }

    public Drawable getLeftStripDrawable() {
        return this.mLeftStrip;
    }

    public void setRightStripDrawable(Drawable drawable) {
        this.mRightStrip = drawable;
        requestLayout();
        invalidate();
    }

    public void setRightStripDrawable(int resId) {
        setRightStripDrawable(this.mContext.getDrawable(resId));
    }

    public Drawable getRightStripDrawable() {
        return this.mRightStrip;
    }

    public void setStripEnabled(boolean stripEnabled) {
        this.mDrawBottomStrips = stripEnabled;
        invalidate();
    }

    public boolean isStripEnabled() {
        return this.mDrawBottomStrips;
    }

    public void childDrawableStateChanged(View child) {
        if (this.mSelectedTab != -1 && getTabCount() > 0 && child == getChildTabViewAt(this.mSelectedTab)) {
            invalidate();
        }
        super.childDrawableStateChanged(child);
    }

    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (getTabCount() != 0 && this.mDrawBottomStrips) {
            View selectedChild = getChildTabViewAt(this.mSelectedTab);
            Drawable leftStrip = this.mLeftStrip;
            Drawable rightStrip = this.mRightStrip;
            if (leftStrip != null) {
                leftStrip.setState(selectedChild.getDrawableState());
            }
            if (rightStrip != null) {
                rightStrip.setState(selectedChild.getDrawableState());
            }
            if (this.mStripMoved) {
                Rect bounds = this.mBounds;
                bounds.left = selectedChild.getLeft();
                bounds.right = selectedChild.getRight();
                int myHeight = getHeight();
                if (leftStrip != null) {
                    leftStrip.setBounds(Math.min(0, bounds.left - leftStrip.getIntrinsicWidth()), myHeight - leftStrip.getIntrinsicHeight(), bounds.left, myHeight);
                }
                if (rightStrip != null) {
                    rightStrip.setBounds(bounds.right, myHeight - rightStrip.getIntrinsicHeight(), Math.max(getWidth(), bounds.right + rightStrip.getIntrinsicWidth()), myHeight);
                }
                this.mStripMoved = false;
            }
            if (leftStrip != null) {
                leftStrip.draw(canvas);
            }
            if (rightStrip != null) {
                rightStrip.draw(canvas);
            }
        }
    }

    public void setCurrentTab(int index) {
        if (index >= 0 && index < getTabCount() && index != this.mSelectedTab) {
            if (this.mSelectedTab != -1) {
                getChildTabViewAt(this.mSelectedTab).setSelected(false);
            }
            this.mSelectedTab = index;
            getChildTabViewAt(this.mSelectedTab).setSelected(true);
            this.mStripMoved = true;
        }
    }

    public CharSequence getAccessibilityClassName() {
        return TabWidget.class.getName();
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setItemCount(getTabCount());
        event.setCurrentItemIndex(this.mSelectedTab);
    }

    public void focusCurrentTab(int index) {
        int oldTab = this.mSelectedTab;
        setCurrentTab(index);
        if (oldTab != index) {
            getChildTabViewAt(index).requestFocus();
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int count = getTabCount();
        for (int i = 0; i < count; i++) {
            getChildTabViewAt(i).setEnabled(enabled);
        }
    }

    public void addView(View child) {
        if (child.getLayoutParams() == null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1.0f);
            lp.setMargins(0, 0, 0, 0);
            child.setLayoutParams(lp);
        }
        child.setFocusable(true);
        child.setClickable(true);
        if (child.getPointerIcon() == null) {
            child.setPointerIcon(PointerIcon.getSystemIcon(getContext(), 1002));
        }
        super.addView(child);
        child.setOnClickListener(new TabClickListener(getTabCount() - 1));
    }

    public void removeAllViews() {
        super.removeAllViews();
        this.mSelectedTab = -1;
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        if (!isEnabled()) {
            return null;
        }
        return super.onResolvePointerIcon(event, pointerIndex);
    }

    /* access modifiers changed from: package-private */
    public void setTabSelectionListener(OnTabSelectionChanged listener) {
        this.mSelectionChangedListener = listener;
    }

    public void onFocusChange(View v, boolean hasFocus) {
    }
}
