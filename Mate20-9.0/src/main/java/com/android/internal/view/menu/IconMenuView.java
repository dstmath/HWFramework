package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import com.android.internal.R;
import com.android.internal.view.menu.MenuBuilder;
import java.util.ArrayList;

public final class IconMenuView extends ViewGroup implements MenuBuilder.ItemInvoker, MenuView, Runnable {
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
    /* access modifiers changed from: private */
    public MenuBuilder mMenu;
    private boolean mMenuBeingLongpressed = false;
    private Drawable mMoreIcon;
    private int mNumActualItemsShown;
    private int mRowHeight;
    private Drawable mVerticalDivider;
    private ArrayList<Rect> mVerticalDividerRects;
    private int mVerticalDividerWidth;

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
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

    private static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int focusedPosition;

        public SavedState(Parcelable superState, int focusedPosition2) {
            super(superState);
            this.focusedPosition = focusedPosition2;
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
        TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.MenuView, 0, 0);
        this.mItemBackground = a2.getDrawable(5);
        this.mHorizontalDivider = a2.getDrawable(2);
        this.mHorizontalDividerRects = new ArrayList<>();
        this.mVerticalDivider = a2.getDrawable(3);
        this.mVerticalDividerRects = new ArrayList<>();
        this.mAnimations = a2.getResourceId(0, 0);
        a2.recycle();
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

    /* access modifiers changed from: package-private */
    public int getMaxItems() {
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
        int[] layout = this.mLayout;
        int numRows = this.mLayoutNumRows;
        int itemPos = 0;
        for (int row = 0; row < numRows; row++) {
            int numItemsOnRow = layout[row];
            if (numItemsOnRow == 1) {
                itemPos++;
            } else {
                int itemPos2 = itemPos;
                int itemsOnRowCounter = numItemsOnRow;
                while (itemsOnRowCounter > 0) {
                    int itemPos3 = itemPos2 + 1;
                    if (((LayoutParams) getChildAt(itemPos2).getLayoutParams()).maxNumItemsOnRow < numItemsOnRow) {
                        return false;
                    }
                    itemsOnRowCounter--;
                    itemPos2 = itemPos3;
                }
                itemPos = itemPos2;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public Drawable getItemBackgroundDrawable() {
        return this.mItemBackground.getConstantState().newDrawable(getContext().getResources());
    }

    /* access modifiers changed from: package-private */
    public IconMenuItemView createMoreItemView() {
        Context context = getContext();
        IconMenuItemView itemView = (IconMenuItemView) LayoutInflater.from(context).inflate(17367154, null);
        itemView.initialize(context.getResources().getText(17040538), this.mMoreIcon);
        itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                IconMenuView.this.mMenu.changeMenuMode();
            }
        });
        return itemView;
    }

    public void initialize(MenuBuilder menu) {
        this.mMenu = menu;
    }

    /* JADX WARNING: type inference failed for: r2v12, types: [android.view.ViewGroup$LayoutParams] */
    /* JADX WARNING: Multi-variable type inference failed */
    private void positionChildren(int menuWidth, int menuHeight) {
        LayoutParams childLayoutParams;
        int itemPos;
        int i = menuWidth;
        if (this.mHorizontalDivider != null) {
            this.mHorizontalDividerRects.clear();
        }
        if (this.mVerticalDivider != null) {
            this.mVerticalDividerRects.clear();
        }
        int numRows = this.mLayoutNumRows;
        int numRowsMinus1 = numRows - 1;
        int[] numItemsForRow = this.mLayout;
        LayoutParams childLayoutParams2 = null;
        float itemHeight = ((float) (menuHeight - (this.mHorizontalDividerHeight * (numRows - 1)))) / ((float) numRows);
        float itemTop = 0.0f;
        int itemPos2 = 0;
        int row = 0;
        while (row < numRows) {
            float itemWidth = ((float) (i - (this.mVerticalDividerWidth * (numItemsForRow[row] - 1)))) / ((float) numItemsForRow[row]);
            float itemLeft = 0.0f;
            LayoutParams childLayoutParams3 = childLayoutParams2;
            int itemPosOnRow = 0;
            while (itemPosOnRow < numItemsForRow[row]) {
                View child = getChildAt(itemPos2);
                int numRows2 = numRows;
                int[] numItemsForRow2 = numItemsForRow;
                child.measure(View.MeasureSpec.makeMeasureSpec((int) itemWidth, 1073741824), View.MeasureSpec.makeMeasureSpec((int) itemHeight, 1073741824));
                LayoutParams childLayoutParams4 = child.getLayoutParams();
                childLayoutParams4.left = (int) itemLeft;
                childLayoutParams4.right = (int) (itemLeft + itemWidth);
                childLayoutParams4.top = (int) itemTop;
                childLayoutParams4.bottom = (int) (itemTop + itemHeight);
                float itemLeft2 = itemLeft + itemWidth;
                int itemPos3 = itemPos2 + 1;
                if (this.mVerticalDivider != null) {
                    itemPos = itemPos3;
                    childLayoutParams = childLayoutParams4;
                    this.mVerticalDividerRects.add(new Rect((int) itemLeft2, (int) itemTop, (int) (((float) this.mVerticalDividerWidth) + itemLeft2), (int) (itemTop + itemHeight)));
                } else {
                    itemPos = itemPos3;
                    childLayoutParams = childLayoutParams4;
                }
                itemLeft = itemLeft2 + ((float) this.mVerticalDividerWidth);
                itemPosOnRow++;
                numRows = numRows2;
                numItemsForRow = numItemsForRow2;
                itemPos2 = itemPos;
                childLayoutParams3 = childLayoutParams;
            }
            int numRows3 = numRows;
            int[] numItemsForRow3 = numItemsForRow;
            if (childLayoutParams3 != null) {
                childLayoutParams3.right = i;
            }
            itemTop += itemHeight;
            if (this.mHorizontalDivider != null && row < numRowsMinus1) {
                this.mHorizontalDividerRects.add(new Rect(0, (int) itemTop, i, (int) (((float) this.mHorizontalDividerHeight) + itemTop)));
                itemTop += (float) this.mHorizontalDividerHeight;
            }
            row++;
            childLayoutParams2 = childLayoutParams3;
            numRows = numRows3;
            numItemsForRow = numItemsForRow3;
        }
        int[] iArr = numItemsForRow;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = resolveSize(Integer.MAX_VALUE, widthMeasureSpec);
        calculateItemFittingMetadata(measuredWidth);
        layoutItems(measuredWidth);
        int layoutNumRows = this.mLayoutNumRows;
        setMeasuredDimension(measuredWidth, resolveSize(((this.mRowHeight + this.mHorizontalDividerHeight) * layoutNumRows) - this.mHorizontalDividerHeight, heightMeasureSpec));
        if (layoutNumRows > 0) {
            positionChildren(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
            child.layout(childLayoutParams.left, childLayoutParams.top, childLayoutParams.right, childLayoutParams.bottom);
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        Drawable drawable = this.mHorizontalDivider;
        if (drawable != null) {
            ArrayList<Rect> rects = this.mHorizontalDividerRects;
            for (int i = rects.size() - 1; i >= 0; i--) {
                drawable.setBounds(rects.get(i));
                drawable.draw(canvas);
            }
        }
        Drawable drawable2 = this.mVerticalDivider;
        if (drawable2 != null) {
            ArrayList<Rect> rects2 = this.mVerticalDividerRects;
            for (int i2 = rects2.size() - 1; i2 >= 0; i2--) {
                drawable2.setBounds(rects2.get(i2));
                drawable2.draw(canvas);
            }
        }
    }

    public boolean invokeItem(MenuItemImpl item) {
        return this.mMenu.performItemAction(item, 0);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /* access modifiers changed from: package-private */
    public void markStaleChildren() {
        if (!this.mHasStaleChildren) {
            this.mHasStaleChildren = true;
            requestLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public int getNumActualItemsShown() {
        return this.mNumActualItemsShown;
    }

    /* access modifiers changed from: package-private */
    public void setNumActualItemsShown(int count) {
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
        return super.dispatchKeyEvent(event);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestFocus();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
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
        if (!cycleShortcutAndNormal) {
            removeCallbacks(this);
            setChildrenCaptionMode(false);
            this.mMenuBeingLongpressed = false;
            return;
        }
        setChildrenCaptionMode(true);
    }

    public void run() {
        if (this.mMenuBeingLongpressed) {
            setChildrenCaptionMode(!this.mLastChildrenCaptionMode);
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
            int curNumItemsPerRow = maxNumItemsPerRow;
            while (true) {
                if (curNumItemsPerRow <= 0) {
                    break;
                } else if (lp.desiredWidth < width / curNumItemsPerRow) {
                    lp.maxNumItemsOnRow = curNumItemsPerRow;
                    break;
                } else {
                    curNumItemsPerRow--;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        View focusedView = getFocusedChild();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (getChildAt(i) == focusedView) {
                return new SavedState(superState, i);
            }
        }
        return new SavedState(superState, -1);
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.focusedPosition < getChildCount()) {
            View v = getChildAt(ss.focusedPosition);
            if (v != null) {
                v.requestFocus();
            }
        }
    }
}
