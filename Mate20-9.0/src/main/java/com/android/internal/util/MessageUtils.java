package com.android.internal.util;

import android.util.Log;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class MessageUtils {
    private static final boolean DBG = false;
    public static final String[] DEFAULT_PREFIXES = {"CMD_", "EVENT_"};
    private static final String TAG = MessageUtils.class.getSimpleName();

    public static class DuplicateConstantError extends Error {
        private DuplicateConstantError() {
        }

        public DuplicateConstantError(String name1, String name2, int value) {
            super(String.format("Duplicate constant value: both %s and %s = %d", new Object[]{name1, name2, Integer.valueOf(value)}));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0070, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0071, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0073 A[ExcHandler: IllegalAccessException | SecurityException (e java.lang.Throwable), Splitter:B:14:0x0049] */
    public static SparseArray<String> findMessageNames(Class[] classes, String[] prefixes) {
        Class[] clsArr = classes;
        String[] strArr = prefixes;
        SparseArray<String> messageNames = new SparseArray<>();
        int length = clsArr.length;
        int i = 0;
        while (i < length) {
            Class c = clsArr[i];
            String className = c.getName();
            try {
                Field[] fields = c.getDeclaredFields();
                int length2 = fields.length;
                int i2 = 0;
                while (i2 < length2) {
                    Field field = fields[i2];
                    int modifiers = field.getModifiers();
                    if (!(!Modifier.isStatic(modifiers)) && !(!Modifier.isFinal(modifiers))) {
                        String name = field.getName();
                        int length3 = strArr.length;
                        int i3 = 0;
                        while (i3 < length3) {
                            String prefix = strArr[i3];
                            if (name.startsWith(prefix)) {
                                String str = prefix;
                                try {
                                    field.setAccessible(true);
                                    int value = field.getInt(null);
                                    String previousName = messageNames.get(value);
                                    if (previousName != null) {
                                        if (!previousName.equals(name)) {
                                            throw new DuplicateConstantError(name, previousName, value);
                                        }
                                    }
                                    messageNames.put(value, name);
                                } catch (IllegalAccessException | SecurityException e) {
                                }
                            }
                            i3++;
                            Class[] clsArr2 = classes;
                            strArr = prefixes;
                        }
                        continue;
                    }
                    i2++;
                    Class[] clsArr3 = classes;
                    strArr = prefixes;
                }
                continue;
            } catch (SecurityException e2) {
                SecurityException securityException = e2;
                String str2 = TAG;
                Log.e(str2, "Can't list fields of class " + className);
            }
            i++;
            clsArr = classes;
            strArr = prefixes;
        }
        return messageNames;
    }

    public static SparseArray<String> findMessageNames(Class[] classNames) {
        return findMessageNames(classNames, DEFAULT_PREFIXES);
    }
}
