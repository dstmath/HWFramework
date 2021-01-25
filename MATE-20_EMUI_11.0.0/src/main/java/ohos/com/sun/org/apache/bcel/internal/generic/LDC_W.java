package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class LDC_W extends LDC {
    LDC_W() {
    }

    public LDC_W(int i) {
        super(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LDC, ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        setIndex(byteSequence.readUnsignedShort());
        this.opcode = 19;
    }
}
