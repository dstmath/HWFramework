package com.android.internal.telephony.imsphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsReasonInfo;
import android.widget.Toast;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.PhoneConstants;
import java.util.List;

public class HwCustImsPhoneCallTrackerImpl extends HwCustImsPhoneCallTracker {
    private static final boolean DBG = true;
    private static final boolean IS_FORKED_CALL_LOGGING = SystemProperties.getBoolean("ro.config.hw_ForkedCallLog", false);
    private static final boolean IS_REJECT_HW_CALL = SystemProperties.getBoolean("ro.config.isRejectHwCall", false);
    private static final boolean IS_VDF = SystemProperties.get("ro.config.hw_opta", "0").equals("02");
    private static final boolean IS_VOWIFI_ENHANCEMENT_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_vowifi_enhancement", false);
    private static final String LOG_TAG = "HwCustImsPhoneCallTrackerImpl";
    private Context mContext;
    private int mRejectCause = -1;
    private String mTelecomCallId = null;
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
        return HuaweiTelephonyConfigs.isQcomPlatform();
    }

    public int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        int code = reasonInfo.getCode();
        if (code == 361) {
            return 16;
        }
        if (code != 19488) {
            return 36;
        }
        return 43;
    }

    public boolean isForkedCallLoggingEnabled() {
        return IS_FORKED_CALL_LOGGING;
    }

    public void addSipErrorPopup(ImsReasonInfo reasonInfo, Context context) {
        Rlog.d(LOG_TAG, "[HwCustImsPhoneCallTrackerImpl] addSipErrorPopup reasonInfo = " + reasonInfo + " context = " + context);
        if (getToastReasonInfo(reasonInfo) && context != null) {
            Toast.makeText(context, context.getResources().getString(17040330), 1).show();
        }
    }

    private boolean getToastReasonInfo(ImsReasonInfo reasonInfo) {
        int code = reasonInfo.getCode();
        Rlog.d(LOG_TAG, "[HwCustImsPhoneCallTrackerImpl] getToastReasonInfo code = " + code);
        if (!(code == 132 || code == 145)) {
            switch (code) {
                case 401:
                case 402:
                case 403:
                case 404:
                    break;
                default:
                    return false;
            }
        }
        return DBG;
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
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(1, new Notification.Builder(this.mContext).setSmallIcon(iconId).setContentTitle(notifiTitle).setContentText(notifiText).setShowWhen(DBG).setStyle(new Notification.BigTextStyle().bigText(notifiText)).build());
        }
    }

    public void rejectCallForCause(ImsCall imsCall) throws ImsException {
        if (imsCall == null) {
            Rlog.d(LOG_TAG, "rejectImsCallForCause, imsCall is null");
            return;
        }
        Rlog.i(LOG_TAG, "rejectCallForCause, cause:" + this.mRejectCause);
        imsCall.reject(getImsCallRejectReasonFromCause(this.mRejectCause));
        clearCallRejectMark();
    }

    private int getImsCallRejectReasonFromCause(int cause) {
        switch (cause) {
            case 0:
                return 520;
            case 1:
                return 521;
            default:
                return 504;
        }
    }

    public int getRejectCallCause(ImsPhoneCall ringCall) {
        if (!IS_REJECT_HW_CALL) {
            Rlog.d(LOG_TAG, "IS_REJECT_HW_CALL is false");
            return -1;
        } else if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
            Rlog.d(LOG_TAG, "Platform is not Hisi");
            return -1;
        } else if (ringCall == null || ringCall.getPhone() == null || 5 != ringCall.getPhone().getPhoneType()) {
            Rlog.d(LOG_TAG, "PhoneType is not PHONE_TYPE_IMS");
            return -1;
        } else if (ringCall.getPhone().getState() != PhoneConstants.State.RINGING) {
            Rlog.d(LOG_TAG, "There is no a ringing call");
            return -1;
        } else {
            List<Connection> cs = ringCall.getConnections();
            if (cs == null || cs.isEmpty()) {
                Rlog.d(LOG_TAG, "Ringing call not has Connection");
                return -1;
            } else if (this.mTelecomCallId == null || this.mTelecomCallId.isEmpty()) {
                Rlog.i(LOG_TAG, "getRejectCallCause - mTelecomCallId is null!");
                return -1;
            } else {
                for (Connection c : cs) {
                    if (this.mTelecomCallId.equals(c.getTelecomCallId())) {
                        return this.mRejectCause;
                    }
                }
                return -1;
            }
        }
    }

    public void markCallRejectCause(String telecomCallId, int cause) {
        Rlog.d(LOG_TAG, "markCallRejectCause, telecomCallId: " + telecomCallId + " cause:" + cause);
        this.mTelecomCallId = telecomCallId;
        this.mRejectCause = cause;
    }

    private void clearCallRejectMark() {
        Rlog.i(LOG_TAG, "clearCallRejectMark");
        this.mTelecomCallId = null;
        this.mRejectCause = -1;
    }
}
