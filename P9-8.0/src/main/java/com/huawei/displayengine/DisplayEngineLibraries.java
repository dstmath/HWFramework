package com.huawei.displayengine;

import java.util.Map;

class DisplayEngineLibraries {
    private static final String TAG = "DE J DisplayEngineLibraries";
    private static boolean mJniLoaded = true;
    private volatile ImageProcessor mImageProcessor;
    private Object mImageProcessorLock;
    private final DisplayEngineManager mInterface;

    private static native int native_deinitAlgorithm(int i, int i2);

    private static native void native_finalize();

    private static native int native_getParamAlgorithm(int i, int i2, int i3, Object obj, byte[] bArr);

    private static native void native_init();

    private static native int native_initAlgorithm(int i);

    private static native int native_processAlgorithm(int i, int i2, int i3, Object obj, byte[] bArr);

    private static native int native_setParamAlgorithm(int i, int i2, int i3, Object obj, byte[] bArr);

    static {
        DElog.d(TAG, "loadLibrary displayengine-jni.so");
        try {
            System.loadLibrary("displayengine-jni");
        } catch (UnsatisfiedLinkError e) {
            DElog.e(TAG, "ERROR: could not load displayengine-jni natives");
        }
        DElog.d(TAG, "loadLibrary displayengine-jni.so done");
    }

    public DisplayEngineLibraries(DisplayEngineManager context) {
        DElog.i(TAG, "DisplayEngineLibraries enter");
        this.mInterface = context;
        if (mJniLoaded) {
            nativeInit();
        }
        this.mImageProcessorLock = new Object();
        DElog.d(TAG, "DisplayEngineLibraries exit");
    }

    protected void finalize() throws Throwable {
        try {
            DElog.d(TAG, "call native_finalize()");
            if (mJniLoaded) {
                nativeFinalize();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
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

    public Object imageProcess(String command, Map<String, Object> param) {
        if (!mJniLoaded) {
            DElog.e(TAG, "jni load failed");
            throw new UnsupportedOperationException("jni load failed");
        } else if (command == null) {
            return null;
        } else {
            if (ImageProcessor.isCommandOwner(command)) {
                return getImageProcessor().imageProcess(command, param);
            }
            DElog.e(TAG, "imageProcess() error! undefine command=" + command);
            return null;
        }
    }

    public void setScene(int scene, int action) {
        if (mJniLoaded && ImageProcessor.isSceneSensitive(scene, action)) {
            try {
                getImageProcessor().setScene(scene, action);
            } catch (RuntimeException e) {
                DElog.e(TAG, "setScene() error, RuntimeException :" + e);
            }
        }
    }

    public static void nativeInit() {
        native_init();
    }

    public static void nativeFinalize() {
        native_finalize();
    }

    public static int nativeInitAlgorithm(int algorithm) {
        return native_initAlgorithm(algorithm);
    }

    public static int nativeDeinitAlgorithm(int algorithm, int handle) {
        return native_deinitAlgorithm(algorithm, handle);
    }

    public static int nativeProcessAlgorithm(int algorithm, int handle, int type, Object object, byte[] param) {
        return native_processAlgorithm(algorithm, handle, type, object, param);
    }

    public static int nativeGetParamAlgorithm(int algorithm, int handle, int type, Object object, byte[] param) {
        return native_getParamAlgorithm(algorithm, handle, type, object, param);
    }

    public static int nativeSetParamAlgorithm(int algorithm, int handle, int type, Object object, byte[] param) {
        return native_setParamAlgorithm(algorithm, handle, type, object, param);
    }
}
