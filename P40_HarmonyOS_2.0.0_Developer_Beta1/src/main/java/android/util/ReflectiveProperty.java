package android.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ReflectiveProperty<T, V> extends Property<T, V> {
    private static final String PREFIX_GET = "get";
    private static final String PREFIX_IS = "is";
    private static final String PREFIX_SET = "set";
    private Field mField;
    private Method mGetter;
    private Method mSetter;

    public ReflectiveProperty(Class<T> propertyHolder, Class<V> valueType, String name) {
        super(valueType, name);
        String capitalizedName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            this.mGetter = propertyHolder.getMethod(PREFIX_GET + capitalizedName, null);
        } catch (NoSuchMethodException e) {
            try {
                this.mGetter = propertyHolder.getMethod(PREFIX_IS + capitalizedName, null);
            } catch (NoSuchMethodException e2) {
                this.mField = propertyHolder.getField(name);
                Class fieldType = this.mField.getType();
                if (!typesMatch(valueType, fieldType)) {
                    throw new NoSuchPropertyException("Underlying type (" + fieldType + ") does not match Property type (" + valueType + ")");
                }
                return;
            } catch (NoSuchFieldException e3) {
                throw new NoSuchPropertyException("No accessor method or field found for property with name " + name);
            }
        }
        Class getterType = this.mGetter.getReturnType();
        if (typesMatch(valueType, getterType)) {
            try {
                this.mSetter = propertyHolder.getMethod(PREFIX_SET + capitalizedName, getterType);
            } catch (NoSuchMethodException e4) {
            }
        } else {
            throw new NoSuchPropertyException("Underlying type (" + getterType + ") does not match Property type (" + valueType + ")");
        }
    }

    private boolean typesMatch(Class<V> valueType, Class getterType) {
        if (getterType == valueType) {
            return true;
        }
        if (!getterType.isPrimitive()) {
            return false;
        }
        if (getterType == Float.TYPE && valueType == Float.class) {
            return true;
        }
        if (getterType == Integer.TYPE && valueType == Integer.class) {
            return true;
        }
        if (getterType == Boolean.TYPE && valueType == Boolean.class) {
            return true;
        }
        if (getterType == Long.TYPE && valueType == Long.class) {
            return true;
        }
        if (getterType == Double.TYPE && valueType == Double.class) {
            return true;
        }
        if (getterType == Short.TYPE && valueType == Short.class) {
            return true;
        }
        if (getterType == Byte.TYPE && valueType == Byte.class) {
            return true;
        }
        if (getterType == Character.TYPE && valueType == Character.class) {
            return true;
        }
        return false;
    }

    @Override // android.util.Property
    public void set(T object, V value) {
        Method method = this.mSetter;
        if (method != null) {
            try {
                method.invoke(object, value);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            } catch (InvocationTargetException e2) {
                throw new RuntimeException(e2.getCause());
            }
        } else {
            Field field = this.mField;
            if (field != null) {
                try {
                    field.set(object, value);
                } catch (IllegalAccessException e3) {
                    throw new AssertionError();
                }
            } else {
                throw new UnsupportedOperationException("Property " + getName() + " is read-only");
            }
        }
    }

    @Override // android.util.Property
    public V get(T object) {
        Method method = this.mGetter;
        if (method != null) {
            try {
                return (V) method.invoke(object, null);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            } catch (InvocationTargetException e2) {
                throw new RuntimeException(e2.getCause());
            }
        } else {
            Field field = this.mField;
            if (field != null) {
                try {
                    return (V) field.get(object);
                } catch (IllegalAccessException e3) {
                    throw new AssertionError();
                }
            } else {
                throw new AssertionError();
            }
        }
    }

    @Override // android.util.Property
    public boolean isReadOnly() {
        return this.mSetter == null && this.mField == null;
    }
}
