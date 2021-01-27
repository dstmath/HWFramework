package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public class MONITOREXIT extends Instruction implements ExceptionThrower, StackConsumer {
    public MONITOREXIT() {
        super(Constants.MONITOREXIT, 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        return new Class[]{ExceptionConstants.NULL_POINTER_EXCEPTION};
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitExceptionThrower(this);
        visitor.visitStackConsumer(this);
        visitor.visitMONITOREXIT(this);
    }
}
