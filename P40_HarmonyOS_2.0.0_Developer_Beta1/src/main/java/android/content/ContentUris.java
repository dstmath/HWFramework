package android.content;

import android.net.Uri;
import java.util.List;

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

    public static Uri removeId(Uri contentUri) {
        String last = contentUri.getLastPathSegment();
        if (last != null) {
            Long.parseLong(last);
            List<String> segments = contentUri.getPathSegments();
            Uri.Builder builder = contentUri.buildUpon();
            builder.path((String) null);
            for (int i = 0; i < segments.size() - 1; i++) {
                builder.appendPath(segments.get(i));
            }
            return builder.build();
        }
        throw new IllegalArgumentException("No path segments to remove");
    }
}
