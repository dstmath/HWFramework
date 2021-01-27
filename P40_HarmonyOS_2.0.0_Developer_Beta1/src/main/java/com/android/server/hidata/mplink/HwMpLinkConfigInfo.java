package com.android.server.hidata.mplink;

public class HwMpLinkConfigInfo {
    private static final int DEFAULT_CAPACITY = 16;
    private String mAppName;
    private String mCondition;
    private String mCustMac;
    private String mEncryptType;
    private String mGatewayType;
    private String mMultiNetwork;
    private String mReserved;
    private String mVendorOui;

    public String getCondition() {
        return this.mCondition;
    }

    public void setCondition(String mCondition2) {
        this.mCondition = mCondition2;
    }

    public String getVendorOui() {
        return this.mVendorOui;
    }

    public void setVendorOui(String mVendorOui2) {
        this.mVendorOui = mVendorOui2;
    }

    public String getCustMac() {
        return this.mCustMac;
    }

    public void setCustMac(String mCustMac2) {
        this.mCustMac = mCustMac2;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public void setAppName(String mAppName2) {
        this.mAppName = mAppName2;
    }

    public String getMultNetwork() {
        return this.mMultiNetwork;
    }

    public void setMultiNetwork(String mMultiNetwork2) {
        this.mMultiNetwork = mMultiNetwork2;
    }

    public String getGatewayType() {
        return this.mGatewayType;
    }

    public void setGatewayType(String mGatewayType2) {
        this.mGatewayType = mGatewayType2;
    }

    public String getEncryptType() {
        return this.mEncryptType;
    }

    public void setEncryptType(String mEncryptType2) {
        this.mEncryptType = mEncryptType2;
    }

    public String getReserved() {
        return this.mReserved;
    }

    public void setReserved(String mReserved2) {
        this.mReserved = mReserved2;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(16);
        buffer.append("VendorOui: ");
        buffer.append(this.mVendorOui);
        buffer.append(" ,MultNetwork: ");
        buffer.append(this.mMultiNetwork);
        buffer.append(" ,GatewayType: ");
        buffer.append(this.mGatewayType);
        buffer.append(" ,CustMac: ");
        buffer.append(this.mCustMac);
        buffer.append(" ,Reserved: ");
        buffer.append(this.mReserved);
        buffer.append(" ,EncryptType: ");
        buffer.append(this.mEncryptType);
        buffer.append(" ,Condition: ");
        buffer.append(this.mCondition);
        return buffer.toString();
    }
}
