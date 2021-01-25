package android.support.v4.view;

import android.annotation.SuppressLint;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.os.BuildCompat;
import android.view.Menu;
import android.view.MenuItem;

public final class MenuCompat {
    @Deprecated
    public static void setShowAsAction(MenuItem item, int actionEnum) {
        item.setShowAsAction(actionEnum);
    }

    @SuppressLint({"NewApi"})
    public static void setGroupDividerEnabled(Menu menu, boolean enabled) {
        if (menu instanceof SupportMenu) {
            ((SupportMenu) menu).setGroupDividerEnabled(enabled);
        } else if (BuildCompat.isAtLeastP()) {
            menu.setGroupDividerEnabled(enabled);
        }
    }

    private MenuCompat() {
    }
}
