package com.huawei.android.location;

import android.content.Context;
import android.location.Country;
import android.location.CountryDetector;
import android.os.Looper;
import com.huawei.android.content.ContextEx;

public class CountryDetectorEx {
    private CountryDetector mCountryDetector = null;

    public CountryDetectorEx(Context context) {
        this.mCountryDetector = (CountryDetector) context.getSystemService(ContextEx.COUNTRY_DETECTOR);
    }

    public void addCountryListener(CountryListenerEx listener, Looper looper) {
        this.mCountryDetector.addCountryListener(listener.getCountryListener(), looper);
    }

    public String getCountryIso() {
        String countryIso = "";
        Country country = this.mCountryDetector.detectCountry();
        if (country != null) {
            return country.getCountryIso();
        }
        return countryIso;
    }

    public void removeCountryListener(CountryListenerEx listener) {
        this.mCountryDetector.removeCountryListener(listener.getCountryListener());
    }

    public boolean isCountryDetectorValid() {
        return this.mCountryDetector != null;
    }
}
