package android.content.pm;

import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.PackageParserException;
import java.io.File;

public interface IHwPackageParser {
    void changeApplicationEuidIfNeeded(ApplicationInfo applicationInfo, int i);

    void initMetaData(Activity activity);

    void needStopApp(String str, File file) throws PackageParserException;
}
