package ohos.media.sessioncore.delegate;

import ohos.media.common.sessioncore.AVBrowserResult;
import ohos.media.common.sessioncore.AVBrowserRoot;
import ohos.utils.PacMap;

public interface IAVBrowserServiceDelegate {
    AVBrowserRoot onGetRoot(String str, int i, PacMap pacMap);

    void onLoadAVElement(String str, AVBrowserResult aVBrowserResult);

    void onLoadAVElementList(String str, AVBrowserResult aVBrowserResult);

    void onLoadAVElementList(String str, AVBrowserResult aVBrowserResult, PacMap pacMap);
}
