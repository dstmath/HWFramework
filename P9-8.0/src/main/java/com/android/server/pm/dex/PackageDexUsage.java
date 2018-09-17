package com.android.server.pm.dex;

import android.os.Build;
import android.util.AtomicFile;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.FastPrintWriter;
import com.android.server.pm.AbstractStatsBase;
import com.android.server.pm.PackageManagerServiceUtils;
import dalvik.system.VMRuntime;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import libcore.io.IoUtils;

public class PackageDexUsage extends AbstractStatsBase<Void> {
    private static final String DEX_LINE_CHAR = "#";
    private static final int PACKAGE_DEX_USAGE_VERSION = 1;
    private static final String PACKAGE_DEX_USAGE_VERSION_HEADER = "PACKAGE_MANAGER__PACKAGE_DEX_USAGE__";
    private static final String SPLIT_CHAR = ",";
    private static final String TAG = "PackageDexUsage";
    @GuardedBy("mPackageUseInfoMap")
    private Map<String, PackageUseInfo> mPackageUseInfoMap = new HashMap();

    public static class DexUseInfo {
        private boolean mIsUsedByOtherApps;
        private final Set<String> mLoaderIsas;
        private final int mOwnerUserId;

        public DexUseInfo(boolean isUsedByOtherApps, int ownerUserId) {
            this(isUsedByOtherApps, ownerUserId, null);
        }

        public DexUseInfo(boolean isUsedByOtherApps, int ownerUserId, String loaderIsa) {
            this.mIsUsedByOtherApps = isUsedByOtherApps;
            this.mOwnerUserId = ownerUserId;
            this.mLoaderIsas = new HashSet();
            if (loaderIsa != null) {
                this.mLoaderIsas.add(loaderIsa);
            }
        }

        public DexUseInfo(DexUseInfo other) {
            this.mIsUsedByOtherApps = other.mIsUsedByOtherApps;
            this.mOwnerUserId = other.mOwnerUserId;
            this.mLoaderIsas = new HashSet(other.mLoaderIsas);
        }

        private boolean merge(DexUseInfo dexUseInfo) {
            boolean z;
            boolean oldIsUsedByOtherApps = this.mIsUsedByOtherApps;
            if (this.mIsUsedByOtherApps) {
                z = true;
            } else {
                z = dexUseInfo.mIsUsedByOtherApps;
            }
            this.mIsUsedByOtherApps = z;
            if (this.mLoaderIsas.addAll(dexUseInfo.mLoaderIsas) || oldIsUsedByOtherApps != this.mIsUsedByOtherApps) {
                return true;
            }
            return false;
        }

        public boolean isUsedByOtherApps() {
            return this.mIsUsedByOtherApps;
        }

        public int getOwnerUserId() {
            return this.mOwnerUserId;
        }

        public Set<String> getLoaderIsas() {
            return this.mLoaderIsas;
        }
    }

    public static class PackageUseInfo {
        private final Map<String, DexUseInfo> mDexUseInfoMap;
        private boolean mIsUsedByOtherApps;

        public PackageUseInfo() {
            this.mIsUsedByOtherApps = false;
            this.mDexUseInfoMap = new HashMap();
        }

        public PackageUseInfo(PackageUseInfo other) {
            this.mIsUsedByOtherApps = other.mIsUsedByOtherApps;
            this.mDexUseInfoMap = new HashMap();
            for (Entry<String, DexUseInfo> e : other.mDexUseInfoMap.entrySet()) {
                this.mDexUseInfoMap.put((String) e.getKey(), new DexUseInfo((DexUseInfo) e.getValue()));
            }
        }

        private boolean merge(boolean isUsedByOtherApps) {
            boolean oldIsUsedByOtherApps = this.mIsUsedByOtherApps;
            if (this.mIsUsedByOtherApps) {
                isUsedByOtherApps = true;
            }
            this.mIsUsedByOtherApps = isUsedByOtherApps;
            if (oldIsUsedByOtherApps != this.mIsUsedByOtherApps) {
                return true;
            }
            return false;
        }

        public boolean isUsedByOtherApps() {
            return this.mIsUsedByOtherApps;
        }

        public Map<String, DexUseInfo> getDexUseInfoMap() {
            return this.mDexUseInfoMap;
        }
    }

    public PackageDexUsage() {
        super("package-dex-usage.list", "PackageDexUsage_DiskWriter", false);
    }

    public boolean record(String owningPackageName, String dexPath, int ownerUserId, String loaderIsa, boolean isUsedByOtherApps, boolean primaryOrSplit) {
        if (PackageManagerServiceUtils.checkISA(loaderIsa)) {
            synchronized (this.mPackageUseInfoMap) {
                PackageUseInfo packageUseInfo = (PackageUseInfo) this.mPackageUseInfoMap.get(owningPackageName);
                boolean -wrap0;
                if (packageUseInfo == null) {
                    packageUseInfo = new PackageUseInfo();
                    if (primaryOrSplit) {
                        packageUseInfo.mIsUsedByOtherApps = isUsedByOtherApps;
                    } else {
                        packageUseInfo.mDexUseInfoMap.put(dexPath, new DexUseInfo(isUsedByOtherApps, ownerUserId, loaderIsa));
                    }
                    this.mPackageUseInfoMap.put(owningPackageName, packageUseInfo);
                    return true;
                } else if (primaryOrSplit) {
                    -wrap0 = packageUseInfo.merge(isUsedByOtherApps);
                    return -wrap0;
                } else {
                    DexUseInfo newData = new DexUseInfo(isUsedByOtherApps, ownerUserId, loaderIsa);
                    DexUseInfo existingData = (DexUseInfo) packageUseInfo.mDexUseInfoMap.get(dexPath);
                    if (existingData == null) {
                        packageUseInfo.mDexUseInfoMap.put(dexPath, newData);
                        return true;
                    } else if (ownerUserId != existingData.mOwnerUserId) {
                        throw new IllegalArgumentException("Trying to change ownerUserId for  dex path " + dexPath + " from " + existingData.mOwnerUserId + " to " + ownerUserId);
                    } else {
                        -wrap0 = existingData.merge(newData);
                        return -wrap0;
                    }
                }
            }
        }
        throw new IllegalArgumentException("loaderIsa " + loaderIsa + " is unsupported");
    }

    public void read() {
        read((Void) null);
    }

    public void maybeWriteAsync() {
        maybeWriteAsync((Void) null);
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
            Slog.e(TAG, "Failed to write usage for dex files", e);
        }
    }

    void write(Writer out) {
        Map<String, PackageUseInfo> packageUseInfoMapClone = clonePackageUseInfoMap();
        FastPrintWriter fpw = new FastPrintWriter(out);
        fpw.print(PACKAGE_DEX_USAGE_VERSION_HEADER);
        fpw.println(1);
        for (Entry<String, PackageUseInfo> pEntry : packageUseInfoMapClone.entrySet()) {
            String packageName = (String) pEntry.getKey();
            PackageUseInfo packageUseInfo = (PackageUseInfo) pEntry.getValue();
            CharSequence charSequence = SPLIT_CHAR;
            CharSequence[] charSequenceArr = new CharSequence[2];
            charSequenceArr[0] = packageName;
            charSequenceArr[1] = writeBoolean(packageUseInfo.mIsUsedByOtherApps);
            fpw.println(String.join(charSequence, charSequenceArr));
            for (Entry<String, DexUseInfo> dEntry : packageUseInfo.mDexUseInfoMap.entrySet()) {
                DexUseInfo dexUseInfo = (DexUseInfo) dEntry.getValue();
                fpw.println(DEX_LINE_CHAR + ((String) dEntry.getKey()));
                charSequence = SPLIT_CHAR;
                charSequenceArr = new CharSequence[2];
                charSequenceArr[0] = Integer.toString(dexUseInfo.mOwnerUserId);
                charSequenceArr[1] = writeBoolean(dexUseInfo.mIsUsedByOtherApps);
                fpw.print(String.join(charSequence, charSequenceArr));
                for (String isa : dexUseInfo.mLoaderIsas) {
                    fpw.print(SPLIT_CHAR + isa);
                }
                fpw.println();
            }
        }
        fpw.flush();
    }

    protected void readInternal(Void data) {
        IOException e;
        Object in;
        Throwable th;
        BufferedReader in2 = null;
        try {
            BufferedReader in3 = new BufferedReader(new InputStreamReader(getFile().openRead()));
            try {
                read(in3);
                IoUtils.closeQuietly(in3);
                in2 = in3;
            } catch (FileNotFoundException e2) {
                in2 = in3;
                IoUtils.closeQuietly(in2);
            } catch (IOException e3) {
                e = e3;
                in = in3;
                try {
                    Slog.w(TAG, "Failed to parse package dex usage.", e);
                    IoUtils.closeQuietly(in);
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(in);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                in = in3;
                IoUtils.closeQuietly(in);
                throw th;
            }
        } catch (FileNotFoundException e4) {
            IoUtils.closeQuietly(in2);
        } catch (IOException e5) {
            e = e5;
            Slog.w(TAG, "Failed to parse package dex usage.", e);
            IoUtils.closeQuietly(in);
        }
    }

    void read(Reader reader) throws IOException {
        Map<String, PackageUseInfo> data = new HashMap();
        BufferedReader in = new BufferedReader(reader);
        String versionLine = in.readLine();
        if (versionLine == null) {
            throw new IllegalStateException("No version line found.");
        } else if (versionLine.startsWith(PACKAGE_DEX_USAGE_VERSION_HEADER)) {
            int version = Integer.parseInt(versionLine.substring(PACKAGE_DEX_USAGE_VERSION_HEADER.length()));
            if (version != 1) {
                throw new IllegalStateException("Unexpected version: " + version);
            }
            String currentPakage = null;
            PackageUseInfo currentPakageData = null;
            Set<String> supportedIsas = new HashSet();
            for (String abi : Build.SUPPORTED_ABIS) {
                supportedIsas.add(VMRuntime.getInstructionSet(abi));
            }
            while (true) {
                String s = in.readLine();
                String[] elems;
                if (s == null) {
                    synchronized (this.mPackageUseInfoMap) {
                        this.mPackageUseInfoMap.clear();
                        this.mPackageUseInfoMap.putAll(data);
                    }
                    return;
                } else if (!s.startsWith(DEX_LINE_CHAR)) {
                    elems = s.split(SPLIT_CHAR);
                    if (elems.length != 2) {
                        throw new IllegalStateException("Invalid PackageDexUsage line: " + s);
                    }
                    currentPakage = elems[0];
                    currentPakageData = new PackageUseInfo();
                    currentPakageData.mIsUsedByOtherApps = readBoolean(elems[1]);
                    data.put(currentPakage, currentPakageData);
                } else if (currentPakage == null) {
                    throw new IllegalStateException("Malformed PackageDexUsage file. Expected package line before dex line.");
                } else {
                    String dexPath = s.substring(DEX_LINE_CHAR.length());
                    s = in.readLine();
                    if (s == null) {
                        throw new IllegalStateException("Could not fine dexUseInfo for line: " + s);
                    }
                    elems = s.split(SPLIT_CHAR);
                    if (elems.length < 3) {
                        throw new IllegalStateException("Invalid PackageDexUsage line: " + s);
                    }
                    DexUseInfo dexUseInfo = new DexUseInfo(readBoolean(elems[1]), Integer.parseInt(elems[0]));
                    for (int i = 2; i < elems.length; i++) {
                        String isa = elems[i];
                        if (supportedIsas.contains(isa)) {
                            dexUseInfo.mLoaderIsas.add(elems[i]);
                        } else {
                            Slog.wtf(TAG, "Unsupported ISA when parsing PackageDexUsage: " + isa);
                        }
                    }
                    if (supportedIsas.isEmpty()) {
                        Slog.wtf(TAG, "Ignore dexPath when parsing PackageDexUsage because of unsupported isas. dexPath=" + dexPath);
                    } else {
                        currentPakageData.mDexUseInfoMap.put(dexPath, dexUseInfo);
                    }
                }
            }
        } else {
            throw new IllegalStateException("Invalid version line: " + versionLine);
        }
    }

    public void syncData(Map<String, Set<Integer>> packageToUsersMap) {
        synchronized (this.mPackageUseInfoMap) {
            Iterator<Entry<String, PackageUseInfo>> pIt = this.mPackageUseInfoMap.entrySet().iterator();
            while (pIt.hasNext()) {
                Entry<String, PackageUseInfo> pEntry = (Entry) pIt.next();
                PackageUseInfo packageUseInfo = (PackageUseInfo) pEntry.getValue();
                Set<Integer> users = (Set) packageToUsersMap.get((String) pEntry.getKey());
                if (users == null) {
                    pIt.remove();
                } else {
                    Iterator<Entry<String, DexUseInfo>> dIt = packageUseInfo.mDexUseInfoMap.entrySet().iterator();
                    while (dIt.hasNext()) {
                        if (!users.contains(Integer.valueOf(((DexUseInfo) ((Entry) dIt.next()).getValue()).mOwnerUserId))) {
                            dIt.remove();
                        }
                    }
                    if (!packageUseInfo.mIsUsedByOtherApps && packageUseInfo.mDexUseInfoMap.isEmpty()) {
                        pIt.remove();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0017, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean clearUsedByOtherApps(String packageName) {
        synchronized (this.mPackageUseInfoMap) {
            PackageUseInfo packageUseInfo = (PackageUseInfo) this.mPackageUseInfoMap.get(packageName);
            if (packageUseInfo == null || (packageUseInfo.mIsUsedByOtherApps ^ 1) != 0) {
            } else {
                packageUseInfo.mIsUsedByOtherApps = false;
                return true;
            }
        }
    }

    public boolean removePackage(String packageName) {
        boolean z;
        synchronized (this.mPackageUseInfoMap) {
            z = this.mPackageUseInfoMap.remove(packageName) != null;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:23:0x0053, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean removeUserPackage(String packageName, int userId) {
        synchronized (this.mPackageUseInfoMap) {
            PackageUseInfo packageUseInfo = (PackageUseInfo) this.mPackageUseInfoMap.get(packageName);
            if (packageUseInfo == null) {
                return false;
            }
            boolean updated = false;
            Iterator<Entry<String, DexUseInfo>> dIt = packageUseInfo.mDexUseInfoMap.entrySet().iterator();
            while (dIt.hasNext()) {
                if (((DexUseInfo) ((Entry) dIt.next()).getValue()).mOwnerUserId == userId) {
                    dIt.remove();
                    updated = true;
                }
            }
            if (packageUseInfo.mDexUseInfoMap.isEmpty() && (packageUseInfo.mIsUsedByOtherApps ^ 1) != 0) {
                this.mPackageUseInfoMap.remove(packageName);
                updated = true;
            }
        }
    }

    public boolean removeDexFile(String packageName, String dexFile, int userId) {
        synchronized (this.mPackageUseInfoMap) {
            PackageUseInfo packageUseInfo = (PackageUseInfo) this.mPackageUseInfoMap.get(packageName);
            if (packageUseInfo == null) {
                return false;
            }
            boolean removeDexFile = removeDexFile(packageUseInfo, dexFile, userId);
            return removeDexFile;
        }
    }

    private boolean removeDexFile(PackageUseInfo packageUseInfo, String dexFile, int userId) {
        DexUseInfo dexUseInfo = (DexUseInfo) packageUseInfo.mDexUseInfoMap.get(dexFile);
        if (dexUseInfo == null || dexUseInfo.mOwnerUserId != userId) {
            return false;
        }
        packageUseInfo.mDexUseInfoMap.remove(dexFile);
        return true;
    }

    public PackageUseInfo getPackageUseInfo(String packageName) {
        PackageUseInfo packageUseInfo = null;
        synchronized (this.mPackageUseInfoMap) {
            PackageUseInfo useInfo = (PackageUseInfo) this.mPackageUseInfoMap.get(packageName);
            if (useInfo != null) {
                packageUseInfo = new PackageUseInfo(useInfo);
            }
        }
        return packageUseInfo;
    }

    public Set<String> getAllPackagesWithSecondaryDexFiles() {
        Set<String> packages = new HashSet();
        synchronized (this.mPackageUseInfoMap) {
            for (Entry<String, PackageUseInfo> entry : this.mPackageUseInfoMap.entrySet()) {
                if (!((PackageUseInfo) entry.getValue()).mDexUseInfoMap.isEmpty()) {
                    packages.add((String) entry.getKey());
                }
            }
        }
        return packages;
    }

    public void clear() {
        synchronized (this.mPackageUseInfoMap) {
            this.mPackageUseInfoMap.clear();
        }
    }

    private Map<String, PackageUseInfo> clonePackageUseInfoMap() {
        Map<String, PackageUseInfo> clone = new HashMap();
        synchronized (this.mPackageUseInfoMap) {
            for (Entry<String, PackageUseInfo> e : this.mPackageUseInfoMap.entrySet()) {
                clone.put((String) e.getKey(), new PackageUseInfo((PackageUseInfo) e.getValue()));
            }
        }
        return clone;
    }

    private String writeBoolean(boolean bool) {
        return bool ? "1" : "0";
    }

    private boolean readBoolean(String bool) {
        if ("0".equals(bool)) {
            return false;
        }
        if ("1".equals(bool)) {
            return true;
        }
        throw new IllegalArgumentException("Unknown bool encoding: " + bool);
    }

    private boolean contains(int[] array, int elem) {
        for (int i : array) {
            if (elem == i) {
                return true;
            }
        }
        return false;
    }

    public String dump() {
        StringWriter sw = new StringWriter();
        write(sw);
        return sw.toString();
    }
}
