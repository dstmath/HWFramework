package com.android.server.om;

public final class DumpState {
    private String mField;
    private String mPackageName;
    private int mUserId = -1;
    private boolean mVerbose;

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setField(String field) {
        this.mField = field;
    }

    public String getField() {
        return this.mField;
    }

    public void setVerbose(boolean verbose) {
        this.mVerbose = verbose;
    }

    public boolean isVerbose() {
        return this.mVerbose;
    }
}
