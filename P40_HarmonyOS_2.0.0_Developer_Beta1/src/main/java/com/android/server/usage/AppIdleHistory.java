package com.android.server.usage;

import android.app.usage.AppStandbyInfo;
import android.app.usage.UsageStatsManager;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.controllers.JobStatus;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import com.android.server.voiceinteraction.DatabaseHelper;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AppIdleHistory {
    @VisibleForTesting
    static final String APP_IDLE_FILENAME = "app_idle_stats.xml";
    private static final String ATTR_BUCKETING_REASON = "bucketReason";
    private static final String ATTR_BUCKET_ACTIVE_TIMEOUT_TIME = "activeTimeoutTime";
    private static final String ATTR_BUCKET_WORKING_SET_TIMEOUT_TIME = "workingSetTimeoutTime";
    private static final String ATTR_CURRENT_BUCKET = "appLimitBucket";
    private static final String ATTR_ELAPSED_IDLE = "elapsedIdleTime";
    private static final String ATTR_LAST_PREDICTED_TIME = "lastPredictedTime";
    private static final String ATTR_LAST_RUN_JOB_TIME = "lastJobRunTime";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_SCREEN_IDLE = "screenIdleTime";
    private static final boolean DEBUG = false;
    private static final long ONE_MINUTE = 60000;
    private static final int STANDBY_BUCKET_UNKNOWN = -1;
    private static final String TAG = "AppIdleHistory";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PACKAGES = "packages";
    private long mElapsedDuration;
    private long mElapsedSnapshot;
    private SparseArray<ArrayMap<String, AppUsageHistory>> mIdleHistory = new SparseArray<>();
    private boolean mScreenOn;
    private long mScreenOnDuration;
    private long mScreenOnSnapshot;
    private final File mStorageDir;

    /* access modifiers changed from: package-private */
    public static class AppUsageHistory {
        long bucketActiveTimeoutTime;
        long bucketWorkingSetTimeoutTime;
        int bucketingReason;
        int currentBucket;
        int lastInformedBucket;
        long lastJobRunTime;
        int lastPredictedBucket = -1;
        long lastPredictedTime;
        long lastUsedElapsedTime;
        long lastUsedScreenTime;

        AppUsageHistory() {
        }
    }

    AppIdleHistory(File storageDir, long elapsedRealtime) {
        this.mElapsedSnapshot = elapsedRealtime;
        this.mScreenOnSnapshot = elapsedRealtime;
        this.mStorageDir = storageDir;
        readScreenOnTime();
    }

    public void updateDisplay(boolean screenOn, long elapsedRealtime) {
        if (screenOn != this.mScreenOn) {
            this.mScreenOn = screenOn;
            if (this.mScreenOn) {
                this.mScreenOnSnapshot = elapsedRealtime;
                return;
            }
            this.mScreenOnDuration += elapsedRealtime - this.mScreenOnSnapshot;
            this.mElapsedDuration += elapsedRealtime - this.mElapsedSnapshot;
            this.mElapsedSnapshot = elapsedRealtime;
        }
    }

    public long getScreenOnTime(long elapsedRealtime) {
        long screenOnTime = this.mScreenOnDuration;
        if (this.mScreenOn) {
            return screenOnTime + (elapsedRealtime - this.mScreenOnSnapshot);
        }
        return screenOnTime;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public File getScreenOnTimeFile() {
        return new File(this.mStorageDir, "screen_on_time");
    }

    private void readScreenOnTime() {
        File screenOnTimeFile = getScreenOnTimeFile();
        if (screenOnTimeFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(screenOnTimeFile));
                this.mScreenOnDuration = Long.parseLong(reader.readLine());
                this.mElapsedDuration = Long.parseLong(reader.readLine());
                reader.close();
            } catch (IOException | NumberFormatException e) {
            }
        } else {
            writeScreenOnTime();
        }
    }

    private void writeScreenOnTime() {
        AtomicFile screenOnTimeFile = new AtomicFile(getScreenOnTimeFile());
        FileOutputStream fos = null;
        try {
            fos = screenOnTimeFile.startWrite();
            fos.write((Long.toString(this.mScreenOnDuration) + "\n" + Long.toString(this.mElapsedDuration) + "\n").getBytes());
            screenOnTimeFile.finishWrite(fos);
        } catch (IOException e) {
            screenOnTimeFile.failWrite(fos);
        }
    }

    public void writeAppIdleDurations() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        this.mElapsedDuration += elapsedRealtime - this.mElapsedSnapshot;
        this.mElapsedSnapshot = elapsedRealtime;
        writeScreenOnTime();
    }

    public AppUsageHistory reportUsage(AppUsageHistory appUsageHistory, String packageName, int newBucket, int usageReason, long elapsedRealtime, long timeout) {
        if (timeout > elapsedRealtime) {
            long timeoutTime = this.mElapsedDuration + (timeout - this.mElapsedSnapshot);
            if (newBucket == 10) {
                appUsageHistory.bucketActiveTimeoutTime = Math.max(timeoutTime, appUsageHistory.bucketActiveTimeoutTime);
            } else if (newBucket == 20) {
                appUsageHistory.bucketWorkingSetTimeoutTime = Math.max(timeoutTime, appUsageHistory.bucketWorkingSetTimeoutTime);
            } else {
                throw new IllegalArgumentException("Cannot set a timeout on bucket=" + newBucket);
            }
        }
        if (elapsedRealtime != 0) {
            appUsageHistory.lastUsedElapsedTime = this.mElapsedDuration + (elapsedRealtime - this.mElapsedSnapshot);
            appUsageHistory.lastUsedScreenTime = getScreenOnTime(elapsedRealtime);
        }
        if (appUsageHistory.currentBucket > newBucket) {
            appUsageHistory.currentBucket = newBucket;
        }
        appUsageHistory.bucketingReason = usageReason | 768;
        return appUsageHistory;
    }

    public AppUsageHistory reportUsage(String packageName, int userId, int newBucket, int usageReason, long nowElapsed, long timeout) {
        return reportUsage(getPackageHistory(getUserHistory(userId), packageName, nowElapsed, true), packageName, newBucket, usageReason, nowElapsed, timeout);
    }

    private ArrayMap<String, AppUsageHistory> getUserHistory(int userId) {
        ArrayMap<String, AppUsageHistory> userHistory = this.mIdleHistory.get(userId);
        if (userHistory != null) {
            return userHistory;
        }
        ArrayMap<String, AppUsageHistory> userHistory2 = new ArrayMap<>();
        this.mIdleHistory.put(userId, userHistory2);
        readAppIdleTimes(userId, userHistory2);
        return userHistory2;
    }

    private AppUsageHistory getPackageHistory(ArrayMap<String, AppUsageHistory> userHistory, String packageName, long elapsedRealtime, boolean create) {
        AppUsageHistory appUsageHistory = userHistory.get(packageName);
        if (appUsageHistory != null || !create) {
            return appUsageHistory;
        }
        AppUsageHistory appUsageHistory2 = new AppUsageHistory();
        appUsageHistory2.lastUsedElapsedTime = getElapsedTime(elapsedRealtime);
        appUsageHistory2.lastUsedScreenTime = getScreenOnTime(elapsedRealtime);
        appUsageHistory2.lastPredictedTime = getElapsedTime(0);
        appUsageHistory2.currentBucket = 50;
        appUsageHistory2.bucketingReason = 256;
        appUsageHistory2.lastInformedBucket = -1;
        appUsageHistory2.lastJobRunTime = Long.MIN_VALUE;
        userHistory.put(packageName, appUsageHistory2);
        return appUsageHistory2;
    }

    public void onUserRemoved(int userId) {
        this.mIdleHistory.remove(userId);
    }

    public boolean isIdle(String packageName, int userId, long elapsedRealtime) {
        return getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, true).currentBucket >= 40;
    }

    public AppUsageHistory getAppUsageHistory(String packageName, int userId, long elapsedRealtime) {
        return getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, true);
    }

    public void setAppStandbyBucket(String packageName, int userId, long elapsedRealtime, int bucket, int reason) {
        setAppStandbyBucket(packageName, userId, elapsedRealtime, bucket, reason, false);
    }

    public void setAppStandbyBucket(String packageName, int userId, long elapsedRealtime, int bucket, int reason, boolean resetTimeout) {
        AppUsageHistory appUsageHistory = getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, true);
        appUsageHistory.currentBucket = bucket;
        appUsageHistory.bucketingReason = reason;
        long elapsed = getElapsedTime(elapsedRealtime);
        if ((65280 & reason) == 1280) {
            appUsageHistory.lastPredictedTime = elapsed;
            appUsageHistory.lastPredictedBucket = bucket;
        }
        if (resetTimeout) {
            appUsageHistory.bucketActiveTimeoutTime = elapsed;
            appUsageHistory.bucketWorkingSetTimeoutTime = elapsed;
        }
    }

    public void updateLastPrediction(AppUsageHistory app, long elapsedTimeAdjusted, int bucket) {
        app.lastPredictedTime = elapsedTimeAdjusted;
        app.lastPredictedBucket = bucket;
    }

    public void setLastJobRunTime(String packageName, int userId, long elapsedRealtime) {
        getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, true).lastJobRunTime = getElapsedTime(elapsedRealtime);
    }

    public long getTimeSinceLastJobRun(String packageName, int userId, long elapsedRealtime) {
        AppUsageHistory appUsageHistory = getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, false);
        if (appUsageHistory == null || appUsageHistory.lastJobRunTime == Long.MIN_VALUE) {
            return JobStatus.NO_LATEST_RUNTIME;
        }
        return getElapsedTime(elapsedRealtime) - appUsageHistory.lastJobRunTime;
    }

    public int getAppStandbyBucket(String packageName, int userId, long elapsedRealtime) {
        AppUsageHistory appUsageHistory = getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, false);
        if (appUsageHistory == null) {
            return 50;
        }
        return appUsageHistory.currentBucket;
    }

    public ArrayList<AppStandbyInfo> getAppStandbyBuckets(int userId, boolean appIdleEnabled) {
        ArrayMap<String, AppUsageHistory> userHistory = getUserHistory(userId);
        int size = userHistory.size();
        ArrayList<AppStandbyInfo> buckets = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            buckets.add(new AppStandbyInfo(userHistory.keyAt(i), appIdleEnabled ? userHistory.valueAt(i).currentBucket : 10));
        }
        return buckets;
    }

    public int getAppStandbyReason(String packageName, int userId, long elapsedRealtime) {
        AppUsageHistory appUsageHistory = getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, false);
        if (appUsageHistory != null) {
            return appUsageHistory.bucketingReason;
        }
        return 0;
    }

    public long getElapsedTime(long elapsedRealtime) {
        return (elapsedRealtime - this.mElapsedSnapshot) + this.mElapsedDuration;
    }

    public int setIdle(String packageName, int userId, boolean idle, long elapsedRealtime) {
        AppUsageHistory appUsageHistory = getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, true);
        if (idle) {
            appUsageHistory.currentBucket = 40;
            appUsageHistory.bucketingReason = 1024;
        } else {
            appUsageHistory.currentBucket = 10;
            appUsageHistory.bucketingReason = UsbTerminalTypes.TERMINAL_OUT_HEADMOUNTED;
        }
        return appUsageHistory.currentBucket;
    }

    public void clearUsage(String packageName, int userId) {
        getUserHistory(userId).remove(packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldInformListeners(String packageName, int userId, long elapsedRealtime, int bucket) {
        AppUsageHistory appUsageHistory = getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, true);
        if (appUsageHistory.lastInformedBucket == bucket) {
            return false;
        }
        appUsageHistory.lastInformedBucket = bucket;
        return true;
    }

    /* access modifiers changed from: package-private */
    public int getThresholdIndex(String packageName, int userId, long elapsedRealtime, long[] screenTimeThresholds, long[] elapsedTimeThresholds) {
        AppUsageHistory appUsageHistory = getPackageHistory(getUserHistory(userId), packageName, elapsedRealtime, false);
        if (appUsageHistory == null) {
            return screenTimeThresholds.length - 1;
        }
        long screenOnDelta = getScreenOnTime(elapsedRealtime) - appUsageHistory.lastUsedScreenTime;
        long elapsedDelta = getElapsedTime(elapsedRealtime) - appUsageHistory.lastUsedElapsedTime;
        for (int i = screenTimeThresholds.length - 1; i >= 0; i--) {
            if (screenOnDelta >= screenTimeThresholds[i] && elapsedDelta >= elapsedTimeThresholds[i]) {
                return i;
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public File getUserFile(int userId) {
        return new File(new File(new File(this.mStorageDir, DatabaseHelper.SoundModelContract.KEY_USERS), Integer.toString(userId)), APP_IDLE_FILENAME);
    }

    public boolean userFileExists(int userId) {
        return getUserFile(userId).exists();
    }

    private void readAppIdleTimes(int userId, ArrayMap<String, AppUsageHistory> userHistory) {
        Throwable th;
        XmlPullParser parser;
        int type;
        int i;
        int i2;
        int i3;
        FileInputStream fis = null;
        try {
            fis = new AtomicFile(getUserFile(userId)).openRead();
            parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            if (type != 2) {
                Slog.e(TAG, "Unable to read app idle file for user " + userId);
                IoUtils.closeQuietly(fis);
                return;
            }
            try {
                if (!parser.getName().equals(TAG_PACKAGES)) {
                    IoUtils.closeQuietly(fis);
                    return;
                }
                while (true) {
                    int type2 = parser.next();
                    if (type2 == i) {
                        break;
                    }
                    if (type2 == i2) {
                        if (parser.getName().equals("package")) {
                            String packageName = parser.getAttributeValue(null, "name");
                            AppUsageHistory appUsageHistory = new AppUsageHistory();
                            appUsageHistory.lastUsedElapsedTime = Long.parseLong(parser.getAttributeValue(null, ATTR_ELAPSED_IDLE));
                            appUsageHistory.lastUsedScreenTime = Long.parseLong(parser.getAttributeValue(null, ATTR_SCREEN_IDLE));
                            appUsageHistory.lastPredictedTime = getLongValue(parser, ATTR_LAST_PREDICTED_TIME, 0);
                            String currentBucketString = parser.getAttributeValue(null, ATTR_CURRENT_BUCKET);
                            if (currentBucketString == null) {
                                i3 = 10;
                            } else {
                                i3 = Integer.parseInt(currentBucketString);
                            }
                            appUsageHistory.currentBucket = i3;
                            String bucketingReason = parser.getAttributeValue(null, ATTR_BUCKETING_REASON);
                            appUsageHistory.lastJobRunTime = getLongValue(parser, ATTR_LAST_RUN_JOB_TIME, Long.MIN_VALUE);
                            appUsageHistory.bucketActiveTimeoutTime = getLongValue(parser, ATTR_BUCKET_ACTIVE_TIMEOUT_TIME, 0);
                            appUsageHistory.bucketWorkingSetTimeoutTime = getLongValue(parser, ATTR_BUCKET_WORKING_SET_TIMEOUT_TIME, 0);
                            appUsageHistory.bucketingReason = 256;
                            if (bucketingReason != null) {
                                try {
                                    appUsageHistory.bucketingReason = Integer.parseInt(bucketingReason, 16);
                                } catch (NumberFormatException e) {
                                }
                            }
                            appUsageHistory.lastInformedBucket = -1;
                            try {
                                userHistory.put(packageName, appUsageHistory);
                            } catch (IOException | XmlPullParserException e2) {
                            }
                        }
                    }
                    i = 1;
                    i2 = 2;
                }
                IoUtils.closeQuietly(fis);
                return;
            } catch (IOException | XmlPullParserException e3) {
                try {
                    Slog.e(TAG, "Unable to read app idle file for user " + userId);
                    IoUtils.closeQuietly(fis);
                    return;
                } catch (Throwable th2) {
                    th = th2;
                }
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(fis);
            throw th;
        }
        while (true) {
            type = parser.next();
            i = 1;
            i2 = 2;
            if (type == 2 || type == 1) {
                break;
            }
        }
    }

    private long getLongValue(XmlPullParser parser, String attrName, long defValue) {
        String value = parser.getAttributeValue(null, attrName);
        if (value == null) {
            return defValue;
        }
        return Long.parseLong(value);
    }

    public void writeAppIdleTimes(int userId) {
        AtomicFile appIdleFile = new AtomicFile(getUserFile(userId));
        try {
            FileOutputStream fos = appIdleFile.startWrite();
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            FastXmlSerializer xml = new FastXmlSerializer();
            xml.setOutput(bos, StandardCharsets.UTF_8.name());
            xml.startDocument((String) null, true);
            xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xml.startTag((String) null, TAG_PACKAGES);
            ArrayMap<String, AppUsageHistory> userHistory = getUserHistory(userId);
            int N = userHistory.size();
            for (int i = 0; i < N; i++) {
                AppUsageHistory history = userHistory.valueAt(i);
                xml.startTag((String) null, "package");
                xml.attribute((String) null, "name", userHistory.keyAt(i));
                xml.attribute((String) null, ATTR_ELAPSED_IDLE, Long.toString(history.lastUsedElapsedTime));
                xml.attribute((String) null, ATTR_SCREEN_IDLE, Long.toString(history.lastUsedScreenTime));
                xml.attribute((String) null, ATTR_LAST_PREDICTED_TIME, Long.toString(history.lastPredictedTime));
                xml.attribute((String) null, ATTR_CURRENT_BUCKET, Integer.toString(history.currentBucket));
                xml.attribute((String) null, ATTR_BUCKETING_REASON, Integer.toHexString(history.bucketingReason));
                if (history.bucketActiveTimeoutTime > 0) {
                    xml.attribute((String) null, ATTR_BUCKET_ACTIVE_TIMEOUT_TIME, Long.toString(history.bucketActiveTimeoutTime));
                }
                if (history.bucketWorkingSetTimeoutTime > 0) {
                    xml.attribute((String) null, ATTR_BUCKET_WORKING_SET_TIMEOUT_TIME, Long.toString(history.bucketWorkingSetTimeoutTime));
                }
                if (history.lastJobRunTime != Long.MIN_VALUE) {
                    xml.attribute((String) null, ATTR_LAST_RUN_JOB_TIME, Long.toString(history.lastJobRunTime));
                }
                xml.endTag((String) null, "package");
            }
            xml.endTag((String) null, TAG_PACKAGES);
            xml.endDocument();
            appIdleFile.finishWrite(fos);
        } catch (Exception e) {
            appIdleFile.failWrite(null);
            Slog.e(TAG, "Error writing app idle file for user " + userId);
        }
    }

    public void dump(IndentingPrintWriter idpw, int userId, String pkg) {
        ArrayMap<String, AppUsageHistory> userHistory;
        String str = pkg;
        idpw.println("App Standby States:");
        idpw.increaseIndent();
        ArrayMap<String, AppUsageHistory> userHistory2 = this.mIdleHistory.get(userId);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long totalElapsedTime = getElapsedTime(elapsedRealtime);
        long screenOnTime = getScreenOnTime(elapsedRealtime);
        if (userHistory2 != null) {
            int P = userHistory2.size();
            int p = 0;
            while (p < P) {
                String packageName = userHistory2.keyAt(p);
                AppUsageHistory appUsageHistory = userHistory2.valueAt(p);
                if (str == null || str.equals(packageName)) {
                    idpw.print("package=" + packageName);
                    idpw.print(" u=" + userId);
                    idpw.print(" bucket=" + appUsageHistory.currentBucket + " reason=" + UsageStatsManager.reasonToString(appUsageHistory.bucketingReason));
                    idpw.print(" used=");
                    userHistory = userHistory2;
                    TimeUtils.formatDuration(totalElapsedTime - appUsageHistory.lastUsedElapsedTime, idpw);
                    idpw.print(" usedScr=");
                    TimeUtils.formatDuration(screenOnTime - appUsageHistory.lastUsedScreenTime, idpw);
                    idpw.print(" lastPred=");
                    TimeUtils.formatDuration(totalElapsedTime - appUsageHistory.lastPredictedTime, idpw);
                    idpw.print(" activeLeft=");
                    TimeUtils.formatDuration(appUsageHistory.bucketActiveTimeoutTime - totalElapsedTime, idpw);
                    idpw.print(" wsLeft=");
                    TimeUtils.formatDuration(appUsageHistory.bucketWorkingSetTimeoutTime - totalElapsedTime, idpw);
                    idpw.print(" lastJob=");
                    TimeUtils.formatDuration(totalElapsedTime - appUsageHistory.lastJobRunTime, idpw);
                    StringBuilder sb = new StringBuilder();
                    sb.append(" idle=");
                    sb.append(isIdle(packageName, userId, elapsedRealtime) ? "y" : "n");
                    idpw.print(sb.toString());
                    idpw.println();
                } else {
                    userHistory = userHistory2;
                }
                p++;
                str = pkg;
                userHistory2 = userHistory;
            }
            idpw.println();
            idpw.print("totalElapsedTime=");
            TimeUtils.formatDuration(getElapsedTime(elapsedRealtime), idpw);
            idpw.println();
            idpw.print("totalScreenOnTime=");
            TimeUtils.formatDuration(getScreenOnTime(elapsedRealtime), idpw);
            idpw.println();
            idpw.decreaseIndent();
        }
    }
}
