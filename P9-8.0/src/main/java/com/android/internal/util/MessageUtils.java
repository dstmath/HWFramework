package com.android.internal.util;

import android.util.Log;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class MessageUtils {
    private static final boolean DBG = false;
    public static final String[] DEFAULT_PREFIXES = new String[]{"CMD_", "EVENT_"};
    private static final String TAG = MessageUtils.class.getSimpleName();

    public static class DuplicateConstantError extends Error {
        private DuplicateConstantError() {
        }

        public DuplicateConstantError(String name1, String name2, int value) {
            super(String.format("Duplicate constant value: both %s and %s = %d", new Object[]{name1, name2, Integer.valueOf(value)}));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ab A:{Splitter: B:25:0x008b, ExcHandler: java.lang.IllegalArgumentException (e java.lang.IllegalArgumentException), Catch:{ SecurityException -> 0x00a9, SecurityException -> 0x00a9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00a9 A:{Splitter: B:22:0x0084, ExcHandler: java.lang.SecurityException (e java.lang.SecurityException), Catch:{ SecurityException -> 0x00a9, SecurityException -> 0x00a9 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static SparseArray<String> findMessageNames(Class[] classes, String[] prefixes) {
        SparseArray<String> messageNames = new SparseArray();
        int i = 0;
        int length = classes.length;
        while (true) {
            int i2 = i;
            if (i2 >= length) {
                return messageNames;
            }
            Class c = classes[i2];
            String className = c.getName();
            try {
                Field[] fields = c.getDeclaredFields();
                i = 0;
                int length2 = fields.length;
                while (true) {
                    int i3 = i;
                    if (i3 >= length2) {
                        continue;
                        break;
                    }
                    Field field = fields[i3];
                    int modifiers = field.getModifiers();
                    if (((Modifier.isStatic(modifiers) ^ 1) | (Modifier.isFinal(modifiers) ^ 1)) == 0) {
                        String name = field.getName();
                        for (String prefix : prefixes) {
                            if (name.startsWith(prefix)) {
                                try {
                                    field.setAccessible(true);
                                    try {
                                        int value = field.getInt(null);
                                        String previousName = (String) messageNames.get(value);
                                        if (previousName == null || (previousName.equals(name) ^ 1) == 0) {
                                            messageNames.put(value, name);
                                        } else {
                                            throw new DuplicateConstantError(name, previousName, value);
                                        }
                                    } catch (IllegalArgumentException e) {
                                    }
                                } catch (SecurityException e2) {
                                }
                            }
                        }
                        continue;
                    }
                    i = i3 + 1;
                }
            } catch (SecurityException e3) {
                Log.e(TAG, "Can't list fields of class " + className);
            }
            i = i2 + 1;
        }
    }

    public static SparseArray<String> findMessageNames(Class[] classNames) {
        return findMessageNames(classNames, DEFAULT_PREFIXES);
    }
}
