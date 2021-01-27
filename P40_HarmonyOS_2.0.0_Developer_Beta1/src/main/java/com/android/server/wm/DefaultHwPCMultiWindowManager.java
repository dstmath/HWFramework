package com.android.server.wm;

import android.graphics.Rect;
import java.util.ArrayList;
import java.util.List;

public class DefaultHwPCMultiWindowManager {
    public static final int FIX_ORIENTATION_LANDSCAPE = 2;
    public static final int FIX_ORIENTATION_NONE = 0;
    public static final int FIX_ORIENTATION_PORTRAIT = 1;
    public static final String TAG = "HwPCMultiWindowManager";

    public static class EntryEx {
        public int originalWindowState;
        public Rect windowBounds;
        public int windowState;

        public Rect getWindowBounds() {
            return this.windowBounds;
        }
    }

    public EntryEx getEntry(String entryKey) {
        return null;
    }

    public void storeTaskSettings(TaskRecordEx recordEx) {
    }

    public void restoreTaskWindowState(TaskRecordEx recordEx) {
    }

    public boolean isPortraitApp(TaskRecordEx recordEx) {
        return false;
    }

    public boolean isFixedOrientationPortrait(int screenOrientation) {
        return false;
    }

    public boolean isFixedOrientationLandscape(int screenOrientation) {
        return false;
    }

    public Rect getWindowBounds(TaskRecordEx recordEx) {
        return null;
    }

    public Rect getLaunchBounds(TaskRecordEx recordEx) {
        return null;
    }

    public void updateTaskByRequestedOrientation(TaskRecordEx recordEx, int requestedOrientation) {
    }

    public void resizeTaskFromPC(TaskRecordEx recordEx, Rect rect) {
    }

    public Rect getMaximizedBounds() {
        return new Rect(0, 0, 0, 0);
    }

    public Rect getSplitLeftWindowBounds() {
        return new Rect(0, 0, 0, 0);
    }

    public Rect getSplitRightWindowBounds() {
        return new Rect(0, 0, 0, 0);
    }

    public boolean isSupportResize(TaskRecordEx recordEx, boolean isFullscreen, boolean isMaximized) {
        return false;
    }

    public boolean isSpecialVideo(String pkgName) {
        return false;
    }

    public boolean isOlnyFullscreen(String pkgName) {
        return false;
    }

    public void setForceUpdateTask(int taskId) {
    }

    public List<String> getPortraitMaximizedPkgList() {
        return new ArrayList();
    }
}
