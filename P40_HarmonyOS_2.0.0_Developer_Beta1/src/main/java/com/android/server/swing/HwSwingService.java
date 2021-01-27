package com.android.server.swing;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;

public class HwSwingService extends Service {
    private static final String TAG = "HwSwingService";

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* JADX WARN: Type inference failed for: r3v2, types: [com.android.server.swing.HwSwingPolicyService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting HwSwingPolicyService");
        try {
            ServiceManager.addService("hwswing", (IBinder) new HwSwingPolicyService(this));
            return 1;
        } catch (SecurityException e) {
            Log.e(TAG, "start HwSwingPolicyService error : " + e.getMessage());
            return 1;
        } catch (IllegalStateException e2) {
            Log.e(TAG, "start HwSwingPolicyService error : " + e2.getMessage());
            return 1;
        }
    }
}
