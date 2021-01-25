package com.android.internal.telephony.euicc;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.euicc.DownloadableSubscription;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HwIccIdUtil;
import com.android.internal.telephony.euicc.EuiccConnector;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccSlot;
import com.huawei.android.telephony.SubscriptionManagerEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class HwEuiccControllerEx implements IHwEuiccControllerEx {
    private static final String DOWNLOAD_ESIM_FOR_VSIM_KEY = "esim_subid_by_vsim_download";
    private static final int ERROR = 2;
    private static final int EVENT_GET_BEST_COMPONENT_RETRY = 2001;
    private static final int EVENT_HW_BASE = 2000;
    private static final int EVENT_STARTOTA_FOR_EUICC = 2002;
    private static final String EXTRA_EMBEDDED_SUBSCRIPTION_ICCID = "huawei.telephony.euicc.extra.EXTRA_EMBEDDED_SUBSCRIPTION_ICCID";
    private static final String EXTRA_EMBEDDED_SUBSCRIPTION_SUB_ID = "huawei.telephony.euicc.extra.EXTRA_EMBEDDED_SUBSCRIPTION_SUB_ID";
    private static final int GET_BEST_COMPONENT_RETRY_DELAY_TIME = 2000;
    private static final int HOTA_ACCESS_ALLOW = 2;
    private static final int HOTA_ACCESS_NOT_ALLOW = 1;
    private static final int ICCID_INDEX = 2;
    private static final String INTENT_VALUE_KEY = "result";
    private static final int IS_HOTA_VERSION_DEFAULT = -1;
    private static final boolean IS_SUPPORT_OSU = SystemProperties.getBoolean("hw_mc.telephony.enable_esim_osu", true);
    private static final String KRY_HOTA_FLAG = "euicc_hota_flag";
    private static final String KRY_IS_HOTA_VERSION = "euicc_is_hota_version";
    private static final String KRY_OTA_REASON = "euicc_ota_reason";
    private static final String LOG_TAG = "HwEuiccControllerEx";
    private static final int OK = 0;
    private static final String PACKAGES_NAME_SKYTONE = "com.huawei.skytone";
    private static final int RETRY_MAX_TIME = 60;
    private static final int STARTOTA_DELAY_TIME = 100;
    private Context mContext;
    private Handler mHandler = new MyHandler();
    private String mIccid;
    private IHwEuiccControllerInner mInner;
    private PackageManager mPm;
    private int mRetryTimes;
    private SimStateChangedBroadcastReceiver mSimStateChangeReceiver;

    public HwEuiccControllerEx(Context context, IHwEuiccControllerInner euiccController) {
        this.mContext = context;
        this.mInner = euiccController;
        if (IS_SUPPORT_OSU) {
            registerSimStateChangeforEuicc();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logi(String log) {
        Rlog.i(LOG_TAG, log);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String log) {
        Rlog.e(LOG_TAG, log);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendResult(PendingIntent callbackIntent, int resultCode, Intent extrasIntent) {
        EuiccController.get().sendResult(callbackIntent, resultCode, extrasIntent);
    }

    private boolean callerCanReadPhoneStatePrivileged() {
        return this.mContext.checkCallingPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") == 0;
    }

    private boolean callerCanWriteEmbeddedSubscriptions() {
        return this.mContext.checkCallingPermission("android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS") == 0;
    }

    public void requestDefaultSmdpAddress(String cardId, final PendingIntent callbackIntent) {
        if (callerCanReadPhoneStatePrivileged()) {
            logi("requestDefaultSmdpAddress, enter.");
            long token = Binder.clearCallingIdentity();
            try {
                this.mInner.getEuiccConnector().getEuiccConnectorEx().requestDefaultSmdpAddress(cardId, new EuiccConnector.RequestDefaultSmdpAddressCommandCallback() {
                    /* class com.android.internal.telephony.euicc.HwEuiccControllerEx.AnonymousClass1 */

                    public void onRequestDefaultSmdpAddressComplete(String result) {
                        Intent extrasIntent = new Intent();
                        int resultCode = TextUtils.isEmpty(result) ? 2 : 0;
                        HwEuiccControllerEx hwEuiccControllerEx = HwEuiccControllerEx.this;
                        hwEuiccControllerEx.logi("onRequestDefaultSmdpAddressComplete， resultCode:" + resultCode);
                        extrasIntent.putExtra(HwEuiccControllerEx.INTENT_VALUE_KEY, result);
                        HwEuiccControllerEx.this.sendResult(callbackIntent, resultCode, extrasIntent);
                    }

                    public void onEuiccServiceUnavailable() {
                        HwEuiccControllerEx.this.loge("requestDefaultSmdpAddress, onEuiccServiceUnavailable.");
                        HwEuiccControllerEx.this.sendResult(callbackIntent, 2, null);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to request smdp address");
        }
    }

    public void resetMemory(String cardId, int options, final PendingIntent callbackIntent) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            logi("resetMemory, enter. options = " + options);
            long token = Binder.clearCallingIdentity();
            try {
                this.mInner.getEuiccConnector().getEuiccConnectorEx().resetMemory(cardId, options, new EuiccConnector.ResetMemoryCommandCallback() {
                    /* class com.android.internal.telephony.euicc.HwEuiccControllerEx.AnonymousClass2 */

                    public void onResetMemoryComplete(int result) {
                        int resultCode;
                        HwEuiccControllerEx hwEuiccControllerEx = HwEuiccControllerEx.this;
                        hwEuiccControllerEx.logi("onResetMemoryComplete, result = " + result);
                        Intent extrasIntent = new Intent();
                        if (result != 0) {
                            resultCode = 2;
                            extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                        } else {
                            resultCode = 0;
                        }
                        HwEuiccControllerEx.this.sendResult(callbackIntent, resultCode, extrasIntent);
                    }

                    public void onEuiccServiceUnavailable() {
                        HwEuiccControllerEx.this.loge("resetMemory, onEuiccServiceUnavailable.");
                        HwEuiccControllerEx.this.sendResult(callbackIntent, 2, null);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to reset memory");
        }
    }

    public void setDefaultSmdpAddress(String cardId, String defaultSmdpAddress, final PendingIntent callbackIntent) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            logi("setDefaultSmdpAddress, enter.");
            long token = Binder.clearCallingIdentity();
            try {
                this.mInner.getEuiccConnector().getEuiccConnectorEx().setDefaultSmdpAddress(cardId, defaultSmdpAddress, new EuiccConnector.SetDefaultSmdpAddressCommandCallback() {
                    /* class com.android.internal.telephony.euicc.HwEuiccControllerEx.AnonymousClass3 */

                    public void onSetDefaultSmdpAddressComplete(int result) {
                        int resultCode;
                        HwEuiccControllerEx hwEuiccControllerEx = HwEuiccControllerEx.this;
                        hwEuiccControllerEx.logi("onSetDefaultSmdpAddressComplete, result = " + result);
                        Intent extrasIntent = new Intent();
                        if (result != 0) {
                            resultCode = 2;
                            extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                        } else {
                            resultCode = 0;
                        }
                        HwEuiccControllerEx.this.sendResult(callbackIntent, resultCode, extrasIntent);
                    }

                    public void onEuiccServiceUnavailable() {
                        HwEuiccControllerEx.this.loge("setDefaultSmdpAddress, onEuiccServiceUnavailable.");
                        HwEuiccControllerEx.this.sendResult(callbackIntent, 2, null);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to set smdp address");
        }
    }

    public void cancelSession() {
        if (callerCanWriteEmbeddedSubscriptions()) {
            logi("cancelSession, enter.");
            long token = Binder.clearCallingIdentity();
            try {
                this.mInner.getEuiccConnector().getEuiccConnectorEx().cancelSession();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to cancel session");
        }
    }

    public void putSubIdForVsim(PendingIntent callbackIntent, Intent extrasIntent) {
        Log.i(LOG_TAG, "putExtraSubIdForVsim iccid begin");
        if (callbackIntent == null || extrasIntent == null || TextUtils.isEmpty(this.mIccid)) {
            Log.e(LOG_TAG, "putExtraSubIdForVsim intent is null or iccid is null");
        } else if (!PACKAGES_NAME_SKYTONE.equals(callbackIntent.getCreatorPackage())) {
            Log.i(LOG_TAG, "putSubIdForVsim, getCreatorPackage is not skytone");
        } else {
            int subId = 0;
            List<SubscriptionInfo> allSubscriptionInfoList = SubscriptionManagerEx.getAllSubscriptionInfoList(this.mContext);
            if (allSubscriptionInfoList != null) {
                Iterator<SubscriptionInfo> it = allSubscriptionInfoList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    SubscriptionInfo info = it.next();
                    if (info.isEmbedded() && this.mIccid.equals(info.getIccId())) {
                        subId = info.getSubscriptionId();
                        break;
                    }
                }
            }
            if (subId != 0) {
                Log.i(LOG_TAG, "putSubIdForVsim put subid=" + subId);
                this.mIccid = null;
                extrasIntent.putExtra(EXTRA_EMBEDDED_SUBSCRIPTION_SUB_ID, subId);
                setEsimSubIdForVsim(subId);
            }
        }
    }

    public void putIccidByDownloadableSubscription(PendingIntent callbackIntent, Intent extrasIntent, DownloadableSubscription downloadableSubscription) {
        Log.i(LOG_TAG, "putIccidByDownloadableSubscription begin");
        if (downloadableSubscription == null || callbackIntent == null || extrasIntent == null) {
            Log.i(LOG_TAG, "putIccidByDownloadableSubscription downloadableSubscription is null");
        } else if (!PACKAGES_NAME_SKYTONE.equals(callbackIntent.getCreatorPackage())) {
            Log.i(LOG_TAG, "putIccidByDownloadableSubscription, getCreatorPackage is not skytone");
        } else {
            String metadataString = HwTelephonyManager.getDefault().getCarrierName(downloadableSubscription);
            if (TextUtils.isEmpty(metadataString)) {
                Log.i(LOG_TAG, "putIccidByDownloadableSubscription metadata is null");
                return;
            }
            String[] metadatas = metadataString.split(":");
            if (metadatas != null && metadatas.length >= 2) {
                this.mIccid = HwIccIdUtil.padTrailingFs(metadatas[1]);
            }
            extrasIntent.putExtra(EXTRA_EMBEDDED_SUBSCRIPTION_ICCID, this.mIccid);
        }
    }

    private String getEsimSubIdForVsim() {
        Context context = this.mContext;
        if (context == null) {
            return "";
        }
        return Settings.System.getString(context.getContentResolver(), DOWNLOAD_ESIM_FOR_VSIM_KEY);
    }

    private void setEsimSubIdForVsim(int subId) {
        if (this.mContext != null && subId > 0) {
            Log.i(LOG_TAG, "set esim subid by vsim download, subid=" + subId);
            String subIds = getEsimSubIdForVsim();
            StringBuilder stringBuilder = new StringBuilder();
            if (TextUtils.isEmpty(subIds)) {
                stringBuilder.append(subId);
            } else {
                stringBuilder.append(subIds);
                stringBuilder.append(":");
                stringBuilder.append(subId);
            }
            Settings.System.putString(this.mContext.getContentResolver(), DOWNLOAD_ESIM_FOR_VSIM_KEY, stringBuilder.toString());
        }
    }

    private void registerSimStateChangeforEuicc() {
        if (this.mContext != null) {
            this.mSimStateChangeReceiver = new SimStateChangedBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SIM_STATE_CHANGED");
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            this.mContext.registerReceiver(this.mSimStateChangeReceiver, filter);
        }
    }

    public void processEuiccServiceUnavailable() {
        this.mPm = this.mContext.getPackageManager();
        if (EuiccConnector.findBestComponent(this.mPm) == null) {
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(EVENT_GET_BEST_COMPONENT_RETRY), 2000);
            this.mRetryTimes = 0;
            return;
        }
        logi("initBestComponent, has got best component.");
        this.mInner.startOtaUpdatingIfNecessary();
    }

    public void processOtaStatusChanged(int status) {
        Settings.Global.putInt(this.mContext.getContentResolver(), KRY_OTA_REASON, 0);
        Intent intent = new Intent("android.telephony.euicc.action.OTA_STATUS_CHANGED");
        intent.setPackage("com.huawei.esimmanager");
        intent.putExtra("status", status);
        this.mContext.sendBroadcast(intent, "android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS");
        logi("processOtaStatusChanged.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetBestComponentRetry() {
        PackageManager packageManager = this.mPm;
        if (packageManager == null || this.mRetryTimes >= RETRY_MAX_TIME) {
            logi("handleGetBestComponentRetry has arrived max times, stop!");
            this.mRetryTimes = 0;
        } else if (EuiccConnector.findBestComponent(packageManager) == null) {
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(EVENT_GET_BEST_COMPONENT_RETRY), 2000);
            this.mRetryTimes++;
        } else {
            logi("handleGetBestComponentRetry has got euicc service, try to refresh subscription!");
            Handler handler2 = this.mHandler;
            handler2.sendMessageDelayed(handler2.obtainMessage(EVENT_STARTOTA_FOR_EUICC), 100);
            this.mRetryTimes = 0;
        }
    }

    public void startOtaUpdating(int cardId, int otaReason) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            Settings.Global.putInt(this.mContext.getContentResolver(), KRY_OTA_REASON, otaReason);
            this.mInner.startOtaUpdatingIfNecessary(cardId);
            return;
        }
        throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to start euicc Os Updating");
    }

    /* access modifiers changed from: private */
    public class SimStateChangedBroadcastReceiver extends BroadcastReceiver {
        boolean isHota;
        int versionInDb;
        int versionInfile;

        private SimStateChangedBroadcastReceiver() {
            this.isHota = false;
            this.versionInfile = Integer.MAX_VALUE;
            this.versionInDb = Integer.MAX_VALUE;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                setHotaFlag();
                String actionType = intent.getAction();
                if (!TextUtils.isEmpty(actionType)) {
                    char c = 65535;
                    int hashCode = actionType.hashCode();
                    if (hashCode != -229777127) {
                        if (hashCode == 798292259 && actionType.equals("android.intent.action.BOOT_COMPLETED")) {
                            c = 1;
                        }
                    } else if (actionType.equals("android.intent.action.SIM_STATE_CHANGED")) {
                        c = 0;
                    }
                    if (c == 0) {
                        processSimStateChanged();
                    } else if (c != 1) {
                        HwEuiccControllerEx.this.logi("SimStateChangedBroadcastReceiver: illegal type, " + actionType);
                    } else {
                        HwEuiccControllerEx.this.logi("isFinishStart unregister ");
                        HwEuiccControllerEx.this.mContext.unregisterReceiver(HwEuiccControllerEx.this.mSimStateChangeReceiver);
                    }
                }
            }
        }

        private void processSimStateChanged() {
            UiccSlot slotInfo = HwEuiccControllerEx.this.getEuiccSlot();
            if (slotInfo == null) {
                HwEuiccControllerEx.this.logi("processSimStateChanged : slotInfo is null.");
            } else if (slotInfo.getCardState() == null || !slotInfo.getCardState().isCardPresent()) {
                HwEuiccControllerEx.this.logi("processSimStateChanged : card state is not ready.");
            } else {
                boolean isSlotReady = slotInfo.isActive();
                boolean isEuicc = slotInfo.isEuicc();
                HwEuiccControllerEx hwEuiccControllerEx = HwEuiccControllerEx.this;
                hwEuiccControllerEx.logi("processSimStateChanged, isHota=" + this.isHota + " isSlotReady=" + isSlotReady + " isEuicc=" + isEuicc);
                if (isSlotReady && this.isHota && isEuicc) {
                    HwEuiccControllerEx.this.mContext.unregisterReceiver(HwEuiccControllerEx.this.mSimStateChangeReceiver);
                    HwEuiccControllerEx.this.mInner.startOtaUpdatingIfNecessary();
                }
            }
        }

        private void setHotaFlag() {
            if (this.versionInfile == Integer.MAX_VALUE && this.versionInDb == Integer.MAX_VALUE) {
                this.versionInfile = getVersionInFile();
                this.versionInDb = Settings.Global.getInt(HwEuiccControllerEx.this.mContext.getContentResolver(), HwEuiccControllerEx.KRY_IS_HOTA_VERSION, -1);
                HwEuiccControllerEx hwEuiccControllerEx = HwEuiccControllerEx.this;
                hwEuiccControllerEx.logi("setHotaFlag, versionInfile=" + this.versionInfile + " versionInDb=" + this.versionInDb);
                int i = this.versionInfile;
                if (i == 0 || i <= this.versionInDb) {
                    Settings.Global.putInt(HwEuiccControllerEx.this.mContext.getContentResolver(), HwEuiccControllerEx.KRY_HOTA_FLAG, 1);
                } else {
                    Settings.Global.putInt(HwEuiccControllerEx.this.mContext.getContentResolver(), HwEuiccControllerEx.KRY_HOTA_FLAG, 2);
                    if (this.versionInDb != -1) {
                        this.isHota = true;
                    }
                }
                if (this.versionInDb != this.versionInfile) {
                    Settings.Global.putInt(HwEuiccControllerEx.this.mContext.getContentResolver(), HwEuiccControllerEx.KRY_IS_HOTA_VERSION, this.versionInfile);
                }
            }
        }

        private int getVersionInFile() {
            int version = 0;
            FileReader input = null;
            BufferedReader reader = null;
            try {
                FileReader input2 = new FileReader(new File("/odm/etc/firmware/euicc/version.txt"));
                BufferedReader reader2 = new BufferedReader(input2);
                while (true) {
                    String strLine = reader2.readLine();
                    if (strLine == null) {
                        try {
                            break;
                        } catch (IOException e) {
                            HwEuiccControllerEx.this.loge("getVersionInFile, close file, IOException");
                        }
                    } else if (strLine.startsWith("version=")) {
                        version = Integer.parseInt(strLine.substring("version=".length()));
                    }
                }
                input2.close();
                reader2.close();
            } catch (FileNotFoundException e2) {
                HwEuiccControllerEx.this.loge("getVersionInFile, FileNotFoundException");
                if (0 != 0) {
                    input.close();
                }
                if (0 != 0) {
                    reader.close();
                }
            } catch (IOException e3) {
                HwEuiccControllerEx.this.loge("getVersionInFile, IOException");
                if (0 != 0) {
                    input.close();
                }
                if (0 != 0) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        input.close();
                    } catch (IOException e4) {
                        HwEuiccControllerEx.this.loge("getVersionInFile, close file, IOException");
                        throw th;
                    }
                }
                if (0 != 0) {
                    reader.close();
                }
                throw th;
            }
            return version;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UiccSlot getEuiccSlot() {
        UiccSlot[] slots = UiccController.getInstance().getUiccSlots();
        if (slots == null) {
            return null;
        }
        for (UiccSlot slotInfo : slots) {
            if (slotInfo != null && slotInfo.isEuicc()) {
                return slotInfo;
            }
        }
        return null;
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == HwEuiccControllerEx.EVENT_GET_BEST_COMPONENT_RETRY) {
                HwEuiccControllerEx hwEuiccControllerEx = HwEuiccControllerEx.this;
                hwEuiccControllerEx.logi("handleMessage: EVENT_GET_BEST_COMPONENT_RETRY, mRetryTimes = " + HwEuiccControllerEx.this.mRetryTimes);
                HwEuiccControllerEx.this.handleGetBestComponentRetry();
            } else if (i == HwEuiccControllerEx.EVENT_STARTOTA_FOR_EUICC) {
                HwEuiccControllerEx.this.logi("handleMessage: EVENT_REFRESH_SUBSCRIPTION_FOR_EUICC");
                HwEuiccControllerEx.this.mInner.startOtaUpdatingIfNecessary();
            }
            super.handleMessage(msg);
        }
    }
}
