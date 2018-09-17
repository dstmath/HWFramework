package com.android.server.usage;

import android.app.usage.TimeSparseArray;
import android.os.Build.VERSION;
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
    private static final String TAG = "UsageStatsDatabase";
    private final UnixCalendar mCal;
    private boolean mFirstUpdate;
    private final File[] mIntervalDirs;
    private final Object mLock;
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
        this.mLock = new Object();
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
                    i += BACKUP_VERSION;
                } else {
                    throw new IllegalStateException("Failed to create directory " + f.getAbsolutePath());
                }
            }
            checkVersionAndBuildLocked();
            indexFilesLocked();
            TimeSparseArray[] timeSparseArrayArr = this.mSortedStatFiles;
            length = timeSparseArrayArr.length;
            for (i = 0; i < length; i += BACKUP_VERSION) {
                TimeSparseArray<AtomicFile> files = timeSparseArrayArr[i];
                int startIndex = files.closestIndexOnOrAfter(currentTimeMillis);
                if (startIndex >= 0) {
                    int i2;
                    int fileCount = files.size();
                    for (i2 = startIndex; i2 < fileCount; i2 += BACKUP_VERSION) {
                        ((AtomicFile) files.valueAt(i2)).delete();
                    }
                    for (i2 = startIndex; i2 < fileCount; i2 += BACKUP_VERSION) {
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
            for (i = 0; i < fileCount - 1; i += BACKUP_VERSION) {
                if (((AtomicFile) files.valueAt(i)).getBaseFile().getPath().endsWith(CHECKED_IN_SUFFIX)) {
                    lastCheckin = i;
                }
            }
            int start = lastCheckin + BACKUP_VERSION;
            if (start == fileCount - 1) {
                return true;
            }
            try {
                IntervalStats stats = new IntervalStats();
                i = start;
                while (i < fileCount - 1) {
                    UsageStatsXml.read((AtomicFile) files.valueAt(i), stats);
                    if (checkinAction.checkin(stats)) {
                        i += BACKUP_VERSION;
                    } else {
                        return DEBUG;
                    }
                }
                i = start;
                while (i < fileCount - 1) {
                    AtomicFile file = (AtomicFile) files.valueAt(i);
                    File checkedInFile = new File(file.getBaseFile().getPath() + CHECKED_IN_SUFFIX);
                    if (file.getBaseFile().renameTo(checkedInFile)) {
                        files.setValueAt(i, new AtomicFile(checkedInFile));
                        i += BACKUP_VERSION;
                    } else {
                        Slog.e(TAG, "Failed to mark file " + file.getBaseFile().getPath() + " as checked-in");
                        return true;
                    }
                }
                return true;
            } catch (IOException e) {
                Slog.e(TAG, "Failed to check-in", e);
                return DEBUG;
            }
        }
    }

    private void indexFilesLocked() {
        FilenameFilter backupFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(UsageStatsDatabase.BAK_SUFFIX) ? UsageStatsDatabase.DEBUG : true;
            }
        };
        for (int i = 0; i < this.mSortedStatFiles.length; i += BACKUP_VERSION) {
            if (this.mSortedStatFiles[i] == null) {
                this.mSortedStatFiles[i] = new TimeSparseArray();
            } else {
                this.mSortedStatFiles[i].clear();
            }
            File[] files = this.mIntervalDirs[i].listFiles(backupFileFilter);
            if (files != null) {
                int length = files.length;
                for (int i2 = 0; i2 < length; i2 += BACKUP_VERSION) {
                    File f = files[i2];
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

    private void checkVersionAndBuildLocked() {
        Throwable th;
        BufferedWriter bufferedWriter;
        BufferedWriter writer;
        IOException e;
        Throwable th2;
        Throwable th3 = null;
        String currentFingerprint = getBuildFingerprint();
        this.mFirstUpdate = true;
        this.mNewUpdate = true;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.mVersionFile));
            int version;
            try {
                version = Integer.parseInt(reader.readLine());
                String buildFingerprint = reader.readLine();
                if (buildFingerprint != null) {
                    this.mFirstUpdate = DEBUG;
                }
                if (currentFingerprint.equals(buildFingerprint)) {
                    this.mNewUpdate = DEBUG;
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Throwable th4) {
                        th = th4;
                    }
                }
                th = null;
                if (th != null) {
                    try {
                        throw th;
                    } catch (NumberFormatException e2) {
                        bufferedReader = reader;
                    }
                } else {
                    if (version != CURRENT_VERSION) {
                        Slog.i(TAG, "Upgrading from version " + version + " to " + CURRENT_VERSION);
                        doUpgradeLocked(version);
                    }
                    if (version == CURRENT_VERSION || this.mNewUpdate) {
                        bufferedWriter = null;
                        try {
                            writer = new BufferedWriter(new FileWriter(this.mVersionFile));
                            try {
                                writer.write(Integer.toString(CURRENT_VERSION));
                                writer.write("\n");
                                writer.write(currentFingerprint);
                                writer.write("\n");
                                writer.flush();
                                if (writer != null) {
                                    try {
                                        writer.close();
                                    } catch (Throwable th5) {
                                        th3 = th5;
                                    }
                                }
                                if (th3 != null) {
                                    try {
                                        throw th3;
                                    } catch (IOException e3) {
                                        e = e3;
                                        bufferedWriter = writer;
                                    }
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                bufferedWriter = writer;
                                if (bufferedWriter != null) {
                                    try {
                                        bufferedWriter.close();
                                    } catch (Throwable th22) {
                                        if (th3 == null) {
                                            th3 = th22;
                                        } else if (th3 != th22) {
                                            th3.addSuppressed(th22);
                                        }
                                    }
                                }
                                if (th3 == null) {
                                    try {
                                        throw th3;
                                    } catch (IOException e4) {
                                        e = e4;
                                        Slog.e(TAG, "Failed to write new version");
                                        throw new RuntimeException(e);
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            if (bufferedWriter != null) {
                                bufferedWriter.close();
                            }
                            if (th3 == null) {
                                throw th;
                            }
                            throw th3;
                        }
                    }
                }
            } catch (Throwable th8) {
                th = th8;
                th22 = null;
                bufferedReader = reader;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th9) {
                        if (th22 == null) {
                            th22 = th9;
                        } else if (th22 != th9) {
                            th22.addSuppressed(th9);
                        }
                    }
                }
                if (th22 == null) {
                    throw th;
                }
                try {
                    throw th22;
                } catch (NumberFormatException e5) {
                    version = 0;
                    if (version != CURRENT_VERSION) {
                        Slog.i(TAG, "Upgrading from version " + version + " to " + CURRENT_VERSION);
                        doUpgradeLocked(version);
                    }
                    if (version == CURRENT_VERSION) {
                    }
                    bufferedWriter = null;
                    writer = new BufferedWriter(new FileWriter(this.mVersionFile));
                    writer.write(Integer.toString(CURRENT_VERSION));
                    writer.write("\n");
                    writer.write(currentFingerprint);
                    writer.write("\n");
                    writer.flush();
                    if (writer != null) {
                        writer.close();
                    }
                    if (th3 != null) {
                        throw th3;
                    }
                }
            }
        } catch (Throwable th10) {
            th = th10;
            th22 = null;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (th22 == null) {
                throw th22;
            }
            throw th;
        }
    }

    private String getBuildFingerprint() {
        return VERSION.RELEASE + ";" + VERSION.CODENAME + ";" + VERSION.INCREMENTAL;
    }

    private void doUpgradeLocked(int thisVersion) {
        if (thisVersion < 2) {
            Slog.i(TAG, "Deleting all usage stats files");
            for (int i = 0; i < this.mIntervalDirs.length; i += BACKUP_VERSION) {
                File[] files = this.mIntervalDirs[i].listFiles();
                if (files != null) {
                    int length = files.length;
                    for (int i2 = 0; i2 < length; i2 += BACKUP_VERSION) {
                        files[i2].delete();
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onTimeChanged(long timeDiffMillis) {
        synchronized (this.mLock) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Time changed by ");
            TimeUtils.formatDuration(timeDiffMillis, logBuilder);
            logBuilder.append(".");
            int filesDeleted = 0;
            int filesMoved = 0;
            TimeSparseArray[] timeSparseArrayArr = this.mSortedStatFiles;
            int length = timeSparseArrayArr.length;
            loop0:
            for (int i = 0; i < length; i += BACKUP_VERSION) {
                TimeSparseArray<AtomicFile> files = timeSparseArrayArr[i];
                int fileCount = files.size();
                for (int i2 = 0; i2 < fileCount; i2 += BACKUP_VERSION) {
                    AtomicFile file = (AtomicFile) files.valueAt(i2);
                    long newTime = files.keyAt(i2) + timeDiffMillis;
                    if (newTime < 0) {
                        filesDeleted += BACKUP_VERSION;
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
                        File newFile = new File(file.getBaseFile().getParentFile(), newName);
                        filesMoved += BACKUP_VERSION;
                        file.getBaseFile().renameTo(newFile);
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
                    for (int i = startIndex; i <= endIndex; i += BACKUP_VERSION) {
                        try {
                            UsageStatsXml.read((AtomicFile) intervalStats.valueAt(i), stats);
                            if (beginTime < stats.endTime) {
                                combiner.combine(stats, DEBUG, results);
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
            pruneFilesOlderThan(this.mIntervalDirs[CURRENT_VERSION], this.mCal.getTimeInMillis());
            this.mCal.setTimeInMillis(currentTimeMillis);
            this.mCal.addMonths(-6);
            pruneFilesOlderThan(this.mIntervalDirs[2], this.mCal.getTimeInMillis());
            this.mCal.setTimeInMillis(currentTimeMillis);
            this.mCal.addWeeks(-4);
            pruneFilesOlderThan(this.mIntervalDirs[BACKUP_VERSION], this.mCal.getTimeInMillis());
            this.mCal.setTimeInMillis(currentTimeMillis);
            this.mCal.addDays(-7);
            pruneFilesOlderThan(this.mIntervalDirs[0], this.mCal.getTimeInMillis());
            indexFilesLocked();
        }
    }

    private static void pruneFilesOlderThan(File dir, long expiryTime) {
        File[] files = dir.listFiles();
        if (files != null) {
            int length = files.length;
            for (int i = 0; i < length; i += BACKUP_VERSION) {
                long beginTime;
                File f = files[i];
                String path = f.getPath();
                if (path.endsWith(BAK_SUFFIX)) {
                    f = new File(path.substring(0, path.length() - BAK_SUFFIX.length()));
                }
                try {
                    beginTime = UsageStatsXml.parseBeginTime(f);
                } catch (IOException e) {
                    beginTime = 0;
                }
                if (beginTime < expiryTime) {
                    new AtomicFile(f).delete();
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
                    out.writeInt(BACKUP_VERSION);
                    out.writeInt(this.mSortedStatFiles[0].size());
                    for (i = 0; i < this.mSortedStatFiles[0].size(); i += BACKUP_VERSION) {
                        writeIntervalStatsToStream(out, (AtomicFile) this.mSortedStatFiles[0].valueAt(i));
                    }
                    out.writeInt(this.mSortedStatFiles[BACKUP_VERSION].size());
                    for (i = 0; i < this.mSortedStatFiles[BACKUP_VERSION].size(); i += BACKUP_VERSION) {
                        writeIntervalStatsToStream(out, (AtomicFile) this.mSortedStatFiles[BACKUP_VERSION].valueAt(i));
                    }
                    out.writeInt(this.mSortedStatFiles[2].size());
                    for (i = 0; i < this.mSortedStatFiles[2].size(); i += BACKUP_VERSION) {
                        writeIntervalStatsToStream(out, (AtomicFile) this.mSortedStatFiles[2].valueAt(i));
                    }
                    out.writeInt(this.mSortedStatFiles[CURRENT_VERSION].size());
                    for (i = 0; i < this.mSortedStatFiles[CURRENT_VERSION].size(); i += BACKUP_VERSION) {
                        writeIntervalStatsToStream(out, (AtomicFile) this.mSortedStatFiles[CURRENT_VERSION].valueAt(i));
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
                IntervalStats weeklyConfigSource = getLatestUsageStats(BACKUP_VERSION);
                IntervalStats monthlyConfigSource = getLatestUsageStats(2);
                IntervalStats yearlyConfigSource = getLatestUsageStats(CURRENT_VERSION);
                try {
                    DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
                    int backupDataVersion = in.readInt();
                    if (backupDataVersion < BACKUP_VERSION || backupDataVersion > BACKUP_VERSION) {
                        indexFilesLocked();
                        return;
                    }
                    int i;
                    for (i = 0; i < this.mIntervalDirs.length; i += BACKUP_VERSION) {
                        deleteDirectoryContents(this.mIntervalDirs[i]);
                    }
                    int fileCount = in.readInt();
                    for (i = 0; i < fileCount; i += BACKUP_VERSION) {
                        putUsageStats(0, mergeStats(deserializeIntervalStats(getIntervalStatsBytes(in)), dailyConfigSource));
                    }
                    fileCount = in.readInt();
                    for (i = 0; i < fileCount; i += BACKUP_VERSION) {
                        putUsageStats(BACKUP_VERSION, mergeStats(deserializeIntervalStats(getIntervalStatsBytes(in)), weeklyConfigSource));
                    }
                    fileCount = in.readInt();
                    for (i = 0; i < fileCount; i += BACKUP_VERSION) {
                        putUsageStats(2, mergeStats(deserializeIntervalStats(getIntervalStatsBytes(in)), monthlyConfigSource));
                    }
                    fileCount = in.readInt();
                    for (i = 0; i < fileCount; i += BACKUP_VERSION) {
                        putUsageStats(CURRENT_VERSION, mergeStats(deserializeIntervalStats(getIntervalStatsBytes(in)), yearlyConfigSource));
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
        File[] files = directory.listFiles();
        int length = files.length;
        for (int i = 0; i < length; i += BACKUP_VERSION) {
            deleteDirectory(files[i]);
        }
    }

    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            int length = files.length;
            for (int i = 0; i < length; i += BACKUP_VERSION) {
                File file = files[i];
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
