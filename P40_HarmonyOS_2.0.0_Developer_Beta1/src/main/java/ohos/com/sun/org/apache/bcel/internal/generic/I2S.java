package ohos.com.sun.org.apache.bcel.internal.generic;

public class I2S extends ConversionInstruction {
    public I2S() {
        super(147);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitConversionInstruction(this);
        visitor.visitI2S(this);
    }
}
