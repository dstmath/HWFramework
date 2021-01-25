package ohos.hiviewdfx;

public class FreezeDetectorUtils {
    public static final HiLogLabel LOG_TAG = new HiLogLabel(3, 218115333, "FreezeDetector_Java");
    private static volatile FreezeDetectorUtils freezeDetectorUtils = null;

    public static boolean loadJniLibrary() {
        boolean z = true;
        if (freezeDetectorUtils == null) {
            synchronized (FreezeDetectorUtils.class) {
                try {
                    HiLog.info(LOG_TAG, "Load libfreezedetector_jni.z.so", new Object[0]);
                    System.loadLibrary("freezedetector_jni.z");
                } catch (UnsatisfiedLinkError unused) {
                    HiLog.error(LOG_TAG, "Could not load libfreezedetector_jni.z.so", new Object[0]);
                    z = false;
                }
            }
        }
        return z;
    }
}
