package ohos.aafwk.ability;

import java.util.List;
import ohos.utils.net.Uri;

public class DataUriUtils {
    private static final String URI_SPLIT = "/";

    public static long getId(Uri uri) {
        String lastPathSegments = getLastPathSegments(uri);
        if (lastPathSegments == null) {
            return -1;
        }
        return Long.parseLong(lastPathSegments);
    }

    public static Uri attachId(Uri uri, long j) {
        String str;
        if (uri != null) {
            String encodedPath = uri.getEncodedPath();
            if (encodedPath == null) {
                str = "";
            } else {
                str = encodedPath + "/" + j;
            }
            return new Uri.Builder().scheme(uri.getScheme()).encodedAuthority(uri.getEncodedAuthority()).encodedPath(str).encodedQuery(uri.getEncodedQuery()).encodedFragment(uri.getEncodedFragment()).build();
        }
        throw new IllegalArgumentException("The data uri is illegal");
    }

    public static Uri deleteId(Uri uri) {
        String lastPathSegments = getLastPathSegments(uri);
        if (lastPathSegments != null) {
            Long.parseLong(lastPathSegments);
            String str = null;
            int lastIndexOf = uri.getEncodedPath().lastIndexOf("/");
            if (lastIndexOf != -1) {
                str = uri.getEncodedPath().substring(0, lastIndexOf);
            }
            return new Uri.Builder().scheme(uri.getScheme()).encodedAuthority(uri.getEncodedAuthority()).encodedPath(str).encodedQuery(uri.getEncodedQuery()).encodedFragment(uri.getEncodedFragment()).build();
        }
        throw new IllegalArgumentException("No id need to remove");
    }

    private static String getLastPathSegments(Uri uri) {
        if (uri != null) {
            List decodedPathList = uri.getDecodedPathList();
            if (decodedPathList == null || decodedPathList.isEmpty()) {
                return null;
            }
            return (String) decodedPathList.get(decodedPathList.size() - 1);
        }
        throw new IllegalArgumentException("The data uri is illegal");
    }
}
