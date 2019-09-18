package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import java.util.Iterator;

/* compiled from: CommandParams */
class SelectItemParams extends CommandParams {
    boolean mLoadTitleIcon = false;
    Menu mMenu = null;

    SelectItemParams(CommandDetails cmdDet, Menu menu, boolean loadTitleIcon) {
        super(cmdDet);
        this.mMenu = menu;
        this.mLoadTitleIcon = loadTitleIcon;
    }

    /* access modifiers changed from: package-private */
    public boolean setIcon(Bitmap icon) {
        if (icon == null || this.mMenu == null) {
            return false;
        }
        if (!this.mLoadTitleIcon || this.mMenu.titleIcon != null) {
            Iterator<Item> it = this.mMenu.items.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Item item = it.next();
                if (item.icon == null) {
                    item.icon = icon;
                    break;
                }
            }
        } else {
            this.mMenu.titleIcon = icon;
        }
        return true;
    }
}
