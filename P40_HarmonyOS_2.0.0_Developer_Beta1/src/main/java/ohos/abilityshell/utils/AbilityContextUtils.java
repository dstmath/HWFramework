package ohos.abilityshell.utils;

import android.content.ContentProvider;
import android.content.Context;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import ohos.abilityshell.IAbilityShell;
import ohos.app.AbilityContext;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityContextUtils {
    private static final String SHELL_FIELD_NAME = "shell";
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");

    private AbilityContextUtils() {
    }

    @Deprecated
    public static IAbilityShell getAbilityContext(Object obj) {
        if (!(obj instanceof AbilityContext)) {
            AppLog.e(SHELL_LABEL, "AbilityContextUtils::getAbilityContext object not AbilityContext type", new Object[0]);
            return null;
        }
        try {
            Field[] declaredFields = AbilityContext.class.getDeclaredFields();
            IAbilityShell iAbilityShell = null;
            for (Field field : declaredFields) {
                if (SHELL_FIELD_NAME.equals(field.getName())) {
                    try {
                        iAbilityShell = (IAbilityShell) AccessController.doPrivileged(new PrivilegedExceptionAction(field, obj) {
                            /* class ohos.abilityshell.utils.$$Lambda$AbilityContextUtils$ExOIfY4JNl46Zw_Ilk3cnYB07JE */
                            private final /* synthetic */ Field f$0;
                            private final /* synthetic */ Object f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            @Override // java.security.PrivilegedExceptionAction
                            public final Object run() {
                                return AbilityContextUtils.lambda$getAbilityContext$0(this.f$0, this.f$1);
                            }
                        });
                    } catch (PrivilegedActionException e) {
                        AppLog.w(SHELL_LABEL, "AbilityContextUtils::getAbilityContext occur exception %{public}s", e.getException().getMessage());
                    }
                }
            }
            return iAbilityShell;
        } catch (SecurityException unused) {
            AppLog.e(SHELL_LABEL, "AbilityContextUtils::getAbilityContext getDeclaredFields failed", new Object[0]);
            return null;
        }
    }

    static /* synthetic */ IAbilityShell lambda$getAbilityContext$0(Field field, Object obj) throws Exception {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object obj2 = field.get(obj);
        IAbilityShell iAbilityShell = obj2 instanceof IAbilityShell ? (IAbilityShell) obj2 : null;
        field.setAccessible(false);
        return iAbilityShell;
    }

    public static Object getAndroidContext(Object obj) {
        if (!(obj instanceof AbilityContext)) {
            AppLog.e(SHELL_LABEL, "AbilityContextUtils::getAndroidContext object not AbilityContext type", new Object[0]);
            return null;
        }
        boolean isDeviceEncryptedStorage = ((AbilityContext) obj).isDeviceEncryptedStorage();
        try {
            Field[] declaredFields = AbilityContext.class.getDeclaredFields();
            Context context = null;
            for (Field field : declaredFields) {
                if (SHELL_FIELD_NAME.equals(field.getName())) {
                    try {
                        context = (Context) AccessController.doPrivileged(new PrivilegedExceptionAction(field, obj, isDeviceEncryptedStorage, false) {
                            /* class ohos.abilityshell.utils.$$Lambda$AbilityContextUtils$p_1nVoDFTqV5Uh90lP0O_0d6og */
                            private final /* synthetic */ Field f$0;
                            private final /* synthetic */ Object f$1;
                            private final /* synthetic */ boolean f$2;
                            private final /* synthetic */ boolean f$3;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            @Override // java.security.PrivilegedExceptionAction
                            public final Object run() {
                                return AbilityContextUtils.lambda$getAndroidContext$1(this.f$0, this.f$1, this.f$2, this.f$3);
                            }
                        });
                    } catch (PrivilegedActionException e) {
                        AppLog.w(SHELL_LABEL, "AbilityContextUtils::getAndroidContext occur exception %{public}s", e.getException().getMessage());
                    }
                }
            }
            return context;
        } catch (SecurityException unused) {
            AppLog.e(SHELL_LABEL, "AbilityContextUtils::getAndroidContext getDeclaredFields failed", new Object[0]);
            return null;
        }
    }

    static /* synthetic */ Context lambda$getAndroidContext$1(Field field, Object obj, boolean z, boolean z2) throws Exception {
        Context context;
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object obj2 = field.get(obj);
        Context context2 = null;
        if (obj2 instanceof Context) {
            context = (Context) obj2;
        } else if (obj2 instanceof ContentProvider) {
            context = ((ContentProvider) obj2).getContext();
        } else {
            AppLog.w(SHELL_LABEL, "AbilityContextUtils::context type is wrong", new Object[0]);
            context = null;
        }
        if (context != null) {
            if (z) {
                context2 = context.createDeviceProtectedStorageContext();
            } else {
                context2 = context.createCredentialProtectedStorageContext();
            }
        }
        field.setAccessible(z2);
        return context2;
    }
}
