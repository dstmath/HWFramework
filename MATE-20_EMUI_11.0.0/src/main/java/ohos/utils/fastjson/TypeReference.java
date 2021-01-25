package ohos.utils.fastjson;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ohos.utils.fastjson.util.ParameterizedTypeImpl;
import ohos.utils.fastjson.util.TypeUtils;

public class TypeReference<T> {
    static ConcurrentMap<Type, Type> classTypeCache = new ConcurrentHashMap(16, 0.75f, 1);
    protected final Type type;

    protected TypeReference() {
        Type type2 = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        if (type2 instanceof Class) {
            this.type = type2;
            return;
        }
        Type type3 = classTypeCache.get(type2);
        if (type3 == null) {
            classTypeCache.putIfAbsent(type2, type2);
            type3 = classTypeCache.get(type2);
        }
        this.type = type3;
    }

    protected TypeReference(Type... typeArr) {
        Class<?> cls = getClass();
        ParameterizedType parameterizedType = (ParameterizedType) ((ParameterizedType) cls.getGenericSuperclass()).getActualTypeArguments()[0];
        Type rawType = parameterizedType.getRawType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        int i = 0;
        for (int i2 = 0; i2 < actualTypeArguments.length; i2++) {
            if ((actualTypeArguments[i2] instanceof TypeVariable) && i < typeArr.length) {
                actualTypeArguments[i2] = typeArr[i];
                i++;
            }
            if (actualTypeArguments[i2] instanceof GenericArrayType) {
                actualTypeArguments[i2] = TypeUtils.checkPrimitiveArray((GenericArrayType) actualTypeArguments[i2]);
            }
            if (actualTypeArguments[i2] instanceof ParameterizedType) {
                actualTypeArguments[i2] = handlerParameterizedType((ParameterizedType) actualTypeArguments[i2], typeArr, i);
            }
        }
        ParameterizedTypeImpl parameterizedTypeImpl = new ParameterizedTypeImpl(actualTypeArguments, cls, rawType);
        Type type2 = classTypeCache.get(parameterizedTypeImpl);
        if (type2 == null) {
            classTypeCache.putIfAbsent(parameterizedTypeImpl, parameterizedTypeImpl);
            type2 = classTypeCache.get(parameterizedTypeImpl);
        }
        this.type = type2;
    }

    private Type handlerParameterizedType(ParameterizedType parameterizedType, Type[] typeArr, int i) {
        Class<?> cls = getClass();
        Type rawType = parameterizedType.getRawType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        for (int i2 = 0; i2 < actualTypeArguments.length; i2++) {
            if ((actualTypeArguments[i2] instanceof TypeVariable) && i < typeArr.length) {
                actualTypeArguments[i2] = typeArr[i];
                i++;
            }
            if (actualTypeArguments[i2] instanceof GenericArrayType) {
                actualTypeArguments[i2] = TypeUtils.checkPrimitiveArray((GenericArrayType) actualTypeArguments[i2]);
            }
            if (actualTypeArguments[i2] instanceof ParameterizedType) {
                return handlerParameterizedType((ParameterizedType) actualTypeArguments[i2], typeArr, i);
            }
        }
        return new ParameterizedTypeImpl(actualTypeArguments, cls, rawType);
    }

    public Type getType() {
        return this.type;
    }
}
