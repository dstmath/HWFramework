package com.android.server.wifi.wifipro;

import android.os.Build;
import android.telephony.PhoneStateListener;
import android.util.Log;
import java.lang.reflect.Field;

public class DualPhoneStateListener extends PhoneStateListener {
    private static final Field FIELD_subID;
    private static int LOLLIPOP_VER = 21;
    private static String TAG = "MQoS";

    static {
        Field field;
        if (Build.VERSION.SDK_INT >= LOLLIPOP_VER) {
            field = getDeclaredField(PhoneStateListener.class, "mSubId");
        } else {
            field = getDeclaredField(PhoneStateListener.class, "mSubscription");
        }
        FIELD_subID = field;
    }

    public DualPhoneStateListener() {
    }

    public DualPhoneStateListener(int subscription) {
        if (FIELD_subID != null) {
            if (!FIELD_subID.isAccessible()) {
                FIELD_subID.setAccessible(true);
            }
            if (Build.VERSION.SDK_INT == LOLLIPOP_VER) {
                setFieldValue(this, FIELD_subID, Long.valueOf((long) subscription));
            } else {
                setFieldValue(this, FIELD_subID, Integer.valueOf(subscription));
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static int getSubscription(PhoneStateListener obj) {
        if (FIELD_subID != null) {
            if (!FIELD_subID.isAccessible()) {
                FIELD_subID.setAccessible(true);
            }
            if (Build.VERSION.SDK_INT == LOLLIPOP_VER) {
                return ((Long) getFieldValue(obj, FIELD_subID)).intValue();
            }
            return ((Integer) getFieldValue(obj, FIELD_subID)).intValue();
        }
        throw new UnsupportedOperationException();
    }

    private static boolean isEmpty(String str) {
        boolean z = true;
        if (str == null || str.length() == 0) {
            return true;
        }
        if (str.trim().length() != 0) {
            z = false;
        }
        return z;
    }

    private static Field getDeclaredField(Class<?> targetClass, String name) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getDeclaredField(name);
        } catch (SecurityException e) {
            String str = TAG;
            Log.e(str, name + ":" + e.getCause());
            return null;
        } catch (NoSuchFieldException e2) {
            String str2 = TAG;
            Log.e(str2, name + ",no such field.");
            return null;
        }
    }

    private static void setFieldValue(Object receiver, Field field, Object value) {
        if (field != null) {
            try {
                field.set(receiver, value);
            } catch (Exception e) {
                String str = TAG;
                Log.e(str, "Exception in setFieldValue: " + e.getClass().getSimpleName());
            }
        }
    }

    private static Object getFieldValue(Object receiver, Field field) {
        if (field == null) {
            return null;
        }
        try {
            return field.get(receiver);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "Exception in getFieldValue: " + e.getClass().getSimpleName());
            throw new UnsupportedOperationException();
        }
    }
}
