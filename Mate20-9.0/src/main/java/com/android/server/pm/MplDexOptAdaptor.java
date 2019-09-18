package com.android.server.pm;

import android.content.pm.PackageParser;
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

    private static class DexOptPathParam {
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

    public static MplDexOptAdaptor getInstance() {
        return sInstance;
    }

    private MplDexOptAdaptor() {
    }

    public void setInstaller(PackageDexOptimizer pdo, Installer installer) {
        this.mPackageDexOptimizer = pdo;
        this.mInstaller = installer;
    }

    /* access modifiers changed from: package-private */
    public void dexOptParamPrepare() {
        this.mDexOptPathParamList.clear();
    }

    /* access modifiers changed from: package-private */
    public void dexOptParamAdd(String path, String isa, String compilerFilter, String clContext, String profileName, String dexMetadataPath, boolean profileUpdated, boolean downGrade, int uid, int compilationReason, int dexoptFlags) {
        ArrayList<DexOptPathParam> arrayList = this.mDexOptPathParamList;
        DexOptPathParam dexOptPathParam = new DexOptPathParam(path, isa, compilerFilter, clContext, profileName, dexMetadataPath, profileUpdated, downGrade, uid, compilationReason, dexoptFlags);
        arrayList.add(dexOptPathParam);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v15, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v21, resolved type: java.util.ArrayList} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public int dexOptParamProcess(PackageParser.Package pkg, CompilerStats.PackageStats packageStats) {
        PackageParser.Package packageR;
        int result;
        int[] retDexopt;
        MplDexOptAdaptor mplDexOptAdaptor = this;
        int result2 = 0;
        if (mplDexOptAdaptor.mDexOptPathParamList.size() <= 0) {
            return 0;
        }
        int size = mplDexOptAdaptor.mDexOptPathParamList.size();
        ArrayList<DexOptPathParam> list = null;
        ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> dexOptNeededMap = mplDexOptAdaptor.mDexOptNeededArray.get(Process.myTid());
        if (dexOptNeededMap != null) {
            packageR = pkg;
            list = dexOptNeededMap.get(packageR);
        } else {
            packageR = pkg;
        }
        ArrayList<DexOptPathParam> list2 = list;
        if (list2 == null || size != list2.size()) {
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
                result2 = -1;
                retDexopt = null;
            }
        } else {
            int listSize = list2.size();
            int[] retDexopt2 = new int[listSize];
            for (int i2 = 0; i2 < listSize; i2++) {
                retDexopt2[i2] = list2.get(i2).mDexOptNeeded;
            }
            retDexopt = retDexopt2;
        }
        if (retDexopt != null && size == retDexopt.length) {
            int i3 = 0;
            while (true) {
                int i4 = i3;
                if (i4 >= size) {
                    break;
                }
                DexOptPathParam param2 = mplDexOptAdaptor.mDexOptPathParamList.get(i4);
                int dexoptNeeded = mplDexOptAdaptor.mPackageDexOptimizer.mplAdjustDexoptNeeded(retDexopt[i4]);
                int[] retDexopt3 = retDexopt;
                int size2 = size;
                ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> dexOptNeededMap2 = dexOptNeededMap;
                DexOptPathParam dexOptPathParam = param2;
                int i5 = i4;
                ArrayList<DexOptPathParam> list3 = list2;
                int newResult = mplDexOptAdaptor.mPackageDexOptimizer.mplDexOptPath(packageR, param2.mPath, param2.mIsa, param2.mCompilerFilter, param2.mProfileUpdated, param2.mCLContext, param2.mDexOptFlags, param2.mUid, packageStats, param2.mDownGrade, param2.mProfileName, param2.mDexMetadataPath, param2.mCompilationReason, dexoptNeeded);
                result = result;
                if (!(result == -1 || newResult == 0)) {
                    result = newResult;
                }
                i3 = i5 + 1;
                packageR = pkg;
                list2 = list3;
                retDexopt = retDexopt3;
                size = size2;
                dexOptNeededMap = dexOptNeededMap2;
                mplDexOptAdaptor = this;
            }
        }
        int i6 = size;
        ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> arrayMap = dexOptNeededMap;
        ArrayList<DexOptPathParam> arrayList = list2;
        return result;
    }

    /* access modifiers changed from: package-private */
    public void getDexOptNeededCacheClear() {
        int tid = Process.myTid();
        this.mDexOptNeededArray.delete(tid);
        Log.i(TAG, "getDexOptNeededCacheClear tid=" + tid);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01be, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01bf, code lost:
        r33 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01c8, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01c9, code lost:
        r6 = r31;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01cd, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01ce, code lost:
        r32 = r2;
        r33 = r6;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0216  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01c8 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:62:0x0163] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01f4  */
    public void getDexOptNeededCachePrepare(List<PackageParser.Package> pkgs, int compilationReason, boolean bootComplete, DexManager dexManager, PackageManagerService pms, int mode) {
        ArrayList<DexOptPathParam> dexOptPathParamList;
        int[] retDexopt;
        int loopCount;
        int tid;
        DexOptPathParam param;
        CompilerStats compilerStats;
        boolean[] profileUpdateds;
        ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> dexOptNeededMap;
        boolean[] downGrade;
        CompilerStats compilerStats2 = pms.getCompilerStats();
        ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> dexOptNeededMap2 = new ArrayMap<>();
        ArrayList<DexOptPathParam> dexOptPathParamList2 = new ArrayList<>();
        for (PackageParser.Package pkg : pkgs) {
            if (PackageDexOptimizer.canOptimizePackage(pkg)) {
                int pkgCompilationReason = compilationReason;
                int dexoptFlags = 4;
                if (mode != 0) {
                    PackageManagerService packageManagerService = pms;
                } else {
                    if (pms.isUseProfileForDexopt(pkg)) {
                        pkgCompilationReason = 3;
                    }
                    dexoptFlags = bootComplete ? 4 : 0;
                    if (compilationReason == 0) {
                        dexoptFlags |= 1024;
                    }
                }
                int pkgCompilationReason2 = pkgCompilationReason;
                int dexoptFlags2 = dexoptFlags;
                DexoptOptions options = new DexoptOptions(pkg.packageName, pkgCompilationReason2, dexoptFlags2);
                String[] instructionSets = InstructionSets.getAppDexInstructionSets(pkg.applicationInfo);
                ArrayList<DexOptPathParam> paramList = new ArrayList<>();
                String[] strArr = pkg.usesLibraryFiles;
                ArrayList<DexOptPathParam> paramList2 = paramList;
                CompilerStats.PackageStats orCreatePackageStats = compilerStats2.getOrCreatePackageStats(pkg.packageName);
                int i = dexoptFlags2;
                int i2 = pkgCompilationReason2;
                int collectDexOptPathItems = collectDexOptPathItems(pkg, strArr, instructionSets, orCreatePackageStats, dexManager.getPackageUseInfoOrDefault(pkg.packageName), options, paramList2);
                ArrayList<DexOptPathParam> paramList3 = paramList2;
                dexOptNeededMap2.put(pkg, paramList3);
                dexOptPathParamList2.addAll(paramList3);
            }
        }
        PackageManagerService packageManagerService2 = pms;
        int tid2 = Process.myTid();
        this.mDexOptNeededArray.put(tid2, dexOptNeededMap2);
        Log.i(TAG, "getDexOptNeededCachePrepare tid=" + tid2);
        long callingId = Binder.clearCallingIdentity();
        int size = dexOptPathParamList2.size();
        try {
            String[] dexPaths = new String[size];
            String[] isas = new String[size];
            String[] filters = new String[size];
            String[] clContext = new String[size];
            boolean[] profileUpdateds2 = new boolean[size];
            boolean[] downGrade2 = new boolean[size];
            int[] uids = new int[size];
            int i3 = 0;
            while (i3 < size) {
                try {
                    tid = tid2;
                    param = dexOptPathParamList2.get(i3);
                } catch (Throwable th) {
                    th = th;
                    int i4 = tid2;
                    CompilerStats compilerStats3 = compilerStats2;
                    ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> arrayMap = dexOptNeededMap2;
                    ArrayList<DexOptPathParam> arrayList = dexOptPathParamList2;
                    Binder.restoreCallingIdentity(callingId);
                    throw th;
                }
                try {
                    dexPaths[i3] = param.mPath;
                    isas[i3] = param.mIsa;
                    filters[i3] = param.mCompilerFilter;
                    clContext[i3] = param.mCLContext;
                    compilerStats = compilerStats2;
                    profileUpdateds = profileUpdateds2;
                    try {
                        profileUpdateds[i3] = param.mProfileUpdated;
                        dexOptNeededMap = dexOptNeededMap2;
                        downGrade = downGrade2;
                    } catch (Throwable th2) {
                        th = th2;
                        ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> arrayMap2 = dexOptNeededMap2;
                        ArrayList<DexOptPathParam> arrayList2 = dexOptPathParamList2;
                        Binder.restoreCallingIdentity(callingId);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    CompilerStats compilerStats4 = compilerStats2;
                    ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> arrayMap3 = dexOptNeededMap2;
                    ArrayList<DexOptPathParam> arrayList3 = dexOptPathParamList2;
                    Binder.restoreCallingIdentity(callingId);
                    throw th;
                }
                try {
                    downGrade[i3] = param.mDownGrade;
                    int i5 = param.mUid;
                    DexOptPathParam dexOptPathParam = param;
                    int[] uids2 = uids;
                    uids2[i3] = i5;
                    i3++;
                    uids = uids2;
                    profileUpdateds2 = profileUpdateds;
                    downGrade2 = downGrade;
                    tid2 = tid;
                    compilerStats2 = compilerStats;
                    dexOptNeededMap2 = dexOptNeededMap;
                } catch (Throwable th4) {
                    th = th4;
                    ArrayList<DexOptPathParam> arrayList4 = dexOptPathParamList2;
                    Binder.restoreCallingIdentity(callingId);
                    throw th;
                }
            }
            CompilerStats compilerStats5 = compilerStats2;
            ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> arrayMap4 = dexOptNeededMap2;
            boolean[] profileUpdateds3 = profileUpdateds2;
            boolean[] downGrade3 = downGrade2;
            int[] uids3 = uids;
            try {
                retDexopt = new int[size];
                if (size % 500 == 0) {
                    try {
                        loopCount = size / 500;
                    } catch (Installer.InstallerException e) {
                        ie = e;
                        int[] iArr = uids3;
                        String[] strArr2 = dexPaths;
                        dexOptPathParamList = dexOptPathParamList2;
                    }
                } else {
                    loopCount = (size / 500) + 1;
                }
                int j = 0;
                while (true) {
                    int j2 = j;
                    if (j2 >= loopCount) {
                        String[] strArr3 = dexPaths;
                        dexOptPathParamList = dexOptPathParamList2;
                        break;
                    }
                    int loopCount2 = loopCount;
                    int min = j2 * 500;
                    int max = (j2 + 1) * 500;
                    if (max > size) {
                        max = size;
                    }
                    dexOptPathParamList = dexOptPathParamList2;
                    try {
                        int[] ret = this.mInstaller.getDexOptNeeded((String[]) Arrays.copyOfRange(dexPaths, min, max), (String[]) Arrays.copyOfRange(isas, min, max), (String[]) Arrays.copyOfRange(filters, min, max), (String[]) Arrays.copyOfRange(clContext, min, max), Arrays.copyOfRange(profileUpdateds3, min, max), Arrays.copyOfRange(downGrade3, min, max), Arrays.copyOfRange(uids3, min, max));
                        if (ret == null) {
                            String[] strArr4 = dexPaths;
                            break;
                        }
                        int[] uids4 = uids3;
                        String[] dexPaths2 = dexPaths;
                        if (ret.length != max - min) {
                            break;
                        }
                        for (int k = min; k < max; k++) {
                            retDexopt[k] = ret[k - min];
                        }
                        j = j2 + 1;
                        loopCount = loopCount2;
                        dexOptPathParamList2 = dexOptPathParamList;
                        uids3 = uids4;
                        dexPaths = dexPaths2;
                        PackageManagerService packageManagerService3 = pms;
                    } catch (Installer.InstallerException e2) {
                        ie = e2;
                        try {
                            Slog.w(TAG, "mInstaller.getDexOptNeeded InstallerException", ie);
                            retDexopt = null;
                            Binder.restoreCallingIdentity(callingId);
                            if (retDexopt == null) {
                            }
                            Slog.w(TAG, "getDexOptNeeded catch exception or dis-match size!");
                        } catch (Throwable th5) {
                            th = th5;
                            ArrayList<DexOptPathParam> arrayList5 = dexOptPathParamList;
                            Binder.restoreCallingIdentity(callingId);
                            throw th;
                        }
                    } catch (Throwable th6) {
                    }
                }
                retDexopt = null;
            } catch (Installer.InstallerException e3) {
                ie = e3;
                int[] iArr2 = uids3;
                String[] strArr5 = dexPaths;
                dexOptPathParamList = dexOptPathParamList2;
                Slog.w(TAG, "mInstaller.getDexOptNeeded InstallerException", ie);
                retDexopt = null;
                Binder.restoreCallingIdentity(callingId);
                if (retDexopt == null) {
                }
                Slog.w(TAG, "getDexOptNeeded catch exception or dis-match size!");
            } catch (Throwable th7) {
                th = th7;
                ArrayList<DexOptPathParam> arrayList6 = dexOptPathParamList2;
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
            Binder.restoreCallingIdentity(callingId);
            if (retDexopt == null) {
            } else if (retDexopt.length != size) {
                ArrayList<DexOptPathParam> arrayList7 = dexOptPathParamList;
            } else {
                int index = 0;
                int i6 = 0;
                while (true) {
                    int i7 = i6;
                    if (i7 < size) {
                        dexOptPathParamList.get(i7).mDexOptNeeded = retDexopt[index];
                        i6 = i7 + 1;
                        index++;
                    } else {
                        return;
                    }
                }
            }
            Slog.w(TAG, "getDexOptNeeded catch exception or dis-match size!");
        } catch (Throwable th8) {
            th = th8;
            int i8 = tid2;
            CompilerStats compilerStats6 = compilerStats2;
            ArrayMap<PackageParser.Package, ArrayList<DexOptPathParam>> arrayMap5 = dexOptNeededMap2;
            ArrayList<DexOptPathParam> arrayList8 = dexOptPathParamList2;
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x0193 A[LOOP:2: B:63:0x0191->B:64:0x0193, LOOP_END] */
    private int collectDexOptPathItems(PackageParser.Package pkg, String[] sharedLibraries, String[] targetInstructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options, ArrayList<DexOptPathParam> dexOptPathParamList) {
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
        PackageParser.Package packageR = pkg;
        String[] instructionSets2 = targetInstructionSets != null ? targetInstructionSets : InstructionSets.getAppDexInstructionSets(packageR.applicationInfo);
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(instructionSets2);
        List<String> paths2 = pkg.getAllCodePaths();
        int pathSize2 = paths2.size();
        int sharedGid = UserHandle.getSharedAppGid(packageR.applicationInfo.uid);
        if (sharedGid == -1) {
            Slog.wtf(TAG, "Well this is awkward; package " + packageR.applicationInfo.name + " had UID " + packageR.applicationInfo.uid, new Throwable());
            sharedGid = 9999;
        }
        boolean[] pathsWithCode2 = new boolean[paths2.size()];
        pathsWithCode2[0] = (packageR.applicationInfo.flags & 4) != 0;
        for (int i2 = 1; i2 < pathSize2; i2++) {
            pathsWithCode2[i2] = (packageR.splitFlags[i2 + -1] & 4) != 0;
        }
        String[] classLoaderContexts2 = DexoptUtils.getClassLoaderContexts(packageR.applicationInfo, sharedLibraries, pathsWithCode2);
        if (paths2.size() != classLoaderContexts2.length) {
            String[] splitCodePaths = packageR.applicationInfo.getSplitCodePaths();
            StringBuilder sb = new StringBuilder();
            sb.append("Inconsistent information between PackageParser.Package and its ApplicationInfo. pkg.getAllCodePaths=");
            sb.append(paths2);
            sb.append(" pkg.applicationInfo.getBaseCodePath=");
            sb.append(packageR.applicationInfo.getBaseCodePath());
            sb.append(" pkg.applicationInfo.getSplitCodePaths=");
            sb.append(splitCodePaths == null ? "null" : Arrays.toString(splitCodePaths));
            throw new IllegalStateException(sb.toString());
        }
        int count2 = 0;
        int count3 = 0;
        while (true) {
            int i3 = count3;
            if (i3 < pathSize2) {
                if (pathsWithCode2[i3] && classLoaderContexts2[i3] != null) {
                    String path = paths2.get(i3);
                    if (options.getSplitName() == null || options.getSplitName().equals(new File(path).getName())) {
                        String profileName = ArtManager.getProfileName(i3 == 0 ? null : packageR.splitNames[i3 - 1]);
                        if (options.isDexoptInstallWithDexMetadata()) {
                            File dexMetadataFile = DexMetadataHelper.findDexMetadataForFile(new File(path));
                            dexMetadataPath = dexMetadataFile == null ? null : dexMetadataFile.getAbsolutePath();
                            String str = dexMetadataPath;
                        } else {
                            dexMetadataPath = null;
                        }
                        if (options.isDexoptAsSharedLibrary()) {
                            PackageDexUsage.PackageUseInfo packageUseInfo2 = packageUseInfo;
                        } else if (!packageUseInfo.isUsedByOtherApps(path)) {
                            isUsedByOtherApps = false;
                            instructionSets = instructionSets2;
                            paths = paths2;
                            pathSize = pathSize2;
                            String compilerFilter = mplDexOptAdaptor.mPackageDexOptimizer.mplGetRealCompilerFilter(packageR.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                            String profileName2 = profileName;
                            boolean profileUpdated = !options.isCheckForProfileUpdates() && mplDexOptAdaptor.mPackageDexOptimizer.mplIsProfileUpdated(packageR, sharedGid, profileName, compilerFilter);
                            int dexoptFlags = mplDexOptAdaptor.mPackageDexOptimizer.mplGetDexFlags(packageR, compilerFilter, options);
                            length = dexCodeInstructionSets.length;
                            int count4 = count2;
                            count = 0;
                            while (count < length) {
                                DexOptPathParam dexOptPathParam = new DexOptPathParam(path, dexCodeInstructionSets[count], compilerFilter, classLoaderContexts2[i3], profileName2, dexMetadataPath, profileUpdated, options.isDowngrade(), sharedGid, options.getCompilationReason(), dexoptFlags);
                                dexOptPathParamList.add(dexOptPathParam);
                                count4++;
                                count++;
                                String[] strArr = sharedLibraries;
                                PackageDexUsage.PackageUseInfo packageUseInfo3 = packageUseInfo;
                                length = length;
                                path = path;
                                i3 = i3;
                                classLoaderContexts2 = classLoaderContexts2;
                                pathsWithCode2 = pathsWithCode2;
                            }
                            ArrayList<DexOptPathParam> arrayList = dexOptPathParamList;
                            i = i3;
                            classLoaderContexts = classLoaderContexts2;
                            pathsWithCode = pathsWithCode2;
                            count2 = count4;
                            String[] strArr2 = sharedLibraries;
                            count3 = i + 1;
                            instructionSets2 = instructionSets;
                            paths2 = paths;
                            pathSize2 = pathSize;
                            classLoaderContexts2 = classLoaderContexts;
                            pathsWithCode2 = pathsWithCode;
                            mplDexOptAdaptor = this;
                            packageR = pkg;
                        }
                        isUsedByOtherApps = true;
                        instructionSets = instructionSets2;
                        paths = paths2;
                        pathSize = pathSize2;
                        String compilerFilter2 = mplDexOptAdaptor.mPackageDexOptimizer.mplGetRealCompilerFilter(packageR.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                        String profileName22 = profileName;
                        boolean profileUpdated2 = !options.isCheckForProfileUpdates() && mplDexOptAdaptor.mPackageDexOptimizer.mplIsProfileUpdated(packageR, sharedGid, profileName, compilerFilter2);
                        int dexoptFlags2 = mplDexOptAdaptor.mPackageDexOptimizer.mplGetDexFlags(packageR, compilerFilter2, options);
                        length = dexCodeInstructionSets.length;
                        int count42 = count2;
                        count = 0;
                        while (count < length) {
                        }
                        ArrayList<DexOptPathParam> arrayList2 = dexOptPathParamList;
                        i = i3;
                        classLoaderContexts = classLoaderContexts2;
                        pathsWithCode = pathsWithCode2;
                        count2 = count42;
                        String[] strArr22 = sharedLibraries;
                        count3 = i + 1;
                        instructionSets2 = instructionSets;
                        paths2 = paths;
                        pathSize2 = pathSize;
                        classLoaderContexts2 = classLoaderContexts;
                        pathsWithCode2 = pathsWithCode;
                        mplDexOptAdaptor = this;
                        packageR = pkg;
                    }
                }
                DexoptOptions dexoptOptions = options;
                ArrayList<DexOptPathParam> arrayList3 = dexOptPathParamList;
                instructionSets = instructionSets2;
                paths = paths2;
                pathSize = pathSize2;
                i = i3;
                classLoaderContexts = classLoaderContexts2;
                pathsWithCode = pathsWithCode2;
                String[] strArr222 = sharedLibraries;
                count3 = i + 1;
                instructionSets2 = instructionSets;
                paths2 = paths;
                pathSize2 = pathSize;
                classLoaderContexts2 = classLoaderContexts;
                pathsWithCode2 = pathsWithCode;
                mplDexOptAdaptor = this;
                packageR = pkg;
            } else {
                DexoptOptions dexoptOptions2 = options;
                ArrayList<DexOptPathParam> arrayList4 = dexOptPathParamList;
                String[] strArr3 = instructionSets2;
                List<String> list = paths2;
                int i4 = pathSize2;
                String[] strArr4 = classLoaderContexts2;
                boolean[] zArr = pathsWithCode2;
                return count2;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0132  */
    public void dumpPkgListDexoptState(IndentingPrintWriter pw, Collection<PackageParser.Package> packages, DexManager dexManager) {
        String[] retDexStatus;
        String[] retDexStatus2;
        String[] isas;
        String[] dexPaths;
        IndentingPrintWriter indentingPrintWriter = pw;
        ArrayList<DexOptPathParam> dexOptPathParamList = new ArrayList<>();
        for (PackageParser.Package pkg : packages) {
            String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(InstructionSets.getAppDexInstructionSets(pkg.applicationInfo));
            List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
            int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);
            Iterator<String> it = paths.iterator();
            while (it.hasNext()) {
                String path = it.next();
                int length = dexCodeInstructionSets.length;
                int i = 0;
                while (i < length) {
                    DexOptPathParam dexOptPathParam = r10;
                    DexOptPathParam dexOptPathParam2 = new DexOptPathParam(path, dexCodeInstructionSets[i], null, null, null, null, false, false, sharedGid, 0, 0);
                    dexOptPathParamList.add(dexOptPathParam);
                    i++;
                    length = length;
                    it = it;
                }
                Iterator<String> it2 = it;
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
            retDexStatus = new String[(2 * size)];
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
                        int min2 = 2 * min;
                        int min3 = 2 * max;
                        for (int k = min2; k < min3; k++) {
                            retDexStatus[k] = ret[k - min2];
                        }
                        j++;
                    }
                } catch (Installer.InstallerException e) {
                    ie = e;
                    Slog.w(TAG, "mInstaller.getDexFileStatus InstallerException", ie);
                    retDexStatus = null;
                    Slog.w(TAG, "getDexFileOptimizationInfo catch exception or dis-match size!");
                    retDexStatus = null;
                    int index = 0;
                    while (r8.hasNext()) {
                    }
                    String[] strArr = retDexStatus;
                    ArrayList<DexOptPathParam> arrayList = dexOptPathParamList;
                    int i3 = size;
                    String[] strArr2 = dexPaths2;
                    String[] strArr3 = isas2;
                }
            }
            retDexStatus = null;
        } catch (Installer.InstallerException e2) {
            ie = e2;
            Slog.w(TAG, "mInstaller.getDexFileStatus InstallerException", ie);
            retDexStatus = null;
            Slog.w(TAG, "getDexFileOptimizationInfo catch exception or dis-match size!");
            retDexStatus = null;
            int index2 = 0;
            while (r8.hasNext()) {
            }
            String[] strArr4 = retDexStatus;
            ArrayList<DexOptPathParam> arrayList2 = dexOptPathParamList;
            int i32 = size;
            String[] strArr22 = dexPaths2;
            String[] strArr32 = isas2;
        }
        if (retDexStatus == null || retDexStatus.length != 2 * size) {
            Slog.w(TAG, "getDexFileOptimizationInfo catch exception or dis-match size!");
            retDexStatus = null;
        }
        int index22 = 0;
        for (PackageParser.Package pkg2 : packages) {
            indentingPrintWriter.println("[" + pkg2.packageName + "]");
            pw.increaseIndent();
            boolean mapleApp = (pkg2.applicationInfo.hwFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0;
            String[] dexCodeInstructionSets2 = InstructionSets.getDexCodeInstructionSets(InstructionSets.getAppDexInstructionSets(pkg2.applicationInfo));
            List<String> paths2 = pkg2.getAllCodePathsExcludingResourceOnly();
            ArrayList<DexOptPathParam> dexOptPathParamList2 = dexOptPathParamList;
            PackageDexUsage.PackageUseInfo useInfo = dexManager.getPackageUseInfoOrDefault(pkg2.packageName);
            int index3 = index22;
            Iterator<String> it3 = paths2.iterator();
            while (it3.hasNext()) {
                Iterator<String> it4 = it3;
                String path2 = it3.next();
                StringBuilder sb = new StringBuilder();
                int size2 = size;
                sb.append("path: ");
                sb.append(path2);
                indentingPrintWriter.println(sb.toString());
                pw.increaseIndent();
                int length2 = dexCodeInstructionSets2.length;
                int i4 = 0;
                while (i4 < length2) {
                    int i5 = length2;
                    String isa = dexCodeInstructionSets2[i4];
                    if (mapleApp) {
                        dexPaths = dexPaths2;
                        indentingPrintWriter.println("It's a mapled app.");
                        isas = isas2;
                    } else {
                        dexPaths = dexPaths2;
                        if (retDexStatus != null) {
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append(isa);
                            isas = isas2;
                            sb2.append(": [status=");
                            sb2.append(retDexStatus[2 * index3]);
                            sb2.append("] [reason=");
                            sb2.append(retDexStatus[(2 * index3) + 1]);
                            sb2.append("]");
                            indentingPrintWriter.println(sb2.toString());
                        } else {
                            isas = isas2;
                            indentingPrintWriter.println(isa + ": [Exception]");
                        }
                    }
                    index3++;
                    i4++;
                    length2 = i5;
                    dexPaths2 = dexPaths;
                    isas2 = isas;
                }
                String[] dexPaths3 = dexPaths2;
                String[] isas3 = isas2;
                if (useInfo.isUsedByOtherApps(path2)) {
                    indentingPrintWriter.println("used by other apps: " + useInfo.getLoadingPackages(path2));
                }
                Map<String, PackageDexUsage.DexUseInfo> dexUseInfoMap = useInfo.getDexUseInfoMap();
                if (!dexUseInfoMap.isEmpty()) {
                    indentingPrintWriter.println("known secondary dex files:");
                    pw.increaseIndent();
                    for (Map.Entry<String, PackageDexUsage.DexUseInfo> e3 : dexUseInfoMap.entrySet()) {
                        String path3 = path2;
                        PackageDexUsage.DexUseInfo dexUseInfo = e3.getValue();
                        indentingPrintWriter.println(e3.getKey());
                        pw.increaseIndent();
                        String[] retDexStatus3 = retDexStatus;
                        StringBuilder sb3 = new StringBuilder();
                        Map<String, PackageDexUsage.DexUseInfo> dexUseInfoMap2 = dexUseInfoMap;
                        sb3.append("class loader context: ");
                        sb3.append(dexUseInfo.getClassLoaderContext());
                        indentingPrintWriter.println(sb3.toString());
                        if (dexUseInfo.isUsedByOtherApps()) {
                            indentingPrintWriter.println("used by other apps: " + dexUseInfo.getLoadingPackages());
                        }
                        pw.decreaseIndent();
                        path2 = path3;
                        retDexStatus = retDexStatus3;
                        dexUseInfoMap = dexUseInfoMap2;
                    }
                    retDexStatus2 = retDexStatus;
                    Map<String, PackageDexUsage.DexUseInfo> map = dexUseInfoMap;
                    pw.decreaseIndent();
                } else {
                    retDexStatus2 = retDexStatus;
                    Map<String, PackageDexUsage.DexUseInfo> map2 = dexUseInfoMap;
                }
                pw.decreaseIndent();
                it3 = it4;
                size = size2;
                dexPaths2 = dexPaths3;
                isas2 = isas3;
                retDexStatus = retDexStatus2;
                DexManager dexManager2 = dexManager;
            }
            int i6 = size;
            String[] strArr5 = dexPaths2;
            String[] strArr6 = isas2;
            pw.decreaseIndent();
            dexOptPathParamList = dexOptPathParamList2;
            index22 = index3;
        }
        String[] strArr42 = retDexStatus;
        ArrayList<DexOptPathParam> arrayList22 = dexOptPathParamList;
        int i322 = size;
        String[] strArr222 = dexPaths2;
        String[] strArr322 = isas2;
    }

    private static void assertValidInstructionSetList(String[] instructionSets) throws Installer.InstallerException {
        int length = instructionSets.length;
        int i = 0;
        while (i < length) {
            String instr = instructionSets[i];
            boolean match = false;
            String[] strArr = Build.SUPPORTED_ABIS;
            int length2 = strArr.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length2) {
                    break;
                } else if (VMRuntime.getInstructionSet(strArr[i2]).equals(instr)) {
                    match = true;
                    break;
                } else {
                    i2++;
                }
            }
            if (match) {
                i++;
            } else {
                throw new Installer.InstallerException("Invalid instruction set: " + instr);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int[] getDexOptNeeded(IInstalld installd, String[] fileNames, String[] instructionSets, String[] compilerFilters, String[] clContexts, boolean[] newProfiles, boolean[] downGrades, int[] uids) throws Installer.InstallerException {
        if (installd == null) {
            return null;
        }
        int size = fileNames.length;
        if (size <= 0 || instructionSets.length != size || compilerFilters.length != size || clContexts.length != size || newProfiles.length != size || uids.length != size || downGrades.length != size) {
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
        if (installd == null) {
            return null;
        }
        int size = fileNames.length;
        if (size <= 0 || uids.length != size) {
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
        if (installd == null) {
            return null;
        }
        int size = fileNames.length;
        if (size <= 0 || instructionSets.length != size || uids.length != size) {
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
        String[] strArr = null;
        if (installd == null) {
            return null;
        }
        try {
            String[] outputPaths = new String[2];
            installd.getDexFileOutputPaths(fileName, instructionSet, uid, outputPaths);
            if (!outputPaths[0].isEmpty() && !outputPaths[1].isEmpty()) {
                strArr = outputPaths;
            }
            return strArr;
        } catch (Exception e) {
            throw Installer.InstallerException.from(e);
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getDexFileOptimizationInfo(IInstalld installd, String[] fileNames, String[] instructionSets, int[] uids) throws Installer.InstallerException {
        if (installd == null) {
            return null;
        }
        int size = fileNames.length;
        if (size <= 0 || instructionSets.length != size || uids.length != size) {
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
