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

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0021, code lost:
        r9 = r3;
        r3 = r2;
        r2 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001b, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
        r3 = null;
     */
    @Nullable
    @RequiresApi(19)
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

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0037, code lost:
        r4 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0038, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003c, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003d, code lost:
        r11 = r5;
        r5 = r4;
        r4 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x004f, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0050, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0054, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0055, code lost:
        r11 = r4;
        r4 = r3;
        r3 = r11;
     */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x004f A[ExcHandler: all (th java.lang.Throwable)] */
    @Nullable
    @RequiresApi(19)
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

    @Nullable
    @RequiresApi(19)
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
                int read = is.read(buffer);
                int readLen = read;
                if (read == -1) {
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
