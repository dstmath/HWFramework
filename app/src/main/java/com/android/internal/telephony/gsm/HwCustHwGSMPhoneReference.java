package com.android.internal.telephony.gsm;

import android.os.Message;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.OperatorInfo;
import java.util.ArrayList;

public class HwCustHwGSMPhoneReference {
    GsmCdmaPhone mPhone;

    public HwCustHwGSMPhoneReference(GsmCdmaPhone mGSMPhone) {
        this.mPhone = mGSMPhone;
    }

    public String getCustOperatorNameBySpn(String rplmn, String tempName) {
        return tempName;
    }

    public String modifyTheFormatName(String rplmn, String tempName, String radioTechStr) {
        return tempName.concat(radioTechStr);
    }

    public ArrayList<OperatorInfo> filterActAndRepeatedItems(ArrayList<OperatorInfo> searchResult) {
        return searchResult;
    }

    public void selectCsgNetworkManually(Message response) {
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
    }

    public void registerForCsgRecordsLoadedEvent() {
    }

    public void unregisterForCsgRecordsLoadedEvent() {
    }
}
