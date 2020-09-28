package com.android.internal.telephony.cdnr;

import java.util.Arrays;
import java.util.List;

public final class BrandOverrideEfData implements EfData {
    private final String mRegisteredPlmn;
    private final String mSpn;

    public BrandOverrideEfData(String operatorName, String registeredPlmn) {
        this.mSpn = operatorName;
        this.mRegisteredPlmn = registeredPlmn;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public String getServiceProviderName() {
        return this.mSpn;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public int getServiceProviderNameDisplayCondition() {
        return 0;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public List<String> getServiceProviderDisplayInformation() {
        return Arrays.asList(this.mRegisteredPlmn);
    }
}
