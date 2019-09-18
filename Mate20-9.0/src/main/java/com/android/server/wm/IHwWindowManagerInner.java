package com.android.server.wm;

import android.app.AppOpsManager;
import android.content.res.Configuration;
import android.hardware.display.DisplayManagerInternal;
import com.android.server.input.InputManagerService;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowManagerService;

public interface IHwWindowManagerInner {
    Configuration computeNewConfiguration(int i);

    AppOpsManager getAppOps();

    DisplayContent getDefaultDisplayContentLocked();

    DisplayManagerInternal getDisplayManagerInternal();

    AppWindowToken getFocusedAppWindowToken();

    InputManagerService getInputManager();

    WindowState getInputMethodWindow();

    InputMonitor getInputMonitor();

    WindowManagerPolicy getPolicy();

    RootWindowContainer getRoot();

    WindowManagerService getService();

    TaskSnapshotController getTaskSnapshotController();

    HwWMDAMonitorProxy getWMMonitor();

    WindowAnimator getWindowAnimator();

    WindowManagerService.H getWindowMangerServiceHandler();

    WindowHashMap getWindowMap();

    WindowSurfacePlacer getWindowSurfacePlacer();

    void updateAppOpsState();
}
