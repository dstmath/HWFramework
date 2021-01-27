package com.android.server.pm;

import android.content.pm.PackageParser;
import android.util.ArraySet;
import java.io.File;
import java.util.List;

public interface IHwPluginPackage {
    public static final int PLUGIN_MERGE_FAILD = -1;
    public static final int PLUGIN_MERGE_INIT = 0;
    public static final int PLUGIN_MERGE_INSTALLED_EMPTY = 3;
    public static final int PLUGIN_MERGE_INSTALLED_FAILD = -3;
    public static final int PLUGIN_MERGE_SUCCEEDED = 1;
    public static final int PLUGIN_MERGE_SYSTEM_EMPTY = 2;
    public static final int PLUGIN_MERGE_SYSTEM_FAILD = -2;
    public static final String TAG_MERGEPLUGIN_BASE = "mergePlugin_base";

    boolean assertPluginConsistent(String str, PackageParser.ApkLite apkLite, long j, long j2) throws PackageManagerException;

    boolean checkVersion(PackageParser.ApkLite apkLite, long j, long j2) throws PackageManagerException;

    void installMergePlugin(PackageParser.PackageLite packageLite, ArraySet<String> arraySet, List<File> list, List<String> list2, List<String> list3) throws PackageManagerException;

    int mergePluginCommit(PackageParser.Package v, PackageSetting packageSetting, PackageSetting packageSetting2) throws PackageManagerException;

    boolean renameStage();
}
