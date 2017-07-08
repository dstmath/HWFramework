package huawei.android.hwdrm;

import android.common.HwDrmManager;
import android.content.ContentValues;
import android.graphics.BitmapRegionDecoder;
import android.media.MediaFile;
import android.os.SystemProperties;
import java.io.FileInputStream;
import java.io.IOException;

public class HwDrmManagerImpl implements HwDrmManager {
    private static final boolean HW_DRM_FL_ONLY_OPEN = false;
    private static final boolean HW_DRM_OPEN = false;
    private static HwDrmManager mHwDrmManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.hwdrm.HwDrmManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.hwdrm.HwDrmManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwdrm.HwDrmManagerImpl.<clinit>():void");
    }

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
