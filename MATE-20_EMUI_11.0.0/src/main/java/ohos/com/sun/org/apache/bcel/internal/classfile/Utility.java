package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.FilterReader;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import ohos.ai.engine.bigreport.BigReportKeyValue;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;

public abstract class Utility {
    private static int[] CHAR_MAP = new int[48];
    private static final char ESCAPE_CHAR = '$';
    private static final int FREE_CHARS = 48;
    private static int[] MAP_CHAR = new int[256];
    private static int consumed_chars = 0;
    private static boolean wide = false;

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:0:0x0000 */
    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v3, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1 */
    private static final short byteToShort(byte b) {
        if (b < 0) {
            b += 256;
        }
        return b == true ? (short) 1 : 0;
    }

    public static final String classOrInterface(int i) {
        return (i & 512) != 0 ? BigReportKeyValue.KEY_INTERFACE_NAME : Constants.ATTRNAME_CLASS;
    }

    public static boolean isJavaIdentifierPart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || ((c >= '0' && c <= '9') || c == '_');
    }

    private static final boolean is_digit(char c) {
        return c >= '0' && c <= '9';
    }

    private static final boolean is_space(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private static final int pow2(int i) {
        return 1 << i;
    }

    public static final String accessToString(int i) {
        return accessToString(i, false);
    }

    public static final String accessToString(int i, boolean z) {
        StringBuffer stringBuffer = new StringBuffer();
        int i2 = 0;
        int i3 = 0;
        while (i2 < 2048) {
            i2 = pow2(i3);
            if ((i & i2) != 0 && (!z || !(i2 == 32 || i2 == 512))) {
                stringBuffer.append(ohos.com.sun.org.apache.bcel.internal.Constants.ACCESS_NAMES[i3] + " ");
            }
            i3++;
        }
        return stringBuffer.toString().trim();
    }

    public static final String codeToString(byte[] bArr, ConstantPool constantPool, int i, int i2, boolean z) {
        StringBuffer stringBuffer = new StringBuffer(bArr.length * 20);
        ByteSequence byteSequence = new ByteSequence(bArr);
        int i3 = 0;
        for (int i4 = 0; i4 < i; i4++) {
            try {
                codeToString(byteSequence, constantPool, z);
            } catch (IOException e) {
                System.out.println(stringBuffer.toString());
                e.printStackTrace();
                throw new ClassFormatException("Byte code error: " + e);
            }
        }
        while (byteSequence.available() > 0) {
            if (i2 < 0 || i3 < i2) {
                stringBuffer.append(fillup(byteSequence.getIndex() + ":", 6, true, ' ') + codeToString(byteSequence, constantPool, z) + '\n');
            }
            i3++;
        }
        return stringBuffer.toString();
    }

    public static final String codeToString(byte[] bArr, ConstantPool constantPool, int i, int i2) {
        return codeToString(bArr, constantPool, i, i2, true);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0317  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0336 A[FALL_THROUGH] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x03ed  */
    public static final String codeToString(ByteSequence byteSequence, ConstantPool constantPool, boolean z) throws IOException {
        int i;
        int i2;
        short s;
        int i3;
        int i4;
        short readUnsignedByte = (short) byteSequence.readUnsignedByte();
        StringBuffer stringBuffer = new StringBuffer(ohos.com.sun.org.apache.bcel.internal.Constants.OPCODE_NAMES[readUnsignedByte]);
        if (readUnsignedByte == 170 || readUnsignedByte == 171) {
            int index = byteSequence.getIndex() % 4;
            i2 = index == 0 ? 0 : 4 - index;
            for (int i5 = 0; i5 < i2; i5++) {
                byte readByte = byteSequence.readByte();
                if (readByte != 0) {
                    System.err.println("Warning: Padding byte != 0 in " + ohos.com.sun.org.apache.bcel.internal.Constants.OPCODE_NAMES[readUnsignedByte] + ":" + ((int) readByte));
                }
            }
            i = byteSequence.readInt();
        } else {
            i2 = 0;
            i = 0;
        }
        if (readUnsignedByte != 132) {
            String str = "";
            if (readUnsignedByte != 192) {
                if (readUnsignedByte != 193) {
                    switch (readUnsignedByte) {
                        case 18:
                            int readUnsignedByte2 = byteSequence.readUnsignedByte();
                            StringBuilder sb = new StringBuilder();
                            sb.append("\t\t");
                            sb.append(constantPool.constantToString(readUnsignedByte2, constantPool.getConstant(readUnsignedByte2).getTag()));
                            if (z) {
                                str = " (" + readUnsignedByte2 + ")";
                            }
                            sb.append(str);
                            stringBuffer.append(sb.toString());
                            break;
                        case 19:
                        case 20:
                            int readUnsignedShort = byteSequence.readUnsignedShort();
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("\t\t");
                            sb2.append(constantPool.constantToString(readUnsignedShort, constantPool.getConstant(readUnsignedShort).getTag()));
                            if (z) {
                                str = " (" + readUnsignedShort + ")";
                            }
                            sb2.append(str);
                            stringBuffer.append(sb2.toString());
                            break;
                        default:
                            switch (readUnsignedByte) {
                                default:
                                    switch (readUnsignedByte) {
                                        case 153:
                                        case 154:
                                        case 155:
                                        case 156:
                                        case 157:
                                        case 158:
                                        case 159:
                                        case 160:
                                        case 161:
                                        case 162:
                                        case 163:
                                        case 164:
                                        case 165:
                                        case 166:
                                        case 167:
                                        case 168:
                                            stringBuffer.append("\t\t#" + ((byteSequence.getIndex() - 1) + byteSequence.readShort()));
                                            break;
                                        case 169:
                                            break;
                                        case 170:
                                            int readInt = byteSequence.readInt();
                                            int readInt2 = byteSequence.readInt();
                                            int index2 = ((byteSequence.getIndex() - 12) - i2) - 1;
                                            stringBuffer.append("\tdefault = " + (i + index2) + ", low = " + readInt + ", high = " + readInt2 + "(");
                                            int[] iArr = new int[((readInt2 - readInt) + 1)];
                                            for (int i6 = 0; i6 < iArr.length; i6++) {
                                                iArr[i6] = byteSequence.readInt() + index2;
                                                stringBuffer.append(iArr[i6]);
                                                if (i6 < iArr.length - 1) {
                                                    stringBuffer.append(", ");
                                                }
                                            }
                                            stringBuffer.append(")");
                                            break;
                                        case 171:
                                            int readInt3 = byteSequence.readInt();
                                            int index3 = ((byteSequence.getIndex() - 8) - i2) - 1;
                                            int[] iArr2 = new int[readInt3];
                                            int[] iArr3 = new int[readInt3];
                                            stringBuffer.append("\tdefault = " + (i + index3) + ", npairs = " + readInt3 + " (");
                                            for (int i7 = 0; i7 < readInt3; i7++) {
                                                iArr2[i7] = byteSequence.readInt();
                                                iArr3[i7] = byteSequence.readInt() + index3;
                                                stringBuffer.append("(" + iArr2[i7] + ", " + iArr3[i7] + ")");
                                                if (i7 < readInt3 - 1) {
                                                    stringBuffer.append(", ");
                                                }
                                            }
                                            stringBuffer.append(")");
                                            break;
                                        default:
                                            switch (readUnsignedByte) {
                                                case 178:
                                                case 179:
                                                case 180:
                                                case 181:
                                                    int readUnsignedShort2 = byteSequence.readUnsignedShort();
                                                    StringBuilder sb3 = new StringBuilder();
                                                    sb3.append("\t\t");
                                                    sb3.append(constantPool.constantToString(readUnsignedShort2, (byte) 9));
                                                    if (z) {
                                                        str = " (" + readUnsignedShort2 + ")";
                                                    }
                                                    sb3.append(str);
                                                    stringBuffer.append(sb3.toString());
                                                    break;
                                                case 182:
                                                case 183:
                                                case 184:
                                                    int readUnsignedShort3 = byteSequence.readUnsignedShort();
                                                    StringBuilder sb4 = new StringBuilder();
                                                    sb4.append("\t");
                                                    sb4.append(constantPool.constantToString(readUnsignedShort3, (byte) 10));
                                                    if (z) {
                                                        str = " (" + readUnsignedShort3 + ")";
                                                    }
                                                    sb4.append(str);
                                                    stringBuffer.append(sb4.toString());
                                                    break;
                                                case 185:
                                                    int readUnsignedShort4 = byteSequence.readUnsignedShort();
                                                    int readUnsignedByte3 = byteSequence.readUnsignedByte();
                                                    StringBuilder sb5 = new StringBuilder();
                                                    sb5.append("\t");
                                                    sb5.append(constantPool.constantToString(readUnsignedShort4, (byte) 11));
                                                    if (z) {
                                                        str = " (" + readUnsignedShort4 + ")\t";
                                                    }
                                                    sb5.append(str);
                                                    sb5.append(readUnsignedByte3);
                                                    sb5.append("\t");
                                                    sb5.append(byteSequence.readUnsignedByte());
                                                    stringBuffer.append(sb5.toString());
                                                    break;
                                                default:
                                                    switch (readUnsignedByte) {
                                                        case 187:
                                                            break;
                                                        case 188:
                                                            stringBuffer.append("\t\t<" + ohos.com.sun.org.apache.bcel.internal.Constants.TYPE_NAMES[byteSequence.readByte()] + ">");
                                                            break;
                                                        case 189:
                                                            int readUnsignedShort5 = byteSequence.readUnsignedShort();
                                                            StringBuilder sb6 = new StringBuilder();
                                                            sb6.append("\t\t<");
                                                            sb6.append(compactClassName(constantPool.getConstantString(readUnsignedShort5, (byte) 7), false));
                                                            sb6.append(">");
                                                            if (z) {
                                                                str = " (" + readUnsignedShort5 + ")";
                                                            }
                                                            sb6.append(str);
                                                            stringBuffer.append(sb6.toString());
                                                            break;
                                                        default:
                                                            switch (readUnsignedByte) {
                                                                case 196:
                                                                    wide = true;
                                                                    stringBuffer.append("\t(wide)");
                                                                    break;
                                                                case 197:
                                                                    int readUnsignedShort6 = byteSequence.readUnsignedShort();
                                                                    int readUnsignedByte4 = byteSequence.readUnsignedByte();
                                                                    StringBuilder sb7 = new StringBuilder();
                                                                    sb7.append("\t<");
                                                                    sb7.append(compactClassName(constantPool.getConstantString(readUnsignedShort6, (byte) 7), false));
                                                                    sb7.append(">\t");
                                                                    sb7.append(readUnsignedByte4);
                                                                    if (z) {
                                                                        str = " (" + readUnsignedShort6 + ")";
                                                                    }
                                                                    sb7.append(str);
                                                                    stringBuffer.append(sb7.toString());
                                                                    break;
                                                                case 198:
                                                                case 199:
                                                                    break;
                                                                case 200:
                                                                case 201:
                                                                    stringBuffer.append("\t\t#" + ((byteSequence.getIndex() - 1) + byteSequence.readInt()));
                                                                    break;
                                                                default:
                                                                    if (ohos.com.sun.org.apache.bcel.internal.Constants.NO_OF_OPERANDS[readUnsignedByte] > 0) {
                                                                        for (int i8 = 0; i8 < ohos.com.sun.org.apache.bcel.internal.Constants.TYPE_OF_OPERANDS[readUnsignedByte].length; i8++) {
                                                                            stringBuffer.append("\t\t");
                                                                            switch (ohos.com.sun.org.apache.bcel.internal.Constants.TYPE_OF_OPERANDS[readUnsignedByte][i8]) {
                                                                                case 8:
                                                                                    stringBuffer.append((int) byteSequence.readByte());
                                                                                    break;
                                                                                case 9:
                                                                                    stringBuffer.append((int) byteSequence.readShort());
                                                                                    break;
                                                                                case 10:
                                                                                    stringBuffer.append(byteSequence.readInt());
                                                                                    break;
                                                                                default:
                                                                                    System.err.println("Unreachable default case reached!");
                                                                                    stringBuffer.setLength(0);
                                                                                    break;
                                                                            }
                                                                        }
                                                                        break;
                                                                    }
                                                                    break;
                                                            }
                                                    }
                                            }
                                    }
                                case 54:
                                case 55:
                                case 56:
                                case 57:
                                case 58:
                                    if (wide) {
                                        i4 = byteSequence.readUnsignedShort();
                                        wide = false;
                                    } else {
                                        i4 = byteSequence.readUnsignedByte();
                                    }
                                    stringBuffer.append("\t\t%" + i4);
                                    break;
                            }
                        case 21:
                        case 22:
                        case 23:
                        case 24:
                        case 25:
                            break;
                    }
                }
                int readUnsignedShort7 = byteSequence.readUnsignedShort();
                StringBuilder sb8 = new StringBuilder();
                sb8.append("\t<");
                sb8.append(constantPool.constantToString(readUnsignedShort7, (byte) 7));
                sb8.append(">");
                if (z) {
                    str = " (" + readUnsignedShort7 + ")";
                }
                sb8.append(str);
                stringBuffer.append(sb8.toString());
            }
            stringBuffer.append("\t");
            int readUnsignedShort72 = byteSequence.readUnsignedShort();
            StringBuilder sb82 = new StringBuilder();
            sb82.append("\t<");
            sb82.append(constantPool.constantToString(readUnsignedShort72, (byte) 7));
            sb82.append(">");
            if (z) {
            }
            sb82.append(str);
            stringBuffer.append(sb82.toString());
        } else {
            if (wide) {
                i3 = byteSequence.readUnsignedShort();
                s = byteSequence.readShort();
                wide = false;
            } else {
                i3 = byteSequence.readUnsignedByte();
                s = byteSequence.readByte();
            }
            stringBuffer.append("\t\t%" + i3 + "\t" + ((int) s));
        }
        return stringBuffer.toString();
    }

    public static final String codeToString(ByteSequence byteSequence, ConstantPool constantPool) throws IOException {
        return codeToString(byteSequence, constantPool, true);
    }

    public static final String compactClassName(String str) {
        return compactClassName(str, true);
    }

    public static final String compactClassName(String str, String str2, boolean z) {
        int length = str2.length();
        String replace = str.replace('/', '.');
        return (!z || !replace.startsWith(str2) || replace.substring(length).indexOf(46) != -1) ? replace : replace.substring(length);
    }

    public static final String compactClassName(String str, boolean z) {
        return compactClassName(str, "java.lang.", z);
    }

    public static final int setBit(int i, int i2) {
        return i | pow2(i2);
    }

    public static final int clearBit(int i, int i2) {
        int pow2 = pow2(i2);
        return (i & pow2) == 0 ? i : i ^ pow2;
    }

    public static final boolean isSet(int i, int i2) {
        return (i & pow2(i2)) != 0;
    }

    public static final String methodTypeToSignature(String str, String[] strArr) throws ClassFormatException {
        StringBuffer stringBuffer = new StringBuffer("(");
        if (strArr != null) {
            for (int i = 0; i < strArr.length; i++) {
                String signature = getSignature(strArr[i]);
                if (!signature.endsWith("V")) {
                    stringBuffer.append(signature);
                } else {
                    throw new ClassFormatException("Invalid type: " + strArr[i]);
                }
            }
        }
        String signature2 = getSignature(str);
        stringBuffer.append(")" + signature2);
        return stringBuffer.toString();
    }

    public static final String[] methodSignatureArgumentTypes(String str) throws ClassFormatException {
        return methodSignatureArgumentTypes(str, true);
    }

    public static final String[] methodSignatureArgumentTypes(String str, boolean z) throws ClassFormatException {
        ArrayList arrayList = new ArrayList();
        try {
            if (str.charAt(0) == '(') {
                for (int i = 1; str.charAt(i) != ')'; i += consumed_chars) {
                    arrayList.add(signatureToString(str.substring(i), z));
                }
                String[] strArr = new String[arrayList.size()];
                arrayList.toArray(strArr);
                return strArr;
            }
            throw new ClassFormatException("Invalid method signature: " + str);
        } catch (StringIndexOutOfBoundsException unused) {
            throw new ClassFormatException("Invalid method signature: " + str);
        }
    }

    public static final String methodSignatureReturnType(String str) throws ClassFormatException {
        return methodSignatureReturnType(str, true);
    }

    public static final String methodSignatureReturnType(String str, boolean z) throws ClassFormatException {
        try {
            return signatureToString(str.substring(str.lastIndexOf(41) + 1), z);
        } catch (StringIndexOutOfBoundsException unused) {
            throw new ClassFormatException("Invalid method signature: " + str);
        }
    }

    public static final String methodSignatureToString(String str, String str2, String str3) {
        return methodSignatureToString(str, str2, str3, true);
    }

    public static final String methodSignatureToString(String str, String str2, String str3, boolean z) {
        return methodSignatureToString(str, str2, str3, z, null);
    }

    public static final String methodSignatureToString(String str, String str2, String str3, boolean z, LocalVariableTable localVariableTable) throws ClassFormatException {
        String str4;
        StringBuffer stringBuffer = new StringBuffer("(");
        int i = str3.indexOf("static") >= 0 ? 0 : 1;
        try {
            if (str.charAt(0) == '(') {
                int i2 = i;
                int i3 = 1;
                while (str.charAt(i3) != ')') {
                    String signatureToString = signatureToString(str.substring(i3), z);
                    stringBuffer.append(signatureToString);
                    if (localVariableTable != null) {
                        LocalVariable localVariable = localVariableTable.getLocalVariable(i2);
                        if (localVariable != null) {
                            stringBuffer.append(" " + localVariable.getName());
                        }
                    } else {
                        stringBuffer.append(" arg" + i2);
                    }
                    i2 = ("double".equals(signatureToString) || "long".equals(signatureToString)) ? i2 + 2 : i2 + 1;
                    stringBuffer.append(", ");
                    i3 += consumed_chars;
                }
                String signatureToString2 = signatureToString(str.substring(i3 + 1), z);
                if (stringBuffer.length() > 1) {
                    stringBuffer.setLength(stringBuffer.length() - 2);
                }
                stringBuffer.append(")");
                StringBuilder sb = new StringBuilder();
                sb.append(str3);
                if (str3.length() > 0) {
                    str4 = " ";
                } else {
                    str4 = "";
                }
                sb.append(str4);
                sb.append(signatureToString2);
                sb.append(" ");
                sb.append(str2);
                sb.append(stringBuffer.toString());
                return sb.toString();
            }
            throw new ClassFormatException("Invalid method signature: " + str);
        } catch (StringIndexOutOfBoundsException unused) {
            throw new ClassFormatException("Invalid method signature: " + str);
        }
    }

    public static final String replace(String str, String str2, String str3) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            if (str.indexOf(str2) == -1) {
                return str;
            }
            int i = 0;
            while (true) {
                int indexOf = str.indexOf(str2, i);
                if (indexOf != -1) {
                    stringBuffer.append(str.substring(i, indexOf));
                    stringBuffer.append(str3);
                    i = str2.length() + indexOf;
                } else {
                    stringBuffer.append(str.substring(i));
                    return stringBuffer.toString();
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            System.err.println(e);
            return str;
        }
    }

    public static final String signatureToString(String str) {
        return signatureToString(str, true);
    }

    public static final String signatureToString(String str, boolean z) {
        consumed_chars = 1;
        int i = 0;
        try {
            char charAt = str.charAt(0);
            if (charAt == 'F') {
                return "float";
            }
            if (charAt == 'L') {
                int indexOf = str.indexOf(59);
                if (indexOf >= 0) {
                    consumed_chars = indexOf + 1;
                    return compactClassName(str.substring(1, indexOf), z);
                }
                throw new ClassFormatException("Invalid signature: " + str);
            } else if (charAt == 'S') {
                return SchemaSymbols.ATTVAL_SHORT;
            } else {
                if (charAt == 'V') {
                    return "void";
                }
                if (charAt == 'I') {
                    return "int";
                }
                if (charAt == 'J') {
                    return "long";
                }
                if (charAt == 'Z') {
                    return "boolean";
                }
                if (charAt != '[') {
                    switch (charAt) {
                        case 'B':
                            return SchemaSymbols.ATTVAL_BYTE;
                        case 'C':
                            return "char";
                        case 'D':
                            return "double";
                        default:
                            throw new ClassFormatException("Invalid signature: `" + str + "'");
                    }
                } else {
                    StringBuffer stringBuffer = new StringBuffer();
                    while (str.charAt(i) == '[') {
                        stringBuffer.append("[]");
                        i++;
                    }
                    String signatureToString = signatureToString(str.substring(i), z);
                    consumed_chars += i;
                    return signatureToString + stringBuffer.toString();
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new ClassFormatException("Invalid signature: " + e + ":" + str);
        }
    }

    public static String getSignature(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        char[] charArray = str.toCharArray();
        boolean z = false;
        int i = 0;
        boolean z2 = false;
        boolean z3 = false;
        while (true) {
            if (i >= charArray.length) {
                i = -1;
                break;
            }
            char c = charArray[i];
            if (c == '\t' || c == '\n' || c == '\f' || c == '\r' || c == ' ') {
                if (z3) {
                    z2 = true;
                }
            } else if (c != '[') {
                if (!z2) {
                    stringBuffer.append(charArray[i]);
                }
                z3 = true;
            } else if (!z3) {
                throw new RuntimeException("Illegal type: " + str);
            }
            i++;
        }
        int countBrackets = i > 0 ? countBrackets(str.substring(i)) : 0;
        String stringBuffer2 = stringBuffer.toString();
        stringBuffer.setLength(0);
        for (int i2 = 0; i2 < countBrackets; i2++) {
            stringBuffer.append('[');
        }
        for (int i3 = 4; i3 <= 12 && !z; i3++) {
            if (ohos.com.sun.org.apache.bcel.internal.Constants.TYPE_NAMES[i3].equals(stringBuffer2)) {
                stringBuffer.append(ohos.com.sun.org.apache.bcel.internal.Constants.SHORT_TYPE_NAMES[i3]);
                z = true;
            }
        }
        if (!z) {
            stringBuffer.append('L' + stringBuffer2.replace('.', '/') + ';');
        }
        return stringBuffer.toString();
    }

    private static int countBrackets(String str) {
        char[] charArray = str.toCharArray();
        boolean z = false;
        int i = 0;
        for (char c : charArray) {
            if (c != '[') {
                if (c != ']') {
                    continue;
                } else if (z) {
                    i++;
                    z = false;
                } else {
                    throw new RuntimeException("Illegally nested brackets:" + str);
                }
            } else if (!z) {
                z = true;
            } else {
                throw new RuntimeException("Illegally nested brackets:" + str);
            }
        }
        if (!z) {
            return i;
        }
        throw new RuntimeException("Illegally nested brackets:" + str);
    }

    public static final byte typeOfMethodSignature(String str) throws ClassFormatException {
        try {
            if (str.charAt(0) == '(') {
                return typeOfSignature(str.substring(str.lastIndexOf(41) + 1));
            }
            throw new ClassFormatException("Invalid method signature: " + str);
        } catch (StringIndexOutOfBoundsException unused) {
            throw new ClassFormatException("Invalid method signature: " + str);
        }
    }

    public static final byte typeOfSignature(String str) throws ClassFormatException {
        try {
            char charAt = str.charAt(0);
            if (charAt == 'F') {
                return 6;
            }
            if (charAt == 'L') {
                return 14;
            }
            if (charAt == 'S') {
                return 9;
            }
            if (charAt == 'V') {
                return 12;
            }
            if (charAt == 'I') {
                return 10;
            }
            if (charAt == 'J') {
                return 11;
            }
            if (charAt == 'Z') {
                return 4;
            }
            if (charAt == '[') {
                return 13;
            }
            switch (charAt) {
                case 'B':
                    return 8;
                case 'C':
                    return 5;
                case 'D':
                    return 7;
                default:
                    throw new ClassFormatException("Invalid method signature: " + str);
            }
        } catch (StringIndexOutOfBoundsException unused) {
            throw new ClassFormatException("Invalid method signature: " + str);
        }
    }

    public static short searchOpcode(String str) {
        String lowerCase = str.toLowerCase();
        for (short s = 0; s < ohos.com.sun.org.apache.bcel.internal.Constants.OPCODE_NAMES.length; s = (short) (s + 1)) {
            if (ohos.com.sun.org.apache.bcel.internal.Constants.OPCODE_NAMES[s].equals(lowerCase)) {
                return s;
            }
        }
        return -1;
    }

    public static final String toHexString(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bArr.length; i++) {
            short byteToShort = byteToShort(bArr[i]);
            String num = Integer.toString(byteToShort, 16);
            if (byteToShort < 16) {
                stringBuffer.append('0');
            }
            stringBuffer.append(num);
            if (i < bArr.length - 1) {
                stringBuffer.append(' ');
            }
        }
        return stringBuffer.toString();
    }

    public static final String format(int i, int i2, boolean z, char c) {
        return fillup(Integer.toString(i), i2, z, c);
    }

    public static final String fillup(String str, int i, boolean z, char c) {
        int length = i - str.length();
        if (length < 0) {
            length = 0;
        }
        char[] cArr = new char[length];
        for (int i2 = 0; i2 < cArr.length; i2++) {
            cArr[i2] = c;
        }
        if (z) {
            return str + new String(cArr);
        }
        return new String(cArr) + str;
    }

    static final boolean equals(byte[] bArr, byte[] bArr2) {
        int length = bArr.length;
        if (length != bArr2.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (bArr[i] != bArr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static final void printArray(PrintStream printStream, Object[] objArr) {
        printStream.println(printArray(objArr, true));
    }

    public static final void printArray(PrintWriter printWriter, Object[] objArr) {
        printWriter.println(printArray(objArr, true));
    }

    public static final String printArray(Object[] objArr) {
        return printArray(objArr, true);
    }

    public static final String printArray(Object[] objArr, boolean z) {
        return printArray(objArr, z, false);
    }

    public static final String printArray(Object[] objArr, boolean z, boolean z2) {
        if (objArr == null) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        if (z) {
            stringBuffer.append('{');
        }
        for (int i = 0; i < objArr.length; i++) {
            if (objArr[i] != null) {
                StringBuilder sb = new StringBuilder();
                String str = "\"";
                sb.append(z2 ? str : "");
                sb.append(objArr[i].toString());
                if (!z2) {
                    str = "";
                }
                sb.append(str);
                stringBuffer.append(sb.toString());
            } else {
                stringBuffer.append("null");
            }
            if (i < objArr.length - 1) {
                stringBuffer.append(", ");
            }
        }
        if (z) {
            stringBuffer.append('}');
        }
        return stringBuffer.toString();
    }

    public static String encode(byte[] bArr, boolean z) throws IOException {
        if (z) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gZIPOutputStream.write(bArr, 0, bArr.length);
            gZIPOutputStream.close();
            byteArrayOutputStream.close();
            bArr = byteArrayOutputStream.toByteArray();
        }
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        JavaWriter javaWriter = new JavaWriter(charArrayWriter);
        for (byte b : bArr) {
            javaWriter.write(b & 255);
        }
        return charArrayWriter.toString();
    }

    public static byte[] decode(String str, boolean z) throws IOException {
        CharArrayReader charArrayReader = new CharArrayReader(str.toCharArray());
        JavaReader javaReader = new JavaReader(charArrayReader);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int read = javaReader.read();
            if (read < 0) {
                break;
            }
            byteArrayOutputStream.write(read);
        }
        byteArrayOutputStream.close();
        charArrayReader.close();
        javaReader.close();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        if (!z) {
            return byteArray;
        }
        GZIPInputStream gZIPInputStream = new GZIPInputStream(new ByteArrayInputStream(byteArray));
        byte[] bArr = new byte[(byteArray.length * 3)];
        int i = 0;
        while (true) {
            int read2 = gZIPInputStream.read();
            if (read2 >= 0) {
                bArr[i] = (byte) read2;
                i++;
            } else {
                byte[] bArr2 = new byte[i];
                System.arraycopy(bArr, 0, bArr2, 0, i);
                return bArr2;
            }
        }
    }

    static {
        int i = 0;
        for (int i2 = 65; i2 <= 90; i2++) {
            CHAR_MAP[i] = i2;
            MAP_CHAR[i2] = i;
            i++;
        }
        for (int i3 = 103; i3 <= 122; i3++) {
            CHAR_MAP[i] = i3;
            MAP_CHAR[i3] = i;
            i++;
        }
        int[] iArr = CHAR_MAP;
        iArr[i] = 36;
        int[] iArr2 = MAP_CHAR;
        iArr2[36] = i;
        int i4 = i + 1;
        iArr[i4] = 95;
        iArr2[95] = i4;
    }

    private static class JavaReader extends FilterReader {
        public JavaReader(Reader reader) {
            super(reader);
        }

        @Override // java.io.FilterReader, java.io.Reader
        public int read() throws IOException {
            int read = this.in.read();
            if (read != 36) {
                return read;
            }
            int read2 = this.in.read();
            if (read2 < 0) {
                return -1;
            }
            if ((read2 < 48 || read2 > 57) && (read2 < 97 || read2 > 102)) {
                return Utility.MAP_CHAR[read2];
            }
            int read3 = this.in.read();
            if (read3 < 0) {
                return -1;
            }
            return Integer.parseInt(new String(new char[]{(char) read2, (char) read3}), 16);
        }

        @Override // java.io.FilterReader, java.io.Reader
        public int read(char[] cArr, int i, int i2) throws IOException {
            for (int i3 = 0; i3 < i2; i3++) {
                cArr[i + i3] = (char) read();
            }
            return i2;
        }
    }

    private static class JavaWriter extends FilterWriter {
        public JavaWriter(Writer writer) {
            super(writer);
        }

        @Override // java.io.FilterWriter, java.io.Writer
        public void write(int i) throws IOException {
            if (!Utility.isJavaIdentifierPart((char) i) || i == 36) {
                this.out.write(36);
                if (i < 0 || i >= 48) {
                    char[] charArray = Integer.toHexString(i).toCharArray();
                    if (charArray.length == 1) {
                        this.out.write(48);
                        this.out.write(charArray[0]);
                        return;
                    }
                    this.out.write(charArray[0]);
                    this.out.write(charArray[1]);
                    return;
                }
                this.out.write(Utility.CHAR_MAP[i]);
                return;
            }
            this.out.write(i);
        }

        @Override // java.io.FilterWriter, java.io.Writer
        public void write(char[] cArr, int i, int i2) throws IOException {
            for (int i3 = 0; i3 < i2; i3++) {
                write(cArr[i + i3]);
            }
        }

        @Override // java.io.FilterWriter, java.io.Writer
        public void write(String str, int i, int i2) throws IOException {
            write(str.toCharArray(), i, i2);
        }
    }

    public static final String convertString(String str) {
        char[] charArray = str.toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '\n') {
                stringBuffer.append("\\n");
            } else if (c == '\r') {
                stringBuffer.append("\\r");
            } else if (c == '\"') {
                stringBuffer.append("\\\"");
            } else if (c == '\'') {
                stringBuffer.append("\\'");
            } else if (c != '\\') {
                stringBuffer.append(charArray[i]);
            } else {
                stringBuffer.append("\\\\");
            }
        }
        return stringBuffer.toString();
    }
}
