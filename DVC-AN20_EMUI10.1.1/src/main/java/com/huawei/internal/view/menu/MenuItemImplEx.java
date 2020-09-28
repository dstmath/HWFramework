package com.huawei.internal.view.menu;

import android.view.MenuItem;
import com.android.internal.view.menu.MenuItemImpl;

public class MenuItemImplEx {
    public static void setProgressStatus(MenuItem item, int status, int progress) {
        if (item instanceof MenuItemImpl) {
            ((MenuItemImpl) item).setProgressStatus(status, progress);
        }
    }
}
