package com.huawei.distributedgw;

import java.util.Objects;

public class InternetSharingRequestEx {
    private int mDeviceType;
    private String mEntryIfaceName;
    private String mExitIfaceName;
    private String mRequestIp;

    public InternetSharingRequestEx(int deviceType, String entryIfaceName) {
        this.mDeviceType = deviceType;
        this.mEntryIfaceName = entryIfaceName;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public String getEntryIfaceName() {
        return this.mEntryIfaceName;
    }

    public String getExitIfaceName() {
        return this.mExitIfaceName;
    }

    public String getRequestIp() {
        return this.mRequestIp;
    }

    public void setDeviceType(int deviceType) {
        this.mDeviceType = deviceType;
    }

    public void setEntryIfaceName(String entryIfaceName) {
        this.mEntryIfaceName = entryIfaceName;
    }

    public void setExitIfaceName(String exitIfaceName) {
        this.mExitIfaceName = exitIfaceName;
    }

    public void setRequestIp(String requestIp) {
        this.mRequestIp = requestIp;
    }

    public String toString() {
        return "GatewayRequest [ mDeviceType=" + this.mDeviceType + " mEntryIfaceName=" + this.mEntryIfaceName + " mExitIfaceName=" + this.mExitIfaceName + " ]";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InternetSharingRequestEx)) {
            return false;
        }
        InternetSharingRequestEx that = (InternetSharingRequestEx) obj;
        if (that.mDeviceType != this.mDeviceType || !Objects.equals(that.mEntryIfaceName, this.mEntryIfaceName) || !Objects.equals(that.mExitIfaceName, this.mExitIfaceName) || !Objects.equals(that.mRequestIp, this.mRequestIp)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mDeviceType), this.mEntryIfaceName, this.mExitIfaceName, this.mRequestIp);
    }
}
