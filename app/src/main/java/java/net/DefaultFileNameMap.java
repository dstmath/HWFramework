package java.net;

import java.util.Locale;
import libcore.net.MimeUtils;
import org.xmlpull.v1.XmlPullParser;

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
        String ext = XmlPullParser.NO_NAMESPACE;
        if (firstCharInExtension > filename.lastIndexOf(47)) {
            ext = filename.substring(firstCharInExtension, lastCharInExtension);
        }
        return MimeUtils.guessMimeTypeFromExtension(ext.toLowerCase(Locale.US));
    }
}
