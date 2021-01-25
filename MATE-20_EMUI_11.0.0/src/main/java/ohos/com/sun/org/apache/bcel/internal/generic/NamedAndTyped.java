package ohos.com.sun.org.apache.bcel.internal.generic;

public interface NamedAndTyped {
    String getName();

    Type getType();

    void setName(String str);

    void setType(Type type);
}
