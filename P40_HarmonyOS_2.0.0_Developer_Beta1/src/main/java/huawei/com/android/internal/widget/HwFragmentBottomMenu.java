package huawei.com.android.internal.widget;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class HwFragmentBottomMenu extends LinearLayout implements View.OnClickListener {
    private static final int DEFAULT_GROUP = 0;
    private static final int DOUBLE_DISPLAY_MENU_COUNT = 2;
    private static final int DOUBLE_MARGIN = 2;
    private static final float EXTRA_DP_VALUE = 0.5f;
    private static final int HEIGHT_HALF_DIV = 2;
    private static final boolean IS_DEBUG = false;
    private static final int MARGIN_BETWEEN_BORDER = 20;
    private static final int MARGIN_BETWEEN_MENU = 4;
    private static final int MARGIN_BETWEEN_PARENT = 0;
    private static final int MAX_LOCATIONS = 2;
    private static final int MENUITEM_WIDTH_MAXIMUM = 62;
    private static final int MENUITEM_WIDTH_MINIMUM = 46;
    private static final int MENU_HEIGHT_MINIMUM = 50;
    private static final int MENU_ITEM_MAX_LINES = 2;
    private static final int MENU_TEXT_SIZE = 9;
    private static final String MORE_MENU_TAG = "more_menu";
    private static final int POPUPMENU_ITEM_HEIGHT = 32;
    private static final int POPUPMENU_WIDTH_MAXIMUM = 240;
    private static final int POPUPMENU_WIDTH_MINIMUM = 136;
    private static final String TAG = "HwFragmentBottomMenu";
    private Context mContext;
    private Drawable mDefinedBackground;
    private int mDisplayMenuCount = 0;
    private int mDisplayMoreMenuCount = 0;
    private HwFragmentMenu mFragmentMenu;
    private OnFragmentMenuListener mFragmentMenuListener;
    private boolean mIsMenuTitleVisible = true;
    private boolean mIsUsingDefined;
    private LayoutInflater mLayoutInflater;
    private ListView mListView;
    private int mMenuBarHeight = 0;
    private int mMenuBarWidth = 0;
    private int mMenuItemWidth = 0;
    private HwFragmentMenuItemView mMoreMenuItemView;
    private List<HwFragmentMenuItem> mMoreMenuList = new ArrayList();
    private int mParentViewWidth = 0;
    private View mPopupLayout;
    private PopupWindow mPopupWindow;
    private int mSetMenuCount = 0;
    private int mSetMoreMenuCount = 0;
    private int mTextSize;

    public interface OnFragmentMenuListener {
        boolean onFragmentMenuItemClick(MenuItem menuItem);
    }

    public HwFragmentBottomMenu(Context context) {
        super(context);
        init(context);
    }

    public HwFragmentBottomMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HwFragmentBottomMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        if (this.mContext.getSystemService("layout_inflater") instanceof LayoutInflater) {
            this.mLayoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        }
        this.mTextSize = this.mContext.getResources().getDimensionPixelSize(34472166) + this.mContext.getResources().getDimensionPixelSize(34471967);
        initBackgroundResource();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mFragmentMenu = new HwFragmentMenu(getContext());
    }

    public HwFragmentMenu getFragmentMenu() {
        if (this.mFragmentMenu == null) {
            this.mFragmentMenu = new HwFragmentMenu(getContext());
        }
        return this.mFragmentMenu;
    }

    public void setFragmentMenu(HwFragmentMenu fragmentMenu) {
        this.mFragmentMenu = fragmentMenu;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        PopupWindow popupWindow = this.mPopupWindow;
        if (popupWindow != null && popupWindow.isShowing()) {
            this.mPopupWindow.dismiss();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onWindowVisibilityChanged(int visibility) {
        PopupWindow popupWindow;
        super.onWindowVisibilityChanged(visibility);
        if (visibility != 0 && (popupWindow = this.mPopupWindow) != null && popupWindow.isShowing()) {
            this.mPopupWindow.dismiss();
        }
    }

    public void addMenu(Menu menu) {
        if (menu != null) {
            int size = menu.size();
            for (int i = 0; i < size; i++) {
                MenuItem item = menu.getItem(i);
                addMenu(item.getItemId(), item.getTitle(), item.getIcon(), item.isVisible());
            }
            refreshMenu();
        }
    }

    public MenuItem addMenu(int itemId, CharSequence title, int icon) {
        return addMenu(itemId, title, icon, true);
    }

    public MenuItem addMenu(int itemId, CharSequence title, int icon, boolean isVisible) {
        return addMenu(itemId, title, getContext().getResources().getDrawable(icon), isVisible);
    }

    public MenuItem addMenu(int itemId, CharSequence title, Drawable icon) {
        return addMenu(itemId, title, icon, true);
    }

    public MenuItem addMenu(int itemId, CharSequence title, Drawable icon, boolean isVisible) {
        return addMenu(0, itemId, title, icon, isVisible, false);
    }

    public MenuItem addMenu(int groupId, int itemId, CharSequence title, Drawable icon, boolean isVisible, boolean isChecked) {
        if (this.mFragmentMenu == null) {
            this.mFragmentMenu = new HwFragmentMenu(getContext());
        }
        MenuItem item = this.mFragmentMenu.findItem(itemId);
        if (item != null) {
            item.setVisible(isVisible);
            item.setEnabled(true);
            item.setIcon(icon);
            return item;
        }
        MenuItem item2 = this.mFragmentMenu.add(groupId, itemId, 0, title);
        if (item2 == null) {
            return null;
        }
        item2.setCheckable(isChecked);
        item2.setVisible(isVisible);
        if (icon != null) {
            item2.setIcon(icon);
            item2.setShowAsAction(2);
        } else {
            item2.setShowAsAction(0);
        }
        return item2;
    }

    public void refreshMenu() {
        removeAllViews();
        this.mSetMenuCount = 0;
        this.mSetMoreMenuCount = 0;
        this.mMoreMenuList.clear();
        int size = this.mFragmentMenu.size();
        for (int i = 0; i < size; i++) {
            if (this.mFragmentMenu.getItem(i) instanceof HwFragmentMenuItem) {
                HwFragmentMenuItem item = (HwFragmentMenuItem) this.mFragmentMenu.getItem(i);
                if (item.isVisible()) {
                    item.setMenuTitleVisible(this.mIsMenuTitleVisible);
                    item.setEnabled(true);
                    if (item.getIcon() != null) {
                        item.setShowAsAction(2);
                    } else {
                        item.setShowAsAction(0);
                    }
                    if (item.requiresActionButton()) {
                        View itemView = item.getFragmentMenuItemView();
                        addView(itemView);
                        itemView.setOnClickListener(this);
                        this.mSetMenuCount++;
                    } else {
                        this.mSetMoreMenuCount++;
                        this.mMoreMenuList.add(item);
                    }
                }
            }
        }
        setMoreMenuItemView();
        addView(this.mMoreMenuItemView);
        if (this.mSetMoreMenuCount > 0) {
            this.mMoreMenuItemView.setVisibility(0);
            this.mSetMenuCount++;
            return;
        }
        this.mMoreMenuItemView.setVisibility(4);
    }

    private void setMoreMenuItemView() {
        if (this.mMoreMenuItemView == null) {
            View view = View.inflate(getContext(), 34013218, null);
            if (view instanceof HwFragmentMenuItemView) {
                this.mMoreMenuItemView = (HwFragmentMenuItemView) view;
                this.mMoreMenuItemView.setTag(MORE_MENU_TAG);
                if (this.mIsMenuTitleVisible) {
                    this.mMoreMenuItemView.setText(33685549);
                }
                this.mMoreMenuItemView.setIcon(this.mContext.getDrawable(33751081));
                this.mMoreMenuItemView.setOnClickListener(this);
                this.mMoreMenuItemView.setSingleLine(false);
                this.mMoreMenuItemView.setMaxLines(2);
                this.mMoreMenuItemView.setEllipsize(TextUtils.TruncateAt.END);
            }
        }
    }

    private int measureHeight(int measureSpec, View view) {
        int result = 0;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        if (specMode == 1073741824) {
            return specSize;
        }
        if (view != null) {
            result = view.getMeasuredHeight();
        }
        if (specMode != Integer.MIN_VALUE) {
            return result;
        }
        return specSize < result ? specSize : result;
    }

    public void setDefinedBackground(Drawable background) {
        this.mIsUsingDefined = true;
        this.mDefinedBackground = background;
    }

    private void initBackgroundResource() {
        if (this.mIsUsingDefined) {
            if (this.mDefinedBackground == null) {
                setPadding(0, 0, 0, 0);
            }
            setBackground(this.mDefinedBackground);
        } else if (HwWidgetFactory.isHwDarkTheme(getContext())) {
            setBackgroundResource(33751610);
        } else {
            setBackgroundResource(33751609);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() != 0) {
            getChildAt(0).measure(widthMeasureSpec, heightMeasureSpec);
            int sizeWidth = View.MeasureSpec.getSize(widthMeasureSpec);
            int measureHeight = measureHeight(heightMeasureSpec, getChildAt(0));
            int minHeight = dip2px(50.0f);
            int sizeHeight = measureHeight > minHeight ? measureHeight : minHeight;
            if (sizeHeight > this.mMenuBarHeight) {
                this.mMenuBarHeight = sizeHeight;
            }
            if (sizeWidth == this.mMenuBarWidth) {
                measureChildren(widthMeasureSpec, heightMeasureSpec);
                setMeasuredDimension(this.mMenuBarWidth, this.mMenuBarHeight);
                return;
            }
            setCountAndWidth(sizeWidth);
            setMoreMenuItemViewProp();
            if (this.mIsMenuTitleVisible) {
                processMenuItemView();
            }
            setMeasuredDimension(this.mMenuBarWidth, this.mMenuBarHeight);
        }
    }

    private void setCountAndWidth(int sizeWidth) {
        this.mParentViewWidth = sizeWidth;
        this.mDisplayMenuCount = this.mSetMenuCount;
        this.mDisplayMoreMenuCount = this.mSetMoreMenuCount;
        this.mMenuItemWidth = dip2px(62.0f);
        this.mMenuBarWidth = (dip2px(70.0f) * this.mSetMenuCount) + dip2px(40.0f);
        if (this.mMenuBarWidth + dip2px(0.0f) > sizeWidth) {
            this.mMenuBarWidth = (dip2px(54.0f) * this.mSetMenuCount) + dip2px(40.0f);
            if (this.mMenuBarWidth + dip2px(0.0f) <= sizeWidth) {
                this.mMenuBarWidth = sizeWidth - dip2px(0.0f);
                this.mMenuItemWidth = ((this.mMenuBarWidth - dip2px(40.0f)) / this.mSetMenuCount) - dip2px(8.0f);
                return;
            }
            setMenuCountAndWidth(sizeWidth);
        }
    }

    private void setMenuCountAndWidth(int sizeWidth) {
        int maxCount = (sizeWidth - dip2px(40.0f)) / dip2px(54.0f);
        if (maxCount == 0) {
            this.mDisplayMenuCount = 1;
            int i = this.mSetMoreMenuCount;
            if (i == 0) {
                this.mDisplayMoreMenuCount = this.mSetMenuCount;
            } else {
                this.mDisplayMoreMenuCount = (this.mSetMenuCount + i) - 1;
            }
            this.mMenuBarWidth = sizeWidth - (dip2px(0.0f) * 2);
            this.mMenuItemWidth = this.mMenuBarWidth - dip2px(40.0f);
            if (this.mMenuItemWidth < 0) {
                this.mMenuItemWidth = 0;
                return;
            }
            return;
        }
        this.mDisplayMenuCount = maxCount;
        if (((sizeWidth - dip2px(40.0f)) / maxCount) - dip2px(8.0f) > dip2px(62.0f)) {
            this.mMenuBarWidth = (dip2px(66.0f) * maxCount) + (dip2px(20.0f) * 2);
            this.mMenuItemWidth = dip2px(62.0f);
        } else {
            this.mMenuBarWidth = sizeWidth - dip2px(0.0f);
            this.mMenuItemWidth = ((this.mMenuBarWidth - dip2px(40.0f)) / this.mDisplayMenuCount) - dip2px(8.0f);
        }
        int i2 = this.mSetMoreMenuCount;
        if (i2 == 0) {
            this.mDisplayMoreMenuCount = (this.mSetMenuCount + 1) - maxCount;
        } else {
            this.mDisplayMoreMenuCount = (this.mSetMenuCount + i2) - maxCount;
        }
    }

    private void setMoreMenuItemViewProp() {
        HwFragmentMenuItemView hwFragmentMenuItemView = this.mMoreMenuItemView;
        if (hwFragmentMenuItemView != null) {
            hwFragmentMenuItemView.setWidth(this.mMenuItemWidth);
            this.mMoreMenuItemView.setSingleLine(false);
            this.mMoreMenuItemView.setMaxLines(2);
            this.mMoreMenuItemView.setEllipsize(TextUtils.TruncateAt.END);
            this.mMoreMenuItemView.measure(0, 0);
        }
    }

    private void processMenuItemView() {
        int displayMenuCount = this.mDisplayMenuCount;
        boolean isTwoLines = false;
        if (this.mDisplayMoreMenuCount > 0) {
            displayMenuCount--;
            HwFragmentMenuItemView hwFragmentMenuItemView = this.mMoreMenuItemView;
            if (hwFragmentMenuItemView != null) {
                hwFragmentMenuItemView.setWidth(this.mMenuItemWidth);
                if (this.mMoreMenuItemView.getPaint().measureText(this.mMoreMenuItemView.getText().toString()) > ((float) this.mMenuItemWidth)) {
                    isTwoLines = true;
                }
                this.mMoreMenuItemView.setSingleLine(false);
                this.mMoreMenuItemView.setMaxLines(2);
                this.mMoreMenuItemView.setEllipsize(TextUtils.TruncateAt.END);
            }
        }
        for (int i = 0; i < displayMenuCount; i++) {
            View childView = getChildAt(i);
            if (childView instanceof HwFragmentMenuItemView) {
                HwFragmentMenuItemView menuItemView = (HwFragmentMenuItemView) childView;
                menuItemView.setWidth(this.mMenuItemWidth);
                if (this.mIsMenuTitleVisible) {
                    isTwoLines = menuItemView.getPaint().measureText(menuItemView.getText().toString()) > ((float) this.mMenuItemWidth);
                }
                menuItemView.setSingleLine(false);
                menuItemView.setMaxLines(2);
                menuItemView.setEllipsize(TextUtils.TruncateAt.END);
            }
        }
        int dip2px = dip2px(50.0f);
        if (isTwoLines) {
            dip2px += this.mTextSize;
        }
        this.mMenuBarHeight = dip2px;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        if (this.mDisplayMenuCount != 0) {
            int childCount = getChildCount();
            int i = this.mDisplayMenuCount;
            int i2 = this.mSetMenuCount;
            if (i < i2) {
                if (this.mSetMoreMenuCount > 0) {
                    processMenuItem(i - 1, i2 - 1, 8);
                } else {
                    this.mMoreMenuItemView.setVisibility(0);
                    processMenuItem(this.mDisplayMenuCount - 1, this.mSetMenuCount, 4);
                }
            } else if (i == i2) {
                if (this.mDisplayMoreMenuCount == 0) {
                    this.mMoreMenuItemView.setVisibility(4);
                }
                setChildViewVisible(childCount);
            } else {
                Log.w(TAG, "invalid menu count");
            }
            setChildViewLayout(childCount);
        }
    }

    private void processMenuItem(int lowerLimit, int upperLimit, int visibility) {
        for (int i = 0; i < this.mSetMenuCount; i++) {
            if (this.mFragmentMenu.getItem(i) instanceof HwFragmentMenuItem) {
                HwFragmentMenuItem item = (HwFragmentMenuItem) this.mFragmentMenu.getItem(i);
                if (MORE_MENU_TAG.equals(getChildAt(i).getTag())) {
                    return;
                }
                if (i < lowerLimit || i >= upperLimit) {
                    getChildAt(i).setVisibility(0);
                    removeFragmentMenuItem(item);
                } else {
                    getChildAt(i).setVisibility(visibility);
                    addFragmentMenuItem(0, item);
                }
            }
        }
    }

    private void addFragmentMenuItem(int index, HwFragmentMenuItem item) {
        if (!this.mMoreMenuList.contains(item)) {
            this.mMoreMenuList.add(index, item);
        }
    }

    private void removeFragmentMenuItem(HwFragmentMenuItem item) {
        if (this.mMoreMenuList.contains(item)) {
            this.mMoreMenuList.remove(item);
        }
    }

    private void setChildViewVisible(int childCount) {
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != 0 && (view instanceof HwFragmentMenuItemView)) {
                MenuItem menuItem = this.mFragmentMenu.findItem(view.getId());
                if (menuItem instanceof HwFragmentMenuItem) {
                    removeFragmentMenuItem((HwFragmentMenuItem) menuItem);
                }
                if (!MORE_MENU_TAG.equals(view.getTag())) {
                    view.setVisibility(0);
                }
            }
        }
    }

    private void setChildViewLayout(int childCount) {
        int count = 0;
        if (getLayoutDirection() == 1) {
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                if (childView.getVisibility() == 0 && (childView instanceof HwFragmentMenuItemView)) {
                    int dip2px = dip2px(20.0f);
                    int i2 = this.mDisplayMenuCount;
                    int dip2px2 = dip2px + ((((i2 - 1) - count) % i2) * this.mMenuItemWidth) + ((((((i2 - 1) - count) % i2) * 2) + 1) * dip2px(4.0f));
                    int paddingTop = getPaddingTop();
                    int dip2px3 = dip2px(20.0f);
                    int i3 = this.mDisplayMenuCount;
                    childView.layout(dip2px2, paddingTop, dip2px3 + (((((i3 - 1) - count) % i3) + 1) * this.mMenuItemWidth) + ((((((i3 - 1) - count) % i3) * 2) + 1) * dip2px(4.0f)), getHeight() + getPaddingTop());
                    count++;
                }
            }
            return;
        }
        for (int i4 = 0; i4 < childCount; i4++) {
            View childView2 = getChildAt(i4);
            if (childView2.getVisibility() == 0 && (childView2 instanceof HwFragmentMenuItemView)) {
                int dip2px4 = dip2px(20.0f);
                int i5 = this.mDisplayMenuCount;
                int dip2px5 = dip2px4 + ((count % i5) * this.mMenuItemWidth) + ((((count % i5) * 2) + 1) * dip2px(4.0f));
                int paddingTop2 = getPaddingTop();
                int dip2px6 = dip2px(20.0f);
                int i6 = this.mDisplayMenuCount;
                childView2.layout(dip2px5, paddingTop2, dip2px6 + (((count % i6) + 1) * this.mMenuItemWidth) + ((((count % i6) * 2) + 1) * dip2px(4.0f)), getHeight() + getPaddingTop());
                count++;
            }
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (MORE_MENU_TAG.equals(view.getTag())) {
            showPopup(view);
            return;
        }
        OnFragmentMenuListener onFragmentMenuListener = this.mFragmentMenuListener;
        if (onFragmentMenuListener != null) {
            onFragmentMenuListener.onFragmentMenuItemClick(this.mFragmentMenu.findItem(view.getId()));
        } else {
            Log.w(TAG, "invalid click");
        }
    }

    @SuppressLint({"InflateParams"})
    public void showPopup(View view) {
        if (this.mFragmentMenu != null && this.mMoreMenuList.size() != 0) {
            configPopupWindow();
            int popupWidth = this.mPopupWindow.getContentView().getMeasuredWidth();
            if (popupWidth < dip2px(136.0f)) {
                popupWidth = dip2px(136.0f);
            } else if (popupWidth > dip2px(240.0f)) {
                popupWidth = dip2px(240.0f);
            } else {
                Log.w(TAG, "invalid popupWidth");
            }
            if (popupWidth > this.mParentViewWidth) {
                int minMenuWidth = dip2px(136.0f);
                int i = this.mParentViewWidth;
                if (i <= minMenuWidth) {
                    i = minMenuWidth;
                }
                popupWidth = i;
            }
            this.mPopupWindow.setWidth(popupWidth);
            int[] locations = new int[2];
            view.getLocationOnScreen(locations);
            int posX = ((locations[0] + view.getWidth()) + dip2px(24.0f)) - popupWidth;
            if (getLayoutDirection() == 1) {
                posX = locations[0] - dip2px(24.0f);
            }
            this.mPopupWindow.showAtLocation(view, 0, posX, (locations[1] + (view.getHeight() / 2)) - (dip2px(32.0f) * this.mMoreMenuList.size()));
        }
    }

    private void configPopupWindow() {
        FragmentMenuItemAdapter adapter = new FragmentMenuItemAdapter(getContext(), 34013219, this.mMoreMenuList);
        if (this.mPopupLayout == null) {
            this.mPopupLayout = this.mLayoutInflater.inflate(34013220, (ViewGroup) null);
            this.mListView = (ListView) this.mPopupLayout.findViewById(34603085);
            this.mPopupWindow = new PopupWindow(this.mPopupLayout);
        }
        this.mListView.setAdapter((ListAdapter) adapter);
        setListViewOnItemClickListener();
        this.mPopupWindow.setWidth(-2);
        this.mPopupWindow.setHeight(-2);
        this.mPopupWindow.setBackgroundDrawable(getResources().getDrawable(33751603));
        this.mPopupWindow.setFocusable(true);
        this.mPopupWindow.setOutsideTouchable(true);
        this.mPopupWindow.update();
        this.mPopupWindow.getContentView().measure(0, 0);
    }

    private void setListViewOnItemClickListener() {
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /* class huawei.com.android.internal.widget.$$Lambda$HwFragmentBottomMenu$sNW4x7fJ2nyS965xNIAFaP24Gp0 */

            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                HwFragmentBottomMenu.this.lambda$setListViewOnItemClickListener$0$HwFragmentBottomMenu(adapterView, view, i, j);
            }
        });
    }

    public /* synthetic */ void lambda$setListViewOnItemClickListener$0$HwFragmentBottomMenu(AdapterView parent, View view, int position, long id) {
        HwFragmentMenuItem fragmentMenuItem = this.mMoreMenuList.get(position);
        OnFragmentMenuListener onFragmentMenuListener = this.mFragmentMenuListener;
        if (!(onFragmentMenuListener == null || fragmentMenuItem == null)) {
            onFragmentMenuListener.onFragmentMenuItemClick(this.mFragmentMenu.findItem(fragmentMenuItem.getItemId()));
        }
        if (this.mPopupWindow.isShowing()) {
            this.mPopupWindow.dismiss();
        }
    }

    public void setMenuItemVisible(int itemId, boolean isVisible) {
        HwFragmentMenuItem item;
        HwFragmentMenu hwFragmentMenu = this.mFragmentMenu;
        if (hwFragmentMenu != null && (hwFragmentMenu.findItem(itemId) instanceof HwFragmentMenuItem) && (item = (HwFragmentMenuItem) this.mFragmentMenu.findItem(itemId)) != null) {
            item.setVisible(isVisible);
            refreshMenu();
            if (item.requiresActionButton() && this.mSetMenuCount < this.mDisplayMenuCount) {
                measure(0, 0);
            }
        }
    }

    public void updateMenuItem(int itemId, int titleId, int iconId) {
        if (this.mFragmentMenu != null) {
            CharSequence title = null;
            Drawable icon = iconId != 0 ? getContext().getResources().getDrawable(iconId) : null;
            if (titleId != 0) {
                title = this.mContext.getResources().getText(titleId);
            }
            updateMenuItem(itemId, title, icon);
        }
    }

    public void updateMenuItem(int itemId, CharSequence title, Drawable icon) {
        HwFragmentMenu hwFragmentMenu = this.mFragmentMenu;
        if (hwFragmentMenu != null) {
            MenuItem menuItem = hwFragmentMenu.findItem(itemId);
            if (menuItem instanceof HwFragmentMenuItem) {
                HwFragmentMenuItem item = (HwFragmentMenuItem) menuItem;
                if (icon != null) {
                    item.setIcon(icon);
                    item.setShowAsAction(2);
                }
                if (title == null) {
                    return;
                }
                if (this.mIsMenuTitleVisible) {
                    item.setTitle(title);
                } else if (!item.requiresActionButton()) {
                    item.setTitle(title);
                } else {
                    Log.w(TAG, "invalid title");
                }
            }
        }
    }

    public void setMenuTitleVisible(boolean isVisible) {
        this.mIsMenuTitleVisible = isVisible;
    }

    private int dip2px(float dpValue) {
        return (int) ((dpValue * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void setOnFragmentMenuListener(OnFragmentMenuListener fragmentMenuListener) {
        this.mFragmentMenuListener = fragmentMenuListener;
    }

    /* access modifiers changed from: private */
    public class FragmentMenuItemAdapter extends ArrayAdapter<HwFragmentMenuItem> {
        private List<HwFragmentMenuItem> mItems;

        FragmentMenuItemAdapter(Context context, int resourceId, List<HwFragmentMenuItem> items) {
            super(context, resourceId, items);
            this.mItems = items;
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        @SuppressLint({"InflateParams"})
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            View convertView = view;
            if (convertView == null) {
                convertView = HwFragmentBottomMenu.this.mLayoutInflater.inflate(34013219, (ViewGroup) null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(34603086);
                convertView.setTag(holder);
            } else if (convertView.getTag() instanceof ViewHolder) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(34603086);
                convertView.setTag(holder);
            }
            holder.text.setText(this.mItems.get(position).getTitle());
            holder.text.setId(this.mItems.get(position).getItemId());
            return convertView;
        }
    }

    private static class ViewHolder {
        TextView text;

        private ViewHolder() {
        }
    }

    public static class HwFragmentMenu implements Menu {
        private Context mContext;
        private List<MenuItem> mMenuItemList = new ArrayList();

        public HwFragmentMenu(Context context) {
            this.mContext = context;
        }

        @Override // android.view.Menu
        public MenuItem add(CharSequence title) {
            return null;
        }

        @Override // android.view.Menu
        public MenuItem add(int titleRes) {
            return null;
        }

        @Override // android.view.Menu
        public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
            if (itemId < 0) {
                return null;
            }
            MenuItem item = findItem(itemId);
            if (item != null) {
                item.setTitle(title);
                return item;
            }
            MenuItem item2 = new HwFragmentMenuItem(this.mContext, itemId, title);
            this.mMenuItemList.add(item2);
            return item2;
        }

        @Override // android.view.Menu
        public MenuItem add(int groupId, int itemId, int order, int titleRes) {
            return add(groupId, itemId, order, this.mContext.getText(titleRes));
        }

        @Override // android.view.Menu
        public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
            return 0;
        }

        @Override // android.view.Menu
        public SubMenu addSubMenu(CharSequence title) {
            return null;
        }

        @Override // android.view.Menu
        public SubMenu addSubMenu(int titleRes) {
            return null;
        }

        @Override // android.view.Menu
        public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
            return null;
        }

        @Override // android.view.Menu
        public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
            return null;
        }

        @Override // android.view.Menu
        public void clear() {
            this.mMenuItemList.clear();
        }

        @Override // android.view.Menu
        public void close() {
        }

        @Override // android.view.Menu
        public MenuItem findItem(int id) {
            for (MenuItem item : this.mMenuItemList) {
                if (item.getItemId() == id) {
                    return item;
                }
            }
            return null;
        }

        @Override // android.view.Menu
        public MenuItem getItem(int index) {
            return this.mMenuItemList.get(index);
        }

        @Override // android.view.Menu
        public boolean hasVisibleItems() {
            return false;
        }

        @Override // android.view.Menu
        public boolean isShortcutKey(int keyCode, KeyEvent event) {
            return false;
        }

        @Override // android.view.Menu
        public boolean performIdentifierAction(int id, int flags) {
            return false;
        }

        @Override // android.view.Menu
        public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
            return false;
        }

        @Override // android.view.Menu
        public void removeGroup(int groupId) {
            clear();
        }

        @Override // android.view.Menu
        public void removeItem(int id) {
            this.mMenuItemList.remove(findItem(id));
        }

        @Override // android.view.Menu
        public void setGroupCheckable(int group, boolean isCheckable, boolean isExclusive) {
        }

        @Override // android.view.Menu
        public void setGroupEnabled(int group, boolean isEnabled) {
        }

        @Override // android.view.Menu
        public void setGroupVisible(int group, boolean isVisible) {
        }

        @Override // android.view.Menu
        public void setQwertyMode(boolean isQwerty) {
        }

        @Override // android.view.Menu
        public int size() {
            return this.mMenuItemList.size();
        }
    }

    public static class HwFragmentMenuItem implements MenuItem {
        private Context mContext;
        private HwFragmentMenuItemView mFragmentMenuItemView;
        private boolean mIsCheckable = true;
        private boolean mIsChecked = false;
        private boolean mIsEnabled = true;
        private boolean mIsVisible = true;
        private int mItemId;
        private int mShowAsAction;
        private CharSequence mTitle;

        public HwFragmentMenuItem() {
        }

        public HwFragmentMenuItem(Context context, int itemId, CharSequence title) {
            this.mContext = context;
            this.mItemId = itemId;
            this.mTitle = title;
        }

        private void initFragmentMenuItemView() {
            if (this.mFragmentMenuItemView == null) {
                View view = View.inflate(this.mContext, 34013218, null);
                if (view instanceof HwFragmentMenuItemView) {
                    this.mFragmentMenuItemView = (HwFragmentMenuItemView) view;
                    this.mFragmentMenuItemView.setId(this.mItemId);
                    this.mFragmentMenuItemView.setText(this.mTitle);
                    this.mFragmentMenuItemView.setClickable(this.mIsEnabled);
                    this.mFragmentMenuItemView.setFocusable(this.mIsEnabled);
                    this.mFragmentMenuItemView.setActivated(this.mIsEnabled);
                }
            }
        }

        @Override // android.view.MenuItem
        public boolean collapseActionView() {
            return false;
        }

        @Override // android.view.MenuItem
        public boolean expandActionView() {
            return false;
        }

        @Override // android.view.MenuItem
        public ActionProvider getActionProvider() {
            return null;
        }

        @Override // android.view.MenuItem
        public View getActionView() {
            return null;
        }

        @Override // android.view.MenuItem
        public char getAlphabeticShortcut() {
            return 0;
        }

        @Override // android.view.MenuItem
        public int getGroupId() {
            return 0;
        }

        @Override // android.view.MenuItem
        public Drawable getIcon() {
            HwFragmentMenuItemView hwFragmentMenuItemView = this.mFragmentMenuItemView;
            if (hwFragmentMenuItemView != null) {
                return hwFragmentMenuItemView.getIcon();
            }
            return null;
        }

        @Override // android.view.MenuItem
        public Intent getIntent() {
            return null;
        }

        @Override // android.view.MenuItem
        public int getItemId() {
            return this.mItemId;
        }

        @Override // android.view.MenuItem
        public ContextMenu.ContextMenuInfo getMenuInfo() {
            return null;
        }

        @Override // android.view.MenuItem
        public char getNumericShortcut() {
            return 0;
        }

        @Override // android.view.MenuItem
        public int getOrder() {
            return 0;
        }

        @Override // android.view.MenuItem
        public SubMenu getSubMenu() {
            return null;
        }

        @Override // android.view.MenuItem
        public CharSequence getTitle() {
            return this.mTitle;
        }

        @Override // android.view.MenuItem
        public CharSequence getTitleCondensed() {
            return null;
        }

        @Override // android.view.MenuItem
        public boolean hasSubMenu() {
            return false;
        }

        @Override // android.view.MenuItem
        public boolean isActionViewExpanded() {
            return false;
        }

        @Override // android.view.MenuItem
        public boolean isCheckable() {
            return this.mIsCheckable;
        }

        @Override // android.view.MenuItem
        public boolean isChecked() {
            return this.mIsChecked;
        }

        @Override // android.view.MenuItem
        public boolean isEnabled() {
            return this.mIsEnabled;
        }

        @Override // android.view.MenuItem
        public boolean isVisible() {
            return this.mIsVisible;
        }

        @Override // android.view.MenuItem
        public MenuItem setActionProvider(ActionProvider arg0) {
            return null;
        }

        @Override // android.view.MenuItem
        public MenuItem setActionView(View arg0) {
            return null;
        }

        @Override // android.view.MenuItem
        public MenuItem setActionView(int resId) {
            return null;
        }

        @Override // android.view.MenuItem
        public MenuItem setAlphabeticShortcut(char alphaChar) {
            return this;
        }

        @Override // android.view.MenuItem
        public MenuItem setCheckable(boolean isCheckable) {
            this.mIsCheckable = isCheckable;
            return this;
        }

        @Override // android.view.MenuItem
        public MenuItem setChecked(boolean isChecked) {
            this.mIsChecked = isChecked;
            return this;
        }

        @Override // android.view.MenuItem
        public MenuItem setEnabled(boolean isEnabled) {
            this.mIsEnabled = isEnabled;
            HwFragmentMenuItemView hwFragmentMenuItemView = this.mFragmentMenuItemView;
            if (hwFragmentMenuItemView != null) {
                hwFragmentMenuItemView.setClickable(isEnabled);
                this.mFragmentMenuItemView.setFocusable(isEnabled);
                this.mFragmentMenuItemView.setActivated(isEnabled);
            }
            return this;
        }

        @Override // android.view.MenuItem
        public MenuItem setIcon(Drawable icon) {
            initFragmentMenuItemView();
            HwFragmentMenuItemView hwFragmentMenuItemView = this.mFragmentMenuItemView;
            if (hwFragmentMenuItemView != null) {
                hwFragmentMenuItemView.setIcon(icon);
            }
            return this;
        }

        @Override // android.view.MenuItem
        public MenuItem setIcon(int iconRes) {
            initFragmentMenuItemView();
            HwFragmentMenuItemView hwFragmentMenuItemView = this.mFragmentMenuItemView;
            if (hwFragmentMenuItemView != null) {
                hwFragmentMenuItemView.setIcon(iconRes);
            }
            return this;
        }

        @Override // android.view.MenuItem
        public MenuItem setIntent(Intent intent) {
            return null;
        }

        @Override // android.view.MenuItem
        public MenuItem setNumericShortcut(char numericChar) {
            return this;
        }

        @Override // android.view.MenuItem
        public MenuItem setOnActionExpandListener(MenuItem.OnActionExpandListener listener) {
            return null;
        }

        @Override // android.view.MenuItem
        public MenuItem setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener menuItemClickListener) {
            return null;
        }

        @Override // android.view.MenuItem
        public MenuItem setShortcut(char numericChar, char alphaChar) {
            return null;
        }

        @Override // android.view.MenuItem
        public void setShowAsAction(int actionEnum) {
            this.mShowAsAction = actionEnum;
            if (actionEnum > 0) {
                initFragmentMenuItemView();
            }
        }

        @Override // android.view.MenuItem
        public MenuItem setShowAsActionFlags(int actionEnum) {
            return null;
        }

        @Override // android.view.MenuItem
        public MenuItem setTitle(CharSequence title) {
            this.mTitle = title;
            HwFragmentMenuItemView hwFragmentMenuItemView = this.mFragmentMenuItemView;
            if (hwFragmentMenuItemView != null) {
                hwFragmentMenuItemView.setText(title);
            }
            return this;
        }

        @Override // android.view.MenuItem
        public MenuItem setTitle(int title) {
            return setTitle(this.mContext.getResources().getString(title));
        }

        @Override // android.view.MenuItem
        public MenuItem setTitleCondensed(CharSequence title) {
            return this;
        }

        @Override // android.view.MenuItem
        public MenuItem setVisible(boolean isVisible) {
            this.mIsVisible = isVisible;
            return this;
        }

        public HwFragmentMenuItemView getFragmentMenuItemView() {
            initFragmentMenuItemView();
            return this.mFragmentMenuItemView;
        }

        public boolean requiresActionButton() {
            return (this.mShowAsAction & 2) == 2;
        }

        /* access modifiers changed from: protected */
        public void setMenuTitleVisible(boolean isVisible) {
            HwFragmentMenuItemView hwFragmentMenuItemView;
            if (!isVisible && (hwFragmentMenuItemView = this.mFragmentMenuItemView) != null) {
                hwFragmentMenuItemView.setText((CharSequence) null);
            }
        }
    }
}
