package android.rms.iaware;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConstant;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppTypeRecoManager {
    public static final String APP_ATTR = "appAttr";
    public static final int APP_FROM_ABROAD = 1;
    public static final int APP_FROM_CHINA = 0;
    public static final int APP_FROM_UNKNOWN = -1;
    public static final String APP_PKG_NAME = "pkgName";
    public static final String APP_STATUS = "appsStatus";
    public static final String APP_TYPE = "appType";
    public static final String APP_TYPE_INIT_ACTION = "APPTYPE_INIT_ACTION";
    public static final int APP_USE_IN_DAY = 7;
    public static final long ONE_DAY = 86400000;
    private static final Object SLOCK = new Object();
    private static final String TAG = "AppTypeRecoManager";
    private static AppTypeRecoManager sAppTypeRecoManager = null;
    private final List<String> mAppUsedInfos = new ArrayList();
    private final ArrayMap<String, AppTypeCacheInfo> mAppsTypeMap = new ArrayMap<>();
    private boolean mIsReady = false;
    private final Object mLock = new Object();
    private final List<String> mTopImList = new ArrayList();

    public static class AppTypeCacheInfo {
        private int mAttr;
        private int mSource;
        private int mType;

        public AppTypeCacheInfo(int type, int attr, int source) {
            this.mType = type;
            this.mAttr = attr;
            this.mSource = source;
        }

        public AppTypeCacheInfo(int type, int attr) {
            this(type, attr, 0);
        }

        public int getType() {
            return this.mType;
        }

        public int getAttribute() {
            return this.mAttr;
        }

        public int getRecogSource() {
            return this.mSource;
        }

        public void setInfo(int type, int attr) {
            this.mType = type;
            this.mAttr = attr;
        }
    }

    public static AppTypeRecoManager getInstance() {
        AppTypeRecoManager appTypeRecoManager;
        synchronized (SLOCK) {
            if (sAppTypeRecoManager == null) {
                sAppTypeRecoManager = new AppTypeRecoManager();
            }
            appTypeRecoManager = sAppTypeRecoManager;
        }
        return appTypeRecoManager;
    }

    private AppTypeRecoManager() {
    }

    public void init(Context ctx) {
        synchronized (this.mLock) {
            AwareLog.i(TAG, "init begin.");
            if (ctx != null) {
                if (!this.mIsReady) {
                    ContentResolver resolver = ctx.getContentResolver();
                    ArrayMap<String, AppTypeCacheInfo> map = new ArrayMap<>();
                    loadAppType(resolver, map);
                    if (!map.isEmpty()) {
                        synchronized (this.mAppsTypeMap) {
                            this.mAppsTypeMap.putAll((ArrayMap<? extends String, ? extends AppTypeCacheInfo>) map);
                        }
                        List<String> allImList = new ArrayList<>();
                        loadAllIm(resolver, allImList);
                        synchronized (this.mTopImList) {
                            this.mTopImList.addAll(allImList);
                            AwareLog.d(TAG, "IMList:" + this.mTopImList);
                        }
                        List<String> usedImList = new ArrayList<>();
                        loadUsedApp(resolver, usedImList, allImList, 0, 7);
                        synchronized (this.mAppUsedInfos) {
                            this.mAppUsedInfos.addAll(usedImList);
                            AwareLog.d(TAG, "NewUsedIMList:" + usedImList);
                        }
                        this.mIsReady = true;
                        AwareLog.i(TAG, "init end.");
                        return;
                    }
                    return;
                }
            }
            AwareLog.i(TAG, "no need to init");
        }
    }

    public void deinit() {
        synchronized (this.mAppsTypeMap) {
            this.mAppsTypeMap.clear();
        }
        synchronized (this.mTopImList) {
            this.mTopImList.clear();
        }
        synchronized (this) {
            this.mIsReady = false;
        }
        synchronized (this.mAppUsedInfos) {
            this.mAppUsedInfos.clear();
        }
        AwareLog.i(TAG, "deinit.");
    }

    public boolean loadInstalledAppTypeInfo() {
        List<AppTypeInfo> list = null;
        try {
            IBinder binder = IAwareCMSManager.getICMSManager();
            if (binder != null) {
                list = IAwareCMSManager.getAllAppTypeInfo(binder);
            } else {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "loadAppTypeInfo RemoteException");
        }
        if (list == null) {
            return false;
        }
        for (AppTypeInfo info : list) {
            if (info != null) {
                addAppType(info.getPkgName(), info.getType(), info.getAttribute());
            }
        }
        return true;
    }

    private void loadAllIm(ContentResolver resolver, List<String> imList) {
        if (resolver != null && imList != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(AwareConstant.Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName"}, " HabitProtectList.deleted = 0 and HabitProtectList.appType = ? and HabitProtectList.userID = 0", new String[]{String.valueOf(0)}, "CAST(HabitProtectList.avgUsedFrequency AS REAL) desc");
                if (cursor == null) {
                    closeCursor(cursor);
                    return;
                }
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    if (pkgName != null && !pkgName.isEmpty()) {
                        imList.add(pkgName);
                    }
                }
                closeCursor(cursor);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error SQLiteException: loadAllIm");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error IllegalStateException: loadAllIm");
            } catch (Throwable th) {
                closeCursor(null);
                throw th;
            }
        }
    }

    public boolean isTopIm(String pkgName, int count) {
        if (pkgName == null || count <= 0) {
            return false;
        }
        return getTopImList(count).contains(pkgName);
    }

    private void loadAppType(ContentResolver resolver, Map<String, AppTypeCacheInfo> typeMap) {
        if (resolver != null && typeMap != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(AwareConstant.Database.APPTYPE_URI, new String[]{"appPkgName", "typeAttri", APP_TYPE, "source"}, null, null, null);
                if (cursor == null) {
                    AwareLog.e(TAG, "loadAppType cursor is null.");
                    closeCursor(cursor);
                    return;
                }
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    int attri = cursor.getInt(1);
                    int apptype = cursor.getInt(2);
                    int source = cursor.getInt(3);
                    if (!TextUtils.isEmpty(pkgName) && apptype != 305) {
                        typeMap.put(pkgName, new AppTypeCacheInfo(apptype, attri, source));
                    }
                }
                closeCursor(cursor);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppType SQLiteException");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadAppType IllegalStateException");
            } catch (Throwable th) {
                closeCursor(null);
                throw th;
            }
        }
    }

    private void loadUsedApp(ContentResolver resolver, List<String> appUsedInfos, List<String> imList, int userId, int dayNum) {
        Cursor cursor;
        Throwable th;
        if (resolver != null && appUsedInfos != null && imList != null) {
            if (imList.size() != 0) {
                Cursor cursor2 = null;
                try {
                    long now = System.currentTimeMillis();
                    try {
                    } catch (SQLiteException e) {
                        AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
                        closeCursor(cursor2);
                    } catch (IllegalStateException e2) {
                        AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
                        closeCursor(cursor2);
                    } catch (Throwable th2) {
                        th = th2;
                        cursor = null;
                        closeCursor(cursor);
                        throw th;
                    }
                    try {
                        cursor = resolver.query(AwareConstant.AppUsageDatabase.APPUSAGE_URI, new String[]{APP_PKG_NAME}, "foregroungTime > ? and foregroungTime < ? and userID = ?", new String[]{String.valueOf(now - (((long) dayNum) * ONE_DAY)), String.valueOf(now), String.valueOf(userId)}, null);
                        if (cursor == null) {
                            closeCursor(cursor);
                            return;
                        }
                        while (cursor.moveToNext()) {
                            try {
                                String pkgName = cursor.getString(0);
                                if (pkgName != null && !pkgName.isEmpty()) {
                                    if (imList.contains(pkgName)) {
                                        appUsedInfos.add(pkgName);
                                    }
                                }
                            } catch (SQLiteException e3) {
                                cursor2 = cursor;
                                AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
                                closeCursor(cursor2);
                            } catch (IllegalStateException e4) {
                                cursor2 = cursor;
                                AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
                                closeCursor(cursor2);
                            } catch (Throwable th3) {
                                th = th3;
                                closeCursor(cursor);
                                throw th;
                            }
                        }
                        closeCursor(cursor);
                    } catch (SQLiteException e5) {
                        cursor2 = null;
                        AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
                        closeCursor(cursor2);
                    } catch (IllegalStateException e6) {
                        cursor2 = null;
                        AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
                        closeCursor(cursor2);
                    } catch (Throwable th4) {
                        th = th4;
                        cursor = null;
                        closeCursor(cursor);
                        throw th;
                    }
                } catch (SQLiteException e7) {
                    AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
                    closeCursor(cursor2);
                } catch (IllegalStateException e8) {
                    AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
                    closeCursor(cursor2);
                } catch (Throwable th5) {
                    th = th5;
                    cursor = cursor2;
                    closeCursor(cursor);
                    throw th;
                }
            }
        }
    }

    public void loadAppUsedInfo(Context cxt, Map<String, Long> appUsedMap, int userId, int dayNum) {
        Throwable th;
        if (cxt == null) {
            return;
        }
        if (appUsedMap != null) {
            ContentResolver resolver = cxt.getContentResolver();
            if (resolver != null) {
                Cursor cursor = null;
                try {
                    long now = System.currentTimeMillis();
                    try {
                        try {
                            cursor = resolver.query(AwareConstant.AppUsageDatabase.APPUSAGE_URI, new String[]{APP_PKG_NAME, "foregroungTime"}, "foregroungTime > ? and foregroungTime < ? and userID = ? ", new String[]{String.valueOf(now - (((long) dayNum) * ONE_DAY)), String.valueOf(now), String.valueOf(userId)}, null);
                            if (cursor == null) {
                                closeCursor(cursor);
                                return;
                            }
                            while (cursor.moveToNext()) {
                                String pkgName = cursor.getString(0);
                                if (pkgName != null) {
                                    if (!pkgName.isEmpty()) {
                                        appUsedMap.put(pkgName, Long.valueOf(cursor.getLong(1)));
                                    }
                                }
                            }
                            closeCursor(cursor);
                        } catch (SQLiteException e) {
                            AwareLog.e(TAG, "Error SQLiteException: loadAppUsedInfo");
                            closeCursor(cursor);
                        } catch (IllegalStateException e2) {
                            AwareLog.e(TAG, "Error IllegalStateException: loadAppUsedInfo");
                            closeCursor(cursor);
                        }
                    } catch (SQLiteException e3) {
                        AwareLog.e(TAG, "Error SQLiteException: loadAppUsedInfo");
                        closeCursor(cursor);
                    } catch (IllegalStateException e4) {
                        AwareLog.e(TAG, "Error IllegalStateException: loadAppUsedInfo");
                        closeCursor(cursor);
                    } catch (Throwable th2) {
                        th = th2;
                        closeCursor(null);
                        throw th;
                    }
                } catch (SQLiteException e5) {
                    AwareLog.e(TAG, "Error SQLiteException: loadAppUsedInfo");
                    closeCursor(cursor);
                } catch (IllegalStateException e6) {
                    AwareLog.e(TAG, "Error IllegalStateException: loadAppUsedInfo");
                    closeCursor(cursor);
                } catch (Throwable th3) {
                    th = th3;
                    closeCursor(null);
                    throw th;
                }
            }
        }
    }

    private List<String> getTopImList(int topN) {
        List<String> result;
        List<String> tmpAppUsedInfos = new ArrayList<>();
        synchronized (this.mAppUsedInfos) {
            tmpAppUsedInfos.addAll(this.mAppUsedInfos);
        }
        synchronized (this.mTopImList) {
            result = new ArrayList<>();
            int i = 0;
            int length = this.mTopImList.size();
            while (true) {
                if (i >= length) {
                    break;
                }
                String pkgName = this.mTopImList.get(i);
                if (pkgName != null) {
                    if (tmpAppUsedInfos.contains(pkgName)) {
                        result.add(pkgName);
                        if (result.size() >= topN) {
                            break;
                        }
                    }
                }
                i++;
            }
        }
        return result;
    }

    public List<String> dumpTopImList(int topN) {
        if (topN <= 0) {
            return null;
        }
        return getTopImList(topN);
    }

    public int getAppType(String pkgName) {
        AppTypeCacheInfo info;
        synchronized (this.mAppsTypeMap) {
            info = this.mAppsTypeMap.get(pkgName);
        }
        if (info == null || info.getType() == -2) {
            return -1;
        }
        return info.getType();
    }

    public int getAppAttribute(String pkgName) {
        AppTypeCacheInfo info;
        synchronized (this.mAppsTypeMap) {
            info = this.mAppsTypeMap.get(pkgName);
        }
        if (info == null) {
            return -1;
        }
        return info.getAttribute();
    }

    public int getAppWhereFrom(String pkgName) {
        synchronized (this.mAppsTypeMap) {
            if (!this.mAppsTypeMap.containsKey(pkgName)) {
                return -1;
            }
            AppTypeCacheInfo appTypeInfo = this.mAppsTypeMap.get(pkgName);
            if (appTypeInfo == null) {
                return -1;
            }
            int appAttr = appTypeInfo.getAttribute();
            if (appAttr == -1) {
                return -1;
            }
            return (appAttr & AppTypeInfo.APP_ATTRIBUTE_OVERSEA) == Integer.MIN_VALUE ? 1 : 0;
        }
    }

    public boolean containsAppType(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        synchronized (this.mAppsTypeMap) {
            if (this.mAppsTypeMap.containsKey(pkgName)) {
                return true;
            }
            return false;
        }
    }

    public Set<String> getAppsByType(int appType) {
        ArrayMap<String, Integer> appList;
        synchronized (this.mAppsTypeMap) {
            appList = new ArrayMap<>(this.mAppsTypeMap.size());
            for (Map.Entry<String, AppTypeCacheInfo> entry : this.mAppsTypeMap.entrySet()) {
                appList.put(entry.getKey(), Integer.valueOf(entry.getValue().getType()));
            }
        }
        ArraySet<String> appSet = new ArraySet<>();
        int size = appList.size();
        for (int i = 0; i < size; i++) {
            if (appList.valueAt(i).intValue() == appType) {
                appSet.add(appList.keyAt(i));
            }
        }
        return appSet;
    }

    public Map<Integer, List<String>> getAppsByAttributes(List<Integer> attributeBitList) {
        if (attributeBitList == null || attributeBitList.isEmpty()) {
            return null;
        }
        Map<Integer, List<String>> result = null;
        synchronized (this.mAppsTypeMap) {
            for (Map.Entry<String, AppTypeCacheInfo> entry : this.mAppsTypeMap.entrySet()) {
                int attribute = entry.getValue().getAttribute();
                if (attribute != -1) {
                    for (Integer attributeBit : attributeBitList) {
                        if ((attributeBit.intValue() & attribute) == attributeBit.intValue()) {
                            if (result == null) {
                                result = new HashMap<>();
                            }
                            List<String> tempList = result.get(attributeBit);
                            if (tempList == null) {
                                tempList = new ArrayList();
                                result.put(attributeBit, tempList);
                            }
                            tempList.add(entry.getKey());
                        }
                    }
                }
            }
        }
        return result;
    }

    public Set<String> getAlarmApps() {
        ArrayMap<String, Integer> appList;
        synchronized (this.mAppsTypeMap) {
            appList = new ArrayMap<>(this.mAppsTypeMap.size());
            for (Map.Entry<String, AppTypeCacheInfo> entry : this.mAppsTypeMap.entrySet()) {
                appList.put(entry.getKey(), Integer.valueOf(entry.getValue().getType()));
            }
        }
        ArraySet<String> appSet = new ArraySet<>();
        int size = appList.size();
        for (int i = 0; i < size; i++) {
            if (appList.valueAt(i).intValue() == 5 || appList.valueAt(i).intValue() == 310) {
                appSet.add(appList.keyAt(i));
            }
        }
        return appSet;
    }

    public void removeAppType(String pkgName) {
        synchronized (this.mAppsTypeMap) {
            this.mAppsTypeMap.remove(pkgName);
        }
    }

    public void addAppType(String pkgName, int type, int attr) {
        synchronized (this.mAppsTypeMap) {
            AppTypeCacheInfo cacheInfo = this.mAppsTypeMap.get(pkgName);
            if (cacheInfo == null) {
                this.mAppsTypeMap.put(pkgName, new AppTypeCacheInfo(type, attr));
            } else {
                cacheInfo.setInfo(type, attr);
            }
        }
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public int convertType(int appType) {
        if (appType <= 255) {
            return appType;
        }
        switch (appType) {
            case AppTypeInfo.PG_APP_TYPE_SMS /* 302 */:
                return 27;
            case AppTypeInfo.PG_APP_TYPE_EMAIL /* 303 */:
                return 1;
            case AppTypeInfo.PG_APP_TYPE_INPUT_METHOD /* 304 */:
                return 19;
            case AppTypeInfo.PG_APP_TYPE_GAME /* 305 */:
                return 9;
            case AppTypeInfo.PG_APP_TYPE_BROWSER /* 306 */:
                return 18;
            case AppTypeInfo.PG_APP_TYPE_EBOOK /* 307 */:
                return 6;
            case AppTypeInfo.PG_APP_TYPE_VIDEO /* 308 */:
                return 8;
            case AppTypeInfo.PG_APP_TYPE_SCRLOCK /* 309 */:
            default:
                return convertTypeEx(appType);
            case AppTypeInfo.PG_APP_TYPE_ALARM /* 310 */:
                return 5;
            case AppTypeInfo.PG_APP_TYPE_IM /* 311 */:
                return 0;
            case AppTypeInfo.PG_APP_TYPE_MUSIC /* 312 */:
                return 7;
        }
    }

    public int convertTypeEx(int appType) {
        if (appType == 301) {
            return 28;
        }
        if (appType == 313) {
            return 3;
        }
        switch (appType) {
            case AppTypeInfo.PG_APP_TYPE_OFFICE /* 315 */:
                return 12;
            case AppTypeInfo.PG_APP_TYPE_GALLERY /* 316 */:
                return 29;
            case AppTypeInfo.PG_APP_TYPE_SIP /* 317 */:
                return 30;
            case AppTypeInfo.PG_APP_TYPE_NEWS_CLIENT /* 318 */:
                return 26;
            case AppTypeInfo.PG_APP_TYPE_SHOP /* 319 */:
                return 14;
            case AppTypeInfo.PG_APP_TYPE_APP_MARKET /* 320 */:
                return 31;
            case AppTypeInfo.PG_APP_TYPE_LIFE_TOOL /* 321 */:
                return 32;
            case AppTypeInfo.PG_APP_TYPE_EDUCATION /* 322 */:
                return 33;
            case AppTypeInfo.PG_APP_TYPE_MONEY /* 323 */:
                return 34;
            case AppTypeInfo.PG_APP_TYPE_CAMERA /* 324 */:
                return 17;
            case AppTypeInfo.PG_APP_TYPE_PEDOMETER /* 325 */:
                return 2;
            default:
                return appType;
        }
    }

    public boolean isReady() {
        return this.mIsReady;
    }
}
