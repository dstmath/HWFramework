package ohos.utils.adapter;

import android.os.Bundle;
import android.util.ArrayMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.PacMap;

public class PacMapUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218119424, "PacMapUtils");

    public static PacMap convertFromBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        Bundle bundle2 = (Bundle) bundle.clone();
        try {
            Method declaredMethod = bundle2.getClass().getSuperclass().getDeclaredMethod("getMap", new Class[0]);
            declaredMethod.setAccessible(true);
            ArrayMap arrayMap = (ArrayMap) declaredMethod.invoke(bundle2, new Object[0]);
            PacMap pacMap = new PacMap(arrayMap.size());
            pacMap.putAll(arrayMap);
            return pacMap;
        } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            HiLog.warn(LABEL, "fail to convert bundle to pacmap, error: %{public}s", e.getMessage());
            return null;
        }
    }

    public static Bundle convertIntoBundle(PacMap pacMap) {
        if (pacMap == null) {
            return null;
        }
        if (pacMap.isEmpty()) {
            return new Bundle(0);
        }
        ArrayMap arrayMap = new ArrayMap(pacMap.getSize());
        arrayMap.putAll(pacMap.getAll());
        Bundle bundle = new Bundle(arrayMap.size());
        try {
            Method declaredMethod = bundle.getClass().getSuperclass().getDeclaredMethod("putAll", ArrayMap.class);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(bundle, arrayMap);
            return bundle;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            HiLog.warn(LABEL, "fail to convert bundle to pacmap, error: %{public}s", e.getMessage());
            return new Bundle(0);
        }
    }
}
