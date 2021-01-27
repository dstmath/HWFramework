package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayManagerService;
import java.io.PrintWriter;

public class DisplayAdapterBridge extends DisplayAdapter {
    private static final String TAG = "DisplayAdapterBridge";
    private DisplayAdapterEx mDisplayAdapterEx;

    public /* bridge */ /* synthetic */ void dumpLocked(PrintWriter x0) {
        DisplayAdapterBridge.super.dumpLocked(x0);
    }

    public DisplayAdapterBridge(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener) {
        super(syncRoot, context, handler, listener, TAG);
    }

    public DisplayAdapterBridge(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener, String name) {
        super(syncRoot, context, handler, listener, name);
    }

    public void setDisplayAdapterEx(DisplayAdapterEx displayAdapterEx) {
        this.mDisplayAdapterEx = displayAdapterEx;
    }

    public void registerLocked() {
        DisplayAdapterBridge.super.registerLocked();
    }

    public boolean createVrDisplay(String displayName, int[] displayParams) {
        DisplayAdapterEx displayAdapterEx = this.mDisplayAdapterEx;
        if (displayAdapterEx != null) {
            return displayAdapterEx.createVrDisplay(displayName, displayParams);
        }
        return false;
    }

    public boolean destroyVrDisplay(String displayName) {
        DisplayAdapterEx displayAdapterEx = this.mDisplayAdapterEx;
        if (displayAdapterEx != null) {
            return displayAdapterEx.destroyVrDisplay(displayName);
        }
        return false;
    }

    public boolean destroyAllVrDisplay() {
        DisplayAdapterEx displayAdapterEx = this.mDisplayAdapterEx;
        if (displayAdapterEx != null) {
            return displayAdapterEx.destroyAllVrDisplay();
        }
        return false;
    }
}
