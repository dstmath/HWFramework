package ohos.eventhandler;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import sun.misc.Cleaner;

/* access modifiers changed from: package-private */
public final class EventHandlerUtils {
    private static final Object JNI_LOAD_LOCK = new Object();
    static final int LOG_DOMAIN = 218108208;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, "JavaEventHandlerUtils");
    private static final long TIME_CONVERSION = 1000;
    private static volatile boolean jniLoaded = false;

    static long fromMillsToNanoSeconds(long j) {
        return j * 1000 * 1000;
    }

    private EventHandlerUtils() {
    }

    static boolean loadJniLibrary() {
        if (jniLoaded) {
            return true;
        }
        synchronized (JNI_LOAD_LOCK) {
            if (jniLoaded) {
                return true;
            }
            try {
                HiLog.info(LOG_LABEL, "Load libeventhandler_jni.z.so", new Object[0]);
                System.loadLibrary("eventhandler_jni.z");
                jniLoaded = true;
            } catch (UnsatisfiedLinkError unused) {
                HiLog.error(LOG_LABEL, "Could not load libeventhandler_jni.z.so", new Object[0]);
            }
            return jniLoaded;
        }
    }

    static void trackObject(Object obj, Runnable runnable) {
        if (obj != null && runnable != null) {
            Cleaner.create(obj, runnable);
        }
    }

    static long fromNanoSecondsToMills(long j) {
        return (j / 1000) / 1000;
    }
}
