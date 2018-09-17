package android.drm;

import android.net.ProxyInfo;
import java.util.HashMap;

public class DrmEvent {
    public static final String DRM_INFO_OBJECT = "drm_info_object";
    public static final String DRM_INFO_STATUS_OBJECT = "drm_info_status_object";
    public static final int TYPE_ALL_RIGHTS_REMOVED = 1001;
    public static final int TYPE_DRM_INFO_PROCESSED = 1002;
    private HashMap<String, Object> mAttributes = new HashMap();
    private String mMessage = ProxyInfo.LOCAL_EXCL_LIST;
    private final int mType;
    private final int mUniqueId;

    protected DrmEvent(int uniqueId, int type, String message, HashMap<String, Object> attributes) {
        this.mUniqueId = uniqueId;
        this.mType = type;
        if (message != null) {
            this.mMessage = message;
        }
        if (attributes != null) {
            this.mAttributes = attributes;
        }
    }

    protected DrmEvent(int uniqueId, int type, String message) {
        this.mUniqueId = uniqueId;
        this.mType = type;
        if (message != null) {
            this.mMessage = message;
        }
    }

    public int getUniqueId() {
        return this.mUniqueId;
    }

    public int getType() {
        return this.mType;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public Object getAttribute(String key) {
        return this.mAttributes.get(key);
    }
}
