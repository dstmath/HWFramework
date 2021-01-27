package android.media;

import android.content.ContentValues;
import android.graphics.BitmapRegionDecoder;
import com.huawei.annotation.HwSystemApi;
import java.io.FileInputStream;
import java.io.IOException;

@HwSystemApi
public class DefaultHwDrmManager implements HwDrmManager {
    private static DefaultHwDrmManager mHwDrmManager = null;

    public static DefaultHwDrmManager getDefault() {
        if (mHwDrmManager == null) {
            mHwDrmManager = new DefaultHwDrmManager();
        }
        return mHwDrmManager;
    }

    @Override // android.media.HwDrmManager
    public void addHwDrmFileType() {
    }

    @Override // android.media.HwDrmManager
    public void setNtpTime(long cachedNtpTime, long cachedElapsedTime) {
    }

    @Override // android.media.HwDrmManager
    public void updateOmaMimeType(String uri, ContentValues values) {
    }

    @Override // android.media.HwDrmManager
    public BitmapRegionDecoder newDrmBitmapRegionDecoderInstance(FileInputStream stream, String pathName, boolean isShareable) throws IOException {
        return null;
    }
}
