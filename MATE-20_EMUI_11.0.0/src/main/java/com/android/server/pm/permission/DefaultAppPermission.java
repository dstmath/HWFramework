package com.android.server.pm.permission;

import java.util.ArrayList;

public class DefaultAppPermission {
    ArrayList<DefaultPermissionGroup> mGrantedGroups = new ArrayList<>();
    ArrayList<DefaultPermissionSingle> mGrantedSingles = new ArrayList<>();
    boolean mIsTrust;
    String mPackageName;

    public static class DefaultPermissionGroup {
        boolean mIsGrant;
        boolean mIsSystemFixed;
        String mName;

        public DefaultPermissionGroup(String name) {
            this(name, true, true);
        }

        public DefaultPermissionGroup(String name, boolean isGrant, boolean isFixed) {
            this.mName = name;
            this.mIsGrant = isGrant;
            this.mIsSystemFixed = isFixed;
        }

        public String toString() {
            return "Group " + this.mName + " grant:" + this.mIsGrant + " fixed:" + this.mIsSystemFixed;
        }
    }

    public static class DefaultPermissionSingle {
        boolean mIsGrant;
        boolean mIsSystemFixed;
        String mName;

        public DefaultPermissionSingle(String name) {
            this(name, true, true);
        }

        public DefaultPermissionSingle(String name, boolean isGrant, boolean isFixed) {
            this.mName = name;
            this.mIsGrant = isGrant;
            this.mIsSystemFixed = isFixed;
        }

        public String toString() {
            return "Single Permission " + this.mName + " grant:" + this.mIsGrant + " fixed:" + this.mIsSystemFixed;
        }
    }

    public String toString() {
        return "Permission for " + this.mPackageName + ", mIsTrust:" + this.mIsTrust;
    }
}
