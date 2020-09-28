package com.huawei.android.location;

import android.location.Country;
import android.location.CountryListener;

public class CountryListenerExt {
    private CountryListener mCountryListener = new CountryListener() {
        /* class com.huawei.android.location.CountryListenerExt.AnonymousClass1 */

        public void onCountryDetected(Country country) {
            if (country != null) {
                CountryListenerExt.this.onCountryDetected(CountryExt.from(country));
            }
        }
    };

    public void onCountryDetected(CountryExt country) {
    }

    public CountryListener getCountryListener() {
        return this.mCountryListener;
    }
}
