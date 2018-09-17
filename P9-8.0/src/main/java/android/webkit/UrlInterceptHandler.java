package android.webkit;

import android.webkit.CacheManager.CacheResult;
import java.util.Map;

@Deprecated
public interface UrlInterceptHandler {
    @Deprecated
    PluginData getPluginData(String str, Map<String, String> map);

    @Deprecated
    CacheResult service(String str, Map<String, String> map);
}
