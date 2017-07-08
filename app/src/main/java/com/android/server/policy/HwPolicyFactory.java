package com.android.server.policy;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import com.android.internal.policy.PhoneLayoutInflater;
import com.android.internal.policy.PhoneWindow;
import com.android.server.policy.GlobalActions.Action;
import java.util.ArrayList;

public class HwPolicyFactory {
    private static final String TAG = "HwPolicyFactory";
    private static final Object mLock = null;
    private static volatile Factory obj;

    public interface Factory {
        void addRebootMenu(ArrayList<Action> arrayList);

        void addUltraPowerSaveImpl(ArrayList arrayList, Context context);

        PhoneLayoutInflater getHwPhoneLayoutInflater(Context context);

        Window getHwPhoneWindow(Context context);

        WindowManagerPolicy getHwPhoneWindowManager();

        void hideBootMessage();

        boolean ifUseHwGlobalActions();

        boolean isHwGlobalActionsShowing();

        void showBootMessage(Context context, int i, int i2);

        void showHwGlobalActionsFragment(Context context, WindowManagerFuncs windowManagerFuncs, PowerManager powerManager, boolean z, boolean z2, boolean z3);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.HwPolicyFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.HwPolicyFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.HwPolicyFactory.<clinit>():void");
    }

    public static WindowManagerPolicy getHwPhoneWindowManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPhoneWindowManager();
        }
        return new PhoneWindowManager();
    }

    public static Window getHwPhoneWindow(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPhoneWindow(context);
        }
        return new PhoneWindow(context);
    }

    public static PhoneLayoutInflater getHwPhoneLayoutInflater(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPhoneLayoutInflater(context);
        }
        return new PhoneLayoutInflater(context);
    }

    private static Factory getImplObject() {
        synchronized (mLock) {
            if (obj != null) {
                Factory factory = obj;
                return factory;
            }
            try {
                obj = (Factory) Class.forName("com.android.server.policy.HwPolicyFactoryImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception is " + e.getMessage());
            }
            if (obj != null) {
                Log.v(TAG, ": success to get AllImpl object and return....");
                factory = obj;
                return factory;
            }
            Log.e(TAG, ": fail to get AllImpl object");
            return null;
        }
    }

    public static void addUltraPowerSave(ArrayList items, Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.addUltraPowerSaveImpl(items, context);
        }
    }

    public static void addRebootMenu(ArrayList<Action> items) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.addRebootMenu(items);
        }
    }

    public static void showHwGlobalActionsFragment(Context tContext, WindowManagerFuncs tWindowManagerFuncs, PowerManager powerManager, boolean keyguardShowing, boolean keyguardSecure, boolean isDeviceProvisioned) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.showHwGlobalActionsFragment(tContext, tWindowManagerFuncs, powerManager, keyguardShowing, keyguardSecure, isDeviceProvisioned);
        }
    }

    public static void showBootMessage(Context tContext, int curr, int total) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.showBootMessage(tContext, curr, total);
        }
    }

    public static void hideBootMessage() {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.hideBootMessage();
        }
    }

    public static boolean ifUseHwGlobalActions() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.ifUseHwGlobalActions();
        }
        return false;
    }

    public static boolean isHwGlobalActionsShowing() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isHwGlobalActionsShowing();
        }
        return false;
    }
}
