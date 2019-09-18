package huawei.com.android.internal.app;

import android.content.res.Resources;
import com.android.internal.app.IHwLocaleStoreInner;
import com.android.internal.app.LocaleStore;
import com.huawei.android.app.IHwLocaleHelperEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HwLocaleHelperEx implements IHwLocaleHelperEx {
    IHwLocaleStoreInner mHWlpinner;

    public HwLocaleHelperEx(IHwLocaleStoreInner inner) {
        this.mHWlpinner = inner;
    }

    public List<String> getRelatedLocalesEx() {
        String[] localeArray = Resources.getSystem().getStringArray(17236083);
        List<String> relatedLocales = new ArrayList<>();
        relatedLocales.addAll(Arrays.asList(localeArray));
        return relatedLocales;
    }

    public int getCompareIntEx(LocaleStore.LocaleInfo lhs, LocaleStore.LocaleInfo rhs, List<String> relatedLocales) {
        String lhsStr = lhs.getId();
        String rhsStr = rhs.getId();
        if (lhsStr.startsWith("en") || lhsStr.startsWith("es") || lhsStr.startsWith("pt")) {
            lhsStr = lhsStr.replace("-Latn", "");
            rhsStr = rhsStr.replace("-Latn", "");
        }
        if (!this.mHWlpinner.isSuggestedLocale(lhs) || (this.mHWlpinner.getSuggestionTypeSim(lhs) == this.mHWlpinner.getSuggestionTypeSim(rhs) && relatedLocales.contains(lhsStr) == relatedLocales.contains(rhsStr))) {
            return 0;
        }
        if (this.mHWlpinner.getSuggestionTypeSim(lhs)) {
            return relatedLocales.contains(lhsStr) ? -1 : 1;
        }
        if (this.mHWlpinner.getSuggestionTypeSim(rhs)) {
            return relatedLocales.contains(rhsStr) ? 1 : -1;
        }
        return 0;
    }
}
