package com.huawei.android.media;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScanner;
import android.net.Uri;
import android.util.Log;

public class HwMediaScanner extends MediaScanner {
    private ContentValues mFileInitContentValues;

    public HwMediaScanner(Context c, String volumeName) {
        super(c, volumeName);
    }

    public Uri scanSingleFile(String path, ContentValues contentValues) {
        if (contentValues == null) {
            Log.w("HwMediaScanner", "scanSingleFile contentValues is null!");
            return null;
        }
        this.mFileInitContentValues = contentValues;
        Uri uri = scanSingleFile(path, (String) contentValues.get("mime_type"));
        this.mFileInitContentValues = null;
        return uri;
    }

    protected void updateValues(String path, ContentValues contentValues) {
        ContentValues initValues = this.mFileInitContentValues;
        if (initValues != null) {
            String initPath = (String) initValues.get("_data");
            if (initPath != null && (initPath.equals(path) ^ 1) == 0) {
                if (initValues.containsKey("datetaken")) {
                    contentValues.put("datetaken", (Long) initValues.get("datetaken"));
                }
                if (initValues.containsKey("latitude")) {
                    contentValues.put("latitude", (Double) initValues.get("latitude"));
                }
                if (initValues.containsKey("longitude")) {
                    contentValues.put("longitude", (Double) initValues.get("longitude"));
                }
            }
        }
    }
}
