package ohos.bundlemgr;

import java.lang.Thread;

/* renamed from: ohos.bundlemgr.-$$Lambda$BundleMgrAdapter$QIYKWrHtLNRnhj26XwNPuGJOl9o  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BundleMgrAdapter$QIYKWrHtLNRnhj26XwNPuGJOl9o implements Thread.UncaughtExceptionHandler {
    public static final /* synthetic */ $$Lambda$BundleMgrAdapter$QIYKWrHtLNRnhj26XwNPuGJOl9o INSTANCE = new $$Lambda$BundleMgrAdapter$QIYKWrHtLNRnhj26XwNPuGJOl9o();

    private /* synthetic */ $$Lambda$BundleMgrAdapter$QIYKWrHtLNRnhj26XwNPuGJOl9o() {
    }

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public final void uncaughtException(Thread thread, Throwable th) {
        BundleMgrAdapter.lambda$startAbilityWithParam$0(thread, th);
    }
}
