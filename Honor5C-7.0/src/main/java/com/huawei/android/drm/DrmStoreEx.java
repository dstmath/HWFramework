package com.huawei.android.drm;

public class DrmStoreEx {

    public static class Action {
        public static final int ACTION_MASK = 255;
        public static final int SHOW_DIALOG = 256;
    }

    public interface ConstraintsColumns {
        public static final String IS_AUTO_USE = "is_auto_use";
        public static final String RIGHTS_COUNT = "rights_count";
    }

    public static class DrmObjectType {
        public static final int DRM_COMBINED_DELIVERY = 6;
        public static final int DRM_FORWARD_LOCK = 5;
        public static final int DRM_SEPARATE_DELIVERY = 7;
        public static final int DRM_SEPARATE_DELIVERY_SF = 8;
        public static final int DRM_UNKNOWN = 4;
    }
}
