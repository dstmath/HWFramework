package huawei.android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.TextView;
import huawei.com.android.internal.view.menu.HwActionMenuItemView;

public class HwActionMenuView extends ActionMenuView implements HwSmartColorListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "HwActionMenuView";
    private int mActionBarMenuItemPadding;
    private int mActionBarMenuItemSize;
    private int mActionBarMenuPadding;
    private int mActionBarOverFlowBtnEndPadding;
    private int mActionBarOverFlowBtnSize;
    private int mActionBarOverFlowBtnStartPadding;
    private Typeface mCondensed;
    private boolean mContain2Lines;
    private boolean mHasOverFlowBtnAtActionBar;
    private boolean mHasVisibleChildAtActionBar;
    private int mMaxSize;
    private int mMinHeight;
    private ActionMenuPresenter mPresenter;
    private Parcelable mSavedState;
    private Typeface mSerif;
    private ColorStateList mSmartIconColor;
    private ColorStateList mSmartTitleColor;
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
        Log.d(TAG, "new HwActionMenuView");
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
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (((View) getParent()).getId() == 16909362) {
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

    /* access modifiers changed from: protected */
    public void onMeasureAtActionBar(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthPadding = this.mActionBarMenuPadding;
        int itemWidthSpec = View.MeasureSpec.makeMeasureSpec(this.mActionBarMenuItemSize, 1073741824);
        int itemHeightSpec = View.MeasureSpec.makeMeasureSpec(this.mActionBarMenuItemSize, 1073741824);
        int childCount = getChildCount();
        int totalWidth = 0;
        int i = 0;
        this.mHasVisibleChildAtActionBar = false;
        int itemWidthSpec2 = itemWidthSpec;
        for (int i2 = 0; i2 < childCount; i2++) {
            View child = getChildAt(i2);
            if (child.getVisibility() != 8) {
                child.setPadding(0, child.getPaddingTop(), 0, child.getPaddingBottom());
                ActionMenuView.LayoutParams lp = (ActionMenuView.LayoutParams) child.getLayoutParams();
                if (lp.isOverflowButton) {
                    itemWidthSpec2 = View.MeasureSpec.makeMeasureSpec(this.mActionBarOverFlowBtnSize, 1073741824);
                }
                child.measure(itemWidthSpec2, itemHeightSpec);
                totalWidth += child.getMeasuredWidth();
                if (i2 < childCount - 1) {
                    if (isNextItemOverFlowBtn(i2)) {
                        totalWidth += this.mActionBarOverFlowBtnStartPadding;
                    } else {
                        totalWidth += this.mActionBarMenuItemPadding;
                    }
                }
                if (i2 == childCount - 1 && lp.isOverflowButton) {
                    this.mHasOverFlowBtnAtActionBar = DEBUG;
                }
                this.mHasVisibleChildAtActionBar = DEBUG;
            }
        }
        int totalWidth2 = (this.mHasOverFlowBtnAtActionBar != 0 ? this.mActionBarOverFlowBtnEndPadding : widthPadding) + totalWidth;
        if (this.mHasVisibleChildAtActionBar) {
            i = totalWidth2;
        }
        setMeasuredDimension(i, heightSize);
    }

    private boolean isNextItemOverFlowBtn(int itemnum) {
        int childCount = getChildCount();
        if (childCount >= 2 && itemnum < childCount - 1 && ((ActionMenuView.LayoutParams) getChildAt(itemnum + 1).getLayoutParams()).isOverflowButton) {
            return DEBUG;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onMeasureAtSplitView(int widthMeasureSpec, int heightMeasureSpec) {
        int cellSize;
        int avgCellSizeRemaining;
        int visibleItemCount;
        int visibleItemCount2;
        int height;
        int avgCellSizeRemaining2;
        int visibleItemCount3;
        boolean textTrans;
        View actionbarOverlayLayout;
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding, -2);
        int childTotalWidth = widthSize;
        View actionbarOverlayLayout2 = (View) ((View) getParent()).getParent();
        View container = (View) actionbarOverlayLayout2.getParent();
        if (container != null) {
            int widthPadding = this.mToolBarMenuPadding * 2;
            int containerWidth = container.getWidth();
            int containerHeight = container.getHeight();
            int parentWidth = (containerWidth < containerHeight ? containerWidth : containerHeight) - widthPadding;
            childTotalWidth = parentWidth > widthSize ? parentWidth : widthSize;
        }
        if (this.mMaxSize != 0) {
            childTotalWidth = this.mMaxSize - (this.mToolBarMenuPadding * 2);
        }
        int avgCellSizeRemaining3 = 0;
        int width = 0;
        int childCount = getChildCount();
        int visibleItemCount4 = 0;
        int i = 0;
        while (i < childCount) {
            int heightPadding2 = heightPadding;
            if (getChildAt(i).getVisibility() != 8) {
                visibleItemCount4++;
            }
            i++;
            heightPadding = heightPadding2;
        }
        if (visibleItemCount4 <= 0) {
            setMeasuredDimension(widthSize, 0);
            int i2 = widthSize;
            int i3 = childTotalWidth;
            View view = actionbarOverlayLayout2;
            View view2 = container;
            int i4 = visibleItemCount4;
        } else {
            if (visibleItemCount4 < 4) {
                cellSize = childTotalWidth / 4;
                avgCellSizeRemaining3 = (childTotalWidth - (cellSize * visibleItemCount4)) / visibleItemCount4;
            } else {
                cellSize = childTotalWidth / visibleItemCount4;
            }
            ItemSpec[] items = new ItemSpec[visibleItemCount4];
            int maxDiff = 0;
            int gone = 0;
            int i5 = 0;
            while (i5 < childCount) {
                TextView child = (TextView) getChildAt(i5);
                int widthSize2 = widthSize;
                int widthSize3 = i5 - gone;
                int childTotalWidth2 = childTotalWidth;
                if (child.getVisibility() != 8) {
                    items[widthSize3] = new ItemSpec();
                    items[widthSize3].usingSmallFont = -1;
                    items[widthSize3].itemSize = cellSize;
                    items[widthSize3].maxSize = getItemMaxSize(widthSize3, visibleItemCount4);
                    child.setSingleLine(DEBUG);
                    child.setMaxLines(1);
                    child.setTypeface(this.mSerif);
                    child.setTranslationX(0.0f);
                    child.setHwCompoundPadding(0, 0, 0, 0);
                    int requiredWidth = ((int) child.getPaint().measureText(child.getText().toString())) + (this.mToolBarMenuItemPadding * 2);
                    items[widthSize3].measuredSize = requiredWidth;
                    if (requiredWidth > cellSize) {
                        actionbarOverlayLayout = actionbarOverlayLayout2;
                        items[widthSize3].wasTooLong = DEBUG;
                        int maxDiff2 = maxDiff;
                        maxDiff = requiredWidth - cellSize > maxDiff2 ? requiredWidth - cellSize : maxDiff2;
                        int i6 = widthSize3;
                    } else {
                        actionbarOverlayLayout = actionbarOverlayLayout2;
                        int i7 = maxDiff;
                        int i8 = widthSize3;
                        items[widthSize3].wasTooLong = false;
                    }
                } else {
                    int index = widthSize3;
                    actionbarOverlayLayout = actionbarOverlayLayout2;
                    int i9 = maxDiff;
                    gone++;
                }
                i5++;
                widthSize = widthSize2;
                childTotalWidth = childTotalWidth2;
                actionbarOverlayLayout2 = actionbarOverlayLayout;
                int i10 = heightMeasureSpec;
            }
            int i11 = childTotalWidth;
            View view3 = actionbarOverlayLayout2;
            int maxDiff3 = maxDiff;
            if (maxDiff3 == 0 || maxDiff3 < avgCellSizeRemaining3) {
                avgCellSizeRemaining = avgCellSizeRemaining3;
                visibleItemCount = visibleItemCount4;
            } else if (visibleItemCount4 == 1) {
                View view4 = container;
                avgCellSizeRemaining = avgCellSizeRemaining3;
                visibleItemCount = visibleItemCount4;
            } else {
                int[] standardIconPos = new int[visibleItemCount4];
                int cellSize2 = cellSize + avgCellSizeRemaining3;
                for (int i12 = 0; i12 < items.length; i12++) {
                    items[i12].itemSize = cellSize2;
                    standardIconPos[i12] = (cellSize2 * i12) + ((cellSize2 - this.mActionBarMenuItemSize) / 2);
                    if (items[i12].measuredSize > cellSize2) {
                        items[i12].wasTooLong = DEBUG;
                    } else {
                        items[i12].wasTooLong = false;
                    }
                }
                int gone2 = 0;
                int i13 = 0;
                while (i13 < childCount) {
                    TextView child2 = (TextView) getChildAt(i13);
                    int index2 = i13 - gone2;
                    View container2 = container;
                    if (child2.getVisibility() == 8) {
                        gone2++;
                    } else if (items[index2].wasTooLong) {
                        adjustSizeFromsiblings(child2, itemHeightSpec, items, index2);
                        if (items[index2].usingSmallFont == 0 && items[index2].wasTooLong) {
                            adjustSizeFromsiblings(child2, itemHeightSpec, items, index2);
                        }
                    }
                    i13++;
                    container = container2;
                }
                int sumSize = 0;
                int gone3 = 0;
                int i14 = 0;
                while (i14 < childCount) {
                    TextView child3 = (TextView) getChildAt(i14);
                    int index3 = i14 - gone3;
                    int cellSize3 = cellSize2;
                    if (child3.getVisibility() != 8) {
                        visibleItemCount3 = visibleItemCount4;
                        avgCellSizeRemaining2 = avgCellSizeRemaining3;
                        child3.setPadding(this.mToolBarMenuItemPadding, child3.getPaddingTop(), this.mToolBarMenuItemPadding, child3.getPaddingBottom());
                        int iconPosition = ((items[index3].itemSize - this.mActionBarMenuItemSize) / 2) + sumSize;
                        int iconTrans = standardIconPos[index3] - iconPosition;
                        if (iconTrans < 0) {
                            int i15 = iconPosition;
                            if (this.mToolBarMenuItemPadding + sumSize + ((items[index3].itemSize - items[index3].measuredSize) / 2) > standardIconPos[index3]) {
                                child3.setTranslationX((float) iconTrans);
                                textTrans = true;
                            } else {
                                textTrans = false;
                            }
                        } else {
                            if ((sumSize - this.mToolBarMenuItemPadding) + ((items[index3].itemSize + items[index3].measuredSize) / 2) < standardIconPos[index3] + this.mActionBarMenuItemSize) {
                                child3.setTranslationX((float) iconTrans);
                                textTrans = DEBUG;
                            } else {
                                textTrans = false;
                            }
                        }
                        if (!textTrans) {
                            child3.setHwCompoundPadding(iconTrans, 0, 0, 0);
                        }
                        sumSize += items[index3].itemSize;
                        child3.measure(View.MeasureSpec.makeMeasureSpec(items[index3].itemSize, 1073741824), itemHeightSpec);
                        width += child3.getMeasuredWidth();
                    } else {
                        avgCellSizeRemaining2 = avgCellSizeRemaining3;
                        visibleItemCount3 = visibleItemCount4;
                        gone3++;
                    }
                    i14++;
                    cellSize2 = cellSize3;
                    visibleItemCount4 = visibleItemCount3;
                    avgCellSizeRemaining3 = avgCellSizeRemaining2;
                }
                int avgCellSizeRemaining4 = avgCellSizeRemaining3;
                int i16 = visibleItemCount4;
                this.mContain2Lines = false;
                int gone4 = 0;
                for (int i17 = 0; i17 < childCount; i17++) {
                    TextView child4 = (TextView) getChildAt(i17);
                    int index4 = i17 - gone4;
                    if (child4.getVisibility() == 8) {
                        gone4++;
                    } else if (items[index4].wasTooLong) {
                        child4.setTypeface(this.mCondensed);
                        if (((int) child4.getPaint().measureText(child4.getText().toString())) + (this.mToolBarMenuItemPadding * 2) > items[index4].itemSize) {
                            child4.setSingleLine(false);
                            child4.setMaxLines(2);
                            this.mContain2Lines = DEBUG;
                        }
                        child4.measure(View.MeasureSpec.makeMeasureSpec(items[index4].itemSize, 1073741824), itemHeightSpec);
                    }
                }
                if (this.mContain2Lines) {
                    height = this.mMinHeight + this.mTextSize;
                } else {
                    height = this.mMinHeight;
                }
                setMeasuredDimension(width, height);
                int i18 = avgCellSizeRemaining4;
            }
            int cellSizeAdding = 0;
            if (maxDiff3 <= 0) {
            } else if (maxDiff3 < avgCellSizeRemaining) {
                cellSizeAdding = maxDiff3;
            }
            int i19 = 0;
            while (i19 < childCount) {
                TextView child5 = (TextView) getChildAt(i19);
                if (child5.getVisibility() != 8) {
                    child5.setPadding(this.mToolBarMenuItemPadding, child5.getPaddingTop(), this.mToolBarMenuItemPadding, child5.getPaddingBottom());
                    visibleItemCount2 = visibleItemCount;
                    if (visibleItemCount2 == 1) {
                        child5.measure(View.MeasureSpec.makeMeasureSpec(cellSize > items[0].measuredSize ? cellSize : items[0].measuredSize, 1073741824), itemHeightSpec);
                    } else {
                        child5.measure(View.MeasureSpec.makeMeasureSpec(cellSize + cellSizeAdding, 1073741824), itemHeightSpec);
                    }
                    width += child5.getMeasuredWidth();
                } else {
                    visibleItemCount2 = visibleItemCount;
                }
                i19++;
                visibleItemCount = visibleItemCount2;
            }
            setMeasuredDimension(width, this.mMinHeight);
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
            } else if (!items[index - 1].wasTooLong && !items[index + 1].wasTooLong) {
                adjustSize(child, itemHeightSpec, items, index, -1, 1);
            }
        }
    }

    private void adjustSize(TextView child, int itemHeightSpec, ItemSpec[] items, int index, int diff) {
        int need = index;
        int brother = index + diff;
        if (!items[brother].wasTooLong) {
            int needMeasuredSize = items[need].measuredSize;
            boolean stillTooLong = false;
            if (items[need].measuredSize > items[need].maxSize && items[need].maxSize > 0) {
                needMeasuredSize = items[need].maxSize;
                stillTooLong = DEBUG;
            }
            int needDiff = needMeasuredSize - items[need].itemSize;
            int overflowDiff = items[brother].itemSize - items[brother].measuredSize;
            if (needDiff < overflowDiff) {
                items[need].itemSize += needDiff;
                items[need].wasTooLong = stillTooLong;
                items[brother].itemSize -= needDiff;
            } else if (overflowDiff > 0) {
                int i = items[need].usingSmallFont;
                boolean z = DEBUG;
                if (i == -1) {
                    TextView textView = child;
                    textView.setTypeface(this.mCondensed);
                    int requiredWidthSmallFont = ((int) textView.getPaint().measureText(textView.getText().toString())) + (2 * this.mToolBarMenuItemPadding);
                    items[need].measuredSize = requiredWidthSmallFont;
                    ItemSpec itemSpec = items[need];
                    if (items[need].itemSize >= requiredWidthSmallFont) {
                        z = false;
                    }
                    itemSpec.wasTooLong = z;
                    items[need].usingSmallFont = 0;
                    return;
                }
                TextView textView2 = child;
                if (items[need].usingSmallFont == 0) {
                    items[need].usingSmallFont = 1;
                    items[need].itemSize += overflowDiff;
                    items[need].wasTooLong = DEBUG;
                    items[brother].itemSize -= overflowDiff;
                    return;
                }
                return;
            }
        }
        TextView textView3 = child;
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
        int needMeasuredSize = items[need].measuredSize;
        boolean stillTooLong = false;
        if (items[need].measuredSize > items[need].maxSize && items[need].maxSize > 0) {
            needMeasuredSize = items[need].maxSize;
            stillTooLong = DEBUG;
        }
        int needDiff = needMeasuredSize - items[need].itemSize;
        if (needDiff <= maxOverflowDiff + minOverflowDiff) {
            int midNeedDiff = needDiff / 2;
            if (midNeedDiff <= minOverflowDiff) {
                int i = brother1;
                items[need].itemSize += needDiff;
                items[need].wasTooLong = stillTooLong;
                items[min].itemSize -= midNeedDiff;
                items[max].itemSize -= needDiff - midNeedDiff;
            } else {
                if (midNeedDiff <= maxOverflowDiff) {
                    items[need].itemSize += needDiff;
                    items[need].wasTooLong = stillTooLong;
                    items[min].itemSize -= minOverflowDiff;
                    items[max].itemSize -= needDiff - minOverflowDiff;
                }
            }
            TextView textView = child;
            int i2 = needDiff;
            return;
        }
        if (items[need].usingSmallFont == -1) {
            child.setTypeface(this.mCondensed);
            int requiredWidthSmallFont = ((int) child.getPaint().measureText(child.getText().toString())) + (2 * this.mToolBarMenuItemPadding);
            items[need].measuredSize = requiredWidthSmallFont;
            int i3 = needDiff;
            items[need].wasTooLong = items[need].itemSize < requiredWidthSmallFont ? DEBUG : false;
            items[need].usingSmallFont = 0;
            return;
        }
        TextView textView2 = child;
        int i4 = needDiff;
        if (items[need].usingSmallFont == 0) {
            items[need].usingSmallFont = 1;
            items[need].itemSize += minOverflowDiff + maxOverflowDiff;
            items[need].wasTooLong = DEBUG;
            items[min].itemSize -= minOverflowDiff;
            items[max].itemSize -= maxOverflowDiff;
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

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean isToolBar;
        int i = top;
        int i2 = bottom;
        int childCount = getChildCount();
        boolean isLayoutRtl = isLayoutRtl();
        ViewParent parent = getParent();
        int i3 = 0;
        boolean z = DEBUG;
        if (parent != null) {
            if (((View) getParent()).getId() != 16909362) {
                z = false;
            }
            isToolBar = z;
        } else {
            if (getContext().getResources().getConfiguration().orientation != 1) {
                z = false;
            }
            isToolBar = z;
        }
        if (isLayoutRtl) {
            int startRight = getWidth() - getStartRight(isToolBar);
            while (i3 < childCount) {
                View v = getChildAt(i3);
                ActionMenuView.LayoutParams lp = (ActionMenuView.LayoutParams) v.getLayoutParams();
                if (v.getVisibility() != 8) {
                    int startRight2 = startRight - lp.rightMargin;
                    int width = v.getMeasuredWidth();
                    int height = v.getMeasuredHeight();
                    int t = getLayoutTop(isToolBar, i, i2, v);
                    v.layout(startRight2 - width, t, startRight2, t + height);
                    startRight = startRight2 - ((lp.leftMargin + width) + getItemPadding(isToolBar, i3));
                }
                i3++;
            }
            return;
        }
        int startLeft = getStartLeft(isToolBar);
        while (i3 < childCount) {
            View v2 = getChildAt(i3);
            ActionMenuView.LayoutParams lp2 = (ActionMenuView.LayoutParams) v2.getLayoutParams();
            if (v2.getVisibility() != 8) {
                int startLeft2 = startLeft + lp2.leftMargin;
                int width2 = v2.getMeasuredWidth();
                int height2 = v2.getMeasuredHeight();
                int t2 = getLayoutTop(isToolBar, i, i2, v2);
                v2.layout(startLeft2, t2, startLeft2 + width2, t2 + height2);
                startLeft = startLeft2 + lp2.rightMargin + width2 + getItemPadding(isToolBar, i3);
            }
            i3++;
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
        return false;
    }

    public void onDetachedFromWindow() {
        if ((this.mPresenter instanceof HwActionMenuPresenter) && (this.mPresenter.isPopupMenuShowing() || this.mPresenter.isOverflowMenuShowing())) {
            this.mSavedState = this.mPresenter.onSaveInstanceState();
        }
        if (this.mPresenter != null) {
            this.mPresenter.dismissPopupMenus();
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if ((this.mPresenter instanceof HwActionMenuPresenter) && this.mSavedState != null) {
            if (!this.mPresenter.isPopupMenuShowing() || !this.mPresenter.isOverflowMenuShowing()) {
                this.mPresenter.onRestoreInstanceState(this.mSavedState);
                this.mSavedState = null;
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mMinHeight = getContext().getResources().getDimensionPixelSize(34471968);
    }

    private void initWidths(Context context) {
        DisplayMetrics dp = context.getResources().getDisplayMetrics();
        Resources res = context.getResources();
        this.mTextSize = res.getDimensionPixelSize(34471965) + res.getDimensionPixelSize(34471967);
        this.mActionBarMenuItemPadding = res.getDimensionPixelSize(34471972);
        this.mActionBarMenuItemSize = res.getDimensionPixelSize(34471973);
        this.mActionBarOverFlowBtnSize = res.getDimensionPixelSize(34472084);
        this.mActionBarMenuPadding = res.getDimensionPixelSize(34471971);
        this.mToolBarMenuItemPadding = res.getDimensionPixelSize(34471970);
        this.mToolBarMenuPadding = res.getDimensionPixelSize(34471969);
        this.mActionBarOverFlowBtnStartPadding = res.getDimensionPixelSize(34471974);
        this.mActionBarOverFlowBtnEndPadding = res.getDimensionPixelSize(34471975);
        float density = dp.density;
        this.mToolBarMenuItemMaxSize3 = res.getIntArray(33816578);
        for (int i = 0; i < this.mToolBarMenuItemMaxSize3.length; i++) {
            this.mToolBarMenuItemMaxSize3[i] = (int) (((float) this.mToolBarMenuItemMaxSize3[i]) * density);
        }
        this.mToolBarMenuItemMaxSize4 = res.getIntArray(33816579);
        for (int i2 = 0; i2 < this.mToolBarMenuItemMaxSize4.length; i2++) {
            this.mToolBarMenuItemMaxSize4[i2] = (int) (((float) this.mToolBarMenuItemMaxSize4[i2]) * density);
        }
        this.mToolBarMenuItemMaxSize5 = res.getIntArray(33816580);
        for (int i3 = 0; i3 < this.mToolBarMenuItemMaxSize5.length; i3++) {
            this.mToolBarMenuItemMaxSize5[i3] = (int) (((float) this.mToolBarMenuItemMaxSize5[i3]) * density);
        }
    }

    public void setSplitViewMaxSize(int maxSize) {
        this.mMaxSize = maxSize;
    }

    public void onSetSmartColor(ColorStateList iconColor, ColorStateList titleColor) {
        this.mSmartIconColor = iconColor;
        this.mSmartTitleColor = titleColor;
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
        return this.mSmartIconColor;
    }

    public ColorStateList getSmartTitleColor() {
        return this.mSmartTitleColor;
    }
}
