package com.android.server.zrhung;

import java.util.ArrayList;

public interface IHwBinderMonitor {
    void addBinderPid(ArrayList<Integer> arrayList, int i);

    void writeTransactonToTrace(String str);
}
