package huawei.android.security.facerecognition;

import android.content.Context;
import com.huawei.android.util.SlogEx;
import huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl;

public class FaceRecognizeManagerFactory {
    private static final String HW_FACERECOGNIZE_FACTORY_IMPL_NAME = "huawei.android.security.facerecognition.FaceRecognizeManagerFactoryImpl";
    private static final String TAG = "FaceRecognizeManagerFactory";
    private static volatile FaceRecognizeManagerFactory sInstance;
    private static final Object sLock = new Object();

    public static FaceRecognizeManagerFactory getInstance() {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = loadFaceRecognizeManagerFactory();
                }
            }
        }
        return sInstance;
    }

    private static FaceRecognizeManagerFactory loadFaceRecognizeManagerFactory() {
        Object object = null;
        try {
            object = Class.forName(HW_FACERECOGNIZE_FACTORY_IMPL_NAME).newInstance();
        } catch (ClassNotFoundException e) {
            SlogEx.e(TAG, "loadFactory() ClassNotFoundException !");
        } catch (InstantiationException e2) {
            SlogEx.e(TAG, "loadFactory() InstantiationException !");
        } catch (IllegalAccessException e3) {
            SlogEx.e(TAG, "loadFactory() IllegalAccessException !");
        } catch (Exception e4) {
            SlogEx.e(TAG, "loadFactory() Exception !");
        }
        if (object == null || !(object instanceof FaceRecognizeManagerFactory)) {
            FaceRecognizeManagerFactory factory = new FaceRecognizeManagerFactory();
            SlogEx.w(TAG, "load default implementation successfully.");
            return factory;
        }
        FaceRecognizeManagerFactory factory2 = (FaceRecognizeManagerFactory) object;
        SlogEx.i(TAG, "load actual implementation successfully.");
        return factory2;
    }

    public DefaultFaceRecognizeManagerImpl getFaceRecognizeManagerImpl(Context context, DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback callback) {
        return new DefaultFaceRecognizeManagerImpl(context, callback);
    }
}
