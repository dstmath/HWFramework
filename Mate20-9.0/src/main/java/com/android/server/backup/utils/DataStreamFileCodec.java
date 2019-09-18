package com.android.server.backup.utils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class DataStreamFileCodec<T> {
    private final DataStreamCodec<T> mCodec;
    private final File mFile;

    public DataStreamFileCodec(File file, DataStreamCodec<T> codec) {
        this.mFile = file;
        this.mCodec = codec;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0021, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0022, code lost:
        r5 = r4;
        r4 = r3;
        r3 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0029, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002d, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0030, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
        r3 = th;
     */
    public T deserialize() throws IOException {
        Throwable th;
        Throwable th2;
        FileInputStream fileInputStream = new FileInputStream(this.mFile);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        T deserialize = this.mCodec.deserialize(dataInputStream);
        $closeResource(null, dataInputStream);
        $closeResource(null, fileInputStream);
        return deserialize;
        $closeResource(th, dataInputStream);
        throw th2;
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

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0027, code lost:
        r4 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002c, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002d, code lost:
        r6 = r5;
        r5 = r4;
        r4 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0034, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0035, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0039, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x003a, code lost:
        r6 = r4;
        r4 = r3;
        r3 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0041, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0045, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0048, code lost:
        throw r2;
     */
    public void serialize(T t) throws IOException {
        Throwable th;
        Throwable th2;
        Throwable th3;
        Throwable th4;
        FileOutputStream fileOutputStream = new FileOutputStream(this.mFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
        this.mCodec.serialize(t, dataOutputStream);
        dataOutputStream.flush();
        $closeResource(null, dataOutputStream);
        $closeResource(null, bufferedOutputStream);
        $closeResource(null, fileOutputStream);
        return;
        $closeResource(th3, dataOutputStream);
        throw th4;
        $closeResource(th, bufferedOutputStream);
        throw th2;
    }
}
