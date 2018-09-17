package tmsdk.fg.module.urlcheck;

import tmsdk.common.module.urlcheck.UrlCheckResult;

public interface ICheckUrlCallback {
    void onCheckUrlCallback(UrlCheckResult urlCheckResult);
}
