package ohos.aafwk.abilityjet.databinding;

import java.lang.reflect.InvocationTargetException;

class DataBindingFactory {
    private static final String BINDING_SUFFIX = "Binding";
    private static final String PACKAGE_NAME = "ohos.aafwk.abilityjet.databinding.";

    private DataBindingFactory() {
    }

    public static <T extends DataBinding> T create(String str, ClassLoader classLoader) {
        try {
            Object newInstance = Class.forName(PACKAGE_NAME + str + BINDING_SUFFIX, false, classLoader).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            if (newInstance instanceof DataBinding) {
                return (T) ((DataBinding) newInstance);
            }
            return null;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            return null;
        }
    }
}
