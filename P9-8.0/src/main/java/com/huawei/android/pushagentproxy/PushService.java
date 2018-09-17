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

    public IBinder onBind(Intent arg0) {
        try {
            if (this.push != null) {
                return this.push.onBind(arg0);
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }

    public boolean bindService(Intent intent, ServiceConnection conn, int flags) {
        try {
            if (this.push != null) {
                return this.push.bindService(intent, conn, flags);
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
        }
        return super.bindService(intent, conn, flags);
    }

    public void onCreate() {
        try {
            loadPush();
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
        }
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (this.push != null) {
                return this.push.onStartCommand(intent, flags, startId);
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        try {
            if (this.push != null) {
                this.push.onDestroy();
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
        }
        super.onDestroy();
    }

    public boolean loadPush() {
        try {
            File jarFile = HwCfgFilePolicy.getCfgFile("jars/hwpush.jar", 0);
            if (jarFile == null || (jarFile.exists() ^ 1) != 0) {
                Log.w(TAG, "HwCfgFilePolicy getCfgFile hwpush.jar fail");
                jarFile = new File("/system/framework/hwpush.jar");
            }
            if (jarFile.exists()) {
                Log.i(TAG, "get push File path is " + jarFile.getAbsolutePath());
                this.push = (Service) new PathClassLoader(jarFile.getAbsolutePath(), ClassLoader.getSystemClassLoader()).loadClass("com.huawei.android.pushagent.PushService").newInstance();
                setParam();
                this.push.onCreate();
                return true;
            }
            Log.e(TAG, "hwpush.jar is not exist!");
            stopSelf();
            return false;
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
            stopSelf();
        }
    }

    public boolean setParam() {
        try {
            if (this.push == null) {
                return false;
            }
            this.push.getClass().getMethod("setParam", new Class[]{Service.class, Bundle.class}).invoke(this.push, new Object[]{this, new Bundle()});
            return true;
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
        }
    }
}
