package android.content;

import android.net.Uri;

public class ContentUris {
    public static long parseId(Uri contentUri) {
        String last = contentUri.getLastPathSegment();
        if (last == null) {
            return -1;
        }
        return Long.parseLong(last);
    }

    public static Uri.Builder appendId(Uri.Builder builder, long id) {
        return builder.appendEncodedPath(String.valueOf(id));
    }

    public static Uri withAppendedId(Uri contentUri, long id) {
        return appendId(contentUri.buildUpon(), id).build();
    }
}
