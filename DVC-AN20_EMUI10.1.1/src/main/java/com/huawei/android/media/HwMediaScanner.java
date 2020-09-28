package com.huawei.android.media;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScanner;
import android.net.Uri;
import android.util.Log;
import com.huawei.android.provider.VoicemailContractEx;

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

    /* access modifiers changed from: protected */
    public void updateValues(String path, ContentValues contentValues) {
        String initPath;
        ContentValues initValues = this.mFileInitContentValues;
        if (initValues != null && (initPath = (String) initValues.get(VoicemailContractEx.Voicemails._DATA)) != null && initPath.equals(path)) {
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
