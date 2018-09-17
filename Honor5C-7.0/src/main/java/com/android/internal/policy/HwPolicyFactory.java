package com.android.internal.policy;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;

public class HwPolicyFactory {
    private static final String TAG = "HwPolicyFactory";
    private static final Object mLock = null;
    private static volatile Factory obj;

    public interface Factory {
        View getHwNavigationBarColorView(Context context);

        PhoneLayoutInflater getHwPhoneLayoutInflater(Context context);

        Window getHwPhoneWindow(Context context);

        Window getHwPhoneWindow(Context context, Window window);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.policy.HwPolicyFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.policy.HwPolicyFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.policy.HwPolicyFactory.<clinit>():void");
    }

    public static Window getHwPhoneWindow(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPhoneWindow(context);
        }
        return new PhoneWindow(context);
    }

    public static Window getHwPhoneWindow(Context context, Window win) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPhoneWindow(context, win);
        }
        return new PhoneWindow(context, win);
    }

    public static PhoneLayoutInflater getHwPhoneLayoutInflater(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPhoneLayoutInflater(context);
        }
        return new PhoneLayoutInflater(context);
    }

    public static View getHwNavigationBarColorView(Context context) {
        Factory obj = getImplObject();
        return obj != null ? obj.getHwNavigationBarColorView(context) : new View(context);
    }

    private static Factory getImplObject() {
        if (obj != null) {
            return obj;
        }
        synchronized (mLock) {
            try {
                obj = (Factory) Class.forName("com.android.internal.policy.HwPolicyFactoryImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception occurs");
            }
        }
        if (obj != null) {
            Log.v(TAG, ": success to get AllImpl object and return....");
            return obj;
        }
        Log.e(TAG, ": fail to get AllImpl object");
        return null;
    }
}
