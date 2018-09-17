package android.common;

import android.content.ContentValues;
import android.graphics.BitmapRegionDecoder;
import java.io.FileInputStream;
import java.io.IOException;

public class HwDrmManagerDummy implements HwDrmManager {
    private static HwDrmManager mHwDrmManager = null;

    private HwDrmManagerDummy() {
    }

    public static HwDrmManager getDefault() {
        if (mHwDrmManager == null) {
            mHwDrmManager = new HwDrmManagerDummy();
        }
        return mHwDrmManager;
    }

    public void addHwDrmFileType() {
    }

    public void setNtpTime(long cachedNtpTime, long cachedElapsedTime) {
    }

    public void updateOmaMimeType(String uri, ContentValues values) {
    }

    public BitmapRegionDecoder newDrmBitmapRegionDecoderInstance(FileInputStream stream, String pathName, boolean isShareable) throws IOException {
        return null;
    }
}
