package android.content.pm;

import android.annotation.SystemApi;
import android.content.AbsIntentFilter;
import android.content.pm.PackageParserEx;
import android.content.res.XmlResourceParser;
import com.huawei.annotation.HwSystemApi;
import java.io.File;

@HwSystemApi
public class DefaultHwPackageParser implements IHwPackageParser {
    private static final int INVALID_VALUE = -1;
    private static final Object M_INSTANCE_LOCK = new Object();
    private static DefaultHwPackageParser mInstance = null;

    @Override // android.content.pm.IHwPackageParser
    @HwSystemApi
    public boolean isDefaultFullScreen(String pkgName) {
        return false;
    }

    @HwSystemApi
    public static DefaultHwPackageParser getDefault() {
        if (mInstance == null) {
            synchronized (M_INSTANCE_LOCK) {
                if (mInstance == null) {
                    mInstance = new DefaultHwPackageParser();
                }
            }
        }
        return mInstance;
    }

    @Override // android.content.pm.IHwPackageParser
    public void initMetaData(PackageParserEx.ActivityEx activityEx) {
    }

    @Override // android.content.pm.IHwPackageParser
    @HwSystemApi
    public boolean needStopApp(String packageName, File packageDir) {
        return false;
    }

    @Override // android.content.pm.IHwPackageParser
    @HwSystemApi
    public float getDefaultNonFullMaxRatio() {
        return 0.0f;
    }

    @Override // android.content.pm.IHwPackageParser
    @HwSystemApi
    public float getDeviceMaxRatio() {
        return 0.0f;
    }

    @Override // android.content.pm.IHwPackageParser
    public float getExclusionNavBarMaxRatio() {
        return 0.0f;
    }

    @Override // android.content.pm.IHwPackageParser
    public boolean isFullScreenDevice() {
        return false;
    }

    @Override // android.content.pm.IHwPackageParser
    @SystemApi
    public void parseIntentFilterState(XmlResourceParser parser, String androidResources, AbsIntentFilter outInfo) {
    }

    @Override // android.content.pm.IHwPackageParser
    public void updateBaseProvider(PackageParserEx.PackageEx owner, ProviderInfo info, int splitIndex) {
    }

    @Override // android.content.pm.IHwPackageParser
    public Float getDefaultAspect(String pkgName, int versionCode) {
        return Float.valueOf(0.0f);
    }

    @Override // android.content.pm.IHwPackageParser
    public int getDisplayChangeAppRestartConfig(int type, String pkgName) {
        return -1;
    }
}
