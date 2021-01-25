package huawei.android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.TextView;
import huawei.com.android.internal.view.menu.HwActionMenuItemView;

public class HwActionMenuView extends ActionMenuView implements HwSmartColorListener {
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_DEBUG = false;
    private static final int ITEM_COUNT_FIVE = 5;
    private static final int ITEM_COUNT_FOUR = 4;
    private static final int ITEM_COUNT_THREE = 3;
    private static final int ITEM_COUNT_TWO = 2;
    private static final String TAG = "HwActionMenuView";
    private int mActionBarMenuItemPadding;
    private int mActionBarMenuItemSize;
    private int mActionBarMenuPadding;
    private int mActionBarOverFlowBtnEndPadding;
    private int mActionBarOverFlowBtnSize;
    private int mActionBarOverFlowBtnStartPadding;
    private Typeface mCondensed;
    private boolean mIsContain2Lines;
    private boolean mIsHaveOverFlowBtnAtActionBar;
    private boolean mIsHaveVisibleChildAtActionBar;
    private int mMaxSize;
    private int mMinHeight;
    private ActionMenuPresenter mPresenter;
    private Parcelable mSavedState;
    private Typeface mSerif;
    private ColorStateList mSmartIconColorList;
    private ColorStateList mSmartTitleColorList;
    private int mTextSize;
    private int[] mToolBarMenuItemMaxSize3Arrays;
    private int[] mToolBarMenuItemMaxSize4Arrays;
    private int[] mToolBarMenuItemMaxSize5Arrays;
    private int mToolBarMenuItemPadding;
    private int mToolBarMenuPadding;

    public HwActionMenuView(Context context) {
        this(context, null);
    }

    public HwActionMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mIsContain2Lines = IS_DEBUG;
        this.mIsHaveVisibleChildAtActionBar = IS_DEBUG;
        this.mIsHaveOverFlowBtnAtActionBar = IS_DEBUG;
        this.mMinHeight = context.getResources().getDimensionPixelSize(34471968);
        initWidths(getContext());
        this.mSerif = Typeface.create("dinnext-serif", 0);
        this.mCondensed = Typeface.create("dinnext-condensed", 0);
    }

    public void setPresenter(ActionMenuPresenter presenter) {
        super.setPresenter(presenter);
        this.mPresenter = presenter;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ActionMenuView, android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!(getParent() instanceof View) || ((View) getParent()).getId() != 16909433) {
            onMeasureAtActionBar(widthMeasureSpec, heightMeasureSpec);
            if (!this.mIsHaveVisibleChildAtActionBar) {
                setPadding(0, 0, 0, 0);
            } else if (isLayoutRtl()) {
                if (this.mIsHaveOverFlowBtnAtActionBar) {
                    setPadding(this.mActionBarOverFlowBtnEndPadding, 0, 0, 0);
                } else {
                    setPadding(this.mActionBarMenuPadding, 0, 0, 0);
                }
            } else if (this.mIsHaveOverFlowBtnAtActionBar) {
                setPadding(0, 0, this.mActionBarOverFlowBtnEndPadding, 0);
            } else {
                setPadding(0, 0, this.mActionBarMenuPadding, 0);
            }
        } else {
            onMeasureAtSplitView(widthMeasureSpec, heightMeasureSpec);
            setPadding(0, 0, 0, 0);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasureAtActionBar(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthPadding = this.mActionBarMenuPadding;
        int itemWidthSpec = View.MeasureSpec.makeMeasureSpec(this.mActionBarMenuItemSize, 1073741824);
        int itemHeightSpec = View.MeasureSpec.makeMeasureSpec(this.mActionBarMenuItemSize, 1073741824);
        int childCount = getChildCount();
        int totalWidth = 0;
        int i = 0;
        this.mIsHaveVisibleChildAtActionBar = IS_DEBUG;
        for (int i2 = 0; i2 < childCount; i2++) {
            View child = getChildAt(i2);
            if (child.getVisibility() != 8) {
                child.setPadding(0, child.getPaddingTop(), 0, child.getPaddingBottom());
                if (child.getLayoutParams() instanceof ActionMenuView.LayoutParams) {
                    ActionMenuView.LayoutParams params = (ActionMenuView.LayoutParams) child.getLayoutParams();
                    if (params.isOverflowButton) {
                        itemWidthSpec = View.MeasureSpec.makeMeasureSpec(this.mActionBarOverFlowBtnSize, 1073741824);
                    }
                    child.measure(itemWidthSpec, itemHeightSpec);
                    totalWidth += child.getMeasuredWidth();
                    if (childCount - 1 > i2) {
                        if (isNextItemOverFlowBtn(i2)) {
                            totalWidth += this.mActionBarOverFlowBtnStartPadding;
                        } else {
                            totalWidth += this.mActionBarMenuItemPadding;
                        }
                    }
                    if (childCount - 1 == i2 && params.isOverflowButton) {
                        this.mIsHaveOverFlowBtnAtActionBar = true;
                    }
                    this.mIsHaveVisibleChildAtActionBar = true;
                }
            }
        }
        int totalWidth2 = (this.mIsHaveOverFlowBtnAtActionBar ? this.mActionBarOverFlowBtnEndPadding : widthPadding) + totalWidth;
        if (this.mIsHaveVisibleChildAtActionBar) {
            i = totalWidth2;
        }
        setMeasuredDimension(i, heightSize);
    }

    private boolean isNextItemOverFlowBtn(int itemNum) {
        int childCount = getChildCount();
        if (childCount < 2 || childCount - 1 <= itemNum) {
            return IS_DEBUG;
        }
        View nextChild = getChildAt(itemNum + 1);
        if (!(nextChild.getLayoutParams() instanceof ActionMenuView.LayoutParams)) {
            return IS_DEBUG;
        }
        return ((ActionMenuView.LayoutParams) nextChild.getLayoutParams()).isOverflowButton;
    }

    /* access modifiers changed from: protected */
    public void onMeasureAtSplitView(int widthMeasureSpec, int heightMeasureSpec) {
        int cellSize;
        int height;
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), -2);
        int childTotalWidth = widthSize;
        int i = this.mMaxSize;
        if (i != 0) {
            childTotalWidth = i - (this.mToolBarMenuPadding * 2);
        }
        int avgCellSizeRemaining = 0;
        int visibleItemCount = getVisibleCount();
        if (visibleItemCount <= 0) {
            setMeasuredDimension(widthSize, 0);
            return;
        }
        if (visibleItemCount < 4) {
            cellSize = childTotalWidth / 4;
            avgCellSizeRemaining = (childTotalWidth - (cellSize * visibleItemCount)) / visibleItemCount;
        } else {
            cellSize = childTotalWidth / visibleItemCount;
        }
        ItemSpec[] items = new ItemSpec[visibleItemCount];
        int maxDiff = calculateMaxDifferent(items, cellSize, visibleItemCount);
        if (maxDiff == 0 || maxDiff < avgCellSizeRemaining || visibleItemCount == 1) {
            int cellSizeAdding = 0;
            if (maxDiff > 0 && maxDiff < avgCellSizeRemaining) {
                cellSizeAdding = maxDiff;
            }
            measureVisibleChildWidth(items, cellSize, cellSizeAdding, itemHeightSpec);
            return;
        }
        int[] standardIconPositions = new int[visibleItemCount];
        fillItemSpec(items, cellSize + avgCellSizeRemaining, standardIconPositions);
        adjustItemSize(items);
        int width = 0 + measureChildWidth(items, itemHeightSpec, standardIconPositions);
        measureTwoLinesChild(items, itemHeightSpec);
        if (this.mIsContain2Lines) {
            height = this.mMinHeight + this.mTextSize;
        } else {
            height = this.mMinHeight;
        }
        setMeasuredDimension(width, height);
    }

    private void fillItemSpec(ItemSpec[] items, int cellSize, int[] standardIconPositions) {
        int length = items.length;
        for (int i = 0; i < length; i++) {
            items[i].mItemSize = cellSize;
            standardIconPositions[i] = (cellSize * i) + ((cellSize - this.mActionBarMenuItemSize) / 2);
            items[i].mIsTooLong = items[i].mMeasuredSize > cellSize ? true : IS_DEBUG;
        }
    }

    private int getVisibleCount() {
        int childCount = getChildCount();
        int visibleItemCount = 0;
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).getVisibility() != 8) {
                visibleItemCount++;
            }
        }
        return visibleItemCount;
    }

    private void measureVisibleChildWidth(ItemSpec[] items, int cellSize, int cellSizeAdding, int itemHeightSpec) {
        int visibleItemCount = items.length;
        int childCount = getChildCount();
        int width = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                child.setPadding(this.mToolBarMenuItemPadding, child.getPaddingTop(), this.mToolBarMenuItemPadding, child.getPaddingBottom());
                if (visibleItemCount == 1) {
                    child.measure(View.MeasureSpec.makeMeasureSpec(cellSize > items[0].mMeasuredSize ? cellSize : items[0].mMeasuredSize, 1073741824), itemHeightSpec);
                } else {
                    child.measure(View.MeasureSpec.makeMeasureSpec(cellSize + cellSizeAdding, 1073741824), itemHeightSpec);
                }
                width += child.getMeasuredWidth();
            }
        }
        setMeasuredDimension(width, this.mMinHeight);
    }

    private int calculateMaxDifferent(ItemSpec[] items, int cellSize, int visibleItemCount) {
        int gone = 0;
        int childCount = getChildCount();
        int maxDiff = 0;
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) instanceof TextView) {
                TextView child = (TextView) getChildAt(i);
                int index = i - gone;
                if (child.getVisibility() == 8 || items.length <= index) {
                    gone++;
                } else {
                    items[index] = new ItemSpec();
                    items[index].mUsingSmallFont = -1;
                    items[index].mItemSize = cellSize;
                    items[index].mMaxSize = getItemMaxSize(index, visibleItemCount);
                    child.setSingleLine(true);
                    child.setMaxLines(1);
                    child.setTypeface(this.mSerif);
                    child.setTranslationX(0.0f);
                    child.setHwCompoundPadding(0, 0, 0, 0);
                    int requiredWidth = ((int) child.getPaint().measureText(child.getText().toString())) + (this.mToolBarMenuItemPadding * 2);
                    items[index].mMeasuredSize = requiredWidth;
                    if (requiredWidth > cellSize) {
                        items[index].mIsTooLong = true;
                        maxDiff = requiredWidth - cellSize > maxDiff ? requiredWidth - cellSize : maxDiff;
                    } else {
                        items[index].mIsTooLong = IS_DEBUG;
                    }
                }
            }
        }
        return maxDiff;
    }

    private void adjustItemSize(ItemSpec[] items) {
        int gone = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) instanceof TextView) {
                TextView child = (TextView) getChildAt(i);
                int index = i - gone;
                if (child.getVisibility() == 8) {
                    gone++;
                } else if (items.length > index && items[index].mIsTooLong) {
                    adjustSizeFromSiblings(child, items, index);
                    if (items[index].mUsingSmallFont == 0 && items[index].mIsTooLong) {
                        adjustSizeFromSiblings(child, items, index);
                    }
                }
            }
        }
    }

    private int measureChildWidth(ItemSpec[] items, int itemHeightSpec, int[] standardIconPositions) {
        int width = 0;
        int gone = 0;
        int childCount = getChildCount();
        int sumSize = 0;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                TextView child = (TextView) view;
                int index = i - gone;
                if (child.getVisibility() != 8) {
                    if (items.length > index) {
                        child.setPadding(this.mToolBarMenuItemPadding, child.getPaddingTop(), this.mToolBarMenuItemPadding, child.getPaddingBottom());
                        int iconTrans = standardIconPositions[index] - (((items[index].mItemSize - this.mActionBarMenuItemSize) / 2) + sumSize);
                        boolean isTextTrans = IS_DEBUG;
                        if (iconTrans < 0) {
                            if (this.mToolBarMenuItemPadding + sumSize + ((items[index].mItemSize - items[index].mMeasuredSize) / 2) > standardIconPositions[index]) {
                                child.setTranslationX((float) iconTrans);
                                isTextTrans = true;
                            }
                        } else if ((sumSize - this.mToolBarMenuItemPadding) + ((items[index].mItemSize + items[index].mMeasuredSize) / 2) < standardIconPositions[index] + this.mActionBarMenuItemSize) {
                            child.setTranslationX((float) iconTrans);
                            isTextTrans = true;
                        }
                        if (!isTextTrans) {
                            child.setHwCompoundPadding(iconTrans, 0, 0, 0);
                        }
                        sumSize += items[index].mItemSize;
                        child.measure(View.MeasureSpec.makeMeasureSpec(items[index].mItemSize, 1073741824), itemHeightSpec);
                        width += child.getMeasuredWidth();
                    }
                }
                gone++;
            }
        }
        return width;
    }

    private void measureTwoLinesChild(ItemSpec[] items, int itemHeightSpec) {
        int gone = 0;
        int childCount = getChildCount();
        this.mIsContain2Lines = IS_DEBUG;
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) instanceof TextView) {
                TextView child = (TextView) getChildAt(i);
                int index = i - gone;
                if (child.getVisibility() == 8) {
                    gone++;
                } else if (items.length > index && items[index].mIsTooLong) {
                    child.setTypeface(this.mCondensed);
                    if (((int) child.getPaint().measureText(child.getText().toString())) + (this.mToolBarMenuItemPadding * 2) > items[index].mItemSize) {
                        child.setSingleLine(IS_DEBUG);
                        child.setMaxLines(2);
                        this.mIsContain2Lines = true;
                    }
                    child.measure(View.MeasureSpec.makeMeasureSpec(items[index].mItemSize, 1073741824), itemHeightSpec);
                }
            }
        }
    }

    private void adjustSizeFromSiblings(TextView child, ItemSpec[] items, int index) {
        if (index == 0 && items.length > 1) {
            adjustSize(child, items, index, 1);
        } else if (index == items.length - 1 && items.length > 1) {
            adjustSize(child, items, index, -1);
        } else if (index < 1 || index + 1 >= items.length || items.length <= 2) {
            Log.e(TAG, "invalid index");
        } else if (!items[index - 1].mIsTooLong && items[index + 1].mIsTooLong) {
            adjustSize(child, items, index, -1);
        } else if (!items[index + 1].mIsTooLong && items[index - 1].mIsTooLong) {
            adjustSize(child, items, index, 1);
        } else if (items[index - 1].mIsTooLong || items[index + 1].mIsTooLong) {
            Log.e(TAG, "invalid item");
        } else {
            adjustSize(child, items, index, -1, 1);
        }
    }

    private void adjustSize(TextView child, ItemSpec[] items, int index, int diff) {
        int brother = index + diff;
        boolean z = true;
        boolean isIndexValid = index >= 0 && index < items.length;
        boolean isBrotherIndexValid = brother >= 0 && brother < items.length;
        if (isIndexValid && isBrotherIndexValid && !items[brother].mIsTooLong) {
            int needMeasuredSize = items[index].mMeasuredSize;
            boolean isStillTooLong = IS_DEBUG;
            if (items[index].mMeasuredSize > items[index].mMaxSize && items[index].mMaxSize > 0) {
                needMeasuredSize = items[index].mMaxSize;
                isStillTooLong = true;
            }
            int needDiff = needMeasuredSize - items[index].mItemSize;
            int overflowDiff = items[brother].mItemSize - items[brother].mMeasuredSize;
            if (needDiff < overflowDiff) {
                items[index].mItemSize += needDiff;
                items[index].mIsTooLong = isStillTooLong;
                items[brother].mItemSize -= needDiff;
            } else if (overflowDiff <= 0) {
                Log.e(TAG, "invalid index");
            } else if (items[index].mUsingSmallFont == -1) {
                child.setTypeface(this.mCondensed);
                int requiredWidthSmallFont = ((int) child.getPaint().measureText(child.getText().toString())) + (this.mToolBarMenuItemPadding * 2);
                items[index].mMeasuredSize = requiredWidthSmallFont;
                ItemSpec itemSpec = items[index];
                if (items[index].mItemSize >= requiredWidthSmallFont) {
                    z = false;
                }
                itemSpec.mIsTooLong = z;
                items[index].mUsingSmallFont = 0;
            } else if (items[index].mUsingSmallFont == 0) {
                items[index].mUsingSmallFont = 1;
                items[index].mItemSize += overflowDiff;
                items[index].mIsTooLong = true;
                items[brother].mItemSize -= overflowDiff;
            } else {
                Log.e(TAG, "invalid item");
            }
        }
    }

    private void adjustSize(TextView child, ItemSpec[] items, int index, int diffToMin, int diffToMax) {
        int brotherMin = index + diffToMin;
        int brotherMax = index + diffToMax;
        int overflowDiffToMin = items[brotherMin].mItemSize - items[brotherMin].mMeasuredSize;
        int overflowDiffToMax = items[brotherMax].mItemSize - items[brotherMax].mMeasuredSize;
        int min = overflowDiffToMin > overflowDiffToMax ? brotherMax : brotherMin;
        int minOverflowDiff = overflowDiffToMin > overflowDiffToMax ? overflowDiffToMax : overflowDiffToMin;
        int max = overflowDiffToMin > overflowDiffToMax ? brotherMax : brotherMin;
        int maxOverflowDiff = overflowDiffToMin > overflowDiffToMax ? overflowDiffToMin : overflowDiffToMax;
        int needMeasuredSize = items[index].mMeasuredSize;
        boolean isStillTooLong = IS_DEBUG;
        if (items[index].mMeasuredSize > items[index].mMaxSize && items[index].mMaxSize > 0) {
            needMeasuredSize = items[index].mMaxSize;
            isStillTooLong = true;
        }
        int needDiff = needMeasuredSize - items[index].mItemSize;
        if (needDiff <= maxOverflowDiff + minOverflowDiff) {
            int midNeedDiff = needDiff / 2;
            if (midNeedDiff <= minOverflowDiff) {
                items[index].mItemSize += needDiff;
                items[index].mIsTooLong = isStillTooLong;
                items[min].mItemSize -= midNeedDiff;
                items[max].mItemSize -= needDiff - midNeedDiff;
            } else if (midNeedDiff <= maxOverflowDiff) {
                items[index].mItemSize += needDiff;
                items[index].mIsTooLong = isStillTooLong;
                items[min].mItemSize -= minOverflowDiff;
                items[max].mItemSize -= needDiff - minOverflowDiff;
            } else {
                Log.e(TAG, "midNeedDiff is invalid");
            }
        } else if (items[index].mUsingSmallFont == -1) {
            child.setTypeface(this.mCondensed);
            int requiredWidthSmallFont = ((int) child.getPaint().measureText(child.getText().toString())) + (this.mToolBarMenuItemPadding * 2);
            items[index].mMeasuredSize = requiredWidthSmallFont;
            items[index].mIsTooLong = items[index].mItemSize < requiredWidthSmallFont;
            items[index].mUsingSmallFont = 0;
        } else if (items[index].mUsingSmallFont == 0) {
            items[index].mUsingSmallFont = 1;
            items[index].mItemSize += minOverflowDiff + maxOverflowDiff;
            items[index].mIsTooLong = true;
            items[min].mItemSize -= minOverflowDiff;
            items[max].mItemSize -= maxOverflowDiff;
        } else {
            Log.e(TAG, "item is invalid");
        }
    }

    private int getItemMaxSize(int index, int visibleItemCount) {
        if (index < 0) {
            return 0;
        }
        int[] iArr = this.mToolBarMenuItemMaxSize3Arrays;
        if (index < iArr.length && visibleItemCount == 3) {
            return iArr[index];
        }
        int[] iArr2 = this.mToolBarMenuItemMaxSize4Arrays;
        if (index < iArr2.length && visibleItemCount == 4) {
            return iArr2[index];
        }
        int[] iArr3 = this.mToolBarMenuItemMaxSize5Arrays;
        if (index >= iArr3.length || visibleItemCount != 5) {
            return 0;
        }
        return iArr3[index];
    }

    /* access modifiers changed from: private */
    public static class ItemSpec {
        boolean mIsTooLong;
        int mItemSize;
        int mMaxSize;
        int mMeasuredSize;
        int mUsingSmallFont;

        private ItemSpec() {
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ActionMenuView, android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        boolean isToolBar;
        int childCount = getChildCount();
        boolean isLayoutRtl = isLayoutRtl();
        boolean z = getParent() instanceof View;
        boolean z2 = IS_DEBUG;
        if (z) {
            if (((View) getParent()).getId() == 16909433) {
                z2 = true;
            }
            isToolBar = z2;
        } else {
            if (getContext().getResources().getConfiguration().orientation == 1) {
                z2 = true;
            }
            isToolBar = z2;
        }
        if (isLayoutRtl) {
            int startRight = getWidth() - getStartRight(isToolBar);
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                if (view.getLayoutParams() instanceof ActionMenuView.LayoutParams) {
                    ActionMenuView.LayoutParams params = (ActionMenuView.LayoutParams) view.getLayoutParams();
                    if (view.getVisibility() != 8) {
                        int startRight2 = startRight - params.rightMargin;
                        int width = view.getMeasuredWidth();
                        int height = view.getMeasuredHeight();
                        int layoutTop = getLayoutTop(isToolBar, top, bottom, view);
                        view.layout(startRight2 - width, layoutTop, startRight2, layoutTop + height);
                        startRight = startRight2 - ((params.leftMargin + width) + getItemPadding(isToolBar, i));
                    }
                }
            }
            return;
        }
        int startLeft = getStartLeft(isToolBar);
        for (int i2 = 0; i2 < childCount; i2++) {
            View view2 = getChildAt(i2);
            if (view2.getLayoutParams() instanceof ActionMenuView.LayoutParams) {
                ActionMenuView.LayoutParams params2 = (ActionMenuView.LayoutParams) view2.getLayoutParams();
                if (view2.getVisibility() != 8) {
                    int startLeft2 = startLeft + params2.leftMargin;
                    int width2 = view2.getMeasuredWidth();
                    int height2 = view2.getMeasuredHeight();
                    int layoutTop2 = getLayoutTop(isToolBar, top, bottom, view2);
                    view2.layout(startLeft2, layoutTop2, startLeft2 + width2, layoutTop2 + height2);
                    startLeft = startLeft2 + params2.rightMargin + width2 + getItemPadding(isToolBar, i2);
                }
            }
        }
    }

    private int getStartLeft(boolean isToolBar) {
        if (isToolBar) {
            return getPaddingLeft();
        }
        return 0;
    }

    private int getStartRight(boolean isToolBar) {
        if (isToolBar) {
            return getPaddingRight();
        }
        return 0;
    }

    private int getLayoutTop(boolean isToolBar, int top, int bottom, View child) {
        if (isToolBar) {
            return getPaddingTop();
        }
        return ((bottom - top) / 2) - (child.getMeasuredHeight() / 2);
    }

    private int getItemPadding(boolean isToolBar, int itemNum) {
        if (isToolBar) {
            return 0;
        }
        if (isNextItemOverFlowBtn(itemNum)) {
            return this.mActionBarOverFlowBtnStartPadding;
        }
        return this.mActionBarMenuItemPadding;
    }

    /* access modifiers changed from: protected */
    public boolean hasDividerBeforeChildAt(int childIndex) {
        return IS_DEBUG;
    }

    @Override // android.widget.ActionMenuView, android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        HwActionMenuPresenter hwActionMenuPresenter = this.mPresenter;
        if ((hwActionMenuPresenter instanceof HwActionMenuPresenter) && (hwActionMenuPresenter.isPopupMenuShowing() || this.mPresenter.isOverflowMenuShowing())) {
            this.mSavedState = this.mPresenter.onSaveInstanceState();
        }
        ActionMenuPresenter actionMenuPresenter = this.mPresenter;
        if (actionMenuPresenter != null) {
            actionMenuPresenter.dismissPopupMenus();
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwActionMenuPresenter hwActionMenuPresenter = this.mPresenter;
        if ((hwActionMenuPresenter instanceof HwActionMenuPresenter) && this.mSavedState != null) {
            if (!hwActionMenuPresenter.isPopupMenuShowing() || !this.mPresenter.isOverflowMenuShowing()) {
                this.mPresenter.onRestoreInstanceState(this.mSavedState);
                this.mSavedState = null;
            }
        }
    }

    @Override // android.widget.ActionMenuView, android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mMinHeight = getContext().getResources().getDimensionPixelSize(34471968);
    }

    private void initWidths(Context context) {
        Resources res = context.getResources();
        this.mTextSize = res.getDimensionPixelSize(34471965) + res.getDimensionPixelSize(34471967);
        this.mActionBarMenuItemPadding = res.getDimensionPixelSize(34471972);
        this.mActionBarMenuItemSize = res.getDimensionPixelSize(34471973);
        this.mActionBarOverFlowBtnSize = res.getDimensionPixelSize(34471950);
        this.mActionBarMenuPadding = res.getDimensionPixelSize(34471971);
        this.mToolBarMenuItemPadding = res.getDimensionPixelSize(34471970);
        this.mToolBarMenuPadding = res.getDimensionPixelSize(34471969);
        this.mActionBarOverFlowBtnStartPadding = res.getDimensionPixelSize(34471974);
        this.mActionBarOverFlowBtnEndPadding = res.getDimensionPixelSize(34471975);
        float density = context.getResources().getDisplayMetrics().density;
        this.mToolBarMenuItemMaxSize3Arrays = res.getIntArray(33816578);
        int length3 = this.mToolBarMenuItemMaxSize3Arrays.length;
        for (int i = 0; i < length3; i++) {
            int[] iArr = this.mToolBarMenuItemMaxSize3Arrays;
            iArr[i] = (int) (((float) iArr[i]) * density);
        }
        this.mToolBarMenuItemMaxSize4Arrays = res.getIntArray(33816579);
        int length4 = this.mToolBarMenuItemMaxSize4Arrays.length;
        for (int i2 = 0; i2 < length4; i2++) {
            int[] iArr2 = this.mToolBarMenuItemMaxSize4Arrays;
            iArr2[i2] = (int) (((float) iArr2[i2]) * density);
        }
        this.mToolBarMenuItemMaxSize5Arrays = res.getIntArray(33816580);
        int length5 = this.mToolBarMenuItemMaxSize5Arrays.length;
        for (int i3 = 0; i3 < length5; i3++) {
            int[] iArr3 = this.mToolBarMenuItemMaxSize5Arrays;
            iArr3[i3] = (int) (((float) iArr3[i3]) * density);
        }
    }

    public void setSplitViewMaxSize(int maxSize) {
        this.mMaxSize = maxSize;
    }

    public void onSetSmartColor(ColorStateList iconColor, ColorStateList titleColor) {
        this.mSmartIconColorList = iconColor;
        this.mSmartTitleColorList = titleColor;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof HwActionMenuItemView) {
                ((HwActionMenuItemView) child).updateTextAndIcon();
            }
            if (child instanceof HwOverflowMenuButton) {
                ((HwOverflowMenuButton) child).updateTextAndIcon();
            }
        }
    }

    public ColorStateList getSmartIconColor() {
        return this.mSmartIconColorList;
    }

    public ColorStateList getSmartTitleColor() {
        return this.mSmartTitleColorList;
    }
}
