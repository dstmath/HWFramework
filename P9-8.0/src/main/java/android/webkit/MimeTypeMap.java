package android.webkit;

import android.rms.iaware.AwareConstant.Database;
import android.text.TextUtils;
import android.util.LogException;
import java.util.regex.Pattern;
import libcore.net.MimeUtils;

public class MimeTypeMap {
    private static final MimeTypeMap sMimeTypeMap = new MimeTypeMap();

    private MimeTypeMap() {
    }

    public static String getFileExtensionFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf(35);
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }
            int query = url.lastIndexOf(63);
            if (query > 0) {
                url = url.substring(0, query);
            }
            int filenamePos = url.lastIndexOf(47);
            String filename = filenamePos >= 0 ? url.substring(filenamePos + 1) : url;
            if (!filename.isEmpty() && Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename)) {
                int dotPos = filename.lastIndexOf(46);
                if (dotPos >= 0) {
                    return filename.substring(dotPos + 1);
                }
            }
        }
        return LogException.NO_VALUE;
    }

    public boolean hasMimeType(String mimeType) {
        return MimeUtils.hasMimeType(mimeType);
    }

    public String getMimeTypeFromExtension(String extension) {
        return MimeUtils.guessMimeTypeFromExtension(extension);
    }

    private static String mimeTypeFromExtension(String extension) {
        return MimeUtils.guessMimeTypeFromExtension(extension);
    }

    public boolean hasExtension(String extension) {
        return MimeUtils.hasExtension(extension);
    }

    public String getExtensionFromMimeType(String mimeType) {
        return MimeUtils.guessExtensionFromMimeType(mimeType);
    }

    String remapGenericMimeType(String mimeType, String url, String contentDisposition) {
        if ("text/plain".equals(mimeType) || Database.UNKNOWN_MIME_TYPE.equals(mimeType)) {
            String filename = null;
            if (contentDisposition != null) {
                filename = URLUtil.parseContentDisposition(contentDisposition);
            }
            if (filename != null) {
                url = filename;
            }
            String newMimeType = getMimeTypeFromExtension(getFileExtensionFromUrl(url));
            if (newMimeType != null) {
                return newMimeType;
            }
            return mimeType;
        } else if ("text/vnd.wap.wml".equals(mimeType)) {
            return "text/plain";
        } else {
            if ("application/vnd.wap.xhtml+xml".equals(mimeType)) {
                return "application/xhtml+xml";
            }
            return mimeType;
        }
    }

    public static MimeTypeMap getSingleton() {
        return sMimeTypeMap;
    }
}
