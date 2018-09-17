package com.android.server.pm;

import java.util.ArrayList;

public class DefaultAppPermission {
    ArrayList<DefaultPermissionGroup> mGrantedGroups;
    String mPackageName;
    boolean mTrust;

    public static class DefaultPermissionGroup {
        boolean mGrant;
        String mName;
        boolean mSystemFixed;

        public DefaultPermissionGroup(String name) {
            this(name, true, true);
        }

        public DefaultPermissionGroup(String name, boolean grant, boolean fixed) {
            this.mName = name;
            this.mGrant = grant;
            this.mSystemFixed = fixed;
        }

        public String toString() {
            return "Group " + this.mName + " grant:" + this.mGrant + " fixed:" + this.mSystemFixed;
        }
    }

    public DefaultAppPermission() {
        this.mGrantedGroups = new ArrayList();
    }

    public String toString() {
        return "Permission for " + this.mPackageName + ", mTrust:" + this.mTrust;
    }
}
