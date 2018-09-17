package com.android.server.rms.record;

import android.util.Log;
import com.huawei.utils.reflect.HwReflectUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JankLogProxy {
    private static final Class<?> CLASS_Jlog = null;
    private static final Class<?> CLASS_JlogConstants = null;
    private static final Field FIELD_RESOURCE_MANAGER = null;
    private static final int JlogID = 0;
    private static final Method METHOD_Jlogd_Arg = null;
    private static final String TAG = "RMS.JankLogProxy";
    private static JankLogProxy mJankLogProxy;
    private final Object mJLog;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.record.JankLogProxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.record.JankLogProxy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.record.JankLogProxy.<clinit>():void");
    }

    public JankLogProxy() {
        this.mJLog = getJlogInstance(CLASS_Jlog);
    }

    public static synchronized JankLogProxy getInstance() {
        JankLogProxy jankLogProxy;
        synchronized (JankLogProxy.class) {
            if (mJankLogProxy == null) {
                mJankLogProxy = new JankLogProxy();
            }
            jankLogProxy = mJankLogProxy;
        }
        return jankLogProxy;
    }

    private static Object getJlogInstance(Class<?> targetClass) {
        if (targetClass == null) {
            return null;
        }
        try {
            Constructor<?> CONSTRUCTOR_Jlog = targetClass.getDeclaredConstructor(new Class[JlogID]);
            if (CONSTRUCTOR_Jlog != null) {
                CONSTRUCTOR_Jlog.setAccessible(true);
            }
            return HwReflectUtils.newInstance(CONSTRUCTOR_Jlog, new Object[JlogID]);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Jlog method not found in class " + targetClass);
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "Unknown exception while trying to get Jlog Instance");
            return null;
        }
    }

    public int jlog_d(String arg1, int arg2, String msg) {
        int result = -1;
        try {
            if (this.mJLog != null) {
                result = ((Integer) HwReflectUtils.invoke(this.mJLog, METHOD_Jlogd_Arg, new Object[]{Integer.valueOf(JlogID), arg1, Integer.valueOf(arg2), msg})).intValue();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unknown exception while trying to invoke [Jlog.d.arg]");
        }
        return result;
    }
}
