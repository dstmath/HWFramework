package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.Attribute;
import ohos.com.sun.org.apache.bcel.internal.classfile.Code;
import ohos.com.sun.org.apache.bcel.internal.classfile.CodeException;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantUtf8;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantValue;
import ohos.com.sun.org.apache.bcel.internal.classfile.ExceptionTable;
import ohos.com.sun.org.apache.bcel.internal.classfile.InnerClass;
import ohos.com.sun.org.apache.bcel.internal.classfile.InnerClasses;
import ohos.com.sun.org.apache.bcel.internal.classfile.LineNumber;
import ohos.com.sun.org.apache.bcel.internal.classfile.LineNumberTable;
import ohos.com.sun.org.apache.bcel.internal.classfile.LocalVariable;
import ohos.com.sun.org.apache.bcel.internal.classfile.LocalVariableTable;
import ohos.com.sun.org.apache.bcel.internal.classfile.SourceFile;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;

/* access modifiers changed from: package-private */
public final class AttributeHTML implements Constants {
    private int attr_count = 0;
    private String class_name;
    private ConstantHTML constant_html;
    private ConstantPool constant_pool;
    private PrintWriter file;

    AttributeHTML(String str, String str2, ConstantPool constantPool, ConstantHTML constantHTML) throws IOException {
        this.class_name = str2;
        this.constant_pool = constantPool;
        this.constant_html = constantHTML;
        this.file = new PrintWriter(new FileOutputStream(str + str2 + "_attributes.html"));
        this.file.println("<HTML><BODY BGCOLOR=\"#C0C0C0\"><TABLE BORDER=0>");
    }

    private final String codeLink(int i, int i2) {
        return "<A HREF=\"" + this.class_name + "_code.html#code" + i2 + "@" + i + "\" TARGET=Code>" + i + "</A>";
    }

    /* access modifiers changed from: package-private */
    public final void close() {
        this.file.println("</TABLE></BODY></HTML>");
        this.file.close();
    }

    /* access modifiers changed from: package-private */
    public final void writeAttribute(Attribute attribute, String str) throws IOException {
        writeAttribute(attribute, str, 0);
    }

    /* access modifiers changed from: package-private */
    public final void writeAttribute(Attribute attribute, String str, int i) throws IOException {
        byte tag = attribute.getTag();
        if (tag != -1) {
            this.attr_count++;
            if (this.attr_count % 2 == 0) {
                this.file.print("<TR BGCOLOR=\"#C0C0C0\"><TD>");
            } else {
                this.file.print("<TR BGCOLOR=\"#A0A0A0\"><TD>");
            }
            this.file.println("<H4><A NAME=\"" + str + "\">" + this.attr_count + " " + ATTRIBUTE_NAMES[tag] + "</A></H4>");
            int i2 = 0;
            switch (tag) {
                case 0:
                    int sourceFileIndex = ((SourceFile) attribute).getSourceFileIndex();
                    this.file.print("<UL><LI><A HREF=\"" + this.class_name + "_cp.html#cp" + sourceFileIndex + "\" TARGET=\"ConstantPool\">Source file index(" + sourceFileIndex + ")</A></UL>\n");
                    break;
                case 1:
                    int constantValueIndex = ((ConstantValue) attribute).getConstantValueIndex();
                    this.file.print("<UL><LI><A HREF=\"" + this.class_name + "_cp.html#cp" + constantValueIndex + "\" TARGET=\"ConstantPool\">Constant value index(" + constantValueIndex + ")</A></UL>\n");
                    break;
                case 2:
                    Code code = (Code) attribute;
                    this.file.print("<UL><LI>Maximum stack size = " + code.getMaxStack() + "</LI>\n<LI>Number of local variables = " + code.getMaxLocals() + "</LI>\n<LI><A HREF=\"" + this.class_name + "_code.html#method" + i + "\" TARGET=Code>Byte code</A></LI></UL>\n");
                    CodeException[] exceptionTable = code.getExceptionTable();
                    int length = exceptionTable.length;
                    if (length > 0) {
                        this.file.print("<P><B>Exceptions handled</B><UL>");
                        while (i2 < length) {
                            int catchType = exceptionTable[i2].getCatchType();
                            this.file.print("<LI>");
                            if (catchType != 0) {
                                this.file.print(this.constant_html.referenceConstant(catchType));
                            } else {
                                this.file.print("Any Exception");
                            }
                            this.file.print("<BR>(Ranging from lines " + codeLink(exceptionTable[i2].getStartPC(), i) + " to " + codeLink(exceptionTable[i2].getEndPC(), i) + ", handled at line " + codeLink(exceptionTable[i2].getHandlerPC(), i) + ")</LI>");
                            i2++;
                        }
                        this.file.print("</UL>");
                        break;
                    }
                    break;
                case 3:
                    int[] exceptionIndexTable = ((ExceptionTable) attribute).getExceptionIndexTable();
                    this.file.print("<UL>");
                    while (i2 < exceptionIndexTable.length) {
                        this.file.print("<LI><A HREF=\"" + this.class_name + "_cp.html#cp" + exceptionIndexTable[i2] + "\" TARGET=\"ConstantPool\">Exception class index(" + exceptionIndexTable[i2] + ")</A>\n");
                        i2++;
                    }
                    this.file.print("</UL>\n");
                    break;
                case 4:
                    LineNumber[] lineNumberTable = ((LineNumberTable) attribute).getLineNumberTable();
                    this.file.print("<P>");
                    while (i2 < lineNumberTable.length) {
                        this.file.print("(" + lineNumberTable[i2].getStartPC() + ",&nbsp;" + lineNumberTable[i2].getLineNumber() + ")");
                        if (i2 < lineNumberTable.length - 1) {
                            this.file.print(", ");
                        }
                        i2++;
                    }
                    break;
                case 5:
                    LocalVariable[] localVariableTable = ((LocalVariableTable) attribute).getLocalVariableTable();
                    this.file.print("<UL>");
                    for (int i3 = 0; i3 < localVariableTable.length; i3++) {
                        String signatureToString = Utility.signatureToString(((ConstantUtf8) this.constant_pool.getConstant(localVariableTable[i3].getSignatureIndex(), (byte) 1)).getBytes(), false);
                        int startPC = localVariableTable[i3].getStartPC();
                        int length2 = localVariableTable[i3].getLength() + startPC;
                        this.file.println("<LI>" + Class2HTML.referenceType(signatureToString) + "&nbsp;<B>" + localVariableTable[i3].getName() + "</B> in slot %" + localVariableTable[i3].getIndex() + "<BR>Valid from lines <A HREF=\"" + this.class_name + "_code.html#code" + i + "@" + startPC + "\" TARGET=Code>" + startPC + "</A> to <A HREF=\"" + this.class_name + "_code.html#code" + i + "@" + length2 + "\" TARGET=Code>" + length2 + "</A></LI>");
                    }
                    this.file.print("</UL>\n");
                    break;
                case 6:
                    InnerClass[] innerClasses = ((InnerClasses) attribute).getInnerClasses();
                    this.file.print("<UL>");
                    while (i2 < innerClasses.length) {
                        int innerNameIndex = innerClasses[i2].getInnerNameIndex();
                        String bytes = innerNameIndex > 0 ? ((ConstantUtf8) this.constant_pool.getConstant(innerNameIndex, (byte) 1)).getBytes() : "&lt;anonymous&gt;";
                        String accessToString = Utility.accessToString(innerClasses[i2].getInnerAccessFlags());
                        this.file.print("<LI><FONT COLOR=\"#FF0000\">" + accessToString + "</FONT> " + this.constant_html.referenceConstant(innerClasses[i2].getInnerClassIndex()) + " in&nbsp;class " + this.constant_html.referenceConstant(innerClasses[i2].getOuterClassIndex()) + " named " + bytes + "</LI>\n");
                        i2++;
                    }
                    this.file.print("</UL>\n");
                    break;
                default:
                    this.file.print("<P>" + attribute.toString());
                    break;
            }
            this.file.println("</TD></TR>");
            this.file.flush();
        }
    }
}
