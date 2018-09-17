package com.android.server.updates;

public class LangIdInstallReceiver extends ConfigUpdateInstallReceiver {
    public LangIdInstallReceiver() {
        super("/data/misc/textclassifier/", "textclassifier.langid.model", "metadata/langid", "version");
    }
}
