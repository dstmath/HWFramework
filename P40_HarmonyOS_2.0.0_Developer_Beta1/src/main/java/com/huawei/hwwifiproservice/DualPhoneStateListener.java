package com.huawei.hwwifiproservice;

import android.os.Build;
import android.telephony.PhoneStateListener;
import android.util.Log;
import java.lang.reflect.Field;

public class DualPhoneStateListener extends PhoneStateListener {
    private static final Field FIELD_SUB_ID;
    private static final int LOLLIPOP_VER = 21;
    private static final String TAG = "MQoS";

    static {
        Field field;
        if (Build.VERSION.SDK_INT >= 21) {
            field = getDeclaredField(PhoneStateListener.class, "mSubId");
        } else {
            field = getDeclaredField(PhoneStateListener.class, "mSubscription");
        }
        FIELD_SUB_ID = field;
    }

    public DualPhoneStateListener() {
    }

    public DualPhoneStateListener(int subscription) {
        Field field = FIELD_SUB_ID;
        if (field != null) {
            if (!field.isAccessible()) {
                FIELD_SUB_ID.setAccessible(true);
            }
            if (Build.VERSION.SDK_INT == 21) {
                setFieldValue(this, FIELD_SUB_ID, Long.valueOf((long) subscription));
            } else {
                setFieldValue(this, FIELD_SUB_ID, Integer.valueOf(subscription));
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static int getSubscription(PhoneStateListener obj) {
        Field field = FIELD_SUB_ID;
        if (field != null) {
            if (!field.isAccessible()) {
                FIELD_SUB_ID.setAccessible(true);
            }
            if (Build.VERSION.SDK_INT == 21) {
                return ((Long) getFieldValue(obj, FIELD_SUB_ID)).intValue();
            }
            return ((Integer) getFieldValue(obj, FIELD_SUB_ID)).intValue();
        }
        throw new UnsupportedOperationException();
    }

    private static boolean isEmpty(String str) {
        if (str == null || str.length() == 0 || str.trim().length() == 0) {
            return true;
        }
        return false;
    }

    private static Field getDeclaredField(Class<?> targetClass, String name) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getDeclaredField(name);
        } catch (SecurityException e) {
            Log.e(TAG, name + ":" + e.getCause());
            return null;
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, name + ",no such field.");
            return null;
        }
    }

    private static void setFieldValue(Object receiver, Field field, Object value) {
        if (field != null) {
            try {
                field.set(receiver, value);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                Log.e(TAG, "Exception in setFieldValue: " + e.getClass().getSimpleName());
            }
        }
    }

    private static Object getFieldValue(Object receiver, Field field) {
        if (field == null) {
            return null;
        }
        try {
            return field.get(receiver);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            Log.e(TAG, "Exception in getFieldValue: " + e.getClass().getSimpleName());
            throw new UnsupportedOperationException();
        }
    }
}
