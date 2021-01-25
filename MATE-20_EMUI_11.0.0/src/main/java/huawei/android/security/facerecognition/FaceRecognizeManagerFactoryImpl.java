package huawei.android.security.facerecognition;

import android.content.Context;
import com.huawei.android.util.SlogEx;
import huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl;

public class FaceRecognizeManagerFactoryImpl extends FaceRecognizeManagerFactory {
    private static final String TAG = "FaceRecognizeManagerFactoryImpl";

    public DefaultFaceRecognizeManagerImpl getFaceRecognizeManagerImpl(Context context, DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback callback) {
        SlogEx.i(TAG, "get FaceRecognizeManagerImpl.");
        return new FaceRecognizeManagerImpl(context, callback);
    }
}
