package huawei.com.android.internal.app;

import android.content.Context;
import com.android.internal.app.IHwLocalePickerInner;
import com.huawei.android.app.IHwLocalePickerEx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwLocalePickerEx implements IHwLocalePickerEx {
    IHwLocalePickerInner inner;
    Context mContext;

    public HwLocalePickerEx(IHwLocalePickerInner inner2, Context context) {
        this.inner = inner2;
        this.mContext = context;
    }

    public String[] getSupportedLanguagesFromConfig() {
        Set<String> realList = new HashSet<>();
        realList.addAll(this.inner.getRealLocaleListEx(this.mContext, this.mContext.getResources().getStringArray(33816592)));
        List<String> supportedLocales = new ArrayList<>(realList);
        return (String[]) supportedLocales.toArray(new String[supportedLocales.size()]);
    }
}
