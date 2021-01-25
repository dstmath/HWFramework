package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class TypefaceCompatUtil {
    private static final String CACHE_FILE_PREFIX = ".font";
    private static final String TAG = "TypefaceCompatUtil";

    private TypefaceCompatUtil() {
    }

    @Nullable
    public static File getTempFile(Context context) {
        String prefix = CACHE_FILE_PREFIX + Process.myPid() + "-" + Process.myTid() + "-";
        for (int i = 0; i < 100; i++) {
            File file = new File(context.getCacheDir(), prefix + i);
            try {
                if (file.createNewFile()) {
                    return file;
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0020, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0021, code lost:
        r3 = r2;
        r2 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001b, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001c, code lost:
        r3 = null;
     */
    @RequiresApi(19)
    @Nullable
    private static ByteBuffer mmap(File file) {
        Throwable th;
        Throwable th2;
        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel channel = fis.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            fis.close();
            return map;
            throw th2;
            if (th != null) {
                try {
                    fis.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
            } else {
                fis.close();
            }
            throw th2;
        } catch (IOException e) {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0037, code lost:
        r4 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003c, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
        r5 = r4;
        r4 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004f, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0050, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0054, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0055, code lost:
        r4 = r3;
        r3 = r4;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x004f A[ExcHandler: all (th java.lang.Throwable)] */
    @RequiresApi(19)
    @Nullable
    public static ByteBuffer mmap(Context context, CancellationSignal cancellationSignal, Uri uri) {
        Throwable th;
        Throwable th2;
        FileInputStream fis;
        Throwable th3;
        Throwable th4;
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r", cancellationSignal);
            if (pfd == null) {
                if (pfd != null) {
                    pfd.close();
                }
                return null;
            }
            fis = new FileInputStream(pfd.getFileDescriptor());
            FileChannel channel = fis.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            fis.close();
            if (pfd != null) {
                pfd.close();
            }
            return map;
            if (th3 != null) {
                try {
                    fis.close();
                } catch (Throwable th5) {
                }
            } else {
                fis.close();
            }
            throw th4;
            throw th2;
            if (pfd != null) {
                if (th != null) {
                    try {
                        pfd.close();
                    } catch (Throwable th6) {
                        th.addSuppressed(th6);
                    }
                } else {
                    pfd.close();
                }
            }
            throw th2;
            throw th4;
        } catch (IOException e) {
            return null;
        }
    }

    @RequiresApi(19)
    @Nullable
    public static ByteBuffer copyToDirectBuffer(Context context, Resources res, int id) {
        File tmpFile = getTempFile(context);
        ByteBuffer byteBuffer = null;
        if (tmpFile == null) {
            return null;
        }
        try {
            if (copyToFile(tmpFile, res, id)) {
                byteBuffer = mmap(tmpFile);
            }
            return byteBuffer;
        } finally {
            tmpFile.delete();
        }
    }

    public static boolean copyToFile(File file, InputStream is) {
        FileOutputStream os = null;
        boolean z = false;
        try {
            os = new FileOutputStream(file, false);
            byte[] buffer = new byte[1024];
            while (true) {
                int readLen = is.read(buffer);
                if (readLen == -1) {
                    break;
                }
                os.write(buffer, 0, readLen);
            }
            z = true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying resource contents to temp file: " + e.getMessage());
        } catch (Throwable th) {
            closeQuietly(null);
            throw th;
        }
        closeQuietly(os);
        return z;
    }

    public static boolean copyToFile(File file, Resources res, int id) {
        InputStream is = null;
        try {
            is = res.openRawResource(id);
            return copyToFile(file, is);
        } finally {
            closeQuietly(is);
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
