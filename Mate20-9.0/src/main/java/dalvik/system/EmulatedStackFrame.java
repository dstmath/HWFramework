package dalvik.system;

import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EmulatedStackFrame {
    private final MethodType callsiteType;
    /* access modifiers changed from: private */
    public final Object[] references;
    /* access modifiers changed from: private */
    public final byte[] stackFrame;
    /* access modifiers changed from: private */
    public final MethodType type;

    public static final class Range {
        public final int numBytes;
        public final int numReferences;
        public final int referencesStart;
        public final int stackFrameStart;

        private Range(int referencesStart2, int numReferences2, int stackFrameStart2, int numBytes2) {
            this.referencesStart = referencesStart2;
            this.numReferences = numReferences2;
            this.stackFrameStart = stackFrameStart2;
            this.numBytes = numBytes2;
        }

        public static Range all(MethodType frameType) {
            return of(frameType, 0, frameType.parameterCount());
        }

        public static Range of(MethodType frameType, int startArg, int endArg) {
            Class<?>[] ptypes = frameType.ptypes();
            int referencesStart2 = 0;
            int stackFrameStart2 = 0;
            for (int i = 0; i < startArg; i++) {
                Class<?> cl = ptypes[i];
                if (!cl.isPrimitive()) {
                    referencesStart2++;
                } else {
                    stackFrameStart2 += EmulatedStackFrame.getSize(cl);
                }
            }
            int numBytes2 = 0;
            int numReferences2 = 0;
            for (int i2 = startArg; i2 < endArg; i2++) {
                Class<?> cl2 = ptypes[i2];
                if (!cl2.isPrimitive()) {
                    numReferences2++;
                } else {
                    numBytes2 += EmulatedStackFrame.getSize(cl2);
                }
            }
            return new Range(referencesStart2, numReferences2, stackFrameStart2, numBytes2);
        }
    }

    public static class StackFrameAccessor {
        private static final int RETURN_VALUE_IDX = -2;
        protected int argumentIdx = 0;
        protected EmulatedStackFrame frame;
        protected ByteBuffer frameBuf = null;
        private int numArgs = 0;
        protected int referencesOffset = 0;

        protected StackFrameAccessor() {
        }

        public StackFrameAccessor attach(EmulatedStackFrame stackFrame) {
            return attach(stackFrame, 0, 0, 0);
        }

        public StackFrameAccessor attach(EmulatedStackFrame stackFrame, int argumentIdx2, int referencesOffset2, int frameOffset) {
            this.frame = stackFrame;
            this.frameBuf = ByteBuffer.wrap(this.frame.stackFrame).order(ByteOrder.LITTLE_ENDIAN);
            this.numArgs = this.frame.type.ptypes().length;
            if (frameOffset != 0) {
                this.frameBuf.position(frameOffset);
            }
            this.referencesOffset = referencesOffset2;
            this.argumentIdx = argumentIdx2;
            return this;
        }

        /* access modifiers changed from: protected */
        public void checkType(Class<?> type) {
            if (this.argumentIdx >= this.numArgs || this.argumentIdx == -1) {
                throw new IllegalArgumentException("Invalid argument index: " + this.argumentIdx);
            }
            Class<?> expectedType = this.argumentIdx == -2 ? this.frame.type.rtype() : this.frame.type.ptypes()[this.argumentIdx];
            if (expectedType != type) {
                throw new IllegalArgumentException("Incorrect type: " + type + ", expected: " + expectedType);
            }
        }

        public void makeReturnValueAccessor() {
            Class<?> rtype = this.frame.type.rtype();
            this.argumentIdx = -2;
            if (rtype.isPrimitive()) {
                this.frameBuf.position(this.frameBuf.capacity() - EmulatedStackFrame.getSize(rtype));
            } else {
                this.referencesOffset = this.frame.references.length - 1;
            }
        }

        public static void copyNext(StackFrameReader reader, StackFrameWriter writer, Class<?> type) {
            if (!type.isPrimitive()) {
                writer.putNextReference(reader.nextReference(type), type);
            } else if (type == Boolean.TYPE) {
                writer.putNextBoolean(reader.nextBoolean());
            } else if (type == Byte.TYPE) {
                writer.putNextByte(reader.nextByte());
            } else if (type == Character.TYPE) {
                writer.putNextChar(reader.nextChar());
            } else if (type == Short.TYPE) {
                writer.putNextShort(reader.nextShort());
            } else if (type == Integer.TYPE) {
                writer.putNextInt(reader.nextInt());
            } else if (type == Long.TYPE) {
                writer.putNextLong(reader.nextLong());
            } else if (type == Float.TYPE) {
                writer.putNextFloat(reader.nextFloat());
            } else if (type == Double.TYPE) {
                writer.putNextDouble(reader.nextDouble());
            }
        }
    }

    public static class StackFrameReader extends StackFrameAccessor {
        public byte nextByte() {
            checkType(Byte.TYPE);
            this.argumentIdx++;
            return (byte) this.frameBuf.getInt();
        }

        public int nextInt() {
            checkType(Integer.TYPE);
            this.argumentIdx++;
            return this.frameBuf.getInt();
        }

        public long nextLong() {
            checkType(Long.TYPE);
            this.argumentIdx++;
            return this.frameBuf.getLong();
        }

        public char nextChar() {
            checkType(Character.TYPE);
            this.argumentIdx++;
            return (char) this.frameBuf.getInt();
        }

        public boolean nextBoolean() {
            checkType(Boolean.TYPE);
            this.argumentIdx++;
            if (this.frameBuf.getInt() != 0) {
                return true;
            }
            return false;
        }

        public short nextShort() {
            checkType(Short.TYPE);
            this.argumentIdx++;
            return (short) this.frameBuf.getInt();
        }

        public float nextFloat() {
            checkType(Float.TYPE);
            this.argumentIdx++;
            return this.frameBuf.getFloat();
        }

        public double nextDouble() {
            checkType(Double.TYPE);
            this.argumentIdx++;
            return this.frameBuf.getDouble();
        }

        public <T> T nextReference(Class<T> expectedType) {
            checkType(expectedType);
            this.argumentIdx++;
            T[] access$200 = this.frame.references;
            int i = this.referencesOffset;
            this.referencesOffset = i + 1;
            return access$200[i];
        }
    }

    public static class StackFrameWriter extends StackFrameAccessor {
        public void putNextByte(byte value) {
            checkType(Byte.TYPE);
            this.argumentIdx++;
            this.frameBuf.putInt(value);
        }

        public void putNextInt(int value) {
            checkType(Integer.TYPE);
            this.argumentIdx++;
            this.frameBuf.putInt(value);
        }

        public void putNextLong(long value) {
            checkType(Long.TYPE);
            this.argumentIdx++;
            this.frameBuf.putLong(value);
        }

        public void putNextChar(char value) {
            checkType(Character.TYPE);
            this.argumentIdx++;
            this.frameBuf.putInt(value);
        }

        public void putNextBoolean(boolean value) {
            checkType(Boolean.TYPE);
            this.argumentIdx++;
            this.frameBuf.putInt(value);
        }

        public void putNextShort(short value) {
            checkType(Short.TYPE);
            this.argumentIdx++;
            this.frameBuf.putInt(value);
        }

        public void putNextFloat(float value) {
            checkType(Float.TYPE);
            this.argumentIdx++;
            this.frameBuf.putFloat(value);
        }

        public void putNextDouble(double value) {
            checkType(Double.TYPE);
            this.argumentIdx++;
            this.frameBuf.putDouble(value);
        }

        public void putNextReference(Object value, Class<?> expectedType) {
            checkType(expectedType);
            this.argumentIdx++;
            Object[] access$200 = this.frame.references;
            int i = this.referencesOffset;
            this.referencesOffset = i + 1;
            access$200[i] = value;
        }
    }

    private EmulatedStackFrame(MethodType type2, MethodType callsiteType2, Object[] references2, byte[] stackFrame2) {
        this.type = type2;
        this.callsiteType = callsiteType2;
        this.references = references2;
        this.stackFrame = stackFrame2;
    }

    public final MethodType getMethodType() {
        return this.type;
    }

    public final MethodType getCallsiteType() {
        return this.callsiteType;
    }

    public static EmulatedStackFrame create(MethodType frameType) {
        int numRefs = 0;
        int frameSize = 0;
        for (Class<?> ptype : frameType.ptypes()) {
            if (!ptype.isPrimitive()) {
                numRefs++;
            } else {
                frameSize += getSize(ptype);
            }
        }
        Class<?> rtype = frameType.rtype();
        if (!rtype.isPrimitive()) {
            numRefs++;
        } else {
            frameSize += getSize(rtype);
        }
        return new EmulatedStackFrame(frameType, frameType, new Object[numRefs], new byte[frameSize]);
    }

    public void setReference(int idx, Object reference) {
        Class<?>[] ptypes = this.type.ptypes();
        if (idx < 0 || idx >= ptypes.length) {
            throw new IllegalArgumentException("Invalid index: " + idx);
        } else if (reference == null || ptypes[idx].isInstance(reference)) {
            this.references[idx] = reference;
        } else {
            throw new IllegalStateException("reference is not of type: " + this.type.ptypes()[idx]);
        }
    }

    public <T> T getReference(int idx, Class<T> referenceType) {
        if (referenceType == this.type.ptypes()[idx]) {
            return this.references[idx];
        }
        throw new IllegalArgumentException("Argument: " + idx + " is of type " + this.type.ptypes()[idx] + " expected " + referenceType + "");
    }

    public void copyRangeTo(EmulatedStackFrame other, Range fromRange, int referencesStart, int primitivesStart) {
        if (fromRange.numReferences > 0) {
            System.arraycopy(this.references, fromRange.referencesStart, other.references, referencesStart, fromRange.numReferences);
        }
        if (fromRange.numBytes > 0) {
            System.arraycopy(this.stackFrame, fromRange.stackFrameStart, other.stackFrame, primitivesStart, fromRange.numBytes);
        }
    }

    public void copyReturnValueTo(EmulatedStackFrame other) {
        Class<?> returnType = this.type.returnType();
        if (!returnType.isPrimitive()) {
            other.references[other.references.length - 1] = this.references[this.references.length - 1];
        } else if (!is64BitPrimitive(returnType)) {
            System.arraycopy(this.stackFrame, this.stackFrame.length - 4, other.stackFrame, other.stackFrame.length - 4, 4);
        } else {
            System.arraycopy(this.stackFrame, this.stackFrame.length - 8, other.stackFrame, other.stackFrame.length - 8, 8);
        }
    }

    public void setReturnValueTo(Object reference) {
        Class<?> returnType = this.type.returnType();
        if (returnType.isPrimitive()) {
            throw new IllegalStateException("return type is not a reference type: " + returnType);
        } else if (reference == null || returnType.isInstance(reference)) {
            this.references[this.references.length - 1] = reference;
        } else {
            throw new IllegalArgumentException("reference is not of type " + returnType);
        }
    }

    private static boolean is64BitPrimitive(Class<?> type2) {
        return type2 == Double.TYPE || type2 == Long.TYPE;
    }

    public static int getSize(Class<?> type2) {
        if (!type2.isPrimitive()) {
            throw new IllegalArgumentException("type.isPrimitive() == false: " + type2);
        } else if (is64BitPrimitive(type2)) {
            return 8;
        } else {
            return 4;
        }
    }
}
