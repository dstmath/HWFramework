package com.android.internal.telephony.cdnr;

import android.text.TextUtils;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import java.util.Arrays;
import java.util.List;

public final class UsimEfData implements EfData {
    private final SIMRecords mUsim;

    public UsimEfData(SIMRecords usim) {
        this.mUsim = usim;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public String getServiceProviderName() {
        String spn = this.mUsim.getServiceProviderName();
        if (TextUtils.isEmpty(spn)) {
            return null;
        }
        return spn;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public int getServiceProviderNameDisplayCondition() {
        return this.mUsim.getCarrierNameDisplayCondition();
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public List<String> getServiceProviderDisplayInformation() {
        String[] spdi = this.mUsim.getServiceProviderDisplayInformation();
        if (spdi != null) {
            return Arrays.asList(spdi);
        }
        return null;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public List<String> getEhplmnList() {
        String[] ehplmns = this.mUsim.getEhplmns();
        if (ehplmns != null) {
            return Arrays.asList(ehplmns);
        }
        return null;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public List<IccRecords.PlmnNetworkName> getPlmnNetworkNameList() {
        String pnnHomeName = this.mUsim.getPnnHomeName();
        if (TextUtils.isEmpty(pnnHomeName)) {
            return null;
        }
        return Arrays.asList(new IccRecords.PlmnNetworkName(pnnHomeName, PhoneConfigurationManager.SSSS));
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public List<IccRecords.OperatorPlmnInfo> getOperatorPlmnList() {
        return null;
    }
}
