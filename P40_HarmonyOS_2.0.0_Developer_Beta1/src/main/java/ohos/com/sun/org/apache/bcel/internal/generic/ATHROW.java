package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;

public class ATHROW extends Instruction implements UnconditionalBranch, ExceptionThrower {
    public ATHROW() {
        super(Constants.ATHROW, 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        return new Class[]{ExceptionConstants.THROWABLE};
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitUnconditionalBranch(this);
        visitor.visitExceptionThrower(this);
        visitor.visitATHROW(this);
    }
}
