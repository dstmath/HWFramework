package com.android.internal.util;

import android.text.TextUtils;
import java.util.Collection;

public class Preconditions {
    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static void checkArgument(boolean expression, String messageTemplate, Object... messageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(messageTemplate, messageArgs));
        }
    }

    public static <T extends CharSequence> T checkStringNotEmpty(T string) {
        if (!TextUtils.isEmpty(string)) {
            return string;
        }
        throw new IllegalArgumentException();
    }

    public static <T extends CharSequence> T checkStringNotEmpty(T string, Object errorMessage) {
        if (!TextUtils.isEmpty(string)) {
            return string;
        }
        throw new IllegalArgumentException(String.valueOf(errorMessage));
    }

    public static <T> T checkNotNull(T reference) {
        if (reference != null) {
            return reference;
        }
        throw new NullPointerException();
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference != null) {
            return reference;
        }
        throw new NullPointerException(String.valueOf(errorMessage));
    }

    public static <T> T checkNotNull(T reference, String messageTemplate, Object... messageArgs) {
        if (reference != null) {
            return reference;
        }
        throw new NullPointerException(String.format(messageTemplate, messageArgs));
    }

    public static void checkState(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static void checkState(boolean expression) {
        checkState(expression, null);
    }

    public static int checkFlagsArgument(int requestedFlags, int allowedFlags) {
        if ((requestedFlags & allowedFlags) == requestedFlags) {
            return requestedFlags;
        }
        throw new IllegalArgumentException("Requested flags 0x" + Integer.toHexString(requestedFlags) + ", but only 0x" + Integer.toHexString(allowedFlags) + " are allowed");
    }

    public static int checkArgumentNonnegative(int value, String errorMessage) {
        if (value >= 0) {
            return value;
        }
        throw new IllegalArgumentException(errorMessage);
    }

    public static int checkArgumentNonnegative(int value) {
        if (value >= 0) {
            return value;
        }
        throw new IllegalArgumentException();
    }

    public static long checkArgumentNonnegative(long value) {
        if (value >= 0) {
            return value;
        }
        throw new IllegalArgumentException();
    }

    public static long checkArgumentNonnegative(long value, String errorMessage) {
        if (value >= 0) {
            return value;
        }
        throw new IllegalArgumentException(errorMessage);
    }

    public static int checkArgumentPositive(int value, String errorMessage) {
        if (value > 0) {
            return value;
        }
        throw new IllegalArgumentException(errorMessage);
    }

    public static float checkArgumentFinite(float value, String valueName) {
        if (Float.isNaN(value)) {
            throw new IllegalArgumentException(valueName + " must not be NaN");
        } else if (!Float.isInfinite(value)) {
            return value;
        } else {
            throw new IllegalArgumentException(valueName + " must not be infinite");
        }
    }

    public static float checkArgumentInRange(float value, float lower, float upper, String valueName) {
        if (Float.isNaN(value)) {
            throw new IllegalArgumentException(valueName + " must not be NaN");
        } else if (value < lower) {
            throw new IllegalArgumentException(String.format("%s is out of range of [%f, %f] (too low)", new Object[]{valueName, Float.valueOf(lower), Float.valueOf(upper)}));
        } else if (value <= upper) {
            return value;
        } else {
            throw new IllegalArgumentException(String.format("%s is out of range of [%f, %f] (too high)", new Object[]{valueName, Float.valueOf(lower), Float.valueOf(upper)}));
        }
    }

    public static int checkArgumentInRange(int value, int lower, int upper, String valueName) {
        if (value < lower) {
            throw new IllegalArgumentException(String.format("%s is out of range of [%d, %d] (too low)", new Object[]{valueName, Integer.valueOf(lower), Integer.valueOf(upper)}));
        } else if (value <= upper) {
            return value;
        } else {
            throw new IllegalArgumentException(String.format("%s is out of range of [%d, %d] (too high)", new Object[]{valueName, Integer.valueOf(lower), Integer.valueOf(upper)}));
        }
    }

    public static long checkArgumentInRange(long value, long lower, long upper, String valueName) {
        if (value < lower) {
            throw new IllegalArgumentException(String.format("%s is out of range of [%d, %d] (too low)", new Object[]{valueName, Long.valueOf(lower), Long.valueOf(upper)}));
        } else if (value <= upper) {
            return value;
        } else {
            throw new IllegalArgumentException(String.format("%s is out of range of [%d, %d] (too high)", new Object[]{valueName, Long.valueOf(lower), Long.valueOf(upper)}));
        }
    }

    public static <T> T[] checkArrayElementsNotNull(T[] value, String valueName) {
        if (value == null) {
            throw new NullPointerException(valueName + " must not be null");
        }
        for (T t : value) {
            if (t == null) {
                throw new NullPointerException(String.format("%s[%d] must not be null", new Object[]{valueName, Integer.valueOf(i)}));
            }
        }
        return value;
    }

    public static <C extends Collection<T>, T> C checkCollectionElementsNotNull(C value, String valueName) {
        if (value == null) {
            throw new NullPointerException(valueName + " must not be null");
        }
        long ctr = 0;
        for (T elem : value) {
            if (elem == null) {
                throw new NullPointerException(String.format("%s[%d] must not be null", new Object[]{valueName, Long.valueOf(ctr)}));
            }
            ctr++;
        }
        return value;
    }

    public static <T> Collection<T> checkCollectionNotEmpty(Collection<T> value, String valueName) {
        if (value == null) {
            throw new NullPointerException(valueName + " must not be null");
        } else if (!value.isEmpty()) {
            return value;
        } else {
            throw new IllegalArgumentException(valueName + " is empty");
        }
    }

    public static float[] checkArrayElementsInRange(float[] value, float lower, float upper, String valueName) {
        checkNotNull(value, valueName + " must not be null");
        int i = 0;
        while (i < value.length) {
            float v = value[i];
            if (Float.isNaN(v)) {
                throw new IllegalArgumentException(valueName + "[" + i + "] must not be NaN");
            } else if (v < lower) {
                throw new IllegalArgumentException(String.format("%s[%d] is out of range of [%f, %f] (too low)", new Object[]{valueName, Integer.valueOf(i), Float.valueOf(lower), Float.valueOf(upper)}));
            } else if (v > upper) {
                throw new IllegalArgumentException(String.format("%s[%d] is out of range of [%f, %f] (too high)", new Object[]{valueName, Integer.valueOf(i), Float.valueOf(lower), Float.valueOf(upper)}));
            } else {
                i++;
            }
        }
        return value;
    }
}
