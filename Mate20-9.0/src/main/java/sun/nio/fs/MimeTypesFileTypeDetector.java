package sun.nio.fs;

import java.nio.file.Path;
import libcore.net.MimeUtils;

class MimeTypesFileTypeDetector extends AbstractFileTypeDetector {
    MimeTypesFileTypeDetector() {
    }

    /* access modifiers changed from: protected */
    public String implProbeContentType(Path path) {
        String mimeType;
        Path fn = path.getFileName();
        if (fn == null) {
            return null;
        }
        String ext = getExtension(fn.toString());
        if (ext.isEmpty()) {
            return null;
        }
        do {
            mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
            if (mimeType == null) {
                ext = getExtension(ext);
            }
            if (mimeType != null) {
                break;
            }
        } while (!ext.isEmpty());
        return mimeType;
    }

    private static String getExtension(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        int dot = name.indexOf(46);
        if (dot < 0 || dot >= name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1);
    }
}
