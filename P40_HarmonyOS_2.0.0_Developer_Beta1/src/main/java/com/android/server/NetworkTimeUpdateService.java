package com.android.server;

import android.os.IBinder;

public interface NetworkTimeUpdateService extends IBinder {
    void systemRunning();
}
