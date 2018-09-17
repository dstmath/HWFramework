package java.net;

import libcore.net.MimeUtils;

class DefaultFileNameMap implements FileNameMap {
    DefaultFileNameMap() {
    }

    public String getContentTypeFor(String filename) {
        if (filename.endsWith("/")) {
            return MimeUtils.guessMimeTypeFromExtension("html");
        }
        int lastCharInExtension = filename.lastIndexOf(35);
        if (lastCharInExtension < 0) {
            lastCharInExtension = filename.length();
        }
        int firstCharInExtension = filename.lastIndexOf(46) + 1;
        String ext = "";
        if (firstCharInExtension > filename.lastIndexOf(47)) {
            ext = filename.substring(firstCharInExtension, lastCharInExtension);
        }
        return MimeUtils.guessMimeTypeFromExtension(ext);
    }
}
