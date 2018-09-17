package com.android.server.wm;

import android.util.ArrayMap;
import java.io.PrintWriter;

class UnknownAppVisibilityController {
    private static final String TAG = "WindowManager";
    private static final int UNKNOWN_STATE_WAITING_RELAYOUT = 2;
    private static final int UNKNOWN_STATE_WAITING_RESUME = 1;
    private static final int UNKNOWN_STATE_WAITING_VISIBILITY_UPDATE = 3;
    private final WindowManagerService mService;
    private final ArrayMap<AppWindowToken, Integer> mUnknownApps = new ArrayMap();

    UnknownAppVisibilityController(WindowManagerService service) {
        this.mService = service;
    }

    boolean allResolved() {
        return this.mUnknownApps.isEmpty();
    }

    void clear() {
        this.mUnknownApps.clear();
    }

    String getDebugMessage() {
        StringBuilder builder = new StringBuilder();
        for (int i = this.mUnknownApps.size() - 1; i >= 0; i--) {
            builder.append("app=").append(this.mUnknownApps.keyAt(i)).append(" state=").append(this.mUnknownApps.valueAt(i));
            if (i != 0) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    void appRemovedOrHidden(AppWindowToken appWindow) {
        this.mUnknownApps.remove(appWindow);
    }

    void notifyLaunched(AppWindowToken appWindow) {
        this.mUnknownApps.put(appWindow, Integer.valueOf(1));
    }

    void notifyAppResumedFinished(AppWindowToken appWindow) {
        if (this.mUnknownApps.containsKey(appWindow) && ((Integer) this.mUnknownApps.get(appWindow)).intValue() == 1) {
            this.mUnknownApps.put(appWindow, Integer.valueOf(2));
        }
    }

    void notifyRelayouted(AppWindowToken appWindow) {
        if (this.mUnknownApps.containsKey(appWindow) && ((Integer) this.mUnknownApps.get(appWindow)).intValue() == 2) {
            this.mUnknownApps.put(appWindow, Integer.valueOf(3));
            this.mService.notifyKeyguardFlagsChanged(new -$Lambda$dubrDXVds2-Tfao2R_0sol3mGo8(this));
        }
    }

    private void notifyVisibilitiesUpdated() {
        boolean changed = false;
        for (int i = this.mUnknownApps.size() - 1; i >= 0; i--) {
            if (((Integer) this.mUnknownApps.valueAt(i)).intValue() == 3) {
                this.mUnknownApps.removeAt(i);
                changed = true;
            }
        }
        if (changed) {
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    void dump(PrintWriter pw, String prefix) {
        if (!this.mUnknownApps.isEmpty()) {
            pw.println(prefix + "Unknown visibilities:");
            for (int i = this.mUnknownApps.size() - 1; i >= 0; i--) {
                pw.println(prefix + "  app=" + this.mUnknownApps.keyAt(i) + " state=" + this.mUnknownApps.valueAt(i));
            }
        }
    }
}
