package sun.nio.fs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.util.Locale;

public abstract class AbstractFileTypeDetector extends FileTypeDetector {
    private static final String TSPECIALS = "()<>@,;:/[]?=\\\"";

    protected abstract String implProbeContentType(Path path) throws IOException;

    protected AbstractFileTypeDetector() {
    }

    public final String probeContentType(Path file) throws IOException {
        if (file == null) {
            throw new NullPointerException("'file' is null");
        }
        String result = implProbeContentType(file);
        if (result == null) {
            return null;
        }
        return parse(result);
    }

    private static String parse(String s) {
        int slash = s.indexOf(47);
        int semicolon = s.indexOf(59);
        if (slash < 0) {
            return null;
        }
        String type = s.substring(0, slash).trim().toLowerCase(Locale.ENGLISH);
        if (!isValidToken(type)) {
            return null;
        }
        String subtype;
        if (semicolon < 0) {
            subtype = s.substring(slash + 1);
        } else {
            subtype = s.substring(slash + 1, semicolon);
        }
        subtype = subtype.trim().toLowerCase(Locale.ENGLISH);
        if (!isValidToken(subtype)) {
            return null;
        }
        StringBuilder sb = new StringBuilder((type.length() + subtype.length()) + 1);
        sb.append(type);
        sb.append('/');
        sb.append(subtype);
        return sb.-java_util_stream_Collectors-mthref-7();
    }

    private static boolean isTokenChar(char c) {
        return c > ' ' && c < 127 && TSPECIALS.indexOf((int) c) < 0;
    }

    private static boolean isValidToken(String s) {
        int len = s.length();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!isTokenChar(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
