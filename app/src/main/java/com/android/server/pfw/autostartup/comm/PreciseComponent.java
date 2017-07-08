package com.android.server.pfw.autostartup.comm;

import android.text.TextUtils;
import java.util.HashSet;
import java.util.Set;

public class PreciseComponent {
    public static final int COMP_TYPE_PROVIDER = 0;
    public static final int COMP_TYPE_RECEIVER = 1;
    public static final int COMP_TYPE_SERVICE = 2;
    public static final int INVALID_INT = -1;
    public static final int SCOPE_ALL = 0;
    public static final int SCOPE_INDIVIDUAL = 1;
    public static final int SCREEN_STATUS_ALL = 0;
    public static final int SCREEN_STATUS_ON = 1;
    private static final String TAG = "PreciseComponent";
    private String mKey;
    private Set<String> mRelatedPkgs;
    private int mScope;
    private int mScreenStatus;
    private int mType;

    public PreciseComponent(int type, String key, int scope, int screenStatus) {
        this.mType = type;
        this.mKey = key;
        this.mScope = scope;
        this.mScreenStatus = screenStatus;
        this.mRelatedPkgs = new HashSet();
    }

    public void addRelatedPkg(String pkg) {
        this.mRelatedPkgs.add(pkg);
    }

    public boolean valid() {
        return (validType() && validScope() && validScreenStatus()) ? validKey() : false;
    }

    public int getCompType() {
        return this.mType;
    }

    public boolean matchKey(String key) {
        return this.mKey.equals(key);
    }

    public boolean isScopeIndividual() {
        return this.mScope == SCREEN_STATUS_ON;
    }

    public boolean existInRelatedPkgs(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        return this.mRelatedPkgs.contains(pkg);
    }

    public void convertToAIDLObj() {
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(TAG).append(" type: ").append(this.mType);
        buf.append(", key: ").append(this.mKey);
        buf.append(", scope: ").append(this.mScope);
        buf.append(", screenStatus: ").append(this.mScreenStatus);
        buf.append(", relatedPkgs: ").append(this.mRelatedPkgs);
        return buf.toString();
    }

    private boolean validType() {
        return this.mType == 0 || SCREEN_STATUS_ON == this.mType || COMP_TYPE_SERVICE == this.mType;
    }

    private boolean validScope() {
        boolean z = false;
        if (this.mScope == 0) {
            return this.mRelatedPkgs.isEmpty();
        }
        if (SCREEN_STATUS_ON != this.mScope) {
            return false;
        }
        if (!this.mRelatedPkgs.isEmpty()) {
            z = true;
        }
        return z;
    }

    private boolean validScreenStatus() {
        return this.mScreenStatus == 0 || SCREEN_STATUS_ON == this.mScreenStatus;
    }

    private boolean validKey() {
        return !TextUtils.isEmpty(this.mKey) ? validServiceKey() : false;
    }

    private boolean validServiceKey() {
        boolean z = true;
        if (COMP_TYPE_SERVICE != this.mType) {
            return true;
        }
        if (this.mKey.indexOf("/") != this.mKey.lastIndexOf("/")) {
            z = false;
        }
        return z;
    }
}
