package android.database.sqlite;

import android.annotation.UnsupportedAppUsage;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SQLiteDatabaseConfiguration {
    private static final Pattern EMAIL_IN_DB_PATTERN = Pattern.compile("[\\w\\.\\-]+@[\\w\\.\\-]+");
    public static final String MEMORY_DB_PATH = ":memory:";
    public boolean configurationEnhancement;
    public final ArrayList<SQLiteCustomFunction> customFunctions = new ArrayList<>();
    public boolean defaultWALEnabled;
    public boolean explicitWALEnabled;
    public boolean foreignKeyConstraintsEnabled;
    public long idleConnectionTimeoutMs = Long.MAX_VALUE;
    public String journalMode;
    public final String label;
    public Locale locale;
    public int lookasideSlotCount = -1;
    public int lookasideSlotSize = -1;
    @UnsupportedAppUsage
    public int maxSqlCacheSize;
    public int openFlags;
    public final String path;
    public String syncMode;

    public SQLiteDatabaseConfiguration(String path2, int openFlags2) {
        if (path2 != null) {
            this.path = path2;
            this.label = stripPathForLogs(path2);
            this.openFlags = openFlags2;
            this.maxSqlCacheSize = 25;
            this.locale = Locale.getDefault();
            initConfigEnhancementFlag();
            if (!this.configurationEnhancement) {
                return;
            }
            if ((openFlags2 & 536870912) == 0) {
                this.openFlags = 536870912 | this.openFlags;
                this.defaultWALEnabled = true;
                this.explicitWALEnabled = false;
                return;
            }
            this.defaultWALEnabled = false;
            this.explicitWALEnabled = true;
            return;
        }
        throw new IllegalArgumentException("path must not be null.");
    }

    public SQLiteDatabaseConfiguration(SQLiteDatabaseConfiguration other) {
        if (other != null) {
            this.path = other.path;
            this.label = other.label;
            updateParametersFrom(other);
            this.configurationEnhancement = other.configurationEnhancement;
            this.defaultWALEnabled = other.defaultWALEnabled;
            this.explicitWALEnabled = other.explicitWALEnabled;
            return;
        }
        throw new IllegalArgumentException("other must not be null.");
    }

    public void updateParametersFrom(SQLiteDatabaseConfiguration other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null.");
        } else if (this.path.equals(other.path)) {
            this.openFlags = other.openFlags;
            this.maxSqlCacheSize = other.maxSqlCacheSize;
            this.locale = other.locale;
            this.foreignKeyConstraintsEnabled = other.foreignKeyConstraintsEnabled;
            this.customFunctions.clear();
            this.customFunctions.addAll(other.customFunctions);
            this.lookasideSlotSize = other.lookasideSlotSize;
            this.lookasideSlotCount = other.lookasideSlotCount;
            this.idleConnectionTimeoutMs = other.idleConnectionTimeoutMs;
            this.journalMode = other.journalMode;
            this.syncMode = other.syncMode;
            this.configurationEnhancement = other.configurationEnhancement;
            this.defaultWALEnabled = other.defaultWALEnabled;
            this.explicitWALEnabled = other.explicitWALEnabled;
        } else {
            throw new IllegalArgumentException("other configuration must refer to the same database.");
        }
    }

    public boolean isInMemoryDb() {
        return this.path.equalsIgnoreCase(MEMORY_DB_PATH);
    }

    /* access modifiers changed from: package-private */
    public boolean isLegacyCompatibilityWalEnabled() {
        return this.journalMode == null && this.syncMode == null && (this.openFlags & Integer.MIN_VALUE) != 0;
    }

    private static String stripPathForLogs(String path2) {
        if (path2.indexOf(64) == -1) {
            return path2;
        }
        return EMAIL_IN_DB_PATTERN.matcher(path2).replaceAll("XX@YY");
    }

    /* access modifiers changed from: package-private */
    public boolean isLookasideConfigSet() {
        return this.lookasideSlotCount >= 0 && this.lookasideSlotSize >= 0;
    }

    public void initConfigEnhancementFlag() {
        String optimizeDbConfigFlag = SystemProperties.get("ro.config.hw_OptiDBConfig");
        if (optimizeDbConfigFlag == null) {
            this.configurationEnhancement = false;
        } else if (!"true".equalsIgnoreCase(optimizeDbConfigFlag) || (!"/data/user/0/com.android.providers.contacts/databases/contacts2.db".equalsIgnoreCase(this.path) && !"/data/user/0/com.android.providers.telephony/databases/mmssms.db".equalsIgnoreCase(this.path))) {
            this.configurationEnhancement = false;
        } else {
            this.configurationEnhancement = true;
        }
    }
}
