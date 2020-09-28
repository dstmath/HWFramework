package com.huawei.gson.internal;

import com.huawei.gson.ExclusionStrategy;
import com.huawei.gson.FieldAttributes;
import com.huawei.gson.Gson;
import com.huawei.gson.TypeAdapter;
import com.huawei.gson.TypeAdapterFactory;
import com.huawei.gson.annotations.Expose;
import com.huawei.gson.annotations.Since;
import com.huawei.gson.annotations.Until;
import com.huawei.gson.reflect.TypeToken;
import com.huawei.gson.stream.JsonReader;
import com.huawei.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Excluder implements TypeAdapterFactory, Cloneable {
    public static final Excluder DEFAULT = new Excluder();
    private static final double IGNORE_VERSIONS = -1.0d;
    private List<ExclusionStrategy> deserializationStrategies = Collections.emptyList();
    private int modifiers = 136;
    private boolean requireExpose;
    private List<ExclusionStrategy> serializationStrategies = Collections.emptyList();
    private boolean serializeInnerClasses = true;
    private double version = IGNORE_VERSIONS;

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public Excluder clone() {
        try {
            return (Excluder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public Excluder withVersion(double ignoreVersionsAfter) {
        Excluder result = clone();
        result.version = ignoreVersionsAfter;
        return result;
    }

    public Excluder withModifiers(int... modifiers2) {
        Excluder result = clone();
        result.modifiers = 0;
        int length = modifiers2.length;
        for (int i = 0; i < length; i++) {
            result.modifiers |= modifiers2[i];
        }
        return result;
    }

    public Excluder disableInnerClassSerialization() {
        Excluder result = clone();
        result.serializeInnerClasses = false;
        return result;
    }

    public Excluder excludeFieldsWithoutExposeAnnotation() {
        Excluder result = clone();
        result.requireExpose = true;
        return result;
    }

    public Excluder withExclusionStrategy(ExclusionStrategy exclusionStrategy, boolean serialization, boolean deserialization) {
        Excluder result = clone();
        if (serialization) {
            result.serializationStrategies = new ArrayList(this.serializationStrategies);
            result.serializationStrategies.add(exclusionStrategy);
        }
        if (deserialization) {
            result.deserializationStrategies = new ArrayList(this.deserializationStrategies);
            result.deserializationStrategies.add(exclusionStrategy);
        }
        return result;
    }

    @Override // com.huawei.gson.TypeAdapterFactory
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
        Class<?> rawType = type.getRawType();
        boolean excludeClass = excludeClassChecks(rawType);
        final boolean skipDeserialize = false;
        final boolean skipSerialize = excludeClass || excludeClassInStrategy(rawType, true);
        if (excludeClass || excludeClassInStrategy(rawType, false)) {
            skipDeserialize = true;
        }
        if (skipSerialize || skipDeserialize) {
            return new TypeAdapter<T>() {
                /* class com.huawei.gson.internal.Excluder.AnonymousClass1 */
                private TypeAdapter<T> delegate;

                @Override // com.huawei.gson.TypeAdapter
                public T read(JsonReader in) throws IOException {
                    if (!skipDeserialize) {
                        return (T) delegate().read(in);
                    }
                    in.skipValue();
                    return null;
                }

                @Override // com.huawei.gson.TypeAdapter
                public void write(JsonWriter out, T value) throws IOException {
                    if (skipSerialize) {
                        out.nullValue();
                    } else {
                        delegate().write(out, value);
                    }
                }

                private TypeAdapter<T> delegate() {
                    TypeAdapter<T> d = this.delegate;
                    if (d != null) {
                        return d;
                    }
                    TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(Excluder.this, type);
                    this.delegate = delegateAdapter;
                    return delegateAdapter;
                }
            };
        }
        return null;
    }

    public boolean excludeField(Field field, boolean serialize) {
        Expose annotation;
        if ((this.modifiers & field.getModifiers()) != 0) {
            return true;
        }
        if (!(this.version == IGNORE_VERSIONS || isValidVersion((Since) field.getAnnotation(Since.class), (Until) field.getAnnotation(Until.class))) || field.isSynthetic()) {
            return true;
        }
        if (this.requireExpose && ((annotation = (Expose) field.getAnnotation(Expose.class)) == null || (!serialize ? !annotation.deserialize() : !annotation.serialize()))) {
            return true;
        }
        if ((!this.serializeInnerClasses && isInnerClass(field.getType())) || isAnonymousOrLocal(field.getType())) {
            return true;
        }
        List<ExclusionStrategy> list = serialize ? this.serializationStrategies : this.deserializationStrategies;
        if (list.isEmpty()) {
            return false;
        }
        FieldAttributes fieldAttributes = new FieldAttributes(field);
        for (ExclusionStrategy exclusionStrategy : list) {
            if (exclusionStrategy.shouldSkipField(fieldAttributes)) {
                return true;
            }
        }
        return false;
    }

    private boolean excludeClassChecks(Class<?> clazz) {
        if (this.version != IGNORE_VERSIONS && !isValidVersion((Since) clazz.getAnnotation(Since.class), (Until) clazz.getAnnotation(Until.class))) {
            return true;
        }
        if ((this.serializeInnerClasses || !isInnerClass(clazz)) && !isAnonymousOrLocal(clazz)) {
            return false;
        }
        return true;
    }

    public boolean excludeClass(Class<?> clazz, boolean serialize) {
        return excludeClassChecks(clazz) || excludeClassInStrategy(clazz, serialize);
    }

    private boolean excludeClassInStrategy(Class<?> clazz, boolean serialize) {
        for (ExclusionStrategy exclusionStrategy : serialize ? this.serializationStrategies : this.deserializationStrategies) {
            if (exclusionStrategy.shouldSkipClass(clazz)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnonymousOrLocal(Class<?> clazz) {
        return !Enum.class.isAssignableFrom(clazz) && (clazz.isAnonymousClass() || clazz.isLocalClass());
    }

    private boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !isStatic(clazz);
    }

    private boolean isStatic(Class<?> clazz) {
        return (clazz.getModifiers() & 8) != 0;
    }

    private boolean isValidVersion(Since since, Until until) {
        return isValidSince(since) && isValidUntil(until);
    }

    private boolean isValidSince(Since annotation) {
        if (annotation == null || annotation.value() <= this.version) {
            return true;
        }
        return false;
    }

    private boolean isValidUntil(Until annotation) {
        if (annotation == null || annotation.value() > this.version) {
            return true;
        }
        return false;
    }
}
