package tmsdk.common;

import tmsdkobf.rc;

/* compiled from: Unknown */
public interface ITMSPlugin {
    boolean handleInstallInstr(rc rcVar);

    boolean hasSetDefaultApp();

    void onReceiveMsg();
}
