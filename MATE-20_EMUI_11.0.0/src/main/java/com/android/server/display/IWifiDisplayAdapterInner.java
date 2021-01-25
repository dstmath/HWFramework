package com.android.server.display;

import android.hardware.display.WifiDisplay;
import android.os.Handler;

public interface IWifiDisplayAdapterInner {
    Handler getHandlerInner();

    WifiDisplay getmActiveDisplayInner();

    int getmActiveDisplayStateInner();

    WifiDisplayController getmDisplayControllerInner();

    PersistentDataStore getmPersistentDataStoreInner();
}
