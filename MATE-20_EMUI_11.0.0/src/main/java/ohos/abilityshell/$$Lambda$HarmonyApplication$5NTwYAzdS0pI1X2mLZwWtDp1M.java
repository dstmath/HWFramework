package ohos.abilityshell;

import java.lang.Thread;
import ohos.appexecfwk.utils.AppLog;

/* renamed from: ohos.abilityshell.-$$Lambda$HarmonyApplication$5NTwYAzdS0pI1X2m-LZwWtDp-1M  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HarmonyApplication$5NTwYAzdS0pI1X2mLZwWtDp1M implements Thread.UncaughtExceptionHandler {
    public static final /* synthetic */ $$Lambda$HarmonyApplication$5NTwYAzdS0pI1X2mLZwWtDp1M INSTANCE = new $$Lambda$HarmonyApplication$5NTwYAzdS0pI1X2mLZwWtDp1M();

    private /* synthetic */ $$Lambda$HarmonyApplication$5NTwYAzdS0pI1X2mLZwWtDp1M() {
    }

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public final void uncaughtException(Thread thread, Throwable th) {
        AppLog.e(HarmonyApplication.SHELL_LABEL, "create application run exception : %{private}s", th.getMessage());
    }
}
