package huawei.android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ActionMenuView;
import android.widget.ActionMenuView.LayoutParams;
import android.widget.TextView;
import com.huawei.android.app.AppOpsManagerEx;

public class HwActionMenuView extends ActionMenuView {
    private static final String TAG = "HwActionMenuView";
    private int mActionBarMenuItemPadding;
    private int mActionBarMenuItemSize;
    private int mActionBarMenuPadding;
    private int mActionBarOverFlowBtnEndPadding;
    private int mActionBarOverFlowBtnStartPadding;
    private Typeface mCondensed;
    private boolean mContain2Lines;
    private boolean mHasOverFlowBtnAtActionBar;
    private boolean mHasVisibleChildAtActionBar;
    private int mMaxSize;
    private int mMinHeight;
    private Parcelable mSavedState;
    private Typeface mSerif;
    private int mTextSize;
    private int[] mToolBarMenuItemMaxSize3;
    private int[] mToolBarMenuItemMaxSize4;
    private int[] mToolBarMenuItemMaxSize5;
    private int mToolBarMenuItemPadding;
    private int mToolBarMenuPadding;

    private static class ItemSpec {
        int itemSize;
        int maxSize;
        int measuredSize;
        int usingSmallFont;
        boolean wasTooLong;

        /* synthetic */ ItemSpec(ItemSpec -this0) {
            this();
        }

        private ItemSpec() {
        }
    }

    public HwActionMenuView(Context context) {
        this(context, null);
    }

    public HwActionMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMinHeight = 0;
        this.mContain2Lines = false;
        this.mHasVisibleChildAtActionBar = false;
        this.mHasOverFlowBtnAtActionBar = false;
        this.mMinHeight = context.getResources().getDimensionPixelSize(34471968);
        initWidths(getContext());
        this.mSerif = Typeface.create("dinnext-serif", 0);
        this.mCondensed = Typeface.create("dinnext-condensed", 0);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (((View) getParent()).getId() == 16909314) {
            onMeasureAtSplitView(widthMeasureSpec, heightMeasureSpec);
            setPadding(0, 0, 0, 0);
            return;
        }
        onMeasureAtActionBar(widthMeasureSpec, heightMeasureSpec);
        if (!this.mHasVisibleChildAtActionBar) {
            setPadding(0, 0, 0, 0);
        } else if (isLayoutRtl()) {
            if (this.mHasOverFlowBtnAtActionBar) {
                setPadding(this.mActionBarOverFlowBtnEndPadding, 0, 0, 0);
            } else {
                setPadding(this.mActionBarMenuPadding, 0, 0, 0);
            }
        } else if (this.mHasOverFlowBtnAtActionBar) {
            setPadding(0, 0, this.mActionBarOverFlowBtnEndPadding, 0);
        } else {
            setPadding(0, 0, this.mActionBarMenuPadding, 0);
        }
    }

    protected void onMeasureAtActionBar(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthPadding = this.mActionBarMenuPadding;
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int itemWidthSpec = MeasureSpec.makeMeasureSpec(this.mActionBarMenuItemSize, AppOpsManagerEx.TYPE_NET);
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding, -2);
        int childCount = getChildCount();
        int totalWidth = 0;
        this.mHasVisibleChildAtActionBar = false;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                child.setPadding(0, child.getPaddingTop(), 0, child.getPaddingBottom());
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                child.measure(itemWidthSpec, itemHeightSpec);
                totalWidth += child.getMeasuredWidth();
                if (i < childCount - 1) {
                    if (isNextItemOverFlowBtn(i)) {
                        totalWidth += this.mActionBarOverFlowBtnStartPadding;
                    } else {
                        totalWidth += this.mActionBarMenuItemPadding;
                    }
                }
                if (i == childCount - 1 && lp.isOverflowButton) {
                    this.mHasOverFlowBtnAtActionBar = true;
                }
                this.mHasVisibleChildAtActionBar = true;
            }
        }
        if (this.mHasOverFlowBtnAtActionBar) {
            widthPadding = this.mActionBarOverFlowBtnEndPadding;
        }
        totalWidth += widthPadding;
        if (!this.mHasVisibleChildAtActionBar) {
            totalWidth = 0;
        }
        setMeasuredDimension(totalWidth, heightSize);
    }

    private boolean isNextItemOverFlowBtn(int itemnum) {
        int childCount = getChildCount();
        if (childCount >= 2 && itemnum < childCount - 1 && ((LayoutParams) getChildAt(itemnum + 1).getLayoutParams()).isOverflowButton) {
            return true;
        }
        return false;
    }

    protected void onMeasureAtSplitView(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), -2);
        int visibleItemCount = 0;
        int childTotalWidth = widthSize;
        View container = (View) ((View) ((View) getParent()).getParent()).getParent();
        if (container != null) {
            int widthPadding = this.mToolBarMenuPadding * 2;
            int containerWidth = container.getWidth();
            int containerHeight = container.getHeight();
            if (containerWidth >= containerHeight) {
                containerWidth = containerHeight;
            }
            int parentWidth = containerWidth - widthPadding;
            childTotalWidth = parentWidth > widthSize ? parentWidth : widthSize;
        }
        if (this.mMaxSize != 0) {
            childTotalWidth = this.mMaxSize - (this.mToolBarMenuPadding * 2);
        }
        int avgCellSizeRemaining = 0;
        int width = 0;
        int childCount = getChildCount();
        for (i = 0; i < childCount; i++) {
            if (getChildAt(i).getVisibility() != 8) {
                visibleItemCount++;
            }
        }
        if (visibleItemCount <= 0) {
            setMeasuredDimension(widthSize, 0);
        } else {
            int cellSize;
            TextView child;
            int index;
            if (visibleItemCount < 4) {
                cellSize = childTotalWidth / 4;
                avgCellSizeRemaining = (childTotalWidth - (cellSize * visibleItemCount)) / visibleItemCount;
            } else {
                cellSize = childTotalWidth / visibleItemCount;
            }
            ItemSpec[] items = new ItemSpec[visibleItemCount];
            int gone = 0;
            int maxDiff = 0;
            for (i = 0; i < childCount; i++) {
                child = (TextView) getChildAt(i);
                index = i - gone;
                if (child.getVisibility() != 8) {
                    items[index] = new ItemSpec();
                    items[index].usingSmallFont = -1;
                    items[index].itemSize = cellSize;
                    items[index].maxSize = getItemMaxSize(index, visibleItemCount);
                    child.setSingleLine(true);
                    child.setMaxLines(1);
                    child.setTypeface(this.mSerif);
                    child.setTranslationX(0.0f);
                    child.setHwCompoundPadding(0, 0, 0, 0);
                    int requiredWidth = ((int) child.getPaint().measureText(child.getText().toString())) + (this.mToolBarMenuItemPadding * 2);
                    items[index].measuredSize = requiredWidth;
                    if (requiredWidth > cellSize) {
                        items[index].wasTooLong = true;
                        if (requiredWidth - cellSize > maxDiff) {
                            maxDiff = requiredWidth - cellSize;
                        }
                    } else {
                        items[index].wasTooLong = false;
                    }
                } else {
                    gone++;
                }
            }
            if (maxDiff == 0 || maxDiff < avgCellSizeRemaining || visibleItemCount == 1) {
                int cellSizeAdding = 0;
                if (maxDiff > 0 && maxDiff < avgCellSizeRemaining) {
                    cellSizeAdding = maxDiff;
                }
                for (i = 0; i < childCount; i++) {
                    child = (TextView) getChildAt(i);
                    if (child.getVisibility() != 8) {
                        child.setPadding(this.mToolBarMenuItemPadding, child.getPaddingTop(), this.mToolBarMenuItemPadding, child.getPaddingBottom());
                        if (visibleItemCount == 1) {
                            child.measure(MeasureSpec.makeMeasureSpec(cellSize > items[0].measuredSize ? cellSize : items[0].measuredSize, 1073741824), itemHeightSpec);
                        } else {
                            child.measure(MeasureSpec.makeMeasureSpec(cellSize + cellSizeAdding, 1073741824), itemHeightSpec);
                        }
                        width += child.getMeasuredWidth();
                    }
                }
                setMeasuredDimension(width, this.mMinHeight);
                return;
            }
            int height;
            int[] standardIconPos = new int[visibleItemCount];
            cellSize += avgCellSizeRemaining;
            for (i = 0; i < items.length; i++) {
                items[i].itemSize = cellSize;
                standardIconPos[i] = (cellSize * i) + ((cellSize - this.mActionBarMenuItemSize) / 2);
                if (items[i].measuredSize > cellSize) {
                    items[i].wasTooLong = true;
                } else {
                    items[i].wasTooLong = false;
                }
            }
            gone = 0;
            for (i = 0; i < childCount; i++) {
                child = (TextView) getChildAt(i);
                index = i - gone;
                if (child.getVisibility() == 8) {
                    gone++;
                } else if (items[index].wasTooLong) {
                    adjustSizeFromsiblings(child, itemHeightSpec, items, index);
                    if (items[index].usingSmallFont == 0 && items[index].wasTooLong) {
                        adjustSizeFromsiblings(child, itemHeightSpec, items, index);
                    }
                }
            }
            gone = 0;
            int sumSize = 0;
            for (i = 0; i < childCount; i++) {
                child = (TextView) getChildAt(i);
                index = i - gone;
                if (child.getVisibility() != 8) {
                    child.setPadding(this.mToolBarMenuItemPadding, child.getPaddingTop(), this.mToolBarMenuItemPadding, child.getPaddingBottom());
                    int iconTrans = standardIconPos[index] - (sumSize + ((items[index].itemSize - this.mActionBarMenuItemSize) / 2));
                    boolean textTrans = false;
                    if (iconTrans < 0) {
                        if ((this.mToolBarMenuItemPadding + sumSize) + ((items[index].itemSize - items[index].measuredSize) / 2) > standardIconPos[index]) {
                            child.setTranslationX((float) iconTrans);
                            textTrans = true;
                        }
                    } else {
                        if ((sumSize - this.mToolBarMenuItemPadding) + ((items[index].itemSize + items[index].measuredSize) / 2) < standardIconPos[index] + this.mActionBarMenuItemSize) {
                            child.setTranslationX((float) iconTrans);
                            textTrans = true;
                        }
                    }
                    if (!textTrans) {
                        child.setHwCompoundPadding(iconTrans, 0, 0, 0);
                    }
                    sumSize += items[index].itemSize;
                    child.measure(MeasureSpec.makeMeasureSpec(items[index].itemSize, 1073741824), itemHeightSpec);
                    width += child.getMeasuredWidth();
                } else {
                    gone++;
                }
            }
            gone = 0;
            this.mContain2Lines = false;
            for (i = 0; i < childCount; i++) {
                child = (TextView) getChildAt(i);
                index = i - gone;
                if (child.getVisibility() == 8) {
                    gone++;
                } else if (items[index].wasTooLong) {
                    child.setTypeface(this.mCondensed);
                    if (((int) child.getPaint().measureText(child.getText().toString())) + (this.mToolBarMenuItemPadding * 2) > items[index].itemSize) {
                        child.setSingleLine(false);
                        child.setMaxLines(2);
                        this.mContain2Lines = true;
                    }
                    child.measure(MeasureSpec.makeMeasureSpec(items[index].itemSize, 1073741824), itemHeightSpec);
                }
            }
            if (this.mContain2Lines) {
                height = this.mMinHeight + this.mTextSize;
            } else {
                height = this.mMinHeight;
            }
            setMeasuredDimension(width, height);
        }
    }

    private void adjustSizeFromsiblings(TextView child, int itemHeightSpec, ItemSpec[] items, int index) {
        if (index == 0 && items.length > 1) {
            adjustSize(child, itemHeightSpec, items, index, 1);
        } else if (index == items.length - 1 && items.length > 1) {
            adjustSize(child, itemHeightSpec, items, index, -1);
        } else if (items.length <= 2) {
        } else {
            if (!items[index - 1].wasTooLong && items[index + 1].wasTooLong) {
                adjustSize(child, itemHeightSpec, items, index, -1);
            } else if (!items[index + 1].wasTooLong && items[index - 1].wasTooLong) {
                adjustSize(child, itemHeightSpec, items, index, 1);
            } else if (!items[index - 1].wasTooLong && (items[index + 1].wasTooLong ^ 1) != 0) {
                adjustSize(child, itemHeightSpec, items, index, -1, 1);
            }
        }
    }

    private void adjustSize(TextView child, int itemHeightSpec, ItemSpec[] items, int index, int diff) {
        int need = index;
        int brother = index + diff;
        if (!items[brother].wasTooLong) {
            int needMeasuredSize = items[index].measuredSize;
            boolean stillTooLong = false;
            if (items[index].measuredSize > items[index].maxSize && items[index].maxSize > 0) {
                needMeasuredSize = items[index].maxSize;
                stillTooLong = true;
            }
            int needDiff = needMeasuredSize - items[index].itemSize;
            int overflowDiff = items[brother].itemSize - items[brother].measuredSize;
            ItemSpec itemSpec;
            if (needDiff < overflowDiff) {
                itemSpec = items[index];
                itemSpec.itemSize += needDiff;
                items[index].wasTooLong = stillTooLong;
                itemSpec = items[brother];
                itemSpec.itemSize -= needDiff;
            } else if (overflowDiff <= 0) {
            } else {
                if (items[index].usingSmallFont == -1) {
                    child.setTypeface(this.mCondensed);
                    int requiredWidthSmallFont = ((int) child.getPaint().measureText(child.getText().toString())) + (this.mToolBarMenuItemPadding * 2);
                    items[index].measuredSize = requiredWidthSmallFont;
                    items[index].wasTooLong = items[index].itemSize < requiredWidthSmallFont;
                    items[index].usingSmallFont = 0;
                } else if (items[index].usingSmallFont == 0) {
                    items[index].usingSmallFont = 1;
                    itemSpec = items[index];
                    itemSpec.itemSize += overflowDiff;
                    items[index].wasTooLong = true;
                    itemSpec = items[brother];
                    itemSpec.itemSize -= overflowDiff;
                }
            }
        }
    }

    private void adjustSize(TextView child, int itemHeightSpec, ItemSpec[] items, int index, int diff1, int diff2) {
        int need = index;
        int brother1 = index + diff1;
        int brother2 = index + diff2;
        int overflowDiff1 = items[brother1].itemSize - items[brother1].measuredSize;
        int overflowDiff2 = items[brother2].itemSize - items[brother2].measuredSize;
        int min = brother1;
        int minOverflowDiff = overflowDiff1;
        int max = brother2;
        int maxOverflowDiff = overflowDiff2;
        if (overflowDiff1 > overflowDiff2) {
            max = brother1;
            maxOverflowDiff = overflowDiff1;
            min = brother2;
            minOverflowDiff = overflowDiff2;
        }
        int needMeasuredSize = items[index].measuredSize;
        boolean stillTooLong = false;
        if (items[index].measuredSize > items[index].maxSize && items[index].maxSize > 0) {
            needMeasuredSize = items[index].maxSize;
            stillTooLong = true;
        }
        int needDiff = needMeasuredSize - items[index].itemSize;
        ItemSpec itemSpec;
        if (needDiff <= maxOverflowDiff + minOverflowDiff) {
            int midNeedDiff = needDiff / 2;
            if (midNeedDiff <= minOverflowDiff) {
                itemSpec = items[index];
                itemSpec.itemSize += needDiff;
                items[index].wasTooLong = stillTooLong;
                itemSpec = items[min];
                itemSpec.itemSize -= midNeedDiff;
                itemSpec = items[max];
                itemSpec.itemSize -= needDiff - midNeedDiff;
            } else if (midNeedDiff <= maxOverflowDiff) {
                itemSpec = items[index];
                itemSpec.itemSize += needDiff;
                items[index].wasTooLong = stillTooLong;
                itemSpec = items[min];
                itemSpec.itemSize -= minOverflowDiff;
                itemSpec = items[max];
                itemSpec.itemSize -= needDiff - minOverflowDiff;
            }
        } else if (items[index].usingSmallFont == -1) {
            child.setTypeface(this.mCondensed);
            int requiredWidthSmallFont = ((int) child.getPaint().measureText(child.getText().toString())) + (this.mToolBarMenuItemPadding * 2);
            items[index].measuredSize = requiredWidthSmallFont;
            items[index].wasTooLong = items[index].itemSize < requiredWidthSmallFont;
            items[index].usingSmallFont = 0;
        } else if (items[index].usingSmallFont == 0) {
            items[index].usingSmallFont = 1;
            itemSpec = items[index];
            itemSpec.itemSize += minOverflowDiff + maxOverflowDiff;
            items[index].wasTooLong = true;
            itemSpec = items[min];
            itemSpec.itemSize -= minOverflowDiff;
            itemSpec = items[max];
            itemSpec.itemSize -= maxOverflowDiff;
        }
    }

    private int getItemMaxSize(int index, int visibleItemCount) {
        if (visibleItemCount == 3) {
            return this.mToolBarMenuItemMaxSize3[index];
        }
        if (visibleItemCount == 4) {
            return this.mToolBarMenuItemMaxSize4[index];
        }
        if (visibleItemCount == 5) {
            return this.mToolBarMenuItemMaxSize5[index];
        }
        return 0;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        boolean isLayoutRtl = isLayoutRtl();
        boolean isToolBar = getParent() != null ? ((View) getParent()).getId() == 16909314 : getContext().getResources().getConfiguration().orientation == 1;
        int i;
        View v;
        LayoutParams lp;
        int width;
        int height;
        int t;
        if (isLayoutRtl) {
            int startRight = getWidth() - getStartRight(isToolBar);
            for (i = 0; i < childCount; i++) {
                v = getChildAt(i);
                lp = (LayoutParams) v.getLayoutParams();
                if (v.getVisibility() != 8) {
                    startRight -= lp.rightMargin;
                    width = v.getMeasuredWidth();
                    height = v.getMeasuredHeight();
                    t = getLayoutTop(isToolBar, top, bottom, v);
                    v.layout(startRight - width, t, startRight, t + height);
                    startRight -= (lp.leftMargin + width) + getItemPadding(isToolBar, i);
                }
            }
            return;
        }
        int startLeft = getStartLeft(isToolBar);
        for (i = 0; i < childCount; i++) {
            v = getChildAt(i);
            lp = (LayoutParams) v.getLayoutParams();
            if (v.getVisibility() != 8) {
                startLeft += lp.leftMargin;
                width = v.getMeasuredWidth();
                height = v.getMeasuredHeight();
                t = getLayoutTop(isToolBar, top, bottom, v);
                v.layout(startLeft, t, startLeft + width, t + height);
                startLeft += (lp.rightMargin + width) + getItemPadding(isToolBar, i);
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

    protected boolean hasDividerBeforeChildAt(int childIndex) {
        return false;
    }

    public void onDetachedFromWindow() {
        if ((getPresenter() instanceof HwActionMenuPresenter) && (((HwActionMenuPresenter) getPresenter()).isPopupMenuShowing() || getPresenter().isOverflowMenuShowing())) {
            this.mSavedState = getPresenter().onSaveInstanceState();
        }
        if (getPresenter() != null) {
            getPresenter().dismissPopupMenus();
        }
        super.onDetachedFromWindow();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if ((getPresenter() instanceof HwActionMenuPresenter) && this.mSavedState != null) {
            if (!((HwActionMenuPresenter) getPresenter()).isPopupMenuShowing() || (getPresenter().isOverflowMenuShowing() ^ 1) != 0) {
                getPresenter().onRestoreInstanceState(this.mSavedState);
                this.mSavedState = null;
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mMinHeight = getContext().getResources().getDimensionPixelSize(34471968);
    }

    private void initWidths(Context context) {
        int i;
        DisplayMetrics dp = context.getResources().getDisplayMetrics();
        Resources res = context.getResources();
        this.mTextSize = res.getDimensionPixelSize(34471965) + res.getDimensionPixelSize(34471967);
        this.mActionBarMenuItemPadding = res.getDimensionPixelSize(34471972);
        this.mActionBarMenuItemSize = res.getDimensionPixelSize(34471973);
        this.mActionBarMenuPadding = res.getDimensionPixelSize(34471971);
        this.mToolBarMenuItemPadding = res.getDimensionPixelSize(34471970);
        this.mToolBarMenuPadding = res.getDimensionPixelSize(34471969);
        this.mActionBarOverFlowBtnStartPadding = res.getDimensionPixelSize(34471974);
        this.mActionBarOverFlowBtnEndPadding = res.getDimensionPixelSize(34471975);
        float density = dp.density;
        this.mToolBarMenuItemMaxSize3 = res.getIntArray(33816578);
        for (i = 0; i < this.mToolBarMenuItemMaxSize3.length; i++) {
            this.mToolBarMenuItemMaxSize3[i] = (int) (((float) this.mToolBarMenuItemMaxSize3[i]) * density);
        }
        this.mToolBarMenuItemMaxSize4 = res.getIntArray(33816579);
        for (i = 0; i < this.mToolBarMenuItemMaxSize4.length; i++) {
            this.mToolBarMenuItemMaxSize4[i] = (int) (((float) this.mToolBarMenuItemMaxSize4[i]) * density);
        }
        this.mToolBarMenuItemMaxSize5 = res.getIntArray(33816580);
        for (i = 0; i < this.mToolBarMenuItemMaxSize5.length; i++) {
            this.mToolBarMenuItemMaxSize5[i] = (int) (((float) this.mToolBarMenuItemMaxSize5[i]) * density);
        }
    }

    public void setSplitViewMaxSize(int maxSize) {
        this.mMaxSize = maxSize;
    }
}
