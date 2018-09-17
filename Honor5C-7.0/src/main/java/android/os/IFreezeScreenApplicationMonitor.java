package android.os;

import android.util.ArrayMap;

public interface IFreezeScreenApplicationMonitor {
    void checkFreezeScreen(ArrayMap<String, Object> arrayMap);
}
