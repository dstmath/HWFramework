package sun.nio.fs;

import java.nio.file.Path;
import libcore.net.MimeUtils;

class MimeTypesFileTypeDetector extends AbstractFileTypeDetector {
    MimeTypesFileTypeDetector() {
    }

    protected String implProbeContentType(Path path) {
        Path fn = path.getFileName();
        if (fn == null) {
            return null;
        }
        String ext = getExtension(fn.toString());
        if (ext.isEmpty()) {
            return null;
        }
        String mimeType;
        do {
            mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
            if (mimeType == null) {
                ext = getExtension(ext);
            }
            if (mimeType != null) {
                break;
            }
        } while ((ext.isEmpty() ^ 1) != 0);
        return mimeType;
    }

    private static String getExtension(String name) {
        String ext = "";
        if (name == null || (name.isEmpty() ^ 1) == 0) {
            return ext;
        }
        int dot = name.indexOf(46);
        if (dot < 0 || dot >= name.length() - 1) {
            return ext;
        }
        return name.substring(dot + 1);
    }
}
