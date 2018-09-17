package java.lang.reflect;

public interface ParameterizedType extends Type {
    Type[] getActualTypeArguments();

    Type getOwnerType();

    Type getRawType();
}
