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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class HwFragmentBottomMenu extends LinearLayout implements View.OnClickListener {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_GROUP = 0;
    private static final int MARGIN_BETWEEN_BORDER = 20;
    private static final int MARGIN_BETWEEN_MENU = 4;
    private static final int MARGIN_BETWEEN_PARENT = 0;
    private static final int MENUITEM_WIDTH_MAXIMUM = 62;
    private static final int MENUITEM_WIDTH_MINIMUM = 46;
    private static final int MENU_HEIGHT_MINIMUM = 50;
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
    /* access modifiers changed from: private */
    public OnFragmentMenuListener mFragemntMenuListener;
    /* access modifiers changed from: private */
    public HwFragmentMenu mFragmentMenu;
    /* access modifiers changed from: private */
    public LayoutInflater mLayoutInflater;
    private ListView mListView;
    private int mMenuBarHeight = 0;
    private int mMenuBarWidth = 0;
    private int mMenuItemWidth = 0;
    private boolean mMenuTitleVisible = true;
    private HwFragmentMenuItemView mMoreMenuItemView;
    /* access modifiers changed from: private */
    public List<HwFragmentMenuItem> mMoreMenuList = new ArrayList();
    private int mParentViewWidth = 0;
    private View mPopupLayout;
    /* access modifiers changed from: private */
    public PopupWindow mPopupWindow;
    private int mSetMenuCount = 0;
    private int mSetMoreMenuCount = 0;
    private int mTextSize;
    private boolean mUsingDefined;

    private class FragmentMenuItemAdapter extends ArrayAdapter<HwFragmentMenuItem> {
        private List<HwFragmentMenuItem> mItems;

        public FragmentMenuItemAdapter(Context context, int resourceId, List<HwFragmentMenuItem> items) {
            super(context, resourceId, items);
            this.mItems = items;
        }

        @SuppressLint({"InflateParams"})
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = HwFragmentBottomMenu.this.mLayoutInflater.inflate(34013219, null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(34603086);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.text.setText(this.mItems.get(position).getTitle());
            holder.text.setId(this.mItems.get(position).getItemId());
            return convertView;
        }
    }

    public static class HwFragmentMenu implements Menu {
        private Context mContext;
        private List<MenuItem> mMenuItemList = new ArrayList();

        public HwFragmentMenu(Context mContext2) {
            this.mContext = mContext2;
        }

        public MenuItem add(CharSequence title) {
            return null;
        }

        public MenuItem add(int titleRes) {
            return null;
        }

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

        public MenuItem add(int groupId, int itemId, int order, int titleRes) {
            return add(groupId, itemId, order, this.mContext.getText(titleRes));
        }

        public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
            return 0;
        }

        public SubMenu addSubMenu(CharSequence title) {
            return null;
        }

        public SubMenu addSubMenu(int titleRes) {
            return null;
        }

        public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
            return null;
        }

        public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
            return null;
        }

        public void clear() {
            this.mMenuItemList.clear();
        }

        public void close() {
        }

        public MenuItem findItem(int id) {
            for (MenuItem item : this.mMenuItemList) {
                if (item.getItemId() == id) {
                    return item;
                }
            }
            return null;
        }

        public MenuItem getItem(int index) {
            return this.mMenuItemList.get(index);
        }

        public boolean hasVisibleItems() {
            return false;
        }

        public boolean isShortcutKey(int keyCode, KeyEvent event) {
            return false;
        }

        public boolean performIdentifierAction(int id, int flags) {
            return false;
        }

        public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
            return false;
        }

        public void removeGroup(int groupId) {
            clear();
        }

        public void removeItem(int id) {
            this.mMenuItemList.remove(findItem(id));
        }

        public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
        }

        public void setGroupEnabled(int group, boolean enabled) {
        }

        public void setGroupVisible(int group, boolean visible) {
        }

        public void setQwertyMode(boolean isQwerty) {
        }

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
        private boolean mIsVisiable = true;
        private int mItemId;
        private int mShowAsAction;
        private CharSequence mTitle;

        public HwFragmentMenuItem() {
        }

        public HwFragmentMenuItem(Context mContext2, int itemId, CharSequence title) {
            this.mContext = mContext2;
            this.mItemId = itemId;
            this.mTitle = title;
        }

        private void initFragmentMenuItemView() {
            if (this.mFragmentMenuItemView == null) {
                this.mFragmentMenuItemView = (HwFragmentMenuItemView) View.inflate(this.mContext, 34013218, null);
                this.mFragmentMenuItemView.setId(this.mItemId);
                this.mFragmentMenuItemView.setText(this.mTitle);
                this.mFragmentMenuItemView.setClickable(this.mIsEnabled);
                this.mFragmentMenuItemView.setFocusable(this.mIsEnabled);
                this.mFragmentMenuItemView.setActivated(this.mIsEnabled);
            }
        }

        public boolean collapseActionView() {
            return false;
        }

        public boolean expandActionView() {
            return false;
        }

        public ActionProvider getActionProvider() {
            return null;
        }

        public View getActionView() {
            return null;
        }

        public char getAlphabeticShortcut() {
            return 0;
        }

        public int getGroupId() {
            return 0;
        }

        public Drawable getIcon() {
            if (this.mFragmentMenuItemView != null) {
                return this.mFragmentMenuItemView.getIcon();
            }
            return null;
        }

        public Intent getIntent() {
            return null;
        }

        public int getItemId() {
            return this.mItemId;
        }

        public ContextMenu.ContextMenuInfo getMenuInfo() {
            return null;
        }

        public char getNumericShortcut() {
            return 0;
        }

        public int getOrder() {
            return 0;
        }

        public SubMenu getSubMenu() {
            return null;
        }

        public CharSequence getTitle() {
            return this.mTitle;
        }

        public CharSequence getTitleCondensed() {
            return null;
        }

        public boolean hasSubMenu() {
            return false;
        }

        public boolean isActionViewExpanded() {
            return false;
        }

        public boolean isCheckable() {
            return this.mIsCheckable;
        }

        public boolean isChecked() {
            return this.mIsChecked;
        }

        public boolean isEnabled() {
            return this.mIsEnabled;
        }

        public boolean isVisible() {
            return this.mIsVisiable;
        }

        public MenuItem setActionProvider(ActionProvider arg0) {
            return null;
        }

        public MenuItem setActionView(View arg0) {
            return null;
        }

        public MenuItem setActionView(int resId) {
            return null;
        }

        public MenuItem setAlphabeticShortcut(char alphaChar) {
            return this;
        }

        public MenuItem setCheckable(boolean checkable) {
            this.mIsCheckable = checkable;
            return this;
        }

        public MenuItem setChecked(boolean checked) {
            this.mIsChecked = checked;
            return this;
        }

        public MenuItem setEnabled(boolean enabled) {
            this.mIsEnabled = enabled;
            if (this.mFragmentMenuItemView != null) {
                this.mFragmentMenuItemView.setClickable(enabled);
                this.mFragmentMenuItemView.setFocusable(enabled);
                this.mFragmentMenuItemView.setActivated(enabled);
            }
            return this;
        }

        public MenuItem setIcon(Drawable icon) {
            initFragmentMenuItemView();
            this.mFragmentMenuItemView.setIcon(icon);
            return this;
        }

        public MenuItem setIcon(int iconRes) {
            initFragmentMenuItemView();
            this.mFragmentMenuItemView.setIcon(iconRes);
            return this;
        }

        public MenuItem setIntent(Intent intent) {
            return null;
        }

        public MenuItem setNumericShortcut(char numericChar) {
            return this;
        }

        public MenuItem setOnActionExpandListener(MenuItem.OnActionExpandListener listener) {
            return null;
        }

        public MenuItem setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener menuItemClickListener) {
            return null;
        }

        public MenuItem setShortcut(char numericChar, char alphaChar) {
            return null;
        }

        public void setShowAsAction(int actionEnum) {
            this.mShowAsAction = actionEnum;
            if (actionEnum > 0) {
                initFragmentMenuItemView();
            }
        }

        public MenuItem setShowAsActionFlags(int actionEnum) {
            return null;
        }

        public MenuItem setTitle(CharSequence title) {
            this.mTitle = title;
            if (this.mFragmentMenuItemView != null) {
                this.mFragmentMenuItemView.setText(title);
            }
            return this;
        }

        public MenuItem setTitle(int title) {
            return setTitle((CharSequence) this.mContext.getResources().getString(title));
        }

        public MenuItem setTitleCondensed(CharSequence title) {
            return this;
        }

        public MenuItem setVisible(boolean visible) {
            this.mIsVisiable = visible;
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
        public void setMenuTitleVisible(boolean visible) {
            if (!visible) {
                this.mFragmentMenuItemView.setText(null);
            }
        }
    }

    public interface OnFragmentMenuListener {
        boolean onFragmentMenuItemClick(MenuItem menuItem);
    }

    private static class ViewHolder {
        TextView text;

        private ViewHolder() {
        }
    }

    public HwFragmentBottomMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HwFragmentBottomMenu(Context context) {
        super(context);
        init(context);
    }

    public HwFragmentBottomMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mTextSize = this.mContext.getResources().getDimensionPixelSize(34472166) + this.mContext.getResources().getDimensionPixelSize(34471967);
        initBackgroundResource();
    }

    /* access modifiers changed from: protected */
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

    public void setFragmentMenu(HwFragmentMenu mFragmentMenu2) {
        this.mFragmentMenu = mFragmentMenu2;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mPopupWindow != null && this.mPopupWindow.isShowing()) {
            this.mPopupWindow.dismiss();
        }
    }

    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != 0 && this.mPopupWindow != null && this.mPopupWindow.isShowing()) {
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

    public void refreshMenu() {
        removeAllViews();
        this.mSetMenuCount = 0;
        this.mSetMoreMenuCount = 0;
        this.mMoreMenuList.clear();
        int size = this.mFragmentMenu.size();
        for (int i = 0; i < size; i++) {
            HwFragmentMenuItem item = (HwFragmentMenuItem) this.mFragmentMenu.getItem(i);
            if (item.isVisible()) {
                item.setMenuTitleVisible(this.mMenuTitleVisible);
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
        if (this.mMoreMenuItemView == null) {
            this.mMoreMenuItemView = (HwFragmentMenuItemView) View.inflate(getContext(), 34013218, null);
            this.mMoreMenuItemView.setTag(MORE_MENU_TAG);
            if (this.mMenuTitleVisible) {
                this.mMoreMenuItemView.setText(33685549);
            }
            this.mMoreMenuItemView.setIcon(this.mContext.getDrawable(33751081));
            this.mMoreMenuItemView.setOnClickListener(this);
            this.mMoreMenuItemView.setSingleLine(false);
            this.mMoreMenuItemView.setMaxLines(2);
            this.mMoreMenuItemView.setEllipsize(TextUtils.TruncateAt.END);
        }
        addView(this.mMoreMenuItemView);
        if (this.mSetMoreMenuCount > 0) {
            this.mMoreMenuItemView.setVisibility(0);
            this.mSetMenuCount++;
            return;
        }
        this.mMoreMenuItemView.setVisibility(4);
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
        this.mUsingDefined = true;
        this.mDefinedBackground = background;
    }

    private void initBackgroundResource() {
        if (this.mUsingDefined) {
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
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i = heightMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() != 0) {
            View view = getChildAt(0);
            view.measure(widthMeasureSpec, i);
            int sizeWidth = View.MeasureSpec.getSize(widthMeasureSpec);
            int measureHeight = measureHeight(i, view);
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
                } else {
                    int maxcount = (sizeWidth - dip2px(40.0f)) / dip2px(54.0f);
                    if (maxcount == 0) {
                        this.mDisplayMenuCount = 1;
                        if (this.mSetMoreMenuCount == 0) {
                            this.mDisplayMoreMenuCount = this.mSetMenuCount;
                        } else {
                            this.mDisplayMoreMenuCount = (this.mSetMenuCount + this.mSetMoreMenuCount) - 1;
                        }
                        this.mMenuBarWidth = sizeWidth - (dip2px(0.0f) * 2);
                        this.mMenuItemWidth = this.mMenuBarWidth - dip2px(40.0f);
                        if (this.mMenuItemWidth < 0) {
                            this.mMenuItemWidth = 0;
                        }
                    } else {
                        this.mDisplayMenuCount = maxcount;
                        if (((sizeWidth - dip2px(40.0f)) / maxcount) - dip2px(8.0f) > dip2px(62.0f)) {
                            this.mMenuBarWidth = (dip2px(66.0f) * maxcount) + (dip2px(20.0f) * 2);
                            this.mMenuItemWidth = dip2px(62.0f);
                        } else {
                            this.mMenuBarWidth = sizeWidth - dip2px(0.0f);
                            this.mMenuItemWidth = ((this.mMenuBarWidth - dip2px(40.0f)) / this.mDisplayMenuCount) - dip2px(8.0f);
                        }
                        if (this.mSetMoreMenuCount == 0) {
                            this.mDisplayMoreMenuCount = (this.mSetMenuCount + 1) - maxcount;
                        } else {
                            this.mDisplayMoreMenuCount = (this.mSetMenuCount + this.mSetMoreMenuCount) - maxcount;
                        }
                    }
                }
            }
            if (this.mMoreMenuItemView != null) {
                this.mMoreMenuItemView.setWidth(this.mMenuItemWidth);
                this.mMoreMenuItemView.setSingleLine(false);
                this.mMoreMenuItemView.setMaxLines(2);
                this.mMoreMenuItemView.setEllipsize(TextUtils.TruncateAt.END);
                this.mMoreMenuItemView.measure(0, 0);
            }
            if (this.mMenuTitleVisible) {
                int displayMenuCount = this.mDisplayMenuCount;
                boolean isTwoLines = false;
                if (this.mDisplayMoreMenuCount > 0) {
                    displayMenuCount--;
                    if (this.mMoreMenuItemView != null) {
                        this.mMoreMenuItemView.setWidth(this.mMenuItemWidth);
                        if (this.mMenuTitleVisible && this.mMoreMenuItemView.getPaint().measureText(this.mMoreMenuItemView.getText().toString()) > ((float) this.mMenuItemWidth)) {
                            isTwoLines = true;
                        }
                        this.mMoreMenuItemView.setSingleLine(false);
                        this.mMoreMenuItemView.setMaxLines(2);
                        this.mMoreMenuItemView.setEllipsize(TextUtils.TruncateAt.END);
                    }
                }
                boolean isTwoLines2 = isTwoLines;
                for (int i2 = 0; i2 < displayMenuCount; i2++) {
                    View childView = getChildAt(i2);
                    if (childView instanceof HwFragmentMenuItemView) {
                        HwFragmentMenuItemView menuItemView = (HwFragmentMenuItemView) childView;
                        menuItemView.setWidth(this.mMenuItemWidth);
                        if (this.mMenuTitleVisible && menuItemView.getPaint().measureText(menuItemView.getText().toString()) > ((float) this.mMenuItemWidth)) {
                            isTwoLines2 = true;
                        }
                        menuItemView.setSingleLine(false);
                        menuItemView.setMaxLines(2);
                        menuItemView.setEllipsize(TextUtils.TruncateAt.END);
                    }
                }
                this.mMenuBarHeight = isTwoLines2 ? dip2px(50.0f) + this.mTextSize : dip2px(50.0f);
            }
            setMeasuredDimension(this.mMenuBarWidth, this.mMenuBarHeight);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mDisplayMenuCount != 0) {
            int childCount = getChildCount();
            int i = 0;
            if (this.mDisplayMenuCount < this.mSetMenuCount) {
                if (this.mSetMoreMenuCount > 0) {
                    for (int i2 = 0; i2 < this.mSetMenuCount; i2++) {
                        HwFragmentMenuItem item = (HwFragmentMenuItem) this.mFragmentMenu.getItem(i2);
                        if (MORE_MENU_TAG.equals(getChildAt(i2).getTag())) {
                            break;
                        }
                        if (i2 < this.mDisplayMenuCount - 1 || i2 >= this.mSetMenuCount - 1) {
                            getChildAt(i2).setVisibility(0);
                            if (this.mMoreMenuList.contains(item)) {
                                this.mMoreMenuList.remove(item);
                            }
                        } else {
                            getChildAt(i2).setVisibility(8);
                            if (!this.mMoreMenuList.contains(item)) {
                                this.mMoreMenuList.add(0, item);
                            }
                        }
                    }
                } else {
                    this.mMoreMenuItemView.setVisibility(0);
                    for (int i3 = 0; i3 < this.mSetMenuCount; i3++) {
                        HwFragmentMenuItem item2 = (HwFragmentMenuItem) this.mFragmentMenu.getItem(i3);
                        if (MORE_MENU_TAG.equals(getChildAt(i3).getTag())) {
                            break;
                        }
                        if (i3 < this.mDisplayMenuCount - 1 || i3 >= this.mSetMenuCount) {
                            getChildAt(i3).setVisibility(0);
                            if (this.mMoreMenuList.contains(item2)) {
                                this.mMoreMenuList.remove(item2);
                            }
                        } else {
                            getChildAt(i3).setVisibility(4);
                            if (!this.mMoreMenuList.contains(item2)) {
                                this.mMoreMenuList.add(0, item2);
                            }
                        }
                    }
                }
            } else if (this.mDisplayMenuCount == this.mSetMenuCount) {
                if (this.mDisplayMoreMenuCount == 0) {
                    this.mMoreMenuItemView.setVisibility(4);
                }
                for (int i4 = 0; i4 < childCount; i4++) {
                    if (getChildAt(i4).getVisibility() != 0) {
                        View view = getChildAt(i4);
                        if (view instanceof HwFragmentMenuItemView) {
                            HwFragmentMenuItem item3 = (HwFragmentMenuItem) this.mFragmentMenu.findItem(view.getId());
                            if (this.mMoreMenuList.contains(item3)) {
                                this.mMoreMenuList.remove(item3);
                            }
                            if (!MORE_MENU_TAG.equals(view.getTag())) {
                                getChildAt(i4).setVisibility(0);
                            }
                        }
                    }
                }
            }
            int count = 0;
            if (1 != getLayoutDirection()) {
                while (true) {
                    int i5 = i;
                    if (i5 >= childCount) {
                        break;
                    }
                    View childView = getChildAt(i5);
                    if (childView.getVisibility() == 0 && (childView instanceof HwFragmentMenuItemView)) {
                        childView.layout(dip2px(20.0f) + ((count % this.mDisplayMenuCount) * this.mMenuItemWidth) + ((((count % this.mDisplayMenuCount) * 2) + 1) * dip2px(4.0f)), getPaddingTop(), dip2px(20.0f) + (((count % this.mDisplayMenuCount) + 1) * this.mMenuItemWidth) + ((((count % this.mDisplayMenuCount) * 2) + 1) * dip2px(4.0f)), getHeight() + getPaddingTop());
                        count++;
                    }
                    i = i5 + 1;
                }
            } else {
                while (true) {
                    int i6 = i;
                    if (i6 >= childCount) {
                        break;
                    }
                    View childView2 = getChildAt(i6);
                    if (childView2.getVisibility() == 0 && (childView2 instanceof HwFragmentMenuItemView)) {
                        childView2.layout(dip2px(20.0f) + ((((this.mDisplayMenuCount - 1) - count) % this.mDisplayMenuCount) * this.mMenuItemWidth) + ((((((this.mDisplayMenuCount - 1) - count) % this.mDisplayMenuCount) * 2) + 1) * dip2px(4.0f)), getPaddingTop(), dip2px(20.0f) + (((((this.mDisplayMenuCount - 1) - count) % this.mDisplayMenuCount) + 1) * this.mMenuItemWidth) + ((((((this.mDisplayMenuCount - 1) - count) % this.mDisplayMenuCount) * 2) + 1) * dip2px(4.0f)), getHeight() + getPaddingTop());
                        count++;
                    }
                    i = i6 + 1;
                }
            }
        }
    }

    public MenuItem addMenu(int itemId, CharSequence title, int icon) {
        return addMenu(itemId, title, icon, true);
    }

    public MenuItem addMenu(int itemId, CharSequence title, int icon, boolean visible) {
        return addMenu(itemId, title, getContext().getResources().getDrawable(icon), visible);
    }

    public MenuItem addMenu(int itemId, CharSequence title, Drawable icon) {
        return addMenu(itemId, title, icon, true);
    }

    public MenuItem addMenu(int itemId, CharSequence title, Drawable icon, boolean visible) {
        return addMenu(0, itemId, title, icon, visible, false);
    }

    public MenuItem addMenu(int groupId, int itemId, CharSequence title, Drawable icon, boolean visible, boolean checked) {
        if (this.mFragmentMenu == null) {
            this.mFragmentMenu = new HwFragmentMenu(getContext());
        }
        MenuItem item = this.mFragmentMenu.findItem(itemId);
        if (item != null) {
            item.setVisible(visible);
            item.setEnabled(true);
            item.setIcon(icon);
            return item;
        }
        MenuItem item2 = this.mFragmentMenu.add(groupId, itemId, 0, title);
        item2.setCheckable(checked);
        item2.setVisible(visible);
        if (icon != null) {
            item2.setIcon(icon);
            item2.setShowAsAction(2);
        } else {
            item2.setShowAsAction(0);
        }
        return item2;
    }

    public void onClick(View v) {
        if (MORE_MENU_TAG.equals(v.getTag())) {
            showPopup(v);
        } else if (this.mFragemntMenuListener != null) {
            this.mFragemntMenuListener.onFragmentMenuItemClick(this.mFragmentMenu.findItem(v.getId()));
        }
    }

    @SuppressLint({"InflateParams"})
    public void showPopup(View view) {
        if (this.mFragmentMenu != null && this.mMoreMenuList.size() != 0) {
            FragmentMenuItemAdapter adapter = new FragmentMenuItemAdapter(getContext(), 34013219, this.mMoreMenuList);
            if (this.mPopupLayout == null) {
                this.mPopupLayout = this.mLayoutInflater.inflate(34013220, null);
                this.mListView = (ListView) this.mPopupLayout.findViewById(34603085);
                this.mPopupWindow = new PopupWindow(this.mPopupLayout);
            }
            this.mListView.setAdapter(adapter);
            this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    HwFragmentMenuItem fragmentMenuItem = (HwFragmentMenuItem) HwFragmentBottomMenu.this.mMoreMenuList.get(position);
                    if (!(HwFragmentBottomMenu.this.mFragemntMenuListener == null || fragmentMenuItem == null)) {
                        HwFragmentBottomMenu.this.mFragemntMenuListener.onFragmentMenuItemClick(HwFragmentBottomMenu.this.mFragmentMenu.findItem(fragmentMenuItem.getItemId()));
                    }
                    if (HwFragmentBottomMenu.this.mPopupWindow.isShowing()) {
                        HwFragmentBottomMenu.this.mPopupWindow.dismiss();
                    }
                }
            });
            this.mPopupWindow.setWidth(-2);
            this.mPopupWindow.setHeight(-2);
            this.mPopupWindow.setBackgroundDrawable(getResources().getDrawable(33751603));
            this.mPopupWindow.setFocusable(true);
            this.mPopupWindow.setOutsideTouchable(true);
            this.mPopupWindow.update();
            this.mPopupWindow.getContentView().measure(0, 0);
            int popupWidth = this.mPopupWindow.getContentView().getMeasuredWidth();
            Log.d(TAG, "popupWidth = " + popupWidth + ", " + dip2px(136.0f) + ", " + dip2px(240.0f));
            if (popupWidth < dip2px(136.0f)) {
                popupWidth = dip2px(136.0f);
            } else if (popupWidth > dip2px(240.0f)) {
                popupWidth = dip2px(240.0f);
            }
            if (popupWidth > this.mParentViewWidth) {
                int minMenuWidth = dip2px(136.0f);
                popupWidth = this.mParentViewWidth > minMenuWidth ? this.mParentViewWidth : minMenuWidth;
            }
            this.mPopupWindow.setWidth(popupWidth);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int xPos = ((location[0] + view.getWidth()) + dip2px(24.0f)) - popupWidth;
            Log.d(TAG, "location = " + location[0] + ", " + getLayoutDirection());
            if (1 == getLayoutDirection()) {
                xPos = location[0] - dip2px(24.0f);
            }
            this.mPopupWindow.showAtLocation(view, 0, xPos, (location[1] + (view.getHeight() / 2)) - (dip2px(32.0f) * this.mMoreMenuList.size()));
        }
    }

    public void setMenuItemVisible(int itemId, boolean visible) {
        if (this.mFragmentMenu != null) {
            HwFragmentMenuItem item = (HwFragmentMenuItem) this.mFragmentMenu.findItem(itemId);
            if (item != null) {
                item.setVisible(visible);
                refreshMenu();
                if (item.requiresActionButton() && this.mSetMenuCount < this.mDisplayMenuCount) {
                    measure(0, 0);
                }
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
        if (this.mFragmentMenu != null) {
            HwFragmentMenuItem item = (HwFragmentMenuItem) this.mFragmentMenu.findItem(itemId);
            if (item != null) {
                if (icon != null) {
                    item.setIcon(icon);
                    item.setShowAsAction(2);
                }
                if (title != null) {
                    if (this.mMenuTitleVisible) {
                        item.setTitle(title);
                    } else if (!item.requiresActionButton()) {
                        item.setTitle(title);
                    }
                }
            }
        }
    }

    public void setMenuTitleVisible(boolean visible) {
        this.mMenuTitleVisible = visible;
    }

    private int dip2px(float dpValue) {
        return (int) ((dpValue * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void setOnFragmentMenuListener(OnFragmentMenuListener fragemntMenuListener) {
        this.mFragemntMenuListener = fragemntMenuListener;
    }
}
