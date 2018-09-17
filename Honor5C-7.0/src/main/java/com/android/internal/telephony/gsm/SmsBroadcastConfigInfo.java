package com.android.internal.telephony.gsm;

public final class SmsBroadcastConfigInfo {
    private int mFromCodeScheme;
    private int mFromServiceId;
    private boolean mSelected;
    private int mToCodeScheme;
    private int mToServiceId;

    public SmsBroadcastConfigInfo(int fromId, int toId, int fromScheme, int toScheme, boolean selected) {
        this.mFromServiceId = fromId;
        this.mToServiceId = toId;
        this.mFromCodeScheme = fromScheme;
        this.mToCodeScheme = toScheme;
        this.mSelected = selected;
    }

    public void setFromServiceId(int fromServiceId) {
        this.mFromServiceId = fromServiceId;
    }

    public int getFromServiceId() {
        return this.mFromServiceId;
    }

    public void setToServiceId(int toServiceId) {
        this.mToServiceId = toServiceId;
    }

    public int getToServiceId() {
        return this.mToServiceId;
    }

    public void setFromCodeScheme(int fromCodeScheme) {
        this.mFromCodeScheme = fromCodeScheme;
    }

    public int getFromCodeScheme() {
        return this.mFromCodeScheme;
    }

    public void setToCodeScheme(int toCodeScheme) {
        this.mToCodeScheme = toCodeScheme;
    }

    public int getToCodeScheme() {
        return this.mToCodeScheme;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }

    public boolean isSelected() {
        return this.mSelected;
    }

    public String toString() {
        return "SmsBroadcastConfigInfo: Id [" + this.mFromServiceId + ',' + this.mToServiceId + "] Code [" + this.mFromCodeScheme + ',' + this.mToCodeScheme + "] " + (this.mSelected ? "ENABLED" : "DISABLED");
    }
}
