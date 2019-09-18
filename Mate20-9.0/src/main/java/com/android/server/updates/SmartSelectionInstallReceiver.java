package com.android.server.updates;

public class SmartSelectionInstallReceiver extends ConfigUpdateInstallReceiver {
    public SmartSelectionInstallReceiver() {
        super("/data/misc/textclassifier/", "textclassifier.model", "metadata/classification", "version");
    }

    /* access modifiers changed from: protected */
    public boolean verifyVersion(int current, int alternative) {
        return true;
    }
}
