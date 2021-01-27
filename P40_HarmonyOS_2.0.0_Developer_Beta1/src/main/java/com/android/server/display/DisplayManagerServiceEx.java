package com.android.server.display;

import com.android.server.display.DisplayManagerService;

public class DisplayManagerServiceEx {

    public static class SyncRootEx {
        private DisplayManagerService.SyncRoot mSyncRoot;

        public SyncRootEx() {
        }

        public SyncRootEx(DisplayManagerService.SyncRoot syncRoot) {
            this.mSyncRoot = syncRoot;
        }

        public DisplayManagerService.SyncRoot getSyncRoot() {
            return this.mSyncRoot;
        }
    }
}
