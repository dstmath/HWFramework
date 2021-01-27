package huawei.android.hwgallerycache;

import android.hwgallerycache.HwGalleryCacheManager;
import android.hwgallerycache.IHwGalleryCacheManagerFactory;

public class HwGalleryCacheManagerFactoryImpl implements IHwGalleryCacheManagerFactory {
    public HwGalleryCacheManager.IHwGalleryCacheManager getGalleryCacheManagerInstance() {
        return new HwGalleryCacheManagerImpl();
    }
}
