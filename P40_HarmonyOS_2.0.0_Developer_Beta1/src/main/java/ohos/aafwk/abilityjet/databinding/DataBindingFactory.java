package ohos.aafwk.abilityjet.databinding;

import java.lang.reflect.InvocationTargetException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

class DataBindingFactory {
    private static final String BINDING_SUFFIX = "Binding";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108672, "DataBindingFactor:");
    private static final String PACKAGE_NAME = "ohos.aafwk.abilityjet.databinding.";

    private DataBindingFactory() {
    }

    public static <T extends DataBinding> T create(String str, ClassLoader classLoader) {
        T t;
        HiLog.debug(LABEL, "Data Binding Factory create enter.", new Object[0]);
        try {
            Object newInstance = Class.forName(PACKAGE_NAME + str + BINDING_SUFFIX, false, classLoader).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            HiLog.debug(LABEL, "Data Binding Factory create instance : %{public}s.", new Object[]{newInstance.toString()});
            if (newInstance instanceof DataBinding) {
                HiLog.debug(LABEL, "Data Binding Factory create instance instanceof DataBinding", new Object[0]);
                t = (T) ((DataBinding) newInstance);
            } else {
                t = null;
            }
            HiLog.debug(LABEL, "Data Binding Factory create end.", new Object[0]);
            return t;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            throw new IllegalArgumentException("Data Binding Factory create instance throw exception");
        }
    }
}
