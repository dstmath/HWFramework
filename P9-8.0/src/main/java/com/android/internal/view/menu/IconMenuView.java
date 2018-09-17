package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import com.android.internal.R;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import java.util.ArrayList;

public final class IconMenuView extends ViewGroup implements ItemInvoker, MenuView, Runnable {
    private static final int ITEM_CAPTION_CYCLE_DELAY = 1000;
    private int mAnimations;
    private boolean mHasStaleChildren;
    private Drawable mHorizontalDivider;
    private int mHorizontalDividerHeight;
    private ArrayList<Rect> mHorizontalDividerRects;
    private Drawable mItemBackground;
    private boolean mLastChildrenCaptionMode;
    private int[] mLayout;
    private int mLayoutNumRows;
    private int mMaxItems;
    private int mMaxItemsPerRow;
    private int mMaxRows;
    private MenuBuilder mMenu;
    private boolean mMenuBeingLongpressed = false;
    private Drawable mMoreIcon;
    private int mNumActualItemsShown;
    private int mRowHeight;
    private Drawable mVerticalDivider;
    private ArrayList<Rect> mVerticalDividerRects;
    private int mVerticalDividerWidth;

    public static class LayoutParams extends MarginLayoutParams {
        int bottom;
        int desiredWidth;
        int left;
        int maxNumItemsOnRow;
        int right;
        int top;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int focusedPosition;

        public SavedState(Parcelable superState, int focusedPosition) {
            super(superState);
            this.focusedPosition = focusedPosition;
        }

        private SavedState(Parcel in) {
            super(in);
            this.focusedPosition = in.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.focusedPosition);
        }
    }

    public IconMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconMenuView, 0, 0);
        this.mRowHeight = a.getDimensionPixelSize(0, 64);
        this.mMaxRows = a.getInt(1, 2);
        this.mMaxItems = a.getInt(4, 6);
        this.mMaxItemsPerRow = a.getInt(2, 3);
        this.mMoreIcon = a.getDrawable(3);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, R.styleable.MenuView, 0, 0);
        this.mItemBackground = a.getDrawable(5);
        this.mHorizontalDivider = a.getDrawable(2);
        this.mHorizontalDividerRects = new ArrayList();
        this.mVerticalDivider = a.getDrawable(3);
        this.mVerticalDividerRects = new ArrayList();
        this.mAnimations = a.getResourceId(0, 0);
        a.recycle();
        if (this.mHorizontalDivider != null) {
            this.mHorizontalDividerHeight = this.mHorizontalDivider.getIntrinsicHeight();
            if (this.mHorizontalDividerHeight == -1) {
                this.mHorizontalDividerHeight = 1;
            }
        }
        if (this.mVerticalDivider != null) {
            this.mVerticalDividerWidth = this.mVerticalDivider.getIntrinsicWidth();
            if (this.mVerticalDividerWidth == -1) {
                this.mVerticalDividerWidth = 1;
            }
        }
        this.mLayout = new int[this.mMaxRows];
        setWillNotDraw(false);
        setFocusableInTouchMode(true);
        setDescendantFocusability(262144);
    }

    int getMaxItems() {
        return this.mMaxItems;
    }

    private void layoutItems(int width) {
        int numItems = getChildCount();
        if (numItems == 0) {
            this.mLayoutNumRows = 0;
            return;
        }
        for (int curNumRows = Math.min((int) Math.ceil((double) (((float) numItems) / ((float) this.mMaxItemsPerRow))), this.mMaxRows); curNumRows <= this.mMaxRows; curNumRows++) {
            layoutItemsUsingGravity(curNumRows, numItems);
            if (curNumRows >= numItems || doItemsFit()) {
                break;
            }
        }
    }

    private void layoutItemsUsingGravity(int numRows, int numItems) {
        int numBaseItemsPerRow = numItems / numRows;
        int rowsThatGetALeftoverItem = numRows - (numItems % numRows);
        int[] layout = this.mLayout;
        for (int i = 0; i < numRows; i++) {
            layout[i] = numBaseItemsPerRow;
            if (i >= rowsThatGetALeftoverItem) {
                layout[i] = layout[i] + 1;
            }
        }
        this.mLayoutNumRows = numRows;
    }

    private boolean doItemsFit() {
        int itemPos = 0;
        int[] layout = this.mLayout;
        int numRows = this.mLayoutNumRows;
        for (int row = 0; row < numRows; row++) {
            int numItemsOnRow = layout[row];
            if (numItemsOnRow == 1) {
                itemPos++;
            } else {
                int itemsOnRowCounter = numItemsOnRow;
                int itemPos2 = itemPos;
                while (itemsOnRowCounter > 0) {
                    itemPos = itemPos2 + 1;
                    if (((LayoutParams) getChildAt(itemPos2).getLayoutParams()).maxNumItemsOnRow < numItemsOnRow) {
                        return false;
                    }
                    itemsOnRowCounter--;
                    itemPos2 = itemPos;
                }
                itemPos = itemPos2;
            }
        }
        return true;
    }

    Drawable getItemBackgroundDrawable() {
        return this.mItemBackground.getConstantState().newDrawable(getContext().getResources());
    }

    IconMenuItemView createMoreItemView() {
        Context context = getContext();
        IconMenuItemView itemView = (IconMenuItemView) LayoutInflater.from(context).inflate((int) R.layout.icon_menu_item_layout, null);
        itemView.initialize(context.getResources().getText(R.string.more_item_label), this.mMoreIcon);
        itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                IconMenuView.this.mMenu.changeMenuMode();
            }
        });
        return itemView;
    }

    public void initialize(MenuBuilder menu) {
        this.mMenu = menu;
    }

    private void positionChildren(int menuWidth, int menuHeight) {
        if (this.mHorizontalDivider != null) {
            this.mHorizontalDividerRects.clear();
        }
        if (this.mVerticalDivider != null) {
            this.mVerticalDividerRects.clear();
        }
        int numRows = this.mLayoutNumRows;
        int numRowsMinus1 = numRows - 1;
        int[] numItemsForRow = this.mLayout;
        int itemPos = 0;
        LayoutParams childLayoutParams = null;
        float itemTop = 0.0f;
        float itemHeight = ((float) (menuHeight - (this.mHorizontalDividerHeight * (numRows - 1)))) / ((float) numRows);
        int row = 0;
        while (row < numRows) {
            float itemLeft = 0.0f;
            float itemWidth = ((float) (menuWidth - (this.mVerticalDividerWidth * (numItemsForRow[row] - 1)))) / ((float) numItemsForRow[row]);
            for (int itemPosOnRow = 0; itemPosOnRow < numItemsForRow[row]; itemPosOnRow++) {
                View child = getChildAt(itemPos);
                child.measure(MeasureSpec.makeMeasureSpec((int) itemWidth, 1073741824), MeasureSpec.makeMeasureSpec((int) itemHeight, 1073741824));
                childLayoutParams = (LayoutParams) child.getLayoutParams();
                childLayoutParams.left = (int) itemLeft;
                childLayoutParams.right = (int) (itemLeft + itemWidth);
                childLayoutParams.top = (int) itemTop;
                childLayoutParams.bottom = (int) (itemTop + itemHeight);
                itemLeft += itemWidth;
                itemPos++;
                if (this.mVerticalDivider != null) {
                    this.mVerticalDividerRects.add(new Rect((int) itemLeft, (int) itemTop, (int) (((float) this.mVerticalDividerWidth) + itemLeft), (int) (itemTop + itemHeight)));
                }
                itemLeft += (float) this.mVerticalDividerWidth;
            }
            if (childLayoutParams != null) {
                childLayoutParams.right = menuWidth;
            }
            itemTop += itemHeight;
            if (this.mHorizontalDivider != null && row < numRowsMinus1) {
                this.mHorizontalDividerRects.add(new Rect(0, (int) itemTop, menuWidth, (int) (((float) this.mHorizontalDividerHeight) + itemTop)));
                itemTop += (float) this.mHorizontalDividerHeight;
            }
            row++;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = View.resolveSize(Integer.MAX_VALUE, widthMeasureSpec);
        calculateItemFittingMetadata(measuredWidth);
        layoutItems(measuredWidth);
        int layoutNumRows = this.mLayoutNumRows;
        -wrap6(measuredWidth, View.resolveSize(((this.mRowHeight + this.mHorizontalDividerHeight) * layoutNumRows) - this.mHorizontalDividerHeight, heightMeasureSpec));
        if (layoutNumRows > 0) {
            positionChildren(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
            child.layout(childLayoutParams.left, childLayoutParams.top, childLayoutParams.right, childLayoutParams.bottom);
        }
    }

    protected void onDraw(Canvas canvas) {
        ArrayList<Rect> rects;
        int i;
        Drawable drawable = this.mHorizontalDivider;
        if (drawable != null) {
            rects = this.mHorizontalDividerRects;
            for (i = rects.size() - 1; i >= 0; i--) {
                drawable.setBounds((Rect) rects.get(i));
                drawable.draw(canvas);
            }
        }
        drawable = this.mVerticalDivider;
        if (drawable != null) {
            rects = this.mVerticalDividerRects;
            for (i = rects.size() - 1; i >= 0; i--) {
                drawable.setBounds((Rect) rects.get(i));
                drawable.draw(canvas);
            }
        }
    }

    public boolean invokeItem(MenuItemImpl item) {
        return this.mMenu.performItemAction(item, 0);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    void markStaleChildren() {
        if (!this.mHasStaleChildren) {
            this.mHasStaleChildren = true;
            requestLayout();
        }
    }

    int getNumActualItemsShown() {
        return this.mNumActualItemsShown;
    }

    void setNumActualItemsShown(int count) {
        this.mNumActualItemsShown = count;
    }

    public int getWindowAnimations() {
        return this.mAnimations;
    }

    public int[] getLayout() {
        return this.mLayout;
    }

    public int getLayoutNumRows() {
        return this.mLayoutNumRows;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 82) {
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                removeCallbacks(this);
                postDelayed(this, (long) ViewConfiguration.getLongPressTimeout());
            } else if (event.getAction() == 1) {
                if (this.mMenuBeingLongpressed) {
                    setCycleShortcutCaptionMode(false);
                    return true;
                }
                removeCallbacks(this);
            }
        }
        return super.-wrap7(event);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestFocus();
    }

    protected void onDetachedFromWindow() {
        setCycleShortcutCaptionMode(false);
        super.onDetachedFromWindow();
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) {
            setCycleShortcutCaptionMode(false);
        }
        super.onWindowFocusChanged(hasWindowFocus);
    }

    private void setCycleShortcutCaptionMode(boolean cycleShortcutAndNormal) {
        if (cycleShortcutAndNormal) {
            setChildrenCaptionMode(true);
            return;
        }
        removeCallbacks(this);
        setChildrenCaptionMode(false);
        this.mMenuBeingLongpressed = false;
    }

    public void run() {
        if (this.mMenuBeingLongpressed) {
            setChildrenCaptionMode(this.mLastChildrenCaptionMode ^ 1);
        } else {
            this.mMenuBeingLongpressed = true;
            setCycleShortcutCaptionMode(true);
        }
        postDelayed(this, 1000);
    }

    private void setChildrenCaptionMode(boolean shortcut) {
        this.mLastChildrenCaptionMode = shortcut;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            ((IconMenuItemView) getChildAt(i)).setCaptionMode(shortcut);
        }
    }

    private void calculateItemFittingMetadata(int width) {
        int maxNumItemsPerRow = this.mMaxItemsPerRow;
        int numItems = getChildCount();
        for (int i = 0; i < numItems; i++) {
            LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            lp.maxNumItemsOnRow = 1;
            for (int curNumItemsPerRow = maxNumItemsPerRow; curNumItemsPerRow > 0; curNumItemsPerRow--) {
                if (lp.desiredWidth < width / curNumItemsPerRow) {
                    lp.maxNumItemsOnRow = curNumItemsPerRow;
                    break;
                }
            }
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.-wrap0();
        View focusedView = getFocusedChild();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (getChildAt(i) == focusedView) {
                return new SavedState(superState, i);
            }
        }
        return new SavedState(superState, -1);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.-wrap2(ss.getSuperState());
        if (ss.focusedPosition < getChildCount()) {
            View v = getChildAt(ss.focusedPosition);
            if (v != null) {
                v.requestFocus();
            }
        }
    }
}
