package ohos.global.icu.impl.duration.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;
import ohos.global.icu.lang.UCharacter;

public class XMLRecordWriter implements RecordWriter {
    private static final String INDENT = "    ";
    static final String NULL_NAME = "Null";
    private List<String> nameStack = new ArrayList();
    private Writer w;

    public XMLRecordWriter(Writer writer) {
        this.w = writer;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public boolean open(String str) {
        newline();
        writeString("<" + str + ">");
        this.nameStack.add(str);
        return true;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public boolean close() {
        int size = this.nameStack.size() - 1;
        if (size < 0) {
            return false;
        }
        newline();
        writeString("</" + this.nameStack.remove(size) + ">");
        return true;
    }

    public void flush() {
        try {
            this.w.flush();
        } catch (IOException unused) {
        }
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public void bool(String str, boolean z) {
        internalString(str, String.valueOf(z));
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public void boolArray(String str, boolean[] zArr) {
        if (zArr != null) {
            String[] strArr = new String[zArr.length];
            for (int i = 0; i < zArr.length; i++) {
                strArr[i] = String.valueOf(zArr[i]);
            }
            stringArray(str, strArr);
        }
    }

    private static String ctos(char c) {
        if (c == '<') {
            return SerializerConstants.ENTITY_LT;
        }
        return c == '&' ? SerializerConstants.ENTITY_AMP : String.valueOf(c);
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public void character(String str, char c) {
        if (c != 65535) {
            internalString(str, ctos(c));
        }
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public void characterArray(String str, char[] cArr) {
        if (cArr != null) {
            String[] strArr = new String[cArr.length];
            for (int i = 0; i < cArr.length; i++) {
                char c = cArr[i];
                if (c == 65535) {
                    strArr[i] = NULL_NAME;
                } else {
                    strArr[i] = ctos(c);
                }
            }
            internalStringArray(str, strArr);
        }
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public void namedIndex(String str, String[] strArr, int i) {
        if (i >= 0) {
            internalString(str, strArr[i]);
        }
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public void namedIndexArray(String str, String[] strArr, byte[] bArr) {
        if (bArr != null) {
            String[] strArr2 = new String[bArr.length];
            for (int i = 0; i < bArr.length; i++) {
                byte b = bArr[i];
                if (b < 0) {
                    strArr2[i] = NULL_NAME;
                } else {
                    strArr2[i] = strArr[b];
                }
            }
            internalStringArray(str, strArr2);
        }
    }

    public static String normalize(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = null;
        boolean z = false;
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            boolean z2 = true;
            if (UCharacter.isWhitespace(charAt)) {
                if (sb == null && (z || charAt != ' ')) {
                    sb = new StringBuilder(str.substring(0, i));
                }
                if (!z) {
                    charAt = ' ';
                    z = true;
                    z2 = false;
                }
            } else {
                if (!(charAt == '<' || charAt == '&')) {
                    z2 = false;
                }
                if (z2 && sb == null) {
                    sb = new StringBuilder(str.substring(0, i));
                }
                z = false;
            }
            if (sb != null) {
                if (z2) {
                    sb.append(charAt == '<' ? SerializerConstants.ENTITY_LT : SerializerConstants.ENTITY_AMP);
                } else {
                    sb.append(charAt);
                }
            }
        }
        return sb != null ? sb.toString() : str;
    }

    private void internalString(String str, String str2) {
        if (str2 != null) {
            newline();
            writeString("<" + str + ">" + str2 + "</" + str + ">");
        }
    }

    private void internalStringArray(String str, String[] strArr) {
        if (strArr != null) {
            push(str + "List");
            for (String str2 : strArr) {
                if (str2 == null) {
                    str2 = NULL_NAME;
                }
                string(str, str2);
            }
            pop();
        }
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public void string(String str, String str2) {
        internalString(str, normalize(str2));
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public void stringArray(String str, String[] strArr) {
        if (strArr != null) {
            push(str + "List");
            for (String str2 : strArr) {
                String normalize = normalize(str2);
                if (normalize == null) {
                    normalize = NULL_NAME;
                }
                internalString(str, normalize);
            }
            pop();
        }
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordWriter
    public void stringTable(String str, String[][] strArr) {
        if (strArr != null) {
            push(str + "Table");
            for (String[] strArr2 : strArr) {
                if (strArr2 == null) {
                    internalString(str + "List", NULL_NAME);
                } else {
                    stringArray(str, strArr2);
                }
            }
            pop();
        }
    }

    private void push(String str) {
        newline();
        writeString("<" + str + ">");
        this.nameStack.add(str);
    }

    private void pop() {
        List<String> list = this.nameStack;
        newline();
        writeString("</" + list.remove(this.nameStack.size() - 1) + ">");
    }

    private void newline() {
        writeString("\n");
        for (int i = 0; i < this.nameStack.size(); i++) {
            writeString("    ");
        }
    }

    private void writeString(String str) {
        Writer writer = this.w;
        if (writer != null) {
            try {
                writer.write(str);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                this.w = null;
            }
        }
    }
}
