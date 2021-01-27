package com.huawei.android.location;

import android.location.Country;

public class CountryExt {
    public static final int COUNTRY_SOURCE_LOCATION = 1;
    private Country mCountry;

    public static CountryExt from(String countryIso, int source) {
        CountryExt countryExt = new CountryExt();
        countryExt.setCountry(new Country(countryIso, source));
        return countryExt;
    }

    public static CountryExt from(Country country) {
        CountryExt countryExt = new CountryExt();
        countryExt.setCountry(country);
        return countryExt;
    }

    private void setCountry(Country country) {
        this.mCountry = country;
    }

    public String getCountryIso() {
        Country country = this.mCountry;
        if (country != null) {
            return country.getCountryIso();
        }
        return null;
    }
}
