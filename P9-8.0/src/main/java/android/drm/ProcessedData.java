package android.drm;

import android.net.ProxyInfo;

public class ProcessedData {
    private String mAccountId = "_NO_USER";
    private final byte[] mData;
    private String mSubscriptionId = ProxyInfo.LOCAL_EXCL_LIST;

    ProcessedData(byte[] data, String accountId) {
        this.mData = data;
        this.mAccountId = accountId;
    }

    ProcessedData(byte[] data, String accountId, String subscriptionId) {
        this.mData = data;
        this.mAccountId = accountId;
        this.mSubscriptionId = subscriptionId;
    }

    public byte[] getData() {
        return this.mData;
    }

    public String getAccountId() {
        return this.mAccountId;
    }

    public String getSubscriptionId() {
        return this.mSubscriptionId;
    }
}
