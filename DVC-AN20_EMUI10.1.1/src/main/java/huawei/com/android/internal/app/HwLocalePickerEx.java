package huawei.com.android.internal.app;

import android.content.Context;
import com.android.internal.app.IHwLocalePickerInner;
import com.huawei.android.app.IHwLocalePickerEx;
import java.util.Iterator;
import java.util.Locale;

public class HwLocalePickerEx implements IHwLocalePickerEx {
    private Context mContext;
    private IHwLocalePickerInner mInner;

    public HwLocalePickerEx() {
    }

    public HwLocalePickerEx(IHwLocalePickerInner inner, Context context) {
        this.mInner = inner;
        this.mContext = context;
    }

    public boolean isBlackLanguage(Context context, String language) {
        Locale locale = Locale.forLanguageTag(language);
        Iterator<Locale> it = HwLocaleHelperEx.getBlackLangsPart(context).iterator();
        while (it.hasNext()) {
            Locale black = it.next();
            if (black.getScript().isEmpty()) {
                if (locale.getLanguage().equals(black.getLanguage())) {
                    return true;
                }
            } else if (locale.getLanguage().equals(black.getLanguage()) && locale.getScript().equals(black.getScript())) {
                return true;
            }
        }
        return false;
    }
}
