package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.MenuPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import com.android.internal.view.menu.MenuPresenter;

/* access modifiers changed from: package-private */
public final class StandardMenuPopup extends MenuPopup implements PopupWindow.OnDismissListener, AdapterView.OnItemClickListener, MenuPresenter, View.OnKeyListener {
    private static final int ITEM_LAYOUT = 17367226;
    private final MenuAdapter mAdapter;
    private View mAnchorView;
    private final View.OnAttachStateChangeListener mAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        /* class com.android.internal.view.menu.StandardMenuPopup.AnonymousClass2 */

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View v) {
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View v) {
            if (StandardMenuPopup.this.mTreeObserver != null) {
                if (!StandardMenuPopup.this.mTreeObserver.isAlive()) {
                    StandardMenuPopup.this.mTreeObserver = v.getViewTreeObserver();
                }
                StandardMenuPopup.this.mTreeObserver.removeGlobalOnLayoutListener(StandardMenuPopup.this.mGlobalLayoutListener);
            }
            v.removeOnAttachStateChangeListener(this);
        }
    };
    private int mContentWidth;
    private final Context mContext;
    private int mDropDownGravity = 0;
    private final ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        /* class com.android.internal.view.menu.StandardMenuPopup.AnonymousClass1 */

        @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
        public void onGlobalLayout() {
            if (StandardMenuPopup.this.isShowing() && !StandardMenuPopup.this.mPopup.isModal()) {
                View anchor = StandardMenuPopup.this.mShownAnchorView;
                if (anchor == null || !anchor.isShown()) {
                    StandardMenuPopup.this.dismiss();
                } else {
                    StandardMenuPopup.this.mPopup.show();
                }
            }
        }
    };
    private boolean mHasContentWidth;
    private final MenuBuilder mMenu;
    private PopupWindow.OnDismissListener mOnDismissListener;
    private final boolean mOverflowOnly;
    private final MenuPopupWindow mPopup;
    private final int mPopupMaxWidth;
    private final int mPopupStyleAttr;
    private final int mPopupStyleRes;
    private MenuPresenter.Callback mPresenterCallback;
    private boolean mShowTitle;
    private View mShownAnchorView;
    private ViewTreeObserver mTreeObserver;
    private boolean mWasDismissed;

    public StandardMenuPopup(Context context, MenuBuilder menu, View anchorView, int popupStyleAttr, int popupStyleRes, boolean overflowOnly) {
        int i;
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mMenu = menu;
        this.mOverflowOnly = overflowOnly;
        this.mAdapter = new MenuAdapter(menu, LayoutInflater.from(context), this.mOverflowOnly, 17367226);
        this.mPopupStyleAttr = popupStyleAttr;
        this.mPopupStyleRes = popupStyleRes;
        Resources res = context.getResources();
        int widthPixels = res.getDisplayMetrics().widthPixels;
        int prefDialogWidth = res.getDimensionPixelSize(R.dimen.config_prefDialogWidth);
        this.mAnchorView = anchorView;
        this.mPopup = new MenuPopupWindow(this.mContext, null, this.mPopupStyleAttr, this.mPopupStyleRes);
        if (HwWidgetFactory.isHwTheme(context)) {
            int popupMaxSize = res.getDimensionPixelSize(com.android.hwext.internal.R.dimen.popup_max_emui);
            Drawable popupBackground = this.mPopup.getBackground();
            Rect paddingRect = new Rect();
            if (popupBackground != null) {
                popupBackground.getPadding(paddingRect);
            }
            if (widthPixels > paddingRect.left + popupMaxSize + paddingRect.right) {
                i = popupMaxSize;
            } else {
                i = widthPixels - (paddingRect.left + paddingRect.right);
            }
            this.mPopupMaxWidth = i;
        } else {
            this.mPopupMaxWidth = widthPixels / 2 > prefDialogWidth ? widthPixels / 2 : prefDialogWidth;
        }
        menu.addMenuPresenter(this, context);
    }

    @Override // com.android.internal.view.menu.MenuPopup
    public void setForceShowIcon(boolean forceShow) {
        this.mAdapter.setForceShowIcon(forceShow);
    }

    @Override // com.android.internal.view.menu.MenuPopup
    public void setGravity(int gravity) {
        this.mDropDownGravity = gravity;
    }

    private boolean tryShow() {
        View view;
        if (isShowing()) {
            return true;
        }
        if (this.mWasDismissed || (view = this.mAnchorView) == null) {
            return false;
        }
        this.mShownAnchorView = view;
        this.mPopup.setOnDismissListener(this);
        this.mPopup.setOnItemClickListener(this);
        this.mPopup.setAdapter(this.mAdapter);
        this.mPopup.setModal(true);
        View anchor = this.mShownAnchorView;
        boolean addGlobalListener = this.mTreeObserver == null;
        this.mTreeObserver = anchor.getViewTreeObserver();
        if (addGlobalListener) {
            this.mTreeObserver.addOnGlobalLayoutListener(this.mGlobalLayoutListener);
        }
        anchor.addOnAttachStateChangeListener(this.mAttachStateChangeListener);
        this.mPopup.setAnchorView(anchor);
        this.mPopup.setDropDownGravity(this.mDropDownGravity);
        if (!this.mHasContentWidth) {
            this.mContentWidth = measureIndividualMenuWidth(this.mAdapter, null, this.mContext, this.mPopupMaxWidth);
            this.mHasContentWidth = true;
        }
        this.mPopup.setContentWidth(this.mContentWidth);
        this.mPopup.setInputMethodMode(2);
        this.mPopup.setEpicenterBounds(getEpicenterBounds());
        this.mPopup.show();
        ListView listView = this.mPopup.getListView();
        listView.setOnKeyListener(this);
        if (this.mShowTitle && this.mMenu.getHeaderTitle() != null) {
            FrameLayout titleItemView = (FrameLayout) LayoutInflater.from(this.mContext).inflate(R.layout.popup_menu_header_item_layout, (ViewGroup) listView, false);
            TextView titleView = (TextView) titleItemView.findViewById(16908310);
            if (titleView != null) {
                titleView.setText(this.mMenu.getHeaderTitle());
            }
            titleItemView.setEnabled(false);
            listView.addHeaderView(titleItemView, null, false);
            this.mPopup.show();
        }
        return true;
    }

    @Override // com.android.internal.view.menu.ShowableListMenu
    public void show() {
        if (!tryShow()) {
            throw new IllegalStateException("StandardMenuPopup cannot be used without an anchor");
        }
    }

    @Override // com.android.internal.view.menu.ShowableListMenu
    public void dismiss() {
        if (isShowing()) {
            this.mPopup.dismiss();
        }
    }

    @Override // com.android.internal.view.menu.MenuPopup
    public void addMenu(MenuBuilder menu) {
    }

    @Override // com.android.internal.view.menu.ShowableListMenu
    public boolean isShowing() {
        return !this.mWasDismissed && this.mPopup.isShowing();
    }

    @Override // android.widget.PopupWindow.OnDismissListener
    public void onDismiss() {
        this.mWasDismissed = true;
        this.mMenu.close();
        ViewTreeObserver viewTreeObserver = this.mTreeObserver;
        if (viewTreeObserver != null) {
            if (!viewTreeObserver.isAlive()) {
                this.mTreeObserver = this.mShownAnchorView.getViewTreeObserver();
            }
            this.mTreeObserver.removeGlobalOnLayoutListener(this.mGlobalLayoutListener);
            this.mTreeObserver = null;
        }
        this.mShownAnchorView.removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
        PopupWindow.OnDismissListener onDismissListener = this.mOnDismissListener;
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public void updateMenuView(boolean cleared) {
        this.mHasContentWidth = false;
        MenuAdapter menuAdapter = this.mAdapter;
        if (menuAdapter != null) {
            menuAdapter.notifyDataSetChanged();
        }
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public void setCallback(MenuPresenter.Callback cb) {
        this.mPresenterCallback = cb;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (subMenu.hasVisibleItems()) {
            MenuPopupHelper subPopup = new MenuPopupHelper(this.mContext, subMenu, this.mShownAnchorView, this.mOverflowOnly, this.mPopupStyleAttr, this.mPopupStyleRes);
            subPopup.setPresenterCallback(this.mPresenterCallback);
            subPopup.setForceShowIcon(MenuPopup.shouldPreserveIconSpacing(subMenu));
            subPopup.setOnDismissListener(this.mOnDismissListener);
            this.mOnDismissListener = null;
            this.mMenu.close(false);
            int horizontalOffset = this.mPopup.getHorizontalOffset();
            int verticalOffset = this.mPopup.getVerticalOffset();
            if ((Gravity.getAbsoluteGravity(this.mDropDownGravity, this.mAnchorView.getLayoutDirection()) & 7) == 5) {
                horizontalOffset += this.mAnchorView.getWidth();
            }
            if (subPopup.tryShow(horizontalOffset, verticalOffset)) {
                MenuPresenter.Callback callback = this.mPresenterCallback;
                if (callback == null) {
                    return true;
                }
                callback.onOpenSubMenu(subMenu);
                return true;
            }
        }
        return false;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (menu == this.mMenu) {
            dismiss();
            MenuPresenter.Callback callback = this.mPresenterCallback;
            if (callback != null) {
                callback.onCloseMenu(menu, allMenusAreClosing);
            }
        }
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public boolean flagActionItems() {
        return false;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public Parcelable onSaveInstanceState() {
        return null;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public void onRestoreInstanceState(Parcelable state) {
    }

    @Override // com.android.internal.view.menu.MenuPopup
    public void setAnchorView(View anchor) {
        this.mAnchorView = anchor;
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != 1 || keyCode != 82) {
            return false;
        }
        dismiss();
        return true;
    }

    @Override // com.android.internal.view.menu.MenuPopup
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    @Override // com.android.internal.view.menu.ShowableListMenu
    public ListView getListView() {
        return this.mPopup.getListView();
    }

    @Override // com.android.internal.view.menu.MenuPopup
    public void setHorizontalOffset(int x) {
        this.mPopup.setHorizontalOffset(x);
    }

    @Override // com.android.internal.view.menu.MenuPopup
    public void setVerticalOffset(int y) {
        this.mPopup.setVerticalOffset(y);
    }

    @Override // com.android.internal.view.menu.MenuPopup
    public void setShowTitle(boolean showTitle) {
        this.mShowTitle = showTitle;
    }

    @Override // com.android.internal.view.menu.MenuPopup
    public MenuPopupWindow getMenuPopup() {
        return this.mPopup;
    }
}
