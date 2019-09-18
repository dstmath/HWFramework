package com.android.server.backup;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class DataChangedJournal {
    private static final int BUFFER_SIZE_BYTES = 8192;
    private static final String FILE_NAME_PREFIX = "journal";
    private final File mFile;

    @FunctionalInterface
    public interface Consumer {
        void accept(String str);
    }

    DataChangedJournal(File file) {
        this.mFile = file;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0020, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0019, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001d, code lost:
        $closeResource(r1, r0);
     */
    public void addPackage(String packageName) throws IOException {
        RandomAccessFile out = new RandomAccessFile(this.mFile, "rws");
        out.seek(out.length());
        out.writeUTF(packageName);
        $closeResource(null, out);
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002b, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002c, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0031, code lost:
        r5 = r4;
        r4 = r3;
        r3 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0038, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003c, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003f, code lost:
        throw r2;
     */
    public void forEach(Consumer consumer) throws IOException {
        Throwable th;
        Throwable th2;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(this.mFile), 8192);
        DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
        while (dataInputStream.available() > 0) {
            consumer.accept(dataInputStream.readUTF());
        }
        $closeResource(null, dataInputStream);
        $closeResource(null, bufferedInputStream);
        return;
        $closeResource(th, dataInputStream);
        throw th2;
    }

    public boolean delete() {
        return this.mFile.delete();
    }

    public boolean equals(Object object) {
        if (!(object instanceof DataChangedJournal)) {
            return false;
        }
        try {
            return this.mFile.getCanonicalPath().equals(((DataChangedJournal) object).mFile.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    public String toString() {
        return this.mFile.toString();
    }

    static DataChangedJournal newJournal(File journalDirectory) throws IOException {
        return new DataChangedJournal(File.createTempFile(FILE_NAME_PREFIX, null, journalDirectory));
    }

    static ArrayList<DataChangedJournal> listJournals(File journalDirectory) {
        ArrayList<DataChangedJournal> journals = new ArrayList<>();
        for (File file : journalDirectory.listFiles()) {
            journals.add(new DataChangedJournal(file));
        }
        return journals;
    }
}
