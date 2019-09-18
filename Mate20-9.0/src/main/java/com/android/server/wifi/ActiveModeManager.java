package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface ActiveModeManager {
    public static final String TAG = "ActiveModeManager";

    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    void start();

    void stop();

    void sendScanAvailableBroadcast(Context context, boolean available) {
        Log.d(TAG, "sending scan available broadcast: " + available);
        Intent intent = new Intent("wifi_scan_available");
        intent.addFlags(67108864);
        if (available) {
            intent.putExtra("scan_enabled", 3);
        } else {
            intent.putExtra("scan_enabled", 1);
        }
        context.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }
}
