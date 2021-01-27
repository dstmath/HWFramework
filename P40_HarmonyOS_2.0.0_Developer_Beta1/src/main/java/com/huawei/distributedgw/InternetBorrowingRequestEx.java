package com.huawei.distributedgw;

import java.util.Objects;

public class InternetBorrowingRequestEx {
    private String mIfaceName;
    private String mRouteIp;
    private String mServiceName;

    public InternetBorrowingRequestEx(String ifaceName, String routeIp) {
        this.mIfaceName = ifaceName;
        this.mRouteIp = routeIp;
    }

    public String getIfaceName() {
        return this.mIfaceName;
    }

    public void setIfaceName(String ifaceName) {
        this.mIfaceName = ifaceName;
    }

    public String getRouteIp() {
        return this.mRouteIp;
    }

    public void setRouteIp(String routeIp) {
        this.mRouteIp = routeIp;
    }

    public String getServiceName() {
        return this.mServiceName;
    }

    public void setServiceName(String serviceName) {
        this.mServiceName = serviceName;
    }

    public String toString() {
        return "gateway borrow [ mIfaceName=" + this.mIfaceName + " mServiceName=" + this.mServiceName + " ]";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InternetBorrowingRequestEx)) {
            return false;
        }
        InternetBorrowingRequestEx that = (InternetBorrowingRequestEx) obj;
        if (!Objects.equals(that.mIfaceName, this.mIfaceName) || !Objects.equals(that.mRouteIp, this.mRouteIp)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mIfaceName, this.mRouteIp);
    }
}
