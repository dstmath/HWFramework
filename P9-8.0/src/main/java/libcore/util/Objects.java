package libcore.util;

import android.icu.impl.PatternTokenizer;
import java.lang.reflect.Field;
import java.util.Arrays;

public final class Objects {
    private Objects() {
    }

    public static boolean equal(Object a, Object b) {
        if (a != b) {
            return a != null ? a.equals(b) : false;
        } else {
            return true;
        }
    }

    public static int hashCode(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    public static String toString(Object o) {
        IllegalAccessException unexpected;
        int i = 0;
        Class<?> c = o.getClass();
        StringBuilder sb = new StringBuilder();
        sb.append(c.getSimpleName()).append('[');
        Field[] declaredFields = c.getDeclaredFields();
        int length = declaredFields.length;
        int i2 = 0;
        while (i < length) {
            int i3;
            Field f = declaredFields[i];
            if ((f.getModifiers() & 136) != 0) {
                i3 = i2;
            } else {
                f.setAccessible(true);
                try {
                    Object value = f.get(o);
                    i3 = i2 + 1;
                    if (i2 > 0) {
                        try {
                            sb.append(',');
                        } catch (IllegalAccessException e) {
                            unexpected = e;
                            throw new AssertionError(unexpected);
                        }
                    }
                    sb.append(f.getName());
                    sb.append('=');
                    if (value.getClass().isArray()) {
                        if (value.getClass() == boolean[].class) {
                            sb.append(Arrays.toString((boolean[]) value));
                        } else if (value.getClass() == byte[].class) {
                            sb.append(Arrays.toString((byte[]) value));
                        } else if (value.getClass() == char[].class) {
                            sb.append(Arrays.toString((char[]) value));
                        } else if (value.getClass() == double[].class) {
                            sb.append(Arrays.toString((double[]) value));
                        } else if (value.getClass() == float[].class) {
                            sb.append(Arrays.toString((float[]) value));
                        } else if (value.getClass() == int[].class) {
                            sb.append(Arrays.toString((int[]) value));
                        } else if (value.getClass() == long[].class) {
                            sb.append(Arrays.toString((long[]) value));
                        } else if (value.getClass() == short[].class) {
                            sb.append(Arrays.toString((short[]) value));
                        } else {
                            sb.append(Arrays.toString((Object[]) value));
                        }
                    } else if (value.getClass() == Character.class) {
                        sb.append(PatternTokenizer.SINGLE_QUOTE).append(value).append(PatternTokenizer.SINGLE_QUOTE);
                    } else if (value.getClass() == String.class) {
                        sb.append('\"').append(value).append('\"');
                    } else {
                        sb.append(value);
                    }
                } catch (IllegalAccessException e2) {
                    unexpected = e2;
                    i3 = i2;
                }
            }
            i++;
            i2 = i3;
        }
        sb.append("]");
        return sb.toString();
    }
}
