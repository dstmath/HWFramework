package huawei.android.hwtheme;

import android.hwtheme.HwThemeManager;
import android.hwtheme.HwThemeManagerDummy;
import android.hwtheme.IHwThemeManagerFactory;

public class DefaultHwThemeManagerFactoryImpl implements IHwThemeManagerFactory {
    @Override // android.hwtheme.IHwThemeManagerFactory
    public HwThemeManager.IHwThemeManager getThemeManagerInstance() {
        return new HwThemeManagerDummy();
    }
}
