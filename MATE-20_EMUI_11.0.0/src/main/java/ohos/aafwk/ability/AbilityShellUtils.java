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
        if (ability == null) {
            Log.error("service ability is null, can't keep running in background", new Object[0]);
            return;
        }
        Object hostContext = ability.getHostContext();
        if (!(hostContext instanceof Context)) {
            Log.error("host context is not Context, can't continue ability", new Object[0]);
            return;
        }
        Context context = (Context) hostContext;
        if (context instanceof Service) {
            try {
                Class<? super Object> superclass = context.getClass().getSuperclass();
                Log.info(CONTEXT_CLASS_LOG, superclass.getName());
                Method declaredMethod = superclass.getDeclaredMethod("keepBackgroundRunning", Integer.TYPE, NotificationRequest.class);
                AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                    /* class ohos.aafwk.ability.$$Lambda$AbilityShellUtils$t9iQKju0UXaan1QV8gv0G7xTAs */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return this.f$0.setAccessible(true);
                    }
                });
                declaredMethod.invoke(context, Integer.valueOf(i), notificationRequest);
            } catch (IllegalAccessException | NoSuchMethodException unused) {
                Log.error("No such method in ability host context", new Object[0]);
            } catch (InvocationTargetException e) {
                Log.error("keepBackgroundRunning failed: %{public}s", e.getCause().getMessage());
            }
        }
    }

    static void cancelBackgroundRunning(Ability ability) {
        if (ability == null) {
            Log.error("service ability is null, can't cancel running in background", new Object[0]);
            return;
        }
        Object hostContext = ability.getHostContext();
        if (!(hostContext instanceof Context)) {
            Log.error("host context is not Context, can't continue ability", new Object[0]);
            return;
        }
        Context context = (Context) hostContext;
        if (context instanceof Service) {
            try {
                Class<? super Object> superclass = context.getClass().getSuperclass();
                Log.info(CONTEXT_CLASS_LOG, superclass.getName());
                Method declaredMethod = superclass.getDeclaredMethod("cancelBackgroundRunning", new Class[0]);
                AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                    /* class ohos.aafwk.ability.$$Lambda$AbilityShellUtils$HOwrCJ5V1MacIpEL7km3Xdb5J9o */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return this.f$0.setAccessible(true);
                    }
                });
                declaredMethod.invoke(context, new Object[0]);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
                Log.error("No such method in ability host context", new Object[0]);
            }
        }
    }

    static boolean continueAbility(Ability ability, boolean z, String str) {
        return ((Boolean) reflectInvoke(ability, "continueAbility", new Class[]{Boolean.TYPE, String.class}, Boolean.valueOf(z), str)).booleanValue();
    }

    static boolean reverseContinueAbility(Ability ability) {
        return ((Boolean) reflectInvoke(ability, "reverseContinueAbility", null, new Object[0])).booleanValue();
    }

    private static Object reflectInvoke(Ability ability, String str, Class<?>[] clsArr, Object... objArr) {
        Method method;
        if (ability == null) {
            Log.error("ability is null", new Object[0]);
            return false;
        }
        Object hostContext = ability.getHostContext();
        if (!(hostContext instanceof Context)) {
            Log.error("host context is not Context", new Object[0]);
            return false;
        }
        Context context = (Context) hostContext;
        if (!(context instanceof Activity)) {
            return false;
        }
        try {
            Class<? super Object> superclass = context.getClass().getSuperclass();
            Log.info(CONTEXT_CLASS_LOG, superclass.getName());
            if (clsArr == null) {
                method = superclass.getDeclaredMethod(str, new Class[0]);
            } else {
                method = superclass.getDeclaredMethod(str, clsArr);
            }
            AccessController.doPrivileged(new PrivilegedAction(method) {
                /* class ohos.aafwk.ability.$$Lambda$AbilityShellUtils$CNxKVaJoC27UfjQx1GM0SFPogoI */
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
        if (ability == null) {
            Log.error("ability is null, can't show driving safety tips", new Object[0]);
            return;
        }
        Object hostContext = ability.getHostContext();
        if (!(hostContext instanceof Context)) {
            Log.error("host context is not Context, can't show driving safety tips", new Object[0]);
            return;
        }
        Context context = (Context) hostContext;
        if (context instanceof Activity) {
            try {
                Class<? super Object> superclass = context.getClass().getSuperclass();
                Log.info(CONTEXT_CLASS_LOG, superclass.getName());
                Method declaredMethod = superclass.getDeclaredMethod("showDrivingSafetyTips", new Class[0]);
                AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                    /* class ohos.aafwk.ability.$$Lambda$AbilityShellUtils$pmZcWbVML7VrABxzUxzgNRyT8c */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return this.f$0.setAccessible(true);
                    }
                });
                declaredMethod.invoke(context, new Object[0]);
            } catch (NoSuchMethodException unused) {
                Log.error("No such method in ability host context", new Object[0]);
            } catch (IllegalAccessException unused2) {
                Log.error("illegal access in ability host context", new Object[0]);
            } catch (InvocationTargetException unused3) {
                Log.error("the method in ability host context throws an exception", new Object[0]);
            }
        }
    }
}
