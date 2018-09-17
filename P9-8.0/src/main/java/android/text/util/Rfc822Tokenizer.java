package android.text.util;

import android.widget.MultiAutoCompleteTextView.Tokenizer;
import java.util.ArrayList;
import java.util.Collection;

public class Rfc822Tokenizer implements Tokenizer {
    public static void tokenize(CharSequence text, Collection<Rfc822Token> out) {
        StringBuilder name = new StringBuilder();
        StringBuilder address = new StringBuilder();
        StringBuilder comment = new StringBuilder();
        int i = 0;
        int cursor = text.length();
        boolean find_add_start = false;
        boolean find_add_end = false;
        while (i < cursor) {
            char c = text.charAt(i);
            if (c == ',' || c == ';') {
                i++;
                while (i < cursor && text.charAt(i) == ' ') {
                    i++;
                }
                crunch(name);
                if (address.length() > 0) {
                    out.add(new Rfc822Token(name.toString(), address.toString(), comment.toString()));
                } else if (name.length() > 0) {
                    out.add(new Rfc822Token(null, name.toString(), comment.toString()));
                }
                name.setLength(0);
                address.setLength(0);
                comment.setLength(0);
            } else if (c == '\"') {
                i++;
                while (i < cursor) {
                    c = text.charAt(i);
                    if (c == '\"') {
                        i++;
                        break;
                    } else if (c == '\\') {
                        if (i + 1 < cursor) {
                            name.append(text.charAt(i + 1));
                        }
                        i += 2;
                    } else {
                        name.append(c);
                        i++;
                    }
                }
            } else if (c == '(') {
                int level = 1;
                i++;
                while (i < cursor && level > 0) {
                    c = text.charAt(i);
                    if (c == ')') {
                        if (level > 1) {
                            comment.append(c);
                        }
                        level--;
                        i++;
                    } else if (c == '(') {
                        comment.append(c);
                        level++;
                        i++;
                    } else if (c == '\\') {
                        if (i + 1 < cursor) {
                            comment.append(text.charAt(i + 1));
                        }
                        i += 2;
                    } else {
                        comment.append(c);
                        i++;
                    }
                }
            } else if (c == '<') {
                find_add_start = true;
                i++;
                while (i < cursor) {
                    c = text.charAt(i);
                    if (c == '>') {
                        find_add_end = true;
                        i++;
                        break;
                    }
                    address.append(c);
                    i++;
                }
            } else if (c == ' ') {
                name.append(0);
                i++;
            } else {
                name.append(c);
                i++;
            }
            if (find_add_start) {
                find_add_start = false;
                if (find_add_end) {
                    find_add_end = false;
                    if (address.length() <= 0) {
                        name.append('<');
                        name.append('>');
                    }
                } else {
                    name.append('<');
                    name.append(address.toString());
                    address.setLength(0);
                }
            }
        }
        crunch(name);
        if (address.length() > 0) {
            out.add(new Rfc822Token(name.toString(), address.toString(), comment.toString()));
        } else if (name.length() > 0) {
            out.add(new Rfc822Token(null, name.toString(), comment.toString()));
        }
    }

    public static Rfc822Token[] tokenize(CharSequence text) {
        ArrayList<Rfc822Token> out = new ArrayList();
        tokenize(text, out);
        return (Rfc822Token[]) out.toArray(new Rfc822Token[out.size()]);
    }

    private static void crunch(StringBuilder sb) {
        int i = 0;
        int len = sb.length();
        while (i < len) {
            if (sb.charAt(i) != 0) {
                i++;
            } else if (i == 0 || i == len - 1 || sb.charAt(i - 1) == ' ' || sb.charAt(i - 1) == 0 || sb.charAt(i + 1) == ' ' || sb.charAt(i + 1) == 0) {
                sb.deleteCharAt(i);
                len--;
            } else {
                i++;
            }
        }
        for (i = 0; i < len; i++) {
            if (sb.charAt(i) == 0) {
                sb.setCharAt(i, ' ');
            }
        }
    }

    public int findTokenStart(CharSequence text, int cursor) {
        int best = 0;
        int i = 0;
        while (i < cursor) {
            i = findTokenEnd(text, i);
            if (i < cursor) {
                i++;
                while (i < cursor && text.charAt(i) == ' ') {
                    i++;
                }
                if (i < cursor) {
                    best = i;
                }
            }
        }
        return best;
    }

    public int findTokenEnd(CharSequence text, int cursor) {
        int len = text.length();
        int i = cursor;
        while (i < len) {
            char c = text.charAt(i);
            if (c == ',' || c == ';') {
                return i;
            }
            if (c == '\"') {
                i++;
                while (i < len) {
                    c = text.charAt(i);
                    if (c == '\"') {
                        i++;
                        break;
                    } else if (c != '\\' || i + 1 >= len) {
                        i++;
                    } else {
                        i += 2;
                    }
                }
            } else if (c == '(') {
                int level = 1;
                i++;
                while (i < len && level > 0) {
                    c = text.charAt(i);
                    if (c == ')') {
                        level--;
                        i++;
                    } else if (c == '(') {
                        level++;
                        i++;
                    } else if (c != '\\' || i + 1 >= len) {
                        i++;
                    } else {
                        i += 2;
                    }
                }
            } else if (c == '<') {
                i++;
                while (i < len) {
                    if (text.charAt(i) == '>') {
                        i++;
                        break;
                    }
                    i++;
                }
            } else {
                i++;
            }
        }
        return i;
    }

    public CharSequence terminateToken(CharSequence text) {
        return text + ", ";
    }
}
