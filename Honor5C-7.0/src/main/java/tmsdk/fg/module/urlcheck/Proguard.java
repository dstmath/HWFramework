package tmsdk.fg.module.urlcheck;

import tmsdk.fg.creator.ManagerCreatorF;

/* compiled from: Unknown */
public final class Proguard {
    public void callAllMethods() {
        UrlCheckManager urlCheckManager = (UrlCheckManager) ManagerCreatorF.getManager(UrlCheckManager.class);
        urlCheckManager.checkUrl(null);
        urlCheckManager.checkUrlEx(null);
        ((UrlCheckManagerV3) ManagerCreatorF.getManager(UrlCheckManagerV3.class)).checkUrl(null, -1, null);
    }
}
