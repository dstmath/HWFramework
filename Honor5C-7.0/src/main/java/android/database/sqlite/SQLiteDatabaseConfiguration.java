package android.database.sqlite;

import android.os.SystemProperties;
import android.security.keymaster.KeymasterDefs;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SQLiteDatabaseConfiguration {
    private static final Pattern EMAIL_IN_DB_PATTERN = null;
    public static final String MEMORY_DB_PATH = ":memory:";
    public boolean configurationEnhancement;
    public final ArrayList<SQLiteCustomFunction> customFunctions;
    public boolean defaultWALEnabled;
    public boolean explicitWALEnabled;
    public boolean foreignKeyConstraintsEnabled;
    public final String label;
    public Locale locale;
    public int maxSqlCacheSize;
    public int openFlags;
    public final String path;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.sqlite.SQLiteDatabaseConfiguration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.sqlite.SQLiteDatabaseConfiguration.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.database.sqlite.SQLiteDatabaseConfiguration.<clinit>():void");
    }

    public SQLiteDatabaseConfiguration(String path, int openFlags) {
        this.customFunctions = new ArrayList();
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
        if ((openFlags & KeymasterDefs.KM_ENUM_REP) == 0) {
            this.openFlags |= KeymasterDefs.KM_ENUM_REP;
            this.defaultWALEnabled = true;
            this.explicitWALEnabled = false;
            return;
        }
        this.defaultWALEnabled = false;
        this.explicitWALEnabled = true;
    }

    public SQLiteDatabaseConfiguration(SQLiteDatabaseConfiguration other) {
        this.customFunctions = new ArrayList();
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
        } else if (optimizeDBConfigFlag.equalsIgnoreCase("true")) {
            this.configurationEnhancement = true;
        } else {
            this.configurationEnhancement = false;
        }
    }
}
