package com.huawei.android.os;

import android.os.WorkSource;

public class WorkSourceEx extends WorkSource {
    public WorkSourceEx(int uid, String name) {
        super(uid, name);
    }

    public static int get(WorkSource workSource, int index) {
        return workSource.get(index);
    }

    public static String getName(WorkSource workSource, int index) {
        return workSource.getName(index);
    }

    public static int size(WorkSource workSource) {
        return workSource.size();
    }
}
