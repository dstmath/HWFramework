package huawei.android.hwtheme;

import android.hwtheme.HwThemeManager.IHwThemeManager;
import android.hwtheme.IHwThemeManagerFactory;

public class HwThemeManagerFactoryImpl implements IHwThemeManagerFactory {
    public IHwThemeManager getThemeManagerInstance() {
        return new HwThemeManagerImpl();
    }
}
