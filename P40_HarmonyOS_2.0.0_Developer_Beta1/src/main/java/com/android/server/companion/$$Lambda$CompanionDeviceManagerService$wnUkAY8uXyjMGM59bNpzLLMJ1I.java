package com.android.server.companion;

import android.content.pm.PackageInfo;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.companion.-$$Lambda$CompanionDeviceManagerService$wnUkAY8uXyjMGM59-bNpzLLMJ1I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$CompanionDeviceManagerService$wnUkAY8uXyjMGM59bNpzLLMJ1I implements BiConsumer {
    public static final /* synthetic */ $$Lambda$CompanionDeviceManagerService$wnUkAY8uXyjMGM59bNpzLLMJ1I INSTANCE = new $$Lambda$CompanionDeviceManagerService$wnUkAY8uXyjMGM59bNpzLLMJ1I();

    private /* synthetic */ $$Lambda$CompanionDeviceManagerService$wnUkAY8uXyjMGM59bNpzLLMJ1I() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((CompanionDeviceManagerService) obj).updateSpecialAccessPermissionAsSystem((PackageInfo) obj2);
    }
}
