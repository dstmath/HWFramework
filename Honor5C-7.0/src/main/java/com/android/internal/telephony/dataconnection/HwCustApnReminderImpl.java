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
import android.provider.Settings.Global;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl;

public class HwCustApnReminderImpl extends HwCustApnReminder {
    private static final String ACTION_DUAL_SIM_IMSI_CHANGE = "android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE";
    private static final String ACTION_RERRESHAPN = "android.intent.action.refreshapn";
    private static final int EVENT_RESET_APN_FAIL_MENTION = 1;
    private static final boolean HWDBG = true;
    private static final boolean IS_DELETE_PREFERAPN = false;
    private static final boolean IS_DISABLE_AP = false;
    private static final int MENTION_INTERVAL_MILLIS = 15000;
    private static final Uri PREFERAPN_NO_UPDATE_URI = null;
    private static final String SUBSCRIPTION_KEY = "subscription";
    private static final String TAG = "HwCustApnReminderImpl";
    private static final boolean isMultiSimEnabled = false;
    private static boolean mAllowApnMention;
    private Context mContext;
    private ApnRemindHandler mHandler;
    private HandlerThread mHandlerThread;

    private class ApnRemindHandler extends Handler {
        public ApnRemindHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            HwCustApnReminderImpl.this.log("[apn_all_fail] handleMessage msg = " + msg);
            switch (msg.what) {
                case HwCustApnReminderImpl.EVENT_RESET_APN_FAIL_MENTION /*1*/:
                    HwCustApnReminderImpl.mAllowApnMention = HwCustApnReminderImpl.HWDBG;
                    HwCustApnReminderImpl.this.getMyHandlerThread().quitSafely();
                    HwCustApnReminderImpl.this.mHandlerThread = null;
                    HwCustApnReminderImpl.this.mHandler = null;
                default:
            }
        }
    }

    private class goSetApnListener implements OnClickListener {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.HwCustApnReminderImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.HwCustApnReminderImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.HwCustApnReminderImpl.<clinit>():void");
    }

    public HwCustApnReminderImpl() {
        this.mContext = null;
        this.mHandlerThread = null;
        this.mHandler = null;
    }

    public void dataRoamingSwitchForCust(String displayName, Context mContext, int mSlotId, boolean isMultiSimEnabled) {
        if (isMultiSimEnabled) {
            log("EE Data roaming is on for double card" + mSlotId);
            if (mSlotId == 0) {
                custRoamingSwitchForCust(mContext, displayName);
            }
            if (mSlotId == EVENT_RESET_APN_FAIL_MENTION && "1 for EE".equals(displayName)) {
                Global.putInt(mContext.getContentResolver(), HwCustHwSIMRecordsImpl.DATA_ROAMING_SIM2, EVENT_RESET_APN_FAIL_MENTION);
                return;
            }
            return;
        }
        log("EE Data roaming is on for single card" + mSlotId);
        custRoamingSwitchForCust(mContext, displayName);
    }

    public void custRoamingSwitchForCust(Context mContext, String displayName) {
        if ("1 for EE".equals(displayName)) {
            Global.putInt(mContext.getContentResolver(), "data_roaming", EVENT_RESET_APN_FAIL_MENTION);
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
        mAllowApnMention = IS_DISABLE_AP;
        log("[apn_all_fail]mentionApnFailed all failed: start activity jump");
        int themeID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        Builder builder = new Builder(new ContextThemeWrapper(this.mContext, themeID), themeID);
        builder.setTitle(33685929).setCancelable(HWDBG).setMessage(33685930).setPositiveButton(33685932, new goSetApnListener()).setNegativeButton(33685931, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                HwCustApnReminderImpl.this.getHandler().sendMessageDelayed(HwCustApnReminderImpl.this.getHandler().obtainMessage(HwCustApnReminderImpl.EVENT_RESET_APN_FAIL_MENTION), 15000);
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
            builder.setPositiveButton(33685933, new goSetApnListener());
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
