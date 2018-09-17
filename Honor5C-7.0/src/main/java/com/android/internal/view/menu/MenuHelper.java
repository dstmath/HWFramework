package com.android.internal.view.menu;

import com.android.internal.view.menu.MenuPresenter.Callback;

public interface MenuHelper {
    void dismiss();

    void setPresenterCallback(Callback callback);
}
