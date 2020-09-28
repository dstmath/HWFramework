package com.android.internal.view.menu;

import com.android.internal.view.menu.MenuPresenter;

public interface MenuHelper {
    void dismiss();

    void setPresenterCallback(MenuPresenter.Callback callback);
}
