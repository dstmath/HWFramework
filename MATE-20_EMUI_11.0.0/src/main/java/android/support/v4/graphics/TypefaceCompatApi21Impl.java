package android.support.v4.graphics;

import android.content.Context;
import android.graphics.Typeface;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.v4.provider.FontsContractCompat;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RequiresApi(21)
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
class TypefaceCompatApi21Impl extends TypefaceCompatBaseImpl {
    private static final String TAG = "TypefaceCompatApi21Impl";

    TypefaceCompatApi21Impl() {
    }

    private File getFile(ParcelFileDescriptor fd) {
        try {
            String path = Os.readlink("/proc/self/fd/" + fd.getFd());
            if (OsConstants.S_ISREG(Os.stat(path).st_mode)) {
                return new File(path);
            }
            return null;
        } catch (ErrnoException e) {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0047, code lost:
        r6 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0048, code lost:
        r7 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004c, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004d, code lost:
        r7 = r6;
        r6 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x005f, code lost:
        r4 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0060, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0064, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0065, code lost:
        r5 = r4;
        r4 = r5;
     */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x005f A[ExcHandler: all (th java.lang.Throwable)] */
    @Override // android.support.v4.graphics.TypefaceCompatBaseImpl
    public Typeface createFromFontInfo(Context context, CancellationSignal cancellationSignal, @NonNull FontsContractCompat.FontInfo[] fonts, int style) {
        ParcelFileDescriptor pfd;
        Throwable th;
        Throwable th2;
        FileInputStream fis;
        Throwable th3;
        Throwable th4;
        if (fonts.length < 1) {
            return null;
        }
        try {
            pfd = context.getContentResolver().openFileDescriptor(findBestInfo(fonts, style).getUri(), "r", cancellationSignal);
            File file = getFile(pfd);
            if (file != null) {
                if (file.canRead()) {
                    Typeface createFromFile = Typeface.createFromFile(file);
                    if (pfd != null) {
                        pfd.close();
                    }
                    return createFromFile;
                }
            }
            fis = new FileInputStream(pfd.getFileDescriptor());
            Typeface createFromInputStream = super.createFromInputStream(context, fis);
            fis.close();
            if (pfd != null) {
                pfd.close();
            }
            return createFromInputStream;
        } catch (IOException e) {
            return null;
        }
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
    }
}
