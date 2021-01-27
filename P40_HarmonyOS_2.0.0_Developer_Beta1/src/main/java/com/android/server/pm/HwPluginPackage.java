package com.android.server.pm;

import android.content.pm.IHwPluginManager;
import android.content.pm.PackageParserEx;
import android.os.storage.StorageManagerEx;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Base64;
import android.util.apk.ApkSignatureVerifierEx;
import com.android.server.pm.InstallerEx;
import com.huawei.android.content.pm.dex.DexMetadataHelperEx;
import com.huawei.android.internal.content.NativeLibraryHelperEx;
import com.huawei.android.os.EnvironmentEx;
import com.huawei.android.os.FileUtilsEx;
import com.huawei.android.os.SELinuxEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.internal.util.ArrayUtilsEx;
import com.huawei.server.pm.InstructionSetsEx;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HwPluginPackage extends DefaultHwPluginPackageEx {
    private static final FileFilter DEX_FILTER = new FileFilter() {
        /* class com.android.server.pm.HwPluginPackage.AnonymousClass2 */

        @Override // java.io.FileFilter
        public boolean accept(File file) {
            String name;
            int lastIndex;
            if (file.isDirectory() || HwPluginPackage.sDexFilterName == null || (lastIndex = (name = file.getName()).lastIndexOf(".")) < 0) {
                return false;
            }
            if (HwPluginPackage.sDexFilterName.equals(name.substring(0, lastIndex))) {
                return HwPluginPackage.IS_LOGD;
            }
            return false;
        }
    };
    private static final String FILE_END_OF_TMP = ".tmp";
    private static final int INITIAL_CAPACITY = 10;
    private static final boolean IS_LOGD = true;
    private static final FileFilter LIB_FILTER = new FileFilter() {
        /* class com.android.server.pm.HwPluginPackage.AnonymousClass1 */

        @Override // java.io.FileFilter
        public boolean accept(File file) {
            if (!file.isDirectory() && file.getName().endsWith(".so") && file.getName().startsWith(HwPluginPackage.sLibFilterPrefix)) {
                return HwPluginPackage.IS_LOGD;
            }
            return false;
        }
    };
    private static final String TAG = "HwPluginPackage";
    private static String sDexFilterName = null;
    private static String sLibFilterPrefix = null;
    private File mInheritedInstalledFilesBase;
    private File mInheritedPresetFilesBase;
    private final List<File> mInstalledInheritedFiles = new ArrayList(10);
    private final Object mLock = new Object();
    private final List<File> mMergeInheritedFiles = new ArrayList(10);
    private final List<String> mMergeInstructionSets = new ArrayList(10);
    private final List<String> mMergeNativeLibPaths = new ArrayList(10);
    private File mMergeStageDir;
    private final List<File> mMergeStagedFiles = new ArrayList(10);
    private String mPackageName;
    private final IHwPackageManagerInnerEx mPm;
    private final List<File> mPresetInheritedFiles = new ArrayList(10);
    private File mStageDir;

    public HwPluginPackage(IHwPackageManagerInnerEx pm, String packageName) {
        this.mPm = pm;
        this.mPackageName = packageName;
    }

    private void addInheritedLib(String codePath, String splitName, List<File> outAddFiles, List<String> nativeLibPaths) {
        File[] fileArr;
        int i = 0;
        String filePath = codePath.substring(0, codePath.lastIndexOf(File.separator));
        File[] libDirs = {new File(filePath, "lib"), new File(filePath, "lib64")};
        int length = libDirs.length;
        int i2 = 0;
        while (i2 < length) {
            File libDir = libDirs[i2];
            if (libDir.exists()) {
                if (libDir.isDirectory()) {
                    List<File> libDirsToInherits = new LinkedList<>();
                    File[] listFiles = libDir.listFiles();
                    int length2 = listFiles.length;
                    int i3 = i;
                    while (i3 < length2) {
                        File archSubDir = listFiles[i3];
                        if (!archSubDir.isDirectory()) {
                            fileArr = listFiles;
                        } else {
                            try {
                                String relLibPath = getRelativePath(archSubDir, new File(filePath));
                                if (!nativeLibPaths.contains(relLibPath)) {
                                    nativeLibPaths.add(relLibPath);
                                }
                                StringBuilder sb = new StringBuilder();
                                sb.append("lib");
                                sb.append(splitName);
                                fileArr = listFiles;
                                sb.append("-");
                                sLibFilterPrefix = sb.toString();
                                SlogEx.d(TAG, "sLibFilterPrefix: " + sLibFilterPrefix + " archSubDir: " + archSubDir);
                                libDirsToInherits.addAll(Arrays.asList(archSubDir.listFiles(LIB_FILTER)));
                            } catch (IOException e) {
                                SlogEx.e(TAG, "Skipping linking of native library directory!");
                                libDirsToInherits.clear();
                            }
                        }
                        i3++;
                        listFiles = fileArr;
                    }
                    outAddFiles.addAll(libDirsToInherits);
                }
            }
            i2++;
            i = 0;
        }
    }

    private void addInheritedDex(File pathFile, boolean isAddMeta, boolean isAddOat, List<File> outAddFiles, List<String> outInstructionSets) {
        File dexMetadataFile;
        if (isAddMeta && (dexMetadataFile = DexMetadataHelperEx.findDexMetadataForFile(pathFile)) != null) {
            outAddFiles.add(dexMetadataFile);
        }
        if (isAddOat) {
            File oatDir = new File(pathFile.getParentFile(), "oat");
            if (oatDir.exists()) {
                File[] archSubdirs = oatDir.listFiles();
                if (archSubdirs != null && archSubdirs.length > 0) {
                    String[] instructionSets = InstructionSetsEx.getAllDexCodeInstructionSets();
                    for (File archSubDir : archSubdirs) {
                        if (ArrayUtilsEx.contains(instructionSets, archSubDir.getName())) {
                            outInstructionSets.add(archSubDir.getName());
                            String name = pathFile.getName();
                            int lastIndex = name.lastIndexOf(".");
                            if (lastIndex >= 0) {
                                sDexFilterName = name.substring(0, lastIndex);
                                SlogEx.d(TAG, "sDexFilterName: " + sDexFilterName + " archSubDir: " + archSubDir);
                                List<File> oatFiles = Arrays.asList(archSubDir.listFiles(DEX_FILTER));
                                if (!oatFiles.isEmpty()) {
                                    outAddFiles.addAll(oatFiles);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static String getRelativePath(File file, File base) throws IOException {
        String pathStr = file.getAbsolutePath();
        String baseStr = base.getAbsolutePath();
        if (pathStr.contains("/.")) {
            throw new IOException("Invalid path (was relative) : " + pathStr);
        } else if (pathStr.startsWith(baseStr)) {
            return pathStr.substring(baseStr.length());
        } else {
            throw new IOException("File: " + pathStr + " outside base: " + baseStr);
        }
    }

    public boolean assertPluginConsistent(String tag, PackageParserEx.ApkLiteEx apk, long versionCode, long pluginVersionCode) throws PackageManagerExceptionEx {
        if (apk == null || this.TAG_MERGEPLUGIN_BASE_EX.equals(tag)) {
            return false;
        }
        if (!(versionCode > 0 && pluginVersionCode > 0) || IHwPluginManager.compareAPIMajorVersion(versionCode, pluginVersionCode) == 0) {
            boolean versionMajorCheck = pluginVersionCode > 0 && IHwPluginManager.compareAPIMajorVersion(apk.getLongVersionCode(), pluginVersionCode) != 0;
            if (!apk.isPlugin() && versionMajorCheck) {
                throw new PackageManagerExceptionEx(-2, tag + " Incompatible apk version " + apk.getLongVersionCode() + " with plugin version: " + pluginVersionCode);
            } else if (apk.isPlugin() && IHwPluginManager.compareAPIMajorVersion(apk.getLongVersionCode(), pluginVersionCode) != 0) {
                throw new PackageManagerExceptionEx(-2, tag + " Incompatible plugin version " + apk.getLongVersionCode() + " with " + pluginVersionCode);
            } else if (versionCode <= 0 || apk.isPlugin()) {
                return false;
            } else {
                return IS_LOGD;
            }
        } else {
            throw new PackageManagerExceptionEx(-2, tag + " Incompatible version " + versionCode + " with plugin version: " + pluginVersionCode);
        }
    }

    public boolean checkVersion(PackageParserEx.ApkLiteEx existBase, long versionCode, long pluginVersionCode) throws PackageManagerExceptionEx {
        if (existBase == null) {
            throw new PackageManagerExceptionEx(-2, "checkVersion Missing existing base package!");
        } else if (versionCode >= 0 || pluginVersionCode >= 0) {
            if (!(versionCode > 0 && pluginVersionCode > 0) || IHwPluginManager.compareAPIMajorVersion(versionCode, pluginVersionCode) == 0) {
                int versionState = versionCode > 0 ? IHwPluginManager.compareAPIMajorVersion(existBase.getLongVersionCode(), versionCode) : 0;
                int pluginVersionState = pluginVersionCode > 0 ? IHwPluginManager.compareAPIMajorVersion(existBase.getLongVersionCode(), pluginVersionCode) : 0;
                if (versionState > 0 || pluginVersionState > 0) {
                    SlogEx.w(TAG, "MergePlugin version " + pluginVersionCode + ":" + versionCode + " is newer, incompatible with existBase version " + existBase.getLongVersionCode());
                    return false;
                } else if (versionState >= 0 && pluginVersionState >= 0) {
                    return IS_LOGD;
                } else {
                    SlogEx.w(TAG, "existBase version " + existBase.getLongVersionCode() + " is older, incompatible Plugin version " + pluginVersionCode + ":" + versionCode + " will drop!");
                    return false;
                }
            } else {
                throw new PackageManagerExceptionEx(-2, "MergePlugin version " + pluginVersionCode + " is incompatible with " + versionCode);
            }
        } else {
            throw new PackageManagerExceptionEx(-2, "checkVersion Illegal version!");
        }
    }

    public void installMergePlugin(PackageParserEx.PackageLiteEx existPackage, ArraySet<String> stagedSplits, List<File> inheritedFiles, List<String> instructionSets, List<String> nativeLibPaths) throws PackageManagerExceptionEx {
        if (((existPackage == null || inheritedFiles == null) ? IS_LOGD : false) || stagedSplits == null) {
            throw new PackageManagerExceptionEx(-2, "Illegal input parameters!");
        } else if (!stagedSplits.contains(null)) {
            throw new PackageManagerExceptionEx(-2, "Full install Merge Plugin must include a base package");
        } else if (!ArrayUtilsEx.isEmpty(existPackage.getSplitNames())) {
            int length = existPackage.getSplitNames().length;
            for (int i = 0; i < length; i++) {
                String splitName = existPackage.getSplitNames()[i];
                File splitFile = new File(existPackage.getSplitCodePaths()[i]);
                if (!stagedSplits.contains(splitName)) {
                    if ((existPackage.getSplitPrivateFlags()[i] & Integer.MIN_VALUE) != 0) {
                        inheritedFiles.add(splitFile);
                        addInheritedDex(splitFile, IS_LOGD, false, inheritedFiles, instructionSets);
                        if (nativeLibPaths != null) {
                            addInheritedLib(existPackage.getSplitCodePaths()[i], splitName, inheritedFiles, nativeLibPaths);
                        }
                    }
                }
            }
        }
    }

    private File buildStageDir() {
        File stagingDir = EnvironmentEx.getDataAppDirectory(StorageManagerEx.UUID_PRIVATE_INTERNAL);
        return new File(stagingDir, "vmdl" + (new SecureRandom().nextInt(2147483646) + 1) + FILE_END_OF_TMP);
    }

    private File getNextCodePath(File targetDir, String packageName) {
        File result;
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        do {
            random.nextBytes(bytes);
            String suffix = Base64.encodeToString(bytes, 10);
            result = new File(targetDir, packageName + "-" + suffix);
        } while (result.exists());
        return result;
    }

    public boolean renameStage() {
        if (!this.mStageDir.exists()) {
            SlogEx.e(TAG, "renameStage mStageDir is not exists!");
            return false;
        }
        File targetDir = this.mStageDir.getParentFile();
        File beforeCodeFile = this.mStageDir;
        File afterCodeFile = this.mInheritedInstalledFilesBase;
        if (afterCodeFile == null) {
            afterCodeFile = getNextCodePath(targetDir, this.mPackageName);
        }
        SlogEx.d(TAG, "Renaming " + beforeCodeFile + " to " + afterCodeFile);
        try {
            Os.rename(beforeCodeFile.getAbsolutePath(), afterCodeFile.getAbsolutePath());
            if (SELinuxEx.restoreconRecursive(afterCodeFile)) {
                return IS_LOGD;
            }
            SlogEx.w(TAG, "Failed to restorecon");
            return IS_LOGD;
        } catch (ErrnoException e) {
            SlogEx.w(TAG, "Failed to rename, ErrnoException.");
            return false;
        }
    }

    private File resolveStageDirLocked() throws IOException {
        if (this.mMergeStageDir == null) {
            File file = this.mStageDir;
            if (file != null) {
                PackageInstallerServiceEx.prepareStageDir(file);
                this.mMergeStageDir = this.mStageDir;
            } else {
                throw new IOException("Missing mStageDir");
            }
        }
        return this.mMergeStageDir;
    }

    public int mergePluginCommit(PackageParserEx.PackageEx presetPkg, PackageSettingEx installedPs, PackageSettingEx disabledPkgSetting) throws PackageManagerExceptionEx {
        PackageParserEx.PackageLiteEx installedLite;
        if (presetPkg == null || installedPs == null || installedPs.isObjNull()) {
            return this.PLUGIN_MERGE_INIT_EX;
        }
        if (IHwPluginManager.compareAPIMajorVersion((long) presetPkg.getVersionCode(), installedPs.getVersionCode()) != 0) {
            SlogEx.e(TAG, "MergePlugin version code " + presetPkg.getVersionCode() + " inconsistent with " + installedPs.getVersionCode());
            return ((long) presetPkg.getVersionCode()) > installedPs.getVersionCode() ? this.PLUGIN_MERGE_INSTALLED_FAILD_EX : this.PLUGIN_MERGE_SYSTEM_FAILD_EX;
        }
        try {
            installedLite = PackageParserEx.parsePackageLite(installedPs.getCodePath(), 0);
        } catch (PackageParserEx.PackageParserExceptionEx packageParserException) {
            if (!packageParserException.isMissBase()) {
                SlogEx.e(TAG, "Couldn't parse for " + presetPkg.getPackageName());
                return this.PLUGIN_MERGE_FAILD_EX;
            }
            File presetBaseFile = new File(presetPkg.getBaseCodePath());
            SlogEx.i(TAG, "mergePluginCommit bindFile base: " + installedPs.getCodePath() + " to " + presetBaseFile);
            try {
                copyFiles(Arrays.asList(presetBaseFile), installedPs.getCodePath(), presetBaseFile.getParentFile());
                installedLite = PackageParserEx.parsePackageLite(installedPs.getCodePath(), 0);
            } catch (PackageParserEx.PackageParserExceptionEx | IOException e) {
                SlogEx.e(TAG, "Couldn't parse for " + presetPkg.getPackageName());
                return this.PLUGIN_MERGE_FAILD_EX;
            }
        }
        try {
            if (ApkSignatureVerifierEx.unsafeGetCertsWithoutVerification(presetPkg.getBaseCodePath(), PackageParserEx.SigningDetailsEx.getSignatureSchemeVersionJAR()).signaturesMatchExactly(ApkSignatureVerifierEx.unsafeGetCertsWithoutVerification(installedLite.getBaseCodePath(), PackageParserEx.SigningDetailsEx.getSignatureSchemeVersionJAR()))) {
                return collectMergePlugin(presetPkg, installedLite, disabledPkgSetting);
            }
            SlogEx.i(TAG, "installed package signatrue mismatch system");
            return this.PLUGIN_MERGE_INSTALLED_FAILD_EX;
        } catch (PackageParserEx.PackageParserExceptionEx e2) {
            SlogEx.e(TAG, "Couldn't obtain signatures from base APK");
            return this.PLUGIN_MERGE_FAILD_EX;
        }
    }

    private int collectMergePlugin(PackageParserEx.PackageEx presetPkg, PackageParserEx.PackageLiteEx installedLite, PackageSettingEx disabledPkgSetting) throws PackageManagerExceptionEx {
        int i = this.PLUGIN_MERGE_FAILD_EX;
        synchronized (this.mLock) {
            try {
                this.mInheritedPresetFilesBase = new File(presetPkg.getCodePath());
                this.mInheritedInstalledFilesBase = new File(installedLite.getCodePath());
                int ret = collectMergePluginLocked(presetPkg, installedLite, disabledPkgSetting);
                SlogEx.i(TAG, "collectMergePluginLocked ret: " + ret);
                if (ret != this.PLUGIN_MERGE_SUCCEEDED_EX) {
                    return ret;
                }
                commitLocked();
                return ret;
            } catch (PackageManagerExceptionEx e) {
                SlogEx.e(TAG, "mergePluginCommit PackageManagerException.");
                return this.PLUGIN_MERGE_FAILD_EX;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SplitInfo {
        boolean isPreset;
        String splitCodePath;
        String splitName;
        int splitPrivateFlag;
        int splitVersionCode;

        SplitInfo() {
        }

        SplitInfo(String splitName2, String splitCodePath2, int splitVersionCode2, int splitPrivateFlag2, boolean isPreset2) {
            this.splitName = splitName2;
            this.splitCodePath = splitCodePath2;
            this.splitVersionCode = splitVersionCode2;
            this.splitPrivateFlag = splitPrivateFlag2;
            this.isPreset = isPreset2;
        }
    }

    private void fixPresetSplit(PackageParserEx.PackageEx presetPkg, PackageSettingEx disabledPkgSetting, boolean isPresetBetter, Map<String, SplitInfo> mergeMap) {
        List<String> disablePlugins;
        List<String> newDisablePlugins = new ArrayList<>(10);
        if (disabledPkgSetting == null || disabledPkgSetting.getDisablePlugins() == null || disabledPkgSetting.getDisablePlugins().length <= 0) {
            disablePlugins = new ArrayList<>(10);
        } else {
            disablePlugins = new ArrayList<>(Arrays.asList(disabledPkgSetting.getDisablePlugins()));
        }
        String[] presetPkgSplitNames = presetPkg.splitNames;
        if (presetPkgSplitNames == null) {
            presetPkgSplitNames = new String[0];
        }
        for (int i = 0; i < presetPkgSplitNames.length; i++) {
            String splitName = presetPkgSplitNames[i];
            boolean isPlugin = (presetPkg.getSplitPrivateFlags()[i] & Integer.MIN_VALUE) != 0 ? IS_LOGD : false;
            if (!isPlugin || !disablePlugins.contains(splitName)) {
                if (isPresetBetter || isPlugin) {
                    mergeMap.put(splitName, new SplitInfo(splitName, presetPkg.getSplitCodePaths()[i], presetPkg.getSplitVersionCodes()[i], presetPkg.getSplitPrivateFlags()[i], IS_LOGD));
                }
            } else if (!newDisablePlugins.contains(splitName)) {
                newDisablePlugins.add(splitName);
            }
        }
        if (disabledPkgSetting != null) {
            disabledPkgSetting.setDisablePlugins((String[]) newDisablePlugins.toArray(new String[newDisablePlugins.size()]));
        }
    }

    private void fixInstalledSplit(PackageParserEx.PackageLiteEx installedLite, boolean isPresetBetter, Map<String, SplitInfo> mergeMap) {
        if (ArrayUtilsEx.isEmpty(installedLite.getSplitNames())) {
            SlogEx.i(TAG, "splitNames for " + installedLite.getPackageName() + " is empty.");
            return;
        }
        for (int i = 0; i < installedLite.getSplitNames().length; i++) {
            String splitName = installedLite.getSplitNames()[i];
            boolean isPlugin = (installedLite.getSplitPrivateFlags()[i] & Integer.MIN_VALUE) != 0 ? IS_LOGD : false;
            SplitInfo splitInfo = mergeMap.get(splitName);
            if (splitInfo == null) {
                if (!isPresetBetter || isPlugin) {
                    mergeMap.put(splitName, new SplitInfo(splitName, installedLite.getSplitCodePaths()[i], installedLite.getSplitVersionCodes()[i], installedLite.getSplitPrivateFlags()[i], false));
                }
            } else if (installedLite.getSplitVersionCodes()[i] >= splitInfo.splitVersionCode) {
                splitInfo.splitVersionCode = installedLite.getSplitVersionCodes()[i];
                splitInfo.splitCodePath = installedLite.getSplitCodePaths()[i];
                splitInfo.splitPrivateFlag = installedLite.getSplitPrivateFlags()[i];
                splitInfo.isPreset = false;
            }
        }
    }

    private int collectMergePluginLocked(PackageParserEx.PackageEx presetPkg, PackageParserEx.PackageLiteEx installedLite, PackageSettingEx disabledPkgSetting) throws PackageManagerExceptionEx {
        boolean isPresetBetter = presetPkg.getVersionCode() > installedLite.getVersionCode() ? IS_LOGD : false;
        Map<String, SplitInfo> mergeMap = new ArrayMap<>(10);
        fixPresetSplit(presetPkg, disabledPkgSetting, isPresetBetter, mergeMap);
        fixInstalledSplit(installedLite, isPresetBetter, mergeMap);
        List<File> presetInheritedLibs = new ArrayList<>(10);
        List<File> installedInheritedLibs = new ArrayList<>(10);
        for (SplitInfo splitInfo : mergeMap.values()) {
            if (splitInfo != null) {
                if (splitInfo.isPreset) {
                    this.mPresetInheritedFiles.add(new File(splitInfo.splitCodePath));
                    addInheritedLib(splitInfo.splitCodePath, splitInfo.splitName, presetInheritedLibs, this.mMergeNativeLibPaths);
                } else {
                    this.mInstalledInheritedFiles.add(new File(splitInfo.splitCodePath));
                    addInheritedLib(splitInfo.splitCodePath, splitInfo.splitName, installedInheritedLibs, this.mMergeNativeLibPaths);
                }
            }
        }
        if (isPresetBetter) {
            String baseCodePath = presetPkg.getBaseCodePath();
            this.mPresetInheritedFiles.add(new File(baseCodePath));
            addInheritedLib(baseCodePath, "base", presetInheritedLibs, this.mMergeNativeLibPaths);
        } else {
            String baseCodePath2 = installedLite.getBaseCodePath();
            this.mInstalledInheritedFiles.add(new File(baseCodePath2));
            addInheritedLib(baseCodePath2, "base", installedInheritedLibs, this.mMergeNativeLibPaths);
        }
        if (this.mInstalledInheritedFiles.size() == 0) {
            return this.PLUGIN_MERGE_INSTALLED_EMPTY_EX;
        }
        if (this.mPresetInheritedFiles.size() == 0) {
            return this.PLUGIN_MERGE_SYSTEM_EMPTY_EX;
        }
        List<File> presetInheritedDexs = new ArrayList<>(10);
        List<File> installedInheritedDexs = new ArrayList<>(10);
        for (File pathFile : this.mPresetInheritedFiles) {
            addInheritedDex(pathFile, IS_LOGD, false, presetInheritedDexs, this.mMergeInstructionSets);
        }
        for (File pathFile2 : this.mInstalledInheritedFiles) {
            addInheritedDex(pathFile2, IS_LOGD, false, installedInheritedDexs, this.mMergeInstructionSets);
        }
        this.mPresetInheritedFiles.addAll(presetInheritedDexs);
        this.mInstalledInheritedFiles.addAll(installedInheritedDexs);
        this.mPresetInheritedFiles.addAll(presetInheritedLibs);
        this.mInstalledInheritedFiles.addAll(installedInheritedLibs);
        return this.PLUGIN_MERGE_SUCCEEDED_EX;
    }

    private void commitLocked() throws PackageManagerExceptionEx {
        try {
            this.mStageDir = buildStageDir();
            File toDir = resolveStageDirLocked();
            SlogEx.d(TAG, "Inherited installd: " + this.mInstalledInheritedFiles + " Inherited disable: " + this.mPresetInheritedFiles + " toDir: " + toDir);
            if (this.mPresetInheritedFiles.isEmpty()) {
                return;
            }
            if (!this.mInstalledInheritedFiles.isEmpty()) {
                if (isLinkPossible(this.mInstalledInheritedFiles, toDir)) {
                    if (!this.mMergeInstructionSets.isEmpty()) {
                        createOatDirs(this.mMergeInstructionSets, new File(toDir, "oat"));
                    }
                    preCreateLibDirs(toDir);
                    linkFiles(this.mInstalledInheritedFiles, toDir, this.mInheritedInstalledFilesBase);
                } else {
                    copyFiles(this.mInstalledInheritedFiles, toDir, this.mInheritedInstalledFilesBase);
                }
                copyFiles(this.mPresetInheritedFiles, toDir, this.mInheritedPresetFilesBase);
            }
        } catch (IOException e) {
            throw new PackageManagerExceptionEx(-4, "Failed to inherit existing install", e);
        }
    }

    private void preCreateLibDirs(File toDir) throws PackageManagerExceptionEx {
        if (!this.mMergeNativeLibPaths.isEmpty()) {
            try {
                for (String libPath : this.mMergeNativeLibPaths) {
                    int splitIndex = libPath.lastIndexOf(47);
                    if (splitIndex >= 0) {
                        if (splitIndex < libPath.length() - 1) {
                            File libDir = new File(toDir, libPath.substring(1, splitIndex));
                            if (!libDir.exists()) {
                                NativeLibraryHelperEx.createNativeLibrarySubdir(libDir);
                            }
                            NativeLibraryHelperEx.createNativeLibrarySubdir(new File(libDir, libPath.substring(splitIndex + 1)));
                        }
                    }
                    SlogEx.e(TAG, "Skipping native library creation for linking due to invalid path: " + libPath);
                }
            } catch (IOException e) {
                throw new PackageManagerExceptionEx(-4, "Failed to inherit existing install", e);
            }
        }
    }

    private boolean isLinkPossible(List<File> fromFiles, File toDir) {
        try {
            StructStat toStat = Os.stat(toDir.getAbsolutePath());
            for (File fromFile : fromFiles) {
                if (Os.stat(fromFile.getAbsolutePath()).st_dev != toStat.st_dev) {
                    return false;
                }
            }
            return IS_LOGD;
        } catch (ErrnoException e) {
            SlogEx.w(TAG, "Failed to detect if linking possible, occur ErrnoException.");
            return false;
        }
    }

    private void createOatDirs(List<String> instructionSets, File fromDir) throws PackageManagerExceptionEx {
        for (String instructionSet : instructionSets) {
            try {
                this.mPm.getInstallerInner().createOatDir(fromDir.getAbsolutePath(), instructionSet);
            } catch (InstallerEx.InstallerExceptionEx e) {
                throw new PackageManagerExceptionEx(-110, e.getMessage(), e.getCause());
            }
        }
    }

    private void linkFiles(List<File> fromFiles, File toDir, File fromDir) throws IOException {
        for (File fromFile : fromFiles) {
            String relativePath = getRelativePath(fromFile, fromDir);
            try {
                this.mPm.getInstallerInner().linkFile(relativePath, fromDir.getAbsolutePath(), toDir.getAbsolutePath());
            } catch (InstallerEx.InstallerExceptionEx e) {
                throw new IOException("failed linkOrCreateDir(" + relativePath + ", " + fromDir + ", " + toDir + ")", e);
            }
        }
        SlogEx.d(TAG, "Linked " + fromFiles.size() + " files into " + toDir);
    }

    private void copyFiles(List<File> fromFiles, File toDir, File fromDir) throws IOException {
        File[] listFiles = toDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().endsWith(FILE_END_OF_TMP)) {
                file.delete();
            }
        }
        int i = 1;
        boolean isTryBindLink = SystemPropertiesEx.getBoolean("ro.plugin.useBindMount", (boolean) IS_LOGD);
        for (File fromFile : fromFiles) {
            String relativePath = getRelativePath(fromFile, fromDir);
            if (isTryBindLink) {
                File[] fileArr = new File[i];
                fileArr[0] = fromFile;
                if (isLinkPossible(Arrays.asList(fileArr), toDir)) {
                    File[] fileArr2 = new File[i];
                    fileArr2[0] = fromFile;
                    linkFiles(Arrays.asList(fileArr2), toDir, fromDir);
                    SlogEx.d(TAG, "linkFile success: " + relativePath + " from: " + fromDir + " -> " + toDir);
                } else {
                    try {
                        if (this.mPm.getInstallerInner().bindFile(relativePath, fromDir.getAbsolutePath(), toDir.getAbsolutePath())) {
                            SlogEx.d(TAG, "bindFile success: " + relativePath + " for: " + fromDir + " -> " + toDir);
                        }
                    } catch (InstallerEx.InstallerExceptionEx e) {
                        SlogEx.e(TAG, "failed bindFile (" + fromFile + " -> " + toDir + ")" + e);
                    }
                }
            }
            File fileToDir = new File(toDir + relativePath);
            File tmpFile = File.createTempFile("inherit", FILE_END_OF_TMP, fileToDir);
            SlogEx.d(TAG, "Copying " + fromFile + " to " + tmpFile);
            if (FileUtilsEx.copyFile(fromFile, tmpFile)) {
                try {
                    Os.chmod(tmpFile.getAbsolutePath(), 420);
                    File toFile = new File(fileToDir, fromFile.getName());
                    SlogEx.d(TAG, "Renaming " + tmpFile + " to " + toFile);
                    if (tmpFile.renameTo(toFile)) {
                        i = 1;
                    } else {
                        throw new IOException("Failed to rename " + tmpFile + " to " + toFile);
                    }
                } catch (ErrnoException e2) {
                    throw new IOException("Failed to chmod " + tmpFile);
                }
            } else {
                throw new IOException("Failed to copy " + fromFile + " to " + tmpFile);
            }
        }
        SlogEx.d(TAG, "Copied " + fromFiles.size() + " files into " + toDir);
    }

    private static void printSplitInfo(PackageParserEx.PackageEx pkg) {
        StringBuilder sb = new StringBuilder(10);
        sb.append(pkg.getPackageName());
        sb.append(" codePath:");
        sb.append(pkg.getCodePath());
        sb.append(" longVersionCode:");
        sb.append(pkg.getLongVersionCode());
        sb.append(" baseCodePath:");
        sb.append(pkg.getBaseCodePath());
        sb.append(" baseRevisionCode:");
        sb.append(pkg.getBaseRevisionCode());
        sb.append(" splitNames:");
        sb.append(Arrays.toString(pkg.splitNames));
        sb.append(" splitRevisionCodes:");
        sb.append(Arrays.toString(pkg.getSplitRevisionCodes()));
        sb.append(" splitCodePaths:");
        sb.append(Arrays.toString(pkg.getSplitCodePaths()));
        SlogEx.i(TAG, sb.toString());
    }

    private static void printSplitInfo(PackageParserEx.PackageLiteEx lite) {
        StringBuilder sb = new StringBuilder(10);
        sb.append(lite.getPackageName());
        sb.append(" codePath:");
        sb.append(lite.getCodePath());
        sb.append(" versionCode:");
        sb.append(lite.getVersionCode());
        sb.append(" versionCodeMajor:");
        sb.append(lite.getVersionCodeMajor());
        sb.append(" baseCodePath:");
        sb.append(lite.getBaseCodePath());
        sb.append(" baseRevisionCode:");
        sb.append(lite.getBaseRevisionCode());
        sb.append(" splitNames:");
        sb.append(Arrays.toString(lite.getSplitNames()));
        sb.append(" splitVersionCodes:");
        sb.append(Arrays.toString(lite.getSplitVersionCodes()));
        sb.append(" splitRevisionCodes:");
        sb.append(Arrays.toString(lite.getSplitRevisionCodes()));
        sb.append(" splitCodePaths:");
        sb.append(Arrays.toString(lite.getSplitCodePaths()));
        SlogEx.i(TAG, sb.toString());
    }
}
