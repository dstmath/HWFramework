package android.os;

import android.util.ArrayMap;

public interface IFreezeScreenApplicationMonitor {
    void cancelCheckFreezeScreen(ArrayMap<String, Object> arrayMap);

    void checkFreezeScreen(ArrayMap<String, Object> arrayMap);
}
