package android.content.pm;

import android.content.pm.PackageParser;
import java.io.File;

public interface IHwPackageParser {
    Float getDefaultAspect(String str);

    float getDefaultNonFullMaxRatio();

    float getDeviceMaxRatio();

    float getExclusionNavBarMaxRatio();

    void initMetaData(PackageParser.Activity activity);

    boolean isDefaultFullScreen(String str);

    boolean isFullScreenDevice();

    void needStopApp(String str, File file) throws PackageParser.PackageParserException;
}
