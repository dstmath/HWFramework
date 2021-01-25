package android.common;

import android.content.pm.ApplicationInfo;
import java.util.List;

public interface IHwLoadedApk {
    void addBaseConfigsLibPaths(ApplicationInfo applicationInfo, List<String> list);

    String makeSplitLibPaths(ApplicationInfo applicationInfo, int i);
}
