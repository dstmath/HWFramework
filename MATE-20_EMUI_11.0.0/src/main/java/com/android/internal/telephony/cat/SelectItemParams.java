package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;

/* access modifiers changed from: package-private */
/* compiled from: CommandParams */
public class SelectItemParams extends CommandParams {
    boolean mLoadTitleIcon = false;
    Menu mMenu = null;

    @UnsupportedAppUsage
    SelectItemParams(CommandDetails cmdDet, Menu menu, boolean loadTitleIcon) {
        super(cmdDet);
        this.mMenu = menu;
        this.mLoadTitleIcon = loadTitleIcon;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.CommandParams
    public boolean setIcon(Bitmap icon) {
        Menu menu;
        if (icon == null || (menu = this.mMenu) == null) {
            return false;
        }
        if (!this.mLoadTitleIcon || menu.titleIcon != null) {
            for (Item item : this.mMenu.items) {
                if (item.icon == null) {
                    item.icon = icon;
                    return true;
                }
            }
            return true;
        }
        this.mMenu.titleIcon = icon;
        return true;
    }
}
