package android.os;

import com.android.internal.os.BinderInternal;

/* renamed from: android.os.-$$Lambda$Binder$IYUHVkWouPK_9CG2s8VwyWBt5_I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Binder$IYUHVkWouPK_9CG2s8VwyWBt5_I implements BinderInternal.WorkSourceProvider {
    public static final /* synthetic */ $$Lambda$Binder$IYUHVkWouPK_9CG2s8VwyWBt5_I INSTANCE = new $$Lambda$Binder$IYUHVkWouPK_9CG2s8VwyWBt5_I();

    private /* synthetic */ $$Lambda$Binder$IYUHVkWouPK_9CG2s8VwyWBt5_I() {
    }

    @Override // com.android.internal.os.BinderInternal.WorkSourceProvider
    public final int resolveWorkSourceUid(int i) {
        return Binder.getCallingUid();
    }
}
