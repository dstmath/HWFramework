package android.content;

import android.net.Uri;
import android.net.Uri.Builder;

public class ContentUris {
    public static long parseId(Uri contentUri) {
        String last = contentUri.getLastPathSegment();
        return last == null ? -1 : Long.parseLong(last);
    }

    public static Builder appendId(Builder builder, long id) {
        return builder.appendEncodedPath(String.valueOf(id));
    }

    public static Uri withAppendedId(Uri contentUri, long id) {
        return appendId(contentUri.buildUpon(), id).build();
    }
}
