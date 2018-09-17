package android.util;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;

public class DebugUtils {
    public static boolean isObjectSelected(Object object) {
        boolean z = false;
        String s = System.getenv("ANDROID_OBJECT_FILTER");
        if (s != null && s.length() > 0) {
            String[] selectors = s.split("@");
            if (object.getClass().getSimpleName().matches(selectors[0])) {
                for (int i = 1; i < selectors.length; i++) {
                    Method declaredMethod;
                    String[] pair = selectors[i].split("=");
                    Class<?> klass = object.getClass();
                    Class<?> parent = klass;
                    do {
                        try {
                            declaredMethod = parent.getDeclaredMethod("get" + pair[0].substring(0, 1).toUpperCase(Locale.ROOT) + pair[0].substring(1), (Class[]) null);
                            parent = klass.getSuperclass();
                            if (parent == null) {
                                break;
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e2) {
                            e2.printStackTrace();
                        } catch (InvocationTargetException e3) {
                            e3.printStackTrace();
                        }
                    } while (declaredMethod == null);
                    if (declaredMethod != null) {
                        Object value = declaredMethod.invoke(object, (Object[]) null);
                        z |= (value != null ? value.toString() : "null").matches(pair[1]);
                    }
                }
            }
        }
        return z;
    }

    public static void buildShortClassTag(Object cls, StringBuilder out) {
        if (cls == null) {
            out.append("null");
            return;
        }
        String simpleName = cls.getClass().getSimpleName();
        if (simpleName == null || simpleName.isEmpty()) {
            simpleName = cls.getClass().getName();
            int end = simpleName.lastIndexOf(46);
            if (end > 0) {
                simpleName = simpleName.substring(end + 1);
            }
        }
        out.append(simpleName);
        out.append('{');
        out.append(Integer.toHexString(System.identityHashCode(cls)));
    }

    public static void printSizeValue(PrintWriter pw, long number) {
        String value;
        float result = (float) number;
        String suffix = LogException.NO_VALUE;
        if (result > 900.0f) {
            suffix = "KB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "MB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "GB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "TB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "PB";
            result /= 1024.0f;
        }
        if (result < 1.0f) {
            value = String.format("%.2f", new Object[]{Float.valueOf(result)});
        } else if (result < 10.0f) {
            value = String.format("%.1f", new Object[]{Float.valueOf(result)});
        } else if (result < 100.0f) {
            value = String.format("%.0f", new Object[]{Float.valueOf(result)});
        } else {
            value = String.format("%.0f", new Object[]{Float.valueOf(result)});
        }
        pw.print(value);
        pw.print(suffix);
    }

    public static String sizeValueToString(long number, StringBuilder outBuilder) {
        String value;
        if (outBuilder == null) {
            outBuilder = new StringBuilder(32);
        }
        float result = (float) number;
        String suffix = LogException.NO_VALUE;
        if (result > 900.0f) {
            suffix = "KB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "MB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "GB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "TB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "PB";
            result /= 1024.0f;
        }
        if (result < 1.0f) {
            value = String.format("%.2f", new Object[]{Float.valueOf(result)});
        } else if (result < 10.0f) {
            value = String.format("%.1f", new Object[]{Float.valueOf(result)});
        } else if (result < 100.0f) {
            value = String.format("%.0f", new Object[]{Float.valueOf(result)});
        } else {
            value = String.format("%.0f", new Object[]{Float.valueOf(result)});
        }
        outBuilder.append(value);
        outBuilder.append(suffix);
        return outBuilder.toString();
    }

    public static String valueToString(Class<?> clazz, String prefix, int value) {
        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && field.getType().equals(Integer.TYPE) && field.getName().startsWith(prefix)) {
                try {
                    if (value == field.getInt(null)) {
                        return field.getName().substring(prefix.length());
                    }
                } catch (IllegalAccessException e) {
                }
            }
        }
        return Integer.toString(value);
    }

    public static String flagsToString(Class<?> clazz, String prefix, int flags) {
        StringBuilder res = new StringBuilder();
        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && field.getType().equals(Integer.TYPE) && field.getName().startsWith(prefix)) {
                try {
                    int value = field.getInt(null);
                    if ((flags & value) != 0) {
                        flags &= ~value;
                        res.append(field.getName().substring(prefix.length())).append('|');
                    }
                } catch (IllegalAccessException e) {
                }
            }
        }
        if (flags != 0 || res.length() == 0) {
            res.append(Integer.toHexString(flags));
        } else {
            res.deleteCharAt(res.length() - 1);
        }
        return res.toString();
    }
}
