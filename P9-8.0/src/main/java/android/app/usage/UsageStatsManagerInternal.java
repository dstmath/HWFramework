package android.app.usage;

import android.content.ComponentName;
import android.content.res.Configuration;
import java.util.List;

public abstract class UsageStatsManagerInternal {

    public static abstract class AppIdleStateChangeListener {
        public abstract void onAppIdleStateChanged(String str, int i, boolean z);

        public abstract void onParoleStateChanged(boolean z);
    }

    public abstract void addAppIdleStateChangeListener(AppIdleStateChangeListener appIdleStateChangeListener);

    public abstract void applyRestoredPayload(int i, String str, byte[] bArr);

    public abstract byte[] getBackupPayload(int i, String str);

    public abstract int[] getIdleUidsForUser(int i);

    public abstract boolean isAppIdle(String str, int i, int i2);

    public abstract boolean isAppIdleParoleOn();

    public abstract void prepareShutdown();

    public abstract List<UsageStats> queryUsageStatsForUser(int i, int i2, long j, long j2, boolean z);

    public abstract void removeAppIdleStateChangeListener(AppIdleStateChangeListener appIdleStateChangeListener);

    public abstract void reportConfigurationChange(Configuration configuration, int i);

    public abstract void reportContentProviderUsage(String str, String str2, int i);

    public abstract void reportEvent(ComponentName componentName, int i, int i2);

    public abstract void reportEvent(ComponentName componentName, int i, int i2, int i3);

    public abstract void reportEvent(String str, int i, int i2);

    public abstract void reportShortcutUsage(String str, String str2, int i);
}
