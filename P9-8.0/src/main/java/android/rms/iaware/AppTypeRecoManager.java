package android.rms.iaware;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.ICMSManager.Stub;
import android.rms.iaware.utils.AppTypeRecoUtils;
import android.util.ArrayMap;
import java.util.List;
import java.util.Map.Entry;
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
    private static final String TAG = "AppTypeRecoManager";
    private static AppTypeRecoManager mAppTypeRecoManager = null;
    private final ArrayMap<String, AppTypeCacheInfo> mAppsTypeMap = new ArrayMap();
    private boolean mIsReady = false;

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
        if (ctx == null || this.mIsReady) {
            AwareLog.i(TAG, "no need to init");
            return;
        }
        ContentResolver resolver = ctx.getContentResolver();
        ArrayMap<String, AppTypeCacheInfo> map = new ArrayMap();
        AppTypeRecoUtils.loadAppType(resolver, map);
        if (!map.isEmpty()) {
            this.mIsReady = true;
            synchronized (this.mAppsTypeMap) {
                this.mAppsTypeMap.putAll(map);
            }
            AwareLog.i(TAG, "init end.");
        }
    }

    public void deinit() {
        synchronized (this.mAppsTypeMap) {
            this.mAppsTypeMap.clear();
        }
        synchronized (this) {
            this.mIsReady = false;
        }
        AwareLog.i(TAG, "deinit.");
    }

    public boolean loadInstalledAppTypeInfo() {
        List<AppTypeInfo> list = null;
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                ParceledListSlice<AppTypeInfo> slice = awareservice.getAllAppTypeInfo();
                if (slice != null) {
                    list = slice.getList();
                }
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

    public int getAppType(String pkgName) {
        AppTypeCacheInfo info;
        synchronized (this.mAppsTypeMap) {
            info = (AppTypeCacheInfo) this.mAppsTypeMap.get(pkgName);
        }
        if (info == null || info.getType() == -2) {
            return -1;
        }
        return info.getType();
    }

    /* JADX WARNING: Missing block: B:18:0x0028, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getAppWhereFrom(String pkgName) {
        synchronized (this.mAppsTypeMap) {
            if (this.mAppsTypeMap.containsKey(pkgName)) {
                AppTypeCacheInfo appTypeInfo = (AppTypeCacheInfo) this.mAppsTypeMap.get(pkgName);
                if (appTypeInfo == null) {
                    return -1;
                }
                int appAttr = appTypeInfo.getAttribute();
                if (appAttr == -1) {
                    return -1;
                }
                int i = (appAttr & AppTypeInfo.APP_ATTRIBUTE_OVERSEA) == AppTypeInfo.APP_ATTRIBUTE_OVERSEA ? 1 : 0;
            } else {
                return -1;
            }
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

    /* JADX WARNING: Missing block: B:15:0x0042, code:
            r2 = new android.util.ArraySet();
            r6 = r1.size();
            r5 = 0;
     */
    /* JADX WARNING: Missing block: B:16:0x004c, code:
            if (r5 >= r6) goto L_0x0066;
     */
    /* JADX WARNING: Missing block: B:18:0x0058, code:
            if (((java.lang.Integer) r1.valueAt(r5)).intValue() != r11) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:19:0x005a, code:
            r2.add((java.lang.String) r1.keyAt(r5));
     */
    /* JADX WARNING: Missing block: B:20:0x0063, code:
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:21:0x0066, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Set<String> getAppsByType(int appType) {
        Throwable th;
        synchronized (this.mAppsTypeMap) {
            try {
                ArrayMap<String, Integer> appList = new ArrayMap(this.mAppsTypeMap.size());
                try {
                    for (Entry<String, AppTypeCacheInfo> entry : this.mAppsTypeMap.entrySet()) {
                        appList.put((String) entry.getKey(), Integer.valueOf(((AppTypeCacheInfo) entry.getValue()).getType()));
                    }
                } catch (Throwable th2) {
                    th = th2;
                    ArrayMap<String, Integer> arrayMap = appList;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0042, code:
            r2 = new android.util.ArraySet();
            r6 = r1.size();
            r5 = 0;
     */
    /* JADX WARNING: Missing block: B:16:0x004c, code:
            if (r5 >= r6) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:18:0x0059, code:
            if (((java.lang.Integer) r1.valueAt(r5)).intValue() == 5) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:20:0x0067, code:
            if (((java.lang.Integer) r1.valueAt(r5)).intValue() != android.rms.iaware.AppTypeInfo.PG_APP_TYPE_ALARM) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:21:0x0069, code:
            r2.add((java.lang.String) r1.keyAt(r5));
     */
    /* JADX WARNING: Missing block: B:22:0x0072, code:
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:23:0x0075, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Set<String> getAlarmApps() {
        Throwable th;
        synchronized (this.mAppsTypeMap) {
            try {
                ArrayMap<String, Integer> appList = new ArrayMap(this.mAppsTypeMap.size());
                try {
                    for (Entry<String, AppTypeCacheInfo> entry : this.mAppsTypeMap.entrySet()) {
                        appList.put((String) entry.getKey(), Integer.valueOf(((AppTypeCacheInfo) entry.getValue()).getType()));
                    }
                } catch (Throwable th2) {
                    th = th2;
                    ArrayMap<String, Integer> arrayMap = appList;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public void removeAppType(String pkgName) {
        synchronized (this.mAppsTypeMap) {
            this.mAppsTypeMap.remove(pkgName);
        }
    }

    public void addAppType(String pkgName, int type, int attr) {
        synchronized (this.mAppsTypeMap) {
            AppTypeCacheInfo cacheInfo = (AppTypeCacheInfo) this.mAppsTypeMap.get(pkgName);
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
            case AppTypeInfo.PG_APP_TYPE_LAUNCHER /*301*/:
                type = 28;
                break;
            case AppTypeInfo.PG_APP_TYPE_SMS /*302*/:
                type = 27;
                break;
            case AppTypeInfo.PG_APP_TYPE_EMAIL /*303*/:
                type = 1;
                break;
            case AppTypeInfo.PG_APP_TYPE_INPUTMETHOD /*304*/:
                type = 19;
                break;
            case AppTypeInfo.PG_APP_TYPE_GAME /*305*/:
                type = 9;
                break;
            case AppTypeInfo.PG_APP_TYPE_BROWSER /*306*/:
                type = 18;
                break;
            case AppTypeInfo.PG_APP_TYPE_EBOOK /*307*/:
                type = 6;
                break;
            case AppTypeInfo.PG_APP_TYPE_VIDEO /*308*/:
                type = 8;
                break;
            case AppTypeInfo.PG_APP_TYPE_ALARM /*310*/:
                type = 5;
                break;
            case AppTypeInfo.PG_APP_TYPE_IM /*311*/:
                type = 0;
                break;
            case AppTypeInfo.PG_APP_TYPE_MUSIC /*312*/:
                type = 7;
                break;
            case AppTypeInfo.PG_APP_TYPE_NAVIGATION /*313*/:
                type = 3;
                break;
            case AppTypeInfo.PG_APP_TYPE_OFFICE /*315*/:
                type = 12;
                break;
            case AppTypeInfo.PG_APP_TYPE_GALLERY /*316*/:
                type = 29;
                break;
            case AppTypeInfo.PG_APP_TYPE_SIP /*317*/:
                type = 30;
                break;
            case AppTypeInfo.PG_APP_TYPE_NEWS_CLIENT /*318*/:
                type = 26;
                break;
            case AppTypeInfo.PG_APP_TYPE_SHOP /*319*/:
                type = 14;
                break;
            case AppTypeInfo.PG_APP_TYPE_APP_MARKET /*320*/:
                type = 31;
                break;
            case AppTypeInfo.PG_APP_TYPE_LIFE_TOOL /*321*/:
                type = 32;
                break;
            case AppTypeInfo.PG_APP_TYPE_EDUCATION /*322*/:
                type = 33;
                break;
            case AppTypeInfo.PG_APP_TYPE_MONEY /*323*/:
                type = 34;
                break;
            case AppTypeInfo.PG_APP_TYPE_CAMERA /*324*/:
                type = 17;
                break;
            case AppTypeInfo.PG_APP_TYPE_PEDOMETER /*325*/:
                type = 2;
                break;
        }
        return type;
    }
}
