package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public abstract class Instruction implements Cloneable, Serializable {
    private static InstructionComparator cmp = InstructionComparator.DEFAULT;
    protected short length = 1;
    protected short opcode = -1;

    public abstract void accept(Visitor visitor);

    /* access modifiers changed from: package-private */
    public void dispose() {
    }

    /* access modifiers changed from: protected */
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
    }

    Instruction() {
    }

    public Instruction(short s, short s2) {
        this.length = s2;
        this.opcode = s;
    }

    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.opcode);
    }

    public String getName() {
        return Constants.OPCODE_NAMES[this.opcode];
    }

    public String toString(boolean z) {
        if (!z) {
            return getName();
        }
        return getName() + "[" + ((int) this.opcode) + "](" + ((int) this.length) + ")";
    }

    @Override // java.lang.Object
    public String toString() {
        return toString(true);
    }

    public String toString(ConstantPool constantPool) {
        return toString(false);
    }

    public Instruction copy() {
        if (InstructionConstants.INSTRUCTIONS[getOpcode()] != null) {
            return this;
        }
        try {
            return (Instruction) clone();
        } catch (CloneNotSupportedException e) {
            System.err.println(e);
            return null;
        }
    }

    public static final Instruction readInstruction(ByteSequence byteSequence) throws IOException {
        boolean z;
        short readUnsignedByte = (short) byteSequence.readUnsignedByte();
        if (readUnsignedByte == 196) {
            z = true;
            readUnsignedByte = (short) byteSequence.readUnsignedByte();
        } else {
            z = false;
        }
        if (InstructionConstants.INSTRUCTIONS[readUnsignedByte] != null) {
            return InstructionConstants.INSTRUCTIONS[readUnsignedByte];
        }
        try {
            try {
                Instruction instruction = (Instruction) Class.forName(className(readUnsignedByte)).newInstance();
                if (z && !(instruction instanceof LocalVariableInstruction) && !(instruction instanceof IINC)) {
                    if (!(instruction instanceof RET)) {
                        throw new Exception("Illegal opcode after wide: " + ((int) readUnsignedByte));
                    }
                }
                instruction.setOpcode(readUnsignedByte);
                instruction.initFromFile(byteSequence, z);
                return instruction;
            } catch (Exception e) {
                throw new ClassGenException(e.toString());
            }
        } catch (ClassNotFoundException unused) {
            throw new ClassGenException("Illegal opcode detected.");
        }
    }

    private static final String className(short s) {
        String upperCase = Constants.OPCODE_NAMES[s].toUpperCase();
        try {
            int length2 = upperCase.length();
            int i = length2 - 2;
            char charAt = upperCase.charAt(i);
            char charAt2 = upperCase.charAt(length2 - 1);
            if (charAt == '_' && charAt2 >= '0' && charAt2 <= '5') {
                upperCase = upperCase.substring(0, i);
            }
            if (upperCase.equals("ICONST_M1")) {
                upperCase = "ICONST";
            }
        } catch (StringIndexOutOfBoundsException e) {
            System.err.println(e);
        }
        return "com.sun.org.apache.bcel.internal.generic." + upperCase;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.StackConsumer
    public int consumeStack(ConstantPoolGen constantPoolGen) {
        return Constants.CONSUME_STACK[this.opcode];
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.StackProducer
    public int produceStack(ConstantPoolGen constantPoolGen) {
        return Constants.PRODUCE_STACK[this.opcode];
    }

    public short getOpcode() {
        return this.opcode;
    }

    public int getLength() {
        return this.length;
    }

    private void setOpcode(short s) {
        this.opcode = s;
    }

    public static InstructionComparator getComparator() {
        return cmp;
    }

    public static void setComparator(InstructionComparator instructionComparator) {
        cmp = instructionComparator;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj instanceof Instruction) {
            return cmp.equals(this, (Instruction) obj);
        }
        return false;
    }
}
