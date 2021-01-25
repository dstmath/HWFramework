package ohos.miscservices.adapter.utils;

import android.view.View;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ReflectUtil {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "ReflectUtil");

    private ReflectUtil() {
    }

    public static Optional<View> getSurfaceView() {
        HiLog.debug(TAG, "getSurfaceView by reflection begin.", new Object[0]);
        Optional<?> aGPWindowObj = getAGPWindowObj();
        if (!aGPWindowObj.isPresent()) {
            HiLog.error(TAG, "getSurfaceView by reflection failed: current AGPWindow object is null.", new Object[0]);
            return Optional.empty();
        }
        try {
            Object invoke = Class.forName("ohos.agp.window.wmc.AGPWindow").getMethod("getSurfaceView", new Class[0]).invoke(aGPWindowObj.get(), new Object[0]);
            if (invoke instanceof View) {
                return Optional.of((View) invoke);
            }
            HiLog.error(TAG, "getSurfaceView failed: current surfaceView object is not instance of View.", new Object[0]);
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            HiLog.error(TAG, "getSurfaceView ClassNotFoundException: %{public}s", e.getLocalizedMessage());
            HiLog.debug(TAG, "getSurfaceView by reflection end.", new Object[0]);
            return Optional.empty();
        } catch (NoSuchMethodException e2) {
            HiLog.error(TAG, "getSurfaceView NoSuchMethodException: %{public}s", e2.getLocalizedMessage());
            HiLog.debug(TAG, "getSurfaceView by reflection end.", new Object[0]);
            return Optional.empty();
        } catch (IllegalAccessException | InvocationTargetException e3) {
            HiLog.error(TAG, "getSurfaceView invoke method Exception: %{public}s", e3.getLocalizedMessage());
            HiLog.debug(TAG, "getSurfaceView by reflection end.", new Object[0]);
            return Optional.empty();
        }
    }

    public static Optional<Context> getHarmonyContext() {
        HiLog.debug(TAG, "getHarmonyContext by reflection begin.", new Object[0]);
        Optional<?> aGPWindowObj = getAGPWindowObj();
        if (!aGPWindowObj.isPresent()) {
            HiLog.error(TAG, "getHarmonyContext by reflection failed: current AGPWindow object is null.", new Object[0]);
            return Optional.empty();
        }
        try {
            Object invoke = Class.forName("ohos.agp.window.wmc.AGPWindow").getMethod("getHarmonyContext", new Class[0]).invoke(aGPWindowObj.get(), new Object[0]);
            if (invoke instanceof Context) {
                return Optional.of((Context) invoke);
            }
            HiLog.error(TAG, "getHarmonyContext failed: current object is not instance of harmony Context.", new Object[0]);
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            HiLog.error(TAG, "getHarmonyContext ClassNotFoundException: %{public}s", e.getLocalizedMessage());
            HiLog.debug(TAG, "getHarmonyContext by reflection end.", new Object[0]);
            return Optional.empty();
        } catch (NoSuchMethodException e2) {
            HiLog.error(TAG, "getHarmonyContext NoSuchMethodException: %{public}s", e2.getLocalizedMessage());
            HiLog.debug(TAG, "getHarmonyContext by reflection end.", new Object[0]);
            return Optional.empty();
        } catch (IllegalAccessException | InvocationTargetException e3) {
            HiLog.error(TAG, "getHarmonyContext invoke method Exception: %{public}s", e3.getLocalizedMessage());
            HiLog.debug(TAG, "getHarmonyContext by reflection end.", new Object[0]);
            return Optional.empty();
        }
    }

    private static Optional<?> getAGPWindowObj() {
        HiLog.debug(TAG, "getAGPWindowObj begin.", new Object[0]);
        try {
            Class<?> cls = Class.forName("ohos.agp.window.wmc.AGPWindowManager");
            Object invoke = cls.getMethod("getInstance", new Class[0]).invoke(cls, new Object[0]);
            if (invoke == null) {
                return Optional.empty();
            }
            Object invoke2 = cls.getMethod("getTopWindow", new Class[0]).invoke(invoke, new Object[0]);
            if (invoke2 instanceof Optional) {
                return (Optional) invoke2;
            }
            HiLog.debug(TAG, "getAGPWindowObj end.", new Object[0]);
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            HiLog.error(TAG, "getAGPWindowObj ClassNotFoundException: %{public}s", e.getLocalizedMessage());
        } catch (NoSuchMethodException e2) {
            HiLog.error(TAG, "getAGPWindowObj NoSuchMethodException: %{public}s", e2.getLocalizedMessage());
        } catch (IllegalAccessException | InvocationTargetException e3) {
            HiLog.error(TAG, "getAGPWindowObj invoke method Exception: %{public}s", e3.getLocalizedMessage());
        }
    }
}
