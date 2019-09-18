package com.android.location.provider;

import com.android.internal.location.ProviderProperties;

public final class ProviderPropertiesUnbundled {
    private final ProviderProperties mProperties;

    public static ProviderPropertiesUnbundled create(boolean requiresNetwork, boolean requiresSatellite, boolean requiresCell, boolean hasMonetaryCost, boolean supportsAltitude, boolean supportsSpeed, boolean supportsBearing, int powerRequirement, int accuracy) {
        ProviderProperties providerProperties = new ProviderProperties(requiresNetwork, requiresSatellite, requiresCell, hasMonetaryCost, supportsAltitude, supportsSpeed, supportsBearing, powerRequirement, accuracy);
        return new ProviderPropertiesUnbundled(providerProperties);
    }

    private ProviderPropertiesUnbundled(ProviderProperties properties) {
        this.mProperties = properties;
    }

    public ProviderProperties getProviderProperties() {
        return this.mProperties;
    }

    public String toString() {
        return this.mProperties.toString();
    }
}
