package com.android.server.updates;

public class LangIdInstallReceiver extends ConfigUpdateInstallReceiver {
    public LangIdInstallReceiver() {
        super("/data/misc/textclassifier/", "lang_id.model", "metadata/lang_id", "version");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.updates.ConfigUpdateInstallReceiver
    public boolean verifyVersion(int current, int alternative) {
        return true;
    }
}
