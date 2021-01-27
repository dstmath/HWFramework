package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public class ANEWARRAY extends CPInstruction implements LoadClass, AllocationInstruction, ExceptionThrower, StackProducer {
    ANEWARRAY() {
    }

    public ANEWARRAY(int i) {
        super(Constants.ANEWARRAY, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        Class[] clsArr = new Class[(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length + 1)];
        System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION, 0, clsArr, 0, ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);
        clsArr[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length] = ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION;
        return clsArr;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitLoadClass(this);
        visitor.visitAllocationInstruction(this);
        visitor.visitExceptionThrower(this);
        visitor.visitStackProducer(this);
        visitor.visitTypedInstruction(this);
        visitor.visitCPInstruction(this);
        visitor.visitANEWARRAY(this);
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
}
