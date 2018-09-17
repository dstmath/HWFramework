package android.net.apf;

import android.net.util.NetworkConstants;
import java.util.ArrayList;
import java.util.HashMap;

public class ApfGenerator {
    public static final String DROP_LABEL = "__DROP__";
    public static final int FILTER_AGE_MEMORY_SLOT = 15;
    public static final int FIRST_PREFILLED_MEMORY_SLOT = 13;
    public static final int IPV4_HEADER_SIZE_MEMORY_SLOT = 13;
    public static final int LAST_PREFILLED_MEMORY_SLOT = 15;
    public static final int MEMORY_SLOTS = 16;
    public static final int PACKET_SIZE_MEMORY_SLOT = 14;
    public static final String PASS_LABEL = "__PASS__";
    private final Instruction mDropLabel = new Instruction(this, Opcodes.LABEL);
    private boolean mGenerated;
    private final ArrayList<Instruction> mInstructions = new ArrayList();
    private final HashMap<String, Instruction> mLabels = new HashMap();
    private final Instruction mPassLabel = new Instruction(this, Opcodes.LABEL);

    private enum ExtendedOpcodes {
        LDM(0),
        STM(16),
        NOT(32),
        NEG(33),
        SWAP(34),
        MOVE(35);
        
        final int value;

        private ExtendedOpcodes(int value) {
            this.value = value;
        }
    }

    public static class IllegalInstructionException extends Exception {
        IllegalInstructionException(String msg) {
            super(msg);
        }
    }

    private class Instruction {
        private byte[] mCompareBytes;
        private boolean mHasImm;
        private int mImm;
        private boolean mImmSigned;
        private byte mImmSize;
        private String mLabel;
        private final byte mOpcode;
        private final byte mRegister;
        private String mTargetLabel;
        private byte mTargetLabelSize;
        int offset;

        Instruction(Opcodes opcode, Register register) {
            this.mOpcode = (byte) opcode.value;
            this.mRegister = (byte) register.value;
        }

        Instruction(ApfGenerator this$0, Opcodes opcode) {
            this(opcode, Register.R0);
        }

        void setImm(int imm, boolean signed) {
            this.mHasImm = true;
            this.mImm = imm;
            this.mImmSigned = signed;
            this.mImmSize = calculateImmSize(imm, signed);
        }

        void setUnsignedImm(int imm) {
            setImm(imm, false);
        }

        void setSignedImm(int imm) {
            setImm(imm, true);
        }

        void setLabel(String label) throws IllegalInstructionException {
            if (ApfGenerator.this.mLabels.containsKey(label)) {
                throw new IllegalInstructionException("duplicate label " + label);
            } else if (this.mOpcode != Opcodes.LABEL.value) {
                throw new IllegalStateException("adding label to non-label instruction");
            } else {
                this.mLabel = label;
                ApfGenerator.this.mLabels.put(label, this);
            }
        }

        void setTargetLabel(String label) {
            this.mTargetLabel = label;
            this.mTargetLabelSize = (byte) 4;
        }

        void setCompareBytes(byte[] bytes) {
            if (this.mOpcode != Opcodes.JNEBS.value) {
                throw new IllegalStateException("adding compare bytes to non-JNEBS instruction");
            }
            this.mCompareBytes = bytes;
        }

        int size() {
            if (this.mOpcode == Opcodes.LABEL.value) {
                return 0;
            }
            int size = 1;
            if (this.mHasImm) {
                size = generatedImmSize() + 1;
            }
            if (this.mTargetLabel != null) {
                size += generatedImmSize();
            }
            if (this.mCompareBytes != null) {
                size += this.mCompareBytes.length;
            }
            return size;
        }

        boolean shrink() throws IllegalInstructionException {
            boolean z = false;
            if (this.mTargetLabel == null) {
                return false;
            }
            int oldSize = size();
            byte oldTargetLabelSize = this.mTargetLabelSize;
            this.mTargetLabelSize = calculateImmSize(calculateTargetLabelOffset(), false);
            if (this.mTargetLabelSize > oldTargetLabelSize) {
                throw new IllegalStateException("instruction grew");
            }
            if (size() < oldSize) {
                z = true;
            }
            return z;
        }

        private byte generateImmSizeField() {
            byte immSize = generatedImmSize();
            return immSize == (byte) 4 ? (byte) 3 : immSize;
        }

        private byte generateInstructionByte() {
            return (byte) (((this.mOpcode << 3) | (generateImmSizeField() << 1)) | this.mRegister);
        }

        private int writeValue(int value, byte[] bytecode, int writingOffset) {
            int i = generatedImmSize() - 1;
            int writingOffset2 = writingOffset;
            while (i >= 0) {
                writingOffset = writingOffset2 + 1;
                bytecode[writingOffset2] = (byte) ((value >> (i * 8)) & 255);
                i--;
                writingOffset2 = writingOffset;
            }
            return writingOffset2;
        }

        void generate(byte[] bytecode) throws IllegalInstructionException {
            if (this.mOpcode != Opcodes.LABEL.value) {
                int i = this.offset;
                int writingOffset = i + 1;
                bytecode[i] = generateInstructionByte();
                if (this.mTargetLabel != null) {
                    i = writeValue(calculateTargetLabelOffset(), bytecode, writingOffset);
                } else {
                    i = writingOffset;
                }
                if (this.mHasImm) {
                    i = writeValue(this.mImm, bytecode, i);
                }
                if (this.mCompareBytes != null) {
                    System.arraycopy(this.mCompareBytes, 0, bytecode, i, this.mCompareBytes.length);
                    i += this.mCompareBytes.length;
                }
                if (i - this.offset != size()) {
                    throw new IllegalStateException("wrote " + (i - this.offset) + " but should have written " + size());
                }
            }
        }

        private byte generatedImmSize() {
            return this.mImmSize > this.mTargetLabelSize ? this.mImmSize : this.mTargetLabelSize;
        }

        private int calculateTargetLabelOffset() throws IllegalInstructionException {
            Instruction targetLabelInstruction;
            if (this.mTargetLabel == ApfGenerator.DROP_LABEL) {
                targetLabelInstruction = ApfGenerator.this.mDropLabel;
            } else if (this.mTargetLabel == ApfGenerator.PASS_LABEL) {
                targetLabelInstruction = ApfGenerator.this.mPassLabel;
            } else {
                targetLabelInstruction = (Instruction) ApfGenerator.this.mLabels.get(this.mTargetLabel);
            }
            if (targetLabelInstruction == null) {
                throw new IllegalInstructionException("label not found: " + this.mTargetLabel);
            }
            int targetLabelOffset = targetLabelInstruction.offset - (this.offset + size());
            if (targetLabelOffset >= 0) {
                return targetLabelOffset;
            }
            throw new IllegalInstructionException("backward branches disallowed; label: " + this.mTargetLabel);
        }

        private byte calculateImmSize(int imm, boolean signed) {
            if (imm == 0) {
                return (byte) 0;
            }
            if ((signed && imm >= -128 && imm <= 127) || (!signed && imm >= 0 && imm <= 255)) {
                return (byte) 1;
            }
            if ((!signed || imm < -32768 || imm > 32767) && (signed || imm < 0 || imm > NetworkConstants.ARP_HWTYPE_RESERVED_HI)) {
                return (byte) 4;
            }
            return (byte) 2;
        }
    }

    private enum Opcodes {
        LABEL(-1),
        LDB(1),
        LDH(2),
        LDW(3),
        LDBX(4),
        LDHX(5),
        LDWX(6),
        ADD(7),
        MUL(8),
        DIV(9),
        AND(10),
        OR(11),
        SH(12),
        LI(13),
        JMP(14),
        JEQ(15),
        JNE(16),
        JGT(17),
        JLT(18),
        JSET(19),
        JNEBS(20),
        EXT(21);
        
        final int value;

        private Opcodes(int value) {
            this.value = value;
        }
    }

    public enum Register {
        R0(0),
        R1(1);
        
        final int value;

        private Register(int value) {
            this.value = value;
        }
    }

    public boolean setApfVersion(int version) {
        return version == 2;
    }

    private void addInstruction(Instruction instruction) {
        if (this.mGenerated) {
            throw new IllegalStateException("Program already generated");
        }
        this.mInstructions.add(instruction);
    }

    public ApfGenerator defineLabel(String name) throws IllegalInstructionException {
        Instruction instruction = new Instruction(this, Opcodes.LABEL);
        instruction.setLabel(name);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJump(String target) {
        Instruction instruction = new Instruction(this, Opcodes.JMP);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad8(Register register, int offset) {
        Instruction instruction = new Instruction(Opcodes.LDB, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad16(Register register, int offset) {
        Instruction instruction = new Instruction(Opcodes.LDH, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad32(Register register, int offset) {
        Instruction instruction = new Instruction(Opcodes.LDW, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad8Indexed(Register register, int offset) {
        Instruction instruction = new Instruction(Opcodes.LDBX, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad16Indexed(Register register, int offset) {
        Instruction instruction = new Instruction(Opcodes.LDHX, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad32Indexed(Register register, int offset) {
        Instruction instruction = new Instruction(Opcodes.LDWX, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addAdd(int value) {
        Instruction instruction = new Instruction(this, Opcodes.ADD);
        instruction.setSignedImm(value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addMul(int value) {
        Instruction instruction = new Instruction(this, Opcodes.MUL);
        instruction.setSignedImm(value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addDiv(int value) {
        Instruction instruction = new Instruction(this, Opcodes.DIV);
        instruction.setSignedImm(value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addAnd(int value) {
        Instruction instruction = new Instruction(this, Opcodes.AND);
        instruction.setUnsignedImm(value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addOr(int value) {
        Instruction instruction = new Instruction(this, Opcodes.OR);
        instruction.setUnsignedImm(value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLeftShift(int value) {
        Instruction instruction = new Instruction(this, Opcodes.SH);
        instruction.setSignedImm(value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addRightShift(int value) {
        Instruction instruction = new Instruction(this, Opcodes.SH);
        instruction.setSignedImm(-value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addAddR1() {
        addInstruction(new Instruction(Opcodes.ADD, Register.R1));
        return this;
    }

    public ApfGenerator addMulR1() {
        addInstruction(new Instruction(Opcodes.MUL, Register.R1));
        return this;
    }

    public ApfGenerator addDivR1() {
        addInstruction(new Instruction(Opcodes.DIV, Register.R1));
        return this;
    }

    public ApfGenerator addAndR1() {
        addInstruction(new Instruction(Opcodes.AND, Register.R1));
        return this;
    }

    public ApfGenerator addOrR1() {
        addInstruction(new Instruction(Opcodes.OR, Register.R1));
        return this;
    }

    public ApfGenerator addLeftShiftR1() {
        addInstruction(new Instruction(Opcodes.SH, Register.R1));
        return this;
    }

    public ApfGenerator addLoadImmediate(Register register, int value) {
        Instruction instruction = new Instruction(Opcodes.LI, register);
        instruction.setSignedImm(value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0Equals(int value, String target) {
        Instruction instruction = new Instruction(this, Opcodes.JEQ);
        instruction.setUnsignedImm(value);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0NotEquals(int value, String target) {
        Instruction instruction = new Instruction(this, Opcodes.JNE);
        instruction.setUnsignedImm(value);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0GreaterThan(int value, String target) {
        Instruction instruction = new Instruction(this, Opcodes.JGT);
        instruction.setUnsignedImm(value);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0LessThan(int value, String target) {
        Instruction instruction = new Instruction(this, Opcodes.JLT);
        instruction.setUnsignedImm(value);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0AnyBitsSet(int value, String target) {
        Instruction instruction = new Instruction(this, Opcodes.JSET);
        instruction.setUnsignedImm(value);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0EqualsR1(String target) {
        Instruction instruction = new Instruction(Opcodes.JEQ, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0NotEqualsR1(String target) {
        Instruction instruction = new Instruction(Opcodes.JNE, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0GreaterThanR1(String target) {
        Instruction instruction = new Instruction(Opcodes.JGT, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0LessThanR1(String target) {
        Instruction instruction = new Instruction(Opcodes.JLT, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0AnyBitsSetR1(String target) {
        Instruction instruction = new Instruction(Opcodes.JSET, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfBytesNotEqual(Register register, byte[] bytes, String target) throws IllegalInstructionException {
        if (register == Register.R1) {
            throw new IllegalInstructionException("JNEBS fails with R1");
        }
        Instruction instruction = new Instruction(Opcodes.JNEBS, register);
        instruction.setUnsignedImm(bytes.length);
        instruction.setTargetLabel(target);
        instruction.setCompareBytes(bytes);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoadFromMemory(Register register, int slot) throws IllegalInstructionException {
        if (slot < 0 || slot > 15) {
            throw new IllegalInstructionException("illegal memory slot number: " + slot);
        }
        Instruction instruction = new Instruction(Opcodes.EXT, register);
        instruction.setUnsignedImm(ExtendedOpcodes.LDM.value + slot);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addStoreToMemory(Register register, int slot) throws IllegalInstructionException {
        if (slot < 0 || slot > 15) {
            throw new IllegalInstructionException("illegal memory slot number: " + slot);
        }
        Instruction instruction = new Instruction(Opcodes.EXT, register);
        instruction.setUnsignedImm(ExtendedOpcodes.STM.value + slot);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addNot(Register register) {
        Instruction instruction = new Instruction(Opcodes.EXT, register);
        instruction.setUnsignedImm(ExtendedOpcodes.NOT.value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addNeg(Register register) {
        Instruction instruction = new Instruction(Opcodes.EXT, register);
        instruction.setUnsignedImm(ExtendedOpcodes.NEG.value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addSwap() {
        Instruction instruction = new Instruction(this, Opcodes.EXT);
        instruction.setUnsignedImm(ExtendedOpcodes.SWAP.value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addMove(Register register) {
        Instruction instruction = new Instruction(Opcodes.EXT, register);
        instruction.setUnsignedImm(ExtendedOpcodes.MOVE.value);
        addInstruction(instruction);
        return this;
    }

    private int updateInstructionOffsets() {
        int offset = 0;
        for (Instruction instruction : this.mInstructions) {
            instruction.offset = offset;
            offset += instruction.size();
        }
        return offset;
    }

    public int programLengthOverEstimate() {
        return updateInstructionOffsets();
    }

    public byte[] generate() throws IllegalInstructionException {
        if (this.mGenerated) {
            throw new IllegalStateException("Can only generate() once!");
        }
        int total_size;
        this.mGenerated = true;
        int iterations_remaining = 10;
        while (true) {
            total_size = updateInstructionOffsets();
            this.mDropLabel.offset = total_size + 1;
            this.mPassLabel.offset = total_size;
            int iterations_remaining2 = iterations_remaining - 1;
            if (iterations_remaining != 0) {
                boolean shrunk = false;
                for (Instruction instruction : this.mInstructions) {
                    if (instruction.shrink()) {
                        shrunk = true;
                    }
                }
                if (!shrunk) {
                    break;
                }
                iterations_remaining = iterations_remaining2;
            } else {
                break;
            }
        }
        byte[] bytecode = new byte[total_size];
        for (Instruction instruction2 : this.mInstructions) {
            instruction2.generate(bytecode);
        }
        return bytecode;
    }
}
