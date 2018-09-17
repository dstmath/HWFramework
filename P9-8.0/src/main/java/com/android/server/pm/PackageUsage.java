package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import android.os.FileUtils;
import android.util.AtomicFile;
import android.util.Log;
import com.android.server.os.HwBootFail;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import libcore.io.IoUtils;

class PackageUsage extends AbstractStatsBase<Map<String, Package>> {
    private static final String USAGE_FILE_MAGIC = "PACKAGE_USAGE__VERSION_";
    private static final String USAGE_FILE_MAGIC_VERSION_1 = "PACKAGE_USAGE__VERSION_1";
    private boolean mIsHistoricalPackageUsageAvailable = true;

    PackageUsage() {
        super("package-usage.list", "PackageUsage_DiskWriter", true);
    }

    boolean isHistoricalPackageUsageAvailable() {
        return this.mIsHistoricalPackageUsageAvailable;
    }

    protected void writeInternal(Map<String, Package> packages) {
        AtomicFile file = getFile();
        FileOutputStream f = null;
        try {
            f = file.startWrite();
            BufferedOutputStream out = new BufferedOutputStream(f);
            FileUtils.setPermissions(file.getBaseFile().getPath(), 416, 1000, 1032);
            StringBuilder sb = new StringBuilder();
            sb.append(USAGE_FILE_MAGIC_VERSION_1);
            sb.append(10);
            out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
            for (Package pkg : packages.values()) {
                if (pkg.getLatestPackageUseTimeInMills() != 0) {
                    sb.setLength(0);
                    sb.append(pkg.packageName);
                    for (long usageTimeInMillis : pkg.mLastPackageUsageTimeInMills) {
                        sb.append(' ');
                        sb.append(usageTimeInMillis);
                    }
                    sb.append(10);
                    out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
                }
            }
            out.flush();
            file.finishWrite(f);
        } catch (IOException e) {
            if (f != null) {
                file.failWrite(f);
            }
            Log.e("PackageManager", "Failed to write package usage times", e);
        }
    }

    protected void readInternal(Map<String, Package> packages) {
        IOException e;
        NullPointerException e2;
        Object in;
        Throwable th;
        AutoCloseable in2 = null;
        try {
            BufferedInputStream in3 = new BufferedInputStream(getFile().openRead());
            try {
                StringBuffer sb = new StringBuffer();
                String firstLine = readLine(in3, sb);
                if (firstLine != null) {
                    if (USAGE_FILE_MAGIC_VERSION_1.equals(firstLine)) {
                        readVersion1LP(packages, in3, sb);
                    } else {
                        readVersion0LP(packages, in3, sb, firstLine);
                    }
                }
                IoUtils.closeQuietly(in3);
                BufferedInputStream bufferedInputStream = in3;
            } catch (FileNotFoundException e3) {
                in2 = in3;
            } catch (IOException e4) {
                e = e4;
                in2 = in3;
                Log.w("PackageManager", "Failed to read package usage times", e);
                IoUtils.closeQuietly(in2);
            } catch (NullPointerException e5) {
                e2 = e5;
                in2 = in3;
                Log.w("PackageManager", "error NullPointerException", e2);
                HwBootFail.brokenFileBootFail(83886087, "/data/system/package-usage.list", new Throwable());
                IoUtils.closeQuietly(in2);
            } catch (Throwable th2) {
                th = th2;
                in2 = in3;
                IoUtils.closeQuietly(in2);
                throw th;
            }
        } catch (FileNotFoundException e6) {
            try {
                this.mIsHistoricalPackageUsageAvailable = false;
                IoUtils.closeQuietly(in2);
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(in2);
                throw th;
            }
        } catch (IOException e7) {
            e = e7;
            Log.w("PackageManager", "Failed to read package usage times", e);
            IoUtils.closeQuietly(in2);
        } catch (NullPointerException e8) {
            e2 = e8;
            Log.w("PackageManager", "error NullPointerException", e2);
            HwBootFail.brokenFileBootFail(83886087, "/data/system/package-usage.list", new Throwable());
            IoUtils.closeQuietly(in2);
        }
    }

    private void readVersion0LP(Map<String, Package> packages, InputStream in, StringBuffer sb, String firstLine) throws IOException {
        String line = firstLine;
        while (line != null) {
            String[] tokens = line.split(" ");
            if (tokens.length != 2) {
                throw new IOException("Failed to parse " + line + " as package-timestamp pair.");
            }
            Package pkg = (Package) packages.get(tokens[0]);
            if (pkg != null) {
                long timestamp = parseAsLong(tokens[1]);
                for (int reason = 0; reason < 8; reason++) {
                    pkg.mLastPackageUsageTimeInMills[reason] = timestamp;
                }
            }
            line = readLine(in, sb);
        }
    }

    private void readVersion1LP(Map<String, Package> packages, InputStream in, StringBuffer sb) throws IOException {
        while (true) {
            String line = readLine(in, sb);
            if (line != null) {
                String[] tokens = line.split(" ");
                if (tokens.length != 9) {
                    throw new IOException("Failed to parse " + line + " as a timestamp array.");
                }
                Package pkg = (Package) packages.get(tokens[0]);
                if (pkg != null) {
                    for (int reason = 0; reason < 8; reason++) {
                        pkg.mLastPackageUsageTimeInMills[reason] = parseAsLong(tokens[reason + 1]);
                    }
                }
            } else {
                return;
            }
        }
    }

    private long parseAsLong(String token) throws IOException {
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException e) {
            throw new IOException("Failed to parse " + token + " as a long.", e);
        }
    }

    private String readLine(InputStream in, StringBuffer sb) throws IOException {
        return readToken(in, sb, 10);
    }

    private String readToken(InputStream in, StringBuffer sb, char endOfToken) throws IOException {
        sb.setLength(0);
        while (true) {
            char ch = in.read();
            if (ch == 65535) {
                if (sb.length() == 0) {
                    return null;
                }
                throw new IOException("Unexpected EOF");
            } else if (ch == endOfToken) {
                return sb.toString();
            } else {
                sb.append((char) ch);
            }
        }
    }
}
