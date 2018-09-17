package com.huawei.bd;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.bd.IBDService.Stub;
import defpackage.cb;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.json.JSONObject;

public final class Reporter {
    public static final int ACTIVITY_CREATE = 1;
    public static final int ACTIVITY_DESTROY = 4;
    public static final int ACTIVITY_PAUSE = 3;
    public static final int ACTIVITY_RESUME = 2;
    public static final int MAX_CONTENT_SIZE = 1024;
    private static final int NO_DATA = -1;
    public static final int PRI_HIGH = 25;
    public static final int PRI_LOW = 5;
    public static final int PRI_NORMAL = 15;
    private static final String TAG = "BD.Reporter";
    private static cb deathRecipient;
    private static int notAvailCount;
    private static int sBetaState;
    private static Method sGetServiceMethod;
    private static IBDService sService;
    private static Class sServiceManagerClazz;

    static {
        sService = null;
        deathRecipient = new cb();
        notAvailCount = 0;
        sBetaState = NO_DATA;
        sServiceManagerClazz = null;
        sGetServiceMethod = null;
        try {
            sServiceManagerClazz = Class.forName("android.os.ServiceManager");
            Class[] clsArr = new Class[ACTIVITY_CREATE];
            clsArr[0] = String.class;
            sGetServiceMethod = sServiceManagerClazz.getMethod("getService", clsArr);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ServiceManager ClassNotFoundException");
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "ServiceManager NoSuchMethodException");
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "ServiceManager IllegalArgumentException");
        } catch (Exception e4) {
            Log.e(TAG, "ServiceManager Exception");
        }
    }

    private Reporter() {
    }

    public static boolean beta(Context context, int i, String str) {
        if (sBetaState == NO_DATA) {
            sBetaState = SystemProperties.getInt("ro.logsystem.usertype", 0);
        }
        return sBetaState == ACTIVITY_PAUSE ? e(context, i, str, PRI_LOW) : false;
    }

    public static boolean c(Context context, int i) {
        return c(context, i, ACTIVITY_CREATE);
    }

    public static boolean c(Context context, int i, int i2) {
        if (context == null || i > 65535 || i2 < ACTIVITY_CREATE) {
            Log.e(TAG, "null == context || eventID > 65535 || count < 1");
            return false;
        }
        IBDService service = getService();
        if (service == null) {
            return false;
        }
        try {
            service.sendAccumulativeData(context.getPackageName(), restrictID(i), i2);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "sendAccumulativeData RemoteException");
            return false;
        }
    }

    public static boolean e(Context context, int i, String str) {
        return handleEvent(context, null, null, i, str, PRI_NORMAL, NO_DATA);
    }

    public static boolean e(Context context, int i, String str, int i2) {
        return handleEvent(context, null, null, i, str, i2, NO_DATA);
    }

    private static IBDService getService() {
        if (ifServiceNotAvailable()) {
            return null;
        }
        if (sService != null) {
            return sService;
        }
        if (sServiceManagerClazz == null || sGetServiceMethod == null) {
            Log.e(TAG, "Can't support using ServiceManager");
            return null;
        }
        try {
            Method method = sGetServiceMethod;
            Object[] objArr = new Object[ACTIVITY_CREATE];
            objArr[0] = "com.huawei.bd.BDService";
            IBinder iBinder = (IBinder) method.invoke(null, objArr);
            if (iBinder == null) {
                notAvailCount += ACTIVITY_CREATE;
                Log.e(TAG, "Can't getService HwBDService");
                return null;
            } else if (iBinder.pingBinder()) {
                iBinder.linkToDeath(deathRecipient, 0);
                sService = Stub.asInterface(iBinder);
                Log.i(TAG, sService.toString());
                return sService;
            } else {
                Log.e(TAG, "HwBDService is not running");
                return null;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "getService ClassNotFoundException");
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "getService IllegalArgumentException");
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getService IllegalAccessException");
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "getService InvocationTargetException");
        } catch (RuntimeException e5) {
            Log.e(TAG, "getService RuntimeException");
        } catch (RemoteException e6) {
            e6.printStackTrace();
        }
    }

    private static boolean handleEvent(Context context, String str, String str2, int i, String str3, int i2, int i3) {
        if (context == null || i > 65535) {
            Log.e(TAG, "null == context || eventID > 65535");
            return false;
        }
        IBDService service = getService();
        if (service == null) {
            return false;
        }
        if (str == null) {
            str = context.getPackageName();
        }
        try {
            if (str3.length() > MAX_CONTENT_SIZE) {
                str3 = str3.substring(0, MAX_CONTENT_SIZE);
            }
            service.sendAppActionData(str, restrictID(i), str3, i2);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "sendAppActionData RemoteException");
            return false;
        }
    }

    private static boolean ifServiceNotAvailable() {
        return notAvailCount > PRI_LOW;
    }

    public static boolean j(Context context, int i, JSONObject jSONObject) {
        return handleEvent(context, null, null, i, jSONObject.toString(), PRI_NORMAL, NO_DATA);
    }

    public static boolean j(Context context, int i, JSONObject jSONObject, int i2) {
        return handleEvent(context, null, null, i, jSONObject.toString(), i2, NO_DATA);
    }

    private static int restrictID(int i) {
        return (65535 & i) | 65536;
    }
}
