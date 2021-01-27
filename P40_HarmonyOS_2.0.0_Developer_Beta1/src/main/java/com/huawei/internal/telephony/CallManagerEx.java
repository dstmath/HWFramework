package com.huawei.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.util.Log;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.huawei.android.util.NoExtAPIException;
import java.util.ArrayList;
import java.util.List;

public final class CallManagerEx {

    public interface disconnectCallbackEx {
        void disconnectNotify(AsyncResult asyncResult);
    }

    public static final Phone getPhoneInCall(CallManager obj) {
        return CallManager.getInstance().getPhoneInCall();
    }

    public Phone getPhoneInCall(CallManager cm, int subscription) {
        return CallManager.getInstance().getPhoneInCall(subscription);
    }

    public static final Call.State getActiveFgCallState(CallManager cm, int subscription) {
        return CallManager.getInstance().getActiveFgCallState(subscription);
    }

    public static final boolean canConference(CallManager cm, Call heldCall, int subscription) {
        return CallManager.getInstance().canConference(heldCall, subscription);
    }

    public static final void setActiveSubscription(CallManager cm, int subscription) {
        CallManager.getInstance().getHwCallManagerEx().setActiveSubscription(subscription);
    }

    public static final int getActiveSubscription(CallManager cm) {
        return CallManager.getInstance().getHwCallManagerEx().getActiveSubscription();
    }

    public static final boolean hasActiveFgCall(CallManager cm, int subscription) {
        return CallManager.getInstance().hasActiveFgCall(subscription);
    }

    public static final boolean hasActiveBgCall(CallManager cm, int subscription) {
        return CallManager.getInstance().hasActiveBgCall(subscription);
    }

    public static final boolean hasActiveRingingCall(CallManager cm, int subscription) {
        return CallManager.getInstance().hasActiveRingingCall(subscription);
    }

    public static final Call getActiveFgCall(CallManager cm, int subscription) {
        return CallManager.getInstance().getActiveFgCall(subscription);
    }

    public static final Call getFirstActiveBgCall(CallManager cm, int subscription) {
        return CallManager.getInstance().getFirstActiveBgCall(subscription);
    }

    public static final Call getFirstActiveRingingCall(CallManager cm) {
        return CallManager.getInstance().getFirstActiveRingingCall();
    }

    public static final Call getFirstActiveRingingCall(CallManager cm, int subscription) {
        return CallManager.getInstance().getFirstActiveRingingCall(subscription);
    }

    public static final Phone getBgPhone(CallManager cm, int subscription) {
        try {
            return CallManager.getInstance().getBgPhone(subscription);
        } catch (Exception e) {
            Log.d("CallManagerEx", "getBgPhone is error");
            return null;
        }
    }

    public static final Phone getFgPhone(CallManager cm, int subscription) {
        if (cm == null) {
            return null;
        }
        return CallManager.getInstance().getFgPhone(subscription);
    }

    public static final Connection dial(CallManager cm, Phone phone, String dialString, int callType, String[] extras) throws CallStateException {
        throw new NoExtAPIException("method not supported.");
    }

    public static final void registerForSubscriptionChange(CallManager cm, Handler h, int what, Object obj) {
        CallManager.getInstance().getHwCallManagerEx().registerForSubscriptionChange(h, what, obj);
    }

    public static final void unregisterForSubscriptionChange(CallManager cm, Handler h) {
        CallManager.getInstance().getHwCallManagerEx().unregisterForSubscriptionChange(h);
    }

    public static final PhoneConstants.State getState(CallManager cm, int subscription) {
        return CallManager.getInstance().getState(subscription);
    }

    public static boolean isCallOnImsEnabled() {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isCallOnCsvtEnabled() {
        throw new NoExtAPIException("method not supported.");
    }

    public static void acceptCall(CallManager cm, Call ringingCall, int callType) throws CallStateException {
        CallManager.getInstance().acceptCall(ringingCall);
    }

    public static boolean getCdmaFlashHold(CallManager cm) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setCdmaFlashHold(CallManager cm, boolean isHold) {
        throw new NoExtAPIException("method not supported.");
    }

    public static Phone getCDMAPhone(CallManager cm) {
        for (Phone phone : cm.getAllPhones()) {
            if (phone.getPhoneType() == 2) {
                return phone;
            }
        }
        return null;
    }

    public static Phone getGSMPhone(CallManager cm) {
        for (Phone phone : cm.getAllPhones()) {
            if (phone.getPhoneType() == 1) {
                return phone;
            }
        }
        return null;
    }

    public static void registerForLineControlInfo(CallManager cm, Handler h, int what, Object obj) {
        CallManager.getInstance().registerForLineControlInfo(h, what, obj);
    }

    public static void unregisterForLineControlInfo(CallManager cm, Handler h) {
        CallManager.getInstance().unregisterForLineControlInfo(h);
    }

    public static PhoneEx getFgPhone() {
        return new PhoneEx(CallManager.getInstance().getFgPhone());
    }

    public static PhoneEx getFgPhone(int subscription) {
        return new PhoneEx(CallManager.getInstance().getFgPhone(subscription));
    }

    public static void registerForPreciseCallStateChanged(Handler h, int what, Object obj) {
        CallManager.getInstance().registerForPreciseCallStateChanged(h, what, obj);
    }

    public static void unregisterForPreciseCallStateChanged(Handler h) {
        CallManager.getInstance().unregisterForPreciseCallStateChanged(h);
    }

    public static void registerForDisconnect(Handler h, int what, Object obj) {
        CallManager.getInstance().registerForDisconnect(h, what, obj);
    }

    public static void unregisterForDisconnect(Handler h) {
        CallManager.getInstance().unregisterForDisconnect(h);
    }

    public static boolean hasActiveFgCall() {
        return CallManager.getInstance().hasActiveFgCall();
    }

    public static CallEx getActiveFgCall() {
        return new CallEx(CallManager.getInstance().getActiveFgCall());
    }

    public static CallEx getActiveFgCall(int subId) {
        return new CallEx(CallManager.getInstance().getActiveFgCall(subId));
    }

    public static List<CallEx> getBackgroundCalls() {
        List<Call> callList = CallManager.getInstance().getBackgroundCalls();
        if (callList == null) {
            return null;
        }
        List<CallEx> callExList = new ArrayList<>();
        int listSize = callList.size();
        for (int i = 0; i < listSize; i++) {
            callExList.add(new CallEx(callList.get(i)));
        }
        return callExList;
    }

    public static List<CallEx> getForegroundCalls() {
        List<Call> callList = CallManager.getInstance().getForegroundCalls();
        if (callList == null) {
            return null;
        }
        List<CallEx> callExList = new ArrayList<>();
        int listSize = callList.size();
        for (int i = 0; i < listSize; i++) {
            callExList.add(new CallEx(callList.get(i)));
        }
        return callExList;
    }
}
