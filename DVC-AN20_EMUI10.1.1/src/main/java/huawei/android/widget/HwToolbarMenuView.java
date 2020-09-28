package huawei.android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.TextView;
import huawei.android.widget.columnsystem.HwColumnSystem;
import huawei.android.widget.utils.ResLoader;
import huawei.com.android.internal.view.menu.HwToolbarMenuItemView;
import java.util.ArrayList;
import java.util.List;

public class HwToolbarMenuView extends ActionMenuView implements HwSmartColorListener {
    private static final int CHILD_SIZE_TWO = 2;
    private static final int DICHOTOMY_SIZE = 2;
    private static final int DOUBLE_SIZE = 2;
    private static final boolean IS_DEBUG = false;
    private static final int SPLIT_MENU_AVERAGE_ITEM_NUM = 4;
    private static final int SPLIT_MENU_ITEM_NUM_THREE = 3;
    private static final String TAG = "HwToolbarMenuView";
    private static final String TYPE_DIMEN = "dimen";
    private static final String TYPE_INTEGER = "integer";
    private int mActionBarMenuItemPadding;
    private int mActionBarMenuItemSize;
    private int mActionBarMenuPadding;
    private int mActionBarOverFlowBtnEndPadding;
    private int mActionBarOverFlowBtnSize;
    private int mActionBarOverFlowBtnStartPadding;
    private float mColumnDensity;
    private int mColumnHeight;
    private int mColumnWidth;
    private boolean mHasOverFlowBtnAtActionBar;
    private boolean mHasVisibleChildAtActionBar;
    private boolean mIsColumnEnabled;
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

    public HwToolbarMenuView(Context context) {
        this(context, null);
    }

    public HwToolbarMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHasVisibleChildAtActionBar = false;
        this.mHasOverFlowBtnAtActionBar = false;
        this.mIsColumnEnabled = false;
        this.mResLoader = ResLoader.getInstance();
        this.mMinHeight = this.mResLoader.getResources(context).getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_split_menu_height"));
        initSize(context);
    }

    public void setPresenter(ActionMenuPresenter presenter) {
        super.setPresenter(presenter);
        this.mPresenter = presenter;
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return super.onApplyWindowInsets(insets);
    }

    public void setColumnEnabled(boolean isColumnEnabled) {
        this.mIsColumnEnabled = isColumnEnabled;
    }

    public boolean isColumnEnabled() {
        return this.mIsColumnEnabled;
    }

    public void configureColumn(int width, int height, float density) {
        this.mColumnWidth = width;
        this.mColumnHeight = height;
        this.mColumnDensity = density;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (((View) getParent()).getId() == 16909427) {
            onMeasureAtSplitView(widthMeasureSpec, heightMeasureSpec);
            setPadding(0, 0, 0, 0);
            return;
        }
        initColumnSize();
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
        for (int i2 = 0; i2 < childCount; i2++) {
            View child = getChildAt(i2);
            if (child.getVisibility() != 8) {
                child.setPadding(0, child.getPaddingTop(), 0, child.getPaddingBottom());
                ViewGroup.LayoutParams paramsTemp = child.getLayoutParams();
                if (paramsTemp instanceof ActionMenuView.LayoutParams) {
                    ActionMenuView.LayoutParams params = (ActionMenuView.LayoutParams) paramsTemp;
                    if (params.isOverflowButton) {
                        itemWidthSpec = View.MeasureSpec.makeMeasureSpec(this.mActionBarOverFlowBtnSize, 1073741824);
                    }
                    child.measure(itemWidthSpec, itemHeightSpec);
                    totalWidth += child.getMeasuredWidth();
                    if (i2 < childCount - 1) {
                        if (isNextItemOverFlowBtn(i2)) {
                            totalWidth += this.mActionBarOverFlowBtnStartPadding;
                        } else {
                            totalWidth += this.mActionBarMenuItemPadding;
                        }
                    }
                    if (i2 == childCount - 1 && params.isOverflowButton) {
                        this.mHasOverFlowBtnAtActionBar = true;
                    }
                    this.mHasVisibleChildAtActionBar = true;
                }
            }
        }
        int totalWidth2 = (this.mHasOverFlowBtnAtActionBar ? this.mActionBarOverFlowBtnEndPadding : widthPadding) + totalWidth;
        if (this.mHasVisibleChildAtActionBar) {
            i = totalWidth2;
        }
        setMeasuredDimension(i, heightSize);
    }

    private void setMeasureAtSplitView(int widthSize, int itemHeightSpec, int cellSize, int maxCellSize) {
        int childCount = getChildCount();
        List<TextView> textChilds = new ArrayList<>(childCount);
        List<ItemSpec> itemSpecs = new ArrayList<>(childCount);
        int requireCellSize = cellSize;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if ((child instanceof TextView) && child.getVisibility() != 8) {
                TextView textChild = (TextView) child;
                textChilds.add(textChild);
                ItemSpec itemSpec = new ItemSpec();
                itemSpec.mItemSize = cellSize;
                textChild.measure(0, itemHeightSpec);
                int requiredWidth = textChild.getMeasuredWidth();
                if (requiredWidth > requireCellSize) {
                    requireCellSize = requiredWidth > maxCellSize ? maxCellSize : requiredWidth;
                }
                itemSpecs.add(itemSpec);
            }
        }
        if (requireCellSize > cellSize) {
            for (ItemSpec itemSpec2 : itemSpecs) {
                itemSpec2.mItemSize = requireCellSize;
            }
        }
        int maxItemHeight = this.mMinHeight;
        this.mSplitMenuTotalWidth = 0;
        int menuHeightPadding = this.mSplitMenuHeightPadding * 2;
        int i2 = 0;
        int childRequiredHeight = maxItemHeight;
        for (int textChildCount = textChilds.size(); i2 < textChildCount; textChildCount = textChildCount) {
            TextView textItemView = textChilds.get(i2);
            ItemSpec itemSpec3 = itemSpecs.get(i2);
            textItemView.setCompoundDrawablePadding(this.mSplitMenuDrawablePadding);
            AutoTextUtils.autoText(this.mSplitMenuNormalTextSize, this.mSplitMenuMinTextSize, this.mSplitMenuTextStep, 1, itemSpec3.mItemSize, itemHeightSpec, textItemView);
            textItemView.measure(View.MeasureSpec.makeMeasureSpec(itemSpec3.mItemSize, 1073741824), itemHeightSpec);
            this.mSplitMenuTotalWidth += itemSpec3.mItemSize;
            int childRequiredHeight2 = textItemView.getMeasuredHeight() + menuHeightPadding;
            childRequiredHeight = childRequiredHeight > childRequiredHeight2 ? childRequiredHeight : childRequiredHeight2;
            i2++;
        }
        setMeasuredDimension(widthSize, childRequiredHeight);
    }

    private int getWidthInColumnSystem(int childCount) {
        HwColumnSystem hwColumnSystem = new HwColumnSystem(this.mContext);
        int columnType = 6;
        if (childCount >= 4) {
            columnType = 7;
        }
        hwColumnSystem.setColumnType(columnType);
        if (this.mColumnWidth <= 0 || this.mColumnHeight <= 0 || this.mColumnDensity <= 0.0f) {
            hwColumnSystem.updateConfigation(this.mContext);
        } else {
            hwColumnSystem.updateConfigation(this.mContext, this.mColumnWidth, this.mColumnHeight, this.mColumnDensity);
        }
        return hwColumnSystem.getSuggestWidth();
    }

    /* access modifiers changed from: protected */
    public void onMeasureAtSplitView(int widthMeasureSpec, int heightMeasureSpec) {
        int cellSize;
        int widthInColumnSystem;
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int childCount = getChildCount();
        int totalWidth = widthSize;
        boolean isEnabled = isColumnEnabled();
        if (isEnabled && (widthInColumnSystem = getWidthInColumnSystem(childCount)) > 0 && widthInColumnSystem <= widthSize) {
            totalWidth = widthInColumnSystem;
        }
        int visibleItemCount = 0;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != 8) {
                int i2 = this.mSplitToolbarMenuItemPadding;
                view.setPadding(i2, 0, i2, 0);
                visibleItemCount++;
            }
        }
        if (visibleItemCount <= 0) {
            setMeasuredDimension(widthSize, 0);
            return;
        }
        int maxCellSize = totalWidth / visibleItemCount;
        if (isEnabled) {
            cellSize = maxCellSize;
        } else if (visibleItemCount > 4) {
            cellSize = totalWidth / visibleItemCount;
        } else if (visibleItemCount == 4) {
            cellSize = (totalWidth - (this.mSplitToolbarMenuItemPaddingLess * 2)) / 4;
        } else {
            cellSize = (totalWidth - (this.mSplitToolbarMenuItemPaddingLess * 2)) / 3;
        }
        setMeasureAtSplitView(widthSize, getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), -1), cellSize, maxCellSize);
    }

    private boolean isNextItemOverFlowBtn(int itemNum) {
        int childCount = getChildCount();
        if (childCount < 2 || itemNum >= childCount - 1) {
            return false;
        }
        View nextChild = getChildAt(itemNum + 1);
        if (!(nextChild.getLayoutParams() instanceof ActionMenuView.LayoutParams) || !((ActionMenuView.LayoutParams) nextChild.getLayoutParams()).isOverflowButton) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static class ItemSpec {
        int mItemSize;

        private ItemSpec() {
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        boolean isToolBar;
        boolean isLayoutRtl;
        boolean isToolBar2;
        int childCount;
        HwToolbarMenuView hwToolbarMenuView = this;
        int childCount2 = getChildCount();
        boolean isLayoutRtl2 = isLayoutRtl();
        boolean z = true;
        if (getParent() != null) {
            if (((View) getParent()).getId() != 16909427) {
                z = false;
            }
            isToolBar = z;
        } else {
            if (hwToolbarMenuView.mContext.getResources().getConfiguration().orientation != 1) {
                z = false;
            }
            isToolBar = z;
        }
        int menuViewWidth = getMeasuredWidth();
        int start = (menuViewWidth - hwToolbarMenuView.mSplitMenuTotalWidth) / 2;
        int startRight = isToolBar ? menuViewWidth - start : menuViewWidth;
        int startLeft = isToolBar ? start : 0;
        int i = 0;
        while (i < childCount2) {
            View curView = hwToolbarMenuView.getChildAt(i);
            if (curView.getVisibility() == 8) {
                childCount = childCount2;
                isLayoutRtl = isLayoutRtl2;
                isToolBar2 = isToolBar;
            } else {
                int itemPadding = hwToolbarMenuView.getItemPadding(isToolBar, i);
                int width = curView.getMeasuredWidth();
                curView.getMeasuredHeight();
                childCount = childCount2;
                isToolBar2 = isToolBar;
                curView.setPadding(curView.getPaddingLeft(), hwToolbarMenuView.getLayoutTop(isToolBar, top, bottom, curView), curView.getPaddingRight(), curView.getPaddingBottom());
                if (isLayoutRtl2) {
                    isLayoutRtl = isLayoutRtl2;
                    curView.layout(startRight - width, 0, startRight, getMeasuredHeight());
                    startRight -= width + itemPadding;
                } else {
                    isLayoutRtl = isLayoutRtl2;
                    curView.layout(startLeft, 0, startLeft + width, getMeasuredHeight());
                    startLeft += width + itemPadding;
                }
            }
            i++;
            hwToolbarMenuView = this;
            childCount2 = childCount;
            isToolBar = isToolBar2;
            isLayoutRtl2 = isLayoutRtl;
        }
    }

    private int getLayoutTop(boolean isToolbar, int top, int bottom, View child) {
        return isToolbar ? this.mSplitMenuHeightPadding * 2 : ((bottom - top) - child.getMeasuredHeight()) / 2;
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
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwActionMenuPresenter hwActionMenuPresenter = this.mPresenter;
        if (hwActionMenuPresenter instanceof HwActionMenuPresenter) {
            boolean isShowing = !hwActionMenuPresenter.isPopupMenuShowing() || !this.mPresenter.isOverflowMenuShowing();
            Parcelable parcelable = this.mSavedState;
            if (parcelable != null && isShowing) {
                this.mPresenter.onRestoreInstanceState(parcelable);
                this.mSavedState = null;
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        ActionMenuPresenter actionMenuPresenter = this.mPresenter;
        if (actionMenuPresenter != null) {
            actionMenuPresenter.updateMenuView(false);
            if (this.mPresenter.isOverflowMenuShowing()) {
                this.mPresenter.hideOverflowMenu();
                this.mPresenter.showOverflowMenuPending();
            }
        }
        this.mMinHeight = this.mResLoader.getResources(this.mContext).getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, TYPE_DIMEN, "hwtoolbar_split_menu_height"));
    }

    private void initColumnSize() {
        this.mActionBarMenuPadding = this.mResLoader.getResources(getContext()).getDimensionPixelSize(this.mResLoader.getIdentifier(getContext(), TYPE_DIMEN, "emui_dimens_default_end"));
        this.mActionBarOverFlowBtnEndPadding = this.mActionBarMenuPadding;
    }

    private void initSize(Context context) {
        Resources res = this.mResLoader.getResources(context);
        this.mActionBarMenuItemPadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_menuitem_padding"));
        this.mActionBarMenuItemSize = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_menuitem_size"));
        this.mActionBarOverFlowBtnSize = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_overflowbtn_size"));
        this.mActionBarOverFlowBtnStartPadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_overflowbtn_start_padding"));
        this.mSplitToolbarMenuItemPadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_split_menuitem_padding"));
        this.mSplitToolbarMenuItemPaddingLess = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_split_menuitem_padding_less"));
        this.mSplitMenuMinTextSize = res.getInteger(this.mResLoader.getIdentifier(context, TYPE_INTEGER, "hwtoolbar_split_menu_min_textsize"));
        this.mSplitMenuTextStep = res.getInteger(this.mResLoader.getIdentifier(context, TYPE_INTEGER, "hwtoolbar_split_menu_textsize_step"));
        this.mSplitMenuNormalTextSize = res.getInteger(this.mResLoader.getIdentifier(context, TYPE_INTEGER, "hwtoolbar_split_menu_normal_textsize"));
        this.mSplitMenuHeightPadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_split_menu_height_padding"));
        this.mSplitMenuDrawablePadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_split_menu_drawable_padding"));
        this.mSplitMenuTopMargin = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, TYPE_DIMEN, "hwtoolbar_split_menuitem_top_margin"));
    }

    @Override // huawei.android.widget.HwSmartColorListener
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

    @Override // huawei.android.widget.HwSmartColorListener
    public ColorStateList getSmartIconColor() {
        return this.mSmartIconColor;
    }

    @Override // huawei.android.widget.HwSmartColorListener
    public ColorStateList getSmartTitleColor() {
        return this.mSmartTitleColor;
    }
}
