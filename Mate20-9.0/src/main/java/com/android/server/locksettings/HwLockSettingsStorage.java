package com.android.server.locksettings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.locksettings.LockSettingsStorage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Calendar;

class HwLockSettingsStorage extends LockSettingsStorage {
    private static final String LOCK_EXTEND_PASSWORD_FILE = "gatekeeper.extendpassword.key";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TABLE = "locksettings";
    private static final String TAG = "HwLockSettingsStorage";
    private FileLogger mFLogger = new FileLogger();
    private LockSettingsDbReport mLSSDbReport;

    private static class FileLogger {
        private static final String CHARSET_ASCII = "US-ASCII";
        private static String FILE_LOG_DIRECTORY = "/data/log/lss_log";
        private static final int MAX_DUMP_DEPTH = 2;
        private static final int MAX_FILE = 2;
        private static final String TAG = "FileLogger";
        private static final String log_file_prefix = "lock_settings_log_0";
        private static final long max_size = 524288;
        private final Calendar mCalendar = Calendar.getInstance();

        public FileLogger() {
            File pFile = new File(FILE_LOG_DIRECTORY);
            if (!pFile.exists() && !pFile.mkdirs()) {
                Slog.i(TAG, "create log directory fail:" + FILE_LOG_DIRECTORY);
            }
        }

        private String getFullFilePath(int idx) {
            return FILE_LOG_DIRECTORY + File.separator + log_file_prefix + Integer.toString(idx + 1) + ".log";
        }

        private RandomAccessFile getTargetFile() {
            try {
                File f0 = new File(getFullFilePath(0));
                File f1 = new File(getFullFilePath(1));
                File target = f0;
                if (f0.exists() && f1.exists()) {
                    target = f0.lastModified() > f1.lastModified() ? f0 : f1;
                }
                long size = target.length();
                if (size >= max_size) {
                    target = target == f0 ? f1 : f0;
                    size = 0;
                }
                RandomAccessFile raf = new RandomAccessFile(target.getCanonicalPath(), "rw");
                if (size == 0) {
                    raf.setLength(0);
                }
                raf.seek(size);
                return raf;
            } catch (FileNotFoundException e) {
                Log.w(TAG, "FileLogerFaile FileNotFoundException");
                closeSafely(null);
                return null;
            } catch (IOException e2) {
                Log.w(TAG, "FileLogerFaile IOException");
                closeSafely(null);
                return null;
            }
        }

        private void closeSafely(RandomAccessFile raf) {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    Log.w(TAG, "Close file fail");
                }
            }
        }

        private boolean write(byte[] content) {
            synchronized (this) {
                RandomAccessFile raf = getTargetFile();
                if (raf == null) {
                    Log.e(TAG, "check raf file fail." + FILE_LOG_DIRECTORY);
                    return false;
                }
                try {
                    raf.write(content);
                    closeSafely(raf);
                    return true;
                } catch (FileNotFoundException e) {
                    Log.w(TAG, "FileLogerFaile FileNotFoundException");
                    closeSafely(raf);
                    return false;
                } catch (IOException e2) {
                    try {
                        Log.w(TAG, "FileLogerFaile IOException");
                        return false;
                    } finally {
                        closeSafely(raf);
                    }
                }
            }
        }

        private boolean write(StringBuilder sb) {
            sb.append(System.lineSeparator());
            return write(getBytes(sb.toString(), CHARSET_ASCII));
        }

        private byte[] getBytes(String msg, String charSet) {
            try {
                return msg.getBytes(charSet);
            } catch (UnsupportedEncodingException e) {
                Log.w(TAG, "FileLogerFaile UnsupportedEncodingException");
                return msg.getBytes(Charset.defaultCharset());
            }
        }

        private static void intToString(StringBuilder sb, int digit, int num) {
            int i = 1;
            int base = 10;
            while (i < digit) {
                if (num < base) {
                    sb.append("0");
                }
                i++;
                base *= 10;
            }
            sb.append(num);
        }

        private StringBuilder makeBaseInfo(StringBuilder sb, String tag) {
            long millis = System.currentTimeMillis();
            synchronized (this.mCalendar) {
                this.mCalendar.setTimeInMillis(millis);
                intToString(sb, 2, this.mCalendar.get(2) + 1);
                sb.append('-');
                intToString(sb, 2, this.mCalendar.get(5));
                sb.append(' ');
                intToString(sb, 2, this.mCalendar.get(11));
                sb.append(':');
                intToString(sb, 2, this.mCalendar.get(12));
                sb.append(':');
                intToString(sb, 2, this.mCalendar.get(13));
                sb.append('.');
                intToString(sb, 3, this.mCalendar.get(14));
            }
            sb.append(" ");
            sb.append(Process.myPid());
            sb.append("-");
            sb.append(Thread.currentThread().getId());
            sb.append(" ");
            sb.append(tag);
            sb.append(": ");
            return sb;
        }

        public void log(String tag, String message) {
            StringBuilder makeBaseInfo = makeBaseInfo(new StringBuilder(), tag);
            makeBaseInfo.append(message);
            write(makeBaseInfo);
        }
    }

    public HwLockSettingsStorage(Context context) {
        super(context);
        this.mLSSDbReport = new LockSettingsDbReport(context);
    }

    public LockSettingsStorage.CredentialHash readCredentialHashEx(int userId) {
        LockSettingsStorage.CredentialHash passwordHash = readExPasswordHashIfExists(userId);
        if (passwordHash != null) {
            return passwordHash;
        }
        return LockSettingsStorage.CredentialHash.createEmptyHash();
    }

    private LockSettingsStorage.CredentialHash readExPasswordHashIfExists(int userId) {
        byte[] stored = readFile(getExLockPasswordFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return LockSettingsStorage.CredentialHash.create(stored, 2);
        }
        Slog.i(TAG, "readPatternHash , cannot get any PasswordHash");
        return null;
    }

    /* access modifiers changed from: package-private */
    public void writeCredentialHashEx(LockSettingsStorage.CredentialHash hash, int userId) {
        writeFile(getExLockPasswordFilename(userId), hash.hash);
    }

    private String getExLockPasswordFilename(int userId) {
        String dataSystemDirectory = Environment.getDataDirectory().getAbsolutePath() + "/system/";
        if (userId != 0) {
            return new File(Environment.getUserSystemDirectory(userId), LOCK_EXTEND_PASSWORD_FILE).getAbsolutePath();
        }
        return dataSystemDirectory + LOCK_EXTEND_PASSWORD_FILE;
    }

    /* access modifiers changed from: package-private */
    public void deleteExPasswordFile(int userId) {
        File file = new File(getExLockPasswordFilename(userId));
        if (file.exists() && !file.delete()) {
            Slog.e(TAG, "Error delet file ");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasSetPassword(int userId) {
        if (new File(getExLockPasswordFilename(userId)).exists()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void flog(String tag, String msg) {
        if (this.mFLogger != null) {
            this.mFLogger.log(tag, msg);
        }
    }

    public void writeKeyValue(SQLiteDatabase db, String key, String value, int userId) {
        HwLockSettingsStorage.super.writeKeyValue(db, key, value, userId);
        this.mLSSDbReport.writeBackItemData(key, value, userId);
    }

    public void restoreDataFromXml(SQLiteDatabase db) {
        this.mLSSDbReport.restoreDataFromXml(db);
    }

    public void syncDataToXmlFile(SQLiteDatabase db) {
        this.mLSSDbReport.syncDataToXmlFile(db);
    }

    public void removeUser(int userId) {
        HwLockSettingsStorage.super.removeUser(userId);
        this.mLSSDbReport.removeUserInfo(userId);
    }
}
