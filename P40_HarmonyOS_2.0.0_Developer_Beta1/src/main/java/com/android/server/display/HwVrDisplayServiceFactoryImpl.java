package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import com.android.server.display.DisplayAdapterEx;
import com.android.server.display.DisplayManagerServiceEx;

public class HwVrDisplayServiceFactoryImpl extends DefaultHwVrDisplayServiceFactory {
    public DefaultHwVrDisplayAdapter getHwVrDisplayAdapter(DisplayManagerServiceEx.SyncRootEx syncRoot, Context context, Handler handler, DisplayAdapterEx.ListenerEx listener, Handler uiHandler) {
        return new HwVrDisplayAdapter(syncRoot, context, handler, listener, uiHandler);
    }
}
