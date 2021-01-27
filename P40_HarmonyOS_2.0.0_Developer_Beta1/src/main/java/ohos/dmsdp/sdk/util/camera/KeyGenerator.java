package ohos.dmsdp.sdk.util.camera;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class KeyGenerator {
    private static final String CHARACTERISTICS_KEY = "android.hardware.camera2.CameraCharacteristics$Key";
    private static final String TAG = KeyGenerator.class.getSimpleName();
    private static Constructor<?> characteristicsKeyConstructor;

    static {
        characteristicsKeyConstructor = null;
        try {
            Class<?> cls = Class.forName(CHARACTERISTICS_KEY);
            if (cls != null) {
                characteristicsKeyConstructor = cls.getDeclaredConstructor(String.class, Class.class);
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            String str = TAG;
            Log.d(str, "exception when invoke constructor of Key." + e.getCause());
        }
    }

    public static CameraCharacteristics.Key<?> generateCharacteristicsKey(String str, Class<?> cls) {
        Constructor<?> constructor = characteristicsKeyConstructor;
        if (constructor == null) {
            return null;
        }
        try {
            if (constructor.newInstance(str, cls) instanceof CameraCharacteristics.Key) {
                return (CameraCharacteristics.Key) characteristicsKeyConstructor.newInstance(str, cls);
            }
            return null;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            String str2 = TAG;
            Log.d(str2, "new characteristics key exception!" + e.getCause());
            return null;
        }
    }
}
