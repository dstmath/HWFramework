package com.android.server.pm;

import java.util.ArrayList;

public class DefaultAppPermission {
    ArrayList<DefaultPermissionGroup> mGrantedGroups = new ArrayList();
    ArrayList<DefaultPermissionSingle> mGrantedSingles = new ArrayList();
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

    public static class DefaultPermissionSingle {
        boolean mGrant;
        String mName;
        boolean mSystemFixed;

        public DefaultPermissionSingle(String name) {
            this(name, true, true);
        }

        public DefaultPermissionSingle(String name, boolean grant, boolean fixed) {
            this.mName = name;
            this.mGrant = grant;
            this.mSystemFixed = fixed;
        }

        public String toString() {
            return "Single Permission " + this.mName + " grant:" + this.mGrant + " fixed:" + this.mSystemFixed;
        }
    }

    public String toString() {
        return "Permission for " + this.mPackageName + ", mTrust:" + this.mTrust;
    }
}
