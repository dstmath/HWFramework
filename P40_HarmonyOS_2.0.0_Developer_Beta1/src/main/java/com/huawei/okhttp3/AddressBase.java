package com.huawei.okhttp3;

@Deprecated
public class AddressBase {
    private String headerHostField = null;

    public String headerHost() {
        return this.headerHostField;
    }

    public void setHeaderHost(String headerHost) {
        this.headerHostField = headerHost;
    }
}
