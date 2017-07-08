package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.MenuItemHoverListener;
import android.widget.MenuPopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import com.android.internal.view.menu.MenuPresenter.Callback;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final class CascadingMenuPopup extends MenuPopup implements MenuPresenter, OnKeyListener, OnDismissListener {
    private static final int HORIZ_POSITION_LEFT = 0;
    private static final int HORIZ_POSITION_RIGHT = 1;
    private static final int SUBMENU_TIMEOUT_MS = 200;
    private View mAnchorView;
    private final OnAttachStateChangeListener mAttachStateChangeListener;
    private final Context mContext;
    private int mDropDownGravity;
    private boolean mForceShowIcon;
    private final OnGlobalLayoutListener mGlobalLayoutListener;
    private boolean mHasXOffset;
    private boolean mHasYOffset;
    private int mLastPosition;
    private final MenuItemHoverListener mMenuItemHoverListener;
    private final int mMenuMaxWidth;
    private OnDismissListener mOnDismissListener;
    private final boolean mOverflowOnly;
    private final List<MenuBuilder> mPendingMenus;
    private final int mPopupStyleAttr;
    private final int mPopupStyleRes;
    private Callback mPresenterCallback;
    private int mRawDropDownGravity;
    private boolean mShouldCloseImmediately;
    private boolean mShowTitle;
    private final List<CascadingMenuInfo> mShowingMenus;
    private View mShownAnchorView;
    private final Handler mSubMenuHoverHandler;
    private ViewTreeObserver mTreeObserver;
    private int mXOffset;
    private int mYOffset;

    private static class CascadingMenuInfo {
        public final MenuBuilder menu;
        public final int position;
        public final MenuPopupWindow window;

        public CascadingMenuInfo(MenuPopupWindow window, MenuBuilder menu, int position) {
            this.window = window;
            this.menu = menu;
            this.position = position;
        }

        public ListView getListView() {
            return this.window.getListView();
        }
    }

    public CascadingMenuPopup(Context context, View anchor, int popupStyleAttr, int popupStyleRes, boolean overflowOnly) {
        this.mPendingMenus = new LinkedList();
        this.mShowingMenus = new ArrayList();
        this.mGlobalLayoutListener = new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (CascadingMenuPopup.this.isShowing() && CascadingMenuPopup.this.mShowingMenus.size() > 0 && !((CascadingMenuInfo) CascadingMenuPopup.this.mShowingMenus.get(CascadingMenuPopup.HORIZ_POSITION_LEFT)).window.isModal()) {
                    View anchor = CascadingMenuPopup.this.mShownAnchorView;
                    if (anchor == null || !anchor.isShown()) {
                        CascadingMenuPopup.this.dismiss();
                        return;
                    }
                    for (CascadingMenuInfo info : CascadingMenuPopup.this.mShowingMenus) {
                        info.window.show();
                    }
                }
            }
        };
        this.mAttachStateChangeListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                if (CascadingMenuPopup.this.mTreeObserver != null) {
                    if (!CascadingMenuPopup.this.mTreeObserver.isAlive()) {
                        CascadingMenuPopup.this.mTreeObserver = v.getViewTreeObserver();
                    }
                    CascadingMenuPopup.this.mTreeObserver.removeGlobalOnLayoutListener(CascadingMenuPopup.this.mGlobalLayoutListener);
                }
                v.removeOnAttachStateChangeListener(this);
            }
        };
        this.mMenuItemHoverListener = new MenuItemHoverListener() {

            /* renamed from: com.android.internal.view.menu.CascadingMenuPopup.3.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ MenuItem val$item;
                final /* synthetic */ MenuBuilder val$menu;
                final /* synthetic */ CascadingMenuInfo val$nextInfo;

                AnonymousClass1(CascadingMenuInfo val$nextInfo, MenuItem val$item, MenuBuilder val$menu) {
                    this.val$nextInfo = val$nextInfo;
                    this.val$item = val$item;
                    this.val$menu = val$menu;
                }

                public void run() {
                    if (this.val$nextInfo != null) {
                        CascadingMenuPopup.this.mShouldCloseImmediately = true;
                        this.val$nextInfo.menu.close(false);
                        CascadingMenuPopup.this.mShouldCloseImmediately = false;
                    }
                    if (this.val$item.isEnabled() && this.val$item.hasSubMenu()) {
                        this.val$menu.performItemAction(this.val$item, CascadingMenuPopup.HORIZ_POSITION_LEFT);
                    }
                }
            }

            public void onItemHoverExit(MenuBuilder menu, MenuItem item) {
                CascadingMenuPopup.this.mSubMenuHoverHandler.removeCallbacksAndMessages(menu);
            }

            public void onItemHoverEnter(MenuBuilder menu, MenuItem item) {
                CascadingMenuPopup.this.mSubMenuHoverHandler.removeCallbacksAndMessages(null);
                int menuIndex = -1;
                int count = CascadingMenuPopup.this.mShowingMenus.size();
                for (int i = CascadingMenuPopup.HORIZ_POSITION_LEFT; i < count; i += CascadingMenuPopup.HORIZ_POSITION_RIGHT) {
                    if (menu == ((CascadingMenuInfo) CascadingMenuPopup.this.mShowingMenus.get(i)).menu) {
                        menuIndex = i;
                        break;
                    }
                }
                if (menuIndex != -1) {
                    CascadingMenuInfo cascadingMenuInfo;
                    int nextIndex = menuIndex + CascadingMenuPopup.HORIZ_POSITION_RIGHT;
                    if (nextIndex < CascadingMenuPopup.this.mShowingMenus.size()) {
                        cascadingMenuInfo = (CascadingMenuInfo) CascadingMenuPopup.this.mShowingMenus.get(nextIndex);
                    } else {
                        cascadingMenuInfo = null;
                    }
                    CascadingMenuPopup.this.mSubMenuHoverHandler.postAtTime(new AnonymousClass1(cascadingMenuInfo, item, menu), menu, SystemClock.uptimeMillis() + 200);
                }
            }
        };
        this.mRawDropDownGravity = HORIZ_POSITION_LEFT;
        this.mDropDownGravity = HORIZ_POSITION_LEFT;
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mAnchorView = (View) Preconditions.checkNotNull(anchor);
        this.mPopupStyleAttr = popupStyleAttr;
        this.mPopupStyleRes = popupStyleRes;
        this.mOverflowOnly = overflowOnly;
        this.mForceShowIcon = false;
        this.mLastPosition = getInitialMenuPosition();
        Resources res = context.getResources();
        this.mMenuMaxWidth = Math.max(res.getDisplayMetrics().widthPixels / 2, res.getDimensionPixelSize(R.dimen.config_prefDialogWidth));
        this.mSubMenuHoverHandler = new Handler();
    }

    public void setForceShowIcon(boolean forceShow) {
        this.mForceShowIcon = forceShow;
    }

    private MenuPopupWindow createPopupWindow() {
        MenuPopupWindow popupWindow = new MenuPopupWindow(this.mContext, null, this.mPopupStyleAttr, this.mPopupStyleRes);
        popupWindow.setHoverListener(this.mMenuItemHoverListener);
        popupWindow.setOnItemClickListener(this);
        popupWindow.setOnDismissListener(this);
        popupWindow.setAnchorView(this.mAnchorView);
        popupWindow.setDropDownGravity(this.mDropDownGravity);
        popupWindow.setModal(true);
        popupWindow.setInputMethodMode(2);
        return popupWindow;
    }

    public void show() {
        if (!isShowing()) {
            for (MenuBuilder menu : this.mPendingMenus) {
                showMenu(menu);
            }
            this.mPendingMenus.clear();
            this.mShownAnchorView = this.mAnchorView;
            if (this.mShownAnchorView != null) {
                boolean addGlobalListener = this.mTreeObserver == null;
                this.mTreeObserver = this.mShownAnchorView.getViewTreeObserver();
                if (addGlobalListener) {
                    this.mTreeObserver.addOnGlobalLayoutListener(this.mGlobalLayoutListener);
                }
                this.mShownAnchorView.addOnAttachStateChangeListener(this.mAttachStateChangeListener);
            }
        }
    }

    public void dismiss() {
        int length = this.mShowingMenus.size();
        if (length > 0) {
            CascadingMenuInfo[] addedMenus = (CascadingMenuInfo[]) this.mShowingMenus.toArray(new CascadingMenuInfo[length]);
            for (int i = length - 1; i >= 0; i--) {
                CascadingMenuInfo info = addedMenus[i];
                if (info.window.isShowing()) {
                    info.window.dismiss();
                }
            }
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != HORIZ_POSITION_RIGHT || keyCode != 82) {
            return false;
        }
        dismiss();
        return true;
    }

    private int getInitialMenuPosition() {
        if (this.mAnchorView.getLayoutDirection() == HORIZ_POSITION_RIGHT) {
            return HORIZ_POSITION_LEFT;
        }
        return HORIZ_POSITION_RIGHT;
    }

    private int getNextMenuPosition(int nextMenuWidth) {
        ListView lastListView = ((CascadingMenuInfo) this.mShowingMenus.get(this.mShowingMenus.size() - 1)).getListView();
        int[] screenLocation = new int[2];
        lastListView.getLocationOnScreen(screenLocation);
        Rect displayFrame = new Rect();
        this.mShownAnchorView.getWindowVisibleDisplayFrame(displayFrame);
        return this.mLastPosition == HORIZ_POSITION_RIGHT ? (screenLocation[HORIZ_POSITION_LEFT] + lastListView.getWidth()) + nextMenuWidth > displayFrame.right ? HORIZ_POSITION_LEFT : HORIZ_POSITION_RIGHT : screenLocation[HORIZ_POSITION_LEFT] - nextMenuWidth < 0 ? HORIZ_POSITION_RIGHT : HORIZ_POSITION_LEFT;
    }

    public void addMenu(MenuBuilder menu) {
        menu.addMenuPresenter(this, this.mContext);
        if (isShowing()) {
            showMenu(menu);
        } else {
            this.mPendingMenus.add(menu);
        }
    }

    private void showMenu(MenuBuilder menu) {
        CascadingMenuInfo parentInfo;
        View findParentViewForSubmenu;
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        MenuAdapter adapter = new MenuAdapter(menu, inflater, this.mOverflowOnly);
        if (!isShowing() && this.mForceShowIcon) {
            adapter.setForceShowIcon(true);
        } else if (isShowing()) {
            adapter.setForceShowIcon(MenuPopup.shouldPreserveIconSpacing(menu));
        }
        int menuWidth = MenuPopup.measureIndividualMenuWidth(adapter, null, this.mContext, this.mMenuMaxWidth);
        MenuPopupWindow popupWindow = createPopupWindow();
        popupWindow.setAdapter(adapter);
        popupWindow.setWidth(menuWidth);
        popupWindow.setDropDownGravity(this.mDropDownGravity);
        if (this.mShowingMenus.size() > 0) {
            parentInfo = (CascadingMenuInfo) this.mShowingMenus.get(this.mShowingMenus.size() - 1);
            findParentViewForSubmenu = findParentViewForSubmenu(parentInfo, menu);
        } else {
            parentInfo = null;
            findParentViewForSubmenu = null;
        }
        if (findParentViewForSubmenu != null) {
            int x;
            popupWindow.setTouchModal(false);
            popupWindow.setEnterTransition(null);
            int nextMenuPosition = getNextMenuPosition(menuWidth);
            boolean showOnRight = nextMenuPosition == HORIZ_POSITION_RIGHT;
            this.mLastPosition = nextMenuPosition;
            int[] tempLocation = new int[2];
            findParentViewForSubmenu.getLocationInWindow(tempLocation);
            int parentOffsetLeft = parentInfo.window.getHorizontalOffset() + tempLocation[HORIZ_POSITION_LEFT];
            int parentOffsetTop = parentInfo.window.getVerticalOffset() + tempLocation[HORIZ_POSITION_RIGHT];
            if ((this.mDropDownGravity & 5) == 5) {
                if (showOnRight) {
                    x = parentOffsetLeft + menuWidth;
                } else {
                    x = parentOffsetLeft - findParentViewForSubmenu.getWidth();
                }
            } else if (showOnRight) {
                x = parentOffsetLeft + findParentViewForSubmenu.getWidth();
            } else {
                x = parentOffsetLeft - menuWidth;
            }
            popupWindow.setHorizontalOffset(x);
            int y = parentOffsetTop;
            popupWindow.setVerticalOffset(parentOffsetTop);
        } else {
            if (this.mHasXOffset) {
                popupWindow.setHorizontalOffset(this.mXOffset);
            }
            if (this.mHasYOffset) {
                popupWindow.setVerticalOffset(this.mYOffset);
            }
            popupWindow.setEpicenterBounds(getEpicenterBounds());
        }
        this.mShowingMenus.add(new CascadingMenuInfo(popupWindow, menu, this.mLastPosition));
        popupWindow.show();
        if (parentInfo == null && this.mShowTitle && menu.getHeaderTitle() != null) {
            ViewGroup listView = popupWindow.getListView();
            View titleItemView = (FrameLayout) inflater.inflate(R.layout.popup_menu_header_item_layout, listView, false);
            TextView titleView = (TextView) titleItemView.findViewById(R.id.title);
            titleItemView.setEnabled(false);
            titleView.setText(menu.getHeaderTitle());
            listView.addHeaderView(titleItemView, null, false);
            popupWindow.show();
        }
    }

    private MenuItem findMenuItemForSubmenu(MenuBuilder parent, MenuBuilder submenu) {
        int count = parent.size();
        for (int i = HORIZ_POSITION_LEFT; i < count; i += HORIZ_POSITION_RIGHT) {
            MenuItem item = parent.getItem(i);
            if (item.hasSubMenu() && submenu == item.getSubMenu()) {
                return item;
            }
        }
        return null;
    }

    private View findParentViewForSubmenu(CascadingMenuInfo parentInfo, MenuBuilder submenu) {
        MenuItem owner = findMenuItemForSubmenu(parentInfo.menu, submenu);
        if (owner == null) {
            return null;
        }
        int headersCount;
        MenuAdapter menuAdapter;
        ListView listView = parentInfo.getListView();
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter instanceof HeaderViewListAdapter) {
            HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter) listAdapter;
            headersCount = headerAdapter.getHeadersCount();
            menuAdapter = (MenuAdapter) headerAdapter.getWrappedAdapter();
        } else {
            headersCount = HORIZ_POSITION_LEFT;
            menuAdapter = (MenuAdapter) listAdapter;
        }
        int ownerPosition = -1;
        int count = menuAdapter.getCount();
        for (int i = HORIZ_POSITION_LEFT; i < count; i += HORIZ_POSITION_RIGHT) {
            if (owner == menuAdapter.getItem(i)) {
                ownerPosition = i;
                break;
            }
        }
        if (ownerPosition == -1) {
            return null;
        }
        int ownerViewPosition = (ownerPosition + headersCount) - listView.getFirstVisiblePosition();
        if (ownerViewPosition < 0 || ownerViewPosition >= listView.getChildCount()) {
            return null;
        }
        return listView.getChildAt(ownerViewPosition);
    }

    public boolean isShowing() {
        return this.mShowingMenus.size() > 0 ? ((CascadingMenuInfo) this.mShowingMenus.get(HORIZ_POSITION_LEFT)).window.isShowing() : false;
    }

    public void onDismiss() {
        CascadingMenuInfo dismissedInfo = null;
        int count = this.mShowingMenus.size();
        for (int i = HORIZ_POSITION_LEFT; i < count; i += HORIZ_POSITION_RIGHT) {
            CascadingMenuInfo info = (CascadingMenuInfo) this.mShowingMenus.get(i);
            if (!info.window.isShowing()) {
                dismissedInfo = info;
                break;
            }
        }
        if (dismissedInfo != null) {
            dismissedInfo.menu.close(false);
        }
    }

    public void updateMenuView(boolean cleared) {
        for (CascadingMenuInfo info : this.mShowingMenus) {
            MenuPopup.toMenuAdapter(info.getListView().getAdapter()).notifyDataSetChanged();
        }
    }

    public void setCallback(Callback cb) {
        this.mPresenterCallback = cb;
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        for (CascadingMenuInfo info : this.mShowingMenus) {
            if (subMenu == info.menu) {
                info.getListView().requestFocus();
                return true;
            }
        }
        if (!subMenu.hasVisibleItems()) {
            return false;
        }
        addMenu(subMenu);
        if (this.mPresenterCallback != null) {
            this.mPresenterCallback.onOpenSubMenu(subMenu);
        }
        return true;
    }

    private int findIndexOfAddedMenu(MenuBuilder menu) {
        int count = this.mShowingMenus.size();
        for (int i = HORIZ_POSITION_LEFT; i < count; i += HORIZ_POSITION_RIGHT) {
            if (menu == ((CascadingMenuInfo) this.mShowingMenus.get(i)).menu) {
                return i;
            }
        }
        return -1;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        int menuIndex = findIndexOfAddedMenu(menu);
        if (menuIndex >= 0) {
            int nextMenuIndex = menuIndex + HORIZ_POSITION_RIGHT;
            if (nextMenuIndex < this.mShowingMenus.size()) {
                ((CascadingMenuInfo) this.mShowingMenus.get(nextMenuIndex)).menu.close(false);
            }
            CascadingMenuInfo info = (CascadingMenuInfo) this.mShowingMenus.remove(menuIndex);
            info.menu.removeMenuPresenter(this);
            if (this.mShouldCloseImmediately) {
                info.window.setExitTransition(null);
                info.window.setAnimationStyle(HORIZ_POSITION_LEFT);
            }
            info.window.dismiss();
            int count = this.mShowingMenus.size();
            if (count > 0) {
                this.mLastPosition = ((CascadingMenuInfo) this.mShowingMenus.get(count - 1)).position;
            } else {
                this.mLastPosition = getInitialMenuPosition();
            }
            if (count == 0) {
                dismiss();
                if (this.mPresenterCallback != null) {
                    this.mPresenterCallback.onCloseMenu(menu, true);
                }
                if (this.mTreeObserver != null) {
                    if (this.mTreeObserver.isAlive()) {
                        this.mTreeObserver.removeGlobalOnLayoutListener(this.mGlobalLayoutListener);
                    }
                    this.mTreeObserver = null;
                }
                this.mShownAnchorView.removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
                this.mOnDismissListener.onDismiss();
            } else if (allMenusAreClosing) {
                ((CascadingMenuInfo) this.mShowingMenus.get(HORIZ_POSITION_LEFT)).menu.close(false);
            }
        }
    }

    public boolean flagActionItems() {
        return false;
    }

    public Parcelable onSaveInstanceState() {
        return null;
    }

    public void onRestoreInstanceState(Parcelable state) {
    }

    public void setGravity(int dropDownGravity) {
        if (this.mRawDropDownGravity != dropDownGravity) {
            this.mRawDropDownGravity = dropDownGravity;
            this.mDropDownGravity = Gravity.getAbsoluteGravity(dropDownGravity, this.mAnchorView.getLayoutDirection());
        }
    }

    public void setAnchorView(View anchor) {
        if (this.mAnchorView != anchor) {
            this.mAnchorView = anchor;
            this.mDropDownGravity = Gravity.getAbsoluteGravity(this.mRawDropDownGravity, this.mAnchorView.getLayoutDirection());
        }
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    public ListView getListView() {
        return this.mShowingMenus.isEmpty() ? null : ((CascadingMenuInfo) this.mShowingMenus.get(this.mShowingMenus.size() - 1)).getListView();
    }

    public void setHorizontalOffset(int x) {
        this.mHasXOffset = true;
        this.mXOffset = x;
    }

    public void setVerticalOffset(int y) {
        this.mHasYOffset = true;
        this.mYOffset = y;
    }

    public void setShowTitle(boolean showTitle) {
        this.mShowTitle = showTitle;
    }
}
