package android.net.apf;

import com.android.server.display.RampAnimator;
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
    private final Instruction mDropLabel;
    private boolean mGenerated;
    private final ArrayList<Instruction> mInstructions;
    private final HashMap<String, Instruction> mLabels;
    private final Instruction mPassLabel;

    private enum ExtendedOpcodes {
        ;
        
        final int value;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.apf.ApfGenerator.ExtendedOpcodes.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.apf.ApfGenerator.ExtendedOpcodes.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.apf.ApfGenerator.ExtendedOpcodes.<clinit>():void");
        }

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
        final /* synthetic */ ApfGenerator this$0;

        Instruction(ApfGenerator this$0, Opcodes opcode, Register register) {
            this.this$0 = this$0;
            this.mOpcode = (byte) opcode.value;
            this.mRegister = (byte) register.value;
        }

        Instruction(ApfGenerator this$0, Opcodes opcode) {
            this(this$0, opcode, Register.R0);
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
            if (this.this$0.mLabels.containsKey(label)) {
                throw new IllegalInstructionException("duplicate label " + label);
            } else if (this.mOpcode != Opcodes.LABEL.value) {
                throw new IllegalStateException("adding label to non-label instruction");
            } else {
                this.mLabel = label;
                this.this$0.mLabels.put(label, this);
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
            return immSize == 4 ? (byte) 3 : immSize;
        }

        private byte generateInstructionByte() {
            return (byte) (((this.mOpcode << 3) | (generateImmSizeField() << 1)) | this.mRegister);
        }

        private int writeValue(int value, byte[] bytecode, int writingOffset) {
            int i = generatedImmSize() - 1;
            int writingOffset2 = writingOffset;
            while (i >= 0) {
                writingOffset = writingOffset2 + 1;
                bytecode[writingOffset2] = (byte) ((value >> (i * 8)) & RampAnimator.DEFAULT_MAX_BRIGHTNESS);
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
                targetLabelInstruction = this.this$0.mDropLabel;
            } else if (this.mTargetLabel == ApfGenerator.PASS_LABEL) {
                targetLabelInstruction = this.this$0.mPassLabel;
            } else {
                targetLabelInstruction = (Instruction) this.this$0.mLabels.get(this.mTargetLabel);
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
            if ((signed && imm >= -128 && imm <= 127) || (!signed && imm >= 0 && imm <= RampAnimator.DEFAULT_MAX_BRIGHTNESS)) {
                return (byte) 1;
            }
            if ((!signed || imm < -32768 || imm > 32767) && (signed || imm < 0 || imm > 65535)) {
                return (byte) 4;
            }
            return (byte) 2;
        }
    }

    private enum Opcodes {
        ;
        
        final int value;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.apf.ApfGenerator.Opcodes.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.apf.ApfGenerator.Opcodes.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.apf.ApfGenerator.Opcodes.<clinit>():void");
        }

        private Opcodes(int value) {
            this.value = value;
        }
    }

    public enum Register {
        ;
        
        final int value;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.apf.ApfGenerator.Register.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.apf.ApfGenerator.Register.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.apf.ApfGenerator.Register.<clinit>():void");
        }

        private Register(int value) {
            this.value = value;
        }
    }

    public ApfGenerator() {
        this.mInstructions = new ArrayList();
        this.mLabels = new HashMap();
        this.mDropLabel = new Instruction(this, Opcodes.LABEL);
        this.mPassLabel = new Instruction(this, Opcodes.LABEL);
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
        Instruction instruction = new Instruction(this, Opcodes.LDB, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad16(Register register, int offset) {
        Instruction instruction = new Instruction(this, Opcodes.LDH, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad32(Register register, int offset) {
        Instruction instruction = new Instruction(this, Opcodes.LDW, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad8Indexed(Register register, int offset) {
        Instruction instruction = new Instruction(this, Opcodes.LDBX, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad16Indexed(Register register, int offset) {
        Instruction instruction = new Instruction(this, Opcodes.LDHX, register);
        instruction.setUnsignedImm(offset);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoad32Indexed(Register register, int offset) {
        Instruction instruction = new Instruction(this, Opcodes.LDWX, register);
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
        addInstruction(new Instruction(this, Opcodes.ADD, Register.R1));
        return this;
    }

    public ApfGenerator addMulR1() {
        addInstruction(new Instruction(this, Opcodes.MUL, Register.R1));
        return this;
    }

    public ApfGenerator addDivR1() {
        addInstruction(new Instruction(this, Opcodes.DIV, Register.R1));
        return this;
    }

    public ApfGenerator addAndR1() {
        addInstruction(new Instruction(this, Opcodes.AND, Register.R1));
        return this;
    }

    public ApfGenerator addOrR1() {
        addInstruction(new Instruction(this, Opcodes.OR, Register.R1));
        return this;
    }

    public ApfGenerator addLeftShiftR1() {
        addInstruction(new Instruction(this, Opcodes.SH, Register.R1));
        return this;
    }

    public ApfGenerator addLoadImmediate(Register register, int value) {
        Instruction instruction = new Instruction(this, Opcodes.LI, register);
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
        Instruction instruction = new Instruction(this, Opcodes.JEQ, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0NotEqualsR1(String target) {
        Instruction instruction = new Instruction(this, Opcodes.JNE, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0GreaterThanR1(String target) {
        Instruction instruction = new Instruction(this, Opcodes.JGT, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0LessThanR1(String target) {
        Instruction instruction = new Instruction(this, Opcodes.JLT, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfR0AnyBitsSetR1(String target) {
        Instruction instruction = new Instruction(this, Opcodes.JSET, Register.R1);
        instruction.setTargetLabel(target);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addJumpIfBytesNotEqual(Register register, byte[] bytes, String target) throws IllegalInstructionException {
        if (register == Register.R1) {
            throw new IllegalInstructionException("JNEBS fails with R1");
        }
        Instruction instruction = new Instruction(this, Opcodes.JNEBS, register);
        instruction.setUnsignedImm(bytes.length);
        instruction.setTargetLabel(target);
        instruction.setCompareBytes(bytes);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addLoadFromMemory(Register register, int slot) throws IllegalInstructionException {
        if (slot < 0 || slot > LAST_PREFILLED_MEMORY_SLOT) {
            throw new IllegalInstructionException("illegal memory slot number: " + slot);
        }
        Instruction instruction = new Instruction(this, Opcodes.EXT, register);
        instruction.setUnsignedImm(ExtendedOpcodes.LDM.value + slot);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addStoreToMemory(Register register, int slot) throws IllegalInstructionException {
        if (slot < 0 || slot > LAST_PREFILLED_MEMORY_SLOT) {
            throw new IllegalInstructionException("illegal memory slot number: " + slot);
        }
        Instruction instruction = new Instruction(this, Opcodes.EXT, register);
        instruction.setUnsignedImm(ExtendedOpcodes.STM.value + slot);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addNot(Register register) {
        Instruction instruction = new Instruction(this, Opcodes.EXT, register);
        instruction.setUnsignedImm(ExtendedOpcodes.NOT.value);
        addInstruction(instruction);
        return this;
    }

    public ApfGenerator addNeg(Register register) {
        Instruction instruction = new Instruction(this, Opcodes.EXT, register);
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
        Instruction instruction = new Instruction(this, Opcodes.EXT, register);
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
