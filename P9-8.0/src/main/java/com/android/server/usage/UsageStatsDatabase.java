package com.android.server.usage;

import android.app.usage.TimeSparseArray;
import android.app.usage.UsageStats;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.server.job.controllers.JobStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

class UsageStatsDatabase {
    static final int BACKUP_VERSION = 1;
    private static final String BAK_SUFFIX = ".bak";
    private static final String CHECKED_IN_SUFFIX = "-c";
    private static final int CURRENT_VERSION = 3;
    private static final boolean DEBUG = false;
    static final String KEY_USAGE_STATS = "usage_stats";
    private static final String RETENTION_LEN_KEY = "ro.usagestats.chooser.retention";
    private static final int SELECTION_LOG_RETENTION_LEN = SystemProperties.getInt(RETENTION_LEN_KEY, 14);
    private static final String TAG = "UsageStatsDatabase";
    private final UnixCalendar mCal;
    private boolean mFirstUpdate;
    private final File[] mIntervalDirs;
    private final Object mLock = new Object();
    private boolean mNewUpdate;
    private final TimeSparseArray<AtomicFile>[] mSortedStatFiles;
    private final File mVersionFile;

    public interface CheckinAction {
        boolean checkin(IntervalStats intervalStats);
    }

    interface StatCombiner<T> {
        void combine(IntervalStats intervalStats, boolean z, List<T> list);
    }

    public UsageStatsDatabase(File dir) {
        this.mIntervalDirs = new File[]{new File(dir, "daily"), new File(dir, "weekly"), new File(dir, "monthly"), new File(dir, "yearly")};
        this.mVersionFile = new File(dir, "version");
        this.mSortedStatFiles = new TimeSparseArray[this.mIntervalDirs.length];
        this.mCal = new UnixCalendar(0);
    }

    public void init(long currentTimeMillis) {
        synchronized (this.mLock) {
            File[] fileArr = this.mIntervalDirs;
            int length = fileArr.length;
            int i = 0;
            while (i < length) {
                File f = fileArr[i];
                f.mkdirs();
                if (f.exists()) {
                    i++;
                } else {
                    throw new IllegalStateException("Failed to create directory " + f.getAbsolutePath());
                }
            }
            checkVersionAndBuildLocked();
            indexFilesLocked();
            for (TimeSparseArray<AtomicFile> files : this.mSortedStatFiles) {
                int startIndex = files.closestIndexOnOrAfter(currentTimeMillis);
                if (startIndex >= 0) {
                    int i2;
                    int fileCount = files.size();
                    for (i2 = startIndex; i2 < fileCount; i2++) {
                        ((AtomicFile) files.valueAt(i2)).delete();
                    }
                    for (i2 = startIndex; i2 < fileCount; i2++) {
                        files.removeAt(i2);
                    }
                }
            }
        }
    }

    public boolean checkinDailyFiles(CheckinAction checkinAction) {
        synchronized (this.mLock) {
            int i;
            TimeSparseArray<AtomicFile> files = this.mSortedStatFiles[0];
            int fileCount = files.size();
            int lastCheckin = -1;
            for (i = 0; i < fileCount - 1; i++) {
                if (((AtomicFile) files.valueAt(i)).getBaseFile().getPath().endsWith(CHECKED_IN_SUFFIX)) {
                    lastCheckin = i;
                }
            }
            int start = lastCheckin + 1;
            if (start == fileCount - 1) {
                return true;
            }
            try {
                IntervalStats stats = new IntervalStats();
                i = start;
                while (i < fileCount - 1) {
                    UsageStatsXml.read((AtomicFile) files.valueAt(i), stats);
                    if (checkinAction.checkin(stats)) {
                        i++;
                    } else {
                        return false;
                    }
                }
                i = start;
                while (i < fileCount - 1) {
                    AtomicFile file = (AtomicFile) files.valueAt(i);
                    File checkedInFile = new File(file.getBaseFile().getPath() + CHECKED_IN_SUFFIX);
                    if (file.getBaseFile().renameTo(checkedInFile)) {
                        files.setValueAt(i, new AtomicFile(checkedInFile));
                        i++;
                    } else {
                        Slog.e(TAG, "Failed to mark file " + file.getBaseFile().getPath() + " as checked-in");
                        return true;
                    }
                }
                return true;
            } catch (IOException e) {
                Slog.e(TAG, "Failed to check-in", e);
                return false;
            }
        }
    }

    private void indexFilesLocked() {
        FilenameFilter backupFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(UsageStatsDatabase.BAK_SUFFIX) ^ 1;
            }
        };
        for (int i = 0; i < this.mSortedStatFiles.length; i++) {
            if (this.mSortedStatFiles[i] == null) {
                this.mSortedStatFiles[i] = new TimeSparseArray();
            } else {
                this.mSortedStatFiles[i].clear();
            }
            File[] files = this.mIntervalDirs[i].listFiles(backupFileFilter);
            if (files != null) {
                for (File f : files) {
                    AtomicFile af = new AtomicFile(f);
                    try {
                        this.mSortedStatFiles[i].put(UsageStatsXml.parseBeginTime(af), af);
                    } catch (IOException e) {
                        Slog.e(TAG, "failed to index file: " + f, e);
                    }
                }
            }
        }
    }

    boolean isFirstUpdate() {
        return this.mFirstUpdate;
    }

    boolean isNewUpdate() {
        return this.mNewUpdate;
    }

    /* JADX WARNING: Removed duplicated region for block: B:68:0x00d8 A:{SYNTHETIC, Splitter: B:68:0x00d8} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00eb A:{Catch:{ IOException -> 0x00de }} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x00dd A:{SYNTHETIC, Splitter: B:71:0x00dd} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ba A:{SYNTHETIC, Splitter: B:49:0x00ba} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00ce A:{Catch:{ NumberFormatException -> 0x00c0, NumberFormatException -> 0x00c0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00bf A:{SYNTHETIC, Splitter: B:52:0x00bf} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x003b A:{Splitter: B:15:0x003a, ExcHandler: java.lang.NumberFormatException (e java.lang.NumberFormatException)} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0097 A:{SYNTHETIC, Splitter: B:32:0x0097} */
    /* JADX WARNING: Removed duplicated region for block: B:92:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009c A:{SYNTHETIC, Splitter: B:35:0x009c} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00c0 A:{Splitter: B:52:0x00bf, ExcHandler: java.lang.NumberFormatException (e java.lang.NumberFormatException), Catch:{ NumberFormatException -> 0x00c0, NumberFormatException -> 0x00c0 }} */
    /* JADX WARNING: Missing block: B:18:0x003c, code:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:19:0x003d, code:
            r6 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkVersionAndBuildLocked() {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        String currentFingerprint = getBuildFingerprint();
        this.mFirstUpdate = true;
        this.mNewUpdate = true;
        BufferedReader reader = null;
        Throwable th3;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(this.mVersionFile));
            try {
                int version = Integer.parseInt(reader2.readLine());
                String buildFingerprint = reader2.readLine();
                if (buildFingerprint != null) {
                    this.mFirstUpdate = false;
                }
                if (currentFingerprint.equals(buildFingerprint)) {
                    this.mNewUpdate = false;
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Throwable th4) {
                        th3 = th4;
                    }
                }
                th3 = null;
                if (th3 != null) {
                    try {
                        throw th3;
                    } catch (NumberFormatException e2) {
                    }
                } else {
                    if (version != 3) {
                        Slog.i(TAG, "Upgrading from version " + version + " to " + 3);
                        doUpgradeLocked(version);
                    }
                    if (version == 3 || this.mNewUpdate) {
                        BufferedWriter writer = null;
                        try {
                            BufferedWriter writer2 = new BufferedWriter(new FileWriter(this.mVersionFile));
                            try {
                                writer2.write(Integer.toString(3));
                                writer2.write("\n");
                                writer2.write(currentFingerprint);
                                writer2.write("\n");
                                writer2.flush();
                                if (writer2 != null) {
                                    try {
                                        writer2.close();
                                    } catch (Throwable th5) {
                                        th2 = th5;
                                    }
                                }
                                if (th2 == null) {
                                    try {
                                        throw th2;
                                    } catch (IOException e3) {
                                        e = e3;
                                        writer = writer2;
                                    }
                                }
                            } catch (Throwable th6) {
                                th3 = th6;
                                writer = writer2;
                                if (writer != null) {
                                    try {
                                        writer.close();
                                    } catch (Throwable th7) {
                                        if (th2 == null) {
                                            th2 = th7;
                                        } else if (th2 != th7) {
                                            th2.addSuppressed(th7);
                                        }
                                    }
                                }
                                if (th2 == null) {
                                    try {
                                        throw th2;
                                    } catch (IOException e4) {
                                        e = e4;
                                        Slog.e(TAG, "Failed to write new version");
                                        throw new RuntimeException(e);
                                    }
                                }
                                throw th3;
                            }
                        } catch (Throwable th8) {
                            th3 = th8;
                            if (writer != null) {
                            }
                            if (th2 == null) {
                            }
                        }
                    }
                }
            } catch (Throwable th9) {
                th3 = th9;
                reader = reader2;
                th7 = null;
                if (reader != null) {
                }
                if (th7 == null) {
                }
            }
        } catch (Throwable th10) {
            th3 = th10;
            th7 = null;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable th11) {
                    if (th7 == null) {
                        th7 = th11;
                    } else if (th7 != th11) {
                        th7.addSuppressed(th11);
                    }
                }
            }
            if (th7 == null) {
                try {
                    throw th7;
                } catch (NumberFormatException e5) {
                }
            } else {
                throw th3;
            }
        }
    }

    private String getBuildFingerprint() {
        return VERSION.RELEASE + ";" + VERSION.CODENAME + ";" + VERSION.INCREMENTAL;
    }

    private void doUpgradeLocked(int thisVersion) {
        if (thisVersion < 2) {
            Slog.i(TAG, "Deleting all usage stats files");
            for (File listFiles : this.mIntervalDirs) {
                File[] files = listFiles.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
        }
    }

    public void onTimeChanged(long timeDiffMillis) {
        synchronized (this.mLock) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Time changed by ");
            TimeUtils.formatDuration(timeDiffMillis, logBuilder);
            logBuilder.append(".");
            int filesDeleted = 0;
            int filesMoved = 0;
            for (TimeSparseArray<AtomicFile> files : this.mSortedStatFiles) {
                int fileCount = files.size();
                for (int i = 0; i < fileCount; i++) {
                    AtomicFile file = (AtomicFile) files.valueAt(i);
                    long newTime = files.keyAt(i) + timeDiffMillis;
                    if (newTime < 0) {
                        filesDeleted++;
                        file.delete();
                    } else {
                        try {
                            file.openRead().close();
                        } catch (IOException e) {
                        }
                        String newName = Long.toString(newTime);
                        if (file.getBaseFile().getName().endsWith(CHECKED_IN_SUFFIX)) {
                            newName = newName + CHECKED_IN_SUFFIX;
                        }
                        filesMoved++;
                        file.getBaseFile().renameTo(new File(file.getBaseFile().getParentFile(), newName));
                    }
                }
                files.clear();
            }
            logBuilder.append(" files deleted: ").append(filesDeleted);
            logBuilder.append(" files moved: ").append(filesMoved);
            Slog.i(TAG, logBuilder.toString());
            indexFilesLocked();
        }
    }

    public IntervalStats getLatestUsageStats(int intervalType) {
        synchronized (this.mLock) {
            if (intervalType >= 0) {
                if (intervalType < this.mIntervalDirs.length) {
                    int fileCount = this.mSortedStatFiles[intervalType].size();
                    if (fileCount == 0) {
                        return null;
                    }
                    try {
                        AtomicFile f = (AtomicFile) this.mSortedStatFiles[intervalType].valueAt(fileCount - 1);
                        IntervalStats stats = new IntervalStats();
                        UsageStatsXml.read(f, stats);
                        return stats;
                    } catch (IOException e) {
                        Slog.e(TAG, "Failed to read usage stats file", e);
                        return null;
                    }
                }
            }
            throw new IllegalArgumentException("Bad interval type " + intervalType);
        }
    }

    public long getLatestUsageStatsBeginTime(int intervalType) {
        synchronized (this.mLock) {
            if (intervalType >= 0) {
                if (intervalType < this.mIntervalDirs.length) {
                    int statsFileCount = this.mSortedStatFiles[intervalType].size();
                    if (statsFileCount > 0) {
                        long keyAt = this.mSortedStatFiles[intervalType].keyAt(statsFileCount - 1);
                        return keyAt;
                    }
                    return -1;
                }
            }
            throw new IllegalArgumentException("Bad interval type " + intervalType);
        }
    }

    public <T> List<T> queryUsageStats(int intervalType, long beginTime, long endTime, StatCombiner<T> combiner) {
        synchronized (this.mLock) {
            if (intervalType >= 0) {
                if (intervalType < this.mIntervalDirs.length) {
                    TimeSparseArray<AtomicFile> intervalStats = this.mSortedStatFiles[intervalType];
                    if (endTime <= beginTime) {
                        return null;
                    }
                    int startIndex = intervalStats.closestIndexOnOrBefore(beginTime);
                    if (startIndex < 0) {
                        startIndex = 0;
                    }
                    int endIndex = intervalStats.closestIndexOnOrBefore(endTime);
                    if (endIndex < 0) {
                        return null;
                    }
                    if (intervalStats.keyAt(endIndex) == endTime) {
                        endIndex--;
                        if (endIndex < 0) {
                            return null;
                        }
                    }
                    IntervalStats stats = new IntervalStats();
                    ArrayList<T> results = new ArrayList();
                    for (int i = startIndex; i <= endIndex; i++) {
                        try {
                            UsageStatsXml.read((AtomicFile) intervalStats.valueAt(i), stats);
                            if (beginTime < stats.endTime) {
                                combiner.combine(stats, false, results);
                            }
                        } catch (IOException e) {
                            Slog.e(TAG, "Failed to read usage stats file", e);
                        }
                    }
                    return results;
                }
            }
            throw new IllegalArgumentException("Bad interval type " + intervalType);
        }
    }

    public int findBestFitBucket(long beginTimeStamp, long endTimeStamp) {
        int bestBucket;
        synchronized (this.mLock) {
            bestBucket = -1;
            long smallestDiff = JobStatus.NO_LATEST_RUNTIME;
            for (int i = this.mSortedStatFiles.length - 1; i >= 0; i--) {
                int index = this.mSortedStatFiles[i].closestIndexOnOrBefore(beginTimeStamp);
                int size = this.mSortedStatFiles[i].size();
                if (index >= 0 && index < size) {
                    long diff = Math.abs(this.mSortedStatFiles[i].keyAt(index) - beginTimeStamp);
                    if (diff < smallestDiff) {
                        smallestDiff = diff;
                        bestBucket = i;
                    }
                }
            }
        }
        return bestBucket;
    }

    public void prune(long currentTimeMillis) {
        synchronized (this.mLock) {
            this.mCal.setTimeInMillis(currentTimeMillis);
            this.mCal.addYears(-3);
            pruneFilesOlderThan(this.mIntervalDirs[3], this.mCal.getTimeInMillis());
            this.mCal.setTimeInMillis(currentTimeMillis);
            this.mCal.addMonths(-6);
            pruneFilesOlderThan(this.mIntervalDirs[2], this.mCal.getTimeInMillis());
            this.mCal.setTimeInMillis(currentTimeMillis);
            this.mCal.addWeeks(-4);
            pruneFilesOlderThan(this.mIntervalDirs[1], this.mCal.getTimeInMillis());
            this.mCal.setTimeInMillis(currentTimeMillis);
            this.mCal.addDays(-7);
            pruneFilesOlderThan(this.mIntervalDirs[0], this.mCal.getTimeInMillis());
            this.mCal.setTimeInMillis(currentTimeMillis);
            this.mCal.addDays(-SELECTION_LOG_RETENTION_LEN);
            for (File pruneChooserCountsOlderThan : this.mIntervalDirs) {
                pruneChooserCountsOlderThan(pruneChooserCountsOlderThan, this.mCal.getTimeInMillis());
            }
            indexFilesLocked();
        }
    }

    private static void pruneFilesOlderThan(File dir, long expiryTime) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                File f2;
                long beginTime;
                String path = f2.getPath();
                if (path.endsWith(BAK_SUFFIX)) {
                    f2 = new File(path.substring(0, path.length() - BAK_SUFFIX.length()));
                }
                try {
                    beginTime = UsageStatsXml.parseBeginTime(f2);
                } catch (IOException e) {
                    beginTime = 0;
                }
                if (beginTime < expiryTime) {
                    new AtomicFile(f2).delete();
                }
            }
        }
    }

    private static void pruneChooserCountsOlderThan(File dir, long expiryTime) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                File f2;
                long beginTime;
                String path = f2.getPath();
                if (path.endsWith(BAK_SUFFIX)) {
                    f2 = new File(path.substring(0, path.length() - BAK_SUFFIX.length()));
                }
                try {
                    beginTime = UsageStatsXml.parseBeginTime(f2);
                } catch (IOException e) {
                    beginTime = 0;
                }
                if (beginTime < expiryTime) {
                    try {
                        AtomicFile af = new AtomicFile(f2);
                        IntervalStats stats = new IntervalStats();
                        UsageStatsXml.read(af, stats);
                        int pkgCount = stats.packageStats.size();
                        for (int i = 0; i < pkgCount; i++) {
                            UsageStats pkgStats = (UsageStats) stats.packageStats.valueAt(i);
                            if (!(pkgStats == null || pkgStats.mChooserCounts == null)) {
                                pkgStats.mChooserCounts.clear();
                            }
                        }
                        UsageStatsXml.write(af, stats);
                    } catch (IOException e2) {
                        Slog.e(TAG, "Failed to delete chooser counts from usage stats file", e2);
                    }
                }
            }
        }
    }

    public void putUsageStats(int intervalType, IntervalStats stats) throws IOException {
        if (stats != null) {
            synchronized (this.mLock) {
                if (intervalType >= 0) {
                    if (intervalType < this.mIntervalDirs.length) {
                        AtomicFile f = (AtomicFile) this.mSortedStatFiles[intervalType].get(stats.beginTime);
                        if (f == null) {
                            f = new AtomicFile(new File(this.mIntervalDirs[intervalType], Long.toString(stats.beginTime)));
                            this.mSortedStatFiles[intervalType].put(stats.beginTime, f);
                        }
                        UsageStatsXml.write(f, stats);
                        stats.lastTimeSaved = f.getLastModifiedTime();
                    }
                }
                throw new IllegalArgumentException("Bad interval type " + intervalType);
            }
        }
    }

    byte[] getBackupPayload(String key) {
        byte[] toByteArray;
        synchronized (this.mLock) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (KEY_USAGE_STATS.equals(key)) {
                prune(System.currentTimeMillis());
                DataOutputStream out = new DataOutputStream(baos);
                try {
                    int i;
                    out.writeInt(1);
                    out.writeInt(this.mSortedStatFiles[0].size());
                    for (i = 0; i < this.mSortedStatFiles[0].size(); i++) {
                        writeIntervalStatsToStream(out, (AtomicFile) this.mSortedStatFiles[0].valueAt(i));
                    }
                    out.writeInt(this.mSortedStatFiles[1].size());
                    for (i = 0; i < this.mSortedStatFiles[1].size(); i++) {
                        writeIntervalStatsToStream(out, (AtomicFile) this.mSortedStatFiles[1].valueAt(i));
                    }
                    out.writeInt(this.mSortedStatFiles[2].size());
                    for (i = 0; i < this.mSortedStatFiles[2].size(); i++) {
                        writeIntervalStatsToStream(out, (AtomicFile) this.mSortedStatFiles[2].valueAt(i));
                    }
                    out.writeInt(this.mSortedStatFiles[3].size());
                    for (i = 0; i < this.mSortedStatFiles[3].size(); i++) {
                        writeIntervalStatsToStream(out, (AtomicFile) this.mSortedStatFiles[3].valueAt(i));
                    }
                } catch (IOException ioe) {
                    Slog.d(TAG, "Failed to write data to output stream", ioe);
                    baos.reset();
                }
            }
            toByteArray = baos.toByteArray();
        }
        return toByteArray;
    }

    void applyRestoredPayload(String key, byte[] payload) {
        synchronized (this.mLock) {
            if (KEY_USAGE_STATS.equals(key)) {
                IntervalStats dailyConfigSource = getLatestUsageStats(0);
                IntervalStats weeklyConfigSource = getLatestUsageStats(1);
                IntervalStats monthlyConfigSource = getLatestUsageStats(2);
                IntervalStats yearlyConfigSource = getLatestUsageStats(3);
                try {
                    DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
                    int backupDataVersion = in.readInt();
                    if (backupDataVersion < 1 || backupDataVersion > 1) {
                        indexFilesLocked();
                        return;
                    }
                    int i;
                    for (File deleteDirectoryContents : this.mIntervalDirs) {
                        deleteDirectoryContents(deleteDirectoryContents);
                    }
                    int fileCount = in.readInt();
                    for (i = 0; i < fileCount; i++) {
                        putUsageStats(0, mergeStats(deserializeIntervalStats(getIntervalStatsBytes(in)), dailyConfigSource));
                    }
                    fileCount = in.readInt();
                    for (i = 0; i < fileCount; i++) {
                        putUsageStats(1, mergeStats(deserializeIntervalStats(getIntervalStatsBytes(in)), weeklyConfigSource));
                    }
                    fileCount = in.readInt();
                    for (i = 0; i < fileCount; i++) {
                        putUsageStats(2, mergeStats(deserializeIntervalStats(getIntervalStatsBytes(in)), monthlyConfigSource));
                    }
                    fileCount = in.readInt();
                    for (i = 0; i < fileCount; i++) {
                        putUsageStats(3, mergeStats(deserializeIntervalStats(getIntervalStatsBytes(in)), yearlyConfigSource));
                    }
                    indexFilesLocked();
                } catch (IOException ioe) {
                    Slog.d(TAG, "Failed to read data from input stream", ioe);
                    indexFilesLocked();
                } catch (Throwable th) {
                    indexFilesLocked();
                }
            }
        }
    }

    private IntervalStats mergeStats(IntervalStats beingRestored, IntervalStats onDevice) {
        if (onDevice == null) {
            return beingRestored;
        }
        if (beingRestored == null) {
            return null;
        }
        beingRestored.activeConfiguration = onDevice.activeConfiguration;
        beingRestored.configurations.putAll(onDevice.configurations);
        beingRestored.events = onDevice.events;
        return beingRestored;
    }

    private void writeIntervalStatsToStream(DataOutputStream out, AtomicFile statsFile) throws IOException {
        IntervalStats stats = new IntervalStats();
        try {
            UsageStatsXml.read(statsFile, stats);
            sanitizeIntervalStatsForBackup(stats);
            byte[] data = serializeIntervalStats(stats);
            out.writeInt(data.length);
            out.write(data);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to read usage stats file", e);
            out.writeInt(0);
        }
    }

    private static byte[] getIntervalStatsBytes(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] buffer = new byte[length];
        in.read(buffer, 0, length);
        return buffer;
    }

    private static void sanitizeIntervalStatsForBackup(IntervalStats stats) {
        if (stats != null) {
            stats.activeConfiguration = null;
            stats.configurations.clear();
            if (stats.events != null) {
                stats.events.clear();
            }
        }
    }

    private static byte[] serializeIntervalStats(IntervalStats stats) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = new DataOutputStream(baos);
        try {
            out.writeLong(stats.beginTime);
            UsageStatsXml.write(out, stats);
        } catch (IOException ioe) {
            Slog.d(TAG, "Serializing IntervalStats Failed", ioe);
            baos.reset();
        }
        return baos.toByteArray();
    }

    private static IntervalStats deserializeIntervalStats(byte[] data) {
        InputStream in = new DataInputStream(new ByteArrayInputStream(data));
        IntervalStats stats = new IntervalStats();
        try {
            stats.beginTime = in.readLong();
            UsageStatsXml.read(in, stats);
            return stats;
        } catch (IOException ioe) {
            Slog.d(TAG, "DeSerializing IntervalStats Failed", ioe);
            return null;
        }
    }

    private static void deleteDirectoryContents(File directory) {
        for (File file : directory.listFiles()) {
            deleteDirectory(file);
        }
    }

    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
