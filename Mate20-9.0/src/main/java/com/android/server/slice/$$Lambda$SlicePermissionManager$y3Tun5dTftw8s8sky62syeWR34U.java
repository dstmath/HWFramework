package com.android.server.slice;

import com.android.server.slice.DirtyTracker;

/* renamed from: com.android.server.slice.-$$Lambda$SlicePermissionManager$y3Tun5dTftw8s8sky62syeWR34U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SlicePermissionManager$y3Tun5dTftw8s8sky62syeWR34U implements DirtyTracker {
    public static final /* synthetic */ $$Lambda$SlicePermissionManager$y3Tun5dTftw8s8sky62syeWR34U INSTANCE = new $$Lambda$SlicePermissionManager$y3Tun5dTftw8s8sky62syeWR34U();

    private /* synthetic */ $$Lambda$SlicePermissionManager$y3Tun5dTftw8s8sky62syeWR34U() {
    }

    public final void onPersistableDirty(DirtyTracker.Persistable persistable) {
        SlicePermissionManager.lambda$writeBackup$0(persistable);
    }
}
