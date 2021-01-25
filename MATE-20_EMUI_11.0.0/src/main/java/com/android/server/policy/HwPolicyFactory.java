package com.android.server.policy;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import com.android.internal.globalactions.Action;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.PhoneLayoutInflater;
import com.android.internal.policy.PhoneWindow;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.keyguard.KeyguardStateMonitor;
import java.util.ArrayList;

public class HwPolicyFactory {
    private static final String TAG = "HwPolicyFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        void addRebootMenu(ArrayList<Action> arrayList);

        void addUltraPowerSaveImpl(ArrayList arrayList, Context context);

        KeyguardStateMonitor getHwKeyguardStateMonitor(Context context, IKeyguardService iKeyguardService, KeyguardStateMonitor.StateCallback stateCallback);

        PhoneLayoutInflater getHwPhoneLayoutInflater(Context context);

        Window getHwPhoneWindow(Context context);

        WindowManagerPolicy getHwPhoneWindowManager();

        void hideBootMessage();

        boolean ifUseHwGlobalActions();

        boolean isHwFastShutdownEnable();

        boolean isHwGlobalActionsShowing();

        void onPowerExtraLongPress();

        void reportToAware(int i, int i2);

        void showBootMessage(Context context, int i, int i2);

        void showHwGlobalActionsFragment(Context context, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs, PowerManager powerManager, boolean z, boolean z2, boolean z3);
    }

    public static WindowManagerPolicy getHwPhoneWindowManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPhoneWindowManager();
        }
        return new PhoneWindowManager();
    }

    public static Window getHwPhoneWindow(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPhoneWindow(context);
        }
        return new PhoneWindow(context);
    }

    public static PhoneLayoutInflater getHwPhoneLayoutInflater(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPhoneLayoutInflater(context);
        }
        return new PhoneLayoutInflater(context);
    }

    private static Factory getImplObject() {
        synchronized (mLock) {
            if (obj != null) {
                return obj;
            }
            try {
                obj = (Factory) Class.forName("com.android.server.policy.HwPolicyFactoryImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception is " + e.getMessage());
            }
            if (obj != null) {
                Log.v(TAG, ": success to get AllImpl object and return....");
                return obj;
            }
            Log.e(TAG, ": fail to get AllImpl object");
            return null;
        }
    }

    public static void addUltraPowerSave(ArrayList items, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.addUltraPowerSaveImpl(items, context);
        }
    }

    public static void addRebootMenu(ArrayList<Action> items) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.addRebootMenu(items);
        }
    }

    public static void showHwGlobalActionsFragment(Context tContext, WindowManagerPolicy.WindowManagerFuncs tWindowManagerFuncs, PowerManager powerManager, boolean keyguardShowing, boolean keyguardSecure, boolean isDeviceProvisioned) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.showHwGlobalActionsFragment(tContext, tWindowManagerFuncs, powerManager, keyguardShowing, keyguardSecure, isDeviceProvisioned);
        }
    }

    public static void onPowerExtraLongPress() {
        Factory object = getImplObject();
        if (object != null) {
            object.onPowerExtraLongPress();
        }
    }

    public static void showBootMessage(Context tContext, int curr, int total) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.showBootMessage(tContext, curr, total);
        }
    }

    public static void hideBootMessage() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.hideBootMessage();
        }
    }

    public static boolean ifUseHwGlobalActions() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.ifUseHwGlobalActions();
        }
        return false;
    }

    public static boolean isHwGlobalActionsShowing() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isHwGlobalActionsShowing();
        }
        return false;
    }

    public static boolean isHwFastShutdownEnable() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isHwFastShutdownEnable();
        }
        return false;
    }

    public static void reportToAware(int code, int duration) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.reportToAware(code, duration);
        }
    }

    public static KeyguardStateMonitor getHwKeyguardStateMonitor(Context context, IKeyguardService service, KeyguardStateMonitor.StateCallback callback) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwKeyguardStateMonitor(context, service, callback);
        }
        return null;
    }
}
