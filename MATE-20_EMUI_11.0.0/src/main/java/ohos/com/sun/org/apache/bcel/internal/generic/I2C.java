package ohos.com.sun.org.apache.bcel.internal.generic;

public class I2C extends ConversionInstruction {
    public I2C() {
        super(146);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitConversionInstruction(this);
        visitor.visitI2C(this);
    }
}
