package com.android.server;

import android.util.Slog;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

class RandomBlock {
    private static final int BLOCK_SIZE = 512;
    private static final boolean DEBUG = false;
    private static final String TAG = "RandomBlock";
    private byte[] block;

    private RandomBlock() {
        this.block = new byte[BLOCK_SIZE];
    }

    static RandomBlock fromFile(String filename) throws IOException {
        Throwable th;
        InputStream stream = null;
        try {
            InputStream stream2 = new FileInputStream(filename);
            try {
                RandomBlock fromStream = fromStream(stream2);
                close(stream2);
                return fromStream;
            } catch (Throwable th2) {
                th = th2;
                stream = stream2;
                close(stream);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            close(stream);
            throw th;
        }
    }

    private static RandomBlock fromStream(InputStream in) throws IOException {
        RandomBlock retval = new RandomBlock();
        int total = 0;
        while (total < BLOCK_SIZE) {
            int result = in.read(retval.block, total, 512 - total);
            if (result == -1) {
                throw new EOFException();
            }
            total += result;
        }
        return retval;
    }

    void toFile(String filename, boolean sync) throws IOException {
        Throwable th;
        RandomAccessFile out = null;
        try {
            RandomAccessFile out2 = new RandomAccessFile(filename, sync ? "rws" : "rw");
            try {
                toDataOut(out2);
                truncateIfPossible(out2);
                close(out2);
            } catch (Throwable th2) {
                th = th2;
                out = out2;
                close(out);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            close(out);
            throw th;
        }
    }

    private static void truncateIfPossible(RandomAccessFile f) {
        try {
            f.setLength(512);
        } catch (IOException e) {
        }
    }

    private void toDataOut(DataOutput out) throws IOException {
        out.write(this.block);
    }

    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Slog.w(TAG, "IOException thrown while closing Closeable", e);
            }
        }
    }
}
