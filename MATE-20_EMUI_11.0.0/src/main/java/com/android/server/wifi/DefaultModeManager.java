package com.android.server.wifi;

import android.content.Context;
import android.os.Looper;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DefaultModeManager implements ActiveModeManager {
    private static final String TAG = "WifiDefaultModeManager";
    private final Context mContext;

    @Override // com.android.server.wifi.ActiveModeManager
    public void start() {
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void stop() {
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public int getScanMode() {
        return 0;
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    DefaultModeManager(Context context, Looper looper) {
        this.mContext = context;
    }
}
