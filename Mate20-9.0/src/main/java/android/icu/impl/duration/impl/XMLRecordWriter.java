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

    public XMLRecordWriter(Writer w2) {
        this.w = w2;
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
        newline();
        writeString("</" + this.nameStack.remove(ix) + ">");
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
                byte value = values[i];
                if (value < 0) {
                    stringValues[i] = NULL_NAME;
                } else {
                    stringValues[i] = names[value];
                }
            }
            internalStringArray(name, stringValues);
        }
    }

    public static String normalize(String str) {
        boolean special;
        if (str == null) {
            return null;
        }
        boolean inWhitespace = false;
        StringBuilder sb = null;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (UCharacter.isWhitespace(c)) {
                if (sb == null && (inWhitespace || c != ' ')) {
                    sb = new StringBuilder(str.substring(0, i));
                }
                if (inWhitespace) {
                } else {
                    inWhitespace = true;
                    special = false;
                    c = ' ';
                }
            } else {
                inWhitespace = false;
                special = c == '<' || c == '&';
                if (special && sb == null) {
                    sb = new StringBuilder(str.substring(0, i));
                }
            }
            if (sb != null) {
                if (special) {
                    sb.append(c == '<' ? "&lt;" : "&amp;");
                } else {
                    sb.append(c);
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
                if (value == null) {
                    value = NULL_NAME;
                }
                string(name, value);
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
        List<String> list = this.nameStack;
        newline();
        writeString("</" + list.remove(this.nameStack.size() - 1) + ">");
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
