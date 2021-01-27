package android.hwouc;

import android.util.Log;

public final class HwOucNative {
    private static final String LIB_JNI_NAME = "hwouc_jni";
    private static final String TAG = "HwOucNative";
    private static HwOucNative sInstance;
    private static boolean sIsJNILoaded = false;

    private native int checkScript(String str);

    private native boolean isCheckScriptSupport();

    public native String getUpdateAuthParams();

    public native int saveUpdateAuth(String str, String str2);

    private HwOucNative() {
        if (!isJNILoaded()) {
            sIsJNILoaded = loadLibrary(LIB_JNI_NAME);
        }
    }

    private boolean isJNILoaded() {
        return sIsJNILoaded;
    }

    private boolean isValid(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private boolean loadLibrary(String library) {
        if (!isValid(library)) {
            return false;
        }
        Log.i(TAG, "loadLibrary, library = " + library);
        try {
            System.loadLibrary(library.trim());
            return true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "loadLibrary, could not be loaded");
            Log.d(TAG, e.getMessage());
            return false;
        } catch (SecurityException e2) {
            Log.e(TAG, "loadLibrary, not allow be loaded");
            Log.d(TAG, e2.getMessage());
            return false;
        } catch (Exception e3) {
            Log.e(TAG, "loadLibrary");
            return false;
        }
    }

    public static synchronized HwOucNative getInstance() {
        HwOucNative hwOucNative;
        synchronized (HwOucNative.class) {
            if (sInstance == null) {
                sInstance = new HwOucNative();
            }
            hwOucNative = sInstance;
        }
        return hwOucNative;
    }

    public boolean isVerifyScriptSupport() {
        if (isJNILoaded()) {
            return isCheckScriptSupport();
        }
        return false;
    }

    public int executeVerifyScript(String verifyFile) {
        if (!isValid(verifyFile)) {
            Log.e(TAG, "executeVerifyScript, isValid verifyFile = " + verifyFile);
            return -1;
        } else if (isVerifyScriptSupport()) {
            Log.i(TAG, "executeVerifyScript, checkScript");
            return checkScript(verifyFile.trim());
        } else {
            Log.i(TAG, "executeVerifyScript, isVerifyScriptSupport = false");
            return -1;
        }
    }
}
