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

    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0113, code lost:
        if (r7 != ']') goto L_0x011c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0115, code lost:
        r1.append("]]");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0125, code lost:
        throw new java.util.regex.PatternSyntaxException("Missing ']", r13, r4 - 1);
     */
    private static String toRegexPattern(String globPattern, boolean isDos) {
        int i;
        StringBuilder regex = new StringBuilder("^");
        boolean inGroup = false;
        int i2 = 0;
        while (i2 < globPattern.length()) {
            int i3 = i2 + 1;
            char c = globPattern.charAt(i2);
            if (c != '*') {
                if (c != ',') {
                    if (c != '/') {
                        if (c != '?') {
                            if (c != '{') {
                                if (c != '}') {
                                    switch (c) {
                                        case Types.DATE:
                                            if (isDos) {
                                                regex.append("[[^\\\\]&&[");
                                            } else {
                                                regex.append("[[^/]&&[");
                                            }
                                            if (next(globPattern, i3) == '^') {
                                                regex.append("\\^");
                                                i3++;
                                            } else {
                                                if (next(globPattern, i3) == '!') {
                                                    regex.append('^');
                                                    i3++;
                                                }
                                                if (next(globPattern, i3) == '-') {
                                                    regex.append('-');
                                                    i3++;
                                                }
                                            }
                                            boolean hasRangeStart = false;
                                            char c2 = c;
                                            char last = 0;
                                            while (true) {
                                                if (i3 >= globPattern.length()) {
                                                    break;
                                                } else {
                                                    i = i3 + 1;
                                                    c2 = globPattern.charAt(i3);
                                                    if (c2 == ']') {
                                                        i3 = i;
                                                        break;
                                                    } else if (c2 != '/' && (!isDos || c2 != '\\')) {
                                                        if (c2 == '\\' || c2 == '[' || (c2 == '&' && next(globPattern, i) == '&')) {
                                                            regex.append('\\');
                                                        }
                                                        regex.append(c2);
                                                        if (c2 != '-') {
                                                            hasRangeStart = true;
                                                            last = c2;
                                                            i3 = i;
                                                        } else if (hasRangeStart) {
                                                            i3 = i + 1;
                                                            char next = next(globPattern, i);
                                                            c2 = next;
                                                            if (!(next == EOL || c2 == ']')) {
                                                                if (c2 >= last) {
                                                                    regex.append(c2);
                                                                    hasRangeStart = false;
                                                                } else {
                                                                    throw new PatternSyntaxException("Invalid range", globPattern, i3 - 3);
                                                                }
                                                            }
                                                        } else {
                                                            throw new PatternSyntaxException("Invalid range", globPattern, i - 1);
                                                        }
                                                    }
                                                }
                                            }
                                            throw new PatternSyntaxException("Explicit 'name separator' in class", globPattern, i - 1);
                                        case Types.TIME:
                                            if (i3 != globPattern.length()) {
                                                int i4 = i3 + 1;
                                                char next2 = globPattern.charAt(i3);
                                                if (isGlobMeta(next2) || isRegexMeta(next2)) {
                                                    regex.append('\\');
                                                }
                                                regex.append(next2);
                                                i2 = i4;
                                                continue;
                                            } else {
                                                throw new PatternSyntaxException("No character to escape", globPattern, i3 - 1);
                                            }
                                        default:
                                            if (isRegexMeta(c)) {
                                                regex.append('\\');
                                            }
                                            regex.append(c);
                                            break;
                                    }
                                } else if (inGroup) {
                                    regex.append("))");
                                    inGroup = false;
                                } else {
                                    regex.append('}');
                                }
                            } else if (!inGroup) {
                                regex.append("(?:(?:");
                                inGroup = true;
                            } else {
                                throw new PatternSyntaxException("Cannot nest groups", globPattern, i3 - 1);
                            }
                        } else if (isDos) {
                            regex.append("[^\\\\]");
                        } else {
                            regex.append("[^/]");
                        }
                    } else if (isDos) {
                        regex.append("\\\\");
                    } else {
                        regex.append(c);
                    }
                } else if (inGroup) {
                    regex.append(")|(?:");
                } else {
                    regex.append(',');
                }
            } else if (next(globPattern, i3) == '*') {
                regex.append(".*");
                i3++;
            } else if (isDos) {
                regex.append("[^\\\\]*");
            } else {
                regex.append("[^/]*");
            }
            i2 = i3;
        }
        if (!inGroup) {
            regex.append('$');
            return regex.toString();
        }
        throw new PatternSyntaxException("Missing '}", globPattern, i2 - 1);
    }

    static String toUnixRegexPattern(String globPattern) {
        return toRegexPattern(globPattern, false);
    }

    static String toWindowsRegexPattern(String globPattern) {
        return toRegexPattern(globPattern, true);
    }
}
