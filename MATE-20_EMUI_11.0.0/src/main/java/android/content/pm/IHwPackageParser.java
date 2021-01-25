package android.content.pm;

import android.content.AbsIntentFilter;
import android.content.pm.PackageParserEx;
import android.content.res.XmlResourceParser;
import java.io.File;

public interface IHwPackageParser {
    Float getDefaultAspect(String str, int i);

    float getDefaultNonFullMaxRatio();

    float getDeviceMaxRatio();

    int getDisplayChangeAppRestartConfig(int i, String str);

    float getExclusionNavBarMaxRatio();

    void initMetaData(PackageParserEx.ActivityEx activityEx);

    boolean isDefaultFullScreen(String str);

    boolean isFullScreenDevice();

    boolean needStopApp(String str, File file);

    void parseIntentFilterState(XmlResourceParser xmlResourceParser, String str, AbsIntentFilter absIntentFilter);

    void updateBaseProvider(PackageParserEx.PackageEx packageEx, ProviderInfo providerInfo, int i);
}
