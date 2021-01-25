package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class Signature extends Attribute {
    private int signature_index;

    private static boolean identPart(int i) {
        return i == 47 || i == 59;
    }

    private static boolean identStart(int i) {
        return i == 84 || i == 76;
    }

    public Signature(Signature signature) {
        this(signature.getNameIndex(), signature.getLength(), signature.getSignatureIndex(), signature.getConstantPool());
    }

    Signature(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, dataInputStream.readUnsignedShort(), constantPool);
    }

    public Signature(int i, int i2, int i3, ConstantPool constantPool) {
        super((byte) 10, i, i2, constantPool);
        this.signature_index = i3;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        System.err.println("Visiting non-standard Signature object");
        visitor.visitSignature(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.signature_index);
    }

    public final int getSignatureIndex() {
        return this.signature_index;
    }

    public final void setSignatureIndex(int i) {
        this.signature_index = i;
    }

    public final String getSignature() {
        return ((ConstantUtf8) this.constant_pool.getConstant(this.signature_index, (byte) 1)).getBytes();
    }

    /* access modifiers changed from: private */
    public static final class MyByteArrayInputStream extends ByteArrayInputStream {
        MyByteArrayInputStream(String str) {
            super(str.getBytes());
        }

        /* access modifiers changed from: package-private */
        public final int mark() {
            return this.pos;
        }

        /* access modifiers changed from: package-private */
        public final String getData() {
            return new String(this.buf);
        }

        /* access modifiers changed from: package-private */
        public final void reset(int i) {
            this.pos = i;
        }

        /* access modifiers changed from: package-private */
        public final void unread() {
            if (this.pos > 0) {
                this.pos--;
            }
        }
    }

    private static final void matchIdent(MyByteArrayInputStream myByteArrayInputStream, StringBuffer stringBuffer) {
        int read = myByteArrayInputStream.read();
        if (read == -1) {
            throw new RuntimeException("Illegal signature: " + myByteArrayInputStream.getData() + " no ident, reaching EOF");
        } else if (!identStart(read)) {
            StringBuffer stringBuffer2 = new StringBuffer();
            int i = 1;
            while (true) {
                char c = (char) read;
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                stringBuffer2.append(c);
                i++;
                read = myByteArrayInputStream.read();
            }
            if (read == 58) {
                myByteArrayInputStream.skip((long) 17);
                stringBuffer.append(stringBuffer2);
                myByteArrayInputStream.read();
                myByteArrayInputStream.unread();
                return;
            }
            for (int i2 = 0; i2 < i; i2++) {
                myByteArrayInputStream.unread();
            }
        } else {
            StringBuffer stringBuffer3 = new StringBuffer();
            int read2 = myByteArrayInputStream.read();
            while (true) {
                stringBuffer3.append((char) read2);
                read2 = myByteArrayInputStream.read();
                if (read2 == -1 || (!Character.isJavaIdentifierPart((char) read2) && read2 != 47)) {
                    break;
                }
            }
            stringBuffer.append(stringBuffer3.toString().replace('/', '.'));
            if (read2 != -1) {
                myByteArrayInputStream.unread();
            }
        }
    }

    private static final void matchGJIdent(MyByteArrayInputStream myByteArrayInputStream, StringBuffer stringBuffer) {
        int read;
        matchIdent(myByteArrayInputStream, stringBuffer);
        int read2 = myByteArrayInputStream.read();
        if (read2 == 60 || read2 == 40) {
            stringBuffer.append((char) read2);
            matchGJIdent(myByteArrayInputStream, stringBuffer);
            while (true) {
                read = myByteArrayInputStream.read();
                if (read == 62 || read == 41) {
                    break;
                } else if (read != -1) {
                    stringBuffer.append(", ");
                    myByteArrayInputStream.unread();
                    matchGJIdent(myByteArrayInputStream, stringBuffer);
                } else {
                    throw new RuntimeException("Illegal signature: " + myByteArrayInputStream.getData() + " reaching EOF");
                }
            }
            stringBuffer.append((char) read);
        } else {
            myByteArrayInputStream.unread();
        }
        int read3 = myByteArrayInputStream.read();
        if (identStart(read3)) {
            myByteArrayInputStream.unread();
            matchGJIdent(myByteArrayInputStream, stringBuffer);
        } else if (read3 == 41) {
            myByteArrayInputStream.unread();
        } else if (read3 != 59) {
            throw new RuntimeException("Illegal signature: " + myByteArrayInputStream.getData() + " read " + ((char) read3));
        }
    }

    public static String translate(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        matchGJIdent(new MyByteArrayInputStream(str), stringBuffer);
        return stringBuffer.toString();
    }

    public static final boolean isFormalParameterList(String str) {
        return str.startsWith("<") && str.indexOf(58) > 0;
    }

    public static final boolean isActualParameterList(String str) {
        return str.startsWith("L") && str.endsWith(">;");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        String signature = getSignature();
        return "Signature(" + signature + ")";
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        return (Signature) clone();
    }
}
