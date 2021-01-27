package com.android.server.pm.dex;

import android.util.AtomicFile;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastPrintWriter;
import com.android.server.pm.AbstractStatsBase;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;

/* access modifiers changed from: package-private */
public class PackageDynamicCodeLoading extends AbstractStatsBase<Void> {
    private static final char FIELD_SEPARATOR = ':';
    static final int FILE_TYPE_DEX = 68;
    static final int FILE_TYPE_NATIVE = 78;
    private static final String FILE_VERSION_HEADER = "DCL1";
    @VisibleForTesting
    static final int MAX_FILES_PER_OWNER = 100;
    private static final Pattern PACKAGE_LINE_PATTERN = Pattern.compile("([A-Z]):([0-9]+):([^:]*):(.*)");
    private static final String PACKAGE_PREFIX = "P:";
    private static final String PACKAGE_SEPARATOR = ",";
    private static final String TAG = "PackageDynamicCodeLoading";
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private Map<String, PackageDynamicCode> mPackageMap = new HashMap();

    PackageDynamicCodeLoading() {
        super("package-dcl.list", "PackageDynamicCodeLoading_DiskWriter", false);
    }

    /* access modifiers changed from: package-private */
    public boolean record(String owningPackageName, String filePath, int fileType, int ownerUserId, String loadingPackageName) {
        boolean add;
        if (isValidFileType(fileType)) {
            synchronized (this.mLock) {
                PackageDynamicCode packageInfo = this.mPackageMap.get(owningPackageName);
                if (packageInfo == null) {
                    packageInfo = new PackageDynamicCode();
                    this.mPackageMap.put(owningPackageName, packageInfo);
                }
                add = packageInfo.add(filePath, (char) fileType, ownerUserId, loadingPackageName);
            }
            return add;
        }
        throw new IllegalArgumentException("Bad file type: " + fileType);
    }

    private static boolean isValidFileType(int fileType) {
        return fileType == 68 || fileType == 78;
    }

    /* access modifiers changed from: package-private */
    public Set<String> getAllPackagesWithDynamicCodeLoading() {
        HashSet hashSet;
        synchronized (this.mLock) {
            hashSet = new HashSet(this.mPackageMap.keySet());
        }
        return hashSet;
    }

    /* access modifiers changed from: package-private */
    public PackageDynamicCode getPackageDynamicCodeInfo(String packageName) {
        PackageDynamicCode packageDynamicCode;
        synchronized (this.mLock) {
            PackageDynamicCode info = this.mPackageMap.get(packageName);
            packageDynamicCode = null;
            if (info != null) {
                packageDynamicCode = new PackageDynamicCode(info);
            }
        }
        return packageDynamicCode;
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        synchronized (this.mLock) {
            this.mPackageMap.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean removePackage(String packageName) {
        boolean z;
        synchronized (this.mLock) {
            z = this.mPackageMap.remove(packageName) != null;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean removeUserPackage(String packageName, int userId) {
        synchronized (this.mLock) {
            PackageDynamicCode packageDynamicCode = this.mPackageMap.get(packageName);
            if (packageDynamicCode == null) {
                return false;
            }
            if (!packageDynamicCode.removeUser(userId)) {
                return false;
            }
            if (packageDynamicCode.mFileUsageMap.isEmpty()) {
                this.mPackageMap.remove(packageName);
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean removeFile(String packageName, String filePath, int userId) {
        synchronized (this.mLock) {
            PackageDynamicCode packageDynamicCode = this.mPackageMap.get(packageName);
            if (packageDynamicCode == null) {
                return false;
            }
            if (!packageDynamicCode.removeFile(filePath, userId)) {
                return false;
            }
            if (packageDynamicCode.mFileUsageMap.isEmpty()) {
                this.mPackageMap.remove(packageName);
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void syncData(Map<String, Set<Integer>> packageToUsersMap) {
        synchronized (this.mLock) {
            Iterator<Map.Entry<String, PackageDynamicCode>> it = this.mPackageMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, PackageDynamicCode> entry = it.next();
                Set<Integer> packageUsers = packageToUsersMap.get(entry.getKey());
                if (packageUsers == null) {
                    it.remove();
                } else {
                    PackageDynamicCode packageDynamicCode = entry.getValue();
                    packageDynamicCode.syncData(packageToUsersMap, packageUsers);
                    if (packageDynamicCode.mFileUsageMap.isEmpty()) {
                        it.remove();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void maybeWriteAsync() {
        super.maybeWriteAsync(null);
    }

    /* access modifiers changed from: package-private */
    public void writeNow() {
        super.writeNow(null);
    }

    /* access modifiers changed from: protected */
    public final void writeInternal(Void data) {
        AtomicFile file = getFile();
        FileOutputStream output = null;
        try {
            output = file.startWrite();
            write(output);
            file.finishWrite(output);
        } catch (IOException e) {
            file.failWrite(output);
            Slog.e(TAG, "Failed to write dynamic usage for secondary code files.", e);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void write(OutputStream output) throws IOException {
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
            writer.println(packageEntry.getKey());
            for (Map.Entry<String, DynamicCodeFile> fileEntry : packageEntry.getValue().mFileUsageMap.entrySet()) {
                String path = fileEntry.getKey();
                DynamicCodeFile dynamicCodeFile = fileEntry.getValue();
                writer.print(dynamicCodeFile.mFileType);
                writer.print(FIELD_SEPARATOR);
                writer.print(dynamicCodeFile.mUserId);
                writer.print(FIELD_SEPARATOR);
                String prefix = "";
                for (String packageName : dynamicCodeFile.mLoadingPackages) {
                    writer.print(prefix);
                    writer.print(packageName);
                    prefix = PACKAGE_SEPARATOR;
                }
                writer.print(FIELD_SEPARATOR);
                writer.println(escape(path));
            }
        }
        writer.flush();
        if (writer.checkError()) {
            throw new IOException("Writer failed");
        }
    }

    /* access modifiers changed from: package-private */
    public void read() {
        super.read((PackageDynamicCodeLoading) null);
    }

    /* access modifiers changed from: protected */
    public final void readInternal(Void data) {
        FileInputStream stream = null;
        try {
            stream = getFile().openRead();
            read((InputStream) stream);
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
            Slog.w(TAG, "Failed to parse dynamic usage for secondary code files.", e2);
        } catch (Throwable th) {
            IoUtils.closeQuietly(stream);
            throw th;
        }
        IoUtils.closeQuietly(stream);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void read(InputStream stream) throws IOException {
        Map<String, PackageDynamicCode> newPackageMap = new HashMap<>();
        read(stream, newPackageMap);
        synchronized (this.mLock) {
            this.mPackageMap = newPackageMap;
        }
    }

    private static void read(InputStream stream, Map<String, PackageDynamicCode> packageMap) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String versionLine = reader.readLine();
        if (FILE_VERSION_HEADER.equals(versionLine)) {
            String line = reader.readLine();
            if (line == null || line.startsWith(PACKAGE_PREFIX)) {
                while (line != null) {
                    String packageName = line.substring(PACKAGE_PREFIX.length());
                    PackageDynamicCode packageInfo = new PackageDynamicCode();
                    while (true) {
                        line = reader.readLine();
                        if (line == null || line.startsWith(PACKAGE_PREFIX)) {
                            break;
                        }
                        readFileInfo(line, packageInfo);
                    }
                    if (!packageInfo.mFileUsageMap.isEmpty()) {
                        packageMap.put(packageName, packageInfo);
                    }
                }
                return;
            }
            throw new IOException("Malformed line: " + line);
        }
        throw new IOException("Incorrect version line: " + versionLine);
    }

    private static void readFileInfo(String line, PackageDynamicCode output) throws IOException {
        try {
            Matcher matcher = PACKAGE_LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
                char type = matcher.group(1).charAt(0);
                int user = Integer.parseInt(matcher.group(2));
                String[] packages = matcher.group(3).split(PACKAGE_SEPARATOR);
                String path = unescape(matcher.group(4));
                if (packages.length == 0) {
                    throw new IOException("Malformed line: " + line);
                } else if (isValidFileType(type)) {
                    output.mFileUsageMap.put(path, new DynamicCodeFile(type, user, packages));
                } else {
                    throw new IOException("Unknown file type: " + line);
                }
            } else {
                throw new IOException("Malformed line: " + line);
            }
        } catch (RuntimeException e) {
            throw new IOException("Unable to parse line: " + line, e);
        }
    }

    @VisibleForTesting
    static String escape(String path) {
        if (path.indexOf(92) == -1 && path.indexOf(10) == -1 && path.indexOf(13) == -1) {
            return path;
        }
        StringBuilder result = new StringBuilder(path.length() + 10);
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '\n') {
                result.append("\\n");
            } else if (c == '\r') {
                result.append("\\r");
            } else if (c != '\\') {
                result.append(c);
            } else {
                result.append("\\\\");
            }
        }
        return result.toString();
    }

    @VisibleForTesting
    static String unescape(String escaped) throws IOException {
        int start = 0;
        int finish = escaped.indexOf(92);
        if (finish == -1) {
            return escaped;
        }
        StringBuilder result = new StringBuilder(escaped.length());
        while (finish < escaped.length() - 1) {
            result.append((CharSequence) escaped, start, finish);
            char charAt = escaped.charAt(finish + 1);
            if (charAt == '\\') {
                result.append('\\');
            } else if (charAt == 'n') {
                result.append('\n');
            } else if (charAt == 'r') {
                result.append('\r');
            } else {
                throw new IOException("Bad escape in: " + escaped);
            }
            start = finish + 2;
            finish = escaped.indexOf(92, start);
            if (finish == -1) {
                result.append((CharSequence) escaped, start, escaped.length());
                return result.toString();
            }
        }
        throw new IOException("Unexpected \\ in: " + escaped);
    }

    /* access modifiers changed from: package-private */
    public static class PackageDynamicCode {
        final Map<String, DynamicCodeFile> mFileUsageMap;

        private PackageDynamicCode() {
            this.mFileUsageMap = new HashMap();
        }

        private PackageDynamicCode(PackageDynamicCode original) {
            this.mFileUsageMap = new HashMap(original.mFileUsageMap.size());
            for (Map.Entry<String, DynamicCodeFile> entry : original.mFileUsageMap.entrySet()) {
                this.mFileUsageMap.put(entry.getKey(), new DynamicCodeFile(entry.getValue()));
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean add(String path, char fileType, int userId, String loadingPackage) {
            DynamicCodeFile fileInfo = this.mFileUsageMap.get(path);
            if (fileInfo == null) {
                if (this.mFileUsageMap.size() >= 100) {
                    return false;
                }
                this.mFileUsageMap.put(path, new DynamicCodeFile(fileType, userId, new String[]{loadingPackage}));
                return true;
            } else if (fileInfo.mUserId == userId) {
                return fileInfo.mLoadingPackages.add(loadingPackage);
            } else {
                throw new IllegalArgumentException("Cannot change userId for '" + path + "' from " + fileInfo.mUserId + " to " + userId);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean removeUser(int userId) {
            boolean updated = false;
            Iterator<DynamicCodeFile> it = this.mFileUsageMap.values().iterator();
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
        private boolean removeFile(String filePath, int userId) {
            DynamicCodeFile fileInfo = this.mFileUsageMap.get(filePath);
            if (fileInfo == null || fileInfo.mUserId != userId) {
                return false;
            }
            this.mFileUsageMap.remove(filePath);
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void syncData(Map<String, Set<Integer>> packageToUsersMap, Set<Integer> owningPackageUsers) {
            Iterator<DynamicCodeFile> fileIt = this.mFileUsageMap.values().iterator();
            while (fileIt.hasNext()) {
                DynamicCodeFile fileInfo = fileIt.next();
                int fileUserId = fileInfo.mUserId;
                if (!owningPackageUsers.contains(Integer.valueOf(fileUserId))) {
                    fileIt.remove();
                } else {
                    Iterator<String> loaderIt = fileInfo.mLoadingPackages.iterator();
                    while (loaderIt.hasNext()) {
                        Set<Integer> loadingPackageUsers = packageToUsersMap.get(loaderIt.next());
                        if (loadingPackageUsers == null || !loadingPackageUsers.contains(Integer.valueOf(fileUserId))) {
                            loaderIt.remove();
                        }
                    }
                    if (fileInfo.mLoadingPackages.isEmpty()) {
                        fileIt.remove();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class DynamicCodeFile {
        final char mFileType;
        final Set<String> mLoadingPackages;
        final int mUserId;

        private DynamicCodeFile(char type, int user, String... packages) {
            this.mFileType = type;
            this.mUserId = user;
            this.mLoadingPackages = new HashSet(Arrays.asList(packages));
        }

        private DynamicCodeFile(DynamicCodeFile original) {
            this.mFileType = original.mFileType;
            this.mUserId = original.mUserId;
            this.mLoadingPackages = new HashSet(original.mLoadingPackages);
        }
    }
}
