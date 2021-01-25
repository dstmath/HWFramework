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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0025, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0026, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0029, code lost:
        throw r2;
     */
    public T deserialize() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(this.mFile);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        T deserialize = this.mCodec.deserialize(dataInputStream);
        $closeResource(null, dataInputStream);
        $closeResource(null, fileInputStream);
        return deserialize;
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002a, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002d, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0031, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0034, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0037, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0038, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003b, code lost:
        throw r2;
     */
    public void serialize(T t) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(this.mFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
        this.mCodec.serialize(t, dataOutputStream);
        dataOutputStream.flush();
        $closeResource(null, dataOutputStream);
        $closeResource(null, bufferedOutputStream);
        $closeResource(null, fileOutputStream);
    }
}
