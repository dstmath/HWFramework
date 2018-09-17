package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.os.Process;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

@RestrictTo({Scope.LIBRARY_GROUP})
class TypefaceCompatUtil {
    private static final String CACHE_FILE_PREFIX = ".font";
    private static final String TAG = "TypefaceCompatUtil";

    private static class ByteBufferInputStream extends InputStream {
        private ByteBuffer mBuf;

        ByteBufferInputStream(ByteBuffer buf) {
            this.mBuf = buf;
        }

        public int read() {
            if (this.mBuf.hasRemaining()) {
                return this.mBuf.get() & 255;
            }
            return -1;
        }

        public int read(byte[] bytes, int off, int len) {
            if (!this.mBuf.hasRemaining()) {
                return -1;
            }
            len = Math.min(len, this.mBuf.remaining());
            this.mBuf.get(bytes, off, len);
            return len;
        }
    }

    private TypefaceCompatUtil() {
    }

    public static File getTempFile(Context context) {
        String prefix = CACHE_FILE_PREFIX + Process.myPid() + "-" + Process.myTid() + "-";
        int i = 0;
        while (i < 100) {
            File file = new File(context.getCacheDir(), prefix + i);
            try {
                if (file.createNewFile()) {
                    return file;
                }
                i++;
            } catch (IOException e) {
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x002e A:{SYNTHETIC, Splitter: B:23:0x002e} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0041 A:{Catch:{ IOException -> 0x0034 }} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0033 A:{SYNTHETIC, Splitter: B:26:0x0033} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @RequiresApi(19)
    private static ByteBuffer mmap(File file) {
        Throwable th;
        FileInputStream fis = null;
        Throwable th2;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            try {
                FileChannel channel = fis2.getChannel();
                ByteBuffer map = channel.map(MapMode.READ_ONLY, 0, channel.size());
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                th2 = null;
                if (th2 == null) {
                    return map;
                }
                try {
                    throw th2;
                } catch (IOException e) {
                    fis = fis2;
                }
            } catch (Throwable th4) {
                th2 = th4;
                fis = fis2;
                th = null;
                if (fis != null) {
                }
                if (th == null) {
                }
            }
        } catch (Throwable th5) {
            th2 = th5;
            th = null;
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable th6) {
                    if (th == null) {
                        th = th6;
                    } else if (th != th6) {
                        th.addSuppressed(th6);
                    }
                }
            }
            if (th == null) {
                try {
                    throw th;
                } catch (IOException e2) {
                    return null;
                }
            }
            throw th2;
        }
    }

    @RequiresApi(19)
    public static ByteBuffer copyToDirectBuffer(Context context, Resources res, int id) {
        File tmpFile = getTempFile(context);
        if (tmpFile == null) {
            return null;
        }
        try {
            if (!copyToFile(tmpFile, res, id)) {
                return null;
            }
            ByteBuffer mmap = mmap(tmpFile);
            tmpFile.delete();
            return mmap;
        } finally {
            tmpFile.delete();
        }
    }

    public static boolean copyToFile(File file, ByteBuffer buffer) {
        return copyToFile(file, new ByteBufferInputStream(buffer));
    }

    public static boolean copyToFile(File file, InputStream is) {
        IOException e;
        Throwable th;
        Closeable os = null;
        try {
            FileOutputStream os2 = new FileOutputStream(file, false);
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int readLen = is.read(buffer);
                    if (readLen != -1) {
                        os2.write(buffer, 0, readLen);
                    } else {
                        closeQuietly(os2);
                        return true;
                    }
                }
            } catch (IOException e2) {
                e = e2;
                os = os2;
                try {
                    Log.e(TAG, "Error copying resource contents to temp file: " + e.getMessage());
                    closeQuietly(os);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    closeQuietly(os);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Object os3 = os2;
                closeQuietly(os);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            Log.e(TAG, "Error copying resource contents to temp file: " + e.getMessage());
            closeQuietly(os);
            return false;
        }
    }

    public static boolean copyToFile(File file, Resources res, int id) {
        Closeable closeable = null;
        try {
            closeable = res.openRawResource(id);
            boolean copyToFile = copyToFile(file, (InputStream) closeable);
            return copyToFile;
        } finally {
            closeQuietly(closeable);
        }
    }

    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }
}
