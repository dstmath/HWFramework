package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class IMPDEP2 extends Instruction {
    public IMPDEP2() {
        super(Constants.IMPDEP2, 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitIMPDEP2(this);
    }
}
