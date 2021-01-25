package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import com.android.server.display.DisplayAdapterEx;
import com.android.server.display.DisplayManagerServiceEx;

public class DefaultHwVrDisplayAdapter extends DisplayAdapterEx {
    private static final String TAG = "DefaultHwVrDisplayAdapter";

    public DefaultHwVrDisplayAdapter(DisplayManagerServiceEx.SyncRootEx syncRoot, Context context, Handler handler, DisplayAdapterEx.ListenerEx listener, Handler uiHandler) {
        super(syncRoot, context, handler, listener, TAG);
    }

    @Override // com.android.server.display.DisplayAdapterEx
    public void registerLocked() {
    }

    @Override // com.android.server.display.DisplayAdapterEx
    public boolean createVrDisplay(String displayName, int[] displayParams) {
        return false;
    }

    @Override // com.android.server.display.DisplayAdapterEx
    public boolean destroyVrDisplay(String displayName) {
        return false;
    }

    @Override // com.android.server.display.DisplayAdapterEx
    public boolean destroyAllVrDisplay() {
        return false;
    }
}
