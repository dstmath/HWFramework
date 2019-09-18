package com.android.internal.view.menu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.android.internal.util.Protocol;
import com.android.internal.view.menu.MenuPresenter;

public class MenuDialogHelper implements MenuHelper, DialogInterface.OnKeyListener, DialogInterface.OnClickListener, DialogInterface.OnDismissListener, MenuPresenter.Callback {
    private AlertDialog mDialog;
    private MenuBuilder mMenu;
    ListMenuPresenter mPresenter;
    private MenuPresenter.Callback mPresenterCallback;

    public MenuDialogHelper(MenuBuilder menu) {
        this.mMenu = menu;
    }

    public void show(IBinder windowToken) {
        MenuBuilder menu = this.mMenu;
        AlertDialog.Builder builder = new AlertDialog.Builder(menu.getContext());
        this.mPresenter = new ListMenuPresenter(builder.getContext(), 17367172);
        this.mPresenter.setCallback(this);
        this.mMenu.addMenuPresenter(this.mPresenter);
        builder.setAdapter(this.mPresenter.getAdapter(), this);
        View headerView = menu.getHeaderView();
        if (headerView != null) {
            builder.setCustomTitle(headerView);
        } else {
            builder.setIcon(menu.getHeaderIcon()).setTitle(menu.getHeaderTitle());
        }
        builder.setOnKeyListener(this);
        this.mDialog = builder.create();
        this.mDialog.setOnDismissListener(this);
        WindowManager.LayoutParams lp = this.mDialog.getWindow().getAttributes();
        lp.type = 1003;
        if (windowToken != null) {
            lp.token = windowToken;
        }
        lp.flags |= Protocol.BASE_WIFI;
        this.mDialog.show();
    }

    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == 82 || keyCode == 4) {
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                Window win = this.mDialog.getWindow();
                if (win != null) {
                    View decor = win.getDecorView();
                    if (decor != null) {
                        KeyEvent.DispatcherState ds = decor.getKeyDispatcherState();
                        if (ds != null) {
                            ds.startTracking(event, this);
                            return true;
                        }
                    }
                }
            } else if (event.getAction() == 1 && !event.isCanceled()) {
                Window win2 = this.mDialog.getWindow();
                if (win2 != null) {
                    View decor2 = win2.getDecorView();
                    if (decor2 != null) {
                        KeyEvent.DispatcherState ds2 = decor2.getKeyDispatcherState();
                        if (ds2 != null && ds2.isTracking(event)) {
                            this.mMenu.close(true);
                            dialog.dismiss();
                            return true;
                        }
                    }
                }
            }
        }
        return this.mMenu.performShortcut(keyCode, event, 0);
    }

    public void setPresenterCallback(MenuPresenter.Callback cb) {
        this.mPresenterCallback = cb;
    }

    public void dismiss() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        this.mPresenter.onCloseMenu(this.mMenu, true);
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (allMenusAreClosing || menu == this.mMenu) {
            dismiss();
        }
        if (this.mPresenterCallback != null) {
            this.mPresenterCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    public boolean onOpenSubMenu(MenuBuilder subMenu) {
        if (this.mPresenterCallback != null) {
            return this.mPresenterCallback.onOpenSubMenu(subMenu);
        }
        return false;
    }

    public void onClick(DialogInterface dialog, int which) {
        this.mMenu.performItemAction((MenuItemImpl) this.mPresenter.getAdapter().getItem(which), 0);
    }
}
