package com.android.server.os;

import android.util.ArrayMap;

public interface IFreezeScreenWindowMonitor {
    void cancelCheckFreezeScreen(ArrayMap<String, Object> arrayMap);

    void checkFreezeScreen(ArrayMap<String, Object> arrayMap);
}
