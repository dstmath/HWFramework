package com.android.server.wm;

import android.os.IBinder;
import java.util.HashMap;

class WindowHashMap extends HashMap<IBinder, WindowState> {
    WindowHashMap() {
    }
}
