package com.android.server.wm;

import android.os.SystemProperties;
import android.util.ArraySet;
import com.android.internal.annotations.VisibleForTesting;

/* access modifiers changed from: package-private */
public class HighRefreshRateBlacklist {
    private static final int MAX_ENTRIES = 50;
    private static final String SYSPROP_KEY = "ro.window_manager.high_refresh_rate_blacklist";
    private static final String SYSPROP_KEY_ENTRY_SUFFIX = "_entry";
    private static final String SYSPROP_KEY_LENGTH_SUFFIX = "_length";
    private ArraySet<String> mBlacklistedPackages = new ArraySet<>();

    /* access modifiers changed from: package-private */
    public interface SystemPropertyGetter {
        String get(String str);

        int getInt(String str, int i);
    }

    static HighRefreshRateBlacklist create() {
        return new HighRefreshRateBlacklist(new SystemPropertyGetter() {
            /* class com.android.server.wm.HighRefreshRateBlacklist.AnonymousClass1 */

            @Override // com.android.server.wm.HighRefreshRateBlacklist.SystemPropertyGetter
            public int getInt(String key, int def) {
                return SystemProperties.getInt(key, def);
            }

            @Override // com.android.server.wm.HighRefreshRateBlacklist.SystemPropertyGetter
            public String get(String key) {
                return SystemProperties.get(key);
            }
        });
    }

    @VisibleForTesting
    HighRefreshRateBlacklist(SystemPropertyGetter propertyGetter) {
        int length = Math.min(propertyGetter.getInt("ro.window_manager.high_refresh_rate_blacklist_length", 0), (int) MAX_ENTRIES);
        for (int i = 1; i <= length; i++) {
            String packageName = propertyGetter.get("ro.window_manager.high_refresh_rate_blacklist_entry" + i);
            if (!packageName.isEmpty()) {
                this.mBlacklistedPackages.add(packageName);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isBlacklisted(String packageName) {
        return this.mBlacklistedPackages.contains(packageName);
    }
}
