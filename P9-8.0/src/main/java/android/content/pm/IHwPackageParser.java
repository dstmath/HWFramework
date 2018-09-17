package android.content.pm;

import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.PackageParserException;
import java.io.File;

public interface IHwPackageParser {
    void initMetaData(Activity activity);

    boolean isDefaultFullScreen(String str);

    void needStopApp(String str, File file) throws PackageParserException;
}
