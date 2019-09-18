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
                ArrayMap arrayMap = new ArrayMap();
                loadAppType(resolver, arrayMap);
                if (!arrayMap.isEmpty()) {
                    synchronized (this.mAppsTypeMap) {
                        this.mAppsTypeMap.putAll(arrayMap);
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
            if (-1 == getAppType(info.getPkgName()) || info.getType() != 255) {
                addAppType(info.getPkgName(), info.getType(), info.getAttribute());
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0041, code lost:
        if (r0 != null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005c, code lost:
        if (r0 == null) goto L_0x005f;
     */
    private void loadAllIM(ContentResolver resolver, List<String> imList) {
        if (resolver != null && imList != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(AwareConstant.Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName"}, " HabitProtectList.deleted = 0 and HabitProtectList.appType = ? and HabitProtectList.userID = 0", new String[]{String.valueOf(0)}, "CAST(HabitProtectList.avgUsedFrequency AS REAL) desc");
                if (cursor == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    if (pkgName != null) {
                        if (!pkgName.isEmpty()) {
                            imList.add(pkgName);
                        }
                    }
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error SQLiteException: loadAllIM");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error IllegalStateException: loadAllIM");
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

    public boolean isTopIM(String pkgName, int count) {
        if (pkgName == null || count <= 0) {
            return false;
        }
        return getTopIMList(count).contains(pkgName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005a, code lost:
        if (r0 != null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0075, code lost:
        if (r0 == null) goto L_0x0078;
     */
    private void loadAppType(ContentResolver resolver, Map<String, AppTypeCacheInfo> typeMap) {
        if (resolver != null && typeMap != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(AwareConstant.Database.APPTYPE_URI, new String[]{"appPkgName", "typeAttri", APP_TYPE, "source"}, null, null, null);
                if (cursor == null) {
                    AwareLog.e(TAG, "loadAppType cursor is null.");
                    if (cursor != null) {
                        cursor.close();
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
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppType SQLiteException");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error: loadAppType IllegalStateException");
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

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0073, code lost:
        if (r10 != null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x009a, code lost:
        if (r10 != null) goto L_0x0075;
     */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008f A[Catch:{ SQLiteException -> 0x0090, IllegalStateException -> 0x0083, all -> 0x007f, all -> 0x0079 }] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00a0  */
    private void loadUsedApp(ContentResolver resolver, List<String> appUsedInfos, List<String> imList, int userId, int dayNum) {
        List<String> list = appUsedInfos;
        List<String> list2 = imList;
        if (resolver == null || list == null || list2 == null || imList.size() == 0) {
            int i = dayNum;
            return;
        }
        Cursor cursor = null;
        try {
            long now = System.currentTimeMillis();
            try {
                cursor = resolver.query(AwareConstant.AppUsageDatabase.APPUSAGE_URI, new String[]{APP_PKGNAME}, "foregroungTime > ? and foregroungTime < ? and userID = ?", new String[]{String.valueOf(now - (((long) dayNum) * 86400000)), String.valueOf(now), String.valueOf(userId)}, null);
                if (cursor == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    if (pkgName != null && !pkgName.isEmpty()) {
                        if (list2.contains(pkgName)) {
                            list.add(pkgName);
                        }
                    }
                }
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
            } catch (IllegalStateException e2) {
                AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (SQLiteException e3) {
            int i2 = dayNum;
            AwareLog.e(TAG, "Error SQLiteException: loadUsedApp");
        } catch (IllegalStateException e4) {
            int i3 = dayNum;
            AwareLog.e(TAG, "Error IllegalStateException: loadUsedApp");
            if (cursor != null) {
            }
        } catch (Throwable th) {
            th = th;
            if (cursor != null) {
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0074, code lost:
        if (r10 != null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x009b, code lost:
        if (r10 != null) goto L_0x0076;
     */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0090 A[Catch:{ SQLiteException -> 0x0091, IllegalStateException -> 0x0084, all -> 0x0080, all -> 0x007a }] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00a1  */
    public void loadAppUsedInfo(Context cxt, Map<String, Long> appUsedMap, int userId, int dayNum) {
        Map<String, Long> map = appUsedMap;
        if (cxt == null || map == null) {
            int i = dayNum;
            return;
        }
        ContentResolver resolver = cxt.getContentResolver();
        if (resolver != null) {
            Cursor cursor = null;
            try {
                long now = System.currentTimeMillis();
                try {
                    cursor = resolver.query(AwareConstant.AppUsageDatabase.APPUSAGE_URI, new String[]{APP_PKGNAME, "foregroungTime"}, "foregroungTime > ? and foregroungTime < ? and userID = ? ", new String[]{String.valueOf(now - (((long) dayNum) * 86400000)), String.valueOf(now), String.valueOf(userId)}, null);
                    if (cursor == null) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return;
                    }
                    while (cursor.moveToNext()) {
                        String pkgName = cursor.getString(0);
                        if (pkgName != null) {
                            if (!pkgName.isEmpty()) {
                                map.put(pkgName, Long.valueOf(cursor.getLong(1)));
                            }
                        }
                    }
                } catch (SQLiteException e) {
                    AwareLog.e(TAG, "Error SQLiteException: loadAppUsedInfo");
                } catch (IllegalStateException e2) {
                    AwareLog.e(TAG, "Error IllegalStateException: loadAppUsedInfo");
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (SQLiteException e3) {
                int i2 = dayNum;
                AwareLog.e(TAG, "Error SQLiteException: loadAppUsedInfo");
            } catch (IllegalStateException e4) {
                int i3 = dayNum;
                AwareLog.e(TAG, "Error IllegalStateException: loadAppUsedInfo");
                if (cursor != null) {
                }
            } catch (Throwable th) {
                th = th;
                if (cursor != null) {
                }
                throw th;
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

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002a, code lost:
        return r2;
     */
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
            int i = (appAttr & AppTypeInfo.APP_ATTRIBUTE_OVERSEA) == Integer.MIN_VALUE ? 1 : 0;
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
        ArrayMap arrayMap;
        synchronized (this.mAppsTypeMap) {
            arrayMap = new ArrayMap(this.mAppsTypeMap.size());
            for (Map.Entry<String, AppTypeCacheInfo> entry : this.mAppsTypeMap.entrySet()) {
                arrayMap.put(entry.getKey(), Integer.valueOf(entry.getValue().getType()));
            }
        }
        ArraySet<String> appSet = new ArraySet<>();
        int size = arrayMap.size();
        for (int i = 0; i < size; i++) {
            if (((Integer) arrayMap.valueAt(i)).intValue() == appType) {
                appSet.add((String) arrayMap.keyAt(i));
            }
        }
        return appSet;
    }

    public Set<String> getAlarmApps() {
        ArrayMap arrayMap;
        synchronized (this.mAppsTypeMap) {
            arrayMap = new ArrayMap(this.mAppsTypeMap.size());
            for (Map.Entry<String, AppTypeCacheInfo> entry : this.mAppsTypeMap.entrySet()) {
                arrayMap.put(entry.getKey(), Integer.valueOf(entry.getValue().getType()));
            }
        }
        ArraySet<String> appSet = new ArraySet<>();
        int size = arrayMap.size();
        for (int i = 0; i < size; i++) {
            if (((Integer) arrayMap.valueAt(i)).intValue() == 5 || ((Integer) arrayMap.valueAt(i)).intValue() == 310) {
                appSet.add((String) arrayMap.keyAt(i));
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
        int type = appType;
        switch (appType) {
            case AppTypeInfo.PG_APP_TYPE_LAUNCHER:
                type = 28;
                break;
            case AppTypeInfo.PG_APP_TYPE_SMS:
                type = 27;
                break;
            case AppTypeInfo.PG_APP_TYPE_EMAIL:
                type = 1;
                break;
            case AppTypeInfo.PG_APP_TYPE_INPUTMETHOD:
                type = 19;
                break;
            case AppTypeInfo.PG_APP_TYPE_GAME:
                type = 9;
                break;
            case AppTypeInfo.PG_APP_TYPE_BROWSER:
                type = 18;
                break;
            case AppTypeInfo.PG_APP_TYPE_EBOOK:
                type = 6;
                break;
            case AppTypeInfo.PG_APP_TYPE_VIDEO:
                type = 8;
                break;
            case AppTypeInfo.PG_APP_TYPE_ALARM:
                type = 5;
                break;
            case AppTypeInfo.PG_APP_TYPE_IM:
                type = 0;
                break;
            case AppTypeInfo.PG_APP_TYPE_MUSIC:
                type = 7;
                break;
            case AppTypeInfo.PG_APP_TYPE_NAVIGATION:
                type = 3;
                break;
            case AppTypeInfo.PG_APP_TYPE_OFFICE:
                type = 12;
                break;
            case AppTypeInfo.PG_APP_TYPE_GALLERY:
                type = 29;
                break;
            case AppTypeInfo.PG_APP_TYPE_SIP:
                type = 30;
                break;
            case AppTypeInfo.PG_APP_TYPE_NEWS_CLIENT:
                type = 26;
                break;
            case AppTypeInfo.PG_APP_TYPE_SHOP:
                type = 14;
                break;
            case AppTypeInfo.PG_APP_TYPE_APP_MARKET:
                type = 31;
                break;
            case AppTypeInfo.PG_APP_TYPE_LIFE_TOOL:
                type = 32;
                break;
            case AppTypeInfo.PG_APP_TYPE_EDUCATION:
                type = 33;
                break;
            case AppTypeInfo.PG_APP_TYPE_MONEY:
                type = 34;
                break;
            case AppTypeInfo.PG_APP_TYPE_CAMERA:
                type = 17;
                break;
            case AppTypeInfo.PG_APP_TYPE_PEDOMETER:
                type = 2;
                break;
        }
        return type;
    }
}
