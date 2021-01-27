package com.android.server.wifi;

import com.android.server.wifi.WifiConfigStore;
import java.util.function.Predicate;

/* renamed from: com.android.server.wifi.-$$Lambda$WifiConfigStore$E5gj7z5ed5kRI6vVgdfi0Qq87ak  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiConfigStore$E5gj7z5ed5kRI6vVgdfi0Qq87ak implements Predicate {
    public static final /* synthetic */ $$Lambda$WifiConfigStore$E5gj7z5ed5kRI6vVgdfi0Qq87ak INSTANCE = new $$Lambda$WifiConfigStore$E5gj7z5ed5kRI6vVgdfi0Qq87ak();

    private /* synthetic */ $$Lambda$WifiConfigStore$E5gj7z5ed5kRI6vVgdfi0Qq87ak() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((WifiConfigStore.StoreData) obj).hasNewDataToSerialize();
    }
}
