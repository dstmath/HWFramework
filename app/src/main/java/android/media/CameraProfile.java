package android.media;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import java.util.Arrays;
import java.util.HashMap;

public class CameraProfile {
    public static final int QUALITY_HIGH = 2;
    public static final int QUALITY_LOW = 0;
    public static final int QUALITY_MEDIUM = 1;
    private static final HashMap<Integer, int[]> sCache = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.CameraProfile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.CameraProfile.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.CameraProfile.<clinit>():void");
    }

    private static final native int native_get_image_encoding_quality_level(int i, int i2);

    private static final native int native_get_num_image_encoding_quality_levels(int i);

    private static final native void native_init();

    public static int getJpegEncodingQualityParameter(int quality) {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = QUALITY_LOW; i < numberOfCameras; i += QUALITY_MEDIUM) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 0) {
                return getJpegEncodingQualityParameter(i, quality);
            }
        }
        return QUALITY_LOW;
    }

    public static int getJpegEncodingQualityParameter(int cameraId, int quality) {
        if (quality < 0 || quality > QUALITY_HIGH) {
            throw new IllegalArgumentException("Unsupported quality level: " + quality);
        }
        int i;
        synchronized (sCache) {
            int[] levels = (int[]) sCache.get(Integer.valueOf(cameraId));
            if (levels == null) {
                levels = getImageEncodingQualityLevels(cameraId);
                sCache.put(Integer.valueOf(cameraId), levels);
            }
            i = levels[quality];
        }
        return i;
    }

    private static int[] getImageEncodingQualityLevels(int cameraId) {
        int nLevels = native_get_num_image_encoding_quality_levels(cameraId);
        if (nLevels != 3) {
            throw new RuntimeException("Unexpected Jpeg encoding quality levels " + nLevels);
        }
        int[] levels = new int[nLevels];
        for (int i = QUALITY_LOW; i < nLevels; i += QUALITY_MEDIUM) {
            levels[i] = native_get_image_encoding_quality_level(cameraId, i);
        }
        Arrays.sort(levels);
        return levels;
    }
}
