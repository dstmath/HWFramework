package com.android.server.wifi.scanner;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanData;
import android.util.ArraySet;
import android.util.Pair;
import android.util.Rational;
import android.util.Slog;
import com.android.server.wifi.WifiConnectivityManager;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.ChannelSettings;
import com.android.server.wifi.WifiNative.ScanSettings;
import com.android.server.wifi.scanner.ChannelHelper.ChannelCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class BackgroundScanScheduler {
    private static final boolean DBG = false;
    public static final int DEFAULT_MAX_AP_PER_SCAN = 32;
    public static final int DEFAULT_MAX_BUCKETS = 8;
    public static final int DEFAULT_MAX_CHANNELS_PER_BUCKET = 16;
    public static final int DEFAULT_MAX_SCANS_TO_BATCH = 10;
    private static final int DEFAULT_PERIOD_MS = 30000;
    private static final int DEFAULT_REPORT_THRESHOLD_PERCENTAGE = 100;
    private static final int EXPONENTIAL_BACK_OFF_BUCKET_IDX = (PREDEFINED_BUCKET_PERIODS.length - 1);
    private static final int NUM_OF_REGULAR_BUCKETS = (PREDEFINED_BUCKET_PERIODS.length - 1);
    private static final int PERIOD_MIN_GCD_MS = 10000;
    private static final int[] PREDEFINED_BUCKET_PERIODS = new int[]{DEFAULT_PERIOD_MS, 120000, 480000, 10000, 60000, 1920000, WifiConnectivityManager.MAX_CONNECTION_ATTEMPTS_TIME_INTERVAL_MS, 960000, 3840000, -1};
    private static final String TAG = "BackgroundScanScheduler";
    private final BucketList mBuckets = new BucketList();
    private final ChannelHelper mChannelHelper;
    private int mMaxApPerScan = 32;
    private int mMaxBatch = 10;
    private int mMaxBuckets = 8;
    private int mMaxChannelsPerBucket = 16;
    private ScanSettings mSchedule;
    private final Map<WifiScanner.ScanSettings, Bucket> mSettingsToScheduledBucket = new HashMap();

    private class Bucket {
        public int bucketId;
        private final ChannelCollection mChannelCollection;
        private final List<WifiScanner.ScanSettings> mScanSettingsList;
        public int period;

        Bucket(int period) {
            this.mScanSettingsList = new ArrayList();
            this.period = period;
            this.bucketId = 0;
            this.mScanSettingsList.clear();
            this.mChannelCollection = BackgroundScanScheduler.this.mChannelHelper.createChannelCollection();
        }

        Bucket(BackgroundScanScheduler this$0, Bucket originalBucket) {
            this(originalBucket.period);
            for (WifiScanner.ScanSettings settings : originalBucket.getSettingsList()) {
                this.mScanSettingsList.add(settings);
            }
        }

        private ChannelSettings createChannelSettings(int frequency) {
            ChannelSettings channelSettings = new ChannelSettings();
            channelSettings.frequency = frequency;
            return channelSettings;
        }

        public boolean addSettings(WifiScanner.ScanSettings scanSettings) {
            this.mChannelCollection.addChannels(scanSettings);
            return this.mScanSettingsList.add(scanSettings);
        }

        public boolean removeSettings(WifiScanner.ScanSettings scanSettings) {
            if (!this.mScanSettingsList.remove(scanSettings)) {
                return false;
            }
            updateChannelCollection();
            return true;
        }

        public List<WifiScanner.ScanSettings> getSettingsList() {
            return this.mScanSettingsList;
        }

        public void updateChannelCollection() {
            this.mChannelCollection.clear();
            for (WifiScanner.ScanSettings settings : this.mScanSettingsList) {
                this.mChannelCollection.addChannels(settings);
            }
        }

        public ChannelCollection getChannelCollection() {
            return this.mChannelCollection;
        }

        public BucketSettings createBucketSettings(int bucketId, int maxChannels) {
            this.bucketId = bucketId;
            int reportEvents = 4;
            int maxPeriodInMs = 0;
            int stepCount = 0;
            for (int i = 0; i < this.mScanSettingsList.size(); i++) {
                WifiScanner.ScanSettings setting = (WifiScanner.ScanSettings) this.mScanSettingsList.get(i);
                int requestedReportEvents = setting.reportEvents;
                if ((requestedReportEvents & 4) == 0) {
                    reportEvents &= -5;
                }
                if ((requestedReportEvents & 1) != 0) {
                    reportEvents |= 1;
                }
                if ((requestedReportEvents & 2) != 0) {
                    reportEvents |= 2;
                }
                if (!(i != 0 || setting.maxPeriodInMs == 0 || setting.maxPeriodInMs == setting.periodInMs)) {
                    this.period = BackgroundScanScheduler.PREDEFINED_BUCKET_PERIODS[BackgroundScanScheduler.findBestRegularBucketIndex(setting.periodInMs, BackgroundScanScheduler.NUM_OF_REGULAR_BUCKETS)];
                    if (setting.maxPeriodInMs < this.period) {
                        maxPeriodInMs = this.period;
                    } else {
                        maxPeriodInMs = setting.maxPeriodInMs;
                    }
                    stepCount = setting.stepCount;
                }
            }
            BucketSettings bucketSettings = new BucketSettings();
            bucketSettings.bucket = bucketId;
            bucketSettings.report_events = reportEvents;
            bucketSettings.period_ms = this.period;
            bucketSettings.max_period_ms = maxPeriodInMs;
            bucketSettings.step_count = stepCount;
            this.mChannelCollection.fillBucketSettings(bucketSettings, maxChannels);
            return bucketSettings;
        }
    }

    private class BucketList {
        private int mActiveBucketCount = 0;
        private final Bucket[] mBuckets = new Bucket[BackgroundScanScheduler.PREDEFINED_BUCKET_PERIODS.length];
        private final Comparator<Bucket> mTimePeriodSortComparator = new Comparator<Bucket>() {
            public int compare(Bucket b1, Bucket b2) {
                return b1.period - b2.period;
            }
        };

        BucketList() {
        }

        public void clearAll() {
            Arrays.fill(this.mBuckets, null);
            this.mActiveBucketCount = 0;
        }

        public void clear(int index) {
            if (this.mBuckets[index] != null) {
                this.mActiveBucketCount--;
                this.mBuckets[index] = null;
            }
        }

        public Bucket getOrCreate(int index) {
            Bucket bucket = this.mBuckets[index];
            if (bucket != null) {
                return bucket;
            }
            this.mActiveBucketCount++;
            bucket = new Bucket(BackgroundScanScheduler.PREDEFINED_BUCKET_PERIODS[index]);
            this.mBuckets[index] = bucket;
            return bucket;
        }

        public boolean isActive(int index) {
            return this.mBuckets[index] != null;
        }

        public Bucket get(int index) {
            return this.mBuckets[index];
        }

        public int size() {
            return this.mBuckets.length;
        }

        public int getActiveCount() {
            return this.mActiveBucketCount;
        }

        public int getActiveRegularBucketCount() {
            if (isActive(BackgroundScanScheduler.EXPONENTIAL_BACK_OFF_BUCKET_IDX)) {
                return this.mActiveBucketCount - 1;
            }
            return this.mActiveBucketCount;
        }

        public List<Bucket> getSortedActiveRegularBucketList() {
            ArrayList<Bucket> activeBuckets = new ArrayList();
            int i = 0;
            while (i < this.mBuckets.length) {
                if (!(this.mBuckets[i] == null || i == BackgroundScanScheduler.EXPONENTIAL_BACK_OFF_BUCKET_IDX)) {
                    activeBuckets.add(this.mBuckets[i]);
                }
                i++;
            }
            Collections.sort(activeBuckets, this.mTimePeriodSortComparator);
            return activeBuckets;
        }
    }

    public int getMaxBuckets() {
        return this.mMaxBuckets;
    }

    public void setMaxBuckets(int maxBuckets) {
        this.mMaxBuckets = maxBuckets;
    }

    public int getMaxChannelsPerBucket() {
        return this.mMaxChannelsPerBucket;
    }

    public void setMaxChannelsPerBucket(int maxChannels) {
        this.mMaxChannelsPerBucket = maxChannels;
    }

    public int getMaxBatch() {
        return this.mMaxBatch;
    }

    public void setMaxBatch(int maxBatch) {
        this.mMaxBatch = maxBatch;
    }

    public int getMaxApPerScan() {
        return this.mMaxApPerScan;
    }

    public void setMaxApPerScan(int maxApPerScan) {
        this.mMaxApPerScan = maxApPerScan;
    }

    public BackgroundScanScheduler(ChannelHelper channelHelper) {
        this.mChannelHelper = channelHelper;
        createSchedule(new ArrayList(), getMaxChannelsPerBucket());
    }

    public void updateSchedule(Collection<WifiScanner.ScanSettings> requests) {
        this.mBuckets.clearAll();
        for (WifiScanner.ScanSettings request : requests) {
            addScanToBuckets(request);
        }
        compactBuckets(getMaxBuckets());
        createSchedule(fixBuckets(optimizeBuckets(), getMaxBuckets(), getMaxChannelsPerBucket()), getMaxChannelsPerBucket());
    }

    public ScanSettings getSchedule() {
        return this.mSchedule;
    }

    public boolean shouldReportFullScanResultForSettings(ScanResult result, int bucketsScanned, WifiScanner.ScanSettings settings) {
        return ScanScheduleUtil.shouldReportFullScanResultForSettings(this.mChannelHelper, result, bucketsScanned, settings, getScheduledBucket(settings));
    }

    public ScanData[] filterResultsForSettings(ScanData[] scanDatas, WifiScanner.ScanSettings settings) {
        return ScanScheduleUtil.filterResultsForSettings(this.mChannelHelper, scanDatas, settings, getScheduledBucket(settings));
    }

    public int getScheduledBucket(WifiScanner.ScanSettings settings) {
        Bucket maxScheduledBucket = (Bucket) this.mSettingsToScheduledBucket.get(settings);
        if (maxScheduledBucket != null) {
            return maxScheduledBucket.bucketId;
        }
        Slog.wtf(TAG, "No bucket found for settings");
        return -1;
    }

    private void createSchedule(List<Bucket> bucketList, int maxChannelsPerBucket) {
        ScanSettings schedule = new ScanSettings();
        schedule.num_buckets = bucketList.size();
        schedule.buckets = new BucketSettings[bucketList.size()];
        schedule.max_ap_per_scan = 0;
        schedule.report_threshold_num_scans = getMaxBatch();
        int bucketId = 0;
        for (Bucket bucket : bucketList) {
            schedule.buckets[bucketId] = bucket.createBucketSettings(bucketId, maxChannelsPerBucket);
            for (WifiScanner.ScanSettings settings : bucket.getSettingsList()) {
                if (settings.numBssidsPerScan > schedule.max_ap_per_scan) {
                    schedule.max_ap_per_scan = settings.numBssidsPerScan;
                }
                if (settings.maxScansToCache != 0 && settings.maxScansToCache < schedule.report_threshold_num_scans) {
                    schedule.report_threshold_num_scans = settings.maxScansToCache;
                }
            }
            bucketId++;
        }
        schedule.report_threshold_percent = 100;
        if (schedule.max_ap_per_scan == 0 || schedule.max_ap_per_scan > getMaxApPerScan()) {
            schedule.max_ap_per_scan = getMaxApPerScan();
        }
        if (schedule.num_buckets > 0) {
            int gcd = schedule.buckets[0].period_ms;
            for (int b = 1; b < schedule.num_buckets; b++) {
                gcd = Rational.gcd(schedule.buckets[b].period_ms, gcd);
            }
            if (gcd < 10000) {
                Slog.wtf(TAG, "found gcd less than min gcd");
                gcd = 10000;
            }
            schedule.base_period_ms = gcd;
        } else {
            schedule.base_period_ms = DEFAULT_PERIOD_MS;
        }
        this.mSchedule = schedule;
    }

    private void addScanToBuckets(WifiScanner.ScanSettings settings) {
        int bucketIndex;
        if (settings.maxPeriodInMs == 0 || settings.maxPeriodInMs == settings.periodInMs) {
            bucketIndex = findBestRegularBucketIndex(settings.periodInMs, NUM_OF_REGULAR_BUCKETS);
        } else {
            bucketIndex = EXPONENTIAL_BACK_OFF_BUCKET_IDX;
        }
        this.mBuckets.getOrCreate(bucketIndex).addSettings(settings);
    }

    private static int findBestRegularBucketIndex(int requestedPeriod, int maxNumBuckets) {
        maxNumBuckets = Math.min(maxNumBuckets, NUM_OF_REGULAR_BUCKETS);
        int index = -1;
        int minDiff = Integer.MAX_VALUE;
        for (int i = 0; i < maxNumBuckets; i++) {
            int diff = Math.abs(PREDEFINED_BUCKET_PERIODS[i] - requestedPeriod);
            if (diff < minDiff) {
                minDiff = diff;
                index = i;
            }
        }
        if (index == -1) {
            Slog.wtf(TAG, "Could not find best bucket for period " + requestedPeriod + " in " + maxNumBuckets + " buckets");
        }
        return index;
    }

    private void compactBuckets(int maxBuckets) {
        int maxRegularBuckets = maxBuckets;
        if (this.mBuckets.isActive(EXPONENTIAL_BACK_OFF_BUCKET_IDX)) {
            maxRegularBuckets = maxBuckets - 1;
        }
        for (int i = NUM_OF_REGULAR_BUCKETS - 1; i >= 0 && this.mBuckets.getActiveRegularBucketCount() > maxRegularBuckets; i--) {
            if (this.mBuckets.isActive(i)) {
                for (WifiScanner.ScanSettings scanRequest : this.mBuckets.get(i).getSettingsList()) {
                    this.mBuckets.getOrCreate(findBestRegularBucketIndex(scanRequest.periodInMs, i)).addSettings(scanRequest);
                }
                this.mBuckets.clear(i);
            }
        }
    }

    private WifiScanner.ScanSettings cloneScanSettings(WifiScanner.ScanSettings originalSettings) {
        WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
        settings.band = originalSettings.band;
        settings.channels = originalSettings.channels;
        settings.periodInMs = originalSettings.periodInMs;
        settings.reportEvents = originalSettings.reportEvents;
        settings.numBssidsPerScan = originalSettings.numBssidsPerScan;
        settings.maxScansToCache = originalSettings.maxScansToCache;
        settings.maxPeriodInMs = originalSettings.maxPeriodInMs;
        settings.stepCount = originalSettings.stepCount;
        settings.isPnoScan = originalSettings.isPnoScan;
        return settings;
    }

    private WifiScanner.ScanSettings createCurrentBucketSplitSettings(WifiScanner.ScanSettings originalSettings, Set<Integer> currentBucketChannels) {
        WifiScanner.ScanSettings currentBucketSettings = cloneScanSettings(originalSettings);
        currentBucketSettings.band = 0;
        currentBucketSettings.channels = new ChannelSpec[currentBucketChannels.size()];
        int chanIdx = 0;
        for (Integer channel : currentBucketChannels) {
            int chanIdx2 = chanIdx + 1;
            currentBucketSettings.channels[chanIdx] = new ChannelSpec(channel.intValue());
            chanIdx = chanIdx2;
        }
        return currentBucketSettings;
    }

    private WifiScanner.ScanSettings createTargetBucketSplitSettings(WifiScanner.ScanSettings originalSettings, Set<Integer> targetBucketChannels) {
        WifiScanner.ScanSettings targetBucketSettings = cloneScanSettings(originalSettings);
        targetBucketSettings.band = 0;
        targetBucketSettings.channels = new ChannelSpec[targetBucketChannels.size()];
        int chanIdx = 0;
        for (Integer channel : targetBucketChannels) {
            int chanIdx2 = chanIdx + 1;
            targetBucketSettings.channels[chanIdx] = new ChannelSpec(channel.intValue());
            chanIdx = chanIdx2;
        }
        targetBucketSettings.reportEvents = originalSettings.reportEvents & 6;
        return targetBucketSettings;
    }

    private Pair<WifiScanner.ScanSettings, WifiScanner.ScanSettings> createSplitSettings(WifiScanner.ScanSettings originalSettings, ChannelCollection targetBucketChannelCol) {
        return Pair.create(createCurrentBucketSplitSettings(originalSettings, targetBucketChannelCol.getMissingChannelsFromSettings(originalSettings)), createTargetBucketSplitSettings(originalSettings, targetBucketChannelCol.getContainingChannelsFromSettings(originalSettings)));
    }

    private Pair<Boolean, WifiScanner.ScanSettings> mergeSettingsToLowerBuckets(WifiScanner.ScanSettings originalSettings, Bucket currentBucket, ListIterator<Bucket> iterTargetBuckets) {
        WifiScanner.ScanSettings remainingSplitSettings = null;
        boolean wasMerged = false;
        Bucket maxScheduledBucket = currentBucket;
        while (iterTargetBuckets.hasPrevious()) {
            Bucket targetBucket = (Bucket) iterTargetBuckets.previous();
            ChannelCollection targetBucketChannelCol = targetBucket.getChannelCollection();
            if (targetBucketChannelCol.containsSettings(originalSettings)) {
                targetBucket.addSettings(originalSettings);
                maxScheduledBucket = targetBucket;
                wasMerged = true;
            } else if (targetBucketChannelCol.partiallyContainsSettings(originalSettings)) {
                Pair<WifiScanner.ScanSettings, WifiScanner.ScanSettings> splitSettings;
                if (remainingSplitSettings == null) {
                    splitSettings = createSplitSettings(originalSettings, targetBucketChannelCol);
                } else {
                    splitSettings = createSplitSettings(remainingSplitSettings, targetBucketChannelCol);
                }
                targetBucket.addSettings((WifiScanner.ScanSettings) splitSettings.second);
                remainingSplitSettings = splitSettings.first;
                wasMerged = true;
            }
        }
        this.mSettingsToScheduledBucket.put(originalSettings, maxScheduledBucket);
        return Pair.create(Boolean.valueOf(wasMerged), remainingSplitSettings);
    }

    private List<Bucket> optimizeBuckets() {
        this.mSettingsToScheduledBucket.clear();
        List<Bucket> sortedBuckets = this.mBuckets.getSortedActiveRegularBucketList();
        ListIterator<Bucket> iterBuckets = sortedBuckets.listIterator();
        List<WifiScanner.ScanSettings> currentBucketSplitSettingsList = new ArrayList();
        while (iterBuckets.hasNext()) {
            Bucket currentBucket = (Bucket) iterBuckets.next();
            Iterator<WifiScanner.ScanSettings> iterSettings = currentBucket.getSettingsList().iterator();
            currentBucketSplitSettingsList.clear();
            while (iterSettings.hasNext()) {
                Pair<Boolean, WifiScanner.ScanSettings> mergeResult = mergeSettingsToLowerBuckets((WifiScanner.ScanSettings) iterSettings.next(), currentBucket, sortedBuckets.listIterator(iterBuckets.previousIndex()));
                if (((Boolean) mergeResult.first).booleanValue()) {
                    iterSettings.remove();
                    WifiScanner.ScanSettings remainingSplitSettings = mergeResult.second;
                    if (remainingSplitSettings != null) {
                        currentBucketSplitSettingsList.add(remainingSplitSettings);
                    }
                }
            }
            for (WifiScanner.ScanSettings splitSettings : currentBucketSplitSettingsList) {
                currentBucket.addSettings(splitSettings);
            }
            if (currentBucket.getSettingsList().isEmpty()) {
                iterBuckets.remove();
            } else {
                currentBucket.updateChannelCollection();
            }
        }
        if (this.mBuckets.isActive(EXPONENTIAL_BACK_OFF_BUCKET_IDX)) {
            Bucket exponentialBucket = this.mBuckets.get(EXPONENTIAL_BACK_OFF_BUCKET_IDX);
            for (WifiScanner.ScanSettings settings : exponentialBucket.getSettingsList()) {
                this.mSettingsToScheduledBucket.put(settings, exponentialBucket);
            }
            sortedBuckets.add(exponentialBucket);
        }
        return sortedBuckets;
    }

    private List<Set<Integer>> partitionChannelSet(Set<Integer> originalChannelSet, int maxChannelsPerBucket) {
        ArrayList<Set<Integer>> channelSetList = new ArrayList();
        ArraySet<Integer> channelSet = new ArraySet();
        for (Integer add : originalChannelSet) {
            channelSet.add(add);
            if (channelSet.size() == maxChannelsPerBucket) {
                channelSetList.add(channelSet);
                channelSet = new ArraySet();
            }
        }
        if (!channelSet.isEmpty()) {
            channelSetList.add(channelSet);
        }
        return channelSetList;
    }

    private List<Bucket> createSplitBuckets(Bucket originalBucket, List<Set<Integer>> channelSets) {
        List<Bucket> splitBucketList = new ArrayList();
        int channelSetIdx = 0;
        for (Set<Integer> channelSet : channelSets) {
            Bucket splitBucket;
            if (channelSetIdx == 0) {
                splitBucket = originalBucket;
            } else {
                splitBucket = new Bucket(this, originalBucket);
            }
            ChannelCollection splitBucketChannelCollection = splitBucket.getChannelCollection();
            splitBucketChannelCollection.clear();
            for (Integer channel : channelSet) {
                splitBucketChannelCollection.addChannel(channel.intValue());
            }
            channelSetIdx++;
            splitBucketList.add(splitBucket);
        }
        return splitBucketList;
    }

    private List<Bucket> fixBuckets(List<Bucket> originalBucketList, int maxBuckets, int maxChannelsPerBucket) {
        List<Bucket> fixedBucketList = new ArrayList();
        int totalNumBuckets = originalBucketList.size();
        for (Bucket originalBucket : originalBucketList) {
            Set<Integer> channelSet = originalBucket.getChannelCollection().getChannelSet();
            if (channelSet.size() > maxChannelsPerBucket) {
                List<Set<Integer>> channelSetList = partitionChannelSet(channelSet, maxChannelsPerBucket);
                int newTotalNumBuckets = (channelSetList.size() + totalNumBuckets) - 1;
                if (newTotalNumBuckets <= maxBuckets) {
                    for (Bucket bucket : createSplitBuckets(originalBucket, channelSetList)) {
                        fixedBucketList.add(bucket);
                    }
                    totalNumBuckets = newTotalNumBuckets;
                } else {
                    fixedBucketList.add(originalBucket);
                }
            } else {
                fixedBucketList.add(originalBucket);
            }
        }
        return fixedBucketList;
    }
}
