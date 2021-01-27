package com.android.internal.telephony.cdnr;

import android.text.TextUtils;
import com.android.internal.telephony.uicc.RuimRecords;

public final class RuimEfData implements EfData {
    private static final int DEFAULT_CARRIER_NAME_DISPLAY_CONDITION_BITMASK = 0;
    private final RuimRecords mRuim;

    public RuimEfData(RuimRecords ruim) {
        this.mRuim = ruim;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public String getServiceProviderName() {
        String spn = this.mRuim.getServiceProviderName();
        if (TextUtils.isEmpty(spn)) {
            return null;
        }
        return spn;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public int getServiceProviderNameDisplayCondition() {
        if (this.mRuim.getCsimSpnDisplayCondition()) {
            return 2;
        }
        return 0;
    }
}
