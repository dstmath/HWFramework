package java.lang.reflect;

import java.lang.reflect.GenericDeclaration;

public interface TypeVariable<D extends GenericDeclaration> extends Type {
    Type[] getBounds();

    D getGenericDeclaration();

    String getName();
}
