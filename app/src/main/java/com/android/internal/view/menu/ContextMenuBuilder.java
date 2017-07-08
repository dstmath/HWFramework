package com.android.internal.view.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.EventLog;
import android.view.ContextMenu;
import android.view.View;
import com.android.internal.R;

public class ContextMenuBuilder extends MenuBuilder implements ContextMenu {
    public ContextMenuBuilder(Context context) {
        super(context);
    }

    public ContextMenu setHeaderIcon(Drawable icon) {
        return (ContextMenu) super.setHeaderIconInt(icon);
    }

    public ContextMenu setHeaderIcon(int iconRes) {
        return (ContextMenu) super.setHeaderIconInt(iconRes);
    }

    public ContextMenu setHeaderTitle(CharSequence title) {
        return (ContextMenu) super.setHeaderTitleInt(title);
    }

    public ContextMenu setHeaderTitle(int titleRes) {
        return (ContextMenu) super.setHeaderTitleInt(titleRes);
    }

    public ContextMenu setHeaderView(View view) {
        return (ContextMenu) super.setHeaderViewInt(view);
    }

    public MenuDialogHelper showDialog(View originalView, IBinder token) {
        if (originalView != null) {
            originalView.createContextMenu(this);
        }
        if (getVisibleItems().size() <= 0) {
            return null;
        }
        EventLog.writeEvent(50001, 1);
        MenuDialogHelper helper = new MenuDialogHelper(this);
        helper.show(token);
        return helper;
    }

    public MenuPopupHelper showPopup(Context context, View originalView, float x, float y) {
        if (originalView != null) {
            originalView.createContextMenu(this);
        }
        if (getVisibleItems().size() <= 0) {
            return null;
        }
        EventLog.writeEvent(50001, 1);
        originalView.getLocationOnScreen(new int[2]);
        MenuPopupHelper helper = new MenuPopupHelper(context, this, originalView, false, R.attr.contextPopupMenuStyle);
        helper.show(Math.round(x), Math.round(y));
        return helper;
    }
}
