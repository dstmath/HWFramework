package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.Attribute;
import ohos.com.sun.org.apache.bcel.internal.classfile.ClassParser;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;
import ohos.com.sun.org.apache.bcel.internal.classfile.Method;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;

public class Class2HTML implements Constants {
    private static String class_name;
    private static String class_package;
    private static ConstantPool constant_pool;
    private String dir;
    private JavaClass java_class;

    public Class2HTML(JavaClass javaClass, String str) throws IOException {
        Method[] methods = javaClass.getMethods();
        this.java_class = javaClass;
        this.dir = str;
        class_name = javaClass.getClassName();
        constant_pool = javaClass.getConstantPool();
        int lastIndexOf = class_name.lastIndexOf(46);
        if (lastIndexOf > -1) {
            class_package = class_name.substring(0, lastIndexOf);
        } else {
            class_package = "";
        }
        ConstantHTML constantHTML = new ConstantHTML(str, class_name, class_package, methods, constant_pool);
        AttributeHTML attributeHTML = new AttributeHTML(str, class_name, constant_pool, constantHTML);
        new MethodHTML(str, class_name, methods, javaClass.getFields(), constantHTML, attributeHTML);
        writeMainHTML(attributeHTML);
        new CodeHTML(str, class_name, methods, constant_pool, constantHTML);
        attributeHTML.close();
    }

    public static void _main(String[] strArr) {
        ClassParser classParser;
        String[] strArr2 = new String[strArr.length];
        char c = SecuritySupport.getSystemProperty("file.separator").toCharArray()[0];
        String str = "." + c;
        String str2 = null;
        int i = 0;
        int i2 = 0;
        while (i < strArr.length) {
            try {
                if (strArr[i].charAt(0) != '-') {
                    strArr2[i2] = strArr[i];
                    i2++;
                } else if (strArr[i].equals("-d")) {
                    i++;
                    str = strArr[i];
                    if (!str.endsWith("" + c)) {
                        str = str + c;
                    }
                    new File(str).mkdirs();
                } else if (strArr[i].equals("-zip")) {
                    i++;
                    str2 = strArr[i];
                } else {
                    System.out.println("Unknown option " + strArr[i]);
                }
                i++;
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace(System.out);
                return;
            }
        }
        if (i2 == 0) {
            System.err.println("Class2HTML: No input files specified.");
            return;
        }
        for (int i3 = 0; i3 < i2; i3++) {
            System.out.print("Processing " + strArr2[i3] + "...");
            if (str2 == null) {
                classParser = new ClassParser(strArr2[i3]);
            } else {
                classParser = new ClassParser(str2, strArr2[i3]);
            }
            new Class2HTML(classParser.parse(), str);
            System.out.println("Done.");
        }
    }

    static String referenceClass(int i) {
        String compactClassName = Utility.compactClassName(constant_pool.getConstantString(i, (byte) 7));
        String compactClassName2 = Utility.compactClassName(compactClassName, class_package + ".", true);
        return "<A HREF=\"" + class_name + "_cp.html#cp" + i + "\" TARGET=ConstantPool>" + compactClassName2 + "</A>";
    }

    static final String referenceType(String str) {
        String compactClassName = Utility.compactClassName(str);
        String compactClassName2 = Utility.compactClassName(compactClassName, class_package + ".", true);
        int indexOf = str.indexOf(91);
        if (indexOf > -1) {
            str = str.substring(0, indexOf);
        }
        if (str.equals("int") || str.equals(SchemaSymbols.ATTVAL_SHORT) || str.equals("boolean") || str.equals("void") || str.equals("char") || str.equals(SchemaSymbols.ATTVAL_BYTE) || str.equals("long") || str.equals("double") || str.equals("float")) {
            return "<FONT COLOR=\"#00FF00\">" + str + "</FONT>";
        }
        return "<A HREF=\"" + str + ".html\" TARGET=_top>" + compactClassName2 + "</A>";
    }

    static String toHTML(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            try {
                char charAt = str.charAt(i);
                if (charAt == '\n') {
                    stringBuffer.append("\\n");
                } else if (charAt == '\r') {
                    stringBuffer.append("\\r");
                } else if (charAt == '<') {
                    stringBuffer.append(SerializerConstants.ENTITY_LT);
                } else if (charAt != '>') {
                    stringBuffer.append(charAt);
                } else {
                    stringBuffer.append(SerializerConstants.ENTITY_GT);
                }
            } catch (StringIndexOutOfBoundsException unused) {
            }
        }
        return stringBuffer.toString();
    }

    private void writeMainHTML(AttributeHTML attributeHTML) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileOutputStream(this.dir + class_name + ".html"));
        Attribute[] attributes = this.java_class.getAttributes();
        printWriter.println("<HTML>\n<HEAD><TITLE>Documentation for " + class_name + "</TITLE></HEAD>\n<FRAMESET BORDER=1 cols=\"30%,*\">\n<FRAMESET BORDER=1 rows=\"80%,*\">\n<FRAME NAME=\"ConstantPool\" SRC=\"" + class_name + "_cp.html\"\n MARGINWIDTH=\"0\" MARGINHEIGHT=\"0\" FRAMEBORDER=\"1\" SCROLLING=\"AUTO\">\n<FRAME NAME=\"Attributes\" SRC=\"" + class_name + "_attributes.html\"\n MARGINWIDTH=\"0\" MARGINHEIGHT=\"0\" FRAMEBORDER=\"1\" SCROLLING=\"AUTO\">\n</FRAMESET>\n<FRAMESET BORDER=1 rows=\"80%,*\">\n<FRAME NAME=\"Code\" SRC=\"" + class_name + "_code.html\"\n MARGINWIDTH=0 MARGINHEIGHT=0 FRAMEBORDER=1 SCROLLING=\"AUTO\">\n<FRAME NAME=\"Methods\" SRC=\"" + class_name + "_methods.html\"\n MARGINWIDTH=0 MARGINHEIGHT=0 FRAMEBORDER=1 SCROLLING=\"AUTO\">\n</FRAMESET></FRAMESET></HTML>");
        printWriter.close();
        for (int i = 0; i < attributes.length; i++) {
            Attribute attribute = attributes[i];
            attributeHTML.writeAttribute(attribute, ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_CLASS + i);
        }
    }
}
