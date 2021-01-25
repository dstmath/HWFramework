package ohos.global.i18n.utils;

import java.util.ArrayList;
import java.util.List;
import ohos.global.resource.LocaleFallBackException;

public abstract class LocalesFallback {
    public abstract ArrayList<String> findValidAndSort(String str, List<String> list) throws LocaleFallBackException;

    public static LocalesFallback getInstance() throws LocaleFallBackException {
        try {
            Object newInstance = Class.forName("ohos.global.i18n.utils.LocalesFallbackImpl").newInstance();
            if (newInstance != null) {
                return (LocalesFallback) newInstance;
            }
            return null;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new LocaleFallBackException("fail to create LocalesFallback instance", e);
        }
    }
}
