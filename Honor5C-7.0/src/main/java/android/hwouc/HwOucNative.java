package android.hwouc;

import android.util.Log;

public final class HwOucNative {
    private static final String LIB_JNI_NAME = "hwouc_jni";
    private static HwOucNative SINGLETON = null;
    private static final String TAG = "HwOucNative";
    private static boolean mIsJNILoaded;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hwouc.HwOucNative.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hwouc.HwOucNative.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hwouc.HwOucNative.<clinit>():void");
    }

    private native int checkScript(String str);

    private native boolean isCheckScriptSupport();

    public native String getUpdateAuthParams();

    public native int saveUpdateAuth(String str, String str2);

    private HwOucNative() {
        if (!isJNILoaded()) {
            mIsJNILoaded = loadLibrary(LIB_JNI_NAME);
        }
    }

    private boolean isJNILoaded() {
        return mIsJNILoaded;
    }

    private boolean isValid(String str) {
        return (str == null || str.trim().isEmpty()) ? false : true;
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
            Log.d(TAG, e3.getMessage());
            return false;
        }
    }

    public static HwOucNative getInstance() {
        if (SINGLETON == null) {
            SINGLETON = new HwOucNative();
        }
        return SINGLETON;
    }

    public boolean isVerifyScriptSupport() {
        return isJNILoaded() ? isCheckScriptSupport() : false;
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
