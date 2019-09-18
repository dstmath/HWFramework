package android.support.v4.content;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.OperationCanceledException;
import android.support.v4.os.CancellationSignal;

public final class ContentResolverCompat {
    private ContentResolverCompat() {
    }

    public static Cursor query(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        Object obj;
        if (Build.VERSION.SDK_INT >= 16) {
            if (cancellationSignal != null) {
                try {
                    obj = cancellationSignal.getCancellationSignalObject();
                } catch (Exception e) {
                    if (e instanceof OperationCanceledException) {
                        throw new android.support.v4.os.OperationCanceledException();
                    }
                    throw e;
                }
            } else {
                obj = null;
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder, (android.os.CancellationSignal) obj);
        }
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
    }
}
