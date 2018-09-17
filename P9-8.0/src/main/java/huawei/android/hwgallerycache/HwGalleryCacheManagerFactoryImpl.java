package huawei.android.hwgallerycache;

import android.hwgallerycache.HwGalleryCacheManager.IHwGalleryCacheManager;
import android.hwgallerycache.IHwGalleryCacheManagerFactory;

public class HwGalleryCacheManagerFactoryImpl implements IHwGalleryCacheManagerFactory {
    public IHwGalleryCacheManager getGalleryCacheManagerInstance() {
        return new HwGalleryCacheManagerImpl();
    }
}
