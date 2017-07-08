package android.util;

import android.view.WindowManager.LayoutParams;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;

public class DebugUtils {
    public static java.lang.String flagsToString(java.lang.Class<?> r1, java.lang.String r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.DebugUtils.flagsToString(java.lang.Class, java.lang.String, int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.DebugUtils.flagsToString(java.lang.Class, java.lang.String, int):java.lang.String");
    }

    public static boolean isObjectSelected(Object object) {
        boolean z = false;
        String s = System.getenv("ANDROID_OBJECT_FILTER");
        if (s != null && s.length() > 0) {
            String[] selectors = s.split("@");
            if (object.getClass().getSimpleName().matches(selectors[0])) {
                for (int i = 1; i < selectors.length; i++) {
                    Method declaredMethod;
                    String[] pair = selectors[i].split(PtmLog.KEY_VAL_SEP);
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
        String suffix = "";
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
        if (result < LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
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
        String suffix = "";
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
        if (result < LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
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
}
