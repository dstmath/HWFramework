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

/* access modifiers changed from: package-private */
public final class ProcessedPackagesJournal {
    private static final boolean DEBUG = true;
    private static final String JOURNAL_FILE_NAME = "processed";
    private static final String TAG = "ProcessedPackagesJournal";
    @GuardedBy({"mProcessedPackages"})
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
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0034, code lost:
        throw r4;
     */
    public void addPackage(String packageName) {
        synchronized (this.mProcessedPackages) {
            if (this.mProcessedPackages.add(packageName)) {
                File journalFile = new File(this.mStateDirectory, JOURNAL_FILE_NAME);
                try {
                    RandomAccessFile out = new RandomAccessFile(journalFile, "rws");
                    out.seek(out.length());
                    out.writeUTF(packageName);
                    $closeResource(null, out);
                } catch (IOException e) {
                    Slog.e(TAG, "Can't log backup of " + packageName + " to " + journalFile);
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0043, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0044, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0047, code lost:
        throw r4;
     */
    private void loadFromDisk() {
        File journalFile = new File(this.mStateDirectory, JOURNAL_FILE_NAME);
        if (journalFile.exists()) {
            try {
                DataInputStream oldJournal = new DataInputStream(new BufferedInputStream(new FileInputStream(journalFile)));
                while (true) {
                    String packageName = oldJournal.readUTF();
                    Slog.v(TAG, "   + " + packageName);
                    this.mProcessedPackages.add(packageName);
                }
            } catch (EOFException e) {
            } catch (IOException e2) {
                Slog.e(TAG, "Error reading processed packages journal", e2);
            }
        }
    }
}
