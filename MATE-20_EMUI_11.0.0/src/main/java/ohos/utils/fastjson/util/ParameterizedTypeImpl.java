package ohos.utils.fastjson.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ParameterizedTypeImpl implements ParameterizedType {
    private final Type[] actualTypeArguments;
    private final Type ownerType;
    private final Type rawType;

    public ParameterizedTypeImpl(Type[] typeArr, Type type, Type type2) {
        this.actualTypeArguments = typeArr;
        this.ownerType = type;
        this.rawType = type2;
    }

    @Override // java.lang.reflect.ParameterizedType
    public Type[] getActualTypeArguments() {
        return this.actualTypeArguments;
    }

    @Override // java.lang.reflect.ParameterizedType
    public Type getOwnerType() {
        return this.ownerType;
    }

    @Override // java.lang.reflect.ParameterizedType
    public Type getRawType() {
        return this.rawType;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ParameterizedTypeImpl parameterizedTypeImpl = (ParameterizedTypeImpl) obj;
        if (!Arrays.equals(this.actualTypeArguments, parameterizedTypeImpl.actualTypeArguments)) {
            return false;
        }
        Type type = this.ownerType;
        if (type == null ? parameterizedTypeImpl.ownerType != null : !type.equals(parameterizedTypeImpl.ownerType)) {
            return false;
        }
        Type type2 = this.rawType;
        if (type2 != null) {
            return type2.equals(parameterizedTypeImpl.rawType);
        }
        return parameterizedTypeImpl.rawType == null;
    }

    @Override // java.lang.Object
    public int hashCode() {
        Type[] typeArr = this.actualTypeArguments;
        int i = 0;
        int hashCode = (typeArr != null ? Arrays.hashCode(typeArr) : 0) * 31;
        Type type = this.ownerType;
        int hashCode2 = (hashCode + (type != null ? type.hashCode() : 0)) * 31;
        Type type2 = this.rawType;
        if (type2 != null) {
            i = type2.hashCode();
        }
        return hashCode2 + i;
    }
}
