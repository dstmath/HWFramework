package java.lang.invoke;

import dalvik.system.EmulatedStackFrame;
import dalvik.system.EmulatedStackFrame.Range;
import dalvik.system.EmulatedStackFrame.StackFrameAccessor;
import dalvik.system.EmulatedStackFrame.StackFrameReader;
import dalvik.system.EmulatedStackFrame.StackFrameWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Types;
import sun.invoke.util.Wrapper;

public class Transformers {
    private static final Method TRANSFORM_INTERNAL;

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

    public static class AlwaysThrow extends Transformer {
        private final Class<? extends Throwable> exceptionType;

        public AlwaysThrow(Class<?> nominalReturnType, Class<? extends Throwable> exType) {
            super(MethodType.methodType((Class) nominalReturnType, (Class) exType));
            this.exceptionType = exType;
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            throw ((Throwable) emulatedStackFrame.getReference(0, this.exceptionType));
        }
    }

    public static class BindTo extends Transformer {
        private final MethodHandle delegate;
        private final Range range = Range.all(type());
        private final Object receiver;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.BindTo.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.BindTo.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.BindTo.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public BindTo(MethodHandle delegate, Object receiver) {
            super(delegate.type().dropParameterTypes(0, 1));
            this.delegate = delegate;
            this.receiver = receiver;
        }
    }

    public static class CatchException extends Transformer {
        private final Class<?> exType;
        private final MethodHandle handler;
        private final Range handlerArgsRange;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.CatchException.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.CatchException.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.CatchException.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public CatchException(MethodHandle target, MethodHandle handler, Class<?> exType) {
            super(target.type());
            this.target = target;
            this.handler = handler;
            this.exType = exType;
            this.handlerArgsRange = Range.of(target.type(), 0, handler.type().parameterCount() - 1);
        }
    }

    static class CollectArguments extends Transformer {
        private final MethodHandle collector;
        private final Range collectorRange;
        private final int pos;
        private final Range range1;
        private final Range range2;
        private final int referencesOffset;
        private final int stackFrameOffset;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.CollectArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.CollectArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.CollectArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        CollectArguments(MethodHandle target, MethodHandle collector, int pos, MethodType adapterType) {
            super(adapterType);
            this.target = target;
            this.collector = collector;
            this.pos = pos;
            int numFilterArgs = collector.type().parameterCount();
            int numAdapterArgs = type().parameterCount();
            this.collectorRange = Range.of(type(), pos, pos + numFilterArgs);
            this.range1 = Range.of(type(), 0, pos);
            if (pos + numFilterArgs < numAdapterArgs) {
                this.range2 = Range.of(type(), pos + numFilterArgs, numAdapterArgs);
            } else {
                this.range2 = null;
            }
            Class<?> collectorRType = collector.type().rtype();
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
        private final Range copyRange;
        private final int numArrayArgs;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Collector.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Collector.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.Collector.transform(dalvik.system.EmulatedStackFrame):void");
        }

        Collector(MethodHandle delegate, Class<?> arrayType, int length) {
            super(delegate.type().asCollectorType(arrayType, length));
            this.target = delegate;
            this.arrayOffset = delegate.type().parameterCount() - 1;
            this.arrayTypeChar = Wrapper.basicTypeChar(arrayType.getComponentType());
            this.numArrayArgs = length;
            this.copyRange = Range.of(delegate.type(), 0, this.arrayOffset);
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

        public Constant(Class<?> type, Object value) {
            super(MethodType.methodType(type));
            this.type = type;
            if (!type.isPrimitive()) {
                this.asReference = value;
                this.typeChar = 'L';
            } else if (type == Integer.TYPE) {
                this.asInt = ((Integer) value).intValue();
                this.typeChar = 'I';
            } else if (type == Character.TYPE) {
                this.asInt = ((Character) value).charValue();
                this.typeChar = 'C';
            } else if (type == Short.TYPE) {
                this.asInt = ((Short) value).shortValue();
                this.typeChar = 'S';
            } else if (type == Byte.TYPE) {
                this.asInt = ((Byte) value).byteValue();
                this.typeChar = 'B';
            } else if (type == Boolean.TYPE) {
                this.asInt = ((Boolean) value).booleanValue() ? 1 : 0;
                this.typeChar = 'Z';
            } else if (type == Long.TYPE) {
                this.asLong = ((Long) value).longValue();
                this.typeChar = 'J';
            } else if (type == Float.TYPE) {
                this.asFloat = ((Float) value).floatValue();
                this.typeChar = 'F';
            } else if (type == Double.TYPE) {
                this.asDouble = ((Double) value).doubleValue();
                this.typeChar = 'D';
            } else {
                throw new AssertionError("unknown type: " + this.typeChar);
            }
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            boolean z = true;
            StackFrameWriter writer = new StackFrameWriter();
            writer.attach(emulatedStackFrame);
            writer.makeReturnValueAccessor();
            switch (this.typeChar) {
                case 'B':
                    writer.putNextByte((byte) this.asInt);
                    return;
                case 'C':
                    writer.putNextChar((char) this.asInt);
                    return;
                case 'D':
                    writer.putNextDouble(this.asDouble);
                    return;
                case Types.DATALINK /*70*/:
                    writer.putNextFloat(this.asFloat);
                    return;
                case 'I':
                    writer.putNextInt(this.asInt);
                    return;
                case 'J':
                    writer.putNextLong(this.asLong);
                    return;
                case 'L':
                    writer.putNextReference(this.asReference, this.type);
                    return;
                case 'S':
                    writer.putNextShort((short) this.asInt);
                    return;
                case 'Z':
                    if (this.asInt != 1) {
                        z = false;
                    }
                    writer.putNextBoolean(z);
                    return;
                default:
                    throw new AssertionError("Unexpected typeChar: " + this.typeChar);
            }
        }
    }

    static class Construct extends Transformer {
        private final Range callerRange = Range.all(type());
        private final MethodHandle constructorHandle;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Construct.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Construct.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.Construct.transform(dalvik.system.EmulatedStackFrame):void");
        }

        Construct(MethodHandle constructorHandle, MethodType returnedType) {
            super(returnedType);
            this.constructorHandle = constructorHandle;
        }

        MethodHandle getConstructorHandle() {
            return this.constructorHandle;
        }

        private static boolean isAbstract(Class<?> klass) {
            return (klass.getModifiers() & 1024) == 1024;
        }

        private static void checkInstantiable(Class<?> klass) throws InstantiationException {
            if (isAbstract(klass)) {
                throw new InstantiationException("Can't instantiate " + (klass.isInterface() ? "interface " : "abstract class ") + klass);
            }
        }
    }

    public static class DropArguments extends Transformer {
        private final MethodHandle delegate;
        private final Range range1;
        private final Range range2;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.DropArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.DropArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.DropArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public DropArguments(MethodType type, MethodHandle delegate, int startPos, int numDropped) {
            super(type);
            this.delegate = delegate;
            this.range1 = Range.of(type, 0, startPos);
            int numArgs = type.ptypes().length;
            if (startPos + numDropped < numArgs) {
                this.range2 = Range.of(type, startPos + numDropped, numArgs);
            } else {
                this.range2 = null;
            }
        }
    }

    public static class ExplicitCastArguments extends Transformer {
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.ExplicitCastArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.ExplicitCastArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.ExplicitCastArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public ExplicitCastArguments(MethodHandle target, MethodType type) {
            super(type);
            this.target = target;
        }

        private void explicitCastArguments(EmulatedStackFrame callerFrame, EmulatedStackFrame targetFrame) {
            StackFrameReader reader = new StackFrameReader();
            reader.attach(callerFrame);
            StackFrameWriter writer = new StackFrameWriter();
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
                StackFrameWriter writer = new StackFrameWriter();
                writer.attach(callerFrame);
                writer.makeReturnValueAccessor();
                if (from != Void.TYPE) {
                    StackFrameReader reader = new StackFrameReader();
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

        private static void explicitCastFromBoolean(boolean fromValue, StackFrameWriter writer, Class<?> to) {
            int value = fromValue ? 1 : 0;
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

        private static byte readPrimitiveAsByte(StackFrameReader reader, Class<?> from) {
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
            return (byte) 0;
        }

        private static char readPrimitiveAsChar(StackFrameReader reader, Class<?> from) {
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

        private static short readPrimitiveAsShort(StackFrameReader reader, Class<?> from) {
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
            return (short) 0;
        }

        private static int readPrimitiveAsInt(StackFrameReader reader, Class<?> from) {
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

        private static long readPrimitiveAsLong(StackFrameReader reader, Class<?> from) {
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

        private static float readPrimitiveAsFloat(StackFrameReader reader, Class<?> from) {
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

        private static double readPrimitiveAsDouble(StackFrameReader reader, Class<?> from) {
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

        private static void explicitCastToBoolean(StackFrameReader reader, Class<?> from, StackFrameWriter writer) {
            writer.putNextBoolean(toBoolean(readPrimitiveAsByte(reader, from)));
        }

        private static void explicitCastPrimitives(StackFrameReader reader, Class<?> from, StackFrameWriter writer, Class<?> to) {
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

        private static void unboxNull(StackFrameWriter writer, Class<?> to) {
            if (to == Boolean.TYPE) {
                writer.putNextBoolean(false);
            } else if (to == Byte.TYPE) {
                writer.putNextByte((byte) 0);
            } else if (to == Character.TYPE) {
                writer.putNextChar(0);
            } else if (to == Short.TYPE) {
                writer.putNextShort((short) 0);
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

        private static void unboxNonNull(Object ref, Class<?> from, StackFrameWriter writer, Class<?> to) {
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

        private static void unbox(Object ref, Class<?> from, StackFrameWriter writer, Class<?> to) {
            if (ref == null) {
                unboxNull(writer, to);
            } else {
                unboxNonNull(ref, from, writer, to);
            }
        }

        private static void box(StackFrameReader reader, Class<?> from, StackFrameWriter writer, Class<?> to) {
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

        private static void explicitCast(StackFrameReader reader, Class<?> from, StackFrameWriter writer, Class<?> to) {
            if (from.lambda$-java_util_function_Predicate_4628(to)) {
                StackFrameAccessor.copyNext(reader, writer, from);
            } else if (!from.isPrimitive()) {
                Object ref = reader.nextReference(from);
                if (to.isInterface()) {
                    writer.putNextReference(ref, to);
                } else if (to.isPrimitive()) {
                    unbox(ref, from, writer, to);
                } else {
                    writer.putNextReference(to.cast(ref), to);
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
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FilterArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FilterArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.FilterArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        FilterArguments(MethodHandle target, int pos, MethodHandle[] filters) {
            super(deriveType(target, pos, filters));
            this.target = target;
            this.pos = pos;
            this.filters = filters;
        }

        private static MethodType deriveType(MethodHandle target, int pos, MethodHandle[] filters) {
            Class<?>[] filterArgs = new Class[filters.length];
            for (int i = 0; i < filters.length; i++) {
                filterArgs[i] = filters[i].type().parameterType(0);
            }
            return target.type().replaceParameterTypes(pos, filters.length + pos, filterArgs);
        }
    }

    public static class FilterReturnValue extends Transformer {
        private final Range allArgs = Range.all(type());
        private final MethodHandle filter;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FilterReturnValue.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FilterReturnValue.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.FilterReturnValue.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public FilterReturnValue(MethodHandle target, MethodHandle filter) {
            super(MethodType.methodType(filter.type().rtype(), target.type().ptypes()));
            this.target = target;
            this.filter = filter;
        }
    }

    static class FoldArguments extends Transformer {
        private final MethodHandle combiner;
        private final Range combinerArgs;
        private final int referencesOffset;
        private final int stackFrameOffset;
        private final MethodHandle target;
        private final Range targetArgs = Range.all(type());

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FoldArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.FoldArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.FoldArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        FoldArguments(MethodHandle target, MethodHandle combiner) {
            super(deriveType(target, combiner));
            this.target = target;
            this.combiner = combiner;
            this.combinerArgs = Range.all(combiner.type());
            Class<?> combinerRType = combiner.type().rtype();
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

        private static MethodType deriveType(MethodHandle target, MethodHandle combiner) {
            if (combiner.type().rtype() == Void.TYPE) {
                return target.type();
            }
            return target.type().dropParameterTypes(0, 1);
        }
    }

    public static class GuardWithTest extends Transformer {
        private final MethodHandle fallback;
        private final MethodHandle target;
        private final MethodHandle test;
        private final Range testArgsRange;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.GuardWithTest.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.GuardWithTest.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.GuardWithTest.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public GuardWithTest(MethodHandle test, MethodHandle target, MethodHandle fallback) {
            super(target.type());
            this.test = test;
            this.target = target;
            this.fallback = fallback;
            this.testArgsRange = Range.of(target.type(), 0, test.type().parameterCount());
        }
    }

    static class InsertArguments extends Transformer {
        private final int pos;
        private final Range range1;
        private final Range range2;
        private final MethodHandle target;
        private final Object[] values;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.InsertArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.InsertArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.InsertArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        InsertArguments(MethodHandle target, int pos, Object[] values) {
            super(target.type().dropParameterTypes(pos, values.length + pos));
            this.target = target;
            this.pos = pos;
            this.values = values;
            MethodType type = type();
            this.range1 = Range.of(type, 0, pos);
            this.range2 = Range.of(type, pos, type.parameterCount());
        }
    }

    static class Invoker extends Transformer {
        private final Range copyRange = Range.of(type(), 1, type().parameterCount());
        private final boolean isExactInvoker;
        private final MethodType targetType;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Invoker.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Invoker.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.Invoker.transform(dalvik.system.EmulatedStackFrame):void");
        }

        Invoker(MethodType targetType, boolean isExactInvoker) {
            super(targetType.insertParameterTypes(0, MethodHandle.class));
            this.targetType = targetType;
            this.isExactInvoker = isExactInvoker;
        }
    }

    public static class PermuteArguments extends Transformer {
        private final int[] reorder;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.PermuteArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.PermuteArguments.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.PermuteArguments.transform(dalvik.system.EmulatedStackFrame):void");
        }

        public PermuteArguments(MethodType type, MethodHandle target, int[] reorder) {
            super(type);
            this.target = target;
            this.reorder = reorder;
        }
    }

    public static class ReferenceArrayElementGetter extends Transformer {
        private final Class<?> arrayClass;

        public ReferenceArrayElementGetter(Class<?> arrayClass) {
            super(MethodType.methodType(arrayClass.getComponentType(), new Class[]{arrayClass, Integer.TYPE}));
            this.arrayClass = arrayClass;
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            StackFrameReader reader = new StackFrameReader();
            reader.attach(emulatedStackFrame);
            Object[] array = (Object[]) reader.nextReference(this.arrayClass);
            int index = reader.nextInt();
            StackFrameWriter writer = new StackFrameWriter();
            writer.attach(emulatedStackFrame);
            writer.makeReturnValueAccessor();
            writer.putNextReference(array[index], this.arrayClass.getComponentType());
        }
    }

    public static class ReferenceArrayElementSetter extends Transformer {
        private final Class<?> arrayClass;

        public ReferenceArrayElementSetter(Class<?> arrayClass) {
            super(MethodType.methodType(Void.TYPE, new Class[]{arrayClass, Integer.TYPE, arrayClass.getComponentType()}));
            this.arrayClass = arrayClass;
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            StackFrameReader reader = new StackFrameReader();
            reader.attach(emulatedStackFrame);
            ((Object[]) reader.nextReference(this.arrayClass))[reader.nextInt()] = reader.nextReference(this.arrayClass.getComponentType());
        }
    }

    public static class ReferenceIdentity extends Transformer {
        private final Class<?> type;

        public ReferenceIdentity(Class<?> type) {
            super(MethodType.methodType((Class) type, (Class) type));
            this.type = type;
        }

        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            StackFrameReader reader = new StackFrameReader();
            reader.attach(emulatedStackFrame);
            StackFrameWriter writer = new StackFrameWriter();
            writer.attach(emulatedStackFrame);
            writer.makeReturnValueAccessor();
            writer.putNextReference(reader.nextReference(this.type), this.type);
        }
    }

    static class Spreader extends Transformer {
        private final int arrayOffset;
        private final char arrayTypeChar;
        private final Range copyRange;
        private final int numArrayArgs;
        private final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Spreader.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.Spreader.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.Spreader.transform(dalvik.system.EmulatedStackFrame):void");
        }

        Spreader(MethodHandle target, MethodType spreaderType, int numArrayArgs) {
            super(spreaderType);
            this.target = target;
            this.arrayOffset = spreaderType.parameterCount() - 1;
            Class<?> componentType = spreaderType.ptypes()[this.arrayOffset].getComponentType();
            if (componentType == null) {
                throw new AssertionError((Object) "Trailing argument must be an array.");
            }
            this.arrayTypeChar = Wrapper.basicTypeChar(componentType);
            this.numArrayArgs = numArrayArgs;
            this.copyRange = Range.of(spreaderType, 0, this.arrayOffset);
        }

        public static void spreadArray(Object[] array, StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                Object o = array[i];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B':
                        writer.putNextByte(((Byte) o).byteValue());
                        break;
                    case 'C':
                        writer.putNextChar(((Character) o).charValue());
                        break;
                    case 'D':
                        writer.putNextDouble(((Double) o).doubleValue());
                        break;
                    case Types.DATALINK /*70*/:
                        writer.putNextFloat(((Float) o).floatValue());
                        break;
                    case 'I':
                        writer.putNextInt(((Integer) o).intValue());
                        break;
                    case 'J':
                        writer.putNextLong(((Long) o).longValue());
                        break;
                    case 'L':
                        writer.putNextReference(o, argumentType);
                        break;
                    case 'S':
                        writer.putNextShort(((Short) o).shortValue());
                        break;
                    case 'Z':
                        writer.putNextBoolean(((Boolean) o).booleanValue());
                        break;
                    default:
                        break;
                }
            }
        }

        public static void spreadArray(int[] array, StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                int j = array[i];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'D':
                        writer.putNextDouble((double) j);
                        break;
                    case Types.DATALINK /*70*/:
                        writer.putNextFloat((float) j);
                        break;
                    case 'I':
                        writer.putNextInt(j);
                        break;
                    case 'J':
                        writer.putNextLong((long) j);
                        break;
                    case 'L':
                        writer.putNextReference(Integer.valueOf(j), argumentType);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        public static void spreadArray(long[] array, StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                long l = array[i];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'D':
                        writer.putNextDouble((double) l);
                        break;
                    case Types.DATALINK /*70*/:
                        writer.putNextFloat((float) l);
                        break;
                    case 'J':
                        writer.putNextLong(l);
                        break;
                    case 'L':
                        writer.putNextReference(Long.valueOf(l), argumentType);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        public static void spreadArray(byte[] array, StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                byte b = array[i];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B':
                        writer.putNextByte(b);
                        break;
                    case 'D':
                        writer.putNextDouble((double) b);
                        break;
                    case Types.DATALINK /*70*/:
                        writer.putNextFloat((float) b);
                        break;
                    case 'I':
                        writer.putNextInt(b);
                        break;
                    case 'J':
                        writer.putNextLong((long) b);
                        break;
                    case 'L':
                        writer.putNextReference(Byte.valueOf(b), argumentType);
                        break;
                    case 'S':
                        writer.putNextShort((short) b);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        public static void spreadArray(short[] array, StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                short s = array[i];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'D':
                        writer.putNextDouble((double) s);
                        break;
                    case Types.DATALINK /*70*/:
                        writer.putNextFloat((float) s);
                        break;
                    case 'I':
                        writer.putNextInt(s);
                        break;
                    case 'J':
                        writer.putNextLong((long) s);
                        break;
                    case 'L':
                        writer.putNextReference(Short.valueOf(s), argumentType);
                        break;
                    case 'S':
                        writer.putNextShort(s);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        public static void spreadArray(char[] array, StackFrameWriter writer, MethodType type, int numArgs, int offset) {
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

        public static void spreadArray(boolean[] array, StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                boolean z = array[i];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'L':
                        writer.putNextReference(Boolean.valueOf(z), argumentType);
                        break;
                    case 'Z':
                        writer.putNextBoolean(z);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        public static void spreadArray(double[] array, StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                double d = array[i];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'D':
                        writer.putNextDouble(d);
                        break;
                    case 'L':
                        writer.putNextReference(Double.valueOf(d), argumentType);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        public static void spreadArray(float[] array, StackFrameWriter writer, MethodType type, int numArgs, int offset) {
            Class<?>[] ptypes = type.ptypes();
            for (int i = 0; i < numArgs; i++) {
                Class<?> argumentType = ptypes[i + offset];
                float f = array[i];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'D':
                        writer.putNextDouble((double) f);
                        break;
                    case Types.DATALINK /*70*/:
                        writer.putNextFloat(f);
                        break;
                    case 'L':
                        writer.putNextReference(Float.valueOf(f), argumentType);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }
    }

    static class VarargsCollector extends Transformer {
        final MethodHandle target;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.VarargsCollector.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void transform(dalvik.system.EmulatedStackFrame r1) throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.Transformers.VarargsCollector.transform(dalvik.system.EmulatedStackFrame):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.Transformers.VarargsCollector.transform(dalvik.system.EmulatedStackFrame):void");
        }

        VarargsCollector(MethodHandle target) {
            super(target.type(), 6);
            if (lastParameterTypeIsAnArray(target.type().ptypes())) {
                this.target = target;
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

        private static Object referenceArray(StackFrameReader reader, Class<?>[] ptypes, Class<?> elementType, int offset, int length) {
            Object arityArray = Array.newInstance((Class) elementType, length);
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                Object o = null;
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B':
                        o = Byte.valueOf(reader.nextByte());
                        break;
                    case 'C':
                        o = Character.valueOf(reader.nextChar());
                        break;
                    case 'D':
                        o = Double.valueOf(reader.nextDouble());
                        break;
                    case Types.DATALINK /*70*/:
                        o = Float.valueOf(reader.nextFloat());
                        break;
                    case 'I':
                        o = Integer.valueOf(reader.nextInt());
                        break;
                    case 'J':
                        o = Long.valueOf(reader.nextLong());
                        break;
                    case 'L':
                        o = reader.nextReference(argumentType);
                        break;
                    case 'S':
                        o = Short.valueOf(reader.nextShort());
                        break;
                    case 'Z':
                        o = Boolean.valueOf(reader.nextBoolean());
                        break;
                    default:
                        break;
                }
                Array.set(arityArray, i, elementType.cast(o));
            }
            return arityArray;
        }

        private static Object intArray(StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            int[] arityArray = new int[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B':
                        arityArray[i] = reader.nextByte();
                        break;
                    case 'I':
                        arityArray[i] = reader.nextInt();
                        break;
                    case 'S':
                        arityArray[i] = reader.nextShort();
                        break;
                    default:
                        arityArray[i] = ((Integer) reader.nextReference(argumentType)).intValue();
                        break;
                }
            }
            return arityArray;
        }

        private static Object longArray(StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            long[] arityArray = new long[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B':
                        arityArray[i] = (long) reader.nextByte();
                        break;
                    case 'I':
                        arityArray[i] = (long) reader.nextInt();
                        break;
                    case 'J':
                        arityArray[i] = reader.nextLong();
                        break;
                    case 'S':
                        arityArray[i] = (long) reader.nextShort();
                        break;
                    default:
                        arityArray[i] = ((Long) reader.nextReference(argumentType)).longValue();
                        break;
                }
            }
            return arityArray;
        }

        private static Object byteArray(StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            byte[] arityArray = new byte[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B':
                        arityArray[i] = reader.nextByte();
                        break;
                    default:
                        arityArray[i] = ((Byte) reader.nextReference(argumentType)).byteValue();
                        break;
                }
            }
            return arityArray;
        }

        private static Object shortArray(StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            short[] arityArray = new short[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B':
                        arityArray[i] = (short) reader.nextByte();
                        break;
                    case 'S':
                        arityArray[i] = reader.nextShort();
                        break;
                    default:
                        arityArray[i] = ((Short) reader.nextReference(argumentType)).shortValue();
                        break;
                }
            }
            return arityArray;
        }

        private static Object charArray(StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            char[] arityArray = new char[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'C':
                        arityArray[i] = reader.nextChar();
                        break;
                    default:
                        arityArray[i] = ((Character) reader.nextReference(argumentType)).charValue();
                        break;
                }
            }
            return arityArray;
        }

        private static Object booleanArray(StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            boolean[] arityArray = new boolean[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'Z':
                        arityArray[i] = reader.nextBoolean();
                        break;
                    default:
                        arityArray[i] = ((Boolean) reader.nextReference(argumentType)).booleanValue();
                        break;
                }
            }
            return arityArray;
        }

        private static Object floatArray(StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            float[] arityArray = new float[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B':
                        arityArray[i] = (float) reader.nextByte();
                        break;
                    case Types.DATALINK /*70*/:
                        arityArray[i] = reader.nextFloat();
                        break;
                    case 'I':
                        arityArray[i] = (float) reader.nextInt();
                        break;
                    case 'J':
                        arityArray[i] = (float) reader.nextLong();
                        break;
                    case 'S':
                        arityArray[i] = (float) reader.nextShort();
                        break;
                    default:
                        arityArray[i] = ((Float) reader.nextReference(argumentType)).floatValue();
                        break;
                }
            }
            return arityArray;
        }

        private static Object doubleArray(StackFrameReader reader, Class<?>[] ptypes, int offset, int length) {
            double[] arityArray = new double[length];
            for (int i = 0; i < length; i++) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B':
                        arityArray[i] = (double) reader.nextByte();
                        break;
                    case 'D':
                        arityArray[i] = reader.nextDouble();
                        break;
                    case Types.DATALINK /*70*/:
                        arityArray[i] = (double) reader.nextFloat();
                        break;
                    case 'I':
                        arityArray[i] = (double) reader.nextInt();
                        break;
                    case 'J':
                        arityArray[i] = (double) reader.nextLong();
                        break;
                    case 'S':
                        arityArray[i] = (double) reader.nextShort();
                        break;
                    default:
                        arityArray[i] = ((Double) reader.nextReference(argumentType)).doubleValue();
                        break;
                }
            }
            return arityArray;
        }

        private static Object makeArityArray(MethodType callerFrameType, StackFrameReader callerFrameReader, int indexOfArityArray, Class<?> arityArrayType) {
            int arityArrayLength = callerFrameType.ptypes().length - indexOfArityArray;
            Object elementType = arityArrayType.getComponentType();
            Class<?>[] callerPTypes = callerFrameType.ptypes();
            switch (Wrapper.basicTypeChar(elementType)) {
                case 'B':
                    return byteArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                case 'C':
                    return charArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                case 'D':
                    return doubleArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                case Types.DATALINK /*70*/:
                    return floatArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                case 'I':
                    return intArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                case 'J':
                    return longArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                case 'L':
                    return referenceArray(callerFrameReader, callerPTypes, elementType, indexOfArityArray, arityArrayLength);
                case 'S':
                    return shortArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                case 'Z':
                    return booleanArray(callerFrameReader, callerPTypes, indexOfArityArray, arityArrayLength);
                default:
                    throw new InternalError("Unexpected type: " + elementType);
            }
        }

        public static Object collectArguments(char basicComponentType, Class<?> componentType, StackFrameReader reader, Class<?>[] types, int startIdx, int length) {
            switch (basicComponentType) {
                case 'B':
                    return byteArray(reader, types, startIdx, length);
                case 'C':
                    return charArray(reader, types, startIdx, length);
                case 'D':
                    return doubleArray(reader, types, startIdx, length);
                case Types.DATALINK /*70*/:
                    return floatArray(reader, types, startIdx, length);
                case 'I':
                    return intArray(reader, types, startIdx, length);
                case 'J':
                    return longArray(reader, types, startIdx, length);
                case 'L':
                    return referenceArray(reader, types, componentType, startIdx, length);
                case 'S':
                    return shortArray(reader, types, startIdx, length);
                case 'Z':
                    return booleanArray(reader, types, startIdx, length);
                default:
                    throw new InternalError("Unexpected type: " + basicComponentType);
            }
        }

        private static void copyParameter(StackFrameReader reader, StackFrameWriter writer, Class<?> ptype) {
            switch (Wrapper.basicTypeChar(ptype)) {
                case 'B':
                    writer.putNextByte(reader.nextByte());
                    return;
                case 'C':
                    writer.putNextChar(reader.nextChar());
                    return;
                case 'D':
                    writer.putNextDouble(reader.nextDouble());
                    return;
                case Types.DATALINK /*70*/:
                    writer.putNextFloat(reader.nextFloat());
                    return;
                case 'I':
                    writer.putNextInt(reader.nextInt());
                    return;
                case 'J':
                    writer.putNextLong(reader.nextLong());
                    return;
                case 'L':
                    writer.putNextReference(reader.nextReference(ptype), ptype);
                    return;
                case 'S':
                    writer.putNextShort(reader.nextShort());
                    return;
                case 'Z':
                    writer.putNextBoolean(reader.nextBoolean());
                    return;
                default:
                    throw new InternalError("Unexpected type: " + ptype);
            }
        }

        private static void prepareFrame(EmulatedStackFrame callerFrame, EmulatedStackFrame targetFrame) {
            StackFrameWriter targetWriter = new StackFrameWriter();
            targetWriter.attach(targetFrame);
            StackFrameReader callerReader = new StackFrameReader();
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
            Class[] ptypes = new Class[ptypesLength];
            System.arraycopy(callerType.ptypes(), 0, (Object) ptypes, 0, ptypesLength - 1);
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
