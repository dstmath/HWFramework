package huawei.android.hwdrm;

import android.common.HwDrmManager;
import android.content.ContentValues;
import android.graphics.BitmapRegionDecoder;
import android.media.MediaFile;
import android.os.SystemProperties;
import java.io.FileInputStream;
import java.io.IOException;

public class HwDrmManagerImpl implements HwDrmManager {
    private static final boolean HW_DRM_FL_ONLY_OPEN = "true".equals(SystemProperties.get("ro.huawei.cust.drm.fl_only"));
    private static final boolean HW_DRM_OPEN = "true".equals(SystemProperties.get("ro.huawei.cust.oma_drm"));
    private static HwDrmManager mHwDrmManager = null;

    private HwDrmManagerImpl() {
    }

    public static HwDrmManager getDefault() {
        if (mHwDrmManager == null) {
            mHwDrmManager = new HwDrmManagerImpl();
        }
        return mHwDrmManager;
    }

    public static boolean supportHwOmaDrm() {
        return HW_DRM_OPEN;
    }

    public static boolean supportDrmFlOnly() {
        return HW_DRM_OPEN ? HW_DRM_FL_ONLY_OPEN : false;
    }

    public void addHwDrmFileType() {
        if (HW_DRM_OPEN) {
            MediaFile.hwAddFileType("DCF", 51, "application/vnd.oma.drm.content");
        }
    }

    public void setNtpTime(long cachedNtpTime, long cachedElapsedTime) {
        if (0 == SystemProperties.getLong("net.ntp.time", 0) && cachedNtpTime > 0) {
            SystemProperties.set("net.ntp.time", String.valueOf(cachedNtpTime));
            SystemProperties.set("net.ntp.timereference", String.valueOf(cachedElapsedTime));
        }
    }

    public void updateOmaMimeType(String uri, ContentValues values) {
        if (values != null) {
            values.put("scanned", Integer.valueOf(0));
            if (uri == null) {
                return;
            }
            if (uri.endsWith(".dm")) {
                values.put("mimetype", "application/vnd.oma.drm.message");
            } else if (uri.endsWith(".dcf")) {
                values.put("mimetype", "application/vnd.oma.drm.content");
            }
        }
    }

    public BitmapRegionDecoder newDrmBitmapRegionDecoderInstance(FileInputStream stream, String pathName, boolean isShareable) throws IOException {
        if (stream == null || pathName == null || !pathName.endsWith(".dcf")) {
            return null;
        }
        return BitmapRegionDecoder.hwNewInstance(stream.getFD(), isShareable);
    }
}
