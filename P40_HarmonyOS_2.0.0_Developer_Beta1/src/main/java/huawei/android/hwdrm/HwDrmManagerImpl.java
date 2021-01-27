package huawei.android.hwdrm;

import android.content.ContentValues;
import android.graphics.BitmapRegionDecoder;
import android.media.DefaultHwDrmManager;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.FileInputStream;
import java.io.IOException;

public class HwDrmManagerImpl extends DefaultHwDrmManager {
    private static final boolean HW_DRM_FL_ONLY_OPEN = "true".equals(SystemPropertiesEx.get("ro.huawei.cust.drm.fl_only"));
    private static final boolean HW_DRM_OPEN = "true".equals(SystemPropertiesEx.get("ro.huawei.cust.oma_drm"));
    private static final String TAG = "HwDrmManagerImpl";
    private static HwDrmManagerImpl sHwDrmManager = null;

    private HwDrmManagerImpl() {
    }

    public static HwDrmManagerImpl getDefault() {
        synchronized (HwDrmManagerImpl.class) {
            if (sHwDrmManager == null) {
                sHwDrmManager = new HwDrmManagerImpl();
            }
        }
        return sHwDrmManager;
    }

    public static boolean supportHwOmaDrm() {
        return HW_DRM_OPEN;
    }

    public static boolean supportDrmFlOnly() {
        return HW_DRM_OPEN && HW_DRM_FL_ONLY_OPEN;
    }

    public void addHwDrmFileType() {
    }

    public void setNtpTime(long cachedNtpTime, long cachedElapsedTime) {
        if (SystemPropertiesEx.getLong("net.ntp.time", 0) == 0 && cachedNtpTime > 0) {
            SystemPropertiesEx.set("net.ntp.time", String.valueOf(cachedNtpTime));
            SystemPropertiesEx.set("net.ntp.timereference", String.valueOf(cachedElapsedTime));
        }
    }

    public void updateOmaMimeType(String uri, ContentValues values) {
        if (values != null) {
            values.put("scanned", (Integer) 0);
            if (uri == null) {
                return;
            }
            if (uri.endsWith(".dm")) {
                values.put("mimetype", "application/vnd.oma.drm.message");
            } else if (uri.endsWith(".dcf")) {
                values.put("mimetype", "application/vnd.oma.drm.content");
            } else {
                Log.i(TAG, "uri error");
            }
        }
    }

    public BitmapRegionDecoder newDrmBitmapRegionDecoderInstance(FileInputStream stream, String pathName, boolean isShareable) throws IOException {
        return null;
    }
}
