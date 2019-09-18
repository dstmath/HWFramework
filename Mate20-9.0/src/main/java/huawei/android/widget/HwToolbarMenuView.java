package huawei.android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import huawei.com.android.internal.view.menu.HwToolbarMenuItemView;
import java.util.ArrayList;

public class HwToolbarMenuView extends ActionMenuView implements HwSmartColorListener {
    private static final boolean DEBUG = true;
    private static final int SPLIT_MENU_AVERAGE_ITEM_NUM = 4;
    private static final String TAG = "HwToolbarMenuView";
    private int mActionBarMenuItemPadding;
    private int mActionBarMenuItemSize;
    private int mActionBarMenuPadding;
    private int mActionBarOverFlowBtnEndPadding;
    private int mActionBarOverFlowBtnSize;
    private int mActionBarOverFlowBtnStartPadding;
    private boolean mHasOverFlowBtnAtActionBar;
    private boolean mHasVisibleChildAtActionBar;
    private HwCutoutUtil mHwCutoutUtil;
    private int mMinHeight;
    private ActionMenuPresenter mPresenter;
    private ResLoader mResLoader;
    private Parcelable mSavedState;
    private ColorStateList mSmartIconColor;
    private ColorStateList mSmartTitleColor;
    private int mSplitMenuDrawablePadding;
    private int mSplitMenuHeightPadding;
    private int mSplitMenuMinTextSize;
    private int mSplitMenuNormalTextSize;
    private int mSplitMenuTextStep;
    private int mSplitMenuTopMargin;
    private int mSplitMenuTotalWidth;
    private int mSplitToolbarMenuItemPadding;
    private int mSplitToolbarMenuItemPaddingLess;

    private static class ItemSpec {
        int endPadding;
        int itemSize;
        int measuredSize;
        int startPadding;
        boolean wasTooLong;

        private ItemSpec() {
        }
    }

    public HwToolbarMenuView(Context context) {
        this(context, null);
    }

    public HwToolbarMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMinHeight = 0;
        this.mHasVisibleChildAtActionBar = false;
        this.mHasOverFlowBtnAtActionBar = false;
        this.mHwCutoutUtil = null;
        Log.d(TAG, "new HwToolbarMenuView");
        this.mResLoader = ResLoader.getInstance();
        this.mMinHeight = this.mResLoader.getResources(context).getDimensionPixelSize(this.mResLoader.getIdentifier(context, ResLoaderUtil.DIMEN, "hwtoolbar_split_menu_height"));
        initSize(context);
        this.mHwCutoutUtil = new HwCutoutUtil();
    }

    public void setPresenter(ActionMenuPresenter presenter) {
        super.setPresenter(presenter);
        this.mPresenter = presenter;
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mHwCutoutUtil.checkCutoutStatus(insets, this, this.mContext);
        return super.onApplyWindowInsets(insets);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (((View) getParent()).getId() == 16909362) {
            onMeasureAtSplitView(widthMeasureSpec, heightMeasureSpec);
            setPadding(0, 0, 0, 0);
            return;
        }
        onMeasureAtActionBar(heightMeasureSpec);
        if (this.mHasVisibleChildAtActionBar) {
            int padding = this.mHasOverFlowBtnAtActionBar ? this.mActionBarOverFlowBtnEndPadding : this.mActionBarMenuPadding;
            if (isLayoutRtl()) {
                setPadding(padding, 0, 0, 0);
            } else {
                setPadding(0, 0, padding, 0);
            }
        } else {
            setPadding(0, 0, 0, 0);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasureAtActionBar(int heightMeasureSpec) {
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

    /* access modifiers changed from: protected */
    public void onMeasureAtSplitView(int widthMeasureSpec, int heightMeasureSpec) {
        int cellSize;
        int maxCellSize;
        int cellSize2;
        int textChildCount;
        int childCount;
        int heightPadding;
        int requireCellSize;
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightPadding2 = getPaddingTop() + getPaddingBottom();
        int widthPadding = this.mSplitToolbarMenuItemPadding * 2;
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding2, -1);
        int menuHeightPadding = this.mSplitMenuHeightPadding * 2;
        int childCount2 = getChildCount();
        int childTotalWidth = widthSize - (childCount2 * widthPadding);
        int visibleItemCount = 0;
        for (int i = 0; i < childCount2; i++) {
            View v = getChildAt(i);
            if (v.getVisibility() != 8) {
                v.setPadding(0, 0, 0, 0);
                visibleItemCount++;
            }
        }
        if (visibleItemCount <= 0) {
            setMeasuredDimension(widthSize, 0);
            return;
        }
        ArrayList<TextView> textChilds = new ArrayList<>();
        ArrayList<ItemSpec> itemSpecs = new ArrayList<>();
        int maxCellSize2 = childTotalWidth / visibleItemCount;
        int childTotalWidth2 = childTotalWidth - this.mHwCutoutUtil.getCutoutPadding();
        if (visibleItemCount > 4) {
            cellSize = childTotalWidth2 / visibleItemCount;
        } else {
            childTotalWidth2 -= this.mSplitToolbarMenuItemPaddingLess * 2;
            cellSize = childTotalWidth2 / 4;
        }
        int cellSize3 = cellSize;
        int requireCellSize2 = cellSize;
        int i2 = 0;
        while (i2 < childCount2) {
            View child = getChildAt(i2);
            if (child instanceof TextView) {
                heightPadding = heightPadding2;
                if (child.getVisibility() != 8) {
                    TextView textChild = (TextView) child;
                    textChilds.add(textChild);
                    ItemSpec itemSpec = new ItemSpec();
                    itemSpec.itemSize = cellSize3;
                    textChild.measure(0, itemHeightSpec);
                    int requiredWidth = textChild.getMeasuredWidth();
                    childCount = childCount2;
                    requireCellSize = requireCellSize2;
                    if (requiredWidth > requireCellSize) {
                        requireCellSize = requiredWidth > maxCellSize2 ? maxCellSize2 : requiredWidth;
                    }
                    itemSpec.measuredSize = requiredWidth;
                    TextView textView = textChild;
                    itemSpec.endPadding = 0;
                    itemSpec.startPadding = 0;
                    itemSpec.wasTooLong = requiredWidth > itemSpec.itemSize;
                    itemSpecs.add(itemSpec);
                } else {
                    childCount = childCount2;
                    requireCellSize = requireCellSize2;
                }
            } else {
                heightPadding = heightPadding2;
                childCount = childCount2;
                requireCellSize = requireCellSize2;
            }
            requireCellSize2 = requireCellSize;
            i2++;
            heightPadding2 = heightPadding;
            childCount2 = childCount;
            int i3 = heightMeasureSpec;
        }
        int i4 = childCount2;
        int requireCellSize3 = requireCellSize2;
        if (requireCellSize3 > cellSize3) {
            int itemSize = itemSpecs.size();
            for (int i5 = 0; i5 < itemSize; i5++) {
                ItemSpec itemSpec2 = itemSpecs.get(i5);
                itemSpec2.itemSize = requireCellSize3;
                itemSpec2.wasTooLong = itemSpec2.measuredSize > requireCellSize3;
            }
        }
        int cellSize4 = textChilds.size();
        int maxItemHeight = this.mMinHeight;
        for (int i6 = 0; i6 < cellSize4; i6++) {
            if (itemSpecs.get(i6) != null && itemSpecs.get(i6).wasTooLong) {
                adjustSize(i6, itemSpecs);
            }
        }
        this.mSplitMenuTotalWidth = 0;
        int maxItemHeight2 = maxItemHeight;
        int i7 = 0;
        while (i7 < cellSize4) {
            TextView textItemView = textChilds.get(i7);
            ItemSpec itemSpec3 = itemSpecs.get(i7);
            if (isLayoutRtl()) {
                textChildCount = cellSize4;
                cellSize2 = cellSize3;
                maxCellSize = maxCellSize2;
                textItemView.setPadding(itemSpec3.endPadding, 0, itemSpec3.startPadding, 0);
            } else {
                textChildCount = cellSize4;
                cellSize2 = cellSize3;
                maxCellSize = maxCellSize2;
                textItemView.setPadding(itemSpec3.startPadding, 0, itemSpec3.endPadding, 0);
            }
            textItemView.setCompoundDrawablePadding(this.mSplitMenuDrawablePadding);
            int i8 = this.mSplitMenuNormalTextSize;
            int i9 = this.mSplitMenuMinTextSize;
            int i10 = this.mSplitMenuTextStep;
            ArrayList<ItemSpec> itemSpecs2 = itemSpecs;
            int i11 = itemSpec3.itemSize;
            int requireCellSize4 = requireCellSize3;
            ItemSpec itemSpec4 = itemSpec3;
            int i12 = i8;
            int cellSize5 = cellSize2;
            TextView textItemView2 = textItemView;
            int maxCellSize3 = maxCellSize;
            ArrayList<ItemSpec> itemSpecs3 = itemSpecs2;
            ArrayList<TextView> textChilds2 = textChilds;
            int visibleItemCount2 = visibleItemCount;
            AutoTextUtils.autoText(i12, i9, i10, 1, i11, itemHeightSpec, textItemView2);
            TextView textItemView3 = textItemView2;
            textItemView3.measure(View.MeasureSpec.makeMeasureSpec(itemSpec4.itemSize, 1073741824), itemHeightSpec);
            this.mSplitMenuTotalWidth += itemSpec4.itemSize + widthPadding;
            int childRequiredHeight = textItemView3.getMeasuredHeight() + menuHeightPadding + this.mSplitMenuTopMargin;
            maxItemHeight2 = maxItemHeight2 > childRequiredHeight ? maxItemHeight2 : childRequiredHeight;
            i7++;
            cellSize3 = cellSize5;
            itemSpecs = itemSpecs3;
            maxCellSize2 = maxCellSize3;
            textChilds = textChilds2;
            visibleItemCount = visibleItemCount2;
            cellSize4 = textChildCount;
            requireCellSize3 = requireCellSize4;
        }
        int textChildCount2 = cellSize4;
        int textChildCount3 = cellSize3;
        int i13 = maxCellSize2;
        ArrayList<ItemSpec> arrayList = itemSpecs;
        ArrayList<TextView> arrayList2 = textChilds;
        int i14 = visibleItemCount;
        int i15 = requireCellSize3;
        setMeasuredDimension(widthSize, maxItemHeight2);
    }

    private boolean isNextItemOverFlowBtn(int itemnum) {
        int childCount = getChildCount();
        if (childCount >= 2 && itemnum < childCount - 1 && ((ActionMenuView.LayoutParams) getChildAt(itemnum + 1).getLayoutParams()).isOverflowButton) {
            return DEBUG;
        }
        return false;
    }

    private void adjustSize(int index, ArrayList<ItemSpec> items) {
        if (index >= items.size() - 1 || index <= 0) {
            Log.i(TAG, "adjustSize: the item index >= count - 1 or <= 0 and index is " + index);
        } else if (items.get(index - 1).wasTooLong || items.get(index + 1).wasTooLong) {
            Log.i(TAG, "adjustSize: " + index + " the pre item was too long or the next item was too long");
        } else {
            ItemSpec preItem = items.get(index - 1);
            ItemSpec nextItem = items.get(index + 1);
            ItemSpec currentItem = items.get(index);
            int diff = currentItem.measuredSize - currentItem.itemSize;
            int preRemainder = (preItem.itemSize - preItem.measuredSize) - preItem.endPadding;
            int nextRemainder = nextItem.itemSize - nextItem.measuredSize;
            int preRemainderEnd = (preRemainder / 2) + preItem.endPadding;
            int nextRemainderStart = nextRemainder / 2;
            int minRemainder = preRemainderEnd < nextRemainderStart ? preRemainderEnd : nextRemainderStart;
            int harfDiff = diff / 2;
            int harfDiff2 = harfDiff < minRemainder ? harfDiff : minRemainder;
            preItem.itemSize -= harfDiff2;
            if (harfDiff2 < preItem.endPadding) {
                preItem.endPadding -= harfDiff2;
            } else {
                preItem.startPadding = harfDiff2 - preItem.endPadding;
                preItem.endPadding = 0;
            }
            currentItem.itemSize += harfDiff2 * 2;
            nextItem.itemSize -= harfDiff2;
            nextItem.endPadding = harfDiff2;
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean isToolBar;
        boolean isLayoutRtl;
        int cutoutPadding;
        int cutoutLeftPadding;
        int cutoutRightPadding;
        int childCount;
        int childCount2 = getChildCount();
        boolean isLayoutRtl2 = isLayoutRtl();
        int cutoutRightPadding2 = 0;
        int cutoutLeftPadding2 = 0;
        int cutoutPadding2 = 0;
        if (getParent() != null) {
            isToolBar = ((View) getParent()).getId() == 16909362;
        } else {
            isToolBar = this.mContext.getResources().getConfiguration().orientation == 1;
        }
        if (this.mHwCutoutUtil != null && this.mHwCutoutUtil.getNeedFitCutout()) {
            if (1 == this.mHwCutoutUtil.getDisplayRotate()) {
                cutoutLeftPadding2 = this.mHwCutoutUtil.getCutoutPadding();
                cutoutPadding2 = this.mHwCutoutUtil.getCutoutPadding();
            } else if (3 == this.mHwCutoutUtil.getDisplayRotate()) {
                cutoutRightPadding2 = this.mHwCutoutUtil.getCutoutPadding();
                cutoutPadding2 = this.mHwCutoutUtil.getCutoutPadding();
            }
        }
        int menuViewWidth = getMeasuredWidth();
        int start = ((menuViewWidth - this.mSplitMenuTotalWidth) - cutoutPadding2) / 2;
        int startRight = isToolBar ? (menuViewWidth - start) - cutoutRightPadding2 : menuViewWidth;
        int startLeft = isToolBar ? start + cutoutLeftPadding2 : 0;
        int startRight2 = startRight;
        int i = 0;
        while (i < childCount2) {
            View v = getChildAt(i);
            if (v.getVisibility() == 8) {
                int i2 = top;
                childCount = childCount2;
                isLayoutRtl = isLayoutRtl2;
                cutoutRightPadding = cutoutRightPadding2;
                cutoutLeftPadding = cutoutLeftPadding2;
                cutoutPadding = cutoutPadding2;
            } else {
                int itemPadding = getItemPadding(isToolBar, i);
                int itemPaddingStart = isToolBar ? this.mSplitToolbarMenuItemPadding : 0;
                int width = v.getMeasuredWidth();
                int measuredHeight = v.getMeasuredHeight();
                childCount = childCount2;
                cutoutRightPadding = cutoutRightPadding2;
                int cutoutRightPadding3 = getLayoutTop(isToolBar, top, bottom, v);
                int leftPadding = v.getPaddingLeft();
                cutoutLeftPadding = cutoutLeftPadding2;
                cutoutPadding = cutoutPadding2;
                v.setPadding(leftPadding, cutoutRightPadding3, v.getPaddingRight(), v.getPaddingBottom());
                if (isLayoutRtl2) {
                    int startRight3 = startRight2 - itemPaddingStart;
                    int i3 = leftPadding;
                    isLayoutRtl = isLayoutRtl2;
                    int i4 = cutoutRightPadding3;
                    v.layout(startRight3 - width, 0, startRight3, getMeasuredHeight());
                    startRight2 = startRight3 - (width + itemPadding);
                } else {
                    isLayoutRtl = isLayoutRtl2;
                    int i5 = cutoutRightPadding3;
                    int startLeft2 = startLeft + itemPaddingStart;
                    v.layout(startLeft2, 0, startLeft2 + width, getMeasuredHeight());
                    startLeft = startLeft2 + width + itemPadding;
                }
            }
            i++;
            childCount2 = childCount;
            cutoutRightPadding2 = cutoutRightPadding;
            cutoutLeftPadding2 = cutoutLeftPadding;
            cutoutPadding2 = cutoutPadding;
            isLayoutRtl2 = isLayoutRtl;
        }
        int i6 = top;
        int i7 = childCount2;
        boolean z = isLayoutRtl2;
        int i8 = cutoutRightPadding2;
        int i9 = cutoutLeftPadding2;
        int i10 = cutoutPadding2;
    }

    private int getLayoutTop(boolean isToolbar, int top, int bottom, View child) {
        int toolbarHeight = bottom - top;
        if (isToolbar && toolbarHeight > this.mMinHeight) {
            return this.mSplitMenuHeightPadding + this.mSplitMenuTopMargin;
        }
        if (isToolbar) {
            toolbarHeight -= this.mSplitMenuTopMargin;
        }
        int midVertical = (toolbarHeight / 2) - (child.getMeasuredHeight() / 2);
        if (isToolbar) {
            midVertical += this.mSplitMenuTopMargin;
        }
        return midVertical;
    }

    private int getItemPadding(boolean isToolBar, int itemNum) {
        if (isToolBar) {
            return this.mSplitToolbarMenuItemPadding;
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
        if (this.mPresenter != null) {
            this.mPresenter.updateMenuView(false);
            if (this.mPresenter.isOverflowMenuShowing()) {
                this.mPresenter.hideOverflowMenu();
                this.mPresenter.showOverflowMenuPending();
            }
        }
        this.mMinHeight = this.mResLoader.getResources(this.mContext).getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "hwtoolbar_split_menu_height"));
    }

    private void initSize(Context context) {
        Context context2 = context;
        Resources res = this.mResLoader.getResources(context2);
        int menuItemPaddingId = this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_menuitem_padding");
        this.mActionBarMenuItemPadding = res.getDimensionPixelSize(menuItemPaddingId);
        this.mActionBarMenuItemSize = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_menuitem_size"));
        this.mActionBarOverFlowBtnSize = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_overflowbtn_size"));
        this.mActionBarMenuPadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_menu_padding"));
        this.mActionBarOverFlowBtnStartPadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_overflowbtn_start_padding"));
        this.mActionBarOverFlowBtnEndPadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_overflowbtn_end_padding"));
        this.mSplitToolbarMenuItemPadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_split_menuitem_padding"));
        this.mSplitToolbarMenuItemPaddingLess = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_split_menuitem_padding_less"));
        this.mSplitMenuMinTextSize = res.getInteger(this.mResLoader.getIdentifier(context2, "integer", "hwtoolbar_split_menu_min_textsize"));
        this.mSplitMenuTextStep = res.getInteger(this.mResLoader.getIdentifier(context2, "integer", "hwtoolbar_split_menu_textsize_step"));
        this.mSplitMenuNormalTextSize = res.getInteger(this.mResLoader.getIdentifier(context2, "integer", "hwtoolbar_split_menu_normal_textsize"));
        int i = menuItemPaddingId;
        int splitMenuHeightPaddingId = this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_split_menu_height_padding");
        this.mSplitMenuHeightPadding = res.getDimensionPixelSize(splitMenuHeightPaddingId);
        int i2 = splitMenuHeightPaddingId;
        int splitMenuDrawablePaddingId = this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_split_menu_drawable_padding");
        this.mSplitMenuDrawablePadding = res.getDimensionPixelSize(splitMenuDrawablePaddingId);
        int i3 = splitMenuDrawablePaddingId;
        this.mSplitMenuTopMargin = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context2, ResLoaderUtil.DIMEN, "hwtoolbar_split_menuitem_top_margin"));
    }

    public void onSetSmartColor(ColorStateList iconColor, ColorStateList titleColor) {
        this.mSmartIconColor = iconColor;
        this.mSmartTitleColor = titleColor;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof HwToolbarMenuItemView) {
                ((HwToolbarMenuItemView) child).updateTextAndIcon();
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
