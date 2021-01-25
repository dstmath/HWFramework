package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public class INSTANCEOF extends CPInstruction implements LoadClass, ExceptionThrower, StackProducer, StackConsumer {
    INSTANCEOF() {
    }

    public INSTANCEOF(int i) {
        super(Constants.INSTANCEOF, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        return ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LoadClass
    public ObjectType getLoadClassType(ConstantPoolGen constantPoolGen) {
        Type type = getType(constantPoolGen);
        if (type instanceof ArrayType) {
            type = ((ArrayType) type).getBasicType();
        }
        if (type instanceof ObjectType) {
            return (ObjectType) type;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitLoadClass(this);
        visitor.visitExceptionThrower(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitTypedInstruction(this);
        visitor.visitCPInstruction(this);
        visitor.visitINSTANCEOF(this);
    }
}
