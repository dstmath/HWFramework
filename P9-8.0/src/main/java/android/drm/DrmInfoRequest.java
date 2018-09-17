package android.drm;

import android.net.ProxyInfo;
import java.util.HashMap;
import java.util.Iterator;

public class DrmInfoRequest {
    public static final String ACCOUNT_ID = "account_id";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    public static final int TYPE_REGISTRATION_INFO = 1;
    public static final int TYPE_RIGHTS_ACQUISITION_INFO = 3;
    public static final int TYPE_RIGHTS_ACQUISITION_PROGRESS_INFO = 4;
    public static final int TYPE_UNREGISTRATION_INFO = 2;
    private final int mInfoType;
    private final String mMimeType;
    private final HashMap<String, Object> mRequestInformation = new HashMap();

    public DrmInfoRequest(int infoType, String mimeType) {
        this.mInfoType = infoType;
        this.mMimeType = mimeType;
        if (!isValid()) {
            throw new IllegalArgumentException("infoType: " + infoType + "," + "mimeType: " + mimeType);
        }
    }

    public String getMimeType() {
        return this.mMimeType;
    }

    public int getInfoType() {
        return this.mInfoType;
    }

    public void put(String key, Object value) {
        this.mRequestInformation.put(key, value);
    }

    public Object get(String key) {
        return this.mRequestInformation.get(key);
    }

    public Iterator<String> keyIterator() {
        return this.mRequestInformation.keySet().iterator();
    }

    public Iterator<Object> iterator() {
        return this.mRequestInformation.values().iterator();
    }

    boolean isValid() {
        if (this.mMimeType == null || (this.mMimeType.equals(ProxyInfo.LOCAL_EXCL_LIST) ^ 1) == 0 || this.mRequestInformation == null) {
            return false;
        }
        return isValidType(this.mInfoType);
    }

    static boolean isValidType(int infoType) {
        switch (infoType) {
            case 1:
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }
}
