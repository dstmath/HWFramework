package java.lang.invoke;

import dalvik.system.EmulatedStackFrame;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Types;
import sun.invoke.util.Wrapper;

public class Transformers {
    /* access modifiers changed from: private */
    public static final Method TRANSFORM_INTERNAL;

    public static class AlwaysThrow extends Transformer {
        private final Class<? extends Throwable> exceptionType;

        public AlwaysThrow(Class<?> nominalReturnType, Class<? extends Throwable> exType) {
            super(MethodType.methodType(nominalReturnType, (Class<?>) exType));
            this.exceptionType = exType;
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            throw ((Throwable) emulatedStackFrame.getReference(0, this.exceptionType));
        }
    }

    public static class BindTo extends Transformer {
        private final MethodHandle delegate;
        private final EmulatedStackFrame.Range range = EmulatedStackFrame.Range.all(type());
        private final Object receiver;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.BindTo.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.BindTo.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.BindTo.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public BindTo(MethodHandle delegate2, Object receiver2) {
            super(delegate2.type().dropParameterTypes(0, 1));
            this.delegate = delegate2;
            this.receiver = receiver2;
        }
    }

    public static class CatchException extends Transformer {
        private final Class<?> exType;
        private final MethodHandle handler;
        private final EmulatedStackFrame.Range handlerArgsRange;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.CatchException.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.CatchException.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.CatchException.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public CatchException(MethodHandle target2, MethodHandle handler2, Class<?> exType2) {
            super(target2.type());
            this.target = target2;
            this.handler = handler2;
            this.exType = exType2;
            this.handlerArgsRange = EmulatedStackFrame.Range.of(target2.type(), 0, handler2.type().parameterCount() - 1);
        }
    }

    static class CollectArguments extends Transformer {
        private final MethodHandle collector;
        private final EmulatedStackFrame.Range collectorRange;
        private final int pos;
        private final EmulatedStackFrame.Range range1;
        private final EmulatedStackFrame.Range range2;
        private final int referencesOffset;
        private final int stackFrameOffset;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.CollectArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.CollectArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.CollectArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        CollectArguments(MethodHandle target2, MethodHandle collector2, int pos2, MethodType adapterType) {
            super(adapterType);
            this.target = target2;
            this.collector = collector2;
            this.pos = pos2;
            int numFilterArgs = collector2.type().parameterCount();
            int numAdapterArgs = type().parameterCount();
            this.collectorRange = EmulatedStackFrame.Range.of(type(), pos2, pos2 + numFilterArgs);
            this.range1 = EmulatedStackFrame.Range.of(type(), 0, pos2);
            if (pos2 + numFilterArgs < numAdapterArgs) {
                this.range2 = EmulatedStackFrame.Range.of(type(), pos2 + numFilterArgs, numAdapterArgs);
            } else {
                this.range2 = null;
            }
            Class<?> collectorRType = collector2.type().rtype();
            if (collectorRType == Void.TYPE) {
                this.stackFrameOffset = 0;
                this.referencesOffset = 0;
            } else if (collectorRType.isPrimitive()) {
                this.stackFrameOffset = EmulatedStackFrame.getSize(collectorRType);
                this.referencesOffset = 0;
            } else {
                this.stackFrameOffset = 0;
                this.referencesOffset = 1;
            }
        }
    }

    static class Collector extends Transformer {
        private final int arrayOffset;
        private final char arrayTypeChar;
        private final EmulatedStackFrame.Range copyRange;
        private final int numArrayArgs;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Collector.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Collector.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.Collector.transform(dalvik.system.EmulatedStackFrame):void");
        }

        Collector(MethodHandle delegate, Class<?> arrayType, int length) {
            super(delegate.type().asCollectorType(arrayType, length));
            this.target = delegate;
            this.arrayOffset = delegate.type().parameterCount() - 1;
            this.arrayTypeChar = Wrapper.basicTypeChar(arrayType.getComponentType());
            this.numArrayArgs = length;
            this.copyRange = EmulatedStackFrame.Range.of(delegate.type(), 0, this.arrayOffset);
        }
    }

    public static class Constant extends Transformer {
        private double asDouble;
        private float asFloat;
        private int asInt;
        private long asLong;
        private Object asReference;
        private final Class<?> type;
        private char typeChar;

        public Constant(Class<?> type2, Object value) {
            super(MethodType.methodType(type2));
            this.type = type2;
            if (!type2.isPrimitive()) {
                this.asReference = value;
                this.typeChar = 'L';
            } else if (type2 == Integer.TYPE) {
                this.asInt = ((Integer) value).intValue();
                this.typeChar = 'I';
            } else if (type2 == Character.TYPE) {
                this.asInt = ((Character) value).charValue();
                this.typeChar = 'C';
            } else if (type2 == Short.TYPE) {
                this.asInt = ((Short) value).shortValue();
                this.typeChar = 'S';
            } else if (type2 == Byte.TYPE) {
                this.asInt = ((Byte) value).byteValue();
                this.typeChar = 'B';
            } else if (type2 == Boolean.TYPE) {
                this.asInt = ((Boolean) value).booleanValue() ? 1 : 0;
                this.typeChar = 'Z';
            } else if (type2 == Long.TYPE) {
                this.asLong = ((Long) value).longValue();
                this.typeChar = 'J';
            } else if (type2 == Float.TYPE) {
                this.asFloat = ((Float) value).floatValue();
                this.typeChar = 'F';
            } else if (type2 == Double.TYPE) {
                this.asDouble = ((Double) value).doubleValue();
                this.typeChar = 'D';
            } else {
                throw new AssertionError((Object) "unknown type: " + this.typeChar);
            }
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            EmulatedStackFrame.StackFrameWriter writer = new EmulatedStackFrame.StackFrameWriter();
            writer.attach(emulatedStackFrame);
            writer.makeReturnValueAccessor();
            char c = this.typeChar;
            if (c == 'F') {
                writer.putNextFloat(this.asFloat);
            } else if (c == 'L') {
                writer.putNextReference(this.asReference, this.type);
            } else if (c == 'S') {
                writer.putNextShort((short) this.asInt);
            } else if (c != 'Z') {
                switch (c) {
                    case 'B':
                        writer.putNextByte((byte) this.asInt);
                        return;
                    case 'C':
                        writer.putNextChar((char) this.asInt);
                        return;
                    case 'D':
                        writer.putNextDouble(this.asDouble);
                        return;
                    default:
                        switch (c) {
                            case 'I':
                                writer.putNextInt(this.asInt);
                                return;
                            case 'J':
                                writer.putNextLong(this.asLong);
                                return;
                            default:
                                throw new AssertionError((Object) "Unexpected typeChar: " + this.typeChar);
                        }
                }
            } else {
                boolean z = true;
                if (this.asInt != 1) {
                    z = false;
                }
                writer.putNextBoolean(z);
            }
        }
    }

    static class Construct extends Transformer {
        private final EmulatedStackFrame.Range callerRange = EmulatedStackFrame.Range.all(type());
        private final MethodHandle constructorHandle;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Construct.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Construct.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.Construct.transform(dalvik.system.EmulatedStackFrame):void");
        }

        Construct(MethodHandle constructorHandle2, MethodType returnedType) {
            super(returnedType);
            this.constructorHandle = constructorHandle2;
        }

        /* access modifiers changed from: package-private */
        public MethodHandle getConstructorHandle() {
            return this.constructorHandle;
        }

        private static boolean isAbstract(Class<?> klass) {
            return (klass.getModifiers() & 1024) == 1024;
        }

        private static void checkInstantiable(Class<?> klass) throws InstantiationException {
            if (isAbstract(klass)) {
                String s = klass.isInterface() ? "interface " : "abstract class ";
                throw new InstantiationException("Can't instantiate " + s + klass);
            }
        }
    }

    public static class DropArguments extends Transformer {
        private final MethodHandle delegate;
        private final EmulatedStackFrame.Range range1;
        private final EmulatedStackFrame.Range range2;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.DropArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.DropArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.DropArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public DropArguments(MethodType type, MethodHandle delegate2, int startPos, int numDropped) {
            super(type);
            this.delegate = delegate2;
            this.range1 = EmulatedStackFrame.Range.of(type, 0, startPos);
            int numArgs = type.ptypes().length;
            if (startPos + numDropped < numArgs) {
                this.range2 = EmulatedStackFrame.Range.of(type, startPos + numDropped, numArgs);
            } else {
                this.range2 = null;
            }
        }
    }

    public static class ExplicitCastArguments extends Transformer {
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.ExplicitCastArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.ExplicitCastArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.ExplicitCastArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public ExplicitCastArguments(MethodHandle target2, MethodType type) {
            super(type);
            this.target = target2;
        }

        private void explicitCastArguments(EmulatedStackFrame callerFrame, EmulatedStackFrame targetFrame) {
            EmulatedStackFrame.StackFrameReader reader = new EmulatedStackFrame.StackFrameReader();
            reader.attach(callerFrame);
            EmulatedStackFrame.StackFrameWriter writer = new EmulatedStackFrame.StackFrameWriter();
            writer.attach(targetFrame);
            Class<?>[] fromTypes = type().ptypes();
            Class<?>[] toTypes = this.target.type().ptypes();
            for (int i = 0; i < fromTypes.length; i++) {
                explicitCast(reader, fromTypes[i], writer, toTypes[i]);
            }
        }

        private void explicitCastReturnValue(EmulatedStackFrame callerFrame, EmulatedStackFrame targetFrame) {
            Class<?> from = this.target.type().rtype();
            Class<?> to = type().rtype();
            if (to != Void.TYPE) {
                EmulatedStackFrame.StackFrameWriter writer = new EmulatedStackFrame.StackFrameWriter();
                writer.attach(callerFrame);
                writer.makeReturnValueAccessor();
                if (from != Void.TYPE) {
                    EmulatedStackFrame.StackFrameReader reader = new EmulatedStackFrame.StackFrameReader();
                    reader.attach(targetFrame);
                    reader.makeReturnValueAccessor();
                    explicitCast(reader, this.target.type().rtype(), writer, type().rtype());
                } else if (to.isPrimitive()) {
                    unboxNull(writer, to);
                } else {
                    writer.putNextReference(null, to);
                }
            }
        }

        private static void throwUnexpectedType(Class<?> unexpectedType) {
            throw new InternalError("Unexpected type: " + unexpectedType);
        }

        private static void explicitCastFromBoolean(boolean fromValue, EmulatedStackFrame.StackFrameWriter writer, Class<?> to) {
            int value = fromValue;
            if (to == Byte.TYPE) {
                writer.putNextByte((byte) value);
            } else if (to == Character.TYPE) {
                writer.putNextChar((char) value);
            } else if (to == Short.TYPE) {
                writer.putNextShort((short) value);
            } else if (to == Integer.TYPE) {
                writer.putNextInt(value);
            } else if (to == Long.TYPE) {
                writer.putNextLong((long) value);
            } else if (to == Float.TYPE) {
                writer.putNextFloat((float) value);
            } else if (to == Double.TYPE) {
                writer.putNextDouble((double) value);
            } else {
                throwUnexpectedType(to);
            }
        }

        private static boolean toBoolean(byte value) {
            return (value & 1) == 1;
        }

        private static byte readPrimitiveAsByte(EmulatedStackFrame.StackFrameReader reader, Class<?> from) {
            if (from == Byte.TYPE) {
                return reader.nextByte();
            }
            if (from == Character.TYPE) {
                return (byte) reader.nextChar();
            }
            if (from == Short.TYPE) {
                return (byte) reader.nextShort();
            }
            if (from == Integer.TYPE) {
                return (byte) reader.nextInt();
            }
            if (from == Long.TYPE) {
                return (byte) ((int) reader.nextLong());
            }
            if (from == Float.TYPE) {
                return (byte) ((int) reader.nextFloat());
            }
            if (from == Double.TYPE) {
                return (byte) ((int) reader.nextDouble());
            }
            throwUnexpectedType(from);
            return 0;
        }

        private static char readPrimitiveAsChar(EmulatedStackFrame.StackFrameReader reader, Class<?> from) {
            if (from == Byte.TYPE) {
                return (char) reader.nextByte();
            }
            if (from == Character.TYPE) {
                return reader.nextChar();
            }
            if (from == Short.TYPE) {
                return (char) reader.nextShort();
            }
            if (from == Integer.TYPE) {
                return (char) reader.nextInt();
            }
            if (from == Long.TYPE) {
                return (char) ((int) reader.nextLong());
            }
            if (from == Float.TYPE) {
                return (char) ((int) reader.nextFloat());
            }
            if (from == Double.TYPE) {
                return (char) ((int) reader.nextDouble());
            }
            throwUnexpectedType(from);
            return 0;
        }

        private static short readPrimitiveAsShort(EmulatedStackFrame.StackFrameReader reader, Class<?> from) {
            if (from == Byte.TYPE) {
                return (short) reader.nextByte();
            }
            if (from == Character.TYPE) {
                return (short) reader.nextChar();
            }
            if (from == Short.TYPE) {
                return reader.nextShort();
            }
            if (from == Integer.TYPE) {
                return (short) reader.nextInt();
            }
            if (from == Long.TYPE) {
                return (short) ((int) reader.nextLong());
            }
            if (from == Float.TYPE) {
                return (short) ((int) reader.nextFloat());
            }
            if (from == Double.TYPE) {
                return (short) ((int) reader.nextDouble());
            }
            throwUnexpectedType(from);
            return 0;
        }

        private static int readPrimitiveAsInt(EmulatedStackFrame.StackFrameReader reader, Class<?> from) {
            if (from == Byte.TYPE) {
                return reader.nextByte();
            }
            if (from == Character.TYPE) {
                return reader.nextChar();
            }
            if (from == Short.TYPE) {
                return reader.nextShort();
            }
            if (from == Integer.TYPE) {
                return reader.nextInt();
            }
            if (from == Long.TYPE) {
                return (int) reader.nextLong();
            }
            if (from == Float.TYPE) {
                return (int) reader.nextFloat();
            }
            if (from == Double.TYPE) {
                return (int) reader.nextDouble();
            }
            throwUnexpectedType(from);
            return 0;
        }

        private static long readPrimitiveAsLong(EmulatedStackFrame.StackFrameReader reader, Class<?> from) {
            if (from == Byte.TYPE) {
                return (long) reader.nextByte();
            }
            if (from == Character.TYPE) {
                return (long) reader.nextChar();
            }
            if (from == Short.TYPE) {
                return (long) reader.nextShort();
            }
            if (from == Integer.TYPE) {
                return (long) reader.nextInt();
            }
            if (from == Long.TYPE) {
                return reader.nextLong();
            }
            if (from == Float.TYPE) {
                return (long) reader.nextFloat();
            }
            if (from == Double.TYPE) {
                return (long) reader.nextDouble();
            }
            throwUnexpectedType(from);
            return 0;
        }

        private static float readPrimitiveAsFloat(EmulatedStackFrame.StackFrameReader reader, Class<?> from) {
            if (from == Byte.TYPE) {
                return (float) reader.nextByte();
            }
            if (from == Character.TYPE) {
                return (float) reader.nextChar();
            }
            if (from == Short.TYPE) {
                return (float) reader.nextShort();
            }
            if (from == Integer.TYPE) {
                return (float) reader.nextInt();
            }
            if (from == Long.TYPE) {
                return (float) reader.nextLong();
            }
            if (from == Float.TYPE) {
                return reader.nextFloat();
            }
            if (from == Double.TYPE) {
                return (float) reader.nextDouble();
            }
            throwUnexpectedType(from);
            return 0.0f;
        }

        private static double readPrimitiveAsDouble(EmulatedStackFrame.StackFrameReader reader, Class<?> from) {
            if (from == Byte.TYPE) {
                return (double) reader.nextByte();
            }
            if (from == Character.TYPE) {
                return (double) reader.nextChar();
            }
            if (from == Short.TYPE) {
                return (double) reader.nextShort();
            }
            if (from == Integer.TYPE) {
                return (double) reader.nextInt();
            }
            if (from == Long.TYPE) {
                return (double) reader.nextLong();
            }
            if (from == Float.TYPE) {
                return (double) reader.nextFloat();
            }
            if (from == Double.TYPE) {
                return reader.nextDouble();
            }
            throwUnexpectedType(from);
            return 0.0d;
        }

        private static void explicitCastToBoolean(EmulatedStackFrame.StackFrameReader reader, Class<?> from, EmulatedStackFrame.StackFrameWriter writer) {
            writer.putNextBoolean(toBoolean(readPrimitiveAsByte(reader, from)));
        }

        private static void explicitCastPrimitives(EmulatedStackFrame.StackFrameReader reader, Class<?> from, EmulatedStackFrame.StackFrameWriter writer, Class<?> to) {
            if (to == Byte.TYPE) {
                writer.putNextByte(readPrimitiveAsByte(reader, from));
            } else if (to == Character.TYPE) {
                writer.putNextChar(readPrimitiveAsChar(reader, from));
            } else if (to == Short.TYPE) {
                writer.putNextShort(readPrimitiveAsShort(reader, from));
            } else if (to == Integer.TYPE) {
                writer.putNextInt(readPrimitiveAsInt(reader, from));
            } else if (to == Long.TYPE) {
                writer.putNextLong(readPrimitiveAsLong(reader, from));
            } else if (to == Float.TYPE) {
                writer.putNextFloat(readPrimitiveAsFloat(reader, from));
            } else if (to == Double.TYPE) {
                writer.putNextDouble(readPrimitiveAsDouble(reader, from));
            } else {
                throwUnexpectedType(to);
            }
        }

        private static void unboxNull(EmulatedStackFrame.StackFrameWriter writer, Class<?> to) {
            if (to == Boolean.TYPE) {
                writer.putNextBoolean(false);
            } else if (to == Byte.TYPE) {
                writer.putNextByte((byte) 0);
            } else if (to == Character.TYPE) {
                writer.putNextChar(0);
            } else if (to == Short.TYPE) {
                writer.putNextShort(0);
            } else if (to == Integer.TYPE) {
                writer.putNextInt(0);
            } else if (to == Long.TYPE) {
                writer.putNextLong(0);
            } else if (to == Float.TYPE) {
                writer.putNextFloat(0.0f);
            } else if (to == Double.TYPE) {
                writer.putNextDouble(0.0d);
            } else {
                throwUnexpectedType(to);
            }
        }

        private static void unboxNonNull(Object ref, Class<?> from, EmulatedStackFrame.StackFrameWriter writer, Class<?> to) {
            if (to == Boolean.TYPE) {
                if (from == Boolean.class) {
                    writer.putNextBoolean(((Boolean) ref).booleanValue());
                } else if (from == Float.class || from == Double.class) {
                    writer.putNextBoolean(toBoolean((byte) ((int) ((Double) ref).doubleValue())));
                } else {
                    writer.putNextBoolean(toBoolean((byte) ((int) ((Long) ref).longValue())));
                }
            } else if (to == Byte.TYPE) {
                writer.putNextByte(((Byte) ref).byteValue());
            } else if (to == Character.TYPE) {
                writer.putNextChar(((Character) ref).charValue());
            } else if (to == Short.TYPE) {
                writer.putNextShort(((Short) ref).shortValue());
            } else if (to == Integer.TYPE) {
                writer.putNextInt(((Integer) ref).intValue());
            } else if (to == Long.TYPE) {
                writer.putNextLong(((Long) ref).longValue());
            } else if (to == Float.TYPE) {
                writer.putNextFloat(((Float) ref).floatValue());
            } else if (to == Double.TYPE) {
                writer.putNextDouble(((Double) ref).doubleValue());
            } else {
                throwUnexpectedType(to);
            }
        }

        private static void unbox(Object ref, Class<?> from, EmulatedStackFrame.StackFrameWriter writer, Class<?> to) {
            if (ref == null) {
                unboxNull(writer, to);
            } else {
                unboxNonNull(ref, from, writer, to);
            }
        }

        private static void box(EmulatedStackFrame.StackFrameReader reader, Class<?> from, EmulatedStackFrame.StackFrameWriter writer, Class<?> to) {
            Object boxed = null;
            if (from == Boolean.TYPE) {
                boxed = Boolean.valueOf(reader.nextBoolean());
            } else if (from == Byte.TYPE) {
                boxed = Byte.valueOf(reader.nextByte());
            } else if (from == Character.TYPE) {
                boxed = Character.valueOf(reader.nextChar());
            } else if (from == Short.TYPE) {
                boxed = Short.valueOf(reader.nextShort());
            } else if (from == Integer.TYPE) {
                boxed = Integer.valueOf(reader.nextInt());
            } else if (from == Long.TYPE) {
                boxed = Long.valueOf(reader.nextLong());
            } else if (from == Float.TYPE) {
                boxed = Float.valueOf(reader.nextFloat());
            } else if (from == Double.TYPE) {
                boxed = Double.valueOf(reader.nextDouble());
            } else {
                throwUnexpectedType(from);
            }
            writer.putNextReference(to.cast(boxed), to);
        }

        private static void explicitCast(EmulatedStackFrame.StackFrameReader reader, Class<?> from, EmulatedStackFrame.StackFrameWriter writer, Class<?> to) {
            if (from.equals(to)) {
                EmulatedStackFrame.StackFrameAccessor.copyNext(reader, writer, from);
            } else if (!from.isPrimitive()) {
                Object ref = reader.nextReference(from);
                if (to.isInterface()) {
                    writer.putNextReference(ref, to);
                } else if (!to.isPrimitive()) {
                    writer.putNextReference(to.cast(ref), to);
                } else {
                    unbox(ref, from, writer, to);
                }
            } else if (!to.isPrimitive()) {
                box(reader, from, writer, to);
            } else if (from == Boolean.TYPE) {
                explicitCastFromBoolean(reader.nextBoolean(), writer, to);
            } else if (to == Boolean.TYPE) {
                explicitCastToBoolean(reader, from, writer);
            } else {
                explicitCastPrimitives(reader, from, writer, to);
            }
        }
    }

    static class FilterArguments extends Transformer {
        private final MethodHandle[] filters;
        private final int pos;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FilterArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FilterArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.FilterArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        FilterArguments(MethodHandle target2, int pos2, MethodHandle[] filters2) {
            super(deriveType(target2, pos2, filters2));
            this.target = target2;
            this.pos = pos2;
            this.filters = filters2;
        }

        private static MethodType deriveType(MethodHandle target2, int pos2, MethodHandle[] filters2) {
            Class<?>[] filterArgs = new Class[filters2.length];
            for (int i = 0; i < filters2.length; i++) {
                filterArgs[i] = filters2[i].type().parameterType(0);
            }
            return target2.type().replaceParameterTypes(pos2, filters2.length + pos2, filterArgs);
        }
    }

    public static class FilterReturnValue extends Transformer {
        private final EmulatedStackFrame.Range allArgs = EmulatedStackFrame.Range.all(type());
        private final MethodHandle filter;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FilterReturnValue.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FilterReturnValue.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.FilterReturnValue.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public FilterReturnValue(MethodHandle target2, MethodHandle filter2) {
            super(MethodType.methodType(filter2.type().rtype(), (Class<?>[]) target2.type().ptypes()));
            this.target = target2;
            this.filter = filter2;
        }
    }

    static class FoldArguments extends Transformer {
        private final MethodHandle combiner;
        private final EmulatedStackFrame.Range combinerArgs;
        private final int referencesOffset;
        private final int stackFrameOffset;
        private final MethodHandle target;
        private final EmulatedStackFrame.Range targetArgs = EmulatedStackFrame.Range.all(type());

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FoldArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FoldArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.FoldArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        FoldArguments(MethodHandle target2, MethodHandle combiner2) {
            super(deriveType(target2, combiner2));
            this.target = target2;
            this.combiner = combiner2;
            this.combinerArgs = EmulatedStackFrame.Range.all(combiner2.type());
            Class<?> combinerRType = combiner2.type().rtype();
            if (combinerRType == Void.TYPE) {
                this.stackFrameOffset = 0;
                this.referencesOffset = 0;
            } else if (combinerRType.isPrimitive()) {
                this.stackFrameOffset = EmulatedStackFrame.getSize(combinerRType);
                this.referencesOffset = 0;
            } else {
                this.stackFrameOffset = 0;
                this.referencesOffset = 1;
            }
        }

        private static MethodType deriveType(MethodHandle target2, MethodHandle combiner2) {
            if (combiner2.type().rtype() == Void.TYPE) {
                return target2.type();
            }
            return target2.type().dropParameterTypes(0, 1);
        }
    }

    public static class GuardWithTest extends Transformer {
        private final MethodHandle fallback;
        private final MethodHandle target;
        private final MethodHandle test;
        private final EmulatedStackFrame.Range testArgsRange;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.GuardWithTest.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.GuardWithTest.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.GuardWithTest.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public GuardWithTest(MethodHandle test2, MethodHandle target2, MethodHandle fallback2) {
            super(target2.type());
            this.test = test2;
            this.target = target2;
            this.fallback = fallback2;
            this.testArgsRange = EmulatedStackFrame.Range.of(target2.type(), 0, test2.type().parameterCount());
        }
    }

    static class InsertArguments extends Transformer {
        private final int pos;
        private final EmulatedStackFrame.Range range1;
        private final EmulatedStackFrame.Range range2;
        private final MethodHandle target;
        private final Object[] values;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.InsertArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.InsertArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.InsertArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        InsertArguments(MethodHandle target2, int pos2, Object[] values2) {
            super(target2.type().dropParameterTypes(pos2, values2.length + pos2));
            this.target = target2;
            this.pos = pos2;
            this.values = values2;
            MethodType type = type();
            this.range1 = EmulatedStackFrame.Range.of(type, 0, pos2);
            this.range2 = EmulatedStackFrame.Range.of(type, pos2, type.parameterCount());
        }
    }

    static class Invoker extends Transformer {
        private final EmulatedStackFrame.Range copyRange = EmulatedStackFrame.Range.of(type(), 1, type().parameterCount());
        private final boolean isExactInvoker;
        private final MethodType targetType;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Invoker.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Invoker.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.Invoker.transform(dalvik.system.EmulatedStackFrame):void");
        }

        Invoker(MethodType targetType2, boolean isExactInvoker2) {
            super(targetType2.insertParameterTypes(0, (Class<?>[]) new Class[]{MethodHandle.class}));
            this.targetType = targetType2;
            this.isExactInvoker = isExactInvoker2;
        }
    }

    public static class PermuteArguments extends Transformer {
        private final int[] reorder;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.PermuteArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.PermuteArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.PermuteArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public PermuteArguments(MethodType type, MethodHandle target2, int[] reorder2) {
            super(type);
            this.target = target2;
            this.reorder = reorder2;
        }
    }

    public static class ReferenceArrayElementGetter extends Transformer {
        private final Class<?> arrayClass;

        public ReferenceArrayElementGetter(Class<?> arrayClass2) {
            super(MethodType.methodType(arrayClass2.getComponentType(), (Class<?>[]) new Class[]{arrayClass2, Integer.TYPE}));
            this.arrayClass = arrayClass2;
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            EmulatedStackFrame.StackFrameReader reader = new EmulatedStackFrame.StackFrameReader();
            reader.attach(emulatedStackFrame);
            int index = reader.nextInt();
            EmulatedStackFrame.StackFrameWriter writer = new EmulatedStackFrame.StackFrameWriter();
            writer.attach(emulatedStackFrame);
            writer.makeReturnValueAccessor();
            writer.putNextReference(((Object[]) reader.nextReference(this.arrayClass))[index], this.arrayClass.getComponentType());
        }
    }

    public static class ReferenceArrayElementSetter extends Transformer {
        private final Class<?> arrayClass;

        public ReferenceArrayElementSetter(Class<?> arrayClass2) {
            super(MethodType.methodType((Class<?>) Void.TYPE, (Class<?>[]) new Class[]{arrayClass2, Integer.TYPE, arrayClass2.getComponentType()}));
            this.arrayClass = arrayClass2;
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            EmulatedStackFrame.StackFrameReader reader = new EmulatedStackFrame.StackFrameReader();
            reader.attach(emulatedStackFrame);
            ((Object[]) reader.nextReference(this.arrayClass))[reader.nextInt()] = reader.nextReference(this.arrayClass.getComponentType());
        }
    }

    public static class ReferenceIdentity extends Transformer {
        private final Class<?> type;

        public ReferenceIdentity(Class<?> type2) {
            super(MethodType.methodType(type2, type2));
            this.type = type2;
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            EmulatedStackFrame.StackFrameReader reader = new EmulatedStackFrame.StackFrameReader();
            reader.attach(emulatedStackFrame);
            EmulatedStackFrame.StackFrameWriter writer = new EmulatedStackFrame.StackFrameWriter();
            writer.attach(emulatedStackFrame);
            writer.makeReturnValueAccessor();
            writer.putNextReference(reader.nextReference(this.type), this.type);
        }
    }

    static class Spreader extends Transformer {
        private final int arrayOffset;
        private final char arrayTypeChar;
        private final EmulatedStackFrame.Range copyRange;
        private final int numArrayArgs;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Spreader.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Spreader.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.Spreader.transform(dalvik.system.EmulatedStackFrame):void");
        }

        Spreader(MethodHandle target2, MethodType spreaderType, int numArrayArgs2) {
            super(spreaderType);
            this.target = target2;
            this.arrayOffset = spreaderType.parameterCount() - 1;
            Class<?> componentType = spreaderType.ptypes()[this.arrayOffset].getComponentType();
            if (componentType != null) {
                this.arrayTypeChar = Wrapper.basicTypeChar(componentType);
                this.numArrayArgs = numArrayArgs2;
                this.copyRange = EmulatedStackFrame.Range.of(spreaderType, 0, this.arrayOffset);
                return;
            }
            throw new AssertionError((Object) "Trailing argument must be an array.");
        }

        public static void spreadArray(Object[] array, EmulatedStackFrame.StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                Long l = array[i];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'F') {
                    writer.putNextFloat(l.floatValue());
                } else if (basicTypeChar == 'L') {
                    writer.putNextReference(l, argumentType);
                } else if (basicTypeChar == 'S') {
                    writer.putNextShort(l.shortValue());
                } else if (basicTypeChar != 'Z') {
                    switch (basicTypeChar) {
                        case 'B':
                            writer.putNextByte(l.byteValue());
                            break;
                        case 'C':
                            writer.putNextChar(l.charValue());
                            break;
                        case 'D':
                            writer.putNextDouble(l.doubleValue());
                            break;
                        default:
                            switch (basicTypeChar) {
                                case 'I':
                                    writer.putNextInt(l.intValue());
                                    break;
                                case 'J':
                                    writer.putNextLong(l.longValue());
                                    break;
                            }
                    }
                } else {
                    writer.putNextBoolean(l.booleanValue());
                }
            }
        }

        public static void spreadArray(int[] array, EmulatedStackFrame.StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                int j = array[i];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'D') {
                    writer.putNextDouble((double) j);
                } else if (basicTypeChar == 'F') {
                    writer.putNextFloat((float) j);
                } else if (basicTypeChar != 'L') {
                    switch (basicTypeChar) {
                        case 'I':
                            writer.putNextInt(j);
                            break;
                        case 'J':
                            writer.putNextLong((long) j);
                            break;
                        default:
                            throw new AssertionError();
                    }
                } else {
                    writer.putNextReference(Integer.valueOf(j), argumentType);
                }
            }
        }

        public static void spreadArray(long[] array, EmulatedStackFrame.StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                long l = array[i];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'D') {
                    writer.putNextDouble((double) l);
                } else if (basicTypeChar == 'F') {
                    writer.putNextFloat((float) l);
                } else if (basicTypeChar == 'J') {
                    writer.putNextLong(l);
                } else if (basicTypeChar == 'L') {
                    writer.putNextReference(Long.valueOf(l), argumentType);
                } else {
                    throw new AssertionError();
                }
            }
        }

        public static void spreadArray(byte[] array, EmulatedStackFrame.StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                byte b = array[i];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'B') {
                    writer.putNextByte(b);
                } else if (basicTypeChar == 'D') {
                    writer.putNextDouble((double) b);
                } else if (basicTypeChar == 'F') {
                    writer.putNextFloat((float) b);
                } else if (basicTypeChar == 'L') {
                    writer.putNextReference(Byte.valueOf(b), argumentType);
                } else if (basicTypeChar != 'S') {
                    switch (basicTypeChar) {
                        case 'I':
                            writer.putNextInt(b);
                            break;
                        case 'J':
                            writer.putNextLong((long) b);
                            break;
                        default:
                            throw new AssertionError();
                    }
                } else {
                    writer.putNextShort((short) b);
                }
            }
        }

        public static void spreadArray(short[] array, EmulatedStackFrame.StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                short s = array[i];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'D') {
                    writer.putNextDouble((double) s);
                } else if (basicTypeChar == 'F') {
                    writer.putNextFloat((float) s);
                } else if (basicTypeChar == 'L') {
                    writer.putNextReference(Short.valueOf(s), argumentType);
                } else if (basicTypeChar != 'S') {
                    switch (basicTypeChar) {
                        case 'I':
                            writer.putNextInt(s);
                            break;
                        case 'J':
                            writer.putNextLong((long) s);
                            break;
                        default:
                            throw new AssertionError();
                    }
                } else {
                    writer.putNextShort(s);
                }
            }
        }

        public static void spreadArray(char[] array, EmulatedStackFrame.StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                char c = array[i];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'C':
                        writer.putNextChar(c);
                        break;
                    case 'D':
                        writer.putNextDouble((double) c);
                        break;
                    case Types.DATALINK /*70*/:
                        writer.putNextFloat((float) c);
                        break;
                    case 'I':
                        writer.putNextInt(c);
                        break;
                    case 'J':
                        writer.putNextLong((long) c);
                        break;
                    case 'L':
                        writer.putNextReference(Character.valueOf(c), argumentType);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        public static void spreadArray(boolean[] array, EmulatedStackFrame.StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                boolean z = array[i];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'L') {
                    writer.putNextReference(Boolean.valueOf(z), argumentType);
                } else if (basicTypeChar == 'Z') {
                    writer.putNextBoolean(z);
                } else {
                    throw new AssertionError();
                }
            }
        }

        public static void spreadArray(double[] array, EmulatedStackFrame.StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                double d = array[i];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'D') {
                    writer.putNextDouble(d);
                } else if (basicTypeChar == 'L') {
                    writer.putNextReference(Double.valueOf(d), argumentType);
                } else {
                    throw new AssertionError();
                }
            }
        }

        public static void spreadArray(float[] array, EmulatedStackFrame.StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                float f = array[i];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'D') {
                    writer.putNextDouble((double) f);
                } else if (basicTypeChar == 'F') {
                    writer.putNextFloat(f);
                } else if (basicTypeChar == 'L') {
                    writer.putNextReference(Float.valueOf(f), argumentType);
                } else {
                    throw new AssertionError();
                }
            }
        }
    }

    public static abstract class Transformer extends MethodHandle implements Cloneable {
        protected Transformer(MethodType type) {
            super(Transformers.TRANSFORM_INTERNAL.getArtMethod(), 5, type);
        }

        protected Transformer(MethodType type, int invokeKind) {
            super(Transformers.TRANSFORM_INTERNAL.getArtMethod(), invokeKind, type);
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    static class VarargsCollector extends Transformer {
        final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.VarargsCollector.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:292)
            	at jadx.core.ProcessClass.process(ProcessClass.java:36)
            	at java.util.ArrayList.forEach(ArrayList.java:1257)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
            	... 6 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.VarargsCollector.transform(dalvik.system.EmulatedStackFrame):void, dex: boot_classes.dex
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.VarargsCollector.transform(dalvik.system.EmulatedStackFrame):void");
        }

        VarargsCollector(MethodHandle target2) {
            super(target2.type(), 6);
            if (lastParameterTypeIsAnArray(target2.type().ptypes())) {
                this.target = target2;
                return;
            }
            throw new IllegalArgumentException("target does not have array as last parameter");
        }

        private static boolean lastParameterTypeIsAnArray(Class<?>[] parameterTypes) {
            if (parameterTypes.length == 0) {
                return false;
            }
            return parameterTypes[parameterTypes.length - 1].isArray();
        }

        public boolean isVarargsCollector() {
            return true;
        }

        public MethodHandle asFixedArity() {
            return this.target;
        }

        private static void throwWrongMethodTypeException(MethodType from, MethodType to) {
            throw new WrongMethodTypeException("Cannot convert " + from + " to " + to);
        }

        private static boolean arityArgumentsConvertible(Class<?>[] ptypes, int arityStart, Class<?> elementType) {
            if (ptypes.length - 1 == arityStart && ptypes[arityStart].isArray() && ptypes[arityStart].getComponentType() == elementType) {
                return true;
            }
            for (int i = arityStart; i < ptypes.length; i++) {
                if (!MethodType.canConvert(ptypes[i], elementType)) {
                    return false;
                }
            }
            return true;
        }

        private static Object referenceArray(EmulatedStackFrame.StackFrameReader reader, Class<?>[] ptypes, Class<?> elementType, int offset, int length) {
            Object arityArray = Array.newInstance(elementType, length);
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                Object o = null;
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'F') {
                    o = Float.valueOf(reader.nextFloat());
                } else if (basicTypeChar == 'L') {
                    o = reader.nextReference(argumentType);
                } else if (basicTypeChar == 'S') {
                    o = Short.valueOf(reader.nextShort());
                } else if (basicTypeChar != 'Z') {
                    switch (basicTypeChar) {
                        case 'B':
                            o = Byte.valueOf(reader.nextByte());
                            break;
                        case 'C':
                            o = Character.valueOf(reader.nextChar());
                            break;
                        case 'D':
                            o = Double.valueOf(reader.nextDouble());
                            break;
                        default:
                            switch (basicTypeChar) {
                                case 'I':
                                    o = Integer.valueOf(reader.nextInt());
                                    break;
                                case 'J':
                                    o = Long.valueOf(reader.nextLong());
                                    break;
                            }
                    }
                } else {
                    o = Boolean.valueOf(reader.nextBoolean());
                }
                Array.set(arityArray, i, elementType.cast(o));
            }
            return arityArray;
        }

        private static Object intArray(EmulatedStackFrame.StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            int[] arityArray = new int[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'B') {
                    arityArray[i] = reader.nextByte();
                } else if (basicTypeChar == 'I') {
                    arityArray[i] = reader.nextInt();
                } else if (basicTypeChar != 'S') {
                    arityArray[i] = ((Integer) reader.nextReference(argumentType)).intValue();
                } else {
                    arityArray[i] = reader.nextShort();
                }
            }
            return arityArray;
        }

        private static Object longArray(EmulatedStackFrame.StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            long[] arityArray = new long[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'B') {
                    arityArray[i] = (long) reader.nextByte();
                } else if (basicTypeChar != 'S') {
                    switch (basicTypeChar) {
                        case 'I':
                            arityArray[i] = (long) reader.nextInt();
                            break;
                        case 'J':
                            arityArray[i] = reader.nextLong();
                            break;
                        default:
                            arityArray[i] = ((Long) reader.nextReference(argumentType)).longValue();
                            break;
                    }
                } else {
                    arityArray[i] = (long) reader.nextShort();
                }
            }
            return arityArray;
        }

        private static Object byteArray(EmulatedStackFrame.StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            byte[] arityArray = new byte[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                if (Wrapper.basicTypeChar(argumentType) != 'B') {
                    arityArray[i] = ((Byte) reader.nextReference(argumentType)).byteValue();
                } else {
                    arityArray[i] = reader.nextByte();
                }
            }
            return arityArray;
        }

        private static Object shortArray(EmulatedStackFrame.StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            short[] arityArray = new short[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'B') {
                    arityArray[i] = (short) reader.nextByte();
                } else if (basicTypeChar != 'S') {
                    arityArray[i] = ((Short) reader.nextReference(argumentType)).shortValue();
                } else {
                    arityArray[i] = reader.nextShort();
                }
            }
            return arityArray;
        }

        private static Object charArray(EmulatedStackFrame.StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            char[] arityArray = new char[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                if (Wrapper.basicTypeChar(argumentType) != 'C') {
                    arityArray[i] = ((Character) reader.nextReference(argumentType)).charValue();
                } else {
                    arityArray[i] = reader.nextChar();
                }
            }
            return arityArray;
        }

        private static Object booleanArray(EmulatedStackFrame.StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            boolean[] arityArray = new boolean[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                if (Wrapper.basicTypeChar(argumentType) != 'Z') {
                    arityArray[i] = ((Boolean) reader.nextReference(argumentType)).booleanValue();
                } else {
                    arityArray[i] = reader.nextBoolean();
                }
            }
            return arityArray;
        }

        private static Object floatArray(EmulatedStackFrame.StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            float[] arityArray = new float[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'B') {
                    arityArray[i] = (float) reader.nextByte();
                } else if (basicTypeChar == 'F') {
                    arityArray[i] = reader.nextFloat();
                } else if (basicTypeChar != 'S') {
                    switch (basicTypeChar) {
                        case 'I':
                            arityArray[i] = (float) reader.nextInt();
                            break;
                        case 'J':
                            arityArray[i] = (float) reader.nextLong();
                            break;
                        default:
                            arityArray[i] = ((Float) reader.nextReference(argumentType)).floatValue();
                            break;
                    }
                } else {
                    arityArray[i] = (float) reader.nextShort();
                }
            }
            return arityArray;
        }

        private static Object doubleArray(EmulatedStackFrame.StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            double[] arityArray = new double[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                char basicTypeChar = Wrapper.basicTypeChar(argumentType);
                if (basicTypeChar == 'B') {
                    arityArray[i] = (double) reader.nextByte();
                } else if (basicTypeChar == 'D') {
                    arityArray[i] = reader.nextDouble();
                } else if (basicTypeChar == 'F') {
                    arityArray[i] = (double) reader.nextFloat();
                } else if (basicTypeChar != 'S') {
                    switch (basicTypeChar) {
                        case 'I':
                            arityArray[i] = (double) reader.nextInt();
                            break;
                        case 'J':
                            arityArray[i] = (double) reader.nextLong();
                            break;
                        default:
                            arityArray[i] = ((Double) reader.nextReference(argumentType)).doubleValue();
                            break;
                    }
                } else {
                    arityArray[i] = (double) reader.nextShort();
                }
            }
            return arityArray;
        }

        private static Object makeArityArray(MethodType callerFrameType, EmulatedStackFrame.StackFrameReader callerFrameReader, int indexOfArityArray, Class<?> arityArrayType) {
            int arityArrayLength = callerFrameType.ptypes().length - indexOfArityArray;
            Class<?> elementType = arityArrayType.getComponentType();
            Class<?>[] callerPTypes = callerFrameType.ptypes();
            char elementBasicType = Wrapper.basicTypeChar(elementType);
            if (elementBasicType == 'F') {
                return floatArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
            }
            if (elementBasicType == 'L') {
                return referenceArray(callerFrameReader, callerPTypes, elementType, indexOfArityArray, arityArrayLength);
            }
            if (elementBasicType == 'S') {
                return shortArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
            }
            if (elementBasicType == 'Z') {
                return booleanArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
            }
            switch (elementBasicType) {
                case 'B':
                    return byteArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                case 'C':
                    return charArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                case 'D':
                    return doubleArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                default:
                    switch (elementBasicType) {
                        case 'I':
                            return intArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                        case 'J':
                            return longArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                        default:
                            throw new InternalError("Unexpected type: " + elementType);
                    }
            }
        }

        public static Object collectArguments(char basicComponentType, Class<?> componentType, EmulatedStackFrame.StackFrameReader reader, Class<?>[] types, int startIdx, int length) {
            if (basicComponentType == 'F') {
                return floatArray(reader, types, startIdx, length);
            }
            if (basicComponentType == 'L') {
                return referenceArray(reader, types, componentType, startIdx, length);
            }
            if (basicComponentType == 'S') {
                return shortArray(reader, types, startIdx, length);
            }
            if (basicComponentType == 'Z') {
                return booleanArray(reader, types, startIdx, length);
            }
            switch (basicComponentType) {
                case 'B':
                    return byteArray(reader, types, startIdx, length);
                case 'C':
                    return charArray(reader, types, startIdx, length);
                case 'D':
                    return doubleArray(reader, types, startIdx, length);
                default:
                    switch (basicComponentType) {
                        case 'I':
                            return intArray(reader, types, startIdx, length);
                        case 'J':
                            return longArray(reader, types, startIdx, length);
                        default:
                            throw new InternalError("Unexpected type: " + basicComponentType);
                    }
            }
        }

        private static void copyParameter(EmulatedStackFrame.StackFrameReader reader, EmulatedStackFrame.StackFrameWriter writer, Class<?> ptype) {
            char basicTypeChar = Wrapper.basicTypeChar(ptype);
            if (basicTypeChar == 'F') {
                writer.putNextFloat(reader.nextFloat());
            } else if (basicTypeChar == 'L') {
                writer.putNextReference(reader.nextReference(ptype), ptype);
            } else if (basicTypeChar == 'S') {
                writer.putNextShort(reader.nextShort());
            } else if (basicTypeChar != 'Z') {
                switch (basicTypeChar) {
                    case 'B':
                        writer.putNextByte(reader.nextByte());
                        return;
                    case 'C':
                        writer.putNextChar(reader.nextChar());
                        return;
                    case 'D':
                        writer.putNextDouble(reader.nextDouble());
                        return;
                    default:
                        switch (basicTypeChar) {
                            case 'I':
                                writer.putNextInt(reader.nextInt());
                                return;
                            case 'J':
                                writer.putNextLong(reader.nextLong());
                                return;
                            default:
                                throw new InternalError("Unexpected type: " + ptype);
                        }
                }
            } else {
                writer.putNextBoolean(reader.nextBoolean());
            }
        }

        private static void prepareFrame(EmulatedStackFrame callerFrame, EmulatedStackFrame targetFrame) {
            EmulatedStackFrame.StackFrameWriter targetWriter = new EmulatedStackFrame.StackFrameWriter();
            targetWriter.attach(targetFrame);
            EmulatedStackFrame.StackFrameReader callerReader = new EmulatedStackFrame.StackFrameReader();
            callerReader.attach(callerFrame);
            MethodType targetMethodType = targetFrame.getMethodType();
            int indexOfArityArray = targetMethodType.ptypes().length - 1;
            for (int i = 0; i < indexOfArityArray; i++) {
                copyParameter(callerReader, targetWriter, targetMethodType.ptypes()[i]);
            }
            Class<?> arityArrayType = targetMethodType.ptypes()[indexOfArityArray];
            targetWriter.putNextReference(makeArityArray(callerFrame.getMethodType(), callerReader, indexOfArityArray, arityArrayType), arityArrayType);
        }

        private static MethodType makeTargetFrameType(MethodType callerType, MethodType targetType) {
            int ptypesLength = targetType.ptypes().length;
            Class<?>[] ptypes = new Class[ptypesLength];
            System.arraycopy((Object) callerType.ptypes(), 0, (Object) ptypes, 0, ptypesLength - 1);
            ptypes[ptypesLength - 1] = targetType.ptypes()[ptypesLength - 1];
            return MethodType.methodType(callerType.rtype(), ptypes);
        }
    }

    private Transformers() {
    }

    static {
        try {
            TRANSFORM_INTERNAL = MethodHandle.class.getDeclaredMethod("transformInternal", EmulatedStackFrame.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        }
    }
}
