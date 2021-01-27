package ohos.com.sun.org.apache.bcel.internal.generic;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import ohos.com.sun.org.apache.bcel.internal.classfile.Attribute;
import ohos.com.sun.org.apache.bcel.internal.classfile.Code;
import ohos.com.sun.org.apache.bcel.internal.classfile.CodeException;
import ohos.com.sun.org.apache.bcel.internal.classfile.ExceptionTable;
import ohos.com.sun.org.apache.bcel.internal.classfile.LineNumber;
import ohos.com.sun.org.apache.bcel.internal.classfile.LineNumberTable;
import ohos.com.sun.org.apache.bcel.internal.classfile.LocalVariable;
import ohos.com.sun.org.apache.bcel.internal.classfile.LocalVariableTable;
import ohos.com.sun.org.apache.bcel.internal.classfile.LocalVariableTypeTable;
import ohos.com.sun.org.apache.bcel.internal.classfile.Method;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;

public class MethodGen extends FieldGenOrMethodGen {
    private String[] arg_names;
    private Type[] arg_types;
    private String class_name;
    private ArrayList code_attrs_vec;
    private ArrayList exception_vec;
    private InstructionList il;
    private ArrayList line_number_vec;
    private int max_locals;
    private int max_stack;
    private ArrayList observers;
    private boolean strip_attributes;
    private ArrayList throws_vec;
    private ArrayList type_vec;
    private ArrayList variable_vec;

    public MethodGen(int i, Type type, Type[] typeArr, String[] strArr, String str, String str2, InstructionList instructionList, ConstantPoolGen constantPoolGen) {
        InstructionHandle instructionHandle;
        this.variable_vec = new ArrayList();
        this.type_vec = new ArrayList();
        this.line_number_vec = new ArrayList();
        this.exception_vec = new ArrayList();
        this.throws_vec = new ArrayList();
        this.code_attrs_vec = new ArrayList();
        setAccessFlags(i);
        setType(type);
        setArgumentTypes(typeArr);
        setArgumentNames(strArr);
        setName(str);
        setClassName(str2);
        setInstructionList(instructionList);
        setConstantPool(constantPoolGen);
        boolean z = isAbstract() || isNative();
        InstructionHandle instructionHandle2 = null;
        if (!z) {
            instructionHandle2 = instructionList.getStart();
            instructionHandle = instructionList.getEnd();
            if (!isStatic() && str2 != null) {
                addLocalVariable("this", new ObjectType(str2), instructionHandle2, instructionHandle);
            }
        } else {
            instructionHandle = null;
        }
        if (typeArr != null) {
            int length = typeArr.length;
            for (Type type2 : typeArr) {
                if (Type.VOID == type2) {
                    throw new ClassGenException("'void' is an illegal argument type for a method");
                }
            }
            if (strArr == null) {
                strArr = new String[length];
                for (int i2 = 0; i2 < length; i2++) {
                    strArr[i2] = Constants.ELEMNAME_ARG_STRING + i2;
                }
                setArgumentNames(strArr);
            } else if (length != strArr.length) {
                throw new ClassGenException("Mismatch in argument array lengths: " + length + " vs. " + strArr.length);
            }
            if (!z) {
                for (int i3 = 0; i3 < length; i3++) {
                    addLocalVariable(strArr[i3], typeArr[i3], instructionHandle2, instructionHandle);
                }
            }
        }
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public MethodGen(Method method, String str, ConstantPoolGen constantPoolGen) {
        this(method.getAccessFlags(), Type.getReturnType(method.getSignature()), Type.getArgumentTypes(method.getSignature()), null, method.getName(), str, (method.getAccessFlags() & 1280) == 0 ? new InstructionList(method.getCode().getCode()) : null, constantPoolGen);
        String[] exceptionNames;
        InstructionHandle instructionHandle;
        Attribute[] attributes = method.getAttributes();
        for (Attribute attribute : attributes) {
            if (attribute instanceof Code) {
                Code code = (Code) attribute;
                setMaxStack(code.getMaxStack());
                setMaxLocals(code.getMaxLocals());
                CodeException[] exceptionTable = code.getExceptionTable();
                if (exceptionTable != null) {
                    for (CodeException codeException : exceptionTable) {
                        int catchType = codeException.getCatchType();
                        ObjectType objectType = catchType > 0 ? new ObjectType(method.getConstantPool().getConstantString(catchType, (byte) 7)) : null;
                        int endPC = codeException.getEndPC();
                        if (method.getCode().getCode().length == endPC) {
                            instructionHandle = this.il.getEnd();
                        } else {
                            instructionHandle = this.il.findHandle(endPC).getPrev();
                        }
                        addExceptionHandler(this.il.findHandle(codeException.getStartPC()), instructionHandle, this.il.findHandle(codeException.getHandlerPC()), objectType);
                    }
                }
                Attribute[] attributes2 = code.getAttributes();
                for (Attribute attribute2 : attributes2) {
                    if (attribute2 instanceof LineNumberTable) {
                        LineNumber[] lineNumberTable = ((LineNumberTable) attribute2).getLineNumberTable();
                        for (LineNumber lineNumber : lineNumberTable) {
                            addLineNumber(this.il.findHandle(lineNumber.getStartPC()), lineNumber.getLineNumber());
                        }
                    } else if (attribute2 instanceof LocalVariableTable) {
                        LocalVariable[] localVariableTable = ((LocalVariableTable) attribute2).getLocalVariableTable();
                        removeLocalVariables();
                        for (LocalVariable localVariable : localVariableTable) {
                            InstructionHandle findHandle = this.il.findHandle(localVariable.getStartPC());
                            InstructionHandle findHandle2 = this.il.findHandle(localVariable.getStartPC() + localVariable.getLength());
                            addLocalVariable(localVariable.getName(), Type.getType(localVariable.getSignature()), localVariable.getIndex(), findHandle == null ? this.il.getStart() : findHandle, findHandle2 == null ? this.il.getEnd() : findHandle2);
                        }
                    } else if (attribute2 instanceof LocalVariableTypeTable) {
                        LocalVariable[] localVariableTypeTable = ((LocalVariableTypeTable) attribute2).getLocalVariableTypeTable();
                        removeLocalVariableTypes();
                        for (LocalVariable localVariable2 : localVariableTypeTable) {
                            InstructionHandle findHandle3 = this.il.findHandle(localVariable2.getStartPC());
                            InstructionHandle findHandle4 = this.il.findHandle(localVariable2.getStartPC() + localVariable2.getLength());
                            addLocalVariableType(localVariable2.getName(), Type.getType(localVariable2.getSignature()), localVariable2.getIndex(), findHandle3 == null ? this.il.getStart() : findHandle3, findHandle4 == null ? this.il.getEnd() : findHandle4);
                        }
                    } else {
                        addCodeAttribute(attribute2);
                    }
                }
            } else if (attribute instanceof ExceptionTable) {
                for (String str2 : ((ExceptionTable) attribute).getExceptionNames()) {
                    addException(str2);
                }
            } else {
                addAttribute(attribute);
            }
        }
    }

    public LocalVariableGen addLocalVariable(String str, Type type, int i, InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        if (type.getType() != 16) {
            int size = type.getSize() + i;
            if (size > this.max_locals) {
                this.max_locals = size;
            }
            LocalVariableGen localVariableGen = new LocalVariableGen(i, str, type, instructionHandle, instructionHandle2);
            int indexOf = this.variable_vec.indexOf(localVariableGen);
            if (indexOf >= 0) {
                this.variable_vec.set(indexOf, localVariableGen);
            } else {
                this.variable_vec.add(localVariableGen);
            }
            return localVariableGen;
        }
        throw new IllegalArgumentException("Can not use " + type + " as type for local variable");
    }

    public LocalVariableGen addLocalVariable(String str, Type type, InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        return addLocalVariable(str, type, this.max_locals, instructionHandle, instructionHandle2);
    }

    public void removeLocalVariable(LocalVariableGen localVariableGen) {
        this.variable_vec.remove(localVariableGen);
    }

    public void removeLocalVariables() {
        this.variable_vec.clear();
    }

    private static final void sort(LocalVariableGen[] localVariableGenArr, int i, int i2) {
        int index = localVariableGenArr[(i + i2) / 2].getIndex();
        int i3 = i;
        int i4 = i2;
        while (true) {
            if (localVariableGenArr[i3].getIndex() < index) {
                i3++;
            } else {
                while (index < localVariableGenArr[i4].getIndex()) {
                    i4--;
                }
                if (i3 <= i4) {
                    LocalVariableGen localVariableGen = localVariableGenArr[i3];
                    localVariableGenArr[i3] = localVariableGenArr[i4];
                    localVariableGenArr[i4] = localVariableGen;
                    i3++;
                    i4--;
                }
                if (i3 > i4) {
                    break;
                }
            }
        }
        if (i < i4) {
            sort(localVariableGenArr, i, i4);
        }
        if (i3 < i2) {
            sort(localVariableGenArr, i3, i2);
        }
    }

    public LocalVariableGen[] getLocalVariables() {
        int size = this.variable_vec.size();
        LocalVariableGen[] localVariableGenArr = new LocalVariableGen[size];
        this.variable_vec.toArray(localVariableGenArr);
        for (int i = 0; i < size; i++) {
            if (localVariableGenArr[i].getStart() == null) {
                localVariableGenArr[i].setStart(this.il.getStart());
            }
            if (localVariableGenArr[i].getEnd() == null) {
                localVariableGenArr[i].setEnd(this.il.getEnd());
            }
        }
        if (size > 1) {
            sort(localVariableGenArr, 0, size - 1);
        }
        return localVariableGenArr;
    }

    private LocalVariableGen[] getLocalVariableTypes() {
        int size = this.type_vec.size();
        LocalVariableGen[] localVariableGenArr = new LocalVariableGen[size];
        this.type_vec.toArray(localVariableGenArr);
        for (int i = 0; i < size; i++) {
            if (localVariableGenArr[i].getStart() == null) {
                localVariableGenArr[i].setStart(this.il.getStart());
            }
            if (localVariableGenArr[i].getEnd() == null) {
                localVariableGenArr[i].setEnd(this.il.getEnd());
            }
        }
        if (size > 1) {
            sort(localVariableGenArr, 0, size - 1);
        }
        return localVariableGenArr;
    }

    public LocalVariableTable getLocalVariableTable(ConstantPoolGen constantPoolGen) {
        LocalVariableGen[] localVariables = getLocalVariables();
        int length = localVariables.length;
        LocalVariable[] localVariableArr = new LocalVariable[length];
        for (int i = 0; i < length; i++) {
            localVariableArr[i] = localVariables[i].getLocalVariable(constantPoolGen);
        }
        return new LocalVariableTable(constantPoolGen.addUtf8("LocalVariableTable"), (localVariableArr.length * 10) + 2, localVariableArr, constantPoolGen.getConstantPool());
    }

    public LocalVariableTypeTable getLocalVariableTypeTable(ConstantPoolGen constantPoolGen) {
        LocalVariableGen[] localVariableTypes = getLocalVariableTypes();
        int length = localVariableTypes.length;
        LocalVariable[] localVariableArr = new LocalVariable[length];
        for (int i = 0; i < length; i++) {
            localVariableArr[i] = localVariableTypes[i].getLocalVariable(constantPoolGen);
        }
        return new LocalVariableTypeTable(constantPoolGen.addUtf8("LocalVariableTypeTable"), (localVariableArr.length * 10) + 2, localVariableArr, constantPoolGen.getConstantPool());
    }

    private LocalVariableGen addLocalVariableType(String str, Type type, int i, InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        if (type.getType() != 16) {
            int size = type.getSize() + i;
            if (size > this.max_locals) {
                this.max_locals = size;
            }
            LocalVariableGen localVariableGen = new LocalVariableGen(i, str, type, instructionHandle, instructionHandle2);
            int indexOf = this.type_vec.indexOf(localVariableGen);
            if (indexOf >= 0) {
                this.type_vec.set(indexOf, localVariableGen);
            } else {
                this.type_vec.add(localVariableGen);
            }
            return localVariableGen;
        }
        throw new IllegalArgumentException("Can not use " + type + " as type for local variable");
    }

    private void removeLocalVariableTypes() {
        this.type_vec.clear();
    }

    public LineNumberGen addLineNumber(InstructionHandle instructionHandle, int i) {
        LineNumberGen lineNumberGen = new LineNumberGen(instructionHandle, i);
        this.line_number_vec.add(lineNumberGen);
        return lineNumberGen;
    }

    public void removeLineNumber(LineNumberGen lineNumberGen) {
        this.line_number_vec.remove(lineNumberGen);
    }

    public void removeLineNumbers() {
        this.line_number_vec.clear();
    }

    public LineNumberGen[] getLineNumbers() {
        LineNumberGen[] lineNumberGenArr = new LineNumberGen[this.line_number_vec.size()];
        this.line_number_vec.toArray(lineNumberGenArr);
        return lineNumberGenArr;
    }

    public LineNumberTable getLineNumberTable(ConstantPoolGen constantPoolGen) {
        int size = this.line_number_vec.size();
        LineNumber[] lineNumberArr = new LineNumber[size];
        for (int i = 0; i < size; i++) {
            try {
                lineNumberArr[i] = ((LineNumberGen) this.line_number_vec.get(i)).getLineNumber();
            } catch (ArrayIndexOutOfBoundsException unused) {
            }
        }
        return new LineNumberTable(constantPoolGen.addUtf8("LineNumberTable"), (lineNumberArr.length * 4) + 2, lineNumberArr, constantPoolGen.getConstantPool());
    }

    public CodeExceptionGen addExceptionHandler(InstructionHandle instructionHandle, InstructionHandle instructionHandle2, InstructionHandle instructionHandle3, ObjectType objectType) {
        if (instructionHandle == null || instructionHandle2 == null || instructionHandle3 == null) {
            throw new ClassGenException("Exception handler target is null instruction");
        }
        CodeExceptionGen codeExceptionGen = new CodeExceptionGen(instructionHandle, instructionHandle2, instructionHandle3, objectType);
        this.exception_vec.add(codeExceptionGen);
        return codeExceptionGen;
    }

    public void removeExceptionHandler(CodeExceptionGen codeExceptionGen) {
        this.exception_vec.remove(codeExceptionGen);
    }

    public void removeExceptionHandlers() {
        this.exception_vec.clear();
    }

    public CodeExceptionGen[] getExceptionHandlers() {
        CodeExceptionGen[] codeExceptionGenArr = new CodeExceptionGen[this.exception_vec.size()];
        this.exception_vec.toArray(codeExceptionGenArr);
        return codeExceptionGenArr;
    }

    private CodeException[] getCodeExceptions() {
        int size = this.exception_vec.size();
        CodeException[] codeExceptionArr = new CodeException[size];
        for (int i = 0; i < size; i++) {
            try {
                codeExceptionArr[i] = ((CodeExceptionGen) this.exception_vec.get(i)).getCodeException(this.cp);
            } catch (ArrayIndexOutOfBoundsException unused) {
            }
        }
        return codeExceptionArr;
    }

    public void addException(String str) {
        this.throws_vec.add(str);
    }

    public void removeException(String str) {
        this.throws_vec.remove(str);
    }

    public void removeExceptions() {
        this.throws_vec.clear();
    }

    public String[] getExceptions() {
        String[] strArr = new String[this.throws_vec.size()];
        this.throws_vec.toArray(strArr);
        return strArr;
    }

    private ExceptionTable getExceptionTable(ConstantPoolGen constantPoolGen) {
        int size = this.throws_vec.size();
        int[] iArr = new int[size];
        for (int i = 0; i < size; i++) {
            try {
                iArr[i] = constantPoolGen.addClass((String) this.throws_vec.get(i));
            } catch (ArrayIndexOutOfBoundsException unused) {
            }
        }
        return new ExceptionTable(constantPoolGen.addUtf8("Exceptions"), (size * 2) + 2, iArr, constantPoolGen.getConstantPool());
    }

    public void addCodeAttribute(Attribute attribute) {
        this.code_attrs_vec.add(attribute);
    }

    public void removeCodeAttribute(Attribute attribute) {
        this.code_attrs_vec.remove(attribute);
    }

    public void removeCodeAttributes() {
        this.code_attrs_vec.clear();
    }

    public Attribute[] getCodeAttributes() {
        Attribute[] attributeArr = new Attribute[this.code_attrs_vec.size()];
        this.code_attrs_vec.toArray(attributeArr);
        return attributeArr;
    }

    public Method getMethod() {
        LocalVariableTable localVariableTable;
        LocalVariableTypeTable localVariableTypeTable;
        LineNumberTable lineNumberTable;
        Code code;
        String signature = getSignature();
        int addUtf8 = this.cp.addUtf8(this.name);
        int addUtf82 = this.cp.addUtf8(signature);
        InstructionList instructionList = this.il;
        ExceptionTable exceptionTable = null;
        byte[] byteCode = instructionList != null ? instructionList.getByteCode() : null;
        if (this.variable_vec.size() <= 0 || this.strip_attributes) {
            localVariableTable = null;
        } else {
            localVariableTable = getLocalVariableTable(this.cp);
            addCodeAttribute(localVariableTable);
        }
        if (this.type_vec.size() <= 0 || this.strip_attributes) {
            localVariableTypeTable = null;
        } else {
            localVariableTypeTable = getLocalVariableTypeTable(this.cp);
            addCodeAttribute(localVariableTypeTable);
        }
        if (this.line_number_vec.size() <= 0 || this.strip_attributes) {
            lineNumberTable = null;
        } else {
            LineNumberTable lineNumberTable2 = getLineNumberTable(this.cp);
            addCodeAttribute(lineNumberTable2);
            lineNumberTable = lineNumberTable2;
        }
        Attribute[] codeAttributes = getCodeAttributes();
        int i = 0;
        for (Attribute attribute : codeAttributes) {
            i += attribute.getLength() + 6;
        }
        CodeException[] codeExceptions = getCodeExceptions();
        int length = codeExceptions.length * 8;
        if (this.il == null || isAbstract()) {
            code = null;
        } else {
            Attribute[] attributes = getAttributes();
            for (Attribute attribute2 : attributes) {
                if (attribute2 instanceof Code) {
                    removeAttribute(attribute2);
                }
            }
            Code code2 = new Code(this.cp.addUtf8("Code"), byteCode.length + 8 + 2 + length + 2 + i, this.max_stack, this.max_locals, byteCode, codeExceptions, codeAttributes, this.cp.getConstantPool());
            addAttribute(code2);
            code = code2;
        }
        if (this.throws_vec.size() > 0) {
            exceptionTable = getExceptionTable(this.cp);
            addAttribute(exceptionTable);
        }
        Method method = new Method(this.access_flags, addUtf8, addUtf82, getAttributes(), this.cp.getConstantPool());
        if (localVariableTable != null) {
            removeCodeAttribute(localVariableTable);
        }
        if (localVariableTypeTable != null) {
            removeCodeAttribute(localVariableTypeTable);
        }
        if (lineNumberTable != null) {
            removeCodeAttribute(lineNumberTable);
        }
        if (code != null) {
            removeAttribute(code);
        }
        if (exceptionTable != null) {
            removeAttribute(exceptionTable);
        }
        return method;
    }

    public void removeNOPs() {
        InstructionTargeter[] targeters;
        InstructionList instructionList = this.il;
        if (instructionList != null) {
            InstructionHandle start = instructionList.getStart();
            while (start != null) {
                InstructionHandle instructionHandle = start.next;
                if (instructionHandle != null && (start.getInstruction() instanceof NOP)) {
                    try {
                        this.il.delete(start);
                    } catch (TargetLostException e) {
                        InstructionHandle[] targets = e.getTargets();
                        for (int i = 0; i < targets.length; i++) {
                            for (InstructionTargeter instructionTargeter : targets[i].getTargeters()) {
                                instructionTargeter.updateTarget(targets[i], instructionHandle);
                            }
                        }
                    }
                }
                start = instructionHandle;
            }
        }
    }

    public void setMaxLocals(int i) {
        this.max_locals = i;
    }

    public int getMaxLocals() {
        return this.max_locals;
    }

    public void setMaxStack(int i) {
        this.max_stack = i;
    }

    public int getMaxStack() {
        return this.max_stack;
    }

    public String getClassName() {
        return this.class_name;
    }

    public void setClassName(String str) {
        this.class_name = str;
    }

    public void setReturnType(Type type) {
        setType(type);
    }

    public Type getReturnType() {
        return getType();
    }

    public void setArgumentTypes(Type[] typeArr) {
        this.arg_types = typeArr;
    }

    public Type[] getArgumentTypes() {
        return (Type[]) this.arg_types.clone();
    }

    public void setArgumentType(int i, Type type) {
        this.arg_types[i] = type;
    }

    public Type getArgumentType(int i) {
        return this.arg_types[i];
    }

    public void setArgumentNames(String[] strArr) {
        this.arg_names = strArr;
    }

    public String[] getArgumentNames() {
        return (String[]) this.arg_names.clone();
    }

    public void setArgumentName(int i, String str) {
        this.arg_names[i] = str;
    }

    public String getArgumentName(int i) {
        return this.arg_names[i];
    }

    public InstructionList getInstructionList() {
        return this.il;
    }

    public void setInstructionList(InstructionList instructionList) {
        this.il = instructionList;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen
    public String getSignature() {
        return Type.getMethodSignature(this.type, this.arg_types);
    }

    public void setMaxStack() {
        if (this.il != null) {
            this.max_stack = getMaxStack(this.cp, this.il, getExceptionHandlers());
        } else {
            this.max_stack = 0;
        }
    }

    public void setMaxLocals() {
        int index;
        int i = 0;
        if (this.il != null) {
            int i2 = !isStatic();
            if (this.arg_types != null) {
                while (true) {
                    Type[] typeArr = this.arg_types;
                    if (i >= typeArr.length) {
                        break;
                    }
                    i2 += typeArr[i].getSize();
                    i++;
                }
            }
            for (InstructionHandle start = this.il.getStart(); start != null; start = start.getNext()) {
                Instruction instruction = start.getInstruction();
                if (((instruction instanceof LocalVariableInstruction) || (instruction instanceof RET) || (instruction instanceof IINC)) && (index = ((IndexedInstruction) instruction).getIndex() + ((TypedInstruction) instruction).getType(this.cp).getSize()) > i2) {
                    i2 = index;
                }
            }
            this.max_locals = i2;
            return;
        }
        this.max_locals = 0;
    }

    public void stripAttributes(boolean z) {
        this.strip_attributes = z;
    }

    /* access modifiers changed from: package-private */
    public static final class BranchTarget {
        int stackDepth;
        InstructionHandle target;

        BranchTarget(InstructionHandle instructionHandle, int i) {
            this.target = instructionHandle;
            this.stackDepth = i;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class BranchStack {
        Stack branchTargets = new Stack();
        Hashtable visitedTargets = new Hashtable();

        BranchStack() {
        }

        public void push(InstructionHandle instructionHandle, int i) {
            if (!visited(instructionHandle)) {
                this.branchTargets.push(visit(instructionHandle, i));
            }
        }

        public BranchTarget pop() {
            if (!this.branchTargets.empty()) {
                return (BranchTarget) this.branchTargets.pop();
            }
            return null;
        }

        private final BranchTarget visit(InstructionHandle instructionHandle, int i) {
            BranchTarget branchTarget = new BranchTarget(instructionHandle, i);
            this.visitedTargets.put(instructionHandle, branchTarget);
            return branchTarget;
        }

        private final boolean visited(InstructionHandle instructionHandle) {
            return this.visitedTargets.get(instructionHandle) != null;
        }
    }

    public static int getMaxStack(ConstantPoolGen constantPoolGen, InstructionList instructionList, CodeExceptionGen[] codeExceptionGenArr) {
        BranchTarget pop;
        InstructionHandle[] targets;
        BranchStack branchStack = new BranchStack();
        for (CodeExceptionGen codeExceptionGen : codeExceptionGenArr) {
            InstructionHandle handlerPC = codeExceptionGen.getHandlerPC();
            if (handlerPC != null) {
                branchStack.push(handlerPC, 1);
            }
        }
        InstructionHandle start = instructionList.getStart();
        int i = 0;
        int i2 = 0;
        while (start != null) {
            Instruction instruction = start.getInstruction();
            short opcode = instruction.getOpcode();
            i2 += instruction.produceStack(constantPoolGen) - instruction.consumeStack(constantPoolGen);
            if (i2 > i) {
                i = i2;
            }
            if (instruction instanceof BranchInstruction) {
                BranchInstruction branchInstruction = (BranchInstruction) instruction;
                if (instruction instanceof Select) {
                    for (InstructionHandle instructionHandle : ((Select) branchInstruction).getTargets()) {
                        branchStack.push(instructionHandle, i2);
                    }
                } else {
                    if (!(branchInstruction instanceof IfInstruction)) {
                        if (opcode == 168 || opcode == 201) {
                            branchStack.push(start.getNext(), i2 - 1);
                        }
                    }
                    branchStack.push(branchInstruction.getTarget(), i2);
                }
                start = null;
                branchStack.push(branchInstruction.getTarget(), i2);
            } else if (opcode == 191 || opcode == 169 || (opcode >= 172 && opcode <= 177)) {
                start = null;
            }
            if (start != null) {
                start = start.getNext();
            }
            if (start == null && (pop = branchStack.pop()) != null) {
                start = pop.target;
                i2 = pop.stackDepth;
            }
        }
        return i;
    }

    public void addObserver(MethodObserver methodObserver) {
        if (this.observers == null) {
            this.observers = new ArrayList();
        }
        this.observers.add(methodObserver);
    }

    public void removeObserver(MethodObserver methodObserver) {
        ArrayList arrayList = this.observers;
        if (arrayList != null) {
            arrayList.remove(methodObserver);
        }
    }

    public void update() {
        ArrayList arrayList = this.observers;
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ((MethodObserver) it.next()).notify(this);
            }
        }
    }

    @Override // java.lang.Object
    public final String toString() {
        StringBuffer stringBuffer = new StringBuffer(Utility.methodSignatureToString(Type.getMethodSignature(this.type, this.arg_types), this.name, Utility.accessToString(this.access_flags), true, getLocalVariableTable(this.cp)));
        if (this.throws_vec.size() > 0) {
            Iterator it = this.throws_vec.iterator();
            while (it.hasNext()) {
                stringBuffer.append("\n\t\tthrows " + it.next());
            }
        }
        return stringBuffer.toString();
    }

    public MethodGen copy(String str, ConstantPoolGen constantPoolGen) {
        MethodGen methodGen = new MethodGen(((MethodGen) clone()).getMethod(), str, this.cp);
        if (this.cp != constantPoolGen) {
            methodGen.setConstantPool(constantPoolGen);
            methodGen.getInstructionList().replaceConstantPool(this.cp, constantPoolGen);
        }
        return methodGen;
    }
}
