package sun.nio.fs;

import java.sql.Types;
import java.util.regex.PatternSyntaxException;

public class Globs {
    private static char EOL = 0;
    private static final String globMetaChars = "\\*?[{";
    private static final String regexMetaChars = ".^$+{[]|()";

    private Globs() {
    }

    private static boolean isRegexMeta(char c) {
        return regexMetaChars.indexOf((int) c) != -1;
    }

    private static boolean isGlobMeta(char c) {
        return globMetaChars.indexOf((int) c) != -1;
    }

    private static char next(String glob, int i) {
        if (i < glob.length()) {
            return glob.charAt(i);
        }
        return EOL;
    }

    /* JADX WARNING: Removed duplicated region for block: B:75:0x012c  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0092 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String toRegexPattern(String globPattern, boolean isDos) {
        boolean inGroup = false;
        StringBuilder regex = new StringBuilder("^");
        int i = 0;
        while (i < globPattern.length()) {
            int i2 = i + 1;
            char c = globPattern.charAt(i);
            switch (c) {
                case ZipConstants.CENOFF /*42*/:
                    if (next(globPattern, i2) != '*') {
                        if (!isDos) {
                            regex.append("[^/]*");
                            i = i2;
                            break;
                        }
                        regex.append("[^\\\\]*");
                        i = i2;
                        break;
                    }
                    regex.append(".*");
                    i = i2 + 1;
                    break;
                case ',':
                    if (inGroup) {
                        regex.append(")|(?:");
                    } else {
                        regex.append(',');
                    }
                    i = i2;
                    break;
                case '/':
                    if (isDos) {
                        regex.append("\\\\");
                    } else {
                        regex.append(c);
                    }
                    i = i2;
                    break;
                case '?':
                    if (isDos) {
                        regex.append("[^\\\\]");
                    } else {
                        regex.append("[^/]");
                    }
                    i = i2;
                    break;
                case Types.DATE /*91*/:
                    if (isDos) {
                        regex.append("[[^\\\\]&&[");
                    } else {
                        regex.append("[[^/]&&[");
                    }
                    if (next(globPattern, i2) == '^') {
                        regex.append("\\^");
                        i = i2 + 1;
                    } else {
                        if (next(globPattern, i2) == '!') {
                            regex.append('^');
                            i = i2 + 1;
                        } else {
                            i = i2;
                        }
                        if (next(globPattern, i) == '-') {
                            regex.append('-');
                            i++;
                        }
                    }
                    boolean hasRangeStart = false;
                    char last = 0;
                    while (i < globPattern.length()) {
                        i2 = i + 1;
                        c = globPattern.charAt(i);
                        if (c == ']') {
                            i = i2;
                        } else if (c == '/' || (isDos && c == '\\')) {
                            throw new PatternSyntaxException("Explicit 'name separator' in class", globPattern, i2 - 1);
                        } else {
                            if (c == '\\' || c == '[' || (c == '&' && next(globPattern, i2) == '&')) {
                                regex.append('\\');
                            }
                            regex.append(c);
                            if (c != '-') {
                                hasRangeStart = true;
                                last = c;
                                i = i2;
                            } else if (hasRangeStart) {
                                i = i2 + 1;
                                c = next(globPattern, i2);
                                if (!(c == EOL || c == ']')) {
                                    if (c < last) {
                                        throw new PatternSyntaxException("Invalid range", globPattern, i - 3);
                                    }
                                    regex.append(c);
                                    hasRangeStart = false;
                                }
                            } else {
                                throw new PatternSyntaxException("Invalid range", globPattern, i2 - 1);
                            }
                        }
                        if (c != ']') {
                            regex.append("]]");
                            break;
                        }
                        throw new PatternSyntaxException("Missing ']", globPattern, i - 1);
                    }
                    if (c != ']') {
                    }
                    break;
                case Types.TIME /*92*/:
                    if (i2 != globPattern.length()) {
                        i = i2 + 1;
                        char next = globPattern.charAt(i2);
                        if (isGlobMeta(next) || isRegexMeta(next)) {
                            regex.append('\\');
                        }
                        regex.append(next);
                        break;
                    }
                    throw new PatternSyntaxException("No character to escape", globPattern, i2 - 1);
                case '{':
                    if (!inGroup) {
                        regex.append("(?:(?:");
                        inGroup = true;
                        i = i2;
                        break;
                    }
                    throw new PatternSyntaxException("Cannot nest groups", globPattern, i2 - 1);
                case '}':
                    if (inGroup) {
                        regex.append("))");
                        inGroup = false;
                    } else {
                        regex.append('}');
                    }
                    i = i2;
                    break;
                default:
                    if (isRegexMeta(c)) {
                        regex.append('\\');
                    }
                    regex.append(c);
                    i = i2;
                    break;
            }
        }
        if (!inGroup) {
            return regex.append('$').-java_util_stream_Collectors-mthref-7();
        }
        throw new PatternSyntaxException("Missing '}", globPattern, i - 1);
    }

    static String toUnixRegexPattern(String globPattern) {
        return toRegexPattern(globPattern, false);
    }

    static String toWindowsRegexPattern(String globPattern) {
        return toRegexPattern(globPattern, true);
    }
}
