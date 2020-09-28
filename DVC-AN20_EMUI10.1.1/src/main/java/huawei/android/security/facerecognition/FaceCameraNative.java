package huawei.android.security.facerecognition;

import com.huawei.annotation.HwSystemApi;
import huawei.android.security.facerecognition.utils.LogUtil;

public class FaceCameraNative {
    private static final String TAG = "FaceCameraNative";

    @HwSystemApi
    public static native int native_send_image(byte[] bArr);

    static {
        try {
            System.loadLibrary("FaceRecognizeSendImage");
        } catch (UnsatisfiedLinkError e) {
            LogUtil.e(TAG, "LoadLibrary occurs error " + e.toString());
        }
    }
}
