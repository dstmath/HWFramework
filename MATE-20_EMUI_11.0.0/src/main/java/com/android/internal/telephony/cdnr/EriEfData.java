package com.android.internal.telephony.cdnr;

public final class EriEfData implements EfData {
    private final String mEriText;

    public EriEfData(String eriText) {
        this.mEriText = eriText;
    }

    @Override // com.android.internal.telephony.cdnr.EfData
    public String getServiceProviderName() {
        return this.mEriText;
    }
}
