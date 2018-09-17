package com.google.android.mms.util;

import android.content.Context;
import android.drm.DrmManagerClient;
import android.util.Log;

public class DownloadDrmHelper {
    public static final String EXTENSION_DRM_MESSAGE = ".dm";
    public static final String EXTENSION_INTERNAL_FWDL = ".fl";
    public static final String MIMETYPE_DRM_MESSAGE = "application/vnd.oma.drm.message";
    private static final String TAG = "DownloadDrmHelper";

    public static boolean isDrmMimeType(Context context, String mimetype) {
        if (context == null) {
            return false;
        }
        try {
            DrmManagerClient drmClient = new DrmManagerClient(context);
            if (drmClient == null || mimetype == null || mimetype.length() <= 0) {
                return false;
            }
            return drmClient.canHandle("", mimetype);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "DrmManagerClient instance could not be created, context is Illegal.");
            return false;
        } catch (IllegalStateException e2) {
            Log.w(TAG, "DrmManagerClient didn't initialize properly.");
            return false;
        }
    }

    public static boolean isDrmConvertNeeded(String mimetype) {
        return "application/vnd.oma.drm.message".equals(mimetype);
    }

    public static String modifyDrmFwLockFileExtension(String filename) {
        if (filename == null) {
            return filename;
        }
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex != -1) {
            filename = filename.substring(0, extensionIndex);
        }
        return filename.concat(EXTENSION_INTERNAL_FWDL);
    }

    public static String getOriginalMimeType(Context context, String path, String containingMime) {
        String result = containingMime;
        DrmManagerClient drmClient = new DrmManagerClient(context);
        try {
            if (drmClient.canHandle(path, null)) {
                return drmClient.getOriginalMimeType(path);
            }
            return result;
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Can't get original mime type since path is null or empty string.");
            return result;
        } catch (IllegalStateException e2) {
            Log.w(TAG, "DrmManagerClient didn't initialize properly.");
            return result;
        }
    }
}
