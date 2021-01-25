package com.android.server.storage;

import android.content.pm.PackageStats;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.Log;
import com.android.server.storage.FileCollector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DiskStatsFileLogger {
    public static final String APP_CACHES_KEY = "cacheSizes";
    public static final String APP_CACHE_AGG_KEY = "cacheSize";
    public static final String APP_DATA_KEY = "appDataSizes";
    public static final String APP_DATA_SIZE_AGG_KEY = "appDataSize";
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
    private FileCollector.MeasurementResult mResult;
    private long mSystemSize;

    public DiskStatsFileLogger(FileCollector.MeasurementResult result, FileCollector.MeasurementResult downloadsResult, List<PackageStats> stats, long systemSize) {
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

    /* JADX INFO: Multiple debug info for r3v3 long: [D('appDataSizeList' org.json.JSONArray), D('appDataSize' long)] */
    private void addAppsToJson(JSONObject json) throws JSONException {
        long cacheSizeSum;
        JSONArray names = new JSONArray();
        JSONArray appSizeList = new JSONArray();
        JSONArray cacheSizeList = new JSONArray();
        JSONArray cacheSizeList2 = new JSONArray();
        long appSizeSum = 0;
        long appDataSizeSum = 0;
        long cacheSizeSum2 = 0;
        boolean isExternal = Environment.isExternalStorageEmulated();
        Iterator<Map.Entry<String, PackageStats>> it = filterOnlyPrimaryUser().entrySet().iterator();
        while (it.hasNext()) {
            PackageStats stat = it.next().getValue();
            long appSize = stat.codeSize;
            long appDataSize = stat.dataSize;
            long cacheSize = stat.cacheSize;
            if (isExternal) {
                cacheSizeSum = cacheSizeSum2;
                appSize += stat.externalCodeSize;
                appDataSize += stat.externalDataSize;
                cacheSize += stat.externalCacheSize;
            } else {
                cacheSizeSum = cacheSizeSum2;
            }
            long appSizeSum2 = appSizeSum + appSize;
            appDataSizeSum += appDataSize;
            cacheSizeSum2 = cacheSizeSum + cacheSize;
            names.put(stat.packageName);
            appSizeList.put(appSize);
            cacheSizeList.put(appDataSize);
            cacheSizeList2.put(cacheSize);
            cacheSizeList2 = cacheSizeList2;
            cacheSizeList = cacheSizeList;
            names = names;
            it = it;
            appSizeSum = appSizeSum2;
        }
        json.put(PACKAGE_NAMES_KEY, names);
        json.put(APP_SIZES_KEY, appSizeList);
        json.put(APP_CACHES_KEY, cacheSizeList2);
        json.put(APP_DATA_KEY, cacheSizeList);
        json.put(APP_SIZE_AGG_KEY, appSizeSum);
        json.put(APP_CACHE_AGG_KEY, cacheSizeSum2);
        json.put(APP_DATA_SIZE_AGG_KEY, appDataSizeSum);
    }

    private ArrayMap<String, PackageStats> filterOnlyPrimaryUser() {
        ArrayMap<String, PackageStats> packageMap = new ArrayMap<>();
        for (PackageStats stat : this.mPackageStats) {
            if (stat.userHandle == 0) {
                PackageStats existingStats = packageMap.get(stat.packageName);
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
