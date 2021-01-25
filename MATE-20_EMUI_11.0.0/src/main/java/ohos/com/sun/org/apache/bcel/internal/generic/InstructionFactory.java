package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.Serializable;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;

public class InstructionFactory implements InstructionConstants, Serializable {
    private static MethodObject[] append_mos = {new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.STRING}, 1), new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.OBJECT}, 1), null, null, new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.BOOLEAN}, 1), new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.CHAR}, 1), new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.FLOAT}, 1), new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.DOUBLE}, 1), new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.INT}, 1), new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.INT}, 1), new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.INT}, 1), new MethodObject(Constants.STRING_BUFFER_CLASS, "append", Type.STRINGBUFFER, new Type[]{Type.LONG}, 1)};
    protected ClassGen cg;
    protected ConstantPoolGen cp;

    public InstructionFactory(ClassGen classGen, ConstantPoolGen constantPoolGen) {
        this.cg = classGen;
        this.cp = constantPoolGen;
    }

    public InstructionFactory(ClassGen classGen) {
        this(classGen, classGen.getConstantPool());
    }

    public InstructionFactory(ConstantPoolGen constantPoolGen) {
        this(null, constantPoolGen);
    }

    public InvokeInstruction createInvoke(String str, String str2, Type type, Type[] typeArr, short s) {
        int i;
        String methodSignature = Type.getMethodSignature(type, typeArr);
        int i2 = 0;
        for (Type type2 : typeArr) {
            i2 += type2.getSize();
        }
        if (s == 185) {
            i = this.cp.addInterfaceMethodref(str, str2, methodSignature);
        } else {
            i = this.cp.addMethodref(str, str2, methodSignature);
        }
        switch (s) {
            case 182:
                return new INVOKEVIRTUAL(i);
            case 183:
                return new INVOKESPECIAL(i);
            case 184:
                return new INVOKESTATIC(i);
            case 185:
                return new INVOKEINTERFACE(i, i2 + 1);
            default:
                throw new RuntimeException("Oops: Unknown invoke kind:" + ((int) s));
        }
    }

    public InstructionList createPrintln(String str) {
        InstructionList instructionList = new InstructionList();
        int addFieldref = this.cp.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;");
        int addMethodref = this.cp.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V");
        instructionList.append(new GETSTATIC(addFieldref));
        instructionList.append(new PUSH(this.cp, str));
        instructionList.append(new INVOKEVIRTUAL(addMethodref));
        return instructionList;
    }

    public Instruction createConstant(Object obj) {
        PUSH push;
        if (obj instanceof Number) {
            push = new PUSH(this.cp, (Number) obj);
        } else if (obj instanceof String) {
            push = new PUSH(this.cp, (String) obj);
        } else if (obj instanceof Boolean) {
            push = new PUSH(this.cp, (Boolean) obj);
        } else if (obj instanceof Character) {
            push = new PUSH(this.cp, (Character) obj);
        } else {
            throw new ClassGenException("Illegal type: " + obj.getClass());
        }
        return push.getInstruction();
    }

    /* access modifiers changed from: private */
    public static class MethodObject {
        int access;
        String[] arg_names;
        Type[] arg_types;
        String class_name;
        String name;
        Type result_type;

        MethodObject(String str, String str2, Type type, Type[] typeArr, int i) {
            this.class_name = str;
            this.name = str2;
            this.result_type = type;
            this.arg_types = typeArr;
            this.access = i;
        }
    }

    private InvokeInstruction createInvoke(MethodObject methodObject, short s) {
        return createInvoke(methodObject.class_name, methodObject.name, methodObject.result_type, methodObject.arg_types, s);
    }

    private static final boolean isString(Type type) {
        return (type instanceof ObjectType) && ((ObjectType) type).getClassName().equals("java.lang.String");
    }

    public Instruction createAppend(Type type) {
        byte type2 = type.getType();
        if (isString(type)) {
            return createInvoke(append_mos[0], ohos.com.sun.org.apache.bcel.internal.Constants.INVOKEVIRTUAL);
        }
        switch (type2) {
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                return createInvoke(append_mos[type2], ohos.com.sun.org.apache.bcel.internal.Constants.INVOKEVIRTUAL);
            case 12:
            default:
                throw new RuntimeException("Oops: No append for this type? " + type);
            case 13:
            case 14:
                return createInvoke(append_mos[1], ohos.com.sun.org.apache.bcel.internal.Constants.INVOKEVIRTUAL);
        }
    }

    public FieldInstruction createFieldAccess(String str, String str2, Type type, short s) {
        int addFieldref = this.cp.addFieldref(str, str2, type.getSignature());
        switch (s) {
            case 178:
                return new GETSTATIC(addFieldref);
            case 179:
                return new PUTSTATIC(addFieldref);
            case 180:
                return new GETFIELD(addFieldref);
            case 181:
                return new PUTFIELD(addFieldref);
            default:
                throw new RuntimeException("Oops: Unknown getfield kind:" + ((int) s));
        }
    }

    public static Instruction createThis() {
        return new ALOAD(0);
    }

    public static ReturnInstruction createReturn(Type type) {
        switch (type.getType()) {
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
                return IRETURN;
            case 6:
                return FRETURN;
            case 7:
                return DRETURN;
            case 11:
                return LRETURN;
            case 12:
                return RETURN;
            case 13:
            case 14:
                return ARETURN;
            default:
                throw new RuntimeException("Invalid type: " + type);
        }
    }

    private static final ArithmeticInstruction createBinaryIntOp(char c, String str) {
        if (c == '%') {
            return IREM;
        }
        if (c == '&') {
            return IAND;
        }
        if (c == '*') {
            return IMUL;
        }
        if (c == '+') {
            return IADD;
        }
        if (c == '-') {
            return ISUB;
        }
        if (c == '/') {
            return IDIV;
        }
        if (c == '<') {
            return ISHL;
        }
        if (c != '>') {
            if (c == '^') {
                return IXOR;
            }
            if (c == '|') {
                return IOR;
            }
            throw new RuntimeException("Invalid operand " + str);
        } else if (str.equals(">>>")) {
            return IUSHR;
        } else {
            return ISHR;
        }
    }

    private static final ArithmeticInstruction createBinaryLongOp(char c, String str) {
        if (c == '%') {
            return LREM;
        }
        if (c == '&') {
            return LAND;
        }
        if (c == '*') {
            return LMUL;
        }
        if (c == '+') {
            return LADD;
        }
        if (c == '-') {
            return LSUB;
        }
        if (c == '/') {
            return LDIV;
        }
        if (c == '<') {
            return LSHL;
        }
        if (c != '>') {
            if (c == '^') {
                return LXOR;
            }
            if (c == '|') {
                return LOR;
            }
            throw new RuntimeException("Invalid operand " + str);
        } else if (str.equals(">>>")) {
            return LUSHR;
        } else {
            return LSHR;
        }
    }

    private static final ArithmeticInstruction createBinaryFloatOp(char c) {
        if (c == '*') {
            return FMUL;
        }
        if (c == '+') {
            return FADD;
        }
        if (c == '-') {
            return FSUB;
        }
        if (c == '/') {
            return FDIV;
        }
        throw new RuntimeException("Invalid operand " + c);
    }

    private static final ArithmeticInstruction createBinaryDoubleOp(char c) {
        if (c == '*') {
            return DMUL;
        }
        if (c == '+') {
            return DADD;
        }
        if (c == '-') {
            return DSUB;
        }
        if (c == '/') {
            return DDIV;
        }
        throw new RuntimeException("Invalid operand " + c);
    }

    public static ArithmeticInstruction createBinaryOperation(String str, Type type) {
        char c = str.toCharArray()[0];
        switch (type.getType()) {
            case 5:
            case 8:
            case 9:
            case 10:
                return createBinaryIntOp(c, str);
            case 6:
                return createBinaryFloatOp(c);
            case 7:
                return createBinaryDoubleOp(c);
            case 11:
                return createBinaryLongOp(c, str);
            default:
                throw new RuntimeException("Invalid type " + type);
        }
    }

    public static StackInstruction createPop(int i) {
        if (i == 2) {
            return POP2;
        }
        return POP;
    }

    public static StackInstruction createDup(int i) {
        if (i == 2) {
            return DUP2;
        }
        return DUP;
    }

    public static StackInstruction createDup_2(int i) {
        if (i == 2) {
            return DUP2_X2;
        }
        return DUP_X2;
    }

    public static StackInstruction createDup_1(int i) {
        if (i == 2) {
            return DUP2_X1;
        }
        return DUP_X1;
    }

    public static LocalVariableInstruction createStore(Type type, int i) {
        switch (type.getType()) {
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
                return new ISTORE(i);
            case 6:
                return new FSTORE(i);
            case 7:
                return new DSTORE(i);
            case 11:
                return new LSTORE(i);
            case 12:
            default:
                throw new RuntimeException("Invalid type " + type);
            case 13:
            case 14:
                return new ASTORE(i);
        }
    }

    public static LocalVariableInstruction createLoad(Type type, int i) {
        switch (type.getType()) {
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
                return new ILOAD(i);
            case 6:
                return new FLOAD(i);
            case 7:
                return new DLOAD(i);
            case 11:
                return new LLOAD(i);
            case 12:
            default:
                throw new RuntimeException("Invalid type " + type);
            case 13:
            case 14:
                return new ALOAD(i);
        }
    }

    public static ArrayInstruction createArrayLoad(Type type) {
        switch (type.getType()) {
            case 4:
            case 8:
                return BALOAD;
            case 5:
                return CALOAD;
            case 6:
                return FALOAD;
            case 7:
                return DALOAD;
            case 9:
                return SALOAD;
            case 10:
                return IALOAD;
            case 11:
                return LALOAD;
            case 12:
            default:
                throw new RuntimeException("Invalid type " + type);
            case 13:
            case 14:
                return AALOAD;
        }
    }

    public static ArrayInstruction createArrayStore(Type type) {
        switch (type.getType()) {
            case 4:
            case 8:
                return BASTORE;
            case 5:
                return CASTORE;
            case 6:
                return FASTORE;
            case 7:
                return DASTORE;
            case 9:
                return SASTORE;
            case 10:
                return IASTORE;
            case 11:
                return LASTORE;
            case 12:
            default:
                throw new RuntimeException("Invalid type " + type);
            case 13:
            case 14:
                return AASTORE;
        }
    }

    public Instruction createCast(Type type, Type type2) {
        if ((type instanceof BasicType) && (type2 instanceof BasicType)) {
            byte type3 = type2.getType();
            byte type4 = type.getType();
            if (type3 == 11 && (type4 == 5 || type4 == 8 || type4 == 9)) {
                type4 = 10;
            }
            String[] strArr = {"C", "F", "D", "B", "S", "I", "L"};
            String str = "com.sun.org.apache.bcel.internal.generic." + strArr[type4 - 5] + "2" + strArr[type3 - 5];
            try {
                return (Instruction) Class.forName(str).newInstance();
            } catch (Exception unused) {
                throw new RuntimeException("Could not find instruction: " + str);
            }
        } else if (!(type instanceof ReferenceType) || !(type2 instanceof ReferenceType)) {
            throw new RuntimeException("Can not cast " + type + " to " + type2);
        } else if (type2 instanceof ArrayType) {
            return new CHECKCAST(this.cp.addArrayClass((ArrayType) type2));
        } else {
            return new CHECKCAST(this.cp.addClass(((ObjectType) type2).getClassName()));
        }
    }

    public GETFIELD createGetField(String str, String str2, Type type) {
        return new GETFIELD(this.cp.addFieldref(str, str2, type.getSignature()));
    }

    public GETSTATIC createGetStatic(String str, String str2, Type type) {
        return new GETSTATIC(this.cp.addFieldref(str, str2, type.getSignature()));
    }

    public PUTFIELD createPutField(String str, String str2, Type type) {
        return new PUTFIELD(this.cp.addFieldref(str, str2, type.getSignature()));
    }

    public PUTSTATIC createPutStatic(String str, String str2, Type type) {
        return new PUTSTATIC(this.cp.addFieldref(str, str2, type.getSignature()));
    }

    public CHECKCAST createCheckCast(ReferenceType referenceType) {
        if (referenceType instanceof ArrayType) {
            return new CHECKCAST(this.cp.addArrayClass((ArrayType) referenceType));
        }
        return new CHECKCAST(this.cp.addClass((ObjectType) referenceType));
    }

    public INSTANCEOF createInstanceOf(ReferenceType referenceType) {
        if (referenceType instanceof ArrayType) {
            return new INSTANCEOF(this.cp.addArrayClass((ArrayType) referenceType));
        }
        return new INSTANCEOF(this.cp.addClass((ObjectType) referenceType));
    }

    public NEW createNew(ObjectType objectType) {
        return new NEW(this.cp.addClass(objectType));
    }

    public NEW createNew(String str) {
        return createNew(new ObjectType(str));
    }

    public Instruction createNewArray(Type type, short s) {
        ArrayType arrayType;
        if (s != 1) {
            if (type instanceof ArrayType) {
                arrayType = (ArrayType) type;
            } else {
                arrayType = new ArrayType(type, s);
            }
            return new MULTIANEWARRAY(this.cp.addArrayClass(arrayType), s);
        } else if (type instanceof ObjectType) {
            return new ANEWARRAY(this.cp.addClass((ObjectType) type));
        } else {
            if (type instanceof ArrayType) {
                return new ANEWARRAY(this.cp.addArrayClass((ArrayType) type));
            }
            return new NEWARRAY(((BasicType) type).getType());
        }
    }

    public static Instruction createNull(Type type) {
        switch (type.getType()) {
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
                return ICONST_0;
            case 6:
                return FCONST_0;
            case 7:
                return DCONST_0;
            case 11:
                return LCONST_0;
            case 12:
                return NOP;
            case 13:
            case 14:
                return ACONST_NULL;
            default:
                throw new RuntimeException("Invalid type: " + type);
        }
    }

    public static BranchInstruction createBranchInstruction(short s, InstructionHandle instructionHandle) {
        switch (s) {
            case 153:
                return new IFEQ(instructionHandle);
            case 154:
                return new IFNE(instructionHandle);
            case 155:
                return new IFLT(instructionHandle);
            case 156:
                return new IFGE(instructionHandle);
            case 157:
                return new IFGT(instructionHandle);
            case 158:
                return new IFLE(instructionHandle);
            case 159:
                return new IF_ICMPEQ(instructionHandle);
            case 160:
                return new IF_ICMPNE(instructionHandle);
            case 161:
                return new IF_ICMPLT(instructionHandle);
            case 162:
                return new IF_ICMPGE(instructionHandle);
            case 163:
                return new IF_ICMPGT(instructionHandle);
            case 164:
                return new IF_ICMPLE(instructionHandle);
            case 165:
                return new IF_ACMPEQ(instructionHandle);
            case 166:
                return new IF_ACMPNE(instructionHandle);
            case 167:
                return new GOTO(instructionHandle);
            case 168:
                return new JSR(instructionHandle);
            default:
                switch (s) {
                    case 198:
                        return new IFNULL(instructionHandle);
                    case 199:
                        return new IFNONNULL(instructionHandle);
                    case 200:
                        return new GOTO_W(instructionHandle);
                    case 201:
                        return new JSR_W(instructionHandle);
                    default:
                        throw new RuntimeException("Invalid opcode: " + ((int) s));
                }
        }
    }

    public void setClassGen(ClassGen classGen) {
        this.cg = classGen;
    }

    public ClassGen getClassGen() {
        return this.cg;
    }

    public void setConstantPool(ConstantPoolGen constantPoolGen) {
        this.cp = constantPoolGen;
    }

    public ConstantPoolGen getConstantPool() {
        return this.cp;
    }
}
