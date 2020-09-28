package com.huawei.android.telephony;

import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseCallState;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.ims.ImsReasonInfo;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.motiondetection.MotionTypeApps;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class PhoneStateListenerEx extends PhoneStateListener {
    public static final int LISTEN_CALL_DISCONNECT_CAUSES = 33554432;
    public static final int LISTEN_OEM_HOOK_RAW_EVENT = 32768;
    public static final int LISTEN_PRECISE_CALL_STATE = 2048;
    private static final String LOG_TAG = "PhoneStateListenerEx";
    protected int mSubscription = 0;

    public PhoneStateListenerEx() {
    }

    public PhoneStateListenerEx(int subscription) {
        super(Integer.valueOf(subscription));
        this.mSubscription = subscription;
    }

    public PhoneStateListenerEx(int subId, Looper looper) {
        super(Integer.valueOf(subId), looper);
    }

    public static int getSubscription(PhoneStateListener obj) {
        try {
            final Field filed = Class.forName("android.telephony.PhoneStateListener").getDeclaredField("mSubId");
            AccessController.doPrivileged(new PrivilegedAction() {
                /* class com.huawei.android.telephony.PhoneStateListenerEx.AnonymousClass1 */

                @Override // java.security.PrivilegedAction
                public Object run() {
                    filed.setAccessible(true);
                    return null;
                }
            });
            return SubscriptionManager.getSlotIndex(((Integer) filed.get(obj)).intValue());
        } catch (RuntimeException e) {
            throw new NoExtAPIException("method not supported.");
        } catch (Exception e2) {
            throw new NoExtAPIException("method not supported.");
        }
    }

    public static void setSubscription(PhoneStateListener obj, int subscription) {
        try {
            final Field filed = Class.forName("android.telephony.PhoneStateListener").getDeclaredField("mSubId");
            AccessController.doPrivileged(new PrivilegedAction() {
                /* class com.huawei.android.telephony.PhoneStateListenerEx.AnonymousClass2 */

                @Override // java.security.PrivilegedAction
                public Object run() {
                    filed.setAccessible(true);
                    return null;
                }
            });
            filed.set(obj, Integer.valueOf(subscription));
        } catch (RuntimeException e) {
            throw new NoExtAPIException("method not supported.");
        } catch (Exception e2) {
            throw new NoExtAPIException("method not supported.");
        }
    }

    public void onPreciseCallStateChangedCallBack(PreciseCallStateEx callState) {
    }

    public void onPreciseCallStateChanged(PreciseCallState callState) {
        onPreciseCallStateChangedCallBack(new PreciseCallStateEx(callState));
    }

    public void onOemHookRawEvent(byte[] rawData) {
    }

    public static int getListenCarrierNetwoekChange() {
        return 65536;
    }

    public void onImsCallDisconnectCauseChanged(ImsReasonInfo imsReasonInfo) {
        Rlog.d(LOG_TAG, "onImsCallDisconnectCauseChanged: imsReasonInfo " + imsReasonInfo);
        onImsCallDisconnectCauseChangedCallBack(getDisconnectCauseFromReasonInfo(imsReasonInfo));
    }

    public void onImsCallDisconnectCauseChangedCallBack(int imsDisconnectCause) {
        Rlog.d(LOG_TAG, "onImsCallDisconnectCauseChangedCallBack: imsDisconnectCause " + imsDisconnectCause);
    }

    private int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        if (reasonInfo == null) {
            Rlog.d(LOG_TAG, "getDisconnectCauseFromReasonInfo: reasonInfo == null");
            return 36;
        }
        int code = reasonInfo.getCode();
        switch (code) {
            case 21:
                return 21;
            case 106:
            case 121:
            case 122:
            case 123:
            case 124:
            case 131:
            case 132:
            case 144:
                return 18;
            case MetricConstant.GPS_METRIC_ID_EX /*{ENCODED_INT: 108}*/:
                return 45;
            case 111:
                return 17;
            case 112:
            case 505:
                return 61;
            case 143:
                return 16;
            case MotionTypeApps.TYPE_FLIP_MUTE_CALL /*{ENCODED_INT: 201}*/:
            case 202:
            case MotionTypeApps.TYPE_FLIP_MUTE_AOD /*{ENCODED_INT: 203}*/:
            case 335:
                return 13;
            case 240:
                return 20;
            case 241:
                return 21;
            case 243:
                return 58;
            case 244:
                return 46;
            case 245:
                return 47;
            case 246:
                return 48;
            case 247:
                return 66;
            case 248:
                return 69;
            case 249:
                return 70;
            case 250:
                return 67;
            case 251:
                return 68;
            case 321:
            case 331:
            case 340:
            case 361:
            case 362:
                return 12;
            case 332:
                return 12;
            case 333:
                return 7;
            case 337:
            case 341:
                return 8;
            case 338:
                return 4;
            case 352:
            case 354:
                return 9;
            case 363:
                return 63;
            case 364:
                return 64;
            case 501:
                return 3;
            case 510:
                return 2;
            case 1014:
                return 52;
            case 1016:
                return 51;
            case 1100:
                return DisconnectCauseEx.CALL_DROP_IWLAN_TO_LTE_UNAVAILABLE;
            case 1403:
                return 53;
            case 1404:
                return 1049;
            case 1405:
                return 55;
            case 1406:
                return 54;
            case 1407:
                return 59;
            case 1512:
                return 60;
            case 1514:
                return 71;
            case 1515:
                return 25;
            case 3001:
                return 1048;
            default:
                Rlog.d(LOG_TAG, "getDisconnectCauseFromReasonInfo: code is " + code + ", cause is " + 36);
                return 36;
        }
    }

    public void onCallDisconnectCauseChangedCallBack(int disconnectCause, int preciseDisconnectCause) {
    }

    public void onCallDisconnectCauseChanged(int disconnectCause, int preciseDisconnectCause) {
        onCallDisconnectCauseChangedCallBack(disconnectCause, preciseDisconnectCause);
    }
}
