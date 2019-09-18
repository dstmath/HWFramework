package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;

public class ActionMenuView extends LinearLayout implements MenuBuilder.ItemInvoker, MenuView {
    static final int GENERATED_ITEM_PADDING = 4;
    static final int MIN_CELL_SIZE = 56;
    private static final String TAG = "ActionMenuView";
    private MenuPresenter.Callback mActionMenuPresenterCallback;
    private boolean mFormatItems;
    private int mFormatItemsWidth;
    private int mGeneratedItemPadding;
    private MenuBuilder mMenu;
    /* access modifiers changed from: private */
    public MenuBuilder.Callback mMenuBuilderCallback;
    private int mMinCellSize;
    /* access modifiers changed from: private */
    public OnMenuItemClickListener mOnMenuItemClickListener;
    private Context mPopupContext;
    private int mPopupTheme;
    private ActionMenuPresenter mPresenter;
    private boolean mReserveOverflow;

    public interface ActionMenuChildView {
        boolean needsDividerAfter();

        boolean needsDividerBefore();
    }

    private class ActionMenuPresenterCallback implements MenuPresenter.Callback {
        private ActionMenuPresenterCallback() {
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            return false;
        }
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {
        @ViewDebug.ExportedProperty(category = "layout")
        public int cellsUsed;
        @ViewDebug.ExportedProperty(category = "layout")
        public boolean expandable;
        public boolean expanded;
        @ViewDebug.ExportedProperty(category = "layout")
        public int extraPixels;
        @ViewDebug.ExportedProperty(category = "layout")
        public boolean isOverflowButton;
        @ViewDebug.ExportedProperty(category = "layout")
        public boolean preventEdgeOffset;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(ViewGroup.LayoutParams other) {
            super(other);
        }

        public LayoutParams(LayoutParams other) {
            super((LinearLayout.LayoutParams) other);
            this.isOverflowButton = other.isOverflowButton;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.isOverflowButton = false;
        }

        public LayoutParams(int width, int height, boolean isOverflowButton2) {
            super(width, height);
            this.isOverflowButton = isOverflowButton2;
        }

        /* access modifiers changed from: protected */
        public void encodeProperties(ViewHierarchyEncoder encoder) {
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
            return ActionMenuView.this.mOnMenuItemClickListener != null && ActionMenuView.this.mOnMenuItemClickListener.onMenuItemClick(item);
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

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean wasFormatted = this.mFormatItems;
        this.mFormatItems = View.MeasureSpec.getMode(widthMeasureSpec) == 1073741824;
        if (wasFormatted != this.mFormatItems) {
            this.mFormatItemsWidth = 0;
        }
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
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
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            onMeasureExactFormat(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:139:0x02b5  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x02ea  */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x02ec  */
    private void onMeasureExactFormat(int widthMeasureSpec, int heightMeasureSpec) {
        int visibleItemCount;
        boolean needsExpansion;
        int visibleItemCount2;
        boolean singleItem;
        int heightSize;
        long smallestItemsAt;
        boolean centerSingleExpandedItem;
        int cellsRemaining;
        int visibleItemCount3;
        int heightPadding;
        int cellSizeRemaining;
        int visibleItemCount4;
        boolean z;
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize2 = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthPadding = getPaddingLeft() + getPaddingRight();
        int heightPadding2 = getPaddingTop() + getPaddingBottom();
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding2, -2);
        int widthSize2 = widthSize - widthPadding;
        int cellCount = widthSize2 / this.mMinCellSize;
        int cellSizeRemaining2 = widthSize2 % this.mMinCellSize;
        if (cellCount == 0) {
            setMeasuredDimension(widthSize2, 0);
            return;
        }
        int cellSize = this.mMinCellSize + (cellSizeRemaining2 / cellCount);
        boolean hasOverflow = false;
        long smallestItemsAt2 = 0;
        int childCount = getChildCount();
        int heightSize3 = heightSize2;
        int maxChildHeight = 0;
        int visibleItemCount5 = 0;
        int expandableItemCount = 0;
        int maxCellsUsed = 0;
        int cellsRemaining2 = cellCount;
        int i = 0;
        while (true) {
            int widthPadding2 = widthPadding;
            if (i >= childCount) {
                break;
            }
            ActionMenuItemView childAt = getChildAt(i);
            int cellCount2 = cellCount;
            if (childAt.getVisibility() == 8) {
                heightPadding = heightPadding2;
                cellSizeRemaining = cellSizeRemaining2;
            } else {
                boolean isGeneratedItem = childAt instanceof ActionMenuItemView;
                int visibleItemCount6 = visibleItemCount5 + 1;
                if (isGeneratedItem) {
                    cellSizeRemaining = cellSizeRemaining2;
                    visibleItemCount4 = visibleItemCount6;
                    z = false;
                    childAt.setPadding(this.mGeneratedItemPadding, 0, this.mGeneratedItemPadding, 0);
                } else {
                    cellSizeRemaining = cellSizeRemaining2;
                    visibleItemCount4 = visibleItemCount6;
                    z = false;
                }
                LayoutParams lp = (LayoutParams) childAt.getLayoutParams();
                lp.expanded = z;
                lp.extraPixels = z ? 1 : 0;
                lp.cellsUsed = z;
                lp.expandable = z;
                lp.leftMargin = z;
                lp.rightMargin = z;
                lp.preventEdgeOffset = isGeneratedItem && childAt.hasText();
                int cellsUsed = measureChildForCells(childAt, cellSize, lp.isOverflowButton ? 1 : cellsRemaining2, itemHeightSpec, heightPadding2);
                maxCellsUsed = Math.max(maxCellsUsed, cellsUsed);
                heightPadding = heightPadding2;
                if (lp.expandable != 0) {
                    expandableItemCount++;
                }
                if (lp.isOverflowButton) {
                    hasOverflow = true;
                }
                cellsRemaining2 -= cellsUsed;
                maxChildHeight = Math.max(maxChildHeight, childAt.getMeasuredHeight());
                if (cellsUsed == 1) {
                    View view = childAt;
                    smallestItemsAt2 |= (long) (1 << i);
                    visibleItemCount5 = visibleItemCount4;
                    maxChildHeight = maxChildHeight;
                } else {
                    visibleItemCount5 = visibleItemCount4;
                }
            }
            i++;
            widthPadding = widthPadding2;
            cellCount = cellCount2;
            cellSizeRemaining2 = cellSizeRemaining;
            heightPadding2 = heightPadding;
            int i2 = heightMeasureSpec;
        }
        int i3 = cellCount;
        int i4 = cellSizeRemaining2;
        boolean centerSingleExpandedItem2 = hasOverflow && visibleItemCount5 == 2;
        boolean needsExpansion2 = false;
        while (true) {
            if (expandableItemCount <= 0 || cellsRemaining2 <= 0) {
                needsExpansion = needsExpansion2;
                visibleItemCount = visibleItemCount5;
            } else {
                long minCellsAt = 0;
                int minCells = Integer.MAX_VALUE;
                int minCellsItemCount = 0;
                int i5 = 0;
                while (true) {
                    int i6 = i5;
                    if (i6 >= childCount) {
                        break;
                    }
                    View child = getChildAt(i6);
                    boolean needsExpansion3 = needsExpansion2;
                    LayoutParams lp2 = (LayoutParams) child.getLayoutParams();
                    View view2 = child;
                    if (!lp2.expandable) {
                        visibleItemCount3 = visibleItemCount5;
                    } else if (lp2.cellsUsed < minCells) {
                        visibleItemCount3 = visibleItemCount5;
                        minCellsItemCount = 1;
                        minCellsAt = (long) (1 << i6);
                        minCells = lp2.cellsUsed;
                    } else {
                        visibleItemCount3 = visibleItemCount5;
                        if (lp2.cellsUsed == minCells) {
                            minCellsItemCount++;
                            minCellsAt |= (long) (1 << i6);
                        }
                    }
                    i5 = i6 + 1;
                    needsExpansion2 = needsExpansion3;
                    visibleItemCount5 = visibleItemCount3;
                }
                needsExpansion = needsExpansion2;
                visibleItemCount = visibleItemCount5;
                smallestItemsAt2 |= minCellsAt;
                if (minCellsItemCount > cellsRemaining2) {
                    boolean z2 = centerSingleExpandedItem2;
                    break;
                }
                int minCells2 = minCells + 1;
                int i7 = 0;
                while (i7 < childCount) {
                    View child2 = getChildAt(i7);
                    LayoutParams lp3 = (LayoutParams) child2.getLayoutParams();
                    int minCellsItemCount2 = minCellsItemCount;
                    int cellsRemaining3 = cellsRemaining2;
                    if ((minCellsAt & ((long) (1 << i7))) == 0) {
                        if (lp3.cellsUsed == minCells2) {
                            smallestItemsAt2 |= (long) (1 << i7);
                        }
                        centerSingleExpandedItem = centerSingleExpandedItem2;
                        cellsRemaining2 = cellsRemaining3;
                    } else {
                        if (!centerSingleExpandedItem2 || !lp3.preventEdgeOffset) {
                            centerSingleExpandedItem = centerSingleExpandedItem2;
                            cellsRemaining = cellsRemaining3;
                        } else {
                            cellsRemaining = cellsRemaining3;
                            if (cellsRemaining == 1) {
                                centerSingleExpandedItem = centerSingleExpandedItem2;
                                child2.setPadding(this.mGeneratedItemPadding + cellSize, 0, this.mGeneratedItemPadding, 0);
                            } else {
                                centerSingleExpandedItem = centerSingleExpandedItem2;
                            }
                        }
                        lp3.cellsUsed++;
                        lp3.expanded = true;
                        cellsRemaining2 = cellsRemaining - 1;
                    }
                    i7++;
                    minCellsItemCount = minCellsItemCount2;
                    centerSingleExpandedItem2 = centerSingleExpandedItem;
                }
                boolean z3 = centerSingleExpandedItem2;
                needsExpansion2 = true;
                visibleItemCount5 = visibleItemCount;
            }
        }
        needsExpansion = needsExpansion2;
        visibleItemCount = visibleItemCount5;
        long smallestItemsAt3 = smallestItemsAt2;
        if (!hasOverflow) {
            visibleItemCount2 = visibleItemCount;
            if (visibleItemCount2 == 1) {
                singleItem = true;
                if (cellsRemaining2 > 0 || smallestItemsAt3 == 0) {
                    int i8 = visibleItemCount2;
                } else if (cellsRemaining2 < visibleItemCount2 - 1 || singleItem || maxCellsUsed > 1) {
                    float expandCount = (float) Long.bitCount(smallestItemsAt3);
                    if (!singleItem) {
                        if ((smallestItemsAt3 & 1) != 0 && !((LayoutParams) getChildAt(0).getLayoutParams()).preventEdgeOffset) {
                            expandCount -= 0.5f;
                        }
                        int i9 = visibleItemCount2;
                        if ((((long) (1 << (childCount - 1))) & smallestItemsAt3) != 0 && !((LayoutParams) getChildAt(childCount - 1).getLayoutParams()).preventEdgeOffset) {
                            expandCount -= 0.5f;
                        }
                    }
                    int extraPixels = expandCount > 0.0f ? (int) (((float) (cellsRemaining2 * cellSize)) / expandCount) : 0;
                    int i10 = 0;
                    while (i10 < childCount) {
                        boolean singleItem2 = singleItem;
                        float expandCount2 = expandCount;
                        if ((((long) (1 << i10)) & smallestItemsAt3) != 0) {
                            View child3 = getChildAt(i10);
                            LayoutParams lp4 = (LayoutParams) child3.getLayoutParams();
                            if (child3 instanceof ActionMenuItemView) {
                                lp4.extraPixels = extraPixels;
                                lp4.expanded = true;
                                if (i10 == 0 && !lp4.preventEdgeOffset) {
                                    lp4.leftMargin = (-extraPixels) / 2;
                                }
                                needsExpansion = true;
                            } else {
                                if (lp4.isOverflowButton) {
                                    lp4.extraPixels = extraPixels;
                                    lp4.expanded = true;
                                    lp4.rightMargin = (-extraPixels) / 2;
                                    needsExpansion = true;
                                } else {
                                    if (i10 != 0) {
                                        lp4.leftMargin = extraPixels / 2;
                                    }
                                    if (i10 != childCount - 1) {
                                        lp4.rightMargin = extraPixels / 2;
                                    }
                                }
                                i10++;
                                singleItem = singleItem2;
                                expandCount = expandCount2;
                            }
                        }
                        i10++;
                        singleItem = singleItem2;
                        expandCount = expandCount2;
                    }
                    float f = expandCount;
                } else {
                    boolean z4 = singleItem;
                    int i11 = visibleItemCount2;
                }
                if (needsExpansion) {
                    int i12 = 0;
                    while (true) {
                        int i13 = i12;
                        if (i13 >= childCount) {
                            break;
                        }
                        View child4 = getChildAt(i13);
                        LayoutParams lp5 = (LayoutParams) child4.getLayoutParams();
                        if (!lp5.expanded) {
                            smallestItemsAt = smallestItemsAt3;
                        } else {
                            smallestItemsAt = smallestItemsAt3;
                            child4.measure(View.MeasureSpec.makeMeasureSpec((lp5.cellsUsed * cellSize) + lp5.extraPixels, 1073741824), itemHeightSpec);
                        }
                        i12 = i13 + 1;
                        smallestItemsAt3 = smallestItemsAt;
                    }
                }
                if (heightMode == 1073741824) {
                    heightSize = maxChildHeight;
                } else {
                    heightSize = heightSize3;
                }
                setMeasuredDimension(widthSize2, heightSize);
            }
        } else {
            visibleItemCount2 = visibleItemCount;
        }
        singleItem = false;
        if (cellsRemaining2 > 0) {
        }
        int i82 = visibleItemCount2;
        if (needsExpansion) {
        }
        if (heightMode == 1073741824) {
        }
        setMeasuredDimension(widthSize2, heightSize);
    }

    static int measureChildForCells(View child, int cellSize, int cellsRemaining, int parentHeightMeasureSpec, int parentHeightPadding) {
        View view = child;
        int i = cellsRemaining;
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        int childHeightSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(parentHeightMeasureSpec) - parentHeightPadding, View.MeasureSpec.getMode(parentHeightMeasureSpec));
        ActionMenuItemView itemView = view instanceof ActionMenuItemView ? (ActionMenuItemView) view : null;
        boolean expandable = false;
        boolean hasText = itemView != null && itemView.hasText();
        int cellsUsed = 0;
        if (i > 0 && (!hasText || i >= 2)) {
            view.measure(View.MeasureSpec.makeMeasureSpec(cellSize * i, Integer.MIN_VALUE), childHeightSpec);
            int measuredWidth = view.getMeasuredWidth();
            cellsUsed = measuredWidth / cellSize;
            if (measuredWidth % cellSize != 0) {
                cellsUsed++;
            }
            if (hasText && cellsUsed < 2) {
                cellsUsed = 2;
            }
        }
        if (!lp.isOverflowButton && hasText) {
            expandable = true;
        }
        lp.expandable = expandable;
        lp.cellsUsed = cellsUsed;
        view.measure(View.MeasureSpec.makeMeasureSpec(cellsUsed * cellSize, 1073741824), childHeightSpec);
        return cellsUsed;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int overflowWidth;
        int dividerWidth;
        int midVertical;
        boolean isLayoutRtl;
        int l;
        int r;
        if (!this.mFormatItems) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        int childCount = getChildCount();
        int midVertical2 = (bottom - top) / 2;
        int dividerWidth2 = getDividerWidth();
        int nonOverflowCount = 0;
        int widthRemaining = ((right - left) - getPaddingRight()) - getPaddingLeft();
        boolean hasOverflow = false;
        boolean isLayoutRtl2 = isLayoutRtl();
        int widthRemaining2 = widthRemaining;
        int nonOverflowWidth = 0;
        int overflowWidth2 = 0;
        int i = 0;
        while (i < childCount) {
            View v = getChildAt(i);
            if (v.getVisibility() == 8) {
                midVertical = midVertical2;
                isLayoutRtl = isLayoutRtl2;
            } else {
                LayoutParams p = (LayoutParams) v.getLayoutParams();
                if (p.isOverflowButton) {
                    int overflowWidth3 = v.getMeasuredWidth();
                    if (hasDividerBeforeChildAt(i) != 0) {
                        overflowWidth3 += dividerWidth2;
                    }
                    int height = v.getMeasuredHeight();
                    if (isLayoutRtl2) {
                        isLayoutRtl = isLayoutRtl2;
                        l = getPaddingLeft() + p.leftMargin;
                        r = l + overflowWidth3;
                    } else {
                        isLayoutRtl = isLayoutRtl2;
                        r = (getWidth() - getPaddingRight()) - p.rightMargin;
                        l = r - overflowWidth3;
                    }
                    int t = midVertical2 - (height / 2);
                    midVertical = midVertical2;
                    v.layout(l, t, r, t + height);
                    widthRemaining2 -= overflowWidth3;
                    hasOverflow = true;
                    overflowWidth2 = overflowWidth3;
                } else {
                    midVertical = midVertical2;
                    isLayoutRtl = isLayoutRtl2;
                    int size = v.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                    nonOverflowWidth += size;
                    widthRemaining2 -= size;
                    if (hasDividerBeforeChildAt(i)) {
                        nonOverflowWidth += dividerWidth2;
                    }
                    nonOverflowCount++;
                }
            }
            i++;
            isLayoutRtl2 = isLayoutRtl;
            midVertical2 = midVertical;
        }
        int midVertical3 = midVertical2;
        boolean isLayoutRtl3 = isLayoutRtl2;
        int i2 = 1;
        if (childCount != 1 || hasOverflow) {
            if (hasOverflow) {
                i2 = 0;
            }
            int spacerCount = nonOverflowCount - i2;
            int i3 = 0;
            int spacerSize = Math.max(0, spacerCount > 0 ? widthRemaining2 / spacerCount : 0);
            if (isLayoutRtl3) {
                int startRight = getWidth() - getPaddingRight();
                while (i3 < childCount) {
                    View v2 = getChildAt(i3);
                    LayoutParams lp = (LayoutParams) v2.getLayoutParams();
                    int spacerCount2 = spacerCount;
                    if (v2.getVisibility() == 8) {
                        dividerWidth = dividerWidth2;
                        overflowWidth = overflowWidth2;
                    } else if (lp.isOverflowButton != 0) {
                        dividerWidth = dividerWidth2;
                        overflowWidth = overflowWidth2;
                    } else {
                        int startRight2 = startRight - lp.rightMargin;
                        int width = v2.getMeasuredWidth();
                        int height2 = v2.getMeasuredHeight();
                        int t2 = midVertical3 - (height2 / 2);
                        dividerWidth = dividerWidth2;
                        overflowWidth = overflowWidth2;
                        v2.layout(startRight2 - width, t2, startRight2, t2 + height2);
                        startRight = startRight2 - ((lp.leftMargin + width) + spacerSize);
                    }
                    i3++;
                    spacerCount = spacerCount2;
                    dividerWidth2 = dividerWidth;
                    overflowWidth2 = overflowWidth;
                }
                int i4 = dividerWidth2;
                int i5 = overflowWidth2;
            } else {
                int i6 = dividerWidth2;
                int i7 = overflowWidth2;
                int startLeft = getPaddingLeft();
                while (i3 < childCount) {
                    View v3 = getChildAt(i3);
                    LayoutParams lp2 = (LayoutParams) v3.getLayoutParams();
                    if (v3.getVisibility() != 8 && !lp2.isOverflowButton) {
                        int startLeft2 = startLeft + lp2.leftMargin;
                        int width2 = v3.getMeasuredWidth();
                        int height3 = v3.getMeasuredHeight();
                        int t3 = midVertical3 - (height3 / 2);
                        v3.layout(startLeft2, t3, startLeft2 + width2, t3 + height3);
                        startLeft = startLeft2 + lp2.rightMargin + width2 + spacerSize;
                    }
                    i3++;
                }
            }
            return;
        }
        View v4 = getChildAt(0);
        int width3 = v4.getMeasuredWidth();
        int height4 = v4.getMeasuredHeight();
        int l2 = ((right - left) / 2) - (width3 / 2);
        int t4 = midVertical3 - (height4 / 2);
        int i8 = width3;
        v4.layout(l2, t4, l2 + width3, t4 + height4);
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

    /* access modifiers changed from: protected */
    public LayoutParams generateDefaultLayoutParams() {
        LayoutParams params = new LayoutParams(-2, -2);
        params.gravity = 16;
        return params;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        LayoutParams result;
        if (p == null) {
            return generateDefaultLayoutParams();
        }
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

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && (p instanceof LayoutParams);
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

    public void setMenuCallbacks(MenuPresenter.Callback pcb, MenuBuilder.Callback mcb) {
        this.mActionMenuPresenterCallback = pcb;
        this.mMenuBuilderCallback = mcb;
    }

    public MenuBuilder peekMenu() {
        return this.mMenu;
    }

    public boolean showOverflowMenu() {
        return this.mPresenter != null && this.mPresenter.showOverflowMenu();
    }

    public boolean hideOverflowMenu() {
        return this.mPresenter != null && this.mPresenter.hideOverflowMenu();
    }

    public boolean isOverflowMenuShowing() {
        return this.mPresenter != null && this.mPresenter.isOverflowMenuShowing();
    }

    public boolean isOverflowMenuShowPending() {
        return this.mPresenter != null && this.mPresenter.isOverflowMenuShowPending();
    }

    public void dismissPopupMenus() {
        if (this.mPresenter != null) {
            this.mPresenter.dismissPopupMenus();
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasDividerBeforeChildAt(int childIndex) {
        if (childIndex == 0) {
            return false;
        }
        View childBefore = getChildAt(childIndex - 1);
        View child = getChildAt(childIndex);
        boolean result = false;
        if (childIndex < getChildCount() && (childBefore instanceof ActionMenuChildView)) {
            result = false | ((ActionMenuChildView) childBefore).needsDividerAfter();
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

    /* access modifiers changed from: protected */
    public ActionMenuPresenter getPresenter() {
        return this.mPresenter;
    }

    /* access modifiers changed from: protected */
    public boolean getFormatItems() {
        return this.mFormatItems;
    }

    /* access modifiers changed from: protected */
    public int getGeneratedItemPadding() {
        return this.mGeneratedItemPadding;
    }

    public void setSplitViewMaxSize(int maxSize) {
    }
}
