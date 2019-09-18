package com.android.internal.telephony.cat;

/* compiled from: CommandDetails */
class DeviceIdentities extends ValueObject {
    public int destinationId;
    public int sourceId;

    DeviceIdentities() {
    }

    /* access modifiers changed from: package-private */
    public ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.DEVICE_IDENTITIES;
    }
}
