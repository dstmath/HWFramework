package ohos.data.rdb.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.app.Context;
import ohos.data.DatabaseFileConfig;
import ohos.data.DatabaseFileType;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SqliteDatabaseUtils {
    private static final Object CREATE_DIR_LOCK = new Object();
    public static final int DATA_TYPE_BLOB = 4;
    public static final int DATA_TYPE_BOOLEAN = 5;
    public static final int DATA_TYPE_FLOAT = 2;
    public static final int DATA_TYPE_INTEGER = 1;
    public static final int DATA_TYPE_NULL = 0;
    public static final int DATA_TYPE_STRING = 3;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "SqliteDatabaseUtils");
    public static final int SQL_FIRST_CHARACTER = 3;
    public static final int STATEMENT_ATTACH = 3;
    public static final int STATEMENT_BEGIN = 5;
    public static final int STATEMENT_COMMIT = 6;
    public static final int STATEMENT_DDL = 9;
    public static final int STATEMENT_DETACH = 4;
    public static final int STATEMENT_OTHER = 99;
    public static final int STATEMENT_PRAGMA = 8;
    public static final int STATEMENT_ROLLBACK = 7;
    public static final int STATEMENT_SELECT = 1;
    private static final Map<String, Integer> STATEMENT_TYPE = new HashMap(20);
    public static final int STATEMENT_UPDATE = 2;

    static {
        STATEMENT_TYPE.put("SEL", 1);
        STATEMENT_TYPE.put("INS", 2);
        STATEMENT_TYPE.put("UPD", 2);
        STATEMENT_TYPE.put("REP", 2);
        STATEMENT_TYPE.put("DEL", 2);
        STATEMENT_TYPE.put("ATT", 3);
        STATEMENT_TYPE.put("DET", 4);
        STATEMENT_TYPE.put("COM", 6);
        STATEMENT_TYPE.put("END", 6);
        STATEMENT_TYPE.put("ROL", 7);
        STATEMENT_TYPE.put("BEG", 5);
        STATEMENT_TYPE.put("PRA", 8);
        STATEMENT_TYPE.put("CRE", 9);
        STATEMENT_TYPE.put("DRO", 9);
        STATEMENT_TYPE.put("ALT", 9);
    }

    private SqliteDatabaseUtils() {
    }

    public static int getSqlStatementType(String str) {
        Integer num;
        if (str == null) {
            return 99;
        }
        String trim = str.trim();
        if (trim.length() >= 3 && (num = STATEMENT_TYPE.get(trim.substring(0, 3).toUpperCase(Locale.ROOT))) != null) {
            return num.intValue();
        }
        return 99;
    }

    public static int getObjectType(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof byte[]) {
            return 4;
        }
        if ((obj instanceof Float) || (obj instanceof Double)) {
            return 2;
        }
        if ((obj instanceof Long) || (obj instanceof Integer) || (obj instanceof Short) || (obj instanceof Byte)) {
            return 1;
        }
        return obj instanceof Boolean ? 5 : 3;
    }

    public static boolean checkDbFileName(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("The database name is null or the length is 0. ");
        }
        File file = new File(str);
        if (file.isDirectory()) {
            throw new IllegalArgumentException("The databaseName is directory.");
        } else if (!file.exists()) {
            return true;
        } else {
            if (file.canWrite() && file.canRead()) {
                return true;
            }
            throw new IllegalArgumentException("The databaseName can not write or read.");
        }
    }

    public static void deleteFile(String str) {
        File file = new File(str);
        if (!file.exists()) {
            HiLog.info(LABEL, "File %{private}s does not exist", new Object[]{str});
        } else if (file.delete()) {
            HiLog.info(LABEL, "FileName= %{private}s has been deleted", new Object[]{str});
        } else {
            HiLog.info(LABEL, "Failed to delete File %{private}s", new Object[]{str});
        }
    }

    public static boolean renameFile(String str, String str2) {
        File file = new File(str);
        if (!file.exists()) {
            HiLog.info(LABEL, "File %{private}s does not exist", new Object[]{str});
            return false;
        } else if (!file.renameTo(new File(str2))) {
            return false;
        } else {
            HiLog.info(LABEL, "Rename oldFileName = %{private}s to newFileName  %{private}s", new Object[]{str, str2});
            return true;
        }
    }

    public static void copyFile(String str, String str2) throws IOException {
        File file = new File(str);
        if (file.exists()) {
            Files.copy(file.toPath(), new File(str2).toPath(), new CopyOption[0]);
        } else {
            HiLog.info(LABEL, "File %{private}s does not exist", new Object[]{str});
            throw new IllegalArgumentException("File does not exist.");
        }
    }

    public static File getDefaultDatabasePath(Context context, String str) {
        if (str == null || "".equals(str)) {
            throw new IllegalArgumentException("name should not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("context should not be null");
        } else if (!str.contains(File.separator)) {
            File databaseDir = context.getDatabaseDir();
            if (databaseDir != null) {
                File file = new File(databaseDir, "db");
                synchronized (CREATE_DIR_LOCK) {
                    if (!file.exists()) {
                        if (!file.mkdir()) {
                            throw new IllegalStateException("Create database dir failed.");
                        }
                    }
                }
                return new File(file, str);
            }
            throw new IllegalArgumentException("Database dir in context is null ");
        } else {
            throw new IllegalArgumentException("Incorrect database name.");
        }
    }

    public static File getCorruptPath(String str) {
        File file = new File(str);
        String name = file.getName();
        File parentFile = file.getParentFile();
        String name2 = parentFile.getName();
        String parent = parentFile.getParent();
        boolean z = true;
        if (name2.split("_").length <= 1) {
            z = false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(DatabaseFileType.CORRUPT.getValue());
        sb.append(z ? "_encrypt" : "");
        File file2 = new File(parent, sb.toString());
        synchronized (CREATE_DIR_LOCK) {
            if (!file2.exists()) {
                if (!file2.mkdir()) {
                    throw new IllegalStateException("Create database dir failed.");
                }
            }
        }
        return new File(file2, name);
    }

    public static File getDatabasePath(Context context, DatabaseFileConfig databaseFileConfig) {
        if (context == null) {
            throw new IllegalArgumentException("context should not be null.");
        } else if (databaseFileConfig != null) {
            String name = databaseFileConfig.getName();
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name should not be null.");
            } else if (!name.contains(File.separator)) {
                File databaseDir = context.getDatabaseDir();
                if (databaseDir != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(databaseFileConfig.getDatabaseFileType().getValue());
                    sb.append(databaseFileConfig.isEncrypted() ? "_encrypt" : "");
                    File file = new File(databaseDir, sb.toString());
                    synchronized (CREATE_DIR_LOCK) {
                        if (!file.exists()) {
                            if (!file.mkdir()) {
                                throw new IllegalStateException("Create database dir failed.");
                            }
                        }
                    }
                    return new File(file, name);
                }
                throw new IllegalArgumentException("Path in context is null.");
            } else {
                throw new IllegalArgumentException("Incorrect database name.");
            }
        } else {
            throw new IllegalArgumentException("StoreConfig should not be null.");
        }
    }
}
