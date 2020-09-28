package com.huawei.gson.internal;

import com.huawei.gson.InstanceCreator;
import com.huawei.gson.JsonIOException;
import com.huawei.gson.internal.reflect.ReflectionAccessor;
import com.huawei.gson.reflect.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class ConstructorConstructor {
    private final ReflectionAccessor accessor = ReflectionAccessor.getInstance();
    private final Map<Type, InstanceCreator<?>> instanceCreators;

    public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators2) {
        this.instanceCreators = instanceCreators2;
    }

    public <T> ObjectConstructor<T> get(TypeToken<T> typeToken) {
        final Type type = typeToken.getType();
        Class<? super T> rawType = typeToken.getRawType();
        final InstanceCreator<?> instanceCreator = this.instanceCreators.get(type);
        if (instanceCreator != null) {
            return new ObjectConstructor<T>() {
                /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass1 */

                @Override // com.huawei.gson.internal.ObjectConstructor
                public T construct() {
                    return (T) instanceCreator.createInstance(type);
                }
            };
        }
        final InstanceCreator<?> instanceCreator2 = this.instanceCreators.get(rawType);
        if (instanceCreator2 != null) {
            return new ObjectConstructor<T>() {
                /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass2 */

                @Override // com.huawei.gson.internal.ObjectConstructor
                public T construct() {
                    return (T) instanceCreator2.createInstance(type);
                }
            };
        }
        ObjectConstructor<T> defaultConstructor = newDefaultConstructor(rawType);
        if (defaultConstructor != null) {
            return defaultConstructor;
        }
        ObjectConstructor<T> defaultImplementation = newDefaultImplementationConstructor(type, rawType);
        if (defaultImplementation != null) {
            return defaultImplementation;
        }
        return newUnsafeAllocator(type, rawType);
    }

    private <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType) {
        try {
            final Constructor<? super T> constructor = rawType.getDeclaredConstructor(new Class[0]);
            if (!constructor.isAccessible()) {
                this.accessor.makeAccessible(constructor);
            }
            return new ObjectConstructor<T>() {
                /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass3 */

                @Override // com.huawei.gson.internal.ObjectConstructor
                public T construct() {
                    try {
                        return (T) constructor.newInstance(null);
                    } catch (InstantiationException e) {
                        throw new RuntimeException("Failed to invoke " + constructor + " with no args", e);
                    } catch (InvocationTargetException e2) {
                        throw new RuntimeException("Failed to invoke " + constructor + " with no args", e2.getTargetException());
                    } catch (IllegalAccessException e3) {
                        throw new AssertionError(e3);
                    }
                }
            };
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private <T> ObjectConstructor<T> newDefaultImplementationConstructor(final Type type, Class<? super T> rawType) {
        if (Collection.class.isAssignableFrom(rawType)) {
            if (SortedSet.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass4 */

                    @Override // com.huawei.gson.internal.ObjectConstructor
                    public T construct() {
                        return (T) new TreeSet();
                    }
                };
            }
            if (EnumSet.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass5 */

                    @Override // com.huawei.gson.internal.ObjectConstructor
                    public T construct() {
                        Type type = type;
                        if (type instanceof ParameterizedType) {
                            Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                            if (elementType instanceof Class) {
                                return (T) EnumSet.noneOf((Class) elementType);
                            }
                            throw new JsonIOException("Invalid EnumSet type: " + type.toString());
                        }
                        throw new JsonIOException("Invalid EnumSet type: " + type.toString());
                    }
                };
            }
            if (Set.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass6 */

                    @Override // com.huawei.gson.internal.ObjectConstructor
                    public T construct() {
                        return (T) new LinkedHashSet();
                    }
                };
            }
            if (Queue.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass7 */

                    @Override // com.huawei.gson.internal.ObjectConstructor
                    public T construct() {
                        return (T) new ArrayDeque();
                    }
                };
            }
            return new ObjectConstructor<T>() {
                /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass8 */

                @Override // com.huawei.gson.internal.ObjectConstructor
                public T construct() {
                    return (T) new ArrayList();
                }
            };
        } else if (!Map.class.isAssignableFrom(rawType)) {
            return null;
        } else {
            if (ConcurrentNavigableMap.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass9 */

                    @Override // com.huawei.gson.internal.ObjectConstructor
                    public T construct() {
                        return (T) new ConcurrentSkipListMap();
                    }
                };
            }
            if (ConcurrentMap.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass10 */

                    @Override // com.huawei.gson.internal.ObjectConstructor
                    public T construct() {
                        return (T) new ConcurrentHashMap();
                    }
                };
            }
            if (SortedMap.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass11 */

                    @Override // com.huawei.gson.internal.ObjectConstructor
                    public T construct() {
                        return (T) new TreeMap();
                    }
                };
            }
            if (!(type instanceof ParameterizedType) || String.class.isAssignableFrom(TypeToken.get(((ParameterizedType) type).getActualTypeArguments()[0]).getRawType())) {
                return new ObjectConstructor<T>() {
                    /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass13 */

                    @Override // com.huawei.gson.internal.ObjectConstructor
                    public T construct() {
                        return (T) new LinkedTreeMap();
                    }
                };
            }
            return new ObjectConstructor<T>() {
                /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass12 */

                @Override // com.huawei.gson.internal.ObjectConstructor
                public T construct() {
                    return (T) new LinkedHashMap();
                }
            };
        }
    }

    private <T> ObjectConstructor<T> newUnsafeAllocator(final Type type, final Class<? super T> rawType) {
        return new ObjectConstructor<T>() {
            /* class com.huawei.gson.internal.ConstructorConstructor.AnonymousClass14 */
            private final UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();

            @Override // com.huawei.gson.internal.ObjectConstructor
            public T construct() {
                try {
                    return (T) this.unsafeAllocator.newInstance(rawType);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to invoke no-args constructor for " + type + ". Registering an InstanceCreator with Gson for this type may fix this problem.", e);
                }
            }
        };
    }

    public String toString() {
        return this.instanceCreators.toString();
    }
}
