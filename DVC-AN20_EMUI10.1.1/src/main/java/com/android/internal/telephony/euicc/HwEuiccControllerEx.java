package com.android.internal.telephony.euicc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.euicc.DownloadableSubscription;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HwIccIdUtil;
import com.android.internal.telephony.euicc.EuiccConnector;
import com.huawei.android.telephony.SubscriptionManagerEx;
import java.util.Iterator;
import java.util.List;

public class HwEuiccControllerEx implements IHwEuiccControllerEx {
    private static final String DOWNLOAD_ESIM_FOR_VSIM_KEY = "esim_subid_by_vsim_download";
    private static final int ERROR = 2;
    private static final String EXTRA_EMBEDDED_SUBSCRIPTION_ICCID = "huawei.telephony.euicc.extra.EXTRA_EMBEDDED_SUBSCRIPTION_ICCID";
    private static final String EXTRA_EMBEDDED_SUBSCRIPTION_SUB_ID = "huawei.telephony.euicc.extra.EXTRA_EMBEDDED_SUBSCRIPTION_SUB_ID";
    private static final int ICCID_INDEX = 2;
    private static final String INTENT_VALUE_KEY = "result";
    private static final String LOG_TAG = "HwEuiccControllerEx";
    private static final int OK = 0;
    private static final String PACKAGES_NAME_SKYTONE = "com.huawei.skytone";
    private Context mContext;
    private String mIccid;
    private IHwEuiccControllerInner mInner;

    public HwEuiccControllerEx(Context context, IHwEuiccControllerInner euiccController) {
        this.mContext = context;
        this.mInner = euiccController;
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

    private String getEsimSubIdForVsim() {
        Context context = this.mContext;
        if (context == null) {
            return "";
        }
        return Settings.System.getString(context.getContentResolver(), DOWNLOAD_ESIM_FOR_VSIM_KEY);
    }
}
