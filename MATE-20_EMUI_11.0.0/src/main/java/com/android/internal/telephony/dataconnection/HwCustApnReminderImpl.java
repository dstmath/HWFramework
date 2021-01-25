package com.android.internal.telephony.dataconnection;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl;
import huawei.cust.HwCfgFilePolicy;

public class HwCustApnReminderImpl extends HwCustApnReminder {
    private static final String ACTION_DUAL_SIM_IMSI_CHANGE = "android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE";
    private static final String ACTION_RERRESHAPN = "android.intent.action.refreshapn";
    private static final int EVENT_RESET_APN_FAIL_MENTION = 1;
    private static final boolean HWDBG = true;
    private static final boolean IS_DELETE_PREFERAPN = SystemProperties.getBoolean("ro.config.delete.preferapn", false);
    private static boolean IS_DISABLE_AP = false;
    private static final int MENTION_INTERVAL_MILLIS = 15000;
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final String SUBSCRIPTION_KEY = "subscription";
    private static final String TAG = "HwCustApnReminderImpl";
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static boolean mAllowApnMention = HWDBG;
    private Context mContext = null;
    private ApnRemindHandler mHandler = null;
    private HandlerThread mHandlerThread = null;

    public void dataRoamingSwitchForCust(String displayName, Context mContext2, int mSlotId, boolean isMultiSimEnabled2) {
        if (isMultiSimEnabled2) {
            log("EE Data roaming is on for double card" + mSlotId);
            if (mSlotId == 0) {
                custRoamingSwitchForCust(mContext2, displayName);
            }
            if (mSlotId == 1 && "1 for EE".equals(displayName)) {
                Settings.Global.putInt(mContext2.getContentResolver(), HwCustHwSIMRecordsImpl.DATA_ROAMING_SIM2, 1);
                return;
            }
            return;
        }
        log("EE Data roaming is on for single card" + mSlotId);
        custRoamingSwitchForCust(mContext2, displayName);
    }

    public void custRoamingSwitchForCust(Context mContext2, String displayName) {
        if ("1 for EE".equals(displayName)) {
            Settings.Global.putInt(mContext2.getContentResolver(), "data_roaming", 1);
        }
    }

    public void handleAllApnPermActiveFailed(Context mContext2) {
        this.mContext = mContext2;
        String mAllApnFailedMention = SettingsEx.Systemex.getString(mContext2.getContentResolver(), "hw_apn_all_failed_mention");
        if (mAllowApnMention && "true".equals(mAllApnFailedMention)) {
            mentionApnFailed();
        }
    }

    /* access modifiers changed from: private */
    public class goSetApnListener implements DialogInterface.OnClickListener {
        private goSetApnListener() {
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int whichButton) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.Settings$ApnSettingsActivity");
            int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
            intent.putExtra(HwCustApnReminderImpl.SUBSCRIPTION_KEY, defaultDataSubId);
            HwCustApnReminderImpl hwCustApnReminderImpl = HwCustApnReminderImpl.this;
            hwCustApnReminderImpl.log("[apn_all_fail][apnTracker] jump to ApnSettings, subId is " + defaultDataSubId);
            intent.addFlags(268435456);
            HwCustApnReminderImpl.this.mContext.startActivity(intent);
            dialog.dismiss();
        }
    }

    private void mentionApnFailed() {
        mAllowApnMention = false;
        log("[apn_all_fail]mentionApnFailed all failed: start activity jump");
        int themeID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this.mContext, themeID), themeID);
        builder.setTitle(33685990).setCancelable(HWDBG).setMessage(33685989).setPositiveButton(33685987, new goSetApnListener()).setNegativeButton(33685988, new DialogInterface.OnClickListener() {
            /* class com.android.internal.telephony.dataconnection.HwCustApnReminderImpl.AnonymousClass2 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.android.internal.telephony.dataconnection.HwCustApnReminderImpl.AnonymousClass1 */

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialog) {
                HwCustApnReminderImpl.this.getHandler().sendMessageDelayed(HwCustApnReminderImpl.this.getHandler().obtainMessage(1), 15000);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(2003);
        alertDialog.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ApnRemindHandler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new ApnRemindHandler(getMyHandlerThread().getLooper());
        }
        return this.mHandler;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private HandlerThread getMyHandlerThread() {
        if (this.mHandlerThread == null) {
            this.mHandlerThread = new HandlerThread("ApnRemindHandlerThread");
            this.mHandlerThread.start();
        }
        return this.mHandlerThread;
    }

    /* access modifiers changed from: private */
    public class ApnRemindHandler extends Handler {
        public ApnRemindHandler(Looper mLooper) {
            super(mLooper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            HwCustApnReminderImpl hwCustApnReminderImpl = HwCustApnReminderImpl.this;
            hwCustApnReminderImpl.log("[apn_all_fail] handleMessage msg = " + msg);
            if (msg.what == 1) {
                boolean unused = HwCustApnReminderImpl.mAllowApnMention = HwCustApnReminderImpl.HWDBG;
                HwCustApnReminderImpl.this.getMyHandlerThread().quitSafely();
                HwCustApnReminderImpl.this.mHandlerThread = null;
                HwCustApnReminderImpl.this.mHandler = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String message) {
        Rlog.d(TAG, message);
    }

    public void setSimRefreshingState(boolean bSimRefreshing) {
        Log.d(TAG, "SIM card is refreshing.");
    }

    public void notifyDisableAp(Context context, String oldImsi) {
        if (context != null) {
            IS_DISABLE_AP = "true".equals(Settings.Global.getString(context.getContentResolver(), "dualimsi.disableap"));
            if (IS_DISABLE_AP && oldImsi != null) {
                Log.d(TAG, "Cust config is true and SIM is refreshing and imsi is changed, broadcast dual SIM IMSI changed.");
                ActivityManagerNative.broadcastStickyIntent(new Intent(ACTION_DUAL_SIM_IMSI_CHANGE), (String) null, 0);
            }
        }
    }

    public void showNewAddAPN(Context mContext2, AlertDialog.Builder builder) {
        if (mContext2 != null && builder != null) {
            this.mContext = mContext2;
            builder.setPositiveButton(33685991, new goSetApnListener());
        }
    }

    public void deletePreferApn(Context context, String imsi, int slotId) {
        int subId;
        boolean deletePreferApnState = false;
        boolean hasHwCfgConfig = false;
        try {
            Boolean deletePrefer = (Boolean) HwCfgFilePolicy.getValue("delete_preferapn_switch", slotId, Boolean.class);
            if (deletePrefer != null) {
                deletePreferApnState = deletePrefer.booleanValue();
                hasHwCfgConfig = HWDBG;
            }
            if (!hasHwCfgConfig || deletePreferApnState) {
                log("HwCfgFile:imsi is changed, delete the prefer apn.");
                if ((IS_DELETE_PREFERAPN || deletePreferApnState) && context != null && imsi != null) {
                    log("imsi is changed, delete the prefer apn.");
                    ContentResolver resolver = context.getContentResolver();
                    if (isMultiSimEnabled) {
                        SubscriptionController subscriptionController = SubscriptionController.getInstance();
                        if (subscriptionController != null && (subId = subscriptionController.getSubIdUsingPhoneId(slotId)) != -1) {
                            resolver.delete(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) subId), null, null);
                        } else {
                            return;
                        }
                    } else {
                        resolver.delete(PREFERAPN_NO_UPDATE_URI, null, null);
                    }
                    context.sendBroadcast(new Intent(ACTION_RERRESHAPN));
                }
            }
        } catch (Exception e) {
            log("Exception: read delete_preferapn_switch error in deletePreferApn");
        }
    }
}
