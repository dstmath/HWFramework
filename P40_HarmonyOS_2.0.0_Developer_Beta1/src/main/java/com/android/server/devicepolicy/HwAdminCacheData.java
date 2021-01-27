package com.android.server.devicepolicy;

import android.os.Bundle;
import java.util.HashMap;
import java.util.List;

public class HwAdminCacheData {
    private HashMap<Integer, Boolean> booleanMap;
    private HashMap<String, Bundle> bundleMap;
    private HashMap<Integer, List<String>> listMap;

    public HwAdminCacheData() {
        this.bundleMap = null;
        this.booleanMap = null;
        this.listMap = null;
        this.bundleMap = new HashMap<>();
        this.booleanMap = new HashMap<>();
        this.listMap = new HashMap<>();
    }

    public HashMap<Integer, Boolean> getBooleanMap() {
        return this.booleanMap;
    }

    public HashMap<String, Bundle> getBundleMap() {
        return this.bundleMap;
    }

    public HashMap<Integer, List<String>> getListMap() {
        return this.listMap;
    }
}
