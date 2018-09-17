package com.android.dex;

public final class Code {
    private final CatchHandler[] catchHandlers;
    private final int debugInfoOffset;
    private final int insSize;
    private final short[] instructions;
    private final int outsSize;
    private final int registersSize;
    private final Try[] tries;

    public static class CatchHandler {
        final int[] addresses;
        final int catchAllAddress;
        final int offset;
        final int[] typeIndexes;

        public CatchHandler(int[] typeIndexes, int[] addresses, int catchAllAddress, int offset) {
            this.typeIndexes = typeIndexes;
            this.addresses = addresses;
            this.catchAllAddress = catchAllAddress;
            this.offset = offset;
        }

        public int[] getTypeIndexes() {
            return this.typeIndexes;
        }

        public int[] getAddresses() {
            return this.addresses;
        }

        public int getCatchAllAddress() {
            return this.catchAllAddress;
        }

        public int getOffset() {
            return this.offset;
        }
    }

    public static class Try {
        final int catchHandlerIndex;
        final int instructionCount;
        final int startAddress;

        Try(int startAddress, int instructionCount, int catchHandlerIndex) {
            this.startAddress = startAddress;
            this.instructionCount = instructionCount;
            this.catchHandlerIndex = catchHandlerIndex;
        }

        public int getStartAddress() {
            return this.startAddress;
        }

        public int getInstructionCount() {
            return this.instructionCount;
        }

        public int getCatchHandlerIndex() {
            return this.catchHandlerIndex;
        }
    }

    public Code(int registersSize, int insSize, int outsSize, int debugInfoOffset, short[] instructions, Try[] tries, CatchHandler[] catchHandlers) {
        this.registersSize = registersSize;
        this.insSize = insSize;
        this.outsSize = outsSize;
        this.debugInfoOffset = debugInfoOffset;
        this.instructions = instructions;
        this.tries = tries;
        this.catchHandlers = catchHandlers;
    }

    public int getRegistersSize() {
        return this.registersSize;
    }

    public int getInsSize() {
        return this.insSize;
    }

    public int getOutsSize() {
        return this.outsSize;
    }

    public int getDebugInfoOffset() {
        return this.debugInfoOffset;
    }

    public short[] getInstructions() {
        return this.instructions;
    }

    public Try[] getTries() {
        return this.tries;
    }

    public CatchHandler[] getCatchHandlers() {
        return this.catchHandlers;
    }
}
