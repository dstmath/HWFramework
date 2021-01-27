package com.android.server.pm;

import com.android.server.pm.CompilerStats;

public class CompilerStatsEx {
    private CompilerStats mStats;

    public static class PackageStatsEx {
        private CompilerStats.PackageStats mPackageStats;

        public CompilerStats.PackageStats getPackageStats() {
            return this.mPackageStats;
        }

        public void setPackageStats(CompilerStats.PackageStats mPackageStats2) {
            this.mPackageStats = mPackageStats2;
        }
    }

    public CompilerStats getCompilerStats() {
        return this.mStats;
    }

    public void setCompilerStats(CompilerStats mStats2) {
        this.mStats = mStats2;
    }
}
