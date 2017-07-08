package com.huawei.utils.reflect;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class EasyInvokeFactory {
    private static Map<Class<?>, EasyInvokeUtils> invokeUtilsMap;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.utils.reflect.EasyInvokeFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.utils.reflect.EasyInvokeFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.utils.reflect.EasyInvokeFactory.<clinit>():void");
    }

    public static synchronized <T extends EasyInvokeUtils> T getInvokeUtils(Class<T> clazz) {
        EasyInvokeUtils invokeUtil;
        synchronized (EasyInvokeFactory.class) {
            invokeUtil = (EasyInvokeUtils) invokeUtilsMap.get(clazz);
            if (invokeUtil == null) {
                try {
                    invokeUtil = (EasyInvokeUtils) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                    invokeUtilsMap.put(clazz, invokeUtil);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                } catch (IllegalArgumentException e3) {
                    e3.printStackTrace();
                } catch (InstantiationException e4) {
                    e4.printStackTrace();
                } catch (IllegalAccessException e5) {
                    e5.printStackTrace();
                } catch (InvocationTargetException e6) {
                    e6.printStackTrace();
                }
                if (invokeUtil == null) {
                    Log.e("EasyInvokeFactory", "create instance error clazz[" + clazz + "]");
                }
            }
        }
        return invokeUtil;
    }
}
