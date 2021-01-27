package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public class INVOKESPECIAL extends InvokeInstruction {
    INVOKESPECIAL() {
    }

    public INVOKESPECIAL(int i) {
        super(183, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        Class[] clsArr = new Class[(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 4)];
        System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0, clsArr, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
        clsArr[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 3] = ExceptionConstants.UNSATISFIED_LINK_ERROR;
        clsArr[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 2] = ExceptionConstants.ABSTRACT_METHOD_ERROR;
        clsArr[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 1] = ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
        clsArr[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length] = ExceptionConstants.NULL_POINTER_EXCEPTION;
        return clsArr;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitStackConsumer(this);
        visitor.visitStackProducer(this);
        visitor.visitLoadClass(this);
        visitor.visitCPInstruction(this);
        visitor.visitFieldOrMethod(this);
        visitor.visitInvokeInstruction(this);
        visitor.visitINVOKESPECIAL(this);
    }
}
