package com.android.server.backup;

import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;

final class ProcessedPackagesJournal {
    private static final boolean DEBUG = true;
    private static final String JOURNAL_FILE_NAME = "processed";
    private static final String TAG = "ProcessedPackagesJournal";
    @GuardedBy("mProcessedPackages")
    private final Set<String> mProcessedPackages = new HashSet();
    private final File mStateDirectory;

    ProcessedPackagesJournal(File stateDirectory) {
        this.mStateDirectory = stateDirectory;
    }

    /* access modifiers changed from: package-private */
    public void init() {
        synchronized (this.mProcessedPackages) {
            loadFromDisk();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasBeenProcessed(String packageName) {
        boolean contains;
        synchronized (this.mProcessedPackages) {
            contains = this.mProcessedPackages.contains(packageName);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public void addPackage(String packageName) {
        RandomAccessFile out;
        synchronized (this.mProcessedPackages) {
            if (this.mProcessedPackages.add(packageName)) {
                File journalFile = new File(this.mStateDirectory, JOURNAL_FILE_NAME);
                try {
                    out = new RandomAccessFile(journalFile, "rws");
                    out.seek(out.length());
                    out.writeUTF(packageName);
                    $closeResource(null, out);
                } catch (IOException e) {
                    Slog.e(TAG, "Can't log backup of " + packageName + " to " + journalFile);
                } catch (Throwable th) {
                    $closeResource(r3, out);
                    throw th;
                }
            }
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

    /* access modifiers changed from: package-private */
    public Set<String> getPackagesCopy() {
        HashSet hashSet;
        synchronized (this.mProcessedPackages) {
            hashSet = new HashSet(this.mProcessedPackages);
        }
        return hashSet;
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        synchronized (this.mProcessedPackages) {
            this.mProcessedPackages.clear();
            new File(this.mStateDirectory, JOURNAL_FILE_NAME).delete();
        }
    }

    private void loadFromDisk() {
        DataInputStream oldJournal;
        File journalFile = new File(this.mStateDirectory, JOURNAL_FILE_NAME);
        if (journalFile.exists()) {
            try {
                oldJournal = new DataInputStream(new BufferedInputStream(new FileInputStream(journalFile)));
                while (true) {
                    String packageName = oldJournal.readUTF();
                    Slog.v(TAG, "   + " + packageName);
                    this.mProcessedPackages.add(packageName);
                }
            } catch (EOFException e) {
            } catch (IOException e2) {
                Slog.e(TAG, "Error reading processed packages journal", e2);
            } catch (Throwable th) {
                $closeResource(r2, oldJournal);
                throw th;
            }
        }
    }
}
