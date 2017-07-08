package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.MenuPopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import com.android.internal.view.menu.MenuPresenter.Callback;

final class StandardMenuPopup extends MenuPopup implements OnDismissListener, OnItemClickListener, MenuPresenter, OnKeyListener {
    private final MenuAdapter mAdapter;
    private View mAnchorView;
    private final OnAttachStateChangeListener mAttachStateChangeListener;
    private int mContentWidth;
    private final Context mContext;
    private int mDropDownGravity;
    private final OnGlobalLayoutListener mGlobalLayoutListener;
    private boolean mHasContentWidth;
    private final MenuBuilder mMenu;
    private OnDismissListener mOnDismissListener;
    private final boolean mOverflowOnly;
    private final MenuPopupWindow mPopup;
    private final int mPopupMaxWidth;
    private final int mPopupStyleAttr;
    private final int mPopupStyleRes;
    private Callback mPresenterCallback;
    private boolean mShowTitle;
    private View mShownAnchorView;
    private ViewTreeObserver mTreeObserver;
    private boolean mWasDismissed;

    public StandardMenuPopup(Context context, MenuBuilder menu, View anchorView, int popupStyleAttr, int popupStyleRes, boolean overflowOnly) {
        this.mGlobalLayoutListener = new OnGlobalLayoutListener() {
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
        this.mAttachStateChangeListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

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
        this.mDropDownGravity = 0;
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mMenu = menu;
        this.mOverflowOnly = overflowOnly;
        this.mAdapter = new MenuAdapter(menu, LayoutInflater.from(context), this.mOverflowOnly);
        this.mPopupStyleAttr = popupStyleAttr;
        this.mPopupStyleRes = popupStyleRes;
        Resources res = context.getResources();
        this.mPopupMaxWidth = Math.max(res.getDisplayMetrics().widthPixels / 2, res.getDimensionPixelSize(R.dimen.config_prefDialogWidth));
        this.mAnchorView = anchorView;
        this.mPopup = new MenuPopupWindow(this.mContext, null, this.mPopupStyleAttr, this.mPopupStyleRes);
        menu.addMenuPresenter(this, context);
    }

    public void setForceShowIcon(boolean forceShow) {
        this.mAdapter.setForceShowIcon(forceShow);
    }

    public void setGravity(int gravity) {
        this.mDropDownGravity = gravity;
    }

    private boolean tryShow() {
        if (isShowing()) {
            return true;
        }
        if (this.mWasDismissed || this.mAnchorView == null) {
            return false;
        }
        this.mShownAnchorView = this.mAnchorView;
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
            this.mContentWidth = MenuPopup.measureIndividualMenuWidth(this.mAdapter, null, this.mContext, this.mPopupMaxWidth);
            this.mHasContentWidth = true;
        }
        this.mPopup.setContentWidth(this.mContentWidth);
        this.mPopup.setInputMethodMode(2);
        this.mPopup.setEpicenterBounds(getEpicenterBounds());
        this.mPopup.show();
        ViewGroup listView = this.mPopup.getListView();
        listView.setOnKeyListener(this);
        if (this.mShowTitle && this.mMenu.getHeaderTitle() != null) {
            FrameLayout titleItemView = (FrameLayout) LayoutInflater.from(this.mContext).inflate((int) R.layout.popup_menu_header_item_layout, listView, false);
            TextView titleView = (TextView) titleItemView.findViewById(R.id.title);
            if (titleView != null) {
                titleView.setText(this.mMenu.getHeaderTitle());
            }
            titleItemView.setEnabled(false);
            listView.addHeaderView(titleItemView, null, false);
            this.mPopup.show();
        }
        return true;
    }

    public void show() {
        if (!tryShow()) {
            throw new IllegalStateException("StandardMenuPopup cannot be used without an anchor");
        }
    }

    public void dismiss() {
        if (isShowing()) {
            this.mPopup.dismiss();
        }
    }

    public void addMenu(MenuBuilder menu) {
    }

    public boolean isShowing() {
        return !this.mWasDismissed ? this.mPopup.isShowing() : false;
    }

    public void onDismiss() {
        this.mWasDismissed = true;
        this.mMenu.close();
        if (this.mTreeObserver != null) {
            if (!this.mTreeObserver.isAlive()) {
                this.mTreeObserver = this.mShownAnchorView.getViewTreeObserver();
            }
            this.mTreeObserver.removeGlobalOnLayoutListener(this.mGlobalLayoutListener);
            this.mTreeObserver = null;
        }
        this.mShownAnchorView.removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
        if (this.mOnDismissListener != null) {
            this.mOnDismissListener.onDismiss();
        }
    }

    public void updateMenuView(boolean cleared) {
        this.mHasContentWidth = false;
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public void setCallback(Callback cb) {
        this.mPresenterCallback = cb;
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (subMenu.hasVisibleItems()) {
            MenuPopupHelper subPopup = new MenuPopupHelper(this.mContext, subMenu, this.mShownAnchorView, this.mOverflowOnly, this.mPopupStyleAttr, this.mPopupStyleRes);
            subPopup.setPresenterCallback(this.mPresenterCallback);
            subPopup.setForceShowIcon(MenuPopup.shouldPreserveIconSpacing(subMenu));
            subPopup.setOnDismissListener(this.mOnDismissListener);
            this.mOnDismissListener = null;
            this.mMenu.close(false);
            if (subPopup.tryShow(this.mPopup.getHorizontalOffset(), this.mPopup.getVerticalOffset())) {
                if (this.mPresenterCallback != null) {
                    this.mPresenterCallback.onOpenSubMenu(subMenu);
                }
                return true;
            }
        }
        return false;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (menu == this.mMenu) {
            dismiss();
            if (this.mPresenterCallback != null) {
                this.mPresenterCallback.onCloseMenu(menu, allMenusAreClosing);
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

    public void setAnchorView(View anchor) {
        this.mAnchorView = anchor;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != 1 || keyCode != 82) {
            return false;
        }
        dismiss();
        return true;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    public ListView getListView() {
        return this.mPopup.getListView();
    }

    public void setHorizontalOffset(int x) {
        this.mPopup.setHorizontalOffset(x);
    }

    public void setVerticalOffset(int y) {
        this.mPopup.setVerticalOffset(y);
    }

    public void setShowTitle(boolean showTitle) {
        this.mShowTitle = showTitle;
    }

    public MenuPopupWindow getMenuPopup() {
        return this.mPopup;
    }
}
