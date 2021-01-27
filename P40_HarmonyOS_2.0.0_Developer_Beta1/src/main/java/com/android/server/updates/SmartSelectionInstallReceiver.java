package com.android.server.updates;

public class SmartSelectionInstallReceiver extends ConfigUpdateInstallReceiver {
    public SmartSelectionInstallReceiver() {
        super("/data/misc/textclassifier/", "textclassifier.model", "metadata/classification", "version");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.updates.ConfigUpdateInstallReceiver
    public boolean verifyVersion(int current, int alternative) {
        return true;
    }
}
