package ohos.global.icu.impl.duration.impl;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.lang.UCharacter;

public class XMLRecordReader implements RecordReader {
    private boolean atTag;
    private List<String> nameStack = new ArrayList();
    private Reader r;
    private String tag;

    public XMLRecordReader(Reader reader) {
        this.r = reader;
        if (getTag().startsWith("?xml")) {
            advance();
        }
        if (getTag().startsWith("!--")) {
            advance();
        }
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public boolean open(String str) {
        if (!getTag().equals(str)) {
            return false;
        }
        this.nameStack.add(str);
        advance();
        return true;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public boolean close() {
        int size = this.nameStack.size() - 1;
        String tag2 = getTag();
        if (!tag2.equals(PsuedoNames.PSEUDONAME_ROOT + this.nameStack.get(size))) {
            return false;
        }
        this.nameStack.remove(size);
        advance();
        return true;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public boolean bool(String str) {
        String string = string(str);
        if (string != null) {
            return "true".equals(string);
        }
        return false;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public boolean[] boolArray(String str) {
        String[] stringArray = stringArray(str);
        if (stringArray == null) {
            return null;
        }
        boolean[] zArr = new boolean[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            zArr[i] = "true".equals(stringArray[i]);
        }
        return zArr;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public char character(String str) {
        String string = string(str);
        if (string != null) {
            return string.charAt(0);
        }
        return 65535;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public char[] characterArray(String str) {
        String[] stringArray = stringArray(str);
        if (stringArray == null) {
            return null;
        }
        char[] cArr = new char[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            cArr[i] = stringArray[i].charAt(0);
        }
        return cArr;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public byte namedIndex(String str, String[] strArr) {
        String string = string(str);
        if (string == null) {
            return -1;
        }
        for (int i = 0; i < strArr.length; i++) {
            if (string.equals(strArr[i])) {
                return (byte) i;
            }
        }
        return -1;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public byte[] namedIndexArray(String str, String[] strArr) {
        String[] stringArray = stringArray(str);
        if (stringArray == null) {
            return null;
        }
        byte[] bArr = new byte[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            String str2 = stringArray[i];
            int i2 = 0;
            while (true) {
                if (i2 >= strArr.length) {
                    bArr[i] = -1;
                    break;
                } else if (strArr[i2].equals(str2)) {
                    bArr[i] = (byte) i2;
                    break;
                } else {
                    i2++;
                }
            }
        }
        return bArr;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public String string(String str) {
        if (!match(str)) {
            return null;
        }
        String readData = readData();
        if (match(PsuedoNames.PSEUDONAME_ROOT + str)) {
            return readData;
        }
        return null;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public String[] stringArray(String str) {
        if (match(str + "List")) {
            ArrayList arrayList = new ArrayList();
            while (true) {
                String string = string(str);
                if (string == null) {
                    break;
                }
                if ("Null".equals(string)) {
                    string = null;
                }
                arrayList.add(string);
            }
            if (match(PsuedoNames.PSEUDONAME_ROOT + str + "List")) {
                return (String[]) arrayList.toArray(new String[arrayList.size()]);
            }
        }
        return null;
    }

    @Override // ohos.global.icu.impl.duration.impl.RecordReader
    public String[][] stringTable(String str) {
        if (!match(str + "Table")) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        while (true) {
            String[] stringArray = stringArray(str);
            if (stringArray == null) {
                break;
            }
            arrayList.add(stringArray);
        }
        if (match(PsuedoNames.PSEUDONAME_ROOT + str + "Table")) {
            return (String[][]) arrayList.toArray(new String[arrayList.size()][]);
        }
        return null;
    }

    private boolean match(String str) {
        if (!getTag().equals(str)) {
            return false;
        }
        advance();
        return true;
    }

    private String getTag() {
        if (this.tag == null) {
            this.tag = readNextTag();
        }
        return this.tag;
    }

    private void advance() {
        this.tag = null;
    }

    private String readData() {
        int readChar;
        StringBuilder sb = new StringBuilder();
        boolean z = false;
        boolean z2 = false;
        while (true) {
            readChar = readChar();
            if (readChar == -1 || readChar == 60) {
                break;
            }
            if (readChar == 38) {
                int readChar2 = readChar();
                if (readChar2 == 35) {
                    StringBuilder sb2 = new StringBuilder();
                    int i = 10;
                    int readChar3 = readChar();
                    if (readChar3 == 120) {
                        i = 16;
                        readChar3 = readChar();
                    }
                    while (readChar3 != 59 && readChar3 != -1) {
                        sb2.append((char) readChar3);
                        readChar3 = readChar();
                    }
                    try {
                        readChar = (char) Integer.parseInt(sb2.toString(), i);
                    } catch (NumberFormatException e) {
                        System.err.println("numbuf: " + sb2.toString() + " radix: " + i);
                        throw e;
                    }
                } else {
                    StringBuilder sb3 = new StringBuilder();
                    while (readChar2 != 59 && readChar2 != -1) {
                        sb3.append((char) readChar2);
                        readChar2 = readChar();
                    }
                    String sb4 = sb3.toString();
                    if (sb4.equals("lt")) {
                        readChar = 60;
                    } else if (sb4.equals("gt")) {
                        readChar = 62;
                    } else if (sb4.equals("quot")) {
                        readChar = 34;
                    } else if (sb4.equals("apos")) {
                        readChar = 39;
                    } else if (sb4.equals("amp")) {
                        readChar = 38;
                    } else {
                        System.err.println("unrecognized character entity: '" + sb4 + "'");
                    }
                }
            }
            if (!UCharacter.isWhitespace(readChar)) {
                z2 = false;
            } else if (!z2) {
                readChar = 32;
                z2 = true;
            }
            sb.append((char) readChar);
        }
        if (readChar == 60) {
            z = true;
        }
        this.atTag = z;
        return sb.toString();
    }

    private String readNextTag() {
        int readChar;
        while (true) {
            if (!this.atTag) {
                readChar = readChar();
                if (readChar != 60 && readChar != -1) {
                    if (!UCharacter.isWhitespace(readChar)) {
                        PrintStream printStream = System.err;
                        printStream.println("Unexpected non-whitespace character " + Integer.toHexString(readChar));
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (readChar == 60) {
            this.atTag = true;
        }
        if (!this.atTag) {
            return null;
        }
        this.atTag = false;
        StringBuilder sb = new StringBuilder();
        while (true) {
            int readChar2 = readChar();
            if (readChar2 == 62 || readChar2 == -1) {
                break;
            }
            sb.append((char) readChar2);
        }
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public int readChar() {
        try {
            return this.r.read();
        } catch (IOException unused) {
            return -1;
        }
    }
}
