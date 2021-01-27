package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import com.android.server.display.DisplayAdapterEx;
import com.android.server.display.DisplayManagerServiceEx;

public class DefaultHwVrDisplayServiceFactory {

    private static class Instance {
        private static DefaultHwVrDisplayServiceFactory sInstance = new DefaultHwVrDisplayServiceFactory();

        private Instance() {
        }
    }

    public static DefaultHwVrDisplayServiceFactory getInstance() {
        return Instance.sInstance;
    }

    public DefaultHwVrDisplayAdapter getHwVrDisplayAdapter(DisplayManagerServiceEx.SyncRootEx syncRoot, Context context, Handler handler, DisplayAdapterEx.ListenerEx listener, Handler uiHandler) {
        return new DefaultHwVrDisplayAdapter(syncRoot, context, handler, listener, uiHandler);
    }
}
