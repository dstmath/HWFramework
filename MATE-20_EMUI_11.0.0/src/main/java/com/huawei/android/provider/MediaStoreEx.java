package com.huawei.android.provider;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import com.huawei.annotation.HwSystemApi;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class MediaStoreEx {
    @HwSystemApi
    public static final String GET_STORAGE_ID_CALL = "get_storageId";
    public static final String IS_DRM = "is_drm";
    @HwSystemApi
    public static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    @HwSystemApi
    public static final String PARAM_LIMIT = "limit";
    @HwSystemApi
    public static final String UNKNOWN_STRING = "<unknown>";
    @HwSystemApi
    public static final String VOLUME_EXTERNAL = "external";
    @HwSystemApi
    public static final String VOLUME_EXTERNAL_PRIMARY = "external_primary";
    @HwSystemApi
    public static final String VOLUME_INTERNAL = "internal";

    public interface MediaColumns {
        public static final String ALBUM_SORT_INDEX = "album_sort_index";
        public static final String BUCKET_DISPLAY_NAME_ALIAS = "bucket_display_name_alias";
        public static final String CAMERA_PERCEPTION = "cam_perception";
        public static final String HW_IMAGE_REFOCUS = "hw_image_refocus";
        public static final String HW_RECTIFY_OFFSET = "hw_rectify_offset";
        public static final String HW_VOICE_OFFSET = "hw_voice_offset";
        public static final String IS_HW_BURST = "is_hw_burst";
        public static final String IS_HW_FAVORITE = "is_hw_favorite";
        public static final String IS_HW_PRIVACY = "is_hw_privacy";
        public static final String SECURITY_CAMERA_LAUNCHER_TIME = "security_camera_launcher_time";
        public static final String SPECIAL_FILE_OFFSET = "special_file_offset";
        public static final String SPECIAL_FILE_TYPE = "special_file_type";
    }

    @HwSystemApi
    public static String getVolumeName(File file) {
        return MediaStore.getVolumeName(file);
    }

    @HwSystemApi
    public static Set<String> getExternalVolumeNames(Context context) {
        return MediaStore.getExternalVolumeNames(context);
    }

    @HwSystemApi
    public static File getVolumePath(String volumeName) throws FileNotFoundException {
        return MediaStore.getVolumePath(volumeName);
    }

    @HwSystemApi
    public static Collection<File> getVolumeScanPaths(String volumeName) throws FileNotFoundException {
        return MediaStore.getVolumeScanPaths(volumeName);
    }

    @HwSystemApi
    public static Uri setIncludePending(Uri uri) {
        return MediaStore.setIncludePending(uri.buildUpon()).build();
    }

    @HwSystemApi
    public static Uri setIncludeTrashed(Uri uri) {
        return MediaStore.setIncludeTrashed(uri);
    }

    @HwSystemApi
    public static StorageVolume[] getVolumeList(int userId, int flags) {
        return StorageManager.getVolumeList(userId, flags);
    }

    private static boolean isClonedProfile(UserInfo ui) {
        if (ui == null || ui.id == ActivityManager.getCurrentUser() || ui.isManagedProfile()) {
            return false;
        }
        return true;
    }

    private static UserInfo getAppClonedUserInfo(Context context) {
        if (!IS_SUPPORT_CLONE_APP) {
            return null;
        }
        List<UserInfo> profiles = ((UserManager) context.getSystemService("user")).getProfiles(context.getUserId());
        int uiCount = profiles == null ? 0 : profiles.size();
        for (int i = 0; i < uiCount; i++) {
            UserInfo ui = profiles.get(i);
            if (isClonedProfile(ui)) {
                return ui;
            }
        }
        return null;
    }

    @HwSystemApi
    public static StorageVolume[] getAppClonedUserVolumes(Context context) {
        StorageVolume[] storageVolumes = null;
        UserInfo ui = getAppClonedUserInfo(context);
        if (ui != null) {
            StorageManager storageManager = (StorageManager) context.getSystemService("storage");
            storageVolumes = StorageManager.getVolumeList(ui.id, 512);
        }
        if (storageVolumes != null) {
            return storageVolumes;
        }
        return new StorageVolume[0];
    }

    public static final class Files {

        @HwSystemApi
        public interface FileColumns extends MediaColumns {
            public static final String STORAGE_ID = "storage_id";
        }

        public static final String getStorageIdInFileColumns() {
            return FileColumns.STORAGE_ID;
        }

        public static final String getFormatInFileColumns() {
            return "format";
        }
    }
}
