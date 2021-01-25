package com.android.server.display;

public class DisplayAdapterBridgeUtils {
    public static DisplayAdapterBridge createDisplayAdapterBridge(DisplayAdapterEx displayAdapterEx) {
        if (displayAdapterEx == null) {
            return null;
        }
        return displayAdapterEx.getDisplayAdapterBridge();
    }
}
