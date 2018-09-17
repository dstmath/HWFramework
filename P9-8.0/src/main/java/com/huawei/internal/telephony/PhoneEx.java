package com.huawei.internal.telephony;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.ServiceState;
import android.util.Log;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.RILConstants;
import com.huawei.android.util.NoExtAPIException;
import java.util.Map;

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

    public static final boolean isManualNetSelAllowed(Phone obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void setDataReadinessChecks(Phone obj, boolean checkConnectivity, boolean checkSubscription, boolean tryDataCalls) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void setOnUnsolOemHookExtApp(Phone obj, Handler h, int what, Object object) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void setTransmitPower(Phone obj, int powerLevel, Message onCompleted) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void unSetOnUnsolOemHookExtApp(Phone obj, Handler h) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void invokeDepersonalization(Phone obj, String pin, int type, Message response) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void invokeSimlessHW(Phone obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void setModemPower(Phone obj, boolean on) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isRadioOn(Phone obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void registerForSimRecordsLoaded(Phone phoneObj, Handler h, int what, Object obj) {
        phoneObj.registerForSimRecordsLoaded(h, what, obj);
    }

    public static void unregisterForSimRecordsLoaded(Phone obj, Handler h) {
        obj.unregisterForSimRecordsLoaded(h);
    }

    public static boolean isVTModifyAllowed(Phone obj) throws CallStateException {
        throw new CallStateException("isVTModifyAllowed is not supported in this phone ");
    }

    public static void rejectConnectionTypeChange(Phone obj, Connection conn) throws CallStateException {
        throw new CallStateException("rejectConnectionTypeChange is not supported in this phone ");
    }

    public static void acceptConnectionTypeChange(Phone obj, Connection conn, Map<String, String> map) throws CallStateException {
        throw new CallStateException("acceptConnectionTypeChange is not supported in this phone ");
    }

    public static void changeConnectionType(Phone obj, Message msg, Connection conn, int newCallType, Map<String, String> map) throws CallStateException {
        throw new CallStateException("changeConnectionType is not supported in this phone ");
    }

    public static void addParticipant(Phone obj, String dialString, int clir, int callType, String[] extras) throws CallStateException {
        throw new CallStateException("addParticipant is not supported in this phone ");
    }

    public static void hangupWithReason(Phone obj, int callId, String userUri, boolean mpty, int failCause, String errorInfo) throws CallStateException {
        throw new CallStateException("hangupWithReason is not supported in this phone ");
    }

    public static int getCallType(Phone obj, Call call) throws CallStateException {
        throw new CallStateException("getCallType is not supported in this phone ");
    }

    public static boolean isIdleEx() {
        boolean retVal = true;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("phone");
        if (b != null) {
            try {
                b.transact(1001, data, reply, 0);
                retVal = reply.readInt() == 1;
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
        obj.switchVoiceCallBackgroundState(lchStatus);
    }

    public PhoneEx(Phone obj) {
        this.mphone = obj;
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        if (this.mphone != null) {
            this.mphone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, onComplete);
        }
    }

    public ServiceState getServiceState() {
        if (this.mphone == null) {
            return null;
        }
        return this.mphone.getServiceState();
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        if (this.mphone != null) {
            this.mphone.getCallForwardingOption(commandInterfaceCFReason, onComplete);
        }
    }

    public int getPhoneType() {
        if (this.mphone == null) {
            return -1;
        }
        return this.mphone.getPhoneType();
    }

    public String getLine1Number() {
        if (this.mphone == null) {
            return null;
        }
        return this.mphone.getLine1Number();
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        if (this.mphone != null) {
            this.mphone.setCallWaiting(enable, onComplete);
        }
    }

    public boolean isCspPlmnEnabled() {
        if (this.mphone == null) {
            return false;
        }
        return this.mphone.isCspPlmnEnabled();
    }

    public void selectNetworkManually(OperatorInfo opt, boolean bl, Message message) {
        if (this.mphone != null) {
            this.mphone.selectNetworkManually(opt, bl, message);
        }
    }

    public void supplyPin(String pin, Message callback) {
        if (this.mphone != null) {
            this.mphone.getIccCard().supplyPin(pin, callback);
        }
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        if (this.mphone != null) {
            this.mphone.setNetworkSelectionModeAutomatic(response);
        }
    }

    public void supplyPuk(String pin, String newPin, Message callback) {
        if (this.mphone != null) {
            this.mphone.getIccCard().supplyPuk(pin, newPin, callback);
        }
    }
}
