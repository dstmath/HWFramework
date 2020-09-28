package com.huawei.networkit.grs.local.model;

import java.util.Set;

public class CountryGroup {
    private Set<String> countries;
    private String description;
    private String id;
    private String name;

    public String getId() {
        return this.id;
    }

    public void setId(String id2) {
        this.id = id2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public Set<String> getCountries() {
        return this.countries;
    }

    public void setCountries(Set<String> countries2) {
        this.countries = countries2;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description2) {
        this.description = description2;
    }
}
