package ohos.global.i18n.utils;

import java.util.ArrayList;
import java.util.List;
import ohos.global.resource.LocaleFallBackException;
import ohos.global.resource.ResourceManagerImpl;

public class LocalesFallbackImpl extends LocalesFallback {
    @Override // ohos.global.i18n.utils.LocalesFallback
    public ArrayList<String> findValidAndSort(String str, List<String> list) throws LocaleFallBackException {
        return ResourceManagerImpl.findValidAndSort(str, list);
    }
}
