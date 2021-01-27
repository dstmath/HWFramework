package ohos.bundlemgr.freeinstall;

import java.lang.Thread;
import ohos.appexecfwk.utils.AppLog;

/* renamed from: ohos.bundlemgr.freeinstall.-$$Lambda$ErrorReminder$OTUnrn4KXUv-CNsb1otIeNL309Q  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ErrorReminder$OTUnrn4KXUvCNsb1otIeNL309Q implements Thread.UncaughtExceptionHandler {
    public static final /* synthetic */ $$Lambda$ErrorReminder$OTUnrn4KXUvCNsb1otIeNL309Q INSTANCE = new $$Lambda$ErrorReminder$OTUnrn4KXUvCNsb1otIeNL309Q();

    private /* synthetic */ $$Lambda$ErrorReminder$OTUnrn4KXUvCNsb1otIeNL309Q() {
    }

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public final void uncaughtException(Thread thread, Throwable th) {
        AppLog.e(ErrorReminder.ERROR_REMINDER, "thread %{public}s exception %{public}s", thread.getName(), th.getMessage());
    }
}
