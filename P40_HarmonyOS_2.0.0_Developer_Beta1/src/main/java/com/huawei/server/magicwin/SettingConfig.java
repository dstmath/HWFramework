package com.huawei.server.magicwin;

import com.huawei.server.utils.Utils;

public class SettingConfig {
    private boolean hwDialogShown;
    private int hwDragMode = 0;
    private boolean hwMagicWinEnabled;
    private String name;

    public SettingConfig(String name2, boolean isEnabled, boolean isDialogShown, int dragMode, String reason) {
        Utils.dbg(Utils.TAG_SETTING, "new setting:" + name2 + " " + isEnabled + " " + isDialogShown + " " + dragMode + " " + reason);
        this.name = name2;
        this.hwMagicWinEnabled = isEnabled;
        this.hwDialogShown = isDialogShown;
        this.hwDragMode = dragMode;
    }

    public String getName() {
        return this.name;
    }

    public boolean getHwMagicWinEnabled() {
        return this.hwMagicWinEnabled;
    }

    public void setMagicWinEnabled(boolean isEnabled) {
        this.hwMagicWinEnabled = isEnabled;
    }

    public boolean getHwDialogShown() {
        return this.hwDialogShown;
    }

    public void setHwDialogShown(boolean isDialogShown) {
        this.hwDialogShown = isDialogShown;
    }

    public int getDragMode() {
        return this.hwDragMode;
    }

    public void setDragMode(int mode) {
        this.hwDragMode = mode;
    }
}
