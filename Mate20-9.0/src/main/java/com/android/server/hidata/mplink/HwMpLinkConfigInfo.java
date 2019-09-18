package com.android.server.hidata.mplink;

public class HwMpLinkConfigInfo {
    private String mAppName;
    private String mCondition;
    private String mCustMac;
    private String mEncryptType;
    private String mGatewayType;
    private String mMultNetwork;
    private String mReserved;
    private String mVendorOui;

    public String getCondition() {
        return this.mCondition;
    }

    public void setCondition(String mCondition2) {
        this.mCondition = mCondition2;
    }

    public String getmReserved() {
        return this.mReserved;
    }

    public String getVendorOui() {
        return this.mVendorOui;
    }

    public void setmVendorOui(String mVendorOui2) {
        this.mVendorOui = mVendorOui2;
    }

    public String getCustMac() {
        return this.mCustMac;
    }

    public void setmCustMac(String mCustMac2) {
        this.mCustMac = mCustMac2;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public void setmAppName(String mAppName2) {
        this.mAppName = mAppName2;
    }

    public String getMultNetwork() {
        return this.mMultNetwork;
    }

    public void setmMultNetwork(String mMultNetwork2) {
        this.mMultNetwork = mMultNetwork2;
    }

    public String getGatewayType() {
        return this.mGatewayType;
    }

    public void setmGatewayType(String mGatewayType2) {
        this.mGatewayType = mGatewayType2;
    }

    public String getEncryptType() {
        return this.mEncryptType;
    }

    public void setmEncryptType(String mEncryptType2) {
        this.mEncryptType = mEncryptType2;
    }

    public String getReserved() {
        return this.mReserved;
    }

    public void setmReserved(String mReserved2) {
        this.mReserved = mReserved2;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("VendorOui: ");
        sb.append(this.mVendorOui);
        sb.append(" ,MultNetwork: ");
        sb.append(this.mMultNetwork);
        sb.append(" ,GatewayType: ");
        sb.append(this.mGatewayType);
        sb.append(" ,CustMac: ");
        sb.append(this.mCustMac);
        sb.append(" ,Reserved: ");
        sb.append(this.mReserved);
        sb.append(" ,EncryptType: ");
        sb.append(this.mEncryptType);
        sb.append(" ,Condition: ");
        sb.append(this.mCondition);
        return sb.toString();
    }
}
