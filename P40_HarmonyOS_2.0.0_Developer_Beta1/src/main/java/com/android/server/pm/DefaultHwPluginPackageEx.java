package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import android.util.ArraySet;
import java.io.File;
import java.util.List;

public class DefaultHwPluginPackageEx implements IHwPluginPackage {
    public int PLUGIN_MERGE_FAILD_EX = -1;
    public int PLUGIN_MERGE_INIT_EX = 0;
    public int PLUGIN_MERGE_INSTALLED_EMPTY_EX = 3;
    public int PLUGIN_MERGE_INSTALLED_FAILD_EX = -3;
    public int PLUGIN_MERGE_SUCCEEDED_EX = 1;
    public int PLUGIN_MERGE_SYSTEM_EMPTY_EX = 2;
    public int PLUGIN_MERGE_SYSTEM_FAILD_EX = -2;
    public String TAG_MERGEPLUGIN_BASE_EX = "mergePlugin_base";

    public boolean assertPluginConsistent(String tag, PackageParser.ApkLite apk, long versionCode, long pluginVersionCode) throws PackageManagerException {
        try {
            PackageParserEx.ApkLiteEx liteEx = new PackageParserEx.ApkLiteEx();
            liteEx.setApkLite(apk);
            return assertPluginConsistent(tag, liteEx, versionCode, pluginVersionCode);
        } catch (PackageManagerExceptionEx exceptionEx) {
            throw exceptionEx.getManagerException();
        }
    }

    public boolean assertPluginConsistent(String tag, PackageParserEx.ApkLiteEx apk, long versionCode, long pluginVersionCode) throws PackageManagerExceptionEx {
        return false;
    }

    public boolean checkVersion(PackageParser.ApkLite existBase, long versionCode, long pluginVersionCode) throws PackageManagerException {
        try {
            PackageParserEx.ApkLiteEx liteEx = new PackageParserEx.ApkLiteEx();
            liteEx.setApkLite(existBase);
            return checkVersion(liteEx, versionCode, pluginVersionCode);
        } catch (PackageManagerExceptionEx exceptionEx) {
            throw exceptionEx.getManagerException();
        }
    }

    public boolean checkVersion(PackageParserEx.ApkLiteEx existBase, long versionCode, long pluginVersionCode) throws PackageManagerExceptionEx {
        return false;
    }

    public void installMergePlugin(PackageParser.PackageLite existPackage, ArraySet<String> stagedSplits, List<File> inheritedFiles, List<String> instructionSets, List<String> nativeLibPaths) throws PackageManagerException {
        try {
            PackageParserEx.PackageLiteEx liteEx = new PackageParserEx.PackageLiteEx();
            liteEx.setPackageLite(existPackage);
            installMergePlugin(liteEx, stagedSplits, inheritedFiles, instructionSets, nativeLibPaths);
        } catch (PackageManagerExceptionEx exceptionEx) {
            throw exceptionEx.getManagerException();
        }
    }

    public void installMergePlugin(PackageParserEx.PackageLiteEx existPackage, ArraySet<String> arraySet, List<File> list, List<String> list2, List<String> list3) throws PackageManagerExceptionEx {
    }

    public int mergePluginCommit(PackageParser.Package systemPkg, PackageSetting installedPkg, PackageSetting disablePkg) throws PackageManagerException {
        try {
            PackageSettingEx installedPkgEx = new PackageSettingEx();
            installedPkgEx.setPackageSetting(installedPkg);
            PackageSettingEx disablePkgEx = new PackageSettingEx();
            disablePkgEx.setPackageSetting(disablePkg);
            return mergePluginCommit(new PackageParserEx.PackageEx(systemPkg), installedPkgEx, disablePkgEx);
        } catch (PackageManagerExceptionEx exceptionEx) {
            throw exceptionEx.getManagerException();
        }
    }

    public int mergePluginCommit(PackageParserEx.PackageEx systemPkg, PackageSettingEx installedPkg, PackageSettingEx disablePkg) throws PackageManagerExceptionEx {
        return 0;
    }

    public boolean renameStage() {
        return false;
    }
}
