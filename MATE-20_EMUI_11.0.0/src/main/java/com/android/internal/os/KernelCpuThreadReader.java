package com.android.internal.os;

import android.os.Process;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

public class KernelCpuThreadReader {
    private static final String CPU_STATISTICS_FILENAME = "time_in_state";
    private static final boolean DEBUG = false;
    private static final Path DEFAULT_INITIAL_TIME_IN_STATE_PATH = DEFAULT_PROC_PATH.resolve("self/time_in_state");
    private static final String DEFAULT_PROCESS_NAME = "unknown_process";
    private static final Path DEFAULT_PROC_PATH = Paths.get("/proc", new String[0]);
    private static final String DEFAULT_THREAD_NAME = "unknown_thread";
    private static final int ID_ERROR = -1;
    private static final String PROCESS_DIRECTORY_FILTER = "[0-9]*";
    private static final String PROCESS_NAME_FILENAME = "cmdline";
    private static final String TAG = "KernelCpuThreadReader";
    private static final String THREAD_NAME_FILENAME = "comm";
    private int[] mFrequenciesKhz;
    private FrequencyBucketCreator mFrequencyBucketCreator;
    private final Injector mInjector;
    private final Path mProcPath;
    private final ProcTimeInStateReader mProcTimeInStateReader;
    private Predicate<Integer> mUidPredicate;

    @VisibleForTesting
    public KernelCpuThreadReader(int numBuckets, Predicate<Integer> uidPredicate, Path procPath, Path initialTimeInStatePath, Injector injector) throws IOException {
        this.mUidPredicate = uidPredicate;
        this.mProcPath = procPath;
        this.mProcTimeInStateReader = new ProcTimeInStateReader(initialTimeInStatePath);
        this.mInjector = injector;
        setNumBuckets(numBuckets);
    }

    public static KernelCpuThreadReader create(int numBuckets, Predicate<Integer> uidPredicate) {
        try {
            return new KernelCpuThreadReader(numBuckets, uidPredicate, DEFAULT_PROC_PATH, DEFAULT_INITIAL_TIME_IN_STATE_PATH, new Injector());
        } catch (IOException e) {
            Slog.e(TAG, "Failed to initialize KernelCpuThreadReader", e);
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005d, code lost:
        if (r3 != null) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005f, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0062, code lost:
        throw r5;
     */
    public ArrayList<ProcessCpuUsage> getProcessCpuUsage() {
        ArrayList<ProcessCpuUsage> processCpuUsages = new ArrayList<>();
        try {
            DirectoryStream<Path> processPaths = Files.newDirectoryStream(this.mProcPath, PROCESS_DIRECTORY_FILTER);
            for (Path processPath : processPaths) {
                int processId = getProcessId(processPath);
                int uid = this.mInjector.getUidForPid(processId);
                if (uid != -1) {
                    if (processId != -1) {
                        if (this.mUidPredicate.test(Integer.valueOf(uid))) {
                            ProcessCpuUsage processCpuUsage = getProcessCpuUsage(processPath, processId, uid);
                            if (processCpuUsage != null) {
                                processCpuUsages.add(processCpuUsage);
                            }
                        }
                    }
                }
            }
            $closeResource(null, processPaths);
            if (!processCpuUsages.isEmpty()) {
                return processCpuUsages;
            }
            Slog.w(TAG, "Didn't successfully get any process CPU information for UIDs specified");
            return null;
        } catch (IOException e) {
            Slog.w(TAG, "Failed to iterate over process paths", e);
            return null;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public int[] getCpuFrequenciesKhz() {
        return this.mFrequenciesKhz;
    }

    /* access modifiers changed from: package-private */
    public void setNumBuckets(int numBuckets) {
        if (numBuckets < 1) {
            Slog.w(TAG, "Number of buckets must be at least 1, but was " + numBuckets);
            return;
        }
        int[] iArr = this.mFrequenciesKhz;
        if (iArr == null || iArr.length != numBuckets) {
            this.mFrequencyBucketCreator = new FrequencyBucketCreator(this.mProcTimeInStateReader.getFrequenciesKhz(), numBuckets);
            this.mFrequenciesKhz = this.mFrequencyBucketCreator.bucketFrequencies(this.mProcTimeInStateReader.getFrequenciesKhz());
        }
    }

    /* access modifiers changed from: package-private */
    public void setUidPredicate(Predicate<Integer> uidPredicate) {
        this.mUidPredicate = uidPredicate;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0044, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0045, code lost:
        if (r3 != null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0047, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004a, code lost:
        throw r5;
     */
    private ProcessCpuUsage getProcessCpuUsage(Path processPath, int processId, int uid) {
        Path allThreadsPath = processPath.resolve("task");
        ArrayList<ThreadCpuUsage> threadCpuUsages = new ArrayList<>();
        try {
            DirectoryStream<Path> threadPaths = Files.newDirectoryStream(allThreadsPath);
            for (Path threadDirectory : threadPaths) {
                ThreadCpuUsage threadCpuUsage = getThreadCpuUsage(threadDirectory);
                if (threadCpuUsage != null) {
                    threadCpuUsages.add(threadCpuUsage);
                }
            }
            $closeResource(null, threadPaths);
            if (threadCpuUsages.isEmpty()) {
                return null;
            }
            return new ProcessCpuUsage(processId, getProcessName(processPath), uid, threadCpuUsages);
        } catch (IOException e) {
            return null;
        }
    }

    private ThreadCpuUsage getThreadCpuUsage(Path threadDirectory) {
        try {
            int threadId = Integer.parseInt(threadDirectory.getFileName().toString());
            String threadName = getThreadName(threadDirectory);
            long[] cpuUsagesLong = this.mProcTimeInStateReader.getUsageTimesMillis(threadDirectory.resolve(CPU_STATISTICS_FILENAME));
            if (cpuUsagesLong == null) {
                return null;
            }
            return new ThreadCpuUsage(threadId, threadName, this.mFrequencyBucketCreator.bucketValues(cpuUsagesLong));
        } catch (NumberFormatException e) {
            Slog.w(TAG, "Failed to parse thread ID when iterating over /proc/*/task", e);
            return null;
        }
    }

    private String getProcessName(Path processPath) {
        String processName = ProcStatsUtil.readSingleLineProcFile(processPath.resolve(PROCESS_NAME_FILENAME).toString());
        if (processName != null) {
            return processName;
        }
        return DEFAULT_PROCESS_NAME;
    }

    private String getThreadName(Path threadPath) {
        String threadName = ProcStatsUtil.readNullSeparatedFile(threadPath.resolve(THREAD_NAME_FILENAME).toString());
        if (threadName == null) {
            return DEFAULT_THREAD_NAME;
        }
        return threadName;
    }

    private int getProcessId(Path processPath) {
        String fileName = processPath.getFileName().toString();
        try {
            return Integer.parseInt(fileName);
        } catch (NumberFormatException e) {
            Slog.w(TAG, "Failed to parse " + fileName + " as process ID", e);
            return -1;
        }
    }

    @VisibleForTesting
    public static class FrequencyBucketCreator {
        private final int[] mBucketStartIndices;
        private final int mNumBuckets = this.mBucketStartIndices.length;
        private final int mNumFrequencies;

        @VisibleForTesting
        public FrequencyBucketCreator(long[] frequencies, int targetNumBuckets) {
            this.mNumFrequencies = frequencies.length;
            this.mBucketStartIndices = getBucketStartIndices(getClusterStartIndices(frequencies), targetNumBuckets, this.mNumFrequencies);
        }

        @VisibleForTesting
        public int[] bucketValues(long[] values) {
            Preconditions.checkArgument(values.length == this.mNumFrequencies);
            int[] buckets = new int[this.mNumBuckets];
            for (int bucketIdx = 0; bucketIdx < this.mNumBuckets; bucketIdx++) {
                int bucketStartIdx = getLowerBound(bucketIdx, this.mBucketStartIndices);
                int bucketEndIdx = getUpperBound(bucketIdx, this.mBucketStartIndices, values.length);
                for (int valuesIdx = bucketStartIdx; valuesIdx < bucketEndIdx; valuesIdx++) {
                    buckets[bucketIdx] = (int) (((long) buckets[bucketIdx]) + values[valuesIdx]);
                }
            }
            return buckets;
        }

        @VisibleForTesting
        public int[] bucketFrequencies(long[] frequencies) {
            Preconditions.checkArgument(frequencies.length == this.mNumFrequencies);
            int[] buckets = new int[this.mNumBuckets];
            for (int i = 0; i < buckets.length; i++) {
                buckets[i] = (int) frequencies[this.mBucketStartIndices[i]];
            }
            return buckets;
        }

        private static int[] getClusterStartIndices(long[] frequencies) {
            ArrayList<Integer> indices = new ArrayList<>();
            indices.add(0);
            for (int i = 0; i < frequencies.length - 1; i++) {
                if (frequencies[i] >= frequencies[i + 1]) {
                    indices.add(Integer.valueOf(i + 1));
                }
            }
            return ArrayUtils.convertToIntArray(indices);
        }

        /* JADX INFO: Multiple debug info for r5v2 int: [D('previousBucketsInCluster' int), D('numBucketsInCluster' int)] */
        private static int[] getBucketStartIndices(int[] clusterStartIndices, int targetNumBuckets, int numFrequencies) {
            int previousBucketsInCluster;
            int numClusters = clusterStartIndices.length;
            if (numClusters > targetNumBuckets) {
                return Arrays.copyOfRange(clusterStartIndices, 0, targetNumBuckets);
            }
            ArrayList<Integer> bucketStartIndices = new ArrayList<>();
            for (int clusterIdx = 0; clusterIdx < numClusters; clusterIdx++) {
                int clusterStartIdx = getLowerBound(clusterIdx, clusterStartIndices);
                int clusterEndIdx = getUpperBound(clusterIdx, clusterStartIndices, numFrequencies);
                if (clusterIdx != numClusters - 1) {
                    previousBucketsInCluster = targetNumBuckets / numClusters;
                } else {
                    previousBucketsInCluster = targetNumBuckets - ((numClusters - 1) * (targetNumBuckets / numClusters));
                }
                int numFrequenciesInBucket = Math.max(1, (clusterEndIdx - clusterStartIdx) / previousBucketsInCluster);
                for (int bucketIdx = 0; bucketIdx < previousBucketsInCluster; bucketIdx++) {
                    int bucketStartIdx = (bucketIdx * numFrequenciesInBucket) + clusterStartIdx;
                    if (bucketStartIdx >= clusterEndIdx) {
                        break;
                    }
                    bucketStartIndices.add(Integer.valueOf(bucketStartIdx));
                }
            }
            return ArrayUtils.convertToIntArray(bucketStartIndices);
        }

        private static int getLowerBound(int index, int[] startIndices) {
            return startIndices[index];
        }

        private static int getUpperBound(int index, int[] startIndices, int max) {
            if (index != startIndices.length - 1) {
                return startIndices[index + 1];
            }
            return max;
        }
    }

    public static class ProcessCpuUsage {
        public final int processId;
        public final String processName;
        public ArrayList<ThreadCpuUsage> threadCpuUsages;
        public final int uid;

        @VisibleForTesting
        public ProcessCpuUsage(int processId2, String processName2, int uid2, ArrayList<ThreadCpuUsage> threadCpuUsages2) {
            this.processId = processId2;
            this.processName = processName2;
            this.uid = uid2;
            this.threadCpuUsages = threadCpuUsages2;
        }
    }

    public static class ThreadCpuUsage {
        public final int threadId;
        public final String threadName;
        public int[] usageTimesMillis;

        @VisibleForTesting
        public ThreadCpuUsage(int threadId2, String threadName2, int[] usageTimesMillis2) {
            this.threadId = threadId2;
            this.threadName = threadName2;
            this.usageTimesMillis = usageTimesMillis2;
        }
    }

    @VisibleForTesting
    public static class Injector {
        public int getUidForPid(int pid) {
            return Process.getUidForPid(pid);
        }
    }
}
