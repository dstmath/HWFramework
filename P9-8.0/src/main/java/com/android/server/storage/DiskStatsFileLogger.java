package com.android.server.storage;

import android.content.pm.PackageStats;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.Log;
import com.android.server.storage.FileCollector.MeasurementResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DiskStatsFileLogger {
    public static final String APP_CACHES_KEY = "cacheSizes";
    public static final String APP_CACHE_AGG_KEY = "cacheSize";
    public static final String APP_SIZES_KEY = "appSizes";
    public static final String APP_SIZE_AGG_KEY = "appSize";
    public static final String AUDIO_KEY = "audioSize";
    public static final String DOWNLOADS_KEY = "downloadsSize";
    public static final String LAST_QUERY_TIMESTAMP_KEY = "queryTime";
    public static final String MISC_KEY = "otherSize";
    public static final String PACKAGE_NAMES_KEY = "packageNames";
    public static final String PHOTOS_KEY = "photosSize";
    public static final String SYSTEM_KEY = "systemSize";
    private static final String TAG = "DiskStatsLogger";
    public static final String VIDEOS_KEY = "videosSize";
    private long mDownloadsSize;
    private List<PackageStats> mPackageStats;
    private MeasurementResult mResult;
    private long mSystemSize;

    public DiskStatsFileLogger(MeasurementResult result, MeasurementResult downloadsResult, List<PackageStats> stats, long systemSize) {
        this.mResult = result;
        this.mDownloadsSize = downloadsResult.totalAccountedSize();
        this.mSystemSize = systemSize;
        this.mPackageStats = stats;
    }

    public void dumpToFile(File file) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(file);
        JSONObject representation = getJsonRepresentation();
        if (representation != null) {
            pw.println(representation);
        }
        pw.close();
    }

    private JSONObject getJsonRepresentation() {
        JSONObject json = new JSONObject();
        try {
            json.put(LAST_QUERY_TIMESTAMP_KEY, System.currentTimeMillis());
            json.put(PHOTOS_KEY, this.mResult.imagesSize);
            json.put(VIDEOS_KEY, this.mResult.videosSize);
            json.put(AUDIO_KEY, this.mResult.audioSize);
            json.put(DOWNLOADS_KEY, this.mDownloadsSize);
            json.put(SYSTEM_KEY, this.mSystemSize);
            json.put(MISC_KEY, this.mResult.miscSize);
            addAppsToJson(json);
            return json;
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    private void addAppsToJson(JSONObject json) throws JSONException {
        JSONArray names = new JSONArray();
        JSONArray appSizeList = new JSONArray();
        JSONArray cacheSizeList = new JSONArray();
        long appSizeSum = 0;
        long cacheSizeSum = 0;
        boolean isExternal = Environment.isExternalStorageEmulated();
        for (Entry<String, PackageStats> entry : filterOnlyPrimaryUser().entrySet()) {
            PackageStats stat = (PackageStats) entry.getValue();
            long appSize = stat.codeSize + stat.dataSize;
            long cacheSize = stat.cacheSize;
            if (isExternal) {
                appSize += stat.externalCodeSize + stat.externalDataSize;
                cacheSize += stat.externalCacheSize;
            }
            appSizeSum += appSize;
            cacheSizeSum += cacheSize;
            names.put(stat.packageName);
            appSizeList.put(appSize);
            cacheSizeList.put(cacheSize);
        }
        json.put(PACKAGE_NAMES_KEY, names);
        json.put(APP_SIZES_KEY, appSizeList);
        json.put(APP_CACHES_KEY, cacheSizeList);
        json.put(APP_SIZE_AGG_KEY, appSizeSum);
        json.put(APP_CACHE_AGG_KEY, cacheSizeSum);
    }

    private ArrayMap<String, PackageStats> filterOnlyPrimaryUser() {
        ArrayMap<String, PackageStats> packageMap = new ArrayMap();
        for (PackageStats stat : this.mPackageStats) {
            if (stat.userHandle == 0) {
                PackageStats existingStats = (PackageStats) packageMap.get(stat.packageName);
                if (existingStats != null) {
                    existingStats.cacheSize += stat.cacheSize;
                    existingStats.codeSize += stat.codeSize;
                    existingStats.dataSize += stat.dataSize;
                    existingStats.externalCacheSize += stat.externalCacheSize;
                    existingStats.externalCodeSize += stat.externalCodeSize;
                    existingStats.externalDataSize += stat.externalDataSize;
                } else {
                    packageMap.put(stat.packageName, new PackageStats(stat));
                }
            }
        }
        return packageMap;
    }
}
