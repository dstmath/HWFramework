package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public class GETSTATIC extends FieldInstruction implements PushInstruction, ExceptionThrower {
    GETSTATIC() {
    }

    public GETSTATIC(int i) {
        super(Constants.GETSTATIC, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction, ohos.com.sun.org.apache.bcel.internal.generic.StackProducer
    public int produceStack(ConstantPoolGen constantPoolGen) {
        return getFieldSize(constantPoolGen);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        Class[] clsArr = new Class[(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 1)];
        System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0, clsArr, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
        clsArr[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length] = ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
        return clsArr;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitPushInstruction(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitLoadClass(this);
        visitor.visitCPInstruction(this);
        visitor.visitFieldOrMethod(this);
        visitor.visitFieldInstruction(this);
        visitor.visitGETSTATIC(this);
    }
}
