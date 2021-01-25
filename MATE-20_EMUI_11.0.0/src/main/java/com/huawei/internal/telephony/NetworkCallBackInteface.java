package com.huawei.internal.telephony;

public interface NetworkCallBackInteface {
    public static final int MANUAL_SEARCH_NETWORK_FAIL = 1;
    public static final int MANUAL_SEARCH_NETWORK_SUCCESS = 0;

    void endUploadAvailableNetworks();

    void endUploadAvailableNetworks(int i);

    void showAvailableNetworks(Object obj);
}
