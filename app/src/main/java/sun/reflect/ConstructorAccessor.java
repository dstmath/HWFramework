package sun.reflect;

import java.lang.reflect.InvocationTargetException;

public interface ConstructorAccessor {
    Object newInstance(Object[] objArr) throws InstantiationException, IllegalArgumentException, InvocationTargetException;
}
