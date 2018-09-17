package com.huawei.android.provider;

public final class MediaStoreEx {
    public static final String IS_DRM = "is_drm";

    public static final class Files {
        public static final String getStorageIdInFileColumns() {
            return "storage_id";
        }

        public static final String getFormatInFileColumns() {
            return "format";
        }
    }

    public interface MediaColumns {
        public static final String ALBUM_SORT_INDEX = "album_sort_index";
        public static final String BUCKET_DISPLAY_NAME_ALIAS = "bucket_display_name_alias";
        public static final String HW_IMAGE_REFOCUS = "hw_image_refocus";
        public static final String HW_RECTIFY_OFFSET = "hw_rectify_offset";
        public static final String HW_VOICE_OFFSET = "hw_voice_offset";
        public static final String IS_HW_BURST = "is_hw_burst";
        public static final String IS_HW_FAVORITE = "is_hw_favorite";
        public static final String IS_HW_PRIVACY = "is_hw_privacy";
        public static final String SPECIAL_FILE_OFFSET = "special_file_offset";
        public static final String SPECIAL_FILE_TYPE = "special_file_type";
    }
}
