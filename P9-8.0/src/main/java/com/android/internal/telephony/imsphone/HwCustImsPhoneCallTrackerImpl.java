package com.android.internal.telephony.imsphone;

import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.android.ims.ImsManager;
import com.android.ims.ImsReasonInfo;

public class HwCustImsPhoneCallTrackerImpl extends HwCustImsPhoneCallTracker {
    private static final String BOARD_PLATFORM_TAG = "ro.board.platform";
    private static final boolean DBG = true;
    private static final boolean IS_FORKED_CALL_LOGGING = SystemProperties.getBoolean("ro.config.hw_ForkedCallLog", false);
    private static final boolean IS_VDF = SystemProperties.get("ro.config.hw_opta", "0").equals("02");
    private static final boolean IS_VOWIFI_ENHANCEMENT_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_vowifi_enhancement", false);
    private static final String LOG_TAG = "HwCustImsPhoneCallTrackerImpl";
    private static final String PLATFORM_QUALCOMM = "msm";
    private Context mContext;
    private TelephonyManager mTelephonyManager;

    public HwCustImsPhoneCallTrackerImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean checkImsRegistered() {
        if (IS_VDF && isQcomPlatform() && this.mContext != null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            if (this.mTelephonyManager != null) {
                return this.mTelephonyManager.isImsRegistered();
            }
        }
        return DBG;
    }

    private boolean isQcomPlatform() {
        return SystemProperties.get("ro.board.platform", "").startsWith(PLATFORM_QUALCOMM);
    }

    public int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        switch (reasonInfo.getCode()) {
            case 361:
                return 16;
            default:
                return 36;
        }
    }

    public boolean isForkedCallLoggingEnabled() {
        return IS_FORKED_CALL_LOGGING;
    }

    public void addSipErrorPopup(ImsReasonInfo reasonInfo, Context context) {
        Rlog.d(LOG_TAG, "[HwCustImsPhoneCallTrackerImpl] addSipErrorPopup reasonInfo = " + reasonInfo + " context = " + context);
        if (getToastReasonInfo(reasonInfo) && context != null) {
            Toast.makeText(context, context.getResources().getString(17040258), 1).show();
        }
    }

    private boolean getToastReasonInfo(ImsReasonInfo reasonInfo) {
        int code = reasonInfo.getCode();
        Rlog.d(LOG_TAG, "[HwCustImsPhoneCallTrackerImpl] getToastReasonInfo code = " + code);
        switch (code) {
            case 132:
            case 145:
            case 401:
            case 402:
            case 403:
            case 404:
                return DBG;
            default:
                return false;
        }
    }

    public void handleCallDropErrors(ImsReasonInfo reasonInfo) {
        Rlog.d(LOG_TAG, "handleCallDropErrors is called");
        int code = reasonInfo.getCode();
        if (this.mContext != null) {
            Rlog.d(LOG_TAG, "isWfcEnabledByUser:" + ImsManager.isWfcEnabledByUser(this.mContext));
            if (IS_VOWIFI_ENHANCEMENT_SUPPORTED && ImsManager.isWfcEnabledByUser(this.mContext)) {
                String notifiTitle = this.mContext.getResources().getString(33686000);
                if (352 == code) {
                    showCallDropNotification(notifiTitle, this.mContext.getResources().getString(33686001), 33751742);
                } else if (332 == code) {
                    showCallDropNotification(notifiTitle, this.mContext.getResources().getString(33686002), 33751742);
                }
            }
        }
    }

    private void showCallDropNotification(String notifiTitle, String notifiText, int iconId) {
        if (this.mContext != null && notifiTitle != null && notifiText != null) {
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(1, new Builder(this.mContext).setSmallIcon(iconId).setContentTitle(notifiTitle).setContentText(notifiText).setShowWhen(DBG).setStyle(new BigTextStyle().bigText(notifiText)).build());
        }
    }
}
