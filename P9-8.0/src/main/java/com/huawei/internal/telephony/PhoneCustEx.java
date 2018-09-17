package com.huawei.internal.telephony;

import android.os.Message;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.huawei.android.util.NoExtAPIException;
import java.util.ArrayList;

public class PhoneCustEx {
    private static final int DISABLE = 0;
    private static final int ENABLE = 1;
    private static final String TAG = "PhoneCustEx";

    public static void getPOLCapability(Phone obj, Message onComplete) {
        obj.getPOLCapabilty(onComplete);
    }

    public static void getPreferedOperatorList(Phone obj, Message onComplete) {
        obj.getPreferedOperatorList(onComplete);
    }

    public static void setPOLEntry(Phone obj, int index, String numeric, int act, Message onComplete) {
        obj.setPOLEntry(index, numeric, act, onComplete);
    }

    public static void setPOLEntry(Phone obj, ArrayList<NetworkInfoWithActCustEx> list, Message onComplete) {
        if (list == null) {
            Log.e(TAG, "Network info List is empty!");
            return;
        }
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            NetworkInfoWithActCustEx ni = (NetworkInfoWithActCustEx) list.get(i);
            if (ni == null) {
                Log.e(TAG, "Network info Item is empty! Index : " + i);
            } else if (i != list.size() - 1) {
                obj.setPOLEntry(ni.getPriority(), ni.getOperatorNumeric(), ni.getAccessTechnology(), null);
            } else {
                obj.setPOLEntry(ni.getPriority(), ni.getOperatorNumeric(), ni.getAccessTechnology(), onComplete);
            }
        }
    }

    public static void setBypassEmergencyR(Phone obj, boolean on) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isBypassEmergencyR(Phone obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isIccCardStatusPowerSavingR(Phone obj, int sub) {
        throw new NoExtAPIException("method not supported.");
    }

    public void setLTEReleaseVersion(Phone obj, boolean state, Message response) {
        obj.setLTEReleaseVersion(state ? 1 : 0, response);
    }

    public static void riseCdmaCutoffFreq(Phone obj, boolean on) {
        obj.riseCdmaCutoffFreq(on);
    }

    public static void selectCsgNetworkManually(Phone obj, Message response) {
        obj.selectCsgNetworkManually(response);
    }

    public static void trigerCallLostEvent(Phone phone) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getLteReleaseVersion(Phone obj) {
        return obj.getLteReleaseVersion();
    }
}
