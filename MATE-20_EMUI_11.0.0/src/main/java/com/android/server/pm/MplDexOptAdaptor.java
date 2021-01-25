package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.dex.ArtManager;
import android.content.pm.dex.DexMetadataHelper;
import android.os.Binder;
import android.os.Build;
import android.os.IInstalld;
import android.os.Process;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.pm.CompilerStats;
import com.android.server.pm.Installer;
import com.android.server.pm.dex.DexManager;
import com.android.server.pm.dex.DexoptOptions;
import com.android.server.pm.dex.DexoptUtils;
import com.android.server.pm.dex.PackageDexUsage;
import dalvik.system.VMRuntime;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MplDexOptAdaptor {
    private static final int MAX_NUM = 500;
    static final int MODE_BOOT = 1;
    static final int MODE_HOTA = 0;
    static final int MODE_IDLE = 2;
    private static final String TAG = "MplDexOptAdaptor";
    private static MplDexOptAdaptor sInstance = new MplDexOptAdaptor();
    private SparseArray<ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>>> mDexOptNeededArray = new SparseArray<>(2);
    private ArrayList<DexOptPathParam> mDexOptPathParamList = new ArrayList<>();
    private Installer mInstaller;
    private PackageDexOptimizer mPackageDexOptimizer;

    public static MplDexOptAdaptor getInstance() {
        return sInstance;
    }

    private MplDexOptAdaptor() {
    }

    public void setInstaller(PackageDexOptimizer pdo, Installer installer) {
        this.mPackageDexOptimizer = pdo;
        this.mInstaller = installer;
    }

    /* access modifiers changed from: private */
    public static class DexOptPathParam {
        String mCLContext;
        int mCompilationReason;
        String mCompilerFilter;
        String mDexMetadataPath;
        int mDexOptFlags;
        int mDexOptNeeded;
        boolean mDownGrade;
        String mIsa;
        String mPath;
        String mProfileName;
        boolean mProfileUpdated;
        int mUid;

        private DexOptPathParam(String path, String isa, String compilerFilter, String clContext, String profileName, String dexMetadataPath, boolean profileUpdated, boolean downGrade, int uid, int compilationReason, int dexoptFlags) {
            this.mPath = path;
            this.mIsa = isa;
            this.mCompilerFilter = compilerFilter;
            this.mCLContext = clContext;
            this.mProfileName = profileName;
            this.mDexMetadataPath = dexMetadataPath;
            this.mProfileUpdated = profileUpdated;
            this.mDownGrade = downGrade;
            this.mUid = uid;
            this.mCompilationReason = compilationReason;
            this.mDexOptFlags = dexoptFlags;
            this.mDexOptNeeded = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void dexOptParamPrepare() {
        this.mDexOptPathParamList.clear();
    }

    /* access modifiers changed from: package-private */
    public void dexOptParamAdd(String path, String isa, String compilerFilter, String clContext, String profileName, String dexMetadataPath, boolean profileUpdated, boolean downGrade, int uid, int compilationReason, int dexoptFlags) {
        this.mDexOptPathParamList.add(new DexOptPathParam(path, isa, compilerFilter, clContext, profileName, dexMetadataPath, profileUpdated, downGrade, uid, compilationReason, dexoptFlags));
    }

    /* JADX INFO: Multiple debug info for r5v7 java.lang.String[]: [D('retDexopt' int[]), D('dexPaths' java.lang.String[])] */
    /* access modifiers changed from: package-private */
    public int dexOptParamProcess(PackageParser.Package pkg, CompilerStats.PackageStats packageStats) {
        int[] retDexopt;
        MplDexOptAdaptor mplDexOptAdaptor = this;
        int result = 0;
        if (mplDexOptAdaptor.mDexOptPathParamList.size() <= 0) {
            return 0;
        }
        int size = mplDexOptAdaptor.mDexOptPathParamList.size();
        ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> dexOptNeededMap = mplDexOptAdaptor.mDexOptNeededArray.get(Process.myTid());
        ArrayList<DexOptPathParam> list = dexOptNeededMap != null ? dexOptNeededMap.get(pkg) : null;
        if (list == null || size != list.size()) {
            String[] dexPaths = new String[size];
            String[] isas = new String[size];
            String[] compilerFilters = new String[size];
            String[] clContexts = new String[size];
            boolean[] profileUpdateds = new boolean[size];
            boolean[] downGrades = new boolean[size];
            int[] uids = new int[size];
            for (int i = 0; i < size; i++) {
                DexOptPathParam param = mplDexOptAdaptor.mDexOptPathParamList.get(i);
                dexPaths[i] = param.mPath;
                isas[i] = param.mIsa;
                compilerFilters[i] = param.mCompilerFilter;
                clContexts[i] = param.mCLContext;
                profileUpdateds[i] = param.mProfileUpdated;
                downGrades[i] = param.mDownGrade;
                uids[i] = param.mUid;
            }
            try {
                retDexopt = mplDexOptAdaptor.mInstaller.getDexOptNeeded(dexPaths, isas, compilerFilters, clContexts, profileUpdateds, downGrades, uids);
            } catch (Installer.InstallerException ie) {
                Slog.w(TAG, "mInstaller.getDexOptNeeded InstallerException", ie);
                result = -1;
                retDexopt = null;
            }
        } else {
            int listSize = list.size();
            int[] retDexopt2 = new int[listSize];
            for (int i2 = 0; i2 < listSize; i2++) {
                retDexopt2[i2] = list.get(i2).mDexOptNeeded;
            }
            retDexopt = retDexopt2;
        }
        if (retDexopt == null || size != retDexopt.length) {
            return result;
        }
        int result2 = result;
        int result3 = 0;
        while (result3 < size) {
            DexOptPathParam param2 = mplDexOptAdaptor.mDexOptPathParamList.get(result3);
            int newResult = mplDexOptAdaptor.mPackageDexOptimizer.mplDexOptPath(pkg, param2.mPath, param2.mIsa, param2.mCompilerFilter, param2.mProfileUpdated, param2.mCLContext, param2.mDexOptFlags, param2.mUid, packageStats, param2.mDownGrade, param2.mProfileName, param2.mDexMetadataPath, param2.mCompilationReason, mplDexOptAdaptor.mPackageDexOptimizer.mplAdjustDexoptNeeded(retDexopt[result3]));
            if (result2 == -1 || newResult == 0) {
                result2 = result2;
            } else {
                result2 = newResult;
            }
            result3++;
            mplDexOptAdaptor = this;
            retDexopt = retDexopt;
            size = size;
            dexOptNeededMap = dexOptNeededMap;
            list = list;
        }
        return result2;
    }

    /* access modifiers changed from: package-private */
    public void getDexOptNeededCacheClear() {
        int tid = Process.myTid();
        this.mDexOptNeededArray.delete(tid);
        Log.i(TAG, "getDexOptNeededCacheClear tid=" + tid);
    }

    /* JADX INFO: Multiple debug info for r0v23 'retDexopt'  int[]: [D('i' int), D('retDexopt' int[])] */
    /* JADX INFO: Multiple debug info for r10v5 int: [D('loopCount' int), D('min' int)] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x01b1, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01bb, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x01c2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01bb A[ExcHandler: all (th java.lang.Throwable), Splitter:B:53:0x0156] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01ec  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x020d  */
    public void getDexOptNeededCachePrepare(List<PackageParser.Package> pkgs, int compilationReason, boolean bootComplete, DexManager dexManager, PackageManagerService pms, int mode) {
        Throwable th;
        int size;
        ArrayList<DexOptPathParam> dexOptPathParamList;
        int[] retDexopt;
        Installer.InstallerException ie;
        int loopCount;
        DexOptPathParam param;
        CompilerStats compilerStats = pms.getCompilerStats();
        ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> dexOptNeededMap = new ArrayMap<>();
        ArrayList<DexOptPathParam> dexOptPathParamList2 = new ArrayList<>();
        for (PackageParser.Package pkg : pkgs) {
            if (PackageDexOptimizer.canOptimizePackage(pkg)) {
                int pkgCompilationReason = compilationReason;
                int dexoptFlags = 4;
                if (mode == 0) {
                    if (pms.isUseProfileForDexopt(pkg)) {
                        pkgCompilationReason = 3;
                    }
                    dexoptFlags = bootComplete ? 4 : 0;
                    if (compilationReason == 0) {
                        dexoptFlags |= 1024;
                    }
                }
                DexoptOptions options = new DexoptOptions(pkg.packageName, pkgCompilationReason, dexoptFlags);
                String[] instructionSets = InstructionSets.getAppDexInstructionSets(pkg.applicationInfo);
                ArrayList<DexOptPathParam> paramList = new ArrayList<>();
                collectDexOptPathItems(pkg, pkg.usesLibraryInfos, instructionSets, compilerStats.getOrCreatePackageStats(pkg.packageName), dexManager.getPackageUseInfoOrDefault(pkg.packageName), options, paramList);
                dexOptNeededMap.put(pkg, paramList);
                dexOptPathParamList2.addAll(paramList);
            }
        }
        int tid = Process.myTid();
        this.mDexOptNeededArray.put(tid, dexOptNeededMap);
        Log.i(TAG, "getDexOptNeededCachePrepare tid=" + tid);
        long callingId = Binder.clearCallingIdentity();
        int size2 = dexOptPathParamList2.size();
        try {
            String[] dexPaths = new String[size2];
            String[] isas = new String[size2];
            String[] filters = new String[size2];
            String[] clContext = new String[size2];
            boolean[] profileUpdateds = new boolean[size2];
            boolean[] downGrade = new boolean[size2];
            int[] uids = new int[size2];
            int i = 0;
            while (i < size2) {
                try {
                    param = dexOptPathParamList2.get(i);
                } catch (Throwable th2) {
                    th = th2;
                    Binder.restoreCallingIdentity(callingId);
                    throw th;
                }
                try {
                    dexPaths[i] = param.mPath;
                    isas[i] = param.mIsa;
                    filters[i] = param.mCompilerFilter;
                    clContext[i] = param.mCLContext;
                    profileUpdateds[i] = param.mProfileUpdated;
                } catch (Throwable th3) {
                    th = th3;
                    Binder.restoreCallingIdentity(callingId);
                    throw th;
                }
                try {
                    downGrade[i] = param.mDownGrade;
                    uids[i] = param.mUid;
                    i++;
                    uids = uids;
                    downGrade = downGrade;
                    tid = tid;
                    compilerStats = compilerStats;
                    dexOptNeededMap = dexOptNeededMap;
                } catch (Throwable th4) {
                    th = th4;
                    Binder.restoreCallingIdentity(callingId);
                    throw th;
                }
            }
            int[] uids2 = uids;
            try {
                retDexopt = new int[size2];
                if (size2 % 500 == 0) {
                    try {
                        loopCount = size2 / 500;
                    } catch (Installer.InstallerException e) {
                        ie = e;
                        size = size2;
                        dexOptPathParamList = dexOptPathParamList2;
                    }
                } else {
                    loopCount = (size2 / 500) + 1;
                }
                int j = 0;
                while (true) {
                    if (j >= loopCount) {
                        size = size2;
                        dexOptPathParamList = dexOptPathParamList2;
                        break;
                    }
                    int min = j * 500;
                    dexOptPathParamList = dexOptPathParamList2;
                    int max = (j + 1) * 500;
                    if (max > size2) {
                        max = size2;
                    }
                    size = size2;
                    try {
                        int[] ret = this.mInstaller.getDexOptNeeded((String[]) Arrays.copyOfRange(dexPaths, min, max), (String[]) Arrays.copyOfRange(isas, min, max), (String[]) Arrays.copyOfRange(filters, min, max), (String[]) Arrays.copyOfRange(clContext, min, max), Arrays.copyOfRange(profileUpdateds, min, max), Arrays.copyOfRange(downGrade, min, max), Arrays.copyOfRange(uids2, min, max));
                        if (ret == null) {
                            break;
                        } else if (ret.length != max - min) {
                            break;
                        } else {
                            for (int k = min; k < max; k++) {
                                retDexopt[k] = ret[k - min];
                            }
                            j++;
                            loopCount = loopCount;
                            dexOptPathParamList2 = dexOptPathParamList;
                            uids2 = uids2;
                            dexPaths = dexPaths;
                            size2 = size;
                        }
                    } catch (Installer.InstallerException e2) {
                        ie = e2;
                        try {
                            Slog.w(TAG, "mInstaller.getDexOptNeeded InstallerException", ie);
                            retDexopt = null;
                            Binder.restoreCallingIdentity(callingId);
                            if (retDexopt != null) {
                            }
                            Slog.w(TAG, "getDexOptNeeded catch exception or dis-match size!");
                        } catch (Throwable th5) {
                            th = th5;
                            Binder.restoreCallingIdentity(callingId);
                            throw th;
                        }
                    } catch (Throwable th6) {
                    }
                }
                retDexopt = null;
            } catch (Installer.InstallerException e3) {
                ie = e3;
                size = size2;
                dexOptPathParamList = dexOptPathParamList2;
                Slog.w(TAG, "mInstaller.getDexOptNeeded InstallerException", ie);
                retDexopt = null;
                Binder.restoreCallingIdentity(callingId);
                if (retDexopt != null) {
                }
                Slog.w(TAG, "getDexOptNeeded catch exception or dis-match size!");
            } catch (Throwable th7) {
                th = th7;
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
            Binder.restoreCallingIdentity(callingId);
            if (retDexopt != null) {
                if (retDexopt.length == size) {
                    int index = 0;
                    int i2 = 0;
                    while (i2 < size) {
                        dexOptPathParamList.get(i2).mDexOptNeeded = retDexopt[index];
                        i2++;
                        index++;
                    }
                    return;
                }
            }
            Slog.w(TAG, "getDexOptNeeded catch exception or dis-match size!");
        } catch (Throwable th8) {
            th = th8;
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:0x0189 A[LOOP:2: B:62:0x0187->B:63:0x0189, LOOP_END] */
    private int collectDexOptPathItems(PackageParser.Package pkg, List<SharedLibraryInfo> sharedLibraries, String[] targetInstructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options, ArrayList<DexOptPathParam> dexOptPathParamList) {
        boolean[] pathsWithCode;
        String[] classLoaderContexts;
        int i;
        int pathSize;
        List<String> paths;
        String[] instructionSets;
        String dexMetadataPath;
        boolean isUsedByOtherApps;
        int length;
        int count;
        MplDexOptAdaptor mplDexOptAdaptor = this;
        String[] instructionSets2 = targetInstructionSets != null ? targetInstructionSets : InstructionSets.getAppDexInstructionSets(pkg.applicationInfo);
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(instructionSets2);
        List<String> paths2 = pkg.getAllCodePaths();
        int pathSize2 = paths2.size();
        int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);
        if (sharedGid == -1) {
            Slog.wtf(TAG, "Well this is awkward; package " + pkg.applicationInfo.name + " had UID " + pkg.applicationInfo.uid, new Throwable());
            sharedGid = 9999;
        }
        boolean[] pathsWithCode2 = new boolean[paths2.size()];
        pathsWithCode2[0] = (pkg.applicationInfo.flags & 4) != 0;
        for (int i2 = 1; i2 < pathSize2; i2++) {
            pathsWithCode2[i2] = (pkg.splitFlags[i2 + -1] & 4) != 0;
        }
        String[] classLoaderContexts2 = DexoptUtils.getClassLoaderContexts(pkg.applicationInfo, sharedLibraries, pathsWithCode2);
        if (paths2.size() != classLoaderContexts2.length) {
            String[] splitCodePaths = pkg.applicationInfo.getSplitCodePaths();
            StringBuilder sb = new StringBuilder();
            sb.append("Inconsistent information between PackageParser.Package and its ApplicationInfo. pkg.getAllCodePaths=");
            sb.append(paths2);
            sb.append(" pkg.applicationInfo.getBaseCodePath=");
            sb.append(pkg.applicationInfo.getBaseCodePath());
            sb.append(" pkg.applicationInfo.getSplitCodePaths=");
            sb.append(splitCodePaths == null ? "null" : Arrays.toString(splitCodePaths));
            throw new IllegalStateException(sb.toString());
        }
        int count2 = 0;
        int i3 = 0;
        while (i3 < pathSize2) {
            if (pathsWithCode2[i3] && classLoaderContexts2[i3] != null) {
                String path = paths2.get(i3);
                if (options.getSplitName() == null || options.getSplitName().equals(new File(path).getName())) {
                    String profileName = ArtManager.getProfileName(i3 == 0 ? null : pkg.splitNames[i3 - 1]);
                    if (options.isDexoptInstallWithDexMetadata()) {
                        File dexMetadataFile = DexMetadataHelper.findDexMetadataForFile(new File(path));
                        dexMetadataPath = dexMetadataFile == null ? null : dexMetadataFile.getAbsolutePath();
                    } else {
                        dexMetadataPath = null;
                    }
                    if (!options.isDexoptAsSharedLibrary()) {
                        if (!packageUseInfo.isUsedByOtherApps(path)) {
                            isUsedByOtherApps = false;
                            instructionSets = instructionSets2;
                            paths = paths2;
                            pathSize = pathSize2;
                            String compilerFilter = mplDexOptAdaptor.mPackageDexOptimizer.mplGetRealCompilerFilter(pkg.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                            boolean profileUpdated = !options.isCheckForProfileUpdates() && mplDexOptAdaptor.mPackageDexOptimizer.mplIsProfileUpdated(pkg, sharedGid, profileName, compilerFilter);
                            int dexoptFlags = mplDexOptAdaptor.mPackageDexOptimizer.mplGetDexFlags(pkg, compilerFilter, options);
                            length = dexCodeInstructionSets.length;
                            int count3 = count2;
                            count = 0;
                            while (count < length) {
                                dexOptPathParamList.add(new DexOptPathParam(path, dexCodeInstructionSets[count], compilerFilter, classLoaderContexts2[i3], profileName, dexMetadataPath, profileUpdated, options.isDowngrade(), sharedGid, options.getCompilationReason(), dexoptFlags));
                                count3++;
                                count++;
                                length = length;
                                profileName = profileName;
                                path = path;
                                i3 = i3;
                                classLoaderContexts2 = classLoaderContexts2;
                                pathsWithCode2 = pathsWithCode2;
                            }
                            i = i3;
                            classLoaderContexts = classLoaderContexts2;
                            pathsWithCode = pathsWithCode2;
                            count2 = count3;
                            i3 = i + 1;
                            mplDexOptAdaptor = this;
                            instructionSets2 = instructionSets;
                            paths2 = paths;
                            pathSize2 = pathSize;
                            classLoaderContexts2 = classLoaderContexts;
                            pathsWithCode2 = pathsWithCode;
                        }
                    }
                    isUsedByOtherApps = true;
                    instructionSets = instructionSets2;
                    paths = paths2;
                    pathSize = pathSize2;
                    String compilerFilter2 = mplDexOptAdaptor.mPackageDexOptimizer.mplGetRealCompilerFilter(pkg.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                    if (!options.isCheckForProfileUpdates()) {
                    }
                    int dexoptFlags2 = mplDexOptAdaptor.mPackageDexOptimizer.mplGetDexFlags(pkg, compilerFilter2, options);
                    length = dexCodeInstructionSets.length;
                    int count32 = count2;
                    count = 0;
                    while (count < length) {
                    }
                    i = i3;
                    classLoaderContexts = classLoaderContexts2;
                    pathsWithCode = pathsWithCode2;
                    count2 = count32;
                    i3 = i + 1;
                    mplDexOptAdaptor = this;
                    instructionSets2 = instructionSets;
                    paths2 = paths;
                    pathSize2 = pathSize;
                    classLoaderContexts2 = classLoaderContexts;
                    pathsWithCode2 = pathsWithCode;
                }
            }
            instructionSets = instructionSets2;
            paths = paths2;
            pathSize = pathSize2;
            i = i3;
            classLoaderContexts = classLoaderContexts2;
            pathsWithCode = pathsWithCode2;
            i3 = i + 1;
            mplDexOptAdaptor = this;
            instructionSets2 = instructionSets;
            paths2 = paths;
            pathSize2 = pathSize;
            classLoaderContexts2 = classLoaderContexts;
            pathsWithCode2 = pathsWithCode;
        }
        return count2;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0102, code lost:
        r3 = null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x012d  */
    public void dumpPkgListDexoptState(IndentingPrintWriter pw, Collection<PackageParser.Package> packages, DexManager dexManager) {
        String[] retDexStatus;
        Iterator<PackageParser.Package> it;
        PackageDexUsage.PackageUseInfo useInfo;
        String[] isas;
        String[] dexPaths;
        Installer.InstallerException ie;
        ArrayList<DexOptPathParam> dexOptPathParamList = new ArrayList<>();
        for (PackageParser.Package pkg : packages) {
            String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(InstructionSets.getAppDexInstructionSets(pkg.applicationInfo));
            List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
            int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);
            for (String path : paths) {
                int i = 0;
                for (int length = dexCodeInstructionSets.length; i < length; length = length) {
                    dexOptPathParamList.add(new DexOptPathParam(path, dexCodeInstructionSets[i], null, null, null, null, false, false, sharedGid, 0, 0));
                    i++;
                }
            }
        }
        int size = dexOptPathParamList.size();
        String[] dexPaths2 = new String[size];
        String[] isas2 = new String[size];
        int[] uids = new int[size];
        for (int i2 = 0; i2 < size; i2++) {
            DexOptPathParam param = dexOptPathParamList.get(i2);
            dexPaths2[i2] = param.mPath;
            isas2[i2] = param.mIsa;
            uids[i2] = param.mUid;
        }
        try {
            retDexStatus = new String[(size * 2)];
            int loopCount = size % 500 == 0 ? size / 500 : (size / 500) + 1;
            int j = 0;
            while (true) {
                if (j >= loopCount) {
                    break;
                }
                int min = j * 500;
                int max = (j + 1) * 500;
                if (max > size) {
                    max = size;
                }
                try {
                    String[] ret = this.mInstaller.getDexFileOptimizationInfo((String[]) Arrays.copyOfRange(dexPaths2, min, max), (String[]) Arrays.copyOfRange(isas2, min, max), Arrays.copyOfRange(uids, min, max));
                    if (ret == null) {
                        break;
                    } else if (ret.length != (max - min) * 2) {
                        break;
                    } else {
                        int min2 = min * 2;
                        int max2 = max * 2;
                        for (int k = min2; k < max2; k++) {
                            retDexStatus[k] = ret[k - min2];
                        }
                        j++;
                        loopCount = loopCount;
                    }
                } catch (Installer.InstallerException e) {
                    ie = e;
                    Slog.w(TAG, "mInstaller.getDexFileStatus InstallerException", ie);
                    retDexStatus = null;
                    Slog.w(TAG, "getDexFileOptimizationInfo catch exception or dis-match size!");
                    retDexStatus = null;
                    int index = 0;
                    while (it.hasNext()) {
                    }
                }
            }
        } catch (Installer.InstallerException e2) {
            ie = e2;
            Slog.w(TAG, "mInstaller.getDexFileStatus InstallerException", ie);
            retDexStatus = null;
            Slog.w(TAG, "getDexFileOptimizationInfo catch exception or dis-match size!");
            retDexStatus = null;
            int index2 = 0;
            while (it.hasNext()) {
            }
        }
        if (retDexStatus == null || retDexStatus.length != size * 2) {
            Slog.w(TAG, "getDexFileOptimizationInfo catch exception or dis-match size!");
            retDexStatus = null;
        }
        int index22 = 0;
        for (it = packages.iterator(); it.hasNext(); it = it) {
            PackageParser.Package pkg2 = it.next();
            pw.println("[" + pkg2.packageName + "]");
            pw.increaseIndent();
            boolean mapleApp = (pkg2.applicationInfo.hwFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0;
            String[] dexCodeInstructionSets2 = InstructionSets.getDexCodeInstructionSets(InstructionSets.getAppDexInstructionSets(pkg2.applicationInfo));
            List<String> paths2 = pkg2.getAllCodePathsExcludingResourceOnly();
            int index3 = index22;
            PackageDexUsage.PackageUseInfo useInfo2 = dexManager.getPackageUseInfoOrDefault(pkg2.packageName);
            Iterator<String> it2 = paths2.iterator();
            while (it2.hasNext()) {
                String path2 = it2.next();
                pw.println("path: " + path2);
                pw.increaseIndent();
                int length2 = dexCodeInstructionSets2.length;
                int i3 = 0;
                while (i3 < length2) {
                    String isa = dexCodeInstructionSets2[i3];
                    if (mapleApp) {
                        dexPaths = dexPaths2;
                        pw.println("It's a mapled app.");
                        isas = isas2;
                    } else {
                        dexPaths = dexPaths2;
                        if (retDexStatus != null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(isa);
                            isas = isas2;
                            sb.append(": [status=");
                            sb.append(retDexStatus[index3 * 2]);
                            sb.append("] [reason=");
                            sb.append(retDexStatus[(index3 * 2) + 1]);
                            sb.append("]");
                            pw.println(sb.toString());
                        } else {
                            isas = isas2;
                            pw.println(isa + ": [Exception]");
                        }
                    }
                    index3++;
                    i3++;
                    length2 = length2;
                    dexPaths2 = dexPaths;
                    isas2 = isas;
                }
                if (useInfo2.isUsedByOtherApps(path2)) {
                    pw.println("used by other apps: " + useInfo2.getLoadingPackages(path2));
                }
                Map<String, PackageDexUsage.DexUseInfo> dexUseInfoMap = useInfo2.getDexUseInfoMap();
                if (!dexUseInfoMap.isEmpty()) {
                    pw.println("known secondary dex files:");
                    pw.increaseIndent();
                    for (Map.Entry<String, PackageDexUsage.DexUseInfo> e3 : dexUseInfoMap.entrySet()) {
                        String dex = e3.getKey();
                        PackageDexUsage.DexUseInfo dexUseInfo = e3.getValue();
                        pw.println(dex);
                        pw.increaseIndent();
                        pw.println("class loader context: " + dexUseInfo.getClassLoaderContext());
                        if (dexUseInfo.isUsedByOtherApps()) {
                            pw.println("used by other apps: " + dexUseInfo.getLoadingPackages());
                        }
                        pw.decreaseIndent();
                        useInfo2 = useInfo2;
                        path2 = path2;
                    }
                    useInfo = useInfo2;
                    pw.decreaseIndent();
                } else {
                    useInfo = useInfo2;
                }
                pw.decreaseIndent();
                dexOptPathParamList = dexOptPathParamList;
                size = size;
                dexPaths2 = dexPaths2;
                isas2 = isas2;
                useInfo2 = useInfo;
            }
            pw.decreaseIndent();
            index22 = index3;
        }
    }

    private static void assertValidInstructionSetList(String[] instructionSets) throws Installer.InstallerException {
        for (String instr : instructionSets) {
            boolean match = false;
            String[] strArr = Build.SUPPORTED_ABIS;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (VMRuntime.getInstructionSet(strArr[i]).equals(instr)) {
                    match = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!match) {
                throw new Installer.InstallerException("Invalid instruction set: " + instr);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int[] getDexOptNeeded(IInstalld installd, String[] fileNames, String[] instructionSets, String[] compilerFilters, String[] clContexts, boolean[] newProfiles, boolean[] downGrades, int[] uids) throws Installer.InstallerException {
        int size;
        if (installd == null || (size = fileNames.length) <= 0 || instructionSets.length != size || compilerFilters.length != size || clContexts.length != size || newProfiles.length != size || uids.length != size || downGrades.length != size) {
            return null;
        }
        assertValidInstructionSetList(instructionSets);
        try {
            return installd.getDexOptNeeded(fileNames, instructionSets, compilerFilters, clContexts, newProfiles, downGrades, uids);
        } catch (Exception e) {
            throw Installer.InstallerException.from(e);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean[] isDexOptNeeded(IInstalld installd, String[] fileNames, int[] uids) throws Installer.InstallerException {
        int size;
        if (installd == null || (size = fileNames.length) <= 0 || uids.length != size) {
            return null;
        }
        try {
            return installd.isDexOptNeeded(fileNames, uids);
        } catch (Exception e) {
            throw Installer.InstallerException.from(e);
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getDexFileStatus(IInstalld installd, String[] fileNames, String[] instructionSets, int[] uids) throws Installer.InstallerException {
        int size;
        if (installd == null || (size = fileNames.length) <= 0 || instructionSets.length != size || uids.length != size) {
            return null;
        }
        assertValidInstructionSetList(instructionSets);
        try {
            String[] dexFileStatus = new String[size];
            installd.getDexFileStatus(fileNames, instructionSets, uids, dexFileStatus);
            return dexFileStatus;
        } catch (Exception e) {
            throw Installer.InstallerException.from(e);
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getDexFileOutputPaths(IInstalld installd, String fileName, String instructionSet, int uid) throws Installer.InstallerException {
        if (installd == null) {
            return null;
        }
        try {
            String[] outputPaths = new String[2];
            installd.getDexFileOutputPaths(fileName, instructionSet, uid, outputPaths);
            if (outputPaths[0].isEmpty() || outputPaths[1].isEmpty()) {
                return null;
            }
            return outputPaths;
        } catch (Exception e) {
            throw Installer.InstallerException.from(e);
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getDexFileOptimizationInfo(IInstalld installd, String[] fileNames, String[] instructionSets, int[] uids) throws Installer.InstallerException {
        int size;
        if (installd == null || (size = fileNames.length) <= 0 || instructionSets.length != size || uids.length != size) {
            return null;
        }
        assertValidInstructionSetList(instructionSets);
        try {
            String[] status = new String[(size * 2)];
            installd.getDexFileOptimizationStatus(fileNames, instructionSets, uids, status);
            return status;
        } catch (Exception e) {
            throw Installer.InstallerException.from(e);
        }
    }
}
