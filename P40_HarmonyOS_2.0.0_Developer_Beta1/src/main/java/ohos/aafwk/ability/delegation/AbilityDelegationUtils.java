package ohos.aafwk.ability.delegation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.AbilitySliceLifecycleExecutor;
import ohos.aafwk.ability.AbilitySliceManager;
import ohos.aafwk.ability.AbilitySliceScheduler;
import ohos.aafwk.ability.AbilitySliceStack;
import ohos.aafwk.content.Intent;
import ohos.app.AbilityContext;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.TouchEvent;

class AbilityDelegationUtils {
    private AbilityDelegationUtils() {
    }

    /* access modifiers changed from: package-private */
    public enum LifecycleAction {
        START("start"),
        INACTIVE("inactive"),
        ACTIVE("active"),
        BACKGROUND("background"),
        FOREGROUND("foreground"),
        STOP("stop");
        
        String value;

        private LifecycleAction(String str) {
            this.value = str;
        }

        public String getValue() {
            return this.value;
        }
    }

    static boolean reflectDispatchKeyEvent(Ability ability, KeyEvent keyEvent) {
        try {
            Method declaredMethod = Ability.class.getDeclaredMethod("dispatchKeyEvent", KeyEvent.class);
            AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$QyhRfrC10LK2uT7LBql5Cn4qUyU */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectDispatchKeyEvent$0(this.f$0);
                }
            });
            Object invoke = declaredMethod.invoke(ability, keyEvent);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
            return false;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("reflect dispatchKeyEvent failed: " + e);
        }
    }

    static void reflectDispatchBackKey(Ability ability) {
        try {
            Method declaredMethod = Ability.class.getDeclaredMethod("notifyBackKeyPressed", new Class[0]);
            AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$umfpocLkJHWRI9yJY9urB1WqMQI */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectDispatchBackKey$1(this.f$0);
                }
            });
            declaredMethod.invoke(ability, new Object[0]);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("reflect notifyBackKeyPressed failed: " + e);
        }
    }

    static /* synthetic */ Object lambda$reflectDispatchBackKey$1(Method method) {
        method.setAccessible(true);
        return null;
    }

    static boolean reflectDispatchTouchEvent(Ability ability, TouchEvent touchEvent) {
        try {
            Method declaredMethod = Ability.class.getDeclaredMethod("dispatchTouchEvent", TouchEvent.class);
            AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$OLbNIQjinhMIsD_7AcU1oSvItZk */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectDispatchTouchEvent$2(this.f$0);
                }
            });
            Object invoke = declaredMethod.invoke(ability, touchEvent);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
            return false;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("reflect dispatchTouchEvent failed: " + e);
        }
    }

    static AbilitySliceManager reflectAbilitySliceManager(Ability ability) {
        try {
            Field declaredField = Ability.class.getDeclaredField("abilitySliceManager");
            AccessController.doPrivileged(new PrivilegedAction(declaredField) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$aSbqE8qarSlkQ75fPTBUmChknMc */
                private final /* synthetic */ Field f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectAbilitySliceManager$3(this.f$0);
                }
            });
            Object obj = declaredField.get(ability);
            if (obj instanceof AbilitySliceManager) {
                return (AbilitySliceManager) obj;
            }
            return null;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("reflect AbilitySliceManager failed: " + e);
        }
    }

    static AbilitySliceScheduler reflectAbilitySliceScheduler(Ability ability) {
        try {
            Field declaredField = AbilitySliceManager.class.getDeclaredField("abilitySliceScheduler");
            AccessController.doPrivileged(new PrivilegedAction(declaredField) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$YN6sJn6Km2NXLf8hKB6G4rEpwY */
                private final /* synthetic */ Field f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectAbilitySliceScheduler$4(this.f$0);
                }
            });
            Object obj = declaredField.get(reflectAbilitySliceManager(ability));
            if (obj instanceof AbilitySliceScheduler) {
                return (AbilitySliceScheduler) obj;
            }
            return null;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("reflect AbilitySliceScheduler failed: " + e);
        }
    }

    static AbilitySliceStack reflectAbilitySliceStack(Ability ability) {
        try {
            Field declaredField = AbilitySliceScheduler.class.getDeclaredField("abilitySliceStack");
            AccessController.doPrivileged(new PrivilegedAction(declaredField) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$YLisGa6Po2cQjn0jWu84uB67lWQ */
                private final /* synthetic */ Field f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectAbilitySliceStack$5(this.f$0);
                }
            });
            Object obj = declaredField.get(reflectAbilitySliceScheduler(ability));
            if (obj instanceof AbilitySliceStack) {
                return (AbilitySliceStack) obj;
            }
            return null;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("reflect AbilitySliceStack failed: " + e);
        }
    }

    static AbilitySlice reflectTopAbilitySlice(Ability ability) {
        try {
            Field declaredField = AbilitySliceScheduler.class.getDeclaredField("topAbilitySlice");
            AccessController.doPrivileged(new PrivilegedAction(declaredField) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$BzKEujSTOUVCxWEX2ww1Z6t24c4 */
                private final /* synthetic */ Field f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectTopAbilitySlice$6(this.f$0);
                }
            });
            Object obj = declaredField.get(reflectAbilitySliceScheduler(ability));
            if (obj instanceof AbilitySlice) {
                return (AbilitySlice) obj;
            }
            return null;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("reflect topAbilitySliceImpl failed: " + e);
        }
    }

    static void reflectSliceLifecycle(AbilitySlice abilitySlice, Intent intent, LifecycleAction lifecycleAction) {
        String value = lifecycleAction.getValue();
        if (lifecycleAction == LifecycleAction.START || lifecycleAction == LifecycleAction.FOREGROUND) {
            try {
                Method declaredMethod = AbilitySlice.class.getDeclaredMethod(value, Intent.class);
                AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                    /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$4yLUIEnSUKetg_V6ZmlC8qH7itA */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return AbilityDelegationUtils.lambda$reflectSliceLifecycle$7(this.f$0);
                    }
                });
                declaredMethod.invoke(abilitySlice, intent);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException("reflect abilitySlice " + value + " failed: " + e);
            }
        } else {
            try {
                Method declaredMethod2 = AbilitySlice.class.getDeclaredMethod(value, new Class[0]);
                AccessController.doPrivileged(new PrivilegedAction(declaredMethod2) {
                    /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$gMyJMjeDGa9wzVeBqYjgIqIXlA */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return AbilityDelegationUtils.lambda$reflectSliceLifecycle$8(this.f$0);
                    }
                });
                declaredMethod2.invoke(abilitySlice, new Object[0]);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e2) {
                throw new IllegalStateException("reflect abilitySlice " + value + " failed: " + e2);
            }
        }
    }

    static /* synthetic */ Object lambda$reflectSliceLifecycle$8(Method method) {
        method.setAccessible(true);
        return null;
    }

    static int reflectGetSliceState(AbilitySlice abilitySlice) {
        try {
            Method declaredMethod = AbilitySlice.class.getDeclaredMethod("getState", new Class[0]);
            AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$07n2COWoSATt2rwUMUQvKvaGfA */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectGetSliceState$9(this.f$0);
                }
            });
            Object invoke = declaredMethod.invoke(abilitySlice, new Object[0]);
            if (invoke instanceof AbilitySliceLifecycleExecutor.LifecycleState) {
                return ((AbilitySliceLifecycleExecutor.LifecycleState) invoke).getValue();
            }
            return -1;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("reflect abilitySlice getState failed: " + e);
        }
    }

    private static List<AbilitySlice> cast2SliceList(Object obj) {
        try {
            if (obj instanceof List) {
                return (List) obj;
            }
            return new ArrayList();
        } catch (ClassCastException unused) {
            return new ArrayList();
        }
    }

    static List<AbilitySlice> reflectGetAllSlices(AbilitySliceStack abilitySliceStack) {
        try {
            Method declaredMethod = AbilitySliceStack.class.getDeclaredMethod("getAllSlices", new Class[0]);
            AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$Jk924liJeaAWIELpaY8UQ2fgrM8 */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectGetAllSlices$10(this.f$0);
                }
            });
            return cast2SliceList(declaredMethod.invoke(abilitySliceStack, new Object[0]));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("reflect abilitySliceStack getAllSlices failed: " + e);
        }
    }

    static void reflectAbilityLifecycle(Ability ability, Intent intent, LifecycleAction lifecycleAction) {
        String value = lifecycleAction.getValue();
        if (lifecycleAction == LifecycleAction.START || lifecycleAction == LifecycleAction.FOREGROUND || lifecycleAction == LifecycleAction.ACTIVE) {
            try {
                Method declaredMethod = Ability.class.getDeclaredMethod(value, Intent.class);
                AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                    /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$0JWD4izIoNJBd9ssry3jyXMRMnw */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return AbilityDelegationUtils.lambda$reflectAbilityLifecycle$11(this.f$0);
                    }
                });
                declaredMethod.invoke(ability, intent);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException("reflect ability " + value + " failed: " + e);
            }
        } else {
            try {
                Method declaredMethod2 = Ability.class.getDeclaredMethod(value, new Class[0]);
                AccessController.doPrivileged(new PrivilegedAction(declaredMethod2) {
                    /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$kbj9bbAiYIE7dLTnzdhOE09KVgM */
                    private final /* synthetic */ Method f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return AbilityDelegationUtils.lambda$reflectAbilityLifecycle$12(this.f$0);
                    }
                });
                declaredMethod2.invoke(ability, new Object[0]);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e2) {
                throw new IllegalStateException("reflect ability " + value + " failed: " + e2);
            }
        }
    }

    static /* synthetic */ Object lambda$reflectAbilityLifecycle$12(Method method) {
        method.setAccessible(true);
        return null;
    }

    static int reflectGetAbilityState(Ability ability) {
        try {
            Method declaredMethod = Ability.class.getDeclaredMethod("getCurrentState", new Class[0]);
            AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$R6vycf9tNkyfT1ku2vICLSgh3KE */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectGetAbilityState$13(this.f$0);
                }
            });
            Object invoke = declaredMethod.invoke(ability, new Object[0]);
            if (invoke instanceof Integer) {
                return ((Integer) invoke).intValue();
            }
            throw new IllegalStateException("get ability lifecycle state failed.");
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("get ability lifecycle state failed: " + e);
        }
    }

    static Object reflectGetTopAbility(Ability ability) {
        try {
            Method method = AbilityContext.class.getMethod("getContext", new Class[0]);
            AccessController.doPrivileged(new PrivilegedAction(method) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$MhjweV9m2xKmeZsyHd9LGB81SAQ */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectGetTopAbility$14(this.f$0);
                }
            });
            Object invoke = method.invoke(ability, new Object[0]);
            Method declaredMethod = invoke.getClass().getDeclaredMethod("getTopAbility", new Class[0]);
            AccessController.doPrivileged(new PrivilegedAction(declaredMethod) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationUtils$N_ZupQG2HNYldaKQFezASR6Hkpg */
                private final /* synthetic */ Method f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return AbilityDelegationUtils.lambda$reflectGetTopAbility$15(this.f$0);
                }
            });
            return declaredMethod.invoke(invoke, new Object[0]);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("get top ability failed: " + e);
        }
    }
}
