package java.lang.reflect;

public interface InvocationHandler {
    Object invoke(Object obj, Method method, Object[] objArr) throws Throwable;
}
