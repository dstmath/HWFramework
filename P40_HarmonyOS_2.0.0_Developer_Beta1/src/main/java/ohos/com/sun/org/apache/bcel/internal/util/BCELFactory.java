package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import ohos.aafwk.ability.Ability;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;
import ohos.com.sun.org.apache.bcel.internal.generic.AllocationInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ArrayInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.CodeExceptionGen;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor;
import ohos.com.sun.org.apache.bcel.internal.generic.FieldInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.IINC;
import ohos.com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionConstants;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InvokeInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.LDC;
import ohos.com.sun.org.apache.bcel.internal.generic.LDC2_W;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.MULTIANEWARRAY;
import ohos.com.sun.org.apache.bcel.internal.generic.MethodGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEWARRAY;
import ohos.com.sun.org.apache.bcel.internal.generic.ObjectType;
import ohos.com.sun.org.apache.bcel.internal.generic.RET;
import ohos.com.sun.org.apache.bcel.internal.generic.ReturnInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.Select;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.dmsdp.sdk.DMSDPConfig;

class BCELFactory extends EmptyVisitor {
    private ConstantPoolGen _cp;
    private MethodGen _mg;
    private PrintWriter _out;
    private HashMap branch_map = new HashMap();
    private ArrayList branches = new ArrayList();

    BCELFactory(MethodGen methodGen, PrintWriter printWriter) {
        this._mg = methodGen;
        this._cp = methodGen.getConstantPool();
        this._out = printWriter;
    }

    public void start() {
        if (!(this._mg.isAbstract() || this._mg.isNative())) {
            for (InstructionHandle start = this._mg.getInstructionList().getStart(); start != null; start = start.getNext()) {
                Instruction instruction = start.getInstruction();
                boolean z = instruction instanceof BranchInstruction;
                if (z) {
                    this.branch_map.put(instruction, start);
                }
                if (!start.hasTargeters()) {
                    this._out.print(Ability.PREFIX);
                } else if (z) {
                    PrintWriter printWriter = this._out;
                    printWriter.println("    InstructionHandle ih_" + start.getPosition() + DMSDPConfig.LIST_TO_STRING_SPLIT);
                } else {
                    PrintWriter printWriter2 = this._out;
                    printWriter2.print("    InstructionHandle ih_" + start.getPosition() + " = ");
                }
                if (!visitInstruction(instruction)) {
                    instruction.accept(this);
                }
            }
            updateBranchTargets();
            updateExceptionHandlers();
        }
    }

    private boolean visitInstruction(Instruction instruction) {
        if (InstructionConstants.INSTRUCTIONS[instruction.getOpcode()] == null || (instruction instanceof ConstantPushInstruction) || (instruction instanceof ReturnInstruction)) {
            return false;
        }
        PrintWriter printWriter = this._out;
        printWriter.println("il.append(InstructionConstants." + instruction.getName().toUpperCase() + ");");
        return true;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitLocalVariableInstruction(LocalVariableInstruction localVariableInstruction) {
        short opcode = localVariableInstruction.getOpcode();
        Type type = localVariableInstruction.getType(this._cp);
        if (opcode == 132) {
            PrintWriter printWriter = this._out;
            printWriter.println("il.append(new IINC(" + localVariableInstruction.getIndex() + ", " + ((IINC) localVariableInstruction).getIncrement() + "));");
            return;
        }
        String str = opcode < 54 ? "Load" : "Store";
        PrintWriter printWriter2 = this._out;
        printWriter2.println("il.append(_factory.create" + str + "(" + BCELifier.printType(type) + ", " + localVariableInstruction.getIndex() + "));");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitArrayInstruction(ArrayInstruction arrayInstruction) {
        short opcode = arrayInstruction.getOpcode();
        Type type = arrayInstruction.getType(this._cp);
        String str = opcode < 79 ? "Load" : "Store";
        PrintWriter printWriter = this._out;
        printWriter.println("il.append(_factory.createArray" + str + "(" + BCELifier.printType(type) + "));");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitFieldInstruction(FieldInstruction fieldInstruction) {
        short opcode = fieldInstruction.getOpcode();
        String className = fieldInstruction.getClassName(this._cp);
        String fieldName = fieldInstruction.getFieldName(this._cp);
        Type fieldType = fieldInstruction.getFieldType(this._cp);
        PrintWriter printWriter = this._out;
        printWriter.println("il.append(_factory.createFieldAccess(\"" + className + "\", \"" + fieldName + "\", " + BCELifier.printType(fieldType) + ", Constants." + Constants.OPCODE_NAMES[opcode].toUpperCase() + "));");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitInvokeInstruction(InvokeInstruction invokeInstruction) {
        short opcode = invokeInstruction.getOpcode();
        String className = invokeInstruction.getClassName(this._cp);
        String methodName = invokeInstruction.getMethodName(this._cp);
        Type returnType = invokeInstruction.getReturnType(this._cp);
        Type[] argumentTypes = invokeInstruction.getArgumentTypes(this._cp);
        PrintWriter printWriter = this._out;
        printWriter.println("il.append(_factory.createInvoke(\"" + className + "\", \"" + methodName + "\", " + BCELifier.printType(returnType) + ", " + BCELifier.printArgumentTypes(argumentTypes) + ", Constants." + Constants.OPCODE_NAMES[opcode].toUpperCase() + "));");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitAllocationInstruction(AllocationInstruction allocationInstruction) {
        Type type;
        if (allocationInstruction instanceof CPInstruction) {
            type = ((CPInstruction) allocationInstruction).getType(this._cp);
        } else {
            type = ((NEWARRAY) allocationInstruction).getType();
        }
        short opcode = ((Instruction) allocationInstruction).getOpcode();
        short s = 1;
        if (opcode != 197) {
            switch (opcode) {
                case 187:
                    PrintWriter printWriter = this._out;
                    printWriter.println("il.append(_factory.createNew(\"" + ((ObjectType) type).getClassName() + "\"));");
                    return;
                case 188:
                case 189:
                    break;
                default:
                    throw new RuntimeException("Oops: " + ((int) opcode));
            }
        } else {
            s = ((MULTIANEWARRAY) allocationInstruction).getDimensions();
        }
        PrintWriter printWriter2 = this._out;
        printWriter2.println("il.append(_factory.createNewArray(" + BCELifier.printType(type) + ", (short) " + ((int) s) + "));");
    }

    private void createConstant(Object obj) {
        String obj2 = obj.toString();
        if (obj instanceof String) {
            obj2 = '\"' + Utility.convertString(obj.toString()) + '\"';
        } else if (obj instanceof Character) {
            obj2 = "(char)0x" + Integer.toHexString(((Character) obj).charValue());
        }
        this._out.println("il.append(new PUSH(_cp, " + obj2 + "));");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitLDC(LDC ldc) {
        createConstant(ldc.getValue(this._cp));
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitLDC2_W(LDC2_W ldc2_w) {
        createConstant(ldc2_w.getValue(this._cp));
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitConstantPushInstruction(ConstantPushInstruction constantPushInstruction) {
        createConstant(constantPushInstruction.getValue());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitINSTANCEOF(INSTANCEOF r3) {
        Type type = r3.getType(this._cp);
        PrintWriter printWriter = this._out;
        printWriter.println("il.append(new INSTANCEOF(_cp.addClass(" + BCELifier.printType(type) + ")));");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitCHECKCAST(CHECKCAST checkcast) {
        Type type = checkcast.getType(this._cp);
        PrintWriter printWriter = this._out;
        printWriter.println("il.append(_factory.createCheckCast(" + BCELifier.printType(type) + "));");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitReturnInstruction(ReturnInstruction returnInstruction) {
        Type type = returnInstruction.getType(this._cp);
        PrintWriter printWriter = this._out;
        printWriter.println("il.append(_factory.createReturn(" + BCELifier.printType(type) + "));");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitBranchInstruction(BranchInstruction branchInstruction) {
        BranchHandle branchHandle = (BranchHandle) this.branch_map.get(branchInstruction);
        int position = branchHandle.getPosition();
        String str = branchInstruction.getName() + "_" + position;
        String str2 = "null";
        if (branchInstruction instanceof Select) {
            this.branches.add(branchInstruction);
            StringBuffer stringBuffer = new StringBuffer("new int[] { ");
            int[] matchs = ((Select) branchInstruction).getMatchs();
            for (int i = 0; i < matchs.length; i++) {
                stringBuffer.append(matchs[i]);
                if (i < matchs.length - 1) {
                    stringBuffer.append(", ");
                }
            }
            stringBuffer.append(" }");
            this._out.print("    Select " + str + " = new " + branchInstruction.getName().toUpperCase() + "(" + ((Object) stringBuffer) + ", new InstructionHandle[] { ");
            for (int i2 = 0; i2 < matchs.length; i2++) {
                this._out.print(str2);
                if (i2 < matchs.length - 1) {
                    this._out.print(", ");
                }
            }
            this._out.println(");");
        } else {
            int position2 = branchHandle.getTarget().getPosition();
            if (position > position2) {
                str2 = "ih_" + position2;
            } else {
                this.branches.add(branchInstruction);
            }
            this._out.println("    BranchInstruction " + str + " = _factory.createBranchInstruction(Constants." + branchInstruction.getName().toUpperCase() + ", " + str2 + ");");
        }
        if (branchHandle.hasTargeters()) {
            this._out.println("    ih_" + position + " = il.append(" + str + ");");
            return;
        }
        this._out.println("    il.append(" + str + ");");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.generic.Visitor
    public void visitRET(RET ret) {
        PrintWriter printWriter = this._out;
        printWriter.println("il.append(new RET(" + ret.getIndex() + ")));");
    }

    private void updateBranchTargets() {
        Iterator it = this.branches.iterator();
        while (it.hasNext()) {
            BranchInstruction branchInstruction = (BranchInstruction) it.next();
            BranchHandle branchHandle = (BranchHandle) this.branch_map.get(branchInstruction);
            String str = branchInstruction.getName() + "_" + branchHandle.getPosition();
            int position = branchHandle.getTarget().getPosition();
            this._out.println(Ability.PREFIX + str + ".setTarget(ih_" + position + ");");
            if (branchInstruction instanceof Select) {
                InstructionHandle[] targets = ((Select) branchInstruction).getTargets();
                for (int i = 0; i < targets.length; i++) {
                    int position2 = targets[i].getPosition();
                    this._out.println(Ability.PREFIX + str + ".setTarget(" + i + ", ih_" + position2 + ");");
                }
            }
        }
    }

    private void updateExceptionHandlers() {
        String str;
        CodeExceptionGen[] exceptionHandlers = this._mg.getExceptionHandlers();
        for (CodeExceptionGen codeExceptionGen : exceptionHandlers) {
            if (codeExceptionGen.getCatchType() == null) {
                str = "null";
            } else {
                str = BCELifier.printType(codeExceptionGen.getCatchType());
            }
            this._out.println("    method.addExceptionHandler(ih_" + codeExceptionGen.getStartPC().getPosition() + ", ih_" + codeExceptionGen.getEndPC().getPosition() + ", ih_" + codeExceptionGen.getHandlerPC().getPosition() + ", " + str + ");");
        }
    }
}
