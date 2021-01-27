package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.bcel.internal.util.ClassQueue;
import ohos.com.sun.org.apache.bcel.internal.util.ClassVector;
import ohos.com.sun.org.apache.bcel.internal.util.Repository;
import ohos.com.sun.org.apache.bcel.internal.util.SyntheticRepository;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;

public class JavaClass extends AccessFlags implements Cloneable, Node {
    public static final byte FILE = 2;
    public static final byte HEAP = 1;
    public static final byte ZIP = 3;
    static boolean debug = false;
    static char sep = '/';
    private Attribute[] attributes;
    private String class_name;
    private int class_name_index;
    private ConstantPool constant_pool;
    private Field[] fields;
    private String file_name;
    private String[] interface_names;
    private int[] interfaces;
    private int major;
    private Method[] methods;
    private int minor;
    private String package_name;
    private transient Repository repository;
    private byte source;
    private String source_file_name;
    private String superclass_name;
    private int superclass_name_index;

    public JavaClass(int i, int i2, String str, int i3, int i4, int i5, ConstantPool constantPool, int[] iArr, Field[] fieldArr, Method[] methodArr, Attribute[] attributeArr, byte b) {
        this.source_file_name = "<Unknown>";
        this.source = 1;
        this.repository = SyntheticRepository.getInstance();
        iArr = iArr == null ? new int[0] : iArr;
        if (attributeArr == null) {
            this.attributes = new Attribute[0];
        }
        fieldArr = fieldArr == null ? new Field[0] : fieldArr;
        methodArr = methodArr == null ? new Method[0] : methodArr;
        this.class_name_index = i;
        this.superclass_name_index = i2;
        this.file_name = str;
        this.major = i3;
        this.minor = i4;
        this.access_flags = i5;
        this.constant_pool = constantPool;
        this.interfaces = iArr;
        this.fields = fieldArr;
        this.methods = methodArr;
        this.attributes = attributeArr;
        this.source = b;
        int i6 = 0;
        while (true) {
            if (i6 >= attributeArr.length) {
                break;
            } else if (attributeArr[i6] instanceof SourceFile) {
                this.source_file_name = ((SourceFile) attributeArr[i6]).getSourceFileName();
                break;
            } else {
                i6++;
            }
        }
        this.class_name = constantPool.getConstantString(i, (byte) 7);
        this.class_name = Utility.compactClassName(this.class_name, false);
        int lastIndexOf = this.class_name.lastIndexOf(46);
        if (lastIndexOf < 0) {
            this.package_name = "";
        } else {
            this.package_name = this.class_name.substring(0, lastIndexOf);
        }
        if (i2 > 0) {
            this.superclass_name = constantPool.getConstantString(i2, (byte) 7);
            this.superclass_name = Utility.compactClassName(this.superclass_name, false);
        } else {
            this.superclass_name = Constants.OBJECT_CLASS;
        }
        this.interface_names = new String[iArr.length];
        for (int i7 = 0; i7 < iArr.length; i7++) {
            this.interface_names[i7] = Utility.compactClassName(constantPool.getConstantString(iArr[i7], (byte) 7), false);
        }
    }

    public JavaClass(int i, int i2, String str, int i3, int i4, int i5, ConstantPool constantPool, int[] iArr, Field[] fieldArr, Method[] methodArr, Attribute[] attributeArr) {
        this(i, i2, str, i3, i4, i5, constantPool, iArr, fieldArr, methodArr, attributeArr, (byte) 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitJavaClass(this);
    }

    static final void Debug(String str) {
        if (debug) {
            System.out.println(str);
        }
    }

    public void dump(File file) throws IOException {
        String parent = file.getParent();
        if (parent != null) {
            new File(parent).mkdirs();
        }
        dump(new DataOutputStream(new FileOutputStream(file)));
    }

    public void dump(String str) throws IOException {
        dump(new File(str));
    }

    public byte[] getBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dump(dataOutputStream);
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            dataOutputStream.close();
        } catch (Throwable th) {
            try {
                dataOutputStream.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            throw th;
        }
        return byteArrayOutputStream.toByteArray();
    }

    public void dump(OutputStream outputStream) throws IOException {
        dump(new DataOutputStream(outputStream));
    }

    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(-889275714);
        dataOutputStream.writeShort(this.minor);
        dataOutputStream.writeShort(this.major);
        this.constant_pool.dump(dataOutputStream);
        dataOutputStream.writeShort(this.access_flags);
        dataOutputStream.writeShort(this.class_name_index);
        dataOutputStream.writeShort(this.superclass_name_index);
        dataOutputStream.writeShort(this.interfaces.length);
        int i = 0;
        int i2 = 0;
        while (true) {
            int[] iArr = this.interfaces;
            if (i2 >= iArr.length) {
                break;
            }
            dataOutputStream.writeShort(iArr[i2]);
            i2++;
        }
        dataOutputStream.writeShort(this.fields.length);
        int i3 = 0;
        while (true) {
            Field[] fieldArr = this.fields;
            if (i3 >= fieldArr.length) {
                break;
            }
            fieldArr[i3].dump(dataOutputStream);
            i3++;
        }
        dataOutputStream.writeShort(this.methods.length);
        int i4 = 0;
        while (true) {
            Method[] methodArr = this.methods;
            if (i4 >= methodArr.length) {
                break;
            }
            methodArr[i4].dump(dataOutputStream);
            i4++;
        }
        Attribute[] attributeArr = this.attributes;
        if (attributeArr != null) {
            dataOutputStream.writeShort(attributeArr.length);
            while (true) {
                Attribute[] attributeArr2 = this.attributes;
                if (i >= attributeArr2.length) {
                    break;
                }
                attributeArr2[i].dump(dataOutputStream);
                i++;
            }
        } else {
            dataOutputStream.writeShort(0);
        }
        dataOutputStream.close();
    }

    public Attribute[] getAttributes() {
        return this.attributes;
    }

    public String getClassName() {
        return this.class_name;
    }

    public String getPackageName() {
        return this.package_name;
    }

    public int getClassNameIndex() {
        return this.class_name_index;
    }

    public ConstantPool getConstantPool() {
        return this.constant_pool;
    }

    public Field[] getFields() {
        return this.fields;
    }

    public String getFileName() {
        return this.file_name;
    }

    public String[] getInterfaceNames() {
        return this.interface_names;
    }

    public int[] getInterfaceIndices() {
        return this.interfaces;
    }

    public int getMajor() {
        return this.major;
    }

    public Method[] getMethods() {
        return this.methods;
    }

    public Method getMethod(Method method) {
        int i = 0;
        while (true) {
            Method[] methodArr = this.methods;
            if (i >= methodArr.length) {
                return null;
            }
            Method method2 = methodArr[i];
            if (method.getName().equals(method2.getName()) && method.getModifiers() == method2.getModifiers() && Type.getSignature(method).equals(method2.getSignature())) {
                return method2;
            }
            i++;
        }
    }

    public int getMinor() {
        return this.minor;
    }

    public String getSourceFileName() {
        return this.source_file_name;
    }

    public String getSuperclassName() {
        return this.superclass_name;
    }

    public int getSuperclassNameIndex() {
        return this.superclass_name_index;
    }

    static {
        String str;
        String str2 = null;
        try {
            str = SecuritySupport.getSystemProperty("JavaClass.debug");
            try {
                str2 = SecuritySupport.getSystemProperty("file.separator");
            } catch (SecurityException unused) {
            }
        } catch (SecurityException unused2) {
            str = null;
        }
        if (str != null) {
            debug = new Boolean(str).booleanValue();
        }
        if (str2 != null) {
            try {
                sep = str2.charAt(0);
            } catch (StringIndexOutOfBoundsException unused3) {
            }
        }
    }

    public void setAttributes(Attribute[] attributeArr) {
        this.attributes = attributeArr;
    }

    public void setClassName(String str) {
        this.class_name = str;
    }

    public void setClassNameIndex(int i) {
        this.class_name_index = i;
    }

    public void setConstantPool(ConstantPool constantPool) {
        this.constant_pool = constantPool;
    }

    public void setFields(Field[] fieldArr) {
        this.fields = fieldArr;
    }

    public void setFileName(String str) {
        this.file_name = str;
    }

    public void setInterfaceNames(String[] strArr) {
        this.interface_names = strArr;
    }

    public void setInterfaces(int[] iArr) {
        this.interfaces = iArr;
    }

    public void setMajor(int i) {
        this.major = i;
    }

    public void setMethods(Method[] methodArr) {
        this.methods = methodArr;
    }

    public void setMinor(int i) {
        this.minor = i;
    }

    public void setSourceFileName(String str) {
        this.source_file_name = str;
    }

    public void setSuperclassName(String str) {
        this.superclass_name = str;
    }

    public void setSuperclassNameIndex(int i) {
        this.superclass_name_index = i;
    }

    @Override // java.lang.Object
    public String toString() {
        String accessToString = Utility.accessToString(this.access_flags, true);
        String str = "";
        if (!accessToString.equals(str)) {
            str = accessToString + " ";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(Utility.classOrInterface(this.access_flags));
        sb.append(" ");
        sb.append(this.class_name);
        sb.append(" extends ");
        sb.append(Utility.compactClassName(this.superclass_name, false));
        sb.append('\n');
        StringBuffer stringBuffer = new StringBuffer(sb.toString());
        int length = this.interfaces.length;
        if (length > 0) {
            stringBuffer.append("implements\t\t");
            for (int i = 0; i < length; i++) {
                stringBuffer.append(this.interface_names[i]);
                if (i < length - 1) {
                    stringBuffer.append(", ");
                }
            }
            stringBuffer.append('\n');
        }
        stringBuffer.append("filename\t\t" + this.file_name + '\n');
        stringBuffer.append("compiled from\t\t" + this.source_file_name + '\n');
        stringBuffer.append("compiler version\t" + this.major + "." + this.minor + '\n');
        StringBuilder sb2 = new StringBuilder();
        sb2.append("access flags\t\t");
        sb2.append(this.access_flags);
        sb2.append('\n');
        stringBuffer.append(sb2.toString());
        stringBuffer.append("constant pool\t\t" + this.constant_pool.getLength() + " entries\n");
        stringBuffer.append("ACC_SUPER flag\t\t" + isSuper() + "\n");
        if (this.attributes.length > 0) {
            stringBuffer.append("\nAttribute(s):\n");
            int i2 = 0;
            while (true) {
                Attribute[] attributeArr = this.attributes;
                if (i2 >= attributeArr.length) {
                    break;
                }
                stringBuffer.append(indent(attributeArr[i2]));
                i2++;
            }
        }
        if (this.fields.length > 0) {
            stringBuffer.append("\n" + this.fields.length + " fields:\n");
            for (int i3 = 0; i3 < this.fields.length; i3++) {
                stringBuffer.append("\t" + this.fields[i3] + '\n');
            }
        }
        if (this.methods.length > 0) {
            stringBuffer.append("\n" + this.methods.length + " methods:\n");
            for (int i4 = 0; i4 < this.methods.length; i4++) {
                stringBuffer.append("\t" + this.methods[i4] + '\n');
            }
        }
        return stringBuffer.toString();
    }

    private static final String indent(Object obj) {
        StringTokenizer stringTokenizer = new StringTokenizer(obj.toString(), "\n");
        StringBuffer stringBuffer = new StringBuffer();
        while (stringTokenizer.hasMoreTokens()) {
            stringBuffer.append("\t" + stringTokenizer.nextToken() + "\n");
        }
        return stringBuffer.toString();
    }

    public JavaClass copy() {
        JavaClass javaClass;
        try {
            javaClass = (JavaClass) clone();
        } catch (CloneNotSupportedException unused) {
            javaClass = null;
        }
        javaClass.constant_pool = this.constant_pool.copy();
        javaClass.interfaces = (int[]) this.interfaces.clone();
        javaClass.interface_names = (String[]) this.interface_names.clone();
        javaClass.fields = new Field[this.fields.length];
        int i = 0;
        int i2 = 0;
        while (true) {
            Field[] fieldArr = this.fields;
            if (i2 >= fieldArr.length) {
                break;
            }
            javaClass.fields[i2] = fieldArr[i2].copy(javaClass.constant_pool);
            i2++;
        }
        javaClass.methods = new Method[this.methods.length];
        int i3 = 0;
        while (true) {
            Method[] methodArr = this.methods;
            if (i3 >= methodArr.length) {
                break;
            }
            javaClass.methods[i3] = methodArr[i3].copy(javaClass.constant_pool);
            i3++;
        }
        javaClass.attributes = new Attribute[this.attributes.length];
        while (true) {
            Attribute[] attributeArr = this.attributes;
            if (i >= attributeArr.length) {
                return javaClass;
            }
            javaClass.attributes[i] = attributeArr[i].copy(javaClass.constant_pool);
            i++;
        }
    }

    public final boolean isSuper() {
        return (this.access_flags & 32) != 0;
    }

    public final boolean isClass() {
        return (this.access_flags & 512) == 0;
    }

    public final byte getSource() {
        return this.source;
    }

    public Repository getRepository() {
        return this.repository;
    }

    public void setRepository(Repository repository2) {
        this.repository = repository2;
    }

    public final boolean instanceOf(JavaClass javaClass) {
        JavaClass[] superClasses;
        if (equals(javaClass)) {
            return true;
        }
        for (JavaClass javaClass2 : getSuperClasses()) {
            if (javaClass2.equals(javaClass)) {
                return true;
            }
        }
        if (javaClass.isInterface()) {
            return implementationOf(javaClass);
        }
        return false;
    }

    public boolean implementationOf(JavaClass javaClass) {
        JavaClass[] allInterfaces;
        if (!javaClass.isInterface()) {
            throw new IllegalArgumentException(javaClass.getClassName() + " is no interface");
        } else if (equals(javaClass)) {
            return true;
        } else {
            for (JavaClass javaClass2 : getAllInterfaces()) {
                if (javaClass2.equals(javaClass)) {
                    return true;
                }
            }
            return false;
        }
    }

    public JavaClass getSuperClass() {
        if (Constants.OBJECT_CLASS.equals(getClassName())) {
            return null;
        }
        try {
            return this.repository.loadClass(getSuperclassName());
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            return null;
        }
    }

    public JavaClass[] getSuperClasses() {
        ClassVector classVector = new ClassVector();
        for (JavaClass superClass = getSuperClass(); superClass != null; superClass = superClass.getSuperClass()) {
            classVector.addElement(superClass);
        }
        return classVector.toArray();
    }

    public JavaClass[] getInterfaces() {
        String[] interfaceNames = getInterfaceNames();
        JavaClass[] javaClassArr = new JavaClass[interfaceNames.length];
        for (int i = 0; i < interfaceNames.length; i++) {
            try {
                javaClassArr[i] = this.repository.loadClass(interfaceNames[i]);
            } catch (ClassNotFoundException e) {
                System.err.println(e);
                return null;
            }
        }
        return javaClassArr;
    }

    public JavaClass[] getAllInterfaces() {
        ClassQueue classQueue = new ClassQueue();
        ClassVector classVector = new ClassVector();
        classQueue.enqueue(this);
        while (!classQueue.empty()) {
            JavaClass dequeue = classQueue.dequeue();
            JavaClass superClass = dequeue.getSuperClass();
            JavaClass[] interfaces2 = dequeue.getInterfaces();
            if (dequeue.isInterface()) {
                classVector.addElement(dequeue);
            } else if (superClass != null) {
                classQueue.enqueue(superClass);
            }
            for (JavaClass javaClass : interfaces2) {
                classQueue.enqueue(javaClass);
            }
        }
        return classVector.toArray();
    }
}
