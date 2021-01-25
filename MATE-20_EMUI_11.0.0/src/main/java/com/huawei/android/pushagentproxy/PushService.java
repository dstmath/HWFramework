package com.huawei.android.pushagentproxy;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import dalvik.system.PathClassLoader;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;

public class PushService extends Service {
    private static final String TAG = "PushService";
    private Service push = null;

    @Override // android.app.Service
    public IBinder onBind(Intent arg0) {
        try {
            if (this.push != null) {
                return this.push.onBind(arg0);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "pushService onBind error");
            return null;
        }
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public boolean bindService(Intent intent, ServiceConnection conn, int flags) {
        try {
            if (this.push != null) {
                return this.push.bindService(intent, conn, flags);
            }
        } catch (Exception e) {
            Log.e(TAG, "pushService bindService error");
        }
        return super.bindService(intent, conn, flags);
    }

    @Override // android.app.Service
    public void onCreate() {
        try {
            loadPush();
        } catch (Exception e) {
            Log.e(TAG, "pushService onCreate error");
        }
        super.onCreate();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (this.push != null) {
                return this.push.onStartCommand(intent, flags, startId);
            }
        } catch (Exception e) {
            Log.e(TAG, "pushService onStartCommand error");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public void onDestroy() {
        try {
            if (this.push != null) {
                this.push.onDestroy();
            }
        } catch (Exception e) {
            Log.e(TAG, "pushService onDestroy error");
        }
        super.onDestroy();
    }

    public boolean loadPush() {
        try {
            File jarFile = HwCfgFilePolicy.getCfgFile("jars/hwpush.jar", 0);
            if (jarFile == null || !jarFile.exists()) {
                Log.w(TAG, "HwCfgFilePolicy getCfgFile hwpush jar fail.");
                jarFile = new File("/system/framework/hwpush.jar");
            }
            if (!jarFile.exists()) {
                Log.e(TAG, "hwpush jar is not exist!");
                stopSelf();
                return false;
            }
            Log.i(TAG, "get push file path is " + jarFile.getCanonicalPath());
            this.push = (Service) new PathClassLoader(jarFile.getCanonicalPath(), ClassLoader.getSystemClassLoader()).loadClass("com.huawei.android.pushagent.PushService").newInstance();
            setParam();
            this.push.onCreate();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "pushService loadPush error");
            stopSelf();
            return true;
        }
    }

    public boolean setParam() {
        try {
            if (this.push == null) {
                return false;
            }
            this.push.getClass().getMethod("setParam", Service.class, Bundle.class).invoke(this.push, this, new Bundle());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "pushService setParam error");
        }
    }
}
