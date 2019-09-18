package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Downloads;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.Slog;
import android.util.StatsLog;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Protocol;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class BootReceiver extends BroadcastReceiver {
    private static final String FSCK_FS_MODIFIED = "FILE SYSTEM WAS MODIFIED";
    private static final String FSCK_PASS_PATTERN = "Pass ([1-9]E?):";
    private static final String FSCK_TREE_OPTIMIZATION_PATTERN = "Inode [0-9]+ extent tree.*could be shorter";
    private static final int FS_STAT_FS_FIXED = 1024;
    private static final String FS_STAT_PATTERN = "fs_stat,[^,]*/([^/,]+),(0x[0-9a-fA-F]+)";
    private static final String LAST_HEADER_FILE = "last-header.txt";
    private static final String[] LAST_KMSG_FILES = {"/sys/fs/pstore/console-ramoops", "/proc/last_kmsg"};
    private static final String LAST_SHUTDOWN_TIME_PATTERN = "powerctl_shutdown_time_ms:([0-9]+):([0-9]+)";
    private static final String LOG_FILES_FILE = "log-files.xml";
    /* access modifiers changed from: private */
    public static final int LOG_SIZE = (SystemProperties.getInt("ro.debuggable", 0) == 1 ? 98304 : Protocol.BASE_SYSTEM_RESERVED);
    private static final String METRIC_SHUTDOWN_TIME_START = "begin_shutdown";
    private static final String METRIC_SYSTEM_SERVER = "shutdown_system_server";
    private static final String[] MOUNT_DURATION_PROPS_POSTFIX = {"early", PhoneConstants.APN_TYPE_DEFAULT, "late"};
    private static final String OLD_UPDATER_CLASS = "com.google.android.systemupdater.SystemUpdateReceiver";
    private static final String OLD_UPDATER_PACKAGE = "com.google.android.systemupdater";
    /* access modifiers changed from: private */
    public static final File PSTORE_DIR = new File("/mnt/pstore");
    private static final String SHUTDOWN_METRICS_FILE = "/data/system/shutdown-metrics.txt";
    private static final String SHUTDOWN_TRON_METRICS_PREFIX = "shutdown_";
    private static final String TAG = "BootReceiver";
    private static final String TAG_TOMBSTONE = "SYSTEM_TOMBSTONE";
    /* access modifiers changed from: private */
    public static final File TOMBSTONE_DIR = new File("/data/tombstones");
    private static final int UMOUNT_STATUS_NOT_AVAILABLE = 4;
    private static final int USER_TYPE_CHINA_BETA = 3;
    private static final File lastHeaderFile = new File(Environment.getDataSystemDirectory(), LAST_HEADER_FILE);
    private static final AtomicFile sFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), LOG_FILES_FILE), "log-files");
    private static FileObserver sPstoreObserver = null;
    private static FileObserver sTombstoneObserver = null;

    public void onReceive(final Context context, Intent intent) {
        new Thread() {
            public void run() {
                try {
                    BootReceiver.this.logBootEvents(context);
                } catch (Exception e) {
                    Slog.e(BootReceiver.TAG, "Can't log boot events", e);
                }
                boolean onlyCore = false;
                try {
                    onlyCore = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
                } catch (RemoteException e2) {
                }
                if (!onlyCore) {
                    try {
                        BootReceiver.this.removeOldUpdatePackages(context);
                    } catch (Exception e3) {
                        Slog.e(BootReceiver.TAG, "Can't remove old update packages", e3);
                    }
                }
            }
        }.start();
    }

    /* access modifiers changed from: private */
    public void removeOldUpdatePackages(Context context) {
        Downloads.removeAllDownloadsByPackage(context, OLD_UPDATER_PACKAGE, OLD_UPDATER_CLASS);
    }

    private String getPreviousBootHeaders() {
        try {
            return FileUtils.readTextFile(lastHeaderFile, 0, null);
        } catch (IOException e) {
            return null;
        }
    }

    private String getCurrentBootHeaders() throws IOException {
        StringBuilder sb = new StringBuilder(512);
        sb.append("Build: ");
        sb.append(Build.FINGERPRINT);
        sb.append("\n");
        sb.append("Hardware: ");
        sb.append(Build.BOARD);
        sb.append("\n");
        sb.append("Revision: ");
        sb.append(SystemProperties.get("ro.revision", ""));
        sb.append("\n");
        sb.append("Bootloader: ");
        sb.append(Build.BOOTLOADER);
        sb.append("\n");
        sb.append("Radio: ");
        sb.append(Build.getRadioVersion());
        sb.append("\n");
        sb.append("Kernel: ");
        sb.append(FileUtils.readTextFile(new File("/proc/version"), 1024, "...\n"));
        sb.append("\n");
        return sb.toString();
    }

    private String getBootHeadersToLogAndUpdate() throws IOException {
        String oldHeaders = getPreviousBootHeaders();
        String newHeaders = getCurrentBootHeaders();
        try {
            FileUtils.stringToFile(lastHeaderFile, newHeaders);
        } catch (IOException e) {
            Slog.e(TAG, "Error writing " + lastHeaderFile, e);
        }
        if (oldHeaders == null) {
            return "isPrevious: false\n" + newHeaders;
        }
        return "isPrevious: true\n" + oldHeaders;
    }

    /* access modifiers changed from: private */
    public void logBootEvents(Context ctx) throws IOException {
        DropBoxManager db = (DropBoxManager) ctx.getSystemService("dropbox");
        String headers = getBootHeadersToLogAndUpdate();
        String bootReason = SystemProperties.get("ro.boot.bootreason", null);
        if (!(RecoverySystem.handleAftermath(ctx) == null || db == null)) {
            db.addText("SYSTEM_RECOVERY_LOG", headers + recovery);
        }
        String lastKmsgFooter = "";
        if (bootReason != null) {
            StringBuilder sb = new StringBuilder(512);
            sb.append("\n");
            sb.append("Boot info:\n");
            sb.append("Last boot reason: ");
            sb.append(bootReason);
            sb.append("\n");
            lastKmsgFooter = sb.toString();
        }
        String lastKmsgFooter2 = lastKmsgFooter;
        HashMap<String, Long> timestamps = readTimestamps();
        if (SystemProperties.getLong("ro.runtime.firstboot", 0) == 0) {
            if (!StorageManager.inCryptKeeperBounce()) {
                SystemProperties.set("ro.runtime.firstboot", Long.toString(System.currentTimeMillis()));
            }
            if (db != null) {
                db.addText("SYSTEM_BOOT", headers);
            }
            HashMap<String, Long> hashMap = timestamps;
            String str = headers;
            String str2 = lastKmsgFooter2;
            addFileWithFootersToDropBox(db, hashMap, str, str2, "/proc/last_kmsg", -LOG_SIZE, "SYSTEM_LAST_KMSG");
            addFileWithFootersToDropBox(db, hashMap, str, str2, "/sys/fs/pstore/console-ramoops", -LOG_SIZE, "SYSTEM_LAST_KMSG");
            addFileWithFootersToDropBox(db, hashMap, str, str2, "/sys/fs/pstore/console-ramoops-0", -LOG_SIZE, "SYSTEM_LAST_KMSG");
            addFileToDropBox(db, hashMap, str, "/cache/recovery/log", -LOG_SIZE, "SYSTEM_RECOVERY_LOG");
            addFileToDropBox(db, hashMap, str, "/cache/recovery/last_kmsg", -LOG_SIZE, "SYSTEM_RECOVERY_KMSG");
            addAuditErrorsToDropBox(db, timestamps, headers, -LOG_SIZE, "SYSTEM_AUDIT");
        } else if (db != null) {
            db.addText("SYSTEM_RESTART", headers);
        }
        logFsShutdownTime();
        logFsMountTime();
        addFsckErrorsToDropBoxAndLogFsStat(db, timestamps, headers, -LOG_SIZE, "SYSTEM_FSCK");
        logSystemServerShutdownTimeMetrics();
        File[] tombstoneFiles = TOMBSTONE_DIR.listFiles();
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (tombstoneFiles == null || i3 >= tombstoneFiles.length) {
                writeTimestamps(timestamps);
            } else {
                if (tombstoneFiles[i3].isFile()) {
                    addFileToDropBox(db, timestamps, headers, tombstoneFiles[i3].getPath(), LOG_SIZE, TAG_TOMBSTONE);
                }
                i2 = i3 + 1;
            }
        }
        writeTimestamps(timestamps);
        if (!TOMBSTONE_DIR.exists() && !TOMBSTONE_DIR.mkdirs()) {
            Slog.e(TAG, "Can't create a empty TOMBSTONE_DIR");
        }
        final DropBoxManager dropBoxManager = db;
        final String str3 = headers;
        AnonymousClass2 r2 = new FileObserver(TOMBSTONE_DIR.getPath(), 256) {
            public void onEvent(int event, String path) {
                if (path != null) {
                    HashMap<String, Long> timestamps = BootReceiver.readTimestamps();
                    try {
                        File file = new File(BootReceiver.TOMBSTONE_DIR, path);
                        if (file.isFile() && file.getName().startsWith("tombstone_")) {
                            BootReceiver.addFileToDropBox(dropBoxManager, timestamps, str3, file.getPath(), BootReceiver.LOG_SIZE, BootReceiver.TAG_TOMBSTONE);
                        }
                    } catch (IOException e) {
                        Slog.e(BootReceiver.TAG, "Can't log tombstone", e);
                    } catch (NullPointerException e2) {
                    }
                    BootReceiver.this.writeTimestamps(timestamps);
                }
            }
        };
        sTombstoneObserver = r2;
        sTombstoneObserver.startWatching();
        File[] pstoreFiles = PSTORE_DIR.listFiles();
        while (pstoreFiles != null && i < pstoreFiles.length) {
            addFileToDropBox(db, timestamps, headers, pstoreFiles[i].getPath(), -LOG_SIZE, "SYSTEM_PSTORE");
            i++;
            pstoreFiles = pstoreFiles;
        }
        final DropBoxManager dropBoxManager2 = db;
        final HashMap<String, Long> hashMap2 = timestamps;
        final String str4 = headers;
        AnonymousClass3 r22 = new FileObserver(PSTORE_DIR.getPath(), 8) {
            public void onEvent(int event, String path) {
                try {
                    BootReceiver.addFileToDropBox(dropBoxManager2, hashMap2, str4, new File(BootReceiver.PSTORE_DIR, path).getPath(), -BootReceiver.LOG_SIZE, "SYSTEM_PSTORE");
                } catch (IOException e) {
                    Slog.e(BootReceiver.TAG, "Can't log pstore", e);
                } catch (NullPointerException e2) {
                }
            }
        };
        sPstoreObserver = r22;
        sPstoreObserver.startWatching();
    }

    /* access modifiers changed from: private */
    public static void addFileToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String filename, int maxSize, String tag) throws IOException {
        addFileWithFootersToDropBox(db, timestamps, headers, "", filename, maxSize, tag);
    }

    private static void addFileWithFootersToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String footers, String filename, int maxSize, String tag) throws IOException {
        if (db != null && db.isTagEnabled(tag)) {
            File file = new File(filename);
            if (!file.isDirectory()) {
                long fileTime = file.lastModified();
                if (fileTime > 0) {
                    if (!timestamps.containsKey(filename) || timestamps.get(filename).longValue() != fileTime) {
                        timestamps.put(filename, Long.valueOf(fileTime));
                        String fileContents = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
                        if (tag.equals(TAG_TOMBSTONE) && fileContents.contains(">>> system_server <<<") && 3 == SystemProperties.getInt("ro.logsystem.usertype", 0)) {
                            maxSize = (int) file.length();
                            fileContents = FileUtils.readTextFile(file, maxSize, "[[NO_TRUNCATED]]\n");
                        }
                        String text = headers + fileContents + footers;
                        if (tag.equals(TAG_TOMBSTONE) && fileContents.contains(">>> system_server <<<")) {
                            addTextToDropBox(db, "system_server_native_crash", text, filename, maxSize);
                        }
                        addTextToDropBox(db, tag, text, filename, maxSize);
                    }
                }
            }
        }
    }

    private static void addTextToDropBox(DropBoxManager db, String tag, String text, String filename, int maxSize) {
        Slog.i(TAG, "Copying " + filename + " to DropBox (" + tag + ")");
        db.addText(tag, text);
        EventLog.writeEvent(DropboxLogTags.DROPBOX_FILE_COPY, new Object[]{filename, Integer.valueOf(maxSize), tag});
    }

    private static void addAuditErrorsToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        if (db != null && db.isTagEnabled(tag)) {
            Slog.i(TAG, "Copying audit failures to DropBox");
            File file = new File("/proc/last_kmsg");
            long fileTime = file.lastModified();
            if (fileTime <= 0) {
                file = new File("/sys/fs/pstore/console-ramoops");
                fileTime = file.lastModified();
                if (fileTime <= 0) {
                    file = new File("/sys/fs/pstore/console-ramoops-0");
                    fileTime = file.lastModified();
                }
            }
            if (fileTime > 0) {
                if (!timestamps.containsKey(tag) || timestamps.get(tag).longValue() != fileTime) {
                    timestamps.put(tag, Long.valueOf(fileTime));
                    String log = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
                    StringBuilder sb = new StringBuilder();
                    for (String line : log.split("\n")) {
                        if (line.contains("audit")) {
                            sb.append(line + "\n");
                        }
                    }
                    Slog.i(TAG, "Copied " + sb.toString().length() + " worth of audits to DropBox");
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(headers);
                    sb2.append(sb.toString());
                    db.addText(tag, sb2.toString());
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x000b, code lost:
        if (r6.isTagEnabled(r7) == false) goto L_0x0012;
     */
    private static void addFsckErrorsToDropBoxAndLogFsStat(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        String str;
        int i;
        int lastFsStatLineNumber;
        DropBoxManager dropBoxManager = db;
        boolean uploadEnabled = true;
        if (dropBoxManager != null) {
            str = tag;
        } else {
            str = tag;
            uploadEnabled = false;
        }
        boolean uploadEnabled2 = uploadEnabled;
        Slog.i(TAG, "Checking for fsck errors");
        File file = new File("/dev/fscklogs/log");
        if (file.lastModified() > 0) {
            int i2 = maxSize;
            String log = FileUtils.readTextFile(file, i2, "[[TRUNCATED]]\n");
            Pattern pattern = Pattern.compile(FS_STAT_PATTERN);
            String[] lines = log.split("\n");
            int lastFsStatLineNumber2 = 0;
            int length = lines.length;
            int i3 = 0;
            boolean uploadNeeded = false;
            int lineNumber = 0;
            while (i3 < length) {
                String line = lines[i3];
                if (line.contains(FSCK_FS_MODIFIED)) {
                    uploadNeeded = true;
                } else {
                    if (line.contains("fs_stat")) {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            handleFsckFsStat(matcher, lines, lastFsStatLineNumber2, lineNumber);
                            lastFsStatLineNumber2 = lineNumber;
                        } else {
                            Matcher matcher2 = matcher;
                            lastFsStatLineNumber = lastFsStatLineNumber2;
                            StringBuilder sb = new StringBuilder();
                            i = length;
                            sb.append("cannot parse fs_stat:");
                            sb.append(line);
                            Slog.w(TAG, sb.toString());
                        }
                    } else {
                        lastFsStatLineNumber = lastFsStatLineNumber2;
                        i = length;
                    }
                    lastFsStatLineNumber2 = lastFsStatLineNumber;
                    lineNumber++;
                    i3++;
                    length = i;
                }
                i = length;
                lineNumber++;
                i3++;
                length = i;
            }
            int lastFsStatLineNumber3 = lastFsStatLineNumber2;
            if (!uploadEnabled2 || !uploadNeeded) {
                int i4 = lastFsStatLineNumber3;
            } else {
                int i5 = lastFsStatLineNumber3;
                int i6 = lineNumber;
                addFileToDropBox(dropBoxManager, timestamps, headers, "/dev/fscklogs/log", i2, str);
            }
            file.delete();
        }
    }

    private static void logFsMountTime() {
        for (String propPostfix : MOUNT_DURATION_PROPS_POSTFIX) {
            int duration = SystemProperties.getInt("ro.boottime.init.mount_all." + propPostfix, 0);
            if (duration != 0) {
                MetricsLogger.histogram(null, "boot_mount_all_duration_" + propPostfix, duration);
            }
        }
    }

    private static void logSystemServerShutdownTimeMetrics() {
        File metricsFile = new File(SHUTDOWN_METRICS_FILE);
        String metricsStr = null;
        if (metricsFile.exists()) {
            try {
                metricsStr = FileUtils.readTextFile(metricsFile, 0, null);
            } catch (IOException e) {
                Slog.e(TAG, "Problem reading " + metricsFile, e);
            }
        }
        if (!TextUtils.isEmpty(metricsStr)) {
            String duration = null;
            String start_time = null;
            String reason = null;
            String reboot = null;
            for (String keyValueStr : metricsStr.split(",")) {
                String[] keyValue = keyValueStr.split(":");
                if (keyValue.length != 2) {
                    Slog.e(TAG, "Wrong format of shutdown metrics - " + metricsStr);
                } else {
                    if (keyValue[0].startsWith(SHUTDOWN_TRON_METRICS_PREFIX)) {
                        logTronShutdownMetric(keyValue[0], keyValue[1]);
                        if (keyValue[0].equals(METRIC_SYSTEM_SERVER)) {
                            duration = keyValue[1];
                        }
                    }
                    if (keyValue[0].equals("reboot")) {
                        reboot = keyValue[1];
                    } else if (keyValue[0].equals("reason")) {
                        reason = keyValue[1];
                    } else if (keyValue[0].equals(METRIC_SHUTDOWN_TIME_START)) {
                        start_time = keyValue[1];
                    }
                }
            }
            logStatsdShutdownAtom(reboot, reason, start_time, duration);
        }
        metricsFile.delete();
    }

    private static void logTronShutdownMetric(String metricName, String valueStr) {
        try {
            int value = Integer.parseInt(valueStr);
            if (value >= 0) {
                MetricsLogger.histogram(null, metricName, value);
            }
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Cannot parse metric " + metricName + " int value - " + valueStr);
        }
    }

    private static void logStatsdShutdownAtom(String rebootStr, String reasonStr, String startStr, String durationStr) {
        String str = rebootStr;
        String str2 = startStr;
        boolean reboot = false;
        String reason = "<EMPTY>";
        long start = 0;
        long duration = 0;
        if (str == null) {
            Slog.e(TAG, "No value received for reboot");
        } else if (str.equals("y")) {
            reboot = true;
        } else if (!str.equals("n")) {
            Slog.e(TAG, "Unexpected value for reboot : " + str);
        }
        boolean reboot2 = reboot;
        if (reasonStr != null) {
            reason = reasonStr;
        } else {
            Slog.e(TAG, "No value received for shutdown reason");
        }
        if (str2 != null) {
            try {
                start = Long.parseLong(startStr);
            } catch (NumberFormatException e) {
                NumberFormatException numberFormatException = e;
                Slog.e(TAG, "Cannot parse shutdown start time: " + str2);
            }
        } else {
            Slog.e(TAG, "No value received for shutdown start time");
        }
        if (durationStr != null) {
            try {
                duration = Long.parseLong(durationStr);
            } catch (NumberFormatException e2) {
                NumberFormatException numberFormatException2 = e2;
                Slog.e(TAG, "Cannot parse shutdown duration: " + str2);
            }
        } else {
            Slog.e(TAG, "No value received for shutdown duration");
        }
        StatsLog.write(56, reboot2, reason, start, duration);
    }

    private static void logFsShutdownTime() {
        File f = null;
        String[] strArr = LAST_KMSG_FILES;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            File file = new File(strArr[i]);
            if (file.exists()) {
                f = file;
                break;
            }
            i++;
        }
        if (f != null) {
            try {
                Matcher matcher = Pattern.compile(LAST_SHUTDOWN_TIME_PATTERN, 8).matcher(FileUtils.readTextFile(f, -16384, null));
                if (matcher.find()) {
                    MetricsLogger.histogram(null, "boot_fs_shutdown_duration", Integer.parseInt(matcher.group(1)));
                    MetricsLogger.histogram(null, "boot_fs_shutdown_umount_stat", Integer.parseInt(matcher.group(2)));
                    Slog.i(TAG, "boot_fs_shutdown," + matcher.group(1) + "," + matcher.group(2));
                } else {
                    MetricsLogger.histogram(null, "boot_fs_shutdown_umount_stat", 4);
                    Slog.w(TAG, "boot_fs_shutdown, string not found");
                }
            } catch (IOException e) {
                Slog.w(TAG, "cannot read last msg", e);
            }
        }
    }

    @VisibleForTesting
    public static int fixFsckFsStat(String partition, int statOrg, String[] lines, int startLineNumber, int endLineNumber) {
        String line;
        Pattern treeOptPattern;
        Pattern passPattern;
        String str = partition;
        int stat = statOrg;
        if ((stat & 1024) != 0) {
            Pattern passPattern2 = Pattern.compile(FSCK_PASS_PATTERN);
            Pattern treeOptPattern2 = Pattern.compile(FSCK_TREE_OPTIMIZATION_PATTERN);
            boolean foundOtherFix = false;
            String otherFixLine = null;
            boolean foundTimestampAdjustment = false;
            boolean foundQuotaFix = false;
            boolean foundTreeOptimization = false;
            String currentPass = "";
            int i = startLineNumber;
            while (true) {
                if (i >= endLineNumber) {
                    Pattern pattern = treeOptPattern2;
                    break;
                }
                line = lines[i];
                if (line.contains(FSCK_FS_MODIFIED)) {
                    Pattern pattern2 = passPattern2;
                    Pattern pattern3 = treeOptPattern2;
                    break;
                }
                if (line.startsWith("Pass ")) {
                    Matcher matcher = passPattern2.matcher(line);
                    if (matcher.find()) {
                        currentPass = matcher.group(1);
                    }
                    passPattern = passPattern2;
                    treeOptPattern = treeOptPattern2;
                } else if (!line.startsWith("Inode ")) {
                    passPattern = passPattern2;
                    treeOptPattern = treeOptPattern2;
                    if (line.startsWith("[QUOTA WARNING]") && currentPass.equals("5")) {
                        Slog.i(TAG, "fs_stat, partition:" + str + " found quota warning:" + line);
                        foundQuotaFix = true;
                        if (!foundTreeOptimization) {
                            otherFixLine = line;
                            break;
                        }
                    } else if (!line.startsWith("Update quota info") || !currentPass.equals("5")) {
                        if (!line.startsWith("Timestamp(s) on inode") || !line.contains("beyond 2310-04-04 are likely pre-1970") || !currentPass.equals("1")) {
                            String line2 = line.trim();
                            if (!line2.isEmpty() && !currentPass.isEmpty()) {
                                foundOtherFix = true;
                                otherFixLine = line2;
                                break;
                            }
                        } else {
                            Slog.i(TAG, "fs_stat, partition:" + str + " found timestamp adjustment:" + line);
                            if (lines[i + 1].contains("Fix? yes")) {
                                i++;
                            }
                            foundTimestampAdjustment = true;
                        }
                    }
                } else if (!treeOptPattern2.matcher(line).find() || !currentPass.equals("1")) {
                    Pattern pattern4 = treeOptPattern2;
                    foundOtherFix = true;
                    otherFixLine = line;
                } else {
                    foundTreeOptimization = true;
                    passPattern = passPattern2;
                    StringBuilder sb = new StringBuilder();
                    treeOptPattern = treeOptPattern2;
                    sb.append("fs_stat, partition:");
                    sb.append(str);
                    sb.append(" found tree optimization:");
                    sb.append(line);
                    Slog.i(TAG, sb.toString());
                }
                i++;
                passPattern2 = passPattern;
                treeOptPattern2 = treeOptPattern;
            }
            Pattern pattern42 = treeOptPattern2;
            foundOtherFix = true;
            otherFixLine = line;
            if (foundOtherFix) {
                if (otherFixLine == null) {
                    return stat;
                }
                Slog.i(TAG, "fs_stat, partition:" + str + " fix:" + otherFixLine);
                return stat;
            } else if (foundQuotaFix && !foundTreeOptimization) {
                Slog.i(TAG, "fs_stat, got quota fix without tree optimization, partition:" + str);
                return stat;
            } else if ((!foundTreeOptimization || !foundQuotaFix) && !foundTimestampAdjustment) {
                return stat;
            } else {
                Slog.i(TAG, "fs_stat, partition:" + str + " fix ignored");
                return stat & -1025;
            }
        } else {
            int i2 = endLineNumber;
            return stat;
        }
    }

    private static void handleFsckFsStat(Matcher match, String[] lines, int startLineNumber, int endLineNumber) {
        String partition = match.group(1);
        try {
            int stat = fixFsckFsStat(partition, Integer.decode(match.group(2)).intValue(), lines, startLineNumber, endLineNumber);
            MetricsLogger.histogram(null, "boot_fs_stat_" + partition, stat);
            Slog.i(TAG, "fs_stat, partition:" + partition + " stat:0x" + Integer.toHexString(stat));
        } catch (NumberFormatException e) {
            Slog.w(TAG, "cannot parse fs_stat: partition:" + partition + " stat:" + match.group(2));
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0091, code lost:
        if (1 == 0) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0143, code lost:
        if (r2 != false) goto L_0x0147;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002b A[Catch:{ all -> 0x00a0, Throwable -> 0x00ac, FileNotFoundException -> 0x0121, IOException -> 0x0106, IllegalStateException -> 0x00ec, NullPointerException -> 0x00d2, XmlPullParserException -> 0x00b8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0098 A[SYNTHETIC, Splitter:B:37:0x0098] */
    public static HashMap<String, Long> readTimestamps() {
        HashMap<String, Long> timestamps;
        FileInputStream stream;
        int type;
        synchronized (sFile) {
            timestamps = new HashMap<>();
            boolean success = false;
            try {
                stream = sFile.openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                while (true) {
                    int next = parser.next();
                    type = next;
                    if (next == 2 || type == 1) {
                        if (type != 2) {
                            int outerDepth = parser.getDepth();
                            while (true) {
                                int next2 = parser.next();
                                int type2 = next2;
                                if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                                    success = true;
                                } else if (type2 != 3) {
                                    if (type2 != 4) {
                                        if (parser.getName().equals("log")) {
                                            timestamps.put(parser.getAttributeValue(null, "filename"), Long.valueOf(Long.valueOf(parser.getAttributeValue(null, "timestamp")).longValue()));
                                        } else {
                                            Slog.w(TAG, "Unknown tag: " + parser.getName());
                                            XmlUtils.skipCurrentTag(parser);
                                        }
                                    }
                                }
                            }
                            success = true;
                            if (stream != null) {
                                stream.close();
                            }
                        } else {
                            throw new IllegalStateException("no start tag found");
                        }
                    }
                }
                if (type != 2) {
                }
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "No existing last log timestamp file " + sFile.getBaseFile() + "; starting empty");
            } catch (IOException e2) {
                Slog.w(TAG, "Failed parsing " + e2);
                if (!success) {
                    timestamps.clear();
                }
                return timestamps;
            } catch (IllegalStateException e3) {
                Slog.w(TAG, "Failed parsing " + e3);
                if (!success) {
                    timestamps.clear();
                }
                return timestamps;
            } catch (NullPointerException e4) {
                Slog.w(TAG, "Failed parsing " + e4);
                if (!success) {
                    timestamps.clear();
                }
                return timestamps;
            } catch (XmlPullParserException e5) {
                try {
                    Slog.w(TAG, "Failed parsing " + e5);
                    if (!success) {
                        timestamps.clear();
                    }
                    return timestamps;
                } catch (Throwable th) {
                    if (!success) {
                        timestamps.clear();
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                r4.addSuppressed(th2);
            }
        }
        return timestamps;
        throw th;
    }

    /* access modifiers changed from: private */
    public void writeTimestamps(HashMap<String, Long> timestamps) {
        synchronized (sFile) {
            try {
                FileOutputStream stream = sFile.startWrite();
                try {
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, true);
                    out.startTag(null, "log-files");
                    for (String filename : timestamps.keySet()) {
                        out.startTag(null, "log");
                        out.attribute(null, "filename", filename);
                        out.attribute(null, "timestamp", timestamps.get(filename).toString());
                        out.endTag(null, "log");
                    }
                    out.endTag(null, "log-files");
                    out.endDocument();
                    sFile.finishWrite(stream);
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to write timestamp file, using the backup: " + e);
                    sFile.failWrite(stream);
                } catch (Throwable th) {
                    throw th;
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed to write timestamp file: " + e2);
            }
        }
    }
}
