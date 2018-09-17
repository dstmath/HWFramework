package android.drm;

import android.net.ProxyInfo;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DrmRights {
    private String mAccountId;
    private byte[] mData;
    private String mMimeType;
    private String mSubscriptionId;

    public DrmRights(String rightsFilePath, String mimeType) {
        instantiate(new File(rightsFilePath), mimeType);
    }

    public DrmRights(String rightsFilePath, String mimeType, String accountId) {
        this(rightsFilePath, mimeType);
        this.mAccountId = accountId;
    }

    public DrmRights(String rightsFilePath, String mimeType, String accountId, String subscriptionId) {
        this(rightsFilePath, mimeType);
        this.mAccountId = accountId;
        this.mSubscriptionId = subscriptionId;
    }

    public DrmRights(File rightsFile, String mimeType) {
        instantiate(rightsFile, mimeType);
    }

    private void instantiate(File rightsFile, String mimeType) {
        try {
            this.mData = DrmUtils.readBytes(rightsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mMimeType = mimeType;
        if (!isValid()) {
            throw new IllegalArgumentException("mimeType: " + this.mMimeType + "," + "data: " + Arrays.toString(this.mData));
        }
    }

    public DrmRights(ProcessedData data, String mimeType) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        this.mData = data.getData();
        this.mAccountId = data.getAccountId();
        this.mSubscriptionId = data.getSubscriptionId();
        this.mMimeType = mimeType;
        if (!isValid()) {
            throw new IllegalArgumentException("mimeType: " + this.mMimeType + "," + "data: " + Arrays.toString(this.mData));
        }
    }

    public byte[] getData() {
        return this.mData;
    }

    public String getMimeType() {
        return this.mMimeType;
    }

    public String getAccountId() {
        return this.mAccountId;
    }

    public String getSubscriptionId() {
        return this.mSubscriptionId;
    }

    boolean isValid() {
        if (this.mMimeType == null || (this.mMimeType.equals(ProxyInfo.LOCAL_EXCL_LIST) ^ 1) == 0 || this.mData == null || this.mData.length <= 0) {
            return false;
        }
        return true;
    }
}
