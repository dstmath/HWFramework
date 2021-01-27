package ohos.com.sun.org.apache.bcel.internal.generic;

public interface LoadClass {
    ObjectType getLoadClassType(ConstantPoolGen constantPoolGen);

    Type getType(ConstantPoolGen constantPoolGen);
}
