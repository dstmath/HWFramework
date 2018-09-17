package android.database.sqlite;

import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SQLiteDatabaseConfiguration {
    private static final Pattern EMAIL_IN_DB_PATTERN = Pattern.compile("[\\w\\.\\-]+@[\\w\\.\\-]+");
    public static final String MEMORY_DB_PATH = ":memory:";
    public boolean configurationEnhancement;
    public final ArrayList<SQLiteCustomFunction> customFunctions = new ArrayList();
    public boolean defaultWALEnabled;
    public boolean explicitWALEnabled;
    public boolean foreignKeyConstraintsEnabled;
    public final String label;
    public Locale locale;
    public int maxSqlCacheSize;
    public int openFlags;
    public final String path;

    public SQLiteDatabaseConfiguration(String path, int openFlags) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null.");
        }
        this.path = path;
        this.label = stripPathForLogs(path);
        this.openFlags = openFlags;
        this.maxSqlCacheSize = 25;
        this.locale = Locale.getDefault();
        initConfigEnhancementFlag();
        if (!this.configurationEnhancement) {
            return;
        }
        if ((openFlags & 536870912) == 0) {
            this.openFlags |= 536870912;
            this.defaultWALEnabled = true;
            this.explicitWALEnabled = false;
            return;
        }
        this.defaultWALEnabled = false;
        this.explicitWALEnabled = true;
    }

    public SQLiteDatabaseConfiguration(SQLiteDatabaseConfiguration other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null.");
        }
        this.path = other.path;
        this.label = other.label;
        updateParametersFrom(other);
        this.configurationEnhancement = other.configurationEnhancement;
        this.defaultWALEnabled = other.defaultWALEnabled;
        this.explicitWALEnabled = other.explicitWALEnabled;
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

    private static String stripPathForLogs(String path) {
        if (path.indexOf(64) == -1) {
            return path;
        }
        return EMAIL_IN_DB_PATTERN.matcher(path).replaceAll("XX@YY");
    }

    public void initConfigEnhancementFlag() {
        String optimizeDBConfigFlag = SystemProperties.get("ro.config.hw_OptiDBConfig");
        if (optimizeDBConfigFlag == null) {
            this.configurationEnhancement = false;
        } else if (optimizeDBConfigFlag.equalsIgnoreCase("true") && (this.path.equalsIgnoreCase("/data/user/0/com.android.providers.contacts/databases/contacts2.db") || this.path.equalsIgnoreCase("/data/user/0/com.android.providers.telephony/databases/mmssms.db"))) {
            this.configurationEnhancement = true;
        } else {
            this.configurationEnhancement = false;
        }
    }
}
