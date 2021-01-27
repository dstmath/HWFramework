package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import com.android.server.pm.CompilerStatsEx;
import com.android.server.pm.dex.DexoptOptionsEx;
import com.android.server.pm.dex.PackageDexUsageEx;

public class HwParallelPackageDexOptimizerEx {
    private HwParallelPackageDexOptimizer hwParallelPackageDexOptimizer;

    public HwParallelPackageDexOptimizerEx(Context context, PackageDexOptimizerEx pdo) {
        this.hwParallelPackageDexOptimizer = new HwParallelPackageDexOptimizer(context, pdo.getmOptimizer());
    }

    public HwParallelPackageDexOptimizer getHwParallelPackageDexOptimizer() {
        return this.hwParallelPackageDexOptimizer;
    }

    public void setHwParallelPackageDexOptimizer(HwParallelPackageDexOptimizer hwParallelPackageDexOptimizer2) {
        this.hwParallelPackageDexOptimizer = hwParallelPackageDexOptimizer2;
    }

    public void submit(PackageParserEx.PackageEx pkg, String[] instructionSets, CompilerStatsEx.PackageStatsEx packageStats, PackageDexUsageEx.PackageUseInfoEx packageUseInfo, DexoptOptionsEx options) {
        this.hwParallelPackageDexOptimizer.submit((PackageParser.Package) pkg.getPackage(), instructionSets, packageStats.getPackageStats(), packageUseInfo.getPackageUseInfo(), options.getDexoptOptions());
    }
}
