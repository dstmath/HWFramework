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
import android.rms.iaware.DataContract.Apps.LaunchMode;
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
import libcore.io.IoUtils;

public class LocalTransport extends BackupTransport {
    private static final long CURRENT_SET_TOKEN = 1;
    private static final boolean DEBUG = false;
    private static final long FULL_BACKUP_SIZE_QUOTA = 26214400;
    private static final String FULL_DATA_DIR = "_full";
    private static final String INCREMENTAL_DIR = "_delta";
    private static final long KEY_VALUE_BACKUP_SIZE_QUOTA = 5242880;
    static final long[] POSSIBLE_SETS = new long[]{2, 3, 4, 5, 6, 7, 8, 9};
    private static final String TAG = "LocalTransport";
    private static final String TRANSPORT_DATA_MANAGEMENT_LABEL = "";
    private static final String TRANSPORT_DESTINATION_STRING = "Backing up to debug-only private cache";
    private static final String TRANSPORT_DIR_NAME = "com.android.internal.backup.LocalTransport";
    private Context mContext;
    private FileInputStream mCurFullRestoreStream;
    private File mCurrentSetDir = new File(this.mDataDir, Long.toString(1));
    private File mCurrentSetFullDir = new File(this.mCurrentSetDir, FULL_DATA_DIR);
    private File mCurrentSetIncrementalDir = new File(this.mCurrentSetDir, INCREMENTAL_DIR);
    private File mDataDir = new File(Environment.getDownloadCacheDirectory(), LaunchMode.BACKUP);
    private byte[] mFullBackupBuffer;
    private BufferedOutputStream mFullBackupOutputStream;
    private long mFullBackupSize;
    private byte[] mFullRestoreBuffer;
    private FileOutputStream mFullRestoreSocketStream;
    private String mFullTargetPackage;
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

    private void makeDataDirs() {
        this.mCurrentSetDir.mkdirs();
        this.mCurrentSetFullDir.mkdir();
        this.mCurrentSetIncrementalDir.mkdir();
    }

    public LocalTransport(Context context) {
        this.mContext = context;
        makeDataDirs();
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
        return "";
    }

    public String transportDirName() {
        return TRANSPORT_DIR_NAME;
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
        File packageDir = new File(this.mCurrentSetIncrementalDir, packageInfo.packageName);
        packageDir.mkdirs();
        BackupDataInput changeSet = new BackupDataInput(data.getFileDescriptor());
        int bufSize = 512;
        try {
            byte[] buf = new byte[512];
            while (changeSet.readNextHeader()) {
                File entityFile = new File(packageDir, new String(Base64.encode(changeSet.getKey().getBytes())));
                int dataSize = changeSet.getDataSize();
                if (dataSize >= 0) {
                    if (entityFile.exists()) {
                        entityFile.delete();
                    }
                    FileOutputStream entity = new FileOutputStream(entityFile);
                    if (dataSize > bufSize) {
                        bufSize = dataSize;
                        buf = new byte[dataSize];
                    }
                    changeSet.readEntityData(buf, 0, dataSize);
                    try {
                        entity.write(buf, 0, dataSize);
                        entity.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Unable to update key file " + entityFile.getAbsolutePath());
                        entity.close();
                        return -1000;
                    } catch (Throwable th) {
                        entity.close();
                        throw th;
                    }
                }
                entityFile.delete();
            }
            return 0;
        } catch (IOException e2) {
            Log.v(TAG, "Exception reading backup input:", e2);
            return -1000;
        }
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
        packageDir = new File(this.mCurrentSetFullDir, packageInfo.packageName);
        File[] tarballs = packageDir.listFiles();
        if (tarballs != null) {
            for (File f2 : tarballs) {
                f2.delete();
            }
            packageDir.delete();
        }
        return 0;
    }

    public int finishBackup() {
        return tearDownFullBackup();
    }

    private int tearDownFullBackup() {
        if (this.mSocket != null) {
            int i;
            try {
                if (this.mFullBackupOutputStream != null) {
                    this.mFullBackupOutputStream.flush();
                    this.mFullBackupOutputStream.close();
                }
                this.mSocketInputStream = null;
                this.mFullTargetPackage = null;
                i = this.mSocket;
                i.close();
            } catch (IOException e) {
                i = -1000;
                return i;
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
        int num;
        long[] existing = new long[(POSSIBLE_SETS.length + 1)];
        long[] jArr = POSSIBLE_SETS;
        int i = 0;
        int length = jArr.length;
        int num2 = 0;
        while (i < length) {
            long token = jArr[i];
            if (new File(this.mDataDir, Long.toString(token)).exists()) {
                num = num2 + 1;
                existing[num2] = token;
            } else {
                num = num2;
            }
            i++;
            num2 = num;
        }
        num = num2 + 1;
        existing[num2] = 1;
        RestoreSet[] available = new RestoreSet[num];
        for (int i2 = 0; i2 < available.length; i2++) {
            available[i2] = new RestoreSet("Local disk image", "flash", existing[i2]);
        }
        return available;
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
        if (this.mRestorePackages == null) {
            throw new IllegalStateException("startRestore not called");
        }
        String name;
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

    public int getRestoreData(ParcelFileDescriptor outFd) {
        if (this.mRestorePackages == null) {
            throw new IllegalStateException("startRestore not called");
        } else if (this.mRestorePackage < 0) {
            throw new IllegalStateException("nextRestorePackage not called");
        } else if (this.mRestoreType != 1) {
            throw new IllegalStateException("getRestoreData(fd) for non-key/value dataset");
        } else {
            File packageDir = new File(this.mRestoreSetIncrementalDir, this.mRestorePackages[this.mRestorePackage].packageName);
            ArrayList<DecodedFilename> blobs = contentsByKey(packageDir);
            if (blobs == null) {
                Log.e(TAG, "No keys for package: " + packageDir);
                return -1000;
            }
            BackupDataOutput out = new BackupDataOutput(outFd.getFileDescriptor());
            FileInputStream in;
            try {
                for (DecodedFilename keyEntry : blobs) {
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
            }
        }
    }

    private ArrayList<DecodedFilename> contentsByKey(File dir) {
        File[] allFiles = dir.listFiles();
        if (allFiles == null || allFiles.length == 0) {
            return null;
        }
        ArrayList<DecodedFilename> contents = new ArrayList();
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

    public int getNextFullRestoreDataChunk(ParcelFileDescriptor socket) {
        if (this.mRestoreType != 2) {
            throw new IllegalStateException("Asked for full restore data for non-stream package");
        }
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
        try {
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
        } catch (IOException e2) {
            return -1000;
        }
    }

    public int abortFullRestore() {
        if (this.mRestoreType != 2) {
            throw new IllegalStateException("abortFullRestore() but not currently restoring");
        }
        resetFullRestoreState();
        this.mRestoreType = 0;
        return 0;
    }

    public long getBackupQuota(String packageName, boolean isFullBackup) {
        return isFullBackup ? FULL_BACKUP_SIZE_QUOTA : KEY_VALUE_BACKUP_SIZE_QUOTA;
    }
}
