package com.android.server.updates;

public class SmartSelectionInstallReceiver extends ConfigUpdateInstallReceiver {
    public SmartSelectionInstallReceiver() {
        super("/data/misc/textclassifier/", "textclassifier.smartselection.model", "metadata/smartselection", "version");
    }

    protected boolean verifyVersion(int current, int alternative) {
        return true;
    }
}
