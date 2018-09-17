package android.os;

import android.util.Log;

public class FeatureLayerLoader {
    private static final String TAG = "FeatureLayerLoader";
    boolean DEBUG = false;

    private native int nativeLoad(String str, ClassLoader classLoader);

    static {
        try {
            System.loadLibrary("hwfeature_loader");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "featurelayer_loader library not found!", e);
        }
    }

    public void loadFeature(ClassLoader classLoader, String libName) {
        if (libName == null || classLoader == null) {
            Log.w(TAG, "load feature failed!");
            return;
        }
        try {
            String fullLibName = "lib" + libName + ".so";
            nativeLoad(fullLibName, classLoader);
            if (this.DEBUG) {
                Log.i(TAG, "after nativeLoad " + fullLibName);
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "native load library[" + libName + "] not found!", e);
        }
    }
}
