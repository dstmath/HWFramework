package android.icu.impl.duration.impl;

import android.icu.lang.UCharacter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class XMLRecordWriter implements RecordWriter {
    private static final String INDENT = "    ";
    static final String NULL_NAME = "Null";
    private List<String> nameStack = new ArrayList();
    private Writer w;

    public XMLRecordWriter(Writer w) {
        this.w = w;
    }

    public boolean open(String title) {
        newline();
        writeString("<" + title + ">");
        this.nameStack.add(title);
        return true;
    }

    public boolean close() {
        int ix = this.nameStack.size() - 1;
        if (ix < 0) {
            return false;
        }
        String name = (String) this.nameStack.remove(ix);
        newline();
        writeString("</" + name + ">");
        return true;
    }

    public void flush() {
        try {
            this.w.flush();
        } catch (IOException e) {
        }
    }

    public void bool(String name, boolean value) {
        internalString(name, String.valueOf(value));
    }

    public void boolArray(String name, boolean[] values) {
        if (values != null) {
            String[] stringValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                stringValues[i] = String.valueOf(values[i]);
            }
            stringArray(name, stringValues);
        }
    }

    private static String ctos(char value) {
        if (value == '<') {
            return "&lt;";
        }
        if (value == '&') {
            return "&amp;";
        }
        return String.valueOf(value);
    }

    public void character(String name, char value) {
        if (value != 65535) {
            internalString(name, ctos(value));
        }
    }

    public void characterArray(String name, char[] values) {
        if (values != null) {
            String[] stringValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                char value = values[i];
                if (value == 65535) {
                    stringValues[i] = NULL_NAME;
                } else {
                    stringValues[i] = ctos(value);
                }
            }
            internalStringArray(name, stringValues);
        }
    }

    public void namedIndex(String name, String[] names, int value) {
        if (value >= 0) {
            internalString(name, names[value]);
        }
    }

    public void namedIndexArray(String name, String[] names, byte[] values) {
        if (values != null) {
            String[] stringValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                int value = values[i];
                if (value < 0) {
                    stringValues[i] = NULL_NAME;
                } else {
                    stringValues[i] = names[value];
                }
            }
            internalStringArray(name, stringValues);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x002f A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String normalize(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = null;
        boolean inWhitespace = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            boolean special;
            if (UCharacter.isWhitespace(c)) {
                if (sb == null && (inWhitespace || c != ' ')) {
                    sb = new StringBuilder(str.substring(0, i));
                }
                if (!inWhitespace) {
                    inWhitespace = true;
                    special = false;
                    c = ' ';
                    if (sb != null) {
                        if (special) {
                            String str2;
                            if (c == '<') {
                                str2 = "&lt;";
                            } else {
                                str2 = "&amp;";
                            }
                            sb.append(str2);
                        } else {
                            sb.append(c);
                        }
                    }
                }
            } else {
                inWhitespace = false;
                special = c == '<' || c == '&';
                if (special && sb == null) {
                    sb = new StringBuilder(str.substring(0, i));
                }
                if (sb != null) {
                }
            }
        }
        if (sb != null) {
            return sb.toString();
        }
        return str;
    }

    private void internalString(String name, String normalizedValue) {
        if (normalizedValue != null) {
            newline();
            writeString("<" + name + ">" + normalizedValue + "</" + name + ">");
        }
    }

    private void internalStringArray(String name, String[] normalizedValues) {
        if (normalizedValues != null) {
            push(name + "List");
            for (String value : normalizedValues) {
                String value2;
                if (value2 == null) {
                    value2 = NULL_NAME;
                }
                string(name, value2);
            }
            pop();
        }
    }

    public void string(String name, String value) {
        internalString(name, normalize(value));
    }

    public void stringArray(String name, String[] values) {
        if (values != null) {
            push(name + "List");
            for (String normalize : values) {
                String value = normalize(normalize);
                if (value == null) {
                    value = NULL_NAME;
                }
                internalString(name, value);
            }
            pop();
        }
    }

    public void stringTable(String name, String[][] values) {
        if (values != null) {
            push(name + "Table");
            for (String[] rowValues : values) {
                if (rowValues == null) {
                    internalString(name + "List", NULL_NAME);
                } else {
                    stringArray(name, rowValues);
                }
            }
            pop();
        }
    }

    private void push(String name) {
        newline();
        writeString("<" + name + ">");
        this.nameStack.add(name);
    }

    private void pop() {
        String name = (String) this.nameStack.remove(this.nameStack.size() - 1);
        newline();
        writeString("</" + name + ">");
    }

    private void newline() {
        writeString("\n");
        for (int i = 0; i < this.nameStack.size(); i++) {
            writeString(INDENT);
        }
    }

    private void writeString(String str) {
        if (this.w != null) {
            try {
                this.w.write(str);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                this.w = null;
            }
        }
    }
}
