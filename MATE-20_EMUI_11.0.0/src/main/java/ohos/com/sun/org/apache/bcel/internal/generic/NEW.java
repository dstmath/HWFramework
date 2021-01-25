package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public class NEW extends CPInstruction implements LoadClass, AllocationInstruction, ExceptionThrower, StackProducer {
    NEW() {
    }

    public NEW(int i) {
        super(Constants.NEW, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        Class[] clsArr = new Class[(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length + 2)];
        System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION, 0, clsArr, 0, ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);
        clsArr[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length + 1] = ExceptionConstants.INSTANTIATION_ERROR;
        clsArr[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length] = ExceptionConstants.ILLEGAL_ACCESS_ERROR;
        return clsArr;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LoadClass
    public ObjectType getLoadClassType(ConstantPoolGen constantPoolGen) {
        return (ObjectType) getType(constantPoolGen);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitLoadClass(this);
        visitor.visitAllocationInstruction(this);
        visitor.visitExceptionThrower(this);
        visitor.visitStackProducer(this);
        visitor.visitTypedInstruction(this);
        visitor.visitCPInstruction(this);
        visitor.visitNEW(this);
    }
}
