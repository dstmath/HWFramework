package android.common;

import android.content.ContentValues;
import android.graphics.BitmapRegionDecoder;
import java.io.FileInputStream;
import java.io.IOException;

public interface HwDrmManager {
    void addHwDrmFileType();

    BitmapRegionDecoder newDrmBitmapRegionDecoderInstance(FileInputStream fileInputStream, String str, boolean z) throws IOException;

    void setNtpTime(long j, long j2);

    void updateOmaMimeType(String str, ContentValues contentValues);
}
