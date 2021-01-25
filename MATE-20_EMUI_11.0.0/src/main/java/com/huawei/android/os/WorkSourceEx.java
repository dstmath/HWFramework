package com.huawei.android.os;

import android.os.WorkSource;

public class WorkSourceEx extends WorkSource {
    private static final int INVALID_SIZE = -1;
    private static final int INVALID_UID = -1;

    public WorkSourceEx(int uid, String name) {
        super(uid, name);
    }

    public static int get(WorkSource workSource, int index) {
        if (workSource != null) {
            return workSource.get(index);
        }
        return -1;
    }

    public static String getName(WorkSource workSource, int index) {
        if (workSource != null) {
            return workSource.getName(index);
        }
        return null;
    }

    public static int size(WorkSource workSource) {
        if (workSource != null) {
            return workSource.size();
        }
        return -1;
    }
}
