package com.android.internal.backup;

import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupTransport;
import android.app.backup.RestoreDescription;
import android.app.backup.RestoreSet;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.ArrayMap;
import android.util.Log;
import com.android.org.bouncycastle.util.encoders.Base64;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import libcore.io.IoUtils;

public class LocalTransport extends BackupTransport {
    private static final long CURRENT_SET_TOKEN = 1;
    private static final boolean DEBUG = false;
    private static final long FULL_BACKUP_SIZE_QUOTA = 26214400;
    private static final String FULL_DATA_DIR = "_full";
    private static final String INCREMENTAL_DIR = "_delta";
    private static final long KEY_VALUE_BACKUP_SIZE_QUOTA = 5242880;
    static final long[] POSSIBLE_SETS = {2, 3, 4, 5, 6, 7, 8, 9};
    private static final String TAG = "LocalTransport";
    private static final String TRANSPORT_DATA_MANAGEMENT_LABEL = "";
    private static final String TRANSPORT_DESTINATION_STRING = "Backing up to debug-only private cache";
    private static final String TRANSPORT_DIR_NAME = "com.android.internal.backup.LocalTransport";
    private Context mContext;
    private FileInputStream mCurFullRestoreStream;
    private File mCurrentSetDir = new File(this.mDataDir, Long.toString(1));
    private File mCurrentSetFullDir = new File(this.mCurrentSetDir, FULL_DATA_DIR);
    private File mCurrentSetIncrementalDir = new File(this.mCurrentSetDir, INCREMENTAL_DIR);
    private File mDataDir = new File(Environment.getDownloadCacheDirectory(), "backup");
    private byte[] mFullBackupBuffer;
    private BufferedOutputStream mFullBackupOutputStream;
    private long mFullBackupSize;
    private byte[] mFullRestoreBuffer;
    private FileOutputStream mFullRestoreSocketStream;
    private String mFullTargetPackage;
    private final LocalTransportParameters mParameters;
    private int mRestorePackage = -1;
    private PackageInfo[] mRestorePackages = null;
    private File mRestoreSetDir;
    private File mRestoreSetFullDir;
    private File mRestoreSetIncrementalDir;
    private int mRestoreType;
    private ParcelFileDescriptor mSocket;
    private FileInputStream mSocketInputStream;

    static class DecodedFilename implements Comparable<DecodedFilename> {
        public File file;
        public String key;

        public DecodedFilename(File f) {
            this.file = f;
            this.key = new String(Base64.decode(f.getName()));
        }

        public int compareTo(DecodedFilename other) {
            return this.key.compareTo(other.key);
        }
    }

    private class KVOperation {
        final String key;
        final byte[] value;

        KVOperation(String k, byte[] v) {
            this.key = k;
            this.value = v;
        }
    }

    private void makeDataDirs() {
        this.mCurrentSetDir.mkdirs();
        this.mCurrentSetFullDir.mkdir();
        this.mCurrentSetIncrementalDir.mkdir();
    }

    public LocalTransport(Context context, LocalTransportParameters parameters) {
        this.mContext = context;
        this.mParameters = parameters;
        makeDataDirs();
    }

    /* access modifiers changed from: package-private */
    public LocalTransportParameters getParameters() {
        return this.mParameters;
    }

    public String name() {
        return new ComponentName(this.mContext, getClass()).flattenToShortString();
    }

    public Intent configurationIntent() {
        return null;
    }

    public String currentDestinationString() {
        return TRANSPORT_DESTINATION_STRING;
    }

    public Intent dataManagementIntent() {
        return null;
    }

    public String dataManagementLabel() {
        return TRANSPORT_DATA_MANAGEMENT_LABEL;
    }

    public String transportDirName() {
        return TRANSPORT_DIR_NAME;
    }

    public int getTransportFlags() {
        int flags = LocalTransport.super.getTransportFlags();
        if (this.mParameters.isFakeEncryptionFlag()) {
            return flags | Integer.MIN_VALUE;
        }
        return flags;
    }

    public long requestBackupTime() {
        return 0;
    }

    public int initializeDevice() {
        deleteContents(this.mCurrentSetDir);
        makeDataDirs();
        return 0;
    }

    public int performBackup(PackageInfo packageInfo, ParcelFileDescriptor data) {
        return performBackup(packageInfo, data, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x013c A[SYNTHETIC, Splitter:B:71:0x013c] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0148 A[Catch:{ IOException -> 0x0140 }] */
    public int performBackup(PackageInfo packageInfo, ParcelFileDescriptor data, int flags) {
        Iterator<KVOperation> it;
        Throwable th;
        Throwable th2;
        PackageInfo packageInfo2 = packageInfo;
        boolean isIncremental = (flags & 2) != 0;
        boolean isNonIncremental = (flags & 4) != 0;
        if (isIncremental) {
            Log.i(TAG, "Performing incremental backup for " + packageInfo2.packageName);
        } else if (isNonIncremental) {
            Log.i(TAG, "Performing non-incremental backup for " + packageInfo2.packageName);
        } else {
            Log.i(TAG, "Performing backup for " + packageInfo2.packageName);
        }
        File packageDir = new File(this.mCurrentSetIncrementalDir, packageInfo2.packageName);
        boolean hasDataForPackage = !packageDir.mkdirs();
        if (!isIncremental || (!this.mParameters.isNonIncrementalOnly() && hasDataForPackage)) {
            if (isNonIncremental && hasDataForPackage) {
                Log.w(TAG, "Requested non-incremental, deleting existing data.");
                clearBackupData(packageInfo);
                packageDir.mkdirs();
            }
            try {
                ArrayList<KVOperation> changeOps = parseBackupStream(data);
                ArrayMap arrayMap = new ArrayMap();
                Iterator<KVOperation> it2 = changeOps.iterator();
                int updatedSize = parseKeySizes(packageDir, arrayMap);
                while (it2.hasNext() != 0) {
                    KVOperation op = it2.next();
                    Integer curSize = (Integer) arrayMap.get(op.key);
                    if (curSize != null) {
                        updatedSize -= curSize.intValue();
                    }
                    if (op.value != null) {
                        updatedSize += op.value.length;
                    }
                }
                if (((long) updatedSize) > KEY_VALUE_BACKUP_SIZE_QUOTA) {
                    return -1005;
                }
                Iterator<KVOperation> it3 = changeOps.iterator();
                while (it3.hasNext()) {
                    KVOperation op2 = it3.next();
                    File element = new File(packageDir, op2.key);
                    element.delete();
                    if (op2.value != null) {
                        try {
                            FileOutputStream out = new FileOutputStream(element);
                            try {
                                it = it3;
                                KVOperation kVOperation = op2;
                                try {
                                    out.write(op2.value, 0, op2.value.length);
                                } catch (Throwable th3) {
                                    th2 = th3;
                                    th = null;
                                    if (th == null) {
                                        try {
                                            out.close();
                                        } catch (Throwable th4) {
                                            th.addSuppressed(th4);
                                        }
                                    } else {
                                        out.close();
                                    }
                                    throw th2;
                                }
                                try {
                                    out.close();
                                } catch (IOException e) {
                                }
                            } catch (Throwable th5) {
                                KVOperation kVOperation2 = op2;
                                th2 = th5;
                                th = null;
                                if (th == null) {
                                }
                                throw th2;
                            }
                        } catch (IOException e2) {
                            KVOperation kVOperation3 = op2;
                            Log.e(TAG, "Unable to update key file " + element);
                            return -1000;
                        }
                    } else {
                        it = it3;
                    }
                    it3 = it;
                    PackageInfo packageInfo3 = packageInfo;
                }
                return 0;
            } catch (IOException e3) {
                IOException iOException = e3;
                Log.v(TAG, "Exception reading backup input", e3);
                return -1000;
            }
        } else {
            if (this.mParameters.isNonIncrementalOnly()) {
                Log.w(TAG, "Transport is in non-incremental only mode.");
            } else {
                Log.w(TAG, "Requested incremental, but transport currently stores no data for the package, requesting non-incremental retry.");
            }
            return -1006;
        }
    }

    private ArrayList<KVOperation> parseBackupStream(ParcelFileDescriptor data) throws IOException {
        ArrayList<KVOperation> changeOps = new ArrayList<>();
        BackupDataInput changeSet = new BackupDataInput(data.getFileDescriptor());
        while (changeSet.readNextHeader()) {
            String base64Key = new String(Base64.encode(changeSet.getKey().getBytes()));
            int dataSize = changeSet.getDataSize();
            byte[] buf = dataSize >= 0 ? new byte[dataSize] : null;
            if (dataSize >= 0) {
                changeSet.readEntityData(buf, 0, dataSize);
            }
            changeOps.add(new KVOperation(base64Key, buf));
        }
        return changeOps;
    }

    private int parseKeySizes(File packageDir, ArrayMap<String, Integer> datastore) {
        int totalSize = 0;
        String[] elements = packageDir.list();
        if (elements != null) {
            for (String file : elements) {
                int size = (int) new File(packageDir, file).length();
                totalSize += size;
                datastore.put(file, Integer.valueOf(size));
            }
        }
        return totalSize;
    }

    private void deleteContents(File dirname) {
        File[] contents = dirname.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (f.isDirectory()) {
                    deleteContents(f);
                }
                f.delete();
            }
        }
    }

    public int clearBackupData(PackageInfo packageInfo) {
        File packageDir = new File(this.mCurrentSetIncrementalDir, packageInfo.packageName);
        File[] fileset = packageDir.listFiles();
        if (fileset != null) {
            for (File f : fileset) {
                f.delete();
            }
            packageDir.delete();
        }
        File packageDir2 = new File(this.mCurrentSetFullDir, packageInfo.packageName);
        File[] tarballs = packageDir2.listFiles();
        if (tarballs != null) {
            for (File f2 : tarballs) {
                f2.delete();
            }
            packageDir2.delete();
        }
        return 0;
    }

    public int finishBackup() {
        return tearDownFullBackup();
    }

    private int tearDownFullBackup() {
        if (this.mSocket != null) {
            try {
                if (this.mFullBackupOutputStream != null) {
                    this.mFullBackupOutputStream.flush();
                    this.mFullBackupOutputStream.close();
                }
                this.mSocketInputStream = null;
                this.mFullTargetPackage = null;
                this.mSocket.close();
            } catch (IOException e) {
                return -1000;
            } finally {
                this.mSocket = null;
                this.mFullBackupOutputStream = null;
            }
        }
        return 0;
    }

    private File tarballFile(String pkgName) {
        return new File(this.mCurrentSetFullDir, pkgName);
    }

    public long requestFullBackupTime() {
        return 0;
    }

    public int checkFullBackupSize(long size) {
        if (size <= 0) {
            return -1002;
        }
        if (size > FULL_BACKUP_SIZE_QUOTA) {
            return -1005;
        }
        return 0;
    }

    public int performFullBackup(PackageInfo targetPackage, ParcelFileDescriptor socket) {
        if (this.mSocket != null) {
            Log.e(TAG, "Attempt to initiate full backup while one is in progress");
            return -1000;
        }
        try {
            this.mFullBackupSize = 0;
            this.mSocket = ParcelFileDescriptor.dup(socket.getFileDescriptor());
            this.mSocketInputStream = new FileInputStream(this.mSocket.getFileDescriptor());
            this.mFullTargetPackage = targetPackage.packageName;
            this.mFullBackupBuffer = new byte[4096];
            return 0;
        } catch (IOException e) {
            Log.e(TAG, "Unable to process socket for full backup");
            return -1000;
        }
    }

    public int sendBackupData(int numBytes) {
        if (this.mSocket == null) {
            Log.w(TAG, "Attempted sendBackupData before performFullBackup");
            return -1000;
        }
        this.mFullBackupSize += (long) numBytes;
        if (this.mFullBackupSize > FULL_BACKUP_SIZE_QUOTA) {
            return -1005;
        }
        if (numBytes > this.mFullBackupBuffer.length) {
            this.mFullBackupBuffer = new byte[numBytes];
        }
        if (this.mFullBackupOutputStream == null) {
            try {
                this.mFullBackupOutputStream = new BufferedOutputStream(new FileOutputStream(tarballFile(this.mFullTargetPackage)));
            } catch (FileNotFoundException e) {
                return -1000;
            }
        }
        int bytesLeft = numBytes;
        while (bytesLeft > 0) {
            try {
                int nRead = this.mSocketInputStream.read(this.mFullBackupBuffer, 0, bytesLeft);
                if (nRead < 0) {
                    Log.w(TAG, "Unexpected EOD; failing backup");
                    return -1000;
                }
                this.mFullBackupOutputStream.write(this.mFullBackupBuffer, 0, nRead);
                bytesLeft -= nRead;
            } catch (IOException e2) {
                Log.e(TAG, "Error handling backup data for " + this.mFullTargetPackage);
                return -1000;
            }
        }
        return 0;
    }

    public void cancelFullBackup() {
        File archive = tarballFile(this.mFullTargetPackage);
        tearDownFullBackup();
        if (archive.exists()) {
            archive.delete();
        }
    }

    public RestoreSet[] getAvailableRestoreSets() {
        long[] existing = new long[(POSSIBLE_SETS.length + 1)];
        int i = 0;
        int num = 0;
        for (long token : POSSIBLE_SETS) {
            if (new File(this.mDataDir, Long.toString(token)).exists()) {
                existing[num] = token;
                num++;
            }
        }
        existing[num] = 1;
        RestoreSet[] available = new RestoreSet[(num + 1)];
        while (true) {
            int i2 = i;
            if (i2 >= available.length) {
                return available;
            }
            available[i2] = new RestoreSet("Local disk image", "flash", existing[i2]);
            i = i2 + 1;
        }
    }

    public long getCurrentRestoreSet() {
        return 1;
    }

    public int startRestore(long token, PackageInfo[] packages) {
        this.mRestorePackages = packages;
        this.mRestorePackage = -1;
        this.mRestoreSetDir = new File(this.mDataDir, Long.toString(token));
        this.mRestoreSetIncrementalDir = new File(this.mRestoreSetDir, INCREMENTAL_DIR);
        this.mRestoreSetFullDir = new File(this.mRestoreSetDir, FULL_DATA_DIR);
        return 0;
    }

    public RestoreDescription nextRestorePackage() {
        String name;
        if (this.mRestorePackages != null) {
            boolean found = false;
            do {
                int i = this.mRestorePackage + 1;
                this.mRestorePackage = i;
                if (i >= this.mRestorePackages.length) {
                    return RestoreDescription.NO_MORE_PACKAGES;
                }
                name = this.mRestorePackages[this.mRestorePackage].packageName;
                String[] contents = new File(this.mRestoreSetIncrementalDir, name).list();
                if (contents != null && contents.length > 0) {
                    this.mRestoreType = 1;
                    found = true;
                }
                if (!found && new File(this.mRestoreSetFullDir, name).length() > 0) {
                    this.mRestoreType = 2;
                    this.mCurFullRestoreStream = null;
                    found = true;
                    continue;
                }
            } while (!found);
            return new RestoreDescription(name, this.mRestoreType);
        }
        throw new IllegalStateException("startRestore not called");
    }

    public int getRestoreData(ParcelFileDescriptor outFd) {
        FileInputStream in;
        if (this.mRestorePackages == null) {
            throw new IllegalStateException("startRestore not called");
        } else if (this.mRestorePackage < 0) {
            throw new IllegalStateException("nextRestorePackage not called");
        } else if (this.mRestoreType == 1) {
            File packageDir = new File(this.mRestoreSetIncrementalDir, this.mRestorePackages[this.mRestorePackage].packageName);
            ArrayList<DecodedFilename> blobs = contentsByKey(packageDir);
            if (blobs == null) {
                Log.e(TAG, "No keys for package: " + packageDir);
                return -1000;
            }
            BackupDataOutput out = new BackupDataOutput(outFd.getFileDescriptor());
            try {
                Iterator<DecodedFilename> it = blobs.iterator();
                while (it.hasNext()) {
                    DecodedFilename keyEntry = it.next();
                    File f = keyEntry.file;
                    in = new FileInputStream(f);
                    int size = (int) f.length();
                    byte[] buf = new byte[size];
                    in.read(buf);
                    out.writeEntityHeader(keyEntry.key, size);
                    out.writeEntityData(buf, size);
                    in.close();
                }
                return 0;
            } catch (IOException e) {
                Log.e(TAG, "Unable to read backup records", e);
                return -1000;
            } catch (Throwable th) {
                in.close();
                throw th;
            }
        } else {
            throw new IllegalStateException("getRestoreData(fd) for non-key/value dataset");
        }
    }

    private ArrayList<DecodedFilename> contentsByKey(File dir) {
        File[] allFiles = dir.listFiles();
        if (allFiles == null || allFiles.length == 0) {
            return null;
        }
        ArrayList<DecodedFilename> contents = new ArrayList<>();
        for (File f : allFiles) {
            contents.add(new DecodedFilename(f));
        }
        Collections.sort(contents);
        return contents;
    }

    public void finishRestore() {
        if (this.mRestoreType == 2) {
            resetFullRestoreState();
        }
        this.mRestoreType = 0;
    }

    private void resetFullRestoreState() {
        IoUtils.closeQuietly(this.mCurFullRestoreStream);
        this.mCurFullRestoreStream = null;
        this.mFullRestoreSocketStream = null;
        this.mFullRestoreBuffer = null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0073, code lost:
        return -1000;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    public int getNextFullRestoreDataChunk(ParcelFileDescriptor socket) {
        if (this.mRestoreType == 2) {
            if (this.mCurFullRestoreStream == null) {
                String name = this.mRestorePackages[this.mRestorePackage].packageName;
                try {
                    this.mCurFullRestoreStream = new FileInputStream(new File(this.mRestoreSetFullDir, name));
                    this.mFullRestoreSocketStream = new FileOutputStream(socket.getFileDescriptor());
                    this.mFullRestoreBuffer = new byte[2048];
                } catch (IOException e) {
                    Log.e(TAG, "Unable to read archive for " + name);
                    return -1002;
                }
            }
            int nRead = this.mCurFullRestoreStream.read(this.mFullRestoreBuffer);
            if (nRead < 0) {
                nRead = -1;
            } else if (nRead == 0) {
                Log.w(TAG, "read() of archive file returned 0; treating as EOF");
                nRead = -1;
            } else {
                this.mFullRestoreSocketStream.write(this.mFullRestoreBuffer, 0, nRead);
            }
            return nRead;
        }
        throw new IllegalStateException("Asked for full restore data for non-stream package");
    }

    public int abortFullRestore() {
        if (this.mRestoreType == 2) {
            resetFullRestoreState();
            this.mRestoreType = 0;
            return 0;
        }
        throw new IllegalStateException("abortFullRestore() but not currently restoring");
    }

    public long getBackupQuota(String packageName, boolean isFullBackup) {
        return isFullBackup ? FULL_BACKUP_SIZE_QUOTA : KEY_VALUE_BACKUP_SIZE_QUOTA;
    }
}
