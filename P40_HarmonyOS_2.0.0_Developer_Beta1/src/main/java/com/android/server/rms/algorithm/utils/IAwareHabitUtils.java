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
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private static final String IAWARE_VERSION = "2020-01-21-19-40";
    public static final int IM_DB_TYPE = 0;
    public static final int IM_TOP_N = 3;
    private static final String SELECT_DELETED_SQL = " (select appPkgName from PkgName WHERE deleted=1 AND userID = ?)";
    private static final String SELECT_PKGNAME_SQL = " (select appPkgName from PkgName WHERE userID = ?)";
    private static final String TAG = "IAwareHabitUtils";
    public static final int UNDELETED_FLAG = 0;
    private static final String WHERECLAUSE = "appPkgName =?  and userId = ?";

    public static void loadUsageData(ContentResolver resolver, Map<String, Integer> usageCount, int userId) {
        if (resolver != null && usageCount != null) {
            Cursor c = null;
            try {
                c = resolver.query(AwareConstant.Database.PKGNAME_URI, new String[]{"appPkgName", "totalUseTimes"}, "deleted =0 AND userID=?", new String[]{String.valueOf(userId)}, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        String pkgName = c.getString(0);
                        int useTotalTimes = c.getInt(1);
                        if (!TextUtils.isEmpty(pkgName)) {
                            usageCount.put(pkgName, Integer.valueOf(useTotalTimes));
                        }
                    }
                    c.close();
                } else if (c != null) {
                    c.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUsageData ");
                if (0 == 0) {
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadUsageData ");
                if (0 == 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00b9 A[SYNTHETIC] */
    public static void loadAppAssociateInfo(ContentResolver resolver, Map<String, Integer> map, ArrayList<ArrayList<Integer>> data, int userId) {
        int j;
        int j2;
        int j3 = 0;
        int i = 1;
        if (!(resolver == null || map == null || data == null)) {
            if (!data.isEmpty()) {
                data.clear();
            }
            int mapSize = map.size();
            initData(data, mapSize);
            Cursor c = null;
            try {
                c = resolver.query(AwareConstant.Database.ASSOCIATE_URI, new String[]{"srcPkgName", "dstPkgName", "transitionTimes"}, "srcPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND dstPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND userID=?", new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)}, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        String srcPkgName = c.getString(j3);
                        String dstPkgName = c.getString(i);
                        int transitionTimes = c.getInt(2);
                        if (((!map.containsKey(srcPkgName) || !map.containsKey(dstPkgName)) ? j3 : i) != 0) {
                            int i2 = map.get(srcPkgName).intValue();
                            int j4 = map.get(dstPkgName).intValue();
                            if (i2 < mapSize) {
                                j2 = j4;
                                if (j2 < mapSize) {
                                    j = i;
                                    if (j == 0) {
                                        data.get(i2).set(j2, Integer.valueOf(transitionTimes));
                                    }
                                }
                            } else {
                                j2 = j4;
                            }
                            j = 0;
                            if (j == 0) {
                            }
                        }
                        j3 = 0;
                        i = 1;
                    }
                    c.close();
                } else if (c != null) {
                    c.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppAssociateInfo ");
                if (0 == 0) {
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadAppAssociateInfo ");
                if (0 == 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        }
    }

    private static void initData(ArrayList<ArrayList<Integer>> data, int size) {
        if (data != null) {
            for (int i = 0; i < size; i++) {
                ArrayList<Integer> list = new ArrayList<>();
                for (int j = 0; j < size; j++) {
                    list.add(0);
                }
                data.add(list);
            }
        }
    }

    public static void loadPkgInfo(ContentResolver resolver, Map<String, Integer> map, Map<Integer, String> revertMap, Map<String, Integer> usageCount, Map<String, UsageDistribution> appUsageDistributionMap, int userId) {
        if (resolver != null && map != null && revertMap != null && usageCount != null && appUsageDistributionMap != null) {
            Cursor c = null;
            try {
                int i = 1;
                c = resolver.query(AwareConstant.Database.PKGNAME_URI, new String[]{"appPkgName", "totalUseTimes", "totalInDay", "totalInNight"}, "deleted =0  AND userID =?", new String[]{String.valueOf(userId)}, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        String name = c.getString(0);
                        int useTotalTimes = c.getInt(i);
                        appUsageDistributionMap.put(name, new UsageDistribution(c.getInt(2), c.getInt(3)));
                        if (!map.containsKey(name)) {
                            int size = map.size();
                            map.put(name, Integer.valueOf(size));
                            revertMap.put(Integer.valueOf(size), name);
                            usageCount.put(name, Integer.valueOf(useTotalTimes));
                        }
                        i = 1;
                    }
                    c.close();
                } else if (c != null) {
                    c.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadPkgInfo ");
                if (0 == 0) {
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadPkgInfo ");
                if (0 == 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        }
    }

    public static void updatePkgNameTable(Context ctx) {
        ContentResolver resolver;
        if (ctx != null && (resolver = ctx.getContentResolver()) != null) {
            Cursor c = null;
            try {
                c = resolver.query(AwareConstant.Database.PKGRECORD_URI, new String[]{"appPkgName", "flag", "userID"}, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        String name = c.getString(0);
                        int flag = c.getInt(1);
                        int userId = c.getInt(2);
                        ContentValues values = new ContentValues();
                        if (flag == 0) {
                            values.put("deleted", (Integer) 0);
                            values.put(DELETED_TIME_STR, (Integer) 0);
                            resolver.update(AwareConstant.Database.PKGNAME_URI, values, "appPkgName=? AND userID=?", new String[]{name, String.valueOf(userId)});
                        } else {
                            values.put("deleted", (Integer) 1);
                            values.put(DELETED_TIME_STR, (Integer) 29);
                            resolver.update(AwareConstant.Database.PKGNAME_URI, values, "appPkgName=? AND userID=?", new String[]{name, String.valueOf(userId)});
                        }
                    }
                    deleteOverDueInfo(resolver, ctx);
                    ContentValues values2 = new ContentValues();
                    values2.put(DELETED_TIME_STR, (Integer) 1);
                    resolver.update(AwareConstant.Database.PKGNAME_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_DECREMENT).build(), values2, null, null);
                    c.close();
                } else if (c != null) {
                    c.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updatePkgNameTable ");
                if (0 == 0) {
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: updatePkgNameTable ");
                if (0 == 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        }
    }

    public static void updateReInstallPkgNameInfo(ContentResolver resolver, List<Integer> userIdList) {
        if (!(resolver == null || userIdList == null)) {
            ArraySet<String> set = new ArraySet<>();
            for (int i = 0; i < userIdList.size(); i++) {
                set.clear();
                loadReInstallPkgFromUserData(resolver, set, userIdList.get(i).intValue());
                AwareLog.i(TAG, " update Name userId=" + userIdList.get(i) + " set=" + set + " i=" + i);
                Iterator<String> it = set.iterator();
                while (it.hasNext()) {
                    String name = it.next();
                    ContentValues values = new ContentValues();
                    values.put("deleted", (Integer) 0);
                    values.put(DELETED_TIME_STR, (Integer) 0);
                    try {
                        resolver.update(AwareConstant.Database.PKGNAME_URI, values, "appPkgName=? AND userID=?", new String[]{name, String.valueOf(userIdList.get(i))});
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
        Cursor c = null;
        try {
            c = resolver.query(AwareConstant.Database.USERDATA_URI, new String[]{"UserData.appPkgName"}, "UserData.appPkgName NOT IN (SELECT appPkgName from PkgRecord where userID=?)  AND UserData.appPkgName IN (SELECT appPkgName from PkgName where deleted =1 and userID=?) AND UserData.userID=?", new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)}, "UserData._id");
            if (c != null) {
                while (c.moveToNext()) {
                    String name = c.getString(0);
                    if (!TextUtils.isEmpty(name)) {
                        set.add(name);
                    }
                }
                c.close();
            } else if (c != null) {
                c.close();
            }
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: loadReInstallPkgFromUserData ");
            if (0 == 0) {
            }
        } catch (IllegalStateException e2) {
            AwareLog.e(TAG, "Error: loadReInstallPkgFromUserData ");
            if (0 == 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private static void deleteOverDueInfo(ContentResolver resolver, Context ctx) {
        UserManager um;
        List<UserInfo> userInfoList;
        if (!(resolver == null || ctx == null || (um = UserManager.get(ctx)) == null || (userInfoList = um.getUsers()) == null)) {
            resolver.delete(AwareConstant.Database.PKGNAME_URI, "deleted=1 AND deletedTime=0", null);
            for (int i = 0; i < userInfoList.size(); i++) {
                UserInfo info = userInfoList.get(i);
                if (info != null) {
                    int userId = info.id;
                    resolver.delete(AwareConstant.Database.ASSOCIATE_URI, "userID = ? AND (srcPkgName NOT IN  (select appPkgName from PkgName WHERE userID = ?) OR dstPkgName NOT IN  (select appPkgName from PkgName WHERE userID = ?))", new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)});
                }
            }
        }
    }

    public static void loadUserdataInfo(ContentResolver resolver, Map<String, Integer> map, Map<Integer, String> revertMap, List<Map.Entry<Integer, Long>> list, int userId, Map<String, List<Long>> startTimeMap) {
        if (resolver != null && map != null && revertMap != null && list != null && startTimeMap != null) {
            Cursor c = null;
            try {
                int i = 0;
                c = resolver.query(AwareConstant.Database.USERDATA_URI, new String[]{"UserData.appPkgName", "UserData.time", "UserData.switchToFgTime"}, "UserData.appPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND UserData.appPkgName NOT IN (select appPkgName from PkgRecord where flag=1 AND userID = ?)  AND UserData.userID=?", new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)}, "UserData._id");
                if (c != null) {
                    while (c.moveToNext()) {
                        String name = c.getString(i);
                        long time = c.getLong(1);
                        long startTime = c.getLong(2);
                        if (startTime != 0) {
                            if (!map.containsKey(name)) {
                                int size = map.size();
                                map.put(name, Integer.valueOf(size));
                                revertMap.put(Integer.valueOf(size), name);
                            }
                            List<Long> startTimeList = startTimeMap.get(name);
                            if (startTimeList != null) {
                                startTimeList.add(Long.valueOf(startTime));
                            } else {
                                List<Long> startTimeList2 = new ArrayList<>();
                                startTimeList2.add(Long.valueOf(startTime));
                                startTimeMap.put(name, startTimeList2);
                            }
                            list.add(new AbstractMap.SimpleEntry<>(map.get(name), Long.valueOf(time)));
                            i = 0;
                        }
                    }
                    c.close();
                } else if (c != null) {
                    c.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUserdataInfo ");
                if (0 == 0) {
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadUserdataInfo ");
                if (0 == 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        }
    }

    public static List<Integer> getUserIDList(ContentResolver resolver) {
        Cursor c = null;
        try {
            Cursor c2 = resolver.query(AwareConstant.Database.USERDATA_URI, new String[]{"userID"}, "userID >=0 )GROUP BY (userID", null, null);
            if (c2 == null) {
                if (c2 != null) {
                    c2.close();
                }
                return null;
            }
            ArrayList<Integer> list = new ArrayList<>();
            while (c2.moveToNext()) {
                list.add(Integer.valueOf(c2.getInt(0)));
            }
            c2.close();
            return list;
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: getUserIDList ");
            if (0 != 0) {
                c.close();
            }
            return null;
        } catch (IllegalStateException e2) {
            AwareLog.e(TAG, "Error: getUserIDList ");
            if (0 != 0) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    public static void deleteTable(ContentResolver resolver) {
        if (resolver != null) {
            try {
                resolver.delete(AwareConstant.Database.ASSOCIATE_URI, null, null);
                resolver.delete(AwareConstant.Database.PKGNAME_URI, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteTable ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: deleteTable ");
            }
        }
    }

    public static void deleteUserCount(ContentResolver resolver, int userId) {
        if (resolver != null) {
            try {
                resolver.delete(AwareConstant.Database.ASSOCIATE_URI, "userID = ?", new String[]{String.valueOf(userId)});
                resolver.delete(AwareConstant.Database.PKGNAME_URI, "userID = ?", new String[]{String.valueOf(userId)});
                resolver.delete(AwareConstant.Database.PKGRECORD_URI, "userID = ?", new String[]{String.valueOf(userId)});
                resolver.delete(AwareConstant.Database.USERDATA_URI, "userID = ?", new String[]{String.valueOf(userId)});
                resolver.delete(AwareConstant.Database.HABITPROTECTLIST_URI, "userID = ?", new String[]{String.valueOf(userId)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteUserCount ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: deleteUserCount ");
            }
        }
    }

    public static Set<String> loadRemovedPkg(ContentResolver resolver, int userId) {
        if (resolver == null) {
            return null;
        }
        Cursor c = null;
        try {
            Cursor c2 = resolver.query(AwareConstant.Database.PKGRECORD_URI, new String[]{"appPkgName"}, "flag =1  AND userID =?", new String[]{String.valueOf(userId)}, null);
            if (c2 == null) {
                if (c2 != null) {
                    c2.close();
                }
                return null;
            }
            ArraySet<String> set = new ArraySet<>();
            while (c2.moveToNext()) {
                String pkg = c2.getString(0);
                if (!TextUtils.isEmpty(pkg)) {
                    set.add(pkg);
                }
            }
            c2.close();
            return set;
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: loadRemovedPkg ");
            if (0 != 0) {
                c.close();
            }
            return null;
        } catch (IllegalStateException e2) {
            AwareLog.e(TAG, "Error: loadRemovedPkg ");
            if (0 != 0) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    public static void decreaseAppCount(ContentResolver resolver) {
        if (resolver == null) {
            AwareLog.e(TAG, "decreaseAppCount resolver is null");
            return;
        }
        ContentValues values = new ContentValues();
        values.put(DELETED_TIME_STR, (Integer) 1);
        try {
            resolver.update(AwareConstant.Database.PKGNAME_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_APP_SWITCH_DECREMENT).build(), values, null, null);
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: decreaseAppCount ");
        } catch (IllegalStateException e2) {
            AwareLog.e(TAG, "Error: decreaseAppCount ");
        }
    }

    public static void insertDataToUserdataTable(ContentResolver resolver, String pkgName, long time, int userId) {
        if (resolver != null && pkgName != null) {
            ContentValues values = new ContentValues();
            values.put("appPkgName", pkgName);
            values.put("time", Long.valueOf(time));
            values.put("userID", Integer.valueOf(userId));
            values.put("switchToFgTime", Long.valueOf(System.currentTimeMillis()));
            try {
                resolver.insert(AwareConstant.Database.USERDATA_URI, values);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertDataToUserdataTable ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: insertDataToUserdataTable ");
            }
        }
    }

    public static void deleteTheRemovedPkgFromDB(ContentResolver resolver, String pkgName, int userId) {
        if (resolver != null && pkgName != null) {
            try {
                resolver.delete(AwareConstant.Database.PKGRECORD_URI, "appPkgName =? AND userID =?", new String[]{pkgName, String.valueOf(userId)});
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
                resolver.insert(AwareConstant.Database.PKGRECORD_URI, values);
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
            cvs.put("deleted", (Integer) 1);
            cvs.put(DELETED_TIME_STR, (Integer) 14);
            try {
                resolver.update(AwareConstant.Database.HABITPROTECTLIST_URI, cvs, WHERECLAUSE, new String[]{pkgName, String.valueOf(userId)});
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
            try {
                cursor = resolver.query(AwareConstant.Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.deleted", "HabitProtectList.avgUsedFrequency"}, "userId = ?", new String[]{String.valueOf(userID)}, "CAST (HabitProtectList.avgUsedFrequency AS REAL) desc");
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
                    cursor.close();
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadHabitProtectList ");
                if (0 == 0) {
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadHabitProtectList ");
                if (0 == 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
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
                resolver.insert(AwareConstant.Database.HABITPROTECTLIST_URI, cvs);
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
            cvs.put("deleted", (Integer) 0);
            cvs.put(DELETED_TIME_STR, (Integer) 0);
            AwareLog.d(TAG, "habit protect list update type: pkgName:" + pkgName);
            try {
                resolver.update(AwareConstant.Database.HABITPROTECTLIST_URI, cvs, WHERECLAUSE, new String[]{pkgName, String.valueOf(userId)});
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
            values.put(DELETED_TIME_STR, (Integer) 1);
            try {
                resolver.update(AwareConstant.Database.HABITPROTECTLIST_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_DECREMENT).build(), values, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp ");
            }
            try {
                resolver.delete(AwareConstant.Database.HABITPROTECTLIST_URI, "deleted = 1  and deletedTime < 1", null);
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
                resolver.update(AwareConstant.Database.HABITPROTECTLIST_URI, cvs, WHERECLAUSE, new String[]{pkgName, String.valueOf(userId)});
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
                cursor = resolver.query(AwareConstant.Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.recentUsed", "HabitProtectList.avgUsedFrequency"}, " HabitProtectList.deleted = 0 and userId = ?", new String[]{String.valueOf(userId)}, null);
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
                    cursor.close();
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUnDeletedHabitProtectList ");
                if (0 == 0) {
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadUnDeletedHabitProtectList ");
                if (0 == 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    private static AwareConfig getConfig(String featureName, String configName) {
        if (TextUtils.isEmpty(featureName) || TextUtils.isEmpty(configName)) {
            AwareLog.e(TAG, "featureName or configName is null");
            return null;
        }
        try {
            IBinder binder = IAwareCMSManager.getICMSManager();
            if (binder != null) {
                return IAwareCMSManager.getConfig(binder, featureName, configName);
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "AppMngFeature getConfig RemoteException");
            return null;
        }
    }

    public static void getHabitFilterListFromCMS(ArraySet<String> pkgSet1, ArraySet<String> pkgSet2) {
        if (pkgSet1 != null) {
            AwareConfig habitConfig = getConfig(APPMNG, HABIT_FILTER_LIST);
            if (habitConfig == null) {
                AwareLog.e(TAG, "getHabitFilterListFromCMS failure cause null configList");
                return;
            }
            int count = 0;
            for (AwareConfig.Item item : habitConfig.getConfigList()) {
                count++;
                if (item == null || item.getSubItemList() == null) {
                    AwareLog.e(TAG, "getHabitFilterListFromCMS continue cause null item");
                } else {
                    for (AwareConfig.SubItem subitem : item.getSubItemList()) {
                        if (subitem != null && !TextUtils.isEmpty(subitem.getValue())) {
                            if (count == 1) {
                                pkgSet1.add(subitem.getValue());
                            } else if (count == 2 && pkgSet2 != null) {
                                pkgSet2.add(subitem.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    public static Map<String, Integer> getConfigFromCMS(String tag, String configName) {
        String name;
        AwareConfig habitConfig = getConfig(tag, configName);
        if (habitConfig == null) {
            AwareLog.e(TAG, "getConfigFromCMS failure cause null configList");
            return null;
        }
        ArrayMap<String, Integer> config = new ArrayMap<>();
        for (AwareConfig.Item item : habitConfig.getConfigList()) {
            if (item == null || item.getSubItemList() == null) {
                AwareLog.e(TAG, "getConfigFromCMS continue cause null item");
            } else {
                for (AwareConfig.SubItem subitem : item.getSubItemList()) {
                    if (!(subitem == null || (name = subitem.getName()) == null)) {
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
        AwareLog.d(TAG, "getConfigFromCMS config=" + config);
        return config;
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
        UserManager um;
        UserInfo user;
        if (ctx == null || (um = UserManager.get(ctx)) == null || (user = um.getUserInfo(userId)) == null) {
            return false;
        }
        return user.isGuest();
    }

    public static int getTimeType(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(11);
        if (hour < 8 || hour >= DAY_DISTRIBUTION_END_HOUR) {
            return 1;
        }
        return 0;
    }

    public static class UsageDistribution {
        public int mDay;
        public int mNight;

        public UsageDistribution(int day, int night) {
            this.mDay = day;
            this.mNight = night;
        }
    }
}
