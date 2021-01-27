package ohos.sysappcomponents.contact.chain;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.ContactsContract;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import ohos.app.Context;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.LogUtil;

public class PortraitInsertor {
    private static final int BUFFER_SIZE = 102400;
    private static final String TAG = PortraitInsertor.class.getSimpleName();

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0059, code lost:
        if (r5 != null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005f, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0063, code lost:
        throw r4;
     */
    public static void savePortrait(long j, Uri uri, Context context) {
        if (Attribute.getAplatFromContext(context) == null || Attribute.getAplatFromContext(context).getContentResolver() == null) {
            String str = TAG;
            LogUtil.error(str, "saveUpdatedPhoto null, Attribute.getAplatFromContext:" + Attribute.getAplatFromContext(context));
            return;
        }
        FileOutputStream fileOutputStream = null;
        try {
            InputStream openInputStream = Attribute.getAplatFromContext(context).getContentResolver().openInputStream(uri);
            fileOutputStream = Attribute.getAplatFromContext(context).getContentResolver().openAssetFileDescriptor(Uri.withAppendedPath(ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, j), "display_photo"), "rw").createOutputStream();
            byte[] bArr = new byte[BUFFER_SIZE];
            while (true) {
                int read = openInputStream.read(bArr);
                if (read <= 0) {
                    break;
                }
                fileOutputStream.write(bArr, 0, read);
            }
            fileOutputStream.flush();
            openInputStream.close();
        } catch (IOException unused) {
            LogUtil.error(TAG, "saveUpdatedPhoto error");
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fileOutputStream.close();
                } catch (IOException unused2) {
                    LogUtil.error(TAG, "outputStream close error");
                }
            }
            throw th;
        }
        try {
            fileOutputStream.close();
        } catch (IOException unused3) {
            LogUtil.error(TAG, "outputStream close error");
        }
    }
}
