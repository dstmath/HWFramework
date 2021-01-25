package com.huawei.android.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Singleton;
import huawei.hiview.HiEvent;
import java.util.Date;

public class HiEventEx {
    private static final String APP_NAME = "PNAMEID";
    private static final String APP_VERSION = "PVERSIONID";
    private static final Singleton<Boolean> SINGLETON_HIEVENT = new Singleton<Boolean>() {
        /* class com.huawei.android.app.HiEventEx.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public Boolean create() {
            boolean hiEventDefined = true;
            try {
                Class.forName("huawei.hiview.HiEvent");
            } catch (ClassNotFoundException e) {
                hiEventDefined = false;
                Log.e(HiEventEx.TAG, "ClassNotFoundException: huawei.hiview.HiEvent not exsist.");
            }
            return new Boolean(hiEventDefined);
        }
    };
    private static final String TAG = "HiEventEx";
    private static Context lastContext;
    private static String name;
    private static String version;
    private HiEvent event;

    private static boolean isHiEventSdkExist() {
        return ((Boolean) SINGLETON_HIEVENT.get()).booleanValue();
    }

    public HiEventEx(int id) {
        if (isHiEventSdkExist()) {
            this.event = new HiEvent(id);
        }
    }

    /* access modifiers changed from: package-private */
    public HiEvent getHiEvent() {
        return this.event;
    }

    /* access modifiers changed from: package-private */
    public void setHiEvent(HiEvent value) {
        this.event = value;
    }

    public HiEventEx setTime(Date date) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.setTime(date);
        return this;
    }

    public HiEventEx putBool(String key, boolean value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putBool(key, value);
        return this;
    }

    public HiEventEx putBoolArray(String key, boolean[] value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putBoolArray(key, value);
        return this;
    }

    public HiEventEx putByte(String key, byte value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putByte(key, value);
        return this;
    }

    public HiEventEx putByteArray(String key, byte[] value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putByteArray(key, value);
        return this;
    }

    public HiEventEx putShort(String key, short value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putShort(key, value);
        return this;
    }

    public HiEventEx putShortArray(String key, short[] value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putShortArray(key, value);
        return this;
    }

    public HiEventEx putInt(String key, int value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putInt(key, value);
        return this;
    }

    public HiEventEx putIntArray(String key, int[] value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putIntArray(key, value);
        return this;
    }

    public HiEventEx putLong(String key, long value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putLong(key, value);
        return this;
    }

    public HiEventEx putLongArray(String key, long[] value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putLongArray(key, value);
        return this;
    }

    public HiEventEx putFloat(String key, float value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putFloat(key, value);
        return this;
    }

    public HiEventEx putFloatArray(String key, float[] value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putFloatArray(key, value);
        return this;
    }

    public HiEventEx putString(String key, String value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putString(key, value);
        return this;
    }

    public HiEventEx putStringArray(String key, String[] value) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.putStringArray(key, value);
        return this;
    }

    public HiEventEx putHiEvent(String key, HiEventEx value) {
        if (isHiEventSdkExist() && value != null) {
            this.event.putHiEvent(key, value.getHiEvent());
        }
        return this;
    }

    public HiEventEx putHiEventArray(String key, HiEventEx[] value) {
        if (isHiEventSdkExist() && value != null && value.length > 0) {
            HiEvent[] eventValue = new HiEvent[value.length];
            int i = 0;
            while (i < value.length && value[i] != null) {
                eventValue[i] = value[i].getHiEvent();
                i++;
            }
            this.event.putHiEventArray(key, eventValue);
        }
        return this;
    }

    public HiEventEx addFilePath(String path) {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.addFilePath(path);
        return this;
    }

    public HiEventEx reset() {
        if (!isHiEventSdkExist()) {
            return this;
        }
        this.event.reset();
        return this;
    }

    public HiEventEx putAppInfo(Context context) {
        if (!isHiEventSdkExist() || context == null) {
            return this;
        }
        Context context2 = lastContext;
        if (context2 == null || !context2.equals(context)) {
            lastContext = context;
            try {
                name = context.getPackageName();
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(name, 0);
                if (packageInfo != null) {
                    version = packageInfo.versionName;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "HiEventEx putAppInfo NameNotFoundException");
            }
        }
        this.event.putString(APP_NAME, name);
        this.event.putString(APP_VERSION, version);
        return this;
    }

    public int getId() {
        if (!isHiEventSdkExist()) {
            return -1;
        }
        return this.event.getId();
    }
}
