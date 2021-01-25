package com.android.server.pm;

import android.content.pm.IHwPluginManager;
import android.content.pm.PackageParser;
import android.content.pm.dex.DexMetadataHelper;
import android.os.Environment;
import android.os.FileUtils;
import android.os.SELinux;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Base64;
import android.util.Slog;
import android.util.apk.ApkSignatureVerifier;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.util.ArrayUtils;
import com.android.server.pm.Installer;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HwPluginPackage implements IHwPluginPackage {
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
                return true;
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
                return true;
            }
            return false;
        }
    };
    private static final String TAG = "HwPluginPackage";
    private static String sDexFilterName = null;
    private static String sLibFilterPrefix = null;
    @GuardedBy({"mLock"})
    private File mInheritedInstalledFilesBase;
    @GuardedBy({"mLock"})
    private File mInheritedPresetFilesBase;
    @GuardedBy({"mLock"})
    private final List<File> mInstalledInheritedFiles = new ArrayList(10);
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private final List<File> mMergeInheritedFiles = new ArrayList(10);
    @GuardedBy({"mLock"})
    private final List<String> mMergeInstructionSets = new ArrayList(10);
    @GuardedBy({"mLock"})
    private final List<String> mMergeNativeLibPaths = new ArrayList(10);
    @GuardedBy({"mLock"})
    private File mMergeStageDir;
    @GuardedBy({"mLock"})
    private final List<File> mMergeStagedFiles = new ArrayList(10);
    @GuardedBy({"mLock"})
    private String mPackageName;
    private final IHwPackageManagerInner mPm;
    @GuardedBy({"mLock"})
    private final List<File> mPresetInheritedFiles = new ArrayList(10);
    private File mStageDir;

    public HwPluginPackage(IHwPackageManagerInner pm, String packageName) {
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
                                sb.append(AwarenessInnerConstants.DASH_KEY);
                                sLibFilterPrefix = sb.toString();
                                Slog.d(TAG, "sLibFilterPrefix: " + sLibFilterPrefix + " archSubDir: " + archSubDir);
                                libDirsToInherits.addAll(Arrays.asList(archSubDir.listFiles(LIB_FILTER)));
                            } catch (IOException e) {
                                Slog.e(TAG, "Skipping linking of native library directory!");
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
        if (isAddMeta && (dexMetadataFile = DexMetadataHelper.findDexMetadataForFile(pathFile)) != null) {
            outAddFiles.add(dexMetadataFile);
        }
        if (isAddOat) {
            File oatDir = new File(pathFile.getParentFile(), "oat");
            if (oatDir.exists()) {
                File[] archSubdirs = oatDir.listFiles();
                if (archSubdirs != null && archSubdirs.length > 0) {
                    String[] instructionSets = InstructionSets.getAllDexCodeInstructionSets();
                    for (File archSubDir : archSubdirs) {
                        if (ArrayUtils.contains(instructionSets, archSubDir.getName())) {
                            outInstructionSets.add(archSubDir.getName());
                            String name = pathFile.getName();
                            int lastIndex = name.lastIndexOf(".");
                            if (lastIndex >= 0) {
                                sDexFilterName = name.substring(0, lastIndex);
                                Slog.d(TAG, "sDexFilterName: " + sDexFilterName + " archSubDir: " + archSubDir);
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

    @GuardedBy({"mLock"})
    public boolean assertPluginConsistent(String tag, PackageParser.ApkLite apk, long versionCode, long pluginVersionCode) throws PackageManagerException {
        if (apk == null || "mergePlugin_base".equals(tag)) {
            return false;
        }
        if (!(versionCode > 0 && pluginVersionCode > 0) || IHwPluginManager.compareAPIMajorVersion(versionCode, pluginVersionCode) == 0) {
            boolean versionMajorCheck = pluginVersionCode > 0 && IHwPluginManager.compareAPIMajorVersion(apk.getLongVersionCode(), pluginVersionCode) != 0;
            if (!apk.isPlugin && versionMajorCheck) {
                throw new PackageManagerException(-2, tag + " Incompatible apk version " + apk.getLongVersionCode() + " with plugin version: " + pluginVersionCode);
            } else if (apk.isPlugin && IHwPluginManager.compareAPIMajorVersion(apk.getLongVersionCode(), pluginVersionCode) != 0) {
                throw new PackageManagerException(-2, tag + " Incompatible plugin version " + apk.getLongVersionCode() + " with " + pluginVersionCode);
            } else if (versionCode <= 0 || apk.isPlugin) {
                return false;
            } else {
                return true;
            }
        } else {
            throw new PackageManagerException(-2, tag + " Incompatible version " + versionCode + " with plugin version: " + pluginVersionCode);
        }
    }

    public boolean checkVersion(PackageParser.ApkLite existBase, long versionCode, long pluginVersionCode) throws PackageManagerException {
        if (existBase == null) {
            throw new PackageManagerException(-2, "checkVersion Missing existing base package!");
        } else if (versionCode >= 0 || pluginVersionCode >= 0) {
            if (!(versionCode > 0 && pluginVersionCode > 0) || IHwPluginManager.compareAPIMajorVersion(versionCode, pluginVersionCode) == 0) {
                int versionState = versionCode > 0 ? IHwPluginManager.compareAPIMajorVersion(existBase.getLongVersionCode(), versionCode) : 0;
                int pluginVersionState = pluginVersionCode > 0 ? IHwPluginManager.compareAPIMajorVersion(existBase.getLongVersionCode(), pluginVersionCode) : 0;
                if (versionState > 0 || pluginVersionState > 0) {
                    Slog.w(TAG, "MergePlugin version " + pluginVersionCode + AwarenessInnerConstants.COLON_KEY + versionCode + " is newer, incompatible with existBase version " + existBase.getLongVersionCode());
                    return false;
                } else if (versionState >= 0 && pluginVersionState >= 0) {
                    return true;
                } else {
                    Slog.w(TAG, "existBase version " + existBase.getLongVersionCode() + " is older, incompatible Plugin version " + pluginVersionCode + AwarenessInnerConstants.COLON_KEY + versionCode + " will drop!");
                    return false;
                }
            } else {
                throw new PackageManagerException(-2, "MergePlugin version " + pluginVersionCode + " is incompatible with " + versionCode);
            }
        } else {
            throw new PackageManagerException(-2, "checkVersion Illegal version!");
        }
    }

    public void installMergePlugin(PackageParser.PackageLite existPackage, ArraySet<String> stagedSplits, List<File> inheritedFiles, List<String> instructionSets, List<String> nativeLibPaths) throws PackageManagerException {
        if ((existPackage == null || inheritedFiles == null) || stagedSplits == null) {
            throw new PackageManagerException(-2, "Illegal input parameters!");
        } else if (!stagedSplits.contains(null)) {
            throw new PackageManagerException(-2, "Full install Merge Plugin must include a base package");
        } else if (!ArrayUtils.isEmpty(existPackage.splitNames)) {
            int length = existPackage.splitNames.length;
            for (int i = 0; i < length; i++) {
                String splitName = existPackage.splitNames[i];
                File splitFile = new File(existPackage.splitCodePaths[i]);
                if (!stagedSplits.contains(splitName) && (existPackage.splitPrivateFlags[i] & Integer.MIN_VALUE) != 0) {
                    inheritedFiles.add(splitFile);
                    addInheritedDex(splitFile, true, false, inheritedFiles, instructionSets);
                    if (nativeLibPaths != null) {
                        addInheritedLib(existPackage.splitCodePaths[i], splitName, inheritedFiles, nativeLibPaths);
                    }
                }
            }
        }
    }

    private File buildStageDir() {
        File stagingDir = Environment.getDataAppDirectory(StorageManager.UUID_PRIVATE_INTERNAL);
        return new File(stagingDir, "vmdl" + (new SecureRandom().nextInt(2147483646) + 1) + FILE_END_OF_TMP);
    }

    private File getNextCodePath(File targetDir, String packageName) {
        File result;
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        do {
            random.nextBytes(bytes);
            String suffix = Base64.encodeToString(bytes, 10);
            result = new File(targetDir, packageName + AwarenessInnerConstants.DASH_KEY + suffix);
        } while (result.exists());
        return result;
    }

    public boolean renameStage() {
        if (!this.mStageDir.exists()) {
            Slog.e(TAG, "renameStage mStageDir is not exists!");
            return false;
        }
        File targetDir = this.mStageDir.getParentFile();
        File beforeCodeFile = this.mStageDir;
        File afterCodeFile = this.mInheritedInstalledFilesBase;
        if (afterCodeFile == null) {
            afterCodeFile = getNextCodePath(targetDir, this.mPackageName);
        }
        Slog.d(TAG, "Renaming " + beforeCodeFile + " to " + afterCodeFile);
        try {
            Os.rename(beforeCodeFile.getAbsolutePath(), afterCodeFile.getAbsolutePath());
            if (SELinux.restoreconRecursive(afterCodeFile)) {
                return true;
            }
            Slog.w(TAG, "Failed to restorecon");
            return true;
        } catch (ErrnoException e) {
            Slog.w(TAG, "Failed to rename, ErrnoException.");
            return false;
        }
    }

    @GuardedBy({"mLock"})
    private File resolveStageDirLocked() throws IOException {
        if (this.mMergeStageDir == null) {
            File file = this.mStageDir;
            if (file != null) {
                PackageInstallerService.prepareStageDir(file);
                this.mMergeStageDir = this.mStageDir;
            } else {
                throw new IOException("Missing mStageDir");
            }
        }
        return this.mMergeStageDir;
    }

    public int mergePluginCommit(PackageParser.Package presetPkg, PackageSetting installedPs, PackageSetting disabledPkgSetting) throws PackageManagerException {
        if (presetPkg == null || installedPs == null) {
            return 0;
        }
        if (IHwPluginManager.compareAPIMajorVersion((long) presetPkg.mVersionCode, installedPs.versionCode) != 0) {
            Slog.e(TAG, "MergePlugin version code " + presetPkg.mVersionCode + " inconsistent with " + installedPs.versionCode);
            if (((long) presetPkg.mVersionCode) > installedPs.versionCode) {
                return -3;
            }
            return -2;
        }
        try {
            PackageParser.PackageLite installedLite = PackageParser.parsePackageLite(installedPs.codePath, 0);
            try {
                if (!ApkSignatureVerifier.unsafeGetCertsWithoutVerification(presetPkg.baseCodePath, 1).signaturesMatchExactly(ApkSignatureVerifier.unsafeGetCertsWithoutVerification(installedLite.baseCodePath, 1))) {
                    Slog.i(TAG, "installed package signatrue mismatch system");
                    return -3;
                }
            } catch (PackageParser.PackageParserException packageParserException) {
                if (!packageParserException.isMissBase) {
                    Slog.e(TAG, "Couldn't obtain signatures from base APK");
                    return -1;
                }
                File presetBaseFile = new File(presetPkg.baseCodePath);
                Slog.d(TAG, "mergePluginCommit bindFile base: " + installedPs.codePath + " to " + presetBaseFile);
                try {
                    copyFiles(Arrays.asList(presetBaseFile), installedPs.codePath, presetBaseFile.getParentFile());
                    installedLite = PackageParser.parsePackageLite(installedPs.codePath, 0);
                } catch (PackageParser.PackageParserException | IOException e) {
                    Slog.e(TAG, "Couldn't parse for " + presetPkg.packageName);
                    return -1;
                }
            }
            return collectMergePlugin(presetPkg, installedLite, disabledPkgSetting);
        } catch (PackageParser.PackageParserException e2) {
            Slog.e(TAG, "Couldn't parse for " + presetPkg.packageName, e2);
            return -1;
        }
    }

    private int collectMergePlugin(PackageParser.Package presetPkg, PackageParser.PackageLite installedLite, PackageSetting disabledPkgSetting) throws PackageManagerException {
        synchronized (this.mLock) {
            try {
                this.mInheritedPresetFilesBase = new File(presetPkg.codePath);
                this.mInheritedInstalledFilesBase = new File(installedLite.codePath);
                int ret = collectMergePluginLocked(presetPkg, installedLite, disabledPkgSetting);
                Slog.i(TAG, "collectMergePluginLocked ret: " + ret);
                if (ret != 1) {
                    return ret;
                }
                commitLocked();
                return ret;
            } catch (PackageManagerException e) {
                Slog.e(TAG, "mergePluginCommit PackageManagerException.");
                return -1;
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

    private void fixPresetSplit(PackageParser.Package presetPkg, PackageSetting disabledPkgSetting, boolean isPresetBetter, Map<String, SplitInfo> mergeMap) {
        List<String> disablePlugins;
        List<String> newDisablePlugins = new ArrayList<>(10);
        if (disabledPkgSetting == null || disabledPkgSetting.disablePlugins == null || disabledPkgSetting.disablePlugins.length <= 0) {
            disablePlugins = new ArrayList<>(10);
        } else {
            disablePlugins = new ArrayList<>(Arrays.asList(disabledPkgSetting.disablePlugins));
        }
        String[] presetPkgSplitNames = presetPkg.splitNames;
        if (presetPkgSplitNames == null) {
            presetPkgSplitNames = new String[0];
        }
        for (int i = 0; i < presetPkgSplitNames.length; i++) {
            String splitName = presetPkgSplitNames[i];
            boolean isPlugin = (presetPkg.splitPrivateFlags[i] & Integer.MIN_VALUE) != 0;
            if (!isPlugin || !disablePlugins.contains(splitName)) {
                if (isPresetBetter || isPlugin) {
                    mergeMap.put(splitName, new SplitInfo(splitName, presetPkg.splitCodePaths[i], presetPkg.splitVersionCodes[i], presetPkg.splitPrivateFlags[i], true));
                }
            } else if (!newDisablePlugins.contains(splitName)) {
                newDisablePlugins.add(splitName);
            }
        }
        if (disabledPkgSetting != null) {
            disabledPkgSetting.disablePlugins = (String[]) newDisablePlugins.toArray(new String[newDisablePlugins.size()]);
        }
    }

    private void fixInstalledSplit(PackageParser.PackageLite installedLite, boolean isPresetBetter, Map<String, SplitInfo> mergeMap) {
        if (ArrayUtils.isEmpty(installedLite.splitNames)) {
            Slog.i(TAG, "splitNames for " + installedLite.packageName + " is empty.");
            return;
        }
        for (int i = 0; i < installedLite.splitNames.length; i++) {
            String splitName = installedLite.splitNames[i];
            boolean isPlugin = (installedLite.splitPrivateFlags[i] & Integer.MIN_VALUE) != 0;
            SplitInfo splitInfo = mergeMap.get(splitName);
            if (splitInfo == null) {
                if (!isPresetBetter || isPlugin) {
                    mergeMap.put(splitName, new SplitInfo(splitName, installedLite.splitCodePaths[i], installedLite.splitVersionCodes[i], installedLite.splitPrivateFlags[i], false));
                }
            } else if (installedLite.splitVersionCodes[i] >= splitInfo.splitVersionCode) {
                splitInfo.splitVersionCode = installedLite.splitVersionCodes[i];
                splitInfo.splitCodePath = installedLite.splitCodePaths[i];
                splitInfo.splitPrivateFlag = installedLite.splitPrivateFlags[i];
                splitInfo.isPreset = false;
            }
        }
    }

    @GuardedBy({"mLock"})
    private int collectMergePluginLocked(PackageParser.Package presetPkg, PackageParser.PackageLite installedLite, PackageSetting disabledPkgSetting) throws PackageManagerException {
        boolean isPresetBetter = presetPkg.mVersionCode > installedLite.versionCode;
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
            String baseCodePath = presetPkg.baseCodePath;
            this.mPresetInheritedFiles.add(new File(baseCodePath));
            addInheritedLib(baseCodePath, "base", presetInheritedLibs, this.mMergeNativeLibPaths);
        } else {
            String baseCodePath2 = installedLite.baseCodePath;
            this.mInstalledInheritedFiles.add(new File(baseCodePath2));
            addInheritedLib(baseCodePath2, "base", installedInheritedLibs, this.mMergeNativeLibPaths);
        }
        if (this.mInstalledInheritedFiles.size() == 0) {
            return 3;
        }
        if (this.mPresetInheritedFiles.size() == 0) {
            return 2;
        }
        List<File> presetInheritedDexs = new ArrayList<>(10);
        List<File> installedInheritedDexs = new ArrayList<>(10);
        for (File pathFile : this.mPresetInheritedFiles) {
            addInheritedDex(pathFile, true, false, presetInheritedDexs, this.mMergeInstructionSets);
            installedInheritedDexs = installedInheritedDexs;
        }
        for (File pathFile2 : this.mInstalledInheritedFiles) {
            addInheritedDex(pathFile2, true, false, installedInheritedDexs, this.mMergeInstructionSets);
        }
        this.mPresetInheritedFiles.addAll(presetInheritedDexs);
        this.mInstalledInheritedFiles.addAll(installedInheritedDexs);
        this.mPresetInheritedFiles.addAll(presetInheritedLibs);
        this.mInstalledInheritedFiles.addAll(installedInheritedLibs);
        return 1;
    }

    @GuardedBy({"mLock"})
    private void commitLocked() throws PackageManagerException {
        try {
            this.mStageDir = buildStageDir();
            File toDir = resolveStageDirLocked();
            Slog.d(TAG, "Inherited installd: " + this.mInstalledInheritedFiles + " Inherited disable: " + this.mPresetInheritedFiles + " toDir: " + toDir);
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
            throw new PackageManagerException(-4, "Failed to inherit existing install", e);
        }
    }

    private void preCreateLibDirs(File toDir) throws PackageManagerException {
        if (!this.mMergeNativeLibPaths.isEmpty()) {
            try {
                for (String libPath : this.mMergeNativeLibPaths) {
                    int splitIndex = libPath.lastIndexOf(47);
                    if (splitIndex >= 0) {
                        if (splitIndex < libPath.length() - 1) {
                            File libDir = new File(toDir, libPath.substring(1, splitIndex));
                            if (!libDir.exists()) {
                                NativeLibraryHelper.createNativeLibrarySubdir(libDir);
                            }
                            NativeLibraryHelper.createNativeLibrarySubdir(new File(libDir, libPath.substring(splitIndex + 1)));
                        }
                    }
                    Slog.e(TAG, "Skipping native library creation for linking due to invalid path: " + libPath);
                }
            } catch (IOException e) {
                throw new PackageManagerException(-4, "Failed to inherit existing install", e);
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
            return true;
        } catch (ErrnoException e) {
            Slog.w(TAG, "Failed to detect if linking possible, occur ErrnoException.");
            return false;
        }
    }

    private void createOatDirs(List<String> instructionSets, File fromDir) throws PackageManagerException {
        for (String instructionSet : instructionSets) {
            try {
                this.mPm.getInstallerInner().createOatDir(fromDir.getAbsolutePath(), instructionSet);
            } catch (Installer.InstallerException e) {
                throw PackageManagerException.from(e);
            }
        }
    }

    private void linkFiles(List<File> fromFiles, File toDir, File fromDir) throws IOException {
        for (File fromFile : fromFiles) {
            String relativePath = getRelativePath(fromFile, fromDir);
            try {
                this.mPm.getInstallerInner().linkFile(relativePath, fromDir.getAbsolutePath(), toDir.getAbsolutePath());
            } catch (Installer.InstallerException e) {
                throw new IOException("failed linkOrCreateDir(" + relativePath + ", " + fromDir + ", " + toDir + ")", e);
            }
        }
        Slog.d(TAG, "Linked " + fromFiles.size() + " files into " + toDir);
    }

    private void copyFiles(List<File> fromFiles, File toDir, File fromDir) throws IOException {
        File[] listFiles = toDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().endsWith(FILE_END_OF_TMP)) {
                file.delete();
            }
        }
        int i = 1;
        boolean isTryBindLink = SystemProperties.getBoolean("ro.plugin.useBindMount", true);
        for (File fromFile : fromFiles) {
            String relativePath = getRelativePath(fromFile, fromDir);
            if (isTryBindLink) {
                File[] fileArr = new File[i];
                fileArr[0] = fromFile;
                if (isLinkPossible(Arrays.asList(fileArr), toDir)) {
                    File[] fileArr2 = new File[i];
                    fileArr2[0] = fromFile;
                    linkFiles(Arrays.asList(fileArr2), toDir, fromDir);
                    Slog.d(TAG, "linkFile success: " + relativePath + " from: " + fromDir + " -> " + toDir);
                } else {
                    try {
                        if (this.mPm.getInstallerInner().bindFile(relativePath, fromDir.getAbsolutePath(), toDir.getAbsolutePath())) {
                            Slog.d(TAG, "bindFile success: " + relativePath + " for: " + fromDir + " -> " + toDir);
                        }
                    } catch (Installer.InstallerException e) {
                        Slog.e(TAG, "failed bindFile (" + fromFile + " -> " + toDir + ")" + e);
                    }
                }
            }
            File fileToDir = new File(toDir + relativePath);
            File tmpFile = File.createTempFile("inherit", FILE_END_OF_TMP, fileToDir);
            Slog.d(TAG, "Copying " + fromFile + " to " + tmpFile);
            if (FileUtils.copyFile(fromFile, tmpFile)) {
                try {
                    Os.chmod(tmpFile.getAbsolutePath(), 420);
                    File toFile = new File(fileToDir, fromFile.getName());
                    Slog.d(TAG, "Renaming " + tmpFile + " to " + toFile);
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
        Slog.d(TAG, "Copied " + fromFiles.size() + " files into " + toDir);
    }

    private static void printSplitInfo(PackageParser.Package pkg) {
        StringBuilder sb = new StringBuilder(10);
        sb.append(pkg.packageName);
        sb.append(" codePath:");
        sb.append(pkg.codePath);
        sb.append(" longVersionCode:");
        sb.append(pkg.getLongVersionCode());
        sb.append(" baseCodePath:");
        sb.append(pkg.baseCodePath);
        sb.append(" baseRevisionCode:");
        sb.append(pkg.baseRevisionCode);
        sb.append(" splitNames:");
        sb.append(Arrays.toString(pkg.splitNames));
        sb.append(" splitRevisionCodes:");
        sb.append(Arrays.toString(pkg.splitRevisionCodes));
        sb.append(" splitCodePaths:");
        sb.append(Arrays.toString(pkg.splitCodePaths));
        Slog.i(TAG, sb.toString());
    }

    private static void printSplitInfo(PackageParser.PackageLite lite) {
        StringBuilder sb = new StringBuilder(10);
        sb.append(lite.packageName);
        sb.append(" codePath:");
        sb.append(lite.codePath);
        sb.append(" versionCode:");
        sb.append(lite.versionCode);
        sb.append(" versionCodeMajor:");
        sb.append(lite.versionCodeMajor);
        sb.append(" baseCodePath:");
        sb.append(lite.baseCodePath);
        sb.append(" baseRevisionCode:");
        sb.append(lite.baseRevisionCode);
        sb.append(" splitNames:");
        sb.append(Arrays.toString(lite.splitNames));
        sb.append(" splitVersionCodes:");
        sb.append(Arrays.toString(lite.splitVersionCodes));
        sb.append(" splitRevisionCodes:");
        sb.append(Arrays.toString(lite.splitRevisionCodes));
        sb.append(" splitCodePaths:");
        sb.append(Arrays.toString(lite.splitCodePaths));
        Slog.i(TAG, sb.toString());
    }
}
