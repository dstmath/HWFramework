package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

/* compiled from: CommandParams */
class SelectItemParams extends CommandParams {
    boolean mLoadTitleIcon = false;
    Menu mMenu = null;

    SelectItemParams(CommandDetails cmdDet, Menu menu, boolean loadTitleIcon) {
        super(cmdDet);
        this.mMenu = menu;
        this.mLoadTitleIcon = loadTitleIcon;
    }

    boolean setIcon(Bitmap icon) {
        if (icon == null || this.mMenu == null) {
            return false;
        }
        if (!this.mLoadTitleIcon || this.mMenu.titleIcon != null) {
            for (Item item : this.mMenu.items) {
                if (item.icon == null) {
                    item.icon = icon;
                    break;
                }
            }
        }
        this.mMenu.titleIcon = icon;
        return true;
    }
}
