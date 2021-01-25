package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.Attribute;
import ohos.com.sun.org.apache.bcel.internal.classfile.Code;
import ohos.com.sun.org.apache.bcel.internal.classfile.CodeException;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantFieldref;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantInterfaceMethodref;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantMethodref;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantNameAndType;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.classfile.LocalVariable;
import ohos.com.sun.org.apache.bcel.internal.classfile.LocalVariableTable;
import ohos.com.sun.org.apache.bcel.internal.classfile.Method;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;

/* access modifiers changed from: package-private */
public final class CodeHTML implements Constants {
    private static boolean wide = false;
    private String class_name;
    private ConstantHTML constant_html;
    private ConstantPool constant_pool;
    private PrintWriter file;
    private BitSet goto_set;
    private Method[] methods;

    CodeHTML(String str, String str2, Method[] methodArr, ConstantPool constantPool, ConstantHTML constantHTML) throws IOException {
        this.class_name = str2;
        this.methods = methodArr;
        this.constant_pool = constantPool;
        this.constant_html = constantHTML;
        this.file = new PrintWriter(new FileOutputStream(str + str2 + "_code.html"));
        this.file.println("<HTML><BODY BGCOLOR=\"#C0C0C0\">");
        for (int i = 0; i < methodArr.length; i++) {
            writeMethod(methodArr[i], i);
        }
        this.file.println("</BODY></HTML>");
        this.file.close();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x03cc  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x03f9 A[FALL_THROUGH] */
    private final String codeToHTML(ByteSequence byteSequence, int i) throws IOException {
        int i2;
        int i3;
        short s;
        int i4;
        int i5;
        int i6;
        int i7;
        short readUnsignedByte = (short) byteSequence.readUnsignedByte();
        StringBuffer stringBuffer = new StringBuffer("<TT>" + OPCODE_NAMES[readUnsignedByte] + "</TT></TD><TD>");
        if (readUnsignedByte == 170 || readUnsignedByte == 171) {
            int index = byteSequence.getIndex() % 4;
            i3 = index == 0 ? 0 : 4 - index;
            for (int i8 = 0; i8 < i3; i8++) {
                byteSequence.readByte();
            }
            i2 = byteSequence.readInt();
        } else {
            i3 = 0;
            i2 = 0;
        }
        if (readUnsignedByte != 132) {
            if (!(readUnsignedByte == 192 || readUnsignedByte == 193)) {
                switch (readUnsignedByte) {
                    case 18:
                        int readUnsignedByte2 = byteSequence.readUnsignedByte();
                        StringBuilder sb = new StringBuilder();
                        sb.append("<A HREF=\"");
                        sb.append(this.class_name);
                        sb.append("_cp.html#cp");
                        sb.append(readUnsignedByte2);
                        sb.append("\" TARGET=\"ConstantPool\">");
                        ConstantPool constantPool = this.constant_pool;
                        sb.append(Class2HTML.toHTML(constantPool.constantToString(readUnsignedByte2, constantPool.getConstant(readUnsignedByte2).getTag())));
                        sb.append("</a>");
                        stringBuffer.append(sb.toString());
                        break;
                    case 19:
                    case 20:
                        short readShort = byteSequence.readShort();
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("<A HREF=\"");
                        sb2.append(this.class_name);
                        sb2.append("_cp.html#cp");
                        sb2.append((int) readShort);
                        sb2.append("\" TARGET=\"ConstantPool\">");
                        ConstantPool constantPool2 = this.constant_pool;
                        sb2.append(Class2HTML.toHTML(constantPool2.constantToString(readShort, constantPool2.getConstant(readShort).getTag())));
                        sb2.append("</a>");
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
                                        int index2 = (byteSequence.getIndex() + byteSequence.readShort()) - 1;
                                        stringBuffer.append("<A HREF=\"#code" + i + "@" + index2 + "\">" + index2 + "</A>");
                                        break;
                                    case 169:
                                        break;
                                    case 170:
                                        int readInt = byteSequence.readInt();
                                        int readInt2 = byteSequence.readInt();
                                        int index3 = ((byteSequence.getIndex() - 12) - i3) - 1;
                                        int i9 = i2 + index3;
                                        stringBuffer.append("<TABLE BORDER=1><TR>");
                                        int[] iArr = new int[((readInt2 - readInt) + 1)];
                                        for (int i10 = 0; i10 < iArr.length; i10++) {
                                            iArr[i10] = byteSequence.readInt() + index3;
                                            stringBuffer.append("<TH>" + (readInt + i10) + "</TH>");
                                        }
                                        stringBuffer.append("<TH>default</TH></TR>\n<TR>");
                                        for (int i11 = 0; i11 < iArr.length; i11++) {
                                            stringBuffer.append("<TD><A HREF=\"#code" + i + "@" + iArr[i11] + "\">" + iArr[i11] + "</A></TD>");
                                        }
                                        stringBuffer.append("<TD><A HREF=\"#code" + i + "@" + i9 + "\">" + i9 + "</A></TD></TR>\n</TABLE>\n");
                                        break;
                                    case 171:
                                        int readInt3 = byteSequence.readInt();
                                        int index4 = ((byteSequence.getIndex() - 8) - i3) - 1;
                                        int[] iArr2 = new int[readInt3];
                                        int i12 = i2 + index4;
                                        stringBuffer.append("<TABLE BORDER=1><TR>");
                                        for (int i13 = 0; i13 < readInt3; i13++) {
                                            int readInt4 = byteSequence.readInt();
                                            iArr2[i13] = byteSequence.readInt() + index4;
                                            stringBuffer.append("<TH>" + readInt4 + "</TH>");
                                        }
                                        stringBuffer.append("<TH>default</TH></TR>\n<TR>");
                                        for (int i14 = 0; i14 < readInt3; i14++) {
                                            stringBuffer.append("<TD><A HREF=\"#code" + i + "@" + iArr2[i14] + "\">" + iArr2[i14] + "</A></TD>");
                                        }
                                        stringBuffer.append("<TD><A HREF=\"#code" + i + "@" + i12 + "\">" + i12 + "</A></TD></TR>\n</TABLE>\n");
                                        break;
                                    default:
                                        switch (readUnsignedByte) {
                                            case 178:
                                            case 179:
                                            case 180:
                                            case 181:
                                                ConstantFieldref constantFieldref = (ConstantFieldref) this.constant_pool.getConstant(byteSequence.readShort(), (byte) 9);
                                                int classIndex = constantFieldref.getClassIndex();
                                                String compactClassName = Utility.compactClassName(this.constant_pool.getConstantString(classIndex, (byte) 7), false);
                                                String constantToString = this.constant_pool.constantToString(constantFieldref.getNameAndTypeIndex(), (byte) 12);
                                                if (compactClassName.equals(this.class_name)) {
                                                    stringBuffer.append("<A HREF=\"" + this.class_name + "_methods.html#field" + constantToString + "\" TARGET=Methods>" + constantToString + "</A>\n");
                                                    break;
                                                } else {
                                                    stringBuffer.append(this.constant_html.referenceConstant(classIndex) + "." + constantToString);
                                                    break;
                                                }
                                            case 182:
                                            case 183:
                                            case 184:
                                            case 185:
                                                short readShort2 = byteSequence.readShort();
                                                if (readUnsignedByte == 185) {
                                                    byteSequence.readUnsignedByte();
                                                    byteSequence.readUnsignedByte();
                                                    ConstantInterfaceMethodref constantInterfaceMethodref = (ConstantInterfaceMethodref) this.constant_pool.getConstant(readShort2, (byte) 11);
                                                    i6 = constantInterfaceMethodref.getClassIndex();
                                                    this.constant_pool.constantToString(constantInterfaceMethodref);
                                                    i7 = constantInterfaceMethodref.getNameAndTypeIndex();
                                                } else {
                                                    ConstantMethodref constantMethodref = (ConstantMethodref) this.constant_pool.getConstant(readShort2, (byte) 10);
                                                    i6 = constantMethodref.getClassIndex();
                                                    this.constant_pool.constantToString(constantMethodref);
                                                    i7 = constantMethodref.getNameAndTypeIndex();
                                                }
                                                String referenceClass = Class2HTML.referenceClass(i6);
                                                ConstantPool constantPool3 = this.constant_pool;
                                                String html = Class2HTML.toHTML(constantPool3.constantToString(constantPool3.getConstant(i7, (byte) 12)));
                                                String constantToString2 = this.constant_pool.constantToString(((ConstantNameAndType) this.constant_pool.getConstant(i7, (byte) 12)).getSignatureIndex(), (byte) 1);
                                                String[] methodSignatureArgumentTypes = Utility.methodSignatureArgumentTypes(constantToString2, false);
                                                String methodSignatureReturnType = Utility.methodSignatureReturnType(constantToString2, false);
                                                stringBuffer.append(referenceClass + ".<A HREF=\"" + this.class_name + "_cp.html#cp" + ((int) readShort2) + "\" TARGET=ConstantPool>" + html + "</A>(");
                                                for (int i15 = 0; i15 < methodSignatureArgumentTypes.length; i15++) {
                                                    stringBuffer.append(Class2HTML.referenceType(methodSignatureArgumentTypes[i15]));
                                                    if (i15 < methodSignatureArgumentTypes.length - 1) {
                                                        stringBuffer.append(", ");
                                                    }
                                                }
                                                stringBuffer.append("):" + Class2HTML.referenceType(methodSignatureReturnType));
                                                break;
                                            default:
                                                switch (readUnsignedByte) {
                                                    case 187:
                                                        break;
                                                    case 188:
                                                        stringBuffer.append("<FONT COLOR=\"#00FF00\">" + TYPE_NAMES[byteSequence.readByte()] + "</FONT>");
                                                        break;
                                                    case 189:
                                                        stringBuffer.append(this.constant_html.referenceConstant(byteSequence.readShort()));
                                                        break;
                                                    default:
                                                        switch (readUnsignedByte) {
                                                            case 196:
                                                                wide = true;
                                                                stringBuffer.append("(wide)");
                                                                break;
                                                            case 197:
                                                                short readShort3 = byteSequence.readShort();
                                                                byte readByte = byteSequence.readByte();
                                                                stringBuffer.append(this.constant_html.referenceConstant(readShort3) + ":" + ((int) readByte) + "-dimensional");
                                                                break;
                                                            case 198:
                                                            case 199:
                                                                break;
                                                            case 200:
                                                            case 201:
                                                                int index5 = (byteSequence.getIndex() + byteSequence.readInt()) - 1;
                                                                stringBuffer.append("<A HREF=\"#code" + i + "@" + index5 + "\">" + index5 + "</A>");
                                                                break;
                                                            default:
                                                                if (NO_OF_OPERANDS[readUnsignedByte] > 0) {
                                                                    for (int i16 = 0; i16 < TYPE_OF_OPERANDS[readUnsignedByte].length; i16++) {
                                                                        switch (TYPE_OF_OPERANDS[readUnsignedByte][i16]) {
                                                                            case 8:
                                                                                stringBuffer.append(byteSequence.readUnsignedByte());
                                                                                break;
                                                                            case 9:
                                                                                stringBuffer.append((int) byteSequence.readShort());
                                                                                break;
                                                                            case 10:
                                                                                stringBuffer.append(byteSequence.readInt());
                                                                                break;
                                                                            default:
                                                                                System.err.println("Unreachable default case reached!");
                                                                                System.exit(-1);
                                                                                break;
                                                                        }
                                                                        stringBuffer.append("&nbsp;");
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
                                    i5 = byteSequence.readShort();
                                    wide = false;
                                } else {
                                    i5 = byteSequence.readUnsignedByte();
                                }
                                stringBuffer.append("%" + i5);
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
            stringBuffer.append(this.constant_html.referenceConstant(byteSequence.readShort()));
        } else {
            if (wide) {
                i4 = byteSequence.readShort();
                s = byteSequence.readShort();
                wide = false;
            } else {
                i4 = byteSequence.readUnsignedByte();
                s = byteSequence.readByte();
            }
            stringBuffer.append("%" + i4 + " " + ((int) s));
        }
        stringBuffer.append("</TD>");
        return stringBuffer.toString();
    }

    private final void findGotos(ByteSequence byteSequence, Method method, Code code) throws IOException {
        this.goto_set = new BitSet(byteSequence.available());
        if (code != null) {
            CodeException[] exceptionTable = code.getExceptionTable();
            int length = exceptionTable.length;
            for (int i = 0; i < length; i++) {
                this.goto_set.set(exceptionTable[i].getStartPC());
                this.goto_set.set(exceptionTable[i].getEndPC());
                this.goto_set.set(exceptionTable[i].getHandlerPC());
            }
            Attribute[] attributes = code.getAttributes();
            int i2 = 0;
            while (true) {
                if (i2 >= attributes.length) {
                    break;
                } else if (attributes[i2].getTag() == 5) {
                    LocalVariable[] localVariableTable = ((LocalVariableTable) attributes[i2]).getLocalVariableTable();
                    for (int i3 = 0; i3 < localVariableTable.length; i3++) {
                        int startPC = localVariableTable[i3].getStartPC();
                        int length2 = localVariableTable[i3].getLength() + startPC;
                        this.goto_set.set(startPC);
                        this.goto_set.set(length2);
                    }
                } else {
                    i2++;
                }
            }
        }
        while (byteSequence.available() > 0) {
            int readUnsignedByte = byteSequence.readUnsignedByte();
            if (readUnsignedByte != 170 && readUnsignedByte != 171) {
                switch (readUnsignedByte) {
                    default:
                        switch (readUnsignedByte) {
                            case 198:
                            case 199:
                                break;
                            case 200:
                            case 201:
                                this.goto_set.set((byteSequence.getIndex() + byteSequence.readInt()) - 1);
                                break;
                            default:
                                byteSequence.unreadByte();
                                codeToHTML(byteSequence, 0);
                                break;
                        }
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
                        this.goto_set.set((byteSequence.getIndex() + byteSequence.readShort()) - 1);
                        break;
                }
            } else {
                int index = byteSequence.getIndex() % 4;
                int i4 = index == 0 ? 0 : 4 - index;
                for (int i5 = 0; i5 < i4; i5++) {
                    byteSequence.readByte();
                }
                int readInt = byteSequence.readInt();
                if (readUnsignedByte == 170) {
                    int readInt2 = byteSequence.readInt();
                    int readInt3 = byteSequence.readInt();
                    int index2 = ((byteSequence.getIndex() - 12) - i4) - 1;
                    this.goto_set.set(readInt + index2);
                    for (int i6 = 0; i6 < (readInt3 - readInt2) + 1; i6++) {
                        this.goto_set.set(byteSequence.readInt() + index2);
                    }
                } else {
                    int readInt4 = byteSequence.readInt();
                    int index3 = ((byteSequence.getIndex() - 8) - i4) - 1;
                    this.goto_set.set(readInt + index3);
                    for (int i7 = 0; i7 < readInt4; i7++) {
                        byteSequence.readInt();
                        this.goto_set.set(byteSequence.readInt() + index3);
                    }
                }
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:45:0x0184 */
    private void writeMethod(Method method, int i) throws IOException {
        Code code;
        byte[] bArr;
        Attribute[] attributeArr;
        Code code2;
        String signature = method.getSignature();
        String[] methodSignatureArgumentTypes = Utility.methodSignatureArgumentTypes(signature, false);
        String methodSignatureReturnType = Utility.methodSignatureReturnType(signature, false);
        String html = Class2HTML.toHTML(method.getName());
        String replace = Utility.replace(Utility.accessToString(method.getAccessFlags()), " ", "&nbsp;");
        Attribute[] attributes = method.getAttributes();
        this.file.print("<P><B><FONT COLOR=\"#FF0000\">" + replace + "</FONT>&nbsp;<A NAME=method" + i + ">" + Class2HTML.referenceType(methodSignatureReturnType) + "</A>&nbsp<A HREF=\"" + this.class_name + "_methods.html#method" + i + "\" TARGET=Methods>" + html + "</A>(");
        for (int i2 = 0; i2 < methodSignatureArgumentTypes.length; i2++) {
            this.file.print(Class2HTML.referenceType(methodSignatureArgumentTypes[i2]));
            if (i2 < methodSignatureArgumentTypes.length - 1) {
                this.file.print(",&nbsp;");
            }
        }
        this.file.println(")</B></P>");
        byte[] bArr2 = 0;
        if (attributes.length > 0) {
            this.file.print("<H4>Attributes</H4><UL>\n");
            int i3 = 0;
            byte[] bArr3 = null;
            while (i3 < attributes.length) {
                byte tag = attributes[i3].getTag();
                if (tag != -1) {
                    PrintWriter printWriter = this.file;
                    StringBuilder sb = new StringBuilder();
                    sb.append("<LI><A HREF=\"");
                    bArr = bArr2;
                    sb.append(this.class_name);
                    sb.append("_attributes.html#method");
                    sb.append(i);
                    sb.append("@");
                    sb.append(i3);
                    sb.append("\" TARGET=Attributes>");
                    sb.append(ATTRIBUTE_NAMES[tag]);
                    sb.append("</A></LI>\n");
                    printWriter.print(sb.toString());
                } else {
                    bArr = bArr2;
                    this.file.print("<LI>" + attributes[i3] + "</LI>");
                }
                if (tag == 2) {
                    Code code3 = (Code) attributes[i3];
                    Attribute[] attributes2 = code3.getAttributes();
                    bArr3 = code3.getCode();
                    this.file.print("<UL>");
                    int i4 = 0;
                    code2 = code3;
                    while (i4 < attributes2.length) {
                        byte tag2 = attributes2[i4].getTag();
                        this.file.print("<LI><A HREF=\"" + this.class_name + "_attributes.html#method" + i + "@" + i3 + "@" + i4 + "\" TARGET=Attributes>" + ATTRIBUTE_NAMES[tag2] + "</A></LI>\n");
                        i4++;
                        attributes2 = attributes2;
                        code2 = code2;
                        attributes = attributes;
                    }
                    attributeArr = attributes;
                    this.file.print("</UL>");
                } else {
                    attributeArr = attributes;
                    code2 = bArr;
                }
                i3++;
                attributes = attributeArr;
                bArr2 = code2;
            }
            this.file.println("</UL>");
            bArr2 = bArr3;
            code = bArr2;
        } else {
            code = null;
        }
        if (bArr2 != null) {
            ByteSequence byteSequence = new ByteSequence(bArr2);
            byteSequence.mark(byteSequence.available());
            findGotos(byteSequence, method, code);
            byteSequence.reset();
            this.file.println("<TABLE BORDER=0><TR><TH ALIGN=LEFT>Byte<BR>offset</TH><TH ALIGN=LEFT>Instruction</TH><TH ALIGN=LEFT>Argument</TH>");
            while (byteSequence.available() > 0) {
                int index = byteSequence.getIndex();
                String codeToHTML = codeToHTML(byteSequence, i);
                String str = this.goto_set.get(index) ? "<A NAME=code" + i + "@" + index + "></A>" : "";
                this.file.println("<TR VALIGN=TOP><TD>" + (byteSequence.getIndex() == bArr2.length ? "<A NAME=code" + i + "@" + bArr2.length + ">" + index + "</A>" : "" + index) + "</TD><TD>" + str + codeToHTML + "</TR>");
            }
            this.file.println("<TR><TD> </A></TD></TR>");
            this.file.println("</TABLE>");
        }
    }
}
