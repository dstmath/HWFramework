package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public class INVOKESTATIC extends InvokeInstruction {
    INVOKESTATIC() {
    }

    public INVOKESTATIC(int i) {
        super(Constants.INVOKESTATIC, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        Class[] clsArr = new Class[(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 2)];
        System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0, clsArr, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
        clsArr[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length] = ExceptionConstants.UNSATISFIED_LINK_ERROR;
        clsArr[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 1] = ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
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
        visitor.visitINVOKESTATIC(this);
    }
}
