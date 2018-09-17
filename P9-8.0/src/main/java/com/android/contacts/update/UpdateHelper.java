package com.android.contacts.update;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings.System;
import android.util.SparseArray;
import com.android.contacts.compatibility.NumberLocationCache;
import com.android.contacts.external.separated.ISeparatedResourceUtils;
import com.android.contacts.external.separated.SeparatedResourceUtils;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.numbermark.YellowPageDataManager;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.huawei.numberlocation.NLContentProvider;

public class UpdateHelper {
    private static final String TAG = "DownloadService";
    private static boolean isInit = false;
    private static final SparseArray<Updater> mUpdateMap = new SparseArray();

    public static class CCUpdater extends Updater {
        private ISeparatedResourceUtils mISeparatedResourceUtils = new SeparatedResourceUtils();

        public CCUpdater(Context context) {
            super(context, 3);
        }

        public String getTitle() {
            ISeparatedResourceUtils iSeparatedResourceUtils = this.mISeparatedResourceUtils;
            Context context = this.mContext;
            ISeparatedResourceUtils iSeparatedResourceUtils2 = this.mISeparatedResourceUtils;
            return iSeparatedResourceUtils.getString(context, 7);
        }

        public String getSucessNoti() {
            ISeparatedResourceUtils iSeparatedResourceUtils = this.mISeparatedResourceUtils;
            Context context = this.mContext;
            ISeparatedResourceUtils iSeparatedResourceUtils2 = this.mISeparatedResourceUtils;
            return iSeparatedResourceUtils.getString(context, 8);
        }
    }

    public static class NLUpdater extends Updater {
        private ISeparatedResourceUtils mISeparatedResourceUtils = new SeparatedResourceUtils();

        public NLUpdater(Context context) {
            super(context, 1);
        }

        public void setItem(int item) {
            super.setItem(item);
            notifyChange();
        }

        public void handleComplete(DownloadResponse response) {
            super.handleComplete(response);
            NumberLocationCache.clearLocation();
            NumberLocationCache.clear();
            UpdateHelper.refreshCallLog(this.mContext);
            this.mContext.sendBroadcast(new Intent("com.huawei.intent.action.UPDATE_NUMBER_LOCATION_ALL"), "com.huawei.contacts.permission.HW_CONTACTS_ALL");
            notifyChange();
        }

        public String getTitle() {
            ISeparatedResourceUtils iSeparatedResourceUtils = this.mISeparatedResourceUtils;
            Context context = this.mContext;
            ISeparatedResourceUtils iSeparatedResourceUtils2 = this.mISeparatedResourceUtils;
            return iSeparatedResourceUtils.getString(context, 5);
        }

        public String getSucessNoti() {
            ISeparatedResourceUtils iSeparatedResourceUtils = this.mISeparatedResourceUtils;
            Context context = this.mContext;
            ISeparatedResourceUtils iSeparatedResourceUtils2 = this.mISeparatedResourceUtils;
            return iSeparatedResourceUtils.getString(context, 6);
        }

        private void notifyChange() {
            this.mContext.getContentResolver().notifyChange(NLContentProvider.CONTENT_URI_FVERSION, null, false);
        }
    }

    public static class YPUpdater extends Updater {
        private static final String KEY_AUTO_YP = "auto_item_4";
        private ISeparatedResourceUtils mISeparatedResourceUtils = new SeparatedResourceUtils();

        public YPUpdater(Context context) {
            super(context, 4);
        }

        public void handleComplete(DownloadResponse response) {
            super.handleComplete(response);
            new YellowPageDataManager(this.mContext).prepareYellowPageDataAsync(true);
            UpdateHelper.refreshCallLog(this.mContext);
        }

        public String getTitle() {
            ISeparatedResourceUtils iSeparatedResourceUtils = this.mISeparatedResourceUtils;
            Context context = this.mContext;
            ISeparatedResourceUtils iSeparatedResourceUtils2 = this.mISeparatedResourceUtils;
            return iSeparatedResourceUtils.getString(context, 9);
        }

        public String getSucessNoti() {
            ISeparatedResourceUtils iSeparatedResourceUtils = this.mISeparatedResourceUtils;
            Context context = this.mContext;
            ISeparatedResourceUtils iSeparatedResourceUtils2 = this.mISeparatedResourceUtils;
            return iSeparatedResourceUtils.getString(context, 10);
        }

        public int getItem() {
            return SharePreferenceUtil.getDefaultSp_de(this.mContext).getInt(KEY_AUTO_YP, System.getInt(this.mContext.getContentResolver(), "hw_update_yellowpage_option", 2));
        }

        public void setItem(int item, boolean isImmediate) {
            System.putString(this.mContext.getContentResolver(), "hw_numbermark_notification_option", "false");
            System.putInt(this.mContext.getContentResolver(), "hw_update_yellowpage_option", item);
            super.setItem(item, isImmediate);
        }
    }

    public static synchronized void init(Context context) {
        synchronized (UpdateHelper.class) {
            if (context == null) {
                HwLog.i(TAG, "update helper init null");
            } else {
                initUpdateMap(context);
                sheduleAlarm(context);
            }
        }
    }

    public static synchronized IUpdate getUpdaterInstance(int fileId, Context context) {
        IUpdate updater;
        synchronized (UpdateHelper.class) {
            updater = (IUpdate) mUpdateMap.get(fileId);
            if (updater == null) {
                updater = getUpdaterAndInit(context, fileId);
            }
        }
        return updater;
    }

    private static synchronized IUpdate getUpdaterAndInit(Context context, int fileId) {
        IUpdate iUpdate;
        synchronized (UpdateHelper.class) {
            if (context == null) {
                HwLog.i(TAG, "update helper getUpdaterAndInit null");
                iUpdate = null;
            } else {
                initUpdateMap(context);
                iUpdate = (IUpdate) mUpdateMap.get(fileId);
            }
        }
        return iUpdate;
    }

    private static synchronized void initUpdateMap(Context context) {
        synchronized (UpdateHelper.class) {
            if (!isInit) {
                context = context.getApplicationContext();
                mUpdateMap.put(1, new NLUpdater(context));
                mUpdateMap.put(3, new CCUpdater(context));
                mUpdateMap.put(4, new YPUpdater(context));
                isInit = true;
                if (HwLog.HWDBG) {
                    HwLog.d(TAG, "update helper init finish");
                }
            }
        }
    }

    private static synchronized void sheduleAlarm(Context context) {
        synchronized (UpdateHelper.class) {
            boolean isCamcardInstall = EmuiFeatureManager.isCamCardApkInstalled(context);
            HwLog.i(TAG, "sheduleAlarm CamCardApkInstalled : " + isCamcardInstall);
            int size = mUpdateMap.size();
            for (int i = 0; i < size; i++) {
                Updater updater = (Updater) mUpdateMap.valueAt(i);
                if (isCamcardInstall || !(updater instanceof CCUpdater)) {
                    updater.scheduleAutoUpdate();
                } else {
                    HwLog.i(TAG, "sheduleAlarm skip CCUpdater");
                }
            }
        }
    }

    private static void refreshCallLog(Context context) {
        SharePreferenceUtil.getDefaultSp_de(context).edit().putBoolean("reference_is_refresh_calllog", true).commit();
        context.sendBroadcast(new Intent("com.android.contacts.action.UPDATE_YP"), "com.huawei.contacts.permission.HW_CONTACTS_ALL");
    }
}
