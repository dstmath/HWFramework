package ohos.com.sun.org.apache.bcel.internal.generic;

public class I2B extends ConversionInstruction {
    public I2B() {
        super(145);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitConversionInstruction(this);
        visitor.visitI2B(this);
    }
}
