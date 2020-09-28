package com.huawei.networkit.grs.local.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service {
    private List<CountryGroup> countryGroups = new ArrayList(16);
    private String name;
    private String routeBy;
    private Map<String, Serving> servings = new HashMap(16);

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public String getRouteBy() {
        return this.routeBy;
    }

    public void addServing(String groupId, Serving serving) {
        this.servings.put(groupId, serving);
    }

    public Serving getServing(String groupId) {
        return this.servings.get(groupId);
    }

    public void setRouteBy(String routeBy2) {
        this.routeBy = routeBy2;
    }

    public List<CountryGroup> getCountryGroups() {
        return this.countryGroups;
    }

    public void setCountryGroups(List<CountryGroup> countryGroups2) {
        this.countryGroups = countryGroups2;
    }
}
