package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public class CHECKCAST extends CPInstruction implements LoadClass, ExceptionThrower, StackProducer, StackConsumer {
    CHECKCAST() {
    }

    public CHECKCAST(int i) {
        super(Constants.CHECKCAST, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        Class[] clsArr = new Class[(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length + 1)];
        System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION, 0, clsArr, 0, ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);
        clsArr[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length] = ExceptionConstants.CLASS_CAST_EXCEPTION;
        return clsArr;
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
        visitor.visitCHECKCAST(this);
    }
}
