package com.android.server.pm;

import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Log;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.IndentingPrintWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import libcore.io.IoUtils;

class CompilerStats extends AbstractStatsBase<Void> {
    private static final int COMPILER_STATS_VERSION = 1;
    private static final String COMPILER_STATS_VERSION_HEADER = "PACKAGE_MANAGER__COMPILER_STATS__";
    private final Map<String, PackageStats> packageStats = new HashMap();

    static class PackageStats {
        private final Map<String, Long> compileTimePerCodePath = new ArrayMap(2);
        private final String packageName;

        public PackageStats(String packageName) {
            this.packageName = packageName;
        }

        public String getPackageName() {
            return this.packageName;
        }

        public long getCompileTime(String codePath) {
            String storagePath = getStoredPathFromCodePath(codePath);
            synchronized (this.compileTimePerCodePath) {
                Long l = (Long) this.compileTimePerCodePath.get(storagePath);
                if (l == null) {
                    return 0;
                }
                long longValue = l.longValue();
                return longValue;
            }
        }

        public void setCompileTime(String codePath, long compileTimeInMs) {
            String storagePath = getStoredPathFromCodePath(codePath);
            synchronized (this.compileTimePerCodePath) {
                if (compileTimeInMs <= 0) {
                    this.compileTimePerCodePath.remove(storagePath);
                } else {
                    this.compileTimePerCodePath.put(storagePath, Long.valueOf(compileTimeInMs));
                }
            }
        }

        private static String getStoredPathFromCodePath(String codePath) {
            return codePath.substring(codePath.lastIndexOf(File.separatorChar) + 1);
        }

        public void dump(IndentingPrintWriter ipw) {
            synchronized (this.compileTimePerCodePath) {
                if (this.compileTimePerCodePath.size() == 0) {
                    ipw.println("(No recorded stats)");
                } else {
                    for (Entry<String, Long> e : this.compileTimePerCodePath.entrySet()) {
                        ipw.println(" " + ((String) e.getKey()) + " - " + e.getValue());
                    }
                }
            }
        }
    }

    public CompilerStats() {
        super("package-cstats.list", "CompilerStats_DiskWriter", false);
    }

    public PackageStats getPackageStats(String packageName) {
        PackageStats packageStats;
        synchronized (this.packageStats) {
            packageStats = (PackageStats) this.packageStats.get(packageName);
        }
        return packageStats;
    }

    public void setPackageStats(String packageName, PackageStats stats) {
        synchronized (this.packageStats) {
            this.packageStats.put(packageName, stats);
        }
    }

    public PackageStats createPackageStats(String packageName) {
        PackageStats newStats;
        synchronized (this.packageStats) {
            newStats = new PackageStats(packageName);
            this.packageStats.put(packageName, newStats);
        }
        return newStats;
    }

    public PackageStats getOrCreatePackageStats(String packageName) {
        synchronized (this.packageStats) {
            PackageStats existingStats = (PackageStats) this.packageStats.get(packageName);
            if (existingStats != null) {
                return existingStats;
            }
            PackageStats createPackageStats = createPackageStats(packageName);
            return createPackageStats;
        }
    }

    public void deletePackageStats(String packageName) {
        synchronized (this.packageStats) {
            this.packageStats.remove(packageName);
        }
    }

    public void write(Writer out) {
        FastPrintWriter fpw = new FastPrintWriter(out);
        fpw.print(COMPILER_STATS_VERSION_HEADER);
        fpw.println(1);
        synchronized (this.packageStats) {
            for (PackageStats pkg : this.packageStats.values()) {
                synchronized (pkg.compileTimePerCodePath) {
                    if (!pkg.compileTimePerCodePath.isEmpty()) {
                        fpw.println(pkg.getPackageName());
                        for (Entry<String, Long> e : pkg.compileTimePerCodePath.entrySet()) {
                            fpw.println("-" + ((String) e.getKey()) + ":" + e.getValue());
                        }
                    }
                }
            }
        }
        fpw.flush();
    }

    public boolean read(Reader r) {
        synchronized (this.packageStats) {
            this.packageStats.clear();
            try {
                BufferedReader in = new BufferedReader(r);
                String versionLine = in.readLine();
                if (versionLine == null) {
                    throw new IllegalArgumentException("No version line found.");
                } else if (versionLine.startsWith(COMPILER_STATS_VERSION_HEADER)) {
                    int version = Integer.parseInt(versionLine.substring(COMPILER_STATS_VERSION_HEADER.length()));
                    if (version != 1) {
                        throw new IllegalArgumentException("Unexpected version: " + version);
                    }
                    String s;
                    PackageStats currentPackage = new PackageStats("fake package");
                    while (true) {
                        s = in.readLine();
                        if (s == null) {
                        } else if (s.startsWith("-")) {
                            int colonIndex = s.indexOf(58);
                            if (colonIndex != -1 && colonIndex != 1) {
                                currentPackage.setCompileTime(s.substring(1, colonIndex), Long.parseLong(s.substring(colonIndex + 1)));
                            }
                        } else {
                            currentPackage = getOrCreatePackageStats(s);
                        }
                    }
                    throw new IllegalArgumentException("Could not parse data " + s);
                } else {
                    throw new IllegalArgumentException("Invalid version line: " + versionLine);
                }
            } catch (Exception e) {
                Log.e("PackageManager", "Error parsing compiler stats", e);
                return false;
            }
        }
        return true;
    }

    void writeNow() {
        writeNow(null);
    }

    boolean maybeWriteAsync() {
        return maybeWriteAsync(null);
    }

    protected void writeInternal(Void data) {
        AtomicFile file = getFile();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = file.startWrite();
            OutputStreamWriter osw = new OutputStreamWriter(fileOutputStream);
            write(osw);
            osw.flush();
            file.finishWrite(fileOutputStream);
        } catch (IOException e) {
            if (fileOutputStream != null) {
                file.failWrite(fileOutputStream);
            }
            Log.e("PackageManager", "Failed to write compiler stats", e);
        }
    }

    void read() {
        read((Void) null);
    }

    protected void readInternal(Void data) {
        Throwable th;
        BufferedReader in = null;
        try {
            BufferedReader in2 = new BufferedReader(new InputStreamReader(getFile().openRead()));
            try {
                read(in2);
                IoUtils.closeQuietly(in2);
                in = in2;
            } catch (FileNotFoundException e) {
                in = in2;
                IoUtils.closeQuietly(in);
            } catch (Throwable th2) {
                th = th2;
                in = in2;
                IoUtils.closeQuietly(in);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            IoUtils.closeQuietly(in);
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(in);
            throw th;
        }
    }
}
