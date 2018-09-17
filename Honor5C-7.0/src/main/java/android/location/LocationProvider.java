package android.location;

import com.android.internal.location.ProviderProperties;

public class LocationProvider {
    public static final int AVAILABLE = 2;
    public static final String BAD_CHARS_REGEX = "[^a-zA-Z0-9]";
    public static final int OUT_OF_SERVICE = 0;
    public static final int TEMPORARILY_UNAVAILABLE = 1;
    private final String mName;
    private final ProviderProperties mProperties;

    public LocationProvider(String name, ProviderProperties properties) {
        if (name.matches(BAD_CHARS_REGEX)) {
            throw new IllegalArgumentException("provider name contains illegal character: " + name);
        }
        this.mName = name;
        this.mProperties = properties;
    }

    public String getName() {
        return this.mName;
    }

    public boolean meetsCriteria(Criteria criteria) {
        return propertiesMeetCriteria(this.mName, this.mProperties, criteria);
    }

    public static boolean propertiesMeetCriteria(String name, ProviderProperties properties, Criteria criteria) {
        if (LocationManager.PASSIVE_PROVIDER.equals(name) || properties == null) {
            return false;
        }
        if (criteria.getAccuracy() != 0 && criteria.getAccuracy() < properties.mAccuracy) {
            return false;
        }
        if (criteria.getPowerRequirement() != 0 && criteria.getPowerRequirement() < properties.mPowerRequirement) {
            return false;
        }
        if (criteria.isAltitudeRequired() && !properties.mSupportsAltitude) {
            return false;
        }
        if (criteria.isSpeedRequired() && !properties.mSupportsSpeed) {
            return false;
        }
        if (criteria.isBearingRequired() && !properties.mSupportsBearing) {
            return false;
        }
        if (criteria.isCostAllowed() || !properties.mHasMonetaryCost) {
            return true;
        }
        return false;
    }

    public boolean requiresNetwork() {
        return this.mProperties.mRequiresNetwork;
    }

    public boolean requiresSatellite() {
        return this.mProperties.mRequiresSatellite;
    }

    public boolean requiresCell() {
        return this.mProperties.mRequiresCell;
    }

    public boolean hasMonetaryCost() {
        return this.mProperties.mHasMonetaryCost;
    }

    public boolean supportsAltitude() {
        return this.mProperties.mSupportsAltitude;
    }

    public boolean supportsSpeed() {
        return this.mProperties.mSupportsSpeed;
    }

    public boolean supportsBearing() {
        return this.mProperties.mSupportsBearing;
    }

    public int getPowerRequirement() {
        return this.mProperties.mPowerRequirement;
    }

    public int getAccuracy() {
        return this.mProperties.mAccuracy;
    }
}
