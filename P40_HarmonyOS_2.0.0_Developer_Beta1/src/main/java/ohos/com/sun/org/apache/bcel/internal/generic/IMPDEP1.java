package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class IMPDEP1 extends Instruction {
    public IMPDEP1() {
        super(Constants.IMPDEP1, 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitIMPDEP1(this);
    }
}
