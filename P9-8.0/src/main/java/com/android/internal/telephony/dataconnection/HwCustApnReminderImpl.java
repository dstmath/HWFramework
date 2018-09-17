package com.android.internal.telephony.dataconnection;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl;

public class HwCustApnReminderImpl extends HwCustApnReminder {
    private static final String ACTION_DUAL_SIM_IMSI_CHANGE = "android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE";
    private static final String ACTION_RERRESHAPN = "android.intent.action.refreshapn";
    private static final int EVENT_RESET_APN_FAIL_MENTION = 1;
    private static final boolean HWDBG = true;
    private static final boolean IS_DELETE_PREFERAPN = SystemProperties.getBoolean("ro.config.delete.preferapn", false);
    private static final boolean IS_DISABLE_AP = SystemProperties.getBoolean("ro.config.dualimsi.disableap", false);
    private static final int MENTION_INTERVAL_MILLIS = 15000;
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final String SUBSCRIPTION_KEY = "subscription";
    private static final String TAG = "HwCustApnReminderImpl";
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static boolean mAllowApnMention = HWDBG;
    private Context mContext = null;
    private ApnRemindHandler mHandler = null;
    private HandlerThread mHandlerThread = null;

    private class ApnRemindHandler extends Handler {
        public ApnRemindHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            HwCustApnReminderImpl.this.log("[apn_all_fail] handleMessage msg = " + msg);
            switch (msg.what) {
                case 1:
                    HwCustApnReminderImpl.mAllowApnMention = HwCustApnReminderImpl.HWDBG;
                    HwCustApnReminderImpl.this.getMyHandlerThread().quitSafely();
                    HwCustApnReminderImpl.this.mHandlerThread = null;
                    HwCustApnReminderImpl.this.mHandler = null;
                    return;
                default:
                    return;
            }
        }
    }

    private class goSetApnListener implements OnClickListener {
        /* synthetic */ goSetApnListener(HwCustApnReminderImpl this$0, goSetApnListener -this1) {
            this();
        }

        private goSetApnListener() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.Settings$ApnSettingsActivity");
            int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
            intent.putExtra(HwCustApnReminderImpl.SUBSCRIPTION_KEY, defaultDataSubId);
            HwCustApnReminderImpl.this.log("[apn_all_fail][apnTracker] jump to ApnSettings, subId is " + defaultDataSubId);
            intent.addFlags(268435456);
            HwCustApnReminderImpl.this.mContext.startActivity(intent);
            dialog.dismiss();
        }
    }

    public void dataRoamingSwitchForCust(String displayName, Context mContext, int mSlotId, boolean isMultiSimEnabled) {
        if (isMultiSimEnabled) {
            log("EE Data roaming is on for double card" + mSlotId);
            if (mSlotId == 0) {
                custRoamingSwitchForCust(mContext, displayName);
            }
            if (mSlotId == 1 && "1 for EE".equals(displayName)) {
                Global.putInt(mContext.getContentResolver(), HwCustHwSIMRecordsImpl.DATA_ROAMING_SIM2, 1);
                return;
            }
            return;
        }
        log("EE Data roaming is on for single card" + mSlotId);
        custRoamingSwitchForCust(mContext, displayName);
    }

    public void custRoamingSwitchForCust(Context mContext, String displayName) {
        if ("1 for EE".equals(displayName)) {
            Global.putInt(mContext.getContentResolver(), "data_roaming", 1);
        }
    }

    public void handleAllApnPermActiveFailed(Context mContext) {
        this.mContext = mContext;
        String mAllApnFailedMention = Systemex.getString(mContext.getContentResolver(), "hw_apn_all_failed_mention");
        if (mAllowApnMention && "true".equals(mAllApnFailedMention)) {
            mentionApnFailed();
        }
    }

    private void mentionApnFailed() {
        mAllowApnMention = false;
        log("[apn_all_fail]mentionApnFailed all failed: start activity jump");
        int themeID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        Builder builder = new Builder(new ContextThemeWrapper(this.mContext, themeID), themeID);
        builder.setTitle(33685990).setCancelable(HWDBG).setMessage(33685989).setPositiveButton(33685987, new goSetApnListener(this, null)).setNegativeButton(33685988, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                HwCustApnReminderImpl.this.getHandler().sendMessageDelayed(HwCustApnReminderImpl.this.getHandler().obtainMessage(1), 15000);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(2003);
        alertDialog.show();
    }

    private ApnRemindHandler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new ApnRemindHandler(getMyHandlerThread().getLooper());
        }
        return this.mHandler;
    }

    private HandlerThread getMyHandlerThread() {
        if (this.mHandlerThread == null) {
            this.mHandlerThread = new HandlerThread("ApnRemindHandlerThread");
            this.mHandlerThread.start();
        }
        return this.mHandlerThread;
    }

    private void log(String message) {
        Rlog.d(TAG, message);
    }

    public void setSimRefreshingState(boolean bSimRefreshing) {
        Log.d(TAG, "SIM card is refreshing.");
    }

    public void notifyDisableAp(String oldImsi) {
        if (IS_DISABLE_AP && oldImsi != null) {
            Log.d(TAG, "Cust config is true and SIM is refreshing and imsi is changed, broadcast dual SIM IMSI changed.");
            ActivityManagerNative.broadcastStickyIntent(new Intent(ACTION_DUAL_SIM_IMSI_CHANGE), null, 0);
        }
    }

    public void showNewAddAPN(Context mContext, Builder builder) {
        if (mContext != null && builder != null) {
            this.mContext = mContext;
            builder.setPositiveButton(33685991, new goSetApnListener(this, null));
        }
    }

    public void deletePreferApn(Context context, String imsi, int slotId) {
        if (IS_DELETE_PREFERAPN && context != null && imsi != null) {
            log("imsi is changed, delete the prefer apn.");
            ContentResolver resolver = context.getContentResolver();
            if (isMultiSimEnabled) {
                resolver.delete(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId), null, null);
            } else {
                resolver.delete(PREFERAPN_NO_UPDATE_URI, null, null);
            }
            context.sendBroadcast(new Intent(ACTION_RERRESHAPN));
        }
    }
}
