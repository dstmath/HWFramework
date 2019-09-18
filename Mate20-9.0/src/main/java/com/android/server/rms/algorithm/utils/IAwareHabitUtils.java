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
import com.android.server.gesture.GestureNavConst;
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
    private static final String IAWARE_VERSION = "2019-01-24-03-16";
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

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004b, code lost:
        if (r7 != null) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0066, code lost:
        if (r7 == null) goto L_0x0069;
     */
    public static void loadUsageData(ContentResolver resolver, Map<String, Integer> usageCount, int userId) {
        if (resolver != null && usageCount != null) {
            Cursor c = null;
            try {
                c = resolver.query(AwareConstant.Database.PKGNAME_URI, new String[]{"appPkgName", "totalUseTimes"}, "deleted =0 AND userID=?", new String[]{String.valueOf(userId)}, null);
                if (c == null) {
                    if (c != null) {
                        c.close();
                    }
                    return;
                }
                while (c.moveToNext()) {
                    String pkgName = c.getString(0);
                    int useTotalTimes = c.getInt(1);
                    if (!TextUtils.isEmpty(pkgName)) {
                        usageCount.put(pkgName, Integer.valueOf(useTotalTimes));
                    }
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUsageData ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadUsageData ");
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00b1, code lost:
        if (r13 != null) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00cc, code lost:
        if (r13 == null) goto L_0x00cf;
     */
    public static void loadAppAssociateInfo(ContentResolver resolver, Map<String, Integer> map, ArrayList<ArrayList<Integer>> data, int userId) {
        Map<String, Integer> map2 = map;
        ArrayList<ArrayList<Integer>> arrayList = data;
        boolean z = true;
        boolean z2 = false;
        if (!(resolver == null || map2 == null || arrayList == null)) {
            if (!data.isEmpty()) {
                data.clear();
            }
            int mapSize = map.size();
            initData(arrayList, mapSize);
            Cursor c = null;
            try {
                c = resolver.query(AwareConstant.Database.ASSOCIATE_URI, new String[]{"srcPkgName", "dstPkgName", "transitionTimes"}, "srcPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND dstPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND userID=?", new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)}, null);
                if (c == null) {
                    if (c != null) {
                        c.close();
                    }
                    return;
                }
                while (c.moveToNext()) {
                    String srcPkgName = c.getString(z2);
                    String dstPkgName = c.getString(z);
                    int transitionTimes = c.getInt(2);
                    if ((!map2.containsKey(srcPkgName) || !map2.containsKey(dstPkgName)) ? z2 : z) {
                        int i = map2.get(srcPkgName).intValue();
                        int j = map2.get(dstPkgName).intValue();
                        if ((i >= mapSize || j >= mapSize) ? z2 : z) {
                            arrayList.get(i).set(j, Integer.valueOf(transitionTimes));
                        }
                    }
                    z = true;
                    z2 = false;
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppAssociateInfo ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadAppAssociateInfo ");
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0083, code lost:
        if (r12 != null) goto L_0x0085;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009e, code lost:
        if (r12 == null) goto L_0x00a1;
     */
    public static void loadPkgInfo(ContentResolver resolver, Map<String, Integer> map, Map<Integer, String> revertMap, Map<String, Integer> usageCount, Map<String, UsageDistribution> appUsageDistributionMap, int userId) {
        Map<String, Integer> map2 = map;
        Map<Integer, String> map3 = revertMap;
        Map<String, Integer> map4 = usageCount;
        Map<String, UsageDistribution> map5 = appUsageDistributionMap;
        if (resolver != null && map2 != null && map3 != null && map4 != null && map5 != null) {
            Cursor c = null;
            try {
                c = resolver.query(AwareConstant.Database.PKGNAME_URI, new String[]{"appPkgName", "totalUseTimes", "totalInDay", "totalInNight"}, "deleted =0  AND userID =?", new String[]{String.valueOf(userId)}, null);
                if (c == null) {
                    if (c != null) {
                        c.close();
                    }
                    return;
                }
                while (c.moveToNext()) {
                    String name = c.getString(0);
                    int useTotalTimes = c.getInt(1);
                    map5.put(name, new UsageDistribution(c.getInt(2), c.getInt(3)));
                    if (!map2.containsKey(name)) {
                        int size = map.size();
                        map2.put(name, Integer.valueOf(size));
                        map3.put(Integer.valueOf(size), name);
                        map4.put(name, Integer.valueOf(useTotalTimes));
                    }
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadPkgInfo ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadPkgInfo ");
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00b6, code lost:
        if (r8 != null) goto L_0x00b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00d1, code lost:
        if (r8 == null) goto L_0x00d4;
     */
    public static void updatePkgNameTable(Context ctx) {
        if (ctx != null) {
            ContentResolver resolver = ctx.getContentResolver();
            if (resolver != null) {
                Cursor c = null;
                try {
                    c = resolver.query(AwareConstant.Database.PKGRECORD_URI, new String[]{"appPkgName", "flag", "userID"}, null, null, null);
                    if (c == null) {
                        if (c != null) {
                            c.close();
                        }
                        return;
                    }
                    while (c.moveToNext()) {
                        String name = c.getString(0);
                        int flag = c.getInt(1);
                        int userId = c.getInt(2);
                        ContentValues values = new ContentValues();
                        if (flag == 0) {
                            values.put("deleted", 0);
                            values.put(DELETED_TIME_STR, 0);
                            resolver.update(AwareConstant.Database.PKGNAME_URI, values, "appPkgName=? AND userID=?", new String[]{name, String.valueOf(userId)});
                        } else {
                            values.put("deleted", 1);
                            values.put(DELETED_TIME_STR, 29);
                            resolver.update(AwareConstant.Database.PKGNAME_URI, values, "appPkgName=? AND userID=?", new String[]{name, String.valueOf(userId)});
                        }
                    }
                    deleteOverDueInfo(resolver, ctx);
                    ContentValues values2 = new ContentValues();
                    values2.put(DELETED_TIME_STR, 1);
                    resolver.update(AwareConstant.Database.PKGNAME_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_DECREMENT).build(), values2, null, null);
                } catch (SQLiteException e) {
                    AwareLog.e(TAG, "Error: updatePkgNameTable ");
                } catch (IllegalStateException e2) {
                    AwareLog.e(TAG, "Error: updatePkgNameTable ");
                    if (c != null) {
                        c.close();
                    }
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                    throw th;
                }
            }
        }
    }

    public static void updateReInstallPkgNameInfo(ContentResolver resolver, List<Integer> userIdList) {
        if (resolver != null && userIdList != null) {
            ArraySet<String> set = new ArraySet<>();
            for (int i = 0; i < userIdList.size(); i++) {
                set.clear();
                loadReInstallPkgFromUserData(resolver, set, userIdList.get(i).intValue());
                AwareLog.i(TAG, " update Name userId=" + userIdList.get(i) + " set=" + set + " i=" + i);
                Iterator<String> it = set.iterator();
                while (it.hasNext()) {
                    String name = it.next();
                    ContentValues values = new ContentValues();
                    values.put("deleted", 0);
                    values.put(DELETED_TIME_STR, 0);
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0049, code lost:
        if (r7 != null) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0064, code lost:
        if (r7 == null) goto L_0x0067;
     */
    private static void loadReInstallPkgFromUserData(ContentResolver resolver, Set<String> set, int userId) {
        Cursor c = null;
        try {
            c = resolver.query(AwareConstant.Database.USERDATA_URI, new String[]{"UserData.appPkgName"}, "UserData.appPkgName NOT IN (SELECT appPkgName from PkgRecord where userID=?)  AND UserData.appPkgName IN (SELECT appPkgName from PkgName where deleted =1 and userID=?) AND UserData.userID=?", new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)}, "UserData._id");
            if (c == null) {
                if (c != null) {
                    c.close();
                }
                return;
            }
            while (c.moveToNext()) {
                String name = c.getString(0);
                if (!TextUtils.isEmpty(name)) {
                    set.add(name);
                }
            }
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: loadReInstallPkgFromUserData ");
        } catch (IllegalStateException e2) {
            AwareLog.e(TAG, "Error: loadReInstallPkgFromUserData ");
            if (c != null) {
                c.close();
            }
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private static void deleteOverDueInfo(ContentResolver resolver, Context ctx) {
        if (resolver != null && ctx != null) {
            UserManager um = UserManager.get(ctx);
            if (um != null) {
                List<UserInfo> userInfoList = um.getUsers();
                if (userInfoList != null) {
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
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b8, code lost:
        if (r12 != null) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d3, code lost:
        if (r12 == null) goto L_0x00d6;
     */
    public static void loadUserdataInfo(ContentResolver resolver, Map<String, Integer> map, Map<Integer, String> revertMap, List<Map.Entry<Integer, Long>> list, int userId, Map<String, List<Long>> startTimeMap) {
        Map<String, Integer> map2 = map;
        Map<Integer, String> map3 = revertMap;
        List<Map.Entry<Integer, Long>> list2 = list;
        Map<String, List<Long>> map4 = startTimeMap;
        if (resolver != null && map2 != null && map3 != null && list2 != null && map4 != null) {
            Cursor c = null;
            try {
                int i = 0;
                int i2 = 1;
                c = resolver.query(AwareConstant.Database.USERDATA_URI, new String[]{"UserData.appPkgName", "UserData.time", "UserData.switchToFgTime"}, "UserData.appPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND UserData.appPkgName NOT IN (select appPkgName from PkgRecord where flag=1 AND userID = ?)  AND UserData.userID=?", new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)}, "UserData._id");
                if (c == null) {
                    if (c != null) {
                        c.close();
                    }
                    return;
                }
                while (c.moveToNext()) {
                    String name = c.getString(i);
                    long time = c.getLong(i2);
                    long startTime = c.getLong(2);
                    if (startTime != 0) {
                        if (!map2.containsKey(name)) {
                            int size = map.size();
                            map2.put(name, Integer.valueOf(size));
                            map3.put(Integer.valueOf(size), name);
                        }
                        List<Long> startTimeList = map4.get(name);
                        if (startTimeList != null) {
                            startTimeList.add(Long.valueOf(startTime));
                        } else {
                            List<Long> startTimeList2 = new ArrayList<>();
                            startTimeList2.add(Long.valueOf(startTime));
                            map4.put(name, startTimeList2);
                        }
                        list2.add(new AbstractMap.SimpleEntry<>(map2.get(name), Long.valueOf(time)));
                        i = 0;
                        i2 = 1;
                    }
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUserdataInfo ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadUserdataInfo ");
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        }
    }

    public static List<Integer> getUserIDList(ContentResolver resolver) {
        Cursor c = null;
        try {
            c = resolver.query(AwareConstant.Database.USERDATA_URI, new String[]{"userID"}, "userID >=0 )GROUP BY (userID", null, null);
            if (c == null) {
                if (c != null) {
                    c.close();
                }
                return null;
            }
            ArrayList<Integer> list = new ArrayList<>();
            while (c.moveToNext()) {
                list.add(Integer.valueOf(c.getInt(0)));
            }
            if (c != null) {
                c.close();
            }
            return list;
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: getUserIDList ");
            if (c != null) {
                c.close();
            }
            return null;
        } catch (IllegalStateException e2) {
            AwareLog.e(TAG, "Error: getUserIDList ");
            if (c != null) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (c != null) {
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
            c = resolver.query(AwareConstant.Database.PKGRECORD_URI, new String[]{"appPkgName"}, "flag =1  AND userID =?", new String[]{String.valueOf(userId)}, null);
            if (c == null) {
                if (c != null) {
                    c.close();
                }
                return null;
            }
            ArraySet<String> set = new ArraySet<>();
            while (c.moveToNext()) {
                String pkg = c.getString(0);
                if (!TextUtils.isEmpty(pkg)) {
                    set.add(pkg);
                }
            }
            if (c != null) {
                c.close();
            }
            return set;
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: loadRemovedPkg ");
            if (c != null) {
                c.close();
            }
            return null;
        } catch (IllegalStateException e2) {
            AwareLog.e(TAG, "Error: loadRemovedPkg ");
            if (c != null) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (c != null) {
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
        values.put(DELETED_TIME_STR, 1);
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
            cvs.put("deleted", 1);
            cvs.put(DELETED_TIME_STR, 14);
            try {
                resolver.update(AwareConstant.Database.HABITPROTECTLIST_URI, cvs, WHERECLAUSE, new String[]{pkgName, String.valueOf(userId)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteHabitProtectList ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: deleteHabitProtectList ");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0076, code lost:
        if (r0 != null) goto L_0x0078;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0091, code lost:
        if (r0 == null) goto L_0x0094;
     */
    public static void loadHabitProtectList(ContentResolver resolver, List<ProtectApp> protectAppList, int userID) {
        if (resolver != null && protectAppList != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(AwareConstant.Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.deleted", "HabitProtectList.avgUsedFrequency"}, "userId = ?", new String[]{String.valueOf(userID)}, "CAST (HabitProtectList.avgUsedFrequency AS REAL) desc");
                if (cursor == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    int apptype = cursor.getInt(1);
                    int deletedTag = cursor.getInt(2);
                    String avgUsed = cursor.getString(3);
                    float avg = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                    try {
                        avg = Float.parseFloat(avgUsed);
                    } catch (NumberFormatException ex) {
                        AwareLog.e(TAG, "parseFloat exception " + ex.toString());
                    }
                    protectAppList.add(new ProtectApp(pkgName, apptype, deletedTag, avg));
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadHabitProtectList ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadHabitProtectList ");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
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
            cvs.put("deleted", 0);
            cvs.put(DELETED_TIME_STR, 0);
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
            values.put(DELETED_TIME_STR, 1);
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

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0074, code lost:
        if (r0 != null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x008f, code lost:
        if (r0 == null) goto L_0x0092;
     */
    public static void loadUnDeletedHabitProtectList(ContentResolver resolver, List<ProtectApp> protectAppList, int userId) {
        if (resolver != null && protectAppList != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(AwareConstant.Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.recentUsed", "HabitProtectList.avgUsedFrequency"}, " HabitProtectList.deleted = 0 and userId = ?", new String[]{String.valueOf(userId)}, null);
                if (cursor == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    int apptype = cursor.getInt(1);
                    String recentUsed = cursor.getString(2);
                    String avgUsed = cursor.getString(3);
                    float avg = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                    try {
                        avg = Float.parseFloat(avgUsed);
                    } catch (NumberFormatException ex) {
                        AwareLog.e(TAG, "parseFloat exception " + ex.toString());
                    }
                    protectAppList.add(new ProtectApp(pkgName, apptype, recentUsed, avg));
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUnDeletedHabitProtectList ");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadUnDeletedHabitProtectList ");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
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
        AwareConfig configList = null;
        try {
            IBinder binder = IAwareCMSManager.getICMSManager();
            if (binder != null) {
                configList = IAwareCMSManager.getConfig(binder, featureName, configName);
            } else {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "AppMngFeature getConfig RemoteException");
        }
        return configList;
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
        if (hour < 8 || hour >= 19) {
            return 1;
        }
        return 0;
    }
}
