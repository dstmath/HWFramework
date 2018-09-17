package android.drm;

import java.util.HashMap;

public class DrmErrorEvent extends DrmEvent {
    public static final int TYPE_ACQUIRE_DRM_INFO_FAILED = 2008;
    public static final int TYPE_NOT_SUPPORTED = 2003;
    public static final int TYPE_NO_INTERNET_CONNECTION = 2005;
    public static final int TYPE_OUT_OF_MEMORY = 2004;
    public static final int TYPE_PROCESS_DRM_INFO_FAILED = 2006;
    public static final int TYPE_REMOVE_ALL_RIGHTS_FAILED = 2007;
    public static final int TYPE_RIGHTS_NOT_INSTALLED = 2001;
    public static final int TYPE_RIGHTS_RENEWAL_NOT_ALLOWED = 2002;

    public DrmErrorEvent(int uniqueId, int type, String message) {
        super(uniqueId, type, message);
        checkTypeValidity(type);
    }

    public DrmErrorEvent(int uniqueId, int type, String message, HashMap<String, Object> attributes) {
        super(uniqueId, type, message, attributes);
        checkTypeValidity(type);
    }

    private void checkTypeValidity(int type) {
        if (type < TYPE_RIGHTS_NOT_INSTALLED || type > TYPE_ACQUIRE_DRM_INFO_FAILED) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
