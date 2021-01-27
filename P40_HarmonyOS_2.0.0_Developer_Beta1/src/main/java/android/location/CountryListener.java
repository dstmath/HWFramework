package android.location;

import android.annotation.UnsupportedAppUsage;

public interface CountryListener {
    @UnsupportedAppUsage
    void onCountryDetected(Country country);
}
