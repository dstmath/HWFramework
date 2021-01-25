package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;

/* compiled from: CommandDetails */
class DeviceIdentities extends ValueObject {
    @UnsupportedAppUsage
    public int destinationId;
    public int sourceId;

    DeviceIdentities() {
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.ValueObject
    public ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.DEVICE_IDENTITIES;
    }
}
