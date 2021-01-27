package ohos.aafwk.ability;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import ohos.aafwk.utils.log.Log;
import ohos.event.notification.NotificationRequest;

/* access modifiers changed from: package-private */
public class AbilityShellUtils {
    private static final String CONTEXT_CLASS_LOG = "contextClass = %{public}s";

    AbilityShellUtils() {
    }

    static void keepBackgroundRunning(Ability ability, int i, NotificationRequest notificationRequest) {
        reflectInvokeService(ability, "keepBackgroundRunning", new Class[]{Integer.TYPE, NotificationRequest.class}, Integer.valueOf(i), notificationRequest);
    }

    static void cancelBackgroundRunning(Ability ability) {
        reflectInvokeService(ability, "cancelBackgroundRunning", null, new Object[0]);
    }

    private static void reflectInvokeService(Ability ability, String str, Class<?>[] clsArr, Object... objArr) {
        if (ability == null) {
            Log.error("service ability is null, can't %{public}s", str);
            return;
        }
        Object hostContext = ability.getHostContext();
        if (!(hostContext instanceof Context)) {
            Log.error("host context is not Context, can't %{public}s", str);
            return;
        }
        Context context = (Context) hostContext;
        if (context instanceof Service) {
            invokeMethod(context, str, clsArr, objArr);
        }
    }

    static boolean continueAbility(Ability ability, boolean z, String str) {
        return ((Boolean) reflectInvoke(ability, "continueAbility", new Class[]{Boolean.TYPE, String.class}, Boolean.valueOf(z), str)).booleanValue();
    }

    static boolean reverseContinueAbility(Ability ability) {
        return ((Boolean) reflectInvoke(ability, "reverseContinueAbility", null, new Object[0])).booleanValue();
    }

    private static Object reflectInvoke(Ability ability, String str, Class<?>[] clsArr, Object... objArr) {
        if (ability == null) {
            Log.error("ability is null, can't %{public}s", str);
            return false;
        }
        Object hostContext = ability.getHostContext();
        if (!(hostContext instanceof Context)) {
            Log.error("host context is not Context, can't %{public}s", str);
            return false;
        }
        Context context = (Context) hostContext;
        if (!(context instanceof Activity)) {
            return false;
        }
        return invokeMethod(context, str, clsArr, objArr);
    }

    private static Object invokeMethod(Context context, String str, Class<?>[] clsArr, Object... objArr) {
        Method method;
        try {
            Class<? super Object> superclass = context.getClass().getSuperclass();
            Log.info(CONTEXT_CLASS_LOG, superclass.getName());
            if (clsArr == null) {
                method = superclass.getDeclaredMethod(str, new Class[0]);
            } else {
                method = superclass.getDeclaredMethod(str, clsArr);
            }
            AccessController.doPrivileged(new PrivilegedAction(method) {
                /* class ohos.aafwk.ability.$$Lambda$AbilityShellUtils$q_07fmW9Nq3KMBXBhgVy00tO3d0 */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return this.f$0.setAccessible(true);
                }
            });
            return method.invoke(context, objArr);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.error("Invoke method in ability host context fail: %{public}s", e.getMessage());
            return false;
        }
    }

    static void showDrivingSafetyTips(Ability ability) {
        reflectInvoke(ability, "showDrivingSafetyTips", null, new Object[0]);
    }
}
