package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.net.wifi.WifiScanLog;
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
import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.Slog;
import android.util.StatsLog;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.ZygoteInit;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.FastXmlSerializer;
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
    private static final int CHINA_BETA_LOG_SIZE = 4194304;
    private static final String FSCK_FS_MODIFIED = "FILE SYSTEM WAS MODIFIED";
    private static final String FSCK_PASS_PATTERN = "Pass ([1-9]E?):";
    private static final String FSCK_TREE_OPTIMIZATION_PATTERN = "Inode [0-9]+ extent tree.*could be shorter";
    private static final int FS_STAT_FS_FIXED = 1024;
    private static final String FS_STAT_PATTERN = "fs_stat,[^,]*/([^/,]+),(0x[0-9a-fA-F]+)";
    private static final String LAST_HEADER_FILE = "last-header.txt";
    private static final String[] LAST_KMSG_FILES = {"/sys/fs/pstore/console-ramoops", "/proc/last_kmsg"};
    private static final String LAST_SHUTDOWN_TIME_PATTERN = "powerctl_shutdown_time_ms:([0-9]+):([0-9]+)";
    private static final String LOG_FILES_FILE = "log-files.xml";
    private static final int LOG_SIZE = (SystemProperties.getInt("ro.debuggable", 0) == 1 ? 98304 : 65536);
    private static final String METRIC_SHUTDOWN_TIME_START = "begin_shutdown";
    private static final String METRIC_SYSTEM_SERVER = "shutdown_system_server";
    private static final String[] MOUNT_DURATION_PROPS_POSTFIX = {"early", PhoneConstants.APN_TYPE_DEFAULT, "late"};
    private static final String OLD_UPDATER_CLASS = "com.google.android.systemupdater.SystemUpdateReceiver";
    private static final String OLD_UPDATER_PACKAGE = "com.google.android.systemupdater";
    private static final String SHUTDOWN_METRICS_FILE = "/data/system/shutdown-metrics.txt";
    private static final String SHUTDOWN_TRON_METRICS_PREFIX = "shutdown_";
    private static final String TAG = "BootReceiver";
    private static final String TAG_TOMBSTONE = "SYSTEM_TOMBSTONE";
    private static final File TOMBSTONE_DIR = new File("/data/tombstones");
    private static final int UMOUNT_STATUS_NOT_AVAILABLE = 4;
    private static final int USER_TYPE_CHINA_BETA = 3;
    private static final File lastHeaderFile = new File(Environment.getDataSystemDirectory(), LAST_HEADER_FILE);
    private static final AtomicFile sFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), LOG_FILES_FILE), "log-files");
    private static FileObserver sTombstoneObserver = null;

    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, Intent intent) {
        new Thread() {
            /* class com.android.server.BootReceiver.AnonymousClass1 */

            @Override // java.lang.Thread, java.lang.Runnable
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
    /* access modifiers changed from: public */
    private void removeOldUpdatePackages(Context context) {
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
    /* access modifiers changed from: public */
    private void logBootEvents(Context ctx) throws IOException {
        String lastKmsgFooter;
        final DropBoxManager db = (DropBoxManager) ctx.getSystemService(Context.DROPBOX_SERVICE);
        final String headers = getBootHeadersToLogAndUpdate();
        String bootReason = SystemProperties.get("ro.boot.bootreason", null);
        String recovery = RecoverySystem.handleAftermath(ctx);
        if (!(recovery == null || db == null)) {
            db.addText("SYSTEM_RECOVERY_LOG", headers + recovery);
        }
        if (bootReason != null) {
            StringBuilder sb = new StringBuilder(512);
            sb.append("\n");
            sb.append("Boot info:\n");
            sb.append("Last boot reason: ");
            sb.append(bootReason);
            sb.append("\n");
            lastKmsgFooter = sb.toString();
        } else {
            lastKmsgFooter = "";
        }
        HashMap<String, Long> timestamps = readTimestamps();
        if (SystemProperties.getLong("ro.runtime.firstboot", 0) == 0) {
            if (!StorageManager.inCryptKeeperBounce()) {
                SystemProperties.set("ro.runtime.firstboot", Long.toString(System.currentTimeMillis()));
            }
            if (db != null) {
                db.addText("SYSTEM_BOOT", headers);
            }
            addFileWithFootersToDropBox(db, timestamps, headers, lastKmsgFooter, "/proc/last_kmsg", -LOG_SIZE, "SYSTEM_LAST_KMSG");
            addFileWithFootersToDropBox(db, timestamps, headers, lastKmsgFooter, "/sys/fs/pstore/console-ramoops", -LOG_SIZE, "SYSTEM_LAST_KMSG");
            addFileWithFootersToDropBox(db, timestamps, headers, lastKmsgFooter, "/sys/fs/pstore/console-ramoops-0", -LOG_SIZE, "SYSTEM_LAST_KMSG");
            addFileToDropBox(db, timestamps, headers, "/cache/recovery/log", -LOG_SIZE, "SYSTEM_RECOVERY_LOG");
            addFileToDropBox(db, timestamps, headers, "/cache/recovery/last_kmsg", -LOG_SIZE, "SYSTEM_RECOVERY_KMSG");
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
        while (tombstoneFiles != null && i < tombstoneFiles.length) {
            if (tombstoneFiles[i].isFile()) {
                addFileToDropBox(db, timestamps, headers, tombstoneFiles[i].getPath(), LOG_SIZE, TAG_TOMBSTONE);
            }
            i++;
        }
        writeTimestamps(timestamps);
        sTombstoneObserver = new FileObserver(TOMBSTONE_DIR.getPath(), 256) {
            /* class com.android.server.BootReceiver.AnonymousClass2 */

            @Override // android.os.FileObserver
            public void onEvent(int event, String path) {
                HashMap<String, Long> timestamps = BootReceiver.readTimestamps();
                try {
                    File file = new File(BootReceiver.TOMBSTONE_DIR, path);
                    if (file.isFile() && file.getName().startsWith("tombstone_")) {
                        BootReceiver.addFileToDropBox(db, timestamps, headers, file.getPath(), BootReceiver.LOG_SIZE, BootReceiver.TAG_TOMBSTONE);
                    }
                } catch (IOException e) {
                    Slog.e(BootReceiver.TAG, "Can't log tombstone", e);
                }
                BootReceiver.this.writeTimestamps(timestamps);
            }
        };
        sTombstoneObserver.startWatching();
    }

    /* access modifiers changed from: private */
    public static void addFileToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String filename, int maxSize, String tag) throws IOException {
        addFileWithFootersToDropBox(db, timestamps, headers, "", filename, maxSize, tag);
    }

    private static void addFileWithFootersToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String footers, String filename, int maxSize, String tag) throws IOException {
        if (db != null && db.isTagEnabled(tag)) {
            File file = new File(filename);
            long fileTime = file.lastModified();
            if (fileTime > 0) {
                if (!timestamps.containsKey(filename) || timestamps.get(filename).longValue() != fileTime) {
                    timestamps.put(filename, Long.valueOf(fileTime));
                    String fileContents = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
                    if (ZygoteInit.sIsMygote && tag.equals(TAG_TOMBSTONE) && 3 == SystemProperties.getInt("ro.logsystem.usertype", 0)) {
                        fileContents = FileUtils.readTextFile(file, 4194304, "[[TRUNCATED]]\n");
                    }
                    String text = headers + fileContents + footers;
                    if (tag.equals(TAG_TOMBSTONE) && fileContents.contains(">>> system_server <<<")) {
                        addTextToDropBox(db, "system_server_native_crash", text, filename, maxSize);
                    }
                    if (tag.equals(TAG_TOMBSTONE)) {
                        StatsLog.write(186);
                    }
                    addTextToDropBox(db, tag, text, filename, maxSize);
                }
            }
        }
    }

    private static void addTextToDropBox(DropBoxManager db, String tag, String text, String filename, int maxSize) {
        Slog.i(TAG, "Copying " + filename + " to DropBox (" + tag + ")");
        db.addText(tag, text);
        EventLog.writeEvent((int) DropboxLogTags.DROPBOX_FILE_COPY, filename, Integer.valueOf(maxSize), tag);
    }

    private static void addAuditErrorsToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        if (db == null) {
            return;
        }
        if (db.isTagEnabled(tag)) {
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
                    String[] split = log.split("\n");
                    for (String line : split) {
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

    /* JADX WARNING: Removed duplicated region for block: B:10:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002e A[RETURN] */
    private static void addFsckErrorsToDropBoxAndLogFsStat(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        boolean uploadEnabled;
        File file;
        int lastFsStatLineNumber;
        if (db != null) {
            if (db.isTagEnabled(tag)) {
                uploadEnabled = true;
                Slog.i(TAG, "Checking for fsck errors");
                file = new File("/dev/fscklogs/log");
                if (file.lastModified() <= 0) {
                    String log = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
                    Pattern pattern = Pattern.compile(FS_STAT_PATTERN);
                    String[] lines = log.split("\n");
                    int i = 0;
                    boolean uploadNeeded = false;
                    int lineNumber = 0;
                    int lastFsStatLineNumber2 = 0;
                    for (int length = lines.length; i < length; length = length) {
                        String line = lines[i];
                        if (line.contains(FSCK_FS_MODIFIED)) {
                            uploadNeeded = true;
                        } else {
                            if (line.contains("fs_stat")) {
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    handleFsckFsStat(matcher, lines, lastFsStatLineNumber2, lineNumber);
                                    lastFsStatLineNumber2 = lineNumber;
                                } else {
                                    lastFsStatLineNumber = lastFsStatLineNumber2;
                                    Slog.w(TAG, "cannot parse fs_stat:" + line);
                                }
                            } else {
                                lastFsStatLineNumber = lastFsStatLineNumber2;
                            }
                            lastFsStatLineNumber2 = lastFsStatLineNumber;
                        }
                        lineNumber++;
                        i++;
                    }
                    if (uploadEnabled && uploadNeeded) {
                        addFileToDropBox(db, timestamps, headers, "/dev/fscklogs/log", maxSize, tag);
                    }
                    file.delete();
                    return;
                }
                return;
            }
        }
        uploadEnabled = false;
        Slog.i(TAG, "Checking for fsck errors");
        file = new File("/dev/fscklogs/log");
        if (file.lastModified() <= 0) {
        }
    }

    private static void logFsMountTime() {
        String[] strArr = MOUNT_DURATION_PROPS_POSTFIX;
        for (String propPostfix : strArr) {
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
            for (String keyValueStr : metricsStr.split(SmsManager.REGEX_PREFIX_DELIMITER)) {
                String[] keyValue = keyValueStr.split(SettingsStringUtil.DELIMITER);
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

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0041  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0044  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x004b A[SYNTHETIC, Splitter:B:14:0x004b] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0070 A[SYNTHETIC, Splitter:B:21:0x0070] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008e  */
    private static void logStatsdShutdownAtom(String rebootStr, String reasonStr, String startStr, String durationStr) {
        boolean reboot;
        String reason = "<EMPTY>";
        long start = 0;
        long duration = 0;
        if (rebootStr == null) {
            Slog.e(TAG, "No value received for reboot");
        } else if (rebootStr.equals("y")) {
            reboot = true;
            if (reasonStr == null) {
                reason = reasonStr;
            } else {
                Slog.e(TAG, "No value received for shutdown reason");
            }
            if (startStr == null) {
                try {
                    start = Long.parseLong(startStr);
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "Cannot parse shutdown start time: " + startStr);
                }
            } else {
                Slog.e(TAG, "No value received for shutdown start time");
            }
            if (durationStr == null) {
                try {
                    duration = Long.parseLong(durationStr);
                } catch (NumberFormatException e2) {
                    Slog.e(TAG, "Cannot parse shutdown duration: " + startStr);
                }
            } else {
                Slog.e(TAG, "No value received for shutdown duration");
            }
            StatsLog.write(56, reboot, reason, start, duration);
        } else if (!rebootStr.equals("n")) {
            Slog.e(TAG, "Unexpected value for reboot : " + rebootStr);
        }
        reboot = false;
        if (reasonStr == null) {
        }
        if (startStr == null) {
        }
        if (durationStr == null) {
        }
        StatsLog.write(56, reboot, reason, start, duration);
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
                    Slog.i(TAG, "boot_fs_shutdown," + matcher.group(1) + SmsManager.REGEX_PREFIX_DELIMITER + matcher.group(2));
                    return;
                }
                MetricsLogger.histogram(null, "boot_fs_shutdown_umount_stat", 4);
                Slog.w(TAG, "boot_fs_shutdown, string not found");
            } catch (IOException e) {
                Slog.w(TAG, "cannot read last msg", e);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x008f, code lost:
        r8 = true;
        r9 = r14;
     */
    @VisibleForTesting
    public static int fixFsckFsStat(String partition, int statOrg, String[] lines, int startLineNumber, int endLineNumber) {
        boolean foundQuotaFix;
        Pattern passPattern;
        if ((statOrg & 1024) == 0) {
            return statOrg;
        }
        Pattern passPattern2 = Pattern.compile(FSCK_PASS_PATTERN);
        Pattern treeOptPattern = Pattern.compile(FSCK_TREE_OPTIMIZATION_PATTERN);
        String currentPass = "";
        boolean foundTreeOptimization = false;
        boolean foundQuotaFix2 = false;
        boolean foundTimestampAdjustment = false;
        boolean foundOtherFix = false;
        String otherFixLine = null;
        int i = startLineNumber;
        while (true) {
            if (i >= endLineNumber) {
                foundQuotaFix = foundQuotaFix2;
                break;
            }
            String line = lines[i];
            if (line.contains(FSCK_FS_MODIFIED)) {
                foundQuotaFix = foundQuotaFix2;
                break;
            }
            foundQuotaFix = foundQuotaFix2;
            if (line.startsWith("Pass ")) {
                Matcher matcher = passPattern2.matcher(line);
                if (matcher.find()) {
                    currentPass = matcher.group(1);
                }
                passPattern = passPattern2;
                foundQuotaFix2 = foundQuotaFix;
            } else if (!line.startsWith("Inode ")) {
                passPattern = passPattern2;
                if (!line.startsWith("[QUOTA WARNING]") || !currentPass.equals(WifiScanLog.EVENT_KEY5)) {
                    if (!line.startsWith("Update quota info") || !currentPass.equals(WifiScanLog.EVENT_KEY5)) {
                        if (!line.startsWith("Timestamp(s) on inode") || !line.contains("beyond 2310-04-04 are likely pre-1970") || !currentPass.equals("1")) {
                            String line2 = line.trim();
                            if (!line2.isEmpty() && !currentPass.isEmpty()) {
                                foundOtherFix = true;
                                otherFixLine = line2;
                                break;
                            }
                        } else {
                            Slog.i(TAG, "fs_stat, partition:" + partition + " found timestamp adjustment:" + line);
                            if (lines[i + 1].contains("Fix? yes")) {
                                i++;
                            }
                            foundTimestampAdjustment = true;
                            foundQuotaFix2 = foundQuotaFix;
                        }
                    }
                    foundQuotaFix2 = foundQuotaFix;
                } else {
                    Slog.i(TAG, "fs_stat, partition:" + partition + " found quota warning:" + line);
                    foundQuotaFix2 = true;
                    if (!foundTreeOptimization) {
                        otherFixLine = line;
                        foundQuotaFix = true;
                        break;
                    }
                }
            } else if (!treeOptPattern.matcher(line).find() || !currentPass.equals("1")) {
                break;
            } else {
                foundTreeOptimization = true;
                Slog.i(TAG, "fs_stat, partition:" + partition + " found tree optimization:" + line);
                passPattern = passPattern2;
                foundQuotaFix2 = foundQuotaFix;
            }
            i++;
            passPattern2 = passPattern;
        }
        if (foundOtherFix) {
            if (otherFixLine == null) {
                return statOrg;
            }
            Slog.i(TAG, "fs_stat, partition:" + partition + " fix:" + otherFixLine);
            return statOrg;
        } else if (foundQuotaFix && !foundTreeOptimization) {
            Slog.i(TAG, "fs_stat, got quota fix without tree optimization, partition:" + partition);
            return statOrg;
        } else if ((!foundTreeOptimization || !foundQuotaFix) && !foundTimestampAdjustment) {
            return statOrg;
        } else {
            Slog.i(TAG, "fs_stat, partition:" + partition + " fix ignored");
            return statOrg & -1025;
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
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0094, code lost:
        if (1 == 0) goto L_0x0096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a6, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a7, code lost:
        if (r3 != null) goto L_0x00a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ad, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ae, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b1, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0145, code lost:
        if (0 != 0) goto L_0x0149;
     */
    public static HashMap<String, Long> readTimestamps() {
        HashMap<String, Long> timestamps;
        FileInputStream stream;
        XmlPullParser parser;
        int type;
        synchronized (sFile) {
            timestamps = new HashMap<>();
            try {
                stream = sFile.openRead();
                parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "No existing last log timestamp file " + sFile.getBaseFile() + "; starting empty");
            } catch (IOException e2) {
                Slog.w(TAG, "Failed parsing " + e2);
                if (0 == 0) {
                    timestamps.clear();
                }
                return timestamps;
            } catch (IllegalStateException e3) {
                Slog.w(TAG, "Failed parsing " + e3);
                if (0 == 0) {
                    timestamps.clear();
                }
                return timestamps;
            } catch (NullPointerException e4) {
                Slog.w(TAG, "Failed parsing " + e4);
                if (0 == 0) {
                    timestamps.clear();
                }
                return timestamps;
            } catch (XmlPullParserException e5) {
                Slog.w(TAG, "Failed parsing " + e5);
                if (0 == 0) {
                    timestamps.clear();
                }
                return timestamps;
            } catch (Throwable th) {
                if (0 == 0) {
                    timestamps.clear();
                }
                throw th;
            }
            while (true) {
                type = parser.next();
                if (type == 2 || type == 1) {
                    break;
                }
            }
            if (type == 2) {
                int outerDepth = parser.getDepth();
                while (true) {
                    int type2 = parser.next();
                    if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        break;
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
                if (stream != null) {
                    stream.close();
                }
            } else {
                throw new IllegalStateException("no start tag found");
            }
        }
        return timestamps;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeTimestamps(HashMap<String, Long> timestamps) {
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
