package android.text.util;

import android.widget.MultiAutoCompleteTextView;
import java.util.ArrayList;
import java.util.Collection;

public class Rfc822Tokenizer implements MultiAutoCompleteTextView.Tokenizer {
    public static void tokenize(CharSequence text, Collection<Rfc822Token> out) {
        StringBuilder name = new StringBuilder();
        StringBuilder address = new StringBuilder();
        StringBuilder comment = new StringBuilder();
        int i = 0;
        int cursor = text.length();
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
                while (true) {
                    if (i >= cursor) {
                        break;
                    }
                    char c2 = text.charAt(i);
                    if (c2 == '\"') {
                        i++;
                        break;
                    } else if (c2 == '\\') {
                        if (i + 1 < cursor) {
                            name.append(text.charAt(i + 1));
                        }
                        i += 2;
                    } else {
                        name.append(c2);
                        i++;
                    }
                }
            } else if (c == '(') {
                int level = 1;
                i++;
                while (i < cursor && level > 0) {
                    char c3 = text.charAt(i);
                    if (c3 == ')') {
                        if (level > 1) {
                            comment.append(c3);
                        }
                        level--;
                        i++;
                    } else if (c3 == '(') {
                        comment.append(c3);
                        level++;
                        i++;
                    } else if (c3 == '\\') {
                        if (i + 1 < cursor) {
                            comment.append(text.charAt(i + 1));
                        }
                        i += 2;
                    } else {
                        comment.append(c3);
                        i++;
                    }
                }
            } else if (c == '<') {
                i++;
                while (true) {
                    if (i >= cursor) {
                        break;
                    }
                    char c4 = text.charAt(i);
                    if (c4 == '>') {
                        i++;
                        break;
                    } else {
                        address.append(c4);
                        i++;
                    }
                }
            } else if (c == ' ') {
                name.append((char) 0);
                i++;
            } else {
                name.append(c);
                i++;
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
        ArrayList<Rfc822Token> out = new ArrayList<>();
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
        for (int i2 = 0; i2 < len; i2++) {
            if (sb.charAt(i2) == 0) {
                sb.setCharAt(i2, ' ');
            }
        }
    }

    @Override // android.widget.MultiAutoCompleteTextView.Tokenizer
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

    @Override // android.widget.MultiAutoCompleteTextView.Tokenizer
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
                while (true) {
                    if (i >= len) {
                        break;
                    }
                    char c2 = text.charAt(i);
                    if (c2 == '\"') {
                        i++;
                        break;
                    } else if (c2 != '\\' || i + 1 >= len) {
                        i++;
                    } else {
                        i += 2;
                    }
                }
            } else if (c == '(') {
                int level = 1;
                i++;
                while (i < len && level > 0) {
                    char c3 = text.charAt(i);
                    if (c3 == ')') {
                        level--;
                        i++;
                    } else if (c3 == '(') {
                        level++;
                        i++;
                    } else if (c3 != '\\' || i + 1 >= len) {
                        i++;
                    } else {
                        i += 2;
                    }
                }
            } else if (c == '<') {
                i++;
                while (true) {
                    if (i >= len) {
                        break;
                    } else if (text.charAt(i) == '>') {
                        i++;
                        break;
                    } else {
                        i++;
                    }
                }
            } else {
                i++;
            }
        }
        return i;
    }

    @Override // android.widget.MultiAutoCompleteTextView.Tokenizer
    public CharSequence terminateToken(CharSequence text) {
        return ((Object) text) + ", ";
    }
}
