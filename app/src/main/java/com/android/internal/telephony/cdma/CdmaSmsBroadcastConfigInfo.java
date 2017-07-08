package com.android.internal.telephony.cdma;

public class CdmaSmsBroadcastConfigInfo {
    private int mFromServiceCategory;
    private int mLanguage;
    private boolean mSelected;
    private int mToServiceCategory;

    public CdmaSmsBroadcastConfigInfo(int fromServiceCategory, int toServiceCategory, int language, boolean selected) {
        this.mFromServiceCategory = fromServiceCategory;
        this.mToServiceCategory = toServiceCategory;
        this.mLanguage = language;
        this.mSelected = selected;
    }

    public int getFromServiceCategory() {
        return this.mFromServiceCategory;
    }

    public int getToServiceCategory() {
        return this.mToServiceCategory;
    }

    public int getLanguage() {
        return this.mLanguage;
    }

    public boolean isSelected() {
        return this.mSelected;
    }

    public String toString() {
        return "CdmaSmsBroadcastConfigInfo: Id [" + this.mFromServiceCategory + ", " + this.mToServiceCategory + "] " + (isSelected() ? "ENABLED" : "DISABLED");
    }
}
