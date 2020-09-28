package com.huawei.dmsdpsdk2.util.camera;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class KeyGenerator {
    private static final String CHARACTERISTICS_KEY = "android.hardware.camera2.CameraCharacteristics$Key";
    private static final String TAG = KeyGenerator.class.getSimpleName();
    private static Constructor characteristicsKeyConstructor;

    static {
        characteristicsKeyConstructor = null;
        try {
            Class characteristicsKeyClazz = Class.forName(CHARACTERISTICS_KEY);
            if (characteristicsKeyClazz != null) {
                characteristicsKeyConstructor = characteristicsKeyClazz.getDeclaredConstructor(String.class, Class.class);
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            String str = TAG;
            Log.d(str, "exception when invoke constructor of Key." + e.getCause());
        }
    }

    public static CameraCharacteristics.Key generateCharacteristicsKey(String name, Class type) {
        Constructor constructor = characteristicsKeyConstructor;
        if (constructor == null) {
            return null;
        }
        try {
            if (!(constructor.newInstance(name, type) instanceof CameraCharacteristics.Key)) {
                return null;
            }
            return (CameraCharacteristics.Key) characteristicsKeyConstructor.newInstance(name, type);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            String str = TAG;
            Log.d(str, "new characteristics key exception!" + e.getCause());
            return null;
        }
    }
}
