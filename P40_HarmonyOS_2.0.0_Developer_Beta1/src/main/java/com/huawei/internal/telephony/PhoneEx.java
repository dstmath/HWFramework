package com.huawei.internal.telephony;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.ServiceState;
import android.util.Log;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.RILConstants;
import com.huawei.android.util.NoExtAPIException;

public class PhoneEx {
    public static final int CALL_DOMAIN_AUTOMATIC = 3;
    public static final int CALL_DOMAIN_CS = 1;
    public static final int CALL_DOMAIN_NOT_SET = 4;
    public static final int CALL_DOMAIN_PS = 2;
    public static final int CALL_TYPE_UNKNOWN = 10;
    public static final int CALL_TYPE_VOICE = 0;
    public static final int CALL_TYPE_VT = 3;
    public static final int CALL_TYPE_VT_NODIR = 4;
    public static final int CALL_TYPE_VT_RX = 2;
    public static final int CALL_TYPE_VT_TX = 1;
    public static final String EXTRAS_IS_CONFERENCE_URI = "isConferenceUri";
    public static final String FEATURE_ENABLE_MMS = "enableMMS";
    public static final String FEATURE_ENABLE_MMS_SUB1 = "enableMMS_sub1";
    public static final String FEATURE_ENABLE_MMS_SUB2 = "enableMMS_sub2";
    public static final int NT_MODE_CDMA = 4;
    public static final int NT_MODE_CDMA_NO_EVDO = 5;
    public static final int NT_MODE_EVDO_NO_CDMA = 6;
    public static final int NT_MODE_GLOBAL = 7;
    public static final int NT_MODE_GSM_ONLY = 1;
    public static final int NT_MODE_GSM_UMTS = 3;
    public static final int NT_MODE_LTE_CDMA_AND_EVDO = 8;
    public static final int NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA = 9;
    public static final int NT_MODE_LTE_GSM_WCDMA = 9;
    public static final int NT_MODE_LTE_ONLY = 11;
    public static final int NT_MODE_LTE_WCDMA = 12;
    public static final int NT_MODE_NR_LTE = 67;
    public static final int NT_MODE_NR_LTE_EVDO_CDMA = 64;
    public static final int NT_MODE_NR_LTE_EVDO_CDMA_WCDMA_GSM = 69;
    public static final int NT_MODE_NR_LTE_WCDMA = 68;
    public static final int NT_MODE_NR_LTE_WCDMA_GSM = 65;
    public static final int NT_MODE_NR_ONLY = 66;
    public static final int NT_MODE_TD_SCDMA_CDMA_EVDO_GSM_WCDMA = 21;
    public static final int NT_MODE_TD_SCDMA_GSM = 16;
    public static final int NT_MODE_TD_SCDMA_GSM_LTE = 17;
    public static final int NT_MODE_TD_SCDMA_GSM_WCDMA = 18;
    public static final int NT_MODE_TD_SCDMA_GSM_WCDMA_LTE = 20;
    public static final int NT_MODE_TD_SCDMA_LTE = 15;
    public static final int NT_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA = 22;
    public static final int NT_MODE_TD_SCDMA_ONLY = 13;
    public static final int NT_MODE_TD_SCDMA_WCDMA = 14;
    public static final int NT_MODE_TD_SCDMA_WCDMA_LTE = 19;
    public static final int NT_MODE_WCDMA_ONLY = 2;
    public static final int NT_MODE_WCDMA_PREF = 0;
    public static final int PHONE_TYPE_RIL_IMS = 4;
    public static final int PIN_GENERAL_FAILURE = 2;
    public static final int PIN_PASSWORD_INCORRECT = 1;
    public static final int PIN_RESULT_SUCCESS = 0;
    public static final int PREFERRED_NT_MODE = RILConstants.PREFERRED_NETWORK_MODE;
    public static final String REASON_VOICE_CALL_ENDED = "2GVoiceCallEnded";
    private Phone mphone;

    public static final int getSubscription(Phone obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void setOnUnsolOemHookExtApp(Phone obj, Handler h, int what, Object object) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void unSetOnUnsolOemHookExtApp(Phone obj, Handler h) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void invokeSimlessHW(Phone obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void setModemPower(Phone obj, boolean on) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void registerForSimRecordsLoaded(Phone phoneObj, Handler h, int what, Object obj) {
        if (phoneObj != null) {
            phoneObj.registerForSimRecordsLoaded(h, what, obj);
        }
    }

    public static void unregisterForSimRecordsLoaded(Phone obj, Handler h) {
        if (obj != null) {
            obj.unregisterForSimRecordsLoaded(h);
        }
    }

    public static boolean isIdleEx() {
        boolean retVal = true;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder b = ServiceManager.getService(PhoneConstantsEx.PHONE_KEY);
        if (b != null) {
            boolean z = false;
            try {
                b.transact(1001, data, reply, 0);
                if (reply.readInt() == 1) {
                    z = true;
                }
                retVal = z;
            } catch (RemoteException e) {
                Log.e("PhoneEx", "add-on isIdle() in exception....");
            }
        }
        reply.recycle();
        data.recycle();
        return retVal;
    }

    public static final String getFeatureEnable(int sub) {
        if (sub == 0) {
            return FEATURE_ENABLE_MMS_SUB1;
        }
        if (sub == 1) {
            return FEATURE_ENABLE_MMS_SUB2;
        }
        return null;
    }

    public static void setLocalCallHold(Phone obj, int lchStatus) {
        if (obj != null) {
            obj.switchVoiceCallBackgroundState(lchStatus);
        }
    }

    public PhoneEx(Phone obj) {
        this.mphone = obj;
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        Phone phone = this.mphone;
        if (phone != null) {
            phone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, onComplete);
        }
    }

    public ServiceState getServiceState() {
        Phone phone = this.mphone;
        if (phone == null) {
            return null;
        }
        return phone.getServiceState();
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        Phone phone = this.mphone;
        if (phone != null) {
            phone.getCallForwardingOption(commandInterfaceCFReason, onComplete);
        }
    }

    public int getPhoneType() {
        Phone phone = this.mphone;
        if (phone == null) {
            return -1;
        }
        return phone.getPhoneType();
    }

    public String getLine1Number() {
        Phone phone = this.mphone;
        if (phone == null) {
            return null;
        }
        return phone.getLine1Number();
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        Phone phone = this.mphone;
        if (phone != null) {
            phone.setCallWaiting(enable, onComplete);
        }
    }

    public boolean isCspPlmnEnabled() {
        Phone phone = this.mphone;
        if (phone == null) {
            return false;
        }
        return phone.isCspPlmnEnabled();
    }

    public void selectNetworkManually(OperatorInfo opt, boolean bl, Message message) {
        Phone phone = this.mphone;
        if (phone != null) {
            phone.selectNetworkManually(opt, bl, message);
        }
    }

    public void supplyPin(String pin, Message callback) {
        Phone phone = this.mphone;
        if (phone != null && phone.getIccCard() != null) {
            this.mphone.getIccCard().supplyPin(pin, callback);
        }
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        Phone phone = this.mphone;
        if (phone != null) {
            phone.setNetworkSelectionModeAutomatic(response);
        }
    }

    public void supplyPuk(String pin, String newPin, Message callback) {
        Phone phone = this.mphone;
        if (phone != null && phone.getIccCard() != null) {
            this.mphone.getIccCard().supplyPuk(pin, newPin, callback);
        }
    }

    public int getSubId() {
        Phone phone = this.mphone;
        if (phone == null) {
            return -1;
        }
        return phone.getSubId();
    }
}
