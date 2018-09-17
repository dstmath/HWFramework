package com.android.server.wifi.aware;

import android.net.wifi.aware.Characteristics;
import android.os.Bundle;

public class Capabilities {
    public int maxAppInfoLen;
    public int maxConcurrentAwareClusters;
    public int maxExtendedServiceSpecificInfoLen;
    public int maxMatchFilterLen;
    public int maxNdiInterfaces;
    public int maxNdpSessions;
    public int maxPublishes;
    public int maxQueuedTransmitMessages;
    public int maxServiceNameLen;
    public int maxServiceSpecificInfoLen;
    public int maxSubscribeInterfaceAddresses;
    public int maxSubscribes;
    public int maxTotalMatchFilterLen;
    public int supportedCipherSuites;

    public Characteristics toPublicCharacteristics() {
        Bundle bundle = new Bundle();
        bundle.putInt("key_max_service_name_length", this.maxServiceNameLen);
        bundle.putInt("key_max_service_specific_info_length", this.maxServiceSpecificInfoLen);
        bundle.putInt("key_max_match_filter_length", this.maxMatchFilterLen);
        return new Characteristics(bundle);
    }

    public String toString() {
        return "Capabilities [maxConcurrentAwareClusters=" + this.maxConcurrentAwareClusters + ", maxPublishes=" + this.maxPublishes + ", maxSubscribes=" + this.maxSubscribes + ", maxServiceNameLen=" + this.maxServiceNameLen + ", maxMatchFilterLen=" + this.maxMatchFilterLen + ", maxTotalMatchFilterLen=" + this.maxTotalMatchFilterLen + ", maxServiceSpecificInfoLen=" + this.maxServiceSpecificInfoLen + ", maxExtendedServiceSpecificInfoLen=" + this.maxExtendedServiceSpecificInfoLen + ", maxNdiInterfaces=" + this.maxNdiInterfaces + ", maxNdpSessions=" + this.maxNdpSessions + ", maxAppInfoLen=" + this.maxAppInfoLen + ", maxQueuedTransmitMessages=" + this.maxQueuedTransmitMessages + ", maxSubscribeInterfaceAddresses=" + this.maxSubscribeInterfaceAddresses + ", supportedCipherSuites=" + this.supportedCipherSuites + "]";
    }
}
