package com.android.server.display;

import com.android.server.display.DisplayManagerService;
import com.android.server.display.DisplayManagerServiceEx;

public class DisplayManagerServiceExUtils {
    public static DisplayManagerServiceEx.SyncRootEx createSyncRootEx(DisplayManagerService.SyncRoot syncRoot) {
        return new DisplayManagerServiceEx.SyncRootEx(syncRoot);
    }
}
