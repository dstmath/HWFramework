package com.huawei.networkit.grs.local.model;

import java.util.Map;

public class Serving {
    private Map<String, String> addresses;
    private String countryGroup;

    public String getCountryGroup() {
        return this.countryGroup;
    }

    public void setCountryGroup(String countryGroup2) {
        this.countryGroup = countryGroup2;
    }

    public Map<String, String> getAddresses() {
        return this.addresses;
    }

    public void setAddresses(Map<String, String> addresses2) {
        this.addresses = addresses2;
    }
}
