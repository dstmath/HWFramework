package com.android.server.hidata.arbitration;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.hidata.appqoe.HwAPPQoEAPKConfig;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwArbitrationUXWrapper {
    private static Uri APP_SETTING_URI = Uri.parse("content://com.huawei.systemserver.networkengine.SmartNetworkProvider/apps_settings");
    private static final int HW_HIDATA = 1;
    private static final int INVALID_VALUE = -2;
    private static final int MSG_SMARTNW_APP_SETTING_CHANGED = 4002;
    private static final int MSG_SMARTNW_TOTAL_SETTING_CHANGED = 4001;
    private static final String NAME = "name";
    private static final String PATH_SETTINGS = "/apps_settings";
    private static final String SCHEME = "content://";
    private static final String SETTINGS_AUTHORITY = "com.huawei.systemserver.networkengine.SmartNetworkProvider";
    private static int SMARTNW_SWITCH_TO_WLAN = 1;
    private static final int SMART_NETWORK_DISABLED = 0;
    private static final int SMART_NETWORK_ENABLED = 1;
    private static final int SMART_NETWORK_ENABLED_BY_USER = 3;
    private static final int SMART_NETWORK_NOT_SETTING = -1;
    private static final String TAG = "HwArbitrationUXWrapper";
    private static final String TOTAL_SETTINGS = "/total_settings";
    private static Uri TOTAL_SETTING_URI = Uri.parse("content://com.huawei.systemserver.networkengine.SmartNetworkProvider/total_settings");
    private static final int TRANSACTION_CONFIG_COMPLETE = 11;
    private static final int TRANSACTION_STATE_CHANGE = 12;
    private static final String UPDATE_ALL = "update_all";
    private static final String VALUE = "value";
    private static HwArbitrationUXWrapper mHwArbitrationUxWrapper;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HwAPPQoEResourceManger mHwAPPQoEResourceManger;
    private Set<String> mMplinkSupportLists = new HashSet();

    public static HwArbitrationUXWrapper createInstance(Context context, Handler handler) {
        if (mHwArbitrationUxWrapper == null) {
            mHwArbitrationUxWrapper = new HwArbitrationUXWrapper(context, handler);
        }
        return mHwArbitrationUxWrapper;
    }

    public static HwArbitrationUXWrapper getInstance() {
        return mHwArbitrationUxWrapper;
    }

    private HwArbitrationUXWrapper(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
        registerSmartNWSettingsChanges();
    }

    public static void configUpdateComplete() {
        logD(TAG, "ConfigUpdateComplete");
        IBinder hidataAssist = ServiceManager.getService("HiMPEngineService");
        if (hidataAssist != null) {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            try {
                _data.writeInterfaceToken("com.huawei.systemserver.networkengine.IHiMPEngineManager");
                _data.writeInt(1);
                hidataAssist.transact(11, _data, _reply, 1);
                _reply.readException();
            } catch (RemoteException e) {
                Slog.e(TAG, "configUpdateComplete transact error");
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
            _reply.recycle();
            _data.recycle();
        }
    }

    public void stateChange(int uid, int state) {
        logD(TAG, "enter stateChange");
        String packageName = getPackageName(this.mContext, uid);
        if (TextUtils.isEmpty(packageName)) {
            logE(TAG, "stateChange packageName is null or length is 0");
            return;
        }
        IBinder hidataAssist = ServiceManager.getService("HiMPEngineService");
        logD(TAG, "uid: " + uid + ", packageName:" + packageName + ", state:" + state);
        if (hidataAssist != null) {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            try {
                _data.writeInterfaceToken("com.huawei.systemserver.networkengine.IHiMPEngineManager");
                _data.writeInt(1);
                _data.writeString(packageName);
                _data.writeInt(state);
                hidataAssist.transact(12, _data, _reply, 1);
                _reply.readException();
            } catch (RemoteException e) {
                Slog.e(TAG, "stateChange transact error");
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
            _reply.recycle();
            _data.recycle();
        }
    }

    public List<String> getSupportList() {
        logD(TAG, "enter getSupportList");
        if (this.mHwAPPQoEResourceManger == null) {
            return new ArrayList();
        }
        this.mMplinkSupportLists.clear();
        List<HwAPPQoEAPKConfig> mAPPConfigList = this.mHwAPPQoEResourceManger.getAPKConfigList();
        if (mAPPConfigList != null && !mAPPConfigList.isEmpty()) {
            for (HwAPPQoEAPKConfig config : mAPPConfigList) {
                this.mMplinkSupportLists.add(config.packageName);
            }
        }
        return new ArrayList(this.mMplinkSupportLists);
    }

    public void notifyUIEvent(int event) {
        logD(TAG, "enter notify UIEvent");
        if (SMARTNW_SWITCH_TO_WLAN == event) {
            this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_Stop_MPLink_By_Notification);
            CollectUserFingersHandler collectUserFingersHandler = CollectUserFingersHandler.getInstance();
            if (collectUserFingersHandler != null) {
                collectUserFingersHandler.setCHRQoEPrefManualSwitch();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0085, code lost:
        if (r10 != null) goto L_0x0087;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0087, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00ca, code lost:
        if (r10 == null) goto L_0x00cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00ce, code lost:
        if (1 != r2) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00d0, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00d2, code lost:
        return r0;
     */
    public static boolean isAppUserEnabled(Context context, int uid) {
        boolean z = false;
        if (context == null) {
            logE(TAG, "context is null");
            return false;
        }
        if (TextUtils.isEmpty(getPackageName(context, uid))) {
            logE(TAG, "isAppUserEnabled packageName is null or length is 0");
            return false;
        } else if (!isSmartNetworkEnabled(context)) {
            return false;
        } else {
            int value = -2;
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(APP_SETTING_URI, new String[]{"value"}, "name='" + packageName + "'", null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    value = cursor.getInt(0);
                }
                logD(TAG, "isAppUserEnabled:" + uid + ", packageName:" + packageName + ", switch value:" + value);
            } catch (Exception e) {
                logE(TAG, "get package cursor exception" + e);
                logD(TAG, "isAppUserEnabled:" + uid + ", packageName:" + packageName + ", switch value:" + -2);
            } catch (Throwable th) {
                logD(TAG, "isAppUserEnabled:" + uid + ", packageName:" + packageName + ", switch value:" + -2);
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002f, code lost:
        if (r2 != null) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0031, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004e, code lost:
        if (r2 == null) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0051, code lost:
        logD(TAG, "is SmartNetworkEnabled total_switch:" + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0068, code lost:
        if (1 == r1) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006b, code lost:
        if (3 != r1) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006f, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0070, code lost:
        return r0;
     */
    public static boolean isSmartNetworkEnabled(Context context) {
        boolean z = false;
        if (context == null) {
            logE(TAG, "isSmartNetworkEnabled context is null");
            return false;
        }
        int value = -2;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(TOTAL_SETTING_URI, new String[]{"total_switch"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                value = cursor.getInt(0);
            }
        } catch (Exception e) {
            logD(TAG, "get total switch failed" + e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private void registerSmartNWSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(APP_SETTING_URI, true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange, Uri uri) {
                String appName = uri.getLastPathSegment();
                if (HwArbitrationUXWrapper.UPDATE_ALL.equals(appName)) {
                    HwArbitrationUXWrapper.logD(HwArbitrationUXWrapper.TAG, "smart network: update all ");
                    return;
                }
                int uid = HwArbitrationUXWrapper.getAppUid(HwArbitrationUXWrapper.this.mContext, appName);
                if (-1 != uid && !HwArbitrationUXWrapper.isAppUserEnabled(HwArbitrationUXWrapper.this.mContext, uid)) {
                    HwArbitrationUXWrapper.this.mHandler.sendMessage(HwArbitrationUXWrapper.this.mHandler.obtainMessage(4002, uid, 0));
                    HwArbitrationUXWrapper.logD(HwArbitrationUXWrapper.TAG, "MSG_SMARTNW_APP_SETTING_CHANGED, uid:" + uid);
                }
            }
        });
        this.mContext.getContentResolver().registerContentObserver(TOTAL_SETTING_URI, true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                if (!HwArbitrationUXWrapper.isSmartNetworkEnabled(HwArbitrationUXWrapper.this.mContext)) {
                    HwArbitrationUXWrapper.this.mHandler.sendMessage(HwArbitrationUXWrapper.this.mHandler.obtainMessage(4001));
                    HwArbitrationUXWrapper.logD(HwArbitrationUXWrapper.TAG, "MSG_SMARTNW_TOTAL_SETTING_CHANGED");
                }
            }
        });
    }

    public static String getPackageName(Context context, int uid) {
        if (uid == -1 || context == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        String name = pm.getNameForUid(uid);
        if (TextUtils.isEmpty(name)) {
            name = null;
        }
        return name;
    }

    public static int getAppUid(Context context, String processName) {
        int uid = -1;
        if (TextUtils.isEmpty(processName) || context == null) {
            return -1;
        }
        PackageManager pm = context.getPackageManager();
        if (pm != null) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(processName, 1);
                if (ai != null) {
                    uid = ai.uid;
                }
            } catch (PackageManager.NameNotFoundException e) {
                logD(TAG, "NameNotFoundException: " + e.getMessage());
            }
        }
        return uid;
    }

    public static void logD(String tag, String log) {
        Log.d(tag, log);
    }

    public static void logI(String tag, String log) {
        Log.i(tag, log);
    }

    public static void logE(String tag, String log) {
        Log.e(tag, log);
    }
}
