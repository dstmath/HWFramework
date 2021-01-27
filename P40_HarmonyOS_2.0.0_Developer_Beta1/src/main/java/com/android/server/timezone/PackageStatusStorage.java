package com.android.server.timezone;

import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.FastXmlSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public final class PackageStatusStorage {
    private static final String ATTRIBUTE_CHECK_STATUS = "checkStatus";
    private static final String ATTRIBUTE_DATA_APP_VERSION = "dataAppPackageVersion";
    private static final String ATTRIBUTE_OPTIMISTIC_LOCK_ID = "optimisticLockId";
    private static final String ATTRIBUTE_UPDATE_APP_VERSION = "updateAppPackageVersion";
    private static final String LOG_TAG = "timezone.PackageStatusStorage";
    private static final String TAG_PACKAGE_STATUS = "PackageStatus";
    private static final long UNKNOWN_PACKAGE_VERSION = -1;
    private final AtomicFile mPackageStatusFile;

    PackageStatusStorage(File storageDir) {
        this.mPackageStatusFile = new AtomicFile(new File(storageDir, "package-status.xml"), "timezone-status");
    }

    /* access modifiers changed from: package-private */
    public void initialize() throws IOException {
        if (!this.mPackageStatusFile.getBaseFile().exists()) {
            insertInitialPackageStatus();
        }
    }

    /* access modifiers changed from: package-private */
    public void deleteFileForTests() {
        synchronized (this) {
            this.mPackageStatusFile.delete();
        }
    }

    /* access modifiers changed from: package-private */
    public PackageStatus getPackageStatus() {
        PackageStatus packageStatusLocked;
        synchronized (this) {
            try {
                packageStatusLocked = getPackageStatusLocked();
            } catch (ParseException e) {
                Slog.e(LOG_TAG, "Package status invalid, resetting and retrying", e);
                recoverFromBadData(e);
                try {
                    return getPackageStatusLocked();
                } catch (ParseException e2) {
                    throw new IllegalStateException("Recovery from bad file failed", e2);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return packageStatusLocked;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0040, code lost:
        if (r0 != null) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0042, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0045, code lost:
        throw r2;
     */
    @GuardedBy({"this"})
    private PackageStatus getPackageStatusLocked() throws ParseException {
        try {
            FileInputStream fis = this.mPackageStatusFile.openRead();
            XmlPullParser parser = parseToPackageStatusTag(fis);
            Integer checkStatus = getNullableIntAttribute(parser, ATTRIBUTE_CHECK_STATUS);
            if (checkStatus == null) {
                if (fis != null) {
                    $closeResource(null, fis);
                }
                return null;
            }
            PackageStatus packageStatus = new PackageStatus(checkStatus.intValue(), new PackageVersions((long) getIntAttribute(parser, ATTRIBUTE_UPDATE_APP_VERSION), (long) getIntAttribute(parser, ATTRIBUTE_DATA_APP_VERSION)));
            if (fis != null) {
                $closeResource(null, fis);
            }
            return packageStatus;
        } catch (IOException e) {
            ParseException e2 = new ParseException("Error reading package status", 0);
            e2.initCause(e);
            throw e2;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    @GuardedBy({"this"})
    private int recoverFromBadData(Exception cause) {
        this.mPackageStatusFile.delete();
        try {
            return insertInitialPackageStatus();
        } catch (IOException e) {
            IllegalStateException fatal = new IllegalStateException(e);
            fatal.addSuppressed(cause);
            throw fatal;
        }
    }

    private int insertInitialPackageStatus() throws IOException {
        int initialOptimisticLockId = (int) System.currentTimeMillis();
        writePackageStatusLocked(null, initialOptimisticLockId, null);
        return initialOptimisticLockId;
    }

    /* access modifiers changed from: package-private */
    public CheckToken generateCheckToken(PackageVersions currentInstalledVersions) {
        int optimisticLockId;
        CheckToken checkToken;
        if (currentInstalledVersions != null) {
            synchronized (this) {
                try {
                    optimisticLockId = getCurrentOptimisticLockId();
                } catch (ParseException e) {
                    Slog.w(LOG_TAG, "Unable to find optimistic lock ID from package status");
                    optimisticLockId = recoverFromBadData(e);
                }
                int newOptimisticLockId = optimisticLockId + 1;
                try {
                    if (writePackageStatusWithOptimisticLockCheck(optimisticLockId, newOptimisticLockId, 1, currentInstalledVersions)) {
                        checkToken = new CheckToken(newOptimisticLockId, currentInstalledVersions);
                    } else {
                        throw new IllegalStateException("Unable to update status to CHECK_STARTED. synchronization failure?");
                    }
                } catch (IOException e2) {
                    throw new IllegalStateException(e2);
                }
            }
            return checkToken;
        }
        throw new NullPointerException("currentInstalledVersions == null");
    }

    /* access modifiers changed from: package-private */
    public void resetCheckState() {
        int optimisticLockId;
        synchronized (this) {
            try {
                optimisticLockId = getCurrentOptimisticLockId();
            } catch (ParseException e) {
                Slog.w(LOG_TAG, "resetCheckState: Unable to find optimistic lock ID from package status");
                optimisticLockId = recoverFromBadData(e);
            }
            int newOptimisticLockId = optimisticLockId + 1;
            try {
                if (!writePackageStatusWithOptimisticLockCheck(optimisticLockId, newOptimisticLockId, null, null)) {
                    throw new IllegalStateException("resetCheckState: Unable to reset package status, newOptimisticLockId=" + newOptimisticLockId);
                }
            } catch (IOException e2) {
                throw new IllegalStateException(e2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean markChecked(CheckToken checkToken, boolean succeeded) {
        boolean writePackageStatusWithOptimisticLockCheck;
        synchronized (this) {
            int optimisticLockId = checkToken.mOptimisticLockId;
            try {
                writePackageStatusWithOptimisticLockCheck = writePackageStatusWithOptimisticLockCheck(optimisticLockId, optimisticLockId + 1, Integer.valueOf(succeeded ? 2 : 3), checkToken.mPackageVersions);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return writePackageStatusWithOptimisticLockCheck;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001b, code lost:
        if (r0 != null) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        throw r2;
     */
    @GuardedBy({"this"})
    private int getCurrentOptimisticLockId() throws ParseException {
        try {
            FileInputStream fis = this.mPackageStatusFile.openRead();
            int intAttribute = getIntAttribute(parseToPackageStatusTag(fis), ATTRIBUTE_OPTIMISTIC_LOCK_ID);
            if (fis != null) {
                $closeResource(null, fis);
            }
            return intAttribute;
        } catch (IOException e) {
            ParseException e2 = new ParseException("Unable to read file", 0);
            e2.initCause(e);
            throw e2;
        }
    }

    private static XmlPullParser parseToPackageStatusTag(FileInputStream fis) throws ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            while (true) {
                int type = parser.next();
                if (type != 1) {
                    String tag = parser.getName();
                    if (type == 2 && TAG_PACKAGE_STATUS.equals(tag)) {
                        return parser;
                    }
                } else {
                    throw new ParseException("Unable to find PackageStatus tag", 0);
                }
            }
        } catch (XmlPullParserException e) {
            throw new IllegalStateException("Unable to configure parser", e);
        } catch (IOException e2) {
            ParseException e22 = new ParseException("Error reading XML", 0);
            e2.initCause(e2);
            throw e22;
        }
    }

    @GuardedBy({"this"})
    private boolean writePackageStatusWithOptimisticLockCheck(int optimisticLockId, int newOptimisticLockId, Integer status, PackageVersions packageVersions) throws IOException {
        try {
            if (getCurrentOptimisticLockId() != optimisticLockId) {
                return false;
            }
            writePackageStatusLocked(status, newOptimisticLockId, packageVersions);
            return true;
        } catch (ParseException e) {
            recoverFromBadData(e);
            return false;
        }
    }

    @GuardedBy({"this"})
    private void writePackageStatusLocked(Integer status, int optimisticLockId, PackageVersions packageVersions) throws IOException {
        boolean z = false;
        boolean z2 = status == null;
        if (packageVersions == null) {
            z = true;
        }
        if (z2 == z) {
            try {
                FileOutputStream fos = this.mPackageStatusFile.startWrite();
                XmlSerializer serializer = new FastXmlSerializer();
                serializer.setOutput(fos, StandardCharsets.UTF_8.name());
                serializer.startDocument(null, true);
                serializer.startTag(null, TAG_PACKAGE_STATUS);
                serializer.attribute(null, ATTRIBUTE_CHECK_STATUS, status == null ? "" : Integer.toString(status.intValue()));
                serializer.attribute(null, ATTRIBUTE_OPTIMISTIC_LOCK_ID, Integer.toString(optimisticLockId));
                long dataAppVersion = -1;
                serializer.attribute(null, ATTRIBUTE_UPDATE_APP_VERSION, Long.toString(status == null ? -1 : packageVersions.mUpdateAppVersion));
                if (status != null) {
                    dataAppVersion = packageVersions.mDataAppVersion;
                }
                serializer.attribute(null, ATTRIBUTE_DATA_APP_VERSION, Long.toString(dataAppVersion));
                serializer.endTag(null, TAG_PACKAGE_STATUS);
                serializer.endDocument();
                serializer.flush();
                this.mPackageStatusFile.finishWrite(fos);
            } catch (IOException e) {
                if (0 != 0) {
                    this.mPackageStatusFile.failWrite(null);
                }
                throw e;
            }
        } else {
            throw new IllegalArgumentException("Provide both status and packageVersions, or neither.");
        }
    }

    public void forceCheckStateForTests(int checkStatus, PackageVersions packageVersions) throws IOException {
        synchronized (this) {
            try {
                writePackageStatusLocked(Integer.valueOf(checkStatus), (int) System.currentTimeMillis(), packageVersions);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private static Integer getNullableIntAttribute(XmlPullParser parser, String attributeName) throws ParseException {
        String attributeValue = parser.getAttributeValue(null, attributeName);
        if (attributeValue != null) {
            try {
                if (attributeValue.isEmpty()) {
                    return null;
                }
                return Integer.valueOf(Integer.parseInt(attributeValue));
            } catch (NumberFormatException e) {
                throw new ParseException("Bad integer for attributeName=" + attributeName + ": " + attributeValue, 0);
            }
        } else {
            throw new ParseException("Attribute " + attributeName + " missing", 0);
        }
    }

    private static int getIntAttribute(XmlPullParser parser, String attributeName) throws ParseException {
        Integer value = getNullableIntAttribute(parser, attributeName);
        if (value != null) {
            return value.intValue();
        }
        throw new ParseException("Missing attribute " + attributeName, 0);
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("Package status: " + getPackageStatus());
    }
}
