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
    public static final String APPTYPE_INIT_ACTION = "APPTYPE_INIT_ACTION";
    public static final String APP_ATTR = "appAttr";
    public static final int APP_FROM_ABROAD = 1;
    public static final int APP_FROM_CHINA = 0;
    public static final int APP_FROM_UNKNOWN = -1;
    public static final String APP_PKGNAME = "pkgName";
    public static final String APP_STATUS = "appsSatus";
    public static final String APP_TYPE = "appType";
    public static final int APP_USEINDAY = 7;
    public static final long ONE_DAY = 86400000;
    private static final String TAG = "AppTypeRecoManager";
    private static AppTypeRecoManager mAppTypeRecoManager = null;
    private final List<String> mAppUsedInfos = new ArrayList();
    private final ArrayMap<String, AppTypeCacheInfo> mAppsTypeMap = new ArrayMap<>();
    private boolean mIsReady = false;
    private final List<String> mTopIMList = new ArrayList();

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
            this.mType = type;
            this.mAttr = attr;
            this.mSource = 0;
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

    public static synchronized AppTypeRecoManager getInstance() {
        AppTypeRecoManager appTypeRecoManager;
        synchronized (AppTypeRecoManager.class) {
            if (mAppTypeRecoManager == null) {
                mAppTypeRecoManager = new AppTypeRecoManager();
            }
            appTypeRecoManager = mAppTypeRecoManager;
        }
        return appTypeRecoManager;
    }

    private AppTypeRecoManager() {
    }

    public synchronized void init(Context ctx) {
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
                    loadAllIM(resolver, allImList);
                    synchronized (this.mTopIMList) {
                        this.mTopIMList.addAll(allImList);
                        AwareLog.d(TAG, "IMList:" + this.mTopIMList);
                    }
                    List<String> usedIMList = new ArrayList<>();
                    loadUsedApp(resolver, usedIMList, allImList, 0, 7);
                    synchronized (this.mAppUsedInfos) {
                        this.mAppUsedInfos.addAll(usedIMList);
                        AwareLog.d(TAG, "NewUsedIMList:" + usedIMList);
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

    public void deinit() {
        synchronized (this.mAppsTypeMap) {
            this.mAppsTypeMap.clear();
        }
        synchronized (this.mTopIMList) {
            this.mTopIMList.clear();
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

    private void loadAllIM(ContentResolver resolver, List<String> imList) {
        if (resolver != null && imList != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(AwareConstant.Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName"}, " HabitProtectList.deleted = 0 and HabitProtectList.appType = ? and HabitProtectList.userID = 0", new String[]{String.valueOf(0)}, "CAST(HabitProtectList.avgUsedFrequency AS REAL) desc");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String pkgName = cursor.getString(0);
                        if (pkgName != null && !pkgName.isEmpty()) {
                            imList.add(pkgName);
                        }
                    }
                    cursor.close();
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error SQLiteException: loadAllIM");
                if (0 == 0) {
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error IllegalStateException: loadAllIM");
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

    public boolean isTopIM(String pkgName, int count) {
        if (pkgName == null || count <= 0) {
            return false;
        }
        return getTopIMList(count).contains(pkgName);
    }

    private void loadAppType(ContentResolver resolver, Map<String, AppTypeCacheInfo> typeMap) {
        if (resolver != null && typeMap != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(AwareConstant.Database.APPTYPE_URI, new String[]{"appPkgName", "typeAttri", APP_TYPE, "source"}, null, null, null);
                if (cursor == null) {
                    AwareLog.e(TAG, "loadAppType cursor is null.");
                    if (cursor != null) {
                        cursor.close();
                        return;
                    }
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
                cursor.close();
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppType SQLiteException");
                if (0 == 0) {
                }
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadAppType IllegalStateException");
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

    /* JADX WARNING: Removed duplicated region for block: B:65:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:77:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:79:? A[RETURN, SYNTHETIC] */
    private void loadUsedApp(ContentResolver resolver, List<String> appUsedInfos, List<String> imList, int userId, int dayNum) {
        Cursor cursor;
        if (resolver != null && appUsedInfos != null && imList != null) {
            if (imList.size() != 0) {
                Cursor cursor2 = null;
                try {
                    long now = System.currentTimeMillis();
                    try {
                        cursor = null;
                    } catch (SQLiteException e) {
                        AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
                        if (cursor2 == null) {
                        }
                        cursor2.close();
                    } catch (IllegalStateException e2) {
                        try {
                            AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
                            if (cursor2 == null) {
                            }
                            cursor2.close();
                        } catch (Throwable th) {
                            th = th;
                            cursor = cursor2;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        cursor = null;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                    try {
                        Cursor cursor3 = resolver.query(AwareConstant.AppUsageDatabase.APPUSAGE_URI, new String[]{APP_PKGNAME}, "foregroungTime > ? and foregroungTime < ? and userID = ?", new String[]{String.valueOf(now - (((long) dayNum) * 86400000)), String.valueOf(now), String.valueOf(userId)}, null);
                        if (cursor3 != null) {
                            while (cursor3.moveToNext()) {
                                try {
                                    String pkgName = cursor3.getString(0);
                                    if (pkgName != null && !pkgName.isEmpty()) {
                                        if (imList.contains(pkgName)) {
                                            appUsedInfos.add(pkgName);
                                        }
                                    }
                                } catch (SQLiteException e3) {
                                    cursor2 = cursor3;
                                    AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
                                    if (cursor2 == null) {
                                        return;
                                    }
                                    cursor2.close();
                                } catch (IllegalStateException e4) {
                                    cursor2 = cursor3;
                                    AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
                                    if (cursor2 == null) {
                                        return;
                                    }
                                    cursor2.close();
                                } catch (Throwable th3) {
                                    th = th3;
                                    cursor = cursor3;
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    throw th;
                                }
                            }
                            cursor3.close();
                        } else if (cursor3 != null) {
                            cursor3.close();
                        }
                    } catch (SQLiteException e5) {
                        cursor2 = null;
                        AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
                        if (cursor2 == null) {
                        }
                        cursor2.close();
                    } catch (IllegalStateException e6) {
                        cursor2 = null;
                        AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
                        if (cursor2 == null) {
                        }
                        cursor2.close();
                    } catch (Throwable th4) {
                        th = th4;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                } catch (SQLiteException e7) {
                    AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
                    if (cursor2 == null) {
                    }
                    cursor2.close();
                } catch (IllegalStateException e8) {
                    AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
                    if (cursor2 == null) {
                    }
                    cursor2.close();
                } catch (Throwable th5) {
                    th = th5;
                    cursor = null;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x009d A[Catch:{ SQLiteException -> 0x009e, IllegalStateException -> 0x0091, all -> 0x008b, all -> 0x00ac }] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00af  */
    /* JADX WARNING: Removed duplicated region for block: B:63:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:64:? A[RETURN, SYNTHETIC] */
    public void loadAppUsedInfo(Context cxt, Map<String, Long> appUsedMap, int userId, int dayNum) {
        if (cxt == null) {
            return;
        }
        if (appUsedMap != null) {
            ContentResolver resolver = cxt.getContentResolver();
            if (resolver != null) {
                Cursor cursor = null;
                try {
                    long now = System.currentTimeMillis();
                    long timeDiff = now - (((long) dayNum) * 86400000);
                    try {
                    } catch (SQLiteException e) {
                        AwareLog.e(TAG, "Error SQLiteException: loadAppUsedInfo");
                        if (0 == 0) {
                        }
                        cursor.close();
                    } catch (IllegalStateException e2) {
                        AwareLog.e(TAG, "Error IllegalStateException: loadAppUsedInfo");
                        if (0 != 0) {
                        }
                    } catch (Throwable th) {
                        th = th;
                        if (0 != 0) {
                        }
                        throw th;
                    }
                    try {
                        cursor = resolver.query(AwareConstant.AppUsageDatabase.APPUSAGE_URI, new String[]{APP_PKGNAME, "foregroungTime"}, "foregroungTime > ? and foregroungTime < ? and userID = ? ", new String[]{String.valueOf(timeDiff), String.valueOf(now), String.valueOf(userId)}, null);
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                String pkgName = cursor.getString(0);
                                if (pkgName != null) {
                                    if (!pkgName.isEmpty()) {
                                        appUsedMap.put(pkgName, Long.valueOf(cursor.getLong(1)));
                                    }
                                }
                            }
                            cursor.close();
                        } else if (cursor != null) {
                            cursor.close();
                        }
                    } catch (SQLiteException e3) {
                        AwareLog.e(TAG, "Error SQLiteException: loadAppUsedInfo");
                        if (0 == 0) {
                            return;
                        }
                        cursor.close();
                    } catch (IllegalStateException e4) {
                        AwareLog.e(TAG, "Error IllegalStateException: loadAppUsedInfo");
                        if (0 != 0) {
                            cursor.close();
                        }
                    }
                } catch (SQLiteException e5) {
                    AwareLog.e(TAG, "Error SQLiteException: loadAppUsedInfo");
                    if (0 == 0) {
                    }
                    cursor.close();
                } catch (IllegalStateException e6) {
                    AwareLog.e(TAG, "Error IllegalStateException: loadAppUsedInfo");
                    if (0 != 0) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (0 != 0) {
                    }
                    throw th;
                }
            }
        }
    }

    private List<String> getTopIMList(int topN) {
        List<String> result;
        List<String> tmpAppUsedInfos = new ArrayList<>();
        synchronized (this.mAppUsedInfos) {
            tmpAppUsedInfos.addAll(this.mAppUsedInfos);
        }
        synchronized (this.mTopIMList) {
            result = new ArrayList<>();
            int i = 0;
            int length = this.mTopIMList.size();
            while (true) {
                if (i >= length) {
                    break;
                }
                String pkgName = this.mTopIMList.get(i);
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

    public List<String> dumpTopIMList(int topN) {
        if (topN <= 0) {
            return null;
        }
        return getTopIMList(topN);
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
            return (appAttr & Integer.MIN_VALUE) == Integer.MIN_VALUE ? 1 : 0;
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

    public int convertType(int appType) {
        if (appType <= 255) {
            return appType;
        }
        switch (appType) {
            case AppTypeInfo.PG_APP_TYPE_LAUNCHER:
                return 28;
            case AppTypeInfo.PG_APP_TYPE_SMS:
                return 27;
            case AppTypeInfo.PG_APP_TYPE_EMAIL:
                return 1;
            case AppTypeInfo.PG_APP_TYPE_INPUTMETHOD:
                return 19;
            case AppTypeInfo.PG_APP_TYPE_GAME:
                return 9;
            case AppTypeInfo.PG_APP_TYPE_BROWSER:
                return 18;
            case AppTypeInfo.PG_APP_TYPE_EBOOK:
                return 6;
            case AppTypeInfo.PG_APP_TYPE_VIDEO:
                return 8;
            case AppTypeInfo.PG_APP_TYPE_SCRLOCK:
            case AppTypeInfo.PG_APP_TYPE_LOCATION_PROVIDER:
            default:
                return appType;
            case AppTypeInfo.PG_APP_TYPE_ALARM:
                return 5;
            case AppTypeInfo.PG_APP_TYPE_IM:
                return 0;
            case AppTypeInfo.PG_APP_TYPE_MUSIC:
                return 7;
            case AppTypeInfo.PG_APP_TYPE_NAVIGATION:
                return 3;
            case AppTypeInfo.PG_APP_TYPE_OFFICE:
                return 12;
            case AppTypeInfo.PG_APP_TYPE_GALLERY:
                return 29;
            case AppTypeInfo.PG_APP_TYPE_SIP:
                return 30;
            case AppTypeInfo.PG_APP_TYPE_NEWS_CLIENT:
                return 26;
            case AppTypeInfo.PG_APP_TYPE_SHOP:
                return 14;
            case AppTypeInfo.PG_APP_TYPE_APP_MARKET:
                return 31;
            case AppTypeInfo.PG_APP_TYPE_LIFE_TOOL:
                return 32;
            case AppTypeInfo.PG_APP_TYPE_EDUCATION:
                return 33;
            case AppTypeInfo.PG_APP_TYPE_MONEY:
                return 34;
            case AppTypeInfo.PG_APP_TYPE_CAMERA:
                return 17;
            case AppTypeInfo.PG_APP_TYPE_PEDOMETER:
                return 2;
        }
    }

    public boolean isReady() {
        return this.mIsReady;
    }
}
