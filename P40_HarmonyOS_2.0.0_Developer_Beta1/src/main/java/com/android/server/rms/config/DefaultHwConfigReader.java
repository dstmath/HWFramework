package com.android.server.rms.config;

import android.content.Context;
import android.rms.config.ResourceConfig;

public class DefaultHwConfigReader {
    public boolean updateResConfig(Context context) {
        return false;
    }

    public boolean loadResConfig(Context context) {
        return false;
    }

    public String getWhiteList(int groupID, int type) {
        return null;
    }

    public ResourceConfig getResConfig(int groupID, int subType) {
        return null;
    }

    public int getSubTypeNum(int groupID) {
        return 0;
    }

    public int getResourceThreshold(int groupID, int subType) {
        return -1;
    }

    public int getResourceStrategy(int groupID, int subType) {
        return -1;
    }
}
