package com.huawei.android.hwaps;

import android.util.Log;

public class HwapsWrapper {
    private static final String TAG = "Hwaps";
    private static IHwapsFactory mFactory;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.HwapsWrapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.HwapsWrapper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.HwapsWrapper.<clinit>():void");
    }

    private static synchronized IHwapsFactory getHwapsFactoryImpl() {
        synchronized (HwapsWrapper.class) {
            if (mFactory != null) {
                IHwapsFactory iHwapsFactory = mFactory;
                return iHwapsFactory;
            }
            try {
                mFactory = (IHwapsFactory) Class.forName("com.huawei.android.hwaps.HwapsFactoryImpl").newInstance();
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "reflection exception is ClassNotFoundException");
            } catch (InstantiationException e2) {
                Log.e(TAG, "reflection exception is InstantiationException");
            } catch (IllegalAccessException e3) {
                Log.e(TAG, "reflection exception is IllegalAccessException");
            }
            if (mFactory == null) {
                Log.e(TAG, "failes to get HwapsFactoryImpl");
            }
            iHwapsFactory = mFactory;
            return iHwapsFactory;
        }
    }

    public static IFpsRequest getFpsRequest() {
        IHwapsFactory factory = getHwapsFactoryImpl();
        if (factory != null) {
            return factory.getFpsRequest();
        }
        return null;
    }

    public static IFpsController getFpsController() {
        IHwapsFactory factory = getHwapsFactoryImpl();
        if (factory != null) {
            return factory.getFpsController();
        }
        return null;
    }

    public static IEventAnalyzed getEventAnalyzed() {
        IHwapsFactory factory = getHwapsFactoryImpl();
        if (factory != null) {
            return factory.getEventAnalyzed();
        }
        return null;
    }
}
