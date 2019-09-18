package com.android.server.wm;

import android.util.ArrayMap;
import java.io.PrintWriter;

class UnknownAppVisibilityController {
    private static final String TAG = "WindowManager";
    private static final int UNKNOWN_STATE_WAITING_RELAYOUT = 2;
    private static final int UNKNOWN_STATE_WAITING_RESUME = 1;
    private static final int UNKNOWN_STATE_WAITING_VISIBILITY_UPDATE = 3;
    private final WindowManagerService mService;
    private final ArrayMap<AppWindowToken, Integer> mUnknownApps = new ArrayMap<>();

    UnknownAppVisibilityController(WindowManagerService service) {
        this.mService = service;
    }

    /* access modifiers changed from: package-private */
    public boolean allResolved() {
        return this.mUnknownApps.isEmpty();
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        this.mUnknownApps.clear();
    }

    /* access modifiers changed from: package-private */
    public String getDebugMessage() {
        StringBuilder builder = new StringBuilder();
        for (int i = this.mUnknownApps.size() - 1; i >= 0; i--) {
            builder.append("app=");
            builder.append(this.mUnknownApps.keyAt(i));
            builder.append(" state=");
            builder.append(this.mUnknownApps.valueAt(i));
            if (i != 0) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    /* access modifiers changed from: package-private */
    public void appRemovedOrHidden(AppWindowToken appWindow) {
        this.mUnknownApps.remove(appWindow);
    }

    /* access modifiers changed from: package-private */
    public void notifyLaunched(AppWindowToken appWindow) {
        this.mUnknownApps.put(appWindow, 1);
    }

    /* access modifiers changed from: package-private */
    public void notifyAppResumedFinished(AppWindowToken appWindow) {
        if (this.mUnknownApps.containsKey(appWindow) && this.mUnknownApps.get(appWindow).intValue() == 1) {
            this.mUnknownApps.put(appWindow, 2);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyRelayouted(AppWindowToken appWindow) {
        if (this.mUnknownApps.containsKey(appWindow) && this.mUnknownApps.get(appWindow).intValue() == 2) {
            this.mUnknownApps.put(appWindow, 3);
            this.mService.notifyKeyguardFlagsChanged(new Runnable() {
                public final void run() {
                    UnknownAppVisibilityController.this.notifyVisibilitiesUpdated();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void notifyVisibilitiesUpdated() {
        boolean changed = false;
        for (int i = this.mUnknownApps.size() - 1; i >= 0; i--) {
            if (this.mUnknownApps.valueAt(i).intValue() == 3) {
                this.mUnknownApps.removeAt(i);
                changed = true;
            }
        }
        if (changed) {
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        if (!this.mUnknownApps.isEmpty()) {
            pw.println(prefix + "Unknown visibilities:");
            for (int i = this.mUnknownApps.size() + -1; i >= 0; i += -1) {
                pw.println(prefix + "  app=" + this.mUnknownApps.keyAt(i) + " state=" + this.mUnknownApps.valueAt(i));
            }
        }
    }
}
