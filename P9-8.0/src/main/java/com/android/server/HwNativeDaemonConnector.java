package com.android.server;

import android.content.Context;

public interface HwNativeDaemonConnector {
    void reportChrForAddRouteFail(String str, String str2);

    void setContext(Context context);
}
