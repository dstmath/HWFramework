package android.support.v4.content;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;

public final class MimeTypeFilter {
    private MimeTypeFilter() {
    }

    private static boolean mimeTypeAgainstFilter(@NonNull String[] mimeTypeParts, @NonNull String[] filterParts) {
        if (filterParts.length != 2) {
            throw new IllegalArgumentException("Ill-formatted MIME type filter. Must be type/subtype.");
        } else if (filterParts[0].isEmpty() || filterParts[1].isEmpty()) {
            throw new IllegalArgumentException("Ill-formatted MIME type filter. Type or subtype empty.");
        } else if (mimeTypeParts.length != 2) {
            return false;
        } else {
            if ("*".equals(filterParts[0]) || (filterParts[0].equals(mimeTypeParts[0]) ^ 1) == 0) {
                return "*".equals(filterParts[1]) || (filterParts[1].equals(mimeTypeParts[1]) ^ 1) == 0;
            } else {
                return false;
            }
        }
    }

    public static boolean matches(@Nullable String mimeType, @NonNull String filter) {
        if (mimeType == null) {
            return false;
        }
        return mimeTypeAgainstFilter(mimeType.split("/"), filter.split("/"));
    }

    public static String matches(@Nullable String mimeType, @NonNull String[] filters) {
        if (mimeType == null) {
            return null;
        }
        String[] mimeTypeParts = mimeType.split("/");
        for (String filter : filters) {
            if (mimeTypeAgainstFilter(mimeTypeParts, filter.split("/"))) {
                return filter;
            }
        }
        return null;
    }

    public static String matches(@Nullable String[] mimeTypes, @NonNull String filter) {
        if (mimeTypes == null) {
            return null;
        }
        String[] filterParts = filter.split("/");
        for (String mimeType : mimeTypes) {
            if (mimeTypeAgainstFilter(mimeType.split("/"), filterParts)) {
                return mimeType;
            }
        }
        return null;
    }

    public static String[] matchesMany(@Nullable String[] mimeTypes, @NonNull String filter) {
        int i = 0;
        if (mimeTypes == null) {
            return new String[0];
        }
        ArrayList<String> list = new ArrayList();
        String[] filterParts = filter.split("/");
        int length = mimeTypes.length;
        while (i < length) {
            String mimeType = mimeTypes[i];
            if (mimeTypeAgainstFilter(mimeType.split("/"), filterParts)) {
                list.add(mimeType);
            }
            i++;
        }
        return (String[]) list.toArray(new String[list.size()]);
    }
}
