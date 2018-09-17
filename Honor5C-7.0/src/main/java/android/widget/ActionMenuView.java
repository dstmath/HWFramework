package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import com.android.internal.os.HwBootFail;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.android.internal.view.menu.MenuView;

public class ActionMenuView extends LinearLayout implements ItemInvoker, MenuView {
    static final int GENERATED_ITEM_PADDING = 4;
    static final int MIN_CELL_SIZE = 56;
    private static final String TAG = "ActionMenuView";
    private Callback mActionMenuPresenterCallback;
    private boolean mFormatItems;
    private int mFormatItemsWidth;
    private int mGeneratedItemPadding;
    private MenuBuilder mMenu;
    private MenuBuilder.Callback mMenuBuilderCallback;
    private int mMinCellSize;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    private Context mPopupContext;
    private int mPopupTheme;
    private ActionMenuPresenter mPresenter;
    private boolean mReserveOverflow;

    public interface ActionMenuChildView {
        boolean needsDividerAfter();

        boolean needsDividerBefore();
    }

    private class ActionMenuPresenterCallback implements Callback {
        private ActionMenuPresenterCallback() {
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            return false;
        }
    }

    public static class LayoutParams extends android.widget.LinearLayout.LayoutParams {
        @ExportedProperty(category = "layout")
        public int cellsUsed;
        @ExportedProperty(category = "layout")
        public boolean expandable;
        public boolean expanded;
        @ExportedProperty(category = "layout")
        public int extraPixels;
        @ExportedProperty(category = "layout")
        public boolean isOverflowButton;
        @ExportedProperty(category = "layout")
        public boolean preventEdgeOffset;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams other) {
            super(other);
        }

        public LayoutParams(LayoutParams other) {
            super((android.widget.LinearLayout.LayoutParams) other);
            this.isOverflowButton = other.isOverflowButton;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.isOverflowButton = false;
        }

        public LayoutParams(int width, int height, boolean isOverflowButton) {
            super(width, height);
            this.isOverflowButton = isOverflowButton;
        }

        protected void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("layout:overFlowButton", this.isOverflowButton);
            encoder.addProperty("layout:cellsUsed", this.cellsUsed);
            encoder.addProperty("layout:extraPixels", this.extraPixels);
            encoder.addProperty("layout:expandable", this.expandable);
            encoder.addProperty("layout:preventEdgeOffset", this.preventEdgeOffset);
        }
    }

    private class MenuBuilderCallback implements MenuBuilder.Callback {
        private MenuBuilderCallback() {
        }

        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            if (ActionMenuView.this.mOnMenuItemClickListener != null) {
                return ActionMenuView.this.mOnMenuItemClickListener.onMenuItemClick(item);
            }
            return false;
        }

        public void onMenuModeChange(MenuBuilder menu) {
            if (ActionMenuView.this.mMenuBuilderCallback != null) {
                ActionMenuView.this.mMenuBuilderCallback.onMenuModeChange(menu);
            }
        }
    }

    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem menuItem);
    }

    public ActionMenuView(Context context) {
        this(context, null);
    }

    public ActionMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBaselineAligned(false);
        float density = context.getResources().getDisplayMetrics().density;
        this.mMinCellSize = (int) (56.0f * density);
        this.mGeneratedItemPadding = (int) (4.0f * density);
        this.mPopupContext = context;
        this.mPopupTheme = 0;
    }

    public void setPopupTheme(int resId) {
        if (this.mPopupTheme != resId) {
            this.mPopupTheme = resId;
            if (resId == 0) {
                this.mPopupContext = this.mContext;
            } else {
                this.mPopupContext = new ContextThemeWrapper(this.mContext, resId);
            }
        }
    }

    public int getPopupTheme() {
        return this.mPopupTheme;
    }

    public void setPresenter(ActionMenuPresenter presenter) {
        this.mPresenter = presenter;
        this.mPresenter.setMenuView(this);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mPresenter != null) {
            this.mPresenter.updateMenuView(false);
            if (this.mPresenter.isOverflowMenuShowing()) {
                this.mPresenter.hideOverflowMenu();
                this.mPresenter.showOverflowMenu();
            }
        }
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.mOnMenuItemClickListener = listener;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean z;
        boolean wasFormatted = this.mFormatItems;
        if (MeasureSpec.getMode(widthMeasureSpec) == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
            z = true;
        } else {
            z = false;
        }
        this.mFormatItems = z;
        if (wasFormatted != this.mFormatItems) {
            this.mFormatItemsWidth = 0;
        }
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (!(!this.mFormatItems || this.mMenu == null || widthSize == this.mFormatItemsWidth)) {
            this.mFormatItemsWidth = widthSize;
            this.mMenu.onItemsChanged(true);
        }
        int childCount = getChildCount();
        if (!this.mFormatItems || childCount <= 0) {
            for (int i = 0; i < childCount; i++) {
                LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
                lp.rightMargin = 0;
                lp.leftMargin = 0;
            }
            if (childCount == 0) {
                setMeasuredDimension(0, 0);
                return;
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
        }
        onMeasureExactFormat(widthMeasureSpec, heightMeasureSpec);
    }

    private void onMeasureExactFormat(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthPadding = getPaddingLeft() + getPaddingRight();
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int itemHeightSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec, heightPadding, -2);
        widthSize -= widthPadding;
        int cellCount = widthSize / this.mMinCellSize;
        int cellSizeRemaining = widthSize % this.mMinCellSize;
        if (cellCount == 0) {
            setMeasuredDimension(widthSize, 0);
            return;
        }
        int i;
        LayoutParams lp;
        int cellSize = this.mMinCellSize + (cellSizeRemaining / cellCount);
        int cellsRemaining = cellCount;
        int maxChildHeight = 0;
        int maxCellsUsed = 0;
        int expandableItemCount = 0;
        int visibleItemCount = 0;
        boolean hasOverflow = false;
        long smallestItemsAt = 0;
        int childCount = getChildCount();
        for (i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                int cellsAvailable;
                boolean isGeneratedItem = child instanceof ActionMenuItemView;
                visibleItemCount++;
                if (isGeneratedItem) {
                    child.setPadding(this.mGeneratedItemPadding, 0, this.mGeneratedItemPadding, 0);
                }
                lp = (LayoutParams) child.getLayoutParams();
                lp.expanded = false;
                lp.extraPixels = 0;
                lp.cellsUsed = 0;
                lp.expandable = false;
                lp.leftMargin = 0;
                lp.rightMargin = 0;
                lp.preventEdgeOffset = isGeneratedItem ? ((ActionMenuItemView) child).hasText() : false;
                if (lp.isOverflowButton) {
                    cellsAvailable = 1;
                } else {
                    cellsAvailable = cellsRemaining;
                }
                int cellsUsed = measureChildForCells(child, cellSize, cellsAvailable, itemHeightSpec, heightPadding);
                maxCellsUsed = Math.max(maxCellsUsed, cellsUsed);
                if (lp.expandable) {
                    expandableItemCount++;
                }
                if (lp.isOverflowButton) {
                    hasOverflow = true;
                }
                cellsRemaining -= cellsUsed;
                maxChildHeight = Math.max(maxChildHeight, child.getMeasuredHeight());
                if (cellsUsed == 1) {
                    smallestItemsAt |= (long) (1 << i);
                }
            }
        }
        boolean centerSingleExpandedItem = hasOverflow && visibleItemCount == 2;
        boolean needsExpansion = false;
        while (expandableItemCount > 0 && cellsRemaining > 0) {
            int minCells = HwBootFail.STAGE_BOOT_SUCCESS;
            long minCellsAt = 0;
            int minCellsItemCount = 0;
            for (i = 0; i < childCount; i++) {
                int i2;
                lp = (LayoutParams) getChildAt(i).getLayoutParams();
                if (lp.expandable) {
                    i2 = lp.cellsUsed;
                    if (r0 < minCells) {
                        minCells = lp.cellsUsed;
                        minCellsAt = (long) (1 << i);
                        minCellsItemCount = 1;
                    } else {
                        i2 = lp.cellsUsed;
                        if (r0 == minCells) {
                            minCellsAt |= (long) (1 << i);
                            minCellsItemCount++;
                        }
                    }
                }
            }
            smallestItemsAt |= minCellsAt;
            if (minCellsItemCount > cellsRemaining) {
                break;
            }
            minCells++;
            for (i = 0; i < childCount; i++) {
                child = getChildAt(i);
                lp = (LayoutParams) child.getLayoutParams();
                if ((((long) (1 << i)) & minCellsAt) == 0) {
                    i2 = lp.cellsUsed;
                    if (r0 == minCells) {
                        smallestItemsAt |= (long) (1 << i);
                    }
                } else {
                    if (centerSingleExpandedItem && lp.preventEdgeOffset && cellsRemaining == 1) {
                        child.setPadding(this.mGeneratedItemPadding + cellSize, 0, this.mGeneratedItemPadding, 0);
                    }
                    lp.cellsUsed++;
                    lp.expanded = true;
                    cellsRemaining--;
                }
            }
            needsExpansion = true;
        }
        boolean singleItem = !hasOverflow && visibleItemCount == 1;
        if (cellsRemaining > 0 && smallestItemsAt != 0 && (cellsRemaining < visibleItemCount - 1 || singleItem || maxCellsUsed > 1)) {
            float expandCount = (float) Long.bitCount(smallestItemsAt);
            if (!singleItem) {
                if ((1 & smallestItemsAt) != 0) {
                    if (!((LayoutParams) getChildAt(0).getLayoutParams()).preventEdgeOffset) {
                        expandCount -= 0.5f;
                    }
                }
                if ((((long) (1 << (childCount - 1))) & smallestItemsAt) != 0) {
                    if (!((LayoutParams) getChildAt(childCount - 1).getLayoutParams()).preventEdgeOffset) {
                        expandCount -= 0.5f;
                    }
                }
            }
            int extraPixels = expandCount > 0.0f ? (int) (((float) (cellsRemaining * cellSize)) / expandCount) : 0;
            for (i = 0; i < childCount; i++) {
                if ((((long) (1 << i)) & smallestItemsAt) != 0) {
                    child = getChildAt(i);
                    lp = (LayoutParams) child.getLayoutParams();
                    if (child instanceof ActionMenuItemView) {
                        lp.extraPixels = extraPixels;
                        lp.expanded = true;
                        if (i == 0 && !lp.preventEdgeOffset) {
                            lp.leftMargin = (-extraPixels) / 2;
                        }
                        needsExpansion = true;
                    } else if (lp.isOverflowButton) {
                        lp.extraPixels = extraPixels;
                        lp.expanded = true;
                        lp.rightMargin = (-extraPixels) / 2;
                        needsExpansion = true;
                    } else {
                        if (i != 0) {
                            lp.leftMargin = extraPixels / 2;
                        }
                        if (i != childCount - 1) {
                            lp.rightMargin = extraPixels / 2;
                        }
                    }
                }
            }
        }
        if (needsExpansion) {
            for (i = 0; i < childCount; i++) {
                child = getChildAt(i);
                lp = (LayoutParams) child.getLayoutParams();
                if (lp.expanded) {
                    child.measure(MeasureSpec.makeMeasureSpec((lp.cellsUsed * cellSize) + lp.extraPixels, EditorInfo.IME_FLAG_NO_ENTER_ACTION), itemHeightSpec);
                }
            }
        }
        if (heightMode != 1073741824) {
            heightSize = maxChildHeight;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    static int measureChildForCells(View child, int cellSize, int cellsRemaining, int parentHeightMeasureSpec, int parentHeightPadding) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int childHeightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(parentHeightMeasureSpec) - parentHeightPadding, MeasureSpec.getMode(parentHeightMeasureSpec));
        ActionMenuItemView itemView = child instanceof ActionMenuItemView ? (ActionMenuItemView) child : null;
        boolean hasText = itemView != null ? itemView.hasText() : false;
        int cellsUsed = 0;
        if (cellsRemaining > 0 && (!hasText || cellsRemaining >= 2)) {
            child.measure(MeasureSpec.makeMeasureSpec(cellSize * cellsRemaining, RtlSpacingHelper.UNDEFINED), childHeightSpec);
            int measuredWidth = child.getMeasuredWidth();
            cellsUsed = measuredWidth / cellSize;
            if (measuredWidth % cellSize != 0) {
                cellsUsed++;
            }
            if (hasText && cellsUsed < 2) {
                cellsUsed = 2;
            }
        }
        lp.expandable = !lp.isOverflowButton ? hasText : false;
        lp.cellsUsed = cellsUsed;
        child.measure(MeasureSpec.makeMeasureSpec(cellsUsed * cellSize, EditorInfo.IME_FLAG_NO_ENTER_ACTION), childHeightSpec);
        return cellsUsed;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mFormatItems) {
            int i;
            View v;
            int height;
            int l;
            int t;
            int childCount = getChildCount();
            int midVertical = (bottom - top) / 2;
            int dividerWidth = getDividerWidth();
            int nonOverflowWidth = 0;
            int nonOverflowCount = 0;
            int widthRemaining = ((right - left) - getPaddingRight()) - getPaddingLeft();
            boolean hasOverflow = false;
            boolean isLayoutRtl = isLayoutRtl();
            for (i = 0; i < childCount; i++) {
                v = getChildAt(i);
                if (v.getVisibility() != 8) {
                    LayoutParams p = (LayoutParams) v.getLayoutParams();
                    if (p.isOverflowButton) {
                        int r;
                        int overflowWidth = v.getMeasuredWidth();
                        if (hasDividerBeforeChildAt(i)) {
                            overflowWidth += dividerWidth;
                        }
                        height = v.getMeasuredHeight();
                        if (isLayoutRtl) {
                            l = getPaddingLeft() + p.leftMargin;
                            r = l + overflowWidth;
                        } else {
                            r = (getWidth() - getPaddingRight()) - p.rightMargin;
                            l = r - overflowWidth;
                        }
                        t = midVertical - (height / 2);
                        v.layout(l, t, r, t + height);
                        widthRemaining -= overflowWidth;
                        hasOverflow = true;
                    } else {
                        int size = (v.getMeasuredWidth() + p.leftMargin) + p.rightMargin;
                        nonOverflowWidth += size;
                        widthRemaining -= size;
                        if (hasDividerBeforeChildAt(i)) {
                            nonOverflowWidth += dividerWidth;
                        }
                        nonOverflowCount++;
                    }
                }
            }
            int width;
            if (childCount != 1 || hasOverflow) {
                int spacerCount = nonOverflowCount - (hasOverflow ? 0 : 1);
                int spacerSize = Math.max(0, spacerCount > 0 ? widthRemaining / spacerCount : 0);
                LayoutParams lp;
                if (isLayoutRtl) {
                    int startRight = getWidth() - getPaddingRight();
                    for (i = 0; i < childCount; i++) {
                        v = getChildAt(i);
                        lp = (LayoutParams) v.getLayoutParams();
                        if (!(v.getVisibility() == 8 || lp.isOverflowButton)) {
                            startRight -= lp.rightMargin;
                            width = v.getMeasuredWidth();
                            height = v.getMeasuredHeight();
                            t = midVertical - (height / 2);
                            v.layout(startRight - width, t, startRight, t + height);
                            startRight -= (lp.leftMargin + width) + spacerSize;
                        }
                    }
                } else {
                    int startLeft = getPaddingLeft();
                    for (i = 0; i < childCount; i++) {
                        v = getChildAt(i);
                        lp = (LayoutParams) v.getLayoutParams();
                        if (!(v.getVisibility() == 8 || lp.isOverflowButton)) {
                            startLeft += lp.leftMargin;
                            width = v.getMeasuredWidth();
                            height = v.getMeasuredHeight();
                            t = midVertical - (height / 2);
                            v.layout(startLeft, t, startLeft + width, t + height);
                            startLeft += (lp.rightMargin + width) + spacerSize;
                        }
                    }
                }
                return;
            }
            v = getChildAt(0);
            width = v.getMeasuredWidth();
            height = v.getMeasuredHeight();
            l = ((right - left) / 2) - (width / 2);
            t = midVertical - (height / 2);
            v.layout(l, t, l + width, t + height);
            return;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dismissPopupMenus();
    }

    public void setOverflowIcon(Drawable icon) {
        getMenu();
        this.mPresenter.setOverflowIcon(icon);
    }

    public Drawable getOverflowIcon() {
        getMenu();
        return this.mPresenter.getOverflowIcon();
    }

    public boolean isOverflowReserved() {
        return this.mReserveOverflow;
    }

    public void setOverflowReserved(boolean reserveOverflow) {
        this.mReserveOverflow = reserveOverflow;
    }

    protected LayoutParams generateDefaultLayoutParams() {
        LayoutParams params = new LayoutParams(-2, -2);
        params.gravity = 16;
        return params;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        if (p == null) {
            return generateDefaultLayoutParams();
        }
        LayoutParams result;
        if (p instanceof LayoutParams) {
            result = new LayoutParams((LayoutParams) p);
        } else {
            result = new LayoutParams(p);
        }
        if (result.gravity <= 0) {
            result.gravity = 16;
        }
        return result;
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p != null ? p instanceof LayoutParams : false;
    }

    public LayoutParams generateOverflowButtonLayoutParams() {
        LayoutParams result = generateDefaultLayoutParams();
        result.isOverflowButton = true;
        return result;
    }

    public boolean invokeItem(MenuItemImpl item) {
        return this.mMenu.performItemAction(item, 0);
    }

    public int getWindowAnimations() {
        return 0;
    }

    public void initialize(MenuBuilder menu) {
        this.mMenu = menu;
    }

    public Menu getMenu() {
        if (this.mMenu == null) {
            Context context = getContext();
            this.mMenu = new MenuBuilder(context);
            this.mMenu.setCallback(new MenuBuilderCallback());
            this.mPresenter = new ActionMenuPresenter(context);
            this.mPresenter.setReserveOverflow(true);
            this.mPresenter.setCallback(this.mActionMenuPresenterCallback != null ? this.mActionMenuPresenterCallback : new ActionMenuPresenterCallback());
            this.mMenu.addMenuPresenter(this.mPresenter, this.mPopupContext);
            this.mPresenter.setMenuView(this);
        }
        return this.mMenu;
    }

    public void setMenuCallbacks(Callback pcb, MenuBuilder.Callback mcb) {
        this.mActionMenuPresenterCallback = pcb;
        this.mMenuBuilderCallback = mcb;
    }

    public MenuBuilder peekMenu() {
        return this.mMenu;
    }

    public boolean showOverflowMenu() {
        return this.mPresenter != null ? this.mPresenter.showOverflowMenu() : false;
    }

    public boolean hideOverflowMenu() {
        return this.mPresenter != null ? this.mPresenter.hideOverflowMenu() : false;
    }

    public boolean isOverflowMenuShowing() {
        return this.mPresenter != null ? this.mPresenter.isOverflowMenuShowing() : false;
    }

    public boolean isOverflowMenuShowPending() {
        return this.mPresenter != null ? this.mPresenter.isOverflowMenuShowPending() : false;
    }

    public void dismissPopupMenus() {
        if (this.mPresenter != null) {
            this.mPresenter.dismissPopupMenus();
        }
    }

    protected boolean hasDividerBeforeChildAt(int childIndex) {
        if (childIndex == 0) {
            return false;
        }
        View childBefore = getChildAt(childIndex - 1);
        View child = getChildAt(childIndex);
        boolean result = false;
        if (childIndex < getChildCount() && (childBefore instanceof ActionMenuChildView)) {
            result = ((ActionMenuChildView) childBefore).needsDividerAfter();
        }
        if (childIndex > 0 && (child instanceof ActionMenuChildView)) {
            result |= ((ActionMenuChildView) child).needsDividerBefore();
        }
        return result;
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        return false;
    }

    public void setExpandedActionViewsExclusive(boolean exclusive) {
        this.mPresenter.setExpandedActionViewsExclusive(exclusive);
    }

    protected ActionMenuPresenter getPresenter() {
        return this.mPresenter;
    }

    protected boolean getFormatItems() {
        return this.mFormatItems;
    }

    protected int getGeneratedItemPadding() {
        return this.mGeneratedItemPadding;
    }

    public void setSplitViewMaxSize(int maxSize) {
    }
}
