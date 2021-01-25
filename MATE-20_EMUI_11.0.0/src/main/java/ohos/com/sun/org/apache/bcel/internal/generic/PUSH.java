package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.xpath.internal.XPath;

public final class PUSH implements CompoundInstruction, VariableLengthInstruction, InstructionConstants {
    private Instruction instruction;

    public PUSH(ConstantPoolGen constantPoolGen, int i) {
        if (i >= -1 && i <= 5) {
            this.instruction = INSTRUCTIONS[i + 3];
        } else if (i >= -128 && i <= 127) {
            this.instruction = new BIPUSH((byte) i);
        } else if (i < -32768 || i > 32767) {
            this.instruction = new LDC(constantPoolGen.addInteger(i));
        } else {
            this.instruction = new SIPUSH((short) i);
        }
    }

    public PUSH(ConstantPoolGen constantPoolGen, boolean z) {
        this.instruction = INSTRUCTIONS[(z ? 1 : 0) + 3];
    }

    public PUSH(ConstantPoolGen constantPoolGen, float f) {
        double d = (double) f;
        if (d == XPath.MATCH_SCORE_QNAME) {
            this.instruction = FCONST_0;
        } else if (d == 1.0d) {
            this.instruction = FCONST_1;
        } else if (d == 2.0d) {
            this.instruction = FCONST_2;
        } else {
            this.instruction = new LDC(constantPoolGen.addFloat(f));
        }
    }

    public PUSH(ConstantPoolGen constantPoolGen, long j) {
        if (j == 0) {
            this.instruction = LCONST_0;
        } else if (j == 1) {
            this.instruction = LCONST_1;
        } else {
            this.instruction = new LDC2_W(constantPoolGen.addLong(j));
        }
    }

    public PUSH(ConstantPoolGen constantPoolGen, double d) {
        if (d == XPath.MATCH_SCORE_QNAME) {
            this.instruction = DCONST_0;
        } else if (d == 1.0d) {
            this.instruction = DCONST_1;
        } else {
            this.instruction = new LDC2_W(constantPoolGen.addDouble(d));
        }
    }

    public PUSH(ConstantPoolGen constantPoolGen, String str) {
        if (str == null) {
            this.instruction = ACONST_NULL;
        } else {
            this.instruction = new LDC(constantPoolGen.addString(str));
        }
    }

    public PUSH(ConstantPoolGen constantPoolGen, Number number) {
        if ((number instanceof Integer) || (number instanceof Short) || (number instanceof Byte)) {
            this.instruction = new PUSH(constantPoolGen, number.intValue()).instruction;
        } else if (number instanceof Double) {
            this.instruction = new PUSH(constantPoolGen, number.doubleValue()).instruction;
        } else if (number instanceof Float) {
            this.instruction = new PUSH(constantPoolGen, number.floatValue()).instruction;
        } else if (number instanceof Long) {
            this.instruction = new PUSH(constantPoolGen, number.longValue()).instruction;
        } else {
            throw new ClassGenException("What's this: " + number);
        }
    }

    public PUSH(ConstantPoolGen constantPoolGen, Character ch) {
        this(constantPoolGen, (int) ch.charValue());
    }

    public PUSH(ConstantPoolGen constantPoolGen, Boolean bool) {
        this(constantPoolGen, bool.booleanValue());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CompoundInstruction
    public final InstructionList getInstructionList() {
        return new InstructionList(this.instruction);
    }

    public final Instruction getInstruction() {
        return this.instruction;
    }

    public String toString() {
        return this.instruction.toString() + " (PUSH)";
    }
}
