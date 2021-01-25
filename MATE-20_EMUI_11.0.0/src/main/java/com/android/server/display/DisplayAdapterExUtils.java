package com.android.server.display;

import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayAdapterEx;

public class DisplayAdapterExUtils {
    public static DisplayAdapterEx.ListenerEx createListenerEx(DisplayAdapter.Listener listener) {
        return new DisplayAdapterEx.ListenerEx(listener);
    }
}
