package com.huawei.zxing.resultdispatch.wifi;

import android.os.Build.VERSION;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class PlatformSupportManager<T> {
    private static final String TAG = null;
    private final T defaultImplementation;
    private final SortedMap<Integer, String> implementations;
    private final Class<T> managedInterface;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.resultdispatch.wifi.PlatformSupportManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.resultdispatch.wifi.PlatformSupportManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.resultdispatch.wifi.PlatformSupportManager.<clinit>():void");
    }

    protected PlatformSupportManager(Class<T> managedInterface, T defaultImplementation) {
        if (!managedInterface.isInterface()) {
            throw new IllegalArgumentException();
        } else if (managedInterface.isInstance(defaultImplementation)) {
            this.managedInterface = managedInterface;
            this.defaultImplementation = defaultImplementation;
            this.implementations = new TreeMap(Collections.reverseOrder());
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected final void addImplementationClass(int minVersion, String className) {
        this.implementations.put(Integer.valueOf(minVersion), className);
    }

    public final T build() {
        for (Integer minVersion : this.implementations.keySet()) {
            if (VERSION.SDK_INT >= minVersion.intValue()) {
                try {
                    Class<? extends T> clazz = Class.forName((String) this.implementations.get(minVersion)).asSubclass(this.managedInterface);
                    Log.i(TAG, "Using implementation " + clazz + " of " + this.managedInterface + " for SDK " + minVersion);
                    return clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                } catch (ClassNotFoundException cnfe) {
                    Log.w(TAG, cnfe);
                } catch (IllegalAccessException iae) {
                    Log.w(TAG, iae);
                } catch (InstantiationException ie) {
                    Log.w(TAG, ie);
                } catch (NoSuchMethodException nsme) {
                    Log.w(TAG, nsme);
                } catch (InvocationTargetException ite) {
                    Log.w(TAG, ite);
                }
            }
        }
        Log.i(TAG, "Using default implementation " + this.defaultImplementation.getClass() + " of " + this.managedInterface);
        return this.defaultImplementation;
    }
}
