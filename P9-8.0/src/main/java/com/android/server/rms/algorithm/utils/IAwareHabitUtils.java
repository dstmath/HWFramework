package com.android.server.rms.algorithm.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareConstant.Database;
import android.rms.iaware.AwareLog;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class IAwareHabitUtils {
    public static final String APPMNG = "AppManagement";
    public static final String AUTO_APP_SWITCH_DECREMENT = "appSwitch_decrement";
    public static final String AUTO_DECREMENT = "decrement";
    public static final String AUTO_DECREMENT_TYPE = "decrement_type";
    public static final float AVGUSEDFREQUENCY = 0.5f;
    private static final int DAY_DISTRIBUTION_END_HOUR = 19;
    private static final int DAY_DISTRIBUTION_START_HOUR = 8;
    public static final int DECREASE_ROUNDS = 29;
    public static final int DELETED_FLAG = 1;
    private static final String DELETED_TIME_STR = "deletedTime";
    public static final int EMAIL_DB_TYPE = 1;
    public static final int EMAIL_TOP_N = 1;
    public static final String HABIT_CONFIG = "HabitConfig";
    public static final String HABIT_EMAIL_COUNT = "emailCount";
    public static final String HABIT_FILTER_LIST = "HabitFilterList";
    public static final String HABIT_HIGH_END = "highEnd";
    public static final String HABIT_IM_COUNT = "imCount";
    public static final String HABIT_LOW_END = "lowEnd";
    public static final String HABIT_LRU_COUNT = "lruCount";
    public static final String HABIT_MOST_USED_COUNT = "mostUsedCount";
    public static final int HABIT_PROTECT_MAX_TRAIN_COUNTS = 14;
    private static final String IAWARE_VERSION = "2017-11-22-18-25";
    public static final int IM_DB_TYPE = 0;
    public static final int IM_TOP_N = 3;
    private static final String SELECT_DELETED_SQL = " (select appPkgName from PkgName WHERE deleted=1 AND userID = ?)";
    private static final String SELECT_PKGNAME_SQL = " (select appPkgName from PkgName WHERE userID = ?)";
    private static final String TAG = "IAwareHabitUtils";
    public static final int UNDELETED_FLAG = 0;
    private static final String WHERECLAUSE = "appPkgName =?  and userId = ?";

    public static class UsageDistribution {
        public int mDay;
        public int mNight;

        public UsageDistribution(int day, int night) {
            this.mDay = day;
            this.mNight = night;
        }
    }

    public static void loadUsageData(ContentResolver resolver, Map<String, Integer> usageCount, int userId) {
        if (resolver != null && usageCount != null) {
            String whereClause = "deleted =0 AND userID=?";
            Cursor c = null;
            try {
                c = resolver.query(Database.PKGNAME_URI, new String[]{"appPkgName", "totalUseTimes"}, whereClause, new String[]{String.valueOf(userId)}, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        String pkgName = c.getString(0);
                        int useTotalTimes = c.getInt(1);
                        if (!TextUtils.isEmpty(pkgName)) {
                            usageCount.put(pkgName, Integer.valueOf(useTotalTimes));
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    return;
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUsageData ");
                if (c != null) {
                    c.close();
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadUsageData ");
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x0052 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0089 A:{Catch:{ SQLiteException -> 0x00bd, IllegalStateException -> 0x00e5, all -> 0x00f6 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void loadAppAssociateInfo(ContentResolver resolver, Map<String, Integer> map, ArrayList<ArrayList<Integer>> data, int userId) {
        boolean checkParam = resolver == null || map == null || data == null;
        if (!checkParam) {
            if (!data.isEmpty()) {
                data.clear();
            }
            int mapSize = map.size();
            initData(data, mapSize);
            Cursor c = null;
            try {
                c = resolver.query(Database.ASSOCIATE_URI, new String[]{"srcPkgName", "dstPkgName", "transitionTimes"}, "srcPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND dstPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND userID=?", new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)}, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        boolean isContainsKey;
                        String srcPkgName = c.getString(0);
                        String dstPkgName = c.getString(1);
                        int transitionTimes = c.getInt(2);
                        if (map.containsKey(srcPkgName)) {
                            if (map.containsKey(dstPkgName)) {
                                isContainsKey = true;
                                if (!isContainsKey) {
                                    boolean isNeedSet;
                                    int i = ((Integer) map.get(srcPkgName)).intValue();
                                    int j = ((Integer) map.get(dstPkgName)).intValue();
                                    if (i < mapSize && j < mapSize) {
                                        isNeedSet = true;
                                    } else {
                                        isNeedSet = false;
                                    }
                                    if (isNeedSet) {
                                        ((ArrayList) data.get(i)).set(j, Integer.valueOf(transitionTimes));
                                    }
                                }
                            }
                        }
                        isContainsKey = false;
                        if (!isContainsKey) {
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    return;
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppAssociateInfo ");
                if (c != null) {
                    c.close();
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadAppAssociateInfo ");
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    private static void initData(ArrayList<ArrayList<Integer>> data, int size) {
        if (data != null) {
            for (int i = 0; i < size; i++) {
                ArrayList<Integer> list = new ArrayList();
                for (int j = 0; j < size; j++) {
                    list.add(Integer.valueOf(0));
                }
                data.add(list);
            }
        }
    }

    public static void loadPkgInfo(ContentResolver resolver, Map<String, Integer> map, Map<Integer, String> revertMap, Map<String, Integer> usageCount, Map<String, UsageDistribution> appUsageDistributionMap, int userId) {
        if (resolver != null && map != null && revertMap != null && usageCount != null && appUsageDistributionMap != null) {
            String whereClause = "deleted =0  AND userID =?";
            Cursor c = null;
            try {
                c = resolver.query(Database.PKGNAME_URI, new String[]{"appPkgName", "totalUseTimes", "totalInDay", "totalInNight"}, whereClause, new String[]{String.valueOf(userId)}, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        String name = c.getString(0);
                        int useTotalTimes = c.getInt(1);
                        appUsageDistributionMap.put(name, new UsageDistribution(c.getInt(2), c.getInt(3)));
                        if (!map.containsKey(name)) {
                            int size = map.size();
                            map.put(name, Integer.valueOf(size));
                            revertMap.put(Integer.valueOf(size), name);
                            usageCount.put(name, Integer.valueOf(useTotalTimes));
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    return;
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadPkgInfo ");
                if (c != null) {
                    c.close();
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadPkgInfo ");
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public static void updatePkgNameTable(Context ctx) {
        if (ctx != null) {
            ContentResolver resolver = ctx.getContentResolver();
            if (resolver != null) {
                Cursor c = null;
                try {
                    c = resolver.query(Database.PKGRECORD_URI, new String[]{"appPkgName", "flag", "userID"}, null, null, null);
                    if (c != null) {
                        ContentValues values;
                        while (c.moveToNext()) {
                            String name = c.getString(0);
                            int flag = c.getInt(1);
                            int userId = c.getInt(2);
                            values = new ContentValues();
                            if (flag != 0) {
                                values.put("deleted", Integer.valueOf(1));
                                values.put(DELETED_TIME_STR, Integer.valueOf(29));
                                resolver.update(Database.PKGNAME_URI, values, "appPkgName=? AND userID=?", new String[]{name, String.valueOf(userId)});
                            } else {
                                values.put("deleted", Integer.valueOf(0));
                                values.put(DELETED_TIME_STR, Integer.valueOf(0));
                                resolver.update(Database.PKGNAME_URI, values, "appPkgName=? AND userID=?", new String[]{name, String.valueOf(userId)});
                            }
                        }
                        deleteOverDueInfo(resolver, ctx);
                        values = new ContentValues();
                        values.put(DELETED_TIME_STR, Integer.valueOf(1));
                        resolver.update(Database.PKGNAME_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_DECREMENT).build(), values, null, null);
                        if (c != null) {
                            c.close();
                        }
                        return;
                    }
                    if (c != null) {
                        c.close();
                    }
                } catch (SQLiteException e) {
                    AwareLog.e(TAG, "Error: updatePkgNameTable ");
                    if (c != null) {
                        c.close();
                    }
                } catch (IllegalStateException e2) {
                    AwareLog.e(TAG, "Error: updatePkgNameTable ");
                    if (c != null) {
                        c.close();
                    }
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    public static void updateReInstallPkgNameInfo(ContentResolver resolver, List<Integer> userIdList) {
        if (resolver != null && userIdList != null) {
            ArraySet<String> set = new ArraySet();
            for (int i = 0; i < userIdList.size(); i++) {
                set.clear();
                loadReInstallPkgFromUserData(resolver, set, ((Integer) userIdList.get(i)).intValue());
                AwareLog.i(TAG, " update Name userId=" + userIdList.get(i) + " set=" + set + " i=" + i);
                Iterator it = set.iterator();
                while (it.hasNext()) {
                    String name = (String) it.next();
                    ContentValues values = new ContentValues();
                    values.put("deleted", Integer.valueOf(0));
                    values.put(DELETED_TIME_STR, Integer.valueOf(0));
                    try {
                        resolver.update(Database.PKGNAME_URI, values, "appPkgName=? AND userID=?", new String[]{name, String.valueOf(userIdList.get(i))});
                    } catch (SQLiteException e) {
                        AwareLog.e(TAG, "Error: updateReInstallPkgNameInfo ");
                    } catch (IllegalStateException e2) {
                        AwareLog.e(TAG, "Error: updateReInstallPkgNameInfo ");
                    }
                }
            }
        }
    }

    private static void loadReInstallPkgFromUserData(ContentResolver resolver, Set<String> set, int userId) {
        String whereClause = "UserData.appPkgName NOT IN (SELECT appPkgName from PkgRecord where userID=?)  AND UserData.appPkgName IN (SELECT appPkgName from PkgName where deleted =1 and userID=?) AND UserData.userID=?";
        Cursor c = null;
        try {
            ContentResolver contentResolver = resolver;
            c = contentResolver.query(Database.USERDATA_URI, new String[]{"UserData.appPkgName"}, whereClause, new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)}, "UserData._id");
            if (c != null) {
                while (c.moveToNext()) {
                    String name = c.getString(0);
                    if (!TextUtils.isEmpty(name)) {
                        set.add(name);
                    }
                }
                if (c != null) {
                    c.close();
                }
                return;
            }
            if (c != null) {
                c.close();
            }
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: loadReInstallPkgFromUserData ");
            if (c != null) {
                c.close();
            }
        } catch (IllegalStateException e2) {
            AwareLog.e(TAG, "Error: loadReInstallPkgFromUserData ");
            if (c != null) {
                c.close();
            }
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
        }
    }

    private static void deleteOverDueInfo(ContentResolver resolver, Context ctx) {
        if (resolver != null && ctx != null) {
            UserManager um = UserManager.get(ctx);
            if (um != null) {
                List<UserInfo> userInfoList = um.getUsers();
                if (userInfoList != null) {
                    resolver.delete(Database.PKGNAME_URI, "deleted=1 AND deletedTime=0", null);
                    String delAssociateSql = "userID = ? AND (srcPkgName NOT IN  (select appPkgName from PkgName WHERE userID = ?) OR dstPkgName NOT IN  (select appPkgName from PkgName WHERE userID = ?))";
                    for (int i = 0; i < userInfoList.size(); i++) {
                        UserInfo info = (UserInfo) userInfoList.get(i);
                        if (info != null) {
                            int userId = info.id;
                            resolver.delete(Database.ASSOCIATE_URI, delAssociateSql, new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)});
                        }
                    }
                }
            }
        }
    }

    public static void loadUserdataInfo(ContentResolver resolver, Map<String, Integer> map, Map<Integer, String> revertMap, List<Entry<Integer, Long>> list, int userId, Map<String, List<Long>> startTimeMap) {
        if (resolver != null && map != null && revertMap != null && list != null && startTimeMap != null) {
            String whereClause = "UserData.appPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND UserData.appPkgName NOT IN (select appPkgName from PkgRecord where flag=1 AND userID = ?)  AND UserData.userID=?";
            Cursor c = null;
            try {
                ContentResolver contentResolver = resolver;
                c = contentResolver.query(Database.USERDATA_URI, new String[]{"UserData.appPkgName", "UserData.time", "UserData.switchToFgTime"}, whereClause, new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)}, "UserData._id");
                if (c != null) {
                    while (c.moveToNext()) {
                        String name = c.getString(0);
                        long time = c.getLong(1);
                        long startTime = c.getLong(2);
                        if (startTime != 0) {
                            if (!map.containsKey(name)) {
                                int size = map.size();
                                map.put(name, Integer.valueOf(size));
                                revertMap.put(Integer.valueOf(size), name);
                            }
                            List<Long> startTimeList = (List) startTimeMap.get(name);
                            if (startTimeList == null) {
                                startTimeList = new ArrayList();
                                startTimeList.add(Long.valueOf(startTime));
                                startTimeMap.put(name, startTimeList);
                            } else {
                                startTimeList.add(Long.valueOf(startTime));
                            }
                            list.add(new SimpleEntry(map.get(name), Long.valueOf(time)));
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    return;
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUserdataInfo ");
                if (c != null) {
                    c.close();
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadUserdataInfo ");
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public static List<Integer> getUserIDList(ContentResolver resolver) {
        ArrayList<Integer> arrayList;
        Throwable th;
        String whereClause = "userID >=0 )GROUP BY (userID";
        Cursor cursor = null;
        try {
            cursor = resolver.query(Database.USERDATA_URI, new String[]{"userID"}, whereClause, null, null);
            if (cursor != null) {
                ArrayList<Integer> list = new ArrayList();
                while (cursor.moveToNext()) {
                    try {
                        list.add(Integer.valueOf(cursor.getInt(0)));
                    } catch (SQLiteException e) {
                        arrayList = list;
                    } catch (IllegalStateException e2) {
                        arrayList = list;
                    } catch (Throwable th2) {
                        th = th2;
                        arrayList = list;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return list;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (SQLiteException e3) {
            try {
                AwareLog.e(TAG, "Error: getUserIDList ");
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (IllegalStateException e4) {
            AwareLog.e(TAG, "Error: getUserIDList ");
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
    }

    public static void deleteTable(ContentResolver resolver) {
        if (resolver != null) {
            try {
                resolver.delete(Database.ASSOCIATE_URI, null, null);
                resolver.delete(Database.PKGNAME_URI, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteTable ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: deleteTable ");
            }
        }
    }

    public static void deleteUserCount(ContentResolver resolver, int userId) {
        if (resolver != null) {
            String whereClause = "userID = ?";
            try {
                resolver.delete(Database.ASSOCIATE_URI, whereClause, new String[]{String.valueOf(userId)});
                resolver.delete(Database.PKGNAME_URI, whereClause, new String[]{String.valueOf(userId)});
                resolver.delete(Database.PKGRECORD_URI, whereClause, new String[]{String.valueOf(userId)});
                resolver.delete(Database.USERDATA_URI, whereClause, new String[]{String.valueOf(userId)});
                resolver.delete(Database.HABITPROTECTLIST_URI, whereClause, new String[]{String.valueOf(userId)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteUserCount ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: deleteUserCount ");
            }
        }
    }

    public static Set<String> loadRemovedPkg(ContentResolver resolver, int userId) {
        ArraySet<String> arraySet;
        Throwable th;
        if (resolver == null) {
            return null;
        }
        String whereClause = "flag =1  AND userID =?";
        Cursor cursor = null;
        try {
            cursor = resolver.query(Database.PKGRECORD_URI, new String[]{"appPkgName"}, whereClause, new String[]{String.valueOf(userId)}, null);
            if (cursor != null) {
                ArraySet<String> set = new ArraySet();
                while (cursor.moveToNext()) {
                    try {
                        String pkg = cursor.getString(0);
                        if (!TextUtils.isEmpty(pkg)) {
                            set.add(pkg);
                        }
                    } catch (SQLiteException e) {
                        arraySet = set;
                    } catch (IllegalStateException e2) {
                        arraySet = set;
                    } catch (Throwable th2) {
                        th = th2;
                        arraySet = set;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return set;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (SQLiteException e3) {
            try {
                AwareLog.e(TAG, "Error: loadRemovedPkg ");
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (IllegalStateException e4) {
            AwareLog.e(TAG, "Error: loadRemovedPkg ");
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
    }

    public static void decreaseAppCount(ContentResolver resolver) {
        if (resolver != null) {
            ContentValues values = new ContentValues();
            values.put(DELETED_TIME_STR, Integer.valueOf(1));
            try {
                resolver.update(Database.PKGNAME_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_APP_SWITCH_DECREMENT).build(), values, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: decreaseAppCount ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: decreaseAppCount ");
            }
            return;
        }
        AwareLog.e(TAG, "decreaseAppCount resolver is null");
    }

    public static void insertDataToUserdataTable(ContentResolver resolver, String pkgName, long time, int userId) {
        if (resolver != null && pkgName != null) {
            ContentValues values = new ContentValues();
            values.put("appPkgName", pkgName);
            values.put("time", Long.valueOf(time));
            values.put("userID", Integer.valueOf(userId));
            values.put("switchToFgTime", Long.valueOf(System.currentTimeMillis()));
            try {
                resolver.insert(Database.USERDATA_URI, values);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertDataToUserdataTable ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: insertDataToUserdataTable ");
            }
        }
    }

    public static void deleteTheRemovedPkgFromDB(ContentResolver resolver, String pkgName, int userId) {
        if (resolver != null && pkgName != null) {
            String whereClause = "appPkgName =? AND userID =?";
            try {
                resolver.delete(Database.PKGRECORD_URI, whereClause, new String[]{pkgName, String.valueOf(userId)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteTheRemovedPkgFromDB ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: deleteTheRemovedPkgFromDB ");
            }
        }
    }

    public static void insertDataToPkgRecordTable(ContentResolver resolver, ContentValues values) {
        if (resolver != null && values != null) {
            try {
                resolver.insert(Database.PKGRECORD_URI, values);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertDataToPkgNameTable ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: insertDataToPkgNameTable ");
            }
        }
    }

    public static void deleteHabitProtectList(ContentResolver resolver, String pkgName, int userId) {
        if (resolver != null && pkgName != null) {
            ContentValues cvs = new ContentValues();
            cvs.put("deleted", Integer.valueOf(1));
            cvs.put(DELETED_TIME_STR, Integer.valueOf(14));
            try {
                resolver.update(Database.HABITPROTECTLIST_URI, cvs, WHERECLAUSE, new String[]{pkgName, String.valueOf(userId)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteHabitProtectList ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: deleteHabitProtectList ");
            }
        }
    }

    public static void loadHabitProtectList(ContentResolver resolver, List<ProtectApp> protectAppList, int userID) {
        if (resolver != null && protectAppList != null) {
            Cursor cursor = null;
            String whereClause = "userId = ?";
            try {
                ContentResolver contentResolver = resolver;
                cursor = contentResolver.query(Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.deleted", "HabitProtectList.avgUsedFrequency"}, whereClause, new String[]{String.valueOf(userID)}, "CAST (HabitProtectList.avgUsedFrequency AS REAL) desc");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String pkgName = cursor.getString(0);
                        int apptype = cursor.getInt(1);
                        int deletedTag = cursor.getInt(2);
                        float avg = 0.0f;
                        try {
                            avg = Float.parseFloat(cursor.getString(3));
                        } catch (NumberFormatException ex) {
                            AwareLog.e(TAG, "parseFloat exception " + ex.toString());
                        }
                        protectAppList.add(new ProtectApp(pkgName, apptype, deletedTag, avg));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadHabitProtectList ");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadHabitProtectList ");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public static void insertHabitProtectList(ContentResolver resolver, String pkgName, int type, int userId) {
        if (resolver != null && pkgName != null) {
            ContentValues cvs = new ContentValues();
            cvs.put("appPkgName", pkgName);
            cvs.put("appType", Integer.valueOf(type));
            cvs.put("recentUsed", "");
            cvs.put("userID", Integer.valueOf(userId));
            AwareLog.d(TAG, "habit protect list insert:" + type + " pkgName:" + pkgName);
            try {
                resolver.insert(Database.HABITPROTECTLIST_URI, cvs);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertHabitProtectList ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: insertHabitProtectList ");
            }
        }
    }

    public static void updateDeletedHabitProtectList(ContentResolver resolver, String pkgName, int userId) {
        if (resolver != null && pkgName != null) {
            ContentValues cvs = new ContentValues();
            cvs.put("deleted", Integer.valueOf(0));
            cvs.put(DELETED_TIME_STR, Integer.valueOf(0));
            AwareLog.d(TAG, "habit protect list update type: pkgName:" + pkgName);
            try {
                resolver.update(Database.HABITPROTECTLIST_URI, cvs, WHERECLAUSE, new String[]{pkgName, String.valueOf(userId)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectList ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectList ");
            }
        }
    }

    public static void updateDeletedHabitProtectApp(ContentResolver resolver) {
        if (resolver != null) {
            ContentValues values = new ContentValues();
            values.put(DELETED_TIME_STR, Integer.valueOf(1));
            try {
                resolver.update(Database.HABITPROTECTLIST_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_DECREMENT).build(), values, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp ");
            }
            try {
                resolver.delete(Database.HABITPROTECTLIST_URI, "deleted = 1  and deletedTime < 1", null);
            } catch (SQLiteException e3) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp ");
            } catch (IllegalStateException e4) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp ");
            }
        }
    }

    public static void updateHabitProtectList(ContentResolver resolver, String pkgName, String recentUsed, String avgUsedFrequency, int userId) {
        if (resolver != null) {
            ContentValues cvs = new ContentValues();
            cvs.put("recentUsed", recentUsed);
            cvs.put("avgUsedFrequency", avgUsedFrequency);
            try {
                resolver.update(Database.HABITPROTECTLIST_URI, cvs, WHERECLAUSE, new String[]{pkgName, String.valueOf(userId)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateHabitProtectList ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: updateHabitProtectList ");
            }
        }
    }

    public static void loadUnDeletedHabitProtectList(ContentResolver resolver, List<ProtectApp> protectAppList, int userId) {
        if (resolver != null && protectAppList != null) {
            Cursor cursor = null;
            try {
                ContentResolver contentResolver = resolver;
                cursor = contentResolver.query(Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.recentUsed", "HabitProtectList.avgUsedFrequency"}, " HabitProtectList.deleted = 0 and userId = ?", new String[]{String.valueOf(userId)}, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String pkgName = cursor.getString(0);
                        int apptype = cursor.getInt(1);
                        String recentUsed = cursor.getString(2);
                        float avg = 0.0f;
                        try {
                            avg = Float.parseFloat(cursor.getString(3));
                        } catch (NumberFormatException ex) {
                            AwareLog.e(TAG, "parseFloat exception " + ex.toString());
                        }
                        protectAppList.add(new ProtectApp(pkgName, apptype, recentUsed, avg));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUnDeletedHabitProtectList ");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadUnDeletedHabitProtectList ");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private static AwareConfig getConfig(String featureName, String configName) {
        if (TextUtils.isEmpty(featureName) || TextUtils.isEmpty(configName)) {
            AwareLog.e(TAG, "featureName or configName is null");
            return null;
        }
        AwareConfig configList = null;
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice == null) {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            } else {
                configList = awareservice.getConfig(featureName, configName);
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "AppMngFeature getConfig RemoteException");
        }
        return configList;
    }

    public static void getHabitFilterListFromCMS(ArraySet<String> pkgSet1, ArraySet<String> pkgSet2) {
        if (pkgSet1 != null) {
            AwareConfig habitConfig = getConfig(APPMNG, HABIT_FILTER_LIST);
            if (habitConfig != null) {
                int count = 0;
                for (Item item : habitConfig.getConfigList()) {
                    count++;
                    if (item == null || item.getSubItemList() == null) {
                        AwareLog.e(TAG, "getHabitFilterListFromCMS continue cause null item");
                    } else {
                        for (SubItem subitem : item.getSubItemList()) {
                            if (!(subitem == null || TextUtils.isEmpty(subitem.getValue()))) {
                                if (count == 1) {
                                    pkgSet1.add(subitem.getValue());
                                } else if (count == 2 && pkgSet2 != null) {
                                    pkgSet2.add(subitem.getValue());
                                }
                            }
                        }
                    }
                }
                return;
            }
            AwareLog.e(TAG, "getHabitFilterListFromCMS failure cause null configList");
        }
    }

    public static Map<String, Integer> getConfigFromCMS(String tag, String configName) {
        AwareConfig habitConfig = getConfig(tag, configName);
        if (habitConfig != null) {
            ArrayMap<String, Integer> config = new ArrayMap();
            for (Item item : habitConfig.getConfigList()) {
                if (item == null || item.getSubItemList() == null) {
                    AwareLog.e(TAG, "getConfigFromCMS continue cause null item");
                } else {
                    for (SubItem subitem : item.getSubItemList()) {
                        if (subitem != null) {
                            String name = subitem.getName();
                            if (name != null) {
                                int value = 0;
                                try {
                                    value = Integer.parseInt(subitem.getValue());
                                } catch (NumberFormatException e) {
                                    AwareLog.e(TAG, "getConfigFromCMS NumberFormatException Ex");
                                }
                                config.put(name, Integer.valueOf(value));
                            }
                        }
                    }
                }
            }
            AwareLog.d(TAG, "getConfigFromCMS config=" + config);
            return config;
        }
        AwareLog.e(TAG, "getConfigFromCMS failure cause null configList");
        return null;
    }

    public static boolean isGCMApp(Context context, String pkgName) {
        if (pkgName == null || context == null) {
            return false;
        }
        long startTime = System.currentTimeMillis();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }
        Intent sendMsg = new Intent("android.intent.action.SEND");
        sendMsg.setPackage(pkgName);
        sendMsg.setAction("com.google.android.c2dm.intent.RECEIVE");
        List<ResolveInfo> ResolveInfo = pm.queryBroadcastReceivers(sendMsg, 0);
        AwareLog.d(TAG, "isGCMApp spend time:" + (System.currentTimeMillis() - startTime) + " pkg: " + pkgName);
        if (ResolveInfo == null || ResolveInfo.size() <= 0) {
            return false;
        }
        AwareLog.i(TAG, "isGCMApp pkg: " + pkgName);
        return true;
    }

    public static boolean isGuestUser(Context ctx, int userId) {
        boolean z = false;
        if (ctx == null) {
            return false;
        }
        UserManager um = UserManager.get(ctx);
        if (um == null) {
            return false;
        }
        UserInfo user = um.getUserInfo(userId);
        if (user != null) {
            z = user.isGuest();
        }
        return z;
    }

    public static int getTimeType(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(11);
        if (hour >= 8 && hour < 19) {
            return 0;
        }
        return 1;
    }
}
