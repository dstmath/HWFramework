package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class BREAKPOINT extends Instruction {
    public BREAKPOINT() {
        super(Constants.BREAKPOINT, 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitBREAKPOINT(this);
    }
}
