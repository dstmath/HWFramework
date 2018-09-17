package com.android.internal.util;

import android.util.Log;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class MessageUtils {
    private static final boolean DBG = false;
    public static final String[] DEFAULT_PREFIXES = null;
    private static final String TAG = null;

    public static class DuplicateConstantError extends Error {
        private DuplicateConstantError() {
        }

        public DuplicateConstantError(String name1, String name2, int value) {
            super(String.format("Duplicate constant value: both %s and %s = %d", new Object[]{name1, name2, Integer.valueOf(value)}));
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.util.MessageUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.util.MessageUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.MessageUtils.<clinit>():void");
    }

    public static SparseArray<String> findMessageNames(Class[] classes, String[] prefixes) {
        SparseArray<String> messageNames = new SparseArray();
        for (Class c : classes) {
            try {
                for (Field field : c.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (((Modifier.isStatic(modifiers) ? 0 : 1) | (Modifier.isFinal(modifiers) ? 0 : 1)) == 0) {
                        String name = field.getName();
                        for (String prefix : prefixes) {
                            if (name.startsWith(prefix)) {
                                try {
                                    field.setAccessible(true);
                                    try {
                                        int value = field.getInt(null);
                                        String previousName = (String) messageNames.get(value);
                                        if (previousName == null || previousName.equals(name)) {
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
                }
                continue;
            } catch (SecurityException e3) {
                Log.e(TAG, "Can't list fields of class " + c.getName());
            }
        }
        return messageNames;
    }

    public static SparseArray<String> findMessageNames(Class[] classNames) {
        return findMessageNames(classNames, DEFAULT_PREFIXES);
    }
}
