package com.huawei.displayengine;

import android.content.Context;
import java.util.Map;

/* access modifiers changed from: package-private */
public class DisplayEngineLibraries {
    private static final String JNI_LOAD_ERROR = "jni load failed";
    private static final int NORMAL_ERROR = -1;
    private static final String TAG = "DE J DisplayEngineLibraries";
    private static boolean sIsJniLoaded;
    private volatile BrightnessTrainingProcessor mBrightnessTrainingProcessor = null;
    private final Object mBrightnessTrainingeProcessorLock = new Object();
    private final Context mContext;
    private volatile ImageProcessor mImageProcessor = null;
    private final Object mImageProcessorLock = new Object();
    private final DisplayEngineManager mInterface;

    private static native int native_deinitAlgorithm(int i, int i2);

    private static native void native_finalize();

    private static native int native_getParamAlgorithm(int i, int i2, int i3, Object obj, byte[] bArr);

    private static native void native_init();

    private static native int native_initAlgorithm(int i);

    private static native int native_processAlgorithm(int i, int i2, int i3, Object obj, byte[] bArr);

    private static native int native_setParamAlgorithm(int i, int i2, int i3, Object obj, byte[] bArr);

    static {
        sIsJniLoaded = false;
        DeLog.d(TAG, "loadLibrary displayengine-jni.so");
        try {
            System.loadLibrary("displayengine-jni");
            sIsJniLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            DeLog.e(TAG, "ERROR: could not load displayengine-jni natives");
        }
        DeLog.d(TAG, "loadLibrary displayengine-jni.so done");
    }

    DisplayEngineLibraries(DisplayEngineManager context) {
        DeLog.i(TAG, "DisplayEngineLibraries enter");
        this.mInterface = context;
        if (sIsJniLoaded) {
            native_init();
        }
        this.mContext = this.mInterface.getContext();
        DeLog.d(TAG, "DisplayEngineLibraries exit");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        DeLog.d(TAG, "call native_finalize()");
        if (sIsJniLoaded) {
            native_finalize();
        }
    }

    private ImageProcessor getImageProcessor() {
        if (this.mImageProcessor == null) {
            synchronized (this.mImageProcessorLock) {
                if (this.mImageProcessor == null) {
                    this.mImageProcessor = new ImageProcessor(this.mInterface.getService());
                }
            }
        }
        return this.mImageProcessor;
    }

    /* access modifiers changed from: package-private */
    public Object imageProcess(String command, Map<String, Object> param) {
        if (!sIsJniLoaded) {
            DeLog.e(TAG, JNI_LOAD_ERROR);
            throw new UnsupportedOperationException(JNI_LOAD_ERROR);
        } else if (command == null) {
            return null;
        } else {
            if (ImageProcessor.isCommandOwner(command)) {
                return getImageProcessor().imageProcess(command, param);
            }
            DeLog.e(TAG, "imageProcess() error! undefine command=" + command);
            return null;
        }
    }

    private BrightnessTrainingProcessor getBrightnessTrainingProcessor() {
        if (this.mBrightnessTrainingProcessor == null) {
            synchronized (this.mBrightnessTrainingeProcessorLock) {
                if (this.mBrightnessTrainingProcessor == null) {
                    this.mBrightnessTrainingProcessor = new BrightnessTrainingProcessor(this.mContext);
                }
            }
        }
        return this.mBrightnessTrainingProcessor;
    }

    /* access modifiers changed from: package-private */
    public int brightnessTrainingProcess(String command, Map<String, Object> param) {
        DeLog.i(TAG, "brightnessTrainingProcess ");
        if (sIsJniLoaded) {
            return getBrightnessTrainingProcessor().brightnessTrainingProcess(command, param);
        }
        DeLog.e(TAG, JNI_LOAD_ERROR);
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int brightnessTrainingAbort() {
        DeLog.i(TAG, "brightnessTrainingAbort ");
        if (sIsJniLoaded) {
            return getBrightnessTrainingProcessor().brightnessTrainingAbort();
        }
        DeLog.e(TAG, JNI_LOAD_ERROR);
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void setScene(int scene, int action) {
        if (sIsJniLoaded && ImageProcessor.isSceneSensitive(scene, action)) {
            getImageProcessor().setScene(scene, action);
        }
    }

    static int nativeInitAlgorithm(int algorithm) {
        return native_initAlgorithm(algorithm);
    }

    static int nativeDeinitAlgorithm(int algorithm, int handle) {
        return native_deinitAlgorithm(algorithm, handle);
    }

    static int nativeProcessAlgorithm(int algorithm, int handle, int type, Object object, byte[] param) {
        return native_processAlgorithm(algorithm, handle, type, object, param);
    }

    static int nativeGetParamAlgorithm(int algorithm, int handle, int type, Object object, byte[] param) {
        return native_getParamAlgorithm(algorithm, handle, type, object, param);
    }

    static int nativeSetParamAlgorithm(int algorithm, int handle, int type, Object object, byte[] param) {
        return native_setParamAlgorithm(algorithm, handle, type, object, param);
    }
}
