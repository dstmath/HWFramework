package com.huawei.nb.utils.validation;

import android.os.Parcel;
import com.huawei.nb.utils.logger.DSLog;
import java.util.regex.Pattern;

public final class ObjectValidation {
    private ObjectValidation() {
        throw new IllegalStateException("No instances!");
    }

    public static <T> T verifyNotNull(T object, String message) {
        if (object != null) {
            return object;
        }
        throw new NullPointerException(message);
    }

    public static int getHashCode(Object o) {
        if (o != null) {
            return o.hashCode();
        }
        return 0;
    }

    public static int compare(int v1, int v2) {
        if (v1 < v2) {
            return -1;
        }
        return v1 > v2 ? 1 : 0;
    }

    public static int compare(long v1, long v2) {
        if (v1 < v2) {
            return -1;
        }
        return v1 > v2 ? 1 : 0;
    }

    public static int verifyPositive(int value, String param) {
        if (value > 0) {
            return value;
        }
        throw new IllegalArgumentException(param + " > 0 required but it was " + value);
    }

    public static long verifyPositive(long value, String paramName) {
        if (value > 0) {
            return value;
        }
        throw new IllegalArgumentException(paramName + " > 0 required but it was " + value);
    }

    public static Object readFromParcelByClass(Parcel in) {
        Class clazz = (Class) in.readSerializable();
        if (clazz == Integer.class) {
            return Integer.valueOf(in.readInt());
        }
        if (clazz == String.class) {
            return in.readString();
        }
        DSLog.e("read " + clazz.getName() + " from Parcel is not support now.", new Object[0]);
        return null;
    }

    public static void writeToParcelWithClass(Object obj, Parcel dest, int flags) {
        if (obj instanceof Integer) {
            dest.writeSerializable(Integer.class);
            dest.writeInt(((Integer) obj).intValue());
        } else if (obj instanceof String) {
            dest.writeSerializable(String.class);
            dest.writeString((String) obj);
        } else {
            DSLog.e("write " + obj.getClass().getName() + " to Parcel is not support now.", new Object[0]);
        }
    }

    public static boolean checkStringFormat(String strToCheck, String formatPattern) {
        if (strToCheck == null || !Pattern.compile(formatPattern, 2).matcher(strToCheck).matches()) {
            return false;
        }
        return true;
    }
}
