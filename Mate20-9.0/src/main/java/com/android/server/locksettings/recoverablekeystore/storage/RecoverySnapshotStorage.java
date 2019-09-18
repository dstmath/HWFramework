package com.android.server.locksettings.recoverablekeystore.storage;

import android.os.Environment;
import android.security.keystore.recovery.KeyChainSnapshot;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.locksettings.recoverablekeystore.serialization.KeyChainSnapshotDeserializer;
import com.android.server.locksettings.recoverablekeystore.serialization.KeyChainSnapshotParserException;
import com.android.server.locksettings.recoverablekeystore.serialization.KeyChainSnapshotSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.Locale;

public class RecoverySnapshotStorage {
    private static final String ROOT_PATH = "system";
    private static final String STORAGE_PATH = "recoverablekeystore/snapshots/";
    private static final String TAG = "RecoverySnapshotStorage";
    @GuardedBy("this")
    private final SparseArray<KeyChainSnapshot> mSnapshotByUid = new SparseArray<>();
    private final File rootDirectory;

    public static RecoverySnapshotStorage newInstance() {
        return new RecoverySnapshotStorage(new File(Environment.getDataDirectory(), ROOT_PATH));
    }

    @VisibleForTesting
    public RecoverySnapshotStorage(File rootDirectory2) {
        this.rootDirectory = rootDirectory2;
    }

    public synchronized void put(int uid, KeyChainSnapshot snapshot) {
        this.mSnapshotByUid.put(uid, snapshot);
        try {
            writeToDisk(uid, snapshot);
        } catch (IOException | CertificateEncodingException e) {
            Log.e(TAG, String.format(Locale.US, "Error persisting snapshot for %d to disk", new Object[]{Integer.valueOf(uid)}), e);
        }
        return;
    }

    public synchronized KeyChainSnapshot get(int uid) {
        KeyChainSnapshot snapshot = this.mSnapshotByUid.get(uid);
        if (snapshot != null) {
            return snapshot;
        }
        try {
            return readFromDisk(uid);
        } catch (KeyChainSnapshotParserException | IOException e) {
            Log.e(TAG, String.format(Locale.US, "Error reading snapshot for %d from disk", new Object[]{Integer.valueOf(uid)}), e);
            return null;
        }
    }

    public synchronized void remove(int uid) {
        this.mSnapshotByUid.remove(uid);
        getSnapshotFile(uid).delete();
    }

    private void writeToDisk(int uid, KeyChainSnapshot snapshot) throws IOException, CertificateEncodingException {
        FileOutputStream fileOutputStream;
        File snapshotFile = getSnapshotFile(uid);
        try {
            fileOutputStream = new FileOutputStream(snapshotFile);
            KeyChainSnapshotSerializer.serialize(snapshot, fileOutputStream);
            $closeResource(null, fileOutputStream);
        } catch (IOException | CertificateEncodingException e) {
            snapshotFile.delete();
            throw e;
        } catch (Throwable th) {
            $closeResource(r2, fileOutputStream);
            throw th;
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

    private KeyChainSnapshot readFromDisk(int uid) throws IOException, KeyChainSnapshotParserException {
        FileInputStream fileInputStream;
        File snapshotFile = getSnapshotFile(uid);
        try {
            fileInputStream = new FileInputStream(snapshotFile);
            KeyChainSnapshot deserialize = KeyChainSnapshotDeserializer.deserialize(fileInputStream);
            $closeResource(null, fileInputStream);
            return deserialize;
        } catch (KeyChainSnapshotParserException | IOException e) {
            snapshotFile.delete();
            throw e;
        } catch (Throwable th) {
            $closeResource(r2, fileInputStream);
            throw th;
        }
    }

    private File getSnapshotFile(int uid) {
        return new File(getStorageFolder(), getSnapshotFileName(uid));
    }

    private String getSnapshotFileName(int uid) {
        return String.format(Locale.US, "%d.xml", new Object[]{Integer.valueOf(uid)});
    }

    private File getStorageFolder() {
        File folder = new File(this.rootDirectory, STORAGE_PATH);
        folder.mkdirs();
        return folder;
    }
}
