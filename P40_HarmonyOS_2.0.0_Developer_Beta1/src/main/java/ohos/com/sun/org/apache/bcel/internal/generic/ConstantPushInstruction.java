package ohos.com.sun.org.apache.bcel.internal.generic;

public interface ConstantPushInstruction extends PushInstruction, TypedInstruction {
    Number getValue();
}
