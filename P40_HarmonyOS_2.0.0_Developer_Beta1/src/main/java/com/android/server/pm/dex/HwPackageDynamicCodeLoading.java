package com.android.server.pm.dex;

import android.content.pm.ApplicationInfo;
import android.os.FileUtils;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.FastPrintWriter;
import com.android.server.location.HwLogRecordManager;
import com.android.server.pm.AbstractStatsBase;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PackageManagerServiceUtils;
import com.huawei.android.app.HiEventEx;
import com.huawei.android.app.HiViewEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;

public class HwPackageDynamicCodeLoading extends AbstractStatsBase<Void> implements IHwPackageDynamicCodeLoading {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final Pattern DYNAMIC_LINE_PATTERN = Pattern.compile("D:([^:]+):([0-9]+):([^:]+):([0-9])");
    private static final String DYNAMIC_PREFIX = "D:";
    private static final boolean ENABLE;
    private static final String FIELD_SEPARATOR = ":";
    private static final String FILE_VERSION_HEADER = "DCL2";
    private static final int MAX_DYNAMICS = 100;
    private static final int MAX_PACKAGES = 500;
    private static final Pattern PACKAGE_LINE_PATTERN = Pattern.compile("P:([^:]+):([0-9]+):([0-9]+):([0-9])");
    private static final String PACKAGE_PREFIX = "P:";
    private static final String PACKAGE_SEPARATOR = ",";
    private static final int REPORT_LIMIT = 80;
    private static final String TAG = "HwPackageDynamicCodeLoading";
    private final Object mLock;
    private PackageManagerService mPMS;
    @GuardedBy({"mLock"})
    private Map<String, PackageDynamicCode> mPackageMap;
    private ThreadPoolExecutor threadPool;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        ENABLE = z;
    }

    public static HwPackageDynamicCodeLoading getInstance() {
        return SingletonHolder.instance;
    }

    private HwPackageDynamicCodeLoading() {
        super("package-dcl-hw.list", "HwPackageDynamicCodeLoading_DiskWriter", false);
        this.mLock = new Object();
        this.mPackageMap = new HashMap();
        this.mPMS = null;
        this.threadPool = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(10));
        if (!ENABLE) {
            Slog.i(TAG, "HwPackageDynamicCodeLoading not enable");
        } else if (!getFile().exists()) {
            Slog.i(TAG, "Create package-dcl-hw.list");
            HwPackageDynamicCodeLoading.super.writeNow((Object) null);
        }
    }

    public void record(String owningPackageName, long endTime, int primaryDexFlag) {
        if (ENABLE && owningPackageName != null && !owningPackageName.isEmpty() && endTime >= 0 && primaryDexFlag != 0 && primaryDexFlag <= 2) {
            boolean modify = false;
            synchronized (this.mLock) {
                PackageDynamicCode packageInfo = this.mPackageMap.get(owningPackageName);
                if (packageInfo == null) {
                    if (this.mPackageMap.size() < 500) {
                        this.mPackageMap.put(owningPackageName, new PackageDynamicCode(endTime, endTime, primaryDexFlag));
                        modify = true;
                    } else {
                        return;
                    }
                } else if (primaryDexFlag == 1) {
                    packageInfo.mPrimaryDexInfo.mStartTime = endTime;
                    packageInfo.mPrimaryDexInfo.mEndTime = endTime;
                    packageInfo.mPrimaryDexInfo.mPrimaryDexFlag = primaryDexFlag;
                    modify = true;
                } else if (primaryDexFlag != packageInfo.mPrimaryDexInfo.mPrimaryDexFlag) {
                    packageInfo.mPrimaryDexInfo.mEndTime = endTime;
                    packageInfo.mPrimaryDexInfo.mPrimaryDexFlag = primaryDexFlag;
                    modify = true;
                }
            }
            if (modify) {
                maybeWriteAsync();
            }
            if (DEBUG) {
                Slog.i(TAG, "Record: " + owningPackageName + ", " + endTime + ", " + primaryDexFlag);
            }
        }
    }

    public void recordDynamic(String owningPackageName, String secondaryDexPath, int userId, String loadingPackageName, int secondaryDexFlag) {
        String secondaryDexPath2;
        String owningPackageName2;
        boolean modify;
        if (ENABLE && secondaryDexPath != null && !secondaryDexPath.isEmpty() && loadingPackageName != null && !loadingPackageName.isEmpty() && userId >= 0 && secondaryDexFlag != 0 && secondaryDexFlag <= 3) {
            if (secondaryDexPath.startsWith("/data/data")) {
                secondaryDexPath2 = resloveSecondaryDexPath(secondaryDexPath, userId, loadingPackageName);
            } else {
                secondaryDexPath2 = secondaryDexPath;
            }
            if (owningPackageName == null) {
                owningPackageName2 = findOwningPackageName(secondaryDexPath2, userId, loadingPackageName);
            } else {
                owningPackageName2 = owningPackageName;
            }
            synchronized (this.mLock) {
                PackageDynamicCode packageInfo = this.mPackageMap.get(owningPackageName2);
                if (packageInfo == null) {
                    if (this.mPackageMap.size() < 500) {
                        packageInfo = new PackageDynamicCode(System.currentTimeMillis(), System.currentTimeMillis(), 1);
                        this.mPackageMap.put(owningPackageName2, packageInfo);
                        Slog.w(TAG, "Missed package info, recreate it");
                    } else {
                        return;
                    }
                }
                modify = packageInfo.add(secondaryDexPath2, userId, loadingPackageName, secondaryDexFlag);
            }
            if (modify) {
                maybeWriteAsync();
            }
            if (DEBUG) {
                Slog.i(TAG, "RecordDynamic: " + owningPackageName2 + ", " + secondaryDexPath2 + ", " + userId + ", " + loadingPackageName + ", " + secondaryDexFlag);
            }
        }
    }

    public void clear() {
        if (ENABLE) {
            synchronized (this.mLock) {
                this.mPackageMap.clear();
            }
        }
    }

    public void removePackage(String packageName) {
        boolean modify;
        if (ENABLE) {
            synchronized (this.mLock) {
                modify = this.mPackageMap.remove(packageName) != null;
            }
            if (modify) {
                maybeWriteAsync();
            }
        }
    }

    public void removeUserPackage(String packageName, int userId) {
        boolean modify;
        if (ENABLE) {
            synchronized (this.mLock) {
                PackageDynamicCode packageDynamicCode = this.mPackageMap.get(packageName);
                if (packageDynamicCode != null) {
                    modify = packageDynamicCode.removeUser(userId);
                } else {
                    return;
                }
            }
            if (modify) {
                maybeWriteAsync();
            }
        }
    }

    public void removeDexFile(String packageName, String secondaryDexPath, int userId) {
        boolean modify;
        if (ENABLE) {
            synchronized (this.mLock) {
                PackageDynamicCode packageDynamicCode = this.mPackageMap.get(packageName);
                if (packageDynamicCode != null) {
                    modify = packageDynamicCode.removeDexFile(secondaryDexPath, userId);
                } else {
                    return;
                }
            }
            if (modify) {
                maybeWriteAsync();
            }
        }
    }

    public void readAndSync(Map<String, Set<Integer>> packageToUsersMap) {
        if (ENABLE) {
            read();
            synchronized (this.mLock) {
                for (Map.Entry<String, PackageDynamicCode> entry : this.mPackageMap.entrySet()) {
                    Set<Integer> packageUsers = packageToUsersMap.get(entry.getKey());
                    if (packageUsers != null) {
                        entry.getValue().syncData(packageToUsersMap, packageUsers);
                    }
                }
            }
        }
    }

    public void makeDexOptReport() {
        if (ENABLE) {
            this.threadPool.execute(new Runnable() {
                /* class com.android.server.pm.dex.HwPackageDynamicCodeLoading.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwPackageDynamicCodeLoading.this.makeDexOptReportByThread();
                    HwPackageDynamicCodeLoading.this.writeNow();
                }
            });
        }
    }

    public void dump(OutputStream output) {
        if (ENABLE) {
            try {
                write(output);
            } catch (IOException e) {
                Slog.e(TAG, "dump failed");
            }
            writeNow();
        }
    }

    private PackageManagerService getPMS() {
        if (this.mPMS == null) {
            this.mPMS = ServiceManager.getService("package");
        }
        return this.mPMS;
    }

    private String resloveSecondaryDexPath(String secondaryDexPath, int userId, String loadingPackageName) {
        try {
            if ("/data/data".equals(PackageManagerServiceUtils.realpath(new File("/data/user/" + userId)))) {
                Slog.i(TAG, "Symlink works");
                return secondaryDexPath.replaceFirst("/data/data", "/data/user/" + userId);
            }
        } catch (IOException e) {
            Slog.e(TAG, "resolve failed: " + secondaryDexPath);
        }
        return secondaryDexPath;
    }

    private String findOwningPackageName(String secondaryDexPath, int userId, String loadingPackageName) {
        ApplicationInfo loadingAppInfo = getPMS().getApplicationInfo(loadingPackageName, 0, userId);
        try {
            Method method1 = PackageManagerService.class.getDeclaredMethod("getDexManager", new Class[0]);
            method1.setAccessible(true);
            Object object = method1.invoke(getPMS(), new Object[0]);
            if (object instanceof DexManager) {
                Method method2 = DexManager.class.getDeclaredMethod("getDexPackage", ApplicationInfo.class, String.class, Integer.TYPE);
                method2.setAccessible(true);
                String result = method2.invoke(object, loadingAppInfo, secondaryDexPath, Integer.valueOf(userId)).toString();
                method2.setAccessible(false);
                if (!result.endsWith("-0")) {
                    return result.substring(0, result.length() - 2);
                }
            }
            method1.setAccessible(false);
        } catch (NoSuchMethodException e) {
            Slog.e(TAG, "cannot find method");
        } catch (IllegalAccessException e2) {
            Slog.e(TAG, "cannot access method");
        } catch (InvocationTargetException e3) {
            Slog.e(TAG, "cannot invoke method");
        }
        Slog.w(TAG, "Could not find owning package, return loading package: " + loadingPackageName);
        return loadingPackageName;
    }

    private boolean fileIsUnder(String filePath, String directoryPath) {
        if (directoryPath == null) {
            return false;
        }
        try {
            return FileUtils.contains(new File(directoryPath).getCanonicalPath(), new File(filePath).getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    private long makeTimeSlot(long startTime, long endTime) {
        if (endTime > startTime) {
            return endTime - startTime;
        }
        return 0;
    }

    private float makeDivApprox(long divisor, long dividend) {
        if (dividend == 0) {
            return 0.0f;
        }
        return new BigDecimal(divisor).divide(new BigDecimal(dividend), 2, 4).floatValue();
    }

    private float makeDivApprox(float divisor, long dividend) {
        if (dividend == 0) {
            return 0.0f;
        }
        return new BigDecimal((double) divisor).divide(new BigDecimal(dividend), 2, 4).floatValue();
    }

    private void updateEveryPackageInfo(String packageName, PackageDynamicCode packageInfo, SparseArray<HashSet<String>> dexPathByFlag) {
        ApplicationInfo appInfo;
        SparseArray<ApplicationInfo> appInfoByUser = new SparseArray<>();
        for (int i = 1; i <= 4; i++) {
            dexPathByFlag.put(i, new HashSet<>());
        }
        for (Map.Entry<String, SecondaryDexInfo> entry : packageInfo.mSecondaryDexInfoMap.entrySet()) {
            String secondaryDexPath = entry.getKey();
            SecondaryDexInfo secondaryDexInfo = entry.getValue();
            int userId = secondaryDexInfo.mUserId;
            if (appInfoByUser.indexOfKey(userId) >= 0) {
                appInfo = appInfoByUser.get(userId);
            } else {
                appInfo = getPMS().getApplicationInfo(packageName, 4202496, userId);
                if (appInfo == null) {
                    Slog.w(TAG, "Could not find package " + packageName + " for user " + userId);
                    removeUserPackage(packageName, userId);
                }
                appInfoByUser.put(userId, appInfo);
            }
            if (appInfo != null) {
                if (!fileIsUnder(secondaryDexPath, appInfo.credentialProtectedDataDir) && !fileIsUnder(secondaryDexPath, appInfo.deviceProtectedDataDir)) {
                    Slog.w(TAG, "Could not infer CE/DE storage for path " + secondaryDexPath);
                    removeDexFile(packageName, secondaryDexPath, userId);
                } else if (dexPathByFlag.get(secondaryDexInfo.mSecondaryDexFlag) == null) {
                    HashSet<String> tmp = new HashSet<>();
                    tmp.add(secondaryDexPath);
                    dexPathByFlag.put(secondaryDexInfo.mSecondaryDexFlag, tmp);
                } else {
                    dexPathByFlag.get(secondaryDexInfo.mSecondaryDexFlag).add(secondaryDexPath);
                }
            }
        }
    }

    private void makeSummary(Set<String> packages, SummaryInfo summaryInfo) {
        PackageDynamicCode packageInfo;
        for (String packageName : packages) {
            synchronized (this.mLock) {
                packageInfo = new PackageDynamicCode(this.mPackageMap.get(packageName));
            }
            SparseArray<HashSet<String>> dexPathByFlag = new SparseArray<>();
            updateEveryPackageInfo(packageName, packageInfo, dexPathByFlag);
            if (packageInfo.mPrimaryDexInfo.mPrimaryDexFlag == 2) {
                float timeSlot = makeDivApprox(makeTimeSlot(packageInfo.mPrimaryDexInfo.mStartTime, packageInfo.mPrimaryDexInfo.mEndTime), 60000);
                SummaryInfo.access$1516(summaryInfo, timeSlot);
                SummaryInfo.access$1608(summaryInfo);
                summaryInfo.mDescDexOptApp.put(packageName, Float.valueOf(timeSlot));
            } else {
                summaryInfo.mNonDexOptApp.add(packageName);
            }
            if (summaryInfo.mStartTimeMin > packageInfo.mPrimaryDexInfo.mStartTime) {
                summaryInfo.mStartTimeMin = packageInfo.mPrimaryDexInfo.mStartTime;
            }
            if (summaryInfo.mEndTimeMax < packageInfo.mPrimaryDexInfo.mEndTime) {
                summaryInfo.mEndTimeMax = packageInfo.mPrimaryDexInfo.mEndTime;
            }
            int tmp1 = dexPathByFlag.get(1).size();
            int tmp2 = dexPathByFlag.get(2).size();
            int tmp3 = dexPathByFlag.get(3).size();
            int tmp4 = dexPathByFlag.get(4).size();
            int tmpA = tmp1 + tmp2 + tmp3 + tmp4;
            if (tmpA != 0) {
                if (tmp3 == 0 && tmp4 == 0) {
                    summaryInfo.mNonDexOptDex.add(packageName);
                } else if (tmp1 != 0 || tmp2 != 0) {
                    summaryInfo.mAscDexOptDex.put(packageName, Float.valueOf(makeDivApprox(((long) (tmp3 + tmp4)) * 100, (long) tmpA)));
                }
                Iterator<String> it = dexPathByFlag.get(1).iterator();
                while (it.hasNext()) {
                    summaryInfo.mNonDexOptPath.add(it.next());
                }
                Iterator<String> it2 = dexPathByFlag.get(2).iterator();
                while (it2.hasNext()) {
                    summaryInfo.mOnlyDex2OatPath.add(it2.next());
                }
            }
        }
    }

    private SummaryInfo getSummaryInfo() {
        Set<String> packages;
        SummaryInfo result = new SummaryInfo();
        synchronized (this.mLock) {
            packages = new HashSet<>(this.mPackageMap.keySet());
        }
        makeSummary(packages, result);
        result.mTotal = packages.size();
        Collections.sort(new ArrayList<>(result.mDescDexOptApp.entrySet()), new Comparator<Map.Entry<String, Float>>() {
            /* class com.android.server.pm.dex.HwPackageDynamicCodeLoading.AnonymousClass2 */

            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        Collections.sort(new ArrayList<>(result.mAscDexOptDex.entrySet()), new Comparator<Map.Entry<String, Float>>() {
            /* class com.android.server.pm.dex.HwPackageDynamicCodeLoading.AnonymousClass3 */

            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        result.mAverageTimeSlot = makeDivApprox(result.mAverageTimeSlot, (long) result.mCount);
        result.mPercentage = makeDivApprox(((long) result.mCount) * 100, (long) result.mTotal);
        return result;
    }

    private void makeDexOptEvent(SummaryInfo summaryInfo, ReportInfo reportInfo) {
        SimpleDateFormat tmpFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HiViewEx.report(new HiEventEx(907400034).putFloat("AVG", summaryInfo.mAverageTimeSlot).putString("INIT", tmpFormat.format(Long.valueOf(summaryInfo.mStartTimeMin))).putString("LAST", tmpFormat.format(Long.valueOf(summaryInfo.mEndTimeMax))).putFloat("RIO", summaryInfo.mPercentage).putInt("COUNT", summaryInfo.mCount).putInt("TOTAL", summaryInfo.mTotal).putStringArray("NON_DEXOPT_APP", reportInfo.mNonDexOptApp).putStringArray("LONG_DEXOPT_APP", reportInfo.mDescDexOptApp).putFloatArray("LONG_DEXOPT_APP_DATA", reportInfo.mDescDexOptAppData).putStringArray("NON_DEXOPT_DEX", reportInfo.mNonDexOptDex).putStringArray("LOW_DEXOPT_DEX", reportInfo.mAscDexOptDex).putFloatArray("LOW_DEXOPT_DEX_DATA", reportInfo.mAscDexOptDexData).putStringArray("NON_DEXOPT_PATH", reportInfo.mNonDexOptPath).putStringArray("ONLY_DEX2OAT_PATH", reportInfo.mOnlyDex2OatPath).addFilePath("/data/system/package-dcl-hw.list"));
        if (DEBUG) {
            reportInfo.dump(System.out);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void makeDexOptReportByThread() {
        SummaryInfo summaryInfo = getSummaryInfo();
        if (DEBUG) {
            summaryInfo.dump(System.out);
        }
        ReportInfo reportInfo = new ReportInfo();
        reportInfo.mNonDexOptApp = new String[Math.min(summaryInfo.mNonDexOptApp.size(), 80)];
        reportInfo.mDescDexOptApp = new String[Math.min(summaryInfo.mDescDexOptApp.size(), 80)];
        reportInfo.mDescDexOptAppData = new float[reportInfo.mDescDexOptApp.length];
        reportInfo.mNonDexOptDex = new String[Math.min(summaryInfo.mNonDexOptDex.size(), 80)];
        reportInfo.mAscDexOptDex = new String[Math.min(summaryInfo.mAscDexOptDex.size(), 80)];
        reportInfo.mAscDexOptDexData = new float[reportInfo.mAscDexOptDex.length];
        reportInfo.mNonDexOptPath = new String[Math.min(summaryInfo.mNonDexOptPath.size(), 80)];
        reportInfo.mOnlyDex2OatPath = new String[Math.min(summaryInfo.mOnlyDex2OatPath.size(), 80)];
        int index = 0;
        Iterator<String> it = summaryInfo.mNonDexOptApp.iterator();
        while (it.hasNext() && index < reportInfo.mNonDexOptApp.length) {
            reportInfo.mNonDexOptApp[index] = it.next();
            index++;
        }
        int index2 = 0;
        Iterator<Map.Entry<String, Float>> it2 = summaryInfo.mDescDexOptApp.entrySet().iterator();
        while (it2.hasNext() && index2 < reportInfo.mDescDexOptApp.length) {
            reportInfo.mDescDexOptApp[index2] = it2.next().getKey();
            reportInfo.mDescDexOptAppData[index2] = ((Float) summaryInfo.mDescDexOptApp.get(reportInfo.mDescDexOptApp[index2])).floatValue();
            index2++;
        }
        int index3 = 0;
        Iterator<String> it3 = summaryInfo.mNonDexOptDex.iterator();
        while (it3.hasNext() && index3 < reportInfo.mNonDexOptDex.length) {
            reportInfo.mNonDexOptDex[index3] = it3.next();
            index3++;
        }
        int index4 = 0;
        Iterator<Map.Entry<String, Float>> it4 = summaryInfo.mAscDexOptDex.entrySet().iterator();
        while (it4.hasNext() && index4 < reportInfo.mAscDexOptDex.length) {
            reportInfo.mAscDexOptDex[index4] = it4.next().getKey();
            reportInfo.mAscDexOptDexData[index4] = ((Float) summaryInfo.mAscDexOptDex.get(reportInfo.mAscDexOptDex[index4])).floatValue();
            index4++;
        }
        int index5 = 0;
        Iterator<String> it5 = summaryInfo.mNonDexOptPath.iterator();
        while (it5.hasNext() && index5 < reportInfo.mNonDexOptPath.length) {
            reportInfo.mNonDexOptPath[index5] = it5.next();
            index5++;
        }
        int index6 = 0;
        Iterator<String> it6 = summaryInfo.mOnlyDex2OatPath.iterator();
        while (it6.hasNext() && index6 < reportInfo.mOnlyDex2OatPath.length) {
            reportInfo.mOnlyDex2OatPath[index6] = it6.next();
            index6++;
        }
        makeDexOptEvent(summaryInfo, reportInfo);
    }

    public void maybeWriteAsync() {
        if (ENABLE) {
            HwPackageDynamicCodeLoading.super.maybeWriteAsync((Object) null);
        }
    }

    public void writeNow() {
        if (ENABLE) {
            HwPackageDynamicCodeLoading.super.writeNow((Object) null);
        }
    }

    /* access modifiers changed from: protected */
    public final void writeInternal(Void data) {
        AtomicFile file = getFile();
        FileOutputStream output = null;
        try {
            output = file.startWrite();
            write(output);
        } catch (IOException e) {
            file.failWrite(output);
            Slog.e(TAG, "writeInternal failed");
        } catch (Throwable th) {
            file.finishWrite(output);
            throw th;
        }
        file.finishWrite(output);
    }

    private void write(OutputStream output) throws IOException {
        Map<String, PackageDynamicCode> copiedMap;
        synchronized (this.mLock) {
            copiedMap = new HashMap<>(this.mPackageMap.size());
            for (Map.Entry<String, PackageDynamicCode> entry : this.mPackageMap.entrySet()) {
                copiedMap.put(entry.getKey(), new PackageDynamicCode(entry.getValue()));
            }
        }
        write(output, copiedMap);
    }

    private static void write(OutputStream output, Map<String, PackageDynamicCode> packageMap) throws IOException {
        PrintWriter writer = new FastPrintWriter(output);
        writer.println(FILE_VERSION_HEADER);
        for (Map.Entry<String, PackageDynamicCode> packageEntry : packageMap.entrySet()) {
            writer.print(PACKAGE_PREFIX);
            writer.print(packageEntry.getKey());
            writer.println(packageEntry.getValue().mPrimaryDexInfo);
            for (Map.Entry<String, SecondaryDexInfo> dynamicEntry : packageEntry.getValue().mSecondaryDexInfoMap.entrySet()) {
                writer.print(DYNAMIC_PREFIX);
                writer.print(PackageDynamicCodeLoading.escape(dynamicEntry.getKey()));
                writer.println(dynamicEntry.getValue());
            }
        }
        writer.flush();
        if (writer.checkError()) {
            throw new IOException("write error");
        }
    }

    public void read() {
        if (ENABLE) {
            HwPackageDynamicCodeLoading.super.read((Object) null);
        }
    }

    /* access modifiers changed from: protected */
    public final void readInternal(Void data) {
        FileInputStream stream = null;
        try {
            stream = getFile().openRead();
            read(stream);
        } catch (FileNotFoundException e) {
            Slog.w(TAG, "package-dcl-hw.list not found");
        } catch (IOException e2) {
            Slog.e(TAG, "readInternal failed");
        } catch (Throwable th) {
            IoUtils.closeQuietly(stream);
            throw th;
        }
        IoUtils.closeQuietly(stream);
    }

    private void read(InputStream stream) throws IOException {
        Map<String, PackageDynamicCode> newPackageMap = new HashMap<>();
        read(stream, newPackageMap);
        synchronized (this.mLock) {
            this.mPackageMap = newPackageMap;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x009e, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00b5, code lost:
        throw new java.io.IOException("Unable to parse package line: " + r2, r3);
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x009e A[ExcHandler: IllegalStateException | IndexOutOfBoundsException | NumberFormatException (r3v3 'e' java.lang.RuntimeException A[CUSTOM_DECLARE]), Splitter:B:18:0x0087] */
    private static void read(InputStream stream, Map<String, PackageDynamicCode> packageMap) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String versionLine = reader.readLine();
        if (FILE_VERSION_HEADER.equals(versionLine)) {
            String line = reader.readLine();
            if (line == null || line.startsWith(PACKAGE_PREFIX)) {
                while (line != null) {
                    Matcher matcher = PACKAGE_LINE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String packageName = matcher.group(1);
                        PackageDynamicCode packageInfo = new PackageDynamicCode(Long.parseLong(matcher.group(2)), Long.parseLong(matcher.group(3)), Integer.parseInt(matcher.group(4)));
                        while (true) {
                            line = reader.readLine();
                            if (line == null || line.startsWith(PACKAGE_PREFIX)) {
                                break;
                            }
                            readSecondaryDexInfo(line, packageInfo);
                        }
                        packageMap.put(packageName, packageInfo);
                    } else {
                        try {
                            throw new IOException("Malformed package line: " + line);
                        } catch (IllegalStateException | IndexOutOfBoundsException | NumberFormatException e) {
                        }
                    }
                }
                return;
            }
            throw new IOException("Malformed first package line: " + line);
        }
        throw new IOException("Incorrect version line: " + versionLine);
    }

    private static void readSecondaryDexInfo(String dynamicLine, PackageDynamicCode output) throws IOException {
        try {
            Matcher matcher = DYNAMIC_LINE_PATTERN.matcher(dynamicLine);
            if (matcher.matches()) {
                String secondaryDexPath = PackageDynamicCodeLoading.unescape(matcher.group(1));
                int userId = Integer.parseInt(matcher.group(2));
                String[] packages = matcher.group(3).split(",");
                int secondaryDexFlag = Integer.parseInt(matcher.group(4));
                if (packages.length == 0) {
                    throw new IOException("Loading packages empty: " + dynamicLine);
                } else if (output.mSecondaryDexInfoMap != null) {
                    output.mSecondaryDexInfoMap.put(secondaryDexPath, new SecondaryDexInfo(userId, packages, secondaryDexFlag));
                }
            } else {
                throw new IOException("Malformed dynamic line: " + dynamicLine);
            }
        } catch (IllegalStateException | IndexOutOfBoundsException | NumberFormatException e) {
            throw new IOException("Unable to parse dynamic line: " + dynamicLine, e);
        }
    }

    private static class SingletonHolder {
        private static HwPackageDynamicCodeLoading instance = new HwPackageDynamicCodeLoading();

        private SingletonHolder() {
        }
    }

    /* access modifiers changed from: private */
    public static class PackageDynamicCode {
        private PrimaryDexInfo mPrimaryDexInfo;
        private Map<String, SecondaryDexInfo> mSecondaryDexInfoMap;

        private PackageDynamicCode(long startTime, long endTime, int primaryDexFlag) {
            this.mPrimaryDexInfo = new PrimaryDexInfo(startTime, endTime, primaryDexFlag);
            this.mSecondaryDexInfoMap = new HashMap();
        }

        private PackageDynamicCode(PackageDynamicCode original) {
            this.mPrimaryDexInfo = original.mPrimaryDexInfo;
            this.mSecondaryDexInfoMap = new HashMap(original.mSecondaryDexInfoMap.size());
            for (Map.Entry<String, SecondaryDexInfo> entry : original.mSecondaryDexInfoMap.entrySet()) {
                this.mSecondaryDexInfoMap.put(entry.getKey(), new SecondaryDexInfo(entry.getValue()));
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean add(String secondaryDexPath, int userId, String loadingPackageName, int secondaryDexFlag) {
            SecondaryDexInfo dexInfo = this.mSecondaryDexInfoMap.get(secondaryDexPath);
            if (dexInfo == null) {
                if (this.mSecondaryDexInfoMap.size() >= 100) {
                    return false;
                }
                this.mSecondaryDexInfoMap.put(secondaryDexPath, new SecondaryDexInfo(userId, new String[]{loadingPackageName}, secondaryDexFlag));
                return true;
            } else if (dexInfo.mUserId != userId) {
                Slog.w(HwPackageDynamicCodeLoading.TAG, "Cannot change" + secondaryDexPath + " from " + dexInfo.mUserId + " to " + userId);
                return false;
            } else {
                boolean modify = false;
                if (dexInfo.mSecondaryDexFlag < secondaryDexFlag) {
                    SecondaryDexInfo.access$1312(dexInfo, secondaryDexFlag - 1);
                    modify = true;
                }
                return modify || dexInfo.mLoadingPackages.add(loadingPackageName);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean removeUser(int userId) {
            boolean updated = false;
            Iterator<SecondaryDexInfo> it = this.mSecondaryDexInfoMap.values().iterator();
            while (it.hasNext()) {
                if (it.next().mUserId == userId) {
                    it.remove();
                    updated = true;
                }
            }
            return updated;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean removeDexFile(String secondaryDexPath, int userId) {
            SecondaryDexInfo dexInfo = this.mSecondaryDexInfoMap.get(secondaryDexPath);
            if (dexInfo == null || dexInfo.mUserId != userId) {
                return false;
            }
            this.mSecondaryDexInfoMap.remove(secondaryDexPath);
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void syncData(Map<String, Set<Integer>> packageToUsersMap, Set<Integer> owningPackageUsers) {
            Iterator<SecondaryDexInfo> dexIt = this.mSecondaryDexInfoMap.values().iterator();
            while (dexIt.hasNext()) {
                SecondaryDexInfo dexInfo = dexIt.next();
                int userId = dexInfo.mUserId;
                if (!owningPackageUsers.contains(Integer.valueOf(userId))) {
                    dexIt.remove();
                } else {
                    Iterator<String> loaderIt = dexInfo.mLoadingPackages.iterator();
                    while (loaderIt.hasNext()) {
                        Set<Integer> loadingPackageUsers = packageToUsersMap.get(loaderIt.next());
                        if (loadingPackageUsers == null || !loadingPackageUsers.contains(Integer.valueOf(userId))) {
                            loaderIt.remove();
                        }
                    }
                    if (dexInfo.mLoadingPackages.isEmpty()) {
                        dexIt.remove();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PrimaryDexInfo {
        private long mEndTime;
        private int mPrimaryDexFlag;
        private long mStartTime;

        private PrimaryDexInfo(long startTime, long endTime, int primaryDexFlag) {
            this.mStartTime = startTime;
            this.mEndTime = endTime;
            this.mPrimaryDexFlag = primaryDexFlag;
        }

        private PrimaryDexInfo(PrimaryDexInfo original) {
            this(original.mStartTime, original.mEndTime, original.mPrimaryDexFlag);
        }

        public String toString() {
            StringBuffer stringBuffer = new StringBuffer(":");
            stringBuffer.append(String.valueOf(this.mStartTime));
            stringBuffer.append(":");
            stringBuffer.append(String.valueOf(this.mEndTime));
            stringBuffer.append(":");
            return stringBuffer.append(String.valueOf(this.mPrimaryDexFlag)).toString();
        }
    }

    /* access modifiers changed from: private */
    public static class SecondaryDexInfo {
        private Set<String> mLoadingPackages;
        private int mSecondaryDexFlag;
        private int mUserId;

        static /* synthetic */ int access$1312(SecondaryDexInfo x0, int x1) {
            int i = x0.mSecondaryDexFlag + x1;
            x0.mSecondaryDexFlag = i;
            return i;
        }

        private SecondaryDexInfo(int userId, String[] packages, int secondaryDexFlag) {
            this.mUserId = userId;
            this.mLoadingPackages = new HashSet(Arrays.asList(packages));
            this.mSecondaryDexFlag = secondaryDexFlag;
        }

        private SecondaryDexInfo(SecondaryDexInfo original) {
            this.mUserId = original.mUserId;
            this.mLoadingPackages = new HashSet(original.mLoadingPackages);
            this.mSecondaryDexFlag = original.mSecondaryDexFlag;
        }

        public String toString() {
            StringBuffer result = new StringBuffer(":").append(String.valueOf(this.mUserId));
            result.append(":");
            for (String s : this.mLoadingPackages) {
                result.append(s);
                result.append(",");
            }
            result.append(":");
            result.append(String.valueOf(this.mSecondaryDexFlag));
            return result.toString();
        }
    }

    /* access modifiers changed from: private */
    public static class SummaryInfo {
        private HashMap<String, Float> mAscDexOptDex;
        private float mAverageTimeSlot;
        private int mCount;
        private HashMap<String, Float> mDescDexOptApp;
        private long mEndTimeMax;
        private HashSet<String> mNonDexOptApp;
        private HashSet<String> mNonDexOptDex;
        private HashSet<String> mNonDexOptPath;
        private HashSet<String> mOnlyDex2OatPath;
        private float mPercentage;
        private long mStartTimeMin;
        private int mTotal;

        private SummaryInfo() {
            this.mAverageTimeSlot = 0.0f;
            this.mStartTimeMin = Long.MAX_VALUE;
            this.mEndTimeMax = 0;
            this.mPercentage = 0.0f;
            this.mCount = 0;
            this.mTotal = 0;
            this.mNonDexOptApp = new HashSet<>();
            this.mDescDexOptApp = new HashMap<>();
            this.mNonDexOptDex = new HashSet<>();
            this.mAscDexOptDex = new HashMap<>();
            this.mNonDexOptPath = new HashSet<>();
            this.mOnlyDex2OatPath = new HashSet<>();
        }

        static /* synthetic */ float access$1516(SummaryInfo x0, float x1) {
            float f = x0.mAverageTimeSlot + x1;
            x0.mAverageTimeSlot = f;
            return f;
        }

        static /* synthetic */ int access$1608(SummaryInfo x0) {
            int i = x0.mCount;
            x0.mCount = i + 1;
            return i;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dump(OutputStream output) {
            String str;
            String str2;
            String str3;
            String str4;
            if (output != null) {
                SimpleDateFormat tmpFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                PrintWriter writer = new FastPrintWriter(output);
                writer.println("==================== Summary Info ====================");
                writer.println("mAverageTimeSlot: " + this.mAverageTimeSlot + "min");
                StringBuilder sb = new StringBuilder();
                sb.append("mStartTimeMin: ");
                sb.append(tmpFormat.format(Long.valueOf(this.mStartTimeMin)));
                writer.print(sb.toString());
                writer.println("\tmEndTimeMax: " + tmpFormat.format(Long.valueOf(this.mEndTimeMax)));
                writer.print("mPercentage: " + this.mPercentage + "%");
                StringBuilder sb2 = new StringBuilder();
                sb2.append("\tmCount: ");
                sb2.append(this.mCount);
                writer.print(sb2.toString());
                writer.println("\tmTotal: " + this.mTotal);
                StringBuilder sb3 = new StringBuilder();
                sb3.append("mNonDexOptApp:");
                String str5 = " NA";
                sb3.append(this.mNonDexOptApp.size() == 0 ? str5 : "");
                writer.print(sb3.toString());
                Iterator<String> it = this.mNonDexOptApp.iterator();
                while (it.hasNext()) {
                    writer.print(" " + it.next());
                }
                StringBuilder sb4 = new StringBuilder();
                sb4.append("\nmDescDexOptApp:");
                if (this.mDescDexOptApp.size() == 0) {
                    str = str5;
                } else {
                    str = "";
                }
                sb4.append(str);
                writer.print(sb4.toString());
                for (Map.Entry<String, Float> entry : this.mDescDexOptApp.entrySet()) {
                    writer.print(" " + entry.getKey() + HwLogRecordManager.VERTICAL_SEPARATE + entry.getValue() + "min");
                }
                StringBuilder sb5 = new StringBuilder();
                sb5.append("\nmNonDexOptDex:");
                if (this.mNonDexOptDex.size() == 0) {
                    str2 = str5;
                } else {
                    str2 = "";
                }
                sb5.append(str2);
                writer.print(sb5.toString());
                Iterator<String> it2 = this.mNonDexOptDex.iterator();
                while (it2.hasNext()) {
                    writer.print(" " + it2.next());
                }
                StringBuilder sb6 = new StringBuilder();
                sb6.append("\nmAscDexOptDex:");
                if (this.mAscDexOptDex.size() == 0) {
                    str3 = str5;
                } else {
                    str3 = "";
                }
                sb6.append(str3);
                writer.print(sb6.toString());
                for (Map.Entry<String, Float> entry2 : this.mAscDexOptDex.entrySet()) {
                    writer.print(" " + entry2.getKey() + HwLogRecordManager.VERTICAL_SEPARATE + entry2.getValue() + "%");
                }
                StringBuilder sb7 = new StringBuilder();
                sb7.append("\nmNonDexOptPath:");
                if (this.mNonDexOptPath.size() == 0) {
                    str4 = str5;
                } else {
                    str4 = "";
                }
                sb7.append(str4);
                writer.print(sb7.toString());
                Iterator<String> it3 = this.mNonDexOptPath.iterator();
                while (it3.hasNext()) {
                    writer.print(" " + it3.next());
                }
                StringBuilder sb8 = new StringBuilder();
                sb8.append("\nmOnlyDex2OatPath:");
                if (this.mOnlyDex2OatPath.size() != 0) {
                    str5 = "";
                }
                sb8.append(str5);
                writer.print(sb8.toString());
                Iterator<String> it4 = this.mOnlyDex2OatPath.iterator();
                while (it4.hasNext()) {
                    writer.print(" " + it4.next());
                }
                writer.flush();
                if (writer.checkError()) {
                    Slog.w(HwPackageDynamicCodeLoading.TAG, "Dump SummaryInfo error");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ReportInfo {
        private String[] mAscDexOptDex;
        private float[] mAscDexOptDexData;
        private String[] mDescDexOptApp;
        private float[] mDescDexOptAppData;
        private String[] mNonDexOptApp;
        private String[] mNonDexOptDex;
        private String[] mNonDexOptPath;
        private String[] mOnlyDex2OatPath;

        private ReportInfo() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dump(OutputStream output) {
            String str;
            String str2;
            String str3;
            String str4;
            String str5;
            String str6;
            if (output != null) {
                PrintWriter writer = new FastPrintWriter(output);
                writer.println("==================== Report Info ====================");
                StringBuilder sb = new StringBuilder();
                sb.append("mNonDexOptApp:");
                String str7 = " NA";
                sb.append(this.mNonDexOptApp.length == 0 ? str7 : "");
                writer.print(sb.toString());
                for (String s : this.mNonDexOptApp) {
                    writer.print(" " + s);
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append("\nmDescDexOptApp:");
                if (this.mDescDexOptApp.length == 0) {
                    str = str7;
                } else {
                    str = "";
                }
                sb2.append(str);
                writer.print(sb2.toString());
                for (String s2 : this.mDescDexOptApp) {
                    writer.print(" " + s2);
                }
                StringBuilder sb3 = new StringBuilder();
                sb3.append("\nmDescDexOptAppData:");
                if (this.mDescDexOptAppData.length == 0) {
                    str2 = str7;
                } else {
                    str2 = "";
                }
                sb3.append(str2);
                writer.print(sb3.toString());
                for (float f : this.mDescDexOptAppData) {
                    writer.print(" " + f + "min");
                }
                StringBuilder sb4 = new StringBuilder();
                sb4.append("\nmNonDexOptDex:");
                if (this.mNonDexOptDex.length == 0) {
                    str3 = str7;
                } else {
                    str3 = "";
                }
                sb4.append(str3);
                writer.print(sb4.toString());
                for (String s3 : this.mNonDexOptDex) {
                    writer.print(" " + s3);
                }
                StringBuilder sb5 = new StringBuilder();
                sb5.append("\nmAscDexOptDex:");
                if (this.mAscDexOptDex.length == 0) {
                    str4 = str7;
                } else {
                    str4 = "";
                }
                sb5.append(str4);
                writer.print(sb5.toString());
                for (String s4 : this.mAscDexOptDex) {
                    writer.print(" " + s4);
                }
                StringBuilder sb6 = new StringBuilder();
                sb6.append("\nmAscDexOptDexData:");
                if (this.mAscDexOptDexData.length == 0) {
                    str5 = str7;
                } else {
                    str5 = "";
                }
                sb6.append(str5);
                writer.print(sb6.toString());
                for (float f2 : this.mAscDexOptDexData) {
                    writer.print(" " + f2 + "%");
                }
                StringBuilder sb7 = new StringBuilder();
                sb7.append("\nmNonDexOptPath:");
                if (this.mNonDexOptPath.length == 0) {
                    str6 = str7;
                } else {
                    str6 = "";
                }
                sb7.append(str6);
                writer.print(sb7.toString());
                for (String s5 : this.mNonDexOptPath) {
                    writer.print(" " + s5);
                }
                StringBuilder sb8 = new StringBuilder();
                sb8.append("\nmOnlyDex2OatPath:");
                if (this.mOnlyDex2OatPath.length != 0) {
                    str7 = "";
                }
                sb8.append(str7);
                writer.print(sb8.toString());
                for (String s6 : this.mOnlyDex2OatPath) {
                    writer.print(" " + s6);
                }
                writer.flush();
                if (writer.checkError()) {
                    Slog.w(HwPackageDynamicCodeLoading.TAG, "Dump ReportInfo error");
                }
            }
        }
    }
}
