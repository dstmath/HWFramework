package android.media;

import android.app.ActivityThread;
import com.huawei.android.util.NoNeedProvisionException;
import java.nio.BufferUnderflowException;

public class HwMediaDrmExImpl {
    private static final String TAG = "HwMediaDrmExImpl";
    private static String drmPackageName = ActivityThread.currentOpPackageName();

    private static final native int nativeGetOEMCertificateStatus(String str);

    private static final native String nativeGetOEMProvisionRequest(String str);

    private static final native int nativeProvideOEMProvisionResponse(String str, String str2);

    static {
        System.loadLibrary("media_jni");
    }

    public static int getOEMCertificateStatus() throws Exception {
        return nativeGetOEMCertificateStatus(drmPackageName);
    }

    public static String getOEMProvisionRequest() throws NoNeedProvisionException, BufferUnderflowException {
        return nativeGetOEMProvisionRequest(drmPackageName);
    }

    public static int provideOEMProvisionResponse(String provisionResponse) throws Exception {
        return nativeProvideOEMProvisionResponse(provisionResponse, drmPackageName);
    }
}
