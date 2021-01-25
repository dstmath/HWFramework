package com.android.server.backup;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DataChangedJournal {
    private static final int BUFFER_SIZE_BYTES = 8192;
    private static final String FILE_NAME_PREFIX = "journal";
    private final File mFile;

    DataChangedJournal(File file) {
        this.mFile = file;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001b, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002d, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002e, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0031, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0034, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0035, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        throw r2;
     */
    public void forEach(Consumer<String> consumer) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(this.mFile), 8192);
        DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
        while (dataInputStream.available() > 0) {
            consumer.accept(dataInputStream.readUTF());
        }
        $closeResource(null, dataInputStream);
        $closeResource(null, bufferedInputStream);
    }

    public List<String> getPackages() throws IOException {
        List<String> packages = new ArrayList<>();
        Objects.requireNonNull(packages);
        forEach(new Consumer(packages) {
            /* class com.android.server.backup.$$Lambda$NsJlXDEZZRYyD6JsbnCsdcb4L4A */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.add((String) obj);
            }
        });
        return packages;
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
