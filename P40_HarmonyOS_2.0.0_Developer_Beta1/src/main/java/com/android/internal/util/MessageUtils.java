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
            super(String.format("Duplicate constant value: both %s and %s = %d", name1, name2, Integer.valueOf(value)));
        }
    }

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
                            if (name.startsWith(strArr[i3])) {
                                field.setAccessible(true);
                                try {
                                    int value = field.getInt(null);
                                    try {
                                        String previousName = messageNames.get(value);
                                        if (previousName != null) {
                                            if (!previousName.equals(name)) {
                                                throw new DuplicateConstantError(name, previousName, value);
                                            }
                                        }
                                        messageNames.put(value, name);
                                    } catch (IllegalAccessException | SecurityException e) {
                                    }
                                } catch (ExceptionInInitializerError | IllegalArgumentException e2) {
                                }
                            }
                            i3++;
                            strArr = prefixes;
                        }
                        continue;
                    }
                    i2++;
                    strArr = prefixes;
                }
                continue;
            } catch (SecurityException e3) {
                String str = TAG;
                Log.e(str, "Can't list fields of class " + className);
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
