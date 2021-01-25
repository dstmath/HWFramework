package ohos.abilityshell.delegation;

import java.util.concurrent.Callable;
import ohos.abilityshell.HarmonyApplication;

/* renamed from: ohos.abilityshell.delegation.-$$Lambda$AbilityDelegator$ASxK1JHnJqBblNqz5yfXS-_KLkI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AbilityDelegator$ASxK1JHnJqBblNqz5yfXS_KLkI implements Callable {
    public static final /* synthetic */ $$Lambda$AbilityDelegator$ASxK1JHnJqBblNqz5yfXS_KLkI INSTANCE = new $$Lambda$AbilityDelegator$ASxK1JHnJqBblNqz5yfXS_KLkI();

    private /* synthetic */ $$Lambda$AbilityDelegator$ASxK1JHnJqBblNqz5yfXS_KLkI() {
    }

    @Override // java.util.concurrent.Callable
    public final Object call() {
        return HarmonyApplication.getInstance().getTopAbility();
    }
}
