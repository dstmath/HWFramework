package com.android.server.display;

import android.os.Handler;

public interface IWifiDisplayAdapterInner {
    Handler getHandlerInner();

    WifiDisplayController getmDisplayControllerInner();

    PersistentDataStore getmPersistentDataStoreInner();
}
