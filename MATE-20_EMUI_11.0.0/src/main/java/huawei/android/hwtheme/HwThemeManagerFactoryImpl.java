package huawei.android.hwtheme;

import android.hwtheme.HwThemeManager;
import android.hwtheme.IHwThemeManagerFactory;

public class HwThemeManagerFactoryImpl implements IHwThemeManagerFactory {
    public HwThemeManager.IHwThemeManager getThemeManagerInstance() {
        return new HwThemeManagerImpl();
    }
}
