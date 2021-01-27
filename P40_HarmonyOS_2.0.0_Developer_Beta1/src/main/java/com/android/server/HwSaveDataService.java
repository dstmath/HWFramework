package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Slog;
import huawei.android.savedata.IHwSaveData;
import java.util.Map;

public class HwSaveDataService extends IHwSaveData.Stub {
    private static final Object LOCK = new Object();
    public static final String SERVICE_NAME = "hwSaveDataService";
    private static final String TAG = "HwSaveDataService";
    private static HwSaveDataService sHwSaveDataService;
    private final Map<String, IBinder> mBinderMap = new ArrayMap();
    private final Context mContext;

    private HwSaveDataService(Context context) {
        this.mContext = context;
    }

    public static HwSaveDataService getInstance(Context context) {
        HwSaveDataService hwSaveDataService;
        if (context == null) {
            Slog.w(TAG, "getInstance context is null");
            return null;
        }
        synchronized (LOCK) {
            if (sHwSaveDataService == null) {
                sHwSaveDataService = new HwSaveDataService(context);
            }
            hwSaveDataService = sHwSaveDataService;
        }
        return hwSaveDataService;
    }

    public void putBinderObject(String name, IBinder binder) {
        if (!checkPermission()) {
            Slog.w(TAG, "putBinderObject permission denied");
            return;
        }
        synchronized (this.mBinderMap) {
            if (this.mBinderMap.containsKey(name)) {
                Slog.i(TAG, "Overriding service registration name=" + name);
            }
            this.mBinderMap.put(name, binder);
        }
    }

    public void removeBinderObject(String name) {
        if (!checkPermission()) {
            Slog.w(TAG, "removeBinderObject permission denied");
            return;
        }
        synchronized (this.mBinderMap) {
            this.mBinderMap.remove(name);
        }
    }

    public IBinder getBinderObject(String name) {
        IBinder iBinder;
        if (!checkPermission()) {
            Slog.w(TAG, "getBinderObject permission denied");
            return null;
        }
        synchronized (this.mBinderMap) {
            iBinder = this.mBinderMap.get(name);
        }
        return iBinder;
    }

    public Map getBinderObjects(String prefix) {
        Map<String, IBinder> map = new ArrayMap<>();
        if (!checkPermission()) {
            Slog.w(TAG, "getBinderObjects permission denied");
            return map;
        }
        synchronized (this.mBinderMap) {
            for (Map.Entry<String, IBinder> entry : this.mBinderMap.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return map;
    }

    private boolean checkPermission() {
        return Binder.getCallingUid() == 1000 || this.mContext.checkCallingPermission("com.huawei.permission.SAVE_DATA") == 0;
    }
}
