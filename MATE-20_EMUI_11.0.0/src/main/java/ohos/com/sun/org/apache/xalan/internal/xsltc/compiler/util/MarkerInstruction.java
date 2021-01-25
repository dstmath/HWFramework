package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.Visitor;

/* access modifiers changed from: package-private */
public abstract class MarkerInstruction extends Instruction {
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction, ohos.com.sun.org.apache.bcel.internal.generic.StackConsumer
    public final int consumeStack(ConstantPoolGen constantPoolGen) {
        return 0;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public Instruction copy() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction, ohos.com.sun.org.apache.bcel.internal.generic.StackProducer
    public final int produceStack(ConstantPoolGen constantPoolGen) {
        return 0;
    }

    public MarkerInstruction() {
        super(-1, 0);
    }
}
