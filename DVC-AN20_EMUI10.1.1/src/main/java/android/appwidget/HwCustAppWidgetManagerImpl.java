package android.appwidget;

import android.content.pm.ParceledListSlice;
import android.os.SystemProperties;
import android.util.Log;
import java.util.Iterator;

public class HwCustAppWidgetManagerImpl extends HwCustAppWidgetManager {
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final String PKGNAME_SIMPLELUANCHER = "com.huawei.android.simplelauncher";
    private static final String PKGNAME_TOTEMWEATHER = "com.huawei.android.totemweather";
    private static final String TAG = "HwCustAppWidgetManagerImpl";

    public HwCustAppWidgetManagerImpl(AppWidgetManager appWidgetManager) {
        super(appWidgetManager);
    }

    public void hideTotemweatherWidgets(String pkgName, ParceledListSlice providers) {
        String packageName;
        if (IS_DOCOMO && pkgName != null && providers != null && providers.getList() != null && !pkgName.equals(PKGNAME_SIMPLELUANCHER)) {
            Iterator<AppWidgetProviderInfo> iter = providers.getList().iterator();
            while (iter != null && iter.hasNext()) {
                AppWidgetProviderInfo info = iter.next();
                if (info != null) {
                    try {
                        if (!(info.provider == null || (packageName = info.provider.getPackageName()) == null || !packageName.contains(PKGNAME_TOTEMWEATHER))) {
                            iter.remove();
                        }
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "hideTotemweatherWidgets " + e);
                    } catch (Exception e2) {
                        Log.e(TAG, "hideTotemweatherWidgets throw Exception");
                    }
                }
            }
        }
    }
}
