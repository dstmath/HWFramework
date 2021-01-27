package android.hwgallerycache;

import android.hwgallerycache.HwGalleryCacheManager;

public interface IHwGalleryCacheManagerFactory {
    HwGalleryCacheManager.IHwGalleryCacheManager getGalleryCacheManagerInstance();
}
