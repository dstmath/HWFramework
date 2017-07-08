package com.android.server.rms.algorithm.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.rms.iaware.AppTypeInfo;
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
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.pgmng.plug.PGSdk;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class IAwareHabitUtils {
    private static final String APPMNG = "AppManagement";
    public static final String AUTO_APP_SWITCH_DECREMENT = "appSwitch_decrement";
    public static final String AUTO_DECREMENT = "decrement";
    public static final String AUTO_DECREMENT_TYPE = "decrement_type";
    public static final float AVGUSEDFREQUENCY = 0.5f;
    public static final int CLOCK_DB_TYPE = 2;
    public static final int DECREASE_ROUNDS = 29;
    public static final int DELETED_FLAG = 1;
    private static final String DELETED_TIME_STR = "deletedTime";
    public static final int EMAIL_DB_TYPE = 1;
    public static final int EMAIL_TOP_N = 1;
    public static final String HABIT_EMAIL_COUNT = "emailCount";
    private static final String HABIT_FILTER_LIST = "HabitFilterList";
    private static final String HABIT_FORCE_PROTECT_CONFIG = "HabitForceProtectConfig";
    public static final String HABIT_IM_COUNT = "imCount";
    public static final String HABIT_LRU_COUNT = "lruCount";
    public static final String HABIT_MOST_USED_COUNT = "mostUsedCount";
    public static final int HABIT_PROTECT_MAX_TRAIN_COUNTS = 14;
    public static final int IM_DB_TYPE = 0;
    public static final int IM_TOP_N = 3;
    private static final String SELECT_DELETED_SQL = " (select appPkgName from PkgName WHERE deleted=1 AND userID = ?)";
    private static final String SELECT_PKGNAME_SQL = " (select appPkgName from PkgName WHERE userID = ?)";
    private static final String TAG = "IAwareHabitUtils";
    public static final int UNDELETED_FLAG = 0;
    private static final String WHERECLAUSE = "appPkgName =?  and userId = ?";
    private static final ArrayMap<String, Integer> mAppsTypeList = null;
    private static ICMSManager mAwareservice;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.algorithm.utils.IAwareHabitUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.algorithm.utils.IAwareHabitUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.algorithm.utils.IAwareHabitUtils.<clinit>():void");
    }

    public static void loadUsageData(ContentResolver contentResolver, Map<String, Integer> map, int i) {
        if (contentResolver != null && map != null) {
            Cursor query;
            String str = "deleted =0 AND userID=?";
            try {
                Uri uri = Database.PKGNAME_URI;
                String[] strArr = new String[CLOCK_DB_TYPE];
                strArr[UNDELETED_FLAG] = "appPkgName";
                strArr[EMAIL_TOP_N] = "totalUseTimes";
                String[] strArr2 = new String[EMAIL_TOP_N];
                strArr2[UNDELETED_FLAG] = String.valueOf(i);
                query = contentResolver.query(uri, strArr, str, strArr2, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUsageData " + e);
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        map.put(query.getString(UNDELETED_FLAG), Integer.valueOf(query.getInt(EMAIL_TOP_N)));
                    } finally {
                        query.close();
                    }
                }
            }
        }
    }

    public static void loadAppAssociateInfo(ContentResolver contentResolver, Map<String, Integer> map, ArrayList<ArrayList<Integer>> arrayList, int i) {
        if (contentResolver != null && map != null && arrayList != null) {
            Cursor query;
            for (int i2 = UNDELETED_FLAG; i2 < map.size(); i2 += EMAIL_TOP_N) {
                ArrayList arrayList2 = new ArrayList();
                for (int i3 = UNDELETED_FLAG; i3 < map.size(); i3 += EMAIL_TOP_N) {
                    arrayList2.add(Integer.valueOf(UNDELETED_FLAG));
                }
                arrayList.add(arrayList2);
            }
            String str = "srcPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND dstPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND userID=?";
            try {
                Uri uri = Database.ASSOCIATE_URI;
                String[] strArr = new String[IM_TOP_N];
                strArr[UNDELETED_FLAG] = "srcPkgName";
                strArr[EMAIL_TOP_N] = "dstPkgName";
                strArr[CLOCK_DB_TYPE] = "transitionTimes";
                String[] strArr2 = new String[IM_TOP_N];
                strArr2[UNDELETED_FLAG] = String.valueOf(i);
                strArr2[EMAIL_TOP_N] = String.valueOf(i);
                strArr2[CLOCK_DB_TYPE] = String.valueOf(i);
                query = contentResolver.query(uri, strArr, str, strArr2, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppAssociateInfo " + e);
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        String string = query.getString(UNDELETED_FLAG);
                        String string2 = query.getString(EMAIL_TOP_N);
                        int i4 = query.getInt(CLOCK_DB_TYPE);
                        if (map.containsKey(string) && map.containsKey(string2)) {
                            int intValue = ((Integer) map.get(string)).intValue();
                            ((ArrayList) arrayList.get(intValue)).set(((Integer) map.get(string2)).intValue(), Integer.valueOf(i4));
                        }
                    } finally {
                        query.close();
                    }
                }
            }
        }
    }

    public static void loadPkgInfo(ContentResolver contentResolver, Map<String, Integer> map, Map<Integer, String> map2, Map<String, Integer> map3, int i) {
        Cursor query;
        if (contentResolver != null && map != null && map2 != null && map3 != null) {
            String str = "deleted =0  AND userID =?";
            try {
                Uri uri = Database.PKGNAME_URI;
                String[] strArr = new String[CLOCK_DB_TYPE];
                strArr[UNDELETED_FLAG] = "appPkgName";
                strArr[EMAIL_TOP_N] = "totalUseTimes";
                String[] strArr2 = new String[EMAIL_TOP_N];
                strArr2[UNDELETED_FLAG] = String.valueOf(i);
                query = contentResolver.query(uri, strArr, str, strArr2, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadPkgInfo " + e);
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        String string = query.getString(UNDELETED_FLAG);
                        int i2 = query.getInt(EMAIL_TOP_N);
                        if (!map.containsKey(string)) {
                            int size = map.size();
                            map.put(string, Integer.valueOf(size));
                            map2.put(Integer.valueOf(size), string);
                            map3.put(string, Integer.valueOf(i2));
                        }
                    } finally {
                        query.close();
                    }
                }
            }
        }
    }

    public static void updatePkgNameTable(Context context) {
        if (context != null) {
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver != null) {
                Cursor query;
                try {
                    Uri uri = Database.PKGRECORD_URI;
                    String[] strArr = new String[IM_TOP_N];
                    strArr[UNDELETED_FLAG] = "appPkgName";
                    strArr[EMAIL_TOP_N] = "flag";
                    strArr[CLOCK_DB_TYPE] = "userID";
                    query = contentResolver.query(uri, strArr, null, null, null);
                } catch (SQLiteException e) {
                    AwareLog.e(TAG, "Error: updatePkgNameTable " + e);
                    query = null;
                }
                if (query != null) {
                    while (query.moveToNext()) {
                        try {
                            String string = query.getString(UNDELETED_FLAG);
                            int i = query.getInt(EMAIL_TOP_N);
                            int i2 = query.getInt(CLOCK_DB_TYPE);
                            ContentValues contentValues = new ContentValues();
                            String[] strArr2;
                            if (i != 0) {
                                contentValues.put("deleted", Integer.valueOf(EMAIL_TOP_N));
                                contentValues.put(DELETED_TIME_STR, Integer.valueOf(DECREASE_ROUNDS));
                                strArr2 = new String[CLOCK_DB_TYPE];
                                strArr2[UNDELETED_FLAG] = string;
                                strArr2[EMAIL_TOP_N] = String.valueOf(i2);
                                contentResolver.update(Database.PKGNAME_URI, contentValues, "appPkgName=? AND userID=?", strArr2);
                            } else {
                                contentValues.put("deleted", Integer.valueOf(UNDELETED_FLAG));
                                contentValues.put(DELETED_TIME_STR, Integer.valueOf(UNDELETED_FLAG));
                                try {
                                    strArr2 = new String[CLOCK_DB_TYPE];
                                    strArr2[UNDELETED_FLAG] = string;
                                    strArr2[EMAIL_TOP_N] = String.valueOf(i2);
                                    contentResolver.update(Database.PKGNAME_URI, contentValues, "appPkgName=? AND userID=?", strArr2);
                                } catch (SQLiteException e2) {
                                    AwareLog.e(TAG, "Error: updatePkgNameTable " + e2);
                                }
                            }
                        } catch (SQLiteException e22) {
                            AwareLog.e(TAG, "Error: updatePkgNameTable " + e22);
                        } catch (Throwable th) {
                            query.close();
                        }
                    }
                    deleteOverDueInfo(contentResolver, context);
                    ContentValues contentValues2 = new ContentValues();
                    contentValues2.put(DELETED_TIME_STR, Integer.valueOf(EMAIL_TOP_N));
                    try {
                        contentResolver.update(Database.PKGNAME_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_DECREMENT).build(), contentValues2, null, null);
                    } catch (SQLiteException e3) {
                        AwareLog.e(TAG, "Error: updatePkgNameTable " + e3);
                    }
                    query.close();
                }
            }
        }
    }

    public static void updateReInstallPkgNameInfo(ContentResolver contentResolver, List<Integer> list) {
        if (contentResolver != null && list != null) {
            Object arraySet = new ArraySet();
            for (int i = UNDELETED_FLAG; i < list.size(); i += EMAIL_TOP_N) {
                arraySet.clear();
                loadReInstallPkgFromUserData(contentResolver, arraySet, ((Integer) list.get(i)).intValue());
                AwareLog.i(TAG, " update Name userId=" + list.get(i) + " set=" + arraySet + " i=" + i);
                Iterator it = arraySet.iterator();
                while (it.hasNext()) {
                    String str = (String) it.next();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("deleted", Integer.valueOf(UNDELETED_FLAG));
                    contentValues.put(DELETED_TIME_STR, Integer.valueOf(UNDELETED_FLAG));
                    try {
                        String[] strArr = new String[CLOCK_DB_TYPE];
                        strArr[UNDELETED_FLAG] = str;
                        strArr[EMAIL_TOP_N] = String.valueOf(list.get(i));
                        contentResolver.update(Database.PKGNAME_URI, contentValues, "appPkgName=? AND userID=?", strArr);
                    } catch (SQLiteException e) {
                        AwareLog.e(TAG, "Error: updateReInstallPkgNameInfo " + e);
                    }
                }
            }
        }
    }

    private static void loadReInstallPkgFromUserData(ContentResolver contentResolver, Set<String> set, int i) {
        Cursor query;
        String str = "UserData.appPkgName NOT IN (SELECT appPkgName from PkgRecord where userID=?)  AND UserData.appPkgName IN (SELECT appPkgName from PkgName where deleted =1 and userID=?) AND UserData.userID=?";
        try {
            Uri uri = Database.USERDATA_URI;
            String[] strArr = new String[EMAIL_TOP_N];
            strArr[UNDELETED_FLAG] = "UserData.appPkgName";
            String[] strArr2 = new String[IM_TOP_N];
            strArr2[UNDELETED_FLAG] = String.valueOf(i);
            strArr2[EMAIL_TOP_N] = String.valueOf(i);
            strArr2[CLOCK_DB_TYPE] = String.valueOf(i);
            query = contentResolver.query(uri, strArr, str, strArr2, "UserData._id");
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: loadReInstallPkgFromUserData " + e);
            query = null;
        }
        if (query != null) {
            while (query.moveToNext()) {
                try {
                    String string = query.getString(UNDELETED_FLAG);
                    if (string != null) {
                        set.add(string);
                    }
                } finally {
                    query.close();
                }
            }
        }
    }

    private static void deleteOverDueInfo(ContentResolver contentResolver, Context context) {
        if (contentResolver != null && context != null) {
            UserManager userManager = UserManager.get(context);
            if (userManager != null) {
                List users = userManager.getUsers();
                if (users != null) {
                    try {
                        contentResolver.delete(Database.PKGNAME_URI, "deleted=1 AND deletedTime=0", null);
                    } catch (SQLiteException e) {
                        AwareLog.e(TAG, "Error: deleteOverDueInfo " + e);
                    }
                    String str = "userID = ? AND (srcPkgName NOT IN  (select appPkgName from PkgName WHERE userID = ?) OR dstPkgName NOT IN  (select appPkgName from PkgName WHERE userID = ?))";
                    for (int i = UNDELETED_FLAG; i < users.size(); i += EMAIL_TOP_N) {
                        UserInfo userInfo = (UserInfo) users.get(i);
                        if (userInfo != null) {
                            int i2 = userInfo.id;
                            try {
                                Uri uri = Database.ASSOCIATE_URI;
                                String[] strArr = new String[IM_TOP_N];
                                strArr[UNDELETED_FLAG] = String.valueOf(i2);
                                strArr[EMAIL_TOP_N] = String.valueOf(i2);
                                strArr[CLOCK_DB_TYPE] = String.valueOf(i2);
                                contentResolver.delete(uri, str, strArr);
                            } catch (SQLiteException e2) {
                                AwareLog.e(TAG, "Error: deleteOverDueInfo " + e2);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void loadUserdataInfo(ContentResolver contentResolver, Map<String, Integer> map, Map<Integer, String> map2, List<Entry<Integer, Long>> list, int i) {
        if (contentResolver != null && map != null && map2 != null && list != null) {
            Cursor query;
            String str = "UserData.appPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND UserData.appPkgName NOT IN (select appPkgName from PkgRecord where flag=1 AND userID = ?)  AND UserData.userID=?";
            try {
                Uri uri = Database.USERDATA_URI;
                String[] strArr = new String[CLOCK_DB_TYPE];
                strArr[UNDELETED_FLAG] = "UserData.appPkgName";
                strArr[EMAIL_TOP_N] = "UserData.time";
                String[] strArr2 = new String[IM_TOP_N];
                strArr2[UNDELETED_FLAG] = String.valueOf(i);
                strArr2[EMAIL_TOP_N] = String.valueOf(i);
                strArr2[CLOCK_DB_TYPE] = String.valueOf(i);
                query = contentResolver.query(uri, strArr, str, strArr2, "UserData._id");
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUserdataInfo " + e);
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        String string = query.getString(UNDELETED_FLAG);
                        long j = query.getLong(EMAIL_TOP_N);
                        if (!map.containsKey(string)) {
                            int size = map.size();
                            map.put(string, Integer.valueOf(size));
                            map2.put(Integer.valueOf(size), string);
                        }
                        list.add(new SimpleEntry(map.get(string), Long.valueOf(j)));
                    } finally {
                        query.close();
                    }
                }
            }
        }
    }

    public static List<Integer> getUserIDList(ContentResolver contentResolver) {
        Cursor query;
        String str = "userID >=0 )GROUP BY (userID";
        try {
            Uri uri = Database.USERDATA_URI;
            String[] strArr = new String[EMAIL_TOP_N];
            strArr[UNDELETED_FLAG] = "userID";
            query = contentResolver.query(uri, strArr, str, null, null);
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: getUserIDList " + e);
            query = null;
        }
        if (query == null) {
            return null;
        }
        List<Integer> arrayList = new ArrayList();
        while (query.moveToNext()) {
            try {
                arrayList.add(Integer.valueOf(query.getInt(UNDELETED_FLAG)));
            } finally {
                query.close();
            }
        }
        return arrayList;
    }

    public static void deleteTable(ContentResolver contentResolver) {
        if (contentResolver != null) {
            try {
                contentResolver.delete(Database.ASSOCIATE_URI, null, null);
                contentResolver.delete(Database.PKGNAME_URI, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteTable " + e);
            }
        }
    }

    public static void deleteUserCount(ContentResolver contentResolver, int i) {
        if (contentResolver != null) {
            String str = "userID = ?";
            try {
                Uri uri = Database.ASSOCIATE_URI;
                String[] strArr = new String[EMAIL_TOP_N];
                strArr[UNDELETED_FLAG] = String.valueOf(i);
                contentResolver.delete(uri, str, strArr);
                uri = Database.PKGNAME_URI;
                strArr = new String[EMAIL_TOP_N];
                strArr[UNDELETED_FLAG] = String.valueOf(i);
                contentResolver.delete(uri, str, strArr);
                uri = Database.PKGRECORD_URI;
                strArr = new String[EMAIL_TOP_N];
                strArr[UNDELETED_FLAG] = String.valueOf(i);
                contentResolver.delete(uri, str, strArr);
                uri = Database.USERDATA_URI;
                strArr = new String[EMAIL_TOP_N];
                strArr[UNDELETED_FLAG] = String.valueOf(i);
                contentResolver.delete(uri, str, strArr);
                uri = Database.HABITPROTECTLIST_URI;
                strArr = new String[EMAIL_TOP_N];
                strArr[UNDELETED_FLAG] = String.valueOf(i);
                contentResolver.delete(uri, str, strArr);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteUserCount " + e);
            }
        }
    }

    public static Set<String> loadRemovedPkg(ContentResolver contentResolver, int i) {
        if (contentResolver == null) {
            return null;
        }
        Cursor query;
        Set<String> arraySet = new ArraySet();
        String str = "flag =1  AND userID =?";
        try {
            Uri uri = Database.PKGRECORD_URI;
            String[] strArr = new String[EMAIL_TOP_N];
            strArr[UNDELETED_FLAG] = "appPkgName";
            String[] strArr2 = new String[EMAIL_TOP_N];
            strArr2[UNDELETED_FLAG] = String.valueOf(i);
            query = contentResolver.query(uri, strArr, str, strArr2, null);
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: loadRemovedPkg " + e);
            query = null;
        }
        if (query == null) {
            return arraySet;
        }
        while (query.moveToNext()) {
            try {
                arraySet.add(query.getString(UNDELETED_FLAG));
            } finally {
                query.close();
            }
        }
        return arraySet;
    }

    public static void decreaseAppCount(ContentResolver contentResolver) {
        if (contentResolver != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DELETED_TIME_STR, Integer.valueOf(EMAIL_TOP_N));
            try {
                contentResolver.update(Database.PKGNAME_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_APP_SWITCH_DECREMENT).build(), contentValues, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: decreaseAppCount " + e);
            }
            return;
        }
        AwareLog.e(TAG, "decreaseAppCount resolver is null");
    }

    public static void insertDataToUserdataTable(ContentResolver contentResolver, String str, long j, int i) {
        if (contentResolver != null && str != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("appPkgName", str);
            contentValues.put("time", Long.valueOf(j));
            contentValues.put("userID", Integer.valueOf(i));
            try {
                contentResolver.insert(Database.USERDATA_URI, contentValues);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertDataToUserdataTable " + e);
            }
        }
    }

    public static void deleteTheRemovedPkgFromDB(ContentResolver contentResolver, String str, int i) {
        if (contentResolver != null && str != null) {
            String str2 = "appPkgName =? AND userID =?";
            try {
                Uri uri = Database.PKGRECORD_URI;
                String[] strArr = new String[CLOCK_DB_TYPE];
                strArr[UNDELETED_FLAG] = str;
                strArr[EMAIL_TOP_N] = String.valueOf(i);
                contentResolver.delete(uri, str2, strArr);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteTheRemovedPkgFromDB " + e);
            }
        }
    }

    public static void insertDataToPkgRecordTable(ContentResolver contentResolver, ContentValues contentValues) {
        if (contentResolver != null && contentValues != null) {
            try {
                contentResolver.insert(Database.PKGRECORD_URI, contentValues);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertDataToPkgNameTable " + e);
            }
        }
    }

    public static void deleteHabitProtectList(ContentResolver contentResolver, String str, int i) {
        if (contentResolver != null && str != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("deleted", Integer.valueOf(EMAIL_TOP_N));
            contentValues.put(DELETED_TIME_STR, Integer.valueOf(HABIT_PROTECT_MAX_TRAIN_COUNTS));
            try {
                Uri uri = Database.HABITPROTECTLIST_URI;
                String str2 = WHERECLAUSE;
                String[] strArr = new String[CLOCK_DB_TYPE];
                strArr[UNDELETED_FLAG] = str;
                strArr[EMAIL_TOP_N] = String.valueOf(i);
                contentResolver.update(uri, contentValues, str2, strArr);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteHabitProtectList " + e);
            }
        }
    }

    public static void deleteHabitProtectListAppFromDB(ContentResolver contentResolver, String str, int i) {
        if (contentResolver != null && str != null) {
            try {
                Uri uri = Database.HABITPROTECTLIST_URI;
                String str2 = WHERECLAUSE;
                String[] strArr = new String[CLOCK_DB_TYPE];
                strArr[UNDELETED_FLAG] = str;
                strArr[EMAIL_TOP_N] = String.valueOf(i);
                contentResolver.delete(uri, str2, strArr);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteHabitProtectListAppFromDB " + e);
            }
        }
    }

    public static void loadHabitProtectList(Context context, List<ProtectApp> list, int i) {
        Cursor cursor = null;
        if (context != null && list != null) {
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver != null) {
                String str = "userId = ?";
                try {
                    Uri uri = Database.HABITPROTECTLIST_URI;
                    String[] strArr = new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.deleted", "HabitProtectList.avgUsedFrequency", "HabitProtectList.UserId"};
                    String[] strArr2 = new String[EMAIL_TOP_N];
                    strArr2[UNDELETED_FLAG] = String.valueOf(i);
                    cursor = contentResolver.query(uri, strArr, str, strArr2, "CAST (HabitProtectList.avgUsedFrequency AS REAL) desc");
                } catch (SQLiteException e) {
                    AwareLog.e(TAG, "Error: loadHabitProtectList " + e);
                }
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String string = cursor.getString(UNDELETED_FLAG);
                        int i2 = cursor.getInt(EMAIL_TOP_N);
                        int i3 = cursor.getInt(CLOCK_DB_TYPE);
                        String string2 = cursor.getString(IM_TOP_N);
                        int i4 = cursor.getInt(4);
                        float f = 0.0f;
                        try {
                            f = Float.parseFloat(string2);
                        } catch (NumberFormatException e2) {
                            AwareLog.w(TAG, "parseFloat exception " + e2.toString());
                        } catch (Throwable th) {
                            cursor.close();
                        }
                        if (i2 == 0 || i2 == EMAIL_TOP_N) {
                            list.add(new ProtectApp(string, i2, i3, f, i4));
                        }
                        setAppTypList(context, string);
                    }
                    cursor.close();
                }
            }
        }
    }

    private static void setAppTypList(Context context, String str) {
        synchronized (mAppsTypeList) {
            if (!mAppsTypeList.containsKey(str)) {
                mAppsTypeList.put(str, Integer.valueOf(getAppType(context, str)));
            }
        }
    }

    public static void insertHabitProtectList(ContentResolver contentResolver, String str, int i, int i2) {
        if (contentResolver != null && str != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("appPkgName", str);
            contentValues.put("appType", Integer.valueOf(i));
            contentValues.put("recentUsed", AppHibernateCst.INVALID_PKG);
            contentValues.put("userID", Integer.valueOf(i2));
            AwareLog.d(TAG, "habit protect list insert:" + i + " pkgName:" + str);
            try {
                contentResolver.insert(Database.HABITPROTECTLIST_URI, contentValues);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertHabitProtectList " + e);
            }
        }
    }

    public static void updateDeletedHabitProtectList(ContentResolver contentResolver, String str, int i) {
        if (contentResolver != null && str != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("deleted", Integer.valueOf(UNDELETED_FLAG));
            contentValues.put(DELETED_TIME_STR, Integer.valueOf(UNDELETED_FLAG));
            AwareLog.d(TAG, "habit protect list update type: pkgName:" + str);
            try {
                Uri uri = Database.HABITPROTECTLIST_URI;
                String str2 = WHERECLAUSE;
                String[] strArr = new String[CLOCK_DB_TYPE];
                strArr[UNDELETED_FLAG] = str;
                strArr[EMAIL_TOP_N] = String.valueOf(i);
                contentResolver.update(uri, contentValues, str2, strArr);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectList " + e);
            }
        }
    }

    private static synchronized ICMSManager getCMSService() {
        ICMSManager iCMSManager;
        synchronized (IAwareHabitUtils.class) {
            if (mAwareservice == null) {
                mAwareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            }
            iCMSManager = mAwareservice;
        }
        return iCMSManager;
    }

    public static int getAppType(Context context, String str) {
        if (str == null || context == null) {
            AwareLog.e(TAG, "pkgName or ctx is null");
            return -1;
        }
        synchronized (mAppsTypeList) {
            int intValue;
            if (mAppsTypeList.containsKey(str)) {
                intValue = ((Integer) mAppsTypeList.get(str)).intValue();
                if (intValue == -1) {
                    intValue = getAppTypeFromPG(context, str);
                    if (intValue != -1) {
                        mAppsTypeList.put(str, Integer.valueOf(intValue));
                    }
                    return intValue;
                }
                return intValue;
            }
            intValue = getAppTypeFromCMS(str);
            if (-1 == intValue) {
                intValue = getAppTypeFromPG(context, str);
            }
            synchronized (mAppsTypeList) {
                mAppsTypeList.put(str, Integer.valueOf(intValue));
            }
            return intValue;
        }
    }

    public static int getAppTypeForAppMng(String str) {
        int i = -1;
        synchronized (mAppsTypeList) {
            if (mAppsTypeList.containsKey(str)) {
                i = ((Integer) mAppsTypeList.get(str)).intValue();
            }
        }
        return i;
    }

    private static int getAppTypeFromPG(Context context, String str) {
        long currentTimeMillis = System.currentTimeMillis();
        int i = -1;
        PGSdk instance = PGSdk.getInstance();
        if (instance != null) {
            try {
                i = instance.getPkgType(context, str);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "getAppTypeFromPG occur exception.");
            }
        }
        i = convertType(i);
        AwareLog.i(TAG, "getAppTypeFromPG spend time:" + (System.currentTimeMillis() - currentTimeMillis) + "ms PkgName:" + str + " type:" + i);
        return i;
    }

    private static int getAppTypeFromCMS(String str) {
        int i;
        AppTypeInfo appTypeInfo = null;
        long currentTimeMillis = System.currentTimeMillis();
        try {
            ICMSManager cMSService = getCMSService();
            if (cMSService == null) {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            } else {
                appTypeInfo = cMSService.getAppTypeInfo(str);
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAppTypeFromCMS RemoteException");
        }
        if (appTypeInfo == null) {
            i = -1;
        } else {
            i = appTypeInfo.getType();
        }
        AwareLog.i(TAG, "getAppTypeFromCMS spend time:" + (System.currentTimeMillis() - currentTimeMillis) + "ms PkgName:" + str + " type:" + i);
        return i;
    }

    public static boolean loadInstalledAppTypeInfo() {
        List list = null;
        try {
            ICMSManager cMSService = getCMSService();
            if (cMSService == null) {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            } else {
                ParceledListSlice allAppTypeInfo = cMSService.getAllAppTypeInfo();
                if (allAppTypeInfo != null) {
                    list = allAppTypeInfo.getList();
                }
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "loadAppTypeInfo RemoteException");
        }
        if (r0 == null) {
            return false;
        }
        synchronized (mAppsTypeList) {
            for (AppTypeInfo appTypeInfo : r0) {
                mAppsTypeList.put(appTypeInfo.getPkgName(), Integer.valueOf(appTypeInfo.getType()));
            }
        }
        return true;
    }

    private static int convertType(int i) {
        switch (i) {
            case EMAIL_TOP_N /*1*/:
                return MemoryConstant.MSG_BOOST_SIGKILL_SWITCH;
            case CLOCK_DB_TYPE /*2*/:
                return WifiProCommonUtils.HTTP_REDIRECTED;
            case IM_TOP_N /*3*/:
                return MemoryConstant.MSG_DIRECT_SWAPPINESS;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                return MemoryConstant.MSG_FILECACHE_NODE_SET_PROTECT_LRU;
            case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                return MemoryConstant.MSG_FILECACHE_SET_CONFIG_PROTECT_LRU;
            case LifeCycleStateMachine.TIME_OUT /*7*/:
                return 307;
            case ByteUtil.LONG_SIZE /*8*/:
                return 308;
            case HwGnssLogHandlerMsgID.UPDATESVSTATUS /*9*/:
                return 309;
            case AwareAppMngDFX.APPLICATION_STARTTYPE_COLD /*10*/:
                return 310;
            case AwareAppMngDFX.APPLICATION_STARTTYPE_TOTAL /*11*/:
                return 311;
            case HwGnssLogHandlerMsgID.UPDATESETPOSMODE /*12*/:
                return 312;
            default:
                return -1;
        }
    }

    public static boolean isInAppsTypeList(String str) {
        if (str == null) {
            return false;
        }
        synchronized (mAppsTypeList) {
            if (mAppsTypeList.containsKey(str)) {
                return true;
            }
            return false;
        }
    }

    public static Set<String> getAppListByType(int i) {
        synchronized (mAppsTypeList) {
            ArrayMap arrayMap = new ArrayMap(mAppsTypeList);
        }
        Set arraySet = new ArraySet();
        for (int i2 = UNDELETED_FLAG; i2 < arrayMap.size(); i2 += EMAIL_TOP_N) {
            if (((Integer) arrayMap.valueAt(i2)).intValue() == i) {
                arraySet.add(arrayMap.keyAt(i2));
            }
        }
        return arraySet;
    }

    public static void removefromAppsTypeList(String str) {
        synchronized (mAppsTypeList) {
            mAppsTypeList.remove(str);
        }
    }

    public static void clearAppsTypeList() {
        synchronized (mAppsTypeList) {
            mAppsTypeList.clear();
        }
    }

    public static void updateDeletedHabitProtectApp(ContentResolver contentResolver) {
        if (contentResolver != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DELETED_TIME_STR, Integer.valueOf(EMAIL_TOP_N));
            try {
                contentResolver.update(Database.HABITPROTECTLIST_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_DECREMENT).build(), contentValues, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp " + e);
            }
            try {
                contentResolver.delete(Database.HABITPROTECTLIST_URI, "deleted = 1  and deletedTime < 1", null);
            } catch (SQLiteException e2) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp " + e2);
            }
        }
    }

    public static void updateHabitProtectList(ContentResolver contentResolver, String str, String str2, String str3, int i) {
        if (contentResolver != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("recentUsed", str2);
            contentValues.put("avgUsedFrequency", str3);
            try {
                Uri uri = Database.HABITPROTECTLIST_URI;
                String str4 = WHERECLAUSE;
                String[] strArr = new String[CLOCK_DB_TYPE];
                strArr[UNDELETED_FLAG] = str;
                strArr[EMAIL_TOP_N] = String.valueOf(i);
                contentResolver.update(uri, contentValues, str4, strArr);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateHabitProtectList " + e);
            }
        }
    }

    public static void loadUnDeletedHabitProtectList(ContentResolver contentResolver, List<ProtectApp> list, int i) {
        Cursor query;
        if (contentResolver != null && list != null) {
            try {
                Uri uri = Database.HABITPROTECTLIST_URI;
                String[] strArr = new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.recentUsed", "HabitProtectList.avgUsedFrequency"};
                String[] strArr2 = new String[EMAIL_TOP_N];
                strArr2[UNDELETED_FLAG] = String.valueOf(i);
                query = contentResolver.query(uri, strArr, " HabitProtectList.deleted = 0 and userId = ?", strArr2, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUnDeletedHabitProtectList " + e);
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    String string;
                    int i2;
                    String string2;
                    float f;
                    try {
                        string = query.getString(UNDELETED_FLAG);
                        i2 = query.getInt(EMAIL_TOP_N);
                        string2 = query.getString(CLOCK_DB_TYPE);
                        f = 0.0f;
                        f = Float.parseFloat(query.getString(IM_TOP_N));
                    } catch (NumberFormatException e2) {
                        AwareLog.w(TAG, "parseFloat exception " + e2.toString());
                    } catch (Throwable th) {
                        query.close();
                    }
                    if (i2 == 0 || i2 == EMAIL_TOP_N) {
                        list.add(new ProtectApp(string, i2, string2, f));
                    }
                }
                query.close();
            }
        }
    }

    private static AwareConfig getConfig(String str, String str2) {
        AwareConfig awareConfig = null;
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            AwareLog.e(TAG, "featureName or configName is null");
            return null;
        }
        try {
            ICMSManager asInterface = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (asInterface == null) {
                AwareLog.i(TAG, "can not find service IAwareCMSService.");
            } else {
                awareConfig = asInterface.getConfig(str, str2);
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "AppMngFeature getConfig RemoteException");
        }
        return awareConfig;
    }

    public static ArraySet<String> getHabitFilterListFromCMS() {
        AwareConfig config = getConfig(APPMNG, HABIT_FILTER_LIST);
        if (config != null) {
            ArraySet<String> arraySet = new ArraySet();
            for (Item item : config.getConfigList()) {
                if (item == null || item.getSubItemList() == null) {
                    AwareLog.w(TAG, "getHabitFilterListFromCMS continue cause null item");
                } else {
                    for (SubItem subItem : item.getSubItemList()) {
                        if (!(subItem == null || TextUtils.isEmpty(subItem.getValue()))) {
                            arraySet.add(subItem.getValue());
                        }
                    }
                }
            }
            return arraySet;
        }
        AwareLog.w(TAG, "getHabitFilterListFromCMS failure cause null configList");
        return null;
    }

    public static Map<String, Integer> getForceProtectConfigFromCMS() {
        AwareConfig config = getConfig(APPMNG, HABIT_FORCE_PROTECT_CONFIG);
        if (config != null) {
            Map arrayMap = new ArrayMap();
            for (Item item : config.getConfigList()) {
                if (item == null || item.getSubItemList() == null) {
                    AwareLog.w(TAG, "getForceProtectConfigFromCMS continue cause null item");
                } else {
                    for (SubItem subItem : item.getSubItemList()) {
                        if (subItem != null) {
                            String name = subItem.getName();
                            if (name != null) {
                                int parseInt;
                                try {
                                    parseInt = Integer.parseInt(subItem.getValue());
                                } catch (NumberFormatException e) {
                                    AwareLog.w(TAG, "getForceProtectConfigFromCMS NumberFormatException Ex");
                                    parseInt = UNDELETED_FLAG;
                                }
                                arrayMap.put(name, Integer.valueOf(parseInt));
                            }
                        }
                    }
                }
            }
            AwareLog.d(TAG, "getForceProtectConfigFromCMS config=" + arrayMap);
            return arrayMap;
        }
        AwareLog.w(TAG, "getForceProtectConfigFromCMS failure cause null configList");
        return null;
    }

    public static boolean isGCMApp(Context context, String str) {
        if (str == null || context == null) {
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setPackage(str);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        List queryBroadcastReceivers = packageManager.queryBroadcastReceivers(intent, UNDELETED_FLAG);
        AwareLog.d(TAG, "isGCMApp spend time:" + (System.currentTimeMillis() - currentTimeMillis) + " pkg: " + str);
        if (queryBroadcastReceivers == null || queryBroadcastReceivers.size() <= 0) {
            return false;
        }
        AwareLog.i(TAG, "isGCMApp pkg: " + str);
        return true;
    }

    public static boolean isGuestUser(Context context, int i) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        UserManager userManager = UserManager.get(context);
        if (userManager == null) {
            return false;
        }
        UserInfo userInfo = userManager.getUserInfo(i);
        if (userInfo != null) {
            z = userInfo.isGuest();
        }
        return z;
    }
}
