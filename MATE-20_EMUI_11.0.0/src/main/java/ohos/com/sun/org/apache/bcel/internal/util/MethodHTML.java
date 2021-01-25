package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.Attribute;
import ohos.com.sun.org.apache.bcel.internal.classfile.Code;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantValue;
import ohos.com.sun.org.apache.bcel.internal.classfile.ExceptionTable;
import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.classfile.Method;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;

/* access modifiers changed from: package-private */
public final class MethodHTML implements Constants {
    private AttributeHTML attribute_html;
    private String class_name;
    private ConstantHTML constant_html;
    private PrintWriter file;

    MethodHTML(String str, String str2, Method[] methodArr, Field[] fieldArr, ConstantHTML constantHTML, AttributeHTML attributeHTML) throws IOException {
        this.class_name = str2;
        this.attribute_html = attributeHTML;
        this.constant_html = constantHTML;
        this.file = new PrintWriter(new FileOutputStream(str + str2 + "_methods.html"));
        this.file.println("<HTML><BODY BGCOLOR=\"#C0C0C0\"><TABLE BORDER=0>");
        this.file.println("<TR><TH ALIGN=LEFT>Access&nbsp;flags</TH><TH ALIGN=LEFT>Type</TH><TH ALIGN=LEFT>Field&nbsp;name</TH></TR>");
        for (int i = 0; i < fieldArr.length; i++) {
            writeField(fieldArr[i]);
        }
        this.file.println("</TABLE>");
        this.file.println("<TABLE BORDER=0><TR><TH ALIGN=LEFT>Access&nbsp;flags</TH><TH ALIGN=LEFT>Return&nbsp;type</TH><TH ALIGN=LEFT>Method&nbsp;name</TH><TH ALIGN=LEFT>Arguments</TH></TR>");
        for (int i2 = 0; i2 < methodArr.length; i2++) {
            writeMethod(methodArr[i2], i2);
        }
        this.file.println("</TABLE></BODY></HTML>");
        this.file.close();
    }

    private void writeField(Field field) throws IOException {
        String signatureToString = Utility.signatureToString(field.getSignature());
        String name = field.getName();
        String replace = Utility.replace(Utility.accessToString(field.getAccessFlags()), " ", "&nbsp;");
        PrintWriter printWriter = this.file;
        printWriter.print("<TR><TD><FONT COLOR=\"#FF0000\">" + replace + "</FONT></TD>\n<TD>" + Class2HTML.referenceType(signatureToString) + "</TD><TD><A NAME=\"field" + name + "\">" + name + "</A></TD>");
        Attribute[] attributes = field.getAttributes();
        int i = 0;
        for (int i2 = 0; i2 < attributes.length; i2++) {
            AttributeHTML attributeHTML = this.attribute_html;
            Attribute attribute = attributes[i2];
            attributeHTML.writeAttribute(attribute, name + "@" + i2);
        }
        while (true) {
            if (i >= attributes.length) {
                break;
            } else if (attributes[i].getTag() == 1) {
                String constantValue = ((ConstantValue) attributes[i]).toString();
                PrintWriter printWriter2 = this.file;
                printWriter2.print("<TD>= <A HREF=\"" + this.class_name + "_attributes.html#" + name + "@" + i + "\" TARGET=\"Attributes\">" + constantValue + "</TD>\n");
                break;
            } else {
                i++;
            }
        }
        this.file.println("</TR>");
    }

    private final void writeMethod(Method method, int i) throws IOException {
        String signature = method.getSignature();
        String[] methodSignatureArgumentTypes = Utility.methodSignatureArgumentTypes(signature, false);
        String methodSignatureReturnType = Utility.methodSignatureReturnType(signature, false);
        String name = method.getName();
        String accessToString = Utility.accessToString(method.getAccessFlags());
        Attribute[] attributes = method.getAttributes();
        String replace = Utility.replace(accessToString, " ", "&nbsp;");
        String html = Class2HTML.toHTML(name);
        this.file.print("<TR VALIGN=TOP><TD><FONT COLOR=\"#FF0000\"><A NAME=method" + i + ">" + replace + "</A></FONT></TD>");
        this.file.print("<TD>" + Class2HTML.referenceType(methodSignatureReturnType) + "</TD><TD><A HREF=" + this.class_name + "_code.html#method" + i + " TARGET=Code>" + html + "</A></TD>\n<TD>(");
        for (int i2 = 0; i2 < methodSignatureArgumentTypes.length; i2++) {
            this.file.print(Class2HTML.referenceType(methodSignatureArgumentTypes[i2]));
            if (i2 < methodSignatureArgumentTypes.length - 1) {
                this.file.print(", ");
            }
        }
        this.file.print(")</TD></TR>");
        for (int i3 = 0; i3 < attributes.length; i3++) {
            this.attribute_html.writeAttribute(attributes[i3], ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_METHOD + i + "@" + i3, i);
            byte tag = attributes[i3].getTag();
            if (tag == 3) {
                this.file.print("<TR VALIGN=TOP><TD COLSPAN=2></TD><TH ALIGN=LEFT>throws</TH><TD>");
                int[] exceptionIndexTable = ((ExceptionTable) attributes[i3]).getExceptionIndexTable();
                for (int i4 = 0; i4 < exceptionIndexTable.length; i4++) {
                    this.file.print(this.constant_html.referenceConstant(exceptionIndexTable[i4]));
                    if (i4 < exceptionIndexTable.length - 1) {
                        this.file.print(", ");
                    }
                }
                this.file.println("</TD></TR>");
            } else if (tag == 2) {
                Attribute[] attributes2 = ((Code) attributes[i3]).getAttributes();
                for (int i5 = 0; i5 < attributes2.length; i5++) {
                    this.attribute_html.writeAttribute(attributes2[i5], ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_METHOD + i + "@" + i3 + "@" + i5, i);
                }
            }
        }
    }
}
