package com.android.server.wm;

public interface IHwWindowManagerInner {
    TaskSnapshotController getTaskSnapshotController();

    WindowHashMap getWindowMap();
}
