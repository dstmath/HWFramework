package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ClassParser {
    private static final int BUFSIZE = 8192;
    private int access_flags;
    private Attribute[] attributes;
    private int class_name_index;
    private ConstantPool constant_pool;
    private Field[] fields;
    private DataInputStream file;
    private String file_name;
    private int[] interfaces;
    private boolean is_zip;
    private int major;
    private Method[] methods;
    private int minor;
    private int superclass_name_index;
    private ZipFile zip;

    public ClassParser(InputStream inputStream, String str) {
        this.file_name = str;
        String name = inputStream.getClass().getName();
        this.is_zip = name.startsWith("java.util.zip.") || name.startsWith("java.util.jar.");
        if (inputStream instanceof DataInputStream) {
            this.file = (DataInputStream) inputStream;
        } else {
            this.file = new DataInputStream(new BufferedInputStream(inputStream, 8192));
        }
    }

    public ClassParser(String str) throws IOException {
        this.is_zip = false;
        this.file_name = str;
        this.file = new DataInputStream(new BufferedInputStream(new FileInputStream(str), 8192));
    }

    public ClassParser(String str, String str2) throws IOException {
        this.is_zip = true;
        this.zip = new ZipFile(str);
        ZipEntry entry = this.zip.getEntry(str2);
        this.file_name = str2;
        this.file = new DataInputStream(new BufferedInputStream(this.zip.getInputStream(entry), 8192));
    }

    public JavaClass parse() throws IOException, ClassFormatException {
        readID();
        readVersion();
        readConstantPool();
        readClassInfo();
        readInterfaces();
        readFields();
        readMethods();
        readAttributes();
        this.file.close();
        ZipFile zipFile = this.zip;
        if (zipFile != null) {
            zipFile.close();
        }
        return new JavaClass(this.class_name_index, this.superclass_name_index, this.file_name, this.major, this.minor, this.access_flags, this.constant_pool, this.interfaces, this.fields, this.methods, this.attributes, this.is_zip ? (byte) 3 : 2);
    }

    private final void readAttributes() throws IOException, ClassFormatException {
        int readUnsignedShort = this.file.readUnsignedShort();
        this.attributes = new Attribute[readUnsignedShort];
        for (int i = 0; i < readUnsignedShort; i++) {
            this.attributes[i] = Attribute.readAttribute(this.file, this.constant_pool);
        }
    }

    private final void readClassInfo() throws IOException, ClassFormatException {
        this.access_flags = this.file.readUnsignedShort();
        int i = this.access_flags;
        if ((i & 512) != 0) {
            this.access_flags = i | 1024;
        }
        int i2 = this.access_flags;
        if ((i2 & 1024) == 0 || (i2 & 16) == 0) {
            this.class_name_index = this.file.readUnsignedShort();
            this.superclass_name_index = this.file.readUnsignedShort();
            return;
        }
        throw new ClassFormatException("Class can't be both final and abstract");
    }

    private final void readConstantPool() throws IOException, ClassFormatException {
        this.constant_pool = new ConstantPool(this.file);
    }

    private final void readFields() throws IOException, ClassFormatException {
        int readUnsignedShort = this.file.readUnsignedShort();
        this.fields = new Field[readUnsignedShort];
        for (int i = 0; i < readUnsignedShort; i++) {
            this.fields[i] = new Field(this.file, this.constant_pool);
        }
    }

    private final void readID() throws IOException, ClassFormatException {
        if (this.file.readInt() != -889275714) {
            throw new ClassFormatException(this.file_name + " is not a Java .class file");
        }
    }

    private final void readInterfaces() throws IOException, ClassFormatException {
        int readUnsignedShort = this.file.readUnsignedShort();
        this.interfaces = new int[readUnsignedShort];
        for (int i = 0; i < readUnsignedShort; i++) {
            this.interfaces[i] = this.file.readUnsignedShort();
        }
    }

    private final void readMethods() throws IOException, ClassFormatException {
        int readUnsignedShort = this.file.readUnsignedShort();
        this.methods = new Method[readUnsignedShort];
        for (int i = 0; i < readUnsignedShort; i++) {
            this.methods[i] = new Method(this.file, this.constant_pool);
        }
    }

    private final void readVersion() throws IOException, ClassFormatException {
        this.minor = this.file.readUnsignedShort();
        this.major = this.file.readUnsignedShort();
    }
}
