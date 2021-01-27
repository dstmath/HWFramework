package com.android.server.wm;

import java.io.PrintWriter;

public interface IHwTaskSnapshotCacheEx {
    void addLruTaskIdList(int i);

    void dump(PrintWriter printWriter, String str);

    int getLeastRecentTaskId();

    boolean isOverMaxCacheThreshold();

    void removeLruTaskIdList(int i);
}
