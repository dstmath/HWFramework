package ohos.com.sun.org.apache.bcel.internal.generic;

import java.util.StringTokenizer;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;

public abstract class InvokeInstruction extends FieldOrMethod implements ExceptionThrower, TypedInstruction, StackConsumer, StackProducer {
    InvokeInstruction() {
    }

    protected InvokeInstruction(short s, int i) {
        super(s, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(ConstantPool constantPool) {
        StringTokenizer stringTokenizer = new StringTokenizer(constantPool.constantToString(constantPool.getConstant(this.index)));
        return Constants.OPCODE_NAMES[this.opcode] + " " + stringTokenizer.nextToken().replace('.', '/') + stringTokenizer.nextToken();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction, ohos.com.sun.org.apache.bcel.internal.generic.StackConsumer
    public int consumeStack(ConstantPoolGen constantPoolGen) {
        Type[] argumentTypes = Type.getArgumentTypes(getSignature(constantPoolGen));
        int i = this.opcode == 184 ? 0 : 1;
        for (Type type : argumentTypes) {
            i += type.getSize();
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction, ohos.com.sun.org.apache.bcel.internal.generic.StackProducer
    public int produceStack(ConstantPoolGen constantPoolGen) {
        return getReturnType(constantPoolGen).getSize();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return getReturnType(constantPoolGen);
    }

    public String getMethodName(ConstantPoolGen constantPoolGen) {
        return getName(constantPoolGen);
    }

    public Type getReturnType(ConstantPoolGen constantPoolGen) {
        return Type.getReturnType(getSignature(constantPoolGen));
    }

    public Type[] getArgumentTypes(ConstantPoolGen constantPoolGen) {
        return Type.getArgumentTypes(getSignature(constantPoolGen));
    }
}
